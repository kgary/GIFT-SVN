/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.LoadedMetadataEvent;
import com.google.gwt.event.dom.client.LoadedMetadataHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableHTML;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AnswerSetPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.MOCAnswerSetsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.UnSupportedPropertySet;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.QuestionScorer;
import mil.arl.gift.common.util.StringUtils;



/**
 * The AbstractQuestionWidget is the base class that all survey editor question widgets should
 * extend from.  This class encapsulates common logic for all questions such as the question 
 * specific property set data.
 *  
 * @author nblomberg
 *
 */
public abstract class AbstractQuestionWidget extends Composite implements PropertySetListener {

    private static Logger logger = Logger.getLogger(AbstractQuestionWidget.class.getName());
   
    /** The possible place holders for the questions. */
    private final static String WRITING_MODE_PLACEHOLDER = "Click here to enter your question's text";
    private final static String SCORING_MODE_PLACEHOLDER = "Switch to Writing Mode to enter question's text";
    protected final static String READ_ONLY_MODE_PLACEHOLDER = "Cannot edit question's text in read-only mode";
    
    /** The tool tips for the questions. */
    private final static String WRITING_MODE_TOOLTIP = "Click to edit";
    private final static String SCORING_MODE_TOOLTIP = "Switch to Writing Mode to edit question's text";
    protected final static String READ_ONLY_MODE_TOOLTIP = "Cannot edit question's text in read-only mode";
    
    /** Default text to use for survey questions that are emtpy so they can pass validation */
    private static final String DEFAULT_QUESTION_TEXT = "Enter your text here";
    
    /** The list of property sets for the question. */
    protected ArrayList<AbstractPropertySet> propertySets = new ArrayList<AbstractPropertySet>();
    
    /** Indicates whether the survey is editable or if it's read-only */
    protected boolean isReadOnly = false;
    
    /** Indicates whether the Question Widget is currently selected */
    protected boolean isSelected = false;
    
    /** The mode that the widget is set to be authored in. */
    private SurveyEditMode editMode = SurveyEditMode.WritingMode;
    
    /** A container panel that wraps each question widget, allowing common elements to be shared among each extension of this class */
    private FlowPanel container = new FlowPanel();
    
    private Icon helpTextIcon = new Icon(IconType.QUESTION_CIRCLE);
    
    private ManagedTooltip helpTextTooltip = new ManagedTooltip(helpTextIcon);
    
    /** Determines if the widget contains scoring logic. */
    protected boolean isScoredType = false;
    
    /** The panel containing the media associated with this question, if one is defined */
    private SimplePanel mediaPanel = new SimplePanel();
    
    /** The {@link EditableHTML} widget containing this question's text */
    protected EditableHTML questionHtml = new EditableHTML();

    /** The database id corresponding to the AbstractQuestion getQuestionId().  A 0 means that the question is a new question for the database. */
    private int abstractQuestionDbId = 0;

    /** The database id corresponding to the AbstractSurveyuestion getId().  A 0 means the survey question is a new question for the database. */
    private int abstractSurveyQuestionDbId = 0;
    
    /**
     * The set of categories that a question belongs to.  The backend logic only allows categories for SurveyElement QUESTION types,
     * not TEXT types. The categories are optional and a question can be authored without having to add a cateogry.
     * The SurveyEditor currently does NOT allow the user to view or edit the question categories, so this is here
     * only for legacy SAS questions.  In the future, question categories may be allowed to be authored in the Survey Editor, but
     * for now, this is here to simply prevent old SAS questions from losing information when being saved in the new Survey Editor tool.
     */
    private HashSet<String> categorySet = new HashSet<String>();
    
    /**
	 * whether the survey question associated with these common survey question properties
     * can be scored because the survey is a scored survey type and the question supports scoring answers.
	 */
	protected boolean isScoredSurvey = false;
    
	/**
	 * Create widget
	 * @param mode whether the widget is being displayed in writing mode or scoring mode.  Can't be null.  Used
	 * to alter the widget components.
	 * @param isScored whether the survey question associated with these common survey question properties
     * can be scored because the survey is a scored survey type and the question supports scoring answers.
	 */
	public AbstractQuestionWidget(SurveyEditMode mode, boolean isScored) {
	    
	    super();
	    
	    editMode = mode;
	    isScoredSurvey = isScored;

	    // Ordering the custom question properties first.
	    addCustomPropertySets();
	    
	    
	    // common properties that all questions have - These properties appear last in the property set list.
	    CommonPropertySet commonProps = new CommonPropertySet(isScored);
	    addPropertySet(commonProps);
	    
	    QuestionImagePropertySet questionPropertySet = new QuestionImagePropertySet();
	    addPropertySet(questionPropertySet);   
	    
	    UnSupportedPropertySet unSupportedPropertySet = new UnSupportedPropertySet();
	    addPropertySet(unSupportedPropertySet);
	    
	    helpTextIcon.getElement().getStyle().setPosition(Position.ABSOLUTE);
	    helpTextIcon.getElement().getStyle().setTop(0, Unit.PX);
	    helpTextIcon.getElement().getStyle().setRight(0, Unit.PX);
	    helpTextIcon.getElement().getStyle().setFontSize(1.5, Unit.EM);
	    helpTextIcon.getElement().getStyle().setColor("purple");
	    
	    setIsScoredType();
	}
	
