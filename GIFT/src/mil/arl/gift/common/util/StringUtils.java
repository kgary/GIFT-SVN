/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.util.Iterator;

/**
 * A utility class that contains various useful methods
 * for operating on string objects
 * @author tflowers
 *
 */
public class StringUtils {

    /**
     * The default {@link StringUtils.Stringifier} to use if none is provided
     */
    public final static Stringifier<Object> DEFAULT = new Stringifier<Object>() {

        @Override
        public String stringify(Object obj) {
            return String.valueOf(obj);
        }

    };

    /**
     * Represents an object that is responsible for converting a given Object
     * into a String.
     *
     * @author tflowers
     * @param <T> The type of element should be converted to a string.
     */
    public static interface Stringifier<T> {
        /**
         * Converts an object to a string
         * @param obj the object to convert
         * @return the converted form of the object
         */
        String stringify(T obj);
    }

    /**
     * Private constructor since no instances of this class should ever be
     * created
     */
    private StringUtils() {
    }
    
    /**
     * Return true if the string contains one or more letters.
     * 
     * @param value the string to check for letters.
     * @return whether the string contains letters only.  Examples:<br/>
     * "abc" = true<br/>
     * "a bc" = false<br/>
     * "" = false<br/>
     * null = false<br/>
     * " " = false<br/>
     * "_A" = false<br/>
     * "ABC" = true<br/>
     */
    public static boolean isAlpha(final String value) {
        return StringUtils.isNotBlank(value) && value.matches("[a-zA-Z]+");
    }
    
    /**
     * Return true if the string contains one or more letters/numbers.
     * 
     * @param value the string to check for letters/numbers.
     * @return whether the string contains letters/numbers only.  Examples:<br/>
     * "abc" = true<br/>
     * "abc1" = true<br/>
     * "a bc" = false<br/>
     * "" = false<br/>
     * null = false<br/>
     * " " = false<br/>
     * "_A" = false<br/>
     * "_1" = false<br/>
     * "ABC1" = true<br/>
     */
    public static boolean isAlphaNumeric(final String value){
        return StringUtils.isNotBlank(value) && value.matches("[a-zA-Z0-9]+");
    }

    /**
     * Returns true if the given string is null, empty, or blank (only contains whitespace)
     *
     * @param str the string to test
     * @return true if the string is null, empty, or blank
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Returns true if the given string is not null, not empty, and contains non-whitespace
     * characters
     *
     * @param str the string to test
     * @return true if the string is not null, not empty, and contains non-whitespace characters
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * Returns whether the string provided ends with one of the extensions.
     * 
     * @param str the string to check for the extensions.  If null or empty this method returns false.
     * @param extensions the extensions, or suffix, to check the provided string for.  If empty this method
     * returns false.
     * @return true if the string ends with one of the extensions.
     */
    public static boolean endsWith(String str, String...extensions){
        
        if(extensions.length == 0){
            return false;
        }else if(isBlank(str)){
            return false;
        }
        
        for(String ext : extensions){
            if(str.endsWith(ext)){
                return true;
            }
        }
        
        return false;
    }

    /**
     * Null safe equality check. Calls trim on the strings to ignore whitespace.
     *
     * @param a the first string to test
     * @param b the second string to test
     * @return the result of the instance equals method if the null checks pass
     */
    public static boolean equals(String a, String b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null || b == null) {
            return false;
        }

