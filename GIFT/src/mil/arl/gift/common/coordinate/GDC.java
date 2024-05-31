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
 * GDC is a coordinate class defining a geodetic coordinate.
 * 
 * Geodetic coordinate system has a latitude running from -90 (south pole) to
 * +90 (north pole) degrees with 0 degrees being at the equator, and a
 * longitude running from -180 to +180 degrees with 0 degrees being at the
 * prime meridian.  Elevation is referenced to the ellipsoid, which defines 0
 * elevation on the earth.  Thus, a terrain height value of 10m means 10
 * meters above the ellipsoid.
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class GDC extends AbstractCoordinate {
        
    private double elevation = Double.NaN; // meters
    private double latitude = Double.NaN; // degrees
    private double longitude = Double.NaN; // degrees
    
    /** used to format values to 6 decimal places for sub-meter resolution */
    private static final DecimalFormatExt format = new DecimalFormatExt("#.00000#");

    /**
     * Create an un-initialized GDC coordinate. 
     */
    public GDC(){

    }
    
    /**
     * Create and initialize a GDC coordinate, consisting of latitude,
     * longitude, and elevation values. 
     *
     * @param latitude  Geodetic latitude in degrees.
     * @param longitude Geodetic longitude in degrees.
     * @param elevation Geodetic elevation in meters.
     */
    public GDC(double latitude,
               double longitude,
               double elevation) {
        
        set(latitude, longitude, elevation);
    }
    
    /**
     * Sets the coordinate values to the GDC values received.
     *
     * @param latitude  Geodetic latitude value in degrees.
     * @param longitude Geodetic longitude value in degrees.
     * @param elevation Geodetic elevation value in meters.
     */
    public void set(double latitude, double longitude, double elevation){

        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }
    
    /**
     * Get the elevation value of the GDC coordinate.
     *
     * @return Geodetic elevation value in meters.
     */
    public double getElevation() {

        return this.elevation;
    }
    
    /**
     * Get the latitude value of the GDC coordinate.
     *
     * @return Geodetic latitude value in degrees.
     */
    public double getLatitude() {

        return this.latitude;
    }
    
    /**
     * Get the longitude value of the GDC coordinate.
     *
     * @return Geodetic longitude value in degrees.
     */
    public double getLongitude() {

        return this.longitude;
    }
       
    /**
     * Compares the current coordinate values to the received GDC
     * coordinate values.
     *
     * @param latitude  Geodetic latitude value in degrees.
     * @param longitude Geodetic longitude value in degrees.
     * @param elevation Geodetic elevation value in meters.
     * @return boolean equivalency result
     */
    protected boolean equals(double latitude,
                             double longitude,
                             double elevation) {

        return this.latitude == latitude &&
               this.longitude == longitude &&
               this.elevation == elevation;
    }
    


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(elevation);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
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
        GDC other = (GDC) obj;
        if (Double.doubleToLongBits(elevation) != Double.doubleToLongBits(other.elevation))
            return false;
        if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
            return false;
        if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
            return false;
        return true;
    }

    @Override
    public String toString() {

        return "GDC: (" + format.format(this.getLatitude()) + ", " + format.format(this.getLongitude()) + ", " + format.format(this.getElevation()) + ")";
    }
}
