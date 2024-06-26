package com.fiap.techchallenge4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class Techchallenge4Application {

	public static void main(String[] args) {
		SpringApplication.run(Techchallenge4Application.class, args);
	}

}
