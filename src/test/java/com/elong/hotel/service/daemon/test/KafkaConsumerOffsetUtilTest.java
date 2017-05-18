package com.elong.hotel.service.daemon.test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.symantec.cpe.analytics.KafkaMonitorConfiguration;
import com.symantec.cpe.analytics.core.kafka.KafkaOffsetMonitor;
import com.symantec.cpe.analytics.core.managed.ZKClient;
import com.symantec.cpe.analytics.kafka.KafkaConsumerOffsetUtil;

/**
 * @author YiKangFeng.
 */
public class KafkaConsumerOffsetUtilTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		KafkaMonitorConfiguration c = new KafkaMonitorConfiguration();
		c.setZookeeperUrls("192.168.35.57:2181,192.168.35.58:2181");
		ZKClient zkclient = new ZKClient(c);
		try {
			zkclient.start();

			while (true) {
				KafkaConsumerOffsetUtil util = KafkaConsumerOffsetUtil.getInstance(zkclient);
				List<KafkaOffsetMonitor> l = util.getRegularKafkaOffsetMonitors("soa_monitor_consumer",
						"soa_client_apilog");

				for (KafkaOffsetMonitor m : l)
					System.out.println(
							m.getConsumerGroupName() + m.getTopic() + m.getLogSize() + m.getLag() + m.getOwner());

				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (final Throwable t) {

				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
