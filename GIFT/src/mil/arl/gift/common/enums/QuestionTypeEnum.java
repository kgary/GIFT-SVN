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
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.FillInTheBlankQuestion;
import mil.arl.gift.common.survey.MatrixOfChoicesQuestion;
import mil.arl.gift.common.survey.MultipleChoiceQuestion;
import mil.arl.gift.common.survey.RatingScaleQuestion;
import mil.arl.gift.common.survey.SliderQuestion;

/**
 * Different types of survey questions GIFT supports
 *
 * @author jleonard
 */
public class QuestionTypeEnum extends AbstractEnum implements Serializable {

    private static List<QuestionTypeEnum> enumList = new ArrayList<QuestionTypeEnum>(5);

    private static int index = 0;

    public static final QuestionTypeEnum FREE_RESPONSE = new QuestionTypeEnum("FillInTheBlank", "Free Response");

    public static final QuestionTypeEnum MULTIPLE_CHOICE = new QuestionTypeEnum("MultipleChoice", "Multiple Choice");

    public static final QuestionTypeEnum RATING_SCALE = new QuestionTypeEnum("RatingScale", "Rating Scale");

    public static final QuestionTypeEnum MATRIX_OF_CHOICES = new QuestionTypeEnum("MatrixOfChoices", "Matrix Of Choices / Matching");

    public static final QuestionTypeEnum SLIDER_BAR = new QuestionTypeEnum("SliderBar", "Slider Bar");

    public static final QuestionTypeEnum ESSAY = new QuestionTypeEnum("Essay", "Essay");

    public static final QuestionTypeEnum TRUE_FALSE = new QuestionTypeEnum("True/False", "True/False");
    
    private static final long serialVersionUID = 1L;
    
    /** A boolean value specifying whether or not questions corresponding to this question type can be authored.
     *  If false, this question type choice will not be available in the question composer.*/
    private boolean canAuthor = true;

    /**
     * Default Constructor
     *
     * Required by GWT to exist and be public because it is Serializable
     */
    public QuestionTypeEnum() {
        super();
    }

    private QuestionTypeEnum(String name, String displayName) {

        super(index++, name, displayName);
        enumList.add(this);
    }
    
    /**
     * Creates a new question type enum with the specified name, display name, and authoring eligibility status.
     * 
     * @param name The name of the new question type enum
     * @param displayName The display name of the new question type enum
     * @param canAuthor Whether or not the new question type enum should be capable of being authored.
     */
    @SuppressWarnings("unused")
    private QuestionTypeEnum(String name, String displayName, boolean canAuthor) {
    	this(name, displayName);
    	this.canAuthor = canAuthor;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;

        } else if (obj instanceof QuestionTypeEnum) {

            QuestionTypeEnum enumObj = (QuestionTypeEnum) obj;

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
     * Returns whether or not this question type is capable of being authored
     * 
     * @return True, if the question type is capable of being authored. False, otherwise.
     */
    public boolean isAuthorable(){
    	return canAuthor;
    }

    /**
     * Return the enumeration object that has the matching name.
     *
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     * name is not found.
     */
    public static QuestionTypeEnum valueOf(String name)
            throws EnumerationNotFoundException {

        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     *
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     * value is not found.
     */
    public static QuestionTypeEnum valueOf(int value)
            throws EnumerationNotFoundException {

        return AbstractEnum.valueOf(value, VALUES());
    }
    
        /**
     * Return the enumeration object that matches the class implementation
     *
     * @param question The question of the type the enumeration is needed for
     * @return GwtQuestionTypeEnum The matching enumeration
     */
    public static QuestionTypeEnum valueOf(AbstractQuestion question) {

        if (question instanceof FillInTheBlankQuestion) {

            return FREE_RESPONSE;

        } else if (question instanceof MultipleChoiceQuestion) {

            return MULTIPLE_CHOICE;

        } else if (question instanceof RatingScaleQuestion) {

            return RATING_SCALE;

        } else if (question instanceof MatrixOfChoicesQuestion) {

            return MATRIX_OF_CHOICES;

        } else if (question instanceof SliderQuestion) {

            return SLIDER_BAR;

        } else {

            return null;
        }
    }

    /**
     * Returns a List of the currently defined enumerations.
     *
     * @return a List of the currently defined enumerations.
     */
    public static List<QuestionTypeEnum> VALUES() {

        return Collections.unmodifiableList(enumList);
    }
}
