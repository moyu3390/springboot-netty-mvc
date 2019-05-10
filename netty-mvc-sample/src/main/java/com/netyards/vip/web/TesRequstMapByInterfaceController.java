package com.netyards.vip.web;

import com.netyards.nettymvc.annotation.NettyRestController;
/**
 * 
 * @author shihu
 * @Date  2019年5月10日
 *
 */
@NettyRestController
public class TesRequstMapByInterfaceController implements TestRequstMapInterfaceAPI {


public TestModel abc() {
	TestModel test = new TestModel();
	test.setId(23);
	test.setName("this is a test");
	test.setTest("test return is object");
	return test;
}


public String abcStr() {
	return "this is a test";
}

}
