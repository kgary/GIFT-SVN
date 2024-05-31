/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

/**
 * This class contains information about an LMS connection.
 * 
 * @author mhoffman
 *
 */
public class LMSConnectionInfo implements Serializable {



    private static final long serialVersionUID = 8019744606732545705L;
    
    /** the LMS connection name */
    private String name;
    
    /**
     * Default constructor - needed for gwt serialization.
     */
    public LMSConnectionInfo() {
        
    }
    /**
     * Class constructor - set attributes
     * 
     * @param name the LMS connection name
     */
    public LMSConnectionInfo(String name){
        setName(name);
    }
    
    private void setName(String name){
        
        if(name == null){
            throw new IllegalArgumentException("The name can't be null");
        }
        
        this.name = name;
    }
    
    /**
     * Return the name of the LMS connection.
     * 
     * @return String the name value
     */
    public String getName(){
        return name;
    }
    
    @Override
    public int hashCode() { 
        return getName().hashCode(); 
    }
    
    @Override
    public boolean equals(Object otherInfo){
        return otherInfo != null && otherInfo instanceof LMSConnectionInfo &&
                ((LMSConnectionInfo)otherInfo).getName().equals(this.getName());
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LMSConnectionInfo: ");
        sb.append("name = ").append(getName());
        sb.append("]");
        
        return sb.toString();
    }
}
