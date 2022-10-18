package com.examples.sample.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SampleProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleProducerApplication.class, args);

	}

}
