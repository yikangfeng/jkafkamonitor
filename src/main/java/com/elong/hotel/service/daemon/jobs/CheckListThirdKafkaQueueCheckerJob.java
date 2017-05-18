package com.elong.hotel.service.daemon.jobs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.elong.hotel.service.daemon.commons.GlobalUtils;
import com.elong.hotel.service.daemon.commons.SpringUtils;
import com.symantec.cpe.analytics.core.kafka.KafkaOffsetMonitor;
import com.symantec.cpe.analytics.kafka.KafkaConsumerOffsetUtil;

/**
 * @author YiKangFeng.
 */
@Component
@DisallowConcurrentExecution
@DependsOn({ "alertConfig", "checkListCoreKafkaQueueCheckerJob" })
@PropertySource(value = {
		"classpath:conf/custom/env/kafkaqueuechecker-config.properties" }, ignoreResourceNotFound = true)
public class CheckListThirdKafkaQueueCheckerJob extends AbstractJob implements InitializingBean, Job {
	static private final Logger LOGGER = LoggerFactory.getLogger(CheckListThirdKafkaQueueCheckerJob.class);
	@Value("${checklist.third.kafka.zookeeper.urls}")
	private String zookeeperUrls;
	@Value("${checklist.third.kafka.queue.checker.alert.name}")
	private String alertName;
	@Value("${checklist.third.kafka.queue.checker.threshold}")
	private long checkerThreshold;
	@Value("${checklist.third.kafka.queue.checker.consumer.group}")
	private String checkerConsumerGroup;
	@Value("${checklist.third.kafka.queue.checker.topic}")
	private String checkerTopic;
	private KafkaConsumerOffsetUtil kafkaConsumerOffsetUtil;

	@Autowired
	private CheckListCoreKafkaQueueCheckerJob checkListCoreKafkaQueueCheckerJob;

	public CheckListThirdKafkaQueueCheckerJob() {
		this.ALERT_MSG_TEMPLATE = "%s当前hotel-checklist-kafka_third待消费消息为%d条超过阈值%d";
	}

	@SuppressWarnings("static-access")
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		this.zkClient = checkListCoreKafkaQueueCheckerJob.zkClient;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				this.zkClient.stop();
				this.kafkaConsumerOffsetUtil.closeConnection();
			} catch (Throwable t) {
				// TODO Auto-generated catch block
				LOGGER.error("The current job={} closing zkclient error,reasons", jobName(), t);
			}
		}));
		kafkaConsumerOffsetUtil = KafkaConsumerOffsetUtil.getInstance(this.zkClient);
		this.register();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		List<KafkaOffsetMonitor> offsetMonitorList = null;
		try {
			offsetMonitorList = SpringUtils.getBean(CheckListThirdKafkaQueueCheckerJob.class).kafkaConsumerOffsetUtil
					.getRegularKafkaOffsetMonitors(this.checkerConsumerGroup, this.checkerTopic);
			if (offsetMonitorList == null || offsetMonitorList.isEmpty()) {
				LOGGER.warn("The current job={} by consumer group={} find result is null or empty.", jobName(),
						this.checkerConsumerGroup);
				return;
			}
			long totalLag = offsetMonitorList.stream()
					.filter(kafkaOffsetMonitor -> kafkaOffsetMonitor.getTopic().equalsIgnoreCase(this.checkerTopic))
					.mapToLong(KafkaOffsetMonitor::getLag).reduce(0, (x, y) -> x + y);
			if (totalLag > this.checkerThreshold) {
				GlobalUtils.sendMsg(this.alertName, String.format(this.ALERT_MSG_TEMPLATE,
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), totalLag, checkerThreshold));
			} else {
				LOGGER.info(String.format("%s当前hotel-checklist-kafka_third %d条小于阈值%d",
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), totalLag, checkerThreshold));
			}
		} catch (Throwable t) {
			// TODO Auto-generated catch block
			LOGGER.error("The current job={} hotel-checklist-kafka_third queue checker job execute failed,reasons:",
					jobName(), t);
		} finally {
			if (offsetMonitorList != null) {
				offsetMonitorList.clear();
				offsetMonitorList = null;// Help GC.
			}
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
		return CheckListThirdKafkaQueueCheckerJob.class.getName();
	}

	@Override
	protected String jobGroup() {
		// TODO Auto-generated method stub
		return "CHECKLIST_THIRD_QUEUE_CHECK_GROUP";
	}

	@Override
	protected String cronExpression() {
		// TODO Auto-generated method stub
		return "0 */1 * * * ?";
	}

	@Override
	protected String triggerName() {
		// TODO Auto-generated method stub
		return "CHECKLIST_THIRD_QUEUE_CHECK_TRIGGER";
	}

	@Override
	protected String triggerGroup() {
		// TODO Auto-generated method stub
		return "CHECKLIST_THIRD_QUEUE_CHECK_TRIGGER_GROUP";
	}

}
