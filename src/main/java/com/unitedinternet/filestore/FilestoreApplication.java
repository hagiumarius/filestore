package com.unitedinternet.filestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.unitedinternet.filestore")
@EnableJpaRepositories("com.unitedinternet.filestore.repository")
public class FilestoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(FilestoreApplication.class, args);
	}

}
