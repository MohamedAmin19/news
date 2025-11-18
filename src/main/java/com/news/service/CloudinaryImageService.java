package com.news.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryImageService {

    private final Cloudinary cloudinary;

    public CloudinaryImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload image to Cloudinary
     * @param file MultipartFile to upload
     * @return URL of the uploaded image
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Image size exceeds 10MB limit");
        }

        // Upload to Cloudinary
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "news",
                        "resource_type", "image"
                )
        );

        // Return the secure URL
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Upload base64 image to Cloudinary
     * @param base64Image Base64 encoded image string
     * @return URL of the uploaded image
     */
    public String uploadBase64Image(String base64Image) throws IOException {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new IllegalArgumentException("Base64 image is empty");
        }

        // Remove data URL prefix if present
        String base64Data = base64Image;
        if (base64Image.startsWith("data:image/")) {
            int commaIndex = base64Image.indexOf(",");
            if (commaIndex > 0) {
                base64Data = base64Image.substring(commaIndex + 1);
            }
        }

        // Upload to Cloudinary
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                base64Data,
                ObjectUtils.asMap(
                        "folder", "news",
                        "resource_type", "image"
                )
        );

        // Return the secure URL
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Delete image from Cloudinary
     * @param imageUrl The Cloudinary URL of the image
     */
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // Extract public ID from URL
            // Cloudinary URL format: https://res.cloudinary.com/{cloud_name}/image/upload/{folder}/{public_id}.{format}
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            // Ignore deletion errors (image might not exist or URL might be invalid)
        }
    }

    /**
     * Extract public ID from Cloudinary URL
     */
    private String extractPublicId(String url) {
        try {
            // Extract the path after /upload/
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex > 0) {
                String path = url.substring(uploadIndex + "/upload/".length());
                // Remove version if present (v1234567890/)
                if (path.contains("/v")) {
                    int versionEnd = path.indexOf("/", path.indexOf("/v"));
                    if (versionEnd > 0) {
                        path = path.substring(versionEnd + 1);
                    }
                }
                // Remove file extension
                int lastDot = path.lastIndexOf(".");
                if (lastDot > 0) {
                    path = path.substring(0, lastDot);
                }
                return path;
            }
        } catch (Exception e) {
            // Return null if extraction fails
        }
        return null;
    }
}

