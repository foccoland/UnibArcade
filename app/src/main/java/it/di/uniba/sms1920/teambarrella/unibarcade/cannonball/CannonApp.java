package it.di.uniba.sms1920.teambarrella.unibarcade.cannonball;


import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.media.AudioManager;
import android.view.GestureDetector;
import android.view.MotionEvent;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import it.di.uniba.sms1920.teambarrella.unibarcade.R;


public class CannonApp extends AppCompatActivity {


    private GestureDetector gestureDetector; // listens for double taps
    private CannonView cannonView; // custom view to display the game
    private MediaPlayer mediaPlayer;


    /*
     * Controls double tap event
     */
    GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override

        public boolean onDoubleTap(MotionEvent e) {

            cannonView.fireCannonball(e);
            return true;
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //added soundtrack in the background
        mediaPlayer = MediaPlayer.create(this,R.raw.cannonmusic);
        mediaPlayer.start();


        setContentView(R.layout.activity_cannon);



        cannonView = (CannonView) findViewById(R.id.cannonView);






        gestureDetector = new GestureDetector(this, gestureListener);

        // allow volume keys to set game volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onPause() {
        super.onPause();

        mediaPlayer.pause();

        cannonView.stopGame();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();

        cannonView.releaseResources();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    /*
     * Method that handles user touches
     */
    public boolean onTouchEvent(MotionEvent event) {


        //if the state game is set to RUN
        if (cannonView.gameStatus() == CannonView.RUN) {
            // the user touched the screen or dragged along the screen
            if (event.getAction() == MotionEvent.ACTION_DOWN ||
                    event.getAction() == MotionEvent.ACTION_MOVE) {
                cannonView.alignCannon(event); // align the cannon
            }
            //if the state game is set to STOP check touch to restart a new game
        } else if(cannonView.gameStatus() == CannonView.STOP){
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                cannonView.newGame();
                cannonView.setGameMode(CannonView.RUN);

            }//if the state game is set to FIRST check touch to start a fresh new game
        }else if (cannonView.gameStatus() == CannonView.FIRST){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                cannonView.newGame();
                cannonView.setGameMode(CannonView.RUN);
            }

        }
        // call the GestureDetector's onTouchEvent method
        return gestureDetector.onTouchEvent(event);
    }


}

