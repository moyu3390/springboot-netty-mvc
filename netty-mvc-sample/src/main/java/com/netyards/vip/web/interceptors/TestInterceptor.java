package com.netyards.vip.web.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netyards.nettymvc.annotation.NettyInterceptor;
import com.netyards.nettymvc.bind.NettyMvcInterceptor;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
/**
 * 
 * @author shihu
 * @Date  2019年5月10日
 *
 */
@NettyInterceptor
public class TestInterceptor implements NettyMvcInterceptor{
	 private static Logger logger = LoggerFactory.getLogger(TestInterceptor.class); 
	 public boolean preHandle(FullHttpRequest  request, FullHttpResponse response, Object reqObj) throws Exception { 
		 
		 
		 logger.info("this is test the preHander interceptor");
		 
		 return true;
		 };
	@Override
	public void postHandle(FullHttpRequest request, FullHttpResponse response, Object responseObj) throws Exception {
		// TODO Auto-generated method stub
		logger.info("this is test the postHander interceptor");
	}

}
