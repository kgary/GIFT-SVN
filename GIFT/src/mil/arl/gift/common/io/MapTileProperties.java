/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.Serializable;
import java.math.BigDecimal;

import mil.arl.gift.common.util.StringUtils;

/**
 * The property values from a GIFT scenario map tile property file. These
 * property files are typically located within "Training.Apps/maps/[Public |
 * user]/GIFT_Overlay_Resources/". They are used to provide the location of the
 * image file and two points to map pixel coordinates to their respective
 * location within the application's game space. <br/>
 * <br/>
 * An example of the contents of the property file is as follows:
 *
 * <pre>
 * ImageFile=overlay.png
 * LowerLeft_AGL=0, 100
 * UpperRight_AGL=100, 0
 * </pre>
 *
 * @author sharrison
 */
public class MapTileProperties implements Serializable {

    /** default serial version number */
    private static final long serialVersionUID = 1L;

    /** The extension for map tile property files */
    public static final String MAPTILE_EXTENSION = ".maptile.properties";

    /** Image file property key */
    public static final String IMAGE_FILE = "ImageFile";

    /** Lower Left AGL coordinate property key */
    public static final String LOWER_LEFT_AGL = "LowerLeft_AGL";

    /** Upper Right AGL coordinate property key */
    public static final String UPPER_RIGHT_AGL = "UpperRight_AGL";

    /** Zoom Level property key */
    public static final String ZOOM_LEVEL = "ZoomLevel";

    /** Tile coordinate property key */
    public static final String TILE_COORDINATE = "TileCoordinate";

    /** The relative (to the scenario folder) image file path */
    private String imageFilePath;

    /** The lower left AGL image coordinate */
    private MapTileCoordinate lowerLeftAGL;

    /** The upper right AGL image coordinate */
    private MapTileCoordinate upperRightAGL;

    /** The zoom level to which the map tile belongs */
    private int zoomLevel;

    /** The 2D index of a tile at a given zoom level. */
    private MapTileCoordinate tileCoordinate;

    /**
     * No argument constructor used to make the class GWT serializable.
     */
    private MapTileProperties() {
    }

    /**
     * Constructor.
     *
     * @param imageFilePath the path to the image file. The root should be the subfolder of the
     *        scenario folder. Can't be blank. <br/>
     *        For example: <br/>
     *        Image file location:
     *        Training.Apps/maps/Public/LandNavUnity/GIFT_Overlay_Resources/imageFile.png <br/>
     *        Expected relative path: GIFT_Overlay_Resources/imageFile.png).
     * @param lowerLeftAGL the lower left AGL image coordinate. Can't be null.
     * @param upperRightAGL the upper right AGL image coordinate. Can't be null.
     */
    public MapTileProperties(String imageFilePath, MapTileCoordinate lowerLeftAGL, MapTileCoordinate upperRightAGL) {
        this();

        setImageFilePath(imageFilePath);
        setLowerLeftAGL(lowerLeftAGL);
        setUpperRightAGL(upperRightAGL);
        setZoomLevel(0);
        setTileCoordinate(new MapTileCoordinate(1, 1));
    }

    /**
     * Constructor.
     *
     * @param imageFilePath the path to the image file. The root should be the
     *        subfolder of the scenario folder. Can't be blank. <br/>
     *        For example: <br/>
     *        Image file location:
     *        Training.Apps/maps/Public/LandNavUnity/GIFT_Overlay_Resources/imageFile.png
     *        <br/>
     *        Expected relative path: GIFT_Overlay_Resources/imageFile.png).
     * @param zoomLevel the zoomLevel to which this {@link MapTileProperties}
     *        object belongs. Can't be negative.
     * @param tileCoordinate The coordinate of the tile within the given zoom
     *        level. Can't be null.
     */
    public MapTileProperties(String imageFilePath, int zoomLevel, MapTileCoordinate tileCoordinate) {
        this();

        setImageFilePath(imageFilePath);
        setZoomLevel(zoomLevel);
        setTileCoordinate(tileCoordinate);
    }

    /**
     * Retrieves the path to the image file. The root should be the subfolder of the scenario
     * folder. <br/>
     * For example: <br/>
     * Image file location:
     * Training.Apps/maps/Public/LandNavUnity/GIFT_Overlay_Resources/imageFile.png <br/>
     * Expected relative path: GIFT_Overlay_Resources/imageFile.png).
     *
     * @return the image file path. Guaranteed to be non-null and non-empty.
     */
    public String getImageFilePath() {
        return imageFilePath;
    }

    /**
     * Sets the path to the image file. The root should be the subfolder of the scenario folder.
     * Can't be blank. <br/>
     * For example: <br/>
     * Image file location:
     * Training.Apps/maps/Public/LandNavUnity/GIFT_Overlay_Resources/imageFile.png <br/>
     * Expected relative path: GIFT_Overlay_Resources/imageFile.png).
     *
     * @param imageFilePath the image file path to set. Can't be blank.
     */
    private void setImageFilePath(String imageFilePath) {
        if (StringUtils.isBlank(imageFilePath)) {
            throw new IllegalArgumentException("The parameter 'imageFilePath' cannot be blank.");
        }

        this.imageFilePath = imageFilePath;
    }

    /**
     * Retrieves the lower left AGL image coordinate
     *
     * @return the lower left AGL image coordinate. Can be null for
     *         {@link #zoomLevel} > 0.
     */
    public MapTileCoordinate getLowerLeftAGL() {
        return lowerLeftAGL;
    }

