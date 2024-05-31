/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.mvc.MVCArray;

import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.UnityMapPanel;
import mil.arl.gift.tools.map.shared.AGL;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;
import mil.arl.gift.tools.map.shared.GDC;

/**
 * A utility class used to convert between different coordinate types for a
 * {@link UnityMapPanel}.
 *
 * @author tflowers
 *
 */
public class UnityCoordinateConversionUtils {

    /**
     * The private constructor that prevents construction of this class
     */
    private UnityCoordinateConversionUtils() {
    }

    /** The west-most global longitude coordinate */
    private static final double WORLD_WEST_BOUND = -180;

    /** The east-most global longitude coordinate */
    private static final double WORLD_EAST_BOUND = 180;

    /** The north-most global latitude coordinate */
    private static final double WORLD_NORTH_BOUND = 90;

    /** The south-most global latitude coordinate */
    private static final double WORLD_SOUTH_BOUND = -90;

    /** The range of longitude units */
    private static final double LONG_UNITS = WORLD_EAST_BOUND - WORLD_WEST_BOUND;

    /** The range of latitude units */
    private static final double LAT_UNITS = WORLD_NORTH_BOUND - WORLD_SOUTH_BOUND;

    /** The western-most longitude bound of the map of the unity environment */
    private static final double MAP_WEST_BOUND = -LONG_UNITS / (1 << UnityMapPanel.MIN_ZOOM + 1);

    /** The eastern-most longitude bound of the map of the unity environment */
    private static final double MAP_EAST_BOUND = LONG_UNITS / (1 << UnityMapPanel.MIN_ZOOM + 1);

    /** The northern-most latitude bound of the map of the unity environment */
    private static final double MAP_NORTH_BOUND = LAT_UNITS / (1 << UnityMapPanel.MIN_ZOOM);

    /** The southern-most latitude bound of the map of the unity environment */
    private static final double MAP_SOUTH_BOUND = -LAT_UNITS / (1 << UnityMapPanel.MIN_ZOOM);

    /**
     * Ensures that a point is within the bounds of the map and also translates
     * it to coordinates that are compatible with the {@link UnityMapPanel}'s
     * map tile coordinate system.
     *
     * @param point The coordinate that describes which map tile is being
     *        requested. Can't be null.
     * @param zoom The zoom level of the point in question. Can't be less than
     *        0.
     * @return The normalized coordinate. Can be null if there is no normalized
     *         version of the coordinate available. This occurs when the tile
     *         being described is out of bounds for the given zoom level.
     */
    public static Point normalizeTileCoordinates(Point point, int zoom) {

        if (point == null) {
            throw new IllegalArgumentException("The parameter 'point' cannot be null.");
        } else if (zoom < 0) {
            throw new IllegalArgumentException("The parameter 'zoom' must be greater than or equal to zero");
        }

        /* The tile coordinates */
        final int rawX = (int) point.getX();
        final int rawY = (int) point.getY();

        /* The number of tiles at the given zoom level */
        final int totalTiles = 1 << zoom;
        final int normalizedZoom = zoom - UnityMapPanel.MIN_ZOOM;

        /* The total number of tiles that the area should take up at this zoom
         * level. */
        final int totalViewTiles = 1 << normalizedZoom;
        final int margin = (totalTiles - totalViewTiles) / 2;

        int normX = rawX - margin;
        int normY = rawY - margin;

        /* If it's outside the bounds of the zoom level, return no tile */
        if (normX < 0 || normX >= totalViewTiles || normY < 0 || normY >= totalViewTiles) {
            return null;
        }

        return Point.newInstance(normX, normY);
    }

    /**
     * Converts a given {@link AbstractMapCoordinate} to a {@link LatLng} which
     * can be consumed by Google Maps.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param coordinate The {@link AbstractMapCoordinate} to be converted.
     *        Can't be null.
     * @return The resulting {@link LatLng} coordinate. Can't be null.
     */
    public static LatLng convertFromMapCoordinateToLatLng(UnityMapPanel map, AbstractMapCoordinate coordinate) {
        GDC gdc = convertFromMapCoordinateToGDC(map, coordinate);
        return LatLng.newInstance(gdc.getLatitude(), gdc.getLongitude());
    }

    /**
     * Converts a given {@link List} of {@link AbstractMapCoordinate} objects to
     * a {@link List} of {@link LatLng} objects.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param coordinates The {@link List} of {@link AbstractMapCoordinate}
     *        objects which are being converted. Can't be null.
     * @return The {@link List} of {@link LatLng} objects that were created by
     *         the conversion. Can't be null.
     */
    public static List<LatLng> convertFromMapCoordinateToLatLng(UnityMapPanel map, List<AbstractMapCoordinate> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("The parameter 'coordinates' cannot be null.");
        }

