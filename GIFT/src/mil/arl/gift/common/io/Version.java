/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.File;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * This class contains the version properties.
 * 
 * @author mhoffman
 *
 */
public class Version extends CommonProperties {
    
    /** property key labels */
    private static final String SCHEMA_VERSION = "schemaVersion";
    private static final String DATE = "releaseDate";
    private static final String NAME = "name";
    private static final String DOCUMENTATION = "documentation";
    
    private static final String VERSION_FILENAME = "version.txt";
    
    private static final FastDateFormat systemLogFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm z", null, null);    
    private String buildDate = null;
    
    /** the location of the folder containing GIFT and Domain folders */
    private static final File buildPath = new File("..");
    
    /** the singleton instance of this class */
    private static Version instance = null;
    
    /**
     * Return the singleton instance of this class.
     * 
     * @return Version
     */
    public static synchronized Version getInstance(){
        
        if(instance == null){
            instance = new Version();
        }
        
        return instance;
    }

    private Version() {
        super(VERSION_FILENAME);
        
    }
    
    /**
     * Get the current schema version property
     * 
     * @return the schema version of this gift version
     */
    public String getCurrentSchemaVersion(){
        return getPropertyValue(SCHEMA_VERSION);
    }
    
    /**
     * Get the release date of this version
     * 
     * @return the release date of this version
     */
    public String getReleaseDate(){
        return getPropertyValue(DATE);
    }
    
    /**
     * Get the name of this version
     * e.g. "2014-2", i.e. the 2nd release of 2014.
     * 
     * @return the name of this version
     */
    public String getName(){
        return getPropertyValue(NAME);
    }
    
    /**
     * Get the documentation token to use in the URLs to the GIFT portal
     * wiki pages that contain documentation for this GIFT instance.
     * 
     * @return the documentation token to use in URLs
     */
    public String getDocumentationToken(){
        return getPropertyValue(DOCUMENTATION);
    }
    
    /**
     * Return the build date of this GIFT instance (latest modified file in the GIFT\bin directory). 
     * 
     * @return build date in the format of 'yyyy:MM:dd HH:mm z'
     */
    public String getBuildDate(){
        
        if(buildDate == null){            
            long lastModifiedEpoch = FileUtil.getLastModifiedDate(new File("bin"));
            buildDate = systemLogFormat.format(lastModifiedEpoch);
        }
        
        return buildDate;
    }
    
    /**
     * Return the location of the build
     * @return the path to this GIFT instance on this computer (e.g. C:/work/GIFT 2021-1/)
     */
    public String getBuildLocation(){
        try{
            return buildPath.getCanonicalPath();
        }catch(@SuppressWarnings("unused") Exception e){
            return buildPath.getAbsolutePath();
        }
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[Version: ");
        sb.append("name = ").append(getName());
        sb.append(", schema = ").append(getCurrentSchemaVersion());
        sb.append(", documentation = ").append(getDocumentationToken());
        sb.append(", date = ").append(getReleaseDate());
        sb.append("]");
        return sb.toString();
    }

}
