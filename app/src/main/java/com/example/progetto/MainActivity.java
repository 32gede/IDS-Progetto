package com.example.progetto;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.login.LoginActivity;
import com.example.progetto.ui.registration.RegistrationActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private GoogleSignInClient mGoogleSignInClient;

    private Button loginButton;
    private Button registerButton;

    private final ActivityResultLauncher<Intent> authLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    updateUI();
                } else {
                    Log.w("MainActivity", "Autenticazione fallita o annullata");
                    showToast("Autenticazione fallita o annullata");
                }
            }
    );

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        mainViewModel.loginWithGoogle(task).addOnCompleteListener(this, loginTask -> {
                            if (loginTask.isSuccessful()) {
                                Log.d("MainActivity", "Google sign-in riuscito.");
                                showToast("Login con Google effettuato con successo!");
                                updateUI();
                            } else {
                                Log.e("MainActivity", "Google sign-in fallito: " +
                                        loginTask.getException().getMessage());
                                showToast("Login con Google fallito.");
                            }
                        });
                    } catch (ApiException e) {
                        Log.e("MainActivity", "Google sign-in failed: " + e.getStatusCode(), e);
                        showToast("Google sign-in fallito: " + e.getMessage());
                    }
                } else {
                    Log.w("MainActivity", "Google sign-in cancellato dall'utente o non riuscito.");
                    showToast("Google sign-in cancellato o non riuscito.");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (LoginUtils.isLoggedIn(this)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.imageViewGif);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("decepcao-pensativo.gif");

        storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> Glide.with(MainActivity.this)
                        .asGif()
                        .load(uri)
                        .into(imageView))
                .addOnFailureListener(Throwable::printStackTrace);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        loginButton = findViewById(R.id.Login);
        registerButton = findViewById(R.id.Registration);

        updateUI();
        checkAndDeleteExpiredNotifications();

        loginButton.setOnClickListener(v -> {
            if (!LoginUtils.isLoggedIn(this)) {
                Intent loginIntent = new Intent(this, LoginActivity.class);
                authLauncher.launch(loginIntent);
            }
        });

        registerButton.setOnClickListener(v -> {
            if (!LoginUtils.isLoggedIn(this)) {
                Intent registrationIntent = new Intent(this, RegistrationActivity.class);
                authLauncher.launch(registrationIntent);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void updateUI() {
        boolean loggedIn = LoginUtils.isLoggedIn(this);
        Log.d("MainActivity", "Logged in state: " + loggedIn);
        loginButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        registerButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void checkAndDeleteExpiredNotifications() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Calendar today = Calendar.getInstance();
        today.add(Calendar.DAY_OF_YEAR, -7); // One week before today

        firestore.collection("Notification")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String expiryDateStr = document.getString("expiryDate");
                        if (expiryDateStr != null) {
                            try {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Calendar expiryDate = Calendar.getInstance();
                                expiryDate.setTime(dateFormat.parse(expiryDateStr));
                                expiryDate.add(Calendar.DAY_OF_YEAR, 7); // One week after expiry

                                if (expiryDate.before(today)) {
                                    firestore.collection("Notification").document(document.getId()).delete()
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Deleted expired notification: " + document.getId()))
                                            .addOnFailureListener(e -> Log.e(TAG, "Error deleting expired notification", e));
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing expiry date", e);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching notifications", e));
    }
}
