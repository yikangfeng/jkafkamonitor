package com.symantec.cpe.analytics.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.symantec.cpe.analytics.core.kafka.Broker;
import com.symantec.cpe.analytics.core.kafka.KafkaConsumerGroupMetadata;
import com.symantec.cpe.analytics.core.kafka.KafkaOffsetMonitor;
import com.symantec.cpe.analytics.core.kafka.KafkaSpoutMetadata;
import com.symantec.cpe.analytics.core.kafka.TopicPartitionLeader;
import com.symantec.cpe.analytics.core.managed.ZKClient;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class KafkaConsumerOffsetUtil {
	private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerOffsetUtil.class);
	private static final Map<String, SimpleConsumer> consumerMap = new HashMap<String, SimpleConsumer>();
	private static final String clientName = "ServiceDaemonConsumerOffsetChecker";
	private final ZKClient zkClient;

	public static KafkaConsumerOffsetUtil getInstance(ZKClient zkClient) {
		return new KafkaConsumerOffsetUtil(zkClient);
	}

	private KafkaConsumerOffsetUtil(ZKClient zkClient) {
		this.zkClient = zkClient;
	}

	public List<KafkaOffsetMonitor> getSpoutKafkaOffsetMonitors(final String consumerGroup) throws Exception {
		List<KafkaOffsetMonitor> kafkaOffsetMonitors = new ArrayList<KafkaOffsetMonitor>();
		List<String> partitions = new ArrayList<String>();

		try {
			partitions = zkClient.getChildren("/" + consumerGroup);
		} catch (Exception e) {
			LOG.error("Error while listing partitions for the consumer group: " + consumerGroup);
		}
		for (String partition : partitions) {
			byte[] byteData = zkClient.getData("/" + consumerGroup + "/" + partition);
			String data = "";
			if (byteData != null) {
				data = new String(byteData);
			}
			if (!data.trim().isEmpty()) {
				KafkaSpoutMetadata kafkaSpoutMetadata = new ObjectMapper().readValue(data, KafkaSpoutMetadata.class);
				SimpleConsumer consumer = getConsumer(kafkaSpoutMetadata.getBroker().getHost(),
						kafkaSpoutMetadata.getBroker().getPort(), clientName);
				long realOffset = getLastOffset(consumer, kafkaSpoutMetadata.getTopic(),
						kafkaSpoutMetadata.getPartition(), -1, clientName);
				long lag = realOffset - kafkaSpoutMetadata.getOffset();
				KafkaOffsetMonitor kafkaOffsetMonitor = new KafkaOffsetMonitor(consumerGroup,
						kafkaSpoutMetadata.getTopic(), kafkaSpoutMetadata.getPartition(), realOffset,
						kafkaSpoutMetadata.getOffset(), lag, "");
				kafkaOffsetMonitors.add(kafkaOffsetMonitor);
			}
		}

		return kafkaOffsetMonitors;
	}

	public List<KafkaOffsetMonitor> getRegularKafkaOffsetMonitors(final String consumerGroup, final String topic)
			throws Exception {
		List<KafkaOffsetMonitor> kafkaOffsetMonitors = new ArrayList<KafkaOffsetMonitor>();
		if (consumerGroup == null || consumerGroup.isEmpty())
			return kafkaOffsetMonitors;

		List<KafkaConsumerGroupMetadata> kafkaConsumerGroupMetadataList = zkClient
				.getActiveRegularConsumersAndTopic(consumerGroup, topic);
		if (kafkaConsumerGroupMetadataList == null || kafkaConsumerGroupMetadataList.isEmpty())
			return kafkaOffsetMonitors;

		SimpleConsumer consumer = null;
		for (KafkaConsumerGroupMetadata kafkaConsumerGroupMetadata : kafkaConsumerGroupMetadataList) {

			List<TopicPartitionLeader> partitions = getPartitions(kafkaConsumerGroupMetadata.getTopic());
			for (TopicPartitionLeader partition : partitions) {
				long consumerOffset = zkClient.getZKOffset(consumerGroup, partition.getTopic(),
						partition.getPartitionId());
				consumer = getConsumer(partition.getLeaderHost(), partition.getLeaderPort(), clientName);
				long kafkaTopicOffset = getLastOffset(consumer, kafkaConsumerGroupMetadata.getTopic(),
						partition.getPartitionId(), -1, clientName);
				long lag = kafkaTopicOffset - consumerOffset;
				String owner = "";
				try {
					owner = zkClient.getOwner(consumerGroup, partition.getTopic(), partition.getPartitionId());
				} catch (final Throwable ignored) {

				}
				KafkaOffsetMonitor kafkaOffsetMonitor = new KafkaOffsetMonitor(
						kafkaConsumerGroupMetadata.getConsumerGroup(), kafkaConsumerGroupMetadata.getTopic(),
						partition.getPartitionId(), kafkaTopicOffset, consumerOffset, lag, owner);
				kafkaOffsetMonitors.add(kafkaOffsetMonitor);
			}

		}

		if (kafkaConsumerGroupMetadataList != null) {
			kafkaConsumerGroupMetadataList.clear();
			kafkaConsumerGroupMetadataList = null;// Help GC.
		}

		return kafkaOffsetMonitors;
	}

	public List<TopicPartitionLeader> getPartitions(final String topic) throws Exception {
		List<TopicPartitionLeader> partitionsLeader = new LinkedList<TopicPartitionLeader>();
		Map<String, Map<Integer, List<Integer>>> topicPidMap = zkClient.getPartitionsForTopic(topic);
		Map<Integer, List<Integer>> partitions = topicPidMap.get(topic);
		for (Map.Entry<Integer, List<Integer>> partition : partitions.entrySet()) {
			if (partition == null)
				continue;
			int partitionId = partition.getKey().intValue();
			Integer partitionLeaderId = zkClient.getLeaderForPartition(topic, partitionId);
			if (partitionLeaderId == null) {
				LOG.error("No broker for partition {} - {}", topic, partitionId);
				continue;
			}
			Broker broker = zkClient.getBrokerInfo(partitionLeaderId.intValue());
			String partitionLeaderHost = broker.getHost();
			int partitionLeaderPort = broker.getPort().intValue();
			TopicPartitionLeader topicPartitionLeader = new TopicPartitionLeader(topic, partitionId,
					partitionLeaderHost, partitionLeaderPort);
			partitionsLeader.add(topicPartitionLeader);
		}

		if (topicPidMap != null) {
			topicPidMap.clear();
			topicPidMap = null;// Help GC.
		}
		if (partitions != null) {
			partitions.clear();
			partitions = null;// Help GC.
		}

		return partitionsLeader;
	}

	public List<KafkaOffsetMonitor> getAllRegularKafkaOffsetMonitors() throws Exception {
		List<KafkaConsumerGroupMetadata> kafkaConsumerGroupMetadataList = zkClient.getActiveRegularConsumersAndTopics();
		List<KafkaOffsetMonitor> kafkaOffsetMonitors = new ArrayList<KafkaOffsetMonitor>();
		List<Broker> brokers = zkClient.getZKBrokerInfo();
		SimpleConsumer consumer = getConsumer(brokers.get(0).getHost(), brokers.get(0).getPort(), clientName);
		for (KafkaConsumerGroupMetadata kafkaConsumerGroupMetadata : kafkaConsumerGroupMetadataList) {
			List<TopicPartitionLeader> partitions = getPartitions(consumer, kafkaConsumerGroupMetadata.getTopic());
			for (TopicPartitionLeader partition : partitions) {
				consumer = getConsumer(partition.getLeaderHost(), partition.getLeaderPort(), clientName);
				long kafkaTopicOffset = getLastOffset(consumer, kafkaConsumerGroupMetadata.getTopic(),
						partition.getPartitionId(), -1, clientName);
				long consumerOffset = 0;
				if (kafkaConsumerGroupMetadata.getPartitionOffsetMap()
						.get(Integer.toString(partition.getPartitionId())) != null) {
					consumerOffset = kafkaConsumerGroupMetadata.getPartitionOffsetMap()
							.get(Integer.toString(partition.getPartitionId()));
				}
				long lag = kafkaTopicOffset - consumerOffset;
				KafkaOffsetMonitor kafkaOffsetMonitor = new KafkaOffsetMonitor(
						kafkaConsumerGroupMetadata.getConsumerGroup(), kafkaConsumerGroupMetadata.getTopic(),
						partition.getPartitionId(), kafkaTopicOffset, consumerOffset, lag, "");
				kafkaOffsetMonitors.add(kafkaOffsetMonitor);
			}
		}
		return kafkaOffsetMonitors;
	}

	public List<TopicPartitionLeader> getPartitions(SimpleConsumer consumer, String topic) {
		List<TopicPartitionLeader> partitions = new ArrayList<TopicPartitionLeader>();
		TopicMetadataRequest topicMetadataRequest = new TopicMetadataRequest(Collections.singletonList(topic));
		TopicMetadataResponse topicMetadataResponse = consumer.send(topicMetadataRequest);
		List<TopicMetadata> topicMetadataList = topicMetadataResponse.topicsMetadata();
		for (TopicMetadata topicMetadata : topicMetadataList) {
			List<PartitionMetadata> partitionMetadataList = topicMetadata.partitionsMetadata();
			for (PartitionMetadata partitionMetadata : partitionMetadataList) {
				if (partitionMetadata.leader() != null) {
					String partitionLeaderHost = partitionMetadata.leader().host();
					int partitionLeaderPort = partitionMetadata.leader().port();
					int partitionId = partitionMetadata.partitionId();
					TopicPartitionLeader topicPartitionLeader = new TopicPartitionLeader(topic, partitionId,
							partitionLeaderHost, partitionLeaderPort);
					partitions.add(topicPartitionLeader);
				}
			}
		}
		return partitions;
	}

	public long getLastOffset(SimpleConsumer consumer, String topic, int partition, long whichTime, String clientName) {
		long lastOffset = 0;
		try {
			List<String> topics = Collections.singletonList(topic);
			TopicMetadataRequest req = new TopicMetadataRequest(topics);
			kafka.javaapi.TopicMetadataResponse topicMetadataResponse = consumer.send(req);
			TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
			for (TopicMetadata topicMetadata : topicMetadataResponse.topicsMetadata()) {
				for (PartitionMetadata partitionMetadata : topicMetadata.partitionsMetadata()) {
					if (partitionMetadata.partitionId() == partition) {
						String partitionHost = partitionMetadata.leader().host();
						consumer = getConsumer(partitionHost, partitionMetadata.leader().port(), clientName);
						break;
					}
				}
			}
			Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
			requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
			kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo,
					kafka.api.OffsetRequest.CurrentVersion(), clientName);
			OffsetResponse response = consumer.getOffsetsBefore(request);
			if (response.hasError()) {
				LOG.error(
						"Error fetching Offset Data from the Broker. Reason: " + response.errorCode(topic, partition));
				lastOffset = 0;
			}
			long[] offsets = response.offsets(topic, partition);
			lastOffset = offsets[0];
		} catch (Exception e) {
			LOG.error("Error while collecting the log Size for topic: " + topic + ", and partition: " + partition, e);
		}
		return lastOffset;
	}

	public SimpleConsumer getConsumer(String host, int port, String clientName) {
		final String key = String.format("%s:%s", host, port);
		SimpleConsumer consumer = consumerMap.get(key);
		if (consumer == null) {
			consumer = new SimpleConsumer(host, port, 50 * 1000, 64 * 1024, clientName);
			LOG.info(String.format("Created a new Kafka Consumer for host:[%s] port:[%s]", host, port));
			consumerMap.put(key, consumer);
		}
		return consumer;
	}

	public static void closeConnection() {
		for (SimpleConsumer consumer : consumerMap.values()) {
			LOG.info("Closing connection for: " + consumer.host());
			consumer.close();
		}
	}
}
