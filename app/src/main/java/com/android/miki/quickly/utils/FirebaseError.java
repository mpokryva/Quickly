package com.android.miki.quickly.utils;

import com.google.firebase.database.DatabaseError;

/**
 * Created by Miki on 7/13/2017.
 */

public class FirebaseError {

    private String message;
    private String details;
    private static final String IF_ISSUE_PERSISTS = "If this issue persists, contact Quickly support.";

    public FirebaseError(DatabaseError databaseError) {
        this.message = databaseError.getMessage();
        this.details = databaseError.getDetails();
    }

    public FirebaseError(String message, String details) {
        this.message = message;
        this.details = details;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public static FirebaseError from(DatabaseError databaseError) {
        return new FirebaseError(databaseError.getMessage(), databaseError.getDetails());
    }

    public static FirebaseError serverError() {
        String message = "There was a problem connecting to the server.";
        String details = "Please try reloading the chat room in a few minutes. If the issue persists," +
                "contact the developer via the email provided in Quickly's info section on the Play Store.";
        return new FirebaseError(message, details);
    }

    public static FirebaseError noInternetConnection() {
        String message = "You are not not connected to the internet.";
        String details = "The chat room will load automatically once you're reconnected " +
                "to the internet.";
        return new FirebaseError(message, details);
    }

    public static FirebaseError networkError() {
        String message = "A network error has occured. Plase make sure you're connected to the internet.";
        String details = " A network error (such as timeout, interrupted connection or unreachable host) has occurred." + " " +
                IF_ISSUE_PERSISTS;
        return new FirebaseError(message, details);
    }

    public static FirebaseError userNotFoundForEmail() {
        String message = "There's no user corresponding to that email in our records.";
        String details = "Please verify that the email provided is correct and try again." + " " + IF_ISSUE_PERSISTS;
        return new FirebaseError(message, details);
    }

    public static FirebaseError unknownError() {
        String message = "Hmmm... Something weird happened. Try again later.";
        String details = "We're not sure what happened." + " " + IF_ISSUE_PERSISTS;
        return new FirebaseError(message, details);
    }

    public static FirebaseError incorrectPassword() {
        String message = "The password you entered is incorrect.";
        String details = message + " " + "You can try again with another password, or reset your password below.";
        return new FirebaseError(message, details);
    }

    public static FirebaseError invalidCredential() {
        String message = "The authentication session may have expired. Please try again.";
        String details = "The authentication credential provided has either expired or is malformed." + " " +
                IF_ISSUE_PERSISTS;
        return new FirebaseError(message, details);
    }

    public static FirebaseError invalidUser() {
        String message = "This account may have been deleted or disabled. Please contact Quickly support to resolve this issue.";
        String details = "This account may have been deleted or disabled, or its credentials are " +
                "no longer valid. Please contact Quickly support to resolve this issue.";
        return new FirebaseError(message, details);
    }
}
