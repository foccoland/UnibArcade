package it.di.uniba.sms1920.teambarrella.unibarcade.arkanoid;

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
 * Copyright: Giannelli Fabio, 672367
 */

/**
 * Disclaimer:
 * e' stato fatto uso di commenti sia in inglese sia in italiano.
 * Questi ultimi hanno esclusiva funzione descrittiva per i membri del gruppo.
 * Adottare una sola lingua sarebbe stato ovviamente preferibile.
 * Mi scuso per il disagio arrecato.
 */

public class ArkanoidEngine extends SurfaceView implements Runnable {

    UnibArcadeFBHelper fbHelper;
    UnibArcadeDBAdapter dbAdapter;
    String uid, gameId;
    Score newScore;

    Context context;

    //This is our thread
    private Thread gameThread = null;

    //We need a SurfaceHolder when we use Paint and Canvas in a thread.
    //We will see it in action in the draw method.
    /**
     * SurfaceHolder è un'interfaccia che rappresenta un oggetto
     * che "regge" la superficie del display. Ti permette di controllare
     * le dimensioni e il formato del display, editarne i pixel e moni-
     * torare i cambiamenti sulla superficie.
     */
    private SurfaceHolder ourHolder;

    //A boolean which tells if the game is running or not
    /**
     * Che significa "volatile"? Significa che il valore "true/false"
     * rappresentato dalla variabile "playing" non viene salvato nella cache
     * ma nella memoria principale del compilatore.
     * Se playing è false, quindi, non si ferma solo il thread che lo imposta
     * a false, ma TUTTI i thread che sono in "listening" sul valore di questa
     * variabile.
     */
    private volatile boolean playing;

    //Game is paused at the start
    /**
     * Quando il gioco è messo in pausa da un evento, questa variabile
     * viene impostata a true. Il gioco parte in pausa.
     */
    private boolean paused = true;
    private boolean afterMatch = false;

    //A Canvas and a Paint object
    /**
     * Canvas: oggetto che detiene le chiamate "draw" per disegnare.
     * Dove disegna? Su Bitmap, che è una mappa di pixel sulla quale
     * vengono chiamati i metodi di disegno. Con cosa si disegna? Con
     * le primitive Path, Rect, Point, ecc..
     * Paint serve per definire i colori e gli stili di disegno
     */
    private Canvas canvas;
    private Paint paint;

    //Height and width of the screen
    /**
     * valori cartesiani per altezza e larghezza schermo
     */
    private int screenX, screenY;

    //This tracks game frame rate
    /**
     * Frame Per Second
     */
    private long fps;

    //Number of invisible bricks
    int invisibleBricks;

    float speed = 60;

    //This is used to help calculate the fps
    /**
     * Tiene traccia del tempo reale in millisecondi
     * dall'inizio del gioco del frame attualmente eseguito
     */
    private long timeThisFrame;

    // La barra
    Bat bat;

    //La palla
    Ball ball;

    //Rettangolo per le scritte
    Rect r;

    //I mattoncini
    Brick[] bricks = new Brick[200];
    int numBricks = 0;

    //Effetti sonori
    SoundPool soundPool;
    int beep1ID = -1;
    int beep2ID = -1;
    int beep3ID = -1;
    int loseLifeID = -1;
    int explodeID = -1;

    //Punteggio
    int score = 0;

    //Vite
    int lives = 3;

    private boolean win = false;

    public ArkanoidEngine(Context context, int x, int y) {

        super(context);

        this.context = context;

        fbHelper = new UnibArcadeFBHelper();
        dbAdapter = new UnibArcadeDBAdapter(context);

        uid = fbHelper.checkUserSession();
        gameId = LoadingActivity.getGameId();

        r = new Rect();

        //Initialize our SurfaceHolder
        ourHolder = getHolder();
        paint = new Paint();

        //Initialize screenX and screenY
        screenX = x;
        screenY = y;

        /**
         * Il miglior posto per inizializzare la barra e la
         * palla è nel costruttore dell'engine di gioco
         */
        bat = new Bat(screenX, screenY);
        ball = new Ball();

        //Caricamento dei suoni

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        try {
            //Creazione degli oggetti necessari ai controlli
            //per il caricamento
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            //Caricamento dei suoni in memoria
            descriptor = assetManager.openFd("beep1.ogg");
            beep1ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep2.ogg");
            beep2ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep3.ogg");
            beep3ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("loseLife.ogg");
            loseLifeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("explode.ogg");
            explodeID = soundPool.load(descriptor, 0);

        } catch(IOException e) {
            Log.e("error", "failed to load sound files");
        }

        restart();

    }

