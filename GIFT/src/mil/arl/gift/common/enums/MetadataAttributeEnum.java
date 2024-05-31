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
 * Enumeration of the various metadata attributes
 * 
 * @author mhoffman
 *
 */
public class MetadataAttributeEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<MetadataAttributeEnum> enumList = new ArrayList<MetadataAttributeEnum>(2);
    private static int index = 0;

    public static final MetadataAttributeEnum IMI_1               = new MetadataAttributeEnum("IMI1", "IMI 1", 1, Category.ALL);
    public static final MetadataAttributeEnum IMI_2               = new MetadataAttributeEnum("IMI2", "IMI 2", 1, Category.ALL);
    public static final MetadataAttributeEnum IMI_3               = new MetadataAttributeEnum("IMI3", "IMI 3", 1, Category.ALL);
    public static final MetadataAttributeEnum IMI_4               = new MetadataAttributeEnum("IMI4", "IMI 4", 1, Category.ALL);
    
    public static final MetadataAttributeEnum VISUAL            = new MetadataAttributeEnum("Visual", "Visual", 2, Category.NOT_PRACTICE);
    public static final MetadataAttributeEnum TEXTUAL           = new MetadataAttributeEnum("Textual", "Textual", 2, Category.NOT_PRACTICE);
    public static final MetadataAttributeEnum VISUAL_WITH_TEXT           = new MetadataAttributeEnum("Visual with Text", "Visual with Text", 2, Category.NOT_PRACTICE);
    public static final MetadataAttributeEnum VIDEO            = new MetadataAttributeEnum("Video", "Video", 2, Category.NOT_PRACTICE);
//    public static final MetadataAttributeEnum AUDITORY          = new MetadataAttributeEnum("Auditory", "Auditory");
    public static final MetadataAttributeEnum ANIMATION         = new MetadataAttributeEnum("Animation", "Animation", 2, Category.NOT_PRACTICE);
    public static final MetadataAttributeEnum CASE_STUDY        = new MetadataAttributeEnum("Case Study", "Case Study", 2, Category.NOT_PRACTICE);
//  public static final MetadataAttributeEnum CONCEPTUAL_MAP    = new MetadataAttributeEnum("Conceptual Map", "Conceptual Map");
//  public static final MetadataAttributeEnum DIAGRAM           = new MetadataAttributeEnum("Diagram", "Diagram");
  public static final MetadataAttributeEnum GRAPHIC           = new MetadataAttributeEnum("Graphic", "Graphic", 2, Category.NOT_PRACTICE);
  public static final MetadataAttributeEnum WORKED_EXAMPLE            = new MetadataAttributeEnum("Worked Example", "Worked Example", 2, Category.NOT_PRACTICE);
    
    public static final MetadataAttributeEnum EASY_DIFFICULTY   = new MetadataAttributeEnum("Easy Difficulty", "Easy Difficulty", 3, Category.ALL);
    public static final MetadataAttributeEnum MEDIUM_DIFFICULTY = new MetadataAttributeEnum("Medium Difficulty", "Medium Difficulty", 3, Category.ALL);
    public static final MetadataAttributeEnum HARD_DIFFICULTY   = new MetadataAttributeEnum("Hard Difficulty", "Hard Difficulty", 3, Category.ALL);
    
    public static final MetadataAttributeEnum LOW_CONTROL       = new MetadataAttributeEnum("Low User Control", "Low User Control", 4, Category.NOT_PRACTICE);
    public static final MetadataAttributeEnum MEDIUM_CONTROL    = new MetadataAttributeEnum("Medium User Control", "Medium User Control", 4, Category.NOT_PRACTICE);
    public static final MetadataAttributeEnum HIGH_CONTROL      = new MetadataAttributeEnum("High User Control", "High User Control", 4, Category.NOT_PRACTICE);    

    
    public static final MetadataAttributeEnum MULTIPLE_CHOICE   = new MetadataAttributeEnum("Multiple Choice", "Multiple Choice", 5);
//    public static final MetadataAttributeEnum FILL_IN_THE_BLANK = new MetadataAttributeEnum("Fill-in-the-Blank", "Fill-in-the-Blank");
    public static final MetadataAttributeEnum SHORT_RESPONSE    = new MetadataAttributeEnum("Short Response", "Short Response", 5);
    public static final MetadataAttributeEnum ESSAY_RESPONSE    = new MetadataAttributeEnum("Essay Response", "Essay Response", 5);
//    public static final MetadataAttributeEnum SIMULATION        = new MetadataAttributeEnum("Simulation", "Simulation");
    public static final MetadataAttributeEnum LSA               = new MetadataAttributeEnum("LSA", "LSA", 5);
    public static final MetadataAttributeEnum ITEM_RESPONSE     = new MetadataAttributeEnum("Item Response", "Item Response", 5);
