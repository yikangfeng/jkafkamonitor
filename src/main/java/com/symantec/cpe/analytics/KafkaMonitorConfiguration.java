package com.symantec.cpe.analytics;

import javax.validation.Valid;

public class KafkaMonitorConfiguration {

	@Valid
	private String zookeeperUrls = "localhost:2181";

	public String getZookeeperUrls() {
		return zookeeperUrls;
	}

	public void setZookeeperUrls(String zookeeperUrls) {
		this.zookeeperUrls = zookeeperUrls;
	}

}
