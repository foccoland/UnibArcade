package it.di.uniba.sms1920.teambarrella.unibarcade.spaceinvaders;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import it.di.uniba.sms1920.teambarrella.unibarcade.LoadingActivity;
import it.di.uniba.sms1920.teambarrella.unibarcade.R;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.Score;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeDBAdapter;
import it.di.uniba.sms1920.teambarrella.unibarcade.database.UnibArcadeFBHelper;

/**
 * This activity use SurfaceView to display everything on screen and implements Runnable so
 * we can create our thread which will be a game loop.
 */
public class SpaceInvadersEngine extends SurfaceView implements Runnable {

    Context context;
    private static final int maxNumColumn = 6;
    private static final int maxNumRow = 5;
    private static final int maxShelterNum = 4;
    private static final int maxShelterColumn = 7;
    private static final int maxShelterRow = 3;

    // This is our thread
    private Thread gameThread = null;

    /**
     * SurfaceHolder to lock the surface before we draw our graphics.
     * We need it when we use Paint and Canvas in a thread.
     */
    private SurfaceHolder ourHolder;

    /**
     * A boolean which we will set and unset when the game is running or not.
     * It means that the value "true/false" represented by the "playing" variable is not saved in
     * the cache but in the main compiler memory.
     * If playing is false, then not only the thread that sets it to false stops, but ALL threads
     * that are "listening" on the value of this variable
     */
    private volatile boolean playing;

    // When a game is paused by an event, variable paused is setting to true. Game start in paused.
    private boolean paused = true;
    private boolean afterLost = false;

    /**
     * Canvas: object that holds the "draw" calls.
     * Where does he draw? On Bitmap, which is a map of pixels on which drawing methods are called.
     * What do you draw with? With primitive Path, Rect, Point, etc...
     *
     * Paint is used to define colors and drawing styles.
     */
    private Canvas canvas;
    private Paint paint;

    // This variable tracks the game frame rate
    private long fps;

    // This is used to help calculate the fps. It traks real time in millisec since the game began
    private long timeThisFrame;

    // The size of the screen in pixels
    private int screenX;
    private int screenY;

    // The players ship
    private PlayerShip playerShip;

    // The player's bullet
    private Bullet bullet;

    // The invaders bullets
    private Bullet[] invadersBullets = new Bullet[200];
    private int nextBullet;
    private int maxInvaderBullets = 10;

    // Up to 60 invaders
    Invader[] invaders = new Invader[60];
    int numInvaders = 0;

    // The player's shelters are built from bricks
    private DefenceBrick[] bricks = new DefenceBrick[400];
    private int numBricks;

    // For sound FX
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;

    // The score
    int score = 0;

    // Lives
    private int lives = 3;

    //Rectangle for initial text
    Rect r;

    // How menacing (fast) should the sound be
    private long menaceInterval = 1000;
    // Which menace sound should play next
    private boolean uhOrOh;
    // When did we last play a menacing sound
    private long lastMenaceTime = System.currentTimeMillis();

    UnibArcadeFBHelper fbHelper;
    UnibArcadeDBAdapter dbAdapter;
    String uid, gameId;
    Score newScore;

    // When we initialize (call new()) on gameView, this constructor method runs
    public SpaceInvadersEngine(Context context, int x, int y) {

        // The next line of code asks the SurfaceView class to set up our object.
        super(context);

        // Make a globally available copy of the context so we can use it in another method
        this.context = context;

        fbHelper = new UnibArcadeFBHelper();
        dbAdapter = new UnibArcadeDBAdapter(context);

        uid = fbHelper.checkUserSession();
        gameId = LoadingActivity.getGameId();

        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        //Rect for text drawing
        r = new Rect();

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        try {
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }

        prepareLevel();
    }

