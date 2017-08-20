package com.android.miki.quickly.models;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

/**
 * Created by mpokr on 5/26/2017.
 */

public class User implements Serializable {

    private String userId;
    private String displayName;
    private String photoUrl;


    public User() {

    }

    public static User fromCurrentUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        User user = new User();
        if (currentUser != null) {
            user.userId = currentUser.getUid();
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                user.photoUrl = photoUrl.toString();
            }
            user.displayName = currentUser.getDisplayName();
            return user;
        } else {
            throw new IllegalStateException("Cannot make user from a null current user.");
        }
    }

    /*
        **** TEST METHOD ONLY!!! ****
     */
    public User(String displayName, String photoUrl) {
        this.displayName = displayName;
        this.userId = UUID.randomUUID().toString();
        this.photoUrl = photoUrl;
    }

    public String getUserId() {
        return userId;
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
            return this.userId.equals(otherUser.getUserId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

}
