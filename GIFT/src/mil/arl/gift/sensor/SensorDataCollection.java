/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.io.TimeUtil;

/**
 * This class contains a collection of sensor data objects for a single point in time.  It represents
 * a single snapshot of the sensor and its attributes.  This class is useful for sensors which can produce
 * multiple readings of attributes from a single query of that sensor (e.g. Emotiv)
 * 
 * @author mhoffman
 *
 */
public class SensorDataCollection extends AbstractSensorData {

    /** collection of sensor data attribute to value */
    private List<SensorData> sensorDatas;

	/**
     * Class constructor - populate attributes
     * 
     * @param sensorDatas collection of sensor data attribute to value
     * @param elapsedTime - total number of milliseconds since the start of the domain session
     */
    public SensorDataCollection(List<SensorData> sensorDatas, long elapsedTime){
        super(elapsedTime);
        
        this.sensorDatas = Collections.unmodifiableList(sensorDatas);
    }


    /**
     * Return the collection of Sensor Data for the sensor at the elapsed time
     * 
     * @return List<SensorData>
     */
	public List<SensorData> getSensorDatas() {
		return sensorDatas;
	}
	
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SensorDataCollection: ");
        sb.append(" elapsed time = ").append(TimeUtil.formatTimeSystemLog(getElapsedTime()));
        sb.append(" SensorDatas = {");
        
        for(SensorData sData : getSensorDatas()){
            sb.append(sData).append(",");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
