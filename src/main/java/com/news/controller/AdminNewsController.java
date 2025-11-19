package com.news.controller;

import com.news.model.NewsArticle;
import com.news.model.PaginatedResponse;
import com.news.service.FirestoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
     * Get all news with search and pagination (requires authentication)
     * Query parameters:
     * - search: search term to match in titleEnglish, titleArabic, descriptionEnglish, descriptionArabic, category, and date (case-insensitive, partial match)
     * - page: page number (default: 0)
     * - size: page size (default: 10, max: 100)
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<NewsArticle>> getAllNewsWithSearch(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Max page size
        
        // Get all news (we'll filter in memory for search)
        FirestoreService.PaginationResult result = firestoreService.getAllPaginated(COLLECTION_NAME, page, size);
        
        // Apply search filter if provided
        List<NewsArticle> articles = result.getDocuments().stream()
                .map(doc -> NewsArticle.fromMap(doc.get("id").toString(), doc))
                .filter(article -> {
                    // If no search term, return all articles
                    if (search == null || search.trim().isEmpty()) {
                        return true;
                    }
                    
                    String searchLower = search.toLowerCase().trim();
                    
                    // Search in titleEnglish
                    if (article.getTitleEnglish() != null && 
                        article.getTitleEnglish().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    
                    // Search in titleArabic
                    if (article.getTitleArabic() != null && 
                        article.getTitleArabic().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    
                    // Search in descriptionEnglish
                    if (article.getDescriptionEnglish() != null && 
                        article.getDescriptionEnglish().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    
                    // Search in descriptionArabic
                    if (article.getDescriptionArabic() != null && 
                        article.getDescriptionArabic().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    
                    // Search in category
                    if (article.getCategory() != null && 
                        article.getCategory().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    
                    // Search in date
                    if (article.getDate() != null && 
                        article.getDate().toLowerCase().contains(searchLower)) {
                        return true;
                    }
                    
                    return false;
                })
                .collect(Collectors.toList());
        
        // If search was applied, we need to get all results first, then paginate
        if (search != null && !search.trim().isEmpty()) {
            // Get all documents for search
            List<Map<String, Object>> allDocuments = firestoreService.getAll(COLLECTION_NAME);
            
            // Apply search filter to all documents
            List<NewsArticle> allFilteredArticles = allDocuments.stream()
                    .map(doc -> NewsArticle.fromMap(doc.get("id").toString(), doc))
                    .filter(article -> {
                        String searchLower = search.toLowerCase().trim();
                        
                        return (article.getTitleEnglish() != null && 
                                article.getTitleEnglish().toLowerCase().contains(searchLower)) ||
                               (article.getTitleArabic() != null && 
                                article.getTitleArabic().toLowerCase().contains(searchLower)) ||
                               (article.getDescriptionEnglish() != null && 
                                article.getDescriptionEnglish().toLowerCase().contains(searchLower)) ||
                               (article.getDescriptionArabic() != null && 
                                article.getDescriptionArabic().toLowerCase().contains(searchLower)) ||
                               (article.getCategory() != null && 
                                article.getCategory().toLowerCase().contains(searchLower)) ||
                               (article.getDate() != null && 
                                article.getDate().toLowerCase().contains(searchLower));
                    })
                    .collect(Collectors.toList());
            
            // Apply pagination to filtered results
            long totalElements = allFilteredArticles.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int start = page * size;
            int end = Math.min(start + size, allFilteredArticles.size());
            
            articles = start < allFilteredArticles.size() 
                    ? allFilteredArticles.subList(start, end)
                    : new ArrayList<>();
            
            PaginatedResponse<NewsArticle> response = new PaginatedResponse<>(
                    articles,
                    page,
                    size,
                    totalElements,
                    totalPages,
                    page < totalPages - 1,
                    page > 0
            );
            
            return ResponseEntity.ok(response);
        }
        
        // No search - use normal pagination
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

