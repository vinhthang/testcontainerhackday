package com.github.vinhthang.boot;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
public class PulsarLearningTests {
	private static final DockerImageName PULSAR_TEST_IMAGE = DockerImageName.parse(
			"apachepulsar/pulsar:2.10.0"
	);
	PulsarContainer pulsar = new PulsarContainer(PULSAR_TEST_IMAGE);

	@Test
	public void testUsage() throws Exception {
		try (
				PulsarContainer pulsar = new PulsarContainer(PULSAR_TEST_IMAGE)
		) {
			pulsar.start();
			testPulsarFunctionality(pulsar.getPulsarBrokerUrl());
		}
	}

	@Test
	public void testClusterFullyInitialized() throws Exception {
		try (PulsarContainer pulsar = new PulsarContainer(PULSAR_TEST_IMAGE)) {
			pulsar.start();

			try (PulsarAdmin pulsarAdmin = PulsarAdmin.builder().serviceHttpUrl(pulsar.getHttpServiceUrl()).build()) {
				assertThat(pulsarAdmin.clusters().getClusters())
						.hasSize(1)
						.contains("standalone");
			}
		}
	}


	protected void testPulsarFunctionality(String pulsarBrokerUrl) throws Exception {
		try (
				PulsarClient client = PulsarClient.builder().serviceUrl(pulsarBrokerUrl).build();
				Consumer consumer = client.newConsumer().topic("test_topic").subscriptionName("test-subs").subscribe();
				Producer<byte[]> producer = client.newProducer().topic("test_topic").create()
		) {
			producer.send("test containers".getBytes());
			CompletableFuture<Message> future = consumer.receiveAsync();
			Message message = future.get(5, TimeUnit.SECONDS);

			assertThat(new String(message.getData())).isEqualTo("test containers");
		}
	}

}
