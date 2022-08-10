package com.project.sidefit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SidefitApplication {

	public static void main(String[] args) {
		SpringApplication.run(SidefitApplication.class, args);
	}

}
