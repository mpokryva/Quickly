package com.android.miki.quickly.user;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.miki.quickly.Manifest;
import com.android.miki.quickly.R;
import com.android.miki.quickly.firebase_requests.FirebaseRefKeys;
import com.android.miki.quickly.models.User;
import com.android.miki.quickly.ui.CustomProgressWheel;
import com.android.miki.quickly.utils.DialogBuilderHelper;
import com.android.miki.quickly.utils.FirebaseError;
import com.android.miki.quickly.utils.FirebaseListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpokr on 8/25/2017.
 */

public class MyAccountActivity extends AppCompatActivity {

    private GridLayout photoGrid;
    private static final int PICK_USER_PHOTO = 456;
    private int currentIndex = -1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private Uri currentUri;
    private static final String TAG = MyAccountActivity.class.getName();
    private final int MAX_RETRY_MILLISECONDS = 4000;
    private EditText bioEditText;
    private static final String REF_BUNDLE_KEY = "reference";
    private ArrayList<String> uploadRefsUrlsInUsage;
    private RelativeLayout educationField;
    private RelativeLayout occupationField;
    private RelativeLayout ageField;
    private static final int EDUCATION = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        uploadRefsUrlsInUsage = new ArrayList<>();
        photoGrid = findViewById(R.id.photo_grid);
        bioEditText = findViewById(R.id.bio_edittext);
        InputFilter[] inputFilters = new InputFilter[1];
        final int maxBioLength = 250;
        inputFilters[0] = new InputFilter.LengthFilter(maxBioLength);
        bioEditText.setFilters(inputFilters);
        final TextView bioCharCountTV = findViewById(R.id.bio_char_count);
        String initialText = "" + maxBioLength;
        bioCharCountTV.setText(initialText);

        bioEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int charsLeft = maxBioLength - editable.length();
                String displayString = "" + charsLeft;
                bioCharCountTV.setText(displayString);
            }
        });
        educationField = findViewById(R.id.education_field);
        occupationField = findViewById(R.id.occupation_field);
        ageField = findViewById(R.id.age_field);
        Toolbar actionBar = findViewById(R.id.action_bar);
        actionBar.setTitle("Edit Info");
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        FirebaseStorage.getInstance().setMaxUploadRetryTimeMillis(MAX_RETRY_MILLISECONDS);
        FirebaseStorage.getInstance().setMaxDownloadRetryTimeMillis(MAX_RETRY_MILLISECONDS);
        FirebaseStorage.getInstance().setMaxOperationRetryTimeMillis(MAX_RETRY_MILLISECONDS);
        // Load photos, and set listeners.
        for (int i = 0; i < photoGrid.getChildCount(); i++) {
            final int j = i;
            RelativeLayout rl = (RelativeLayout) photoGrid.getChildAt(i);
            ImageButton photoButton = rl.findViewById(R.id.user_photo_button);
            photoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Drawable image = getImageView(j).getDrawable();
                    boolean hasImage = (image != null) && (image instanceof BitmapDrawable);
                    if (hasImage) {
                        removeImageFromStorage(j);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        currentIndex = j;
                        startActivityForResult(intent, PICK_USER_PHOTO);
                    }
                }
            });
            setImageFromFromStorage(j);
        }
        // Register listener for bio changes, and load bio.
        registerBioChangeListener();

    }

    private void initAccountFields() {
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(User.currentUser().getId());
        int lightBlue = ContextCompat.getColor(MyAccountActivity.this, R.color.LightBlue);
        final MaterialDialog progressDialog = new MaterialDialog.Builder(MyAccountActivity.this)
                .progress(true, -1).widgetColor(lightBlue).build();
        final FirebaseListener<Void> listener = new FirebaseListener<Void>() {
            @Override
            public void onLoading() {
                progressDialog.show();
            }

            @Override
            public void onError(FirebaseError error) {
                progressDialog.dismiss();
                Toast.makeText(MyAccountActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(Void nothing) {
                progressDialog.dismiss();
                Toast.makeText(MyAccountActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            }
        };
        final DatabaseReference educationRef = userRef.child(FirebaseRefKeys.EDUCATION);
        educationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() instanceof String) {
                    TextView label = educationField.findViewById(R.id.education_field_label);
                    label.setCompoundDrawables(null, null, null, null); // Hide drawables
                    TextView value = educationField.findViewById(R.id.education_field_value);
                    value.setText((String) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        educationField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeDialogAndSetValue(null, null, R.string.change_education, educationRef, listener).show();
            }
        });
        final DatabaseReference occupationRef = userRef.child(FirebaseRefKeys.OCCUPATION);
        occupationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() instanceof String) {
                    occupationField.setCompoundDrawables(null, null, null, null); // Hide drawables
                    occupationField.setText((String) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        occupationField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeDialogAndSetValue(null, null, R.string.change_occupation, occupationRef, listener).show();
            }
        });
        final DatabaseReference ageRef = userRef.child(FirebaseRefKeys.AGE);
        ageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() instanceof String) {
                    ageField.setCompoundDrawables(null, null, null, null); // Hide drawables
                    ageField.setText((String) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ageField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeDialogAndSetValue(null, null, R.string.change_age, ageRef, listener).show();
            }
        });
    }

    private void setAccountFieldValue(RelativeLayout accountField, String value) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        initAccountFields();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If there's a download in progress, save the reference so you can query it later
        if (!uploadRefsUrlsInUsage.isEmpty()) {
            outState.putStringArrayList(REF_BUNDLE_KEY, uploadRefsUrlsInUsage);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If there was a download in progress, get its reference and create a new StorageReference
        final List<String> stringRef = savedInstanceState.getStringArrayList(REF_BUNDLE_KEY);
        if (stringRef == null) {
            return;
        }
        for (String refUrl : uploadRefsUrlsInUsage) {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(refUrl);
            for (UploadTask uploadTask : ref.getActiveUploadTasks()) {
                uploadTask.addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MyAccountActivity.this, FirebaseError.unknownError().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        // Succeed silently
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_USER_PHOTO:
                    try {
                        if (currentIndex < 0) {
                            throw new IllegalStateException();
                        }
                        currentUri = data.getData();
                        int maxWidth = 100;
                        int maxHeight = 100;
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Should we show an explanation?
                            if (shouldShowRequestPermissionRationale(
                                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                // Explain to the user why we need to read the contacts
                            }
                            ActivityCompat.requestPermissions(MyAccountActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        } else {
                            // Permission already granted
                            startCropActivity(currentUri);
                        }

                    } catch (IllegalStateException | NullPointerException e) {
                        Toast.makeText(this, FirebaseError.unknownError().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                    break;
                case UCrop.REQUEST_CROP:
                    final Uri resultUri = UCrop.getOutput(data);
                    uploadImageToFirebaseAndApply(resultUri, currentIndex, true);
                    break;
            }
        } else {
            if (resultCode == UCrop.RESULT_ERROR) {
                Throwable error = UCrop.getError(data);
                if (error != null) {
                    Log.e(TAG, error.toString());
                }
            }
        }
    }

    private void deleteImageFromPhone(Uri uri) {
        File fileToDelete = new File(uri.getPath());
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                Log.d(TAG, "file Deleted :" + uri.getPath());
            } else {
                Log.d(TAG, "file not Deleted :" + uri.getPath());
            }
        }
    }

    private void removeImageFromStorage(final int imageIndex) {
        ImageView imageView = getImageView(imageIndex);
        final Drawable image = imageView.getDrawable();
        imageView.setImageDrawable(null);
        final CustomProgressWheel progressWheel = getImageProgressWheel(imageIndex);
        progressWheel.setVisibility(View.VISIBLE);
        getImageRef(imageIndex).delete().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressWheel.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    resetImage(imageIndex);
                    Toast.makeText(MyAccountActivity.this, "Removed!", Toast.LENGTH_SHORT).show();
                } else {
                    // Set back to original image if failed and explain failure with toast.
                    getImageView(imageIndex).setImageDrawable(image);
                    Toast.makeText(MyAccountActivity.this, FirebaseError.unknownError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCropActivity(currentUri);
                } else {
                    // permission denied, boo!
                }
            }
        }
    }

    private void startCropActivity(Uri source) {
        File fileToDelete = new File(getFilesDir(), "willDelete");
        Uri destination = Uri.parse(fileToDelete.toURI().toString());
        UCrop.Options options = new UCrop.Options();
        int lightBlue = ContextCompat.getColor(this, R.color.LightBlue);
        int statusBarColor = ContextCompat.getColor(this, R.color.LightBlueDark);
        options.setToolbarColor(lightBlue);
        options.setActiveWidgetColor(lightBlue);
        options.setStatusBarColor(statusBarColor);
        UCrop.of(source, destination)
                .withAspectRatio(1, 1)
                .withOptions(options)
                .withMaxResultSize(100, 100).start(this);
    }

    private void uploadImageToFirebaseAndApply(final Uri uri, final int imageIndex, final boolean shouldDeleteImage) {
        try {
            final CustomProgressWheel progressWheel = getImageProgressWheel(imageIndex);
            progressWheel.setVisibility(View.VISIBLE);
            final InputStream in = this.getContentResolver().openInputStream(uri);
            final StorageReference uploadRef = getImageRef(imageIndex);
            uploadRefsUrlsInUsage.add(uploadRef.toString());
            // Create upload task
            UploadTask uploadTask = uploadRef.putStream(in);
            uploadTask.addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    uploadRefsUrlsInUsage.remove(uploadRef.toString());
                    progressWheel.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(MyAccountActivity.this, "Uploaded!", Toast.LENGTH_SHORT).show();
                        setImageFromIntent(uri, imageIndex);
                        if (shouldDeleteImage) {
                            deleteImageFromPhone(uri);
                        }
                    } else {
                        Toast.makeText(MyAccountActivity.this, FirebaseError.unknownError().getMessage(), Toast.LENGTH_SHORT).show();
                        if (shouldDeleteImage) {
                            deleteImageFromPhone(uri);
                        }
                    }
                }
            });
        } catch (FileNotFoundException e) {
            getImageProgressWheel(imageIndex).setVisibility(View.GONE);
            Toast.makeText(this, FirebaseError.unknownError().getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set ImageView to contents of uri from device (photo file).
     *
     * @param uri
     * @param imageIndex
     */
    private void setImageFromIntent(Uri uri, int imageIndex) {
        ImageView imageView = getImageView(imageIndex);
        try {
            imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri));
            getImageProgressWheel(imageIndex).setVisibility(View.GONE);
            toggleImageButton(imageIndex, true);
        } catch (IOException e) {
            Toast.makeText(this, FirebaseError.unknownError().getMessage(), Toast.LENGTH_SHORT).show();
            getImageProgressWheel(imageIndex).setVisibility(View.GONE);
            toggleImageButton(imageIndex, false);
        }
    }

    private void resetImage(int imageIndex) {
        getImageView(imageIndex).setImageDrawable(null);
        toggleImageButton(imageIndex, false);
    }


    private ImageView getImageView(int imageIndex) {
        RelativeLayout rl = (RelativeLayout) photoGrid.getChildAt(imageIndex);
        return rl.findViewById(R.id.user_photo);
    }

    private ImageButton getImageButton(int imageIndex) {
        RelativeLayout rl = (RelativeLayout) photoGrid.getChildAt(imageIndex);
        return rl.findViewById(R.id.user_photo_button);
    }

    private CustomProgressWheel getImageProgressWheel(int imageIndex) {
        RelativeLayout rl = (RelativeLayout) photoGrid.getChildAt(imageIndex);
        return rl.findViewById(R.id.progress_wheel);
    }

    /**
     * Set image to contents from Firebase Storage.
     * Fails silently if no image exists for the ImageView.
     *
     * @param imageIndex
     */
    private void setImageFromFromStorage(final int imageIndex) {
        StorageReference downloadRef = getImageRef(imageIndex);
        downloadRef.getDownloadUrl().addOnCompleteListener(this, new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    Glide.with(MyAccountActivity.this).load(uri).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            getImageProgressWheel(imageIndex).setVisibility(View.GONE);
                            toggleImageButton(imageIndex, false);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            getImageProgressWheel(imageIndex).setVisibility(View.GONE);
                            toggleImageButton(imageIndex, true);
                            return false;
                        }
                    }).into(getImageView(imageIndex));
                } else {
                    getImageProgressWheel(imageIndex).setVisibility(View.GONE);
                    toggleImageButton(imageIndex, false);
                }
            }
        });
    }


    private void toggleImageButton(int imageIndex, boolean isImageSet) {
        ImageButton button = getImageButton(imageIndex);
        if (isImageSet) {
            Drawable background = ContextCompat.getDrawable(this, R.drawable.ic_remove_circle_white_24dp);
            button.setImageDrawable(background);
        } else {
            Drawable background = ContextCompat.getDrawable(this, R.drawable.ic_add_circle_white_24dp);
            button.setImageDrawable(background);
        }
        button.setColorFilter(ContextCompat.getColor(this, R.color.LightBlue));
    }

    private StorageReference getImageRef(int imageIndex) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();
        return rootRef.child(User.currentUser().getId()).child(FirebaseRefKeys.IMAGES).child("" + imageIndex);
    }

    private void registerBioChangeListener() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference bioRef = database.getReference().child(FirebaseRefKeys.USER_DATA)
                .child(User.currentUser().getId()).child(FirebaseRefKeys.BIO);
        bioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Object bio = dataSnapshot.getValue();
                    if (bio instanceof String) {
                        if (!bioEditText.getText().equals(bio)) {
                            bioEditText.setText((String) bio);
                        }
                    } else {
                        throw new IllegalStateException("User bio must be a string.");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private MaterialDialog makeDialogAndSetValue(String title, String content, int hint,
                                                 final DatabaseReference ref, final FirebaseListener<Void> listener) {
        final int lightBlue = ContextCompat.getColor(this, R.color.LightBlue);
        return new DialogBuilderHelper().inputDialog(this, lightBlue)
                .title(title)
                .content(content)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .input(hint, R.string.empty_string, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        listener.onLoading();
                        ref.setValue(input.toString()).addOnCompleteListener(MyAccountActivity.this,
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            listener.onSuccess(null);
                                        } else {
                                            listener.onError(FirebaseError.unknownError());
                                            Log.d(TAG, task.getException().getMessage());
                                        }
                                    }
                                });
                    }
                }).build();
    }


}
