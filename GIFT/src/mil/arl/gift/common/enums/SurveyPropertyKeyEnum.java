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
import mil.arl.gift.common.io.Constants;

/**
 * Enumeration of the property keys for the survey system properties
 *
 * @author jleonard
 */
public class SurveyPropertyKeyEnum extends AbstractEnum implements Serializable {

    private static List<SurveyPropertyKeyEnum> enumList = new ArrayList<SurveyPropertyKeyEnum>(19);

    private static int index = 0; 
    
    public static final SurveyPropertyKeyEnum ANSWER_WEIGHTS            = new SurveyPropertyKeyEnum("Answer_Weights", "Answer Weights", Constants.COMMA);
    /** one or more course concepts associated with a survey question, usually in the survey composer (e.g. question bank) */
    public static final SurveyPropertyKeyEnum ASSOCIATED_CONCEPTS       = new SurveyPropertyKeyEnum("Associated_Concepts", "Associated Concepts", Constants.COMMA);
    /** a single course concept, that is also contained in the ASSOCIATED_CONCEPTS property, which indicates that this survey question was selected 
     * from the question bank to satisfy the request for this concept and not any other concept.  */
    public static final SurveyPropertyKeyEnum SELECTED_CONCEPT         = new SurveyPropertyKeyEnum("Selected_Concept", "Selected Concept");
    public static final SurveyPropertyKeyEnum CLS_QUESTION_PRIORITY     = new SurveyPropertyKeyEnum("CLS_Question_Priority", "CLS Question Priority");
    public static final SurveyPropertyKeyEnum COLUMN_OPTIONS_KEY        = new SurveyPropertyKeyEnum("Column_Options", "Column Options");
    public static final SurveyPropertyKeyEnum COLUMN_WIDTH_KEY          = new SurveyPropertyKeyEnum("Column_Width", "Column Width");
    public static final SurveyPropertyKeyEnum CORRECT_ANSWER            = new SurveyPropertyKeyEnum("Correct_Answer", "Correct Answer");
    public static final SurveyPropertyKeyEnum DISPLAY_SCALE_LABELS_KEY  = new SurveyPropertyKeyEnum("Display_Scale_Labels", "Display Scale Labels");
    public static final SurveyPropertyKeyEnum HELP_STRING               = new SurveyPropertyKeyEnum("Help_String", "Help String");
    public static final SurveyPropertyKeyEnum HIDE_REPLY_OPTION_LABELS_KEY  = new SurveyPropertyKeyEnum("Hide_Reply_Option_Labels", "Hide Reply Option Labels");
    public static final SurveyPropertyKeyEnum HIDE_SURVEY_NAME              = new SurveyPropertyKeyEnum("Hide_Survey_Name", "Hide Survey Name");
    public static final SurveyPropertyKeyEnum HIDE_SURVEY_PAGE_NUMBERS      = new SurveyPropertyKeyEnum("Hide_Survey_Page_Numbers", "Hide Survey Page Numbers");
    public static final SurveyPropertyKeyEnum HIDE_SURVEY_QUESTION_NUMBERS  = new SurveyPropertyKeyEnum("Hide_Survey_Question_Numbers", "Hide Survey Question Numbers");
    public static final SurveyPropertyKeyEnum IS_ANSWER_FIELD_TEXT_BOX_KEY  = new SurveyPropertyKeyEnum("Is_Answer_Field_Text_Box", "Is Answer Field Text Box");
    public static final SurveyPropertyKeyEnum LEFT_EXTREME_LABEL_KEY        = new SurveyPropertyKeyEnum("Left_Extreme_Label", "Left Label");
    public static final SurveyPropertyKeyEnum LEFT_MARGIN_KEY     		    = new SurveyPropertyKeyEnum("Left_Margin", "Left Margin");
    public static final SurveyPropertyKeyEnum MAXIMUM_SELECTIONS_ALLOWED_KEY    = new SurveyPropertyKeyEnum("Maximum_Selections_Allowed", "Maximum Selections Allowed");
    public static final SurveyPropertyKeyEnum MID_POINT_LABEL_KEY               = new SurveyPropertyKeyEnum("Mid_Point_Label", "Mid Point Label");
    public static final SurveyPropertyKeyEnum MINIMUM_SELECTIONS_REQUIRED_KEY   = new SurveyPropertyKeyEnum("Minimum_Selections_Required", "Minimum Selections Required");
    public static final SurveyPropertyKeyEnum MULTI_SELECT_ENABLED              = new SurveyPropertyKeyEnum("Multi_Select_Enabled", "Multi Select Enabled");
    public static final SurveyPropertyKeyEnum QUESTION_DIFFICULTY               = new SurveyPropertyKeyEnum("Question_Difficulty", "Question Difficulty", QuestionDifficultyEnum.ACCESSOR);
    public static final SurveyPropertyKeyEnum QUESTION_IMAGE_KEY                = new SurveyPropertyKeyEnum("Question_Image", "Question Image");
    public static final SurveyPropertyKeyEnum MEDIA_FILE_SOURCE                 = new SurveyPropertyKeyEnum("Media_File_Source", "Media File Source");
    public static final SurveyPropertyKeyEnum QUESTION_IMAGE_POSITION_KEY       = new SurveyPropertyKeyEnum("Question_Image_Position", "Question Image Position");
    public static final SurveyPropertyKeyEnum QUESTION_IMAGE_WIDTH_KEY          = new SurveyPropertyKeyEnum("Question_Image_Width", "Question Image Width");
    public static final SurveyPropertyKeyEnum DISPLAY_QUESTION_IMAGE            = new SurveyPropertyKeyEnum("Display_Question_Image", "Display Question Image");
    public static final SurveyPropertyKeyEnum RANDOMIZE                         = new SurveyPropertyKeyEnum("Randomize", "Randomize");
    public static final SurveyPropertyKeyEnum RANGE                             = new SurveyPropertyKeyEnum("ValueBounds", "Value Bounds");
    public static final SurveyPropertyKeyEnum REPLY_OPTION_SET_KEY              = new SurveyPropertyKeyEnum("Reply_Option_Set", "Reply Option Set");
    public static final SurveyPropertyKeyEnum REPLY_FEEDBACK                    = new SurveyPropertyKeyEnum("Reply_Feedback", "Reply Feedback", Constants.PIPE);
    public static final SurveyPropertyKeyEnum REPLY_EXTERNAL_TA_OBJ_ID          = new SurveyPropertyKeyEnum("Reply_External_Training_App_Object_Id", "Reply External Training Application Object Id", Constants.COMMA);
    public static final SurveyPropertyKeyEnum REQUIRED                          = new SurveyPropertyKeyEnum("Required", "Required");
    public static final SurveyPropertyKeyEnum RIGHT_EXTREME_LABEL_KEY           = new SurveyPropertyKeyEnum("Right_Extreme_Label", "Right Label");
    public static final SurveyPropertyKeyEnum ROW_OPTIONS_KEY                   = new SurveyPropertyKeyEnum("Row_Options", "Row Options");
    public static final SurveyPropertyKeyEnum SCALE_IMAGE_URI_KEY               = new SurveyPropertyKeyEnum("Scale_Image_Uri", "Scale Image URI");
    public static final SurveyPropertyKeyEnum SCALE_IMAGE_WIDTH_KEY             = new SurveyPropertyKeyEnum("Scale_Image_Width", "Scale Image Width");
    public static final SurveyPropertyKeyEnum DISPLAY_SCALE_IMAGE               = new SurveyPropertyKeyEnum("Display_Scale_Image", "Display Scale Image");
    public static final SurveyPropertyKeyEnum SCORERS                           = new SurveyPropertyKeyEnum("Scorers", "Scorers");
    /**
     * StepSize is used to see by how many the slider should go up and down by.
     */
    public static final SurveyPropertyKeyEnum STEP_SIZE                            = new SurveyPropertyKeyEnum("StepSize", "Step Size");
    public static final SurveyPropertyKeyEnum SURVEY_COMPLETE_SURVEY_BUTTON_LABEL   = new SurveyPropertyKeyEnum("Survey_Complete_Survey_Button_Label", "Survey Next Page Button Label");
    public static final SurveyPropertyKeyEnum SURVEY_GO_BACK_ENABLED                = new SurveyPropertyKeyEnum("Survey_Go_Back_Enabled", "Survey Go Back Enabled");
    public static final SurveyPropertyKeyEnum SURVEY_ITEM_SCALES                    = new SurveyPropertyKeyEnum("Survey_Item_Scales", "Survey Item Scales");
    public static final SurveyPropertyKeyEnum SURVEY_NEXT_PAGE_BUTTON_LABEL         = new SurveyPropertyKeyEnum("Survey_Next_Page_Button_Label", "Survey Next Page Button Label");
    public static final SurveyPropertyKeyEnum TAG                                   = new SurveyPropertyKeyEnum("Tag", "Tag");
    public static final SurveyPropertyKeyEnum TEXT                                  = new SurveyPropertyKeyEnum("Text", "Text");
    public static final SurveyPropertyKeyEnum UNPRESENTABLE                         = new SurveyPropertyKeyEnum("Unpresentable", "Unpresentable");
    public static final SurveyPropertyKeyEnum USE_BAR_LAYOUT                        = new SurveyPropertyKeyEnum("Use_Bar_Layout", "Use Bar Layout");
    public static final SurveyPropertyKeyEnum USE_CUSTOM_ALIGNMENT					= new SurveyPropertyKeyEnum("Use_Custom_Alignmnet", "Use Custom Alignment");
    public static final SurveyPropertyKeyEnum USE_EXISTING_ANSWER_SET               = new SurveyPropertyKeyEnum("Use_Existing_Answer_Set", "Use Existing Answer Set");
    public static final SurveyPropertyKeyEnum SURVEY_TYPE                           = new SurveyPropertyKeyEnum("Survey_Type", "SurveyType");
    public static final SurveyPropertyKeyEnum USE_EXISTING_COLUMN_ANSWER_SET        = new SurveyPropertyKeyEnum("Use_Existing_Column_Answer_Set", "Use Existing Column Answer Set");
    public static final SurveyPropertyKeyEnum USE_EXISTING_ROW_ANSWER_SET           = new SurveyPropertyKeyEnum("Use_Existing_Row_Answer_Set", "Use Existing Row Answer Set");
    public static final SurveyPropertyKeyEnum IS_REMEDIATION_CONTENT                = new SurveyPropertyKeyEnum("Is_Remediation_Content", "Is Remediation Content");
    public static final SurveyPropertyKeyEnum INSTRUCTION_TEXT                      = new SurveyPropertyKeyEnum("Instruction_Text", "Instruction Text");
    public static final SurveyPropertyKeyEnum RESPONSE_FIELD_TYPES                  = new SurveyPropertyKeyEnum("Response_Field_Types", "Response Field Types", Constants.COMMA);
    public static final SurveyPropertyKeyEnum RESPONSE_FIELD_LABELS                 = new SurveyPropertyKeyEnum("Response_Field_Labels", "Response Field Labels", Constants.PIPE);
    public static final SurveyPropertyKeyEnum RESPONSE_FIELD_LEFT_ALIGNED           = new SurveyPropertyKeyEnum("Response_Field_Left_Aligned", "Response Field Left Aligned", Constants.COMMA);
    public static final SurveyPropertyKeyEnum RESPONSE_FIELDS_PER_LINE              = new SurveyPropertyKeyEnum("Response_Fields_Per_Line", "Response Fields Per Line");
    public static final SurveyPropertyKeyEnum QUESTION_WIDGET_ID                    = new SurveyPropertyKeyEnum("Question_Widget_ID", "Question Widget ID");
    
