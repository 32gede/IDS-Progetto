package com.example.progetto.ui.registration;

import static com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.progetto.R;
import com.example.progetto.databinding.ActivityRegistrationBinding;
import com.example.progetto.ui.home.HomeActivity;
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

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                } else {
                    Log.w(TAG, "Google sign-in cancelled or failed.");
                    showToast("Google sign-in cancelled or failed.");
                }
            }
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRegistrationBinding binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registrationViewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);

        // Initialize GoogleSignInClient with the appropriate options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("69630220301-7kuoiisiqn4bn0cf55k74htn0acns9o0.apps.googleusercontent.com")  // Replace with your actual Web Client ID
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        final EditText usernameEditText = binding.email;
        final EditText passwordEditText = binding.password;
        final Button registrationButton = binding.registration;
        final SignInButton googleRegistrationButton = binding.googleRegistration; // Button for Google Sign-In
        final ProgressBar loadingProgressBar = binding.loading;

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

        registrationButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            registrationViewModel.register(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
        });

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
            Log.d(TAG, "Google sign-in succeeded for: " + account.getEmail());

            registrationViewModel.registerWithGoogle(account).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Google registration successful.");
                    showToast("Registration with Google successful!");

                    Intent homeIntent = new Intent(RegistrationActivity.this, HomeActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                    finish();
                } else {
                    Log.e(TAG, "Google registration failed: " + task.getException().getMessage());
                    showToast("Registration with Google failed.");
                }
            });

        } catch (ApiException e) {
            Log.e(TAG, "Google sign-in failed with error code: " + e.getStatusCode(), e);
            if (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                showToast("Google sign-in was cancelled by the user.");
            } else if (e.getStatusCode() == GoogleSignInStatusCodes.NETWORK_ERROR) {
                showToast("Network error during Google sign-in. Check your connection.");
            } else {
                showToast("Google sign-in failed: " + e.getMessage());
            }
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
