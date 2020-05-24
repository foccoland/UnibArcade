// Line.java
// Class Line represents a line with two endpoints.
package it.di.uniba.sms1920.teambarrella.unibarcade.cannonball;

import android.graphics.Point;

public class Line {
    public Point start;
    public Point end;

    /*
     * Default construct initialize points to (0,0)
     */
    public Line() {
        start = new Point(0, 0);
        end = new Point(0, 0);
    }
}