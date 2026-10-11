package com.pdunghh.chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.pdunghh.chess", "com.pdunghh.shared" })
public class ChessServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChessServiceApplication.class, args);
	}

}
