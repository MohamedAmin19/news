package com.news.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    private final Firestore firestore;

    public FirestoreService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    
    /**
     * Save a document to a collection
     * @param collectionName The name of the collection
     * @param documentId The document ID (null for auto-generated)
     * @param data The data to save
     * @return The document ID
     */
    public String save(String collectionName, String documentId, Map<String, Object> data) {
        try {
            DocumentReference docRef;
            if (documentId != null && !documentId.isEmpty()) {
                docRef = firestore.collection(collectionName).document(documentId);
            } else {
                docRef = firestore.collection(collectionName).document();
            }
            ApiFuture<WriteResult> result = docRef.set(data);
            result.get(); // Wait for the write to complete
            return docRef.getId();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving document to Firestore", e);
        }
    }

    /**
     * Get a document by ID
     * @param collectionName The name of the collection
     * @param documentId The document ID
     * @return The document data, or null if not found
     */
    public Map<String, Object> get(String collectionName, String documentId) {
        try {
            DocumentReference docRef = firestore.collection(collectionName).document(documentId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            
            if (document.exists()) {
                return document.getData();
            }
            return null;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error getting document from Firestore", e);
        }
    }

    /**
     * Get all documents from a collection
     * @param collectionName The name of the collection
     * @return List of document data
     */
    public List<Map<String, Object>> getAll(String collectionName) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName).get();
            QuerySnapshot querySnapshot = future.get();
            List<Map<String, Object>> documents = new ArrayList<>();
            
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Map<String, Object> data = document.getData();
                if (data != null) {
                    data.put("id", document.getId()); // Include document ID
                    documents.add(data);
                }
            }
            
            return documents;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error getting documents from Firestore", e);
        }
    }

    /**
     * Get all documents from a collection with pagination
     * @param collectionName The name of the collection
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return PaginationResult containing documents and pagination info
     */
    public PaginationResult getAllPaginated(String collectionName, int page, int size) {
        try {
            // Get total count
            ApiFuture<QuerySnapshot> countFuture = firestore.collection(collectionName).get();
            QuerySnapshot countSnapshot = countFuture.get();
            long totalElements = countSnapshot.size();
            
            // Calculate pagination
            int offset = page * size;
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            // Get paginated documents
            Query query = firestore.collection(collectionName)
                    .limit(size)
                    .offset(offset);
            
            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();
            List<Map<String, Object>> documents = new ArrayList<>();
            
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Map<String, Object> data = document.getData();
                if (data != null) {
                    data.put("id", document.getId());
                    documents.add(data);
                }
            }
            
            return new PaginationResult(documents, totalElements, totalPages);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error getting paginated documents from Firestore", e);
        }
    }

    /**
     * Query documents with a where clause (equality)
     * @param collectionName The name of the collection
     * @param field The field to filter on
     * @param value The value to compare against
     * @return List of matching documents
     */
    public List<Map<String, Object>> query(String collectionName, String field, Object value) {
        try {
            Query query = firestore.collection(collectionName).whereEqualTo(field, value);
            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();
            List<Map<String, Object>> documents = new ArrayList<>();
            
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Map<String, Object> data = document.getData();
                if (data != null) {
                    data.put("id", document.getId());
                    documents.add(data);
                }
            }
            
            return documents;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error querying documents from Firestore", e);
        }
    }

    /**
     * Query documents with a where clause (equality) with pagination
     * @param collectionName The name of the collection
     * @param field The field to filter on
     * @param value The value to compare against
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return PaginationResult containing documents and pagination info
     */
    public PaginationResult queryPaginated(String collectionName, String field, Object value, int page, int size) {
        try {
            // Get total count
            Query countQuery = firestore.collection(collectionName).whereEqualTo(field, value);
            ApiFuture<QuerySnapshot> countFuture = countQuery.get();
            QuerySnapshot countSnapshot = countFuture.get();
            long totalElements = countSnapshot.size();
            
            // Calculate pagination
            int offset = page * size;
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            // Get paginated documents
            Query query = firestore.collection(collectionName)
                    .whereEqualTo(field, value)
                    .limit(size)
                    .offset(offset);
            
            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();
            List<Map<String, Object>> documents = new ArrayList<>();
            
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Map<String, Object> data = document.getData();
                if (data != null) {
                    data.put("id", document.getId());
                    documents.add(data);
                }
            }
            
            return new PaginationResult(documents, totalElements, totalPages);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error querying paginated documents from Firestore", e);
        }
    }

    /**
     * Inner class to hold pagination results
     */
    public static class PaginationResult {
        private final List<Map<String, Object>> documents;
        private final long totalElements;
        private final int totalPages;

        public PaginationResult(List<Map<String, Object>> documents, long totalElements, int totalPages) {
            this.documents = documents;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public List<Map<String, Object>> getDocuments() {
            return documents;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }

    /**
     * Update a document
     * @param collectionName The name of the collection
     * @param documentId The document ID
     * @param data The data to update
     */
    public void update(String collectionName, String documentId, Map<String, Object> data) {
        try {
            DocumentReference docRef = firestore.collection(collectionName).document(documentId);
            ApiFuture<WriteResult> result = docRef.update(data);
            result.get(); // Wait for the update to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error updating document in Firestore", e);
        }
    }

    /**
     * Delete a document
     * @param collectionName The name of the collection
     * @param documentId The document ID
     */
    public void delete(String collectionName, String documentId) {
        try {
            DocumentReference docRef = firestore.collection(collectionName).document(documentId);
            ApiFuture<WriteResult> result = docRef.delete();
            result.get(); // Wait for the delete to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting document from Firestore", e);
        }
    }

    /**
     * Get Firestore instance (for advanced operations)
     * @return Firestore instance
     */
    public Firestore getFirestore() {
        return firestore;
    }
}

