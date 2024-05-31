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
 * Enumeration for the various Score Node types using in scoring a learner's lesson.
 * 
 * @author mhoffman
 *
 */
public class ScoreNodeTypeEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<ScoreNodeTypeEnum> enumList = new ArrayList<ScoreNodeTypeEnum>(2);
    private static int index = 0;
    
    public static final ScoreNodeTypeEnum GRADED_SCORE_NODE = new ScoreNodeTypeEnum("GradedScoreNode", "Graded Score Node");
    public static final ScoreNodeTypeEnum RAW_SCORE_NODE    = new ScoreNodeTypeEnum("RawScoreNode",    "Raw Score Node");
    
    /**
     * Default constructor - needed for gwt serialization.
     */
    public ScoreNodeTypeEnum() {
        
    }
    
    private ScoreNodeTypeEnum(String name, String displayName){
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
    public static ScoreNodeTypeEnum valueOf(String name)
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
    public static ScoreNodeTypeEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<ScoreNodeTypeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }    
    
}


