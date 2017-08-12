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
import com.android.miki.quickly.utils.TextValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;

/**
 * Created by Miki on 8/3/2017.
 */

public class SignUpFragment extends Fragment {

    private TextInputLayout nameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button signUpButton;
    private FirebaseAuth auth;
    private static final String TAG = SignUpFragment.class.getName();
    private SignUpListener callbackToActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        nameInputLayout = (TextInputLayout) view.findViewById(R.id.name_text_input);
        emailInputLayout = (TextInputLayout) view.findViewById(R.id.email_text_input);
        passwordInputLayout = (TextInputLayout) view.findViewById(R.id.password_text_input);
        signUpButton = (Button) view.findViewById(R.id.sign_up_button);
        auth = FirebaseAuth.getInstance();


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameInputLayout.getEditText().getText().toString().trim();
                String email = emailInputLayout.getEditText().getText().toString().trim();
                String password = passwordInputLayout.getEditText().getText().toString().trim();
                if (validateFields()) {
                    createAccount(name, email, password);
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
                callbackToActivity = (SignUpListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement SignUpListener");
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
        String name = nameInputLayout.getEditText().getText().toString().trim();
        boolean areFieldsValid = true;

        TextValidator textValidator = new TextValidator();
        String nameValidationMessage = textValidator.isValidName(name);
        if (nameValidationMessage != null) {
            areFieldsValid = false;
            setError(nameInputLayout, nameValidationMessage);
        }
        String emailValidationMessage = textValidator.isValidEmail(email);
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
            setError(passwordInputLayout, passwordErrorMessage);
        }

        return areFieldsValid;
    }


    private void createAccount(final String name, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up with email and password successful.
                            Log.d(TAG, "createUserWithEmail:success");
                            final FirebaseUser user = auth.getCurrentUser();
                            final UserProfileChangeRequest updateName = new UserProfileChangeRequest.Builder().
                                    setDisplayName(name).build();
                            if (user == null) {
                                Log.d(TAG, "Created user in SignUpFragment is null");
                                callbackToActivity.onFailedSignUp();
                                // TODO: Delete user.
                            } else {
                                user.updateProfile(updateName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            callbackToActivity.onSuccessfulSignUp(user);
                                        } else {
                                            // TODO: Delete user.
                                            callbackToActivity.onFailedSignUp();
                                        }
                                    }
                                });
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            callbackToActivity.onFailedSignUp();
                        }

                        // ...
                    }
                });
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
