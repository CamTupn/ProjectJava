package com.scentstation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScentstationBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScentstationBackendApplication.class, args);
	}

}
