package com.news.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticle {
    private String id;
    private String titleEnglish;
    private String titleArabic;
    private String descriptionEnglish;
    private String descriptionArabic;
    private String image;
    private String date;
    private String category;
    private Boolean isArabic;
    private Boolean isEnglish;
    private Boolean isFeatured;

    /**
     * Convert NewsArticle to Map for Firestore
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (titleEnglish != null) map.put("titleEnglish", titleEnglish);
        if (titleArabic != null) map.put("titleArabic", titleArabic);
        if (descriptionEnglish != null) map.put("descriptionEnglish", descriptionEnglish);
        if (descriptionArabic != null) map.put("descriptionArabic", descriptionArabic);
        if (image != null) map.put("image", image);
        if (date != null) map.put("date", date);
        if (category != null) map.put("category", category);
        if (isArabic != null) map.put("isArabic", isArabic);
        if (isEnglish != null) map.put("isEnglish", isEnglish);
        if (isFeatured != null) map.put("isFeatured", isFeatured);
        return map;
    }

    /**
     * Create NewsArticle from Firestore document data
     */
    public static NewsArticle fromMap(String id, Map<String, Object> data) {
        NewsArticle article = new NewsArticle();
        article.setId(id);
        if (data.get("titleEnglish") != null) article.setTitleEnglish(data.get("titleEnglish").toString());
        if (data.get("titleArabic") != null) article.setTitleArabic(data.get("titleArabic").toString());
        if (data.get("descriptionEnglish") != null) article.setDescriptionEnglish(data.get("descriptionEnglish").toString());
        if (data.get("descriptionArabic") != null) article.setDescriptionArabic(data.get("descriptionArabic").toString());
        if (data.get("image") != null) article.setImage(data.get("image").toString());
        if (data.get("date") != null) article.setDate(data.get("date").toString());
        if (data.get("category") != null) article.setCategory(data.get("category").toString());
        if (data.get("isArabic") != null) {
            Object isArabicValue = data.get("isArabic");
            if (isArabicValue instanceof Boolean) {
                article.setIsArabic((Boolean) isArabicValue);
            } else {
                article.setIsArabic(Boolean.parseBoolean(isArabicValue.toString()));
            }
        }
        if (data.get("isEnglish") != null) {
            Object isEnglishValue = data.get("isEnglish");
            if (isEnglishValue instanceof Boolean) {
                article.setIsEnglish((Boolean) isEnglishValue);
            } else {
                article.setIsEnglish(Boolean.parseBoolean(isEnglishValue.toString()));
            }
        }
        if (data.get("isFeatured") != null) {
            Object isFeaturedValue = data.get("isFeatured");
            if (isFeaturedValue instanceof Boolean) {
                article.setIsFeatured((Boolean) isFeaturedValue);
            } else {
                article.setIsFeatured(Boolean.parseBoolean(isFeaturedValue.toString()));
            }
        }
        return article;
    }
}

