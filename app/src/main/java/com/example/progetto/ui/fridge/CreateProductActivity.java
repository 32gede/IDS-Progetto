package com.example.progetto.ui.fridge;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateProductActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private EditText editTextProductName, editTextCategory, editTextDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_product);

        // Inizializza Firestore
        firestore = FirebaseFirestore.getInstance();

        // Inizializza elementi UI
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextCategory = findViewById(R.id.editTextCategory);
        editTextDescription = findViewById(R.id.editTextDescription);

        // Imposta il pulsante di salvataggio
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        // Recupera i dati dagli EditText
        String name = editTextProductName.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // Controllo di validità
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Nome e categoria sono obbligatori!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crea il prodotto da salvare
        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("category", category);
        product.put("description", description);
        product.put("timestamp", System.currentTimeMillis());

        // Salva su Firestore
        firestore.collection("items")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Prodotto creato con successo!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Errore durante il salvataggio.", Toast.LENGTH_SHORT).show();
                });
    }
}
