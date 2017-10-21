package com.android.miki.quickly.core.chat_room;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.android.miki.quickly.chat_components.ChatRoomObserver;
import com.android.miki.quickly.firebase_requests.DatabaseReferences;
import com.android.miki.quickly.firebase_requests.FirebaseRefKeys;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by mpokr on 10/19/2017.
 */

public class CleanChatRoomJobService extends JobService {

    public static final String ROOM_ID_KEY = "chatRoomId";
    public static final String USER_ID_KEY = "userId";
    public static final String NUM_USERS_KEY = FirebaseRefKeys.NUM_USERS;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        final String roomId = jobParameters.getExtras().getString(ROOM_ID_KEY);
        final String userId = jobParameters.getExtras().getString(USER_ID_KEY);
        final int numUsers = jobParameters.getExtras().getInt(NUM_USERS_KEY);
        if (roomId == null || userId == null) {
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference roomRef = DatabaseReferences.AVAILABLE_CHATS.child(roomId);
                roomRef.child(FirebaseRefKeys.USERS).child(userId).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    }
                });
                roomRef.child(FirebaseRefKeys.NUM_USERS).setValue(numUsers - 1, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    }
                });
                roomRef.child(FirebaseRefKeys.USER_ID_COLOR_MAP).child(userId).removeValue();
                //return true; // Not sure about this. May change to false.
            }
        }).run();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
