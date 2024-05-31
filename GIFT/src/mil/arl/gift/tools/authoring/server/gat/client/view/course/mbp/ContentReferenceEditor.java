/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteChangeEvent;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteChangeHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.course.BooleanEnum;
import generated.course.ConversationTreeFile;
import generated.course.ImageProperties;
import generated.course.LessonMaterial;
import generated.course.LessonMaterialList;
import generated.course.LtiConcepts;
import generated.course.LtiProperties;
import generated.course.Media;
import generated.course.PDFProperties;
import generated.course.SlideShowProperties;
import generated.course.TrainingApplication;
import generated.course.WebpageProperties;
import generated.course.YoutubeVideoProperties;
import generated.course.VideoProperties;
import generated.metadata.Concept;
import generated.metadata.Metadata;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.enums.TrainingApplicationStateEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.event.PointerDownEvent;
import mil.arl.gift.common.gwt.client.event.PointerDownHandler;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableHTML;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.VerticalResizeTextArea;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.FillInTheBlankQuestion;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyProperties;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.place.ConversationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.util.NotifyUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.lm.MediaPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.InteropsEditedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.RealTimeAssessmentPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.TrainingAppInteropEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModalCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.NewOrExistingFileDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMetadataFilesForMerrillQuadrant;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.LockLessonMaterialReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.UnlockLessonMaterialReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchQuestionExport;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.StringListResult;

/**
 * An editor used to create and modify the metadata for content files
 * 
 * @author nroberts
 */
public class ContentReferenceEditor extends AbstractCourseObjectEditor<Metadata> implements PropertySetListener {
	
	/** The text displayed for file selection buttons when no file has been selected */
	private static final String NO_FILE_LABEL = "No File Selected";
	
   protected static final String HIGHLIGHT_TARGET_EMPTY_TEXT = 
            "<p style='color: gray;'>"
        +       "This is where the text that learners can highlight from will appear."
        +   "</p>";

    protected static final String HIGHLIGHT_IDEAL_SELECTION_EMPTY_TEXT = 
            "<p style='color: gray;'>"
        +       "This is where you will be able to select the ideal passage to be highlighted."
        +   "</p>";
    
    protected static final String SUMMARY_TARGET_EMPTY_TEXT = 
            "<p style='color: gray;'>"
        +       "This is where the text for learners to summarize will appear."
        +   "</p>";
    
    /** the css class name that is used for labels of forms that have required input which isn't provided */
    private static final String REQ_FORM_INPUT_LABEL_NOT_PROVIDED_STYLENAME = "requiredFormInputLabelNotProvided";
    
    /** the css class name that is used for borders of form inputs that have required input which isn't populated */
    private static final String REQ_FORM_INPUT_BORDER_NOT_PROVIDED_STYLENAME = "requiredFormInputBorderNotProvided";
    
    /** Placeholder question text used to avoid setting highlight questions' text to null */
    protected static final String HIGHLIGHT_TEXT_PLACEHOLDER = "HIGHLIGHT_TEXT_PLACEHOLDER";
    
    /** Placeholder question text used to avoid setting summarize questions' text to null */
    protected static final String SUMMARIZE_TEXT_PLACEHOLDER = "SUMMARIZE_TEXT_PLACEHOLDER";
    
    /** An empty training application used to clear out the appropriate editor */
    private static final TrainingApplication EMTPY_TRAINING_APPLICATION = new TrainingApplication();
	
    /** logger instance */
	private static Logger logger = Logger.getLogger(ContentReferenceEditor.class.getName());
	
	/** A list of the selection handlers that should be notified when the user selects text in the document */
    private static List<Command> selectionHandlers = new ArrayList<>();
    
    static {
        
        //listen for mouse up events anywhere in the DOM in case the user is selecting text
        Event.addNativePreviewHandler(new NativePreviewHandler() {

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {

                if (event.isCanceled()) {
                    return;
                }

                // If the event targets the popup or the partner, consume it
                Event nativeEvent = Event.as(event.getNativeEvent());

                // Switch on the event type
                int type = nativeEvent.getTypeInt();
                switch (type) {

                case Event.ONMOUSEUP:
                    // Don't eat events if event capture is enabled, as this can
                    // interfere with dialog dragging, for example.
                    if (DOM.getCaptureElement() != null) {
                        event.consume();
                        return;
                    }

                    //notify listeners when a mouse up event is detected
                    for(Command handler : selectionHandlers) {
                        handler.execute();
                    }
                    
                    break;
                }
            }
        });
    }

	private static ContentReferenceEditorUiBinder uiBinder = GWT.create(ContentReferenceEditorUiBinder.class);

	interface ContentReferenceEditorUiBinder extends UiBinder<Widget, ContentReferenceEditor> {
	}
	
	interface Style extends CssResource {
		String contentTypePanelInner();
		String contentTypePanel();
		String iconTypePanelDisplay();
		String interactiveThumbnail();
	}
	
	@UiField
	protected Style style;

	@UiField
	protected DeckPanel mainDeck;
	
	@UiField
	protected Widget choicePanel;
	
	/** The ribbon panel where the content types that the user can select from are displayed */
	@UiField
	protected FlowPanel ribbon;
	
	@UiField
	protected Button slideShowButton;
	
	@UiField
	protected Button powerPointButton;
	
	@UiField
	protected Button pdfButton;
	
	@UiField
	protected Button localWebpageButton;
	
	@UiField
	protected Button imageButton;
	
	@UiField
    protected Button videoButton;
	
	@UiField
	protected Button webAddressButton;
	
	@UiField
	protected Button youTubeButton;
	
	@UiField
	protected Button ltiProviderButton;
	
    @UiField
    protected Button highlightButton;
    
    @UiField
    protected Button convTreeButton;
    
    @UiField
    protected Button summarizeButton;	  
	
	//////////////////
	// PowerPoint
	//////////////////
	
	@UiField
	protected Widget powerPointPanel;
	
	@UiField
	protected Label pptFileLabel;
    
    @UiField
    protected FocusPanel selectPPTFilePanel;
    
    @UiField
    protected Widget pptSelectedPanel;
    
    @UiField
    protected Button removePptButton;
    
    @UiField
    protected Label selectPPTFileLabel;	
    
    //currently this is only being used for PowerPoint content type in the add metadata content editor
    //since the other content types are handled in MediaPanel.java
    @UiField
    protected TextBox pptLinkTextBox;
    ///////////////////////////
	
	@UiField
	protected BlockerPanel inputBlocker;
	
	@UiField
	protected MediaPanel mediaPanel;
	
	/** The interop editor used to author training applications for interactive content */
	@UiField
	protected TrainingAppInteropEditor taInteropEditor;
	
    @UiField
    protected TextBox highlightTitle;
    
    @UiField
    protected Widget highlightPanel;
    
    /** contains authoring elements for creating metadata for conversation tree activity */
    @UiField
    protected Widget convTreePanel;
    
    /** used to author the metadata display name attribute when editing metadata for conversation tree */
    @UiField
    protected TextBox convTreeTitle;
    
    /** manages the conversation tree file being linked to metadata (e.g. edit, copy, remove) */
    @UiField
    protected RealTimeAssessmentPanel conversationTreeSelectPanel;
    
    @UiField
    protected Widget highlightThumbnail;
    
    @UiField
    protected Widget convTreeThumbnail;
    
    @UiField
    protected EditableHTML highlightInstructionsEditor;
    
    @UiField
    protected EditableHTML highlightTargetTextEditor;
    
    @UiField
    protected HTML highlightIdealSelectionText;
    
    @UiField
    protected InlineHTML highlightTitleLabel;
    
    @UiField
    protected InlineHTML summaryTitleLabel;
    
    @UiField
    protected Button highlightResetButton;
    
    @UiField
    protected CheckBox highlightImageCheckbox;
    
    @UiField(provided=true)
    protected InlineQuestionImagePropertySetWidget highlightProperties;
    
    @UiField
    protected FlowPanel highlightPanelInner;    
    
    @UiField
    protected TextBox summaryTitle;
    
    @UiField
    protected Widget summarizeThumbnail;
    
    @UiField
    protected Widget summarizePanel;
    
    @UiField
    protected EditableHTML summaryInstructionsEditor;
    
    @UiField
    protected EditableHTML summaryTargetTextEditor;
    
    @UiField(provided=true)
    protected EditableHTML summaryIdealText = new EditableHTML();
    
    @UiField(provided=true)
    protected VerticalResizeTextArea learnerSummaryTextEditor = new VerticalResizeTextArea();
    
    @UiField
    protected CheckBox summaryMediaCheckbox;

    @UiField(provided=true)
    protected InlineQuestionImagePropertySetWidget summaryProperties;
    
    @UiField
    protected FlowPanel summaryPanelInner;
    
    /** dialog shown to edit a conversation tree file */
    @UiField
    protected CourseObjectModal convTreeEditorDialog;
    
    /**
     * RPC service that is used to retrieve the surveys from the database
     */
    private final SurveyRpcServiceAsync rpcService = GWT.create(SurveyRpcService.class);   
	
	/** An optional command that will be executed whenever the content type is changed */
	private Command typeChangedCommand = null;
	
	/** An optional command that will be executed whenever the underlying metadata is changed*/
	private Command onChangeCommand = null;
	
	/** A dialog used to select PowerPoint files */
	private DefaultGatFileSelectionDialog powerPointFileDialog = new DefaultGatFileSelectionDialog();
	
	/** A dialog used to select conversation tree files */
	private DefaultGatFileSelectionDialog convTreeFileSelectionDialog = new DefaultGatFileSelectionDialog();
	
	/** The path to the course currently being edited*/
	private String currentCoursePath = "";
	
	/** The path to the folder the course resides in */
	private String courseFolderPath = "";
	
	/** The metadata currently being edited */
	private Metadata metadata = null;
	
	/** Whether or not the Content Reference Editor is being displayed in a Lesson Material course object*/
	private boolean useLessonMaterialMode = false;
	
	private boolean useMetadataEditorMode = false;
	
	/** 
	 * The lesson material content to be used by the metadata, assuming the user has selected a content type that uses 
	 * a lesson material file.
	 */
	private LessonMaterialList lessonMaterialList = null;
	
	/** The question to be exported and used by the metadata, assuming the user has selected a content type that uses
     * survey questions.*/
    private AbstractQuestion exportQuestion = null;
    
    /** contains information about the current conversation tree file being linked to this metadata (i.e. the conversation tree file name) */
    private generated.course.ConversationTreeFile convTreeFile = null;

	private boolean readOnly;
	
	/** A command used to handle executing logic when the user selects text in the document */
    private Command selectionHandler;

