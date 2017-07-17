package com.android.miki.quickly.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.miki.quickly.R;
import com.android.miki.quickly.utilities.FirebaseError;
import com.android.miki.quickly.utilities.FirebaseListener;

/**
 * Created by mpokr on 7/15/2017.
 */

public abstract class FirebaseFragment<T> extends Fragment implements FirebaseListener<T> {

    private ContentLoadingProgressBar progressWheel;
    private RelativeLayout loadingLayout;
    private RelativeLayout errorLayout;
    private TextView errorMessage;
    private TextView errorDetails;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_firebase, container, false);
//        progressWheel = (ContentLoadingProgressBar) view.findViewById(R.id.progress_wheel);
//        loadingLayout = (RelativeLayout) view.findViewById(R.id.loading_view);
//        errorLayout = (RelativeLayout) view.findViewById(R.id.error_view);
//        errorMessage = (TextView) view.findViewById(R.id.error_message);
//        errorDetails = (TextView) view.findViewById(R.id.error_details);
        return view;
    }
//
//    @Override
//    public void onLoading() {
//        hideMainLayout();
//        loadingLayout.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void onError(FirebaseError error) {
//        hideMainLayout();
//        errorLayout.setVisibility(View.VISIBLE);
//        errorMessage.setText(error.getMessage());
//        errorDetails.setText(error.getDetails());
//    }

    /**
     * Subclasses should override this method in order to display main content view,
     * and do any other things they require.
     * @param data The data retrieved from Firebase.
     */
    @Override
    public void onSuccess(T data) {
        showMainContent();
    }


    public abstract void showMainContent();

    /**
     * Subclass should override this method.
     */
    public abstract void hideMainLayout();
}
