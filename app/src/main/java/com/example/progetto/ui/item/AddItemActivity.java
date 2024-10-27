package com.example.progetto.ui.item;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {

    private EditText etProductName, etQuantity, etExpiryDate;
    private Button btnSaveProduct;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Inizializza Firestore
        db = FirebaseFirestore.getInstance();

        // Trova gli elementi UI
        etProductName = findViewById(R.id.etProductNameField);
        etQuantity = findViewById(R.id.etQuantityField);
        etExpiryDate = findViewById(R.id.etExpiryDateField); // Aggiungi un campo data di scadenza personalizzato
        btnSaveProduct = findViewById(R.id.btnSaveProduct);

        // Listener per salvare il prodotto
        btnSaveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
    }

    private void saveProduct() {
        String productName = etProductName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String expiryDateText = etExpiryDate.getText().toString().trim();

        // Controlla se tutti i campi sono stati compilati
        if (productName.isEmpty() || quantityText.isEmpty() || expiryDateText.isEmpty()) {
            Toast.makeText(this, "Tutti i campi sono obbligatori", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantità non valida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Converte la data di scadenza in un oggetto Date (esempio di input: "dd/MM/yyyy")
        Date expiryDate;
        try {
            Calendar calendar = Calendar.getInstance();
            String[] parts = expiryDateText.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Mesi vanno da 0 a 11
            int year = Integer.parseInt(parts[2]);
            calendar.set(year, month, day);
            expiryDate = calendar.getTime();
        } catch (Exception e) {
            Toast.makeText(this, "Formato data non valido. Usa 'dd/MM/yyyy'", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crea un oggetto prodotto da salvare in Firestore
        Map<String, Object> product = new HashMap<>();
        product.put("name", productName);
        product.put("quantity", quantity);
        product.put("expiryDate", expiryDate);

        // Aggiungi il prodotto a Firebase Firestore
        db.collection("items")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Prodotto aggiunto con successo", Toast.LENGTH_SHORT).show();
                    finish(); // Chiude l'activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Errore nell'aggiunta del prodotto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
