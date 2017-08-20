package com.android.miki.quickly.group_info;

import java.io.Serializable;

/**
 * Created by mpokr on 6/25/2017.
 */

public interface GroupNameDialogListener extends Serializable {

    String TAG = GroupNameDialogListener.class.getName();

    void editGroupNameRequested();
}
