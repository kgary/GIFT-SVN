package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import generated.dkf.AGL;
import generated.dkf.Coordinate;
import generated.dkf.GCC;
import generated.dkf.GDC;
import mil.arl.gift.lms.impl.lrs.LrsEnum;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Creation of Coordinate Extension JSON used within corresponding Context and Result Extension.
 * 
 * @author Yet Analytics
 *
 */
public class CoordinateExtension {
    
    public enum extensionObjectKeys implements LrsEnum {
        X("x"),
        Y("y"),
        Z("z"),
        LATITUDE("latitude"),
        LONGITUDE("longitude"),
        ELEVATION("elevation"),
        GCC("GCC"),
        GDC("GDC"),
        AGL("AGL");
        private String value;
        extensionObjectKeys(String s) {
            this.value = s;
        }
        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Create ObjectNode from GCC's x, y and z
     * 
     * @param gcc - coordinate
     * 
     * @return object node with x, y and z fields
     */
    private static ObjectNode createExtensionComponent(GCC gcc) {
        if(gcc == null) {
            throw new IllegalArgumentException("gcc can not be null!");
        }
        ObjectNode component = new JsonNodeFactory(true).objectNode();
        component.put(extensionObjectKeys.X.getValue(), gcc.getX());
        component.put(extensionObjectKeys.Y.getValue(), gcc.getY());
        component.put(extensionObjectKeys.Z.getValue(), gcc.getZ());
        return component;
    }
    
    /**
     * Create ObjectNode from GDC's latitude, longitude and elevation
     * 
     * @param gdc - coordinate
     * 
     * @return object node with latitude, longitude and elevation fields
     */
    private static ObjectNode createExtensionComponent(GDC gdc) {
        if(gdc == null) {
            throw new IllegalArgumentException("gdc can not be null!");
        }
        ObjectNode component = new JsonNodeFactory(true).objectNode();
        component.put(extensionObjectKeys.LATITUDE.getValue(), gdc.getLatitude());
        component.put(extensionObjectKeys.LONGITUDE.getValue(), gdc.getLongitude());
        component.put(extensionObjectKeys.ELEVATION.getValue(), gdc.getElevation());
        return component;
    }
    
    /**
     * Create ObjectNode from AGL's x, y and elevation
     * 
     * @param agl - coordinate
     * 
     * @return object node with x, y and elevation fields
     */
    private static ObjectNode createExtensionComponent(AGL agl) {
        if(agl == null) {
            throw new IllegalArgumentException("agl can not be null!");
        }
        ObjectNode component = new JsonNodeFactory(true).objectNode();
        component.put(extensionObjectKeys.X.getValue(), agl.getX());
        component.put(extensionObjectKeys.Y.getValue(), agl.getY());
        component.put(extensionObjectKeys.ELEVATION.getValue(), agl.getElevation());
        return component;
    }
    
    /**
     * Create Object Node with either GCC, GDC or AGL field based on the type of the coordinate
     * 
     * @param coordinate - the location to convert
     * 
     * @return object node with GCC, GDC or AGL field
     */
    private static ObjectNode createExtensionItem(Coordinate coordinate) {
        if(coordinate == null) {
            throw new IllegalArgumentException("coordinate can not be null!");
        }
        ObjectNode item = new JsonNodeFactory(true).objectNode();
        Serializable kind = coordinate.getType();
        if(kind instanceof GCC) {
            item.set(extensionObjectKeys.GCC.getValue(), createExtensionComponent((GCC) kind));
        } else if(kind instanceof GDC) {
            item.set(extensionObjectKeys.GDC.getValue(), createExtensionComponent((GDC) kind));
        } else if(kind instanceof AGL) {
            item.set(extensionObjectKeys.AGL.getValue(), createExtensionComponent((AGL) kind));
        }
        return item;   
    }
    
    /**
     * Create array node that contains the one coordinate
     * 
     * @param coordinate - location
     * 
     * @return ArrayNode containing item corresponding to the location
     */
    public static ArrayNode createExtensionColl(Coordinate coordinate) {
        if(coordinate == null) {
            throw new IllegalArgumentException("coordinate can not be null!");
        }
        List<Coordinate> coll = new ArrayList<Coordinate>(1);
        coll.add(coordinate);
        return createExtensionColl(coll);
    }
    
    /**
     * Iterate over a collection of coordinates and create an Object Node for each
     * 
     * @param coordinates - collection of locations
     * 
     * @return ArrayNode with each item corresponding to a location
     */
    public static ArrayNode createExtensionColl(List<Coordinate> coordinates) {
        if(coordinates == null) {
            throw new IllegalArgumentException("coordinates can not be null!");
        }
        ArrayNode coll = new JsonNodeFactory(true).arrayNode();
        for(Coordinate coordinate : coordinates) {
            ObjectNode item = createExtensionItem(coordinate);
            if(item.size() != 0) {
                coll.add(item);
            }
        }
        return coll;
    }
}
