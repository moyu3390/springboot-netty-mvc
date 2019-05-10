package com.netyards.nettymvc.bind;

import com.netyards.nettymvc.config.NettyRestConfigures;
/**
 * 
 * @author shihu
 * @Date  2019年5月10日
 *
 */
public interface NettyAppListener {
	public void created(NettyRestConfigures  configure);
	public void destoryed(NettyRestConfigures  configure);
}
