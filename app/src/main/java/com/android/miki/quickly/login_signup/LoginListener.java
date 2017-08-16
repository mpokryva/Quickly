package com.android.miki.quickly.login_signup;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Miki on 8/5/2017.
 */

public interface LoginListener {
    void onSuccessfulLogIn(FirebaseUser user);
    void requestFacebookLogin();
    void onFailedLogIn();
}
