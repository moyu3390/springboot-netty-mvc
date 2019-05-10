package com.netyards.vip.web;


import com.netyards.nettymvc.annotation.NettyRequestBody;
import com.netyards.nettymvc.annotation.NettyRequestMapping;
import com.netyards.nettymvc.annotation.NettyRestController;
import com.netyards.nettymvc.bind.NettyHttpMethods;
import com.netyards.vip.web.exception.TestException;
import com.netyards.vip.web.exception.TestModelExceptionHandlers;
/**
 * 
 * @author shihu
 * @Date  2019年5月10日
 *
 */
@NettyRestController
public class TestErrorController {

	@NettyRequestMapping("/testErr")
	public String testError() throws TestException{
		throw TestModelExceptionHandlers.TEST_ERROR.vIPException();
		
		//return "test error";
	}
	
	@NettyRequestMapping(value = "/testmodel", method=NettyHttpMethods.POST)
	public TestModel abc( @NettyRequestBody TestModel test) {
		
		test.setName("this is a test response");
		test.setTest("test return is object");
		return test;
	}
}
