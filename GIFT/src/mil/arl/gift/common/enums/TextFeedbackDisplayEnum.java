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
 * This class contains the various enumerated ways of displaying text feedback
 * 
 * @author mhoffman
 */
public class TextFeedbackDisplayEnum extends AbstractEnum {

	private static final long serialVersionUID = 1L;

    private static List<TextFeedbackDisplayEnum> enumList = new ArrayList<TextFeedbackDisplayEnum>(2);
    private static int index = 0;

    public static final TextFeedbackDisplayEnum NO_EFFECT = new TextFeedbackDisplayEnum("NoEffect", "No Effect");
    public static final TextFeedbackDisplayEnum BEEP_ONLY = new TextFeedbackDisplayEnum("BeepOnly", "Beep Only");
    public static final TextFeedbackDisplayEnum FLASH_ONLY = new TextFeedbackDisplayEnum("FlashOnly", "Flash Only");
    public static final TextFeedbackDisplayEnum BEEP_AND_FLASH = new TextFeedbackDisplayEnum("BeepAndFlash", "Beep and Flash");
    
    private TextFeedbackDisplayEnum(String name, String displayName){
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
    public static TextFeedbackDisplayEnum valueOf(String name)
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
    public static TextFeedbackDisplayEnum valueOf(int value)
        throws EnumerationNotFoundException {
        
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<TextFeedbackDisplayEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
