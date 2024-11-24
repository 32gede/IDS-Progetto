package com.example.progetto.ui.fridge;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.progetto.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class CreateProductActivity extends AddProductActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Codice per il selettore immagini

    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private Uri selectedImageUri;

    private EditText productName, productCategory;
    private ImageView productImageView;
    private Button btnSelectImage, saveButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_product);

        // Inizializza Firestore e Storage
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("product_images");

        // Inizializza elementi UI
        productName = findViewById(R.id.editTextProductName);
        productCategory = findViewById(R.id.editTextCategory);
        productImageView = findViewById(R.id.product_image_view);
        btnSelectImage = findViewById(R.id.btn_select_image);
        saveButton = findViewById(R.id.save_button);
        progressBar = findViewById(R.id.progressBar);

        // Listener per selezionare l'immagine
        btnSelectImage.setOnClickListener(v -> openImageChooser());

        // Listener per salvare il prodotto
        saveButton.setOnClickListener(v -> saveProduct());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            productImageView.setImageURI(selectedImageUri); // Mostra l'immagine selezionata
        }
    }

    private void saveProduct() {
        String name = productName.getText().toString().trim();
        String category = productCategory.getText().toString().trim();

        // Validazione dei campi
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Nome e categoria sono obbligatori!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (selectedImageUri != null) {
            // Carica l'immagine e salva il prodotto
            uploadImageAndSaveProduct(name, category);
        } else {
            // Salva il prodotto senza immagine
            saveProductToFirestore(name, category, null);
        }
    }

    private void uploadImageAndSaveProduct(String name, String category) {
        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveProductToFirestore(name, category, imageUrl);
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Errore nel caricamento dell'immagine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProductToFirestore(String name, String category, @Nullable String imageUrl) {
        // Crea una mappa per il prodotto
        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("category", category);
        product.put("imageUrl", imageUrl);
        product.put("timestamp", System.currentTimeMillis());

        // Salva il prodotto su Firestore
        firestore.collection("items")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Prodotto creato con successo!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Errore durante il salvataggio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
