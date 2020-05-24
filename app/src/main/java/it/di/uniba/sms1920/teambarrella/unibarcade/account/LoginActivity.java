package it.di.uniba.sms1920.teambarrella.unibarcade.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import it.di.uniba.sms1920.teambarrella.unibarcade.MainPageActivity;
import it.di.uniba.sms1920.teambarrella.unibarcade.MusicService;
import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeDBAdapter;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Login";

    private static final int MIN_PWD_LENGTH = 6;

    private ImageView formWallpaper;
    private Toolbar toolbar;
    private TextInputLayout editEmail, editPwd;
    private Button btnLogin, btnGoogleLogin;
    private TextView txtForgottenPwd, txtNotRegistered, txtClickHere;
    private UnibArcadeDBAdapter dbAdapter;
    private MusicService mServ;
    private boolean mIsBound;

    //firebase authentication instance
    private FirebaseAuth mAuth;

    // Google
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        doBindService();

        toolbar = (Toolbar) findViewById(R.id.tlbLogin);
        toolbar.setNavigationIcon(getDrawable(R.drawable.back_arrow));
        toolbar.setTitle(getString(R.string.app_name));

        editEmail = (TextInputLayout) findViewById(R.id.textInputLayout1);
        editPwd = (TextInputLayout) findViewById(R.id.textInputLayout2);

        txtForgottenPwd = (TextView) findViewById(R.id.txtForgottenPwd);
        txtNotRegistered = (TextView) findViewById(R.id.txtNotRegistered);
        txtClickHere = (TextView) findViewById(R.id.txtClickHere);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnGoogleLogin = (Button) findViewById(R.id.btnGoogleLogin);

        //set wallpaper to background login activity
        formWallpaper = (ImageView) findViewById(R.id.form_wallpaper);
        formWallpaper.setBackgroundResource(R.drawable.form_animation);
        AnimationDrawable progressAnimation = (AnimationDrawable) formWallpaper.getBackground();
        progressAnimation.start();

        dbAdapter = new UnibArcadeDBAdapter(getApplicationContext());

        //initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        goBack();

        txtClickHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegisterActivity();
            }
        });

        txtForgottenPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPwd();
            }
        });

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void goToRegisterActivity() {
        Intent goToRegister = new Intent(this, RegisterActivity.class);
        startActivity(goToRegister);
        finish();
    }

    public void goBack() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // Login with email and password
    private void login() {
        String email = editEmail.getEditText().getText().toString();
        String pwd = editPwd.getEditText().getText().toString();

        //Clear TextInputLayout errors
        editEmail.setError(null);
        editPwd.setError(null);

        //check empty fields
        if (email.equals("") || pwd.equals("")) {
            if (email.equals(""))
                editEmail.setError(getString(R.string.strEmptyFields));
            if (pwd.equals(""))
                editPwd.setError(getString(R.string.strEmptyFields));
            return;
        } else if (pwd.length() < MIN_PWD_LENGTH) {
            //check minimum pwd length
            editPwd.setError(getString(R.string.strPwdTooShort));
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            String errorMessage = task.getException().getLocalizedMessage();
                            Log.d(TAG, errorMessage);

                            if (errorMessage.equals("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                                errorMessage = getString(R.string.strNoUserExists);
                                editEmail.setError(errorMessage);
                            } else if (errorMessage.equals("The password is invalid or the user does not have a password.")) {
                                errorMessage = getString(R.string.strPwdWrong);
                                editPwd.setError(errorMessage);
                            } else if (errorMessage.equals("We have blocked all requests from this device due to unusual activity. Try again later. [ Too many unsuccessful login attempts. Please try again later. ]")) {
                                errorMessage = getString(R.string.strUnusualActivity);
                                editEmail.setError(errorMessage);
                            } else if (errorMessage.equals("The email address is badly formatted.")) {
                                errorMessage = getString(R.string.strIncorrectEmail);
                                editEmail.setError(errorMessage);
                            }
                        } else {
                            String userUID = mAuth.getUid();
                            Log.d(TAG, "Login ok! User UID " + userUID);
                            goToMainActivity();
                            LoginActivity.this.finish();
                        }
                    }
                });
    }

    // Google sign in
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Google sign out
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                Intent goToMainPage = new Intent(this, MainPageActivity.class);
                startActivity(goToMainPage);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    // Auth with Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "User registered: " + user.getEmail());

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failed");
                        }
                    }
                });
    }

    // forgotten password - reset with mail
    private void resetPwd() {
        String email = editEmail.getEditText().getText().toString();

        if (email.equals("")) {
            editEmail.setError(getString(R.string.strResetEmail));
        } else {
            //send a email for reset previous password
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                txtClickHere.setVisibility(View.GONE);
                                txtNotRegistered.setText(getString(R.string.emailSent));
                            } else {
                                txtClickHere.setVisibility(View.GONE);
                                txtNotRegistered.setText(getString(R.string.strFailedSend));
                            }
                        }
                    });
        }
    }

    //go to MainActivity
    private void goToMainActivity() {
        Intent goToMain = new Intent(getApplicationContext(), MainPageActivity.class);
        startActivity(goToMain);
        finish();
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

    @Override
    protected void onPause() {
        super.onPause();
        mServ.pauseMusic();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
}
