/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server.authentication;

/**
 * This exception is used when there is a specific user authentication problem.
 *  
 * @author mhoffman
 *
 */
public class UserAuthenticationException extends Exception {

    /**
     * default serial version id
     */
    private static final long serialVersionUID = 1L;
    
    
    /** a user friendly message about the problem */
    private String authenticationProblem;
    
    /**
     * Class constructor - add a cause of the authentication exception
     * 
     * @param authenticationProblem the user friendly message about the authentication problem. Can't be null.
     */
    public UserAuthenticationException(String authenticationProblem){
    	setAuthenticationProblem(authenticationProblem);
    }
    
    /**
     * Class constructor - add a cause of the authentication exception
     * 
     * @param authenticationProblem the user friendly message about the authentication problem. Can't be null.
     * @param cause the exception that caused an authentication problem
     */
    public UserAuthenticationException(String authenticationProblem, Exception cause){        
    	super(cause);
    	setAuthenticationProblem(authenticationProblem);
    }
    
   /**
    * Set a user friendly message about the authentication problem that occurred.
    * 
    * @param authenticationProblem the user friendly message about the authentication problem. Can't be null.
    */
    private void setAuthenticationProblem(String authenticationProblem) {
    	
    	if(authenticationProblem == null){
            throw new IllegalArgumentException("The authentication problem can't be null.");
        }
        
        this.authenticationProblem = authenticationProblem;
    }
    
    /**
     * Return the user friendly message about the authentication problem that occurred.
     * 
     * @return String
     */
    public String getAuthenticationProblem(){
        return authenticationProblem;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[UserAuthenticationException: ");
        sb.append(" authenticationProblem = ").append(getAuthenticationProblem());
        sb.append(", message = ").append(getMessage());
        sb.append("]");
        return sb.toString();
    }
}