//    public static final MetadataAttributeEnum PROBLEM_SOLVING   = new MetadataAttributeEnum("Problem-Solving", "Problem-Solving");
//    public static final MetadataAttributeEnum ENVIRONMENT_3D    = new MetadataAttributeEnum("3D Environment", "3D Environment");
//    public static final MetadataAttributeEnum ENVIRONMENT_2D    = new MetadataAttributeEnum("2D Environment", "2D Environment");
//    public static final MetadataAttributeEnum CONCEPT_PROGRESSION_AS_NEEDED    = new MetadataAttributeEnum("Concept Progression As Needed", "Concept Progression As Needed");
//    public static final MetadataAttributeEnum CONCEPT_PROGRESSION_MASTERY    = new MetadataAttributeEnum("Concept Progression Mastery", "Concept Progression Mastery");
    public static final MetadataAttributeEnum FEEDBACK_FREQ_QUESTION_QUESTION    = new MetadataAttributeEnum("Feedback Frequency Question by Question", "Feedback Frequency Question by Question");
    public static final MetadataAttributeEnum FEEDBACK_FREQ_FOLLOW_ITEMS    = new MetadataAttributeEnum("Feedback Frequency Following All Items", "Feedback Frequency Following All Items");
    public static final MetadataAttributeEnum TRAINING_TYPE_PROCEDURE_EXE    = new MetadataAttributeEnum("Training Type Procedure Execution", "Training Type Procedure Execution");
    public static final MetadataAttributeEnum TRAINING_TYPE_PROBLEM_SOLVE    = new MetadataAttributeEnum("Training Type Problem Solving", "Training Type Problem Solving");
    public static final MetadataAttributeEnum GUIDANCE_HINTS    = new MetadataAttributeEnum("Guidance Hints", "Guidance Hints");
    public static final MetadataAttributeEnum GUIDANCE_PROMPTS    = new MetadataAttributeEnum("Guidance Prompts", "Guidance Prompts");
    public static final MetadataAttributeEnum GUIDANCE_REFLECTION    = new MetadataAttributeEnum("Guidance Reflection", "Guidance Reflection");
    public static final MetadataAttributeEnum GUIDANCE_POSITIVE_AFFECT    = new MetadataAttributeEnum("Guidance Positive Affect", "Guidance Positive Affect");
    public static final MetadataAttributeEnum GUIDANCE_NEGATIVE_AFFECT    = new MetadataAttributeEnum("Guidance Negative Affect", "Guidance Negative Affect");
    public static final MetadataAttributeEnum GUIDANCE_PRAISE    = new MetadataAttributeEnum("Guidance Praise", "Guidance Praise");
    public static final MetadataAttributeEnum GUIDANCE_ASSERTIONS    = new MetadataAttributeEnum("Guidance Assertions", "Guidance Assertions");
    public static final MetadataAttributeEnum GUIDANCE_PUMPS    = new MetadataAttributeEnum("Guidance Pumps", "Guidance Pumps");
    public static final MetadataAttributeEnum TRAINING_FEEDBACK_REALTIME    = new MetadataAttributeEnum("Training Feedback Realtime", "Training Feedback Realtime");
    public static final MetadataAttributeEnum TRAINING_FEEDBACK_AAR    = new MetadataAttributeEnum("Training Feedback AAR", "Training Feedback AAR");
    public static final MetadataAttributeEnum DURATION_SHORT    = new MetadataAttributeEnum("Duration: Short", "Duration: Short");
    public static final MetadataAttributeEnum DURATION_MEDIUM    = new MetadataAttributeEnum("Duration: Medium", "Duration: Medium");
    public static final MetadataAttributeEnum DURATION_LONG    = new MetadataAttributeEnum("Duration: Long", "Duration: Long");
    public static final MetadataAttributeEnum INTENDED_USER_NOVICE    = new MetadataAttributeEnum("Intended User: Novice", "Intended User: Novice");
    public static final MetadataAttributeEnum INTENDED_USER_JOURNEYMAN    = new MetadataAttributeEnum("Intended User: Journeyman", "Intended User: Journeyman");
    public static final MetadataAttributeEnum INTENDED_USER_EXPERT    = new MetadataAttributeEnum("Intended User: Expert", "Intended User: Expert");

    /** used to group/sort enumerations together by the same value */
    private int groupBy;
    
    private Category category;
    
    enum Category{
        NOT_PRACTICE,
        PRACTICE_ONLY,
        ALL
    }
    
    private MetadataAttributeEnum(String name, String displayName, int groupBy, Category category){
    	super(index++, name, displayName);
    	enumList.add(this);
    	
    	this.groupBy = groupBy;
    	this.category = category;
    }
    
    private MetadataAttributeEnum(String name, String displayName, int groupBy){
        this(name, displayName, groupBy, Category.PRACTICE_ONLY);
    }
    
    private MetadataAttributeEnum(String name, String displayName){
        this(name, displayName, Integer.MAX_VALUE);

    }
    
    /**
     * Return the number used to group/sort enumerations of this class.
     * 
     * @return integer
     */
    public int getGroupBy(){
        return groupBy;
    }
    
    public boolean isPracticeAttribute(){
        return category == Category.ALL || category == Category.PRACTICE_ONLY;
    }
    
    public boolean isContentAttribute(){
        return category == Category.ALL || category == Category.NOT_PRACTICE;
    }
    
    @Override
    public int compareTo(AbstractEnum otherEnum){
        
        if(otherEnum instanceof MetadataAttributeEnum){
            
            int otherGroupBy = ((MetadataAttributeEnum)otherEnum).getGroupBy();
            if(this.groupBy < otherGroupBy){
                return -1;
            }else if(this.groupBy > otherGroupBy){
                return 1;
            }else{
                //they are the same group, sort alphabetically (default)
                return super.compareTo(otherEnum);
            }
        }else{
            return super.compareTo(otherEnum);
        }
    }
    
    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static MetadataAttributeEnum valueOf(String name)
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
    public static MetadataAttributeEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<MetadataAttributeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
