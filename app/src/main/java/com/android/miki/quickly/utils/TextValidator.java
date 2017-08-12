package com.android.miki.quickly.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Miki on 8/5/2017.
 */

public class TextValidator {


    /**
     * Returns true if a string matches the following criteria:
     * <p>
     * Not empty.
     * Is a valid email.
     *
     * @param email The string to validate.
     * @return True if the string is a valid email, false otherwise.
     */
    public String isValidEmail(String email) {
        boolean isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        String errorMessage = null;
        if (!isValid) {
            errorMessage = "Invalid email.";
        }
        return errorMessage;
    }

    /**
     * Returns true if a string matches the following criteria:
     * <p>
     * Nonempty.
     *
     * @param name The string (a name) to validate.
     * @return True if the string is a valid name, false otherwise.
     */
    public String isValidName(String name) {
        boolean isValid = !TextUtils.isEmpty(name);
        String errorMessage = null;
        if (!isValid) {
            errorMessage = "Name cannot be empty.";
        }
        return errorMessage;
    }

    /**
     * Returns true if a string matches the following criteria:
     * Reference: // https://stackoverflow.com/questions/3802192/regexp-java-for-password-validation
     * A digit must occur at least once.
     * A lower case letter must occur at least once.
     * An upper case letter must occur at least once.
     * No whitespace allowed in the entire string.
     * Anything, at least eight places though.
     *
     * @param password The string to match (supposed to be a password).
     * @return An error message if the string is not valid, null otherwise.
     */
    public ArrayList<String> isValidPassword(String password) {
        ArrayList<String> errorMessages = new ArrayList<>();
        Pattern digitOccursAtLeastOnce = Pattern.compile(".*[0-9].*");
        if (!doesMatch(digitOccursAtLeastOnce, password)) {
            errorMessages.add("Password must have at least 1 digit.");
        }
        Pattern lowerCaseAtLeastOnce = Pattern.compile(".*(?=.*[a-z]).*");
        if (!doesMatch(lowerCaseAtLeastOnce, password)) {
            errorMessages.add("Password must have at last 1 lowercase letter.");
        }
        Pattern upperCaseAtLeastOnce = Pattern.compile(".*(?=.*[A-Z]).*");
        if (!doesMatch(upperCaseAtLeastOnce, password)) {
            errorMessages.add("Password must have at least 1 uppercase letter.");
        }
        Pattern noWhiteSpace = Pattern.compile(".*(?=\\S+$).*");
        if (!doesMatch(noWhiteSpace, password)) {
            errorMessages.add("Password cannot have any whitespace.");
        }
        Pattern atLeast8Chars = Pattern.compile(".{8,}");
        if (!doesMatch(atLeast8Chars, password)) {
            errorMessages.add("Password must have at least 8 characters.");
        }
        if (errorMessages.size() == 0) {
            return null;
        } else {
            return errorMessages;
        }
    }

    private boolean doesMatch(Pattern p, String s) {
        boolean isValid = p.matcher(s).matches();
        return p.matcher(s).matches();
    }
}
