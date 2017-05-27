package com.android.miki.quickly;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;

/**
 * Created by mpokr on 5/26/2017.
 */

public class ConfigureUserProfileActivity extends AppCompatActivity {

    private TextInputLayout nicknameEditTextWrapper;
    private EditText nicknameEditText;
    private EditText universityEditText;
    private Button joinButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_user);
        nicknameEditTextWrapper = (TextInputLayout) findViewById(R.id.nickname_inputlayout);
        nicknameEditTextWrapper.setHintAnimationEnabled(true);
        nicknameEditText = (EditText) findViewById(R.id.nickname_edittext);
        universityEditText = (EditText) findViewById(R.id.university_edittext);
        joinButton = (Button) findViewById(R.id.join_button);

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname = nicknameEditText.getEditableText().toString();
                nickname = nickname.trim(); // Trim any whitespaces
                if (nickname.length() >= 3) {
                    Pattern p = Pattern.compile("^(?=.{3,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$"); // https://stackoverflow.com/questions/12018245/regular-expression-to-validate-username
                    Matcher m = p.matcher(nickname);
                    boolean isValidNickname = m.matches();
                    if (isValidNickname) {
                        String university = universityEditText.getEditableText().toString();
                        university = university.trim();
                        User user = new User(nickname, university);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference mUsersRef = database.getReference().child("users");
                        mUsersRef.keepSynced(true);
                        mUsersRef.child(user.getUserId()).setValue(user); // Push user to Firebase
                        Intent chatSelectionIntent = new Intent();
                        chatSelectionIntent.setClass(ConfigureUserProfileActivity.this, ChatSelectionActivity.class);
                        chatSelectionIntent.putExtra("user", user);
                        startActivity(chatSelectionIntent); // Start ChatSelectionActivity
                    }
                }
            }
        });
    }
}
