package com.example.progetto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.data.model.UserRepository;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private UserRepository userRepository;
    private Button loginButton;
    private Button registerButton;
    private Button logoutButton;
    private SignInButton googleSignInButton;
    private FirebaseFirestore db;

    // Step 1: Create the ActivityResultLauncher for Google Sign-In
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                } else {
                    Log.w("MainActivity", "Google sign-in failed");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();

        userRepository = new UserRepository(this);

        loginButton = findViewById(R.id.Login);
        registerButton = findViewById(R.id.Registration);
        logoutButton = findViewById(R.id.Logout);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        updateUI();

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            userRepository.logout();
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                LoginUtils.clearLoginState(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });

        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button addFoodButton = findViewById(R.id.addFoodButton);
        addFoodButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
            startActivity(intent);
        });
    }

    // Step 2: Updated Google Sign-In method
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);  // Launch using the new launcher
    }

    // Step 3: Handle Google Sign-In result with Firebase Authentication
    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                Log.d("MainActivity", "Google account: " + account.getEmail());

                // Ottieni le credenziali di autenticazione per Firebase
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                // Firebase Authentication con Google
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(this, task1 -> {
                            if (task1.isSuccessful()) {
                                // Sign-in avvenuto con successo
                                Log.d("MainActivity", "signInWithCredential:success");
                                LoginUtils.saveGoogleLoginState(this, true);
                                updateUI();
                            } else {
                                // Gestisci il fallimento della sign-in
                                Log.w("MainActivity", "signInWithCredential:failure", task1.getException());
                            }
                        });
            }
        } catch (ApiException e) {
            Log.w("MainActivity", "Google sign in failed: " + e.getStatusCode(), e);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (LoginUtils.isLoggedIn(this)) {
            loginButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            googleSignInButton.setVisibility(View.GONE);
        } else {
            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            googleSignInButton.setVisibility(View.VISIBLE);
        }
    }

}