    /** A property used to indicate whether a question uses media that is not a legacy image (e.g. an image in a course folder) */
    public static final SurveyPropertyKeyEnum QUESTION_MEDIA_TYPE_KEY               = new SurveyPropertyKeyEnum("Question_Media_Type", "Question Media Type");
    
    /** a property used to indicate the time at which a question/survey was completed */
    public static final SurveyPropertyKeyEnum COMPLETION_TIME                       = new SurveyPropertyKeyEnum("Completion_Time", "Completion Time");
    
    /** used to indicate if a learner can be given partial credit for answer to a question */
    public static final SurveyPropertyKeyEnum ALLOW_PARTIAL_CREDIT                  = new SurveyPropertyKeyEnum("Allow_Partial_Credit", "Allow Partial Credit");
    
    /** used to indicate if a question can have scoring or not (i.e. question must be in a survey type that can be scored and the question must support having answers scored */
    public static final SurveyPropertyKeyEnum CAN_QUESTION_HAVE_SCORING             = new SurveyPropertyKeyEnum("Can_Question_Have_Scoring", "Can Question Have Scoring");
    
    private static final long serialVersionUID = 1L;
    
    /** the class containing the enumerated attribute values for this survey property key */
    private AttributeValueEnumAccessor attributeAccessor;
    