    /** 
     * Additional interactive content choices provided by the training application 
     * interop editor when interactive content is allowed 
     */
    private List<Widget> trainingAppChoices;

	
	/**
	 * Creates a new editor for modifying metadata content
	 */
	public ContentReferenceEditor() {		
		this(false);
	}
	
	/**
	 * Creates a new editor for modifying metadata content
	 * 
	 * @param useMetadataEditorMode whether or not this widget should hide certain fields for the metadata editor,
	 * true indicates editing metadata and false indicates creating metadata
	 */
	public ContentReferenceEditor(final boolean useMetadataEditorMode) {
	    
        SurveyProperties highlightFeedbackProperties = new SurveyProperties();
        SurveyProperties summaryFeedbackProperties = new SurveyProperties();
        
        highlightFeedbackProperties.setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_QUESTION_NUMBERS, true);
        summaryFeedbackProperties.setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_QUESTION_NUMBERS, true);
        
        highlightProperties = new InlineQuestionImagePropertySetWidget(this);
        summaryProperties = new InlineQuestionImagePropertySetWidget(this);
        learnerSummaryTextEditor.setShowToolbar(false);
	        
		setWidget(uiBinder.createAndBindUi(this));
		this.useMetadataEditorMode = useMetadataEditorMode;
		
		currentCoursePath = GatClientUtility.getBaseCourseFolderPath();
		taInteropEditor.setCourseFolderPath(currentCoursePath);
        
        String courseName = GatClientUtility.getCourseFolderName(currentCoursePath);
        courseFolderPath = currentCoursePath.substring(0, currentCoursePath.indexOf(courseName) + courseName.length());        
        
		//
		// Slideshow
		//
		
		slideShowButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				
				if(metadata != null){
					
					clearEnteredData();
					
					lessonMaterialList = new LessonMaterialList();
					
					Media media = new Media();
					media.setMediaTypeProperties(new SlideShowProperties());
					
					lessonMaterialList.getMedia().add(media);
					lessonMaterialList.setIsCollection(generated.course.BooleanEnum.FALSE);
					
					LessonMaterial lessonMaterial = new LessonMaterial();
					
					//generate a name for the lesson material being edited so that the slide show
					//files can be saved properly
					Date date = new Date();
					DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
					lessonMaterial.setTransitionName("LessonMaterialContent_" + format.format(date));
					
					lessonMaterial.setLessonMaterialList(lessonMaterialList);
					
					mediaPanel.editMetadataSlideShow(lessonMaterial, media, metadata, useMetadataEditorMode);
					
					mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
				}
				
				onTypeChanged();
			}
		});
		
		mediaPanel.setOnChangeCommand(new Command() {
            
            @Override
            public void execute() {
                onChange();
            }
        });
        
		// PowerPoint
        initializePowerPointUI(useMetadataEditorMode);
		
		// PDF
        pdfButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(metadata != null){
                    
                    clearEnteredData();
                    
                    lessonMaterialList = new LessonMaterialList();
                    
                    Media media = new Media();
                    media.setMediaTypeProperties(new PDFProperties());
                    
                    lessonMaterialList.getMedia().add(media);
                    
                    mediaPanel.editMetadataMedia(media, metadata, useMetadataEditorMode);
                    mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                }
                
                onTypeChanged();
            }
        });
        
		// Local webpage
        localWebpageButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(metadata != null){
                    
                    clearEnteredData();
                    
                    lessonMaterialList = new LessonMaterialList();
                    
                    Media media = new Media();
                    media.setMediaTypeProperties(new WebpageProperties());
                    
                    lessonMaterialList.getMedia().add(media);
                    
                    mediaPanel.editMetadataMedia(media, metadata, useMetadataEditorMode);
                    mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                }
                
                onTypeChanged();
            }
        });
        
        // Video
        videoButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(metadata != null){
                    
                    clearEnteredData();
                    
                    lessonMaterialList = new LessonMaterialList();
                    
                    Media media = new Media();
                    media.setMediaTypeProperties(new VideoProperties());
                    
                    lessonMaterialList.getMedia().add(media);
                    
                    mediaPanel.editMetadataMedia(media, metadata, useMetadataEditorMode);
                    mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                }
                
                onTypeChanged();
            }
        });
        
		// Image
        imageButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(metadata != null){
                    
                    clearEnteredData();
                    
                    lessonMaterialList = new LessonMaterialList();
                    
                    Media media = new Media();
                    media.setMediaTypeProperties(new ImageProperties());
                    
                    lessonMaterialList.getMedia().add(media);
                    
                    mediaPanel.editMetadataMedia(media, metadata, useMetadataEditorMode);
                    mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                }
                
                onTypeChanged();
            }
        });
        
		// Website
        webAddressButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(metadata != null){
                    
                    clearEnteredData();

                    lessonMaterialList = new LessonMaterialList();
                    
                    Media media = new Media();
                    media.setMediaTypeProperties(new WebpageProperties());
                    media.setUri("http://www.example.com");
                    lessonMaterialList.getMedia().add(media);
                    
                    mediaPanel.editMetadataMedia(media, metadata, useMetadataEditorMode);
                    mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                }
                
                onTypeChanged();
            }
        });
		
		// Youtube
        youTubeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(metadata != null){
                    
                    clearEnteredData();
                    
                    lessonMaterialList = new LessonMaterialList();
                    
                    Media media = new Media();
                    media.setMediaTypeProperties(new YoutubeVideoProperties());
                    
                    lessonMaterialList.getMedia().add(media);
                    
                    mediaPanel.editMetadataYouTubeVideo(lessonMaterialList, media, metadata, useMetadataEditorMode);
                    mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                }
                
                onTypeChanged();
            }
        });
        
        // LTI provider
        ltiProviderButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                
                if (metadata != null) {

                    clearEnteredData();

                    lessonMaterialList = new LessonMaterialList();

                    Media media = new Media();
                    final LtiProperties ltiProperties = new LtiProperties();
                    ltiProperties.setLtiConcepts(new LtiConcepts());
                    media.setMediaTypeProperties(ltiProperties);

                    if (metadata.getConcepts() != null) {
                        for (Concept concept : metadata.getConcepts().getConcept()) {
                            ltiProperties.getLtiConcepts().getConcepts().add(concept.getName());
                        }
                    }

                    lessonMaterialList.getMedia().add(media);

                    boolean loadedSpecificInstance = false;
                    if (metadata != null && metadata.getPresentAt() != null) {
                        
                        boolean isRemediationOnly = generated.metadata.BooleanEnum.TRUE.equals(metadata.getPresentAt().getRemediationOnly());

                        boolean isRuleOrExample = metadata.getPresentAt().getMerrillQuadrant() != null
                                && (MerrillQuadrantEnum.RULE.getName().equals(metadata.getPresentAt().getMerrillQuadrant())
                                        || MerrillQuadrantEnum.EXAMPLE.getName().equals(metadata.getPresentAt().getMerrillQuadrant()));
                        
                        if (isRemediationOnly) {
                            mediaPanel.editMetadataLtiForMbpRemediation(media, metadata, useMetadataEditorMode);
                            loadedSpecificInstance = true;
                        } else if (isRuleOrExample) {
                            mediaPanel.editMetadataLtiForMbpRuleOrExample(media, metadata, useMetadataEditorMode);
                            loadedSpecificInstance = true;
                        }
                    }

                    // default to regular LTI media panel
                    if (!loadedSpecificInstance) {
                        mediaPanel.editMetadataMedia(media, metadata, useMetadataEditorMode);
                    }

                    mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                }

                onTypeChanged();
            }
        });
        
        highlightButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(metadata != null){
                    
                    clearEnteredData();
                    
                    //create a new question to hold the highlight item's data
                    exportQuestion = new FillInTheBlankQuestion(0, HIGHLIGHT_TEXT_PLACEHOLDER, new SurveyItemProperties(), null, null, null);
                    
                    //make the question required so the user must answer it
                    exportQuestion.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED, true);
                    
                    // use the MULTI_SELECT_ENABLED tag to mark this question as a special type of FillInTheBlankQuestion,
                    // since FillInTheBlankQuestions don't typically use this tag
                    exportQuestion.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.IS_REMEDIATION_CONTENT, true);
                    
                    highlightProperties.edit(new QuestionImagePropertySet());
                    
                    clearHighlightPanel();
                    
                    mainDeck.showWidget(mainDeck.getWidgetIndex(highlightPanel));
                }
                
                onTypeChanged();
            }
        });
        
        summarizeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(metadata != null){
                    
                    clearEnteredData();
                    
                    //create a new question to hold the highlight item's data
                    exportQuestion = new FillInTheBlankQuestion(0, SUMMARIZE_TEXT_PLACEHOLDER, new SurveyItemProperties(), null, null, null);
                    
                    //make the question required so the user must answer it
                    exportQuestion.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED, true);
                    
                    // use the MULTI_SELECT_ENABLED tag to mark this question as a special type of FillInTheBlankQuestion,
                    // since FillInTheBlankQuestions don't typically use this tag
                    exportQuestion.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.IS_REMEDIATION_CONTENT, true);
                    
                    // indicate this question has an answer field so the TUI knows it is a summary
                    exportQuestion.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY, true);
                    
                    summaryProperties.edit(new QuestionImagePropertySet());
                    
                    clearSummaryPanel();
                    
                    mainDeck.showWidget(mainDeck.getWidgetIndex(summarizePanel));
                }
                
                onTypeChanged();
            }
        });
        
        //notify listeners when interops are changed for interactive content
        taInteropEditor.setInteropsEditedCallback(new InteropsEditedCallback() {
            
            @Override
            public void onEdit() {
                onChange();
            }
        });
        
        //load a new training app and show the appropriate editor when an interactive content type is selected
        taInteropEditor.setTypeSelectedCommand(new Command() {
            
            @Override
            public void execute() {
                if(metadata != null){
                    
                    clearEnteredData();
                    
                    TrainingApplication trainingApp = new TrainingApplication();                        
                    trainingApp.setFinishedWhen(TrainingApplicationStateEnum.STOPPED.getDisplayName());
                    
                    taInteropEditor.setTrainingApplication(trainingApp);
                    
                    mainDeck.showWidget(mainDeck.getWidgetIndex(taInteropEditor));
                }
                
                onTypeChanged();
            }
        });
        
        highlightTitle.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(event.getValue() != null && !event.getValue().trim().isEmpty()){
                    
                    if(metadata != null){
                        metadata.setDisplayName(event.getValue());
                    }
                    
                    exportQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, event.getValue());
                    
                } else {            
                    
                    exportQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, null);
                    metadata.setDisplayName(null);
                }
                
                onChange();
            }
        });
        
        highlightInstructionsEditor.setPlaceholder("Enter any instructions that you want your learners to see here.");
        highlightInstructionsEditor.getHtmlEditor().addSummernoteChangeHandler(new SummernoteChangeHandler() {
            
            @Override
            public void onSummernoteChange(SummernoteChangeEvent event) {
                
                if(exportQuestion != null){
                
                    String html = highlightInstructionsEditor.getHtmlEditor().getCode();
                    
                    if(html == null || html.trim().isEmpty() || highlightInstructionsEditor.getHtmlEditor().isEmpty()){                                 
                        exportQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.INSTRUCTION_TEXT, null);
                        
                    } else {                    
                        exportQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.INSTRUCTION_TEXT, html);
                    }
                    
                    highlightResetButton.setEnabled(false);
                    highlightResetButton.setVisible(false);
                }
            }
        });
        
        highlightTargetTextEditor.setPlaceholder("Enter the text from which you want learners to highlight passages here.", "red");
                        
        highlightIdealSelectionText.setHTML(HIGHLIGHT_IDEAL_SELECTION_EMPTY_TEXT);
        
        highlightTargetTextEditor.getHtmlEditor().addSummernoteChangeHandler(new SummernoteChangeHandler() {
            
            @Override
            public void onSummernoteChange(SummernoteChangeEvent event) {
                
                String html = highlightTargetTextEditor.getHtmlEditor().getCode();
                
                if(html == null || html.trim().isEmpty() || highlightTargetTextEditor.getHtmlEditor().isEmpty()){                                   
                    highlightIdealSelectionText.setHTML(HIGHLIGHT_IDEAL_SELECTION_EMPTY_TEXT);
                    exportQuestion.setText(HIGHLIGHT_TEXT_PLACEHOLDER);
                    
                } else {                    
                    highlightIdealSelectionText.setHTML(html);
                    exportQuestion.setText(html);
                }
                
                highlightResetButton.setEnabled(false);
                highlightResetButton.setVisible(false);
                
                onChange();
            }           
        });
        
        
        selectionHandler = new Command() {

            @Override
            public void execute() {
                
                if(mainDeck.getVisibleWidget() == mainDeck.getWidgetIndex(highlightPanel)) {
                
                    String html = highlightTargetTextEditor.getHtmlEditor().getCode();
                    
                    //update the backing data objects with the highlighted text
                    if(html != null && !html.trim().isEmpty() && !highlightTargetTextEditor.getHtmlEditor().isEmpty()){
                        
                        highlightSelection(highlightIdealSelectionText.getElement());
                        
                        String highlightedHtml = highlightIdealSelectionText.getElement().getInnerHTML();
                        
                        Serializable oldValue = exportQuestion.getProperties().getPropertyValue(SurveyPropertyKeyEnum.CORRECT_ANSWER);
                        
                        exportQuestion.getProperties().setPropertyValue(
                                SurveyPropertyKeyEnum.CORRECT_ANSWER, 
                                highlightedHtml
                        );
                        
                        if((oldValue == null && highlightedHtml != null)
                                || (oldValue instanceof String && !StringUtils.equals((String) oldValue, highlightedHtml))) {
                            
                            //text to highlight has changed, so notify listeners
                            onChange();
                        }
                        
                        if(highlightedHtml != null 
                                && !highlightedHtml.equals(highlightTargetTextEditor.getHtmlEditor().getCode())){
                            
                            //if the user has made a selection, show the reset button
                            highlightResetButton.setEnabled(true);
                            highlightResetButton.setVisible(true);
                            
                        } else {
                            
                            //otherwise, hide the reset button when the user has not selected any text
                            highlightResetButton.setEnabled(false);
                            highlightResetButton.setVisible(false);
                        }
                    }
                }
            }
            
        };
        
        highlightResetButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
            	
            	JsniUtility.clearBrowserSelection();
                
                String html = highlightTargetTextEditor.getHtmlEditor().getCode();
                
                if(html == null || html.trim().isEmpty() || highlightTargetTextEditor.getHtmlEditor().isEmpty()){                                   
                    highlightIdealSelectionText.setHTML(HIGHLIGHT_IDEAL_SELECTION_EMPTY_TEXT);
                    
                } else {                    
                    highlightIdealSelectionText.setHTML(html);
                }
                
                highlightResetButton.setEnabled(false);
                highlightResetButton.setVisible(false);
                
                exportQuestion.getProperties().removeProperty(SurveyPropertyKeyEnum.CORRECT_ANSWER);
                
                onChange();
            }
        });
        
        // stop the normal click behavior on the checkbox and let the pointerdownhandler manage changing
        // the checkbox value
        // #4989 - the InlineQuestionImagePropertySetWidget.mainFocus.blurHandler logic was causing an issue
        highlightImageCheckbox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                event.preventDefault();
            }
        });
        
        // change the checkbox value and trigger the checkbox valuechangehandler
        // #4989 - the InlineQuestionImagePropertySetWidget.mainFocus.blurHandler logic was causing an issue
        highlightImageCheckbox.addDomHandler(new PointerDownHandler() {
            
            @Override
            public void onPointerDown(PointerDownEvent event) {
                highlightImageCheckbox.setValue(!highlightImageCheckbox.getValue(), true);
            }
        }, PointerDownEvent.getType());
        
        highlightImageCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                logger.info("setting inline question widget visiblity to "+event.getValue());
                highlightProperties.setVisible(event.getValue());
                
                if(event.getValue()){
                    highlightProperties.onDisplayWidget();
                }else{
                    highlightProperties.clearWidgetProperties();
                }
                
                onChange();
            }
        });
        
        // stop the normal click behavior on the checkbox and let the pointerdownhandler manage changing
        // the checkbox value
        // #4989 - the InlineQuestionImagePropertySetWidget.mainFocus.blurHandler logic was causing an issue
        summaryMediaCheckbox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                event.preventDefault();
            }
        });
        
        // change the checkbox value and trigger the checkbox valuechangehandler
        // #4989 - the InlineQuestionImagePropertySetWidget.mainFocus.blurHandler logic was causing an issue
        summaryMediaCheckbox.addDomHandler(new PointerDownHandler() {
            
            @Override
            public void onPointerDown(PointerDownEvent event) {
                summaryMediaCheckbox.setValue(!summaryMediaCheckbox.getValue(), true);
            }
        }, PointerDownEvent.getType());
        
        summaryMediaCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                logger.info("setting inline question widget visiblity to "+event.getValue());
                summaryProperties.setVisible(event.getValue());
                
                if(event.getValue()){
                    summaryProperties.onDisplayWidget();
                }else{
                    summaryProperties.clearWidgetProperties();
                }

                onChange();
            }
        });
        
        summaryTitle.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(event.getValue() != null && !event.getValue().trim().isEmpty()){
                    
                    if(metadata != null){
                        metadata.setDisplayName(event.getValue());
                    }
                    
                    exportQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, event.getValue());
                    
                } else {            
                    
                    exportQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, null);
                    metadata.setDisplayName(null);
                }
                
                onChange();
            }
        });
        
        summaryInstructionsEditor.setPlaceholder("Enter any instructions that you want your learners to see here.");
        
        summaryInstructionsEditor.getHtmlEditor().addSummernoteChangeHandler(new SummernoteChangeHandler() {
            
            @Override
            public void onSummernoteChange(SummernoteChangeEvent event) {
                
                if(exportQuestion != null){
                
                    String html = summaryInstructionsEditor.getHtmlEditor().getCode();
                    
                    if(html == null || html.trim().isEmpty() || summaryInstructionsEditor.getHtmlEditor().isEmpty()){                                   
                        exportQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.INSTRUCTION_TEXT, null);
                        
                    } else {                    
                        exportQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.INSTRUCTION_TEXT, html);
                    }
                }
            }
        });
        
        summaryTargetTextEditor.setPlaceholder("Enter the text that you want learners to summarize here.", "red");
        
        summaryTargetTextEditor.getHtmlEditor().addSummernoteChangeHandler(new SummernoteChangeHandler() {
            
            @Override
            public void onSummernoteChange(SummernoteChangeEvent event) {
                
                String html = summaryTargetTextEditor.getHtmlEditor().getCode();
                
                if(html == null || html.trim().isEmpty() || summaryTargetTextEditor.getHtmlEditor().isEmpty()){                                 
                    exportQuestion.setText(SUMMARIZE_TEXT_PLACEHOLDER);
                    
                } else {                    
                    exportQuestion.setText(html);
                }
                
                onChange();
            }
        });
        
        summaryIdealText.setPlaceholder("Enter your ideal summary here.", "red");
        summaryIdealText.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                    
                
                if(event.getValue() != null && !event.getValue().trim().isEmpty()){
                    
                    exportQuestion.getProperties().setPropertyValue(
                            SurveyPropertyKeyEnum.CORRECT_ANSWER, 
                            event.getValue()
                    );
                    
                } else {
                    
                    exportQuestion.getProperties().setPropertyValue(
                            SurveyPropertyKeyEnum.CORRECT_ANSWER, 
                            null
                    );
                }
                
                onChange();
                
            }
        });
        
        // a new conversation tree metadata type has been selected, show the panel to author
        // the metadata for a new conversation tree content
        convTreeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(metadata != null){
                    
                    // reset any data models for previously authored content (e.g. summarize passage, conversation tree file)
                    clearEnteredData(); 
                    
                    clearConverastionTreePanel();
                    
                    // this must be created (not null) in order for the metadata editor to hide the attributes panel
                    if(convTreeFile == null){
                        convTreeFile = new generated.course.ConversationTreeFile();
                    }
                    
                    mainDeck.showWidget(mainDeck.getWidgetIndex(convTreePanel));
                }
                
                onTypeChanged();
            }
        });
        
        // The conversation tree metadata display name has changed,
        // update metadata display name attribute and refresh any UI elements (e.g. real time validation)
        convTreeTitle.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(event.getValue() != null && !event.getValue().trim().isEmpty()){
                    
                    if(metadata != null){
                        metadata.setDisplayName(event.getValue());
                    }
                                        
                } else {            
                    
                    metadata.setDisplayName(null);
                }
                
                onChange();
            }
        });
        
        // The edit conversation tree file button has been selected, show the conversation tree editor
        conversationTreeSelectPanel.getEditButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                HashMap<String, String> paramMap = new HashMap<String, String>();
                paramMap.put(ConversationPlace.PARAM_READONLY, Boolean.toString(GatClientUtility.isReadOnly()));
                paramMap.put(ConversationPlace.PARAM_FULLSCREEN, BooleanEnum.TRUE.name());
                paramMap.put(ConversationPlace.PARAM_ALLOW_ASSESSMENTS, BooleanEnum.FALSE.name());
                
                String url = GatClientUtility.getModalDialogUrlWithParams(
                        GatClientUtility.getBaseCourseFolderPath(), convTreeFile.getName(), paramMap);
                
                showConversationTreeModalEditor(GatClientUtility.getBaseCourseFolderPath(), url);
            }           
        });
        
        // The create new conversation tree button was pressed, show the editor for a new conversation tree
        conversationTreeSelectPanel.getAddAssessmentButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                                
                if(GatClientUtility.isReadOnly()) {
                    WarningDialog.error("Read only", "File selection is disabled in Read-Only mode.");
                    return;
                }
                
                NewOrExistingFileDialog.showCreateOrSelect("Conversation Tree", new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent createEvent) {
                        
                        HashMap<String, String> paramMap = new HashMap<String, String>();
                        paramMap.put(ConversationPlace.PARAM_READONLY, Boolean.toString(GatClientUtility.isReadOnly()));
                        paramMap.put(ConversationPlace.PARAM_FULLSCREEN, BooleanEnum.TRUE.name());
                        paramMap.put(ConversationPlace.PARAM_ALLOW_ASSESSMENTS, BooleanEnum.FALSE.name());
                        
                        String url = GatClientUtility.createModalDialogUrlWithParams(
                                GatClientUtility.getBaseCourseFolderPath(), "ConversationTree_", AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION, paramMap);
                        showConversationTreeModalEditor(GatClientUtility.getBaseCourseFolderPath(), url);
                        
                    }
                    
                }, new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent selectEvent) {
                        logger.info("Showing file selection dialog for existing conversation tree file.");
                        convTreeFileSelectionDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION});
                        convTreeFileSelectionDialog.setIntroMessageHTML(DefaultGatFileSelectionDialog.CHOOSE_CONVERSATION_TREE_FILE_METADATA);
                        convTreeFileSelectionDialog.center();
                    }
                    
                });
                
            }
        });
        
        // The delete this conversation tree file button was pressed, confirm deletion type in dialog and then act on it
        conversationTreeSelectPanel.getDeleteButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(!GatClientUtility.isReadOnly()){

                    DeleteRemoveCancelDialog.show("Delete Conversation Tree",
                            "Do you wish to <b>permanently delete</b> this conversation tree or simply remove this reference to prevent it from being used in this part of the course?<br><br>"
                                    + "Other course objects will be unable to use this conversation tree if it is deleted, which may cause validation issues if this conversation tree is being referenced in other parts of the course.",
                            new DeleteRemoveCancelCallback(){

                                @Override
                                public void delete() {
                                    String username = GatClientUtility.getUserName();
                                    String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                    final String path = GatClientUtility.getBaseCourseFolderPath() + "/" + convTreeFile.getName();
                                    
                                    List<String> filesToDelete = new ArrayList<String>();
                                    filesToDelete.add(path);
                                    
                                    DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                                    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                        @Override
                                        public void onFailure(Throwable arg0) {
                                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                    "Failed to delete the file.", 
                                                    arg0.getMessage(), 
                                                    DetailedException.getFullStackTrace(arg0));
                                            dialog.setDialogTitle("Deletion Failed");
                                            dialog.center();
                                        }

                                        @Override
                                        public void onSuccess(GatServiceResult arg0) {
                                            if(arg0.isSuccess()){
                                                // the conversation tree file was successfully deleted
                                                // clear the current conversation tree file reference
                                                convTreeFile.setName(null);
                                                
                                                // show the 'add' conversation tree button again
                                                conversationTreeSelectPanel.removeAssessment();
                                                
                                                // make the 'add' conversation tree button re-appear on the panel
                                                onChange();
                                            }
                                            else{
                                                logger.warning("Was unable to delete the file: " + path + "\nError Message: " + arg0.getErrorMsg());
                                            }
                                            
                                        }
                                        
                                    });
                                }

                                @Override
                                public void remove() {
                                    // removes the referenced but not the file, clear the current conversation tree file reference
                                    convTreeFile.setName(null);
                                    
                                    // show the 'add' conversation tree button again
                                    conversationTreeSelectPanel.removeAssessment();
                                    
                                    // make the 'add' conversation tree button re-appear on the panel
                                    onChange();
                                }

                                @Override
                                public void cancel() {
                                    
                                }
                        
                    });
                }                    

            }           
        });
        
        conversationTreeSelectPanel.setNameOfAssessedItem("Conversation Tree");
        
        // The conversation tree File Selection dialog is done, use the conversation tree file
        // as the conversation for the current metadata authoring
        convTreeFileSelectionDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(logger.isLoggable(Level.INFO)){
                    logger.info("convTreeFileSelectionDialog onValueChange() called, event = " + event);
                }
                    
                if(convTreeFile == null){
                    convTreeFile = new ConversationTreeFile();
                }
                convTreeFile.setName(event.getValue());
                
                // changes the real time assessment panel from an add button to a panel that has
                // a label and the edit/delete buttons
                conversationTreeSelectPanel.setAssessment(event.getValue());
                
                // updates the metadata editor panel
                onChange();
                
                // show the conversation tree editor
                HashMap<String, String> paramMap = new HashMap<String, String>();
                paramMap.put(ConversationPlace.PARAM_READONLY, Boolean.toString(GatClientUtility.isReadOnly()));
                paramMap.put(ConversationPlace.PARAM_FULLSCREEN, BooleanEnum.TRUE.name());
                paramMap.put(ConversationPlace.PARAM_ALLOW_ASSESSMENTS, BooleanEnum.FALSE.name());
                
                String url = GatClientUtility.createModalDialogUrlWithParams(
                        GatClientUtility.getBaseCourseFolderPath(), event.getValue(), AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION, paramMap);
                showConversationTreeModalEditor(GatClientUtility.getBaseCourseFolderPath(), url);
            }            
            
        });
    }
	
	/**
	 * Show the modal dialog to edit the conversation tree file.
	 * 
	 * @param courseFolderPath The path to the course folder relative to the workspaces folder. A trailing slash on the path is optional. 
	 * This path should not be URL encoded. Can't be null.  The conversation tree file is located in this folder.
	 * @param url the URL to give to the modal.  May contain extra parameters that can be parsed and used by the modal.
	 */
    private void showConversationTreeModalEditor(final String courseFolderPath, final String url) {
        convTreeEditorDialog.setCourseObjectUrl(CourseObjectName.CONVERSATION_TREE.getDisplayName() + (GatClientUtility.isReadOnly() ? " (Read Only)" : ""), url);
        convTreeEditorDialog.setSaveButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                logger.info("Finished editing conversation tree");
                
                //save the conversation that has been edited and stop the editor
                GatClientUtility.saveEmbeddedCourseObject();  // save the conversation tree xml file
                String filename = GatClientUtility.getFilenameFromModalUrl(courseFolderPath, url, AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION);
                convTreeEditorDialog.stopEditor();      
                
                //reset the editor modal in case we need to edit another conversation
                CourseObjectModal.resetEmbeddedSaveObject();
                
                //update the UI and data model with the name of the conversation file that was edited
                if(convTreeFile == null){
                    convTreeFile = new ConversationTreeFile();
                }
                
                convTreeFile.setName(filename);
                
                conversationTreeSelectPanel.setAssessment(filename);
                
                // updates the metadata editor panel
                onChange();
                
                if(!GatClientUtility.isReadOnly()){
                    GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this conversation being edited
                }
            }
            
        });
        convTreeEditorDialog.setCancelCallback(new CourseObjectModalCancelCallback() {
            
            @Override
            public void onCancelModal(boolean removeSelection) {
                logger.info("onCancelModal() called - removeSelection = " + removeSelection);

                if (removeSelection) {
                    //make sure the file selection dialog's value is reset if the user cancels editing
                    convTreeFileSelectionDialog.setValue("", true);
                }

            }
        });
        
        convTreeEditorDialog.show();
    
    }
    
    /**
     * Highlights any currently selected text inside the given element, changing the text's background color and returning
     * a string of HTML representing the selected text. If no text is currently selected in the browser window or if
     * the selected text is less than 1 character long, this method will simply return null.
     * 
     * @param target the element within which to highlight the current selection, if one exists
     */
    protected native String highlightSelection(Element target)/*-{
        
        if($wnd.getSelection){
            
            // Nick: The following two functions are based on a solution for determining whether or not the window's
            // selected text is contained within a certain DOM element. This solution was found at
            // http://stackoverflow.com/questions/8339857/how-to-know-if-selected-text-is-inside-a-specific-div
            
            //checks to see if the specified container node is or contains the given node
            function isOrContains(node, container) {
                while (node) {
                    if (node === container) {
                        return true;
                    }
                    node = node.parentNode;
                }
                return false;
            }
            
            //checks to see if the given element contains the browser's currently selected text
            function elementContainsSelection(el) {
                var sel;
                if ($wnd.getSelection) {
                    sel = $wnd.getSelection();
                    if (sel.rangeCount > 0) {
                        for (var i = 0; i < sel.rangeCount; ++i) {
                            if (!isOrContains(sel.getRangeAt(i).commonAncestorContainer, el)) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
            
            //determine if the target element contains the current selection
            if(elementContainsSelection(target)){                 

                var selection = $wnd.getSelection();
                                
                if (selection.rangeCount > 0) {
                    
                  range = selection.getRangeAt(0);
                  
                  //determine if the current selection has 1 or more characters
                  if(!range.collapsed){
                    
                    // Nick: The following two functions are based on a solution for highlighting text found at 
                    // http://stackoverflow.com/questions/2582831/how-can-i-highlight-the-text-of-the-dom-range-object
                    
                    //briefly turns on the browser's document editing mode and applies highlighting to the currently selected range
                    function makeEditableAndHighlight(colour) {
                        var range, sel = $wnd.getSelection();
                        if (sel.rangeCount && sel.getRangeAt) {
                            range = sel.getRangeAt(0);
                        }
                        $doc.designMode = "on";
                        if (range) {
                            sel.removeAllRanges();
                            sel.addRange(range);
                        }
                        // Use HiliteColor since some browsers apply BackColor to the whole block
                        if (!$doc.execCommand("HiliteColor", false, colour)) {
                            $doc.execCommand("BackColor", false, colour);
                        }
                        $doc.designMode = "off";
                    };
                    
                    //highlights the currently selected text with the given color
                    function highlight(colour) {
                        var range;
                        if ($wnd.getSelection) {
                            // IE9 and non-IE
                            try {
                                if (!$doc.execCommand("BackColor", false, colour)) {
                                    makeEditableAndHighlight(colour);
                                }
                            } catch (ex) {
                                makeEditableAndHighlight(colour)
                            }
                        } else if ($doc.selection && $doc.selection.createRange) {
                            // IE <= 8 case
                            range = $doc.selection.createRange();
                            range.execCommand("BackColor", false, colour);
                        }
                    };
        
                    //highlight the currently selected text yellow
                    highlight("yellow");
                    
                    //get the text that was selected as a string of HTML
                    var clonedSelection = range.cloneContents();
                    var div = $doc.createElement('div');
                    div.appendChild(clonedSelection);
                    
                    return div.innerHTML;
                  }
                }
            }
        }
        
        return null;
        
    }-*/;


    /**
	 * Initialize the Powerpoint UI components and handling.
	 * 
	 * @param useMetadataEditorMode whether or not this widget should hide certain fields for the metadata editor,
     * true indicates editing metadata and false indicates creating metadata
	 */
	private void initializePowerPointUI(final boolean useMetadataEditorMode){
	    
	    if(useMetadataEditorMode) {
            
            //in case no file is selected update that UI
            selectPPTFileLabel.setText("No file selected");
            selectPPTFileLabel.getElement().getStyle().setProperty("cursor", "not-allowed");
            
            //in case a file is selected hide the remove component
            removePptButton.setVisible(false);
            
        }else{
            powerPointFileDialog.getFileSelector().setAllowedFileExtensions(Constants.ppt_show_supported_types);
            powerPointFileDialog.setText("Select PowerPoint File");
            powerPointFileDialog.setIntroMessageHTML("Choose a PowerPoint show file to present to the learner ("+Constants.ppt_show_supported_types+").");
        }
        
        powerPointFileDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);
        
        //adding a PowerPoint content type
        powerPointButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(metadata != null){
                    
                    clearEnteredData();
                    
                    clearPowerPointPanel();
                    
                    selectPPTFilePanel.setVisible(true);
                    pptSelectedPanel.setVisible(false);

                    mainDeck.showWidget(mainDeck.getWidgetIndex(powerPointPanel));                 
                }
                
                onTypeChanged();
            }
        });
        
        pptLinkTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(!readOnly && event.getValue() != null){
                    
                    if(metadata != null) {
                        metadata.setDisplayName(event.getValue());
                    }
                
                    onChange();
                }

            }
        });
        
        powerPointFileDialog.setMessageDisplay(DefaultMessageDisplay.ignoreInfoMessage);
        
        selectPPTFilePanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(!useMetadataEditorMode){
                    powerPointFileDialog.center();
                }
            }
        });
        
        powerPointFileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(final ValueChangeEvent<String> event) {
                
                if(metadata != null){
                    
                    Metadata.Simple simpleRef = new Metadata.Simple();
                    simpleRef.setValue(event.getValue());
                    
                    Serializable content = metadata.getContent();
                    if(content instanceof generated.metadata.Metadata.Simple){
                        ((generated.metadata.Metadata.Simple)content).setValue(event.getValue());
                    }else{
                        generated.metadata.Metadata.Simple simple = new generated.metadata.Metadata.Simple();
                        simple.setValue(event.getValue());
                        metadata.setContent(simple);
                    }
                
                    pptFileLabel.setText(
                            event.getValue() != null
                                ? event.getValue()
                                : NO_FILE_LABEL
                    );
                    
                    logger.info("PowerPoint file selected: '"+event.getValue()+"'.");
                    
                    onChange();
                    
                    BsLoadingDialogBox.display("Please Wait", "Checking for metadata references.");
                    
                    if(event.getValue() != null){
                        
                        if(metadata != null && metadata.getPresentAt() != null &&
                                metadata.getPresentAt().getMerrillQuadrant() != null){
                        
                            GetMetadataFilesForMerrillQuadrant action = new GetMetadataFilesForMerrillQuadrant(
                                    GatClientUtility.getUserName(), 
                                    courseFolderPath + "/" + event.getValue(), 
                                    metadata.getPresentAt().getMerrillQuadrant()
                            );
                            
                            SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<StringListResult>(){
    
                                @Override
                                public void onFailure(Throwable thrown) {
                                    
                                    BsLoadingDialogBox.remove();
                                    
                                    new ErrorDetailsDialog( 
                                            "An error occurred while checking for metadata references. You can continue adding content to "
                                            + "this Adaptive courseflow, but if you want to edit an existing metadata file for this "
                                            + "content, you will have to find it manually in the file navigator to edit it.", 
                                            thrown.toString(), 
                                            null).center();
                                }
    
                                @Override
                                public void onSuccess(StringListResult result) {
                                    
                                    BsLoadingDialogBox.remove();
                                    
                                    logger.info("Finished checking for metadata references.\n"+result);
                                    
                                    if(result.isSuccess() && result.getStrings() != null){
                                        
                                        if(!result.getStrings().isEmpty()){                                         
                                        
                                            new MetadataFileReferenceListDialog(event.getValue(), result.getStrings()).center();
                                        }
                                        
                                        if(pptLinkTextBox.getValue() == null || pptLinkTextBox.getValue().isEmpty()){
                                            //update the textfield [if a value wasn't already set]
                                            //(which also sets the metadata displayname attribute)
                                            pptLinkTextBox.setValue(event.getValue(), true);
                                        }
                                        
                                        Notify.notify("", "Successfully set '"+ event.getValue() +"' as the PowerPoint show.", IconType.INFO, NotifyUtil.generateDefaultSettings());

                                        selectPPTFilePanel.setVisible(false);
                                        pptSelectedPanel.setVisible(true);
                                        
                                    } else {
                                        
                                        if(result.getErrorMsg() != null){
                                            
                                            if(result.getErrorDetails() != null){
                                                
                                                new ErrorDetailsDialog(
                                                        result.getErrorMsg(), 
                                                        result.getErrorDetails(), 
                                                        null).center();
                                            } else {
                                                
                                                new ErrorDetailsDialog(
                                                        "An error occurred while checking for metadata references. You can continue adding content to "
                                                        + "this Adaptive courseflow, but if you want to edit an existing metadata file for this "
                                                        + "content, you will have to find it manually in the file navigator to edit it.", 
                                                        result.getErrorMsg(), 
                                                        null).center();
                                            }
                                        
                                        
                                        } else {
                                            
                                            if(result.getErrorDetails() != null){
                                                
                                                new ErrorDetailsDialog(
                                                    "An error occurred while checking for metadata references. You can continue adding content to "
                                                    + "this Adaptive courseflow, but if you want to edit an existing metadata file for this "
                                                    + "content, you will have to find it manually in the file navigator to edit it.", 
                                                    result.getErrorDetails(), 
                                                    null).center();
                            
                                            } else {
                                            
                                                new ErrorDetailsDialog(
                                                        "An error occurred while checking for metadata references. You can continue adding content to "
                                                        + "this Adaptive Courseflow, but if you want to edit an existing metadata file for this "
                                                    + "content, you will have to find it manually in the file navigator to edit it.", 
                                                    "No details available.", 
                                                    null).center();
                                        }
                                    }
                                }
                            }
                            
                        });
                        }
                    }
                }
            }
        });
        
         //handler for removing a guidance file
         removePptButton.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {

                         DeleteRemoveCancelDialog.show("Delete Content", 
                                "Do you wish to <b>permanently delete</b> '"+pptFileLabel.getText()+
                                "' from the course or simply remove the reference to that content in this course object?<br><br>"+
                                        "Other course objects will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.",
                                new DeleteRemoveCancelCallback() {
                            
                                    @Override
                                    public void cancel() {
                                        
                                    }

                                    @Override
                                    public void delete() {
                                        
                                      String username = GatClientUtility.getUserName();
                                      String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                      List<String> filesToDelete = new ArrayList<String>();
                                      final String filePath = courseFolderPath + "/" + pptFileLabel.getText();
                                      filesToDelete.add(filePath);
                                      
                                      DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                                      SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                          @Override
                                          public void onFailure(Throwable error) {
                                              ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                      "Failed to delete the file.", 
                                                      error.getMessage(), 
                                                      DetailedException.getFullStackTrace(error));
                                              dialog.setDialogTitle("Deletion Failed");
                                              dialog.center();
                                          }

                                          @Override
                                          public void onSuccess(GatServiceResult result) {
                                              
                                              if(result.isSuccess()){
                                                  logger.warning("Successfully deleted the file '"+filePath+"'.");
                                              } else{
                                                  logger.warning("Was unable to delete the file: " + filePath + "\nError Message: " + result.getErrorMsg());
                                              }                                           
                                               
                                              resetUI();
                                          }
                                          
                                      });
                                    }

                                    @Override
                                    public void remove() {
                                        resetUI();
                                    }
                                    
                                    private void resetUI(){                                     
                                        metadata.setContent(null);
                                        metadata.setDisplayName(null);
                                        pptFileLabel.setText("Select PowerPoint file");  
                                        selectPPTFilePanel.setVisible(true);
                                        pptSelectedPanel.setVisible(false);
                                    }

                                });
                    }
                    
         });
         
         
         
	}

    /**
     * Notifies the media panel that a concept has been selected or deselected.
     * 
     * @param concept the concept being updated
     * @param selected true if the concept has been selected; false if it has
     *        been deselected.
     */
    public void conceptSelected(String concept, boolean selected) {
        mediaPanel.conceptSelected(concept, selected);
    }

	@Override
	protected void editObject(Metadata metadata) {
		
		editObject(metadata, null);
	}
	
	/**
	 * Loads the given metadata object for editing and loads any resources on its metadata file path
	 * 
	 * @param metadata the metadata to load
	 * @param metadataFilePath an optional file path to the metadata file
	 */
	private void editObject(final Metadata metadata, final String metadataFilePath) {
	    
	    // reset data model for the various types of content the metadata can reference
        this.lessonMaterialList = null;
        this.exportQuestion = null;
        this.convTreeFile = null;
	        
		this.metadata = metadata;
		
        if(metadata != null  
                && metadata.getContent() instanceof generated.metadata.Metadata.LessonMaterial &&
                ((generated.metadata.Metadata.LessonMaterial)metadata.getContent()).getValue() != null){
			
    		AsyncCallback<FetchJAXBObjectResult> callback = new AsyncCallback<FetchJAXBObjectResult>(){
    			@Override
    			public void onFailure(Throwable t) {
    				
    				WarningDialog.error("Failed to load", "An error occurred while trying to load a lesson material file: " + t.getMessage());
    			
    				onFinishedLoading();
    			}

    			@Override
    			public void onSuccess(FetchJAXBObjectResult result) {
    			    
    			    if(logger.isLoggable(Level.INFO)){
    			        logger.info("Result of retrieving lesson material file '"+metadata.getContent()+"' from metadata: "+result);
    			    }
    				
    				if(result.isSuccess()){
    					
	    				lessonMaterialList = (LessonMaterialList) result.getJAXBObject();	 
	    				
	    				updateDisplay();
	    				
	    				onChange();
	    				
    				} else {
    					
    					ErrorDetailsDialog error = new ErrorDetailsDialog(result.getErrorMsg(), 
    							result.getErrorDetails(), result.getErrorStackTrace());
    					error.setText("Failed to Load Lesson Material");
    					error.center();
    					error.addCloseHandler(new CloseHandler<PopupPanel>() {

							@Override
							public void onClose(CloseEvent<PopupPanel> arg0) {
								
								if(metadataFilePath != null){
									closeFile(metadataFilePath);
								}
							}
    						
    					});
    					
    					onFinishedLoading();
    				}
    			}		
    		};
    		
    		FetchJAXBObject action = new FetchJAXBObject();
            action.setRelativePath(GatClientUtility.getBaseCourseFolderPath() + "/" + ((generated.metadata.Metadata.LessonMaterial)metadata.getContent()).getValue());
    		action.setUserName(GatClientUtility.getUserName());
    		
    		SharedResources.getInstance().getDispatchService().execute(action, callback);
			
        } else if(metadata != null  
                && metadata.getContent() instanceof generated.metadata.Metadata.Simple 
                && ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue() != null
                && ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue().endsWith(FileUtil.QUESTION_EXPORT_SUFFIX)){
            
            AsyncCallback<GenericGatServiceResult<AbstractQuestion>> callback = new AsyncCallback<GenericGatServiceResult<AbstractQuestion>>(){
                @Override
                public void onFailure(Throwable t) {
                    
                    WarningDialog.error("Failed to load", "An error occurred while trying to load a question export file: " + t.getMessage());
                    
                    onFinishedLoading();
                }

                @Override
                public void onSuccess(GenericGatServiceResult<AbstractQuestion> result) {
                    
                    GenericRpcResponse<AbstractQuestion> response = result.getResponse();
                    
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("Result of retrieving question export file '"+metadata.getContent()+"' from metadata:\n"+result);
                    }
                    
                    if(response.getWasSuccessful()){
                        
                        exportQuestion = response.getContent();  
                        
                        updateDisplay();
                        
                        onChange();
                        
                    } else {
                        
                        ErrorDetailsDialog error = new ErrorDetailsDialog(
                                response.getException().getReason(), 
                                response.getException().getDetails(), 
                                response.getException().getErrorStackTrace()
                        );
                        error.setText("Failed to Load Question Export");
                        error.center();
                        error.addCloseHandler(new CloseHandler<PopupPanel>() {

                            @Override
                            public void onClose(CloseEvent<PopupPanel> arg0) {
                                
                                if(metadataFilePath != null){
                                    closeFile(metadataFilePath);
                                }
                            }
                            
                        });
                        
                        onFinishedLoading();
                    }
                }       
            };
            
            FetchQuestionExport action = new FetchQuestionExport();
            action.setRelativePath(GatClientUtility.getBaseCourseFolderPath() + "/" + ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue());
            action.setUserName(GatClientUtility.getUserName());
            
            SharedResources.getInstance().getDispatchService().execute(action, callback);
            
        } else if(metadata != null  
                && metadata.getContent() instanceof generated.metadata.Metadata.Simple 
                && ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue() != null
                && ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue().endsWith(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)){
            
            // no need to fetch the conversation tree file (yet).  Assume it exists for now because how else did the user get this far.
            if(convTreeFile == null){
                convTreeFile = new ConversationTreeFile();
            }
            convTreeFile.setName(((generated.metadata.Metadata.Simple)metadata.getContent()).getValue());
            
            updateDisplay();
            
            onChange();
            
        } else {
		    
		    logger.info("clearing lesson material list");
			
			this.lessonMaterialList = null;
			
			updateDisplay();
			
			onChange();
		}
	}
	
	/**
	 * Updates this widgets UI elements to reflect the current state of the metadata being edited
	 */
	private void updateDisplay(){
	    
		if(metadata != null || useLessonMaterialMode){
			
            if(lessonMaterialList != null){
                
                if(lessonMaterialList.getMedia().isEmpty()){
                    
                    mainDeck.showWidget(mainDeck.getWidgetIndex(choicePanel));
                    
                } else {
                    
                    Media item = lessonMaterialList.getMedia().get(0);
                    
                    if(item.getMediaTypeProperties() instanceof SlideShowProperties) {

                        LessonMaterial lessonMaterial = new LessonMaterial();
                        lessonMaterial.setLessonMaterialList(lessonMaterialList);
                        
                        if(item.getName() == null && !item.getName().isEmpty()) {
                            //generate a name for the lesson material being edited so that the slide show
                            //files can be saved properly
                            Date date = new Date();
                            DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                            lessonMaterial.setTransitionName("LessonMaterialContent_" + format.format(date));
                        } else {
                            lessonMaterial.setTransitionName(item.getName());
                        }
                    
                    }

                    if(metadata.getDisplayName() == null){
                        metadata.setDisplayName(item.getName());
                    }
                    
                    boolean loadedSpecificInstance = false;
                    if (item.getMediaTypeProperties() instanceof YoutubeVideoProperties) {

                        // YouTube videos need to be handled differently, since they might have
                        // assessment logic
                        mediaPanel.editMetadataYouTubeVideo(lessonMaterialList, item, metadata, useMetadataEditorMode);
                        loadedSpecificInstance = true;
                    } else if (item.getMediaTypeProperties() instanceof LtiProperties) {
                        if (metadata.getPresentAt() != null) {

                            boolean isRemediationOnly = generated.metadata.BooleanEnum.TRUE.equals(metadata.getPresentAt().getRemediationOnly());

                            boolean isRuleOrExample = metadata.getPresentAt().getMerrillQuadrant() != null
                                    && (MerrillQuadrantEnum.RULE.getName().equals(metadata.getPresentAt().getMerrillQuadrant())
                                            || MerrillQuadrantEnum.EXAMPLE.getName().equals(metadata.getPresentAt().getMerrillQuadrant()));
                            
                            boolean isPractice = metadata.getPresentAt().getMerrillQuadrant() != null
                                    && MerrillQuadrantEnum.PRACTICE.getName().equals(metadata.getPresentAt().getMerrillQuadrant());

                            if (isRemediationOnly) {
                                mediaPanel.editMetadataLtiForMbpRemediation(item, metadata, useMetadataEditorMode);
                                loadedSpecificInstance = true;
                            } else if (isRuleOrExample) {
                                mediaPanel.editMetadataLtiForMbpRuleOrExample(item, metadata, useMetadataEditorMode);
                                loadedSpecificInstance = true;
                            } else if (isPractice) {
                                mediaPanel.editMetadataLtiForMbpPractice(item, metadata, useMetadataEditorMode);
                                loadedSpecificInstance = true;
                            }
                        }
                    }
                    
                    // default to regular media panel
                    if (!loadedSpecificInstance) {
                        mediaPanel.editMetadataMedia(item, metadata, useMetadataEditorMode);
                    }

                    mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                }
                
            }  else if(exportQuestion != null){
                
                if(exportQuestion instanceof FillInTheBlankQuestion){
                    
                    if(exportQuestion.getProperties() != null){
                        
                        if(exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)
                                && exportQuestion.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)){
                            
                            //populate summary panel   
                            String title = null;
                            
                            if(metadata != null && metadata.getDisplayName() != null){
                                title = metadata.getDisplayName();
                            }
                            
                            if(title != null){
                                
                                //make sure the export question's title matches the metadata's
                                if(!title.equals(exportQuestion.getProperties().getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY))){
                                    exportQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, title);
                                }
                                
                            } else if(exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY)){
                                    
                                //if the metadata doesn't have a title but the export question does, assign the export question's title
                                //to the metadata
                                
                                String questionTitle = (String) exportQuestion.getProperties().getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY);
                                
                                title = questionTitle;
                                
                                if(metadata != null){
                                    metadata.setDisplayName(questionTitle);
                                }
                            }
                            
                            summaryTitle.setValue(title);
                            
                            summaryInstructionsEditor.setValue(
                                    (String) exportQuestion.getProperties().getPropertyValue(SurveyPropertyKeyEnum.INSTRUCTION_TEXT)
                            );
                            
                            summaryTargetTextEditor.setValue(
                                    exportQuestion.getText()
                            );
                            
                            if(exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.CORRECT_ANSWER)){
                                
                                String correctAnswer = (String) exportQuestion.getProperties().getPropertyValue(SurveyPropertyKeyEnum.CORRECT_ANSWER);
                                
                                if(!correctAnswer.trim().isEmpty()){
                                    summaryIdealText.setValue(correctAnswer);
                                    
                                } else {
                                    summaryIdealText.setValue(null);
                                }
                                
                            } else {
                                summaryIdealText.setValue(null);
                            }                            
                            
                            
                            QuestionImagePropertySet imageProps = new QuestionImagePropertySet();
                            
                            try {
                               imageProps.load(exportQuestion.getProperties());
                               
                           } catch (LoadSurveyException e) {
                               logger.severe(e.toString());
                           }
                            
                            summaryProperties.edit(imageProps);
                            
                            //check whether or not a question image should be shown
                            if(exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE)){
                                boolean value = exportQuestion.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE);
                                logger.info("setting highlight media checkbox to "+value+" because "+SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE+" property value.");
                                summaryMediaCheckbox.setValue(
                                        value,
                                        true
                                );
                                
                            } else {                                
                                summaryMediaCheckbox.setValue(false, true);
                            }
                            
                            onPropertySetChange(imageProps);
                            
                            mainDeck.showWidget(mainDeck.getWidgetIndex(summarizePanel));
                            
                        } else {
                            
                            //populate highlight panel
                            String title = null;
                            
                            if(metadata != null && metadata.getDisplayName() != null){
                                title = metadata.getDisplayName();
                            }
                            
                            if(title != null){
                                
                                //make sure the export question's title matches the metadata's
                                if(!title.equals(exportQuestion.getProperties().getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY))){
                                    exportQuestion.getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, title);
                                }
                                
                            } else if(exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY)){
                                    
                                //if the metadata doesn't have a title but the export question does, assign the export question's title
                                //to the metadata
                                
                                String questionTitle = (String) exportQuestion.getProperties().getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY);
                                
                                title = questionTitle;
                                
                                if(metadata != null){
                                    metadata.setDisplayName(questionTitle);
                                }
                            }
                            
                            highlightTitle.setValue(title);
                            
                            highlightInstructionsEditor.setValue(
                                    (String) exportQuestion.getProperties().getPropertyValue(SurveyPropertyKeyEnum.INSTRUCTION_TEXT)
                            );
                            
                            highlightTargetTextEditor.setValue(
                                    exportQuestion.getText()
                            );
                            
                            if(exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.CORRECT_ANSWER)){
                                
                                String correctAnswer = (String) exportQuestion.getProperties().getPropertyValue(SurveyPropertyKeyEnum.CORRECT_ANSWER);
                                
                                if(!correctAnswer.trim().isEmpty()){
                                    
                                    highlightIdealSelectionText.setHTML(correctAnswer);
                                    
                                    if(!correctAnswer.equals(exportQuestion.getText())) {
                                        
                                        //show the button to reset highlighting if the ideal text doesn't match the target text
                                        highlightResetButton.setEnabled(true);
                                        highlightResetButton.setVisible(true);
                                    }
                                    
                                } else {
                                    highlightIdealSelectionText.setHTML(exportQuestion.getText());
                                }
                                
                            } else {
                                highlightIdealSelectionText.setHTML(exportQuestion.getText());
                            }
                            
                            QuestionImagePropertySet imageProps = new QuestionImagePropertySet();
                            
                            try {
                               imageProps.load(exportQuestion.getProperties());
                               
                           } catch (LoadSurveyException e) {
                               logger.severe(e.toString());
                           }
                            
                            highlightProperties.edit(imageProps);
                            
                            //check whether or not a question image should be shown
                            if(exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE)){
                                boolean value = exportQuestion.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE);
                                logger.info("setting highlight media checkbox to "+value+" because of property "+SurveyPropertyKeyEnum.DISPLAY_QUESTION_IMAGE+".");
                                highlightImageCheckbox.setValue(
                                        value,
                                        true
                                );
                                
                            } else {                                
                                highlightImageCheckbox.setValue(false, true);
                            }
                            
                            onPropertySetChange(imageProps);
                            
                            mainDeck.showWidget(mainDeck.getWidgetIndex(highlightPanel));
                        }
                        
                    } else {
                        mainDeck.showWidget(mainDeck.getWidgetIndex(choicePanel));
                    }
                    
                } else {
                    mainDeck.showWidget(mainDeck.getWidgetIndex(choicePanel));
                }
                
            } else if(metadata.getContent() instanceof generated.metadata.Metadata.Simple && 
                    ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue() != null){
             
                //set display name attribute for metadata created before this attribute existed
                
                String ref = ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue();
                
                if(metadata.getDisplayName() == null){
                    metadata.setDisplayName(ref);
                }
                
                if(StringUtils.endsWith(ref, Constants.ppt_show_supported_types)){
                    
                    pptFileLabel.setText(ref);
                    
                    selectPPTFilePanel.setVisible(false);
                    pptSelectedPanel.setVisible(true);
                    
                    pptLinkTextBox.setValue(metadata.getDisplayName());
                    
                    if(readOnly){
                        pptLinkTextBox.setEnabled(false);
                    }
                    
                    mainDeck.showWidget(mainDeck.getWidgetIndex(powerPointPanel));
                    
                }else if(StringUtils.endsWith(ref, AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)){
                    
                    // populate specific UI elements
                    convTreeTitle.setValue(metadata.getDisplayName());
                    
                    if(readOnly){
                        convTreeTitle.setEnabled(false);
                    }
                    
                    conversationTreeSelectPanel.setAssessment(ref);
                    
                    mainDeck.showWidget(mainDeck.getWidgetIndex(convTreePanel));
                } else {
                    
                    mediaPanel.editLocalWebpage(metadata, useMetadataEditorMode);
                    mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                }
            
            } else if(metadata.getContent() instanceof generated.metadata.Metadata.URL && 
                    ((generated.metadata.Metadata.URL)metadata.getContent()).getValue() != null){
                //set display name attribute for metadata created before this attribute existed
                
                if(metadata.getDisplayName() == null){
                    metadata.setDisplayName(((generated.metadata.Metadata.URL)metadata.getContent()).getValue());
                }  
                
                mediaPanel.editWebAddress(metadata, useMetadataEditorMode);
                mainDeck.showWidget(mainDeck.getWidgetIndex(mediaPanel));
                
            } else {
                mainDeck.showWidget(mainDeck.getWidgetIndex(choicePanel));
            }
		}
		
        if(metadata != null){
            
            if(metadata.getPresentAt() != null){ 
                
                //detect whether or not remediation-only UI elements should be shown
                boolean isRemediationOnly = generated.metadata.BooleanEnum.TRUE.equals(metadata.getPresentAt().getRemediationOnly());
                logger.info("Setting remediation enabled to "+isRemediationOnly);
                setRemediationEnabled(isRemediationOnly);
            }
        }
        
        onTypeChanged();
		
		onFinishedLoading();
	}
	
	/**
	 * Clears any content references that have been specified by the author
	 */
    private void clearEnteredData(){
                
        metadata.setContent(null);
        metadata.setDisplayName(null);
        
        lessonMaterialList = null;
        exportQuestion = null;
        convTreeFile = null;
        
        //training app interop editor does not allow null, so pass it a default empty training app
        taInteropEditor.setTrainingApplication(EMTPY_TRAINING_APPLICATION);
    }
	
	/**
	 * Assigns the command to be executed whenever the content type is changed
	 * 
	 * @param command the command to be executed
	 */
	public void setTypeChangedCommand(Command command){
		this.typeChangedCommand = command;
	}
	
	/**
	 * Assigns the command to be executed whenever the underlying metadata is changed
	 * 
	 * @param command the command to be executed
	 */
	public void setOnChangeCommand(Command command){
		this.onChangeCommand = command;
	}
	
	/**
    * Notifies listers whenever the type of content represented by this widget has changed and performs some common UI
    * operations that need to be done every time the content type changes
    */
   private void onTypeChanged(){
       
       if(typeChangedCommand != null){
           typeChangedCommand.execute();
       }
       
       onChange();
   }
   
    /**
	 * Executes whatever command is waiting for a change event, assuming such a command has been assigned
	 */
	private void onChange(){
		
		if(onChangeCommand != null){
			onChangeCommand.execute();
		}
	}
	
	/**
	 * Checks whether or not the author has selected a content type yet
	 * 
	 * @return whether or not the author has selected a content type yet
	 */
	public boolean hasSelectedType(){
		return mainDeck.getVisibleWidget() != mainDeck.getWidgetIndex(choicePanel);
	}
	
	/**
	 * Clears all changes to the PowerPoint panel
	 */
	private void clearPowerPointPanel(){
	    pptFileLabel.setText(NO_FILE_LABEL);
	    pptLinkTextBox.setValue(null);
	}
	
	/**
     * Clears all changes to the highlight panel
     */
    private void clearHighlightPanel(){
        
        highlightTitle.setValue(null);
        highlightTargetTextEditor.setValue(null, false);
        highlightInstructionsEditor.setValue(null);
        highlightIdealSelectionText.setHTML(HIGHLIGHT_IDEAL_SELECTION_EMPTY_TEXT);
        highlightImageCheckbox.setValue(false, true);
        highlightResetButton.setEnabled(false);
        highlightResetButton.setVisible(false);
    }
    
    /**
     * Clears all changes to the summary panel
     */
    private void clearSummaryPanel(){
        
        summaryTitle.setValue(null);
        summaryTargetTextEditor.setValue(null, false);
        summaryInstructionsEditor.setValue(null);
        summaryIdealText.setValue(null);
        summaryMediaCheckbox.setValue(false, true);
    }
    
    /**
     * Clears all changes to the conversation tree panel
     */
    private void clearConverastionTreePanel(){
        
        convTreeTitle.setValue(null);
        conversationTreeSelectPanel.removeAssessment();
    }
    
    /**
	 * Gets the supplemental lesson material to be used by the metadata being modified, if applicable. This method will return null
	 * unless the author has selected a content type that uses a lesson material reference, such as a slide show or PDF file.
	 * 
	 * @return the lesson material
	 */
	public LessonMaterialList getLessonMaterial(){
		return lessonMaterialList;
	}
	
	/**
     * Gets the supplemental question to be used by the metadata being modified, if applicable. This method will return null
     * unless the author has selected a content type that uses survey questions, such as a highlight item.
     * 
     * @return the lesson material
     */
    public AbstractQuestion getQuestionExport(){
        
        if(mainDeck.getWidgetIndex(highlightPanel) == mainDeck.getVisibleWidget()){
            
            highlightProperties.getObjectBeingEdited().getProperties().copyInto(exportQuestion.getProperties());    
            
            return exportQuestion;
            
        } else if(mainDeck.getWidgetIndex(summarizePanel) == mainDeck.getVisibleWidget()){
            
            summaryProperties.getObjectBeingEdited().getProperties().copyInto(exportQuestion.getProperties());  
            
            return exportQuestion;
            
        } else {        
            return null;
        }
    }
    
    /**
     * Gets the current conversation tree file that is having metadata authored for it.  This method will return null
     * unless the author has selected the conversation tree content type.
     * 
     * @return the conversation tree file object (it contains a referenced to the conversation tree file name)
     */
    public ConversationTreeFile getConversationTree(){
        
        if(mainDeck.getWidgetIndex(convTreePanel) == mainDeck.getVisibleWidget()){
            return convTreeFile;
        }else{
            return null;
        }
    }
    
     /**
	 * Gets a string describing the validation problems with the current training application. If no problems are found, then an 
	 * empty string will be returned.
	 * 
	 * @return the validation error message
	 */
    public String getValidationErrors(){
        
        StringBuilder errorMsg = new StringBuilder();
        
        if(metadata != null){ 

            if(mainDeck.getVisibleWidget() == mainDeck.getWidgetIndex(powerPointPanel)){      

                if(metadata.getContent() == null || !(metadata.getContent() instanceof generated.metadata.Metadata.Simple) || 
                        ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue() == null){
    
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the PowerPoint file to be used.")
                            .append("</li>");
                }               
                
                if(metadata.getDisplayName() == null || metadata.getDisplayName().isEmpty()){
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the name to give this PowerPoint.")
                            .append("</li>");
                }                
                
            } else if(mainDeck.getVisibleWidget() == mainDeck.getWidgetIndex(highlightPanel)){   
                
                if((metadata != null && metadata.getDisplayName() == null) 
                        || !exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY)){
                    
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify a title for this question.")
                            .append("</li>");
                    
                    highlightTitleLabel.addStyleName(REQ_FORM_INPUT_LABEL_NOT_PROVIDED_STYLENAME);
                }else{
                    highlightTitleLabel.removeStyleName(REQ_FORM_INPUT_LABEL_NOT_PROVIDED_STYLENAME);
                }
                
                String html = highlightTargetTextEditor.getHtmlEditor().getCode();
                
                if(html == null || html.trim().isEmpty() || highlightTargetTextEditor.getHtmlEditor().isEmpty()){
                    
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the text to be highlighted.")
                            .append("</li>");
                }
                
                if(!exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.CORRECT_ANSWER)
                        || (exportQuestion.getText() != null 
                                && exportQuestion.getText().equals(exportQuestion.getProperties().getPropertyValue(
                                        SurveyPropertyKeyEnum.CORRECT_ANSWER
                            )
                    ))){
                    
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please highlight the ideal selection for your text.")
                            .append("</li>");
                    
                    highlightIdealSelectionText.addStyleName(REQ_FORM_INPUT_BORDER_NOT_PROVIDED_STYLENAME);
                }else{
                    highlightIdealSelectionText.removeStyleName(REQ_FORM_INPUT_BORDER_NOT_PROVIDED_STYLENAME);
                }
                
            } else if(mainDeck.getVisibleWidget() == mainDeck.getWidgetIndex(convTreePanel)){
                
                if(metadata != null && metadata.getDisplayName() == null){
                    
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify a title for conversation tree.")
                            .append("</li>");
                    
                    summaryTitleLabel.addStyleName(REQ_FORM_INPUT_LABEL_NOT_PROVIDED_STYLENAME);
                }else{
                    summaryTitleLabel.removeStyleName(REQ_FORM_INPUT_LABEL_NOT_PROVIDED_STYLENAME);
                }
                
                if(convTreeFile == null || StringUtils.isBlank(convTreeFile.getName())){
    
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the Conversation tree to be used.")
                            .append("</li>");
                }   
                
            } else if(mainDeck.getVisibleWidget() == mainDeck.getWidgetIndex(summarizePanel)){
                
                if((metadata != null && metadata.getDisplayName() == null) 
                        || !exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY)){
                    
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify a title for this question.")
                            .append("</li>");
                    
                    summaryTitleLabel.addStyleName(REQ_FORM_INPUT_LABEL_NOT_PROVIDED_STYLENAME);
                }else{
                    summaryTitleLabel.removeStyleName(REQ_FORM_INPUT_LABEL_NOT_PROVIDED_STYLENAME);
                }

                String html = summaryTargetTextEditor.getHtmlEditor().getCode();
                
                if(html == null || html.trim().isEmpty() || summaryTargetTextEditor.getHtmlEditor().isEmpty()){
                    
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the text to be summarized.")
                            .append("</li>");
                }

                if(!exportQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.CORRECT_ANSWER)){
                    
                    errorMsg.append("")
                            .append("<li>")
                            .append( "Please specify the ideal summary for your text.")
                            .append("</li>");
                }
                
            } else if(mainDeck.getVisibleWidget() == mainDeck.getWidgetIndex(mediaPanel)){
                errorMsg.append(mediaPanel.getValidationErrors());
                
            } else if(mainDeck.getVisibleWidget() == mainDeck.getWidgetIndex(taInteropEditor)){
                errorMsg.append(taInteropEditor.getValidationErrors());
            }
            
        } else {
            errorMsg.append(mediaPanel.getValidationErrors());
        }
        
        return errorMsg.toString();
    }
	
	/**
	 * Tells the server to lock the Training Application Reference at the given path.
	 * @param path Path of the TrainingApplicationReference file to lock.
	 */
    public void lockLessonMaterialReference() {
        
        if(metadata != null && metadata.getContent() instanceof generated.metadata.Metadata.LessonMaterial && 
                ((generated.metadata.Metadata.LessonMaterial)metadata.getContent()).getValue() != null){
    
            AsyncCallback<LockFileResult> callback =  new AsyncCallback<LockFileResult>() {
                @Override
                public void onFailure(Throwable t) {
                }
                @Override
                public void onSuccess(LockFileResult result) {
                }
            };
            
            String userName = GatClientUtility.getUserName();
            
            //Try to lock the file.
            LockLessonMaterialReference action = new LockLessonMaterialReference();
            action.setRelativePath(((generated.metadata.Metadata.LessonMaterial)metadata.getContent()).getValue());
            action.setUserName(userName);
            action.setBrowserSessionKey(GatClientUtility.getUserName());
            SharedResources.getInstance().getDispatchService().execute(action, callback);       
        }
    }
	
	/**
	 * Gets the Slide Show folder created when the user uploads a PowerPoint show or null if no folder was created.
	 * 
	 * @return The path to the slide show folder (e.g. Course/Slide Shows/myPpt). Can be null.
	 */
	public String getCreatedSlideShowFolder() {
		return mediaPanel.getCreatedSlideShowFolder();
	}
	
	/**
	 * Tells the server to unlock the training application reference file at the given path.
	 * @param path Path of the training application reference to unlock.
	 */
    public void unlockLessonMaterialReference() {
        
        if(metadata != null && metadata.getContent() instanceof generated.metadata.Metadata.LessonMaterial && 
                ((generated.metadata.Metadata.LessonMaterial)metadata.getContent()).getValue() != null){
        
            AsyncCallback<GatServiceResult> callback =  new AsyncCallback<GatServiceResult>() {
                @Override
                public void onFailure(Throwable t) {
                }
                @Override
                public void onSuccess(GatServiceResult result) {
                }
            };
            
            String userName = GatClientUtility.getUserName();
            String browserSessionKey = GatClientUtility.getBrowserSessionKey();
            
            //Try to lock the training application reference before we open it.
            UnlockLessonMaterialReference action = new UnlockLessonMaterialReference();
            action.setRelativePath(((generated.metadata.Metadata.LessonMaterial)metadata.getContent()).getValue());
            action.setUserName(userName);
            action.setBrowserSessionKey(browserSessionKey);
            SharedResources.getInstance().getDispatchService().execute(action, callback);
        }
    }
	
	public void setReadOnly(boolean readOnly){
		
		this.readOnly = readOnly;
		
		inputBlocker.setVisible(readOnly);	
	}
	
	 private native void closeFile(String path)/*-{
		
		if($wnd.parent != null){
			$wnd.parent.closeFile(path);
			
		}
		
	}-*/;
	
	   /* (non-Javadoc)
     * @see mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener#onPropertySetChange(mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet)
     */
    @Override
    public void onPropertySetChange(AbstractPropertySet propSet) {
        
        if(propSet != null){
            
            if(propSet.equals(highlightProperties.getObjectBeingEdited())){
                
                Serializable position = propSet.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY);
                
                if(position != null && position instanceof Integer){
                    
                    //if the image position for the highlight panel has changed, update the image's actual visual position accordingly
                    if((Integer)position == 1 
                            && highlightPanelInner.getWidgetIndex(highlightProperties) != highlightPanelInner.getWidgetIndex(highlightTargetTextEditor) - 1){
                        
                        highlightPanelInner.insert(highlightProperties, highlightPanelInner.getWidgetIndex(highlightTargetTextEditor));
                        
                    } else if((Integer)position == 0 
                            && highlightPanelInner.getWidgetIndex(highlightProperties) != highlightPanelInner.getWidgetIndex(highlightTargetTextEditor) + 1){
                        highlightPanelInner.insert(highlightProperties, highlightPanelInner.getWidgetIndex(highlightTargetTextEditor) + 1);
                    }
                    
                } else if(highlightPanelInner.getWidgetIndex(highlightProperties) != highlightPanelInner.getWidgetIndex(highlightTargetTextEditor) + 1){
                    highlightPanelInner.insert(highlightProperties, highlightPanelInner.getWidgetIndex(highlightTargetTextEditor));
                }
                
            } else if(propSet.equals(summaryProperties.getObjectBeingEdited())){
                
                Serializable position = propSet.getIntegerPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_POSITION_KEY);
                
                if(position != null && position instanceof Integer){
                    
                    //if the image position for the summary panel has changed, update the image's actual visual position accordingly
                    if((Integer)position == 1 
                            && summaryPanelInner.getWidgetIndex(summaryProperties) != summaryPanelInner.getWidgetIndex(summaryTargetTextEditor) - 1){
                        
                        summaryPanelInner.insert(summaryProperties, summaryPanelInner.getWidgetIndex(summaryTargetTextEditor));
                        
                    } else if((Integer)position == 0 
                            && summaryPanelInner.getWidgetIndex(summaryProperties) != summaryPanelInner.getWidgetIndex(summaryTargetTextEditor) + 1){
                        summaryPanelInner.insert(summaryProperties, summaryPanelInner.getWidgetIndex(summaryTargetTextEditor) + 1);
                    }
                    
                } else if(summaryPanelInner.getWidgetIndex(summaryProperties) != summaryPanelInner.getWidgetIndex(summaryTargetTextEditor) + 1){
                    summaryPanelInner.insert(summaryProperties, summaryPanelInner.getWidgetIndex(summaryTargetTextEditor));
                }
            }
        }
    }
    
    /**
     * Whether or not the user should be able to create content restricted to the Remediation phase
     * 
     * @param enabled whether to enable creating remediation content
     */
    public void setRemediationEnabled(boolean enabled){
        
        highlightThumbnail.setVisible(enabled);
        summarizeThumbnail.setVisible(enabled);
        convTreeThumbnail.setVisible(enabled);
        taInteropEditor.setRemediationEnabled(enabled);
        
        boolean needInsertAppChoices = false;
        
        if(enabled && trainingAppChoices == null) {
            
            //if remediation is enabled, populate the interactive choices based on the training app choices available
            trainingAppChoices = taInteropEditor.getTypeChoices();
            needInsertAppChoices = true;
        }
        
        //show interactive content types if remediation if it is enabled, or hide if it isn't
        if(trainingAppChoices != null) {
            
            for(Widget taChoice : trainingAppChoices) {
                
                if(needInsertAppChoices) {
                    taChoice.addStyleName(style.interactiveThumbnail());
                    ribbon.add(taChoice);
                }
                
                taChoice.setVisible(enabled);
            }
        }
    }

    /**
     * Gets the {@link ContentReferenceEditor}'s instance of the media panel.
     * 
     * @return the media panel
     */
    public MediaPanel getMediaPanel() {
        return mediaPanel;
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        
        //listen for when the user selects text in the document as long as this widget is loaded
        selectionHandlers.add(selectionHandler);
    }
    
    @Override
    protected void onUnload() {
        super.onUnload();
        
        //if this widget is unloaded, stop listening to user text selection
        selectionHandlers.remove(selectionHandler);
    }
    
    /**
     * Gets the training application that has been authored. Will always return null unless interactive content
     * is being authored.
     * 
     * @return the interactive training application content currently being authored. Will be null, if
     * this editor is not authoring interactive content.
     */
    public TrainingApplication getTrainingApp() {
        
        if(taInteropEditor.getCurrentTrainingApp() == null 
                || EMTPY_TRAINING_APPLICATION.equals(taInteropEditor.getCurrentTrainingApp())) {
            return null;
        }
        
        return taInteropEditor.getCurrentTrainingApp();
    }
    
    /**
     * Gets the interactive lesson material that has been authored (i.e. by the training app interop editor). Will
     * always return null unless interactive content is being authored.
     * 
     * @return the interactive lesson material content currently being authored. Will be null, if
     * this editor is not authoring interactive content.
     */
    public LessonMaterial getInteractiveLessonMaterial() {
        return taInteropEditor.getCurrentLessonMaterial();
    }
}