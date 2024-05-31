/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import generated.dkf.Coordinate;
import generated.dkf.Path;
import generated.dkf.Segment;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PathEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.PlacesOfInterestOverlay;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.WrapPanel;
import mil.arl.gift.tools.map.client.draw.PolylineShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A {@link AbstractPlaceOfInterestShape} that associates an {@link Path} place of interest with a {@link Polyline} map shape
 * 
 * @author nroberts
 */
public class PathOfInterestShape extends AbstractPlaceOfInterestShape<Path, PolylineShape<?>> {

    /**
     * Creates a new place shape for the given place of interest. An appropriate map shape will be 
     * created based on the data provided by the given place of interest.
     * 
     * @param placeOfInterest the place of interest to create a place shape for. Cannot be null.
     */
    public PathOfInterestShape(Path placeOfInterest) {
        super(placeOfInterest);
    }
    
    /**
     * Creates a new place shape for the given map shape. An appropriate place of interest will be created based on 
     * the data provided by the given map shape.
     * 
     * @param shape the map shape to create a place shape for. Cannot be null.
     */
    public PathOfInterestShape(PolylineShape<?> shape) {
        super(shape);
    }

    @Override
    protected PolylineShape<?> createMapShape(Path placeOfInterest) {
        
        List<AbstractMapCoordinate> vertices = segmentsToCoords(placeOfInterest.getSegment());
        
        PolylineShape<?> shape = WrapPanel.getInstance().getCurrentMap().createPolyline(vertices);
        shape.setName(placeOfInterest.getName());
        shape.setColor(placeOfInterest.getColorHexRGBA());
        
        return shape;
    }

    @Override
    protected Path createPlaceOfInterest(PolylineShape<?> shape) {
        
        Path path = new Path();
        setPlaceOfInterest(path);
        updatePlaceOfInterest();
        
        return path;
    }

    @Override
    public void updatePlaceOfInterest() {
        
        Path path = getPlaceOfInterest();
        PolylineShape<?> shape = getMapShape();
        
        path.setName(shape.getName());
        path.setColorHexRGBA(shape.getColor());
        
        PathEditor.setCoordinates(path, coordsToSchema(shape.getVertices()), null);
    }

    @Override
    public void updateMapShape() {
        
        Path path = getPlaceOfInterest();
        PolylineShape<?> shape = getMapShape();
        
        List<AbstractMapCoordinate> vertices = segmentsToCoords(path.getSegment());
        
        shape.setVertices(vertices);
        shape.setName(path.getName());
        shape.setColor(path.getColorHexRGBA());
    }

    private List<Coordinate> coordsToSchema(List<AbstractMapCoordinate> coords){
        
        List<Coordinate> schemaCoords = new ArrayList<>();
        
        for(AbstractMapCoordinate vertex : coords) {
            schemaCoords.add(PlacesOfInterestOverlay.toSchemaCoordinate(vertex));
        }
        
        return schemaCoords;
    }
    
    private List<AbstractMapCoordinate> segmentsToCoords(List<Segment> segments){
        List<AbstractMapCoordinate> mapCoords = new ArrayList<>();      
        Iterator<Segment> itr = segments.iterator();
        
        while(itr.hasNext()) {
            
            Segment segment = itr.next();
            
            if(segment.getStart() != null 
                    && segment.getStart().getCoordinate() != null) {
                
                // Add the starting point of each segment. The ending point should be the starting point of the next segment, so
                // it can be ignored here.
                Coordinate coordinate = segment.getStart().getCoordinate();
                mapCoords.add(PlacesOfInterestOverlay.toMapCoordinate(coordinate));
            }
            
            if(!itr.hasNext()
                    && segment.getEnd() != null 
                    && segment.getEnd().getCoordinate() != null){
                
                // For the last element in the list, add the ending point.
                Coordinate coordinate = segment.getEnd().getCoordinate();
                mapCoords.add(PlacesOfInterestOverlay.toMapCoordinate(coordinate));
            }
        }
        
        return mapCoords;
    }
}
