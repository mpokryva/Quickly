package com.android.miki.quickly.login_signup;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by mpokr on 8/16/2017.
 */

public class AuthQueryHelper {

    private FirebaseAuth auth;
    private static final String TAG = AuthQueryHelper.class.getName();

    public AuthQueryHelper() {
        auth = FirebaseAuth.getInstance();
    }

    /**
     * It is the caller's responsibility to validate fields.
     *
     * @param email    The email to log in with.
     * @param password The password to log in with.
     * @param listener Callback for results from query.
     */
    public void logInWith(String email, String password, final FirebaseListener<FirebaseUser> listener) {
        listener.onLoading();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    listener.onSuccess(auth.getCurrentUser());
                } else {
                    FirebaseError error;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        error = FirebaseError.userNotFoundForEmail();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        error = FirebaseError.incorrectPassword();
                    } catch (FirebaseNetworkException e) {
                        error = FirebaseError.networkError();
                    } catch (Exception e) {
                        error = FirebaseError.unknownError();
                    }
                    listener.onError(error);
                }
            }
        });
    }

    public void linkCredentials(FirebaseUser user, AuthCredential credential, final FirebaseListener<FirebaseUser> listener) {
        listener.onLoading();
        user.linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    listener.onSuccess(task.getResult().getUser());
                } else {
                    FirebaseError error;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        Log.e(TAG, "Should not happen: " + e.getLocalizedMessage());
                        error = FirebaseError.unknownError();
                    } catch (FirebaseAuthInvalidCredentialsException|FirebaseAuthRecentLoginRequiredException e) {
                        error = FirebaseError.invalidCredential();
                    } catch (FirebaseAuthInvalidUserException e) {
                        error = FirebaseError.invalidUser();
                    } catch (Exception e) {
                        error = FirebaseError.unknownError();
                    }
                    listener.onError(error);
                }
            }
        });
    }
}
