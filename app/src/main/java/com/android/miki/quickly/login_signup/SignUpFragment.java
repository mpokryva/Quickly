package com.android.miki.quickly.login_signup;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.utils.FieldValidator;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.TextValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Created by mpokr on 8/13/2017.
 */

public class SignUpFragment extends Fragment implements FieldValidator {

    private CustomTextInputLayout emailInputLayout;
    private CustomTextInputLayout nameInputLayout;
    private CustomTextInputLayout passwordInputLayout;
    private Button signUpButton;
    private FirebaseAuth auth;
    private static final String TAG = SignUpFragment.class.getName();
    private SignUpListener callbackToActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        emailInputLayout = view.findViewById(R.id.email_text_input);
        nameInputLayout = view.findViewById(R.id.name_text_input);
        passwordInputLayout = view.findViewById(R.id.password_text_input);
        //passwordInputLayout.setPasswordVisibilityToggleEnabled(true); FOR NOW
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.text_field_error);
        emailInputLayout.setErrorDrawable(drawable);
        nameInputLayout.setErrorDrawable(drawable);
        passwordInputLayout.setErrorDrawable(drawable);

        signUpButton = view.findViewById(R.id.sign_up_button);
        auth = FirebaseAuth.getInstance();
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameInputLayout.getTrimmedText();
                String password = passwordInputLayout.getTrimmedText();
                String email = emailInputLayout.getTrimmedText();
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
        String name = nameInputLayout.getTrimmedText();
        TextValidator textValidator = new TextValidator();
        String nameValidationMessage = textValidator.isValidName(name);
        boolean areFieldsValid = true;
        if (nameValidationMessage != null) {
            areFieldsValid = false;
            nameInputLayout.setError(nameValidationMessage);
        }
        String password = passwordInputLayout.getTrimmedText();
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Please enter a password.");
            areFieldsValid = false;
        }
        String email = emailInputLayout.getTrimmedText();
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Please enter an email address.");
            areFieldsValid = false;
        }
        return areFieldsValid;
    }

    private void createAccount(final String name, final String email, String password) {
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
                                Log.d(TAG, "Created user in SignUpMethodSelectionFragment is null");
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
                            try {
                                // If sign in fails, display a message to the user.
                                Log.e(TAG, "createUserWithEmail:failure", task.getException());
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) { //  Thrown if the password is not strong enough.
                                passwordInputLayout.setError(e.getReason());
                            } catch (FirebaseAuthInvalidCredentialsException e) { // Thrown if the email address is malformed.
                                emailInputLayout.setError(e.getLocalizedMessage());
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            } catch (FirebaseNetworkException e) {
                                FirebaseError networkError = FirebaseError.networkError();
                                Toast.makeText(getContext(), networkError.getMessage(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                FirebaseError unknownError = FirebaseError.unknownError();
                                Toast.makeText(getContext(), unknownError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });
    }

}