    /** (optional) if the property's value is a list of values, the list delimiter string is what separates each value */
    private String listDelimiter = null;

    /**
     * Default Constructor
     *
     * Required by GWT to exist and be public because it is Serializable
     */
    public SurveyPropertyKeyEnum() {
        super();
    }

    private SurveyPropertyKeyEnum(String name, String displayName) {

        super(index++, name, displayName);
        enumList.add(this);
    }
    
    private SurveyPropertyKeyEnum(String name, String displayName, AttributeValueEnumAccessor attributeAccessor){
    	
    	this(name, displayName);
    	this.attributeAccessor = attributeAccessor;
    }
    
    private SurveyPropertyKeyEnum(String name, String displayName, String listDelimiter){
        
        this(name, displayName);
        this.listDelimiter = listDelimiter;
    }
    
    /**
     * Return the property's value is a list of values, the list delimiter string is what separates each value.
     * 
     * @return can be null (e.g. "," for a comma delimited list)
     */
    public String getListDelimiter(){
        return listDelimiter;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;

        } else if (obj instanceof SurveyPropertyKeyEnum) {

            SurveyPropertyKeyEnum enumObj = (SurveyPropertyKeyEnum) obj;

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
     * Returns whether or not this survey property key has any associated attribute values
     * 
     * @return True if the survey property key has any associated attribute values. False, otherwise.
     */
    public boolean hasAttributeValues(){
    	
    	if(this.attributeAccessor != null && !this.attributeAccessor.VALUES().isEmpty()){
    		return true;
    	}  	
    	
    	return false;
    }
    
    /**
     * Return the class containing the possible attribute values of this survey property key
     * 
     * @return Class<? extends AbstractEnum> - can be null if this survey property key has no preset attribute values
     */
    public List<? extends AbstractEnum> getAttributeValues(){
    	
        if(attributeAccessor != null){
        	return attributeAccessor.VALUES();
        }
        
        return null;
    }
    
    /**
     * Return the attribute value instance referred to by the name provided for this survey property key.
     * 
     * @param valueName - a value of this survey property key
     * @return AbstractEnum - can be null if no instance was found.
     */
    public AbstractEnum getAttributeValue(String valueName){
    	
        return AbstractEnum.valueOf(valueName, attributeAccessor.VALUES());
    }

    /**
     * Return the enumeration object that has the matching name.
     *
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     * name is not found.
     */
    public static SurveyPropertyKeyEnum valueOf(String name)
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
    public static SurveyPropertyKeyEnum valueOf(int value)
            throws EnumerationNotFoundException {

        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     *
     * @return a List of the currently defined enumerations.
     */
    public static List<SurveyPropertyKeyEnum> VALUES() {

        return Collections.unmodifiableList(enumList);
    }
    
    /**
     * Creates a new enumeration object with the specified name.
     * 
     * @param name The name of the enumeration object to be created.
     * @return SurveyPropertyKeyEnum The new enumeration object.
     */
    public static SurveyPropertyKeyEnum createEnumeration(String name){
    	return new SurveyPropertyKeyEnum(name, name);
    }
}
