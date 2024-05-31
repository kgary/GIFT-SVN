/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * During a conversion process, a file might contain elements that are no longer supported
 * by GIFT and are unable to be migrated. This singleton object is used to tell the user
 * about these elements.  When a file is being converted and an unmigratable element is found, 
 * call the addIssue(String elementName) method to identify that element to the user.
 * 
 * <br><br>Information is used in the LinkedHashMap like so:
 * LinkedHashMap<<i>file name</i>, HashMap<<i>element name</i>, <i>occurrences</i>>>
 *
 */
public class ConversionIssueList extends LinkedHashMap<String, HashMap<String, Integer>> {
    
    private static final long serialVersionUID = 1L;

	private boolean isPopulated = false;	

	public ConversionIssueList() { }
	
	/**
	 * Used to add an unmigratable element to the list for reporting. 
	 *  
	 * If there are multiple occurrences of an element within the file, you should call this
	 * method for each occurrence.  In this case, just use an identical elementInfo parameter 
	 * and the method will automatically display the amount of occurrences of the element. 
	 * 
	 * @param elementName The name of the element that couldn't be converted. This string should
	 * be detailed enough to allow the user to identify the element within the file.
	 * This string is handled as HTML so any HTML tags can be used. If a line break is desired,
	 * use &ltbr&gt. 
	 * <b>Do not include the &lthtml&gt or &ltbody&gt tags in the string.</b>
	 */
	public void addIssue(String elementName) {
		
		// Iterate though the map to the most recently added key-value pair, 
		// which represents the file that is currently being converted.
		Set<Map.Entry<String, HashMap<String, Integer>>> set = this.entrySet();
		Iterator<Map.Entry<String, HashMap<String, Integer>>> i = set.iterator();
		Map.Entry<String, HashMap<String, Integer>> entry = null;
		
		
		try{
			entry = i.next();
			while (i.hasNext()) {
				entry = i.next();
			}
		}catch(@SuppressWarnings("unused") NoSuchElementException e){
			HashMap<String, Integer> temp = new HashMap<String, Integer>();
			temp.put("NoSuchElementException", 1);
			entry = new AbstractMap.SimpleEntry<String, HashMap<String,Integer>>("entry is null", temp);
		}

		// entry is a key-value pair where the key represents the file that is currently
		// being converted and the value is the list of unmigratable elements within that file.
		entry = handleEntry(entry, elementName);
		 
		isPopulated = true;
	}
	
	/**
	 * This method properly updates the Map based on what element has been found
	 * 
	 * @param entry a map that represents the file currently being converted and its 
	 * collection of issues that have been encountered thus far
	 * @param elementInfo the name of the element that can't be migrated
	 * @return the updated map
	 */
	private Map.Entry<String, HashMap<String, Integer>> handleEntry(
			Map.Entry<String, HashMap<String, Integer>> entry, String elementInfo) {
		
		Iterator<Entry<String, Integer>> i = entry.getValue().entrySet().iterator();
		
		// Determine if the element has already occurred within the file
		boolean foundExistingElement= false;
		while (i.hasNext()) {
			Map.Entry<String, Integer> pair = i.next();
			
			if (pair.getKey().equals(elementInfo)) {
				// The element has already occurred within the file, increase the amount
				// of occurrences by 1.
				foundExistingElement = true;
				
				pair.setValue(pair.getValue() + 1);
				
				break;
			}
		}
		
		if (!foundExistingElement) {
			// This is a new element, insert it as a new map entry.
			entry.getValue().put(elementInfo, 1);
		}
		
		return entry;
	}
	
	/**
	 * Used to determine if issues were found during conversion.
	 * 
	 * @return true if issues were found, false otherwise
	 */
	public boolean isPopulated() {
		return this.isPopulated;
	}
	
	/**
	 * Empties all contents from the ConversionIssueList.
	 * 
	 * This method should be called at the end of the final ConversionWizardUtil execution
	 * in case the user wants to perform a new conversion.
	 * 
	 */
	@Override
	public void clear() {
		super.clear();
		
		isPopulated = false;
	}
}
