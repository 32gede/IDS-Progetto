package com.example.progetto.ui.login;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.progetto.R;
import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.data.model.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;

public class LoginViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final Context context;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();

    private static final String TAG = "LoginViewModel";

    public LoginViewModel(UserRepository userRepository, Context context) {
        this.userRepository = userRepository;
        this.context = context.getApplicationContext(); // Assicura di usare l'ApplicationContext
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


    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public void login(String username, String password) {
        Log.d(TAG, "Attempting login for user: " + username);

        userRepository.loginUser(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String displayName = username.split("@")[0];
                    loginResult.setValue(new LoginResult(new LoggedInUserView(displayName)));

                    // Salva lo stato di login
                    LoginUtils.saveLoginState(context, true, displayName);
                    Log.d(TAG, "Login successful, saved state for user: " + displayName);
                } else {
                    loginResult.setValue(new LoginResult(R.string.login_failed));
                    Log.e(TAG, "Login failed for user: " + username, task.getException());
                }
            }
        });
    }

    public void loginDataChanged(String username, String password) {
        Log.d(TAG, "Login data changed. Username: " + username + ", Password: [PROTECTED]");

        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
            Log.d(TAG, "Invalid username: " + username);
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
            Log.d(TAG, "Invalid password");
        } else {
            loginFormState.setValue(new LoginFormState(true));
            Log.d(TAG, "Login data is valid");
        }
    }

    private boolean isUserNameValid(String username) {
        return username != null && username.trim().length() > 5;
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}
