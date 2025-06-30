package com.genAI.genAi_chatBot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genAI.genAi_chatBot.service.PdfChunkStore;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@CrossOrigin(origins = "https://pdf-chatbot-2-tq2q.onrender.com")
@RestController
@RequestMapping("/api/pdf-chat")
public class PdfChatController {

	@Autowired
    private final PdfChunkStore store;
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public PdfChatController(PdfChunkStore store) {
        this.store = store;
    }

    /* ---------- helpers ---------- */

    private List<String> chunkText(String text, int max) {
        List<String> out = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?]) ");
        StringBuilder buf = new StringBuilder();
        for (String s : sentences) {
            if (buf.length() + s.length() > max) {
                out.add(buf.toString());
                buf = new StringBuilder();
            }
            buf.append(s).append(' ');
        }
        if (!buf.isEmpty()) out.add(buf.toString());
        return out;
    }

    private String extractText(byte[] pdf) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    /* ---------- endpoints ---------- */

    /** Upload & return a documentId */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) {
        try {
            String text = extractText(file.getBytes());
            List<String> chunks = chunkText(text, 500);
            String docId = UUID.randomUUID().toString();
            store.save(docId, chunks);                    // Redis
            return ResponseEntity.ok(docId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    /** DTO for chat */
    public record ChatDTO(String documentId, String question) {}

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatDTO dto) throws IOException {
        if (dto.documentId() == null || dto.question() == null || dto.question().isBlank()) {
            return ResponseEntity.badRequest().body("documentId and question are required.");
        }

        List<String> chunks = store.load(dto.documentId());
        if (chunks.isEmpty()) {
            return ResponseEntity.badRequest().body("⚠️ Please upload PDF before chat.");
        }

        String context = String.join("\n",
                chunks.stream().limit(3).toList());       // send at most 3 chunks

        String prompt = "Answer based only on this context:\n" + context +
                        "\n\nQuestion: " + dto.question();

        /* --- call OpenAI --- */
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(openAiApiKey);

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant."),
                        Map.of("role", "user", "content", prompt))
        );

        JsonNode res = rest.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                new HttpEntity<>(body, h),
                JsonNode.class).getBody();

        String answer = res.path("choices").get(0).path("message").path("content").asText();
        return ResponseEntity.ok(answer);
    }
}
