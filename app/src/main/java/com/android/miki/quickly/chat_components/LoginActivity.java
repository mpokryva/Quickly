package com.android.miki.quickly.chat_components;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.miki.quickly.R;
import com.android.miki.quickly.login_signup.LinkAccountsFragment;
import com.android.miki.quickly.login_signup.LoginFragment;
import com.android.miki.quickly.login_signup.LoginListener;
import com.android.miki.quickly.login_signup.SignUpListener;
import com.android.miki.quickly.login_signup.SignUpMethodSelectionFragment;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.utils.FirebaseError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created by mpokr on 5/26/2017.
 */

public class LoginActivity extends AppCompatActivity implements SignUpListener, LoginListener {

    private FirebaseAuth auth;
    private int currentState;
    private final int LOG_IN = 1;
    private final int SIGN_UP = 0;
    private final String LOG_IN_FRAGMENT_TAG = com.facebook.login.LoginFragment.class.getName();
    private final String SIGN_UP_METHOD_FRAGMENT_TAG = SignUpMethodSelectionFragment.class.getName();
    private Button swapLogInSignUpButton;
    CallbackManager callbackManager;
    private static final String TAG = LoginActivity.class.getName();
    private final boolean IS_TESTING = true;
    private RelativeLayout loadingView;
    private RelativeLayout content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        auth = FirebaseAuth.getInstance();

        initCustomFontsAndColors();
        if (IS_TESTING) {
            auth.signOut();
        }
        swapLogInSignUpButton = findViewById(R.id.swap_log_in_sign_up_button);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                content.setVisibility(View.GONE);
                loadingView.setVisibility(View.VISIBLE);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void initCustomFontsAndColors() {
        // Format title TextView.
        TextView tv = findViewById(R.id.activity_title);
        AssetManager am = this.getApplicationContext().getAssets();
        Typeface tf = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "FiraSans-Heavy.otf"));
        tv.setTypeface(tf);
        content = findViewById(R.id.content);
        loadingView = findViewById(R.id.loading_view);
        ProgressBar progressWheel = loadingView.findViewById(R.id.progress_wheel);
        int lightBlue = ContextCompat.getColor(this, R.color.LightBlue); // Color the progress whel light blue.
        progressWheel.getIndeterminateDrawable().setColorFilter(lightBlue, PorterDuff.Mode.MULTIPLY);
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
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.e(TAG, "signInWithCredential:success");
                    FirebaseUser user = auth.getCurrentUser();
                    enterChatRooms(user);
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        Toast.makeText(LoginActivity.this, "Something went wrong... " +
                                "Please try logging in with Facebook again.", Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthUserCollisionException e) {
                        int lightBlue = ContextCompat.getColor(LoginActivity.this, R.color.LightBlue);
                        new MaterialDialog.Builder(LoginActivity.this)
                                .title(R.string.link_accounts_question)
                                .content(R.string.linking_dialog_message)
                                .positiveText(R.string.link_accounts)
                                .positiveColor(lightBlue)
                                .negativeText(R.string.cancel)
                                .negativeColor(lightBlue)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                        LinkAccountsFragment fragment = LinkAccountsFragment.newInstance(credential);
                                        transaction.replace(R.id.fragment_container, fragment);
                                        transaction.addToBackStack(null);
                                        transaction.commitAllowingStateLoss();
                                    }
                                })
                                .show();
                    } catch (Exception e) {
                        FirebaseError unknownError = FirebaseError.unknownError();
                        Toast.makeText(LoginActivity.this, unknownError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    content.setVisibility(View.VISIBLE);
                    loadingView.setVisibility(View.GONE);
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "signInWithCredential:failure", task.getException());
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
                String tag = "";
                Fragment fragmentToInsert = null;
                switch (state) {
                    case LOG_IN:
                        buttonText = "Don't have an account? Sign up.";
                        boldStartIndex = 22;
                        boldEndIndex = buttonText.length();
                        fragmentToInsert = new LoginFragment();
                        tag = LOG_IN_FRAGMENT_TAG;
                        break;
                    case SIGN_UP:
                        buttonText = "Have an account? Log in.";
                        boldStartIndex = 17;
                        boldEndIndex = buttonText.length();
                        fragmentToInsert = new SignUpMethodSelectionFragment();
                        tag = SIGN_UP_METHOD_FRAGMENT_TAG;
                        break;
                }
                if (fragmentToInsert != null) {
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.fragment_container, fragmentToInsert, tag);
                    transaction.commitAllowingStateLoss();
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

    }

    @Override
    public void onSuccessfulLogIn(FirebaseUser user) {
        enterChatRooms(user);
    }

    @Override
    public void requestFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this,
                Arrays.asList("email", "public_profile"));
    }

    @Override
    public void onFailedLogIn() {
        //Toast.makeText(this, "Couldn't log in", Toast.LENGTH_SHORT).show();
    }
}