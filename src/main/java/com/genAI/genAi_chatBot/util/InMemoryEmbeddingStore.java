package com.genAI.genAi_chatBot.util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InMemoryEmbeddingStore {
    private static List<String> chunks = new ArrayList<>();
    private static List<float[]> embeddings = new ArrayList<>();

    public static void storeChunks(List<String> newChunks) {
        chunks.clear();
        embeddings.clear();
        chunks.addAll(newChunks);
        newChunks.forEach(chunk -> embeddings.add(generateFakeEmbedding(chunk)));
    }

    public static List<String> getChunks() {
        return chunks;
    }

    public static List<String> findTopRelevantChunks(String question, int topK) {
        float[] queryEmbedding = generateFakeEmbedding(question);
        return IntStream.range(0, embeddings.size())
            .boxed()
            .sorted((i, j) -> -Float.compare(cosineSimilarity(queryEmbedding, embeddings.get(i)), cosineSimilarity(queryEmbedding, embeddings.get(j))))
            .limit(topK)
            .map(chunks::get)
            .collect(Collectors.toList());
    }

    // Fake embedding generator for demo; replace with OpenAI Embeddings later
    private static float[] generateFakeEmbedding(String text) {
        float[] vector = new float[128];
        int hash = text.hashCode();
        Arrays.fill(vector, hash % 1000 / 1000f);
        return vector;
    }

    private static float cosineSimilarity(float[] a, float[] b) {
        float dot = 0f, magA = 0f, magB = 0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }
        return (float) (dot / (Math.sqrt(magA) * Math.sqrt(magB) + 1e-10));
    }
}