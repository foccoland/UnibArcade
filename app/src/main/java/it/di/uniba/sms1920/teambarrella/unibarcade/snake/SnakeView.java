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

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import it.di.uniba.sms1920.teambarrella.unibarcade.LoadingActivity;
import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.Score;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeDBAdapter;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeFBHelper;


/**
 * SnakeView: implementation of a simple game of Snake
 */
public class SnakeView extends TileView {

    private static final String TAG = "SnakeView";
    UnibArcadeDBAdapter dbAdapter;

    private UnibArcadeFBHelper fbHelper;

    /**
     * Current mode of application: READY to run, RUNNING, or you have already lost. static final
     * ints are used instead of an enum for performance reasons.
     */
    private int mMode = READY;
    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int LOSE = 3;

    /**
     * Current direction the snake is headed.
     */
    private int mDirection = NORTH;
    private int mNextDirection = NORTH;
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int EAST = 3;
    private static final int WEST = 4;

    /**
     * Labels for the drawables that will be loaded into the TileView class
     */

    private static final int APPLE   = 1;
    private static final int BRICK_STAR = 2;
    private static final int PURPLE_STAR = 3;
    private static final int SNAKE_FACE = 4;


    /**
     * mScore: Used to track the number of apples captured mMoveDelay: number of milliseconds
     * between snake movements. This will decrease as apples are captured.
     */
    private static int mScore = 0;
    private static int totalScore;
    private long mMoveDelay = 0;
    /**
     * mLastMove: Tracks the absolute time when the snake last moved, and is used to determine if a
     * move should be made based on mMoveDelay.
     */
    private long mLastMove;

    /**
     * mStatusText: Text shows to the user in some run states
     */
    private TextView mStatusText;

    /**
     * mArrowsView: View which shows 4 arrows to signify 4 directions in which the snake can move
     */
    private View mArrowsView;

    /**
     * mBackgroundView: Background View which shows 4 different colored triangles pressing which
     * moves the snake
     */
    private View mBackgroundView;

    /**
     * mSnakeTrail: A list of Coordinates that make up the snake's body mAppleList: The secret
     * location of the juicy apples the snake craves.
     */
    //coordinate in cui si muove il serpente
    private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
    //coordinate in cui appaiono le mele
    private ArrayList<Coordinate> mAppleList = new ArrayList<Coordinate>();

    /**
     * Everyone needs a little randomness in their life
     */
    private static final Random RNG = new Random();

    /**
     * Create a simple handler that we can use to cause animation to happen. We set ourselves as a
     * target and we can use the sleep() function to cause an update/invalidate to occur at a later
     * date.
     */

