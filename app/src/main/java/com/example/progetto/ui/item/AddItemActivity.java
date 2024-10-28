package com.example.progetto.ui.item;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        etExpiryDate = findViewById(R.id.etExpiryDateField);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);

        // Listener per mostrare il DatePickerDialog quando si clicca su etExpiryDate
        etExpiryDate.setOnClickListener(v -> showDatePickerDialog());

        // Listener per salvare il prodotto
        btnSaveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
    }

    // Metodo per mostrare il DatePickerDialog e inserire la data selezionata
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Formatta la data selezionata come "dd/MM/yyyy"
                    String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    etExpiryDate.setText(selectedDate);
                },
                year, month, day
        );

        // Imposta la data minima a oggi per il selettore di data
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }


    private void saveProduct() {
        String productName = etProductName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String expiryDateText = etExpiryDate.getText().toString().trim();

        // Recupera l'UID dell'utente corrente
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

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
            int month = Integer.parseInt(parts[1]) - 1; // I mesi partono da 0
            int year = Integer.parseInt(parts[2]);
            calendar.set(year, month, day);
            expiryDate = calendar.getTime();
        } catch (Exception e) {
            Toast.makeText(this, "Formato data non valido. Usa 'dd/MM/yyyy'", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crea un oggetto prodotto da salvare in Firestore, inclusivo di UID
        Map<String, Object> product = new HashMap<>();
        product.put("name", productName);
        product.put("quantity", quantity);
        product.put("expiryDate", expiryDate);
        product.put("uid", uid); // Aggiungi l'UID dell'utente

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
