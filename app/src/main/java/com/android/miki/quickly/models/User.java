package com.android.miki.quickly.models;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by mpokr on 5/26/2017.
 */

public class User implements Serializable{

    private String userId;
    private String nickname;
    private String university;


    public User() {

    }
    public User(String nickname, String university) {
        this.userId = FirebaseInstanceId.getInstance().getId(); // ****For now **** Change to instance ID later (maybe)
        this.nickname = nickname;
        this.university = university;
    }

    /*
        **** TEST METHOD ONLY!!! ****
     */
    public User(String nickname, String university, boolean randomId) {
        this.nickname = nickname;
        this.university = university;
        this.userId = UUID.randomUUID().toString();
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUniversity() {
        return university;
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
