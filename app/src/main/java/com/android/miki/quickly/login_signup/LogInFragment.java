package com.android.miki.quickly.login_signup;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.ui.EditTextUtil;
import com.android.miki.quickly.utils.TextValidator;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * Created by Miki on 8/5/2017.
 */

public class LogInFragment extends Fragment {

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button loginButton;
    private static final String TAG = LogInFragment.class.getName();
    private LogInListener callbackToActivity;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_in, container, false);
        emailInputLayout = (TextInputLayout) view.findViewById(R.id.email_text_input);
        passwordInputLayout = (TextInputLayout) view.findViewById(R.id.password_text_input);
        loginButton = (Button) view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    final FirebaseAuth auth = FirebaseAuth.getInstance();
                    String email = EditTextUtil.getTrimmedText(emailInputLayout.getEditText());
                    String password = EditTextUtil.getTrimmedText(passwordInputLayout.getEditText());
                    if (email != null && password != null) {
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    callbackToActivity.onSuccessfulLogIn(auth.getCurrentUser());
                                } else {
                                    Log.d(TAG, "log in failure: ", task.getException());
                                    Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    callbackToActivity.onFailedLogIn();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Something weird happened. Try logging in again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            try {
                callbackToActivity = (LogInListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement LogInListener");
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
    private boolean validateFields() {
        String email = emailInputLayout.getEditText().getText().toString().trim();
        String password = passwordInputLayout.getEditText().getText().toString().trim();
        TextValidator textValidator = new TextValidator();
        String emailValidationMessage = textValidator.isValidEmail(email);
        boolean areFieldsValid = true;
        if (emailValidationMessage != null) {
            areFieldsValid = false;
            setError(emailInputLayout, emailValidationMessage);
        }
        ArrayList<String> passwordValidationMessages = textValidator.isValidPassword(password);
        if (passwordValidationMessages != null) {
            areFieldsValid = false;
            String passwordErrorMessage = "";
            for (String message : passwordValidationMessages) {
                passwordErrorMessage += message + "\n";
            }
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.text_field_error);
            setError(passwordInputLayout, passwordErrorMessage);
        }
        return areFieldsValid;
    }

    private void setError(TextInputLayout layout, String error) {
        EditText editText = layout.getEditText();
        if (editText != null) {
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.text_field_error);
            editText.setBackground(drawable);
        }
        layout.setError(error);
    }
}
