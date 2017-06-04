package com.android.miki.quickly.chat_components;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.Toolbar;

import com.android.miki.quickly.models.ChatRoom;
import com.android.miki.quickly.models.User;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Miki on 6/3/2017.
 */

public class ChatActionBar extends Toolbar {

    private List<User> users;

    /*
    public ChatActionBar(Toolbar actionBar, List<User> users) {
        this.actionBar = actionBar;
        this.users = users;
    }
    */

    public ChatActionBar(Context context) {
        super(context);
    }

    public String getMaxWidthUserString(int maxWidth) {
        int barWidth = this.getWidth();
        String userString = "";
        for (User user: users) {
            Paint measurer = new Paint();
            if (user != null) {
                String userName = user.getNickname();
                Rect bounds = new Rect();
                measurer.getTextBounds(userName, 0, userName.length(), bounds);
                int nameWidth = bounds.width();
                measurer.getTextBounds(userString, 0, userString.length(), bounds);
                int totalWidth = bounds.width();
                if (totalWidth + nameWidth <= maxWidth) {
                    userString += userName + ", ";
                }
            }
        }
        if (userString.length() >= 1) {
            return userString.substring(0, userString.length() - 2) // Get rid of trailing ", "
        } else {
            return userString; // return ""
        }
    }


    public void setTitle(int maxTitleWidth) {
        this.setTitle(getMaxWidthUserString(maxTitleWidth));
    }




}
