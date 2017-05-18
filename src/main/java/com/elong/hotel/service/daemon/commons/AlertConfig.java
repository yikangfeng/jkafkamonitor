package com.elong.hotel.service.daemon.commons;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author YiKangFeng.
 */
@Component
@PropertySource(value = { "classpath:conf/custom/env/public.properties" }, ignoreResourceNotFound = true)
public class AlertConfig {
	@Value("${alert.default.group}")
	private String defaultGroup;
	@Value("${alert.root.url}")
	private String rootUrl;
	@Value("${alert.service.uri}")
	private String serviceUri;
	@Value("${alert.timeout}")
	private int timeout;
	@Value("${alert.port}")
	private int port;

	public String getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(String group) {
		this.defaultGroup = group;
	}

	public String getRootUrl() {
		return rootUrl;
	}

	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}

	public String getServiceUri() {
		return serviceUri;
	}

	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
