package it.di.uniba.sms1920.teambarrella.unibarcade.arkanoid;

import android.graphics.RectF;

public class Bat {

    //Rettangolo o quadrato
    private RectF rect;

    //Lunghezza della barra
    private float length;

    //Posizione sull'asse delle ascisse della barra
    private float x;

    //Velocita' della barra (del movimento dei pixels)
    private float paddleSpeed;

    //Stati di movimento della barra: puo'
    //stare ferma, andare a sinistra o destra
    final int STOPPED = 0;
    final int LEFT = 1;
    final int RIGHT = 2;

    /**
     * variabile che stabilisce se la barra
     * è in movimento o meno. Se sì, assumerà
     * valori 1 (LEFT) o 2 (RIGHT), altrimenti
     * sarà ferma (STOPPED) e assumerà valore 0
     */
    private int paddleMoving = STOPPED;

    Bat(int screenX, int screenY) {

        //130 pixels larga e 20 pixels alta
        length = 130;
        float height = 20;

        //Posizioniamo la barra al centro del display...
        x = screenX / 2;

        //...e definiamo la sua altezza
        float y = screenY - 20;

        /**
         * Disegniamo il rettangolo rappresentante la barra:
         * i primi due parametri, x e y, definiranno il punto spaziale
         * da cui disegnare il rettangolo, e invece x + length definirà
         * fino a che punto disegnare la base e y + height la sua altezza
         */
        rect = new RectF(x, y, x + length, y + height);

        //velocita' pixels al secondo
        paddleSpeed = 350;
    }

    /**
     *
     * @return il rettangolo che useremo nella classe ArcanoidView
     */
    RectF getRect() {
        return rect;
    }

    /**
     *
     * @param state imposta il movimento, se sinistra o destra
     */
    void setMovementState(int state) {
        paddleMoving = state;
    }


    /**
     *
     * @param fps il frame più recente dell'esecuzione
     */
    void update(float fps) {

        /**
         * Aumento della difficolta'
         */

        /**
         * movimento a sinistra: x sara' ridotto
         * in relazione alla velocita' e ai frame al secondo.
         * Il valore di x, diminuendo, raggiungera' punti sull'asse delle x
         * alla sua sinistra
         */
        if (paddleMoving == LEFT) {
            x = x - paddleSpeed / fps;
        }

        /**
         * movimento a destra: x aumenta
         * in relazione alla velocita' e ai frame al secondo.
         * Il valore di x, aumentando, raggiungera' punti sull'asse delle x
         * alla sua destra
         */
        if (paddleMoving == RIGHT) {
            x = x + paddleSpeed / fps;
        }

        /**
         * aggiorniamo la nuova coordinata cosicchè la barra rimanga
         * nella nuova posizione aggiornata al movimento dato
         */
        rect.left = x;
        rect.right = x + length;
    }

}
