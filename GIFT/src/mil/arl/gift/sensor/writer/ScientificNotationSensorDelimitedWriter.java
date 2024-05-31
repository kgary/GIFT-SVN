/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.writer;

import java.text.DecimalFormat;

import mil.arl.gift.sensor.AbstractEventProducer.EventProducerInformation;


/**
 * This class is responsible for writing sensor and/or filtered data to a file using a string delimited encoding.
 * 
 * @author mhoffman
 *
 */
public class ScientificNotationSensorDelimitedWriter extends GenericSensorDelimitedWriter{
	
    /** format used for attribute values */
    private static final DecimalFormat attributeFormat = new DecimalFormat("0.###############E0");
	
	/**
	 * Class constructor - configure using the writer configuration input for this writer
	 * 
	 * @param configuration parameters to configure this writer
     * @param eventProducerInformation attributes for which data will be written for  
	 */
	public ScientificNotationSensorDelimitedWriter(generated.sensor.GenericSensorDelimitedWriter configuration, EventProducerInformation eventProducerInformation){
		super(configuration, eventProducerInformation);
	}
	
	@Override
	protected DecimalFormat getAttributeFormat(){
	    return attributeFormat;
	}

}
