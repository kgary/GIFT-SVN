/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.coordinate;

/**
 * The Above-Ground-Level (AGL) coordinate system is used in VBS.  More specifically it is used
 * by GIFT to communicate locations with VBS scripting interface.
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class AGL extends AbstractCoordinate {
       
    

    /** container for the coordinate */
    private double x;
    private double y;
    private double z;


    /**
     * Create an un-initialized AGL coordinate.
     */
    public AGL(){
        this(0,0,0);
    }
    
    /**
     * Create and initialize a AGL coordinate, consisting of x, y, and z
     * axis values. 
     *
     * @param x - x axis value.
     * @param y - y axis value.
     * @param z - z axis value.
     */
    public AGL(double x,
               double y,
               double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public double getX(){
        return x;
    }
    
    public double getY(){
        return y;
    }
    
    public double getZ(){
        return z;
    }
        
    /**
     * Compares the current coordinate values to the received AGL
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
        AGL other = (AGL) obj;
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

        return "AGL: (" + this.getX() + ", " + this.getY() + ", " + this.getZ() + ")";
    }
    
}
