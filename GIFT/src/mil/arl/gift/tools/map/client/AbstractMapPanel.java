/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client;

import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SimplePanel;

import mil.arl.gift.tools.map.client.BoundsChangedCallback.Bounds;
import mil.arl.gift.tools.map.client.draw.AbstractMapShape;
import mil.arl.gift.tools.map.client.draw.ManualDrawingCompleteCallback;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.client.draw.PolygonShape;
import mil.arl.gift.tools.map.client.draw.PolylineShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A panel that renders a map for the author to interact with, often in conjunction with overlay controls. This class maintains 
 * the basic controls and functionality that are common to all map types in the GAT.
 * 
 * @author nroberts
 */
public abstract class AbstractMapPanel extends SimplePanel{
    
    /** Whether or not this map has loaded and is ready to be interacted with and drawn to */
    private boolean isReady = false;
    
    /** A callback used to notify a listener when the author finishes manually drawing a shape */
    private ManualDrawingCompleteCallback manualDrawingCompleteCallback;
    
    /** A callback used to notify a listener when the north, south, east, or west bounds of the map's viewing area have changed */
    private BoundsChangedCallback boundsChangedCallback;
    
    /** A callback used to notify a listener when the map is clicked */
    private MapClickedCallback mapClickedCallback;

    /** A command to be invoked when this panel's map is ready to handle operations. Can be null. */
    private Command onMapReady;
    
    /**
     * Creates a new empty map panel
     */
    protected AbstractMapPanel() {
        this(null);
    }
    
    /**
     * Creates a new empty map panel the executes the given command when the map is ready
     * 
     * @param onMapReady a command to be invoked when this panel's map is ready to handle operations. Can be null.
     */
    protected AbstractMapPanel(Command onMapReady) {
        super();
        
        this.onMapReady = onMapReady;
    }
    
    /**
     * Zooms in on the rendered map
     * 
     * @return true if the user can continue zooming in, false if the user is zoomed in as deep as possible
     */
    public abstract boolean zoomIn();
    
    /**
     * Zooms out on the rendered map
     * 
     * @return true if the user can continue zooming out, false if the user is zoomed out as far as possible
     */
    public abstract boolean zoomOut();
    
    /**
     * Notify the appropriate listeners when this map is ready to be interacted with
     */
    protected void notifyMapReady() {
        isReady = true;
        
        if(onMapReady != null) {
            onMapReady.execute();
        }
    }
    
    /**
     * Gets whether or not this map is ready to be interacted with
     * 
     * @return whether this map is ready
     */
    public boolean isReady() {
        return isReady;
    }
    
    /**
     * Creates a point at the given location. Note that this point will not be rendered until explicitly called to do so.
     * 
     * @param location the location to create the point at
     * @return the created point
     */
    public abstract PointShape<?> createPoint(AbstractMapCoordinate location);
    
    /**
     * Creates a polyline with the given vertices. Note that this polyline will not be rendered until explicitly called to do so.
     * 
     * @param vertices the vertices that the polyline should follow
     * @return the created polyline
     */
    public abstract PolylineShape<?> createPolyline(List<AbstractMapCoordinate> vertices);
    
    /**
     * Creates a polygon comprised of the given vertices. Note that this polygon will not be rendered until explicitly called to do so.
     * 
     * @param vertices the vertices that should comprise the polygon
     * @return the created polygon
     */
    public abstract PolygonShape<?> createPolygon(List<AbstractMapCoordinate> vertices);
    
    /**
     * Sets which control mode should be used to handle mouse interaction with the map
     * 
     * @param mode the control mode to use
     */
    public abstract void setControlMode(MapControlModeEnum mode);
    
    /**
     * Sets the callback that should be used to handle when the author finishes manually drawing a shape
     * 
     * @param callback the callback to handle when drawing is complete
     */
    public void setManualDrawingCompleteHandler(ManualDrawingCompleteCallback callback) {
        this.manualDrawingCompleteCallback = callback;
    }
    
    /**
     * Notifies the listener registered with {@link #setManualDrawingCompleteHandler(ManualDrawingCompleteCallback)} when the
     * author has finished manually drawing the given shape
     * 
     * @param drawnShape the shape that the author drew
     */
    protected void onManualDrawingComplete(AbstractMapShape<?> drawnShape) {
        if(this.manualDrawingCompleteCallback != null) {
            this.manualDrawingCompleteCallback.onDrawingComplete(drawnShape);
        }
    }
    
    /**
     * Sets the callback that should be used to handle when the north, south, east, or west bounds of the map's viewing area have changed
     * 
     * @param callback the callback to handle when the bounds of the map's viewing area have changed
     */
    public void setBoundsChangedHandler(BoundsChangedCallback callback) {
        this.boundsChangedCallback = callback;
    }
    
    /**
     * Notifies the listener registered with {@link #setBoundsChangedHandler(BoundsChangedCallback)} when the
     * map has changed the bounds of its viewing area
     * 
     * @param bounds the bounds of the map's current viewing area
     */
    protected void onBoundsChanged(Bounds bounds) {
        if(this.boundsChangedCallback != null) {
            this.boundsChangedCallback.onBoundsChanged(bounds);
        }
    }
    
    /**
     * Sets the callback that should be used to handle when the map is clicked
     * 
     * @param callback the callback to handle when the map is clicked
     */
    public void setMapClickedHandler(MapClickedCallback callback) {
        this.mapClickedCallback = callback;
    }
    
    /**
     * Notifies the listener registered with {@link #setMapClickedHandler(MapClickedCallback)} when the
     * map has been clicked
     * 
     * @param position the position of the mouse on the map when it was clicked
     */
    protected void onMapClicked(AbstractMapCoordinate position) {
        if(this.mapClickedCallback != null) {
            this.mapClickedCallback.onClick(position);
        }
    }
    
    /**
     * Centers this map around the given shapes and optionally modifies the zoom level to fit them as best as possible
     * 
     * @param zoomToFit whether to modify the zoom level to fit the given shapes
     * @param shapes the shapes to center the map around. Cannot be null or empty.
     */
    abstract public void centerView(boolean zoomToFit, AbstractMapShape<?>... shapes);
    
    /**
     * Pans the center of the map to the given location and zooms it to the given level
     * 
     * @param center the location that the map should be centered on. Cannot be null.
     * @param zoomLevel the zoom level to zoom the map to. If null, the map's zoom level will not be changed.
     */
    abstract public void panTo(AbstractMapCoordinate center, Integer zoomLevel);
    
    /**
     * Gets the location of the given locations' centroid (i.e. the center of mass) 
     * 
     * @param locations the locations to get the centroid for. Cannot be null or empty.
     * @return the calculated centroid location. Will not be null.
     */
    abstract public AbstractMapCoordinate getCentroid(AbstractMapCoordinate... locations);
}