    private void prepareLevel() {

        // Here we will initialize all the game objects

        // Make a new player space ship
        playerShip = new PlayerShip(context, screenX, screenY);

        // Prepare the player's bullet
        bullet = new Bullet(screenY);

        // Initialize the invadersBullets array
        for (int i = 0; i < invadersBullets.length; i++) {
            invadersBullets[i] = new Bullet(screenY);
        }

        // Build an army of invaders
        numInvaders = 0;
        for (int column = 0; column < maxNumColumn; column++) {
            for (int row = 0; row < maxNumRow; row++) {
                invaders[numInvaders] = new Invader(context, row, column, screenX, screenY);
                numInvaders++;
            }
        }

        // Reset the menace level
        menaceInterval = 1000;

        // Build the shelters
        numBricks = 0;
        for (int shelterNumber = 0; shelterNumber < maxShelterNum; shelterNumber++) {
            for (int column = 0; column < maxShelterColumn; column++) {
                for (int row = 0; row < maxShelterRow; row++) {
                    bricks[numBricks] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);
                    numBricks++;
                }
            }
        }
    }

    @Override
    public void run() {
        while (playing) {

            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            if (!paused) {
                update();
            }

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

            // Play a sound based on the menace level
            if (!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) {
                        // Play Uh
                        soundPool.play(uhID, 1, 1, 0, 0, 1);

                    } else {
                        // Play Oh
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    // Reset the last menace time
                    lastMenaceTime = System.currentTimeMillis();
                    // Alter value of uhOrOh
                    uhOrOh = !uhOrOh;
                }
            }
        }
    }

    private void update() {

        // Did an invader bump into the side of the screen
        boolean bumped = false;

        // Has the player lost
        boolean lost = false;

        // Move the player's ship
        playerShip.update(fps);

        // Update the invaders if visible
        for (int i = 0; i < numInvaders; i++) {

            if (invaders[i].getVisibility()) {
                // Move the next invader
                invaders[i].update(fps);

                // Does he want to take a shot?
                if (invaders[i].takeAim(playerShip.getX(),
                        playerShip.getLength())) {

                    // If so try and spawn a bullet
                    if (invadersBullets[nextBullet].shoot(invaders[i].getX()
                                    + invaders[i].getLength() / 2,
                            invaders[i].getY(), bullet.DOWN)) {

                        // Shot fired
                        // Prepare for the next shot
                        nextBullet++;

                        // Loop back to the first one if we have reached the last
                        if (nextBullet == maxInvaderBullets) {
                            // This stops the firing of another bullet until one completes its journey
                            // Because if bullet 0 is still active shoot returns false.
                            nextBullet = 0;
                        }
                    }
                }

                // If that move caused them to bump the screen change bumped to true
                if (invaders[i].getX() > screenX - invaders[i].getLength()
                        || invaders[i].getX() < 0) {

                    bumped = true;

                }
            }

        }

        // Update the players bullet
        if (bullet.getStatus()) {
            bullet.update(fps);
        }

        // Update all the invaders bullets if active
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps);
            }
        }

        // Did an invader bump into the edge of the screen
        if (bumped) {

            // Move all the invaders down and change direction
            for (int i = 0; i < numInvaders; i++) {
                invaders[i].dropDownAndReverse();
                // Have the invaders landed
                if (invaders[i].getY() > screenY - screenY / 10) {
                    saveScore();
                    lost = true;
                }
            }

            // Increase the menace level
            // By making the sounds more frequent
            menaceInterval = menaceInterval - 80;
        }

        if (lost) {
            afterLost = true;
            prepareLevel();
        }

        // Has the player's bullet hit the top of the screen
        if (bullet.getImpactPointY() < 0) {
            bullet.setInactive();
        }

        // Has an invaders bullet hit the bottom of the screen
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getImpactPointY() > screenY) {
                invadersBullets[i].setInactive();
            }
        }

        // Has the player's bullet hit an invader
        if (bullet.getStatus()) {
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) {
                        invaders[i].setInvisible();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        bullet.setInactive();
                        score = score + 100;

                        // Has the player won
                        if (score == numInvaders * 100) {
                            paused = true;
                            saveScore();
                            prepareLevel();
                        }
                    }
                }
            }
        }

        // Has an alien bullet hit a shelter brick
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                for (int j = 0; j < numBricks; j++) {
                    if (bricks[j].getVisibility()) {
                        if (RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())) {
                            // A collision has occurred
                            invadersBullets[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }

        }

        // Has a player bullet hit a shelter brick
        if (bullet.getStatus()) {
            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].getVisibility()) {
                    if (RectF.intersects(bullet.getRect(), bricks[i].getRect())) {
                        // A collision has occurred
                        bullet.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }

        // Has an invader bullet hit the player ship
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                if (RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())) {
                    invadersBullets[i].setInactive();
                    lives--;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    // Is it game over?
                    if (lives == 0) {
                        paused = true;
                        afterLost = true;
                        saveScore();
                        prepareLevel();
                    }
                }
            }
        }
    }

    private void saveScore() {
        newScore = new Score(uid, gameId, Integer.toString(score));
        fbHelper.updateScore(newScore);
    }

    @TargetApi(26)
    private void draw() {

        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {

            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

            if(!paused) {
                // Draw the background color
                canvas.drawColor(getResources().getColor(R.color.colorPrimarySpaceInvaders, null));
                //canvas.drawColor(Color.argb(255, 26, 128, 182));

                // Choose the brush color for drawing
                paint.setColor(getResources().getColor(R.color.colorAccentSpaceInvaders, null));

                // Draw the player spaceship
                canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - playerShip.getHeight(), paint);

                // Draw the invaders
                for (int i = 0; i < numInvaders; i++) {
                    if (invaders[i].getVisibility()) {
                        if (uhOrOh) {
                            canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                        } else {
                            canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                        }
                    }
                }

                // Draw the bricks if visible
                for (int i = 0; i < numBricks; i++) {
                    if (bricks[i].getVisibility()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                // Draw the players bullet if active
                if (bullet.getStatus()) {
                    canvas.drawRect(bullet.getRect(), paint);
                }

                // Draw the invaders bullets
                for (int i = 0; i < invadersBullets.length; i++) {
                    if (invadersBullets[i].getStatus()) {
                        canvas.drawRect(invadersBullets[i].getRect(), paint);
                    }
                }

                // Draw the score and remaining lives
                // Change the brush color
                paint.setColor(getResources().getColor(R.color.colorSecondarySpaceInvaders, null));
                paint.setTextSize(70);
                Typeface tf = getResources().getFont(R.font.zrnic);
                paint.setTypeface(tf);
                canvas.drawText("Score: " + score, screenX - 380, 90, paint);
                canvas.drawText("Lives: " + lives, screenX- 380, 180, paint);

            } else {

                if(!afterLost) {

                    canvas.drawColor(getResources().getColor(R.color.colorPrimarySpaceInvaders, null));

                    paint.setColor(getResources().getColor(R.color.colorSecondarySpaceInvaders, null));

                    paint.setTextSize(150);
                    Typeface tf = getResources().getFont(R.font.zrnic);
                    paint.setTypeface(tf);

                    drawCenter(canvas, paint, "Welcome to Space Invaders!");
                    paint.setTextSize(100);
                    drawCenterSecond(canvas, paint, "Touch screen to start!");

                } else {

                    canvas.drawColor(getResources().getColor(R.color.colorPrimarySpaceInvaders, null));

                    paint.setColor(getResources().getColor(R.color.colorSecondarySpaceInvaders, null));

                    paint.setTextSize(150);
                    Typeface tf = getResources().getFont(R.font.zrnic);
                    paint.setTypeface(tf);

                    drawCenter(canvas, paint, "You Lose!");
                    paint.setTextSize(100);
                    drawCenterSecond(canvas, paint, "Score: " + score);
                    drawCenterThird(canvas, paint, "Touch screen to restart!");
                }
            }

            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawCenter(Canvas canvas, Paint paint, String text) {
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom - 200;
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
        float y = cHeight / 2f + r.height() / 2f - r.bottom + 150;
        canvas.drawText(text, x, y, paint);
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:

                if (!afterLost) {
                    paused = false;
                } else {
                    paused = false;
                    afterLost = false;
                    score = 0;
                    lives = 3;
                }

                if (motionEvent.getY() > screenY - screenY / 6) {
                    if (motionEvent.getX() > screenX / 2) {
                        playerShip.setMovementState(playerShip.RIGHT);
                    } else {
                        playerShip.setMovementState(playerShip.LEFT);
                    }

                }

                if (motionEvent.getY() < screenY - screenY / 8) {
                    // Shots fired
                    if (bullet.shoot(playerShip.getX() +
                            playerShip.getLength() / 2, screenY, bullet.UP)) {
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                }
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:

                if (motionEvent.getY() > screenY - screenY / 10) {
                    playerShip.setMovementState(playerShip.STOPPED);
                }

                break;
        }
        return true;
    }

    // If SpaceInvadersActivity is paused/stopped
    // shutdown our thread.
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    // If SpaceInvadersActivity is started then
    // start our thread.
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}
