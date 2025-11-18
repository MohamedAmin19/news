package com.news.config;

import com.cloudinary.Cloudinary;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${CLOUDINARY_CLOUD_NAME:${cloudinary.cloud-name:}}")
    private String cloudName;

    @Value("${CLOUDINARY_API_KEY:${cloudinary.api-key:}}")
    private String apiKey;

    @Value("${CLOUDINARY_API_SECRET:${cloudinary.api-secret:}}")
    private String apiSecret;

    @PostConstruct
    public void disableSSLVerification() {
        try {
            // Create a trust manager that accepts all certificates
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            // Also set as default for Apache HttpClient
            SSLContext.setDefault(sc);

            // Also create a hostname verifier that accepts all hostnames
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // Log error but don't fail startup
            System.err.println("Warning: Failed to disable SSL verification: " + e.getMessage());
        }
    }

    @Bean
    public Cloudinary cloudinary() {
        if (cloudName == null || cloudName.isEmpty() || 
            apiKey == null || apiKey.isEmpty() || 
            apiSecret == null || apiSecret.isEmpty()) {
            throw new IllegalStateException("Cloudinary credentials are not configured. Please set cloudinary.cloud-name, cloudinary.api-key, and cloudinary.api-secret in application.properties or as environment variables.");
        }
        
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        
        return new Cloudinary(config);
    }
}

