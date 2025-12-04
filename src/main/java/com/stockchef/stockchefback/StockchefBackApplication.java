package com.stockchef.stockchefback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan("com.stockchef.stockchefback.model")
public class StockchefBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockchefBackApplication.class, args);
	}

}
