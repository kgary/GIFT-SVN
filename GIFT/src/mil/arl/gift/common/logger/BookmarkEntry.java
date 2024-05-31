/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.logger;

/**
 * A bookmark signifies an event of interest during a lesson and is normally created
 * by an instructor/operator using the monitor.
 * 
 * @author mhoffman
 *
 */
public class BookmarkEntry {

    /** the time stamp associated with this entry */
    private long time;
    
    /** the domain session time stamp associated with this entry */
    private Double ds_time;
    
    /** contains information about the entry */
    private String annotation;
    
    /**
     * Class constructor - set attributes
     * 
     * @param time the time stamp associated with this entry
     * @param annotation contains information about the entry
     */
    public BookmarkEntry(long time, String annotation){
        this.time = time;
        this.annotation = annotation;
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param time the time stamp associated with this entry
     * @param dsTime the domain session time stamp associated with this entry
     * @param annotation contains information about the entry
     */
    public BookmarkEntry(long time, double dsTime, String annotation){
        this.time = time;
        this.ds_time = dsTime;
        this.annotation = annotation;
    }
    
    /**
     * Return the time at which this bookmark was created
     * 
     * @return long
     */
    public long getTime(){
        return time;
    }
    
    /**
     * Return the elapsed domain session time at which this bookmark was created
     * 
     * @return double - can be null if a domain session time is not known (legacy bookmark entry support).
     */
    public Double getDomainSessionTime(){
        return ds_time;
    }
    
    /**
     * Return the bookmark's annotation
     * 
     * @return String
     */
    public String getAnnotation(){
        return annotation;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[BookmarkEntry: ");
        sb.append("time = ").append(getTime());
        sb.append(", domainSessionTime = ").append(getDomainSessionTime());
        sb.append(", annotation = ").append(getAnnotation());
        sb.append("]");
        return sb.toString();
    }
}