	/**
	 * Sets if the question is a scored type of question, which indicates
	 * it contains scoring logic that the user can edit.
	 */
	protected abstract void setIsScoredType();
	
	@Override
	protected void initWidget(Widget widget){
		
		//need to wrap up each question widget in a container so that help icons and images can be added
		container.clear();
		container.add(questionHtml); 	//add the question text
		container.add(widget);			//add the appropriate widget for authoring the question's type
		
		//adding relative positioning to the root element allows us to absolutely position the help text inside it
    	container.getElement().getStyle().setPosition(Position.RELATIVE);       
    	
    	mediaPanel.getElement().getStyle().setProperty("margin", "10px auto");
        mediaPanel.getElement().getStyle().setProperty("display", "block");
        mediaPanel.getElement().getStyle().setProperty("maxWidth", "100%");
        
        questionHtml.getElement().getStyle().setMarginTop(10, Unit.PX);
        questionHtml.getElement().getStyle().setMarginRight(20, Unit.PX);
        questionHtml.getElement().getStyle().setProperty("textShadow", "0px 0px");
        
        if(isReadOnly) {
            logger.warning("questionHtml set in initWidget AbstractQuestionWidget: " + questionHtml.getValue());
            questionHtml.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
            questionHtml.setTooltip(READ_ONLY_MODE_TOOLTIP);
            questionHtml.setEditable(false);
            
        }else if(editMode != null && editMode.equals(SurveyEditMode.ScoringMode)) {
        	questionHtml.setPlaceholder(SCORING_MODE_PLACEHOLDER);     
        	questionHtml.setEditable(false);
        	
        }else{
        	questionHtml.setPlaceholder(WRITING_MODE_PLACEHOLDER);
        	questionHtml.setEditable(true);
        }
        
        questionHtml.refreshWidget();

        container.getElement().getStyle().setMarginBottom(40, Unit.PX);
        
        super.initWidget(container);
	}
	
	/**
	 * Adds a property set to the question.
	 * 
	 * @param props - The property set to be added. (should not be null).
	 */
	protected void addPropertySet(AbstractPropertySet props) {
	    propertySets.add(props);
	}
	
	/**
	 * Sets the categories that belong to the question.  This is optional.
	 * 
	 * @param categories - The set of categories that the question belongs to.  Can be null or empty.
	 */
	private void setCategories(Set<String> categories) {
	    categorySet.clear();
	    
	    if (categories != null) {
	        categorySet.addAll(categories);
	    }
	    
	}
	
	/**
	 * Removes a property set based on the property set class name.  Any property sets
	 * matching the class type will be removed from the set list.
	 * 
	 * @param className - The name of the property set class to be removed.  Should not be null.
  	 */
	protected void removePropertySetByType(String className) {
	   for (AbstractPropertySet prop : propertySets) {
	       if (prop.getClass().getName().equals(className)) {
	           propertySets.remove(prop);
	       }
	   }
	}

	/**
	 * Gets the list of property sets for the widget.
	 * 
	 * @return ArrayList<AbstractPropertySet> - The list of property sets for the widget.
	 */
	public ArrayList<AbstractPropertySet> getPropertySets() {
	    return propertySets;
	}
	
	/**
	 * Returns the property set for a given class/type.
	 * 
	 * @param className - The name of the property set class to get.
	 * @return AbstractPropertySet - The property set if found.  Null is returned if the property set cannot be found.
	 */
	public AbstractPropertySet getPropertySetByType(String className) {
	    AbstractPropertySet propSet = null;
	    for (AbstractPropertySet prop : propertySets) {
           if (prop.getClass().getName().equals(className)) {
               propSet = prop;
           }
        }
	    
	    return propSet;
	}
	
	/**
	 * Accessor to get an Integer property value from a specific property set.
	 * 
	 * @param propSetClassName - The class name of the property set containing the property value.
	 * @param propertyKey - The key of the property.
	 * @return Integer - The integer value of the property if found.  If the property cannot be found, null is returned.
	 */
	public Integer getIntegerValueFromPropertySet(String propSetClassName, SurveyPropertyKeyEnum propertyKey) {
	    Integer propValue = null;
	    
	    AbstractPropertySet propSet = getPropertySetByType(propSetClassName);
	    if (propSet != null) {

	        propValue = propSet.getIntegerPropertyValue(propertyKey);
	    }
	    
	    return propValue;
	}
	
