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
 * Enumeration of the various Merrill Quadrants (used in Pedagogy)
 * 
 * @author mhoffman
 *
 */
public class MerrillQuadrantEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<MerrillQuadrantEnum> enumList = new ArrayList<MerrillQuadrantEnum>(2);
    private static int index = 0;

    public static final MerrillQuadrantEnum RULE = new MerrillQuadrantEnum("Rule", "Rule");
    public static final MerrillQuadrantEnum EXAMPLE = new MerrillQuadrantEnum("Example", "Example");
    public static final MerrillQuadrantEnum RECALL = new MerrillQuadrantEnum("Recall", "Recall");
    public static final MerrillQuadrantEnum PRACTICE = new MerrillQuadrantEnum("Practice", "Practice");
    public static final MerrillQuadrantEnum REMEDIATION_AFTER_RECALL = new MerrillQuadrantEnum("RemediationAfterRecall", "Remediation After Recall");
    public static final MerrillQuadrantEnum REMEDIATION_AFTER_PRACTICE = new MerrillQuadrantEnum("RemediationAfterPractice", "Remediation After Practice");

    /**
     * Default Constructor
     *
     * Required by GWT to exist and be public because it is Serializable
     */
    public MerrillQuadrantEnum() {
        super();
    }
    
    private MerrillQuadrantEnum(String name, String displayName){
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
    public static MerrillQuadrantEnum valueOf(String name)
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
    public static MerrillQuadrantEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<MerrillQuadrantEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
