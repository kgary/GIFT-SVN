/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.logging.Logger;

import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.EntityDisplaySettingsPanel.RenderableAttribute;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A point of data that can be used to obtain information that can be rendered to a map
 * 
 * @author nroberts
 */
public abstract class AbstractMapDataPoint {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AbstractMapDataPoint.class.getName());
    
    /** The z-index to render playable entities with */
    public static final int PLAYABLE_ENTITY_Z_INDEX = 1; //render above non-learner entities
    
    /** The z-index to render learner entities with */
    public static final int LEARNER_ENTITY_Z_INDEX = PLAYABLE_ENTITY_Z_INDEX + 1; //render above playable entities
    
    /** The z-index to render selected entities with */ //render above learner entities
    public static final int SELECTED_ENTITY_Z_INDEX = LEARNER_ENTITY_Z_INDEX + 1;
    
    /** The z-index to render fire line with */ //render above selected entities
    public static final int FIRE_LINE_Z_INDEX = SELECTED_ENTITY_Z_INDEX + 1;
    
    /** The z-index to render detonations with */ //render above fire line
    public static final int DETONATION_Z_INDEX = FIRE_LINE_Z_INDEX + 1;
    
    /** Whether this is currently selected (i.e. clicked on it and a label might be showing)*/
    private boolean isSelected = false;
    
    /** whether the selection mode (isSelected) should be toggled based on repeated selecting (i.e. clicking on it) */
    private boolean toggleSelected = false;    
    
    /**
     * Return whether the selection mode (isSelected) should be toggled based on repeated selecting (i.e. clicking on it)
     * @return false is the default
     */
    public boolean isToggleSelected() {
        return toggleSelected;
    }

    /**
     * Set whether the selection mode (isSelected) should be toggled based on repeated selecting (i.e. clicking on it)
     * @param toggleSelected the toggle mode to set.
     */
    public void setToggleSelected(boolean toggleSelected) {
        this.toggleSelected = toggleSelected;
    }

    /**
     * Gets whether or not the user has selected this entity (i.e. clicked on it)
     * 
     * @param selected whether this entity is selected
     */
    public void setSelected(boolean selected) {
        
        if(toggleSelected){
            
            if(this.isSelected == selected){
                // the value being set is the current value, toggle it to a different value
                selected = !selected;
            }
        }
        logger.info("setting isSelected to "+selected);
        this.isSelected = selected;
    }
    
    /**
     * Gets whether or not the user has selected this entity
     * 
     * @return whether this entity is selected
     */
    public boolean isSelected() {
        return isSelected;
    }
    
    /**
     * Draws a shape to all relevant maps based on the mappable data currently stored in this data point.
     */
    abstract public void draw();
    
    /**
     * Gets the data needed to render this data point to a map
     * 
     * @return the mappable data. Cannot be null.
     */
    abstract public MappableData getMappableData();

    /**
     * Gets renderable data from this entity corresponding to the given attribute. Can be null, if this entity
     * does not contain any data for the given attribute.
     * 
     * @param attribute the renderable attribute for which data is being retrieved from this element
     * @return the appropriate data corresponding to the given attribute
     */
    abstract public String getAttributeData(RenderableAttribute attribute);

    /**
     * Gets the point shape used to render the entity to the map
     * 
     * @return the map point. Can be null.
     */
    public abstract PointShape<?> getMapPoint();
    
    /**
     * Information that can be rendered to a map
     * 
     * @author nroberts
     */
    public static class MappableData {
        
        /** The location where rendering should take place on the map */
        private AbstractMapCoordinate location;
        
        /** The name that should be shown next to the rendered data */
        private String name;
        
        /** The icon that should be rendered */
        private String iconUrl;
        
        /** The priority that should be used when rendering */
        private int priority = 0;
        
        /** The label that should be shown when rendering */
        private String dataLabel;
        
        /** Whether or not the rendered data is selected */
        private boolean selected = false;
        
        /**
         * Creates a new set of renderable data
         * 
         * @param location the location where rendering should take place on the map. Can be null.
         * @param name the name that should be shown next to the rendered data. Can be null.
         * @param iconUrl the icon that should be rendered. Can be null.
         * @param priority the priority that should be used when rendering
         * @param dataLabel the label that should be shown when rendering. Can be null.
         * @param selected whether or not the rendered data is selected
         */
        public MappableData(AbstractMapCoordinate location, String name, String iconUrl, 
                int priority, String dataLabel, boolean selected) {
            
            this.location = location;
            this.name = name;
            this.iconUrl = iconUrl;
            this.priority = priority;
            this.dataLabel = dataLabel;
            this.selected = selected;
        }
        
        /**
         * Gets the location where rendering should take place on the map
         * 
         * @return the render location. Can be null.
         */
        public AbstractMapCoordinate getLocation() {
            return location;
        }

        /**
         * Gets the icon that should be rendered
         * 
         * @return the icon to render. Can be null.
         */
        public String getIconUrl() {
            return iconUrl;
        }
        
        /**
         * Gets the priority that should be used when rendering
         * 
         * @return the render priority
         */
        public int getPriority() {
            return priority;
        }
        
        /**
         * Gets the label that should be shown when rendering
         * 
         * @return the data label. Can be null.
         */
        public String getDataLabel() {
            return dataLabel;
        }
        
        /**
         * Gets whether or not the rendered data is selected
         * 
         * @return whether the rendered data is selected
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Gets the name that should be shown next to the rendered data
         * 
         * @return the name. Can be null if no name should be shown.
         */
        public String getName() {
            return name;
        }
    }
}
