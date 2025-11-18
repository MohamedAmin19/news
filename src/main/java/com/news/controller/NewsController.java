package com.news.controller;

import com.news.model.NewsArticle;
import com.news.service.FirestoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final FirestoreService firestoreService;
    private static final String COLLECTION_NAME = "news";

    public NewsController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @PostMapping
    public ResponseEntity<NewsArticle> addNews(@RequestBody NewsArticle article) {
        Map<String, Object> data = article.toMap();
        String documentId = firestoreService.save(COLLECTION_NAME, null, data);
        article.setId(documentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(article);
    }

    @GetMapping
    public ResponseEntity<List<NewsArticle>> getAllNews() {
        List<Map<String, Object>> documents = firestoreService.getAll(COLLECTION_NAME);
        List<NewsArticle> articles = documents.stream()
                .map(doc -> NewsArticle.fromMap(doc.get("id").toString(), doc))
                .collect(Collectors.toList());
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsArticle> getNewsById(@PathVariable String id) {
        Map<String, Object> data = firestoreService.get(COLLECTION_NAME, id);
        if (data != null) {
            NewsArticle article = NewsArticle.fromMap(id, data);
            return ResponseEntity.ok(article);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<NewsArticle>> getNewsByCategory(@PathVariable String category) {
        List<Map<String, Object>> documents = firestoreService.query(COLLECTION_NAME, "category", category);
        List<NewsArticle> articles = documents.stream()
                .map(doc -> NewsArticle.fromMap(doc.get("id").toString(), doc))
                .collect(Collectors.toList());
        return ResponseEntity.ok(articles);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NewsArticle> updateNews(@PathVariable String id, @RequestBody NewsArticle article) {
        Map<String, Object> existingData = firestoreService.get(COLLECTION_NAME, id);
        if (existingData == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> updateData = article.toMap();
        firestoreService.update(COLLECTION_NAME, id, updateData);
        article.setId(id);
        return ResponseEntity.ok(article);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNews(@PathVariable String id) {
        Map<String, Object> existingData = firestoreService.get(COLLECTION_NAME, id);
        if (existingData == null) {
            return ResponseEntity.notFound().build();
        }
        firestoreService.delete(COLLECTION_NAME, id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully deleted");
        return ResponseEntity.ok(response);
    }
}

