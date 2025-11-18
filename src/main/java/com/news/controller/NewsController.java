package com.news.controller;

import com.news.model.NewsArticle;
import com.news.model.PaginatedResponse;
import com.news.service.FirestoreService;
import com.news.service.CloudinaryImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final FirestoreService firestoreService;
    private final CloudinaryImageService imageService;
    private static final String COLLECTION_NAME = "news";

    public NewsController(FirestoreService firestoreService, CloudinaryImageService imageService) {
        this.firestoreService = firestoreService;
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<?> addNews(@RequestBody NewsArticle article) {
        try {
            // Process image if it's base64
            if (article.getImage() != null && !article.getImage().isEmpty()) {
                // Check if it's a base64 data URL
                if (article.getImage().startsWith("data:image/") || 
                    (article.getImage().length() > 100 && !article.getImage().startsWith("http"))) {
                    try {
                        String imageUrl = imageService.uploadBase64Image(article.getImage());
                        article.setImage(imageUrl);
                    } catch (IllegalArgumentException e) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Invalid image: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                    } catch (IOException e) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Failed to upload image: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                    }
                }
            }
            
            Map<String, Object> data = article.toMap();
            String documentId = firestoreService.save(COLLECTION_NAME, null, data);
            article.setId(documentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(article);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create news: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<PaginatedResponse<NewsArticle>> getNewsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Max page size
        
        FirestoreService.PaginationResult result;
        
        // If category is "all", return all news; otherwise filter by category
        if ("all".equalsIgnoreCase(category)) {
            result = firestoreService.getAllPaginated(COLLECTION_NAME, page, size);
        } else {
            result = firestoreService.queryPaginated(COLLECTION_NAME, "category", category, page, size);
        }
        
        List<NewsArticle> articles = result.getDocuments().stream()
                .map(doc -> NewsArticle.fromMap(doc.get("id").toString(), doc))
                .collect(Collectors.toList());
        
        PaginatedResponse<NewsArticle> response = new PaginatedResponse<>(
                articles,
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                page < result.getTotalPages() - 1,
                page > 0
        );
        
        return ResponseEntity.ok(response);
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNews(@PathVariable String id, @RequestBody NewsArticle article) {
        try {
            Map<String, Object> existingData = firestoreService.get(COLLECTION_NAME, id);
            if (existingData == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Process image if it's base64
            if (article.getImage() != null && !article.getImage().isEmpty()) {
                // Check if it's a base64 data URL
                if (article.getImage().startsWith("data:image/") || 
                    (article.getImage().length() > 100 && !article.getImage().startsWith("http"))) {
                    try {
                        // Delete old image from Cloudinary if it exists
                        String oldImage = existingData.get("image") != null ? 
                            existingData.get("image").toString() : null;
                        if (oldImage != null && oldImage.contains("cloudinary.com")) {
                            try {
                                imageService.deleteImage(oldImage);
                            } catch (Exception e) {
                                // Ignore deletion errors
                            }
                        }
                        
                        String imageUrl = imageService.uploadBase64Image(article.getImage());
                        article.setImage(imageUrl);
                    } catch (IllegalArgumentException e) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Invalid image: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                    } catch (IOException e) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Failed to upload image: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                    }
                }
            }
            
            Map<String, Object> updateData = article.toMap();
            firestoreService.update(COLLECTION_NAME, id, updateData);
            article.setId(id);
            return ResponseEntity.ok(article);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update news: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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

