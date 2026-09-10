package com.cineflow;

import org.springframework.boot.SpringApplication;

public class TestCineflowApplication {

	public static void main(String[] args) {
		SpringApplication.from(CineflowApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
