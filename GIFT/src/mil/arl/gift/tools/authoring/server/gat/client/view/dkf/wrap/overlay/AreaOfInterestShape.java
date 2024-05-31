/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay;

import java.util.ArrayList;
import java.util.List;

import generated.dkf.Area;
import generated.dkf.Coordinate;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.PlacesOfInterestOverlay;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.WrapPanel;
import mil.arl.gift.tools.map.client.draw.PolygonShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A {@link AbstractPlaceOfInterestShape} that associates an {@link Area} place of interest with a {@link PolygonShape} map shape
 * 
 * @author nroberts
 */
public class AreaOfInterestShape extends AbstractPlaceOfInterestShape<Area, PolygonShape<?>> {

    /**
     * Creates a new place shape for the given place of interest. An appropriate map shape will be 
     * created based on the data provided by the given place of interest.
     * 
     * @param placeOfInterest the place of interest to create a place shape for. Cannot be null.
     */
    public AreaOfInterestShape(Area placeOfInterest) {
        super(placeOfInterest);
    }
    
    /**
     * Creates a new place shape for the given map shape. An appropriate place of interest will be created based on 
     * the data provided by the given map shape.
     * 
     * @param shape the map shape to create a place shape for. Cannot be null.
     */
    public AreaOfInterestShape(PolygonShape<?> shape) {
        super(shape);
    }

    @Override
    protected PolygonShape<?> createMapShape(Area placeOfInterest) {
        
        List<AbstractMapCoordinate> vertices = schemaToCoords(placeOfInterest.getCoordinate());
        
        PolygonShape<?> shape = WrapPanel.getInstance().getCurrentMap().createPolygon(vertices);
        shape.setName(placeOfInterest.getName());
        shape.setColor(placeOfInterest.getColorHexRGBA());
        
        return shape;
    }

    @Override
    protected Area createPlaceOfInterest(PolygonShape<?> shape) {
        
        Area area = new Area();
        setPlaceOfInterest(area);
        updatePlaceOfInterest();
        
        return area;
    }

    @Override
    public void updatePlaceOfInterest() {
        
        Area area = getPlaceOfInterest();
        PolygonShape<?> shape = getMapShape();
        
        area.setName(shape.getName());
        area.setColorHexRGBA(shape.getColor());
        
        area.getCoordinate().clear();
        area.getCoordinate().addAll(coordsToSchema(shape.getVertices()));
    }

    @Override
    public void updateMapShape() {
        
        Area area = getPlaceOfInterest();
        PolygonShape<?> shape = getMapShape();
        
        List<AbstractMapCoordinate> vertices = schemaToCoords(area.getCoordinate());
        
        shape.setVertices(vertices);
        shape.setName(area.getName());
        shape.setColor(area.getColorHexRGBA());
    }
    
    private List<Coordinate> coordsToSchema(List<AbstractMapCoordinate> coords){
        
        List<Coordinate> schemaCoords = new ArrayList<>();
        
        for(AbstractMapCoordinate vertex : coords) {
            schemaCoords.add(PlacesOfInterestOverlay.toSchemaCoordinate(vertex));
        }
        
        return schemaCoords;
    }
    
    private List<AbstractMapCoordinate> schemaToCoords(List<Coordinate> coords){
        
        List<AbstractMapCoordinate> mapCoords = new ArrayList<>();
        
        for(Coordinate coord : coords) {
            mapCoords.add(PlacesOfInterestOverlay.toMapCoordinate(coord));
        }
        
        return mapCoords;
    }

}
