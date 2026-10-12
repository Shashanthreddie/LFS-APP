/* 
@author : Ram-Ambati
The main entry point of the Spring Boot application. 
It parses your local .env configuration file and boots up the Spring context.
*/

package com.lfs.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// 1. Load local .env file if it exists (only for keys not already set in OS env)
		try {
			if (Files.exists(Paths.get(".env"))) {
				List<String> lines = Files.readAllLines(Paths.get(".env"));
				for (String line : lines) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					String[] parts = line.split("=", 2);
					if (parts.length == 2) {
						String key = parts[0].trim();
						String value = parts[1].trim();
						if (value.startsWith("\"") && value.endsWith("\"")) {
							value = value.substring(1, value.length() - 1);
						} else if (value.startsWith("'") && value.endsWith("'")) {
							value = value.substring(1, value.length() - 1);
						}
						// Do not overwrite OS environment variables
						if (System.getProperty(key) == null && System.getenv(key) == null) {
							System.setProperty(key, value);
						}
					}
				}
				System.out.println("✓ Loaded environment variables from .env file");
			}
		} catch (IOException e) {
			System.err.println("Warning: Failed to load .env file: " + e.getMessage());
		}

		// 2. Fix JDBC prefix if provided without "jdbc:" (common on Railway/Heroku/Render)
		normalizeDatabaseUrl();

		SpringApplication.run(BackendApplication.class, args);
	}

	private static void normalizeDatabaseUrl() {
		String dbUrl = System.getProperty("SPRING_DATASOURCE_URL");
		if (dbUrl == null || dbUrl.isBlank()) {
			dbUrl = System.getenv("SPRING_DATASOURCE_URL");
		}
		if (dbUrl == null || dbUrl.isBlank()) {
			dbUrl = System.getenv("DATABASE_URL");
		}

		if (dbUrl != null && !dbUrl.isBlank()) {
			if (dbUrl.startsWith("postgresql://")) {
				dbUrl = "jdbc:" + dbUrl;
			}
			System.setProperty("SPRING_DATASOURCE_URL", dbUrl);
		}
	}
}

