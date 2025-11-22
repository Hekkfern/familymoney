package com.familymoney.familymoney;

import org.springframework.boot.SpringApplication;

public class TestFamilymoneyApplication {

	public static void main(String[] args) {
		SpringApplication.from(FamilymoneyApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
