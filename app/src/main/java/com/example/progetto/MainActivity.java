package com.example.progetto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.ui.login.LoginActivity;
import com.example.progetto.ui.registration.RegistrationActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private GoogleSignInClient mGoogleSignInClient;

    private Button loginButton;
    private Button registerButton;
    private Button logoutButton;
    private SignInButton googleSignInButton;
    private FirebaseFirestore db;

    // Unifica ActivityResultLauncher per Login e Registrazione
    private final ActivityResultLauncher<Intent> authLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Chiama updateUI quando torna dal login o registrazione
                    updateUI();
                } else {
                    // Gestisci il fallimento
                    Log.w("MainActivity", "Autenticazione fallita o annullata");
                    showToast("Autenticazione fallita o annullata");
                }
            }
    );

    // ActivityResultLauncher per Google Sign-In
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        mainViewModel.loginWithGoogle(task);  // Gestisci il login tramite Google
                    } catch (ApiException e) {
                        Log.e("MainActivity", "Google sign-in failed: " + e.getStatusCode());
                        showToast("Google sign-in fallito: " + e.getMessage());
                    }
                } else {
                    Log.w("MainActivity", "Google sign-in fallito");
                    showToast("Google sign-in fallito");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        // Gestione dei margini di sistema per Android 12+
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inizializza Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Inizializza il ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Inizializza i bottoni
        loginButton = findViewById(R.id.Login);
        registerButton = findViewById(R.id.Registration);
        logoutButton = findViewById(R.id.Logout);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        // Aggiorna la UI in base allo stato di login
        updateUI();

        loginButton.setOnClickListener(v -> {
            // Controlla se l'utente è già loggato
            if (!LoginUtils.isLoggedIn(this)) {
                // Avvia LoginActivity utilizzando authLauncher
                Intent loginIntent = new Intent(this, LoginActivity.class);
                authLauncher.launch(loginIntent);
            }
        });

        registerButton.setOnClickListener(v -> {
            // Controlla se l'utente è già loggato
            if (!LoginUtils.isLoggedIn(this)) {
                // Avvia RegistrationActivity utilizzando authLauncher
                Intent registrationIntent = new Intent(this, RegistrationActivity.class);
                authLauncher.launch(registrationIntent);
            }
        });

        logoutButton.setOnClickListener(v -> mainViewModel.logout());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // Configura GoogleSignInOptions
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Osserva i cambiamenti dello stato di logout
        mainViewModel.getLogoutSuccess().observe(this, success -> {
            if (success) {
                showToast("Logout avvenuto con successo");
                LoginUtils.saveGoogleLoginState(this, false); // Aggiorna lo stato di logout
                updateUI(); // Aggiorna l'UI per mostrare i bottoni di login
            }
        });
    }

    // Metodo per avviare il processo di Google Sign-In
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    // Metodo per aggiornare l'UI in base allo stato di login
    private void updateUI() {
        boolean loggedIn = LoginUtils.isLoggedIn(this);
        Log.d("MainActivity", "Logged in state: " + loggedIn); // Log dello stato di login
        loginButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        registerButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        logoutButton.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        googleSignInButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
    }

    // Metodo per mostrare un Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
