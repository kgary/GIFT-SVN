/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.util.ArrayList;

/**
 * This exception is used to provide a breakdown of the exception by including
 * a reason and details of the exception.
 * 
 * @author mhoffman
 *
 */
public class DetailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private String reason;
    
    private String details;
    
    /**
     * No-arg constructor needed by GWT RPC. This constructor does not create a valid instance of this class and should not be used 
     * under most circumstances
     */
	protected DetailedException(){
    	
    }
    
    /**
     * Set attributes
     * 
     * @param reason user friendly information about the exception.  Can't be null.
     * @param details developer friendly information about the exception. Can't be null or empty.
     * @param cause the exception that caused this exception to be created.  Can be null.
     */
    public DetailedException(String reason, String details, Throwable cause){
        super(cause);
        
        if(reason == null || reason.isEmpty()){
            throw new IllegalArgumentException("The reason can't be null or empty.");
        }else if(details == null || details.isEmpty()){
            //added the reason value to this exception just in case the caller to this constructor isn't handling exceptions
            //which will show as an obfuscated stacktrace on gwt clients
            throw new IllegalArgumentException("The details can't be null or empty.  The reason value for this object is: "+reason);
        }
        
        this.reason = reason;
        this.details = details;
    } 
    
    @Override
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
     * Return the developer friendly information about the exception.
     * 
     * @return won't be null
     */
    public String getDetails(){
        return details;
    }
    
    /**
     * Returns a gwt-friendly stack trace.<br/>
     * Note: this shouldn't be called on the GWT client.
     * 
     * @return a representation of the stack trace.  Won't be null but can be empty if the
     * cause value is null for this class.
     */
    public ArrayList<String> getErrorStackTrace() {
    	
    	return getFullStackTrace(getCause());
    	
    }
    
    /**
	 * Goes down the tree of causes for the given Throwable and retrieves the stack
	 * trace of each one of them, so they can then be compiled to a list in order on the dialog box.<br/>
	 * Note: this shouldn't be called on the GWT client.
	 *
	 * @param thrown The final Throwable down the chain of exceptions
	 * @return the full stack trace of each exception, followed by the exception that caused it
	 * 			with its own stack trace, all the way to the end of the chain
	 */
	public static ArrayList<String> getFullStackTrace(Throwable thrown){
		ArrayList<String> stackTrace = new ArrayList<String>();
		Throwable currentThrow = thrown; 
		while(currentThrow != null){
			if(currentThrow.getStackTrace() != null) {
				stackTrace.add((currentThrow == thrown ? "" : "caused by ") + currentThrow.toString());
				
				for(StackTraceElement element : currentThrow.getStackTrace()) {
					stackTrace.add("at " + element.toString());
				}
			}
			currentThrow = (currentThrow.getCause() == null ? null : currentThrow.getCause());
		}
		return stackTrace;
	}
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[DetailedException: ");
        sb.append("\nreason = ").append(getReason());
        sb.append("\ndetails = ").append(getDetails());
        sb.append("]");
        return sb.toString();
    }
}