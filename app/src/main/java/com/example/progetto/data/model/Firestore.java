package com.example.progetto.data.model;

import static androidx.core.app.ActivityCompat.startActivityForResult;


import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Firestore {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;

    public Firestore() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("store_images");
    }

    public void getSelectIngredients(String storeId, FirestoreCallback<List<SelectedIngredientUtils>> callback) {
        db.collection("SelectedIngredient")
                .whereEqualTo("recipeId", storeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SelectedIngredientUtils> selectedIngredients = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SelectedIngredientUtils ingredient = document.toObject(SelectedIngredientUtils.class);
                        if (ingredient != null) {
                            selectedIngredients.add(ingredient);
                        }
                    }
                    callback.onSuccess(selectedIngredients); // Passa il risultato al chiamante
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e); // Passa l'errore al chiamante
                });
    }

    public void getIngredients(FirestoreCallback<List<ItemUtils>> callback) {
        db.collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemUtils> ingredients = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ItemUtils item = document.toObject(ItemUtils.class);
                        if (item != null) {
                            ingredients.add(item);
                        }
                    }
                    callback.onSuccess(ingredients); // Passa il risultato al chiamante
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e); // Passa l'errore al chiamante
                });
    }

    public void removeSelectedIngredient(StoreUtils store, FirestoreCallback<Void> callback) {
        db.collection("SelectedIngredient")
                .whereEqualTo("recipeId", store.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    callback.onSuccess(null); // Passa il risultato al chiamante
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e); // Passa l'errore al chiamante
                });
    }

    public void addSelectedIngredient(List<SelectedIngredientRecipeUtils> ingredients, FirestoreCallback<Void> callback) {
        for (SelectedIngredientRecipeUtils ingredient : ingredients) {
            db.collection("SelectedIngredient")
                    .add(ingredient)
                    .addOnSuccessListener(documentReference -> {
                        callback.onSuccess(null); // Passa il risultato al chiamante
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure(e); // Passa l'errore al chiamante
                    });
        }
    }
    public void uploadImage(Uri imageUri, FirestoreCallback<String> callback) {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            if (downloadUri != null) {
                                callback.onSuccess(downloadUri.toString());
                            } else {
                                callback.onFailure(new Exception("Download URL is null"));
                            }
                        } else {
                            callback.onFailure(task.getException());
                        }
                    })
                    .addOnFailureListener(callback::onFailure);
        } else {
            callback.onFailure(new Exception("Image URI is null"));
        }
    }


    public String addSomething(Map<String, Object> object, String directory) {
    String documentId = db.collection(directory).document().getId();
    object.put("id", documentId);
    db.collection(directory)
            .document(documentId) // Use the generated document ID
            .set(object) // Use set instead of add
            .addOnSuccessListener(aVoid -> {
                Log.d("Firestore", "DocumentSnapshot added with ID: " + documentId);
            })
            .addOnFailureListener(e -> {
                Log.e("Firestore", "Error adding document", e);
            });
    return documentId;
}
}

