package com.reactiveauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ReactiveAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveAuthApplication.class, args);
	}

}
