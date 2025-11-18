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

