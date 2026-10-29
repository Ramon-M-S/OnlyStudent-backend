package com.sloth.OnlyStudent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OnlyStudentApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlyStudentApplication.class, args);
	}

}
