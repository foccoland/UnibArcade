package it.di.uniba.sms1920.teambarrella.unibarcade;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;

import it.di.uniba.sms1920.teambarrella.unibarcade.arkanoid.ArkanoidActivity;
import it.di.uniba.sms1920.teambarrella.unibarcade.cannonball.CannonApp;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeDBAdapter;
import it.di.uniba.sms1920.teambarrella.unibarcade.snake.Snake;
import it.di.uniba.sms1920.teambarrella.unibarcade.spaceinvaders.SpaceInvadersActivity;

public class LoadingActivity extends AppCompatActivity {

    private static final String SPACE_INVADERS = "SpaceInvadersActivity";
    private static final String ARKANOID = "ArkanoidActivity";
    private static final String CANNON = "CannonApp";

    private ImageView mainLoading;
    private ImageView funLoading;
    private Random randomArt;
    private RelativeLayout loadingScreen;
    private Display display;
    private UnibArcadeDBAdapter dbAdapter;
    private static final String TAG = "LoadingActivity";
    private static String gameId;
    private volatile boolean backPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        dbAdapter = new UnibArcadeDBAdapter(getApplicationContext());
        backPressed = false;

        mainLoading = (ImageView) findViewById(R.id.main_loading);
        funLoading = (ImageView) findViewById(R.id.fun_loading);
        loadingScreen = (RelativeLayout) findViewById(R.id.loadingActivity);

        //Get display dimens
        display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);

        getRandomArt();

        loadAndPlay();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
    }

    // Goes to the game after a 3 second wait
    private void goToGame() {
        Intent received = getIntent();
        String game = received.getExtras().getString("GAME");
        String[] gameNames = this.getResources().getStringArray(R.array.GameList);

        switch (game) {

            case SPACE_INVADERS:
                startActivity(new Intent(this, SpaceInvadersActivity.class));
                String spaceInvadersName = gameNames[2];
                gameId = dbAdapter.retrieveGameId(spaceInvadersName);
                Log.d(TAG, "Space Invaders Id: " + gameId);
                finish();
                break;

            case ARKANOID:
                startActivity(new Intent(this, ArkanoidActivity.class));
                String arkanoidName = gameNames[1];
                gameId = dbAdapter.retrieveGameId(arkanoidName);
                Log.d(TAG, "Arkanoid Id: " + gameId);
                finish();
                break;

            case CANNON:
                startActivity(new Intent(this, CannonApp.class));
                String cannonName = gameNames[3];
                gameId = dbAdapter.retrieveGameId(cannonName);
                Log.d(TAG, "Cannon Id: " + gameId);
                finish();
                break;

            default:
                startActivity(new Intent(this, Snake.class));
                String snakeName = gameNames[0];
                gameId = dbAdapter.retrieveGameId(snakeName);
                Log.d(TAG, "Snake Id: " + gameId);
                finish();
                break;
        }
    }

    /**
     * Questo metodo simula un loading time per ogni app
     */
    private void loadAndPlay() {

        Thread loading = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    sleep(3000);
                    if (!backPressed) {
                        goToGame();
                    }

                } catch (Exception e) {

                }
            }
        };

        loading.start();
    }

    /**
     * Questo metodo ha il compito di impostare un'animazione
     * di caricamento diversa in maniera del tutto casuale
     * e uno sfondo diverso in base all'animazione di loading impostata
     */

    private void getRandomArt() {

        randomArt = new Random();
        int animationChooser = randomArt.nextInt(3);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        mainLoading.setBackgroundResource(R.drawable.main_loading_animation);

        //Rendo altezza e larghezza modificabili dinamicamente
        funLoading.setAdjustViewBounds(true);

        switch (animationChooser) {

            case 0:
                funLoading.setBackgroundResource(R.drawable.motorbike_loading_animation);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.mbSecondary));
                loadingScreen.setBackgroundColor(getResources().getColor(R.color.mbGreen, null));
                break;

            case 1:
                funLoading.setBackgroundResource(R.drawable.sm_loading_animation);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.smSecondary));
                loadingScreen.setBackgroundColor(getResources().getColor(R.color.smSkyColor, null));
                break;

            case 2:
                funLoading.setBackgroundResource(R.drawable.pm_loading_animation);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.pmSecondary));
                loadingScreen.setBackgroundColor(getResources().getColor(R.color.pmBlack, null));
                break;
        }

        AnimationDrawable progressMainAnimation = (AnimationDrawable) mainLoading.getBackground();
        AnimationDrawable progressFunAnimation = (AnimationDrawable) funLoading.getBackground();

        progressMainAnimation.start();
        progressFunAnimation.start();
    }

    public static String getGameId() {
        return gameId;
    }
}
