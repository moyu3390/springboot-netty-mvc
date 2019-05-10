package com.netyards.nettymvc.bind;

import io.netty.handler.codec.http.HttpResponseStatus;
/**
 * 
 * @author shihu
 * @Date  2019年5月10日
 *
 */
public abstract class NettyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3912856405232240196L;


	public NettyException(){
		super();
	}
	public NettyException(String message) {
		super(message);
		
	}

	public NettyException(String message, Throwable cause) {
		super(message, cause);

	}

	public NettyException(Throwable cause) {
		super(cause);

	}
 
	protected NettyException(String message, Throwable cause,
             boolean enableSuppression,
             boolean writableStackTrace) {
	 super(message, cause, enableSuppression, writableStackTrace);
 }

	public abstract HttpResponseStatus getResponseStatus();

	public  abstract String getExceptionJsonMsg();

}
