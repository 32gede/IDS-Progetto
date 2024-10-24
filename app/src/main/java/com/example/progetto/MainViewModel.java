package com.example.progetto;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.data.model.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>();  // Aggiunto per il logout
    private ExecutorService executor = Executors.newSingleThreadExecutor(); // Thread separato per operazioni lunghe

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

    // Metodo per gestire il login con Google (in un thread separato)
    public void loginWithGoogle(Task<GoogleSignInAccount> task) {
        executor.execute(() -> {
            try {
                GoogleSignInAccount account = Tasks.await(task); // Blocca finché non otteniamo l'account
                if (account != null) {
                    // Effettua il login con l'ID Token dell'account Google
                    userRepository.loginWithGoogle(account.getIdToken()).addOnCompleteListener(resultTask -> {
                        loginSuccess.postValue(resultTask.isSuccessful()); // Aggiorna lo stato del login
                    });
                }
            } catch (Exception e) {
                loginSuccess.postValue(false); // Fallimento del login
            }
        });
    }

    // Metodo per gestire il logout
    public void logout() {
        executor.execute(() -> {
            userRepository.logout(); // Esegui il logout in un thread separato
            LoginUtils.clearLoginState(getApplication()); // Cancella lo stato di login
            logoutSuccess.postValue(true); // Notifica alla UI che il logout è avvenuto
        });
    }
}
