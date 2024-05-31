/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.net.api.message.Message;


//NOTE: code for pausing does NOT support pauses originating from VBS!

/**
 * Wrapper class to be used exclusively by EntityTable.  Wraps around an incoming EntityStateMessage.
 * Used to keep track of last ground truth location update and time.
 * 
 * This class is not thread-safe. Thread safety is managed by EntityTable class.
 * 
 * @author cragusa
 */    
class DeadReckonedEntity {
        
    private Message msg;
    private EntityState es;   
    
    private Point3d lastGroundTruthLocation = new Point3d();
    private long    timeOfLastGroundTruthUpdate;   

    private boolean hasNonZeroVelocity = true;            
    
    private boolean paused = false;
    private long accumulatedPauseTime = 0;
    private long startTimeOfActivePause;
    
    
    //Minimum time interval between receipt of a ground truth update and when a dead reckoned message will be sent.
    //intent is to avoid sending a DR update in very close proximity to a ground truth update.
    //Since ground truth updates are handled in the usual way (i.e. by BaseDomainSession) there is still a chance for close updates
    //when ground truth updates come in very soon after a DR update. Need a more advanced implementation to work around this.
    private static final int MIN_DT_MILLIS = 10;   
    
    /**
     * Constructor used to create DeadReckonedEntity.
     * @param msg
     */
    //called by BaseDomainSession's thread
    DeadReckonedEntity(Message msg, long time) {            
        update(msg, time);            
    } 
  
    /**
     * Updates the state of an existing DeadReckonedEntity instance.
     * 
     * @param msg the Message containing the update.
     * @param time the time of the update. 
     */
    void update(Message msg, long time) {
        
        this.msg = msg;
        this.es = (EntityState)msg.getPayload(); 
        setGroundTruthLocation(this.es.getLocation(), time);
        
        //only do DR if the entity has a non-zero velocity. set the flag here for each new esm
        Vector3d vel = es.getLinearVelocity();
        hasNonZeroVelocity = ( vel.getX() != 0.0 || vel.getY() != 0.0 || vel.getZ() != 0.0 ); 
        
        //we just got a GT update, so any accumulated pauses are blown away
        accumulatedPauseTime = 0;
        
        if( isPaused() ) {
            //if we're currently paused we need to update the pause start time to coincide with the current time.
            startTimeOfActivePause = time;
        }
    }
    
    /**
     * Get the entity ID for the entity represented by this DeadReckonedEntity.
     * 
     * @return the entity ID.
     */
    EntityIdentifier getEntityID() {
        
        return es.getEntityID();
    }
    
    
    /**
     * Dead Reckons this DeadReckonedEntity using the absolute wall clock time (millis since epoch) provided.
     * @param time
     */
    void deadReckon(long time) {
    
        if( !isPaused() ) {

            long dtMillis = getDeltaSimTimeMillis(time);

            if ( dtMillis >= MIN_DT_MILLIS && hasNonZeroVelocity ) {

                double dtSeconds = dtMillis/1000.0;

                Point3d  loc = es.getLocation();
                Vector3d vel = es.getLinearVelocity();

                Point3d lastGtLoc = lastGroundTruthLocation;

                loc.setX(lastGtLoc.getX() + vel.getX() * dtSeconds);
                loc.setY(lastGtLoc.getY() + vel.getY() * dtSeconds);
                loc.setZ(lastGtLoc.getZ() + vel.getZ() * dtSeconds);
               
            }   
        }
    }
    
    /**
     * Get the Message contained by this DeadReckonedEntity.
     * 
     * @return the Message contained by this DeadReckonedEntity.
     */
    Message getDeadReckonedMessage() {
        
        return msg;
    }
   
    /**
     * Determines if this DeadReckonedEntity is stale or not.  Takes into account pauses.  (Entities don't time out of updates are delayed because of pausing.  Sim time must be elapsing for them to timeout.) 
     *   
     * @param time current time in milliseconds since epoch.
     * @param timeOutMillis timeout threshold in milliseconds.
     * @return boolean indicating if this DeadReckonedEntity is timed out.
     */
    boolean isStale(long time, long timeOutMillis) {
        
        return  getDeltaSimTimeMillis(time) > timeOutMillis;
    }
   
