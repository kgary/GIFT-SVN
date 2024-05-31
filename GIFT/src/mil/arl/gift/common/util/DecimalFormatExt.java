/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.text.DecimalFormat;

/**
 * Server side decimal format class that uses the non-GWT compliant java.text.DecimalFormat.
 * 
 * @author mhoffman
 *
 */
public class DecimalFormatExt extends DecimalFormat {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a DecimalFormat using the given pattern and the symbols for the default locale.
     * @param pattern A non-localized pattern string.
     * @throws NullPointerException - if pattern is null
     * @throws IllegalArgumentException - if the given pattern is invalid.
     */
    public DecimalFormatExt(String pattern){
        super(pattern);
    }

}
