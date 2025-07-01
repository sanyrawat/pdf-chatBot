package com.genAI.genAi_chatBot.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebugConfig {

    @Bean               // Spring will run this at startup
    ApplicationRunner dumpVars() {
        return args -> {
            System.out.println("─── ENV DUMP ───────────────");
            for (String k : java.util.List.of(
                    "DB_HOST","DB_PORT","DB_NAME","DB_USER","DB_PASSWORD"))
                System.out.println(k + "=" + System.getenv(k));
            System.out.println("spring.datasource.url="
                + System.getProperty("spring.datasource.url", "NOT-SET"));
            System.out.println("────────────────────────────");
        };
    }
}
