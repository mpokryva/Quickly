package com.android.miki.quickly.core.chat_room;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.google.firebase.database.Query;

/**
 * Created by mpokr on 10/22/2017.
 */

public class GeneralUserRemovalService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
//        Query cleanQuery =
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
