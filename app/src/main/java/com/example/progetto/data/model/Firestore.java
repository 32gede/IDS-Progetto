package com.example.progetto.data.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

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

    public void addSelectedIngredient(List<SelectedIngredientStoreUtils> ingredients, FirestoreCallback<Void> callback) {
        for (SelectedIngredientStoreUtils ingredient : ingredients) {
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

}

