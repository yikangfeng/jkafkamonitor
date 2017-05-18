package com.elong.hotel.service.daemon.jobs;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.symantec.cpe.analytics.core.managed.ZKClient;

/**
 * @author YiKangFeng.
 */
public abstract class AbstractJob {
	@Autowired
	protected SchedulerFactoryBean schedulerFactoryBean;
	ZKClient zkClient;
	protected String ALERT_MSG_TEMPLATE;

	public AbstractJob() {

	}

	protected void register() throws SchedulerException {
		JobDetail jobDetail = JobBuilder.newJob(jobClass()).withIdentity(jobName(), jobGroup()).build();
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression());
		CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerName(), triggerGroup())
				.withSchedule(scheduleBuilder).build();
		schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, cronTrigger);
	}

	abstract protected Class<? extends Job> jobClass();

	abstract protected String jobName();

	abstract protected String jobGroup();

	abstract protected String cronExpression();

	abstract protected String triggerName();

	abstract protected String triggerGroup();

}
