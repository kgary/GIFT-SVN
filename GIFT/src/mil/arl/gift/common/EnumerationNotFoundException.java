/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This exception is used to notify when an exception does not exist
 * 
 * @author mhoffman
 *
 */
public class EnumerationNotFoundException extends EnumException {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * (optional) the name of a enumeration that doesn't exist and is
     * the value that caused this exception
     */
    private String missingValue = null;

    /**
     * Create an exception with the specified message.
     *
     * @param msg The message assigned to this exception.
     * @param missingValue the name of a enumeration that doesn't exist and is
     * the value that caused this exception.  Can be null or empty.
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EnumerationNotFoundException(String msg, String missingValue, Throwable cause) {
        super(msg, cause);
        
        this.missingValue = missingValue;
    }

    /**
     * Create an exception with a string constructed from the parameters.
     *
     * @param enumClassName The name of the class.
     * @param enumValue the value of the enumeration.
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EnumerationNotFoundException(String enumClassName,
                                        int enumValue, Throwable cause) {
        this(enumClassName + " does not contain a matching enumeration for" +
             " value ==  \"" + enumValue + "\"", null, cause);
    }
    
    /**
     * Return the name of a enumeration that doesn't exist and is
     * the value that caused this exception
     * 
     * @return can be null or empty if not set.
     */
    public String getMissingValue(){
        return missingValue;
    }

}
