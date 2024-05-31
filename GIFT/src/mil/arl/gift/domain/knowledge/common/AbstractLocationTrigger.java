/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import javax.vecmath.Point3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.coordinate.AbstractCoordinate;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.net.api.message.Message;

/**
 * This trigger is used to determine if an entity's location is within the radius of a goal location.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractLocationTrigger extends AbstractTrigger {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractLocationTrigger.class);
    
    /** the default radius to use for distance checks */
    public static final double DEFAULT_RADIUS = 2.0;
    
    /** the default flag for whether the trigger requires movement before checking the trigger rule */
    public static final boolean DEFAULT_REQUIRES_MOVEMENT = true;
    
    /** the location to compare to for this trigger */
    private AbstractCoordinate goalLocation;
    
    /** the radius around the location (meters) */
    private double radius;
    
    /** flag to indicate whether this trigger requires a change in location before firing */
    private boolean requiresMovement;
    
    /** flag to indicate whether the location has changed */
    private boolean hasMoved = false;
    
    /** the first reported location */
    private Point3d firstLocation;
    
    /** point used to help evaluate the condition.  Helps with conversion of the location to a point */
    private Point3d goalPoint = new Point3d();
    
    /**
     * Class constructor - set attributes
     * 
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     * @param location the location to compare to for this trigger
     * @param radius the radius around the location (meters)
     * @param requiresMovement flag to indicate whether this trigger requires a change in location before firing
     */
    public AbstractLocationTrigger(String triggerName, AbstractCoordinate location, double radius, boolean requiresMovement){
        super(triggerName);
        
        if(location == null){
            throw new IllegalArgumentException("The location can't be null.");
        }
        this.goalLocation = location;
        
        if(radius < 0){
            throw new IllegalArgumentException("The radius value of "+radius+" must be non-negative.");
        }
        this.radius = radius;
        
        this.requiresMovement = requiresMovement;
    }
    
    /**
     * Class constructor - set attributes
     * Note: uses default radius distance and requires a change in location before trigger will fire.
     * 
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     * @param location the location to compare to for this trigger
     * @param radius optional radius around the location to use for the location trigger.  If null, the default is used {@link #DEFAULT_RADIUS}.
     */
    public AbstractLocationTrigger(String triggerName, AbstractCoordinate location, Double radius){
        this(triggerName, location, radius == null ? DEFAULT_RADIUS : radius, DEFAULT_REQUIRES_MOVEMENT);
    }

    @Override
    public boolean shouldActivate(Message message) {
        
        if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){
            
            //decode location
            EntityState es = (EntityState)message.getPayload();
            
            //DEBUG
            //System.out.print("E.S.: location = "+esm.getLocation()+" orientation = "+esm.getOrientation());            
            
            if(requiresMovement && !hasMoved){
                //the trigger requires movement before it can be fired
                
                if(firstLocation == null){
                    //this is the first location provided, save it                    
                    firstLocation = es.getLocation();
                    return false;
                    
                }
                
                //check if the location has changed
                double distance = es.getLocation().distance(firstLocation);
                if(distance == 0){
                    return false;
                }
                
                hasMoved = true;
            }
            
            CoordinateUtil.getInstance().convertIntoPoint(goalLocation, goalPoint);
            double distance = es.getLocation().distance(goalPoint);            
            if(distance <= radius){
                logger.info("Trigger has been activated because distance = "+distance+" is within radius = "+radius);
                return true;
            }
            
            if(logger.isDebugEnabled()){
                logger.info("Not activating trigger because distance = "+distance+"; trigger = "+this);
            }
        }
        
        return false;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append(", Goal = ").append(goalLocation);
        sb.append(", Radius = ").append(radius);
        sb.append(", Req Movement = ").append(requiresMovement);        
        sb.append("]");
        
        return sb.toString();
    }

}
