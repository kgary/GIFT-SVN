/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor;

import generated.sensor.Filter;
import generated.sensor.Sensor;
import generated.sensor.SensorsConfiguration;
import generated.sensor.Writer;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Contains a bunch of useful methods that operate on the
 * SensorsConfiguration, Sensor, Writer, and Filter classes. Ideally these
 * methods would be defined in their respective classes but since the classes
 * are auto-generated we're left with this utility class.
 * @author elafave
 *
 */
public class SensorsConfigurationUtility {
	
	/**
	 * Removes all filters that aren't referenced by a sensor and then removes
	 * all writers that aren't referenced by either a sensor or a filter.
	 * @param sensorsConfiguration SensorConfiguration that (possibly) contains
	 * filters and writers that are essentially orphan children and need to be
	 * removed.
	 */
	static public void removeUnreferencedFiltersAndWriters(SensorsConfiguration sensorsConfiguration) {
		removeUnreferencedFilters(sensorsConfiguration);
		removeUnreferencedWriters(sensorsConfiguration);
	}

	/**
	 * Removes all filters that aren't referenced by a sensor.
	 * @param sensorsConfiguration SensorConfiguration that (possibly) contains
	 * filters that are essentially orphan children and need to be removed.
	 */
	static public void removeUnreferencedFilters(SensorsConfiguration sensorsConfiguration) {
		//Create a map from each filterId to the corresponding Filter.
		HashMap<BigInteger, Filter> idToFilter = new HashMap<BigInteger, Filter>();
		List<Filter> filters = sensorsConfiguration.getFilters().getFilter();
    	for(Filter filter : filters) {
    		idToFilter.put(filter.getId(), filter);
    	}
    	
    	//Remove every Filter reference from the map so we're left with
    	//just the unreferenced filters.
    	List<Sensor> sensors = sensorsConfiguration.getSensors().getSensor();
    	for(Sensor sensor : sensors) {
    		idToFilter.remove(sensor.getFilterInstance());
    	}
    	
    	//Remove the unreferenced filters from the list.
    	Collection<Filter> unreferencedFilters = idToFilter.values();
    	filters.removeAll(unreferencedFilters);
	}
	
	/**
	 * Removes all writers that aren't referenced by either a sensor or a
	 * filter.
	 * @param sensorsConfiguration SensorConfiguration that (possibly) contains
	 * writers that are essentially orphan children and need to be removed.
	 */
	static public void removeUnreferencedWriters(SensorsConfiguration sensorsConfiguration) {
		//Create a map from each writerId to the corresponding Writer.
		HashMap<BigInteger, Writer> idToWriter = new HashMap<BigInteger, Writer>();
		List<Writer> writers = sensorsConfiguration.getWriters().getWriter();
    	for(Writer writer : writers) {
    		idToWriter.put(writer.getId(), writer);
    	}
    	
    	//Remove every Writer reference from the map so we're left with
    	//just the unreferenced filters.
    	List<Sensor> sensors = sensorsConfiguration.getSensors().getSensor();
    	for(Sensor sensor : sensors) {
    		idToWriter.remove(sensor.getWriterInstance());
    	}
    	List<Filter> filters = sensorsConfiguration.getFilters().getFilter();
    	for(Filter filter : filters) {
    		idToWriter.remove(filter.getWriterInstance());
    	}
    	
    	//Remove the unreferenced writers from the list.
    	Collection<Writer> unreferencedWriters = idToWriter.values();
    	writers.removeAll(unreferencedWriters);
	}
	
	/**
	 * Searches the SensorsConfiguration object for a Filter with the given
	 * filterId and returns it. If no Filter could be found then null is
	 * returned.
	 * @param sensorsConfiguration SensorConfiguration that (possibly) contains
	 * the Filter being searched for.
	 * @param filterId ID of the Filter to find.
	 * @return Filter with the given filterId if it could be found, null
	 * otherwise.
	 */
	static public Filter getFilter(SensorsConfiguration sensorsConfiguration, BigInteger filterId) {
		List<Filter> filters = sensorsConfiguration.getFilters().getFilter();
    	for(Filter filter : filters) {
    		if(filter.getId().equals(filterId)) {
    			return filter;
    		}
    	}
    	return null;
	}
	
	/**
	 * Searches the SensorsConfiguration object for a Writer with the given
	 * writerId and returns it. If no Writer could be found then null is
	 * returned.
	 * @param sensorsConfiguration SensorConfiguration that (possibly) contains
	 * the Writer being searched for.
	 * @param writerId ID of the Writer to find.
	 * @return Writer with the given writerId if it could be found, null
	 * otherwise.
	 */
	static public Writer getWriter(SensorsConfiguration sensorsConfiguration, BigInteger writerId) {
		List<Writer> writers = sensorsConfiguration.getWriters().getWriter();
    	for(Writer writer : writers) {
    		if(writer.getId().equals(writerId)) {
    			return writer;
    		}
    	}
    	return null;
	}
	
	/**
	 * Determines if more than one node (Sensor or Filter) references the
	 * Writer identified by writerId.
	 * @param sensorsConfiguration SensorsConfiguration that contains the
	 * Writer identified by writerId and other nodes (Sensors or Filters) that
	 * may reference the Writer.
	 * @param writerId ID of the Writer in question.
	 * @return True if more than one node (Sensor of Filter) reference the
	 * Writer in question, false otherwise.
	 */
	static public boolean isWriterReferencedMoreThanOnce(SensorsConfiguration sensorsConfiguration, BigInteger writerId) {
		int count = 0;
		List<Sensor> sensors = sensorsConfiguration.getSensors().getSensor();
    	for(Sensor sensor : sensors) {
    		BigInteger sensorsWriterId = sensor.getWriterInstance();
    		if(sensorsWriterId != null && sensorsWriterId.equals(writerId)) {
    			if(count == 1) {
    				return true;
    			}
    			count++;
    		}
    	}
    	List<Filter> filters = sensorsConfiguration.getFilters().getFilter();
    	for(Filter filter : filters) {
    		BigInteger filtersWriterId = filter.getWriterInstance();
    		if(filtersWriterId != null && filtersWriterId.equals(writerId)) {
    			if(count == 1) {
    				return true;
    			}
    			count++;
    		}
    	}
    	return false;
	}
	
	/**
	 * Determines if more than one Sensor references the Filter identified by
	 * filterId.
	 * @param sensorsConfiguration SensorsConfiguration that contains the
	 * Filter identified by filterId and Sensors that may reference it.
	 * @param filterId ID of the Filter in question.
	 * @return True if more than one Sensor references the Filter in question,
	 * false otherwise.
	 */
	static public boolean isFilterReferencedMoreThanOnce(SensorsConfiguration sensorsConfiguration, BigInteger filterId) {
		int count = 0;
		List<Sensor> sensors = sensorsConfiguration.getSensors().getSensor();
    	for(Sensor sensor : sensors) {
    		BigInteger sensorsFilterId = sensor.getFilterInstance();
    		if(sensorsFilterId != null && sensorsFilterId.equals(filterId)) {
    			if(count == 1) {
    				return true;
    			}
    			count++;
    		}
    	}
    	return false;
	}
}
