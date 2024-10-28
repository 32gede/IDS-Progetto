package com.example.progetto.ui.item;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.core.content.FileProvider;

import com.example.progetto.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_STORAGE_PERMISSION = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 4;

    private EditText etProductName, etQuantity, etExpiryDate;
    private ImageView ivProductImage;
    private Button btnSaveProduct, btnSelectImage;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imageUri;
    private String imageUrl;
    private String currentPhotoPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        etProductName = findViewById(R.id.etProductNameField);
        etQuantity = findViewById(R.id.etQuantityField);
        etExpiryDate = findViewById(R.id.etExpiryDateField);
        ivProductImage = findViewById(R.id.ivProductImage);
        btnSelectImage = findViewById(R.id.btnSelectImage); // Pulsante per scattare la foto
        btnSaveProduct = findViewById(R.id.btnSaveProduct);

        btnSelectImage.setOnClickListener(v -> checkStoragePermission());
        etExpiryDate.setOnClickListener(v -> showDatePickerDialog());

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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            } else {
                openImagePicker();
            }
        } else {
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

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("AddItemActivity", "Errore durante la creazione del file immagine", ex);
                Toast.makeText(this, "Errore durante la creazione del file immagine", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                try {
                    imageUri = FileProvider.getUriForFile(this, "com.example.progetto.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (Exception e) {
                    Log.e("AddItemActivity", "Errore durante l'apertura della fotocamera", e);
                    Toast.makeText(this, "Errore durante l'apertura della fotocamera", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
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
                Toast.makeText(this, "Permesso negato per accedere alla memoria", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permesso negato per usare la fotocamera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                imageUri = data.getData();
                ivProductImage.setImageURI(imageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                ivProductImage.setImageURI(imageUri);
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
