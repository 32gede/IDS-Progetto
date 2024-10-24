package com.example.progetto.ui.login;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.progetto.R;
import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.data.model.UserRepository;

public class LoginViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final Context context;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();

    private static final String TAG = "LoginViewModel"; // Tag per i log

    public LoginViewModel(UserRepository userRepository, Context context) {
        this.userRepository = userRepository;
        this.context = context;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    // Metodo per effettuare il login
    public void login(String username, String password) {
        Log.d(TAG, "Attempting login for user: " + username); // Log per il tentativo di login

        // Chiamata al repository per effettuare il login
        userRepository.loginUser(username, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String displayName = username.split("@")[0]; // Estrai il nome visualizzato dall'email
                loginResult.setValue(new LoginResult(new LoggedInUserView(displayName))); // Successo nel login

                // Salva lo stato di login
                LoginUtils.saveLoginState(context, true, displayName);
                Log.d(TAG, "Login successful, saved state for user: " + displayName); // Log per il successo


            } else {
                loginResult.setValue(new LoginResult(R.string.login_failed)); // Login fallito
                Log.d(TAG, "Login failed for user: " + username); // Log per il fallimento
            }
        });
    }

    // Metodo per validare i dati inseriti nel form
    public void loginDataChanged(String username, String password) {
        Log.d(TAG, "Login data changed. Username: " + username + ", Password: [PROTECTED]"); // Log per monitorare i cambiamenti

        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
            Log.d(TAG, "Invalid username: " + username); // Log in caso di errore nel nome utente
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
            Log.d(TAG, "Invalid password"); // Log in caso di errore nella password
        } else {
            loginFormState.setValue(new LoginFormState(true)); // I dati sono validi
            Log.d(TAG, "Login data is valid"); // Log per dati validi
        }
    }

    // Verifica la validità del nome utente
    private boolean isUserNameValid(String username) {
        return username != null && username.trim().length() > 5; // Nome utente valido se ha più di 5 caratteri
    }

    // Verifica la validità della password
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5; // Password valida se ha più di 5 caratteri
    }
}
