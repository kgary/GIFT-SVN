/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableInlineLabel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SaveSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AddSurveyDialog.SurveyDialogOption;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyChangeEditMode;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyCloseEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyFilterEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyPreviewEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveySaveEvent;
import mil.arl.gift.common.survey.SurveyProperties;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.IconStack;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.Placement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;



/**
 * The header widget for the survey editor panel.  The widget contains various functionality for accessing
 * survey name, properties, survey preview, saving, & closing of the survey.
 * 
 * @author nblomberg
 *
 */
public class SurveyEditorHeaderWidget extends Composite  {

    private static Logger logger = Logger.getLogger(SurveyEditorHeaderWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, SurveyEditorHeaderWidget> {
	}
	
	/** This interface is used to change the styling of the filter button from active to inactive */
	interface Style extends CssResource {
		String active();
		String inactive();
	}
	
	@UiField
	Style style;
	
	@UiField
	Button writingModeButton;
	
	@UiField
	Button scoringModeButton;
	
	@UiField
	Button closeButton;
	
	@UiField
	Button saveButton;
	
	@UiField
	Button filterButton;
	
	@UiField
	IconStack iconStackCollectInfo;
	
	@UiField
	IconStack iconStackCollectInfoScored;
	
	@UiField
    IconStack iconStackAssessKnowledge;
	
	@UiField
    IconStack iconStackQuestionBank;
	
	@UiField 
	EditableInlineLabel surveyNameBox;
	
	@UiField
	protected Button surveyPropertiesButton;
	
	@UiField
	protected Button previewButton;
	
	@UiField
	protected FormLabel surveyNameLabel;
	
	@UiField
	protected Tooltip surveyNameTooltip;
	
	@UiField
	protected FormLabel surveyNameRequiredLabel;
	
	@UiField
	protected Tooltip saveTooltip;
	
	/**
	 * Dialog used to select properties for the survey, such as
	 * hide survey name, hide number of questions, and display in full screen
	 */
	private SurveyPropertiesDialog surveyPropertiesDialog;
	
	private ModalDialogBox confirmCloseDialog = new ModalDialogBox();
	
	/** This is a static text that is displayed for any question bank survey (client side only).  It is not used on the backend and is
	 * not saved as the actual survey name.  It is only presented to the user to signify that they are editing a question bank survey.
	 */
	private static final String QUESTION_BANK_NAME = "Question Bank";
	
	/** The type of survey being edited. */
	private SurveyDialogOption surveyType = null; 
	
	/** Flag to control if the survey header is in readonly mode. */
	private boolean isReadOnly = false;
	
	/** Flag to keep track of the status of the filter button */
	private boolean filterOn = false;
	
	/** Text that is displayed in the survey name label. */
	private static final String SURVEY_NAME_LABEL = "Survey Name:";
	
	/** Text that is displayed in the survey name label for read only mode. */
	private static final String SURVEY_NAME_LABEL_READONLY = "(READ-ONLY SURVEY) Survey Name: ";
	
	@UiHandler("writingModeButton")
	void onClickWritingMode(ClickEvent event) {
	    if (!writingModeButton.isActive()) {
	        writingModeButton.setActive(true);
	        scoringModeButton.setActive(false);
	        
	        SharedResources.getInstance().getEventBus().fireEvent(new SurveyChangeEditMode(SurveyEditMode.WritingMode));
	    }
	}
	
	@UiHandler("scoringModeButton") 
	void onClickScoringMode(ClickEvent event) {
	    if (!scoringModeButton.isActive()) {
	        writingModeButton.setActive(false);
	        scoringModeButton.setActive(true);
	        
	        SharedResources.getInstance().getEventBus().fireEvent(new SurveyChangeEditMode(SurveyEditMode.ScoringMode));
	    }
	}
	
	@UiHandler("filterButton") 
	void onClickFilter(ClickEvent event) {
		filterButton.getElement().addClassName(filterOn ? style.active() : style.inactive());
		filterButton.getElement().removeClassName(filterOn ? style.inactive() : style.active());
		filterOn = !filterOn;

		SharedResources.getInstance().getEventBus().fireEvent(new SurveyFilterEvent());
	}
	
	@UiHandler("saveButton") 
	void onClickSave(ClickEvent event) {
	    if(!isReadOnly){
	        SharedResources.getInstance().getEventBus().fireEvent(new SurveySaveEvent());
	    }
	}
	
	@UiHandler("previewButton") 
	void onClickPreview(ClickEvent event) {
	    SharedResources.getInstance().getEventBus().fireEvent(new SurveyPreviewEvent());
	}
	
	/**
	 * Sets/Updates the survey properties for the header.  These are accessed by the user 
	 * via the survey properties dialog.
	 * 
	 * @param props - The current set of survey properties.
	 */
	public void setSurveyProperties(SurveyProperties props) {
	    surveyPropertiesDialog.setEnabled(!isReadOnly);
	    surveyPropertiesDialog.setSurveyProperties(props);
	}
	
	/** 
	 * The survey reference that is currently open for editing. Used to determine whether survey previews
	 * should be displayed in full screen mode or not.
	 */
	private AbstractSurveyReference surveyReference = null;
	
	/**
	 * Creates a new dialog for selecting a survey context survey 
	 */
	public SurveyEditorHeaderWidget() {
	    
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    writingModeButton.setActive(true);
	    
	    surveyPropertiesDialog = new SurveyPropertiesDialog();
	    surveyPropertiesDialog.setText("Survey Properties");
	    surveyPropertiesDialog.setCloseable(true);

	    filterOn = false;
		filterButton.getElement().addClassName(style.active());
		filterButton.getElement().removeClassName(style.inactive());
	    
	    surveyNameBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if(event.getValue() == "") {
                    surveyNameRequiredLabel.setVisible(true);
                }
                else {
                    surveyNameRequiredLabel.setVisible(false);
                }
            }
	    });
	    
	    closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                logger.info("close button clicked.");
            
                if (!isReadOnlyMode()) {
                    // If the user is in edit mode, then bring up a confirmation dialog.
                    confirmCloseDialog.center();
                } else {
                    // If the user is in read only mode, then clicking the close button will close the survey.
                    SharedResources.getInstance().getEventBus().fireEvent(new SurveyCloseEvent());
                }
                
            }
	        
	    });
	    
	    surveyPropertiesButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				surveyPropertiesDialog.center();
			}
	    	
	    });
	    
	    surveyNameTooltip.setPlacement(Placement.BOTTOM);
	    
	    createSurveyConfirmCloseDialog();
	}
	
	/**
	 * Initializes the survey properties dialog with properties from the survey
	 * 
	 * @param type - Type of survey that will be edited.
	 * @param courseData - The current course object data.
	 * @param survey - The current survey
	 * @param isReadOnly - Determines whether the widget is editable
	 */
	public void initializeWidget(SurveyDialogOption type, Survey survey, Boolean isReadOnly) {
	    setSurveyProperties(survey.getProperties());

		initializeWidget(type, survey.getName());

	    setReadOnlyMode(isReadOnly);

	}

	

    /**
	 * Initializes the header widget based on the type of survey being edited.
	 * 
	 * @param type - Type of survey that will be edited.
	 * @param surveyName - The default name for the survey.
	 */
    public void initializeWidget(SurveyDialogOption type, String surveyName) {
        
        // Default to having scoring mode supported.
        writingModeButton.setEnabled(true);
        scoringModeButton.setEnabled(true);
        writingModeButton.setActive(true);
        
	    filterOn = false;
		filterButton.getElement().addClassName(style.active());
		filterButton.getElement().removeClassName(style.inactive());
		filterButton.setVisible(type.equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK));
        
        surveyType = type;
        setIconType(type);

        surveyNameBox.setTooltipText("");

        String headerSurveyName = surveyName;
        switch(type) {
           
            case ASSESSLEARNER_QUESTIONBANK:
                headerSurveyName = QUESTION_BANK_NAME;
                // Do not allow the user to change the name of the question bank survey.
                surveyNameBox.setEditingEnabled(false);
                surveyNameTooltip.setTitle("");
                break;
            case COLLECTINFO_NOTSCORED:
                // If the survey is not scored, then the user should not be able to select
                // scoring mode.
                scoringModeButton.setEnabled(false);
                surveyNameBox.setEditingEnabled(true);
                surveyNameTooltip.setTitle("");
                break;
            case COLLECTINFO_SCORED:
                // intentional passthrough 
            case ASSESSLEARNER_STATIC:
                // nothing special needs to be done here, but explicitly show
                // that the mode is supported so an error is not logged.
                surveyNameBox.setEditingEnabled(true);
                surveyNameTooltip.setTitle("");
                break;
            default:
                    logger.severe("SurveyEditorPanel received unsupported mode.  Panel could not be initialized with mode: " + type);
                break;
        }
        
        // Default the Survey Name in the header.
        if (headerSurveyName != null && headerSurveyName.isEmpty() && surveyReference != null) {
            setSurveyName(surveyReference.getReferencingObjectName());
            
        } else {
            setSurveyName(headerSurveyName);
        }
        
    }
    
    /**
     * Sets the survey icon type in the survey header.
     * 
     * @param type - The type of survey that is being edited.
     */
    private void setIconType(SurveyDialogOption type) {
        
        iconStackCollectInfo.setVisible(false);
        iconStackCollectInfoScored.setVisible(false);
        iconStackAssessKnowledge.setVisible(false);
        iconStackQuestionBank.setVisible(false);
        switch(type) {
        case COLLECTINFO_SCORED:
            iconStackCollectInfoScored.setVisible(true);
            
            break;
        case ASSESSLEARNER_QUESTIONBANK:
            iconStackQuestionBank.setVisible(true);
            break;
        case ASSESSLEARNER_STATIC:
            iconStackAssessKnowledge.setVisible(true);
            break;
        case COLLECTINFO_NOTSCORED:
            // INTENTIONAL FALLTHROUGH
        default:
            iconStackCollectInfo.setVisible(true);
            break;
    }
        
    }

    /**
     * Sets the survey name in the header widget.  
     * 
     * @param name - The name of the header widget.
     */
    public void setSurveyName(String name) {
        logger.info("setSurveyName() called: " + name);
        if (name != null && !name.isEmpty()) {
            surveyNameBox.setValue(name);
        }
        
    }
    
    /**
     * Accessor to get the name of the survey displayed in the header.
     * 
     * @return String - The name of the survey.
     */
    public String getSurveyName() {
        return surveyNameBox.getValue();
    }

    /** 
     * Sets the mode for the header widget.
     * 
     * @param mode
     */
    public void setMode(SurveyEditMode mode) {
       
        boolean isWritingMode = (mode == SurveyEditMode.WritingMode);
        
        writingModeButton.setActive(isWritingMode);
        scoringModeButton.setActive(!isWritingMode);
        
    }

    /**
     * Save handler for saving the survey to the database.  The header
     * will update the survey object with data such as survey name.
     * 
     * @param survey - The survey object that will be saved to the database.
     */
    public void saveSurvey(Survey survey) throws SaveSurveyException {
        String surveyName = getSurveyName();
        
        // If the survey is a question bank type of survey, then the name is already set prior to editing the survey.
        // do not show the user the encoded name, and do not allow them to edit it in the survey editor panel.
        if (surveyName != null && !surveyName.isEmpty() && 
                surveyType != SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
            survey.setName(surveyName);
        }
        
    }
    
    /**
     * Preview handler for previewing the survey. The header will display
     * a dialog containing a preview of the survey.
     * 
     * @param survey - The survey object that will be previewed
     */
    public void previewSurvey(Survey survey){
    	
    	boolean fullScreen = false;
    	
    	if(surveyReference != null){
    		
    		if(surveyReference.supportsFullscreen()){ 
    		    //use the value specified if authored
    			
    			fullScreen = surveyReference.isFullscreen();
    			
    		} else if(SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK == survey.getSurveyType()){
                //if the value isn't authored, then set to true because
    		    //the TUI shows question bank surveys in full screen by default but the GAT doesn't
    		    //allow the author to provide a preference to date
                fullScreen = true;
            }
    		
    	} else if(SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK == survey.getSurveyType()){
            //there is no course survey transition information, then set to true because
    	    //the TUI shows question bank surveys in full screen by default but the GAT doesn't
            //allow the author to provide a preference to date
            fullScreen = true;
        }
    	   
    	PreviewSurveyDialog previewSurveyDialog = new PreviewSurveyDialog(survey, fullScreen, GatClientUtility.isDebug());
    	
    	 //make the preview dialog take up the full screen size
    	previewSurveyDialog.setPopupPosition(10, 10);
    	previewSurveyDialog.getElement().getStyle().setProperty("right", "10px");
    	previewSurveyDialog.getElement().getStyle().setProperty("bottom", "10px");
        
        previewSurveyDialog.show();
    }
    
    /**
     * Creates the confirm close dialog.
     */
    private void createSurveyConfirmCloseDialog() {
        confirmCloseDialog.setGlassEnabled(true);
        confirmCloseDialog.setText("Do you wish to save before exiting?");
        
        HTML confirmMessage = new HTML(
                "Do you wish to save before exiting the survey editor?"
                + "<br/><br/>"
                + "You are about to exit the survey editor.  <font color='red'><b>Any unsaved changes will be lost.  </b></font>");
        
        confirmMessage.getElement().getStyle().setPadding(10, Unit.PX);
        confirmMessage.getElement().getStyle().setProperty("maxWidth", "700px");
        
        confirmCloseDialog.add(confirmMessage);
        
        FlowPanel footer = new FlowPanel();
        
        Button noSaveButton = new Button("Exit without Saving", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                
                confirmCloseDialog.hide();
                
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyCloseEvent());
            }
        });
        
        noSaveButton.setType(ButtonType.DANGER);
        footer.add(noSaveButton);
        
        Button saveFirstButton = new Button("Save First, Then Exit", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                
                confirmCloseDialog.hide();
                
                SharedResources.getInstance().getEventBus().fireEvent(new SurveySaveEvent(true));
            }
        });
        
        saveFirstButton.setType(ButtonType.PRIMARY);
        footer.add(saveFirstButton);

        Button cancelButton = new Button("Cancel", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                confirmCloseDialog.hide();
            }
        });
        
        cancelButton.setType(ButtonType.PRIMARY);
        footer.add(cancelButton);
        
        confirmCloseDialog.setFooterWidget(footer);
    }

    /**
     * Handles when the user has selected a survey reference to edit
     * 
     * @param reference the selected survey reference
     */
    public void onSurveyReferenceSelected(AbstractSurveyReference reference) {
        surveyReference = reference;
    }

    /**
     * Sets the readonly mode for the survey editor header widget.
     * 
     * @param readOnly True if the header should be in readonly mode, false otherwise.
     */
    public void setReadOnlyMode(boolean readOnly) {
        logger.info("setReadOnlyMode = " + readOnly);
        
        isReadOnly = readOnly;

        surveyPropertiesDialog.setEnabled(!readOnly);
        
        // Disable the save button if in readonly mode.
        
        if (isReadOnly) {
            saveButton.addStyleName("disabled");
            saveTooltip.setTitle("Saving is disabled because the survey is in read-only mode.");

            surveyNameLabel.setText(SURVEY_NAME_LABEL_READONLY);
            surveyNameBox.setEditingEnabled(false);
            if (!SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK.equals(surveyType)) {
                surveyNameTooltip.setTitle("Cannot edit survey name because the survey is in read-only mode.");
            }
            surveyNameRequiredLabel.setVisible(false);
        } else {
            saveButton.removeStyleName("disabled");
            saveTooltip.setTitle("Save Survey");

            surveyNameLabel.setText(SURVEY_NAME_LABEL);
            if (!SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK.equals(surveyType)) {
                surveyNameTooltip.setTitle("");
                surveyNameBox.setEditingEnabled(true);
            }
            if(surveyNameBox.getTextEditor().getValue() == "") {
                surveyNameRequiredLabel.setVisible(true);
            }
        }
    }   
    
    /**
     * Accessor to get if the survey header widget is in readonly mode.
     * 
     * @return True if the header is in readonly mode, false otherwise.
     */
    public boolean isReadOnlyMode() {
        return this.isReadOnly;
    }

	/**
	 * Sets whether or not this widget should alter its UI components to accommodate selection mode (i.e. when the user is
	 * selecting survey items)
	 * 
	 * @param isSelectionMode whether or not selection mode is enabled
	 */
	public void setSelectionModeEnabled(boolean selectionEnabled) {
		
		//if selection mode is enabled, then we want to prevent users from modifying the parts of the survey
		//that this widget manages.
		saveButton.setVisible(!selectionEnabled);
		surveyPropertiesButton.setVisible(!selectionEnabled);
		closeButton.setVisible(!selectionEnabled);
		
		surveyNameBox.setEditingEnabled(!selectionEnabled);
	}
}
