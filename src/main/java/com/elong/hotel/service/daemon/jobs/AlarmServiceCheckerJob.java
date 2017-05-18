package com.elong.hotel.service.daemon.jobs;

import java.io.IOException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.elong.hotel.service.daemon.alarm.check.AlertMain;
import com.elong.hotel.service.daemon.commons.GlobalConstants;

/**
 * @author YiKangFeng.
 */
@Component
@DisallowConcurrentExecution
public class AlarmServiceCheckerJob extends AbstractJob implements InitializingBean, Job {
	static private final Logger LOGGER = LoggerFactory.getLogger(AlarmServiceCheckerJob.class);

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		this.register();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub

		try {
			AlertMain.main(new String[] { GlobalConstants.ALARM_SERVICE_CONFIG_FILE_PATH });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("alarm service checker job execute failed,reasons:", e);
		}

	}

	@Override
	protected Class<? extends Job> jobClass() {
		// TODO Auto-generated method stub
		return this.getClass();
	}

	@Override
	protected String jobName() {
		// TODO Auto-generated method stub
		return AlarmServiceCheckerJob.class.getName();
	}

	@Override
	protected String jobGroup() {
		// TODO Auto-generated method stub
		return "ALARM_SERVICE_CHECK_GROUP";
	}

	@Override
	protected String cronExpression() {
		// TODO Auto-generated method stub
		return "0 */1 * * * ?";
	}

	@Override
	protected String triggerName() {
		// TODO Auto-generated method stub
		return "ALARM_SERVICE_CHECK_TRIGGER";
	}

	@Override
	protected String triggerGroup() {
		// TODO Auto-generated method stub
		return "ALARM_SERVICE_CHECK_TRIGGER_GROUP";
	}

}
