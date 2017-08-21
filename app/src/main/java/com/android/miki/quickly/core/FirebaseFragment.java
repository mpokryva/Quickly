package com.android.miki.quickly.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.miki.quickly.R;
import com.android.miki.quickly.utils.FirebaseListener;

/**
 * Created by mpokr on 7/15/2017.
 */

public abstract class FirebaseFragment<T> extends Fragment {


    public final int SUCCESS = 1;
    public final int ERROR = -1;
    public final int LOADING = 2;
    private int state;
    private View content;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.firebase_view, container, false);
        state = LOADING;
        return view;
    }

    /**
     * Use this method to change state.
     * DO NOT CHANGE STATUS DIRECTLY!
     *
     * @param state The new state to set.
     */
    protected void setState(int state) {
        View view = getView();
        if (view != null) {
            if (this.state != state) {
                View viewToHide;
                switch (this.state) {
                    case LOADING:
                        viewToHide = view.findViewById(R.id.loading_view);
                        break;
                    case SUCCESS:
                        viewToHide = content;
                        break;
                    case ERROR:
                        viewToHide = view.findViewById(R.id.error_view);
                        break;
                    default:
                        throw new IllegalArgumentException("OLD Status must be either LOADING, SUCCESS, or ERROR.");
                }
                viewToHide.setVisibility(View.GONE);
                View viewToShow;
                this.state = state; // Update state.
                switch (this.state) {
                    case LOADING:
                        viewToShow = view.findViewById(R.id.loading_view);
                        break;
                    case SUCCESS:
                        viewToShow = content;
                        break;
                    case ERROR:
                        viewToShow = view.findViewById(R.id.error_view);
                        break;
                    default:
                        throw new IllegalArgumentException("NEW Status must be either LOADING, SUCCESS, or ERROR.");
                }
                viewToShow.setVisibility(View.VISIBLE);
            }
        } else {
            throw new IllegalStateException("View in FirebaseFragment should not be null");
        }

    }


    public void setContent(int layoutId) {
        View view = getView();
        if (view != null) {
            ViewStub content = view.findViewById(R.id.content);
            content.setLayoutResource(layoutId);
            this.content = content.inflate();
        } else {
            throw new IllegalStateException("View in FirebaseFragment should not be null");
        }

    }

}
