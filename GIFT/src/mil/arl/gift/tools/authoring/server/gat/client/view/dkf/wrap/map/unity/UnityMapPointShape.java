/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity;

import static mil.arl.gift.common.util.StringUtils.join;
import static mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityCoordinateConversionUtils.convertFromMapCoordinateToAGL;
import static mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityCoordinateConversionUtils.convertFromMapCoordinateToLatLng;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.overlays.Marker;

import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.UnityMapPanel;
import mil.arl.gift.tools.map.client.google.GoogleMapsPointShape;
import mil.arl.gift.tools.map.shared.AGL;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * Represents a {@link PointShape} within a {@link UnityMapPanel}.
 *
 * @author tflowers
 *
 */
public class UnityMapPointShape extends GoogleMapsPointShape {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(UnityMapPointShape.class.getName());

    public UnityMapPointShape(UnityMapPanel mapImpl, AbstractMapCoordinate coordinate) {
        super(mapImpl, coordinate);
    }

    public UnityMapPointShape(UnityMapPanel mapImpl, Marker marker) {
        super(mapImpl, marker);
    }

    @Override
    public AGL getLocation() {
        return (AGL) super.getLocation();
    }

    @Override
    public void setLocation(AbstractMapCoordinate location) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setLocation(" + location + ")");
        }

        AGL agl = convertFromMapCoordinateToAGL((UnityMapPanel) mapImpl, location);
        super.setLocation(agl);
    }

    @Override
    protected LatLng coordinateToLatLng(AbstractMapCoordinate coordinate) {
        final LatLng toRet = convertFromMapCoordinateToLatLng((UnityMapPanel) mapImpl, coordinate);
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(coordinate);
            logger.fine("coordinateToLatLng(" + join(", ", params) + "): " + toRet);
        }

        return toRet;
    }
}