	/**
     * Accessor to get a Boolean property value from a specific property set.
     * 
     * @param propSetClassName - The class name of the property set containing the property value.
     * @param propertyKey - The key of the property.
     * @return Boolean - The Boolean value of the property if found.  If the property cannot be found, null is returned.
     */
	public Boolean getBooleanValueFromPropertySet(String propSetClassName, SurveyPropertyKeyEnum propertyKey) {
        Boolean propValue = null;
        
        AbstractPropertySet propSet = getPropertySetByType(propSetClassName);
        if (propSet != null) {

            propValue = propSet.getBooleanPropertyValue(propertyKey);
        }
        
        return propValue;
    }
	
	/**
     * Accessor to get the Serializable property value from a specific property set.
     * 
     * @param propSetClassName - The class name of the property set containing the property value.
     * @param propertyKey - The key of the property.
     * @return Serializable - The Serializable value of the property if found.  If the property cannot be found, null is returned.
     */
	public Serializable getValueFromPropertySet(String propSetClassName, SurveyPropertyKeyEnum propertyKey) {
        Serializable propValue = null;
        
        AbstractPropertySet propSet = getPropertySetByType(propSetClassName);
        if (propSet != null) {

            propValue = propSet.getPropertyValue(propertyKey);
        }
        
        return propValue;
    }
	
	
	/**
	 * Gets the possible total points for the widget.
	 * 
	 * @return
	 */
	abstract public Double getPossibleTotalPoints();

    /**
     * Gets the edit mode for the widget (writing or scoring mode).
     * 
     * @return SurveyEditMode - The edit mode for the widget (writing or scoring mode).
     */
    public SurveyEditMode getEditMode() {
        return editMode;
    }

    /**
     *  Sets the edit mode for the widget (writing or scoring mode)
     * 
     * @param mode - The edit mode for the widget (writing or scoring mode).  Can't be null.
     */
    protected void setEditMode(SurveyEditMode mode) {
        
        if(mode == null){
            logger.severe("Not changing survey edit mode to null");
            return;
        }
        
        this.editMode = mode;
    }
    
    /**
     * Indicates if the survey is editable or read-only
     * 
     * @return isReadOnly - true if the survey is read-only, false if the survey is editable.
     */
    public boolean isReadOnly() {
        return isReadOnly;
    }
    
    /**
     * Allows for question specific property sets to be added to the widget.
     */
    abstract protected void addCustomPropertySets();
    
    /**
     * Initialize widget function is called after the widget has been constructed (post constructor) but
     * before the widget used.  It is intended so that child/sub widgets can be added to the widget as needed
     * or for basic initialization that may need to be done prior to using the widget.
     * Initialization happens only one time after the widget is constructed.
     */
    public abstract void initializeWidget();
    
    /**
     * Refreshes the widget based on the current mode (writing or scoring).
     * 
     * Allows for widgets to render differently based on the mode of the survey editor panel.
     */
    abstract public void refresh();
    
    /**
     * Gets the string list of concepts covered by the question
     * 
     * @return the concepts in a string list if correct question type, 
     * null otherwise
     */
    public List<String> getConcepts() {
		return null;
	}
    
    /**
     * Sets the difficulty level of the question 
     * 
     * @param difficulty the difficulty, either Easy, Medium, or Hard
     * Does nothing if question can't have a difficulty (i.e. Free Response)
     */
    public void setDifficulty(QuestionDifficultyEnum difficulty) {
	}
	
    /**
     * Gets the question difficulty
     * 
     * @return the question difficulty, null if question can't have a difficulty (i.e. Free Response)
     */
	public String getDifficulty() {
		return null;
	}
	
	/**
	 * Sets the string list of concepts for the question
	 * 
	 * @param concepts the concepts to set for the question,
	 * Does nothing if question can't have concepts (i.e. Free Response)
	 */
	public void setConcepts(ArrayList<String> concepts) {
	}
	
	/**
     * Returns the current list of scoring attributes as a string list so the 
     * question container's multiselect box can be populated on load
     * 
     * @return the string list of the attributes, null if question can't have scoring attributes (i.e. Free Response)
     */
    public List<String> getScoringAttributesAsStringList() {
		return null;
	}
	
	/**
     * Sets the attributes for this question to be scored on. This list comes from
     * the question container widget and is set onValueChange
     * 
     * @param attributes the collection of Attributes, Does nothing if question can't have a scorer (i.e. Free Response)
     */
    abstract public void setScorerProperty(Set<AttributeScorerProperties> attributes);
    

