package com.android.miki.quickly.firebase_queries;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Miki on 7/14/2017.
 */

public abstract class FirebaseQuery {

    private DatabaseReference baseRef;

    public FirebaseQuery(DatabaseReference baseRef) {
        this.baseRef = baseRef;
    }

    public DatabaseReference getBaseRef() {
        return baseRef;
    }
}
