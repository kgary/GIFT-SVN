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
 * Enumeration of the various GiFT encoding types
 * 
 * @author jleonard
 *
 */
public class MessageEncodingTypeEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<MessageEncodingTypeEnum> enumList = new ArrayList<MessageEncodingTypeEnum>(1);
    private static int index = 0;

    public static final MessageEncodingTypeEnum JSON = new MessageEncodingTypeEnum("JSON", "JSON");
    public static final MessageEncodingTypeEnum BINARY = new MessageEncodingTypeEnum("BINARY", "BINARY");

    
    private MessageEncodingTypeEnum(String name, String displayName){
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
    public static MessageEncodingTypeEnum valueOf(String name)
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
    public static MessageEncodingTypeEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<MessageEncodingTypeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
