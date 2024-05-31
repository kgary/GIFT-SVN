/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Enumeration of all versions.
 * @author bzahid
 *
 */
public class VersionEnum implements Comparable<VersionEnum> {
        
	/** */
	private static List<VersionEnum> enumList = new ArrayList<VersionEnum>();
	
	/** GIFT version enums */
    public static final VersionEnum VERSION_1_0 = new VersionEnum("1.0", "1.0", "May 2012");
    public static final VersionEnum VERSION_2_0 = new VersionEnum("2.0", "2.0", "Nov 2012");
    public static final VersionEnum VERSION_3_0 = new VersionEnum("3.0", "3.0", "May 2013");
    public static final VersionEnum VERSION_4_0 = new VersionEnum("4.0", "4.0", "Nov 2013");
    public static final VersionEnum VERSION_5_0 = new VersionEnum("2014-2X", "5.0", "Sept 2014");
    public static final VersionEnum VERSION_5_1 = new VersionEnum("2014-3X", "5.1", "Dec 2014");
    public static final VersionEnum VERSION_6_0 = new VersionEnum("2015-1", "6.0", "June 2015");
    public static final VersionEnum VERSION_7_0 = new VersionEnum("2016-1", "7.0", "July 2016");
    public static final VersionEnum VERSION_8_0 = new VersionEnum("2017-1", "8.0", "July 2017");
    public static final VersionEnum VERSION_9_0 = new VersionEnum("2019-1", "9.0", "April 2019");
    public static final VersionEnum VERSION_10_0 = new VersionEnum("2020-1", "10.0", "April 2020");
    public static final VersionEnum VERSION_11_0 = new VersionEnum("2021-1", "11.0", "April 2021");
    public static final VersionEnum VERSION_12_0 = new VersionEnum("2021-2", "11.0", "Nov 2021");
    public static final VersionEnum VERSION_13_0 = new VersionEnum("2022-1", "12.0", "Nov 2022");
    public static final VersionEnum VERSION_14_0 = new VersionEnum("2023-1", "13.0", "Nov 2022");
    
    /** The schema version number (i.e. 6.0) */
    private String schemaVersion = null;
    
    /** The name of the version (i.e. 2015-1)*/
    private String name = null;
    
    /** a date string for the release of that version of GIFT */
    private String releaseDate = null;
    
    
    private VersionEnum(String name, String schemaVersion, String releaseDate) {    	
    	this.name = name;
    	this.schemaVersion = schemaVersion;
    	this.releaseDate = releaseDate;
    	enumList.add(this);    	
    }
	
    /**
     * Return the version that has the matching name.
     * @param name The name of the version to find.
     * @return The matching enumeration.
     * @throws RuntimeException if an enumeration with a matching name is not found.
     */
    public static VersionEnum valueOf(String name) throws RuntimeException {
    	for(VersionEnum v : enumList) {
    		if(name.equals(v.getSchemaVersion()) || name.equals(v.getName())) {
    			return v;
    		}
    	}
    	throw new RuntimeException("No version enumeration for version named '" + name + "' found.");    	
    }
    
    /**
     * Gets the schema version
     * 
     * @return String
     */
    public String getSchemaVersion() {
    	return schemaVersion;
    }
    
    /**
     * Gets the unique name of this version
     * e.g. "2014-2", i.e. the 2nd release of 2014.
     * 
     * @return String
     */
    public String getName() {
    	return name;
    }
    
    /**
     * Gets the release date for this version
     * 
     * @return "Nov 2014"
     */
    public String getReleaseDate(){
        return releaseDate;
    }
        
    /**
     * Converts the version to a Double
     * 
     * @return Double
     */
    public Double getVersionAsDouble() {
    	return Double.parseDouble(schemaVersion);
    }
        
    /**
     * Gets the previous version. Returns null if the current version is 1.0
     * 
     * @return VersionEnum - the previous version or null if this version is 1.0
     */
    public VersionEnum getPreviousVersion() {    	
    	Collections.sort(enumList);    	
    	try {
    		return enumList.get(enumList.indexOf(this) - 1);
    	} catch (@SuppressWarnings("unused") IndexOutOfBoundsException e) {
    		return null;
    	}
    }
    
    /**
     * Gets the next version. Returns null if the current version is the latest
     * 
     * @return VersionEnum - the next version or null if this version is the latest
     */
    public VersionEnum getNextVersion() {       
        Collections.sort(enumList);     
        try {
            return enumList.get(enumList.indexOf(this) + 1);
        } catch (@SuppressWarnings("unused") IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Returns a sorted list of all version enumerations
     * @return the version values
     */
    public static final List<VersionEnum> VALUES() {
    	Collections.sort(enumList);
    	return Collections.unmodifiableList(enumList);
    }
    
	@Override
	public int compareTo(VersionEnum other) {
		// if this version should come before the other, return -1
		// if they are equal return 0
		// otherwise, the other should come before this version, return 1
		return (this.getVersionAsDouble() < other.getVersionAsDouble()) ? -1 : 
			(this.getVersionAsDouble() == other.getVersionAsDouble()) ? 0 : 1;
	}
	
	@Override
    public String toString(){
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("[Version: ");
	    sb.append("name = ").append(getName());
	    sb.append(", schema = ").append(getSchemaVersion());
	    sb.append(", date = ").append(getReleaseDate());
	    sb.append("]");
	    return sb.toString();
	}
}
