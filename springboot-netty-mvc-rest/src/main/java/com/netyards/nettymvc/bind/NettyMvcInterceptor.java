package com.netyards.nettymvc.bind;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
/**
 * 
 * @author shihu
 * @Date  2019年5月10日
 *
 */
public interface NettyMvcInterceptor {
	
	 default  boolean preHandle(FullHttpRequest  request, FullHttpResponse response, Object reqObj)throws Exception { return true;};

	/**
	 * This implementation is empty.
	 */
	
	 public void postHandle(FullHttpRequest  request, FullHttpResponse response, Object responseObj) throws Exception;
}