    //gestisce le animazioni
    private RefreshHandler mRedrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            SnakeView.this.update();
            SnakeView.this.invalidate();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };

    /**
     * Constructs a SnakeView based on inflation from XML
     * 
     * @param context
     * @param attrs
     */
    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSnakeView(context);
    }

    public SnakeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initSnakeView(context);
    }

    private void initSnakeView(Context context) {

        setFocusable(true);

        Resources r = this.getContext().getResources();

        //risorse usate per "costruire" il gioco
        resetTiles(8);

        loadTileApple(APPLE, r.getDrawable(R.drawable.apple,null));
        loadTile(BRICK_STAR,r.getDrawable(R.drawable.brick,null));
        loadTile(PURPLE_STAR,r.getDrawable(R.drawable.purplestar,null));
        loadTile(SNAKE_FACE,r.getDrawable(R.drawable.purplestar,null));



    }

    //method that creates a new game
    private void initNewGame() {
        mSnakeTrail.clear();
        mAppleList.clear();

        // For now we're just going to load up a short default eastbound snake
        // that's just turned north
        //Add tail at the beginning

        //coordinates with which the snake appears at the beginning of the game
        mSnakeTrail.add(new Coordinate(6, 7));
        mSnakeTrail.add(new Coordinate(5, 7));

        mNextDirection = NORTH;

        //method that creates apples
        addRandomApple();
        addRandomApple();

        //starting speed
        mMoveDelay = 300;
        //score
        mScore = 0;
        totalScore = 0;
    }

    /**
     * Given a ArrayList of coordinates, we need to flatten them into an array of ints before we can
     * stuff them into a map for flattening and storage.
     * 
     * @param cvec : a ArrayList of Coordinate objects
     * @return : a simple array containing the x/y values of the coordinates as
     *         [x1,y1,x2,y2,x3,y3...]
     */
    private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
        int[] rawArray = new int[cvec.size() * 2];

        int i = 0;
        for (Coordinate c : cvec) {
            rawArray[i++] = c.x;
            rawArray[i++] = c.y;
        }

        return rawArray;
    }

    /**
     * Save game state so that the user does not lose anything if the game process is killed while
     * we are in the background.
     * 
     * @return a Bundle with this view's state
     */
    public Bundle saveState() {
        Bundle map = new Bundle();

        map.putIntArray("mAppleList", coordArrayListToArray(mAppleList));
        map.putInt("mDirection", Integer.valueOf(mDirection));
        map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
        map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
        map.putInt("totalScore", Integer.valueOf(totalScore));
        map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail));

        return map;
    }

    /**
     * Given a flattened array of ordinate pairs, we reconstitute them into a ArrayList of
     * Coordinate objects
     * 
     * @param rawArray : [x1,y1,x2,y2,...]
     * @return a ArrayList of Coordinates
     */
    private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
        ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();

        int coordCount = rawArray.length;
        for (int index = 0; index < coordCount; index += 2) {
            Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
            coordArrayList.add(c);
        }
        return coordArrayList;
    }

    /**
     * Restore game state if our process is being relaunched
     * 
     * @param icicle a Bundle containing the game state
     */
    public void restoreState(Bundle icicle) {
        setMode(PAUSE);

        mAppleList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
        mDirection = icicle.getInt("mDirection");
        mNextDirection = icicle.getInt("mNextDirection");
        mMoveDelay = icicle.getLong("mMoveDelay");
        totalScore = icicle.getInt("totalScore");
        mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
    }

    /**
     * Handles snake movement triggers from Snake Activity and moves the snake accordingly. Ignore
     * events that would cause the snake to immediately turn back on itself.
     *
     * @param direction The desired direction of movement
     */
    public void moveSnake(int direction) {
        if (direction == Snake.MOVE_UP) {
            if (mMode == READY | mMode == LOSE) {
                /*
                 * At the beginning of the game, or the end of a previous one,
                 * we should start a new game if UP key is clicked.
                 */
                initNewGame();
                setMode(RUNNING);
                update();
                return;
            }
            if (mMode == PAUSE) {
                /*
                 * If the game is merely paused, we should just continue where we left off.
                 */
                setMode(RUNNING);
                update();
                return;
            }
            if (mDirection != SOUTH) {
                mNextDirection = NORTH;
            }
            return;
        }

        if (direction == Snake.MOVE_DOWN) {
            if (mDirection != NORTH) {
                mNextDirection = SOUTH;
            }
            return;
        }

        if (direction == Snake.MOVE_LEFT) {
            if (mDirection != EAST) {
                mNextDirection = WEST;
            }
            return;
        }

        if (direction == Snake.MOVE_RIGHT) {
            if (mDirection != WEST) {
                mNextDirection = EAST;
            }
            return;
        }

    }

    /**
     * Sets the Dependent views that will be used to give information (such as "Game Over" to the
     * user and also to handle touch events for making movements
     *
     */
    public void setDependentViews(TextView msgView, View arrowView, View backgroundView) {
        mStatusText = msgView;
        mArrowsView = arrowView;
        mBackgroundView = backgroundView;
    }

    /**
     * Updates the current mode of the application (RUNNING or PAUSED or the like) as well as sets
     * the visibility of textview for notification
     * 
     * @param newMode
     */
    public void setMode(int newMode) {
        int oldMode = mMode;
        mMode = newMode;


        if (newMode == RUNNING && oldMode != RUNNING) {
            // hide the game instructions
            mStatusText.setVisibility(View.INVISIBLE);
            update();
            // make the background and arrows visible as soon the snake starts moving
            mArrowsView.setVisibility(View.VISIBLE);
            mBackgroundView.setVisibility(View.VISIBLE);
            return;
        }

        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSE) {
            mArrowsView.setVisibility(View.GONE);
            mBackgroundView.setVisibility(View.GONE);
            str = res.getText(R.string.mode_pause);
        }
        if (newMode == READY) {
            mArrowsView.setVisibility(View.GONE);
            mBackgroundView.setVisibility(View.GONE);

            str = res.getText(R.string.mode_ready);
        }
        if (newMode == LOSE) {

            mArrowsView.setVisibility(View.GONE);
            mBackgroundView.setVisibility(View.GONE);
            mAppleList.clear();
            str = res.getString(R.string.mode_lose, totalScore);

            //when the game ends the score is entered in local db and Firebase
            //check user session and return user uid
            dbAdapter = new UnibArcadeDBAdapter(getContext());
            fbHelper = new UnibArcadeFBHelper();

            //takes the uid and associates it with the game
            String uid = fbHelper.checkUserSession();
            //Log.d(TAG, "User logged uid: " + uid);
            Score score = new Score(uid, LoadingActivity.getGameId(), Integer.toString(totalScore));
            fbHelper.updateScore(score);
            //dbAdapter.insertScoreData(score);
        }

        //set text to game over
        mStatusText.setText(str);
        mStatusText.setVisibility(View.VISIBLE);
    }

    /**
     * @return the Game state as Running, Ready, Paused, Lose
     */
    public int getGameState() {
        return mMode;
    }

    /**
     * Selects a random location within the garden that is not currently covered by the snake.
     * Currently _could_ go into an infinite loop if the snake currently fills the garden, but we'll
     * leave discovery of this prize to a truly excellent snake-player.
     */
    //create apple in random location
    private void addRandomApple() {
        Coordinate newCoord = null;
        boolean found = false;
        while (!found) {
            // Choose a new location for our apple
            int newX = 1 + RNG.nextInt(mXTileCount - 2);
            int newY = 1 + RNG.nextInt(mYTileCount - 2);
            newCoord = new Coordinate(newX, newY);

            // Make sure it's not already under the snake
            boolean collision = false;
            int snakelength = mSnakeTrail.size();
            for (int index = 0; index < snakelength; index++) {
                if (mSnakeTrail.get(index).equals(newCoord)) {
                    collision = true;
                }
            }
            // if we're here and there's been no collision, then we have
            // a good location for an apple. Otherwise, we'll circle back
            // and try again
            found = !collision;
        }
        if (newCoord == null) {
            Log.e(TAG, "Somehow ended up with a null newCoord!");
        }
        mAppleList.add(newCoord);
    }

    /**
     * Handles the basic update loop, checking to see if we are in the running state, determining if
     * a move should be made, updating the snake's location.
     */
    public void update() {
        if (mMode == RUNNING) {
            long now = System.currentTimeMillis();

            if (now - mLastMove > mMoveDelay) {
                clearTiles();
                updateWalls();
                updateSnake();
                updateApples();
                mLastMove = now;
            }
            mRedrawHandler.sleep(mMoveDelay);
        }

    }

    /**
     * Draws some walls.
     */
    private void updateWalls() {
       //asse x
        for (int x = 0; x < mXTileCount/2  ; x++) {
            setTile(BRICK_STAR, x , 0);
            setTile(BRICK_STAR, x , mYTileCount -1 );
        }
        for (int x = (mXTileCount/2) +1; x < mXTileCount  ; x++) {
            setTile(BRICK_STAR, x , 0);
            setTile(BRICK_STAR, x , mYTileCount -1 );
        }

        //asse y
        for (int y = 0; y < mYTileCount/2;y++ ) {
            setTile(BRICK_STAR, 0, y );
            setTile(BRICK_STAR, mXTileCount - 1, y);
        }
        for (int y = (mYTileCount/2) +1; y < mYTileCount  ;y++ ) {
            setTile(BRICK_STAR, 0, y );
            setTile(BRICK_STAR, mXTileCount - 1, y);
        }


    }

    /**
     * Draws some apples.
     */
    private void updateApples() {
        for (Coordinate c : mAppleList) {
            setTile(APPLE, c.x, c.y);
        }
    }

    //method that manages the loop functionality
    //Loop snake asse x
    private int loopSnakex(int i) {
        while(i<0) {
            i += mXTileCount;
        }
        return i % mXTileCount;
    }
    //loop snake asse y
    private int loopSnakey(int i) {
        while(i<0) {
            i += mYTileCount;
        }
        return i % mYTileCount;
    }

    /**
     * Figure out which way the snake is going, see if he's run into anything (the walls, himself,
     * or an apple). If he's not going to die, we then add to the front and subtract from the rear
     * in order to simulate motion. If we want to grow him, we don't subtract from the rear.
     */
    private void updateSnake() {
        boolean growSnake = false;

        // Grab the snake by the head
        Coordinate head = mSnakeTrail.get(0);
        Coordinate newHead = new Coordinate(1, 1);

        mDirection = mNextDirection;

        switch (mDirection) {
            case EAST: {
                newHead = new Coordinate(loopSnakex(head.x + 1), head.y);
                break;
            }
            case WEST: {
                newHead = new Coordinate(loopSnakex(head.x - 1), head.y);
                break;
            }
            case NORTH: {
                newHead = new Coordinate(head.x, loopSnakey(head.y - 1));
                break;
            }
            case SOUTH: {
                newHead = new Coordinate(head.x, loopSnakey(head.y + 1));
                break;
            }
        }

        // Collision detection
        // For now we have a 1-square wall around the entire arena
        //se tocca il bordo il gioco finisce

        if (((newHead.x == 0 || newHead.x == mXTileCount-1 ) &&
                ( newHead.y >0 && newHead.y != (mYTileCount/2) )) ||
                ( (newHead.y == 0 || newHead.y == mYTileCount-1) &&
                        ( newHead.x >0 && newHead.x != (mXTileCount/2)))) {
            setMode(LOSE);
            return;
        }



        // Look for collisions with itself
        //se il serpente tocca un pixel della propria coda il gioco finisce
        int snakelength = mSnakeTrail.size();
        for (int snakeindex = 0; snakeindex < snakelength; snakeindex++) {
            Coordinate c = mSnakeTrail.get(snakeindex);
            if (c.equals(newHead)) {
                setMode(LOSE);
                return;
            }
        }

        // Look for apples
        int applecount = mAppleList.size();
        for (int appleindex = 0; appleindex < applecount; appleindex++) {
            Coordinate c = mAppleList.get(appleindex);
            if (c.equals(newHead)) {
                mAppleList.remove(c);
                addRandomApple();


                // for each apple eaten the score increases by 1
                mScore++;
                totalScore = mScore*100 ;
                //for each apple eaten the speed increases
                mMoveDelay *= 0.9;

                growSnake = true;
            }
        }



        // push a new head onto the ArrayList and pull off the tail
        mSnakeTrail.add(0, newHead);
        // except if we want the snake to grow
        if (!growSnake) {
            mSnakeTrail.remove(mSnakeTrail.size() - 1);
        }

        int index = 0;
        for (Coordinate c : mSnakeTrail) {
            if (index == 0) {
                //if the index is 0, draw the face of the snake as the first pixel
                setTile(PURPLE_STAR, c.x, c.y);
            } else {
                //from the second pixel draw the parts of the tail
                setTile(PURPLE_STAR, c.x, c.y);
            }
            index++;
        }

    }

    /**
     * Simple class containing two integer values and a comparison function. There's probably
     * something I should use instead, but this was quick and easy to build.
     */
    private class Coordinate {
        public int x;
        public int y;

        public Coordinate(int newX, int newY) {
            x = newX;
            y = newY;
        }

        public boolean equals(Coordinate other) {
            if (x == other.x && y == other.y) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Coordinate: [" + x + "," + y + "]";
        }
    }






}
