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
import com.google.gwt.maps.client.events.insertat.InsertAtEventFormatter;
import com.google.gwt.maps.client.events.insertat.InsertAtMapEvent;
import com.google.gwt.maps.client.events.insertat.InsertAtMapHandler;
import com.google.gwt.maps.client.events.removeat.RemoveAtEventFormatter;
import com.google.gwt.maps.client.events.removeat.RemoveAtMapEvent;
import com.google.gwt.maps.client.events.removeat.RemoveAtMapHandler;
import com.google.gwt.maps.client.events.setat.SetAtEventFormatter;
import com.google.gwt.maps.client.events.setat.SetAtMapEvent;
import com.google.gwt.maps.client.events.setat.SetAtMapHandler;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;

import mil.arl.gift.tools.map.client.draw.PolylineShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A polyline that can be drawn on the map rendered by {@link GoogleMapPanel}
 *
 * @author nroberts
 */
public class GoogleMapsPolylineShape extends PolylineShape<GoogleMapPanel> {

    /** The polyline object used to represent this point when it is drawn */
    private Polyline polyline;

    /**
     * The event handlers that will update the appropriate listeners when this shape is edited. This list should only be populated
     * while this shape is editable to save on memory.
     */
    private List<HandlerRegistration> editHandlers;

    /** The handler used to detect when this shape is clicked on */
    private HandlerRegistration clickHandler;

    /**
     * Creates a new polyline to be drawn using the given vertices on the map rendered by {@link GoogleMapPanel}
     *
     * @param map the map that will be used to create the polyline
     * @param vertices the vertices of the polyline to be drawn
     */
    public GoogleMapsPolylineShape(GoogleMapPanel map, List<AbstractMapCoordinate> vertices) {
        super(map, vertices);

        polyline = Polyline.newInstance(PolylineOptions.newInstance());
        polyline.setEditable(false);
    }

    /**
     * Creates a new polyline shape wrapping the given Google Maps polyline
     *
     * @param map the map that will be used to create the polyline
     * @param polyline the Google Maps polyline that this polyline shape should
     *        wrap
     */
    public GoogleMapsPolylineShape(GoogleMapPanel map, Polyline polyline) {
        super(map, GoogleMapPanel.toCoordinateList(polyline.getPath()));

        this.polyline = polyline;
    }

    /**
     * Converts a {@link List} of {@link AbstractMapCoordinate} objects to an
     * {@link MVCArray} of {@link LatLng} objects.
     *
     * @param coordinates The {@link List} of {@link AbstractMapCoordinate}
     *        objects to convert.
     * @return The {@link MVCArray} of {@link LatLng} objects that were produced
     *         during the conversion.
     */
    protected MVCArray<LatLng> coordinatesToLatLngs(List<AbstractMapCoordinate> coordinates) {
        return GoogleMapPanel.toLatLngArray(coordinates);
    }

    /**
     * Converts an {@link MVCArray} of {@link LatLng} objects to an
     * {@link MVCArray} of {@link LatLng} objects.
     *
     * @param latLngs The {@link MVCArray} of {@link LatLng} objects to convert.
     * @return The {@link List} of {@link AbstractMapCoordinate} objects that
     *         were produced during the conversion.
     */
    protected List<AbstractMapCoordinate> latLngsToCoordinates(MVCArray<LatLng> latLngs) {
        return GoogleMapPanel.toCoordinateList(latLngs);
    }

    @Override
    public void draw() {

        if(polyline.getMap() == null) {
            polyline.setMap(mapImpl.getMap());
        }

        boolean isEditable = editHandlers != null;

        MVCArray<LatLng> positions = coordinatesToLatLngs(getVertices());
        polyline.setPath(positions);

        PolylineOptions options = PolylineOptions.newInstance();
        options.setStrokeWeight(4);
        options.setStrokeColor(getColor());
        options.setClickable(clickHandler != null);
        polyline.setOptions(options);

        // For some baffling reason, changing the shape's properties while it is editable causes it's event handlers to
        // stop responding to edit events, so if the shape was editable, we need to reapply its edit handlers
        if(isEditable) {
            setEditable(true);
        }

        //TODO: Show name somehow

        onDraw();
    }

    @Override
    public void erase() {
        if (polyline != null) {
            polyline.setMap(null);
        }
    }

    @Override
    protected void setEditable(boolean editable) {

        removeEditHandlers();

        if(editable) {

            //add event handlers to detect when the author has modified this shape
            editHandlers = new ArrayList<>();

            //handle when this shape is dragged
            editHandlers.add(MapHandlerRegistration.addHandler(polyline, MapEventType.DRAGEND, new DragEndMapHandler() {

                @Override
                public void onEvent(DragEndMapEvent event) {
                    handleEdit();
                }
            }, new DragEndEventFormatter()));

            //handle when vertices are added to this shape
            editHandlers.add(MapHandlerRegistration.addHandler(polyline.getPath(), MapEventType.INSERT_AT, new InsertAtMapHandler() {

                @Override
                public void onEvent(InsertAtMapEvent event) {
                    handleEdit();
                }
            }, new InsertAtEventFormatter()));

            //handle when vertices are removed from this shape
            editHandlers.add(MapHandlerRegistration.addHandler(polyline.getPath(), MapEventType.REMOVE_AT, new RemoveAtMapHandler() {

                @Override
                public void onEvent(RemoveAtMapEvent event) {
                    handleEdit();
                }
            }, new RemoveAtEventFormatter()));

            //handle when the positions of this shape's vertices are changed
            editHandlers.add(MapHandlerRegistration.addHandler(polyline.getPath(), MapEventType.SET_AT, new SetAtMapHandler() {

                @Override
                public void onEvent(SetAtMapEvent event) {
                    handleEdit();
                }
            }, new SetAtEventFormatter()));
        }

        polyline.setEditable(editable);
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

        setVertices(latLngsToCoordinates(polyline.getPath()));

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

            clickHandler = polyline.addClickHandler(new ClickMapHandler() {

                @Override
                public void onEvent(ClickMapEvent event) {
                    onClick();
                }
            });
        }
    }

    @Override
    public boolean isDrawn() {
        return polyline.getMap() != null;
    }
}
