/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.writer;

import java.io.IOException;

import mil.arl.gift.sensor.AbstractEventProducer.EventProducerInformation;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.SensorDataEventListener;
import mil.arl.gift.sensor.filter.SensorFilterEvent;
import mil.arl.gift.sensor.filter.SensorFilterEventListener;

/**
 * This is the base class for all writers in the sensor package.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractWriter implements SensorDataEventListener, SensorFilterEventListener {
    
    /** whether the writer has written anything yet */
    protected boolean haveWrittenSomething = false;
    
    /** contains information about the attributes that the producer could create and this writer needs to be able to handle */    
    protected EventProducerInformation eventProducerInformation;
    
    /**
     * Class constructor - set attribute(s).
     * 
     * @param eventProducerInformation contains information about the attributes that the producer could create and 
     * this writer needs to be able to handle 
     */
    public AbstractWriter(EventProducerInformation eventProducerInformation){
        
        if(eventProducerInformation == null){
            this.eventProducerInformation = new EventProducerInformation();
        }else{
            this.eventProducerInformation = eventProducerInformation;
        }
    }
    
    /**
     * Write the sensor filter event data to the writer
     * 
     * @param sensorFilterEvent the filter event containing data to write
     * @throws IOException if there was a severe problem writing the data 
     */
    protected abstract void write(SensorFilterEvent sensorFilterEvent) throws IOException;
    
    /**
     * Write the sensor data event data to the writer
     * 
     * @param sensorDataEvent the raw sensor event containing data to write
     * @throws IOException if there was a severe problem writing the data
     */
    protected abstract void write(SensorDataEvent sensorDataEvent) throws IOException;
    
    /**
     * Write the string to the writer
     * 
     * @param line a line of text to write 
     */
    public abstract void write(String line);
    
    /**
     * The writer stream should be initialized now that a user id and domain session id are available
     * 
     * @param userId unique user id associated with this writer instance
     * @param domainSessionId unique domain session id associated with this writer instance
     * @param experimentID an optional, unique experiment ID associated with this writer instance. Can be null.
     * @return boolean whether or not the writer was successfully initialized
     */
    public abstract boolean initialize(int userId, int domainSessionId, String experimentID);
    
    /**
     * The writer stream is no longer needed and should be closed or terminated accordingly.
     */
    public abstract void finished();
}
