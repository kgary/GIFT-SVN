/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mil.arl.gift.common.EnumerationNotFoundException;

/**
 * This class contains the various enumerated relational operators
 * 
 * @author mhoffman
 */
public class OperatorEnum extends AbstractEnum implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private static List<OperatorEnum> enumList = new ArrayList<OperatorEnum>(2);
    private static int index = 0;

    public static final OperatorEnum LT = new OperatorEnum("LessThan", "Less Than", "<");
    public static final OperatorEnum GT = new OperatorEnum("GreaterThan", "Greater Than", ">");
    public static final OperatorEnum LTE = new OperatorEnum("LessThanEquals", "Less Than Equals", "<=");
    public static final OperatorEnum GTE = new OperatorEnum("GreaterThanEquals", "Greater Than Equals", ">=");
    public static final OperatorEnum EQUALS = new OperatorEnum("Equals", "Equals", "=");
    
    private String sign;

    /**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatability
     */
    public OperatorEnum() {
        super();
    }
    
    private OperatorEnum(String name, String displayName, String sign){
        super(index++, name, displayName);
        enumList.add(this);
        
        this.sign = sign;
    }
    
    
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;

        } else if (obj instanceof OperatorEnum) {

            OperatorEnum enumObj = (OperatorEnum) obj;

            return enumObj.getValue() == getValue();
        }

        return false;
    }

    @Override
    public int hashCode() {

        int hash = 5;
        hash = hash * 31 + getValue();

        return hash;
    }
    
    /**
     * Gets the sign for the operator
     * 
     * @return String The sign of the operator 
     */
    public String getSign() {
        
        return sign;
    }
    
    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static OperatorEnum valueOf(String name)
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
    public static OperatorEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<OperatorEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