        List<LatLng> latLngs = new ArrayList<>();
        for (AbstractMapCoordinate coordinate : coordinates) {
            latLngs.add(convertFromMapCoordinateToLatLng(map, coordinate));
        }

        return latLngs;
    }

    /**
     * Converts a given {@link AbstractMapCoordinate} within a
     * {@link UnityMapPanel} to a {@link GDC} coordinate.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param coordinate The {@link AbstractMapCoordinate} that is to be
     *        converted. Can't be null.
     * @return The resulting {@link GDC} coordinate. Can't be null.
     */
    private static GDC convertFromMapCoordinateToGDC(UnityMapPanel map, AbstractMapCoordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("The parameter 'coordinate' cannot be null.");
        }

        if (coordinate instanceof GDC) {
            GDC gdc = (GDC) coordinate;
            return gdc;
        } else if (coordinate instanceof AGL) {
            AGL agl = (AGL) coordinate;
            return convertFromAGLToGDC(map, agl);
        } else {
            throw new UnsupportedOperationException(
                    "Unable to convert coordinate of type " + coordinate.getClass().getName() + " to GDC.");
        }
    }

    /**
     * Converts an {@link AGL} coordinate to a {@link GDC} coordinate with
     * respect to a given {@link UnityMapPanel}.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param agl The {@link AGL} coordinate to convert. Can't be null.
     * @return The resulting {@link GDC} coordinate. Can't be null.
     */
    private static GDC convertFromAGLToGDC(UnityMapPanel map, AGL agl) {
        if (map == null) {
            throw new IllegalArgumentException("The parameter 'map' cannot be null.");
        } else if (agl == null) {
            throw new IllegalArgumentException("The parameter 'agl' cannot be null.");
        }

        /* Get the game bounds of the map */
        final double left = map.getTopLeft().getX();
        final double right = map.getBottomRight().getX();
        final double top = map.getTopLeft().getY();
        final double bottom = map.getBottomRight().getY();

        /* Components of the AGL being converted */
        final double x = agl.getX();
        final double y = agl.getY();

        /* Reframe the game bound within the latitude longitude bound */
        final double longitude = reframeWithinRange(x, left, right, MAP_WEST_BOUND, MAP_EAST_BOUND);
        final double latitude = reframeWithinRange(y, top, bottom, MAP_NORTH_BOUND, MAP_SOUTH_BOUND);

        return new GDC(latitude, longitude, 0);
    }

    /**
     * Converts a given {@link LatLng} object into an {@link AGL} object.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param latLng The {@link LatLng} object to convert. Can't be null.
     * @return The {@link AGL} object that was created by the conversion. Can't
     *         be null.
     */
    private static AGL convertFromLatLngToAGL(UnityMapPanel map, LatLng latLng) {
        if (latLng == null) {
            throw new IllegalArgumentException("The parameter 'latLng' cannot be null.");
        }

        GDC gdc = new GDC(latLng.getLatitude(), latLng.getLongitude(), 0);
        return convertFromGDCToAGL(map, gdc);
    }

    /**
     * Converts a {@link GDC} coordinate to its {@link AGL} equivalent within a
     * given {@link UnityMapPanel}.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param gdc The {@link GDC} coordinate to convert. Can't be null.
     * @return The resulting {@link AGL} coordinate. Can't be null.
     */
    private static AGL convertFromGDCToAGL(UnityMapPanel map, GDC gdc) {
        if (gdc == null) {
            throw new IllegalArgumentException("The parameter 'gdc' cannot be null.");
        }

        /* Get the game bounds of the map */
        final double left = map.getTopLeft().getX();
        final double right = map.getBottomRight().getX();
        final double top = map.getTopLeft().getY();
        final double bottom = map.getBottomRight().getY();

        /* Reframe the latitude longitude bounds within the game bounds */
        final double longitude = gdc.getLongitude();
        final double latitude = gdc.getLatitude();

        final double x = reframeWithinRange(longitude, MAP_WEST_BOUND, MAP_EAST_BOUND, left, right);
        final double y = reframeWithinRange(latitude, MAP_NORTH_BOUND, MAP_SOUTH_BOUND, top, bottom);

        return new AGL(x, y, 0);
    }

    /**
     * Converts an {@link AbstractMapCoordinate} object into an {@link AGL}
     * object.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param coordinate The {@link AbstractMapCoordinate} object to convert.
     *        Can't be null.
     * @return The {@link AGL} object created by the conversion logic. Can't be
     *         null.
     */
    public static AGL convertFromMapCoordinateToAGL(UnityMapPanel map, AbstractMapCoordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("The parameter 'coordinate' cannot be null.");
        }

        if (coordinate instanceof AGL) {
            return (AGL) coordinate;
        } else if (coordinate instanceof GDC) {
            return convertFromGDCToAGL(map, (GDC) coordinate);
        } else {
            throw new UnsupportedOperationException(
                    "Unable to convert coordinate of type " + coordinate.getClass().getName() + " to AGL.");
        }
    }

    /**
     * Converts a {@link List} of {@link AbstractMapCoordinate} objects into a
     * {@link List} of {@link AGL} objects.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param coordinates The {@link List} of {@link AbstractMapCoordinate}
     *        objects to convert. Can't be null.
     * @return The {@link List} of {@link AGL} objects created by the
     *         conversion. Can't be null.
     */
    public static List<AGL> convertFromMapCoordinateToAGL(UnityMapPanel map, List<? extends AbstractMapCoordinate> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("The parameter 'coordinates' cannot be null.");
        }

        List<AGL> agls = new ArrayList<>();
        for (AbstractMapCoordinate coordinate : coordinates) {
            agls.add(convertFromMapCoordinateToAGL(map, coordinate));
        }

        return agls;
    }

    /**
     * Converts a {@link LatLng} object to a {@link AbstractMapCoordinate}
     * object.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param latLng The {@link LatLng} object to convert. Can't be null.
     * @return The {@link AbstractMapCoordinate} object created by the
     *         conversion. Can't be null.
     */
    private static AbstractMapCoordinate convertFromLatLngToMapCoordinate(UnityMapPanel map, LatLng latLng) {
        return convertFromLatLngToAGL(map, latLng);
    }

    /**
     * Converts a {@link List} of {@link LatLng} objects to a {@link List} of
     * {@link AbstractMapCoordinate} objects.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param latLngs The {@link List} of {@link LatLng} objects to convert.
     *        Can't be null.
     * @return The {@link List} of {@link AbstractMapCoordinate} objects created
     *         by the conversion. Can't be null.
     */
    private static List<AbstractMapCoordinate> convertFromLatLngToMapCoordinate(UnityMapPanel map, List<LatLng> latLngs) {
        if (latLngs == null) {
            throw new IllegalArgumentException("The parameter 'latLngs' cannot be null.");
        }

        List<AbstractMapCoordinate> mapCoordinates = new ArrayList<>();
        for (LatLng coordinate : latLngs) {
            AbstractMapCoordinate mapCoordinate = convertFromLatLngToMapCoordinate(map, coordinate);
            mapCoordinates.add(mapCoordinate);
        }

        return mapCoordinates;
    }

    /**
     * Converts an {@link MVCArray} of {@link LatLng} objects to a {@link List}
     * of {@link AbstractMapCoordinate} objects.
     *
     * @param map The {@link UnityMapPanel} within which the coordinates exist.
     *        Can't be null.
     * @param latLngArray The {@link MVCArray} of {@link LatLng} to convert.
     *        Can't be null.
     * @return The {@link List} of {@link AbstractMapCoordinate} that was
     *         created as a result of the conversion. Can't be null.
     */
    public static List<AbstractMapCoordinate> convertFromLatLngToMapCoordinate(UnityMapPanel map, MVCArray<LatLng> latLngArray) {
        if (latLngArray == null) {
            throw new IllegalArgumentException("The parameter 'latLngArray' cannot be null.");
        }

        List<LatLng> latLngList = new ArrayList<>();
        for (int i = 0; i < latLngArray.getArray().length(); i++) {
            LatLng latLng = latLngArray.get(i);
            latLngList.add(latLng);
        }

        return convertFromLatLngToMapCoordinate(map, latLngList);
    }

    /**
     * Calculates the relative position of a value within a second range based
     * on its relative position within the first range.
     *
     * <h1>Example</h1>
     *
     * <pre>
     * reframeWithinRange(50, 0, 100, -50, 50); // 0
     * reframeWithinRange(1, 1, 3, 1, 10); // 1
     * </pre>
     *
     * @param value The value with which to calculate the resulting value.
     * @param firstStart The start of the first range.
     * @param firstEnd The end of the first range.
     * @param secondStart The start of the second range.
     * @param secondEnd The end of the second range.
     * @return The equivalent value within the second range.
     */
    private static double reframeWithinRange(double value, double firstStart, double firstEnd, double secondStart, double secondEnd) {
        /* Calculates the length of the two ranges */
        double firstRange = firstEnd - firstStart;
        double secondRange = secondEnd - secondStart;

        /* Determines the normalized position of the value within the first
         * range */
        double normalized = (value - firstStart) / firstRange;

        /* Return the same normalized position within the second range */
        return normalized * secondRange + secondStart;
    }
}
