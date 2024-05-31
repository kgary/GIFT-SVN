/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import com.google.gwt.i18n.client.DefaultCurrencyData;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * Client side decimal format class that uses the GWT compliant com.google.gwt.i18n.client.NumberFormat.
 * 
 * @author mhoffman
 *
 */
public class DecimalFormatExt extends NumberFormat {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /** satisfying the constructor, shouldn't be using this formatter for currency formatting */
    private static final DefaultCurrencyData cData = new DefaultCurrencyData("USD", "$");
    
    /**
     * Creates a DecimalFormat using the given pattern and the symbols for the default locale.
     * @param pattern A non-localized pattern string.
     * @throws NullPointerException - if pattern is null
     * @throws IllegalArgumentException - if the given pattern is invalid.
     */
    public DecimalFormatExt(String pattern){
        super(pattern, cData, false);
    }

}
