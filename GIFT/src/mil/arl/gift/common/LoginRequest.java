/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * Represents a request to login to GIFT.
 * 
 * @author mhoffman
 *
 */
public class LoginRequest {

    /** the unique GIFT user id of a user wanting to login (depending on the type of login, this can either 
     * be the normal gift user id (from the gift user table), or the global user id (from the global user table) */
    private int userId;
    
    /** the username of a GIFT user (optional) */
    private String username;
    
    /** The type of request for being used for logging in. */
    private UserSessionType userType = UserSessionType.GIFT_USER;
    
    /**
     * Class constructor
     * 
     * @param userId the unique GIFT user id of a user wanting to login
     */
    public LoginRequest(int userId){
        this.userId = userId;
    }
    
    /**
     * Return the unique GIFT user id of a user wanting to login
     * 
     * @return int
     */
    public int getUserId(){
        return userId;
    }
    
    /**
     * Set the username of a GIFT user
     * 
     * @param username can be null
     */
    public void setUsername(String username){
        this.username = username;
    }
    
    /**
     * Return the username of a GIFT user
     * 
     * @return String can be null
     */
    public String getUsername(){
        return username;
    }
    
    
    /**
     * @return the userType
     */
    public UserSessionType getUserType() {
        return userType;
    }

    /**
     * @param userType the userType to set
     */
    public void setUserType(UserSessionType userType) {
        this.userType = userType;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LoginRequest: ");
        sb.append("userId = ").append(getUserId());
        sb.append(", username = ").append(getUsername());
        sb.append(", userType = ").append(getUserType());
        sb.append("]");
        return sb.toString();
    }
}