    /**
     * Called when the survey editor mode has been changed (writing or scoring).
     * 
     * Allows the widget to set it's own internal mode state along with changing 
     * any controls that may need to be updated based on the new mode.
     * 
     * @param mode - The mode that the editor has been switched to (writing or scoring mode).
     */
    public void onEditorModeChanged(SurveyEditMode mode) {
        if (isReadOnly){
            questionHtml.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
            questionHtml.setTooltip(READ_ONLY_MODE_TOOLTIP);
            questionHtml.setEditable(false);
            setPlaceholderResponseVisible(false);
            return;
        }else if(mode == SurveyEditMode.ScoringMode) {
            questionHtml.setPlaceholder(SCORING_MODE_PLACEHOLDER);
            questionHtml.setTooltip(SCORING_MODE_TOOLTIP);
        } else {
            questionHtml.setPlaceholder(WRITING_MODE_PLACEHOLDER);
            questionHtml.setTooltip(WRITING_MODE_TOOLTIP);
        }
        questionHtml.setEditable(true);
    }
    
    @Override
    public void onPropertySetChange(AbstractPropertySet propSet) {
        
        if(propSet instanceof CommonPropertySet){
        	
        	CommonPropertySet set = (CommonPropertySet) propSet;
       		
        	Serializable helpText = set.getPropertyValue(SurveyPropertyKeyEnum.HELP_STRING);
        	
        	if(helpText != null && helpText instanceof String && !((String)helpText).isEmpty()){
        		
        		//add the help icon and set its text
        		container.add(helpTextTooltip.asWidget());      		
        		
        		helpTextTooltip.setTitle((String) helpText);
        		
        	} else {
        		
        		//remove the help icon
        		container.remove(helpTextIcon);
        	}
        
        } else if(propSet instanceof QuestionImagePropertySet){
        	
        	QuestionImagePropertySet set = (QuestionImagePropertySet) propSet;
        	     		
        	Boolean displayMedia = null;
        	
        	if(set.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE) != null){
        		displayMedia = set.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE);
        	}
        	
        	final String mediaLocation;
        	
