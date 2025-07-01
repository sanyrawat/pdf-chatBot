package com.genAI.genAi_chatBot.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class DatasourceFix {

  @PostConstruct
  void fixUrl() {
    String host = System.getenv("DB_HOST");
    String port = System.getenv("DB_PORT");
    String db   = System.getenv("DB_NAME");
    String url  = "jdbc:postgresql://%s:%s/%s?sslmode=require".formatted(host, port, db);

    System.setProperty("spring.datasource.url", url);   // programmatically override
  }
}
