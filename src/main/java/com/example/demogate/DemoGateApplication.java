package com.example.demogate;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DestinationTopic;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Properties;

@RestController
@RequestMapping("/api/v1/gateway")
@SpringBootApplication
public class DemoGateApplication {
	private String user;
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	public static void main(String[] args) {
		SpringApplication.run(DemoGateApplication.class, args);
	}

	@GetMapping("/login")
	public String login() throws InterruptedException, ExecutionException {
		kafkaTemplate.send("authen_topic", "daint");

		// Tạo một đối tượng ExecutorService để quản lý các luồng
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Void> future = executor.submit(() -> {
			// Tạo một đối tượng KafkaConsumer để đọc thông điệp từ chủ đề "user_topic"
			Properties properties = new Properties();
			properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
			properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "authen_service");
			properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

			KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
			consumer.subscribe(Collections.singletonList("user_topic"));

			// Đọc thông điệp từ Kafka và lưu trữ vào biến this.user
			while (user == null) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1));
				for (ConsumerRecord<String, String> record : records) {
					user = record.value();
				}
			}

			// Đóng KafkaConsumer
			consumer.close();

			return null;
		});

		// Chờ đợi việc kết thúc của luồng KafkaConsumer
		future.get();

		// Đóng ExecutorService
		executor.shutdown();

		System.out.println(this.user);

		if (this.user == null) {
			return "fail";
		}

		return "success";
	}

	@KafkaListener(topics = "user_topic", groupId = "authen_service")
	public void receive(String user) {
		this.user = user;
	}


}
