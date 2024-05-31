/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AnimateWrapper;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.AbstractMapDataPoint.MappableData;
import mil.arl.gift.tools.map.client.AbstractMapPanel;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * An object used to render data points to the Game Master map
 * 
 * @author nroberts
 */
public class MapShape {
    
    /** 
     * The name of the attribute from which a map shape's displayed name text is derived. An accompanying CSS styling in
     * gift_dashboard.css must read from the same attribute in order to properly display this text.
     */
    private static final String ENTITY_NAME_ATTR = "entity-name";
    
    /** 
     * The name of the attribute from which a map shape's displayed label text is derived. An accompanying CSS styling in
     * gift_dashboard.css must read from the same attribute in order to properly display this text.
     */
    private static final String ENTITY_DATA_LABEL_ATTR = "entity-data-label";
    
    /**
     * The name of the CSS class containing the style rules used to distinguish the elements used to render map shapes. 
     * A CSS class with the same name must exist in gift_dashboard.css for the styling to be applied properly.
     */
    public static final String ENTITY_STYLE = "mapEntity";
    
    /**
     * The name of the CSS class containing the style rules used to distinguish when a map shape's displayed data point 
     * is selected. A CSS class with the same name must exist in gift_dashboard.css for the styling to be applied properly.
     */
    private static final String SELECTED_ENTITY_STYLE = "mapEntitySelected";
    
    /** The unique ID to assign to the next map shape */
    private static int nextId = 0;
    
    /** The unique ID used to identify this map shape. Used to locate DOM elements related to this shape. */
    private int id = nextId != Integer.MAX_VALUE 
            ? nextId++ 
            : Integer.MIN_VALUE;
    
    /** The point shape used to render the entity to the map */
    private PointShape<?> mapPoint;

    /** The point shape used to render the entity to the minimap */
    private PointShape<?> minimapPoint;
    
    /** The main map where this shape should be primarily rendered */
    private AbstractMapPanel map;
    
    /** The minimap where this shape should also be rendered*/
    private AbstractMapPanel minimap;

    /**
     * Creates a new map shape that represents the given data and renders it to the given maps
     * 
     * @param data the mappable data to be rendered. Cannot be null and must provide a location.
     * @param map the main map that the data should be rendered to. Cannot be null.
     * @param minimap the secondary minimap that the data should also be rendered to. Cannot be null.
     */
    public MapShape(MappableData data, AbstractMapPanel map, 
            AbstractMapPanel minimap){

        if(data == null) {
            throw new IllegalArgumentException("The data that a map shape should display cannot be null.");
        }
        
        AbstractMapCoordinate location = data.getLocation();
        
        if(location == null) {
            throw new IllegalArgumentException("The location that a map shape should be placed at cannot be null.");
            
        }
        if (map == null) {
            throw new IllegalArgumentException("An entity shape's map cannot be null.");
            
        }
        if (minimap == null) {
            throw new IllegalArgumentException("An entity shape's minimap cannot be null.");
        }
        
        this.map = map;
        this.mapPoint = map.createPoint(location);
        
        this.minimap = minimap;
        this.minimapPoint = minimap.createPoint(location);
    }

    /**
     * Gets the point shape used to render the data to the map
     * 
     * @return the map point. Will not be null.
     */
    public PointShape<?> getMapPoint() {
        return mapPoint;
    }

    /**
     * Draws points on the appropriate maps that reflect the given mappable data
     * 
     * @param data the mappable data to display. Cannot be null.
     */
    public void draw(final MappableData data) {
        
        if(data == null) {
            throw new IllegalArgumentException("The data to draw cannot be null");
        }
        
        //update the location of the drawn data
        AbstractMapCoordinate location = data.getLocation();
        
        mapPoint.setLocation(location);
        minimapPoint.setLocation(location);
        
        //update the z-index of the drawn data to match its priority
        int priority = data.getPriority();
        
        mapPoint.setZIndex(priority);
        minimapPoint.setZIndex(priority);
        
        //update the URL of the drawn data's icon and add a special suffix unique to this shape
        String iconUrl = data.getIconUrl() + getUniqueUrlTag();
        
        mapPoint.setIcon(iconUrl);
        minimapPoint.setIcon(iconUrl);
        
        // draw the point based on its current state
        mapPoint.draw();
        minimapPoint.draw();
        
        String name = data.getName();

        //modify the element drawn to the main map to reflect the data's current selection state
        Element mapElement = getMapElement(map.getElement());
        if(mapElement != null) {
            
            mapElement.addClassName(ENTITY_STYLE);
            
            //display a name next to the shape, if needed
            if(name != null) {
                mapElement.setAttribute(ENTITY_NAME_ATTR, name);
                
            } else {
                mapElement.removeAttribute(ENTITY_NAME_ATTR);
            }
            
            //visually select this shape, if needed
            if(data.isSelected()) {
                
                mapElement.addClassName(SELECTED_ENTITY_STYLE);
                
                String dataLabel = data.getDataLabel();
                
                //display a data label for this shape, if needed
                if(StringUtils.isNotBlank(dataLabel)) {
                    mapElement.setAttribute(ENTITY_DATA_LABEL_ATTR, dataLabel);
                    
                } else {
                    mapElement.removeAttribute(ENTITY_DATA_LABEL_ATTR);
                }
                
            } else {
                
                mapElement.removeClassName(SELECTED_ENTITY_STYLE);
                mapElement.removeAttribute(ENTITY_DATA_LABEL_ATTR);
            }
        }
        
        //modify the element drawn to the minimap to reflect the current selection state
        mapElement = getMapElement(minimap.getElement());
        if(mapElement != null) {
            
            mapElement.addClassName(ENTITY_STYLE);
            
            //display a name next to the shape, if needed
            if(name != null) {
                mapElement.setAttribute(ENTITY_NAME_ATTR, name);
                
            } else {
                mapElement.removeAttribute(ENTITY_NAME_ATTR);
            }
            
            //visually select this shape, if needed
            if(data.isSelected()) {
                mapElement.addClassName(SELECTED_ENTITY_STYLE);
                
            } else {
                mapElement.removeClassName(SELECTED_ENTITY_STYLE);
            }
        }
    }

