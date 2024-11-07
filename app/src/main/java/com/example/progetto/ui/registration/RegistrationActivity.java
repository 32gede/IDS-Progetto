package com.example.progetto.ui.registration;

import static com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED;
import static com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.NETWORK_ERROR;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.progetto.R;
import com.example.progetto.data.model.UserRepository;
import com.example.progetto.databinding.ActivityRegistrationBinding;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.login.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class RegistrationActivity extends AppCompatActivity {

    private RegistrationViewModel registrationViewModel;
    private static final String TAG = "RegistrationActivity";

    private GoogleSignInClient mGoogleSignInClient;

    // Define Google Sign-In launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleGoogleSignInResult(task);
                } else {
                    Log.w("LoginActivity", "Google sign-in cancelled or failed.");
                    showToast("Google sign-in cancelled or failed.");
                }
            }
    );
    private void signInWithGoogle() {
        Log.d("LoginActivity", "Initiating Google sign-in");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRegistrationBinding binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Create an instance of UserRepository and pass the necessary context
        UserRepository userRepository = new UserRepository(this); // Initialize this according to your app's needs

        // Create the ViewModelFactory with required dependencies
        RegistrationViewModelFactory factory = new RegistrationViewModelFactory(userRepository, this);

        // Initialize the ViewModel using the factory
        registrationViewModel = new ViewModelProvider(this, factory).get(RegistrationViewModel.class);

        // Configure Google Sign-In with correct Web Client ID
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Setup UI elements and observe ViewModel as before
        final EditText usernameEditText = binding.email;
        final EditText passwordEditText = binding.password;
        final Button registrationButton = binding.registration;
        final SignInButton googleRegistrationButton = binding.googleRegistration;
        final ProgressBar loadingProgressBar = binding.loading;

        // Observe form state for enabling/disabling registration button
        registrationViewModel.getRegistrationFormState().observe(this, registrationFormState -> {
            if (registrationFormState == null) return;
            registrationButton.setEnabled(registrationFormState.isDataValid());
            if (registrationFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(registrationFormState.getUsernameError()));
            }
            if (registrationFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(registrationFormState.getPasswordError()));
            }
        });
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                registrationViewModel.loginDataChanged(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registrationViewModel.register(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        // Observe registration result for success or failure
        registrationViewModel.getRegistrationResult().observe(this, registrationResult -> {
            if (registrationResult == null) return;
            loadingProgressBar.setVisibility(View.GONE);

            if (registrationResult.getError() != null) {
                showRegistrationFailed(registrationResult.getError());
            } else if (registrationResult.getSuccess() != null) {
                updateUiWithUser(registrationResult.getSuccess());
                Intent homeIntent = new Intent(RegistrationActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                finish();
            }
        });

        // Register user with email and password
        registrationButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            registrationViewModel.register(
                    usernameEditText.getText().toString(),
                    passwordEditText.getText().toString()
            );
        });

        // Register with Google account
        googleRegistrationButton.setOnClickListener(v -> registerWithGoogle());
    }

    private void registerWithGoogle() {
        Log.d(TAG, "Initiating Google sign-in for registration");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }


    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d("LoginActivity", "Google sign-in succeeded for: " + account.getEmail());
            registrationViewModel.loginWithGoogle(account)
                    .addOnCompleteListener(this, loginTask -> {
                        if (loginTask.isSuccessful()) {
                            Log.d("LoginActivity", "Google sign-in authentication succeeded.");
                            showToast("Login with Google successful!");

                            // Navigate to HomeActivity directly
                            Intent homeIntent = new Intent(RegistrationActivity.this, HomeActivity.class);
                            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(homeIntent);
                            finish(); // Close LoginActivity after starting HomeActivity

                        } else {
                            Log.e("LoginActivity", "Google sign-in failed: " +
                                    loginTask.getException().getMessage());
                            showToast("Login with Google failed.");
                        }
                    });

        } catch (ApiException e) {
            Log.e("LoginActivity", "Google sign-in failed with error code: " + e.getStatusCode(), e);
            showToast("Google sign-in failed: " + e.getMessage());
        }
    }


    private void updateUiWithUser(RegisteredUserView model) {
        String welcome = getString(R.string.welcome) + " " + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showRegistrationFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
