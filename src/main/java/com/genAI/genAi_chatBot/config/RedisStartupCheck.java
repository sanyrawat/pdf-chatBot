package com.genAI.genAi_chatBot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class LogJdbcUrl implements ApplicationRunner {
  @Value("${spring.datasource.url:NOT_SET}") String url;
  public void run(ApplicationArguments args) {
      System.out.println("### JDBC URL AT RUNTIME = " + url);
  }
}

