package com.sipho.authentication;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthenticationApplication {

	public static void main(String[] args) {
		// Load .env file if it exists
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();

			// Set environment variables from .env file
			dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
			);
		} catch (Exception e) {
			System.out.println("No .env file found or error loading it. Using system environment variables.");
		}

		SpringApplication.run(AuthenticationApplication.class, args);
	}

}
