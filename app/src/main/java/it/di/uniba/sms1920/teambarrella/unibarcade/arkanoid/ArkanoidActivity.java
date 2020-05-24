package it.di.uniba.sms1920.teambarrella.unibarcade.arkanoid;

import android.app.Activity;

import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;

import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import it.di.uniba.sms1920.teambarrella.unibarcade.account.LoginActivity;

public class ArkanoidActivity extends Activity {

    ArkanoidEngine arkanoidEngine;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();
        // Load the resolution into a Point object
        Point size = new Point();
        display.getSize(size);

        // Initialize gameView and set it as the view
        arkanoidEngine = new ArkanoidEngine(this, size.x, size.y);
        setContentView(arkanoidEngine);

        mediaPlayer = MediaPlayer.create(this,R.raw.arkanoidmusic);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(50, 50);
        }
        mediaPlayer.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
        arkanoidEngine.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        arkanoidEngine.pause();
        mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }
}
