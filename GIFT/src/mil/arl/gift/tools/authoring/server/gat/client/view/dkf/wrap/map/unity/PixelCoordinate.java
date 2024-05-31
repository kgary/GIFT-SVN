/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Represents a pixel within an image using two coordinates, x and y.
 * 
 * @author tflowers
 *
 */
public class PixelCoordinate {

    /** The horizontal component of the coordinate */
    private final int x;

    /** The vertical component of the coordinate */
    private final int y;

    /**
     * Constructs a {@link PixelCoordinate} with the two given components.
     *
     * @param x The horizontal component. Must be greater than or equal to 0.
     * @param y The vertical component. Must be greater than or equal to 0.
     */
    public PixelCoordinate(int x, int y) {
        if (x < 0) {
            throw new IllegalArgumentException("The parameter 'x' must be non-negative");
        } else if (y < 0) {
            throw new IllegalArgumentException("The parameter 'y' must be non-negative");
        }

        this.x = x;
        this.y = y;
    }

    /**
     * Gets the distance between two points.
     *
     * @param a The first point for which distance should be compared.
     * @param b The second point for which distance should be compared.
     * @return The distance between the two points measured in pixels.
     */
    public static double getDistanceBetween(PixelCoordinate a, PixelCoordinate b) {
        double deltaX = a.getX() - b.getX();
        double deltaY = a.getY() - b.getY();

        return sqrt(pow(deltaX, 2) + pow(deltaY, 2));
    }

    /**
     * Gets the horizontal component of the coordinate.
     *
     * @return The horizontal component. Will always be greater than or equal to
     *         0.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the vertical component of the coordinate.
     *
     * @return The vertical component. Will always be greater than or equal to
     *         0.
     */
    public int getY() {
        return y;
    }
}
