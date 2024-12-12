package com.example.progetto.data.model;
import android.net.Uri;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Firestore {
    private final FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final StorageReference storageRef;

    public Firestore() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("store_images");
    }

    public void getSelectIngredients(String id, FirestoreCallback<List<SelectedIngredientUtils>> callback) {
        // Passa l'errore al chiamante
        db.collection("SelectedIngredient")
                .whereEqualTo("recipeId", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SelectedIngredientUtils> selectedIngredients = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SelectedIngredientUtils ingredient = document.toObject(SelectedIngredientUtils.class);
                        selectedIngredients.add(ingredient);
                    }
                    callback.onSuccess(selectedIngredients); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getIngredients(FirestoreCallback<List<ItemUtils>> callback) {
        // Passa l'errore al chiamante
        db.collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ItemUtils> ingredients = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ItemUtils item = document.toObject(ItemUtils.class);
                        ingredients.add(item);
                    }
                    callback.onSuccess(ingredients); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void removeSelectedIngredient(StoreUtils store, FirestoreCallback<Void> callback) {
        // Passa l'errore al chiamante
        db.collection("SelectedIngredient")
                .whereEqualTo("recipeId", store.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    callback.onSuccess(null); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void addSelectedIngredient(List<SelectedIngredientRecipeUtils> ingredients, FirestoreCallback<Void> callback) {
        for (SelectedIngredientRecipeUtils ingredient : ingredients) {
            // Passa l'errore al chiamante
            db.collection("SelectedIngredient")
                    .add(ingredient)
                    .addOnSuccessListener(documentReference -> {
                        callback.onSuccess(null); // Passa il risultato al chiamante
                    })
                    .addOnFailureListener(callback::onFailure);
        }
    }

    public void getUserIngredients(String id, FirestoreCallback<List<UserProductUtils>> callback) {
        // Passa l'errore al chiamante
        db.collection("user_products")
                .whereEqualTo("userId", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProductUtils> userIngredients = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserProductUtils ingredient = document.toObject(UserProductUtils.class);
                        userIngredients.add(ingredient);
                    }
                    callback.onSuccess(userIngredients); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void uploadImage(Uri imageUri, FirestoreCallback<String> callback) {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
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

    public void loadGlobalRecipes(FirestoreCallback<List<Recipe>> callback) {
        // Passa l'errore al chiamante
        db.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipes.add(recipe);
                    }
                    callback.onSuccess(recipes); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void loadUserRecipes(String userId, FirestoreCallback<List<UserRecipeUtils>> callback) {
        // Passa l'errore al chiamante
        db.collection("recipes_user")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserRecipeUtils> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserRecipeUtils recipe = document.toObject(UserRecipeUtils.class);
                        recipes.add(recipe);
                    }
                    callback.onSuccess(recipes); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getIngredientsOfRecipe(String recipeId, FirestoreCallback<List<SelectedIngredientUtils>> callback) {
        // Passa l'errore al chiamante
        db.collection("SelectedIngredient")
                .whereEqualTo("recipeId", recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SelectedIngredientUtils> ingredients = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SelectedIngredientUtils ingredient = document.toObject(SelectedIngredientUtils.class);
                        ingredients.add(ingredient);
                    }
                    callback.onSuccess(ingredients); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void loadCookableRecipes(String userId, FirestoreCallback<List<UserRecipeUtils>> callback) {
        this.getUserIngredients(userId, new FirestoreCallback<List<UserProductUtils>>() {
            @Override
            public void onSuccess(List<UserProductUtils> userIngredients) {
                loadGlobalRecipes(new FirestoreCallback<List<Recipe>>() {
                    @Override
                    public void onSuccess(List<Recipe> globalRecipes) {
                        List<UserRecipeUtils> cookableRecipes = new ArrayList<>();
                        AtomicInteger processedRecipes = new AtomicInteger(0); // Contatore per tracciare le ricette elaborate

                        if (globalRecipes.isEmpty()) {
                            // Se non ci sono ricette, restituisci subito una lista vuota
                            callback.onSuccess(cookableRecipes);
                            return;
                        }

                        for (Recipe recipe : globalRecipes) {
                            getIngredientsOfRecipe(recipe.getId(), new FirestoreCallback<List<SelectedIngredientUtils>>() {
                                @Override
                                public void onSuccess(List<SelectedIngredientUtils> ingredients) {
                                    // Verifica se l'utente ha tutti gli ingredienti richiesti
                                    boolean isCookable = true;

                                    for (SelectedIngredientUtils ingredient : ingredients) {
                                        UserProductUtils matchingUserIngredient = userIngredients.stream()
                                                .filter(ui -> ui.getName().equals(ingredient.getName()))
                                                .findFirst()
                                                .orElse(null);

                                        if (matchingUserIngredient == null || matchingUserIngredient.getQuantity() < ingredient.getQuantity()) {
                                            isCookable = false;
                                            break;
                                        }
                                    }

                                    if (isCookable) {
                                        // Aggiungi la ricetta alla lista delle cookable
                                        UserRecipeUtils userRecipe = new UserRecipeUtils(recipe, userId);
                                        cookableRecipes.add(userRecipe);
                                    }

                                    // Incrementa il contatore delle ricette processate
                                    if (processedRecipes.incrementAndGet() == globalRecipes.size()) {
                                        // Se tutte le ricette sono state processate, restituisci la lista
                                        callback.onSuccess(cookableRecipes);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    callback.onFailure(e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    public void loadNewerRecipes(FirestoreCallback<List<Recipe>> callback) {
        // Passa l'errore al chiamante
        db.collection("recipes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipes.add(recipe);
                    }
                    callback.onSuccess(recipes); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }
    public void loadPopularRecipes(FirestoreCallback<List<Recipe>> callback) {
        // Passa l'errore al chiamante
        db.collection("recipes")
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipes.add(recipe);
                    }
                    callback.onSuccess(recipes); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }

}


