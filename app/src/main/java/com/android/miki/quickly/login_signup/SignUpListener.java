package com.android.miki.quickly.login_signup;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Miki on 8/4/2017.
 */

public interface SignUpListener {
    void onSuccessfulSignUp(FirebaseUser user);
    void onFailedSignUp();
}
