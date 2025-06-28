package com.genAI.genAi_chatBot.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VERY simple in-memory map: sessionId  ->  List<String> pdfChunks
 * Works only if both requests hit the same JVM.  Not for production scale.
 */
public class SessionStore {
    private static final Map<String, List<String>> STORE = new ConcurrentHashMap<>();

    public static void save(String token, List<String> chunks) {
        STORE.put(token, chunks);
    }

    public static List<String> get(String token) {
        return STORE.get(token);
    }
}
