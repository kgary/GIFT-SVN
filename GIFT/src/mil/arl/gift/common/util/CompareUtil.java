/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.util.Objects;

/**
 * Comparison utility class that is intended to hold common comparison functions that
 * are used.  The class is meant to be statically accessed only.
 * 
 * @author nblomberg
 *
 */
public class CompareUtil {

    /**
     * Constructor (private)
     */
    private CompareUtil() {
    }
    
    /**
     * Wrapper for a null safe comparison between two generic object types (Strings, Integers, etc). 
     * Checks null on the objects without throwing an exception.  This can be used
     * in places where null can be an acceptable value for one or both of the objects
     * being compared.
     * 
     * @param lVal Left side value to be compared.
     * @param rVal Right side value to be compared.
     * @return True if the objects are equal (including if both are null).  False if the objects are not equal.
     */
    public static boolean equalsNullSafe(Object lVal, Object rVal) {
        return Objects.equals(lVal, rVal);
    }
    
    
}