    //Runs when the OS calls onPause on ArkanoidActivity method
    public void pause() {
        /**
         * Fa riferimento al boolean volatile. Qualunque thread che fa uso
         * di questa variabile per fermarsi si fermerà
         */
        playing = false;

        try {
            //Attende che il thread muoia
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    //Runs when the OS calls onResume on ArkanoidActivity method
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {

        while (playing) {

            //Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            //Update the frame
            if (!paused) {
                update();
            }

            // Draw the frame
            draw();

            /**
             * Tempo in millisecs dallo start del thread e del gioco
             */
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

        }

    }

    //Si occupa di chiamare gli update di ogni oggetto
    //della UI
    private void update() {

        if (speed > 25)
            speed = speed - 0.01f;

        Log.d("ArkanoidEngine: ", "Speed (fps aumentati): " + speed);
        Log.d("ArkanoidEngine: ", "FPS: " + fps);
        //Aggiorna la posizione della barra
        bat.update(speed);
        //e della palla
        ball.update(speed);

        //Controlliamo se la palla collide con un mattoncino
        for (int i = 0; i < numBricks; i++) {
            if(bricks[i].getVisibility()) {

                if(RectF.intersects(bricks[i].getRect(), ball.getRect())) {

                    bricks[i].setInvisible();
                    invisibleBricks++;
                    ball.reverseYVelocity();
                    score = score + 100;
                    soundPool.play(explodeID, 1, 1, 0, 0, 1);

                    if (score == numBricks * 100) {
                        afterMatch = true;
                        paused = true;
                        saveScore();
                        win();
                        restart();
                    }
                }
            }
        }

        if (invisibleBricks == numBricks-1) {

        }

        //Controlliamo se la palla collide con la barra
        if(RectF.intersects(bat.getRect(), ball.getRect())) {
            //alla collisione il calcolo della direzione
            //sull'asse delle x è randomico
            ball.setRandomXVelocity();
            //la direzione della pallina viene invertita
            ball.reverseYVelocity();
            ball.clearObstacleY(bat.getRect().top - 2);
            soundPool.play(beep1ID, 1, 1, 0, 0, 1);

        }

        //Controlliamo se la pallina colpisce la parte superiore dello
        //schermo
        if(ball.getRect().top < 0) {
            ball.reverseYVelocity();
            ball.clearObstacleY(12);
            soundPool.play(beep2ID, 1, 1, 0, 0, 1);
        }

        //Controlliamo se la pallina colpisce la parte sinistra dello
        //schermo
        if(ball.getRect().left < 0) {
            ball.reverseXVelocity();
            ball.clearObstacleX(2);
            soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }

        //Controlliamo se la pallina colpisce la parte destra dello
        //schermo
        if(ball.getRect().right > screenX - 10) {
            ball.reverseXVelocity();
            ball.clearObstacleX(screenX - 22);
            soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }

        //Controlliamo lo stato della pallina quando raggiunge la parte
        //bassa dello schermo, perdendo quindi una vita
        if(ball.getRect().bottom > screenY) {

            //La pallina rimbalza...
            ball.reverseYVelocity();
            ball.clearObstacleY(screenY - 2);

            //...perdiamo una vita...
            lives --;
            soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

            if (lives == 0) {
                speed = 60;
                paused = true;
                afterMatch = true;
                saveScore();
                restart();
            }
        }
    }

    private void win() {
        win = true;
        ball.reverseYVelocity();
    }

    private void saveScore() {
        newScore = new Score(uid, gameId, Integer.toString(score));
        fbHelper.updateScore(newScore);
        //dbAdapter.insertScoreData(newScore);
    }

    private void restart() {
        //Il numero di mattoncini invisibili torna a 0
        invisibleBricks = 0;
        //reimposta la pallina nel punto di partenza
        ball.reset(screenX, screenY);

        int brickWidth = screenX / 8;
        int brickHeight = screenY / 10;

        //Costruisci muro di mattoncini
        for (int column = 0; column < 8; column++) {
            for (int row = 0; row < 3; row++) {
                bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                numBricks++;
            }
        }
    }

    @TargetApi(26)
    private void draw() {
        /**
         * Il surfaceHolder ci restituisce con getSurface() la SurfaceView
         * passata dal WindowManager di sistema, e il codice all'interno, relativo
         * al disegno dell'interfaccia, verra' eseguito solo se la SurfaceView
         * ottenuta è valida o meno
         */
        if (ourHolder.getSurface().isValid()) {

            //Lock the canvas for drawing
            canvas = ourHolder.lockCanvas();

            if (!paused) {

                //Draw the background color
                canvas.drawColor(getResources().getColor(R.color.colorSecondaryArkanoid, null));

                //Draw everything to the screen
                /**
                 * Disegniamo la barra
                 */
                canvas.drawRect(bat.getRect(), paint);

                //Scelta del colore di disegno della palla
                paint.setColor(getResources().getColor(R.color.colorAccentArkanoid, null));

                //Disegniamo la palla
                canvas.drawRect(ball.getRect(), paint);

                //Scelta del colore di disegno dei mattoncini
                paint.setColor(getResources().getColor(R.color.colorAccentArkanoid, null));

                //Disegniamo i mattoncini, se visibili
                for(int i = 0; i < numBricks; i++) {
                    if(bricks[i].getVisibility()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                /**
                 * Disegniamo l'HUD
                 */
                //Scegliamo il color per l'HUD
                paint.setColor(getResources().getColor(R.color.colorPrimaryArkanoid, null));

                //Disegniamo lo score

                paint.setTextSize(70);
                Typeface tf = getResources().getFont(R.font.zrnic);
                paint.setTypeface(tf);
                canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 80, paint);
                //Show everything we have drawn
                /**
                 * Dopo aver definito i dettagli di disegno, unlockCanvasAndPost
                 * disegnera' quanto effettivamente deciso nel codice precedente
                 * a quest'istruzione
                 */
            } else {

                if(!afterMatch) {

                    canvas.drawColor(getResources().getColor(R.color.colorSecondaryArkanoid, null));

                    paint.setColor(getResources().getColor(R.color.colorPrimaryArkanoid, null));

                    paint.setTextSize(150);
                    Typeface tf = getResources().getFont(R.font.zrnic);
                    paint.setTypeface(tf);
//                    int xPos = (canvas.getWidth() / 2);
//                    int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;
//                    canvas.drawText("Start!" , xPos, yPos, paint);

                    drawCenter(canvas, paint, "Welcome to Arkanoid!");
                    paint.setTextSize(100);
                    drawCenterSecond(canvas, paint, "Touch screen to start!");

                } else {

                    //Draw the background color
                    canvas.drawColor(getResources().getColor(R.color.colorSecondaryArkanoid, null));

                    //Draw everything to the screen
                    /**
                     * Disegniamo la barra
                     */
                    canvas.drawRect(bat.getRect(), paint);

                    //Scelta del colore di disegno della palla
                    paint.setColor(getResources().getColor(R.color.colorAccentArkanoid, null));

                    //Disegniamo la palla
                    canvas.drawRect(ball.getRect(), paint);

                    //Scelta del colore di disegno dei mattoncini
                    paint.setColor(getResources().getColor(R.color.colorAccentArkanoid, null));

                    /**
                     * Disegniamo l'HUD
                     */
                    //Scegliamo il color per l'HUD
                    paint.setColor(getResources().getColor(R.color.colorPrimaryArkanoid, null));

                    //Disegniamo lo score
                    paint.setTextSize(150);
                    Typeface tf = getResources().getFont(R.font.zrnic);
                    paint.setTypeface(tf);

                    if(!win) {

                        drawCenter(canvas, paint, "You Lose!");
                        drawCenterSecond(canvas, paint, "Score: " + score + "Touch to play again!");

                    } else {

                        drawCenter(canvas, paint, "You Win!");
                        drawCenterSecond(canvas, paint, "Score: " + score + "Touch to play again!");

                    }


                    //Show everything we have drawn
                    /**
                     * Dopo aver definito i dettagli di disegno, unlockCanvasAndPost
                     * disegnera' quanto effettivamente deciso nel codice precedente
                     * a quest'istruzione
                     */
                }
            }

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
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        canvas.drawText(text, x, y, paint);
    }

    private void drawCenterSecond(Canvas canvas, Paint paint, String text) {
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom + 250;
        canvas.drawText(text, x, y, paint);
    }

    /**
     * La SurfaceView implementa il metodo di callback onTouchListener.
     * Facendone override del metodo della superclasse possiamo gestire
     * il rilevamento dei tocchi sullo schermo.
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            //Il giocatore ha toccato lo schermo (e lo tiene premuto)
            case MotionEvent.ACTION_DOWN:

                if (!afterMatch) {
                    paused = false;
                } else {
                    paused = false;
                    afterMatch = false;
                    score = 0;
                    lives = 3;
                }

                if(event.getX() > screenX / 2)
                    bat.setMovementState(bat.RIGHT);
                else
                    bat.setMovementState(bat.LEFT);

                break;

            //Il giocatore ha alzato il dito dallo schermo
            case MotionEvent.ACTION_UP:

                bat.setMovementState(bat.STOPPED);

                break;
        }

        return true;
    }
}
