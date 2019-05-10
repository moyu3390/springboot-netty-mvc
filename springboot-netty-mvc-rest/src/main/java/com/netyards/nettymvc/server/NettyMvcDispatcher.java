package com.netyards.nettymvc.server;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netyards.nettymvc.annotation.NettyExceptionHandler;
import com.netyards.nettymvc.annotation.NettyRequestBody;
import com.netyards.nettymvc.bind.NettyException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
/**
 * 
 * @author shihu
 * @Date  2019年5月10日
 *
 */
public class NettyMvcDispatcher {
 private static Logger logger = LoggerFactory.getLogger(NettyServerSpringBootWrapper.class);
 private final NettyRestContext context;
 public NettyMvcDispatcher(NettyRestContext context) {
	 this.context = context;
 }

 public void service(FullHttpRequest fullHttpRequest,  FullHttpResponse response) throws Exception {
	 
	 String  uri = URLDecoder.decode(fullHttpRequest.uri(), "UTF-8");
  logger.info("query url--"+ uri+"---method--"+fullHttpRequest.method().name());
  Entry<Method, Object> entry = null;
  try {
	   entry = context.getRequestMethod(uri, fullHttpRequest.method());
	  
  }catch(NullPointerException ex) {
	  entry = null;
  }
	 if(entry == null) {
		 ByteBuf byteBuf = Unpooled.copiedBuffer("Not found the resouce",CharsetUtil.UTF_8);
		 response.content().writeBytes(byteBuf);
		 byteBuf.release();
		 response.setStatus(HttpResponseStatus.NOT_FOUND);
		 return;
	 }
	 
	 
	 
	 Method reqMethod = entry.getKey();
	 Parameter[] parameters = reqMethod.getParameters();
	 Object reqObj = null;
	 Object[] paramObjs =null;
	 if(parameters.length >0) {
		 paramObjs= new Object[parameters.length];
		 for(int i=0; i<parameters.length; i++) {
			 if(parameters[i].getAnnotation(NettyRequestBody.class) != null) {
				 reqObj =  getRequestParams( fullHttpRequest, parameters[i].getType());
				 paramObjs[i] = reqObj;
			 }else {
				 if(parameters[i].getType().isAssignableFrom(FullHttpRequest.class)) {
					 paramObjs[i] = fullHttpRequest;
				 }else if(parameters[i].getType().isAssignableFrom(FullHttpResponse.class)) {
					 paramObjs[i] = response;
				 }
			 }
			 
		 }
		 
		 
	 }
	 
	 List<NettyMvcInterceptorWrapper> wrappers =  context.getIntercepters();
	 for(NettyMvcInterceptorWrapper wraper: wrappers) {
		 if(!wraper.getIntercepter().preHandle(fullHttpRequest, response, reqObj)) {
			 return ;
		 }
	 }
	 Object resultObj =null;
	 try {
		 if(paramObjs != null) {
			 resultObj = reqMethod.invoke(entry.getValue(), paramObjs);
		 }else {
			 resultObj = reqMethod.invoke(entry.getValue());
		 }
		 
	 }catch(Exception  e) {
		
		 if(e.getCause().getClass().getAnnotation(NettyExceptionHandler.class) != null) {
			 Throwable cause = e.getCause();
           if(cause  instanceof NettyException && (((NettyException) cause).getExceptionJsonMsg() != null)) {
        	  ByteBuf byteBuf = Unpooled.copiedBuffer(((NettyException) cause).getExceptionJsonMsg(),CharsetUtil.UTF_8);
  			 response.content().writeBytes(byteBuf);
  			 byteBuf.release();
  			 response.headers().set("Content-Type", "application/json;charset=UTF-8");
  			 if(((NettyException) cause).getResponseStatus() != null){
  				 response.setStatus(((NettyException) cause).getResponseStatus());
  			 }else {
  				 response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
  			 }
  			 logger.error(cause.getMessage(), cause);
  			 return;
		    }
		 }
		 
	
		  throw e;
		 
	 }
	
	 
	 int index = wrappers.size()-1;
	 while(index > -1) {
		 wrappers.get(index).getIntercepter().postHandle(fullHttpRequest, response, resultObj);
		 index--;
	 }
	 
	 
	 if(resultObj==null) {
		 ByteBuf byteBuf = Unpooled.copiedBuffer("Not Content reply for this request",CharsetUtil.UTF_8);
		 response.content().writeBytes(byteBuf);
		 byteBuf.release();
		 response.headers().set("Content-Type", "text/plain;charset=UTF-8");
		 response.setStatus(HttpResponseStatus.NO_CONTENT);
		 return;
	 }else if(resultObj instanceof String) {
		 
		 ByteBuf byteBuf = Unpooled.copiedBuffer((String)resultObj,CharsetUtil.UTF_8);
		 response.content().writeBytes(byteBuf);
		 byteBuf.release();
		 response.headers().set("Content-Type", "text/plain;charset=UTF-8");
		 response.setStatus(HttpResponseStatus.OK);
		 return ;
	 }

	 String resultContent = JSON.toJSONString(resultObj, true);
	 ByteBuf byteBuf = Unpooled.copiedBuffer(resultContent,CharsetUtil.UTF_8);
	 response.content().writeBytes(byteBuf);
	 byteBuf.release();
	 response.headers().set("Content-Type", "application/json;charset=UTF-8");
	 response.setStatus(HttpResponseStatus.OK);
	 
 } 
 
 
 
 
 private <T> T getRequestParams(FullHttpRequest req, Class<T> clazz){
     // 处理get请求  
     if (req.method() == HttpMethod.GET) {
    	 JSONObject jsonObj = new JSONObject();
         QueryStringDecoder decoder = new QueryStringDecoder(req.uri());  
         Map<String, List<String>> parame = decoder.parameters();  
         if(parame.size()<1) {
        	 return null;
         }
         Iterator<Entry<String, List<String>>> iterator = parame.entrySet().iterator();         
         while(iterator.hasNext()){
             Entry<String, List<String>> next = iterator.next();
             jsonObj.put(next.getKey(), next.getValue().get(0));
         }
         
         return jsonObj.toJavaObject(clazz);
         
     }else {
    	 ByteBuf jsonBuf = req.content();
         String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
         if(jsonStr != null && !jsonStr.equals("")) {
        	 return JSON.parseObject(jsonStr, clazz);
         }else {
        	 return null;
         }
         
         
     }
     
     
     
     
     
     
  
     
     
     
      // 处理POST请求  
/*     if (req.method() == HttpMethod.POST) {
         HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(  
                 new DefaultHttpDataFactory(false), req);  
         List<InterfaceHttpData> postData = decoder.getBodyHttpDatas(); //
         for(InterfaceHttpData data:postData){
             if (data.getHttpDataType() == HttpDataType.Attribute) {  
                 MemoryAttribute attribute = (MemoryAttribute) data;  
                 requestParams.put(attribute.getName(), attribute.getValue());
             }
         }
     }
     return requestParams;*/
 }

}
