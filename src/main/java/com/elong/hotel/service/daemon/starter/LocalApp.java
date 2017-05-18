package com.elong.hotel.service.daemon.starter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

//@SpringBootApplication
@ComponentScan(basePackages = { "com.elong.nchecklist.alarm.*" })
public class LocalApp implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(LocalApp.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub

	}

}
