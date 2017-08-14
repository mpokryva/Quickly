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

public class EmailSignUpFragment extends Fragment implements FieldValidator {

    private CustomTextInputLayout emailInputLayout;
    private Button nextButton;
    private static final String TAG = SignUpFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email_signup, container, false);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.text_field_error);
        emailInputLayout = view.findViewById(R.id.email_text_input);
        emailInputLayout.setErrorDrawable(drawable);
        nextButton = view.findViewById(R.id.next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    // Reset error.
                    emailInputLayout.setError(null);
                    String email = emailInputLayout.getTrimmedText();
                    NamePasswordSignUpFragment namePasswordSignUpFragment = new NamePasswordSignUpFragment();
                    Bundle args = new Bundle();
                    args.putString("email", email);
                    namePasswordSignUpFragment.setArguments(args);
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    // Using R.id.fragment_container may be unsafe, but fuck it.
                    transaction.replace(R.id.fragment_container, namePasswordSignUpFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });
        return view;
    }


    /**
     * Validates the email and password fields, and displays appropriate error messages if they are invalid.
     *
     * @return True if the fields are valid, false otherwise.
     */
    public boolean validateFields() {
        String email = emailInputLayout.getTrimmedText();
        TextValidator textValidator = new TextValidator();
        boolean areFieldsValid = true;
        String emailValidationMessage = textValidator.isValidEmail(email);
        if (emailValidationMessage != null) {
            areFieldsValid = false;
            emailInputLayout.setError(emailValidationMessage);
        }
        return areFieldsValid;
    }

}
