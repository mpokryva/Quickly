package com.android.miki.quickly.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.android.miki.quickly.R;

/**
 * Created by Miki on 7/6/2017.
 */

public abstract class FirebaseActivity extends AppCompatActivity {

    private Status status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_error);
       // setStatus(Status.LOADING);
    }

//
//    /**
//     * Use this method to change status.
//     * DO NOT CHANGE STATUS DIRECTLY!
//     *
//     * @param status The new status to set.
//     */
//    protected void setStatus(Status status) {
//        if (this.status != status) {
//            this.status = status;
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            Fragment fragment = status.getFragment();
//            if (fragment != null) {
//                transaction.replace(R.id.fragment_container, status.getFragment());
//                transaction.commit();
//            }
//        }
//    }

}