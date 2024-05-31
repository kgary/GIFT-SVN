/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A wrapper class used to ensure that DetailedExceptions passed from the server end are RPC-compliant and can be used on the client end.
 * <br/><br/>
 * Due to the way GWT's serialization and symbolization policies work, stack traces passed from the server end will likely be obfuscated 
 * when they are passed back to the client end, making them unusable. Stack traces may also contain references to classes that aren't 
 * supported by GWT's client-side serialization, which will prevent GWT from sending responses containing those references back to 
 * the client end.
 * <br/><br/>
 * The simplest way around this is to wrap the stack trace up as a collection of strings so that it won't be obfuscated and will only
 * use client-safe classes, which is why this class exists.
 * 
 * @author nroberts
 */
public class DetailedExceptionSerializedWrapper implements Serializable{

    private static final long serialVersionUID = 1L;

    private String reason;
    
    private String details;
    
    private ArrayList<String> stackTrace;
    
    /**
     * A no-argument constructor required by GWT's RPC service for serialization
     */
    @SuppressWarnings("unused")
	private DetailedExceptionSerializedWrapper(){
    	
    }
    
    /**
     * Wraps the given DetailedException so that it can be reliably passed in client-server communications
     * 
     * @param e the DetailedException to wrap
     */
    public DetailedExceptionSerializedWrapper(DetailedException e){
    	
    	this.reason = e.getReason();
    	this.details = e.getDetails();
    	this.stackTrace = e.getErrorStackTrace();
    }
    
    /**
     * Returns the exception's message
     * 
     * @return the exception's message
     */
    public String getMessage(){
        return details;
    }    
    
    /**
     * Return the user friendly information about the exception.
     * 
     * @return won't be null
     */
    public String getReason(){
        return reason;
    }
    
    /**
     * Returns a gwt-friendly stack trace
     * 
     * @return a representation of the stack trace.  Won't be null but can be empty if the
     * cause value is null for this class.
     */
    public ArrayList<String> getErrorStackTrace() {
    	
    	return stackTrace;   	
    }
    
    /**
     * Return the developer friendly information about the exception.
     * 
     * @return won't be null
     */
    public String getDetails(){
        return details;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[DetailedExceptionRpcWrapper: ");
        sb.append("\nreason = ").append(getReason());
        sb.append("\ndetails = ").append(getDetails());
        sb.append("]");
        return sb.toString();
    }
}
