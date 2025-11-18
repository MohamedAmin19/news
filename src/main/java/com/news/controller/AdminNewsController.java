package com.news.controller;

import com.news.model.NewsArticle;
import com.news.service.FirestoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/news")
public class AdminNewsController {

    private final FirestoreService firestoreService;
    private static final String COLLECTION_NAME = "news";

    public AdminNewsController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    /**
     * Get all news with filtering (requires authentication)
     * Query parameters (all optional):
     * - titleEnglish: filter by English title (exact match)
     * - titleArabic: filter by Arabic title (exact match)
     * - descriptionEnglish: filter by English description (exact match)
     * - descriptionArabic: filter by Arabic description (exact match)
     * - category: filter by category (exact match)
     * - date: filter by date (exact match)
     */
    @GetMapping
    public ResponseEntity<List<NewsArticle>> getAllNewsWithFiltering(
            @RequestParam(required = false) String titleEnglish,
            @RequestParam(required = false) String titleArabic,
            @RequestParam(required = false) String descriptionEnglish,
            @RequestParam(required = false) String descriptionArabic,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String date) {
        List<Map<String, Object>> documents;
        
        // Use Firestore query for category if provided (more efficient)
        if (category != null && !category.isEmpty()) {
            documents = firestoreService.query(COLLECTION_NAME, "category", category);
        } else {
            documents = firestoreService.getAll(COLLECTION_NAME);
        }
        
        // Apply client-side filtering for all fields
        List<NewsArticle> articles = documents.stream()
                .map(doc -> NewsArticle.fromMap(doc.get("id").toString(), doc))
                .filter(article -> {
                    // Filter by titleEnglish
                    if (titleEnglish != null && !titleEnglish.isEmpty()) {
                        if (article.getTitleEnglish() == null || !article.getTitleEnglish().equals(titleEnglish)) {
                            return false;
                        }
                    }
                    
                    // Filter by titleArabic
                    if (titleArabic != null && !titleArabic.isEmpty()) {
                        if (article.getTitleArabic() == null || !article.getTitleArabic().equals(titleArabic)) {
                            return false;
                        }
                    }
                    
                    // Filter by descriptionEnglish
                    if (descriptionEnglish != null && !descriptionEnglish.isEmpty()) {
                        if (article.getDescriptionEnglish() == null || !article.getDescriptionEnglish().equals(descriptionEnglish)) {
                            return false;
                        }
                    }
                    
                    // Filter by descriptionArabic
                    if (descriptionArabic != null && !descriptionArabic.isEmpty()) {
                        if (article.getDescriptionArabic() == null || !article.getDescriptionArabic().equals(descriptionArabic)) {
                            return false;
                        }
                    }
                    
                    // Filter by date
                    if (date != null && !date.isEmpty()) {
                        if (article.getDate() == null || !article.getDate().equals(date)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(articles);
    }

    /**
     * Get news by ID (requires authentication)
     */
    @GetMapping("/{id}")
    public ResponseEntity<NewsArticle> getNewsById(@PathVariable String id) {
        Map<String, Object> data = firestoreService.get(COLLECTION_NAME, id);
        if (data != null) {
            NewsArticle article = NewsArticle.fromMap(id, data);
            return ResponseEntity.ok(article);
        }
        return ResponseEntity.notFound().build();
    }
}

