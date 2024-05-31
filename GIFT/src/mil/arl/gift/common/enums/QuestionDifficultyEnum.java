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

import mil.arl.gift.common.AttributeValueEnumAccessor;
import mil.arl.gift.common.EnumerationNotFoundException;


/**
 * Enumeration of the various difficulty levels of a question
 * 
 * @author nroberts
 *
 */
public class QuestionDifficultyEnum extends AbstractEnum implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static List<QuestionDifficultyEnum> enumList = new ArrayList<QuestionDifficultyEnum>(3);
    
    private static int index = 0;

    public static final QuestionDifficultyEnum EASY = 	new QuestionDifficultyEnum("Easy", "Easy");  
    public static final QuestionDifficultyEnum MEDIUM = new QuestionDifficultyEnum("Medium", "Medium");   
    public static final QuestionDifficultyEnum HARD = 	new QuestionDifficultyEnum("Hard", "Hard");
    
    private static final QuestionDifficultyEnum DEFAULT_VALUE = MEDIUM;

    public static final AttributeValueEnumAccessor ACCESSOR = new AttributeValueEnumAccessor(enumList, enumList, DEFAULT_VALUE, null);
    
    /**
     * Default Constructor
     *
     * Required by GWT to exist and be public because it is Serializable
     */
    public QuestionDifficultyEnum() {
        super();
    }
    
    private QuestionDifficultyEnum(String name, String displayName){
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
    public static QuestionDifficultyEnum valueOf(String name)
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
    public static QuestionDifficultyEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<QuestionDifficultyEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
