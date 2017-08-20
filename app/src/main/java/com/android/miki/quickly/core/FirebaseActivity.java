package com.android.miki.quickly.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewStub;

import com.android.miki.quickly.R;

/**
 * Created by Miki on 7/6/2017.
 */

public abstract class FirebaseActivity extends AppCompatActivity {

    public final int SUCCESS = 1;
    public final int ERROR = -1;
    public final int LOADING = 2;
    private int state;
    private View content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);
        state = LOADING;
    }


    /**
     * Use this method to change state.
     * DO NOT CHANGE STATUS DIRECTLY!
     *
     * @param state The new state to set.
     */
    protected void setState(int state) {
        if (this.state != state) {
            View viewToHide;
            switch (this.state) {
                case LOADING:
                    viewToHide = findViewById(R.id.loading_view);
                    break;
                case SUCCESS:
                    viewToHide = content;
                    break;
                case ERROR:
                    viewToHide = findViewById(R.id.error_view);
                    break;
                default:
                    throw new IllegalArgumentException("OLD Status must be either LOADING, SUCCESS, or ERROR.");
            }
            viewToHide.setVisibility(View.GONE);
            View viewToShow;
            this.state = state; // Update state.
            switch (this.state) {
                case LOADING:
                    viewToShow = findViewById(R.id.loading_view);
                    break;
                case SUCCESS:
                    viewToShow = content;
                    break;
                case ERROR:
                    viewToShow = findViewById(R.id.error_view);
                    break;
                default:
                    throw new IllegalArgumentException("NEW Status must be either LOADING, SUCCESS, or ERROR.");
            }
            viewToShow.setVisibility(View.VISIBLE);
        }
    }


    public void setContent(int layoutId) {
        ViewStub content = findViewById(R.id.content);
        content.setLayoutResource(layoutId);
        this.content = content.inflate();
    }



}