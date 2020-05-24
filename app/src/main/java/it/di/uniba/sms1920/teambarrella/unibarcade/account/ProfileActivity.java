package it.di.uniba.sms1920.teambarrella.unibarcade.account;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import it.di.uniba.sms1920.teambarrella.unibarcade.MainPageActivity;
import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeDBAdapter;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeFBHelper;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_EXTERNAL_CONTENT = 0;

    private static final int CAMERA_REQUEST_CODE = 100;

    private ImageView profileWallpaper, editPhoto, editGallery, editProfileConfirm,
            editPasswordConfirm, editProfileDummy, editPasswordDummy;
    private RelativeLayout rlConfirmPasswordLayout, rlEmailSent, rlPasswordError;
    private Button btnDelete, btnConfirmDelete, btnCancelDelete;
    private ToggleButton toggleProfile, togglePassword;
    private TextView txtNickname, txtEmail, txtPassword, txtAreYouSure;
    private TextInputLayout textInputLayout1, textInputLayout2, textInputLayout3, textInputLayout4;
    private TextInputEditText editNickname, editEmail, editOldPassword, editNewPassword;
    private TextView txtSnakeScore, txtArkanoidScore, txtCannonScore, txtSpaceInvadersScore;
    private CircleImageView profilePhoto;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;

    private boolean isInserted = false;

    private UnibArcadeFBHelper fbHelper;
    private UnibArcadeDBAdapter dbAdapter;
    private MainPageActivity mainPage;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mRef;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        rlConfirmPasswordLayout = (RelativeLayout) findViewById(R.id.confirmPasswordLayout);
        rlEmailSent = (RelativeLayout) findViewById(R.id.rlEmailSent);

        txtNickname = (TextView) findViewById(R.id.tvNickname);
        txtEmail = (TextView) findViewById(R.id.tvEmail);
        txtPassword = (TextView) findViewById(R.id.tvPassword);
        txtAreYouSure = (TextView) findViewById(R.id.txtAreYouSure);
        profilePhoto = (CircleImageView) findViewById(R.id.profilePhoto);

        editPhoto = (ImageView) findViewById(R.id.editPhoto);
        editGallery = (ImageView) findViewById(R.id.editGallery);
        toggleProfile = (ToggleButton) findViewById(R.id.toggleProfile);
        togglePassword = (ToggleButton) findViewById(R.id.togglePassword);

        txtSpaceInvadersScore = (TextView) findViewById(R.id.txtSpaceInvadersScore);
        txtSnakeScore = (TextView) findViewById(R.id.txtSnakeScore);
        txtArkanoidScore = (TextView) findViewById(R.id.txtArkanoidScore);
        txtCannonScore = (TextView) findViewById(R.id.txtCannonScore);

        editProfileConfirm = (ImageView) findViewById(R.id.editProfileConfirm);
        editPasswordConfirm = (ImageView) findViewById(R.id.editPasswordConfirm);
        editProfileDummy = (ImageView) findViewById(R.id.editProfileDummy);
        editPasswordDummy = (ImageView) findViewById(R.id.editPasswordDummy);

        //TextInputLayouts
        textInputLayout1 = (TextInputLayout) findViewById(R.id.textInputLayout1);
        textInputLayout2 = (TextInputLayout) findViewById(R.id.textInputLayout2);
        textInputLayout3 = (TextInputLayout) findViewById(R.id.textInputLayout3);
        textInputLayout4 = (TextInputLayout) findViewById(R.id.textInputLayout4);

        editNickname = (TextInputEditText) findViewById(R.id.editNickname);
        editEmail = (TextInputEditText) findViewById(R.id.editEmail);
        editOldPassword = (TextInputEditText) findViewById(R.id.editOldPassword);
        editNewPassword = (TextInputEditText) findViewById(R.id.editNewPassword);

        btnDelete = (Button) findViewById(R.id.btnDeleteProfile);
        btnConfirmDelete = (Button) findViewById(R.id.btnConfirmDelete);
        btnCancelDelete = (Button) findViewById(R.id.btnCancelDelete);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar.setTitle(getString(R.string.strProfile));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_arrow, null));
        setSupportActionBar(toolbar);

        final Typeface tf = Typeface.create(getResources().getFont(R.font.zrnic), Typeface.NORMAL);
        collapsingToolbar.setCollapsedTitleTypeface(tf);
        collapsingToolbar.setExpandedTitleTypeface(tf);

        profileWallpaper = (ImageView) findViewById(R.id.profile_form);
        profileWallpaper.setBackgroundResource(R.drawable.profile_wallpaper_animation);
        AnimationDrawable progressAnimation = (AnimationDrawable) profileWallpaper.getBackground();
        progressAnimation.start();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.strShareApp));
                share.setType("text/plain");
                startActivity(share);

                // Verify that the intent will resolve to an activity
                if (share.resolveActivity(getPackageManager()) != null) {
                    startActivity(share);
                }
            }
        });

        fbHelper = new UnibArcadeFBHelper();
        dbAdapter = new UnibArcadeDBAdapter(this);
        mainPage = new MainPageActivity();

        //check user session and get uid
        fbHelper.checkUserSession();

        // set user info
        setUserProfile();

        showPersonalScore(user.getUid());

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDelete.setVisibility(View.INVISIBLE);
                btnConfirmDelete.setVisibility(View.VISIBLE);
                btnCancelDelete.setVisibility(View.VISIBLE);
                txtAreYouSure.setVisibility(View.VISIBLE);
            }
        });

        btnConfirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });

        btnCancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnConfirmDelete.setVisibility(View.INVISIBLE);
                btnCancelDelete.setVisibility(View.INVISIBLE);
                txtAreYouSure.setVisibility(View.INVISIBLE);

                btnDelete.setVisibility(View.VISIBLE);
            }
        });

        toggleProfile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    editProfileConfirm.setVisibility(View.VISIBLE);

                    txtNickname.setVisibility(View.INVISIBLE);
                    txtEmail.setVisibility(View.INVISIBLE);

                    textInputLayout1.setVisibility(View.VISIBLE);
                    editNickname.setVisibility(View.VISIBLE);

                    textInputLayout2.setVisibility(View.VISIBLE);
                    editEmail.setVisibility(View.VISIBLE);

                } else {

                    //Clearing password TextInputLayouts errors
                    textInputLayout1.setError(null);
                    textInputLayout2.setError(null);

                    editProfileConfirm.setVisibility(View.INVISIBLE);

                    txtNickname.setVisibility(View.VISIBLE);
                    txtEmail.setVisibility(View.VISIBLE);

                    textInputLayout1.setVisibility(View.INVISIBLE);
                    editNickname.setVisibility(View.INVISIBLE);

                    textInputLayout2.setVisibility(View.INVISIBLE);
                    editEmail.setVisibility(View.INVISIBLE);
                }
            }
        });

        togglePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    //Button visibility
                    editPasswordConfirm.setVisibility(View.VISIBLE);

                    //Visibility of new Password layout
                    rlConfirmPasswordLayout.setVisibility(View.VISIBLE);

                    //And relative field and its TextInputLayout
                    textInputLayout3.setVisibility(View.VISIBLE);
                    editOldPassword.setVisibility(View.VISIBLE);

                    txtPassword.setVisibility(View.INVISIBLE);

                } else {

                    //Clearing password TextInputLayouts errors
                    textInputLayout3.setError(null);
                    textInputLayout4.setError(null);

                    editPasswordConfirm.setVisibility(View.INVISIBLE);

                    rlConfirmPasswordLayout.setVisibility(View.GONE);
                    txtPassword.setVisibility(View.VISIBLE);
                    //And relative field and its TextInputLayout
                    textInputLayout3.setVisibility(View.INVISIBLE);
                    editOldPassword.setVisibility(View.INVISIBLE);
                }
            }
        });

        editPasswordConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldPwd = textInputLayout3.getEditText().getText().toString();
                String newPwd = textInputLayout4.getEditText().getText().toString();

                boolean passwordInserted = false;

                if (!(oldPwd.equals("") && newPwd.equals(""))) {
                    if (!oldPwd.equals(newPwd)) {
                        passwordInserted = savePassword(oldPwd, newPwd);
                    }
                }

                if (oldPwd.equals(""))
                    textInputLayout3.setError(getString(R.string.strEmptyFields));
                if (newPwd.equals(""))
                    textInputLayout4.setError(getString(R.string.strEmptyFields));

                if (passwordInserted) {
                    editPasswordConfirm.setVisibility(View.INVISIBLE);
                    textInputLayout3.setVisibility(View.INVISIBLE);

                    txtPassword.setVisibility(View.VISIBLE);

                    togglePassword.setChecked(false);
                }
            }
        });

        editProfileConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nick = textInputLayout1.getEditText().getText().toString();
                String email = textInputLayout2.getEditText().getText().toString();

                boolean emailInserted = false;
                boolean nickInserted = false;

                if (nick.equals(""))
                    textInputLayout1.setError(getString(R.string.strEmptyFields));
                if (email.equals(""))
                    textInputLayout2.setError(getString(R.string.strEmptyFields));

                if (!nick.equals(""))
                    nickInserted = updateUserNick(nick);

                if (!email.equals(""))
                    emailInserted = updateEmail(email);

                if (emailInserted && nickInserted) {

                    textInputLayout1.setError(null);
                    textInputLayout2.setError(null);

                    rlEmailSent.setVisibility(View.VISIBLE);

                    txtEmail.setText(email);
                    txtNickname.setText(nick);

                    //Questi campi diventano invisibili solo se tutto va bene
                    editProfileConfirm.setVisibility(View.INVISIBLE);

                    txtNickname.setVisibility(View.VISIBLE);
                    txtEmail.setVisibility(View.VISIBLE);

                    textInputLayout1.setVisibility(View.INVISIBLE);
                    textInputLayout2.setVisibility(View.INVISIBLE);

                    toggleProfile.setChecked(false);

                } else {
                    textInputLayout2.setError(getString(R.string.strEmailNotUpdated));
                }
            }
        });

        editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkImagePermission();
            }
        });

        editGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPhoto();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        goBack();
    }

    public void goBack() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    //check camera permission
    private void checkImagePermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                //We inform users of the usefulness of the permissions
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.strPermissionNeeded))
                        .setMessage(getString(R.string.strMessagePermission))
                        //What happens when ok button is pressed
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(
                                        new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                            }
                        })
                        //What happens when cancel button is pressed
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        //create and show dialog
                        .create().show();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }
        } else {
            takePictureIntent();
        }
    }

    //set user info
    private void setUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            // nickname, email address, and profile photo Url
            txtNickname.setText(user.getDisplayName());
            txtEmail.setText(user.getEmail());

            //Also editTexts
            textInputLayout1.getEditText().setText(user.getDisplayName());
            textInputLayout2.getEditText().setText(user.getEmail());

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            String uid = user.getUid();
            Log.d(TAG, "email verified: " + emailVerified);
            Log.d(TAG, "User Profile uid: " + uid);

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .into(profilePhoto);
            }
        }
    }

    // insert personal score in profile activity
    private void showPersonalScore(final String userId) {

        String nick = user.getDisplayName();
        String path = "scores/users/" + userId + "/" + "nickname/" + nick + "/game/";

        mRef = db.getReference(path);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            // Attach a listener to read the data at our posts reference
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String score;

                score = dataSnapshot.child("3").getValue(String.class);
                if (score != null)
                    txtSpaceInvadersScore.setText(score);
                else txtSpaceInvadersScore.setText("");

                score = dataSnapshot.child("2").getValue(String.class);
                if (score != null)
                    txtArkanoidScore.setText(score);
                else txtArkanoidScore.setText("");

                score = dataSnapshot.child("4").getValue(String.class);
                if (score != null)
                    txtCannonScore.setText(score);
                else txtCannonScore.setText("");

                score = dataSnapshot.child("1").getValue(String.class);
                if (score != null)
                    txtSnakeScore.setText(score);
                else txtSnakeScore.setText("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Failed to read value: " + error.toException());
            }
        });
    }

    // method that invokes an intent to capture a photo from camera
    private void takePictureIntent() {
        Intent picIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        /**
         *  startActivityForResult() method is protected by a condition that calls resolveActivity(),
         *  which returns the first activity component that can handle the intent. Performing this
         *  check is important because if you call startActivityForResult() using an intent that no
         *  app can handle, your app will crash. So as long as the result is not null, it's safe to
         *  use the intent.
         */
        if (picIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(picIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // method that invokes an intent to capture a photo from storage
    private void pickPhoto() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_EXTERNAL_CONTENT);
    }

    /**
     * The Android Camera application encodes the photo in the return Intent delivered to
     * onActivityResult() as a small Bitmap in the extras, under the key "data". The following
     * code retrieves this image and displays it in an ImageView.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap img = (Bitmap) extras.get("data");
                    profilePhoto.setImageBitmap(img);
                    uploadImageAndSaveUri(img);
                    break;

                case REQUEST_EXTERNAL_CONTENT:
                    Uri imgUri = data.getData();
                    try {
                        // Setting image on image view using Bitmap
                        Bitmap imgGallery = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                        profilePhoto.setImageBitmap(imgGallery);
                        uploadImageAndSaveUri(imgGallery);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    private void uploadImageAndSaveUri(Bitmap img) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference().
                child("images").child(user.getDisplayName() + ".jpeg");
        img.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] image = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(image);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Upload failed! ");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload ok! ");
                getDownloadUrl(storageRef);
            }
        });
    }

    private void getDownloadUrl(StorageReference storageRef) {
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "onSuccess: " + uri);
                updateUserPhoto(uri);
            }
        });
    }

    // update user nickname
    private boolean updateUserNick(String nick) {

        if (!(user.getDisplayName().equals(nick))) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()

                    .setDisplayName(nick)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile updated.");
                                isInserted = true;
                            } else {
                                Log.d(TAG, "Profile nick failed.");
                                isInserted = false;
                            }
                        }
                    });
        }

        return isInserted;
    }

    // update user photo
    private void updateUserPhoto(Uri uri) {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()

                .setPhotoUri(uri)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        } else {
                            Log.d(TAG, "Profile image failed.");
                        }
                    }
                });
    }

    // update user email
    private boolean updateEmail(String email) {

        if (!(user.getEmail().equals(email))) {
            user.updateEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User email address updated.");
                                isInserted = true;
                                user.sendEmailVerification();
                            } else {
                                Log.d(TAG, "User email address failed." + " " + task.getException().toString());
                                isInserted = false;
                            }
                        }
                    });
        }

        return isInserted;
    }

    // Change user password and reauthenticate
    private void updatePassword(String email, String oldPwd, final String newPwd) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPwd);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.updatePassword(newPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                isInserted = false;
                            } else {
                                isInserted = true;
                            }
                        }
                    });
                } else {
                    rlPasswordError.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean savePassword(String oldPwd, String newPwd) {

        String email = user.getEmail();
        updatePassword(email, oldPwd, newPwd);
        if (!getIsInserted()) {
            editProfileDummy.setVisibility(View.VISIBLE);
        } else {
            rlPasswordError.setVisibility(View.VISIBLE);
        }
        return getIsInserted();
    }

    // Delete user from Firebase
    public void deleteUser() {
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mAuth.signOut();
                            ProfileActivity.this.finish();
                        }
                    }
                });
    }

    boolean getIsInserted() {
        return isInserted;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}