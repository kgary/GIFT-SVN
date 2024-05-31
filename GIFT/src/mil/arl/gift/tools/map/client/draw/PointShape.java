/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client.draw;

import mil.arl.gift.tools.map.client.AbstractMapPanel;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A shape that defines a single point at a specific location
 *
 * @author nroberts
 * @param <T> The type of panel that the point exists within
 */
public abstract class PointShape<T extends AbstractMapPanel> extends AbstractMapShape<T> {

    /** This point's location */
    private AbstractMapCoordinate location;

    /** The icon that should be used by this point */
    private String icon;
    
    /**
     * The z-index that this point should be rendered with. If the map supports z-indexing, points 
     * with higher z-indexes will be rendered on top of points with lower z-indexes.
     */
    private Integer zIndex = null;

    /**
     * Defines new a point shape at the given location
     *
     * @param coordinate the location of this point. Cannot be null.
     */
    public PointShape(T mapImpl, AbstractMapCoordinate coordinate) {
       super(mapImpl);
       setLocation(coordinate);
    }

    /**
     * Gets this point's location
     *
     * @return the location. Cannot be null.
     */
    public AbstractMapCoordinate getLocation() {
        return location;
    }

    /**
     * Sets this point's location
     *
     * @param location the location. Cannot be null.
     */
    public void setLocation(AbstractMapCoordinate location) {

        if(location == null) {
            throw new IllegalArgumentException("This point's location cannot be null.");
        }

        this.location = location;
    }
    
    /**
     * Sets the icon that should be used by this point
     * 
     * @param icon the icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    /**
     * Gets the icon that should be used by this point
     * 
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Gets the z-index that this point should be rendered with. If the map supports z-indexing, points 
     * with higher z-indexes will be rendered on top of points with lower z-indexes.
     * 
     * @return this point's z-index
     */
    public Integer getZIndex() {
        return zIndex;
    }

    /**
     * Sets the z-index that this point should be rendered with. If the map supports z-indexing, points 
     * with higher z-indexes will be rendered on top of points with lower z-indexes.
     * 
     * @param zIndex the z-index to give this point
     */
    public void setZIndex(Integer zIndex) {
        this.zIndex = zIndex;
    }
    

    @Override
    public String toString() {
        return new StringBuilder("[PointShape: ")
                .append("location = ").append(location)
                .append(']').toString();
    }
    
}
