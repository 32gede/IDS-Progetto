package com.example.progetto.ui.login;

import android.app.Activity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.progetto.R;
import com.example.progetto.data.model.UserRepository;
import com.example.progetto.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private GoogleSignInClient mGoogleSignInClient;  // Define GoogleSignInClient

    // Define launcher for Google sign-in intent
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        loginViewModel.loginWithGoogle(account)  // Assumes loginWithGoogle exists in LoginViewModel
                                .addOnCompleteListener(this, loginTask -> {
                                    if (loginTask.isSuccessful()) {
                                        Log.d("LoginActivity", "Google sign-in succeeded.");
                                        showToast("Login with Google successful!");
                                    } else {
                                        Log.e("LoginActivity", "Google sign-in failed: " +
                                                loginTask.getException().getMessage());
                                        showToast("Login with Google failed.");
                                    }
                                });
                    } catch (ApiException e) {
                        Log.e("LoginActivity", "Google sign-in failed: " + e.getStatusCode(), e);
                        showToast("Google sign-in failed: " + e.getMessage());
                    }
                } else {
                    Log.w("LoginActivity", "Google sign-in cancelled or failed.");
                    showToast("Google sign-in cancelled or failed.");
                }
            }
    );

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize UserRepository and ViewModel
        UserRepository userRepository = new UserRepository(this);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(userRepository, this))
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.email;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        // Observe LoginFormState to handle form validation
        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Observe LoginResult to handle login feedback
        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("user_display_name", loginResult.getSuccess().getDisplayName());
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish(); // Close LoginActivity after successful login
                }
            }
        });

        // Add text change listeners for form validation
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        // Set login button listener
        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loginViewModel.login(
                    usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
