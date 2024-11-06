package com.example.progetto.ui.registration;

import android.content.Context;
import android.util.Log;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.progetto.R;
import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.data.model.UserRepository;
import com.example.progetto.ui.login.LoginFormState;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegistrationViewModel extends ViewModel {

    private static final String TAG = "RegistrationViewModel";
    private final MutableLiveData<RegistrationFormState> registrationFormState = new MutableLiveData<>();
    private final MutableLiveData<RegistrationResult> registrationResult = new MutableLiveData<>();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final Context context;
    private final UserRepository userRepository;

    // Constructor that takes UserRepository and Context
    public RegistrationViewModel(UserRepository userRepository, Context context) {
        this.userRepository = userRepository;
        this.context = context;
    }

    public Task<AuthResult> loginWithGoogle(GoogleSignInAccount account) {
        MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

        if (account == null) {
            Log.e(TAG, "GoogleSignInAccount is null");
            return Tasks.forException(new Exception("GoogleSignInAccount is null"));
        }

        // Log per debugging
        Log.d(TAG, "Attempting login with Google for account: " + account.getEmail());

        LoginUtils.saveGoogleLoginState(context, true); // Salva lo stato di login
        return userRepository.loginWithGoogle(account.getIdToken());
    }

    public LiveData<RegistrationFormState> getRegistrationFormState() {
        return registrationFormState;
    }

    public LiveData<RegistrationResult> getRegistrationResult() {
        return registrationResult;
    }
    public void loginDataChanged(String username, String password) {
        Log.d(TAG, "Login data changed. Username: " + username + ", Password: [PROTECTED]");

        if (!isUserNameValid(username)) {
            registrationFormState.setValue(new RegistrationFormState(R.string.invalid_username, null));
            Log.d(TAG, "Invalid username: " + username);
        } else if (!isPasswordValid(password)) {
            registrationFormState.setValue(new RegistrationFormState(null, R.string.invalid_password));
            Log.d(TAG, "Invalid password");
        } else {
            registrationFormState.setValue(new RegistrationFormState(true));
            Log.d(TAG, "Login data is valid");
        }
    }

    public void register(String email, String password) {
        if (!isUserNameValid(email) || !isPasswordValid(password)) {
            registrationResult.setValue(new RegistrationResult(R.string.registration_failed));
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email registration successful for user: " + email);
                        LoginUtils.saveGoogleLoginState(context, true);
                        registrationResult.setValue(new RegistrationResult(new RegisteredUserView(email)));
                    } else {
                        Log.e(TAG, "Email registration failed", task.getException());
                        registrationResult.setValue(new RegistrationResult(R.string.registration_failed));
                    }
                });
    }

    /**
     * Registers a new user with Google account using Firebase.
     */
    public Task<AuthResult> registerWithGoogle(GoogleSignInAccount account) {
        if (account == null) {
            Log.e(TAG, "GoogleSignInAccount is null");
            return Tasks.forException(new Exception("GoogleSignInAccount is null"));
        }

        Log.d(TAG, "Attempting Google registration for account: " + account.getEmail());

        String idToken = account.getIdToken();
        if (idToken == null) {
            Log.e(TAG, "ID token is null for GoogleSignInAccount");
            return Tasks.forException(new Exception("ID token is null"));
        }

        // Create a credential for Firebase Authentication
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Save login state upon successful registration with Google
                        Log.d(TAG, "Google registration successful for: " + account.getEmail());
                        LoginUtils.saveGoogleLoginState(context, true);
                        registrationResult.setValue(new RegistrationResult(new RegisteredUserView(account.getEmail())));
                    } else {
                        Log.e(TAG, "Google registration failed", task.getException());
                        registrationResult.setValue(new RegistrationResult(R.string.registration_failed));
                    }
                });
    }
    private boolean isUserNameValid(String username) {
        return username != null && Patterns.EMAIL_ADDRESS.matcher(username).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}
