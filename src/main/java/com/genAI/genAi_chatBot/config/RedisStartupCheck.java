package com.genAI.genAi_chatBot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisStartupCheck {

    private static final Logger log = LoggerFactory.getLogger(RedisStartupCheck.class);

    /**
     * Runs once at application startup:
     * • prints the value of SPRING_REDIS_URL that Spring sees
     * • does a Redis PING and logs PONG (or the exception)
     */
    @Bean
    CommandLineRunner pingRedis(StringRedisTemplate redis, Environment env) {
        return args -> {
            String redisUrl = env.getProperty("SPRING_REDIS_URL");
            log.info("SPRING_REDIS_URL = {}", redisUrl);   // should show redis://default:*****@pdf-redis:6379

            try {
                String pong = redis.getRequiredConnectionFactory()
                                   .getConnection()
                                   .ping();                // returns "PONG" on success
                log.info("✅ Redis ping returned: {}", pong);
            } catch (Exception ex) {
                log.error("❌ Redis ping failed!", ex);
            }
        };
    }
}
