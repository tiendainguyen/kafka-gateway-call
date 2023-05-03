package com.example.demogate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public String login(){
		kafkaTemplate.send("authen_topic", "daint");
    System.out.println(this.user);
		if(this.user == null){
			return "fail";
		}
		return "success";
	}
//	@KafkaListener(topics = "authen_topic", groupId = "authen_service")
//	public void receive(String user) {
//		this.user = user;
//	}
}
