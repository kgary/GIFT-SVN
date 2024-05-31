/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import mil.arl.gift.common.coordinate.GDC;

/**
 * The real-world location of an object with respect to the Earth
 * 
 * @author nroberts
 */
public class Geolocation implements TrainingAppState {

    /** The GDC coordinates representing the object's latitude, longitude, and elevation */
    private GDC coordinates;
    
    /** The accuracy level of the object's latitude and longitude, in meters */
    private Double accuracy;
    
    /** The accuracy level of the object's altitude, in meters*/
    private Double altitudeAccuracy;
    
    /** The object's direction of travel, in degrees relative to true north */
    private Double heading;
    
    /** The object's current ground speed, in meters per second */
    private Double speed;

    /**
     * Creates a new geolocation with the specified properties
     * 
     * @param accuracy
     * @param altitudeAccuracy
     * @param heading
     * @param speed
     */
    public Geolocation(GDC coordinates, Double accuracy, Double altitudeAccuracy,
            Double heading, Double speed) {
        
        this.coordinates = coordinates;
        this.accuracy = accuracy;
        this.altitudeAccuracy = altitudeAccuracy;
        this.heading = heading;
        this.speed = speed;
    }

    /**
     * @return the altitude
     */
    public GDC getCoordinates() {
        return coordinates;
    }

    /**
     * @return the accuracy
     */
    public Double getAccuracy() {
        return accuracy;
    }

    /**
     * @return the altitudeAccuracy
     */
    public Double getAltitudeAccuracy() {
        return altitudeAccuracy;
    }

    /**
     * @return the heading
     */
    public Double getHeading() {
        return heading;
    }

    /**
     * @return the speed
     */
    public Double getSpeed() {
        return speed;
    }
    
    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("[Geolocation: ");
        sb.append("coordinates = ").append(getCoordinates().toString());
        sb.append(", accuracy = ").append(getAccuracy());
        sb.append(", altitudeAccuracy = ").append(getAltitudeAccuracy());
        sb.append(", heading = ").append(getHeading());
        sb.append(", speed = ").append(getSpeed());
        sb.append("]");
        
        return sb.toString();
    }
}
