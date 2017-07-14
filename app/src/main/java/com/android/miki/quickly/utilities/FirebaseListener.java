package com.android.miki.quickly.utilities;

import com.google.firebase.database.DatabaseError;

/**
 * Created by mpokr on 7/4/2017.
 */

public interface FirebaseListener<T> {
    void onLoading();
    void onError(FirebaseError error);
    void onSuccess(T data);
}
