package com.news.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account.path:}")
    private String serviceAccountPath;

    @Value("${firebase.service-account.resource:}")
    private Resource serviceAccountResource;

    @Value("${firebase.project-id:}")
    private String projectId;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions.Builder builder = FirebaseOptions.builder();
                boolean credentialsSet = false;

                // Option 1: Use service account resource (from classpath) - prioritize this
                if (serviceAccountResource != null && serviceAccountResource.exists()) {
                    InputStream serviceAccount = serviceAccountResource.getInputStream();
                    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                    builder.setCredentials(credentials);
                    credentialsSet = true;
                }
                // Option 2: Use service account file path (only if file exists)
                else if (serviceAccountPath != null && !serviceAccountPath.isEmpty()) {
                    java.io.File file = new java.io.File(serviceAccountPath);
                    if (file.exists() && file.isFile()) {
                        FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
                        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                        builder.setCredentials(credentials);
                        credentialsSet = true;
                    }
                }

                // Option 3: Use default credentials (for GCP environments)
                if (!credentialsSet) {
                    try {
                        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
                        builder.setCredentials(credentials);
                        credentialsSet = true;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to initialize Firebase: No service account file found. Please ensure serviceAccountKey.json exists in src/main/resources/ or configure firebase.service-account.path in application.properties", e);
                    }
                }

                // Set project ID if provided
                if (projectId != null && !projectId.isEmpty()) {
                    builder.setProjectId(projectId);
                }

                FirebaseOptions options = builder.build();
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}

