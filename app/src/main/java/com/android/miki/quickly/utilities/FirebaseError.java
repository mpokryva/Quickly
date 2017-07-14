package com.android.miki.quickly.utilities;

import com.google.firebase.database.DatabaseError;

/**
 * Created by Miki on 7/13/2017.
 */

public class FirebaseError {

    private String message;
    private String details;

    public FirebaseError(DatabaseError databaseError) {
        this.message = databaseError.getMessage();
        this.details = databaseError.getDetails();
    }

    public FirebaseError(String message, String details) {
        this.message = message;
        this.details = details;
    }

    public static FirebaseError noFirebaseConnectionError() {
        String message = "There was a problem connecting to the server.";
        String details = "Please verify that you have a working Internet connection.";
        return new FirebaseError(message, details);
    }
}
