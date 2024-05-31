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

import mil.arl.gift.common.EnumerationNotFoundException;

/**
 * Enumeration of the various errors which happen due to modules interacting
 * with each other.
 * 
 * @author mhoffman
 *
 */
public class ErrorEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<ErrorEnum> enumList = new ArrayList<ErrorEnum>(12);
    private static int index = 0;

    public static final ErrorEnum MALFORMED_DATA_ERROR              = new ErrorEnum("MalformedDataError", "Malformed data");
    public static final ErrorEnum USER_NOT_FOUND_ERROR              = new ErrorEnum("UserNotFoundError", "User was not found");
    public static final ErrorEnum INCORRECT_CREDENTIALS             = new ErrorEnum("IncorrectCredentials", "Incorrect Credentials");
    public static final ErrorEnum DB_INSERT_ERROR                   = new ErrorEnum("DBInsertError", "DB Insert failed");
    public static final ErrorEnum DB_UPDATE_ERROR                   = new ErrorEnum("DBUpdateError", "DB Update failed");
    public static final ErrorEnum LMS_RETRIEVE_ERROR                = new ErrorEnum("LmsRetrieveError","LMS Retrieve Error");
    public static final ErrorEnum LEARNER_INSTANTIATED_ERROR        = new ErrorEnum("LearnerInstantiatedError", "Learner Instantiation failed");
    public static final ErrorEnum DOMAIN_SESSION_NOT_FOUND_ERROR    = new ErrorEnum("DomainSessionNotFoundError", "Domain Session Not Found");
    public static final ErrorEnum MESSAGE_TIMEOUT_ERROR             = new ErrorEnum("MessageTimeoutError", "Message Timeout");
    public static final ErrorEnum OPERATION_FAILED                  = new ErrorEnum("OperationFailedError", "Operation Failed");
    public static final ErrorEnum MODULE_NOT_FOUND                  = new ErrorEnum("ModuleNotFound", "Module Not Found");
    public static final ErrorEnum UNHANDLED_MESSAGE_ERROR           = new ErrorEnum("UnhandledMessageError", "Unhandled Message Error");
    public static final ErrorEnum GET_SURVEY_ERROR                  = new ErrorEnum("GetSurveyError", "Get Survey Error");
    
    private ErrorEnum(String name, String displayName){
    	super(index++, name, displayName);
    	enumList.add(this);
    }
    
    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static ErrorEnum valueOf(String name)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         value is not found.
     */
    public static ErrorEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<ErrorEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }

}
