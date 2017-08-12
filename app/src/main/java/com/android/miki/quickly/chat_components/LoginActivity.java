package com.android.miki.quickly.chat_components;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Range;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.login_signup.LogInFragment;
import com.android.miki.quickly.login_signup.LogInListener;
import com.android.miki.quickly.login_signup.SignUpFragment;
import com.android.miki.quickly.login_signup.SignUpListener;
import com.android.miki.quickly.models.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by mpokr on 5/26/2017.
 */

public class LoginActivity extends AppCompatActivity implements SignUpListener, LogInListener {

    private FirebaseAuth auth;
    private int currentState;
    private static final int LOG_IN = 1;
    private static final int SIGN_UP = 0;
    private Button swapLogInSignUpButton;
    private Button fbLoginButton;
    CallbackManager callbackManager;
    private static final String TAG = LoginActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_sign_up);
        auth = FirebaseAuth.getInstance();
        swapLogInSignUpButton = (Button) findViewById(R.id.swap_log_in_sign_up_button);
        fbLoginButton = (Button) findViewById(R.id.fb_login_button);
        fbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this,
                        Arrays.asList("email", "public_profile"));

            }
        });
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                int i =0;
            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        auth.signOut();
    }


    private void enterChatRooms(FirebaseUser user) {
        Intent chatSelectionIntent = new Intent();
        chatSelectionIntent.setClass(LoginActivity.this, ChatSelectionActivity.class);
        User newUser = new User(user.getDisplayName(), "Temp University");
        chatSelectionIntent.putExtra("user", newUser);
        startActivity(chatSelectionIntent); // Start ChatSelectionActivity
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            enterChatRooms(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Switch between sign up and log in.
        swapLogInSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (currentState) {
                    case LOG_IN:
                        updateCurrentState(SIGN_UP);
                        break;
                    case SIGN_UP:
                        updateCurrentState(LOG_IN);
                        break;
                }
            }
        });
        // Check if user is signed in (non-null) and update UI accordingly. Called once when attached.
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                enterChatRoomsIfLoggedIn(firebaseAuth.getCurrentUser());
            }
        });
    }

    private void enterChatRoomsIfLoggedIn(FirebaseUser user) {
        if (user == null) { // User is not logged in. Prompt log in.
            updateCurrentState(LOG_IN);
        } else { // User is logged in. Enter chat rooms.
            enterChatRooms(user);
        }
    }

    private void updateCurrentState(int state) {
        if (state != LOG_IN && state != SIGN_UP) {
            throw new IllegalArgumentException("State must either be LOG_IN or SIGN_UP");
        } else { // Valid state
            if (state != currentState) {
                currentState = state;
                FragmentManager fm = getSupportFragmentManager();
                SpannableStringBuilder builder = null;
                String buttonText = "";
                int boldStartIndex = 0;
                int boldEndIndex = 0;
                Fragment fragmentToInsert = null;
                switch (state) {
                    case LOG_IN:
                        buttonText = "Don't have an account? Sign up.";
                        boldStartIndex = 22;
                        boldEndIndex = buttonText.length();
                        fragmentToInsert = new LogInFragment();
                        break;
                    case SIGN_UP:
                        buttonText = "Have an account? Log in.";
                        boldStartIndex = 17;
                        boldEndIndex = buttonText.length();
                        fragmentToInsert = new SignUpFragment();
                        break;
                }
                if (fragmentToInsert != null) {
                    fm.beginTransaction().replace(R.id.fragment_container, fragmentToInsert).commit();
                }
                builder = new SpannableStringBuilder(buttonText);
                builder.setSpan(new StyleSpan(Typeface.BOLD), boldStartIndex, boldEndIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                swapLogInSignUpButton.setText(builder);

            }
        }
    }

    @Override
    public void onSuccessfulSignUp(FirebaseUser user) {
        enterChatRooms(user);
    }

    @Override
    public void onFailedSignUp() {
        Toast.makeText(this, "Couldn't sign up", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessfulLogIn(FirebaseUser user) {
        enterChatRooms(user);
    }

    @Override
    public void onFailedLogIn() {
        //Toast.makeText(this, "Couldn't log in", Toast.LENGTH_SHORT).show();
    }
}
