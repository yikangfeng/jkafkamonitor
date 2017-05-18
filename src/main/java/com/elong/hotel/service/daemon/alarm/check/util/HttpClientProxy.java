/**   
 * @(#)HttpClientProxy.java	2016年9月21日	上午11:24:53	   
 *     
 * Copyrights (C) 2016艺龙旅行网保留所有权利
 */
package com.elong.hotel.service.daemon.alarm.check.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


/**
 * (类型功能说明描述)
 *
 * <p>
 * 修改历史:											<br>  
 * 修改日期    		修改人员   	版本	 		修改内容<br>  
 * -------------------------------------------------<br>  
 * 2016年9月21日 上午11:24:53   dong.tian     1.0    	初始化创建<br>
 * </p> 
 *
 * @author		dong.tian
 * @version		1.0  
 * @since		JDK1.7
 */
public class HttpClientProxy {
	public static final String CHARSET = "UTF-8";

	private static CloseableHttpClient httpClient;
	private static RequestConfig config;
	static {
		// 设置httpclient的连接池大小
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(5);// 连接池最大并发连接数
		cm.setDefaultMaxPerRoute(5);// 单路由最大并发数
		config = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(2000).build();
		httpClient = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(config).disableContentCompression().build();
	}
	
	public static CloseableHttpClient getInstance(){
		return httpClient;
	}
	
	public static RequestConfig getConfig(){
		return config;
	}
}
