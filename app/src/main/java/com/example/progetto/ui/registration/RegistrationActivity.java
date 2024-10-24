package com.example.progetto.ui.registration;

import android.app.Activity;
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
import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.data.model.UserRepository;
import com.example.progetto.databinding.ActivityRegistrationBinding;

public class RegistrationActivity extends AppCompatActivity {

    private RegistrationViewModel registrationViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.progetto.databinding.ActivityRegistrationBinding binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserRepository userRepository = new UserRepository(this);
        registrationViewModel = new ViewModelProvider(this, new RegistrationViewModelFactory(userRepository, this))
                .get(RegistrationViewModel.class);

        final EditText usernameEditText = binding.email;
        final EditText passwordEditText = binding.password;
        final Button registrationButton = binding.registration;
        final ProgressBar loadingProgressBar = binding.loading;

        // Osserva lo stato del form di registrazione
        registrationViewModel.getRegistrationFormState().observe(this, new Observer<RegistrationFormState>() {
            @Override
            public void onChanged(@Nullable RegistrationFormState registrationFormState) {
                if (registrationFormState == null) {
                    return;
                }
                registrationButton.setEnabled(registrationFormState.isDataValid());
                if (registrationFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(registrationFormState.getUsernameError()));
                }
                if (registrationFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(registrationFormState.getPasswordError()));
                }
            }
        });

        // Osserva il risultato della registrazione
        registrationViewModel.getRegistrationResult().observe(this, new Observer<RegistrationResult>() {
            @Override
            public void onChanged(@Nullable RegistrationResult registrationResult) {
                if (registrationResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);

                // Gestione errore nella registrazione
                if (registrationResult.getError() != null) {
                    showRegistrationFailed(registrationResult.getError());
                    return;  // Non proseguire se c'è stato un errore
                }

                // Gestione registrazione riuscita
                if (registrationResult.getSuccess() != null) {
                    updateUiWithUser(registrationResult.getSuccess());

                    // Log per verificare il login immediato dopo la registrazione
                    Log.d("RegistrationActivity", "Tentativo di login automatico con: " + usernameEditText.getText().toString());


                    Log.d("RegistrationActivity", "Utente loggato: " + userRepository.getLoggedInUser());

                    // Salva lo stato di login
                    LoginUtils.saveLoginState(RegistrationActivity.this, true, usernameEditText.getText().toString());

                    // Imposta il risultato da restituire a chi ha avviato l'Activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("user_display_name", registrationResult.getSuccess().getDisplayName());
                    setResult(Activity.RESULT_OK, resultIntent);

                    // Chiudi la RegistrationActivity dopo una registrazione di successo
                    finish();
                }
            }
        });

        // Listener per aggiornamenti di testo
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Ignora
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Ignora
            }

            @Override
            public void afterTextChanged(Editable s) {
                registrationViewModel.registrationDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        // Listener per la pressione del tasto "Done" sulla tastiera
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    registrationViewModel.register(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        // Listener per il pulsante di registrazione
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                registrationViewModel.register(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }

    private void updateUiWithUser(RegisteredUserView model) {
        String welcome = getString(R.string.welcome) + " " + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showRegistrationFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
