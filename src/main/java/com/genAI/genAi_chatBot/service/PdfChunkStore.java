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
    private final ObjectMapper mapper;           // reuse a single mapper
    private static final Duration TTL = Duration.ofHours(2);

    @Autowired
    public PdfChunkStore(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis  = redis;
        this.mapper = mapper;
    }

    public void save(String docId, List<String> chunks) {
        try {
            redis.opsForValue()
                 .set(docId, mapper.writeValueAsString(chunks), TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize chunks", e);
        }
    }

    public List<String> load(String docId) {
        try {
            String json = redis.opsForValue().get(docId);
            if (json == null) return Collections.emptyList();
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize chunks", e);
        }
    }
}
