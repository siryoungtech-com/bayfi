package com.bayfi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BayfiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BayfiApplication.class, args);
	}

}
