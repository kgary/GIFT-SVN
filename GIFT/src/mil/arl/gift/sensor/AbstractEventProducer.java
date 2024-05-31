/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.sensor.writer.AbstractFileWriter;
import mil.arl.gift.sensor.writer.AbstractWriter;

/**
 * An Event Producer class is a class which produces events.  Examples of
 * event producer classes include sensors and filters.  An event producer can have
 * its events written to a file and/or sent over the network.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractEventProducer {    
    
    protected static final int SECONDS_TO_MILLISECONDS = 1000;
    
    /** event producer properties */
    public static final String DISTRIBUTE_EXTERNALLY = "distributeExternally";
    
    /** whether the events should be sent over the network */
    private Boolean distributeExternally = false;
    
    /** a writer instance responsible for writing the events to a file */
    private AbstractWriter writer = null;
    
    /** information about the data the event producer can producer (e.g. Humidity of type Double, Temperature of type Double) */
    protected EventProducerInformation information;
    
    /** the amount of elapsed domain session time */
    private long domainSessionStartTime;
    
    /** flag used to indicate if this event producer has been started and not stopped yet. */
    private boolean hasStarted = false;
    
    /**
     * Default constructor - default
     */
    public AbstractEventProducer(){
        
    }
    
    /**
     * Return the amount of elapsed domain session time
     * 
     * @return long elapsed domain session time
     */
    public long getDomainSessionStartTime(){
        return domainSessionStartTime;
    }
    
    /**
     * Return whether this event producer's event should be sent over the network
     * 
     * @return boolean flag indicating if the producer's event(s) should be distributed
     */
    public boolean shouldDistributeExternally(){
        return distributeExternally;
    }
    
    /**
     * Set whether this event producer should sent its events out to other gift modules.
     * 
     * @param value to use
     */
    public void setDistributeExternally(boolean value){
        this.distributeExternally = value;
    }
    
    /**
     * Set the event writer instance for this class
     * 
     * @param writer the event writer
     */
    public void setEventWriter(AbstractWriter writer){
        this.writer = writer;
    }
    
    /**
     * Return the event writer instance for this class
     * NOTE: can be null if no writer is needed
     * 
     * @return AbstractWriter the event writer for this event producer
     */
    public AbstractWriter getEventWriter(){
        return writer;
    }
    
    /**
     * Return the information about the data the event producer can producer (e.g. Humidity of type Double, Temperature of type Double) 
     * 
     * @return EventProducerInformation used to contain the information about the data the event producer can producer 
     */
    public EventProducerInformation getEventProducerInformation(){
        return information;
    }
    
    /**
     * Set the information about the data the event producer can producer (e.g. Humidity of type Double, Temperature of type Double) 
     * 
     * @param information used to contain the information about the data the event producer can producer 
     */
    public void setEventProducerInformation(EventProducerInformation information){
        this.information = information;
    }
    
    /**
     * The event producer is being told to stop and will no longer be creating events.
     */
    public void stop(){
        
        if(getEventWriter() != null){
            writer.finished();
        }        
        
        hasStarted = false;
    }
    
    /**
     * The event producer is being told that it is no longer needed and should clean up.
     * This method is meant to get the event producer ready for garbage collection by releasing all of its resources.
     */
    public void dispose(){

    }
    
    /** 
     * The event producer is being told to start creating events.
     * 
     * @param domainSessionStartTime - the epoch time at which the domain session started
     * @throws Exception if the event producer failed to start
     */
    public void start(long domainSessionStartTime) throws Exception{
        
        if(!hasStarted){
            
            this.domainSessionStartTime = domainSessionStartTime;
            
            if(getEventWriter() != null){
                
                //get user id and domain session id
                SensorModule sModule = SensorManager.getInstance().getProducerSensorModule(this);
                boolean writerStarted = writer.initialize(sModule.getUserId(), sModule.getDomainSessionId(), sModule.getExperimentID());
                
                if(writerStarted && writer instanceof AbstractFileWriter){
                    writerFileCreated(((AbstractFileWriter)writer).getFullFileName());
                }
            }
            
            hasStarted = true;
            
        }else{
            throw new Exception("Unable to start "+this+" because it was started earlier and has yet to be stopped.");
        }
    }
    
    /**
     * Notification that this class's writer created a file
     * 
     * @param fileName the name of the file created to write event data too
     */
    protected abstract void writerFileCreated(String fileName);
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AbstractEventProducer:");
        sb.append(" domainSessionStart = ").append(TimeUtil.formatTimeSystemLog(domainSessionStartTime));
        sb.append(", distribute Externally = ").append(shouldDistributeExternally());
        sb.append(", writer = ").append(getEventWriter());        
        sb.append(", producer info = ").append(getEventProducerInformation());
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * This inner class is used to contain information about the data this event producer (i.e. sensor, filter) can create.
     * This class doesn't contain actual values but rather the description of what type of values (i.e. double, string, tuple3d)
     * can be produced for each attribute.
     * 
     * @author mhoffman
     *
     */
    public static class EventProducerInformation{
        
        /** the attributes for an event producer (e.g. Sensor-ABC has attribute point whose type is Tuple3dValue) */
        private List<EventProducerAttribute> attributes = new ArrayList<>();
        
        /**
         * Default constructor
         */
        public EventProducerInformation(){}
        
        /**
         * Add/Register an attribute for the event producer.
         * 
         * @param attribute information about an attribute for an event producer
         */
        public void addAttribute(EventProducerAttribute attribute){
            attributes.add(attribute);
        }
        
        /**
         * Return the collection of attributes for an event producer.
         * 
         * @return List<EventProducerAttribute> collection of attributes added so far
         */
        public List<EventProducerAttribute> getAttributes(){
            return attributes;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[EventProducerInformation: ");
            
            sb.append("attributes = {");
            for(EventProducerAttribute attribute : getAttributes()){
                sb.append(" ").append(attribute).append(",");
            }
            sb.append("}");
            
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * This inner class contains information about a specific attribute (e.g. Sensor-ABC has attribute point) of an event producer.
     * This class doesn't contain actual values but rather the description of what type of values (i.e. double, string, tuple3d)
     * can be produced for each attribute.
     * 
     * @author mhoffman
     *
     */
    public static class EventProducerAttribute{
        
        /** the name of the attribute */
        private SensorAttributeNameEnum name;
        
        /** the container class for values of this attribute for an event producer */
        private Class<? extends AbstractSensorAttributeValue> type;
        
        /**
         * Class constructor - set fields.
         * 
         * @param name the name of the attribute
         * @param type the container class for values of this attribute for an event producer
         */
        public EventProducerAttribute(SensorAttributeNameEnum name, Class<? extends AbstractSensorAttributeValue> type){
            
            if(name == null){
                throw new IllegalArgumentException("The name can't be null.");
            }
            
            this.name = name;
            
            if(type == null){
                throw new IllegalArgumentException("The type can't be null.");
            }
            
            this.type = type;
        }
        
        /**
         * Return the name of the attribute
         * 
         * @return SensorAttributeNameEnum name of the attribute represented by this class
         */
        public SensorAttributeNameEnum getName(){
            return name;
        }
        
        /**
         * Return the the container class for values of this attribute for an event producer
         * 
         * @return Class<? extends AbstractSensorAttributeValue> class that can contain sensor attribute value(s).
         */
        public Class<? extends AbstractSensorAttributeValue> getType(){
            return type;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("{EventProducerAttribute: ");
            sb.append("name = ").append(getName());
            sb.append(", type = ").append(getType());
            sb.append("]");
            return sb.toString();
        }
    }
}
