package com.android.miki.quickly.models;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.android.miki.quickly.firebase_requests.DatabaseReferences;
import com.android.miki.quickly.firebase_requests.FirebaseRefKeys;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by mpokr on 5/26/2017.
 */

public class User implements Serializable {

    private String id;
    private String displayName;
    private String photoUrl;
    @Exclude
    private static User currentUser;
//    private String currentRoomId;
//    private Message lastMessage;


    public User() {

    }

    public static User currentUser() {
        return currentUser;
    }

    public static User createUser(FirebaseUser firebaseUser) {
        if (firebaseUser.getDisplayName() != null) {
            currentUser = new User();
            currentUser = new User();
            currentUser.id = firebaseUser.getUid();
            Uri photoUrl = firebaseUser.getPhotoUrl();
            if (photoUrl != null) {
                currentUser.photoUrl = photoUrl.toString();
            }
            currentUser.displayName = firebaseUser.getDisplayName();
            return currentUser;
        } else {
            return null;
        }
    }

    public static void createCurrentUserAsync(AccessToken accessToken, final FirebaseListener<Void> listener) {
        listener.onLoading();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo userInfo : currentFirebaseUser.getProviderData()) { // Get display name.
            if (userInfo.getProviderId().equals(EmailAuthProvider.PROVIDER_ID)) { // Try to get Firebase user first.
                User user = createUser(currentFirebaseUser);
                if (user != null) {
                    listener.onSuccess(null);
                } else {
                    listener.onError(FirebaseError.unknownError());
                }
            } else if (userInfo.getProviderId().equals(FacebookAuthProvider.PROVIDER_ID)) { // Else, get first name from Facebook.
                if (accessToken != null) {
                    GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                String name = object.getString("first_name");
                                currentUser = new User();
                                currentUser.id = currentFirebaseUser.getUid();
                                Uri photoUrl = currentFirebaseUser.getPhotoUrl();
                                if (photoUrl != null) {
                                    currentUser.photoUrl = photoUrl.toString();
                                }
                                currentUser.displayName = name;
                                String displayName = currentFirebaseUser.getDisplayName();
                                if (displayName != null) {
                                    if (!displayName.equals(name)) {
                                        final UserProfileChangeRequest updateName = new UserProfileChangeRequest.Builder().
                                                setDisplayName(name).build();
                                        currentFirebaseUser.updateProfile(updateName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    listener.onSuccess(null);
                                                } else {
                                                    listener.onError(FirebaseError.unknownError());
                                                }
                                            }
                                        });
                                    } else {
                                        listener.onSuccess(null);
                                    }
                                }
                            } catch (JSONException e) {
                                listener.onError(FirebaseError.unknownError());
                            }
                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "first_name");
                    request.setParameters(parameters);
                    request.executeAsync();
                }
            }
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

//    public String getCurrentRoomId() {
//        return currentRoomId;
//    }
//
//    public void setCurrentRoomId(String roomId) {
//        this.currentRoomId = roomId;
//        DatabaseReferences.USERS.child(this.id).child(FirebaseRefKeys.CURRENT_ROOM_ID).setValue(roomId);
//    }
//
//    public Message getLastMessage() {
//        return lastMessage;
//    }
//
//    public void setLastMessage(Message message) {
//        this.lastMessage = message;
//        DatabaseReferences.USERS.child(this.id).child(FirebaseRefKeys.LAST_MESSAGE).setValue(2);
//    }

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
