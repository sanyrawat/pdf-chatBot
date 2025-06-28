package com.genAI.genAi_chatBot.controller;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genAI.genAi_chatBot.util.InMemoryEmbeddingStore;

import java.io.IOException;
import java.util.*;

@CrossOrigin(origins = "https://chat-bot-pdf-seven.vercel.app")
@RestController
@RequestMapping("/api/pdf-chat")
public class PdfChatController {

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper mapper = new ObjectMapper();

	@Value("${openai.api.key}")
	private String openAiApiKey;

	public PdfChatController() {
		// No WebClient needed with RestTemplate
	}

	private List<String> chunkText(String text, int maxChunkSize) {
		List<String> chunks = new ArrayList<>();
		String[] sentences = text.split("(?<=[.!?]) ");
		StringBuilder chunk = new StringBuilder();
		for (String sentence : sentences) {
			if (chunk.length() + sentence.length() > maxChunkSize) {
				chunks.add(chunk.toString());
				chunk = new StringBuilder();
			}
			chunk.append(sentence).append(" ");
		}
		if (!chunk.isEmpty()) {
			chunks.add(chunk.toString());
		}
		return chunks;
	}

	private String extractTextFromPdf(byte[] inputBytes) throws IOException {
		try (PDDocument document = Loader.loadPDF(inputBytes)) {
			PDFTextStripper stripper = new PDFTextStripper();
			return stripper.getText(document);
		}
	}

	@PostMapping("/upload")
	public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
		try {
			String text = extractTextFromPdf(file.getBytes());
			List<String> chunks = chunkText(text, 500);
			InMemoryEmbeddingStore.storeChunks(chunks);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok("PDF uploaded and processed.");
	}

	@PostMapping("/chat")
	public ResponseEntity<String> chatWithPdf(@RequestBody Map<String, String> payload) throws Exception {
		String userQuestion = payload.get("question");

		List<String> topChunks = InMemoryEmbeddingStore.findTopRelevantChunks(userQuestion, 3);
		if (topChunks == null || topChunks.isEmpty()) {
	        return ResponseEntity.badRequest().body("⚠️ Please upload PDF before chat.");
	    }
		String context = String.join("", topChunks);
		

		String prompt = "Answer based only on this context:\n" + context + "\n\nQuestion: " + userQuestion;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(openAiApiKey);

		Map<String, Object> systemMessage = Map.of("role", "system", "content", "You are a helpful assistant.");
		Map<String, Object> userMessage = Map.of("role", "user", "content", prompt);

		Map<String, Object> requestBody = Map.of("model", "gpt-4o-mini", "messages",
				List.of(systemMessage, userMessage));

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

		ResponseEntity<JsonNode> response = restTemplate.postForEntity("https://api.openai.com/v1/chat/completions",
				entity, JsonNode.class);

		String answer = response.getBody().get("choices").get(0).get("message").get("content").asText();
		return ResponseEntity.ok(answer);
	}
}