        	if(set.getProperties().hasProperty(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY) && set.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY) instanceof String){
        		mediaLocation = (String) set.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
        		
        	} else {
        	    mediaLocation = null;
        	}
        	
        	Integer mediaPosition = null;
        	
        	if(set.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY) != null){
        		mediaPosition = set.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY);
        	}
        	
        	final Integer mediaWidth;
            
            if(set.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY) != null){
                mediaWidth = set.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_WIDTH_KEY);
            } else {
                mediaWidth = 0;
            }
        	
            boolean isLegacyImage = !set.getProperties().hasProperty(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY);
        	
        	if(logger.isLoggable(Level.INFO)) {
                logger.info("Generating question media panel");
            }
        	
        	if(displayMedia == null || !displayMedia || mediaLocation == null || mediaLocation.isEmpty()) {
        	    
        	    /* No media to show, so hide the media panel */
        	    container.remove(mediaPanel);
        	    return;
        	}
        	
        	final Widget media;
            
            if(Constants.isVideoFile(mediaLocation)) {
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("Generating question video");
                }
                
                /* Display a video */
                final Video video = Video.createIfSupported();
                video.setPreload(MediaElement.PRELOAD_AUTO);
                video.setControls(true);
                
                media = video;

                video.addLoadedMetadataHandler(new LoadedMetadataHandler() {
                    
                    @Override
                    public void onLoadedMetadata(LoadedMetadataEvent event) {
                        resizeMedia(mediaWidth);
                    }
                });
                
                video.setSrc(SurveyEditorResources.getInstance().getHostFolderUrl() + "/" + mediaLocation);
                
            } else if(Constants.isAudioFile(mediaLocation)) {
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("Generating question audio");
                }
                
                /* Display an audio file */
                final Audio audio = Audio.createIfSupported();
                audio.setPreload(MediaElement.PRELOAD_AUTO);
                audio.setControls(true);
                
                media = audio;
                
                audio.addLoadedMetadataHandler(new LoadedMetadataHandler() {
                    
                    @Override
                    public void onLoadedMetadata(LoadedMetadataEvent event) {
                        resizeMedia(mediaWidth);
                    }
                });
                
                audio.setSrc(SurveyEditorResources.getInstance().getHostFolderUrl() + "/" + mediaLocation);
                
            } else if(isLegacyImage || Constants.isImageFile(mediaLocation)){
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("Generating question image");
                }
                
                String prefix;
                if(isLegacyImage) {
                    
                    if(logger.isLoggable(Level.INFO)) {
                        logger.info("Legacy question image detected. Modifying media URL appropriately");
                    }
                    
                    /* Legacy image reference. Assume URL is relative to data folder. */
                    prefix = "";
                    
                } else {
                    prefix = SurveyEditorResources.getInstance().getHostFolderUrl() + "/";
                }
                
                /* Display either a legacy image or an image file */
                final Image image = new Image(prefix + mediaLocation);
                
                media = image;

                image.addLoadHandler(new LoadHandler() {
                    @Override
                    public void onLoad(LoadEvent event) {
                        resizeMedia(mediaWidth);
                    }
                });
                
            } else {
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("Generating question unknown media");
                }
                
                /* Display a button that can be used to view a media file with an unknown type */
                final Button button = new Button("Click to View");
                button.setType(ButtonType.PRIMARY);
                button.setIcon(IconType.FILE);
                button.setIconSize(IconSize.TIMES2);
                button.getElement().getStyle().setProperty("padding", "10px 80px");
                button.getElement().getStyle().setProperty("display", "flex");
                button.getElement().getStyle().setProperty("flexDirection", "column");
                button.getElement().getStyle().setProperty("alignItems", "center");
                button.getElement().getStyle().setProperty("fontSize", "20px");
                
                final Tooltip tooltip = new Tooltip("Click to show this media in a new window");
                tooltip.setWidget(button);
                
                media = button;
                
                button.addMouseDownHandler(new MouseDownHandler() {
                    
                    @Override
                    public void onMouseDown(MouseDownEvent event) {
                        event.stopPropagation();
                    }
                });
                
                button.addClickHandler(new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        event.stopPropagation();
                        tooltip.hide();
                        String options = "menubar=no,location=no,resizable=yes,scrollbars=yes,status=no";
                        Window.open(
                                SurveyEditorResources.getInstance().getHostFolderUrl() + "/" + mediaLocation, 
                                "_blank", 
                                options);
                    }
                });
            }
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Question media generated.");
            }
            
            media.getElement().getStyle().setProperty("margin", "10px auto");
            media.getElement().getStyle().setProperty("display", "flex");
            media.getElement().getStyle().setProperty("maxWidth", "100%");

            mediaPanel.setWidget(media);
            
            mediaPanel.getElement().getStyle().setOverflowX(Overflow.AUTO);
            mediaPanel.getElement().getStyle().setOverflowY(Overflow.VISIBLE);
    	
    		if(mediaPosition == null){
    			mediaPosition = 0;
    		}
    		
    		if(mediaPosition == 0){
    			
    			//add the image below the question text
    			container.insert(mediaPanel, container.getWidgetIndex(questionHtml) + 1);  	  
        		
    		} else {
    			
    			//add the image above the question text
    			container.insert(mediaPanel, container.getWidgetIndex(questionHtml));  
    		}
    		
    		if(logger.isLoggable(Level.INFO)) {
                logger.info("Finished generating media panel");
            }
    		
    	}
    }
    
    /**
     * Resizes the media associated with this question to reflect the provided size
     * 
     * @param size the size that the rendered media should be shown with (as a percentage
     * of its original size)
     */
    private void resizeMedia(Integer size) {
        
        Widget media = mediaPanel.getWidget();
        if(media == null) {
            return;
        }
        
        float widthPercentage = (size / 100f);

        if (widthPercentage == 0) {

            widthPercentage = 1f;
        }
        
        int width;
        
        /* Attempt to get the natural width of the media being displayed. The property
         * that needs to be looked at to get this will vary slightly depending on
         * what type of HTML element is used */
        String widthProp = media instanceof Video ? "videoWidth" : "naturalWidth";
        String widthAttr = media.getElement().getPropertyString(widthProp);
        try {
            width = Integer.valueOf(widthAttr);
            
        } catch(@SuppressWarnings("unused") NumberFormatException e) {
            
            /* The element is likely still loading, so the natural width is not yet known.
             * If this happens, fall back on the offset width (i.e. the rendered width) */
            width = media.getOffsetWidth();
        }
        
        media.getElement().setAttribute("width", width * widthPercentage + "px");
        media.getElement().setAttribute("height", (widthPercentage * 100) + "%");
    }

    /**
     * Returns if the widget is a scored type of widget, which means the widget
     * contains scoring logic that the user can edit.
     * 
     * @return True if the widget is a scored type, false otherwise.
     */
    public boolean isScoredType() {  
        return isScoredType;
    }

    /**
     * Load  handler for loading an AbstractQuestionWidget with question data from the survey database.
     * 
     * @param element - The survey database data to be loaded into the widget.
     */
    public abstract void load(AbstractSurveyElement element) throws LoadSurveyException;
    
    
    /**
     * This should be called after the property sets have been loaded for a widget.  This method
     * notifies the widget that the property sets have been updated/changed.
     * 
     * @throws LoadSurveyException
     */
    protected void onLoadNotifyPropertySetChanges() throws LoadSurveyException {
        for (AbstractPropertySet propSet : this.getPropertySets()) {
            onPropertySetChange(propSet);
        }
    }

    /**
     * Save handler for saving the question to the database.
     * 
     * @param absElement - The abstract survey question that will be filled in with the data.
     */
    public void save(AbstractSurveyElement absElement) throws SaveSurveyException {
        
        if (absElement != null) {
            
            if (absElement instanceof AbstractSurveyQuestion) {
                @SuppressWarnings("unchecked")
                AbstractSurveyQuestion<? extends AbstractQuestion> absSurveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion> )absElement;
                AbstractQuestion absQuestion = absSurveyQuestion.getQuestion();

                if (absQuestion != null) {
                    // Set the database id (if it exists)
                    // This must be done so it can be determined if the data is new data (id of 0) or an
                    // update to an existing id.
                    absSurveyQuestion.setId(getAbstractSurveyQuestionDbId());
                    absQuestion.setQuestionId(getAbstractQuestionDbId());
                    absQuestion.setText(getSafeQuestionTextString(true));
                    
                    // Save any categories that this question belonged to.
                    saveQuestionCategories(absQuestion);
                    

                    SurveyItemProperties questionProps = absQuestion.getProperties();
                    for (AbstractPropertySet propSet : this.getPropertySets()) {
                        
                        // Save all the properties as question properties (not surveyquestion properties).
                        propSet.save(questionProps);
                    }
                } else {
                    throw new SaveSurveyException("Encountered a null AbstractQuestion when saving the survey for element of type: " + 
                                                  absElement.getClass().getName());
                    
                }
            } else if (absElement instanceof TextSurveyElement) {
                TextSurveyElement textElement = (TextSurveyElement) absElement;
                textElement.setId(getAbstractSurveyQuestionDbId());
                textElement.setText(getSafeQuestionTextString(false));
                SurveyItemProperties questionProps = textElement.getProperties();
                for (AbstractPropertySet propSet : this.getPropertySets()) {
                    
                    // Save all the properties as question properties (not surveyquestion properties).
                    propSet.save(questionProps);
                }
            } else {
                throw new SaveSurveyException("Unsupported element encountered during save:  " + absElement.getClass().getName());
            }
            
        } else {
            throw new SaveSurveyException("Encountered a null AbstractSurveyElement when saving the survey.");
        }
    }
    
    /**
     * Loads any question categories (optional) into the question.  Currently the survey editor does not allow the user
     * to view or edit the categories, but this will prevent older questions from the SAS from losing the category information
     * and will keep the backend functionality working for a future time when this may be added into the survey editor.
     * 
     * @param categories - Set of categories that the question may belong to.  Can be null or empty if the question has no category.
     * 
     * @throws LoadSurveyException
     */
    public void loadQuestionCategories(Set<String> categories) throws LoadSurveyException {
        setCategories(categories);
    }
    
    /**
     * Saves the question categories to the AbstractQuestion object.  The categories are optional. 
     * 
     * @param absQuestion - The AbstractQuestion to save the categories to.
     * @throws SaveSurveyException
     */
    private void saveQuestionCategories(AbstractQuestion absQuestion) throws SaveSurveyException {
        
        if (absQuestion == null) {
            throw new SaveSurveyException("Unexpected null AbstractQuestion encountered when attempting to save the question categories.");
        }

        for (String category : categorySet) {
            absQuestion.addCategory(category);
        }
    }

    /**
     * Sets the AbstractQuestion database id.  
     * @param id - The db id.  0 is reserved for a new item in the database.
     */
    public void setAbstractQuestionDbId(int id) {
        this.abstractQuestionDbId = id;
        
    }
    
    /**
     * Sets the visibility of the placeholder question (last question in responseContainer)
     * 
     * @param visible
     *             Whether or not the placeholder question should be visible
     */
    public abstract void setPlaceholderResponseVisible(boolean visible);
    
    /**
     * Updates the question reply ids
     * 
     * @param newProperties The properties containing updated ids from the database
     */
    public void setQuestionReplyDbIds(SurveyItemProperties newProperties) {
    	
    	try {
    		if(newProperties.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY) != null) {
	    		// Update list option ids for a multiple choice question
	    		
		    	AnswerSetPropertySet propSet = (AnswerSetPropertySet) getPropertySetByType(AnswerSetPropertySet.class.getName());
		    	
		    	OptionList newOptionList = (OptionList) newProperties.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
		    	OptionList optionList;
		    	
		    	if(propSet.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY) != null) {
		    		optionList = (OptionList) propSet.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
		    	} else {
		    	    optionList = new OptionList();
		    	}
		    	
		    	copyListOptionIds(optionList, newOptionList);
		    	propSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, optionList);
	    	}
	    	
	    	if(newProperties.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY) != null) {	    		
	    		// Update list option ids for a matrix of choices question
	    		MOCAnswerSetsPropertySet propSet = (MOCAnswerSetsPropertySet) getPropertySetByType(MOCAnswerSetsPropertySet.class.getName());
	    		
	    		// Update column list options
	    		OptionList newOptionList = (OptionList) newProperties.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
	    		OptionList optionList;
	    		
	    		if(propSet.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY) != null) {
	    			optionList = (OptionList) propSet.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
		    	} else {
		    	    optionList = new OptionList();
		    	}
		    	
	    		copyListOptionIds(optionList, newOptionList);
	    		propSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY, optionList);
	    		
	    		// Update row list options
	    		newOptionList = (OptionList) newProperties.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
	    		
	    		if(propSet.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY) != null) {
	    			optionList = (OptionList) propSet.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
		    	} else {
		    	    optionList = new OptionList();
		    	}
		    	
	    		copyListOptionIds(optionList, newOptionList);
	    		propSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY, optionList);
	    	}
	    	
    	} catch (Exception e) {
    		logger.warning("Caught exception while attempting to update list option ids: " + e);
    	}
    }
    
    /**
     * Copies the ids from a new option list to an existing option list. Any new list options will be added to the
     * existing option list. This is needed so that new list option entries are not repeatedly inserted into the database
     * when the survey is saved.
     * 
     * @param existingOptionList The current option list contained in the question's property set.
     * @param newOptionList The option list that contains updated ids.
     */
    private void copyListOptionIds(OptionList existingOptionList, OptionList newOptionList) {
    	
    	existingOptionList.setId(newOptionList.getId());
	
    	if(newOptionList.getListOptions() != null) {
	    	for(int i = 0; i < newOptionList.getListOptions().size(); i++) {
	    		
	    		ListOption newOption = newOptionList.getListOptions().get(i);
	    		
	    		if(i > existingOptionList.getListOptions().size()) {
	    		    ListOption listOption = new ListOption(newOption.getId(), newOption.getText(), newOptionList.getId());
	    		    listOption.setSortKey(existingOptionList.getListOptions().size());
	    			existingOptionList.getListOptions().add(listOption); 
	    		} else {
	    			existingOptionList.getListOptions().get(i).setId(newOption.getId());
	    			existingOptionList.getListOptions().get(i).setOptionListId(newOptionList.getId());
	    		}
	    	}
    	}
    }
    
    /**
     * Gets the AbstractQuestion database id.
     * 
     * @return int - The db id.  0 is reserved for a new item in the database.
     */
    public int getAbstractQuestionDbId() {
        return this.abstractQuestionDbId;
    }

    /**
     * Sets the AbstractSurveyQuestion database id.
     * 
     * @param id - The db id.  0 is reserved for a new item in the database.
     */
    public void setAbstractSurveyQuestionDbId(int id) {
        this.abstractSurveyQuestionDbId = id;
        
    }
    
    /**
     * Gets the AbstractSurveyQuestion database id.
     * 
     * @return int - The db id.  0 is reserved for a new item in the database.
     */
    public int getAbstractSurveyQuestionDbId() {
        return this.abstractSurveyQuestionDbId;
    }
    
    /**
     * Gets the raw string used to define the question text
     * 
     * @return the raw question text
     */
    public String getRawQuestionTextString(){
    	return questionHtml.getValue();
    }
    
    
    /**
     * Gets the question text string.  If the value is null, an empty string is returned.
     * 
     * @param provideDefaultIfEmpty - Whether default question text should be provided if the 
     * question text is empty. Can be useful
     * @return String - The question text.  If the text is null, any empty string is returned.
     */
    protected String getSafeQuestionTextString(boolean provideDefaultIfEmpty) {
        // Default to an empty string.
        String questionText = "";
        if (questionHtml.getValue() != null) {
            questionText = questionHtml.getValue();
        }
        
        if(provideDefaultIfEmpty && StringUtils.isBlank(questionText)) {
            questionText = DEFAULT_QUESTION_TEXT;
        }
     
        return questionText;
    }
    
    /**
     * Sets the raw string used to define the question text
     * 
     * @param questionText the raw question text
     */
    public void setRawQuestionTextString(String questionText){
        logger.warning("Changing raw string value...");
    	questionHtml.setValue(questionText);
    }
    
    /**
     * Updates the QuestionScorer totalQuestion flag.  This will be set to true
     * for any question that needs scoring.  It should be set to false for any question
     * that does not have a valid scores set yet in the editor so that it will not get scored at runtime.
     * 
     * @param props - The SurveyItemProperties containing the SCORER property.
     */
    protected void updateTotalQuestionFlag(SurveyItemProperties props) {
        
        if (props != null) {
            
            if (props.hasProperty(SurveyPropertyKeyEnum.SCORERS) && props.getPropertyValue(SurveyPropertyKeyEnum.SCORERS) != null) {
                QuestionScorer questionScorer = (QuestionScorer)props.getPropertyValue(SurveyPropertyKeyEnum.SCORERS);

                if (getPossibleTotalPoints() > 0 && isScoredSurvey) {
                    questionScorer.setTotalQuestion(true);
                    logger.info("updateTotalQuestionFlag() - The question has weights, setting the total question flag to true.");
                } else {
                    questionScorer.setTotalQuestion(false);
                    
                    if (!isScoredSurvey) {
                        logger.info("updateTotalQuestionFlag() - The survey type is not scored, setting the total question flag to false.");
                    } else {
                        logger.info("updateTotalQuestionFlag() - The question has weights of 0 or less, setting the total question flag to false.");
                    }
                }

                // Update the property value of the question scorer.
                props.setPropertyValue(SurveyPropertyKeyEnum.SCORERS, questionScorer);
                
            } else {
                logger.severe("updateTotalQuestionFlag() - Cannot update total question property because the SCORERS property was not found.");
            }
        } else {
            logger.severe("updateTotalQuestionFlag() - A null survey item properties object was encountered.");
        }
    }
    
    /**
     * Checks to see if a property exists in any property set (except the UnSupportedPropertySet)
     * If the property is found, then true is returned.  If the property is not found, false is returned.
     * 
     * @param key - The key to check for.
     * @return True if the property exists in any property set, false if it doesn't exist.
     */
    private boolean doesPropertyExistInAnyPropertySet(SurveyPropertyKeyEnum key) {
        boolean exists = false;
        
        for (AbstractPropertySet prop : propertySets) {
            if ((!(prop instanceof UnSupportedPropertySet)) && prop.getProperties().hasProperty(key)) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }
    
    /**
     * Adds any properties that are not part of a PropertySet to the UnsupportedPropertySet so that
     * the properties can be written out (even if they are not exposed in the Survey Editor.
     * 
     * @param properties The properties to check for.
     */
    private void addUnsupportedProperties(SurveyItemProperties properties) {
        
        AbstractPropertySet propSet = getPropertySetByType(UnSupportedPropertySet.class.getName());
        UnSupportedPropertySet unSupportedSet = (UnSupportedPropertySet) propSet;
        for (SurveyPropertyKeyEnum key : properties.getKeys()) {
            if (!doesPropertyExistInAnyPropertySet(key)) {
                Serializable value = properties.getPropertyValue(key);
                logger.severe("Found a property with key: " + key + ".  The survey editor does not expose this property yet, but it will be saved with the survey.");
                unSupportedSet.getProperties().setPropertyValue(key,  value);
                
            }
        }
    }
    
    /**
     * This method goes through the Question properties and the SurveyQuestion properties for an AbstractSurveyElement item.
     * It will check to see if any properties have not yet been added to any property set for the Survey Editor Question.
     * If the property has not yet been added, it is added to the Unsupported Property Set.
     * 
     * This should be called after all property sets have been loaded into the AbstractSurveyElement item.
     * 
     * @param questionProps - The properties for the Question element.
     * @param surveyQuestionProps - The prooperties for the Survey Question element.
     */
    protected void addUnsupportedProperties(SurveyItemProperties questionProps, SurveyItemProperties surveyQuestionProps) {
        // Order matters here.  The properties of the survey question override the question properties, so the 
        // survey question properties are added first to the unsupported property set (if any are found).
        addUnsupportedProperties(surveyQuestionProps);
        addUnsupportedProperties(questionProps);
    }
    
    /**
	 * Sets the focus on this widget and begins editing it's contents
	 */
    public void startEditing(){
    	
    	if(questionHtml.isAttached()){
    		questionHtml.startEditing();
    	}
    }
    
    /**
	 * Sets whether or not this widget should be read-only
	 * 
	 * @param readOnly whether or not this widget should be read-only
	 */
	public void setReadOnlyMode(boolean readOnly) {
	    this.isReadOnly = readOnly;

	    questionHtml.setEditable(!readOnly);
	    if (readOnly) {
	        questionHtml.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
	        questionHtml.setTooltip(READ_ONLY_MODE_TOOLTIP);
	    }
	}
}
