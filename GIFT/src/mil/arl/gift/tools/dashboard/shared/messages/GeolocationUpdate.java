/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import mil.arl.gift.tools.map.shared.GDC;

/**
 * An {@link EntityStateUpdate} that provides the real-world geolocation of a learner's entity in a domain knowledge session
 * 
 * @author nroberts
 */
@SuppressWarnings("serial")
public class GeolocationUpdate extends EntityStateUpdate {
    
    /** The accuracy level of the learner's latitude and longitude, in meters */
    private Double accuracy;
    
    /** The accuracy level of the learner's altitude, in meters*/
    private Double altitudeAccuracy;
    
    /**
     * Default, no-argument constructor needed for GWT RPC serialization
     */
    private GeolocationUpdate() {
        super();
    }

    /**
     * Creates a new update for the learner with the given user ID at the given real-world geolocation
     * 
     * @param hostDomainSessionId the ID of the domain session that is hosting the domain knowledge session. Cannot be null.
     * @param location the real-world geolocation of the learner. Cannot be null.
     */
    public GeolocationUpdate(int hostDomainSessionId, GDC location) {
        this();
        
        //use an entity ID of 0, since Geolocation messages are only received from mobile device courses, which only have 1 learner entity
        setSessionEntityId(new SessionEntityIdentifier(hostDomainSessionId, 0));
        setLocation(location);
    }
    
    /**
     * Creates a new update for the learner with the given user ID at the given real-world geolocation 
     * with the given location properties
     * 
     * @param hostDomainSessionId the ID of the domain session that is hosting the domain knowledge session. Cannot be null.
     * @param location the real-world geolocation of the learner. Cannot be null.
     * @param accuracy the accuracy of the latitude and longitude provided by the geolocation. Can be null.
     * @param altitudeAccuracy the accuracy of the altitude/elevation provided by the geolocation. Can be null.
     * @param heading the direction of the learner is heading in, in degrees clockwise from north
     * @param speed the learner's speed in meters per second
     */
    public GeolocationUpdate(int hostDomainSessionId, GDC location, Double accuracy, Double altitudeAccuracy,
            Double heading, Double speed) {
        
        this(hostDomainSessionId, location);
        this.accuracy = accuracy;
        this.altitudeAccuracy = altitudeAccuracy;
        setOrientation(heading);
        setVelocity(speed);
    }
    
    /**
     * Gets the accuracy of the latitude and longitude provided by the geolocation. Can be null.
     * 
     * @return the accuracy of the learner's geolocation
     */
    public Double getAccuracy() {
        return accuracy;
    }

    /**
     * Gets the accuracy of the altitude/elevation provided by the geolocation. Can be null.
     * 
     * @return the altitude accuracy of the learner's geolocation
     */
    public Double getAltitudeAccuracy() {
        return altitudeAccuracy;
    }
}
