package com.android.miki.quickly.login_signup;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.miki.quickly.R;
import com.android.miki.quickly.ui.CustomTextInputLayout;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.android.miki.quickly.utils.TextValidator;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by mpokr on 8/16/2017.
 */

public class LinkAccountsFragment extends Fragment {

    private CustomTextInputLayout emailInputLayout;
    private CustomTextInputLayout passwordInputLayout;
    private Button linkAccountsButton;
    private AuthCredential linkingCredential;
    private LoginListener callbackToActivity;
    private static final String TAG = LinkAccountsFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_link_accounts, container, false);
        emailInputLayout = view.findViewById(R.id.email_text_input);
        passwordInputLayout = view.findViewById(R.id.password_text_input);
        linkAccountsButton = view.findViewById(R.id.link_accounts_button);
        linkAccountsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    final String email = emailInputLayout.getTrimmedText();
                    String password = passwordInputLayout.getTrimmedText();
                    final AuthQueryHelper queryHelper = new AuthQueryHelper();
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
                        public void onSuccess(final FirebaseUser user) {
                            queryHelper.linkCredentials(user, linkingCredential, new FirebaseListener<FirebaseUser>() {
                                @Override
                                public void onLoading() {

                                }

                                @Override
                                public void onError(FirebaseError error) {
                                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onSuccess(FirebaseUser user) {
                                    callbackToActivity.onSuccessfulLogIn(user);
                                }
                            });
                        }
                    });
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

    public static LinkAccountsFragment newInstance(AuthCredential linkingCredential) {
        LinkAccountsFragment fragment = new LinkAccountsFragment();
        fragment.linkingCredential = linkingCredential;
        return fragment;
    }
}
