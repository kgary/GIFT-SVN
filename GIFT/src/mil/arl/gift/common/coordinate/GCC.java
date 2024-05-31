/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.coordinate;

import mil.arl.gift.common.util.DecimalFormatExt;

/**
 * GCC is a coordinate class defining a geocentric coordinate.
 * 
 * Note: A valid GCC coordinate must be at least 500 meters distance
 * from the center of the earth.
 * 
 * Geocentric is an earth-centered coordinate system, with (0,0,0)
 * defined as the center of the earth. The z-axis runs through the north pole,
 * the x-axis intersects the prime meridian and equator, and the y-axis is
 * perpendicular to the xz plane (right handed system).
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class GCC extends AbstractCoordinate {
    
       
    /** the x coordinate of GCC */
    private double x;
    
    /** the y coordinate of GCC */
    private double y;
    
    /** the z coordinate of GCC */
    private double z;
    
    /** used to format values to 2 decimal places for sub-meter resolution */
    private static final DecimalFormatExt format = new DecimalFormatExt("#.0#");

    /**
     * Create an un-initialized GCC coordinate. Use system ellipsoid.
     */
    public GCC(){
        this(0,0,0);
    }
    
    /**
     * Create and initialize a GCC coordinate, consisting of x,y, and z
     * axis values. Use system ellipsoid.
     *
     * @param x - x axis value.
     * @param y - y axis value.
     * @param z - z axis value.
     */
    public GCC(double x,
               double y,
               double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Return the x-axis value
     * @return meters along the x-axis
     */
    public double getX(){
        return x;
    }
    
    /**
     * Return the y-axis value
     * @return meters along the y-axis
     */
    public double getY(){
        return y;
    }
    
    /**
     * Return the z-axis value
     * @return meters along the z-axis
     */
    public double getZ(){
        return z;
    }

    /**
     * Compares the current coordinate values to the received GCC
     * coordinate values.
     *
     * @param x Geocentric X value in meters.
     * @param y Geocentric Y value in meters.
     * @param z Geocentric Z value in meters.
     * @return boolean 
     */
    protected boolean equals(double x,
                             double y,
                             double z) {

        return this.getX() == x && this.getY() == y && this.getZ() == z;
    }
    


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GCC other = (GCC) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
            return false;
        return true;
    }

    @Override
    public String toString() {

        return "GCC: (" + format.format(this.getX()) + ", " + format.format(this.getY()) + ", " + format.format(this.getZ()) + ")";
    }
  

}
