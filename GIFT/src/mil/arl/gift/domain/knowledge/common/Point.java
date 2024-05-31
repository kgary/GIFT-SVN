/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import javax.vecmath.Point3d;

import mil.arl.gift.common.coordinate.AGL;
import mil.arl.gift.common.coordinate.AbstractCoordinate;
import mil.arl.gift.common.coordinate.AbstractCoordinate.CoordinateType;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GCC;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainDKFHandler;

/**
 * This class contains information about a point.
 * 
 * @author mhoffman
 *
 */
public class Point extends Point3d implements PlaceOfInterestInterface {

    /**
     * default, auto-added
     */
    private static final long serialVersionUID = 1L;
    
    /** name of the point */
    private String name;
    
    /** the enumerated type of coordinate this point is based on, will be null when instantiating without
     * and underlying {@link AbstractCoordinate} object  */
    private CoordinateType coordinateType = null;
    
    /** the GCC version of the coordinates for this point.  
     * Can be null if the underlying coordinate is not {GCC, GDC}*/
    private GCC gcc;
    
    /** the GDC version of the coordinates for this point.  
     * Can be null if the underlying coordinate is not {GCC, GDC}*/
    private GDC gdc;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - name of this point.  Can't be null or empty string.
     * @param location the coordinate.  Can't be null.
     */
    public Point(String name, Point3d location){
        super(location);
        
        if(StringUtils.isBlank(name)){
            throw new IllegalArgumentException("The name can't be null or empty");
        }
        this.name = name;
    }
    
    /**
     * Class constructor - set attributes using the abstract coordinate contents
     * 
     * @param name - name of this point.  Can't be null or empty string.
     * @param coordinate the GIFT coordinate.  Can't be null.
     */
    public Point(String name, AbstractCoordinate coordinate){
        this(name, CoordinateUtil.getInstance().convertToPoint(coordinate));
        
        if(coordinate instanceof GCC){
            coordinateType = CoordinateType.GCC;
            this.gcc = (GCC)coordinate;
        }else if(coordinate instanceof GDC){
            coordinateType = CoordinateType.GDC;
            this.gdc = (GDC)coordinate;
        }else if(coordinate instanceof AGL){
            coordinateType = CoordinateType.AGL;
        }
    }
    
    /**
     * Class constructor - set attributes using generated class's object for a point
     * 
     * @param point - dkf content for a point.  Can't be null.  The coordinate
     * can't be null and the name can't be null or empty in the point object.
     */
    public Point(generated.dkf.Point point){
        this(point.getName(), DomainDKFHandler.buildCoordinate(point.getCoordinate()));
    }

    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Return the enumerated type of coordinate this point is based on, will be null when instantiating without
     * and underlying {@link AbstractCoordinate} object 
     * @return can be null.
     */
    public CoordinateType getCoordinateType(){
        return coordinateType;
    }
    
    /**
     * Return the GDC instance of the coordinate behind this point.
     * 
     * @return Can be null if the underlying coordinate is not {GCC, GDC}
     */
    public GDC toGDC(){
        
        if(this.gdc != null){
            return this.gdc;
        }
        
        GDC gdc = null;
        CoordinateType coordinateType = getCoordinateType();
        switch(coordinateType){
            case GDC:
                // ideal
                gdc = new GDC(getX(), getY(), getZ());
                break;
            case AGL:
                // not supported - bad
                break;
            case GCC:
                // convert to GDC
                gdc = CoordinateUtil.getInstance().convertFromGCCToGDC(this);
                break;
        }
        
        this.gdc = gdc;        
        return gdc;
    }
    
    /**
     * Return the GCC instance of the coordinate behind this point.
     * 
     * @return Can be null if the underlying coordinate is not {GCC, GDC}
     */
    public GCC toGCC(){
        
        if(this.gcc != null){
            return this.gcc;
        }
        
        GCC gcc = null;
        CoordinateType coordinateType = getCoordinateType();
        switch(coordinateType){
            case GDC:
                // convert to GCC
                gcc = CoordinateUtil.getInstance().convertToGCC(this);
                break;
            case AGL:
                // not supported - bad
                break;
            case GCC:
                // ideal
                gcc = new GCC(getX(), getY(), getZ());
                break;
        }
        
        this.gcc = gcc;
        return gcc;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[Point: ");
        sb.append("name = ").append(getName());
        sb.append(", location = ").append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
