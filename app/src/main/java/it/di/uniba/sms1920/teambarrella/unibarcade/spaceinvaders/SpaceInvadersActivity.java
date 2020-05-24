package it.di.uniba.sms1920.teambarrella.unibarcade.spaceinvaders;

import android.app.Activity;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;

import it.di.uniba.sms1920.teambarrella.unibarcade.R;

/**
 * SpaceInvadersActivity is the entry point to the game.
 * It will handle the lifecycle of the game by calling methods of spaceInvadersEngine when prompted
 * to so by the OS.
 */
public class SpaceInvadersActivity extends Activity {

    /**
     * spaceInvadersEngine will be the view of the game. It will also hold the logic of the game
     * and respond to screen touches as well
     */
    SpaceInvadersEngine spaceInvadersEngine;
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
        spaceInvadersEngine = new SpaceInvadersEngine(this, size.x, size.y);
        setContentView(spaceInvadersEngine);

        mediaPlayer = MediaPlayer.create(this, R.raw.spaceinvadersmusic);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(50, 50);
        }
        mediaPlayer.start();

    }

    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        spaceInvadersEngine.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        spaceInvadersEngine.pause();

        //Tell music to pause
        mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }
}
