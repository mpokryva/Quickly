package com.android.miki.quickly.login_signup;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.utils.FieldValidator;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.android.miki.quickly.utils.TextValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Miki on 8/5/2017.
 */

public class LoginFragment extends Fragment implements FieldValidator {

    private CustomTextInputLayout emailInputLayout;
    private CustomTextInputLayout passwordInputLayout;
    private static final String TAG = LoginFragment.class.getName();
    private LoginListener callbackToActivity;
    public static final int NORMAL_LOG_IN = 0;
    public static final int LINKING_LOG_IN = 1;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_in, container, false);
        emailInputLayout = view.findViewById(R.id.email_text_input);
        passwordInputLayout = view.findViewById(R.id.password_text_input);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.text_field_error);
        emailInputLayout.setErrorDrawable(drawable);
        passwordInputLayout.setErrorDrawable(drawable);
        Button fbLoginButton = view.findViewById(R.id.fb_login_button);
        fbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbackToActivity.requestFacebookLogin();
            }
        });
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        Button loginButton = view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    final String email = emailInputLayout.getTrimmedText();
                    String password = passwordInputLayout.getTrimmedText();
                    AuthQueryHelper queryHelper = new AuthQueryHelper();
                    queryHelper.logInWith(email, password, new FirebaseListener<FirebaseUser>() {
                        @Override
                        public void onLoading() {

                        }

                        @Override
                        public void onError(FirebaseError error) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            callbackToActivity.onFailedLogIn();
                        }

                        @Override
                        public void onSuccess(FirebaseUser user) {
                            callbackToActivity.onSuccessfulLogIn(user);
                        }
                    });
                }
            }
        });
        TextView forgotPassword = view.findViewById(R.id.forgot_password_textview);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                // Using R.id.fragment_container may be unsafe, but fuck it.
                transaction.replace(R.id.fragment_container, new ForgotPasswordFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            try {
                callbackToActivity = (LoginListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement LoginListener");
            }
        } else {
            Log.d(TAG, "Context in onAttach() is not an activity.");
        }
    }


    /**
     * Validates the email and password fields, and displays appropriate error messages if they are invalid.
     *
     * @return True if the fields are valid, false otherwise.
     */
    public boolean validateFields() {
        String email = emailInputLayout.getTrimmedText();
        String password = passwordInputLayout.getTrimmedText();
        String emailErrorMessage = new TextValidator().isValidEmail(email);
        boolean areAllFieldsValid = true;
        if (emailErrorMessage != null) {
            emailInputLayout.setError(emailErrorMessage);
            areAllFieldsValid = false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Please enter a password.");
            areAllFieldsValid = false;
        }
        return areAllFieldsValid;
    }

}
