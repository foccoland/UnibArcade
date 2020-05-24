package it.di.uniba.sms1920.teambarrella.unibarcade.arkanoid;

import android.graphics.Rect;
import android.graphics.RectF;

public class Brick {

    //rettangolo del mattoncino
    RectF rect;

    //visibile oppure no
    private boolean isVisible;

    Brick(int row, int column, int width, int height) {

        isVisible = true;

        int padding = 1;

        /**
         * Costruzione rettangolo relativo al mattoncino:
         * il posizionamento è in relazione alla colonna e alla riga che,
         * moltiplicata con la sua width e la sua height, ci restituisce
         * il mattoncino nell'opportuna posizione della matrice dei
         * mattoncini
         */
        rect = new RectF(
                column * width + padding,
                row * height + padding,
                column * width + width - padding,
                row * height + height - padding
        );
    }

    RectF getRect() {
        return rect;
    }

    void setInvisible() {
        isVisible = false;
    }

    boolean getVisibility() {
        return isVisible;
    }
}
