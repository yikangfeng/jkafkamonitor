package com.elong.hotel.service.daemon.commons;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * @author kangfeng.yi.
 */
@Configuration
public class SchedulerConfig {

	@Autowired
	private SpringJobFactory springJobFactory;

	@Bean
	public Properties quartzProperties() throws IOException {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean
				.setLocation(new ClassPathResource(GlobalConstants.NOT_ENV_RESOURCE_PATH + "/quartz.properties"));
		propertiesFactoryBean.afterPropertiesSet();
		return propertiesFactoryBean.getObject();
	}

	@Bean
	public SchedulerFactoryBean schedulerFactory() throws IOException {
		SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		schedulerFactory.setJobFactory(springJobFactory);
		schedulerFactory.setOverwriteExistingJobs(true);
		schedulerFactory.setQuartzProperties(quartzProperties());
		return schedulerFactory;
	}
}
