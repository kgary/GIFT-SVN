/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import generated.dkf.EntityLocation;
import generated.dkf.EntityLocation.EntityId;
import generated.dkf.LearnerId;
import generated.dkf.StartLocation;

import java.io.Serializable;

import javax.vecmath.Point3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.coordinate.AbstractCoordinate;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GCC;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.domain.DomainDKFHandler;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.course.dkf.team.TeamUtil;
import mil.arl.gift.net.api.message.Message;

/**
 * This trigger is used to determine if an entity's location is within the radius of a goal location.
 * 
 * @author mhoffman
 *
 */
public class EntityLocationTrigger extends AbstractLocationTrigger {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EntityLocationTrigger.class);
    
    /** 
     * the start location of the entity that can activate this trigger
     * Can be null if entityMarking is specified.
     */
    private GCC startLocation;
    
    /** 
     * the unique team org team member name used to identify the entity this trigger is analyzing
     * Can be null if startLocation is specified. 
     */
    private String uniqueTeamMemberName;
    
    /** Cached point used for testing the team member location. */
    private Point3d cachedPoint = new Point3d();
    
    /**
     * Class constructor - use starting location to uniquely identify the entity this trigger is analyzing.
     * 
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     * @param startLocation - the initial location of the entity that can activate this trigger
     * @param triggerLocation - the location that entity must reach to activate this trigger
     * @param radius the radius around the location (meters)
     * @param requiresMovement flag to indicate whether this trigger requires a change in location before firing
     */
    public EntityLocationTrigger(String triggerName, GCC startLocation, GCC triggerLocation, double radius, boolean requiresMovement){
        super(triggerName, triggerLocation, radius, requiresMovement);

        this.startLocation = startLocation;
    }
    
    /**
     * Class constructor - using entity marking to uniquely identify the entity this trigger is analyzing.
     * 
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     * @param entityMarking - the unique entity marking used to identify the entity this trigger is analyzing
     * @param triggerLocation - the location that entity must reach to activate this trigger
     * @param radius the radius around the location (meters)
     * @param requiresMovement flag to indicate whether this trigger requires a change in location before firing
     */
    public EntityLocationTrigger(String triggerName, String entityMarking, GCC triggerLocation, double radius, boolean requiresMovement){
        super(triggerName, triggerLocation, radius, requiresMovement);

        this.uniqueTeamMemberName = entityMarking;
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     * @param trigger - the generated class's object representing the contents of this type of trigger. Can't be null.
     * @param triggerLocation the location the entity needs to reach to activate this trigger.  Can't be null.
     * @param radius the distance in meters the entity can be from the location to activate this trigger.  Can be null
     * if the default value is wanted.  
     */
    public EntityLocationTrigger(String triggerName, EntityLocation trigger, AbstractCoordinate triggerLocation, Double radius){
        super(triggerName, triggerLocation, radius);
        
        if(trigger.getEntityId() == null){
            throw new IllegalArgumentException("The entity id can't be null");
        }
        
        EntityId entityId = trigger.getEntityId();
        Serializable entityIdType = entityId.getTeamMemberRefOrLearnerId();
        if(entityIdType instanceof LearnerId){
            LearnerId learnerId = (LearnerId)entityIdType;
            if(learnerId.getType() instanceof StartLocation){
                AbstractCoordinate sCoord = DomainDKFHandler.buildCoordinate(((StartLocation)learnerId.getType()).getCoordinate());
                this.startLocation = CoordinateUtil.getInstance().convertToGCC(sCoord);
            }else if(learnerId.getType() instanceof String){
                this.uniqueTeamMemberName = (String) learnerId.getType();
            }else{
                throw new IllegalArgumentException("Found unhandled learner id type of "+learnerId.getType());
            }
        }else if(entityIdType instanceof EntityLocation.EntityId.TeamMemberRef){
            this.uniqueTeamMemberName = ((EntityLocation.EntityId.TeamMemberRef)entityIdType).getValue();
        }else{
            throw new IllegalArgumentException("Found unhandled entity id type of "+entityIdType);
        }
    }
    
    @Override
    public void initialize(){   
        if(logger.isInfoEnabled()){
            logger.info("Initializing "+this);        
        }
    }
    
    /**
     * Check whether the entity state is the first entity state for the entity that this
     * trigger is analyzing.  If so, set the triggers team member information for future use.
     * 
     * @param entityState an incoming entity state to check whether it contains information for this trigger's
     * interested entity.
     */
    private void updateTeamMember(EntityState entityState){
        
        if(getTriggerTeamMember() == null && getTeamOrganization() != null){
            
            if(startLocation != null){
                //check to see if this entity state identifies the trigger's team member as defined
                //by a start location
                
                TeamMember<?> foundTeamMember = TeamUtil.getTeamMemberByStartLocation(entityState, TeamUtil.START_RADIUS, cachedPoint, 
                        getTeamOrganization().getRootTeam().getUnits());
                setTriggersTeamMember(foundTeamMember);
                
            }else if(uniqueTeamMemberName != null){
                // check to see if this entity state identifies the trigger's team member as defined
                // by an entity marking     
                
                TeamMember<?> foundTeamMember = getTeamOrganization().getRootTeam().getTeamMemberByEntityMarking(entityState.getEntityMarking().getEntityMarking());
                if(foundTeamMember != null && uniqueTeamMemberName.equals(foundTeamMember.getName())){
                    // the entity state describes the team member this trigger is tracking
                    setTriggersTeamMember(foundTeamMember);
                }
            }
        }

    }

    @Override
    public boolean shouldActivate(Message message) {
        
        if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){
            EntityState esm = (EntityState)message.getPayload();
            
            TeamMember<?> triggerTeamMember = getTriggerTeamMember();
            if(triggerTeamMember == null){
                updateTeamMember(esm);
            }
            
            //only check if the entity state message describes the trigger's entity
            if(!isTriggerTeamMember(esm.getEntityID())){
                return false;
            }
            
            if(logger.isDebugEnabled()){
                logger.debug("Received Entity State Message that matches trigger team member of "+getTriggerTeamMember()+" for trigger "+toString());
            }
         
            return super.shouldActivate(message);
        }
        
        return false;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[EntityLocationTrigger: ");
        sb.append(super.toString()); 
        if(startLocation != null){
            sb.append(", start location = ").append(startLocation);
        }else{
            sb.append(", team member ref = ").append(uniqueTeamMemberName);
        }
        sb.append("]");
        
        return sb.toString();
    }
}
