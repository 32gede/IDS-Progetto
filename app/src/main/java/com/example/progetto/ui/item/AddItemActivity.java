package com.example.progetto.ui.item;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.progetto.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private EditText etProductName, etQuantity, etExpiryDate;
    private ImageView ivProductImage;
    private Button btnSaveProduct, btnSelectImage;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imageUri;
    private String imageUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Inizializza Firestore e Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Trova gli elementi UI
        etProductName = findViewById(R.id.etProductNameField);
        etQuantity = findViewById(R.id.etQuantityField);
        etExpiryDate = findViewById(R.id.etExpiryDateField);
        ivProductImage = findViewById(R.id.ivProductImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);

        // Listener per selezionare l'immagine
        btnSelectImage.setOnClickListener(v -> checkStoragePermission());

        // Listener per mostrare il DatePickerDialog quando si clicca su etExpiryDate
        etExpiryDate.setOnClickListener(v -> showDatePickerDialog());

        // Listener per salvare il prodotto
        btnSaveProduct.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageAndSaveProduct();
            } else {
                saveProduct();
            }
        });
    }

    private void checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Per Android 13 o successivi, usa permesso specifico per immagini
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            } else {
                openImagePicker();
            }
        } else {
            // Per Android 12 o precedenti
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    etExpiryDate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    Toast.makeText(this, "Permesso negato permanentemente. Abilita i permessi dalle impostazioni dell'app.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Permesso negato per accedere alla memoria", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                imageUri = data.getData();
                if (imageUri != null) {
                    ivProductImage.setImageURI(imageUri);
                } else {
                    Toast.makeText(this, "Errore nel selezionare l'immagine.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadImageAndSaveProduct() {
        if (imageUri == null) {
            Toast.makeText(this, "Nessuna immagine selezionata.", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = storage.getReference().child("product_images/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            imageUrl = uri.toString();
                            saveProduct();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Errore durante il recupero dell'URL dell'immagine", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Errore durante il caricamento dell'immagine", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProduct() {
        String productName = etProductName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String expiryDateText = etExpiryDate.getText().toString().trim();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

        Date expiryDate;
        try {
            Calendar calendar = Calendar.getInstance();
            String[] parts = expiryDateText.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year = Integer.parseInt(parts[2]);
            calendar.set(year, month, day);
            expiryDate = calendar.getTime();
        } catch (Exception e) {
            Toast.makeText(this, "Formato data non valido. Usa 'dd/MM/yyyy'", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> product = new HashMap<>();
        product.put("name", productName);
        product.put("quantity", quantity);
        product.put("expiryDate", expiryDate);
        product.put("uid", uid);
        if (imageUrl != null) {
            product.put("imageUrl", imageUrl);
        }

        db.collection("items")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Prodotto aggiunto con successo", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Errore nell'aggiunta del prodotto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
