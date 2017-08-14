package com.android.miki.quickly.login_signup;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
 * Created by mpokr on 8/13/2017.
 */

public class NamePasswordSignUpFragment extends Fragment implements FieldValidator {

    private CustomTextInputLayout nameInputLayout;
    private CustomTextInputLayout passwordInputLayout;
    private Button signUpButton;
    private FirebaseAuth auth;
    private static final String TAG = SignUpFragment.class.getName();
    private SignUpListener callbackToActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_name_password_signup, container, false);
        nameInputLayout = view.findViewById(R.id.name_text_input);
        passwordInputLayout = view.findViewById(R.id.password_text_input);
        passwordInputLayout.setPasswordVisibilityToggleEnabled(true);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.text_field_error);
        nameInputLayout.setErrorDrawable(drawable);
        passwordInputLayout.setErrorDrawable(drawable);


        Bundle args = getArguments();
        final String email;
        if (args != null) {
            email = args.getString("email");
            if (email == null) {
                throw new IllegalArgumentException("Email not passed correctly from previous fragment");
            }
        } else {
            throw new IllegalArgumentException("Email not passed correctly from previous fragment");
        }

        signUpButton = view.findViewById(R.id.sign_up_button);
        auth = FirebaseAuth.getInstance();
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameInputLayout.getTrimmedText();
                String password = passwordInputLayout.getTrimmedText();
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
    public boolean validateFields() {
        String password = passwordInputLayout.getTrimmedText();
        String name = nameInputLayout.getTrimmedText();
        boolean areFieldsValid = true;

        TextValidator textValidator = new TextValidator();
        String nameValidationMessage = textValidator.isValidName(name);
        if (nameValidationMessage != null) {
            areFieldsValid = false;
            nameInputLayout.setError(nameValidationMessage);
        }
        ArrayList<String> passwordValidationMessages = textValidator.isValidPassword(password);
        if (passwordValidationMessages != null) {
            areFieldsValid = false;
            String passwordErrorMessage = "";
            for (String message : passwordValidationMessages) {
                passwordErrorMessage += message + "\n";
            }
            passwordInputLayout.setError(passwordErrorMessage);
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

}
