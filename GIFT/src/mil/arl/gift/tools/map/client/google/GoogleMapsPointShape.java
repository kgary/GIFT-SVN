/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client.google;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.MapEventType;
import com.google.gwt.maps.client.events.MapHandlerRegistration;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.dragend.DragEndEventFormatter;
import com.google.gwt.maps.client.events.dragend.DragEndMapEvent;
import com.google.gwt.maps.client.events.dragend.DragEndMapHandler;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerImage;
import com.google.gwt.maps.client.overlays.MarkerOptions;

import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A point that can be drawn on the map rendered by {@link GoogleMapPanel}
 *
 * @author nroberts
 */
public class GoogleMapsPointShape extends PointShape<GoogleMapPanel> {

    /** The marker object used to represent this point when it is drawn */
    private Marker marker;

    /**
     * The event handlers that will update the appropriate listeners when this
     * shape is edited. This list should only be populated while this shape is
     * editable to save on memory.
     */
    private List<HandlerRegistration> editHandlers;

    /** The event handler that will notify the appropriate listener when this shape is clicked on */
    private HandlerRegistration clickHandler;

    /**
     * Creates a new point to be drawn at the given location on the map rendered
     * by {@link GoogleMapPanel}
     *
     * @param map the map that will be used to create the point
     * @param location the location to draw the point at
     */
    public GoogleMapsPointShape(GoogleMapPanel map, AbstractMapCoordinate location) {
        super(map, location);

        setMarker(Marker.newInstance(MarkerOptions.newInstance()));
    }

    /**
     * Creates a new point shape wrapping the given Google Maps marker
     *
     * @param map the map that will be used to create the point
     * @param marker the Google Maps marker that this point shape should wrap.
     *        Can't be null.
     */
    public GoogleMapsPointShape(GoogleMapPanel map, Marker marker) {
        super(map, GoogleMapPanel.latLngToCoordinate(marker.getPosition()));

        setMarker(marker);
    }

    /**
     * Getter for the Google Maps marker.
     *
     * @return The value of {@link #marker}.
     */
    private Marker getMarker() {
        return marker;
    }

    /**
     * Setter for the Google Maps marker.
     *
     * @param marker The new value of {@link #marker}.
     */
    private void setMarker(Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("The parameter 'marker' cannot be null.");
        }

        this.marker = marker;
    }

    @Override
    public void draw() {

        Marker marker = getMarker();
        if(marker.getMap() == null) {
            marker.setMap(mapImpl.getMap());
        }

        //gather all the attributes to be drawn in one set of options to avoid redundant redraws
        MarkerOptions drawOptions = MarkerOptions.newInstance();

        if(getIcon() == null) {

            //if no icon has been provided, use the default icon
            drawOptions.setIcon(generateSymbolIcon(marker.getDraggable(), getColor()));

        } else {

            //if an icon has been provided, use it
            drawOptions.setIcon(getIcon());
        }

        drawOptions.setPosition(mapImpl.convertCoordinateToLatLng(getLocation()));
        drawOptions.setTitle(getName());
        drawOptions.setClickable(clickHandler != null);

        if(getZIndex() != null) {
            drawOptions.setZindex(getZIndex());
        }

        //draw the marker with all the gathered attributes
        marker.setOptions(drawOptions);

        onDraw();
    }

    /**
     * Converts an {@link AbstractMapCoordinate} object to a {@link LatLng}
     * object.
     *
     * @param coordinate The {@link AbstractMapCoordinate} to convert.
     * @return The {@link LatLng} that is created by the conversion.
     */
    protected LatLng coordinateToLatLng(AbstractMapCoordinate coordinate) {
        return GoogleMapPanel.coordinateToLatLng(coordinate);
    }

    /**
     * Converts an {@link LatLng} object to a {@link AbstractMapCoordinate}
     * object.
     *
     * @param latLng The {@link LatLng} to convert.
     * @return The {@link AbstractMapCoordinate} that is created by the
     *         conversion.
     */
    protected AbstractMapCoordinate latLngToCoordinate(LatLng latLng) {
        return GoogleMapPanel.latLngToCoordinate(latLng);
    }

    @Override
    public void erase() {
        getMarker().clear();
        }

    /**
     * Generates a symbol icon (i.e. not an image) with the given color and draggable state
     * <br/><br/>
     * NOTE: Even though a {@link MarkerImage} Java object is returned by this method, the underlying JavaScript
     * object is actually a Google Maps <a href='https://developers.google.com/maps/documentation/javascript/reference/marker#Symbol'>
     * Symbol</a> and should be treated as such. This method's signature only defines MarkerImage as the return type because that
     * is what GWT Maps's {@link Marker} class expects.
     *
     * @param draggable whether the created icon should indicate that its marker is draggable
     * @param color the color to give the created icon
     * @return a symbol icon reflecting the provided parameters
     */
    private static native MarkerImage generateSymbolIcon(boolean draggable, String color)/*-{
		return {
			path : 'M 0,0 L -6.928,-12 A 8 8 1 1 1 6.928,-12 L 0,0 z',
			fillColor : color,
			fillOpacity : 1,
			strokeColor : '#000',
			strokeWeight : draggable ? 3 : 1, //use a thicker stroke if the marker is draggable
			scale : 1
		};
    }-*/;

    @Override
    protected void setEditable(boolean editable) {

        removeEditHandlers();

        final Marker marker = getMarker();
        if(editable) {

            //add event handlers to detect when the author has modified this shape
            editHandlers = new ArrayList<>();

            //handle when this shape is dragged
            editHandlers.add(MapHandlerRegistration.addHandler(marker, MapEventType.DRAGEND, new DragEndMapHandler() {

                @Override
                public void onEvent(DragEndMapEvent event) {
                    handleEdit();
                }
            }, new DragEndEventFormatter()));
        }

        marker.setDraggable(editable);

        if(getIcon() == null) {

            //if the default icon is being used, update its appearance to indicate it is draggable
            marker.setIcon(generateSymbolIcon(editable, getColor()));
        }
    }

    /**
     * Removes any event handlers that handle when this shape is edited
     */
    private void removeEditHandlers() {

        if(editHandlers != null) {

            //remove event handlers, since the shape is no longer editable
            for(HandlerRegistration handler : editHandlers) {
                handler.removeHandler();
            }

            editHandlers = null;
        }
    }

    /**
     * Handles when the author edits the drawn instance of this shape and update's this shape's values to match the edited drawing
     */
    private void handleEdit() {

        setLocation(latLngToCoordinate(getMarker().getPosition()));

        onEdit();
        onDraw();
    }

    @Override
    protected void setClickable(boolean clickable) {

        if(clickHandler != null) {
            clickHandler.removeHandler();
            clickHandler = null;
        }

        if(clickable) {

            clickHandler = getMarker().addClickHandler(new ClickMapHandler() {

                @Override
                public void onEvent(ClickMapEvent event) {
                    onClick();
                }
            });
        }
    }

    @Override
    public boolean isDrawn() {
        return getMarker().getMap() != null;
    }
}
