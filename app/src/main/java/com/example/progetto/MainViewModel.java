package com.example.progetto;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.data.model.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = "MainViewModel";
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MainViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    // LiveData per osservare il successo del login
    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    // LiveData per osservare il successo del logout
    public LiveData<Boolean> getLogoutSuccess() {
        return logoutSuccess;
    }

    // Metodo per gestire il login con Google
    // In MainViewModel
    public Task<AuthResult> loginWithGoogle(Task<GoogleSignInAccount> task) {
        MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
        return task.continueWithTask(executor, completedTask -> {
            if (completedTask.isSuccessful()) {
                GoogleSignInAccount account = completedTask.getResult();
                if (account != null) {
                    // Restituisce il Task<AuthResult> per l'accesso con Google
                    LoginUtils.saveGoogleLoginState(getApplication(), true);
                    return userRepository.loginWithGoogle(account.getIdToken());
                }
            }
            throw new Exception("Login con Google fallito");
        });
    }

    // Metodo per gestire il logout
    public void logout() {
        executor.execute(() -> {
            userRepository.logout();
            LoginUtils.clearLoginState(getApplication());
            logoutSuccess.postValue(true);
            Log.d(TAG, "Logout avvenuto con successo.");
        });
    }

    // Pulizia delle risorse quando il ViewModel viene distrutto
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
