// CannonView.java
// Displays the Cannon Game
package it.di.uniba.sms1920.teambarrella.unibarcade.cannonball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import java.util.HashMap;
import java.util.Map;


import it.di.uniba.sms1920.teambarrella.unibarcade.LoadingActivity;
import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.Score;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeDBAdapter;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeFBHelper;


@RequiresApi(api = Build.VERSION_CODES.O)
public class CannonView extends SurfaceView implements
        SurfaceHolder.Callback {

    /*
     * Game Constants
     */
    public static final int TARGET_PIECES = 8;
    public static final int MISS_PENALTY = 1;
    public static final int HIT_REWARD = 1;
    private static final int TARGET_SOUND_ID = 0;
    private static final int CANNON_SOUND_ID = 1;
    private static final int BLOCKER_SOUND_ID = 2;
    public static final int STOP = 0;
    public static final int FIRST = 2;
    public static final int RUN = 1;


    /*
     * Instance of Firebase openHelper to send and receive data
     */
    private UnibArcadeFBHelper fbHelper;

    /*
     * Instance of cannonThread object. It controls the game loop
     */
    private CannonThread cannonThread; // controls the game loop


    /*
     * Game variables
     */
    private boolean gameOver; // is the game over?
    private int gameMode; //game mode variable
    private double timeLeft; // the amount of time left in seconds
    private int shotsFired; // the number of shots the user has fired
    private static int totalScore = 0; //used to hold the score


    /*
     * Declaration of game contents variables
     */
    private Line blocker; // start and end points of the blocker
    private int blockerDistance; // blocker distance from left
    private int blockerBeginning; // blocker distance from top
    private int blockerEnd; // blocker bottom edge distance from top
    private int initialBlockerVelocity; // initial blocker speed multiplier
    private float blockerVelocity; // blocker speed multiplier during game
    private Line target; // start and end points of the target
    private int targetDistance; // target distance from left
    private int targetBeginning; // target distance from top
    private double pieceLength; // length of a target piece
    private int targetEnd; // target bottom's distance from top
    private int initialTargetVelocity; // initial target speed multiplier
    private float targetVelocity; // target speed multiplier during game
    private int lineWidth; // width of the target and blocker
    private boolean[] hitStates; // is each target piece hit?
    private int targetPiecesHit; // number of target pieces hit (out of 8)
    private Point cannonball; // cannonball image's upper-left corner
    private int cannonballVelocityX; // cannonball's x velocity
    private int cannonballVelocityY; // cannonball's y velocity
    private boolean cannonballOnScreen; // is the cannonball on the screen
    private int cannonballRadius; // cannonball radius
    private int cannonballSpeed; // cannonball speed
    private int cannonBaseRadius; // cannon base radius
    private int cannonLength; // cannon barrel length
    private Point barrelEnd; // the endpoint of the cannon's barrel
    private int screenWidth; // width of the screen
    private int screenHeight; // height of the screen
    private SoundPool soundPool; // plays sound effects
    private Map<Integer, Integer> soundMap; // maps IDs to SoundPool
    Rect r; //Rect for first,win,lose texts


    /*
     * Paint game variables
     */
    private Paint textPaint; // Paint used to draw text
    private Paint cannonballPaint; // Paint used to draw the cannonball
    private Paint cannonPaint; // Paint used to draw the cannon
    private Paint blockerPaint; // Paint used to draw the blocker
    private Paint targetPaint; // Paint used to draw the target
    private Paint backgroundPaint; // Paint used to clear the drawing area
    Typeface cannonTypeface = getResources().getFont(R.font.zrnic);


    /*
     * Public constructor of CannonView
     */
    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register SurfaceHolder.Callback listener
        getHolder().addCallback(this);

        //Rect for drawing text
        r = new Rect();

        /*
         * Variables initialization
         */
        blocker = new Line();
        target = new Line();
        cannonball = new Point();

        /*
         * Initialize hitStates as a boolean array. Size is the same of target pieces
         */
        hitStates = new boolean[TARGET_PIECES];

        /*
         * Initialize SoundPool to play the app's three sound effects
         */
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .build();

        /*
         * Create a Map of sounds and pre-load sounds
         */
        soundMap = new HashMap<Integer, Integer>();
        soundMap.put(TARGET_SOUND_ID,
                soundPool.load(context, R.raw.cannon_target_hit, 1));
        soundMap.put(CANNON_SOUND_ID,
                soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID,
                soundPool.load(context, R.raw.cannon_blocker_hit, 1));

        /*
         * Construct paints game elemnts
         */
        textPaint = new Paint();
        cannonPaint = new Paint();
        cannonballPaint = new Paint();
        blockerPaint = new Paint();
        targetPaint = new Paint();
        backgroundPaint = new Paint();
    }

    /*
     * Called by surfaceChanged when the size of the SurfaceView changes,
     * such as when it's first added to the View hierarchy
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        /*
         * Next lines sets the game contents among the screen display
         */
        screenWidth = w;
        screenHeight = h;
        cannonBaseRadius = h / 18;
        cannonLength = w / 7;
        cannonballRadius = w / 36;
        cannonballSpeed = w * 3 / 2;
        lineWidth = w / 24;
        blockerDistance = w * 5 / 8;
        blockerBeginning = h / 8;
        blockerEnd = h * 3 / 8;
        initialBlockerVelocity = h / 2;
        blocker.start = new Point(blockerDistance, blockerBeginning);
        blocker.end = new Point(blockerDistance, blockerEnd);
        targetDistance = w * 7 / 8;
        targetBeginning = h / 8;
        targetEnd = h * 7 / 8;
        pieceLength = (targetEnd - targetBeginning) / TARGET_PIECES;
        initialTargetVelocity = -h / 4;
        target.start = new Point(targetDistance, targetBeginning);
        target.end = new Point(targetDistance, targetEnd);
        barrelEnd = new Point(cannonLength, h / 2);

        /*
         * Graphics size and font
         */
        textPaint.setTextSize(60);
        textPaint.setAntiAlias(true);
        textPaint.setColor(getResources().getColor(R.color.colorPrimaryDarkCannonball, null));
        textPaint.setTypeface(cannonTypeface);
        cannonPaint.setStrokeWidth(lineWidth * 1.5f);
        cannonPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryCannonball));
        cannonballPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryCannonball));
        blockerPaint.setStrokeWidth(lineWidth);
        blockerPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryCannonball));
        targetPaint.setStrokeWidth(lineWidth);
        backgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccentCannonball));

        /*
         * All is ready, set game mode to first-time play
         */

        gameMode = FIRST;
    }

    /*
     * Method to start a fresh new game
     */
    public void newGame() {

        /*
         * Reset game elements
         */
        gameMode = RUN;
        for (int i = 0; i < TARGET_PIECES; ++i)
            hitStates[i] = false;
        targetPiecesHit = 0;
        blockerVelocity = initialBlockerVelocity;
        targetVelocity = initialTargetVelocity;
        timeLeft = 10;
        cannonballOnScreen = false;
        shotsFired = 0;
        blocker.start.set(blockerDistance, blockerBeginning);
        blocker.end.set(blockerDistance, blockerEnd);
        target.start.set(targetDistance, targetBeginning);
        target.end.set(targetDistance, targetEnd);

        /*
         * Game state. If the game is over, starts cannonThread
         */
        if (gameOver == true) {
            gameOver = false;
            cannonThread = new CannonThread(getHolder());
            cannonThread.start();
        }
    }


    /*
     * Actions after winning
     */
    public void youWin() {

        cannonThread.setRunning(false);
        fbHelper = new UnibArcadeFBHelper();
        String uid = fbHelper.checkUserSession();

        Score score = new Score(uid, LoadingActivity.getGameId(),
                Integer.toString(totalScore));

        fbHelper.updateScore(score);
        gameOver = true;

    }

    /*
     *Actions after lose
     */
    public void youLose() {

        cannonThread.setRunning(false);
        timeLeft = 0.0;
        fbHelper = new UnibArcadeFBHelper();
        String uid = fbHelper.checkUserSession();

        Score score = new Score(uid, LoadingActivity.getGameId(),
                Integer.toString(totalScore));

        fbHelper.updateScore(score);
        gameOver = true;


    }

    /*
     * Called repeatedly by the CannonThread to update game elements
     */
    public void updatePositions(double elapsedTimeMS) {


        double interval = elapsedTimeMS / 1000.0;

        if (cannonballOnScreen) {

            cannonball.x += interval * cannonballVelocityX;
            cannonball.y += interval * cannonballVelocityY;

            /*
             * Check blocker collision. If you touch it, you get a time penalty
             */
            if (cannonball.x + cannonballRadius > blockerDistance &&
                    cannonball.x - cannonballRadius < blockerDistance &&
                    cannonball.y + cannonballRadius > blocker.start.y &&
                    cannonball.y - cannonballRadius < blocker.end.y) {
                cannonballVelocityX *= -1;
                timeLeft -= MISS_PENALTY;

                //Related sound
                soundPool.play(soundMap.get(BLOCKER_SOUND_ID), 1, 1, 1, 0, 1f);
            }

            // Check for collisions with left and right walls
            else if (cannonball.x + cannonballRadius > screenWidth ||
                    cannonball.x - cannonballRadius < 0)
                cannonballOnScreen = false;

                // Check for collisions with top and bottom walls
            else if (cannonball.y + cannonballRadius > screenHeight ||
                    cannonball.y - cannonballRadius < 0)
                cannonballOnScreen = false; // make the cannonball disappear

                // Check for cannonball collision with target
            else if (cannonball.x + cannonballRadius > targetDistance &&
                    cannonball.x - cannonballRadius < targetDistance &&
                    cannonball.y + cannonballRadius > target.start.y &&
                    cannonball.y - cannonballRadius < target.end.y) {
                // Determine target section number (0 is the top)
                int section = (int) ((cannonball.y - target.start.y) / pieceLength);

                /*
                 * When a target is hit actions. If you hit a target block you get a time bonus
                 */
                if ((section >= 0 && section < TARGET_PIECES) &&
                        !hitStates[section]) {
                    hitStates[section] = true;
                    cannonballOnScreen = false;
                    targetPiecesHit++;
                    timeLeft += HIT_REWARD;
                    totalScore = ((targetPiecesHit * 100));

                    //Related sound
                    soundPool.play(soundMap.get(TARGET_SOUND_ID), 1,
                            1, 1, 0, 1f);


                }
            }
        }

        /*
         * Update blocker position
         */
        double blockerUpdate = interval * blockerVelocity;
        blocker.start.y += blockerUpdate;
        blocker.end.y += blockerUpdate;

        /*
         * Update target position
         */
        double targetUpdate = interval * targetVelocity;
        target.start.y += targetUpdate;
        target.end.y += targetUpdate;

        // if the blocker hit the top or bottom, reverse direction
        if (blocker.start.y < 0 || blocker.end.y > screenHeight)
            blockerVelocity *= -1;

        // if the target hit the top or bottom, reverse direction
        if (target.start.y < 0 || target.end.y > screenHeight)
            targetVelocity *= -1;

        timeLeft -= interval; // subtract from time left

    }


    /*
     * Shoot a cannonball method. Is called in CannonApp
     */
    public void fireCannonball(MotionEvent event) {
        if (cannonballOnScreen)
            return;

        double angle = alignCannon(event);

        // Move the cannonball to be inside the cannon
        cannonball.x = cannonballRadius;
        cannonball.y = screenHeight / 2;

        // Get the x component of the total velocity
        cannonballVelocityX = (int) (cannonballSpeed * Math.sin(angle));

        // Get the y component of the total velocity
        cannonballVelocityY = (int) (-cannonballSpeed * Math.cos(angle));
        cannonballOnScreen = true; // the cannonball is on the screen
        ++shotsFired; // increment shotsFired

        // Related sound
        soundPool.play(soundMap.get(CANNON_SOUND_ID), 1, 1, 1, 0, 1f);
    }


    /*
     * Aligns the cannon in response to a user touch
     */
    public double alignCannon(MotionEvent event) {

        Point touchPoint = new Point((int) event.getX(), (int) event.getY());

        // compute the touch's distance from center of the screen
        // on the y-axis
        double centerMinusY = (screenHeight / 2 - touchPoint.y);

        double angle = 0; // initialize angle to 0

        // calculate the angle the barrel makes with the horizontal
        if (centerMinusY != 0) // prevent division by 0
            angle = Math.atan((double) touchPoint.x / centerMinusY);

        // if the touch is on the lower half of the screen
        if (touchPoint.y > screenHeight / 2)
            angle += Math.PI; // adjust the angle

        // calculate the endpoint of the cannon barrel
        barrelEnd.x = (int) (cannonLength * Math.sin(angle));
        barrelEnd.y = (int) (-cannonLength * Math.cos(angle) + screenHeight / 2);

        return angle; // return the computed angle
    }

    private void drawCenter(Canvas canvas, Paint paint, String text) {
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom - 300;
        canvas.drawText(text, x, y, paint);
    }

    private void drawCenterSecond(Canvas canvas, Paint paint, String text) {
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        canvas.drawText(text, x, y, paint);
    }

    private void drawCenterThird(Canvas canvas, Paint paint, String text) {
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom + 100;
        canvas.drawText(text, x, y, paint);
    }


    /*
     * Draws the game elements. Contains the draws of win and lose condition
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void drawGameElements(Canvas canvas) {

        if (gameMode == FIRST) {

            //Clear the background
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

            //Display welcome text
            textPaint.setTextSize(100);
            drawCenter(canvas, textPaint, getResources().getString(R.string.first_game));
            textPaint.setTextSize(60);
            drawCenterSecond(canvas, textPaint, getResources().getString(R.string.first_game_subtitle));

        } else {

            // Clear the background
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

            // Display current game stats
            canvas.drawText(getResources().getString(
                    R.string.time_remaining_format, timeLeft), 30, 75, textPaint);


            // if a cannonball is currently on the screen, draw it
            if (cannonballOnScreen)
                canvas.drawCircle(cannonball.x, cannonball.y, cannonballRadius, cannonballPaint);

            // draw the cannon barrel
            canvas.drawLine(0, screenHeight / 2, barrelEnd.x, barrelEnd.y, cannonPaint);

            // draw the cannon base
            canvas.drawCircle(0, (int) screenHeight / 2, (int) cannonBaseRadius, cannonPaint);

            // draw the blocker
            canvas.drawLine(blocker.start.x, blocker.start.y, blocker.end.x, blocker.end.y, blockerPaint);

            Point currentPoint = new Point();

            // initialize curPoint to the starting point of the target
            currentPoint.x = target.start.x;
            currentPoint.y = target.start.y;


            // Draws the targets
            for (int i = 1; i <= TARGET_PIECES; ++i) {
                // if this target piece is not hit, draw it
                if (!hitStates[i - 1]) {
                    // alternate coloring of the pieces
                    if (i % 2 == 0)
                        targetPaint.setColor(Color.WHITE);
                    else
                        targetPaint.setColor(ContextCompat.getColor(getContext(), R.color.DarkSlateGrey));

                    canvas.drawLine(currentPoint.x, currentPoint.y, target.end.x,
                            (int) (currentPoint.y + pieceLength), targetPaint);
                }

                // move curPoint to the start of the next piece
                currentPoint.y += pieceLength;
            }


            /*
             * Win condition
             */
            if (targetPiecesHit >= TARGET_PIECES) {

                youWin();

                //Clear the background
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

                //Draw victory texts
                textPaint.setTextSize(100);
                drawCenter(canvas, textPaint, getResources().getString(R.string.win));
                textPaint.setTextSize(60);
                drawCenterSecond(canvas, textPaint, getResources().getString(R.string.reset_game));
                drawCenterThird(canvas, textPaint, getResources().getString(R.string.results_format, shotsFired, totalScore));


                resetScore();
                gameMode = STOP;

                /*
                 * Lose condition
                 */
            } else if (timeLeft <= 0.0) {

                youLose();


                //Clear the background
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

                //Draw lose texts
                textPaint.setTextSize(100);
                drawCenter(canvas, textPaint, getResources().getString(R.string.lose));
                textPaint.setTextSize(60);
                drawCenterSecond(canvas, textPaint, getResources().getString(R.string.reset_game));
                drawCenterThird(canvas, textPaint, getResources().getString(R.string.results_format, shotsFired, totalScore));

                resetScore();
                gameMode = STOP;
            }
        }

    }


    /*
     * Method to stop the game. Called in CannonApp
     */
    public void stopGame() {

        if (cannonThread != null)
            cannonThread.setRunning(false);
    }

    /*
     * Method to check if game is over. Called in CannonApp
     */
    public int gameStatus() {

        return gameMode;
    }

    public void resetScore() {

        totalScore = 0;
    }

    /*
     * Method to set the game mode with gameMode variable. Called in CannonApp
     */
    public void setGameMode(int mode) {

        gameMode = mode;
    }


    public void releaseResources() {

        soundPool.release();
        soundPool = null;
    }

    /*
     * Called when surface changes size
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /*
     * Called when surface is firts created
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        cannonThread = new CannonThread(holder);
        cannonThread.setRunning(true);
        cannonThread.start();

    }


    /*
     * Called when the surface is destroyed
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        boolean retry = true;
        cannonThread.setRunning(false);

        while (retry) {
            try {
                cannonThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }

    }

    /*
     * Thread subclass to control the game loop
     */
    public class CannonThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean threadIsRunning = true;

        /*
         * Controls game loop
         */
        @Override
        public void run() {
            Canvas canvas = null; // used for drawing
            long previousFrameTime = System.currentTimeMillis();


            while (threadIsRunning) {
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    // lock the surfaceHolder for drawing
                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        updatePositions(elapsedTimeMS); // update game state
                        drawGameElements(canvas); // draw game elements continuously
                        previousFrameTime = currentTime; // update previous time
                    }
                } finally {
                    if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        // Initializes the surface holder
        public CannonThread(SurfaceHolder holder) {
            surfaceHolder = holder;
            setName("CannonThread");
        }

        // Sets thread running
        public void setRunning(boolean running) {
            threadIsRunning = running;
        }
    }
}