    boolean isPaused() {
        return paused;
    }

    void setPaused(boolean paused, long time) {

        if( this.paused != paused ) {

            if( paused ) { //starting a pause

                startTimeOfActivePause = time;
            }
            else { //ending a pause
                
                accumulatedPauseTime += (time - startTimeOfActivePause);
            }

            this.paused = paused;
        }
    } 
    
    private void setGroundTruthLocation(Point3d loc, long time) {
        
        lastGroundTruthLocation.set(loc);
        timeOfLastGroundTruthUpdate = time;            
    }      
    
    // elapsedSimTime since last GT update (accounts for pauses)
    private long getDeltaSimTimeMillis(long time) {
        
        long deltaWallClockTimeMillis = time - timeOfLastGroundTruthUpdate;
        
        return deltaWallClockTimeMillis - getCumulativePauseTime(time);        
    }
        
    private long getCumulativePauseTime(long time) {
                   
        long activePauseDuration = isPaused() ? (time - startTimeOfActivePause) : 0;
               
        return accumulatedPauseTime + activePauseDuration;
    }   
}


/**
 * Class to maintain an entity table for the purposes of dead reckoning the entities participating in a domain session.
 * 
 * Entity locations will be ticked (i.e. dead reckoned) periodically to generate assessments in between ground truth updates coming in from the training application.
 * 
 * Each domain session should create a new EntityTable.
 * 
 * Default entity time out is set for 10,000 milliseconds (i.e. Entities must be updated at least once every 10 seconds or they will be dropped from the table.).
 * This value can be user defined by editing the domain module property file.
 * 
 * Dead Reckoning timer interval is 100 milliseconds (10 Hz).  This value can be changed to a user defined value by editing the domain module property file. 
 *
 * @author cragusa
 *
 */
public class EntityTable {    
        
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(EntityTable.class);
    
    /**
     * The default timeout duration in milliseconds. This value is used when a value isn't specified in the properties file. 
     */
    public  static final int DEFAULT_ENTITY_TIMEOUT_MILLIS = 10000;
    
    /**
     *  The default dead reckoning interval. This value is used when a value isn't specified in the properties file. 
     *
     */
    public  static final int DEFAULT_DEAD_RECKONING_INTERVAL_MILLIS = 100;
    
    /**
     * The timer used to tick the entity tables dead reckoning code.
     */
    private Timer drTimer = new Timer("Dead Reckoning Timer");        
   
    /**
     * Map to contain the entities in the entity table. 
     */
    private Map<EntityIdentifier, DeadReckonedEntity> map = new HashMap<EntityIdentifier, DeadReckonedEntity>();
    
    /**
     * Call back to process entities as they are dead reckoned to new locations.
     */
    private DeadReckonedEntityMessageHandler drEntityMessageHandler;
    
    /**
     * Flag to indicate if the entity table is running.
     */
    private boolean running = false;
    
    /**
     * List containing entity ID's of stale entities (i.e. entities that need to be removed because they've timed out). 
     */
    private List<EntityIdentifier> staleEntityList = new LinkedList<EntityIdentifier>();
    
    /**
     * The entity timeout threshold in milliseconds.  
     */
    private static final int entityTimeoutMillis = DomainModuleProperties.getInstance().getEntityTableEntityTimeoutMillis();
    
    /**
     * The interval in milliseconds between entity table ticks.
     */
    private static final int deadReckoningIntervalMillis = DomainModuleProperties.getInstance().getEnityTableDeadReckoningInterval();
       
    /**
     * Flag indicating that the entity table (and thus dead reckoning) is paused.
     */
    private boolean paused = false;
    
    /**
     * Reusable container to hold DeadReckonedEntity references while they are being iterated over, while outside a synchronized block.
     * 
     */
    private List<DeadReckonedEntity> entityList = new ArrayList<DeadReckonedEntity>();

    
    /**
     * Constructs a new EntityTable
     * 
     * @param handler the object that will process the entities information after each tick.
     */
    EntityTable(DeadReckonedEntityMessageHandler handler) {
         
        if(handler == null) {
            
            logger.error("EntityTable::setDeadReckonedEntityMessageHandler called with a null value for handler");
            
            throw new IllegalArgumentException("handler must not be null");
        }        
        
        this.drEntityMessageHandler = handler;
        
        start();
    }
    
