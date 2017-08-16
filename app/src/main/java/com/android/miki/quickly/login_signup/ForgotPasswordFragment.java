package com.android.miki.quickly.login_signup;

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
import com.android.miki.quickly.utils.FieldValidator;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.TextValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

/**
 * Created by mpokr on 8/14/2017.
 */

public class ForgotPasswordFragment extends Fragment implements FieldValidator {

    CustomTextInputLayout emailInputLayout;
    private static final String TAG = ForgotPasswordFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        emailInputLayout = view.findViewById(R.id.email_text_input);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.text_field_error);
        emailInputLayout.setErrorDrawable(drawable);
        Button resetButton = view.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String email = emailInputLayout.getTrimmedText();

                    auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidUserException e) {
                                    FirebaseError emailNotFound = FirebaseError.userNotFoundForEmail();
                                    Toast.makeText(getContext(), emailNotFound.getMessage(), Toast.LENGTH_SHORT).show();
                                } catch (FirebaseNetworkException e) {
                                    FirebaseError networkError = FirebaseError.networkError();
                                    Toast.makeText(getContext(), networkError.getMessage(), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.e(TAG, e.getLocalizedMessage());
                                    Log.e(TAG, e.getClass().getName());
                                    String message = e.getLocalizedMessage().toLowerCase();
                                    if (message.contains("email")) { // Sorta hacky, but works for now.
                                        emailInputLayout.setError(TextValidator.INVALID_EMAIL);
                                    } else {
                                        FirebaseError unknownError = FirebaseError.unknownError();
                                        Toast.makeText(getContext(), unknownError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(getContext(), "New password has been sent. Please check your email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        return view;
    }

    @Override
    public boolean validateFields() {
        String email = emailInputLayout.getTrimmedText();
        String emailErrorMessage = new TextValidator().isValidEmail(email);
        if (emailErrorMessage != null) {
            emailInputLayout.setError(emailErrorMessage);
            return false;
        } else {
            return true;
        }
    }
}