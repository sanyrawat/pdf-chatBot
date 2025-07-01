package com.genAI.genAi_chatBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PdfChunkStore {

    @Autowired
    private JdbcTemplate jdbc;

    public void save(String docId, List<String> chunks) {
        UUID docUUID = UUID.fromString(docId);
        String sql = "INSERT INTO pdf_chunk (document_id, chunk_index, chunk_text) VALUES (?, ?, ?)";
        for (int i = 0; i < chunks.size(); i++) {
            jdbc.update(sql, docUUID, i, chunks.get(i));
        }
    }

    public List<String> load(String docId) {
        UUID docUUID = UUID.fromString(docId);
        return jdbc.queryForList(
            "SELECT chunk_text FROM pdf_chunk WHERE document_id = ? ORDER BY chunk_index",
            String.class, docUUID
        );
    }
}
