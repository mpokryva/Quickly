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


    public User() {

    }

    public static User fromCurrentUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        User user = new User();
        if (currentUser != null) {
            user.id = currentUser.getUid();
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
