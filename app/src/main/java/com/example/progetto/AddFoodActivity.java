package com.example.progetto;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddFoodActivity extends AppCompatActivity {

    private EditText editTextFoodName, editTextExpiryDate, editTextQuantity;
    private Spinner spinnerCategory;
    private Button buttonAddFood, buttonPickDate;
    private Calendar expiryCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        // Inizializza i componenti
        editTextFoodName = findViewById(R.id.editTextFoodName);
        editTextExpiryDate = findViewById(R.id.editTextExpiryDate);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        buttonAddFood = findViewById(R.id.buttonAddFood);
        buttonPickDate = findViewById(R.id.buttonPickDate);

        // Imposta le categorie nello Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.food_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Imposta il listener per il pulsante della data di scadenza
        buttonPickDate.setOnClickListener(v -> showDatePicker());

        // Listener per aggiungere l'alimento
        buttonAddFood.setOnClickListener(v -> addFood());
    }

    // Mostra il DatePicker per selezionare la data di scadenza
    private void showDatePicker() {
        expiryCalendar = Calendar.getInstance();
        int year = expiryCalendar.get(Calendar.YEAR);
        int month = expiryCalendar.get(Calendar.MONTH);
        int day = expiryCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            expiryCalendar.set(year1, month1, dayOfMonth);
            editTextExpiryDate.setText(year1 + "-" + (month1 + 1) + "-" + dayOfMonth);
        }, year, month, day);

        datePickerDialog.show();
    }

    // Aggiungi l'alimento all'inventario
    private void addFood() {
        String foodName = editTextFoodName.getText().toString();
        String expiryDate = editTextExpiryDate.getText().toString();
        String quantity = editTextQuantity.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        // Verifica se i campi sono validi
        if (TextUtils.isEmpty(foodName) || TextUtils.isEmpty(expiryDate) || TextUtils.isEmpty(quantity)) {
            Toast.makeText(this, "Compila tutti i campi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aggiungi logica per salvare l'alimento in Firebase Firestore o nel database locale
        Log.d("AddFoodActivity", "Alimento aggiunto: " + foodName + ", Scadenza: " + expiryDate +
                ", Quantità: " + quantity + ", Categoria: " + category);

        // Messaggio di successo
        Toast.makeText(this, "Alimento aggiunto!", Toast.LENGTH_SHORT).show();

        // Resetta il form
        editTextFoodName.setText("");
        editTextExpiryDate.setText("");
        editTextQuantity.setText("");
        spinnerCategory.setSelection(0);
    }
}
