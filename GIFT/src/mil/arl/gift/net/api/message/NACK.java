/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class represents a NACK (Not Acknowledged) message.  A NACK is a negative
 * response to another network message and is usually caused by an error on the receiving side.
 * 
 * @author mhoffman
 *
 */
public class NACK {
	
	/** an enumerated error that categorizes the reason for the NACK message */
	private ErrorEnum errorEnum = null;

	/** a message describing the reason for the 'not acknowledged' */
	private String errorMsg = null;
	
	/** (optional) information to help fix or work-around the issue causing this NACK */
	private String errorHelp = null;
	
	/**
	 * Class constructor
	 * 
	 * @param errorEnum - an enumerated error that categorizes the reason for the NACK message. Can't be null or empty.
	 * @param errorMsg - a message describing the reason for the 'not acknowledged'. Can't be null.
	 */
	public NACK(ErrorEnum errorEnum, String errorMsg){
	    
	    if(errorEnum == null){
	        throw new IllegalArgumentException("The error enum is null");
	    }else if(StringUtils.isBlank(errorMsg)){
	        throw new IllegalArgumentException("The error message is null or empty");
	    }
		
		this.errorEnum = errorEnum;
		this.errorMsg = errorMsg;
	}
	
	/**
	 * Return an enumerated error that categorizes the reason for the NACK message
	 * 
	 * @return won't be null
	 */
	public ErrorEnum getErrorEnum(){
		return errorEnum;
	}
	
	/**
	 * Return the message describing the reason for the 'not acknowledged'
	 * 
	 * @return the error message, won't be null or empty.
	 */
	public String getErrorMessage(){
		return errorMsg;
	}
	
	/**
	 * Return the information to help fix or work-around the issue causing this NACK
	 * 
	 * @return can be null or empty if there is no additional helpful information set.
	 */
	public String getErrorHelp(){
	    return errorHelp;
	}
	
	/**
	 * Set the error help information that is an additional description to the error message.
	 *  
	 * @param errorHelp can be null or empty as this is optional
	 */
	public void setErrorHelp(String errorHelp){
	    this.errorHelp = errorHelp;
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[NACK: ");
	    sb.append(" type = ").append(getErrorEnum());
	    sb.append(", message = ").append(getErrorMessage());
	    sb.append(", help = ").append(getErrorHelp());
	    sb.append("]");
		
		return sb.toString();
	}
}