        return a.trim().equals(b.trim());
    }

    /**
     * Null safe case-insensitive equality check. Calls trim on the strings to ignore whitespace.
     *
     * @param a the first string to test
     * @param b the second string to test
     * @return the result of the instance equalsIgnoreCase method if the null checks pass
     */
    public static boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null || b == null) {
            return false;
        }

        return a.trim().equalsIgnoreCase(b.trim());
    }

    /**
     * Combines a sequence of objects together as a string with each separated
     * with a given delimiter
     *
     * @param delimiter the string that should come between each separate
     *        element in the sequence of objects
     * @param elements the sequence of objects to join together
     * @return The string containing each of the objects separated by the
     *         delimiters
     */
    public static String join(String delimiter, Iterable<? extends Object> elements) {
        StringBuilder sb = new StringBuilder();
        join(delimiter, elements, sb);
        return sb.toString();
    }

    /**
     * Combines a sequence of objects together as a string with each
     * separated with a given delimiter
     * @param delimiter the string that should come between each separate
     * element in the sequence of objects
     * @param elements the sequence of objects to join together
     * @param sb a string builder to append the joined string to
     */
    public static <T> void join(String delimiter, Iterable<? extends T> elements, StringBuilder sb) {
        if (elements == null) {
            return;
        }

        Iterator<? extends T> iter = elements.iterator();
        join(delimiter, iter, DEFAULT, sb);
    }

    /**
     * Combines a sequence of objects together as a string with each separated
     * with a given delimiter
     *
     * @param delimiter the string that should come between each separate
     *        element in the sequence of objects
     * @param elements the sequence of objects to join together
     * @param stringifier a {@link StringUtils.Stringifier} implementation that
     *        will be used to convert each of the Objects in the sequence to
     *        Strings.
     * @param sb a string builder to append the joined string to
     */
    public static <T> void join(String delimiter, Iterable<? extends T> elements, Stringifier<T> stringifier, StringBuilder sb) {
        if (elements == null) {
            return;
        }

        join(delimiter, elements.iterator(), stringifier, sb);
    }

    /**
     * Combines a sequence of objects together as a string with each
     * separated with a given delimiter
     * @param delimiter the string that should come between each separate
     * element in the sequence of objects
     * @param iter the iterator for a sequence the sequence of Objects to join
     * @param stringifier a {@link StringUtils.Stringifier} implementation that will be used to convert
     * each of the Objects in the sequence to Strings.
     * @param sb a string builder to append the joined string to
     */
    public static <T> void join(String delimiter, Iterator<? extends T> iter, Stringifier<T> stringifier, StringBuilder sb) {
        if (iter == null || sb == null) {
            return;
        }

        //Handle the case of no elements
        if(!iter.hasNext()) {
            return;
        }

        // Grab first element
        T element = iter.next();

        // if stringifier is null, use default; otherwise use the one given.
        if (stringifier == null) {
            sb.append(DEFAULT.stringify(element));

            // Handle all remaining elements
            while (iter.hasNext()) {
                element = iter.next();
                sb.append(delimiter).append(DEFAULT.stringify(element));
            }
        } else {
            sb.append(stringifier.stringify(element));

            // Handle all remaining elements
            while (iter.hasNext()) {
                element = iter.next();
                sb.append(delimiter).append(stringifier.stringify(element));
            }
        }
    }

    /**
     * Returns a string whose value is the {@code target} string with any
     * leading or trailing character sequence matching {@code toTrim} removed.
     * <br/>
     * <br/>
     * This method functions similarly to {@link String#trim()} but allows the
     * caller to trim any characters, not just whitespace characters.
     *
     * @param target the string that should be trimmed
     * @param toTrim the leading or trailing character sequence that should be
     *        removed by the trim
     * @return the target string with leading or trailing character sequences
     *         matching toTrim removed
     */
    public static String trim(String target, String toTrim) {
        return trim(target, toTrim);
    }

    /**
     * Returns a string whose value is the {@code target} string with any
     * leading or trailing character sequence matching {@code toTrim} removed.
     * This version of the method also allows the caller to optionally trim
     * white space characters as well, just like {@link String#trim()}. <br/>
     * <br/>
     * This method functions similarly to {@link String#trim()} but allows the
     * caller to trim any characters, not just whitespace characters.
     *
     * @param target the string that should be trimmed
     * @param toTrim the leading or trailing character sequence that should be
     *        removed by the trim
     * @return the target string with leading or trailing character sequences
     *         matching toTrim removed
     */
    public static String trim(String target, boolean trimWhiteSpace, String toTrim) {

        if(target == null) {
            return null;
        }

        String toReturn;

        if(toTrim == null || toTrim.length() == 0) {
            toReturn = target;

        } else {

            int startIndex = target.indexOf(toTrim) == 0 ? toTrim.length() : 0;
            int lastIndex = target.lastIndexOf(toTrim);

            toReturn = target.substring(startIndex, lastIndex != -1 ? lastIndex : target.length());

        }

        if(trimWhiteSpace) {
            toReturn.trim();
        }

        return toReturn;

    }
}
