/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import java.io.File;
import java.io.Serializable;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import generated.course.BooleanEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;

/**
 * This is the base class for course file handler.  It includes a reference to the default course schema.
 * 
 * @author mhoffman
 *
 */
public class AbstractCourseHandler extends AbstractSchemaHandler {
    
    /**
     * Default constructor - use default schema
     */
    public AbstractCourseHandler(){
        super(COURSE_SCHEMA_FILE);
        
    }
    
    /**
     * Class constructor - use specified schema
     * 
     * @param schemaFile - a course schema file
     */
    public AbstractCourseHandler(File schemaFile){
        super(schemaFile);
        
    }
  
    /**
     * Return a string representation of the Course actions content in XML format.
     * 
     *  @param actions - course actions element
     * @return String - course actions element as a string
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public static String getRawCourseActionKnowledge(generated.course.Actions actions) throws SAXException, JAXBException{
        return getAsXMLString(actions, generated.course.Actions.class, COURSE_SCHEMA_FILE);        
    }
    
    /**
     * Return an Actions object from the raw XML actions element string provided.
     * 
     * @param rawActions - course actions element as a string
     * @return Actions - new actions object
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public generated.course.Actions getActions(String rawActions) throws JAXBException, SAXException{       
        UnmarshalledFile uFile = parseAndValidate(generated.course.Actions.class, rawActions, true);
        return (generated.course.Actions)uFile.getUnmarshalled();
    }
    
    /**
     * Validate the Training Application course transition object against the schema.
     * 
     * @param trainingApplication - populated training application transition object
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public void validateTrainingApplication(generated.course.TrainingApplication trainingApplication) throws SAXException, JAXBException{
        if (trainingApplication.getOptions() == null
                || !BooleanEnum.TRUE.equals(trainingApplication.getOptions().getDisabled())) {
            /* #5138 - passing null here in place of getSchemaObjectFactory() to default to 
             * context class loader from thread to bypass issue about 'class clazz nor any 
             * of its super class is known to this context.' This seems to pull the correct 
             * class needed for marshaling the JAXB object. */
            validateAgainstSchema(trainingApplication, generated.course.TrainingApplication.class, null);
        }
    }
    
    /**
     * Validate the Guidance course transition object against the schema.
     * 
     * @param guidance - populated guidance transition object
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public void validateGuidance(generated.course.Guidance guidance) throws SAXException, JAXBException{
        if (!BooleanEnum.TRUE.equals(guidance.getDisabled())) {
            /* #5138 - passing null here in place of getSchemaObjectFactory() to default to 
             * context class loader from thread to bypass issue about 'class clazz nor any 
             * of its super class is known to this context.' This seems to pull the correct 
             * class needed for marshaling the JAXB object. */
            validateAgainstSchema(guidance, generated.course.Guidance.class, null);
        }
    }
    
    /**
     * Validate the Lesson Material course object object against the schema.
     * 
     * @param lesson material - populated lesson material course object
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public void validateLessonMaterial(generated.course.LessonMaterial lessonMaterial) throws SAXException, JAXBException{
        if (!BooleanEnum.TRUE.equals(lessonMaterial.getDisabled())) {
            /* #5138 - passing null here in place of getSchemaObjectFactory() to default to 
             * context class loader from thread to bypass issue about 'class clazz nor any 
             * of its super class is known to this context.' This seems to pull the correct 
             * class needed for marshaling the JAXB object. */
            validateAgainstSchema(lessonMaterial, generated.course.LessonMaterial.class, null);
        }
    }
    
    /**
     * Checks if the transition has been marked as disabled.
     * 
     * @param transition the transition to check.
     * @return true if the transition has been disabled; false otherwise.
     */
    public static boolean isTransitionDisabled(Serializable transition) {
        BooleanEnum disabled = BooleanEnum.FALSE;

        if (transition != null) {
            if (isTransitionGuidance(transition)) {
                disabled = ((generated.course.Guidance) transition).getDisabled();
            } else if (isTransitionPresentSurvey(transition)) {
                disabled = ((generated.course.PresentSurvey) transition).getDisabled();
            } else if (isTransitionLessonMaterial(transition)) {
                disabled = ((generated.course.LessonMaterial) transition).getDisabled();
            } else if (isTransitionAAR(transition)) {
                disabled = ((generated.course.AAR) transition).getDisabled();
            } else if (isTransitionTrainingApplication(transition)) {
                generated.course.TrainingApplication trainingApp = (generated.course.TrainingApplication) transition;
                if (trainingApp.getOptions() != null) {
                    disabled = trainingApp.getOptions().getDisabled();
                }
            } else if (isTransitionMerrillsBranchPoint(transition)) {
                disabled = ((generated.course.MerrillsBranchPoint) transition).getDisabled();
            } else if (isTransitionAuthoredBranch(transition)) {
                 disabled=((generated.course.AuthoredBranch) transition).getDisabled();
            }
        }

        return BooleanEnum.TRUE.equals(disabled);
    }
    
    /**
     * Checks if the serializable object is a transition.
     * 
     * @param serialObj the object to check.
     * @return true if the object is a transition object; false otherwise.
     */
    public static boolean isTransitionObject(Serializable serialObj) {
        return isTransitionGuidance(serialObj) || isTransitionPresentSurvey(serialObj) || isTransitionLessonMaterial(serialObj) || isTransitionAAR(serialObj)
                || isTransitionTrainingApplication(serialObj) || isTransitionMerrillsBranchPoint(serialObj) || isTransitionAuthoredBranch(serialObj);
    }
    
    
    
    /**
     * Checks if the serializable transition object is Guidance
     * 
     * @param transition object to check
     * @return true if transition is Guidance; false otherwise
     */
    public static boolean isTransitionGuidance(Serializable transition) {
        return transition != null && transition instanceof generated.course.Guidance;
    }

    /**
     * Checks if the serializable transition object is PresentSurvey
     * 
     * @param transition object to check
     * @return true if transition is PresentSurvey; false otherwise
     */
    public static boolean isTransitionPresentSurvey(Serializable transition) {
        return transition != null && transition instanceof generated.course.PresentSurvey;
    }

    /**
     * Checks if the serializable transition object is LessonMaterial
     * 
     * @param transition object to check
     * @return true if transition is LessonMaterial; false otherwise
     */
    public static boolean isTransitionLessonMaterial(Serializable transition) {
        return transition != null && transition instanceof generated.course.LessonMaterial;
    }

    /**
     * Checks if the serializable transition object is AAR
     * 
     * @param transition object to check
     * @return true if transition is AAR; false otherwise
     */
    public static boolean isTransitionAAR(Serializable transition) {
        return transition != null && transition instanceof generated.course.AAR;
    }

    /**
     * Checks if the serializable transition object is TrainingApplication
     * 
     * @param transition object to check
     * @return true if transition is TrainingApplication; false otherwise
     */
    public static boolean isTransitionTrainingApplication(Serializable transition) {
        return transition != null && transition instanceof generated.course.TrainingApplication;
    }

    /**
     * Checks if the serializable transition object is MerrillsBranchPoint
     * 
     * @param transition object to check
     * @return true if transition is MerrillsBranchPoint; false otherwise
     */
    public static boolean isTransitionMerrillsBranchPoint(Serializable transition) {
        return transition != null && transition instanceof generated.course.MerrillsBranchPoint;
    }

    /**
     * Checks if the serializable transition object is AuthoredBranch
     * 
     * @param transition object to check
     * @return true if transition is AuthoredBranch; false otherwise
     */
    public static boolean isTransitionAuthoredBranch(Serializable transition) {
        return transition != null && transition instanceof generated.course.AuthoredBranch;
    }
    

    /**
     * Gets the name that has been authored for the given course transition
     * 
     * @param transition the course element
     * @return the name that was authored for the given transition. Can return a null if the
     *         transition is unknown.
     */
    public static String getTransitionName(Serializable transition) throws IllegalArgumentException {

        String name = null;

        if (transition != null) {
            if (isTransitionGuidance(transition)) {
                name = ((generated.course.Guidance) transition).getTransitionName();
            } else if (isTransitionPresentSurvey(transition)) {
                name = ((generated.course.PresentSurvey) transition).getTransitionName();
            } else if (isTransitionLessonMaterial(transition)) {
                name = ((generated.course.LessonMaterial) transition).getTransitionName();
            } else if (isTransitionAAR(transition)) {
                name = ((generated.course.AAR) transition).getTransitionName();
            } else if (isTransitionTrainingApplication(transition)) {
                name = ((generated.course.TrainingApplication) transition).getTransitionName();
            } else if (isTransitionMerrillsBranchPoint(transition)) {
                name = ((generated.course.MerrillsBranchPoint) transition).getTransitionName();
            } else if (isTransitionAuthoredBranch(transition)) {
                name = ((generated.course.AuthoredBranch) transition).getTransitionName();
            }
        }

        return name;
    }
}
