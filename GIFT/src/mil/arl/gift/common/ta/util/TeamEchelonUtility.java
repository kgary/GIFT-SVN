/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.util;

import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * 
 * Utility class for setting echelon values for various training application objects
 * 
 * @author cpadilla
 *
 */
public class TeamEchelonUtility {
    
    /** SIDC modifier for DIVISION */
    private static final char DIVISION_SIDC_MODIFIER = 'I';
    
    /** SIDC modifier for BRIGADE */
    private static final char BRIGADE_SIDC_MODIFIER = 'H';
    
    /** SIDC modifier for BATTALION */
    private static final char BATTALION_SIDC_MODIFIER = 'F';
    
    /** SIDC modifier for COMPANY */
    private static final char COMPANY_SIDC_MODIFIER = 'E';
    
    /** SIDC modifier for PLATOON */
    private static final char PLATOON_SIDC_MODIFIER = 'D';
    
    /** SIDC modifier for SQUAD */
    private static final char SQUAD_SIDC_MODIFIER = 'B';
    
    /** SIDC modifier for FIRETEAM */
    private static final char FIRETEAM_SIDC_MODIFIER = 'A';
    
    /** SIDC modifier for WILDCARD */
    private static final char WILDCARD_SIDC_MODIFIER = '*';

    /**
     * Sets the modifier value for a {@link SIDC} with the given echelon value.
     * 
     * @param sidc the {@link SIDC} value to modify. Can't be null.
     * @param echelon the echelon value to set. Can't be null.
     */
    public static String setSIDCModifierForEchelon(String modifier, EchelonEnum echelon) {
        
        if (StringUtils.isBlank(modifier) || modifier.length() != 2) {
            throw new IllegalArgumentException("The SIDC modifier must be a string with 2 charactes.");
        }
        
        if (echelon == null) {
            throw new IllegalArgumentException("The echelon value can't be null.");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(modifier.charAt(0));
        if (echelon.equals(EchelonEnum.DIVISION)) {
            sb.append(DIVISION_SIDC_MODIFIER);
        } else if (echelon.equals(EchelonEnum.BRIGADE)) {
            sb.append(BRIGADE_SIDC_MODIFIER);
        } else if (echelon.equals(EchelonEnum.BATTALION)) {
            sb.append(BATTALION_SIDC_MODIFIER);
        } else if (echelon.equals(EchelonEnum.COMPANY)) {
            sb.append(COMPANY_SIDC_MODIFIER);
        } else if (echelon.equals(EchelonEnum.PLATOON)) {
            sb.append(PLATOON_SIDC_MODIFIER);
        } else if (echelon.equals(EchelonEnum.SQUAD)) {
            sb.append(SQUAD_SIDC_MODIFIER);
        } else if (echelon.equals(EchelonEnum.FIRETEAM)) {
            sb.append(FIRETEAM_SIDC_MODIFIER);
        } else {
            sb.append(WILDCARD_SIDC_MODIFIER);
        }

        return sb.toString();
    }

}
