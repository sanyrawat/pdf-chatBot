package com.genAI.genAi_chatBot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
class LogJdbcUrl implements ApplicationRunner {
  @Value("${spring.datasource.url:NOT_SET}") String url;
  public void run(ApplicationArguments args) {
      System.out.println("### JDBC URL AT RUNTIME = " + url);
  }
  @Bean ApplicationRunner dumpVars1() {
	  return args -> {
	    System.out.println("DB_HOST=" + System.getenv("DB_HOST"));
	    System.out.println("DB_PORT=" + System.getenv("DB_PORT"));
	    System.out.println("DB_NAME=" + System.getenv("DB_NAME"));
	    System.out.println("DB_USER=" + System.getenv("DB_USER"));
	    System.out.println("DB_PASSWORD=" + System.getenv("DB_PASSWORD"));
	  };
	}

}

