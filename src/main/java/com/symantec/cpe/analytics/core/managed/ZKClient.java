package com.symantec.cpe.analytics.core.managed;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.symantec.cpe.analytics.KafkaMonitorConfiguration;
import com.symantec.cpe.analytics.core.kafka.Broker;
import com.symantec.cpe.analytics.core.kafka.KafkaConsumerGroupMetadata;
import com.symantec.cpe.analytics.kafka.KafkaConsumerOffsetUtil;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.retry.RetryNTimes;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class ZKClient {
	private KafkaMonitorConfiguration kafkaConfiguration;
	private RetryPolicy retryPolicy = new RetryNTimes(3, 1000);
	private CuratorFramework client;
	private static final List<String> nonSpoutConsumerNodes = Arrays.asList("storm", "config", "consumers",
			"controller_epoch", "zookeeper", "admin", "controller", "brokers", "new add after", "test",
			"isr_change_notification", "hbase", "admin", "zookeeper", "hotel");

	public ZKClient(KafkaMonitorConfiguration kafkaConfiguration) {
		this.kafkaConfiguration = kafkaConfiguration;
	}

	public void start() throws Exception {
		Builder builer = CuratorFrameworkFactory.builder().connectString(kafkaConfiguration.getZookeeperUrls())
				.retryPolicy(retryPolicy).connectionTimeoutMs(2000).sessionTimeoutMs(2000);
		client = builer.build();
		client.start();
	}

	public void stop() throws Exception {
		client.close();
		KafkaConsumerOffsetUtil.closeConnection();
	}

	public List<KafkaConsumerGroupMetadata> getActiveRegularConsumersAndTopic(final String consumerGroup,
			final String topic) throws Exception {
		if (consumerGroup == null || consumerGroup.isEmpty())
			return Collections.<KafkaConsumerGroupMetadata> emptyList();

		List<KafkaConsumerGroupMetadata> kafkaConsumerGroupMetadataList = new ArrayList<KafkaConsumerGroupMetadata>();
		if (client.checkExists().forPath("/consumers/" + consumerGroup + "/offsets") != null) {
			if (client.checkExists().forPath("/consumers/" + consumerGroup + "/offsets/" + topic) != null) {
				List<String> partitions = client.getChildren()
						.forPath("/consumers/" + consumerGroup + "/offsets/" + topic);
				Map<String, Long> partitionOffsetMap = new HashMap<String, Long>();
				for (String partition : partitions) {
					byte[] data = client.getData()
							.forPath("/consumers/" + consumerGroup + "/offsets/" + topic + "/" + partition);
					if (data != null) {
						long offset = Long.parseLong(new String(data));
						partitionOffsetMap.put(partition, offset);
					}
				}
				KafkaConsumerGroupMetadata kafkaConsumerGroupMetadata = new KafkaConsumerGroupMetadata(consumerGroup,
						topic, partitionOffsetMap);
				kafkaConsumerGroupMetadataList.add(kafkaConsumerGroupMetadata);
			}
		}
		return kafkaConsumerGroupMetadataList;
	}

	public List<KafkaConsumerGroupMetadata> getActiveRegularConsumersAndTopics() throws Exception {
		List<KafkaConsumerGroupMetadata> kafkaConsumerGroupMetadataList = new ArrayList<KafkaConsumerGroupMetadata>();
		Set<String> consumerGroups = new HashSet<String>((client.getChildren().forPath("/consumers")));
		for (String consumerGroup : consumerGroups) {
			if (client.checkExists().forPath("/consumers/" + consumerGroup + "/offsets") != null) {
				List<String> topics = client.getChildren().forPath("/consumers/" + consumerGroup + "/offsets");
				for (String topic : topics) {
					List<String> partitions = client.getChildren()
							.forPath("/consumers/" + consumerGroup + "/offsets/" + topic);
					Map<String, Long> partitionOffsetMap = new HashMap<String, Long>();
					for (String partition : partitions) {
						byte[] data = client.getData()
								.forPath("/consumers/" + consumerGroup + "/offsets/" + topic + "/" + partition);
						if (data != null) {
							long offset = Long.parseLong(new String(data));
							partitionOffsetMap.put(partition, offset);
						}
					}
					KafkaConsumerGroupMetadata kafkaConsumerGroupMetadata = new KafkaConsumerGroupMetadata(
							consumerGroup, topic, partitionOffsetMap);
					kafkaConsumerGroupMetadataList.add(kafkaConsumerGroupMetadata);
				}
			}
		}
		return kafkaConsumerGroupMetadataList;
	}

	public List<String> getActiveSpoutConsumerGroups() throws Exception {
		List<String> rootChildren = (client.getChildren().forPath("/"));
		List<String> activeSpoutConsumerGroupList = new ArrayList<String>();
		for (String rootChild : rootChildren) {
			if (!nonSpoutConsumerNodes.contains(rootChild)) {
				activeSpoutConsumerGroupList.add(rootChild);
			}
		}
		return activeSpoutConsumerGroupList;
	}

	public List<String> getChildren(String path) throws Exception {
		return client.getChildren().forPath(path);
	}

	public byte[] getData(String path) throws Exception {
		return client.getData().forPath(path);
	}

	static public final String BrokerIdsPath = "/brokers/ids";
	static public final String BrokerTopicsPath = "/brokers/topics";

	public String getOwner(final String consumerGroup, final String topic, final int pid) throws Exception {
		byte[] data = client.getData()
				.forPath("/consumers/" + consumerGroup + "/owners/" + topic + "/" + String.valueOf(pid));
		if (data == null || data.length == 0)
			return "";
		return new String(data, "UTF-8");
	}

	public long getZKOffset(final String consumerGroup, final String topic, final int pid) throws Exception {
		byte[] data = client.getData()
				.forPath("/consumers/" + consumerGroup + "/offsets/" + topic + "/" + String.valueOf(pid));
		if (data == null || data.length == 0)
			return 0;
		return Long.parseLong(new String(data, "UTF-8"));
	}

	public Integer getLeaderForPartition(final String topic, final int pid) throws Exception {
		byte[] data = client.getData()
				.forPath(BrokerTopicsPath + "/" + topic + "/partitions" + "/" + pid + "/" + "state");
		if (data == null || data.length == 0)
			return null;
		String leaderOfPartition = new String(data, "UTF-8");
		JSONObject jsonObject = null;
		Integer leader = null;
		try {
			jsonObject = JSONObject.parseObject(leaderOfPartition);
			leader = jsonObject.getInteger("leader");
		} catch (final Throwable ignored) {
			return null;
		} finally {
			if (jsonObject != null) {
				jsonObject.clear();
				jsonObject = null;// Help GC.
			}
		}
		return leader;
	}

	public Map<String, Map<Integer, List<Integer>>> getPartitionsForTopic(final String topic) throws Exception {
		Map<String, Map<Integer, List<Integer>>> topicPidMap = new HashMap<>();

		if (topic == null || topic.isEmpty())
			return topicPidMap;

		byte[] data = client.getData().forPath(BrokerTopicsPath + "/" + topic);
		if (data == null || data.length == 0)
			return null;

		String partitions = new String(data, "UTF-8");
		JSONObject jsonObject = null;

		try {
			jsonObject = JSONObject.parseObject(partitions);
			Map<Integer, List<Integer>> pidMap = JSONObject.parseObject(jsonObject.getString("partitions"),
					new TypeReference<Map<Integer, List<Integer>>>() {
					});
			topicPidMap.put(topic, pidMap);
		} catch (final Throwable ignored) {
			return topicPidMap;
		} finally {
			if (jsonObject != null) {
				jsonObject.clear();
				jsonObject = null;// Help GC.
			}
		}

		return topicPidMap;
	}

	public Broker getBrokerInfo(int brokerId) throws Exception {
		if (client.checkExists().forPath(BrokerIdsPath) == null) {
			throw new RuntimeException(BrokerIdsPath);
		}

		return getBrokerHost(getData(BrokerIdsPath + "/" + brokerId), Integer.valueOf(brokerId));
	}

	public List<Broker> getZKBrokerInfo() throws Exception {
		if (client.checkExists().forPath(BrokerIdsPath) == null) {
			throw new RuntimeException(BrokerIdsPath);
		}
		List<Broker> brokers = new LinkedList<Broker>();
		List<String> allBrokersIds = getChildren(BrokerIdsPath);
		if (allBrokersIds != null) {
			for (String brokerId : allBrokersIds) {
				brokers.add(getBrokerHost(getData(BrokerIdsPath + "/" + brokerId), Integer.valueOf(brokerId)));
			}
		}
		if (allBrokersIds != null) {
			allBrokersIds.clear();
			allBrokersIds = null;// Help GC.
		}
		return brokers;
	}

	@SuppressWarnings("unchecked")
	private Broker getBrokerHost(byte[] contents, Integer brokerId) {
		try {
			Map<Object, Object> value = (Map<Object, Object>) JSONObject.parse(new String(contents, "UTF-8"));
			String host = String.class.isInstance(value.get("host")) ? String.class.cast(value.get("host")) : "";
			Integer port = Integer.valueOf(value.get("port").toString());
			return new Broker(host, port, brokerId);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
