package com.genAI.genAi_chatBot.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PdfChunkStore {
    private final StringRedisTemplate redis;

    @Autowired
    public PdfChunkStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void save(String docId, List<String> chunks) throws JsonProcessingException {
        String json = new ObjectMapper().writeValueAsString(chunks);
        redis.opsForValue().set(docId, json, Duration.ofHours(2)); // TTL optional
    }

    public List<String> load(String docId) throws IOException {
        String json = redis.opsForValue().get(docId);
        if (json == null) return Collections.emptyList();
        return new ObjectMapper().readValue(json, new TypeReference<>() {});
    }
}
