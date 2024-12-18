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
import java.util.concurrent.atomic.AtomicBoolean;
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
    // Firestore.java

    public void removeUserProduct(UserProductUtils userProduct, FirestoreCallback<Void> callback) {
        if (userProduct.getProductId() == null) {
            callback.onFailure(new IllegalArgumentException("Provided document path must not be null."));
            return;
        }

        db.collection("userProducts").document(userProduct.getProductId()) // Assumendo che `userProduct` abbia un campo `id` con il documento Firestore
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getUserProducts(String id, FirestoreCallback<List<UserProductUtils>> callback) {
        // Passa l'errore al chiamante
        db.collection("user_products")
                .whereEqualTo("userId", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProductUtils> userProducts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserProductUtils product = document.toObject(UserProductUtils.class);
                        userProducts.add(product);
                    }
                    callback.onSuccess(userProducts); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void removeSelectedIngredient(String id, FirestoreCallback<Void> callback) {
        // Passa l'errore al chiamante
        db.collection("SelectedIngredient")
                .whereEqualTo("recipeId", id)
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
        // Controlla se l'oggetto contiene già una chiave "id"
        String documentId;
        if (object.containsKey("id")) {
            // Usa l'ID già esistente
            documentId = (String) object.get("id");
        } else {
            // Genera un nuovo ID se non è presente
            documentId = db.collection(directory).document().getId();
            object.put("id", documentId);
        }

        Log.d("Firestore", "Adding/Updating document in " + directory + " with ID: " + documentId);
        Log.d("Firestore", "Document: " + object);

        // Usa set con il documentId corretto per creare o aggiornare il documento
        db.collection(directory)
                .document(documentId) // Usa l'ID specificato
                .set(object) // Salva l'oggetto Firestore, sovrascrivendo se esiste già
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "DocumentSnapshot successfully added/updated with ID: " + documentId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding/updating document", e);
                });

        return documentId; // Restituisce l'ID del documento usato
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

    public void loadUserRecipes(String userId, FirestoreCallback<List<Recipe>> callback) {
        db.collection("recipes_user")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No recipes found, return an empty list
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<Recipe> recipes = new ArrayList<>();
                    AtomicInteger pendingTasks = new AtomicInteger(queryDocumentSnapshots.size());
                    AtomicBoolean hasFailureOccurred = new AtomicBoolean(false);

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserRecipeUtils recipeUtil = document.toObject(UserRecipeUtils.class);

                        getRecipe(recipeUtil.getDocumentId(), new FirestoreCallback<Recipe>() {
                            @Override
                            public void onSuccess(Recipe data) {
                                synchronized (recipes) {
                                    recipes.add(data);
                                }
                                if (pendingTasks.decrementAndGet() == 0 && !hasFailureOccurred.get()) {
                                    callback.onSuccess(recipes);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                hasFailureOccurred.set(true);
                                callback.onFailure(e);
                            }
                        });
                    }
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

    public void getRecipe(String recipeId, FirestoreCallback<Recipe> callback) {
        // Passa l'errore al chiamante
        db.collection("recipes")
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Recipe recipe = documentSnapshot.toObject(Recipe.class);
                    callback.onSuccess(recipe); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void loadCookableRecipes(String userId, FirestoreCallback<List<Recipe>> callback) {
        getUserIngredients(userId, new FirestoreCallback<List<UserProductUtils>>() {
            @Override
            public void onSuccess(List<UserProductUtils> userIngredients) {
                loadGlobalRecipes(new FirestoreCallback<List<Recipe>>() {
                    @Override
                    public void onSuccess(List<Recipe> globalRecipes) {
                        List<Recipe> cookableRecipes = new ArrayList<>();
                        AtomicInteger processedRecipes = new AtomicInteger(0); // Contatore per tracciare le ricette elaborate

                        if (globalRecipes.isEmpty()) {
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
                                        cookableRecipes.add(recipe);
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


    public void removeUserRecipe(Recipe recipe, FirestoreCallback<Void> callback) {
        // Passa l'errore al chiamante
        db.collection("recipes_user")
                .document(recipe.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void deleteRecipe(String id, FirestoreCallback<Void> recipeEditActivity) {
        db.collection("recipes")
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    recipeEditActivity.onSuccess(null);
                })
                .addOnFailureListener(recipeEditActivity::onFailure);
    }

    public void updateRecipe(String id, Recipe recipe, List<SelectedIngredientRecipeUtils> appo, FirestoreCallback<Void> firestoreCallback) {
        deleteRecipe(id, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d("Firestore", "Recipe deleted successfully");
                addSomething(recipe.toMap(), "recipes");

                removeSelectedIngredient(id, new FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Log.d("Firestore", "Selected ingredients removed successfully");
                        addSelectedIngredient(appo, new FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                firestoreCallback.onSuccess(null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                firestoreCallback.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        firestoreCallback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                firestoreCallback.onFailure(e);
            }
        });
    }

    public void loadStores(FirestoreCallback<List<StoreUtils>> callback) {
        // Passa l'errore al chiamante
        db.collection("stores")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<StoreUtils> stores = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        StoreUtils store = document.toObject(StoreUtils.class);
                        stores.add(store);
                    }
                    callback.onSuccess(stores); // Passa il risultato al chiamante
                })
                .addOnFailureListener(callback::onFailure);
    }
}