    /**
     * Erases this entity from all appropriate maps
     */
    public void erase() {

        mapPoint.erase();
        minimapPoint.erase();
    }
    
    /**
     * Gets the unique tag that is appended to the URLs of this shape's icons in order
     * to detect which elements in the document are used to render this shape.
     * 
     * @return the unique icon URL tag that is associated with this shape. Will not be null.
     */
    public String getUniqueUrlTag() {
        
        //NOTE: This string must be safe for both icon image URL's and CSS selectors
        return "#MAPICON[" + id + "]";
    }
    
    /**
     * Gets all of the {@link Element}s that represent this shape and are currently drawn to a map
     * in the document. Specifically, this method will return the ultimate parent elements that are used to
     * render this shape onto each of the maps it is currently drawn to.
     * <br/><br/>
     * NOTE: This method uses CSS selectors in order to uniquely identify which elements in the document
     * are being used to represent this shape. As a result, if an element is not currently attached to
     * the document (i.e. it is currently erased), then it will not be included in the returned
     * list of elements.
     * 
     * @return the list of elements that represent this shape on a map in the document. Will not be null,
     * but can be empty if no map elements are currently rendered for this shape
     */
    public List<Element> getMapElements() {
        
        List<Element> mapElements = new ArrayList<>();
        
        //get the element drawn to the main map
        Element mapElement = getMapElement(map.getElement());
        if(mapElement != null) {
            mapElements.add(mapElement);
        }
        
        //get the element drawn to the minimap
        mapElement = getMapElement(minimap.getElement());
        if(mapElement != null) {
            mapElements.add(mapElement);
        }
        
        return mapElements;
    }
    
    /**
     * Gets the first {@link Element}s that represents this shape and are currently drawn to a map
     * within the given container element. Specifically, this method will return the ultimate parent element 
     * that is used to render this element onto each map inside the given element.
     * <br/><br/>
     * NOTE: This method uses CSS selectors in order to uniquely identify which element in the container
     * is being used to represent this shape. As a result, if an element is not currently attached to
     * the container (i.e. it is currently erased), then null will be returned instead.
     * 
     * @param containerElement the element containing the map entity being searched for. Cannot be null.
     * @return the element that represents this shape on a map in the given container. Will be null 
     * if no map element is currently rendered for this shape within the container.
     */
    public Element getMapElement(Element containerElement) {
        
        if(containerElement == null) {
            throw new IllegalArgumentException("A container element must be provided in order to get an entity's map element");
        }
        
        //search the given container element for a map icon corresponding to this entity by checking to see which
        //images in said container have URLs ending with this entity's unique URL tag
        Node imageNode = JsniUtility.querySelector(containerElement, "img[src*=\"" + getUniqueUrlTag() +"\"]");
        
        if(imageNode == null) {
            return null;
        }
        
        //the actual map element is the parent of the map icon, so return the parent element
        return imageNode.getParentElement();
    }
    
    /**
     * Plays a pulsing animation on all of the elements that are currently drawn onto a map for this shape.
     * Will do nothing if no elements are currently drawn onto a map for this shape.
     */
    public void playPulseAnimation() {
        
        for(Element mapElement : getMapElements()) {
            
            //animate each element that currently renders this entity to a map 
            AnimateWrapper wrapper = new AnimateWrapper(mapElement);
            
            String iconAnimationName = Animate.animate(wrapper, Animation.BOUNCE_IN);
            Animate.removeAnimationOnEnd(wrapper, iconAnimationName);
        }
    }
}
