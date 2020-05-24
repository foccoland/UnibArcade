/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.di.uniba.sms1920.teambarrella.unibarcade.snake;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;




/**
 * Snake: a simple game that everyone can enjoy.
 * 
 * This is an implementation of the classic Game "Snake", in which you control a serpent roaming
 * around the garden looking for apples. Be careful, though, because when you catch one, not only
 * will you become longer, but you'll move faster. Running into yourself or the walls will end the
 * game.
 * 
 */
public class Snake extends Activity {



    /**
     * Constants for desired direction of moving the snake
     */
    public static int MOVE_LEFT = 0;
    public static int MOVE_UP = 1;
    public static int MOVE_DOWN = 2;
    public static int MOVE_RIGHT = 3;

    private static String ICICLE_KEY = "snake-view";

    private SnakeView mSnakeView;
    private MediaPlayer mediaPlayer;


    /**
     * Called when Activity is first created. Turns off the title bar, sets up the content views,
     * and fires up the SnakeView.
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.snake_layout);

        mSnakeView = (SnakeView) findViewById(R.id.snake);
        mSnakeView.setDependentViews((TextView) findViewById(R.id.text),
                findViewById(R.id.background), findViewById(R.id.background));

        if (savedInstanceState == null) {
            //if the status is null I create a new game
            mSnakeView.setMode(SnakeView.READY);
        } else {
            //restore the game state
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
                mSnakeView.restoreState(map);
            } else {
                mSnakeView.setMode(SnakeView.PAUSE);
            }
        }

        //method of moving the snake using the touchscreen
        mSnakeView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSnakeView.getGameState() == SnakeView.RUNNING) {
                    // Normalize x,y between 0 and 1
                    float x = event.getX() / v.getWidth();
                    float y = event.getY() / v.getHeight();

                    // Direction will be [0,1,2,3] depending on quadrant
                    int direction = 0;
                    direction = (x > y) ? 1 : 0; //quadrante inf_sx o sup_dx
                    direction += (x > 1 - y) ? 2 : 0; // quadrante sx o dx dei disponibili

                    // Direction is same as the quadrant which was clicked
                    mSnakeView.moveSnake(direction);
                } else {
                    // If the game is not running then on touching any part of the screen
                    // we start the game by sending MOVE_UP signal to SnakeView
                    mSnakeView.moveSnake(MOVE_UP);
                }
                return false;
            }
        });

        //added soundtrack in the background
        mediaPlayer = MediaPlayer.create(this,R.raw.snakemusic);
        mediaPlayer.start();



    }

    @Override
    protected void onPause() {
        super.onPause();
        //I stop the music if the game is paused
        mediaPlayer.stop();
        //if the game is paused, I save the status and show a message
        mSnakeView.setMode(SnakeView.PAUSE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //I stop the music if I get out of the game
       mediaPlayer.pause();


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Store the game state
        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }

    /**
     * Handles key events in the game. Update the direction our snake is traveling based on the
     * DPAD.
     *
     */

    //keyboard movement management
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_W://.KEYCODE_DPAD_UP
                mSnakeView.moveSnake(MOVE_UP);
                break;
            case KeyEvent.KEYCODE_D://KEYCODE_DPAD_RIGHT
                mSnakeView.moveSnake(MOVE_RIGHT);
                break;
            case KeyEvent.KEYCODE_S://KEYCODE_DPAD_DOWN
                mSnakeView.moveSnake(MOVE_DOWN);
                break;
            case KeyEvent.KEYCODE_A://KEYCODE_DPAD_LEFT
                mSnakeView.moveSnake(MOVE_LEFT);
                break;
        }
        return super.onKeyDown(keyCode, msg);
    }


}
