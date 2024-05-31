/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.shared;

import mil.arl.gift.common.util.DecimalFormatExt;

/**
 * A geodetic (GDC) coordinate that denotes a location by its latitude, longitude, and altitude
 *
 * @author nroberts
 *
 */
public class GDC extends AbstractMapCoordinate {

    /** The latitude coordinate component */
    private double latitude = 0;

    /** The longitude coordinate component */
    private double longitude = 0;

    /** The altitude coordinate component */
    private double altitude = 0;
    
    /** used to format values to 6 decimal places for sub-meter resolution */
    private static final DecimalFormatExt format = new DecimalFormatExt("#.00000#");

    /**
     * Creates a new GDC coordinate with latitude, longitude, and altitude all set to 0
     */
    public GDC() {
        super();
    }

    /**
     * Creates a new GDC coordinate with the given latitude, longitude, and altitude
     *
     * @param latitude the coordinate's latitude value
     * @param longitude the coordinate's longitude value
     * @param altitude the coordinate's altitude value
     */
    public GDC(double latitude, double longitude, double altitude) {
        this();

        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /**
     * Gets this coordinate's altitude value
     *
     * @return the altitude
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * Sets this coordinate's altitude value
     *
     * @param altitude the altitude
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    /**
     * Gets this coordinate's longitude value
     *
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets this coordinate's longitude value
     *
     * @param longitude the longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets this coordinate's altitude value
     *
     * @return the altitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets this coordinate's latitude value
     *
     * @param latitude the latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return new StringBuilder("[GDC: ")
                .append("lat = ").append(format.format(latitude))
                .append(", long = ").append(format.format(longitude))
                .append(", alt = ").append(format.format(altitude))
                .append("]").toString();
    }
}
