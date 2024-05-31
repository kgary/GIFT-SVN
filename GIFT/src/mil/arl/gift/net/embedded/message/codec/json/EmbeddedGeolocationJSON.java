/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.embedded.message.codec.json;

import javax.vecmath.Point3d;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.codec.json.Point3dJSON;
import mil.arl.gift.net.embedded.message.EmbeddedGeolocation;
import mil.arl.gift.net.json.JSONCodec;

/**
 * A payload consisting of an encodable geolocation
 *
 * @author nroberts
 */
public class EmbeddedGeolocationJSON implements JSONCodec {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmbeddedGeolocationJSON.class);

    /** message attribute names */
    private static final String COORDINATES_PROPERTY = "coordinates";
    private static final String ACCURACY_PROPERTY = "accuracy";
    private static final String ALTITUDE_ACCURACY_PROPERTY = "altitudeAccuracy";
    private static final String HEADING_PROPERTY = "heading";
    private static final String SPEED_PROPERTY = "speed";

    private static Point3dJSON pointCodec = new Point3dJSON();

    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {

        try {

            Point3d coordPoint = (Point3d) pointCodec.decode((JSONObject) jsonObj.get(COORDINATES_PROPERTY));

            GDC coordinates = new GDC(coordPoint.getX(), coordPoint.getY(), 0 /*ignore elevation since test phones to not properly track it*/);

            Double accuracy;
            if(jsonObj.get(ACCURACY_PROPERTY) != null) {
                accuracy = ((Number) jsonObj.get(ACCURACY_PROPERTY)).doubleValue();
            } else {
                accuracy = null;
            }

            Double altitudeAccuracy;
            if(jsonObj.get(ALTITUDE_ACCURACY_PROPERTY) != null) {
                altitudeAccuracy = ((Number) jsonObj.get(ALTITUDE_ACCURACY_PROPERTY)).doubleValue();
            } else {
                altitudeAccuracy = null;
            }

            Double heading;
            if(jsonObj.get(HEADING_PROPERTY) != null) {
                heading = ((Number) jsonObj.get(HEADING_PROPERTY)).doubleValue();
            } else {
                heading = null;
            }

            Double speed;
            if(jsonObj.get(SPEED_PROPERTY) != null) {
                speed = ((Number) jsonObj.get(SPEED_PROPERTY)).doubleValue();
            } else {
                speed = null;
            }

            return new EmbeddedGeolocation(coordinates, accuracy, altitudeAccuracy, heading, speed);

        } catch (Exception e) {
            logger.error("Caught exception while creating a geolocation from " + jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void encode(JSONObject jsonObj, Object payload) {

        EmbeddedGeolocation geolocation = (EmbeddedGeolocation) payload;

        Point3d coordPoint = new Point3d(geolocation.getCoordinates().getLatitude(),
                geolocation.getCoordinates().getLongitude(),
                0 /*ignore elevation since test phones to not properly track it*/);

        JSONObject coordObj = new JSONObject();
        pointCodec.encode(coordObj, coordPoint);
        jsonObj.put(COORDINATES_PROPERTY, coordObj);

        jsonObj.put(ACCURACY_PROPERTY, geolocation.getAccuracy());
        jsonObj.put(ALTITUDE_ACCURACY_PROPERTY, geolocation.getAltitudeAccuracy());
        jsonObj.put(HEADING_PROPERTY, geolocation.getHeading());
        jsonObj.put(SPEED_PROPERTY, geolocation.getSpeed());
    }
}
