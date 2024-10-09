package com.example.progetto.ui.login;

import android.content.Context;
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

    public void login(String username, String password) {
        userRepository.loginUser(username, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String displayName = username.split("@")[0];
                loginResult.setValue(new LoginResult(new LoggedInUserView(displayName)));
                LoginUtils.saveLoginState(context, true, displayName); // Save login state
            } else {
                loginResult.setValue(new LoginResult(R.string.login_failed));
            }
        });
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    private boolean isUserNameValid(String username) {
        return username != null && username.trim().length() > 5;
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}