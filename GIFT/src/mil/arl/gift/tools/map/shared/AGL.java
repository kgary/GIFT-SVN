/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.shared;

/**
 * Represents an above ground location coordinate. This coordinate type is
 * useful for generic artificial game environments.
 *
 * @author tflowers
 *
 */
public class AGL extends AbstractMapCoordinate {

    /** The x component of the {@link AGL} coordinate */
    private double x;

    /** The y component of the {@link AGL} coordinate */
    private double y;

    /** The elevation of the {@link AGL} coordinate */
    private double elevation;

    /**
     * Constructs a new {@link AGL} at (0, 0, 0)
     */
    public AGL() {
        this(0, 0, 0);
    }

    /**
     * Constructs a new {@link AGL} with the provided components
     *
     * @param x The x component of the coordinate
     * @param y The y component of the coordinate
     * @param elevation The elevation of the coordinate.
     */
    public AGL(double x, double y, double elevation) {
        super();

        setX(x);
        setY(y);
        setElevation(elevation);
    }

    /**
     * Gets the x component of the coordinate.
     *
     * @return The value of x.
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the x component of the coordinate.
     *
     * @param x The new value of x.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Gets the y component of the coordinate.
     *
     * @return The value of y.
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the y component of the coordinate.
     *
     * @param y The new value of y.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Gets the elevation of the coordinate.
     *
     * @return The value of {@link #elevation}.
     */
    public double getElevation() {
        return elevation;
    }

    /**
     * Sets the elevation of the coordinate.
     *
     * @param elevation The new value of {@link #elevation}.
     */
    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    @Override
    public String toString() {
        return new StringBuilder("[AGL: ")
                .append("x = ").append(x)
                .append(", y = ").append(y)
                .append(", elevation = ").append(elevation)
                .append("]").toString();
    }
}