package it.di.uniba.sms1920.teambarrella.unibarcade;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import it.di.uniba.sms1920.teambarrella.unibarcade.account.LoginActivity;
import it.di.uniba.sms1920.teambarrella.unibarcade.account.ProfileActivity;
import it.di.uniba.sms1920.teambarrella.unibarcade.account.RegisterActivity;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeDBAdapter;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeFBHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainPageActivity extends AppCompatActivity {

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    /**
     *
     * @param savedInstanceState
     * Activity menu principale
     */

    ImageView menuWallpaper;

    private UnibArcadeDBAdapter dbAdapter;

    Button btnPlay, btnLogin, btnRegister, btnScore, btnProfile, btnLogout;

    private TextView appTitle, creatorSubTitle;

    //Servizio musicale
    private boolean mIsBound;
    private MusicService mServ;
    HomeWatcher mHomeWatcher;

    //firebase authentication instance
    private FirebaseAuth mAuth;
    
    UnibArcadeFBHelper fbHelper;

    // AuthStateListener is called when there is a change in the authentication state.
    private FirebaseAuth.AuthStateListener mAuthListener;

    public static final String TAG = "MainPageActivity";

    private Context context;
    ConstraintLayout mainPageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        mainPageLayout = (ConstraintLayout) findViewById(R.id.mainPageLayout);
        this.context = this;

        //Button
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnScore = (Button) findViewById(R.id.btnScore);
        btnProfile = (Button) findViewById(R.id.btnProfile);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        creatorSubTitle = (TextView) findViewById(R.id.txtCreators);
        appTitle = (TextView) findViewById(R.id.appMainTitle);

        btnProfile.setVisibility(View.INVISIBLE);
        btnLogout.setVisibility(View.INVISIBLE);

        //new instance for db
        dbAdapter = new UnibArcadeDBAdapter(getApplicationContext());
        fbHelper = new UnibArcadeFBHelper();

        //initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        //get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "Current user: " + user);

        //checking user session
        checkUser();

        //if user already registered
        autoLogin();

        menuWallpaper = (ImageView) findViewById(R.id.menu_wallpaper);
        menuWallpaper.setBackgroundResource(R.drawable.menu_wallpaper_animation);
        AnimationDrawable progressAnimation = (AnimationDrawable) menuWallpaper.getBackground();
        progressAnimation.start();

        //Animation and title color
        applyGradientAppTitle();
        animateTitle();

        //link to MusicService
        doBindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        startService(music);
        Log.e(TAG, "Music Service started");

        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });

        mHomeWatcher.startWatch();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToGamePageActivity();
                //insert game data on db local and Firebase
                dbAdapter.insertGameData(getApplicationContext());
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegisterActivity();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLoginActivity();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                btnLogin.setVisibility(View.VISIBLE);
                btnRegister.setVisibility(View.VISIBLE);
                btnLogout.setVisibility(View.INVISIBLE);
                btnProfile.setVisibility(View.INVISIBLE);
                Log.d(TAG, "User Logged - btnLogout: ");
            }
        });

        btnScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToScoreActivity();
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfileActivity();
            }
        });

        checkConnection();
    }

    private void checkConnection() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String connectionStatus;
                boolean connected = snapshot.getValue(Boolean.class);
                if (!connected) {
                    connectionStatus = "Not Connected";
                    //Log.d(TAG, connectionStatus);
                    final Snackbar offline = Snackbar.make(mainPageLayout, connectionStatus, Snackbar.LENGTH_INDEFINITE);
                    offline.setAction(R.string.strDismiss, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            offline.dismiss();
                        }
                    });
                    offline.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Listener was cancelled");
            }
        });
    }

    // Checking user session
    private void checkUser() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            /**
             * OnAuthStateChanged gets invoked in the UI thread on changes in the authentication state:
             * Right after the listener has been registered
             * When a user is signed in
             * When the current user is signed out
             * When the current user changes
             * When there is a change in the current user’s token
             */
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    //user not login
                    Log.d(TAG, "No user logged");
                    btnLogin.setVisibility(View.VISIBLE);
                    btnRegister.setVisibility(View.VISIBLE);
                    btnLogout.setVisibility(View.INVISIBLE);
                    btnProfile.setVisibility(View.INVISIBLE);
                } else {
                    String uid = mAuth.getCurrentUser().getUid();
                }
            }
        };
    }

    // auto login if the user already registered
    private void autoLogin() {
        if (mAuth.getCurrentUser() != null) {
            // User is signed in (getCurrentUser() will be null if not signed in)
            btnLogin.setVisibility(View.INVISIBLE);
            btnRegister.setVisibility(View.INVISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            btnProfile.setVisibility(View.VISIBLE);
            Log.d(TAG, "User logged: " + mAuth.getCurrentUser().getEmail());
        }
    }

    private void animateTitle() {
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.anim_app_title);
        bounce.reset();

        appTitle.clearAnimation();
        creatorSubTitle.clearAnimation();

        appTitle.startAnimation(bounce);
        creatorSubTitle.startAnimation(bounce);
    }

    private void applyGradientAppTitle() {

        int mainTitleColorUp = this.getColor(R.color.colorAccentPrimary);
        int mainTitleColorDown = this.getColor(R.color.colorAccentPrimaryDark);

        int creatorSubtitleColorUp = this.getColor(R.color.colorPrimaryCannonball);
        int creatorSubtitleColorDown = this.getColor(R.color.colorAccentCannonball);

        //Shaders
        Shader shaderMainTitle = new LinearGradient(
                0, 0, 0, appTitle.getTextSize(),
                mainTitleColorUp, mainTitleColorDown,
                Shader.TileMode.CLAMP);

        Shader shaderCreatorsSubtitle = new LinearGradient(
                0, 0, 0, creatorSubTitle.getTextSize(),
                creatorSubtitleColorUp, creatorSubtitleColorDown,
                Shader.TileMode.CLAMP);

        //Applying shaders
        appTitle.getPaint().setShader(shaderMainTitle);
        creatorSubTitle.getPaint().setShader(shaderCreatorsSubtitle);

    }

    private void goToProfileActivity() {
        Intent goToProfilePage = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(goToProfilePage);
    }

    private void goToScoreActivity() {
        Intent goToScorePage = new Intent(getApplicationContext(), ScoreActivity.class);
        startActivity(goToScorePage);
    }

    private void goToGamePageActivity() {
        Intent goToGamePage = new Intent(getApplicationContext(), GamePageActivity.class);
        startActivity(goToGamePage);
    }

    private void goToRegisterActivity() {
        Intent goToRegister = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(goToRegister);
    }

    private void goToLoginActivity() {
        Intent goToLogin = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(goToLogin);
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

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(serviceConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // add AuthStateListener.
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume() called");

        if (mServ != null) {
            mServ.resumeMusic();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove AuthStateListener
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        doUnbindService();
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        stopService(music);

    }

    @Override
    protected void onPause() {
        super.onPause();

        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }

    }
}

