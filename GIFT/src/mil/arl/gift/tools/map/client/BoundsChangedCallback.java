/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A callback used to notify listeners when a map has changed the bounds of its viewing area
 * 
 * @author nroberts
 */
public interface BoundsChangedCallback {

    /**
     * Invokes logic when a map has changed the bounds of its viewing area
     * 
     * @param bounds the new map's new bounds
     */
    public void onBoundsChanged(Bounds bounds);
    
    /**
     * An object defining the northwest, northeast, southeast, and southwest corners that define a map's boundaries
     * 
     * @author nroberts
     */
    public static class Bounds {
        
        /** 
         * A list of vertices used to store a map's bounds. These vertices essentially define the rectangle that make's
         * up a map's bounds and are ordered as follows: northwest, northeast, southeast, southwest.
         */
        private List<AbstractMapCoordinate> boundVertices = new ArrayList<>();
        
        /** The center of the map's bounds */
        private AbstractMapCoordinate center;
        
        /** The map's zoom level */
        private int zoomLevel;
        
        /**
         * Creates a set of bounds based of the provided coordinates that define a map's northwest, northeast, southeast,
         * and southwest corners, respectively.
         * 
         * @param center a coordinate defining the center of the map's bounds. Cannot be null.
         * @param northWest a coordinate defining the northwest corner of a map's bounds. Cannot be null.
         * @param northEast a coordinate defining the northeast corner of a map's bounds. Cannot be null.
         * @param southEast a coordinate defining the southeast corner of a map's bounds. Cannot be null.
         * @param southWest a coordinate defining the southeast corner of a map's bounds. Cannot be null.
         * @param zoomLevel the map's zoom level
         */
        public Bounds(
                AbstractMapCoordinate center,
                AbstractMapCoordinate northWest,
                AbstractMapCoordinate northEast,
                AbstractMapCoordinate southEast,
                AbstractMapCoordinate southWest,
                int zoomLevel) {
            
            if(center == null) {
                throw new IllegalArgumentException("The location of the center point cannot be null.");
            }
            
            if(northWest == null) {
                throw new IllegalArgumentException("The location of the northwest bound cannot be null.");
            }
            
            if(northEast == null) {
                throw new IllegalArgumentException("The location of the northeast bound cannot be null.");
            }
            
            if(southEast == null) {
                throw new IllegalArgumentException("The location of the southeast bound cannot be null.");
            }
            
            if(southWest == null) {
                throw new IllegalArgumentException("The location of the southwest bound cannot be null.");
            }
            
            this.center = center;
            
            boundVertices.add(northWest);
            boundVertices.add(northEast);
            boundVertices.add(southEast);
            boundVertices.add(southWest);
            
            this.zoomLevel = zoomLevel;
        }

        /**
         * Gets the vertices that make up a map's bounds. The returned list orders the vertices as follows: northwest,
         * northeast, southeast, southwest.
         * 
         * @return the vertices that make up a map's bounds. Will not be null or empty and will not contain null elements.
         */
        public List<AbstractMapCoordinate> getVertices() {
            return boundVertices;
        }

        /**
         * Gets the center of the map's boundaries
         * 
         * @return the center of the map's boundaries. Will not be null.
         */
        public AbstractMapCoordinate getCenter() {
            return center;
        }

        /**
         * Gets the map's zoom level
         * 
         * @return the map's zoom level
         */
        public int getZoomLevel() {
            return zoomLevel;
        }
    }
}
