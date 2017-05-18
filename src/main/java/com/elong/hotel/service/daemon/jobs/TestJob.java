package com.elong.hotel.service.daemon.jobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author YiKangFeng.
 */
@Component
@DisallowConcurrentExecution
public class TestJob extends AbstractJob implements InitializingBean, Job {
	static private final Logger LOGGER = LoggerFactory.getLogger(TestJob.class);

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		this.register();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		LOGGER.info("test job executed.");
	}

	@Override
	protected Class<? extends Job> jobClass() {
		// TODO Auto-generated method stub
		return this.getClass();
	}

	@Override
	protected String jobName() {
		// TODO Auto-generated method stub
		return TestJob.class.getName();
	}

	@Override
	protected String jobGroup() {
		// TODO Auto-generated method stub
		return "TEST_JOB_CHECK_GROUP";
	}

	@Override
	protected String cronExpression() {
		// TODO Auto-generated method stub
		return "0 */2 * * * ?";
	}

	@Override
	protected String triggerName() {
		// TODO Auto-generated method stub
		return "TEST_JOB_CHECK_TRIGGER";
	}

	@Override
	protected String triggerGroup() {
		// TODO Auto-generated method stub
		return "TEST_JOB_CHECK_TRIGGER_GROUP";
	}

}
