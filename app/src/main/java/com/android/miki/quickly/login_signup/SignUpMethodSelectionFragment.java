package com.android.miki.quickly.login_signup;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.miki.quickly.R;
import com.android.miki.quickly.ui.FBLoginButton;

/**
 * Created by mpokr on 8/15/2017.
 */

public class SignUpMethodSelectionFragment extends Fragment {

    private SignUpListener callbackToActivity;
    private static final String TAG = SignUpMethodSelectionFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up_method_selection, container, false);
        Button emailAndPasswordSignUp = view.findViewById(R.id.email_password_sign_up_button);
        emailAndPasswordSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                // Using R.id.fragment_container may be unsafe, but fuck it.
                transaction.replace(R.id.fragment_container, new SignUpFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        FBLoginButton fbLoginButton = view.findViewById(R.id.fb_login_button);
        fbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbackToActivity.requestFacebookLogin();
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
}
