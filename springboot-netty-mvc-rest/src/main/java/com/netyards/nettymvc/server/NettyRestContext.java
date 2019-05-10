package com.netyards.nettymvc.server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netyards.nettymvc.annotation.NettyInterceptor;
import com.netyards.nettymvc.annotation.NettyListener;
import com.netyards.nettymvc.annotation.NettyRequestMapping;
import com.netyards.nettymvc.bind.NettyAppListener;
import com.netyards.nettymvc.bind.NettyMvcInterceptor;

import io.netty.handler.codec.http.HttpMethod;
/**
 * 
 * @author shihu
 * @Date  2019年5月10日
 *
 */
public class NettyRestContext {
	private static Logger logger = LoggerFactory.getLogger(NettyRestContext.class);
	private final static Map<String, Map<String, Entry<Method, Object>>> requestMap = new HashMap<>();
	private final static List<NettyMvcInterceptorWrapper> intercepters = new ArrayList<>();
	private final static List<NettyListenerWrapper> listeners = new ArrayList<>();
	
	public  Entry<Method, Object> getRequestMethod(String url, HttpMethod mehod) {
		return requestMap.get(url).get(mehod.name());
	}
	public Map<String, Map<String, Entry<Method, Object>>> getRequestmap() {
		return requestMap;
	}
	public  List<NettyMvcInterceptorWrapper> getIntercepters() {
		return intercepters;
	}
	public  List<NettyListenerWrapper> getListeners() {
		return listeners;
	}
	
	public void initRequestMap(Map<String, Object> objs) {
		for(Object bean:objs.values()){  
			 
			 logger.info("the controller name-"+bean.getClass().getName());
			Method[] methods =  bean.getClass().getDeclaredMethods();
			logger.info("the controller method size-"+methods.length);
			for(int i=0 ; i< methods.length; i++) {
				Method reqMethodObj = methods[i];
				NettyRequestMapping mapping = reqMethodObj.getAnnotation(NettyRequestMapping.class);
				
				
				if(mapping == null) {
						 Class<?>[] superclasses = bean.getClass().getInterfaces();
						 for( Class<?> superclass: superclasses) { 
							 try {
							 Method supperMethodObj = superclass.getDeclaredMethod(reqMethodObj.getName(), reqMethodObj.getParameterTypes());
							 mapping = supperMethodObj.getAnnotation(NettyRequestMapping.class);
							 //logger.info("interface Annotation path"+ mapping.value());
							 } catch (NoSuchMethodException e) {
								 // TODO Auto-generated catch block
								 mapping= null;
							 } catch (SecurityException e) {
								 // TODO Auto-generated catch block
								 mapping=null;
							 }
							 if(mapping != null) {
								 break;
							 }
						 }
				}
				
				if(mapping != null) {
					String reqHttpPath = mapping.value();
					String reqHttpMethod = mapping.method();
					logger.info("init maping url--"+reqHttpPath+"-----"+reqHttpMethod);
					Map<String, Entry<Method, Object>> map = new HashMap<>();
					Entry<Method,Object> entry = new Node<Method,Object>(reqMethodObj, bean);
					map.put(reqHttpMethod, entry);
					requestMap.put(reqHttpPath, map);
				}
				
			}
			 
		 }
	}
	
	
	
	public void initNettyMvcInterceptorWrapper(Map<String, Object> objs) {
		for(Object bean:objs.values()){  
			if(bean instanceof NettyMvcInterceptor) {
				
				NettyInterceptor nettyInterceptor = bean.getClass().getAnnotation(NettyInterceptor.class);
				if(nettyInterceptor != null) {
					intercepters.add(new NettyMvcInterceptorWrapper(nettyInterceptor.sort(), (NettyMvcInterceptor)bean));
				}
			}
		}
		
		Collections.sort(intercepters);
	}
	
	
	
	public void initNettyListenerWrapper(Map<String, Object> objs) {
		for(Object bean:objs.values()){  
			if(bean instanceof NettyAppListener) {
				
				NettyListener nettyListener = bean.getClass().getAnnotation(NettyListener.class);
				if(nettyListener != null) {
					listeners.add(new NettyListenerWrapper(bean.getClass().getName(), nettyListener.sort(), (NettyAppListener)bean));
					
				}
			}
		}
		
		Collections.sort(listeners);
	}
	
	
	







	static class Node<K,V> implements Map.Entry<K,V> {
	      
	        final K key;
	        V value;
	       

	        Node( K key, V value) {
	        
	            this.key = key;
	            this.value = value;
	        }

	        public final K getKey()        { return key; }
	        public final V getValue()      { return value; }
	        public final String toString() { return key + "=" + value; }

	        public final int hashCode() {
	            return Objects.hashCode(key) ^ Objects.hashCode(value);
	        }

	        public final V setValue(V newValue) {
	            V oldValue = value;
	            value = newValue;
	            return oldValue;
	        }

	        public final boolean equals(Object o) {
	            if (o == this)
	                return true;
	            if (o instanceof Map.Entry) {
	                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
	                if (Objects.equals(key, e.getKey()) &&
	                    Objects.equals(value, e.getValue()))
	                    return true;
	            }
	            return false;
	        }
	    }
	
}