    /**
     * @return true if EntityTable is paused, otherwise returns false.
     */
    private boolean isPaused() {

        return paused;       
    }
       
    /**
     * Sets the paused state of the entity table. 
     * 
     * @param paused boolean indicating the desired state of the entity table.
     */
    synchronized void setPaused(boolean paused) {
        
        this.paused = paused;

        for(DeadReckonedEntity entity : map.values()) {

            synchronized(entity) {

                entity.setPaused(paused, System.currentTimeMillis());
            }
        }        
    }
        
      
    /**
     * Start the entity table ticking (performing dead reckoning).
     */
    private void start() {
                
        TimerTask task = new TimerTask() {
            
            @Override
            public void run() {
                
                EntityTable.this.tick();
            }
        };
                       
        drTimer.schedule(task, 0, deadReckoningIntervalMillis);
        
        running = true;
        
        if(logger.isDebugEnabled()) {
            
            logger.debug("Starting debug timer with interval of " + DEFAULT_DEAD_RECKONING_INTERVAL_MILLIS + " milliseconds");
        }
    }
    
    /**
     * Stop the EntityTable from ticking (i.e. from doing dead reckoning).
     */
    void stop() {
        
        running = false;
        
        if(drTimer != null) {
            
            drTimer.cancel();
            drTimer = null;
        }         
    }
    
    
    /**
     * Client provides a new EntityStateMessage, resulting in either adding a new entity to the entity table, or updating an existing one.
     * 
     * @param esm the entity state message containing the update.
     */
    void update(Message message) {
                
        if( running ) {

            EntityState es = (EntityState)message.getPayload();
            
            EntityIdentifier eid = es.getEntityID();

            DeadReckonedEntity drEntity = null;
            
            synchronized(this) {
                
                long now = System.currentTimeMillis();
                
                if( !map.containsKey( eid ) ) {
                    
                    //no need to sync on drEntity here, because other threads don't know about it until it's put into the map.
                    drEntity = new DeadReckonedEntity(message, now);
                    drEntity.setPaused(isPaused(), now);
                    map.put(eid, drEntity);

                    if(logger.isTraceEnabled()) {

                        logger.trace("Added entity to entity table map with entity ID of: " + eid);
                    }
                }
                else {

                    drEntity = map.get(eid);
                    
                    synchronized(drEntity) {
                        
                        drEntity.setPaused(isPaused(), now);                
                        drEntity.update(message, now);
                    }
                }  
            }
        
        }
        else {
            
            logger.warn("EntityTable received an update, even though it's NOT running");
        }
    }
    
    
    
    /**
     * Tick the entities in the entity table.
     */
    private void tick() {
        
        staleEntityList.clear(); 
        
        entityList.clear();
                
        synchronized(this) {
            
            //copy references to entityList so I can release the lock on this.
            entityList.addAll(map.values());
        }
        
        //below here it's possible that the contents of the map may change, but that's okay, we'll pick up the changes on the next tick.
        //Lock individual entities instead.        
        for(DeadReckonedEntity entity: entityList) {

            //dead reckon the entities, and do the callback for entities that are moving            
            long now = System.currentTimeMillis();

            synchronized(entity) {
                
                if( entity.isStale(now, entityTimeoutMillis) ) {

                    staleEntityList.add(entity.getEntityID());
                }
                else {
                    
                    if( !entity.isPaused() ) {

                        //System.out.print("."); //use to test if DR is pausing when expected

                        entity.deadReckon(now);

                        drEntityMessageHandler.handleDeadReckonedEntityMessage( entity.getDeadReckonedMessage() );
                        
                    }//if

                }//else
                
            }//synchronized

        }//for
                
        if( !staleEntityList.isEmpty() ) {
            
            synchronized(this) {
                
                //remove stale entities;
                for(EntityIdentifier eid : staleEntityList) {

                    map.remove(eid);

                    if(logger.isDebugEnabled()) {

                        logger.debug("Removed entity from entity table map with entity ID of: " + eid);
                    }
                    
                }//for
                
            }//synchronized
            
        } //if
        
    }//tick
}

