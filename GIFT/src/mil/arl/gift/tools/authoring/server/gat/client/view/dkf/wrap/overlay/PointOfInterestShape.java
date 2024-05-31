/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay;

import generated.dkf.Point;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.PlacesOfInterestOverlay;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.WrapPanel;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A {@link AbstractPlaceOfInterestShape} that associates an {@link Point} place of interest with a {@link PointShape} map shape
 * 
 * @author nroberts
 */
public class PointOfInterestShape extends AbstractPlaceOfInterestShape<Point, PointShape<?>> {

    /**
     * Creates a new place shape for the given place of interest. An appropriate map shape will be 
     * created based on the data provided by the given place of interest.
     * 
     * @param placeOfInterest the place of interest to create a place shape for. Cannot be null.
     */
    public PointOfInterestShape(Point placeOfInterest) {
        super(placeOfInterest);
    }
    
    /**
     * Creates a new place shape for the given map shape. An appropriate place of interest will be created based on 
     * the data provided by the given map shape.
     * 
     * @param shape the map shape to create a place shape for. Cannot be null.
     */
    public PointOfInterestShape(PointShape<?> shape) {
        super(shape);
    }
    
    @Override
    protected PointShape<?> createMapShape(Point point) {
        
        AbstractMapCoordinate location = PlacesOfInterestOverlay.toMapCoordinate(point.getCoordinate());
        
        PointShape<?> shape = WrapPanel.getInstance().getCurrentMap().createPoint(location);
        shape.setName(point.getName());
        shape.setColor(point.getColorHexRGBA());
        
        return shape;
    }

    @Override
    protected Point createPlaceOfInterest(PointShape<?> shape) {
        Point point = new Point();
        setPlaceOfInterest(point);
        
        updatePlaceOfInterest();
        
        return point;
    }

    @Override
    public void updatePlaceOfInterest() {
        
        Point point = getPlaceOfInterest();
        PointShape<?> shape = getMapShape();
        
        point.setName(shape.getName());
        point.setColorHexRGBA(shape.getColor());
        point.setCoordinate(PlacesOfInterestOverlay.toSchemaCoordinate(shape.getLocation()));
    }

    @Override
    public void updateMapShape() {
        
        Point point = getPlaceOfInterest();
        PointShape<?> shape = getMapShape();
        
        AbstractMapCoordinate location = PlacesOfInterestOverlay.toMapCoordinate(point.getCoordinate());
        
        shape.setLocation(location);
        shape.setName(point.getName());
        shape.setColor(point.getColorHexRGBA());
    }
}
