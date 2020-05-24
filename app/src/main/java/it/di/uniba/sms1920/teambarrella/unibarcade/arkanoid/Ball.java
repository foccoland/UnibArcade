package it.di.uniba.sms1920.teambarrella.unibarcade.arkanoid;

import android.graphics.RectF;

import java.util.Random;

public class Ball {

    private RectF rect;

    /**
     * Se la pallina cambia direzione, allora
     * cambiera' anche il segno (opportunamente)
     * di queste due variabili
     */
    private float xVelocity;
    private float yVelocity;
    private float ballWidth = 10;
    private float ballHeight = 10;

    Ball() {

        xVelocity = 200;
        yVelocity = -400;

        rect = new RectF();
    }

    RectF getRect() {
        return rect;
    }

    void update(float fps) {

        /**
         * Aumento velocita' palla
         */
//        xVelocity = xVelocity + hardnessSpeed;
//        yVelocity = yVelocity + hardnessSpeed;

        rect.left = rect.left + (xVelocity / fps);
        rect.top = rect.top + (yVelocity / fps);
        rect.right = rect.left + ballWidth;
        rect.bottom = rect.top - ballHeight;
    }

    void reverseYVelocity() {
        yVelocity = -yVelocity;
    }

    void reverseXVelocity() {
        xVelocity = -xVelocity;
    }

    void setRandomXVelocity() {
        Random generator = new Random();
        int answer = generator.nextInt();

        if(answer == 0) {
            reverseXVelocity();
        }
    }

    //Elimina gli ostacoli
    void clearObstacleY(float y) {
        rect.bottom = y;
        rect.top = y - ballHeight;
    }

    void clearObstacleX(float x) {
        rect.left = x;
        rect.right = x + ballWidth;
    }

    //reset pallina nel momento in cui si perde una vita
    void reset(int x, int y) {
        rect.left = x / 2;
        rect.top = y - 20;
        rect.right = x / 2 + ballWidth;
        rect.bottom = y - 20 - ballWidth;
    }
}
