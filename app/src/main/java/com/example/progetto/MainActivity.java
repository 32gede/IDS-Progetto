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

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        // Prova ad ottenere l'account Google
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        mainViewModel.loginWithGoogle(task); // Usare il ViewModel per il login con Google
                    } catch (ApiException e) {
                        // Log dell'errore di Google Sign-In
                        Log.e("MainActivity", "Google sign-in failed: " + e.getStatusCode());
                        showToast("Google sign-in failed: " + e.getMessage());
                    }
                } else {
                    // Fallimento nell'intent di Google Sign-In
                    Log.w("MainActivity", "Google sign-in intent failed");
                    showToast("Google sign-in intent failed");
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

        // Inizializza Firebase Firestore (se usato)
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

        // Listener per i bottoni
        loginButton.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegistrationActivity.class)));
        logoutButton.setOnClickListener(v -> mainViewModel.logout());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // Configura GoogleSignInOptions
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Osserva i cambiamenti nello stato del login
        mainViewModel.getLoginSuccess().observe(this, success -> {
            if (success) {
                // Aggiorna lo stato di login
                LoginUtils.saveGoogleLoginState(this, true);
                updateUI(); // Aggiorna l'UI per mostrare il logout
                showToast("Login successful");
            } else {
                showToast("Login failed");
            }
        });

        mainViewModel.getLogoutSuccess().observe(this, success -> {
            if (success) {
                showToast("Logout successful");
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
        Log.d("MainActivity", "Stampa qualcosa");
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
