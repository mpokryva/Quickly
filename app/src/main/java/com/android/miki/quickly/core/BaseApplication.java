package com.android.miki.quickly.core;

import android.app.Application;
import android.content.Context;

import com.android.miki.quickly.R;
import com.android.miki.quickly.models.User;
import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by mpokr on 8/12/2017.
 */

public class BaseApplication extends Application {

    private Thread.UncaughtExceptionHandler ueHandler;
    private Thread.UncaughtExceptionHandler ueHandlerListener = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-RobotoRegular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);
        ueHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(ueHandlerListener);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


}
