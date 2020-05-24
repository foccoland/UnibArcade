package it.di.uniba.sms1920.teambarrella.unibarcade.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.widget.Toolbar;

import com.muddzdev.styleabletoast.StyleableToast;

import it.di.uniba.sms1920.teambarrella.unibarcade.MusicService;
import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeFBHelper;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeDBAdapter;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private static final int MIN_PWD_LENGTH = 6;

    private TextInputLayout editEmail, editNick, editPwd, editRepeatPwd;
    private Button btnRegister;

    //instance of local db Adapter to access the methods
    private UnibArcadeDBAdapter dbAdapter;

    //instance of Firebase OpenHelper to access the methods
    private UnibArcadeFBHelper fbHelper;

    /**
     * Manage db on Firebase
     * To read or write data from the database, we need an instance of DatabaseReference
     */
    private DatabaseReference mRef;

    private ImageView formWallpaper;
    private Toolbar toolbar;

    private MusicService mServ;
    private boolean mIsBound;

    //firebase authentication instance
    private FirebaseAuth mAuth;
    private boolean isSuccess;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        doBindService();

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.tlbRegister);
        toolbar.setNavigationIcon(getDrawable(R.drawable.back_arrow));
        toolbar.setTitle(getString(R.string.app_name));

        //EditText
        editNick = (TextInputLayout) findViewById(R.id.textInputLayout1);
        editEmail = (TextInputLayout) findViewById(R.id.textInputLayout2);
        editPwd = (TextInputLayout) findViewById(R.id.textInputLayout3);
        editRepeatPwd = (TextInputLayout) findViewById(R.id.textInputLayout4);

        //Button
        btnRegister = (Button) findViewById(R.id.btnRegister);

        //set wallpaper to background register activity
        formWallpaper = (ImageView) findViewById(R.id.form_wallpaper);
        formWallpaper.setBackgroundResource(R.drawable.form_animation);
        AnimationDrawable progressAnimation = (AnimationDrawable) formWallpaper.getBackground();
        progressAnimation.start();

        //new instance for local db
        dbAdapter = new UnibArcadeDBAdapter(getApplicationContext());

        //new instance for Firebase
        fbHelper = new UnibArcadeFBHelper();

        mRef = FirebaseDatabase.getInstance().getReference();

        //initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
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

    //register user on Firebase - Authentication
    private void register() {
        String email = editEmail.getEditText().getText().toString();
        final String nick = editNick.getEditText().getText().toString();
        String pwd = editPwd.getEditText().getText().toString();
        String repeatPwd = editRepeatPwd.getEditText().getText().toString();

        //Clear TextInputLayout errors
        editEmail.setError(null);
        editNick.setError(null);
        editPwd.setError(null);
        editRepeatPwd.setError(null);

        //check empty fields
        if (email.equals("") || nick.equals("") || pwd.equals("") || repeatPwd.equals("")) {

            if (email.equals(""))
                editEmail.setError(getString(R.string.strEmptyFields));
            if (nick.equals(""))
                editNick.setError(getString(R.string.strEmptyFields));
            if (pwd.equals(""))
                editPwd.setError(getString(R.string.strEmptyFields));
            if (repeatPwd.equals(""))
                editRepeatPwd.setError(getString(R.string.strEmptyFields));
            return;
        } else {
            //check matching password
            if (!repeatPwd.equals(pwd)) {
                editPwd.setError(getString(R.string.strPwdError));
                editRepeatPwd.setError(getString(R.string.strPwdError));
                return;
            } else {
                //check pwd length - minimum 6 characters
                if (pwd.length() < MIN_PWD_LENGTH) {
                    editPwd.setError(getString(R.string.strPwdTooShort));
                    editRepeatPwd.setError(getString(R.string.strPwdTooShort));
                    return;
                }
            }
        }

        mAuth.createUserWithEmailAndPassword(email, pwd)
                //Listener called when a Task completes
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "New user registration: " + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            String errorMessage = task.getException().getMessage();
                            if (errorMessage.equals("The email address is already in use by another account.")) {
                                errorMessage = getString(R.string.strMailAlreadyExists);
                                editEmail.setError(errorMessage);
                            } else if (errorMessage.equals("The email address is badly formatted.")) {
                                errorMessage = getString(R.string.strIncorrectEmail);
                                editEmail.setError(errorMessage);
                            }
                            Log.i(TAG, "Failed to create user " + task.getException().getMessage());
                        } else {
                            Log.d(TAG, "Auth ok");
                            //get current user
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "User registered: " + user.getEmail());

                            // add display name (nickname)
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nick)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated. User nick: " + nick);
                                            }
                                        }
                                    });

                            //send email
                            sendEmailVerification(user);
                            //show toast
                            StyleableToast.makeText(getApplicationContext(), getString(R.string.emailSent),
                                    Toast.LENGTH_LONG,
                                    R.style.successfulToast).show();
                            RegisterActivity.this.finish();
                        }
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            isSuccess = true;
                        }
                    }
                });
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServ = ((MusicService.ServiceBinder)service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }
}
