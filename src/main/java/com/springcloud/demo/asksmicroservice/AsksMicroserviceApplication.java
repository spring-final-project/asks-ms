package com.springcloud.demo.asksmicroservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication
@EnableFeignClients
public class AsksMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsksMicroserviceApplication.class, args);
	}
}