    /**
     * Sets the lower left AGL image coordinate
     *
     * @param lowerLeftAGL the lower left AGL image coordinate to set. Can't be null.
     */
    private void setLowerLeftAGL(MapTileCoordinate lowerLeftAGL) {
        if (lowerLeftAGL == null) {
            throw new IllegalArgumentException("The parameter 'lowerLeftAGL' cannot be null.");
        }

        this.lowerLeftAGL = lowerLeftAGL;
    }

    /**
     * Retrieves the upper right AGL image coordinate
     *
     * @return the upper right AGL image coordinate. Can be null for
     *         {@link #zoomLevel} > 0.
     */
    public MapTileCoordinate getUpperRightAGL() {
        return upperRightAGL;
    }

    /**
     * Sets the upper right AGL image coordinate
     *
     * @param upperRightAGL the upper right AGL image coordinate to set. Can't be null.
     */
    private void setUpperRightAGL(MapTileCoordinate upperRightAGL) {
        if (upperRightAGL == null) {
            throw new IllegalArgumentException("The parameter 'upperRightAGL' cannot be null.");
        }

        this.upperRightAGL = upperRightAGL;
    }

    /**
     * Getter for the {@link #zoomLevel} of this {@link MapTileProperties}
     * object.
     *
     * @return The value of {@link #zoomLevel}. Can't be negative.
     */
    public int getZoomLevel() {
        return zoomLevel;
    }

    /**
     * Setter for the {@link #zoomLevel} of this {@link MapTileProperties}
     * object.
     *
     * @param zoomLevel The new value of {@link #zoomLevel}. Can't be negative.
     */
    private void setZoomLevel(int zoomLevel) {
        if (zoomLevel < 0) {
            throw new IllegalArgumentException("The parameter 'zoomLevel' cannot be negative.");
        }

        this.zoomLevel = zoomLevel;
    }

    /**
     * Getter for the {@link #tileCoordinate} of this {@link MapTileProperties}
     * object.
     *
     * @return The value of {@link #tileCoordinate}. Can't be null.
     */
    public MapTileCoordinate getTileCoordinate() {
        return tileCoordinate;
    }

    /**
     * Setter for the {@link #tileCoordinate} of this {@link MapTileProperties}
     * object.
     *
     * @param tileCoordinate The new value of the {@link #tileCoordinate}. Can't
     *        be null.
     */
    private void setTileCoordinate(MapTileCoordinate tileCoordinate) {
        if (tileCoordinate == null) {
            throw new IllegalArgumentException("The parameter 'tileCoordinate' cannot be null.");
        }

        this.tileCoordinate = tileCoordinate;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[MapTileProperties: ");
        sb.append(IMAGE_FILE).append(" = ").append(getImageFilePath());
        sb.append(", ").append(LOWER_LEFT_AGL).append(" = ").append(getLowerLeftAGL());
        sb.append(", ").append(UPPER_RIGHT_AGL).append(" = ").append(getUpperRightAGL());
        sb.append(", ").append(ZOOM_LEVEL).append(" = ").append(getZoomLevel());
        sb.append(", ").append(TILE_COORDINATE).append(" = ").append(getTileCoordinate());
        sb.append("]");

        return sb.toString();
    }

    /**
     * The container for holding map tile coordinate points.
     *
     * @author sharrison
     */
    public static class MapTileCoordinate implements Serializable {

        /** default serial version number */
        private static final long serialVersionUID = 1L;

        /** The x value of the coordinate */
        private BigDecimal x;

        /** The y value of the coordinate */
        private BigDecimal y;

        /**
         * No argument constructor used to make the class GWT serializable.
         */
        private MapTileCoordinate() {
        }

        /**
         * Constructor.
         *
         * @param propertyCoordinateValue the string from the map tile property file defining the
         *        coordinate. Can't be blank. Must be two comma-separated numbers.
         */
        public MapTileCoordinate(String propertyCoordinateValue) {
            this();
            if (StringUtils.isBlank(propertyCoordinateValue)) {
                throw new IllegalArgumentException("The parameter 'propertyCoordinateValue' cannot be blank.");
            }

            String[] split = propertyCoordinateValue.split(Constants.COMMA);
            if (split.length != 2) {
                throw new IllegalArgumentException("The property coordinate value '" + propertyCoordinateValue
                        + "' is invalid. It must be two comma-separated numbers.");
            }

            /* Convert X value */
            try {
                this.x = new BigDecimal(split[0].trim());
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(
                        "The property X coordinate value '" + split[0] + "' is not a valid decimal.", nfe);
            }

            /* Convert Y value */
            try {
                this.y = new BigDecimal(split[1].trim());
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(
                        "The property X coordinate value '" + split[1] + "' is not a valid decimal.", nfe);
            }
        }

        /**
         * Constructor.
         *
         * @param x the x value of the coordinate
         * @param y the y value of the coordinate
         */
        public MapTileCoordinate(double x, double y) {
            this();
            this.x = new BigDecimal(x);
            this.y = new BigDecimal(y);
        }

        /**
         * Retrieves the x value of the coordinate.
         *
         *
         * @return the x value. The lowest possible value is 1.
         */
        public BigDecimal getX() {
            return x;
        }

        /**
         * Retrieves the y value of the coordinate
         *
         * @return the y value. The lowest possible value is 1.
         */
        public BigDecimal getY() {
            return y;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[MapTileCoordinate: ");
            sb.append("x = ").append(getX());
            sb.append(", y = ").append(getY());
            sb.append("]");

            return sb.toString();
        }
    }
}
