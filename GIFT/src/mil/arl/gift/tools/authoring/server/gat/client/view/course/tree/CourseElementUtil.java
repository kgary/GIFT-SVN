/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

import generated.course.AAR;
import generated.course.AAR.CourseObjectsToReview;
import generated.course.ATRemoteSKO;
import generated.course.AuthoredBranch;
import generated.course.AutoTutorSKO;
import generated.course.AutoTutorSession;
import generated.course.BooleanEnum;
import generated.course.ConceptQuestions;
import generated.course.ConceptQuestions.AssessmentRules;
import generated.course.ConceptQuestions.AssessmentRules.AboveExpectation;
import generated.course.ConceptQuestions.AssessmentRules.AtExpectation;
import generated.course.ConceptQuestions.AssessmentRules.BelowExpectation;
import generated.course.ConceptQuestions.QuestionTypes;
import generated.course.Conversation;
import generated.course.ConversationTreeFile;
import generated.course.CustomInteropInputs;
import generated.course.CustomParameters;
import generated.course.DETestbedInteropInputs;
import generated.course.DISInteropInputs;
import generated.course.LogFile;
import generated.course.DkfRef;
import generated.course.EmptyInteropInputs;
import generated.course.Example;
import generated.course.FixedDecayMandatoryBehavior;
import generated.course.GenericLoadInteropInputs;
import generated.course.Guidance;
import generated.course.HAVENInteropInputs;
import generated.course.ImageProperties;
import generated.course.Interop;
import generated.course.InteropInputs;
import generated.course.Interops;
import generated.course.LessonMaterial;
import generated.course.LessonMaterialFiles;
import generated.course.LessonMaterialList;
import generated.course.LocalSKO;
import generated.course.LtiConcepts;
import generated.course.LtiProperties;
import generated.course.MandatoryOption;
import generated.course.Media;
import generated.course.MerrillsBranchPoint;
import generated.course.MobileApp;
import generated.course.Nvpair;
import generated.course.PDFProperties;
import generated.course.PowerPointInteropInputs;
import generated.course.Practice;
import generated.course.Practice.PracticeConcepts;
import generated.course.PresentSurvey;
import generated.course.PresentSurvey.ConceptSurvey;
import generated.course.Recall;
import generated.course.RIDEInteropInputs;
import generated.course.Rule;
import generated.course.SCATTInteropInputs;
import generated.course.ShowAvatarInitially;
import generated.course.SimpleExampleTAInteropInputs;
import generated.course.SimpleMandatoryBehavior;
import generated.course.Size;
import generated.course.SlideShowProperties;
import generated.course.TC3InteropInputs;
import generated.course.TrainingApplication;
import generated.course.Transitions;
import generated.course.UnityInteropInputs;
import generated.course.VBSInteropInputs;
import generated.course.VREngageInteropInputs;
import generated.course.WebpageProperties;
import generated.course.YoutubeVideoProperties;
import generated.course.VideoProperties;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.metadata.MetadataWrapper.ContentTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.FileOperationProgressModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShow;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShowResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;

/**
 * A class used to perform common operations with course elements, such as retrieving display names to be used in user interfaces.
 * 
 * @author nroberts
 */
public class CourseElementUtil {
    
    /** The logger. */
    private static Logger logger = Logger.getLogger(CourseElementUtil.class.getName());
    
    /**
     * names of images to use for course objects
     */
    private static final String MOBILE_APP_IMG = "images/mobile-app.png";
    private static final String UNITY_IMG = "images/Unity.png";
    private static final String SLIDESHOW_IMG = "images/slideshow_icon.png";
    private static final String PDF_IMG = "images/pdf_icon.png";
    private static final String YOUTUBE_IMG = "images/youtube_icon.png";
    private static final String VIDEO_IMG = "images/video_icon.png";
    private static final String IMAGE_IMG = "images/image_icon.png";
    private static final String WEB_IMG = "images/web_icon.png";
    private static final String FILE_IMG = "images/file.png";
    private static final String LTI_IMG = "images/transitions/lti.png";
    
    /**
     * An object containing data related to displaying course element information
     * 
     * @author nroberts
     */
    public static class ElementDisplayData{
        
        /** The element's display name */
        private CourseObjectName name;
        
        /** The element's associated icon */
        private String icon;
        
        /**
         * Creates a new set of display data with the given display name and icon
         * 
         * @param name the display name for this display data
         * @param icon the icon for this display data
         */
        public ElementDisplayData(CourseObjectName name, String icon){
            this.name = name;
            this.icon = icon;
        }
        
        /**
         * Gets the display name
         * 
         * @return the display name
         */
        public CourseObjectName getName(){
            return name;
        }
        
        /**
         * Gets the display icon
         * 
         * @return the display icon
         */
        public String getIcon(){
            return icon;
        }
    }

    /** A mapping from each course element type to its associated display name */
    private final static Map<Class<?>, ElementDisplayData> ELEMENT_CLASS_TO_DISPLAY_NAME = new HashMap<Class<?>, ElementDisplayData>();
    
    private static ErrorDetailsDialog errorDialog = null;
    
