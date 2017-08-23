package com.android.miki.quickly.models;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by mpokr on 5/26/2017.
 */

public class User implements Serializable {

    private String id;
    private String displayName;
    private String photoUrl;
    private static User currentUser;


    public User() {

    }

    public static User currentUser() {
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            currentUser = new User();
            if (currentFirebaseUser != null) {
                currentUser.id = currentFirebaseUser.getUid();
                Uri photoUrl = currentFirebaseUser.getPhotoUrl();
                if (photoUrl != null) {
                    currentUser.photoUrl = photoUrl.toString();
                }
                currentUser.displayName = currentFirebaseUser.getDisplayName();
            } else {
                throw new IllegalStateException("Cannot make user from a null current user.");
            }
        }
        return currentUser;
    }

    /*
        **** TEST METHOD ONLY!!! ****
     */
    public User(String displayName, String photoUrl) {
        this.displayName = displayName;
        this.id = UUID.randomUUID().toString();
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof User) {
            User otherUser = (User) other;
            return this.id.equals(otherUser.getId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
