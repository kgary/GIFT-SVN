/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity;

import static mil.arl.gift.common.util.StringUtils.join;
import static mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityCoordinateConversionUtils.convertFromLatLngToMapCoordinate;
import static mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityCoordinateConversionUtils.convertFromMapCoordinateToAGL;
import static mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity.UnityCoordinateConversionUtils.convertFromMapCoordinateToLatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Polyline;

import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.UnityMapPanel;
import mil.arl.gift.tools.map.client.google.GoogleMapsPolylineShape;
import mil.arl.gift.tools.map.shared.AGL;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * Represents a {@link PolylineShape} within a {@link UnityMapPanel}.
 *
 * @author tflowers
 *
 */
public class UnityMapPolylineShape extends GoogleMapsPolylineShape {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(UnityMapPolylineShape.class.getName());

    public UnityMapPolylineShape(UnityMapPanel map, List<AbstractMapCoordinate> vertices) {
        super(map, vertices);
    }

    public UnityMapPolylineShape(UnityMapPanel map, Polyline polyline) {
        super(map, polyline);
    }

    @Override
    public void setVertices(List<AbstractMapCoordinate> vertices) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(vertices);
            logger.fine("setVertices(" + join(", ", params) + ")");
        }

        List<AGL> agls = convertFromMapCoordinateToAGL((UnityMapPanel) mapImpl, vertices);
        List<AbstractMapCoordinate> abstractAgls = new ArrayList<>(agls.size());
        abstractAgls.addAll(agls);
        super.setVertices(abstractAgls);
    }

    @Override
    protected List<AbstractMapCoordinate> latLngsToCoordinates(MVCArray<LatLng> coordinates) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(coordinates);
            logger.fine("latLngsToCoordinates(" + join(", ", params) + ")");
        }

        return convertFromLatLngToMapCoordinate((UnityMapPanel) mapImpl, coordinates);
    }

    @Override
    protected MVCArray<LatLng> coordinatesToLatLngs(List<AbstractMapCoordinate> coordinates) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(coordinates);
            logger.fine("coordinatesToLatLngs(" + join(", ", params) + ")");
        }

        /* Do the intiial conversion */
        List<LatLng> latLngs = convertFromMapCoordinateToLatLng((UnityMapPanel) mapImpl, coordinates);

        /* Move all the elements into the an MVCArray */
        final MVCArray<LatLng> mvcArray = MVCArray.newInstance();
        for (LatLng latLng : latLngs) {
            mvcArray.push(latLng);
        }

        /* Return the array */
        return mvcArray;
    }
}