    static{
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(Guidance.class, new ElementDisplayData(
                CourseObjectName.GUIDANCE, 
                "images/transitions/guidance.png")
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(PresentSurvey.class, new ElementDisplayData(
                CourseObjectName.SURVEY,
                "images/transitions/survey.png")
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(LessonMaterial.class, new ElementDisplayData(
                CourseObjectName.LESSON_MATERIAL, 
                "images/transitions/lm.png")
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(AAR.class, new ElementDisplayData(
                CourseObjectName.AAR, 
                "images/transitions/aar-2.png")
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(TrainingApplication.class, new ElementDisplayData(
                CourseObjectName.TRAINING_APPLICATION, 
                "images/transitions/ta.png")
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(MerrillsBranchPoint.class, new ElementDisplayData(
                CourseObjectName.ADAPTIVE_COURSEFLOW, 
                "images/transitions/mbp.png")
        );
        
        ELEMENT_CLASS_TO_DISPLAY_NAME.put(AuthoredBranch.class, new ElementDisplayData(
                CourseObjectName.AUTHORED_BRANCH,
                "images/transitions/authored_branch.png")
        );
    }
    
    /**
     * Callback for copying a course object.
     */
    public interface CopyCourseObjectCallback {
        void onCopy(Serializable copiedTransition);
    }
    
    /**
     * Gets the display name for the given course element's type
     * 
     * @param courseElement the course element from which to get the type
     * @return the display name corresponding to the course element's type
     */
    public static String getTypeDisplayName(Serializable courseElement){
        
        if(courseElement instanceof PresentSurvey){
            
            PresentSurvey survey = (PresentSurvey) courseElement;
            
            if(survey.getSurveyChoice() != null){
                
                if(survey.getSurveyChoice() instanceof String){
                    return "Survey/Test";
                
                } else if(survey.getSurveyChoice() instanceof ConceptSurvey){
                    return "Question Bank";
                
                } else if(survey.getSurveyChoice() instanceof Conversation){
                    
                    Conversation conversation = (Conversation) survey.getSurveyChoice();
                    
                    if(conversation.getType() != null){
                        
                        if(conversation.getType() instanceof AutoTutorSession){
                            return "AutoTutor Conversation";
                            
                        } else if(conversation.getType() instanceof ConversationTreeFile){
                            return "Conversation Tree";
                        }
                    }
                }
            }
        
        } else if(courseElement instanceof Guidance){
            
            Guidance survey = (Guidance) courseElement;
            
            if(survey.getGuidanceChoice() != null){
                
                if(survey.getGuidanceChoice() instanceof Guidance.Message){
                    return "Information as Text";
                
                } else if(survey.getGuidanceChoice() instanceof Guidance.File){
                    return "Information from File";
                
                } else if(survey.getGuidanceChoice() instanceof Guidance.URL){
                    return "Information from Web";
                }
            }
        
        } else if(courseElement instanceof TrainingApplication){
            
            TrainingApplication app = (TrainingApplication) courseElement;
            
            if(app.getInterops() != null) {
                Interops interops = app.getInterops();
                if(interops != null && interops.getInterop() != null){
                    
                    for(Interop interop : interops.getInterop()){
                        
                        TrainingApplicationEnum type = TrainingAppUtil.getTrainingAppTypes(interop.getInteropImpl());
                        
                        if(TrainingApplicationEnum.POWERPOINT.equals(type)){
                            return getContentTypeTitle(ContentTypeEnum.POWERPOINT);
                            
                        } else if(TrainingApplicationEnum.VBS.equals(type)){
                            return getContentTypeTitle(ContentTypeEnum.VIRTUAL_BATTLESPACE);
                            
                        } else if(TrainingApplicationEnum.TC3.equals(type)){
                            return getContentTypeTitle(ContentTypeEnum.TC3);
                            
                        } else if(TrainingApplicationEnum.DE_TESTBED.equals(type)){
                            return getContentTypeTitle(ContentTypeEnum.DE_TESTBED);
                            
                        } else if(TrainingApplicationEnum.SUDOKU.equals(type)){
                            return "Sudoku";
                            
                        } else if(TrainingApplicationEnum.ARES.equals(type)){
                            return getContentTypeTitle(ContentTypeEnum.ARES);
                            
                        } else if(TrainingApplicationEnum.SIMPLE_EXAMPLE_TA.equals(type)){
                            return getContentTypeTitle(ContentTypeEnum.DEMO_APPLICATION);

                        } else if(TrainingApplicationEnum.VR_ENGAGE.equals(type)){
                            return getContentTypeTitle(ContentTypeEnum.VR_ENGAGE);
                            
                        } else if(TrainingApplicationEnum.UNITY_DESKTOP.equals(type)){
                            return getContentTypeTitle(ContentTypeEnum.UNITY);
                        } else if(TrainingApplicationEnum.HAVEN.equals(type)){
                            return getContentTypeTitle(ContentTypeEnum.HAVEN);
                            
                        } else if(TrainingApplicationEnum.RIDE.equals(type)){
                            return getContentTypeTitle(ContentTypeEnum.RIDE);
                            
                        }
                    }
                } else {
                    return "Training Application";
                }
            } else if (app.getEmbeddedApps() != null) {
                if (app.getEmbeddedApps().getEmbeddedApp() != null
                        && app.getEmbeddedApps().getEmbeddedApp().getEmbeddedAppImpl() instanceof MobileApp) {
                    return "Mobile Events";
                } else {
                    return "Unity WebGL";
                }
			}
            
        } else if(courseElement instanceof LessonMaterial) {
            LessonMaterial lessonMaterial = (LessonMaterial) courseElement;
            if(lessonMaterial.getLessonMaterialList() != null 
                    && lessonMaterial.getLessonMaterialList().getMedia() != null
                    && !lessonMaterial.getLessonMaterialList().getMedia().isEmpty()) {
                Media media = lessonMaterial.getLessonMaterialList().getMedia().get(0);

                if(media.getMediaTypeProperties() instanceof SlideShowProperties) {
                    return getContentTypeTitle(ContentTypeEnum.SLIDE_SHOW);
                    
                } else if(lessonMaterial.getLessonMaterialList().getIsCollection() == BooleanEnum.FALSE) {
                    
                    if(media.getMediaTypeProperties() instanceof PDFProperties) {
                        return getContentTypeTitle(ContentTypeEnum.PDF);
                    } else if(media.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                        return getContentTypeTitle(ContentTypeEnum.YOUTUBE_VIDEO);
                    } else if(media.getMediaTypeProperties() instanceof VideoProperties) {
                        return getContentTypeTitle(ContentTypeEnum.LOCAL_VIDEO);
                    } else if(media.getMediaTypeProperties() instanceof ImageProperties) {
                        return getContentTypeTitle(ContentTypeEnum.LOCAL_IMAGE);
                    } else if(media.getMediaTypeProperties() instanceof WebpageProperties) {
                        if(isWebAddress(media)) {
                            return getContentTypeTitle(ContentTypeEnum.WEB_ADDRESS);
                        } else {
                            return getContentTypeTitle(ContentTypeEnum.LOCAL_WEBPAGE);
                        }
                    } else if (media.getMediaTypeProperties() instanceof LtiProperties) {
                        return getContentTypeTitle(ContentTypeEnum.LTI_PROVIDER);
                    }
                }
            }
            
            return "Media Collection";
        }
        
        Class<?> clazz = courseElement.getClass();
        
        ElementDisplayData name = ELEMENT_CLASS_TO_DISPLAY_NAME.get(clazz);
        
        if(name != null){
            return ELEMENT_CLASS_TO_DISPLAY_NAME.get(clazz).getName().getDisplayName();
            
        } else {
            return null;
        }
    }
    
    /**
     * Gets the name that has been authored for the given course transition
     * 
     * @param courseElement the course element
     * @return the name that was authored for the given transition
     * @throws IllegalArgumentException if the object given as a course transition is not a known transition and displays an ErrorDetailsDialog
     */
    public static String getTransitionName(Serializable courseTransition) throws IllegalArgumentException {
        
        String name = GatClientUtility.getTransitionName(courseTransition);
        if(name == null){
            String courseName = GatClientUtility.getCourseName();
            String message = "There was an error getting the name of the course object";
            String details = (courseTransition == null ? "A course object in the course \"" + courseName + "\" is null." : 
                    "The course object (" + courseTransition.getClass() +") in the course \"" + courseName + "\" was not identified as a course transition.");
            
            if(errorDialog == null || !errorDialog.isShowing()) {
                errorDialog = new ErrorDetailsDialog(message, details, null);
                errorDialog.setText("Course Object Error");
                errorDialog.center();
            }
            
            throw new IllegalArgumentException(message + " " + details);
        }
        
        return name;
    }
    
    /**
     * Sets the name for the given course transition
     * 
     * @param courseElement the course element
     * @param name the name to apply to the given transition
     * @throws IllegalArgumentException if the object given as a course transition is not a known transition and displays an ErrorDetailsDialog
     */
    public static void setTransitionName(Serializable courseTransition, String name) throws IllegalArgumentException{
        
        if(isTransitionGuidance(courseTransition)){         
            ((Guidance) courseTransition).setTransitionName(name);
        } else if(isTransitionPresentSurvey(courseTransition)){         
            ((PresentSurvey) courseTransition).setTransitionName(name);
        } else if(isTransitionLessonMaterial(courseTransition)){        
            LessonMaterial lm = (LessonMaterial) courseTransition;
            lm.setTransitionName(name);
            
            // If this is not a media collection and the media name is blank, the media item name
            // should match the transition name
            if (lm.getLessonMaterialList() != null && lm.getLessonMaterialList().getIsCollection() == BooleanEnum.FALSE
                    && lm.getLessonMaterialList().getMedia() != null && !lm.getLessonMaterialList().getMedia().isEmpty()) {
                Media media = lm.getLessonMaterialList().getMedia().get(0);
                if (media.getName() == null || media.getName().trim().isEmpty()) {
                    media.setName(name);
                }
            }
            
        } else if(isTransitionAAR(courseTransition)){           
            ((AAR) courseTransition).setTransitionName(name);
        } else if(isTransitionTrainingApplication(courseTransition)){           
            ((TrainingApplication) courseTransition).setTransitionName(name);
        } else if(isTransitionMerrillsBranchPoint(courseTransition)){           
            ((MerrillsBranchPoint) courseTransition).setTransitionName(name);       
        } else if(isTransitionAuthoredBranch(courseTransition)){            
            ((AuthoredBranch) courseTransition).setTransitionName(name);    
        } else {
            String courseName = GatClientUtility.getCourseName();
            String message = "There was an error setting the name of the course object";
            String details = (courseTransition == null ? "A course object in the course \"" + courseName + "\" is null." : 
                    "The course object (" + courseTransition.getClass() +") in the course \"" + courseName + "\" was not identified as a course transition.");
            
            if(errorDialog == null || !errorDialog.isShowing()) {
                errorDialog = new ErrorDetailsDialog(message, details, null);
                errorDialog.setText("Course Object Error");
                errorDialog.center();
            }
            
            throw new IllegalArgumentException(message + " " + details);
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
                disabled = ((AuthoredBranch) transition).getDisabled();
            }
        }

        return BooleanEnum.TRUE.equals(disabled);
    }
    
    /**
     * Gets the icon for the given course element type
     * 
     * @param courseElementClass the course element type
     * @return the icon corresponding to the type
     */
    public static String getIconFromType(Class<?> courseElementClass){
        
        ElementDisplayData name = ELEMENT_CLASS_TO_DISPLAY_NAME.get(courseElementClass);
        
        if(name != null){
            return ELEMENT_CLASS_TO_DISPLAY_NAME.get(courseElementClass).getIcon();
            
        } else {
            return null;
        }
    }
    
    /**
     * Return the title (tooltip) for the content type provided.
     * 
     * @param contentType the content type to get the title for.  If null a default title will
     * be provided.
     * @return the title for the content type (e.g. Highlight Passage).  Won't be null or empty.
     */
    public static String getContentTypeTitle(ContentTypeEnum contentType){
        
        if(contentType == null){
            return "UNKNOWN TYPE - null";
        }
        
        // NOTE: images/icons and tool tip text are taken from:
        // ContentReferencedEditor.ui.xml, TrainingAppInteropEditor.ui.xml
        switch(contentType){
        case ARES:
            return TrainingApplicationEnum.ARES.getDisplayName();
        case DE_TESTBED:
            return TrainingApplicationEnum.DE_TESTBED.getDisplayName();
        case DEMO_APPLICATION:
            return TrainingApplicationEnum.SIMPLE_EXAMPLE_TA.getDisplayName();
        case HIGHLIGHT_PASSAGE:
            return "Highlight Passage";
        case CONVERSATION_TREE:
            return "Conversation Tree";
        case LOCAL_IMAGE:
            return "Image";
        case LOCAL_WEBPAGE:
            return "Local Webpage";
        case LTI_PROVIDER:
            return "LTI provider";
        case PDF:
            return "PDF";
        case POWERPOINT:
            return TrainingApplicationEnum.POWERPOINT.getDisplayName();
        case SLIDE_SHOW:
            return "Slide Show";
        case SUMMARIZE_PASSAGE:
            return "Summarize Passage";
        case TC3:
            return TrainingApplicationEnum.TC3.getDisplayName();
        case UNITY:
            return TrainingApplicationEnum.UNITY_DESKTOP.getDisplayName();
        case VIRTUAL_BATTLESPACE:
            return TrainingApplicationEnum.VBS.getDisplayName();
        case VR_ENGAGE:
            return TrainingApplicationEnum.VR_ENGAGE.getDisplayName();
        case HAVEN:
            return TrainingApplicationEnum.HAVEN.getDisplayName();
        case RIDE:
            return TrainingApplicationEnum.RIDE.getDisplayName();
        case WEB_ADDRESS:
            return "Web Address";
        case YOUTUBE_VIDEO:
            return "YouTube video";
        case LOCAL_VIDEO:
            return "Local video";
        default:
            return "UNKNOWN TYPE - "+contentType;
        }
    }
    
    /**
     * Return the element that graphically depicts the content type.
     * @param contentType the content type to get a graphic element for.  If null a default
     * question mark element will be returned.
     * @return the graphical element for the content type.  Won't be null.
     */
    public static Element getContentTypeGraphic(ContentTypeEnum contentType){
        
        if(contentType == null){
            return new Icon(IconType.QUESTION_CIRCLE_O).getElement();
        }
        
        // NOTE: images/icons and tool tip text are taken from:
        // ContentReferencedEditor.ui.xml, TrainingAppInteropEditor.ui.xml
        Image image;
        Icon icon;
        String imageWidth = "25px";
        switch(contentType){
        case ARES:
            image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.ARES));
            image.setWidth(imageWidth);
            return image.getElement();
        case DE_TESTBED:
            image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.DE_TESTBED));
            image.setWidth(imageWidth);
            return image.getElement();
        case DEMO_APPLICATION:
            image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.SIMPLE_EXAMPLE_TA));
            image.setWidth(imageWidth);
            return image.getElement();
        case HIGHLIGHT_PASSAGE:
            icon = new Icon(IconType.PENCIL_SQUARE_O);
            icon.getElement().getStyle().setFontSize(25, Unit.PX);
            return icon.getElement();
        case CONVERSATION_TREE:
            icon = new Icon(IconType.COMMENTS);
            icon.getElement().getStyle().setFontSize(25, Unit.PX);
            return icon.getElement();
        case LOCAL_IMAGE:
            icon = new Icon(IconType.IMAGE);
            icon.getElement().getStyle().setFontSize(25, Unit.PX);
            return icon.getElement(); 
        case LOCAL_WEBPAGE:
            icon = new Icon(IconType.FILE);
            icon.getElement().getStyle().setFontSize(25, Unit.PX);
            return icon.getElement();  
        case LTI_PROVIDER:
            image = new Image(LTI_IMG);
            image.setWidth(imageWidth);
            return image.getElement();
        case PDF:
            icon = new Icon(IconType.FILE_PDF_O);
            icon.getElement().getStyle().setFontSize(25, Unit.PX);
            return icon.getElement();  
        case POWERPOINT:
            image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.POWERPOINT));
            image.setWidth(imageWidth);
            return image.getElement();
        case SLIDE_SHOW:
            image = new Image(SLIDESHOW_IMG);
            image.setWidth(imageWidth);
            return image.getElement();
        case SUMMARIZE_PASSAGE:
            icon = new Icon(IconType.LIST);
            icon.getElement().getStyle().setFontSize(25, Unit.PX);
            return icon.getElement();
        case TC3:
            image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.TC3));
            image.setWidth(imageWidth);
            return image.getElement();
        case UNITY:
            image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.UNITY_DESKTOP));
            image.setWidth(imageWidth);
            return image.getElement();
        case VIRTUAL_BATTLESPACE:
            image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.VBS));
            image.setWidth(imageWidth);
            return image.getElement();
        case VR_ENGAGE:
            image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.VR_ENGAGE));
            image.setWidth(imageWidth);
            return image.getElement();
        case HAVEN:
            image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.HAVEN));
            image.setWidth(imageWidth);
            return image.getElement();
        case RIDE:
            image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(TrainingApplicationEnum.RIDE));
            image.setWidth(imageWidth);
            return image.getElement();
        case WEB_ADDRESS:
            icon = new Icon(IconType.GLOBE);
            icon.getElement().getStyle().setFontSize(25, Unit.PX);
            return icon.getElement();  
        case YOUTUBE_VIDEO:
            icon = new Icon(IconType.YOUTUBE_PLAY);
            icon.getElement().getStyle().setFontSize(25, Unit.PX);
            return icon.getElement();
        case LOCAL_VIDEO:
            icon = new Icon(IconType.FILE_VIDEO_O);
            icon.getElement().getStyle().setFontSize(25, Unit.PX);
            return icon.getElement();
        default:
            icon = new Icon(IconType.QUESTION_CIRCLE_O);
            icon.getElement().getStyle().setFontSize(25, Unit.PX);
            return icon.getElement();
        }
    }
    
    /**
     * Returns an HTML img element using the path provided as the image source location.
     * 
     * @param imagePath a path to an image (e.g. "images/VBS3.png")
     * @return an img HTML element like 
     * <img src=\""+CourseElementUtil.getTrainingAppTypeIcon(currentTrainingAppType) +"\" width=\"25px\" height=\"25px\">
     */
    public static String getCourseObjectTypeImgTag(String imagePath){
        return "<img src=\""+ imagePath +"\" width=\"25px\" height=\"25px\">";
    }
    
    /**
     * Gets the icon for the given course element's type
     * 
     * @param courseElement the course element from which to get the type
     * @return the icon corresponding to the course element's type
     */
    public static String getTypeIcon(Serializable courseElement){
        
        if(courseElement instanceof TrainingApplication){
        
            TrainingApplication app = (TrainingApplication) courseElement;
            TrainingApplicationEnum type = TrainingAppUtil.getTrainingAppType(app);
            
            if(type != null) {
                // This is the ideal entry point to this if/else block
                return TrainingApplicationEnum.getTrainingAppTypeIcon(type);
            } else if(app.getInterops() != null) {
                
                Interops interops = app.getInterops();
                
                if(interops != null && interops.getInterop() != null){
                    
                    for(Interop interop : interops.getInterop()){
                        // uses the first interop to determine the training app type
                        // NOT ideal but works as a fall back for now...
                        
                        type = TrainingAppUtil.getTrainingAppTypes(interop.getInteropImpl()); 
                        app.setTrainingAppTypeEnum(type.getName());
                        return TrainingApplicationEnum.getTrainingAppTypeIcon(type);
                    }
                } else {
                    return "images/transitions/ta.png";
                }
                
            } else if (app.getEmbeddedApps() != null) {
			    
			    if(app.getEmbeddedApps() != null 
			            && app.getEmbeddedApps().getEmbeddedApp() != null
			            && app.getEmbeddedApps().getEmbeddedApp().getEmbeddedAppImpl() instanceof MobileApp) {
			    
			        app.setTrainingAppTypeEnum(TrainingApplicationEnum.MOBILE_DEVICE_EVENTS.getName());
			        return MOBILE_APP_IMG;
			       
			    } else {
			        app.setTrainingAppTypeEnum(TrainingApplicationEnum.UNITY_EMBEDDED.getName());
			        return UNITY_IMG;
			    }
			}
        } else if(courseElement instanceof LessonMaterial) {
            LessonMaterial lessonMaterial = (LessonMaterial) courseElement;
            if(lessonMaterial.getLessonMaterialList() != null 
                    && lessonMaterial.getLessonMaterialList().getMedia() != null
                    && !lessonMaterial.getLessonMaterialList().getMedia().isEmpty()) {

                Media media = lessonMaterial.getLessonMaterialList().getMedia().get(0);
                
                if(media.getMediaTypeProperties() instanceof SlideShowProperties) {
                    return SLIDESHOW_IMG;
                    
                }  else if(lessonMaterial.getLessonMaterialList().getIsCollection() == BooleanEnum.FALSE) {
                    
                    if(media.getMediaTypeProperties() instanceof PDFProperties) {
                        return PDF_IMG;
                    } else if(media.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                        return YOUTUBE_IMG;
                    } else if(media.getMediaTypeProperties() instanceof ImageProperties) {
                        return IMAGE_IMG;
                    } else if(media.getMediaTypeProperties() instanceof VideoProperties) {
                        return VIDEO_IMG;
                    } else if(media.getMediaTypeProperties() instanceof WebpageProperties) {
                        if(isWebAddress(media)) {
                            return WEB_IMG;
                        } else {
                            return FILE_IMG;
                        }
                    }else if(media.getMediaTypeProperties() instanceof LtiProperties) {
                        return LTI_IMG;
                    }
                }
            }
            
        }
        
        return getIconFromType(courseElement.getClass());
    }

    /**
     * Checks whether the given media object is a web address
     * 
     * @param media the media object to check
     * @return whether the given media object is a web address
     */
    public static boolean isWebAddress(Media media) {
        return (media.getUri() != null && (media.getUri().contains("://") || media.getUri().contains("www.")));
    }
    
    /**
     * Checks whether the given media object is a web address
     * 
     * @param media the media object to check
     * @return whether the given media object is a web address
     */
    public static boolean isWebAddress(generated.dkf.Media media) {
        return (media.getUri() != null && (media.getUri().contains("://") || media.getUri().contains("www.")));
    }
    
    /**
     * Creates a copy of a course object & all of its elements. 
     * 
     * @param courseTransition the course object to copy
     * @param newName The name to give this transition
     * @param callback The callback to execute when the copy is complete.
     * @return the copied course object with " - Copy" at the end of the transition name.
     * @throws IllegalArgumentException if the object given as a course transition is not a known transition and displays an ErrorDetailsDialog
     */
    public static void copyCourseObject(Serializable courseTransition, String newName, CopyCourseObjectCallback callback) throws IllegalArgumentException{
        
        boolean hasAsyncOperation = false;
        Serializable transitionCopy = null;
        
        // Note: only PresentSurvey has the 'Mandatory' feature implemented.
        if(courseTransition instanceof Guidance){
            
            logger.info("Copying a guidance based course object.");
            Guidance transition = (Guidance) courseTransition;
            transitionCopy = new Guidance();
            Guidance copy = (Guidance) transitionCopy;
            
            copy.setTransitionName(newName);
            copy.setDisplayTime(transition.getDisplayTime());
            copy.setFullScreen(transition.getFullScreen());
            copy.setDisabled(transition.getDisabled());
            
            if(transition.getGuidanceChoice() != null) {
                if(transition.getGuidanceChoice() instanceof Guidance.Message) {
                    Guidance.Message msg = (Guidance.Message) transition.getGuidanceChoice();
                    Guidance.Message msgCopy = new Guidance.Message();
                    msgCopy.setContent(msg.getContent());
                    copy.setGuidanceChoice(msgCopy);
                    
                } else if(transition.getGuidanceChoice() instanceof Guidance.URL) {
                    Guidance.URL url = (Guidance.URL) transition.getGuidanceChoice();
                    Guidance.URL urlCopy = new Guidance.URL();
                    urlCopy.setAddress(url.getAddress());
                    urlCopy.setMessage(url.getMessage());
                    copy.setGuidanceChoice(urlCopy);
                    
                } else if(transition.getGuidanceChoice() instanceof Guidance.File) {
                    Guidance.File file = (Guidance.File) transition.getGuidanceChoice();
                    Guidance.File fileCopy = new Guidance.File();
                    fileCopy.setHTML(file.getHTML());
                    fileCopy.setMessage(file.getMessage());
                    copy.setGuidanceChoice(fileCopy);
                }
            }
            
        } else if(courseTransition instanceof PresentSurvey){   
            
            logger.info("Copying a survey based course object.");
            PresentSurvey transition = (PresentSurvey) courseTransition;
            transitionCopy = new PresentSurvey();
            PresentSurvey copy = (PresentSurvey) transitionCopy;
            
            if(transition.getFullScreen() != null){
                copy.setFullScreen(BooleanEnum.fromValue(transition.getFullScreen().value()));
            }
            if(transition.getShowInAAR() != null){
                copy.setShowInAAR(BooleanEnum.fromValue(transition.getShowInAAR().value()));
            }
            copy.setTransitionName(newName);
            if(transition.getDisabled() != null){
                copy.setDisabled(BooleanEnum.fromValue(transition.getDisabled().value()));
            }
            if(transition.getSharedSurvey() != null){
                copy.setSharedSurvey(BooleanEnum.fromValue(transition.getSharedSurvey().value()));
            }
            
            if(transition.getMandatoryOption() != null){
                MandatoryOption mandatoryOptionCopy = new MandatoryOption();
                Serializable behavior = transition.getMandatoryOption().getMandatoryBehavior();
                Serializable behaviorCopy = null;
                if(behavior instanceof SimpleMandatoryBehavior){
                    SimpleMandatoryBehavior simpleCopy = new SimpleMandatoryBehavior();
                    simpleCopy.setUseExistingLearnerStateIfAvailable(((SimpleMandatoryBehavior)behavior).isUseExistingLearnerStateIfAvailable());
                    behaviorCopy = simpleCopy;
                }else if(behavior instanceof FixedDecayMandatoryBehavior){
                    FixedDecayMandatoryBehavior fixedCopy = new FixedDecayMandatoryBehavior();
                    fixedCopy.setLearnerStateShelfLife(((FixedDecayMandatoryBehavior)behavior).getLearnerStateShelfLife());
                    behaviorCopy = fixedCopy;
                }
                mandatoryOptionCopy.setMandatoryBehavior(behaviorCopy);
                copy.setMandatoryOption(mandatoryOptionCopy);
            }

            
            if(transition.getSurveyChoice() != null) {
                if(transition.getSurveyChoice() instanceof Conversation) {
                    Conversation conv = (Conversation) transition.getSurveyChoice();
                    Conversation convCopy = new Conversation();
                    
                    if(conv.getType() != null) {
                        if(conv.getType() instanceof ConversationTreeFile) {
                            ConversationTreeFile ctf = (ConversationTreeFile) conv.getType();
                            ConversationTreeFile ctfCopy = new ConversationTreeFile();
                            ctfCopy.setName(ctf.getName());
                            convCopy.setType(ctfCopy);
                            
                        } else if (conv.getType() instanceof AutoTutorSession) {
                            AutoTutorSession ats = (AutoTutorSession) conv.getType();
                            AutoTutorSession atsCopy = new AutoTutorSession();
                            
                            if(ats.getAutoTutorConfiguration() != null) {
                                if(ats.getAutoTutorConfiguration() instanceof AutoTutorSKO) {
                                    AutoTutorSKO atSko = (AutoTutorSKO) ats.getAutoTutorConfiguration();
                                    AutoTutorSKO atSkoCopy = new AutoTutorSKO();
                                    
                                    if(atSko.getScript() != null) {
                                        if(atSko.getScript() instanceof ATRemoteSKO) {
                                            ATRemoteSKO atrSko = (ATRemoteSKO) atSko.getScript();
                                            ATRemoteSKO atrSkoCopy = new ATRemoteSKO();
                                            
                                            if(atrSko.getURL() != null) {
                                                ATRemoteSKO.URL url = atrSko.getURL();
                                                ATRemoteSKO.URL urlCopy = new ATRemoteSKO.URL();
                                                urlCopy.setAddress(url.getAddress());
                                                atrSkoCopy.setURL(urlCopy);
                                            }
                                            
                                            atSkoCopy.setScript(atrSkoCopy);
                                            
                                        } else if (atSko.getScript() instanceof LocalSKO) {
                                            LocalSKO localSko = (LocalSKO) atSko.getScript();
                                            LocalSKO localSkoCopy = new LocalSKO();
                                            localSkoCopy.setFile(localSko.getFile());
                                            atSkoCopy.setScript(localSkoCopy);
                                        }
                                    }
                                    
                                    atsCopy.setAutoTutorConfiguration(atSkoCopy);
                                    
                                } else if (ats.getAutoTutorConfiguration() instanceof DkfRef) {
                                    DkfRef dkfRef = (DkfRef) ats.getAutoTutorConfiguration();
                                    DkfRef dkfRefCopy = new DkfRef();                                   
                                    dkfRefCopy.setFile(dkfRef.getFile());
                                    atsCopy.setAutoTutorConfiguration(dkfRefCopy);
                                }
                            }
                            
                            convCopy.setType(atsCopy);
                        }
                    }
                    
                    copy.setSurveyChoice(convCopy);
                    
                } else if (transition.getSurveyChoice() instanceof ConceptSurvey) {
                    ConceptSurvey conceptSurvey = (ConceptSurvey) transition.getSurveyChoice();
                    ConceptSurvey conceptSurveyCopy = new ConceptSurvey();
                    
                    List<ConceptQuestions> conceptSurveyCopyQs = conceptSurveyCopy.getConceptQuestions();
                    for(ConceptQuestions conceptQs : conceptSurvey.getConceptQuestions()){
                        
                        ConceptQuestions conceptQsCopy = new ConceptQuestions();
                        conceptQsCopy.setName(conceptQs.getName());
                        
                        AssessmentRules assessmentRulesCopy = new AssessmentRules();
                        AboveExpectation aboveExpectationCopy = new AboveExpectation();
                        aboveExpectationCopy.setNumberCorrect(conceptQs.getAssessmentRules().getAboveExpectation().getNumberCorrect());
                        assessmentRulesCopy.setAboveExpectation(aboveExpectationCopy);
                        AtExpectation atExpectationCopy = new AtExpectation();
                        atExpectationCopy.setNumberCorrect(conceptQs.getAssessmentRules().getAtExpectation().getNumberCorrect());
                        assessmentRulesCopy.setAtExpectation(atExpectationCopy);
                        BelowExpectation belowExpectationCopy = new BelowExpectation();
                        belowExpectationCopy.setNumberCorrect(conceptQs.getAssessmentRules().getBelowExpectation().getNumberCorrect());
                        assessmentRulesCopy.setBelowExpectation(belowExpectationCopy);
                        conceptQsCopy.setAssessmentRules(assessmentRulesCopy);
                        
                        QuestionTypes questionTypesCopy = new QuestionTypes();
                        questionTypesCopy.setEasy(conceptQs.getQuestionTypes().getEasy());
                        questionTypesCopy.setHard(conceptQs.getQuestionTypes().getHard());
                        questionTypesCopy.setMedium(conceptQs.getQuestionTypes().getMedium());
                        conceptQsCopy.setQuestionTypes(questionTypesCopy);
                        
                        conceptSurveyCopyQs.add(conceptQsCopy);
                    }
                    
                    conceptSurveyCopy.setFullScreen(conceptSurvey.getFullScreen());
                    conceptSurveyCopy.setGIFTSurveyKey(conceptSurvey.getGIFTSurveyKey());
                    conceptSurveyCopy.setSkipConceptsByExamination(conceptSurvey.getSkipConceptsByExamination());
                    copy.setSurveyChoice(conceptSurveyCopy);
                    
                } else if (transition.getSurveyChoice() instanceof String) {
                    copy.setSurveyChoice(transition.getSurveyChoice());
                }
            }
            
        } else if(courseTransition instanceof LessonMaterial){
            
            logger.info("Copying a lesson material based course object.");
            LessonMaterial transition = (LessonMaterial) courseTransition;
            transitionCopy = new LessonMaterial();
            LessonMaterial copy = (LessonMaterial) transitionCopy;
            
            copy.setTransitionName(newName);
            copy.setDisabled(transition.getDisabled());
            
            if(transition.getLessonMaterialList() != null && transition.getLessonMaterialList().getMedia() != null) {
                LessonMaterialList lmlCopy = new LessonMaterialList();
                lmlCopy.setIsCollection(transition.getLessonMaterialList().getIsCollection());
                
                for(Media media : transition.getLessonMaterialList().getMedia()) {
                    Media mediaCopy = new Media();
                    mediaCopy.setUri(media.getUri());
                    mediaCopy.setName(media.getName());
                    mediaCopy.setMessage(media.getMessage());
                    
                    if(media.getMediaTypeProperties() != null) {
                        if(media.getMediaTypeProperties() instanceof SlideShowProperties) {
                            SlideShowProperties props = (SlideShowProperties) media.getMediaTypeProperties();
                            SlideShowProperties propsCopy = new SlideShowProperties();
                            
                            mediaCopy.setName(copy.getTransitionName());
                            propsCopy.setDisplayPreviousSlideButton(props.getDisplayPreviousSlideButton());
                            propsCopy.setKeepContinueButton(props.getKeepContinueButton());
                            mediaCopy.setMediaTypeProperties(propsCopy);
                            
                            if(props.getSlideRelativePath() != null && !props.getSlideRelativePath().isEmpty()) {
                                
                                CreateSlideShow action = new CreateSlideShow();
                                action.setUsername(GatClientUtility.getUserName());
                                action.setCopyExistingSlideShow(true);
                                action.setCourseFolderPath(GatClientUtility.getBaseCourseFolderPath());
                                action.setCourseObjectName(copy.getTransitionName());
                                action.setReplaceExisting(false);
                                
                                String pptFilePath = props.getSlideRelativePath().get(0);
                                // Get the slide show folder path from "Slide Shows/TransitionName/Slide01.ppt"
                                pptFilePath = pptFilePath.substring(0, pptFilePath.lastIndexOf("/") + 1); 
                                
                                action.setPptFilePath(GatClientUtility.getBaseCourseFolderPath() + "/" + pptFilePath);
                                
                                hasAsyncOperation = true;
                                copySlideShow(action, transitionCopy, propsCopy, callback);                         
                                
                            }
                            
                        } else if (media.getMediaTypeProperties() instanceof ImageProperties) {
                            mediaCopy.setMediaTypeProperties(new ImageProperties());
                            
                        } else if (media.getMediaTypeProperties() instanceof VideoProperties) {
                            VideoProperties props = (VideoProperties) media.getMediaTypeProperties();
                            VideoProperties propsCopy = new VideoProperties();
                            
                            propsCopy.setAllowAutoPlay(props.getAllowAutoPlay());
                            propsCopy.setAllowFullScreen(props.getAllowFullScreen());
                            if(props.getSize() != null) {
                                propsCopy.setSize(new Size());
                                propsCopy.getSize().setHeight(props.getSize().getHeight());
                                propsCopy.getSize().setHeightUnits(props.getSize().getHeightUnits());
                                propsCopy.getSize().setWidth(props.getSize().getWidth());
                                propsCopy.getSize().setWidthUnits(props.getSize().getHeightUnits());
                                propsCopy.getSize().setConstrainToScreen(props.getSize().getConstrainToScreen());
                            }
                            
                            mediaCopy.setMediaTypeProperties(propsCopy);
                            
                        } else if (media.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                            YoutubeVideoProperties props = (YoutubeVideoProperties) media.getMediaTypeProperties();
                            YoutubeVideoProperties propsCopy = new YoutubeVideoProperties();
                            
                            propsCopy.setAllowAutoPlay(props.getAllowAutoPlay());
                            propsCopy.setAllowFullScreen(props.getAllowFullScreen());
                            if(props.getSize() != null) {
                                propsCopy.setSize(new Size());
                                propsCopy.getSize().setHeight(props.getSize().getHeight());
                                propsCopy.getSize().setHeightUnits(props.getSize().getHeightUnits());
                                propsCopy.getSize().setWidth(props.getSize().getWidth());
                                propsCopy.getSize().setWidthUnits(props.getSize().getWidthUnits());
                                propsCopy.getSize().setConstrainToScreen(props.getSize().getConstrainToScreen());
                            }
                            
                            mediaCopy.setMediaTypeProperties(propsCopy);
                            
                        } else if (media.getMediaTypeProperties() instanceof WebpageProperties) {
                            mediaCopy.setMediaTypeProperties(new WebpageProperties());
                            
                        } else if (media.getMediaTypeProperties() instanceof PDFProperties) {
                            mediaCopy.setMediaTypeProperties(new PDFProperties());
                            
						} else if (media.getMediaTypeProperties() instanceof LtiProperties) {
                            logger.info("Copying an LTI based course object.");
                            LtiProperties props = (LtiProperties) media.getMediaTypeProperties();
                            
                            LtiProperties propsCopy = new LtiProperties();
                            
                            propsCopy.setLtiIdentifier(props.getLtiIdentifier());
                            
                            // need to copy inner values so the copy isn't by reference.
                            CustomParameters cp = new CustomParameters();
                            if (props.getCustomParameters() != null && !props.getCustomParameters().getNvpair().isEmpty()) {
                                for (Nvpair oldPair : props.getCustomParameters().getNvpair()) {
                                    Nvpair newPair = new Nvpair();
                                    newPair.setName(oldPair.getName());
                                    newPair.setValue(oldPair.getValue());
                                    cp.getNvpair().add(newPair);
                                }
                            }
                            propsCopy.setCustomParameters(cp);
                            
                            propsCopy.setAllowScore(props.getAllowScore());
                            propsCopy.setSliderMinValue(props.getSliderMinValue());
                            propsCopy.setSliderMaxValue(props.getSliderMaxValue());
                            
                            // need to copy inner values so the copy isn't be reference.
                            LtiConcepts concepts = new LtiConcepts();
                            if (props.getLtiConcepts() != null && !props.getLtiConcepts().getConcepts().isEmpty()) {
                                for (String oldConcept : props.getLtiConcepts().getConcepts()) {
                                    concepts.getConcepts().add(oldConcept);
                                }
                            }
                            propsCopy.setLtiConcepts(concepts);
                            
                            propsCopy.setIsKnowledge(props.getIsKnowledge());
                            propsCopy.setDisplayMode(props.getDisplayMode());
                            
                            mediaCopy.setMediaTypeProperties(propsCopy);
                        }
                    }
                    
                    lmlCopy.getMedia().add(mediaCopy);
                }
                
                copy.setLessonMaterialList(lmlCopy);
            }
            
            if(transition.getLessonMaterialFiles() != null) {
                LessonMaterialFiles lmfilesCopy = new LessonMaterialFiles();
                lmfilesCopy.getFile().addAll(new ArrayList<String>(transition.getLessonMaterialFiles().getFile()));
                copy.setLessonMaterialFiles(lmfilesCopy);
            }
            
        } else if(courseTransition instanceof AAR){
            
            logger.info("Copying an AAR based course object.");
            AAR transition = (AAR) courseTransition;
            transitionCopy = new AAR();
            AAR copy = (AAR) transitionCopy;
            
            copy.setTransitionName(newName);
            copy.setFullScreen(transition.getFullScreen());
            copy.setDisabled(transition.getDisabled());
            
            if(transition.getCourseObjectsToReview() != null) {
                CourseObjectsToReview cotrCopy = new CourseObjectsToReview();
                cotrCopy.getTransitionName().addAll(new ArrayList<String>(transition.getCourseObjectsToReview().getTransitionName()));
                copy.setCourseObjectsToReview(cotrCopy);
            }
            
        } else if(courseTransition instanceof TrainingApplication){
            
            logger.info("Copying a training application based course object.");
            TrainingApplication transition = (TrainingApplication) courseTransition;
            
            transitionCopy = new TrainingApplication();
            final TrainingApplication copy = (TrainingApplication) transitionCopy;
             
            copy.setTransitionName(newName);
            copy.setFinishedWhen(transition.getFinishedWhen());
            copy.setTrainingAppTypeEnum(transition.getTrainingAppTypeEnum());
            
            if(transition.getDkfRef() != null) {
                DkfRef dkfRefCopy = new DkfRef();
                dkfRefCopy.setFile(transition.getDkfRef().getFile());
                copy.setDkfRef(dkfRefCopy);
            }
            
            if(transition.getGuidance() != null) {
                copyCourseObject(transition.getGuidance(), transition.getGuidance().getTransitionName(), new CopyCourseObjectCallback() {

                    @Override
                    public void onCopy(Serializable copiedTransition) {
                        copy.setGuidance((Guidance) copiedTransition);
                    }
                    
                });
            }
            
            Interops interops = null;
            if (transition.getInterops() != null) {
                interops = transition.getInterops();
            } 
            
            if(interops != null) {
                Interops interopsCopy = new Interops();
                
                for(Interop interop : interops.getInterop()) {
                    Interop interopCopy = new Interop();
                    
                    interopCopy.setInteropImpl(interop.getInteropImpl());
                    interopsCopy.getInterop().add(interopCopy);
                    
                    if(interop.getInteropInputs().getInteropInput() != null) {
                        
                        InteropInputs inputsCopy = new InteropInputs();
                        
                        if(interop.getInteropInputs().getInteropInput() instanceof CustomInteropInputs) {
                            CustomInteropInputs inputCopy = new CustomInteropInputs();                          
                            CustomInteropInputs.LoadArgs args = ((CustomInteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if(args != null) {
                                CustomInteropInputs.LoadArgs argsCopy = new CustomInteropInputs.LoadArgs();
                                
                                for(Nvpair pair : args.getNvpair()) {
                                    Nvpair pairCopy = new Nvpair();
                                    pairCopy.setName(pair.getName());
                                    pairCopy.setValue(pair.getValue());
                                    argsCopy.getNvpair().add(pairCopy);
                                }
                            
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            inputsCopy.setInteropInput(inputCopy);
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof DISInteropInputs) {
                        
                            DISInteropInputs sourceInput = (DISInteropInputs)interop.getInteropInputs().getInteropInput();
                            DISInteropInputs inputCopy = new DISInteropInputs();
                            inputCopy.setLoadArgs(sourceInput.getLoadArgs());
                            
                            if(sourceInput.getLogFile() != null){
                                LogFile copyLogFile = new LogFile();
                                copyLogFile.setDomainSessionLog(sourceInput.getLogFile().getDomainSessionLog()); 
                                inputCopy.setLogFile(copyLogFile);
                            }
                            inputsCopy.setInteropInput(inputCopy);
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof GenericLoadInteropInputs) {
                            GenericLoadInteropInputs inputCopy = new GenericLoadInteropInputs();                            
                            GenericLoadInteropInputs.LoadArgs args = ((GenericLoadInteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if(args != null) {
                                GenericLoadInteropInputs.LoadArgs argsCopy = new GenericLoadInteropInputs.LoadArgs();
                                argsCopy.setContentRef(args.getContentRef());
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            inputsCopy.setInteropInput(inputCopy);
                        }
                        else if(interop.getInteropInputs().getInteropInput() instanceof PowerPointInteropInputs) {
                            
                            logger.info("Copying PowerPoint interop inputs");
                            PowerPointInteropInputs inputCopy = new PowerPointInteropInputs();                          
                            PowerPointInteropInputs.LoadArgs args = ((PowerPointInteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if(args != null) {
                                PowerPointInteropInputs.LoadArgs argsCopy = new PowerPointInteropInputs.LoadArgs();
                                argsCopy.setShowFile(args.getShowFile());
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            inputsCopy.setInteropInput(inputCopy);
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof SCATTInteropInputs) {
                            SCATTInteropInputs inputCopy = new SCATTInteropInputs();
                            inputCopy.setLoadArgs(new SCATTInteropInputs.LoadArgs());
                            inputsCopy.setInteropInput(inputCopy);
                            
                        } else if( interop.getInteropInputs().getInteropInput() instanceof EmptyInteropInputs) {
                            EmptyInteropInputs inputCopy = new EmptyInteropInputs();
                            inputsCopy.setInteropInput(inputCopy);
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof SimpleExampleTAInteropInputs) {
                            SimpleExampleTAInteropInputs inputCopy = new SimpleExampleTAInteropInputs();                            
                            SimpleExampleTAInteropInputs.LoadArgs args = ((SimpleExampleTAInteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if(args != null) {
                                SimpleExampleTAInteropInputs.LoadArgs argsCopy = new SimpleExampleTAInteropInputs.LoadArgs();
                                argsCopy.setScenarioName(args.getScenarioName());
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            inputsCopy.setInteropInput(inputCopy);
                        } else if(interop.getInteropInputs().getInteropInput() instanceof TC3InteropInputs) {
                            TC3InteropInputs inputCopy = new TC3InteropInputs();                            
                            TC3InteropInputs.LoadArgs args = ((TC3InteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if(args != null) {
                                TC3InteropInputs.LoadArgs argsCopy = new TC3InteropInputs.LoadArgs();
                                argsCopy.setScenarioName(args.getScenarioName());
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            inputsCopy.setInteropInput(inputCopy);
                        } else if(interop.getInteropInputs().getInteropInput() instanceof DETestbedInteropInputs) {
                            DETestbedInteropInputs inputCopy = new DETestbedInteropInputs();                            
                            DETestbedInteropInputs.LoadArgs args = ((DETestbedInteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if(args != null) {
                                DETestbedInteropInputs.LoadArgs argsCopy = new DETestbedInteropInputs.LoadArgs();
                                argsCopy.setScenarioName(args.getScenarioName());
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            inputsCopy.setInteropInput(inputCopy);
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof VBSInteropInputs) {
                            VBSInteropInputs inputCopy = new VBSInteropInputs();                            
                            VBSInteropInputs.LoadArgs args = ((VBSInteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if(args != null) {
                                VBSInteropInputs.LoadArgs argsCopy = new VBSInteropInputs.LoadArgs();
                                argsCopy.setScenarioName(args.getScenarioName());
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            inputsCopy.setInteropInput(inputCopy);
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof VREngageInteropInputs) {
                            VREngageInteropInputs inputCopy = new VREngageInteropInputs();                            
                            VREngageInteropInputs.LoadArgs args = ((VREngageInteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if(args != null) {
                                VREngageInteropInputs.LoadArgs argsCopy = new VREngageInteropInputs.LoadArgs();
                                // this is where specific args are copied - none for VR Engage
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            inputsCopy.setInteropInput(inputCopy);
                            
                        } else if(interop.getInteropInputs().getInteropInput() instanceof UnityInteropInputs) {
                            UnityInteropInputs inputCopy = new UnityInteropInputs();                            
                            UnityInteropInputs.LoadArgs args = ((UnityInteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if(args != null) {
                                UnityInteropInputs.LoadArgs argsCopy = new UnityInteropInputs.LoadArgs();
                                // copy specific args
                                for(Nvpair nvpair : args.getNvpair()){
                                    Nvpair newNvPair = new Nvpair();
                                    newNvPair.setName(nvpair.getName());
                                    newNvPair.setValue(nvpair.getValue());
                                    argsCopy.getNvpair().add(newNvPair);
                                }
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            inputsCopy.setInteropInput(inputCopy);
                        } else if(interop.getInteropInputs().getInteropInput() instanceof HAVENInteropInputs) {
                            HAVENInteropInputs sourceInput = (HAVENInteropInputs)interop.getInteropInputs().getInteropInput();
                            HAVENInteropInputs inputCopy = new HAVENInteropInputs();
                            
                            HAVENInteropInputs.LoadArgs args = ((HAVENInteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if (args != null) {
                                HAVENInteropInputs.LoadArgs argsCopy = new HAVENInteropInputs.LoadArgs();
                                // this is where specific args are copied - none for Haven
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            if (sourceInput.getLogFile() != null) {
                                LogFile copyLogFile = new LogFile();
                                copyLogFile.setDomainSessionLog(sourceInput.getLogFile().getDomainSessionLog()); 
                                inputCopy.setLogFile(copyLogFile);
                            }
                        } else if(interop.getInteropInputs().getInteropInput() instanceof RIDEInteropInputs) {
                            RIDEInteropInputs sourceInput = (RIDEInteropInputs)interop.getInteropInputs().getInteropInput();
                            RIDEInteropInputs inputCopy = new RIDEInteropInputs();
                            
                            RIDEInteropInputs.LoadArgs args = ((RIDEInteropInputs) interop.getInteropInputs().getInteropInput()).getLoadArgs();
                            
                            if (args != null) {
                                RIDEInteropInputs.LoadArgs argsCopy = new RIDEInteropInputs.LoadArgs();
                                // this is where specific args are copied - none for RIDE
                                inputCopy.setLoadArgs(argsCopy);
                            }
                            
                            if (sourceInput.getLogFile() != null) {
                                LogFile copyLogFile = new LogFile();
                                copyLogFile.setDomainSessionLog(sourceInput.getLogFile().getDomainSessionLog()); 
                                inputCopy.setLogFile(copyLogFile);
                            }
                        }
                            
                        interopCopy.setInteropInputs(inputsCopy);
                        
                    }
                }
                
                copy.setInterops(interopsCopy);
            }
            
            if(transition.getOptions() != null) {
                TrainingApplication.Options optionsCopy = new TrainingApplication.Options();
                optionsCopy.setDisableInstInterImpl(transition.getOptions().getDisableInstInterImpl());
                optionsCopy.setFullScreen(transition.getOptions().getFullScreen());
                
                if(transition.getOptions().getShowAvatarInitially() != null) {
                    ShowAvatarInitially showAvatar = new ShowAvatarInitially();
                    
                    if(transition.getOptions().getShowAvatarInitially().getAvatarChoice() != null){
                        ShowAvatarInitially.MediaSemantics mediaSemantics = new ShowAvatarInitially.MediaSemantics(); 
                        mediaSemantics.setAvatar(transition.getOptions().getShowAvatarInitially().getAvatarChoice().getAvatar());
                        showAvatar.setAvatarChoice(mediaSemantics);
                    }
                    
                    optionsCopy.setShowAvatarInitially(showAvatar);
                }
                
                optionsCopy.setDisabled(transition.getOptions().getDisabled());
                
                copy.setOptions(optionsCopy);
            }
            
        } else if(courseTransition instanceof MerrillsBranchPoint){
            
            logger.info("Copying a merrills branch point based course object.");
            MerrillsBranchPoint transition = (MerrillsBranchPoint) courseTransition;
            transitionCopy = new MerrillsBranchPoint();
            MerrillsBranchPoint copy = (MerrillsBranchPoint) transitionCopy;
            
            copy.setTransitionName(newName);
            copy.setDisabled(transition.getDisabled());
            
            if(transition.getConcepts() != null) {
                MerrillsBranchPoint.Concepts conceptsCopy = new MerrillsBranchPoint.Concepts();
                conceptsCopy.getConcept().addAll(new ArrayList<String>(transition.getConcepts().getConcept()));
                copy.setConcepts(conceptsCopy);
            }
            
            if(transition.getQuadrants() != null) {
                MerrillsBranchPoint.Quadrants quadCopy = new MerrillsBranchPoint.Quadrants();
                
                for(Serializable content : transition.getQuadrants().getContent()) {
                    
                    if (content instanceof Recall) {
                        Recall recallCopy = new Recall();
                        recallCopy.setAllowedAttempts(((Recall) content).getAllowedAttempts());
                        
                        if(((Recall) content).getPresentSurvey() != null) {
                            Recall.PresentSurvey survey = ((Recall) content).getPresentSurvey();
                            Recall.PresentSurvey surveyCopy = new Recall.PresentSurvey();
                            surveyCopy.setFullScreen(survey.getFullScreen());
                            surveyCopy.setShowInAAR(survey.getShowInAAR());
                            recallCopy.setPresentSurvey(surveyCopy);
                            
                            if(survey.getSurveyChoice() != null) {
                                Recall.PresentSurvey.ConceptSurvey conceptSurvey = survey.getSurveyChoice(); 
                                Recall.PresentSurvey.ConceptSurvey conceptSurveyCopy = new Recall.PresentSurvey.ConceptSurvey();
                                
                                conceptSurveyCopy.setGIFTSurveyKey(conceptSurvey.getGIFTSurveyKey());
                                if(conceptSurvey.getConceptQuestions() != null) {
                                    for(ConceptQuestions questions : conceptSurvey.getConceptQuestions()) {
                                        ConceptQuestions questionsCopy = new ConceptQuestions();
                                        questionsCopy.setName(questions.getName());
                                        
                                        if(questions.getAssessmentRules() != null) {
                                            AssessmentRules rulesCopy = new AssessmentRules();
                                            
                                            if(questions.getAssessmentRules().getAboveExpectation() != null) {
                                                AboveExpectation expectationCopy = new AboveExpectation();
                                                expectationCopy.setNumberCorrect(questions.getAssessmentRules().getAboveExpectation().getNumberCorrect());
                                                rulesCopy.setAboveExpectation(expectationCopy);
                                            }
                                            
                                            if(questions.getAssessmentRules().getAtExpectation() != null) {
                                                AtExpectation expectationCopy = new AtExpectation();
                                                expectationCopy.setNumberCorrect(questions.getAssessmentRules().getAtExpectation().getNumberCorrect());
                                                rulesCopy.setAtExpectation(expectationCopy);
                                            }                                           

                                            if(questions.getAssessmentRules().getBelowExpectation() != null) {
                                                BelowExpectation expectationCopy = new BelowExpectation();
                                                expectationCopy.setNumberCorrect(questions.getAssessmentRules().getBelowExpectation().getNumberCorrect());
                                                rulesCopy.setBelowExpectation(expectationCopy);
                                            }
                                            
                                            questionsCopy.setAssessmentRules(rulesCopy);
                                        }
                                        
                                        if(questions.getQuestionTypes() != null) {
                                            ConceptQuestions.QuestionTypes questionTypesCopy = new ConceptQuestions.QuestionTypes();
                                            
                                            questionTypesCopy.setEasy(questions.getQuestionTypes().getEasy());
                                            questionTypesCopy.setHard(questions.getQuestionTypes().getHard());
                                            questionTypesCopy.setMedium(questions.getQuestionTypes().getMedium());
                                            
                                            questionsCopy.setQuestionTypes(questionTypesCopy);
                                        }
                                        
                                        conceptSurveyCopy.getConceptQuestions().add(questionsCopy);
                                    }
                                }
                                
                                surveyCopy.setSurveyChoice(conceptSurveyCopy);
                            }
                        }
                        
                        quadCopy.getContent().add(recallCopy);
                        
                    } else if (content instanceof Practice) {
                        Practice practiceCopy = new Practice();
                        practiceCopy.setAllowedAttempts(((Practice) content).getAllowedAttempts());
                        
                        if(((Practice) content).getPracticeConcepts() != null) {
                            PracticeConcepts conceptsCopy = new PracticeConcepts();                         
                            conceptsCopy.getCourseConcept().addAll(new ArrayList<String>(((Practice) content).getPracticeConcepts().getCourseConcept()));
                            practiceCopy.setPracticeConcepts(conceptsCopy);
                        }
                        
                        quadCopy.getContent().add(practiceCopy);
                        
                    } else if (content instanceof Rule) {
                        quadCopy.getContent().add(new Rule());
                        
                    } else if (content instanceof Example) {
                        quadCopy.getContent().add(new Example());
                        
                    } else if (content instanceof Transitions) {
                        final Transitions transitionsCopy = new Transitions();
                        for(Serializable transitionType : ((Transitions)content).getTransitionType()) {
                            copyCourseObject(transitionType, CourseElementUtil.getTransitionName(transitionType), new CopyCourseObjectCallback() {

                                @Override
                                public void onCopy(Serializable copiedTransition) {
                                    transitionsCopy.getTransitionType().add(copiedTransition);
                                }
                            });
                        }
                        quadCopy.getContent().add(transitionsCopy);
                    }
                }
                copy.setQuadrants(quadCopy);
            }
            
        } else {
            String courseName = GatClientUtility.getCourseName();
            String message = "Found unhandled course object when attempting to copy";
            String details = (courseTransition == null ? "A course object in the course \"" + courseName + "\" is null." : 
                    "Copying the course object type " + courseTransition.getClass() +" in the course \"" + courseName + "\" is not currently supported.");
            
            if (errorDialog == null || !errorDialog.isShowing()) {
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(message, details, null);
                dialog.setText("Copy Course Object Error");
                dialog.center();                
            }
        
            throw new IllegalArgumentException(message + " " + details);
        }
        
        if(!hasAsyncOperation) {
            callback.onCopy(transitionCopy);
        }
    }
    
    /**
     * Performs a request to the server to create the slide show file. If successful, the slide paths are copied to the Slide Show course object properties.
     * 
     * @param action The action to perform
     * @param copiedTransition The Slide Show course object that was copied
     * @param properties The copied Slide Show course object properties
     * @param callback The callback to execute when the operation is complete.
     */
    private static void copySlideShow(final CreateSlideShow action, final Serializable copiedTransition, final SlideShowProperties properties, final CopyCourseObjectCallback callback) {
        
        final FileOperationProgressModal progressModal = new FileOperationProgressModal(ProgressType.SLIDE_SHOW);
        progressModal.startPollForProgress();
        
        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<CreateSlideShowResult>() {

            @Override
            public void onFailure(Throwable caught) {
                
                progressModal.stopPollForProgress(true);
                BsLoadingDialogBox.remove();
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                        "A server error occured while creating the slide show.", 
                        "The action failed on the server: " + caught.getMessage(), null);
                dialog.setText("Error");
                dialog.center();                
            }

            @Override
            public void onSuccess(final CreateSlideShowResult result) {

                progressModal.stopPollForProgress(!result.isSuccess());
                
                if(result.isSuccess()) {
                    
                    for(String path : result.getRelativeSlidePaths()) {
                        properties.getSlideRelativePath().add(path);
                    }
                    
                    callback.onCopy(copiedTransition);
                                                                    
                } else {
                    ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to copy the Slide Show. " + result.getErrorMsg(), 
                            result.getErrorDetails(), result.getErrorStackTrace());
                    dialog.setText("Error");
                    dialog.center();
                }
            }

        });
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
     * Checks if the serializable transition object is supported by GIFT Wrap
     * 
     * @param transition object to check
     * @return true if transition is supported by GIFT Wrap; false otherwise
     */
    public static boolean isGIFTWrapSupported(Serializable transition) {
        if (!isTransitionTrainingApplication(transition)) {
            return false;
        }

        TrainingApplicationEnum type = TrainingAppUtil.getTrainingAppType((TrainingApplication) transition);
        if (TrainingApplicationEnum.MOBILE_DEVICE_EVENTS.equals(type)) {
            return true;
        } else if (TrainingApplicationEnum.UNITY_EMBEDDED.equals(type)) {
            return true;
        }

        return false;
    }
}
