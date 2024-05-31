/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.OptGroup;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.constants.SelectedTextFormat;
import org.gwtbootstrap3.extras.tagsinput.client.event.ItemRemovedEvent;
import org.gwtbootstrap3.extras.tagsinput.client.event.ItemRemovedHandler;
import org.gwtbootstrap3.extras.tagsinput.client.ui.TagsInput;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;

import generated.course.ConceptQuestions;
import generated.course.ConceptQuestions.AssessmentRules;
import generated.course.LessonMaterialList;
import generated.metadata.Metadata;
import mil.arl.gift.common.TrainingAppCourseObjectWrapper;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.metadata.MetadataSearchResult.QuadrantResultSet;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.common.metadata.MetadataWrapper.ContentTypeEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.place.MetadataPlace;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider.CourseReadOnlyHandler;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance.OptionalGuidanceCreator;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.KnowledgeAssessmentSlider;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.PresentSurveyView.MandatoryBehaviorOptionChoice;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.TrainingAppInteropEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.NumericTextInputCell;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.SurveyPickerQuestionBank;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.DeleteMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.model.course.PracticeApplicationObject;

/**
 * The UI implementation for the Merrill's Branch Point editor
 * 
 * @author nroberts
 */
public class MbpViewImpl extends Composite implements MbpView, CourseReadOnlyHandler {

    private static MbpViewImplUiBinder uiBinder = GWT
            .create(MbpViewImplUiBinder.class);

    interface MbpViewImplUiBinder extends UiBinder<Widget, MbpViewImpl> {
    }
    
    private static final Logger logger = Logger.getLogger(MbpViewImpl.class.getName());
   
    /** The style to use for number boxes in this view */
    private static final String NUMBER_BOX_STYLE =  
			"text-align: right;"
			+ "max-width: 30px;"
			+ "margin: 0 auto;"
			+ "display: block;"
	;
    
    private static final String NO_RULE_CONTENT_MESSAGE = "No Rule phase content was found for the selected course concept(s).";
    private static final String NO_EXAMPLE_CONTENT_MESSAGE = "No Example phase content was found for the selected course concept(s).";
    private static final String NO_REMEDIATION_CONTENT_MESSAGE = "No Remediation phase content was found for the selected course concept(s).";
    
    
    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        String groupLabel();
        String optionsTableMandatoryInvisible();
        String mandatoryControlsRow();
    }
    
    /** A reference to the styles defined in the ui.xml */
    @UiField
    protected Style style;
    
    /** a label shown then there are no course concepts */
    @UiField
    protected Label noCourseConceptsLabel;
    
    /** contains the course concepts multipleselect component */
    @UiField
    protected SimplePanel conceptsSelectPanel;
    
    /** The multiple select dropdown for concepts */
    @UiField
    protected MultipleSelect conceptMultipleSelect;

    /** The panel that contains the concept tags */
    @UiField
    protected FlowPanel conceptsTagPanel;

    /** The concept tag input. Displays the collection of the selected concepts */
    @UiField
    protected TagsInput conceptsTagInput;
    
    /** used to block selected course concept removal on the TagsInput component */
    @UiField
    protected BlockerPanel conceptsTagPanelBlocker;
    
    /** a label shown then there are no course concepts */
    @UiField
    protected Label noPracticeCourseConceptsLabel;
    
    /** contains the course concepts multipleselect component */
    @UiField
    protected SimplePanel practiceConceptsSelectPanel;
    
    /** The multiple select dropdown for concepts */
    @UiField
    protected MultipleSelect practiceConceptMultipleSelect;

    /** The panel that contains the concept tags */
    @UiField
    protected FlowPanel practiceConceptsTagPanel;

    /** The concept tag input. Displays the collection of the selected concepts */
    @UiField
    protected TagsInput practiceConceptsTagInput;
    
    /** used to block selected course concept removal on the TagsInput component */
    @UiField
    protected BlockerPanel practiceConceptsTagPanelBlocker;
    
    @UiField(provided=true)
    protected CellTable<ConceptQuestions> questionCellTable = new CellTable<ConceptQuestions>();
    
    private HashMap<String, KnowledgeAssessmentSlider> conceptSliderMap = new HashMap<>();
    
    private HashMap<String, KnowledgeAssessmentSlider> extraneousConceptSliderMap = new HashMap<>();
    
    @UiField
    protected HTML rulePhaseLabel;
    
    @UiField(provided=true)
    protected Image addRuleContentButton = new Image(GatClientBundle.INSTANCE.add_image());
    
    @UiField(provided=true)
    protected Image addRemediationContentButton = new Image(GatClientBundle.INSTANCE.add_image());
    
    @UiField
    protected OptionalGuidanceCreator ruleGuidanceCreator;
    
    @UiField(provided=true)
    protected Image addExampleContentButton = new Image(GatClientBundle.INSTANCE.add_image());
    
    @UiField
    protected OptionalGuidanceCreator exampleGuidanceCreator;
    
    @UiField(provided=true)
    protected Image addApplicationButton = new Image(GatClientBundle.INSTANCE.add_image());
    
    @UiField
    protected CheckBox showPracticePanelCheckBox;
    
    @UiField
    protected DeckPanel practiceDeckPanel;
    
    @UiField
    protected SimplePanel noPracticePanel;
    
    @UiField 
    protected FlowPanel practicePanel;   
    
    @UiField
    protected OptionalGuidanceCreator recallGuidanceCreator;
    
    @UiField
    protected HTML ruleTransitionsWarning;
    
    @UiField
    protected HTML exampleTransitionsWarning;
    
    @UiField
    protected HTML recallTransitionsWarning;
    
    @UiField
    protected Widget ruleWarningPanel;
    
    @UiField
    protected Widget exampleWarningPanel;
    
    @UiField
    protected Widget recallWarningPanel;
    
    @UiField(provided = true)
    protected Image ruleWarningImage = new Image(GatClientBundle.INSTANCE.warn_image());
    
    @UiField(provided = true)
    protected Image exampleWarningImage = new Image(GatClientBundle.INSTANCE.warn_image());
    
    @UiField(provided = true)
    protected Image recallWarningImage = new Image(GatClientBundle.INSTANCE.warn_image());
    
    @UiField
    protected DeckPanel ruleShowFilesDeck;
    
    @UiField
    protected Widget ruleFilesPanel;
    
    @UiField
    protected BsLoadingIcon ruleFilesLoadingIcon;
    
    @UiField
    protected FlowPanel ruleFilesList;
    
    @UiField
    protected org.gwtbootstrap3.client.ui.Button ruleRefreshButton;
    
    @UiField
    protected Widget ruleFilesLoadingPanel;
    
    @UiField
    protected DeckPanel exampleShowFilesDeck;
    
    @UiField
    protected Widget exampleFilesPanel;
    
    @UiField
    protected BsLoadingIcon exampleFilesLoadingIcon;
    
    @UiField
    protected FlowPanel exampleFilesList;
    
    @UiField
    protected org.gwtbootstrap3.client.ui.Button exampleRefreshButton;
    
    @UiField
    protected Widget exampleFilesLoadingPanel;
    
    @UiField
    protected BsLoadingIcon remediationFilesLoadingIcon;
    
    @UiField
    protected FlowPanel remediationFilesList;
    
    @UiField
    protected org.gwtbootstrap3.client.ui.Button remediationRefreshButton;
    
    @UiField
    protected Widget remediationFilesLoadingPanel;
    
    @UiField
    protected CheckBox excludeRuleExampleContentCheckBox;
    
    
    @UiField
    protected FlowPanel sliderPanel;
    
    @UiField
    protected DeckPanel practiceShowApplicationsDeck;
    
    @UiField
    protected Widget practiceApplicationsPanel;
    
    @UiField
    protected BsLoadingIcon practiceApplicationsLoadingIcon;
    
    @UiField
    protected FlowPanel practiceApplicationsList;
    
    @UiField
    protected org.gwtbootstrap3.client.ui.Button practiceRefreshButton;
    
    @UiField
    protected Widget practiceApplicationsLoadingPanel;
    
    @UiField
    protected CourseObjectModal metadataObjectDialog;
    
    protected AddContentDialog addContentDialog = new AddContentDialog();
    
    protected AddRemediationDialog addRemediationDialog = new AddRemediationDialog();
    
    @UiField
    protected AddApplicationDialog addApplicationDialog;
    
    @UiField 
    protected SurveyPickerQuestionBank surveyPickerQuestionBank;
    
    @UiField
    protected CheckBox recallAllowedAttemptsCheckBox;
    
    @UiField
    protected FlowPanel recallAllowedAttemptsPanel;
    
    @UiField(provided = true)
    protected NumberSpinner recallAllowedAttempts = new NumberSpinner(1, 1, Integer.MAX_VALUE, 1);
    
    @UiField
    protected CheckBox practiceAllowedAttemptsCheckBox;
    
    @UiField
    protected FlowPanel practiceAllowedAttemptsPanel;
    
    @UiField(provided = true)
    protected NumberSpinner practiceAllowedAttempts = new NumberSpinner(1, 1, Integer.MAX_VALUE, 1);
    
    /** The disable checkbox. */
    @UiField
    protected CheckBox disabled;
    
    @UiField
    protected DisclosurePanel mbpOptionsPanel;
    
    @UiField
    protected HTMLPanel optionTablePanel;
    
    @UiField
    protected CheckBox mandatoryCheckBox;
    
    @UiField(provided = true)
    protected ValueListBox<MandatoryBehaviorOptionChoice> mandatoryBehaviorSelector = new ValueListBox<MandatoryBehaviorOptionChoice>(new Renderer<MandatoryBehaviorOptionChoice>() {

        @Override
        public String render(MandatoryBehaviorOptionChoice value) {
            if(value == null) {
                return String.valueOf(value);
            }
            
            switch(value) {
                case AFTER:
                    return "After";
                case ALWAYS:
                    return "Always";
                default:
                    return "Unknown: " + value.toString();
            }
        }

        @Override
        public void render(MandatoryBehaviorOptionChoice value, Appendable appendable) throws IOException {
            appendable.append(render(value));
        }
    });
    
    @UiField
    protected FlowPanel learnerStateShelfLifePanel;
    
    @UiField
    protected NumberSpinner learnerStateShelfLife;
    
    /** the complete collection of authored course concepts */
    private List<String> courseConcepts = new ArrayList<String>();
    
    /** the set of course concepts used for Rule/Example/Recall phases. Can be empty. */
    private List<String> checkOnLearningConcepts = new ArrayList<String>();
    
    // -- 'Concepts:' cell table column definitions ------------------------------------------------------------------------------------
    
    /**
     * An html string representation of an unchecked input box.
     */
    private static final SafeHtml INPUT_CHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled=\"disabled\"/>");

    private static final SafeHtml INPUT_UNCHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled=\"disabled\"/>");

    /** The checkbox column for the 'Concepts:' table */
    private Column<CandidateConcept, Boolean> conceptSelectionColumn = new Column<CandidateConcept, Boolean>(new CheckboxCell()){

		@Override
		public Boolean getValue(CandidateConcept candidate) {			
			return candidate.isChosen();
		}
		
        @Override
        public void render(Context context, CandidateConcept candidate, SafeHtmlBuilder sb){
           if (GatClientUtility.isReadOnly()){

               boolean checked = getValue(candidate);

               if (checked) {
                 sb.append(INPUT_CHECKED_DISABLED);
               } else if (!checked) {
                 sb.append(INPUT_UNCHECKED_DISABLED);
               }
           }else{
              super.render(context, candidate, sb);
           }
        }
    	
    };
    
    // -- 'Questions:' cell table column definitions ------------------------------------------------------------------------------------
    
    /** The 'Concept' column for the 'Questions:' table */
    private Column<ConceptQuestions, String> questionConceptColumn = new Column<ConceptQuestions, String>(new TextCell()){

		@Override
		public String getValue(ConceptQuestions question) {

			if(question.getName() != null){
				return question.getName();
			}
			
			return null;
		}
    	
    };
    
    /** The 'Number of Easy Questions' column for the 'Questions:' table */
    private Column<ConceptQuestions, String> easyColumn = new Column<ConceptQuestions, String>(new NumericTextInputCell(NUMBER_BOX_STYLE)){

		@Override
		public String getValue(ConceptQuestions question) {

			if(question.getQuestionTypes() != null && question.getQuestionTypes().getEasy() != null){
				return question.getQuestionTypes().getEasy().toString();
			}
			
			return null;
		}
		
        @Override
        public void render(Context context, ConceptQuestions question, SafeHtmlBuilder sb) {
            
            if(GatClientUtility.isReadOnly()){
                sb.appendHtmlConstant(getValue(question));
            }else{
                super.render(context, question, sb);
            }

         }
    	
    };
    
    /** The 'Number of Medium Questions' column for the 'Questions:' table */
    private Column<ConceptQuestions, String> mediumColumn = new Column<ConceptQuestions, String>(new NumericTextInputCell(NUMBER_BOX_STYLE)){

		@Override
		public String getValue(ConceptQuestions question) {

			if(question.getQuestionTypes() != null && question.getQuestionTypes().getMedium() != null){
				return question.getQuestionTypes().getMedium().toString();
			}
			
			return null;
		}
		
        @Override
        public void render(Context context, ConceptQuestions question, SafeHtmlBuilder sb) {
            
            if(GatClientUtility.isReadOnly()){
                sb.appendHtmlConstant(getValue(question));
            }else{
                super.render(context, question, sb);
            }

         }
    	
    };
    
    /** The 'Number of Hard Questions' column for the 'Questions:' table */
    private Column<ConceptQuestions, String> hardColumn = new Column<ConceptQuestions, String>(new NumericTextInputCell(NUMBER_BOX_STYLE)){

		@Override
		public String getValue(ConceptQuestions question) {

			if(question.getQuestionTypes() != null && question.getQuestionTypes().getHard() != null){
				return question.getQuestionTypes().getHard().toString();
			}
			
			return null;
		}
		
        @Override
        public void render(Context context, ConceptQuestions question, SafeHtmlBuilder sb) {
            
            if(GatClientUtility.isReadOnly()){
                sb.appendHtmlConstant(getValue(question));
            }else{
                super.render(context, question, sb);
            }

         }
    	
    };
    
    //the table used to show the list of practice applications
    @UiField
	protected CellTable<PracticeApplicationObject> practiceApplicationList;
    
    private Column<PracticeApplicationObject, String> practiceApplicationNameColumn = new Column<PracticeApplicationObject, String>(new TextCell(){
	    
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
            
            boolean invalidEntry = false;
            
            Object contextObj = context.getKey();
            if(contextObj instanceof PracticeApplicationObject){
                
                PracticeApplicationObject practiceAppObj = (PracticeApplicationObject)contextObj;
                if (practiceAppObj.getTrainingApplication() != null && practiceAppObj.getTrainingApplication().getValidationException() != null) {
                    invalidEntry = true;
                    
                }else{
                	
                	String displayName = "UNKNOWN";
                    
                	if(practiceAppObj.getTrainingApplication()!= null && practiceAppObj.getTrainingApplication().getTrainingApplicationObj() != null 
                			&& practiceAppObj.getTrainingApplication().getTrainingApplicationObj().getTransitionName() != null
                			&& !practiceAppObj.getTrainingApplication().getTrainingApplicationObj().getTransitionName().isEmpty()){
                		
                		//get the training application's display name
                		displayName = practiceAppObj.getTrainingApplication().getTrainingApplicationObj().getTransitionName();
                		 
                    } else if (practiceAppObj.getLessonMaterial() != null && practiceAppObj.getLessonMaterial().getMedia() != null
                            && !practiceAppObj.getLessonMaterial().getMedia().isEmpty()) {
                        // get the first media application's display name
                        displayName = practiceAppObj.getLessonMaterial().getMedia().get(0).getName();
                    } else if (practiceAppObj.getMetadataFilesMap() != null && !practiceAppObj.getMetadataFilesMap().isEmpty()) {
                		
                		//if no display name is assigned to the training app, use the name of one of the metadata objects referencing it
                		for(String metadataPath : practiceAppObj.getMetadataFilesMap().keySet()){
                			
                			if(displayName == null){
                				
                				//if no metadata objects have display names, default to the path to the first metadata found
                				displayName = metadataPath;
                			}
                			
                			Metadata metadata = practiceAppObj.getMetadataFilesMap().get(metadataPath);
                			
                			if(metadata.getDisplayName() != null && !metadata.getDisplayName().isEmpty()){
                				displayName = metadata.getDisplayName();
                				break;
                			}
                		}
                	}
                	
                	Label label = new Label(displayName);
                    
                    if(practiceAppObj.doesOnlyContainRequiredConcepts()){
                        label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                    }else{
                      //#3051 - hiding content not applicable to this course object
                        return;
//                        label.getElement().setTitle(OTHER_COURSE_CONCEPT_TEXT);
//                        label.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
//                        label.getElement().getStyle().setColor("gray");
                    }
                    
                    SafeHtml html = SafeHtmlUtils.fromTrustedString(label.toString());
                    sb.append(html);
                }
                
            }
            
            if(invalidEntry){
                //create custom label
                
                Label label = new Label(value);
                label.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
                label.getElement().getStyle().setColor("rgb(200,0,0)");
                
                SafeHtml html = SafeHtmlUtils.fromTrustedString(label.toString());
                sb.append(html);
            }else{
//                super.render(context, value, sb);
            }
        }
        
	}) {

		@Override
		public String getValue(PracticeApplicationObject courseObjectItem) {
			
            if (courseObjectItem != null) {
                if (courseObjectItem.getTrainingApplication() != null) {
			    
			    TrainingAppCourseObjectWrapper trainingAppObjectWrapper = courseObjectItem.getTrainingApplication();
			    if(trainingAppObjectWrapper.getValidationException() != null){
			        return trainingAppObjectWrapper.getInvalidObjectIdentifier();
			    }else{
			        
			        if(courseObjectItem.getMetadataFilesMap().size() == 1){
			            //see if there is a metadata display name field to use
			            generated.metadata.Metadata metadata = courseObjectItem.getMetadataFilesMap().values().iterator().next();
			            if(metadata.getDisplayName() != null){
			                return metadata.getDisplayName();
			            }
			            
			        }
			        
			        if(trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName() != null){
			            return trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName();
			        }
			    }
                } else if (courseObjectItem.getLessonMaterial() != null && courseObjectItem.getLessonMaterial().getMedia() != null
                        && !courseObjectItem.getLessonMaterial().getMedia().isEmpty()) {
                    // get the first media application's display name
                    return courseObjectItem.getLessonMaterial().getMedia().get(0).getName();
			}
            }

			return "UNKNOWN";
		}
		
	};
	
	private Column<PracticeApplicationObject, String> applicationErrorColumn = new Column<PracticeApplicationObject, String>(new ButtonCell(){
	        
	        @Override
	        public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {

	            Object contextObj = context.getKey();
	            if(contextObj instanceof PracticeApplicationObject){
	                
	                PracticeApplicationObject trainingAppObj = (PracticeApplicationObject)contextObj;
	                if(trainingAppObj.getTrainingApplication() != null && trainingAppObj.getTrainingApplication().getValidationException() != null){
	                    
	                    Icon icon = new Icon(IconType.EXCLAMATION_TRIANGLE);
	                    icon.setTitle("See validation error message");
	                    icon.getElement().getStyle().setCursor(Cursor.POINTER);
	                    icon.getElement().getStyle().setMargin(-10, Unit.PX);
	                    icon.getElement().getStyle().setFontSize(20, Unit.PX);
	                    icon.getElement().getStyle().setColor("rgb(200,0,0)");
	                    icon.getElement().getStyle().setProperty("textShadow", "2px 2px 3px rgba(0,0,0,0.25)");

	                    SafeHtml html = SafeHtmlUtils.fromTrustedString(icon.toString());
	                    sb.append(html);
	                }
	            }

	        }
	    }) {

	        @Override
	        public String getValue(PracticeApplicationObject courseObjectItem) {
	            return null;
	        }
	        
	    };
	
	protected Column<PracticeApplicationObject, String> applicationEditColumn = new Column<PracticeApplicationObject, String>(new ButtonCell(){
		
		@Override
	    public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
			
	        Image icon = new Image(GatClientBundle.INSTANCE.edit_image());
	        icon.setTitle("Edit this practice application");
	        icon.getElement().getStyle().setCursor(Cursor.POINTER);
	        icon.getElement().getStyle().setMargin(-10, Unit.PX);
	        
	        Object contextObj = context.getKey();
	        if(contextObj instanceof PracticeApplicationObject){
	            
                PracticeApplicationObject practiceAppObj = (PracticeApplicationObject) contextObj;
                if (practiceAppObj.getTrainingApplication() != null && practiceAppObj.getTrainingApplication().getValidationException() != null) {
	                icon.setTitle("Unable to edit this course object due to an error");
	                icon.getElement().getStyle().setProperty("opacity", "0.5");
	                icon.getElement().getStyle().setCursor(Cursor.DEFAULT);
	            }
	        }
			
	        SafeHtml html = SafeHtmlUtils.fromTrustedString(icon.toString());
	        sb.append(html);
	    }
	}) {

		@Override
		public String getValue(PracticeApplicationObject courseObjectItem) {
			return "";
		}
		
	};
	
	private Column<PracticeApplicationObject, String> applicationDeleteColumn = new Column<PracticeApplicationObject, String>(new ButtonCell(){
		
		@Override
	    public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
			
	        if (!GatClientUtility.isReadOnly()) {
	            Image icon = new Image(GatClientBundle.INSTANCE.cancel_image());

	            icon.setTitle("Delete this practice application");
	            
	            icon.getElement().getStyle().setCursor(Cursor.POINTER);
	            icon.getElement().getStyle().setMargin(-10, Unit.PX);
	            
	            SafeHtml html = SafeHtmlUtils.fromTrustedString(icon.toString());
	            sb.append(html);
	        }

	    }
	}) {

		@Override
		public String getValue(PracticeApplicationObject courseObjectItem) {
			return "";
		}
		
	};
	
	/**
	 * Populate the application type column with a graphic representation of the application.
	 */
   private Column<PracticeApplicationObject, String> applicationTypeColumn = new Column<PracticeApplicationObject, String>(new ButtonCell(){
        
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {            
                
            ContentTypeEnum contentType = null;
            Object contextObj = context.getKey();
            if(contextObj instanceof PracticeApplicationObject){
                
                PracticeApplicationObject practiceAppObj = (PracticeApplicationObject) contextObj;
                if(practiceAppObj.getTrainingApplication() != null) {
                    contentType = practiceAppObj.getTrainingApplication().getContentType();
                }
            }

            HTML html = new HTML(CourseElementUtil.getContentTypeGraphic(contentType).getString());
            html.getElement().getStyle().setMargin(-10, Unit.PX);
            html.setTitle(CourseElementUtil.getContentTypeTitle(contentType));
            
            SafeHtml safeHtml = SafeHtmlUtils.fromTrustedString(html.getHTML());
            sb.append(safeHtml);
        }
    }) {

        @Override
        public String getValue(PracticeApplicationObject courseObjectItem) {
            return "";
        }
        
    };
	
	/**
	 * Used to sort the practice applications table rows based on the name given to the metadata content
	 */
    Comparator<PracticeApplicationObject> practiceApplicationListSorter = new Comparator<PracticeApplicationObject>() {

        @Override
        public int compare(PracticeApplicationObject o1,
                PracticeApplicationObject o2) {
            
            String name1 = null;
            
            if (o1 != null && o1.getTrainingApplication() != null) {

                TrainingAppCourseObjectWrapper trainingAppObjectWrapper = o1.getTrainingApplication();
                if (trainingAppObjectWrapper.getValidationException() != null) {
                    name1 = trainingAppObjectWrapper.getInvalidObjectIdentifier();
                } else if (trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName() != null) {
                    name1 = trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName();
                }
            } else if (o1 != null && o1.getLessonMaterial() != null) {

                LessonMaterialList lmList = o1.getLessonMaterial();
                if (lmList.getMedia() != null && !lmList.getMedia().isEmpty()) {
                    name1 = lmList.getMedia().get(0).getName();
                }
            }
            
            String name2 = null;
            
            if (o2 != null && o2.getTrainingApplication() != null) {

                TrainingAppCourseObjectWrapper trainingAppObjectWrapper = o2.getTrainingApplication();
                if (trainingAppObjectWrapper.getValidationException() != null) {
                    name2 = trainingAppObjectWrapper.getInvalidObjectIdentifier();
                } else if (trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName() != null) {
                    name2 = trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName();
                }
            } else if (o2 != null && o2.getLessonMaterial() != null) {

                LessonMaterialList lmList = o2.getLessonMaterial();
                if (lmList.getMedia() != null && !lmList.getMedia().isEmpty()) {
                    name2 = lmList.getMedia().get(0).getName();
                }
            }
            
            if(name1 != null && name2 != null){
                return name1.compareTo(name2);
                
            } else if(name1 != null && name2 == null){
                return -1;
                
            } else if(name1 == null && name2 != null){
                return 1;
            }
            
            return 0;
        }
    };
	
	/** A data provider responsible for providing the library table with a list of training application objects to display */
	protected ListDataProvider<PracticeApplicationObject> practiceApplicationProvider = new ListDataProvider<PracticeApplicationObject>();

	/** A command used to refresh the metadata file panels */
	private Command refreshMetadataCommand = null;
    
    
    // -- Methods -----------------------------------------------------------------------------------------------------------------------

	/**
	 * constructor to build the view.
	 */
    public MbpViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
        CourseReadOnlyProvider.getInstance().addReadOnlyHandlerManaged(this);
        
        questionCellTable.setPageSize(Integer.MAX_VALUE);

        // hide the no course concepts label until the course concepts are checked
        noCourseConceptsLabel.setVisible(false);
        noPracticeCourseConceptsLabel.setVisible(false);      
        
        // attach the 'Questions:' cell table to its associated columns
        
        questionCellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        
        questionCellTable.addColumn(questionConceptColumn, "Concept");       
        questionCellTable.setColumnWidth(questionConceptColumn, "33%");
        
        questionCellTable.addColumn(easyColumn, new SafeHtmlHeader(new SafeHtmlBuilder().appendHtmlConstant(
        		"<div title='The number of easy-difficulty questions to show for each concept'>Easy</div>"
        	).toSafeHtml())
        );
        questionCellTable.setColumnWidth(easyColumn, "22%");
        
        questionCellTable.addColumn(mediumColumn, new SafeHtmlHeader(new SafeHtmlBuilder().appendHtmlConstant(
        		"<div title='The number of medium-difficulty questions to show for each concept'>Medium</div>"
        	).toSafeHtml())
        );
        questionCellTable.setColumnWidth(mediumColumn, "22%");
        
        questionCellTable.addColumn(hardColumn, new SafeHtmlHeader(new SafeHtmlBuilder().appendHtmlConstant(
        		"<div title='The number of hard-difficulty questions to show for each concept'>Hard</div>"
        	).toSafeHtml())
        );
        questionCellTable.setColumnWidth(hardColumn, "22%");
        
        FlowPanel emptyQuestionsWidget = new FlowPanel();
        
        emptyQuestionsWidget.add(new HTML("<br>"));
        
        Label emptyQuestionsLabel = new Label("Please select one or more concepts above to begin editing concept questions.");
        emptyQuestionsLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
        
        emptyQuestionsWidget.add(emptyQuestionsLabel);
        
        emptyQuestionsWidget.add(new HTML("<br>"));
        
        questionCellTable.setEmptyTableWidget(emptyQuestionsWidget);
        
        practiceDeckPanel.showWidget(practiceDeckPanel.getWidgetIndex(noPracticePanel));
        
        ruleShowFilesDeck.showWidget(ruleShowFilesDeck.getWidgetIndex(ruleFilesPanel));        
        
        exampleShowFilesDeck.showWidget(exampleShowFilesDeck.getWidgetIndex(exampleFilesPanel));      
        
        practiceShowApplicationsDeck.showWidget(practiceShowApplicationsDeck.getWidgetIndex(practiceApplicationsPanel));
        
        //
        // initialize the training application list
        //
        
        practiceApplicationList.addColumn(applicationEditColumn);
        practiceApplicationList.setColumnWidth(applicationEditColumn, "25px");
        
        practiceApplicationList.addColumn(applicationDeleteColumn);
        practiceApplicationList.setColumnWidth(applicationDeleteColumn, "25px");
        
        practiceApplicationList.addColumn(applicationTypeColumn);
        practiceApplicationList.setColumnWidth(applicationTypeColumn, "25px");
        
        practiceApplicationList.addColumn(practiceApplicationNameColumn, "Name");
		practiceApplicationList.setColumnWidth(practiceApplicationNameColumn, "75%");
		
	    practiceApplicationList.addColumn(applicationErrorColumn);
	    practiceApplicationList.setColumnWidth(applicationErrorColumn, "36px");
		
		FlowPanel emptyTableWidget = new FlowPanel();
		emptyTableWidget.setSize("100%", "100%");
		emptyTableWidget.addStyleName("libraryTableEmptyWidget");
		
		Label emptyLabel = new Label("No Practice phase content was found for the selected course concept(s).");
		emptyLabel.getElement().getStyle().setPadding(10, Unit.PX);
		emptyLabel.getElement().getStyle().setFontSize(16, Unit.PX);

		emptyTableWidget.add(emptyLabel);
		
		practiceApplicationList.setEmptyTableWidget(emptyTableWidget);
		
		practiceApplicationProvider.addDataDisplay(practiceApplicationList);
		practiceApplicationProvider.refresh();
		
	    applicationErrorColumn.setFieldUpdater(new FieldUpdater<PracticeApplicationObject, String>() {
	            
	            @Override
	            public void update(int index, PracticeApplicationObject object, String value) {
	                
	                TrainingAppCourseObjectWrapper trainingAppCourseObjectWrapper = object.getTrainingApplication();
                if (trainingAppCourseObjectWrapper != null && trainingAppCourseObjectWrapper.getValidationException() != null) {
	                 
	                    DetailedExceptionSerializedWrapper e = trainingAppCourseObjectWrapper.getValidationException();
                        
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(), e.getErrorStackTrace());
                        dialog.center();
	                }
	            }
	      });
		
		applicationEditColumn.setFieldUpdater(new FieldUpdater<PracticeApplicationObject, String>() {
			
			@Override
			public void update(int index, PracticeApplicationObject object, String value) {
			    
			    TrainingAppCourseObjectWrapper trainingAppCourseObjectWrapper = object.getTrainingApplication();
			    if(trainingAppCourseObjectWrapper == null || trainingAppCourseObjectWrapper.getValidationException() == null){
				
			    	if(object.getMetadataFilesMap() != null && object.getMetadataFilesMap().size() == 1){
			    		
			    		String metadataFilePath = object.getMetadataFilesMap().keySet().iterator().next();
			    		String baseFolder = GatClientUtility.getBaseCourseFolderPath();
			    		
			    		int subIndex = metadataFilePath.indexOf(baseFolder) + baseFolder.length();
			    		 
			    		HashMap<String, String> paramMap = new HashMap<String, String>();
			    		paramMap.put(MetadataPlace.PARAM_READONLY, Boolean.toString(GatClientUtility.isReadOnly()));
			    		
						String url = GatClientUtility.getModalDialogUrlWithParams(
								metadataFilePath.substring(0, subIndex), 
								metadataFilePath.substring(subIndex + 1),
								paramMap
						);
			    		
						metadataObjectDialog.setSaveAndCloseButtonVisible(!GatClientUtility.isReadOnly());
				    	metadataObjectDialog.setCourseObjectUrl(CourseObjectName.METADATA.getDisplayName() + (GatClientUtility.isReadOnly() ? " (Read Only)" : ""), url);
				    	metadataObjectDialog.setSaveButtonHandler(new ClickHandler() {
	
							@Override
							public void onClick(ClickEvent event) {
								GatClientUtility.saveEmbeddedCourseObject();
								metadataObjectDialog.stopEditor();
								
								if(!GatClientUtility.isReadOnly()){
                                    GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this metadata being edited
                                }
							}
							
						});
				    	
				    	metadataObjectDialog.show();
				    	
			    	} else {
			    		
			    		String name = null;
			    		
                        if (trainingAppCourseObjectWrapper != null) {
                            if (trainingAppCourseObjectWrapper.getValidationException() != null) {
                                name = trainingAppCourseObjectWrapper.getInvalidObjectIdentifier();
                            } else if (trainingAppCourseObjectWrapper.getTrainingApplicationObj().getTransitionName() != null) {
                                name = trainingAppCourseObjectWrapper.getTrainingApplicationObj().getTransitionName();
					    }
                        } else if (object.getLessonMaterial() != null) {
                            LessonMaterialList lmList = object.getLessonMaterial();
                            if (lmList.getMedia() != null && !lmList.getMedia().isEmpty()) {
                                name = lmList.getMedia().get(0).getName();
                            }
                        }
			    		
			    		MetadataFileReferenceListDialog dialog = new MetadataFileReferenceListDialog(name, object.getMetadataFilesMap().keySet());
			    		dialog.center();
			    	}
			    }
			}
		});	
		
		// hide the tags panel (and the tags input component) until there are selected course concepts
		// for this adaptive courseflow course object
		conceptsTagPanel.setVisible(false);
		practiceConceptsTagPanel.setVisible(false);
		
		// block click events to prevent removing selected practice course concepts
		// this is to prevent removing inherited check on learning concepts from the pratice phase
        practiceConceptsTagPanelBlocker.block();  

		
		conceptMultipleSelect.setPlaceholder("Course concepts");
		conceptMultipleSelect.setSelectedTextFormat(SelectedTextFormat.STATIC);
		conceptMultipleSelect.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            
            @Override
            public void onValueChange(final ValueChangeEvent<List<String>> event) {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("conceptMultipleSelect.onValueChange(" + event.getValue() + ")");
                }

                // need to delay the refreshing of the tags input until after the ScheduledCommand               
                // in TagsInput.reconfigure() is finished.  'reconfigure' is called in this class 
                // when populating the TagsInput with the set of possible elements.  Not doing this
                // will result in the TagsInput having zero selected elements when initializing this widget
                // and there are already selected course concepts for this adaptive courseflow course object.
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        conceptsTagInput.removeAll();
                        conceptsTagInput.add(event.getValue());
                        conceptsTagInput.refresh();                        
                    }
                });

                logger.info("Setting conceptsTagPanel visibility to "+!event.getValue().isEmpty());
                conceptsTagPanel.setVisible(!event.getValue().isEmpty());
            }
        });
		
		conceptsTagInput.addItemRemovedHandler(new ItemRemovedHandler<String>() {
            @Override
            public void onItemRemoved(ItemRemovedEvent<String> event) {
                List<String> selectedItems = new ArrayList<String>();
                for (Option option : conceptMultipleSelect.getSelectedItems()) {
                    // get all selected except the one that was removed
                    if (!StringUtils.equals(option.getText(), event.getItem())) {
                        selectedItems.add(option.getValue());
                    }
                }

                // update multiple select with the updated selected items list
                logger.info("Setting conceptMultipleSelect items to "+selectedItems);
                conceptMultipleSelect.setValue(selectedItems, true);
            }
        });
		
      practiceConceptMultipleSelect.setPlaceholder("Course concepts");
      practiceConceptMultipleSelect.setSelectedTextFormat(SelectedTextFormat.STATIC);
      practiceConceptMultipleSelect.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
          
          @Override
          public void onValueChange(final ValueChangeEvent<List<String>> event) {

              if (logger.isLoggable(Level.FINE)) {
                  logger.fine("practiceConceptMultipleSelect.onValueChange(" + event.getValue() + ")");
              }
              
              // Concepts that are disabled don't seem to be returned by event.getValue() so use a different approach
              // to determine which concepts are selected.
              final List<String> selectedConceptNames = new ArrayList<>();
              for(Option option : practiceConceptMultipleSelect.getSelectedItems()) {
                  selectedConceptNames.add(option.getValue());
              }

              // need to delay the refreshing of the tags input until after the ScheduledCommand               
              // in TagsInput.reconfigure() is finished.  'reconfigure' is called in this class 
              // when populating the TagsInput with the set of possible elements.  Not doing this
              // will result in the TagsInput having zero selected elements when initializing this widget
              // and there are already selected course concepts for this adaptive courseflow course object.
              Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                  
                  @Override
                  public void execute() {
                      practiceConceptsTagInput.removeAll();
                      practiceConceptsTagInput.add(selectedConceptNames);
                      practiceConceptsTagInput.refresh();                        
                  }
              });

              logger.info("Setting practiceConceptsTagPanel visibility to "+!event.getValue().isEmpty());
              practiceConceptsTagPanel.setVisible(practiceConceptMultipleSelect.getSelectedItems() != null 
                      && !practiceConceptMultipleSelect.getSelectedItems().isEmpty());
          }
      });
      
      mandatoryBehaviorSelector.setValue(MandatoryBehaviorOptionChoice.ALWAYS);
      mandatoryBehaviorSelector.setAcceptableValues(Arrays.asList(MandatoryBehaviorOptionChoice.ALWAYS, MandatoryBehaviorOptionChoice.AFTER));

      setMandatoryControlsVisibility(false);  // #4961 - partially implemented, for now default to ask learner if they want to take the 
                                              // adaptive courseflow again if they have expert course concept knowledge
      mandatoryBehaviorSelector.setValue(MandatoryBehaviorOptionChoice.ALWAYS);
      mandatoryBehaviorSelector.setAcceptableValues(Arrays.asList(MandatoryBehaviorOptionChoice.ALWAYS, MandatoryBehaviorOptionChoice.AFTER));

    }
    
    @Override
    public HasValue<List<String>> getCourseConceptsSelectedConcepts(){
        return conceptMultipleSelect;
    }
    
    @Override
    public HasValue<List<String>> getCourseConceptsSelectedPracticeConcepts(){
        return practiceConceptMultipleSelect;
    }
    
    @Override
    public HasVisibility getSelectedConceptsPanel(){
        return conceptsSelectPanel;
    }
    
    @Override
    public BlockerPanel getSelectedConceptsTagBlockerPanel(){
        return conceptsTagPanelBlocker;
    }
    
    @Override
    public void setCourseConcepts(List<String> courseConcepts, List<String> checkOnLearningConcepts, List<String> practiceConcepts){
        
        if(courseConcepts == null){
            throw new IllegalArgumentException("The course concepts can be empty but not null");
        }else if(checkOnLearningConcepts == null){
            throw new IllegalArgumentException("The check on learner concepts can be empty but not null");
        }else if(practiceConcepts == null){
            throw new IllegalArgumentException("The practice concepts can be empty but not null");
        }
        
        noCourseConceptsLabel.setVisible(courseConcepts.isEmpty());
        noPracticeCourseConceptsLabel.setVisible(courseConcepts.isEmpty());
        
        logger.info("adding the following course concepts to choose from = "+courseConcepts+", check on learning concepts = "+checkOnLearningConcepts+", practice concepts = "+practiceConcepts);
            
        this.courseConcepts.clear();
        this.courseConcepts.addAll(courseConcepts);
        
        this.checkOnLearningConcepts.clear();
        this.checkOnLearningConcepts.addAll(checkOnLearningConcepts);
        
        practiceConceptMultipleSelect.clear();
        conceptMultipleSelect.clear();
        
        OptGroup inheritedConceptsOptionGroup = new OptGroup();
        inheritedConceptsOptionGroup.setStyleName(style.groupLabel());
        inheritedConceptsOptionGroup.setLabel("check on learning concepts");
        OptGroup otherConceptsOptionGroup = new OptGroup();
        otherConceptsOptionGroup.setStyleName(style.groupLabel());
        otherConceptsOptionGroup.setLabel("other course concepts");

        for (String conceptName : courseConcepts) {
            
            if(checkOnLearningConcepts.contains(conceptName)){
                
                Option practiceInheritedConceptOption = new Option();
                practiceInheritedConceptOption.setText(conceptName);
                practiceInheritedConceptOption.setTitle("Practice must include concepts covered (above)");
                practiceInheritedConceptOption.setColor("black");
                practiceInheritedConceptOption.setValue(conceptName);
                practiceInheritedConceptOption.setEnabled(false);  //don't allow the user to deselect
                inheritedConceptsOptionGroup.add(practiceInheritedConceptOption);
                
            }else{
                //add all non-selected check on learner concepts to 'other' group
                
                Option practiceOtherConceptOption = new Option();
                practiceOtherConceptOption.setText(conceptName);
                practiceOtherConceptOption.setColor("black");
                practiceOtherConceptOption.setValue(conceptName);
                practiceOtherConceptOption.setEnabled(true);
                otherConceptsOptionGroup.add(practiceOtherConceptOption);
            }
            
            //
            // add all course concepts as options
            //
            Option option = new Option();
            option.setText(conceptName);
            option.setValue(conceptName);
            conceptMultipleSelect.add(option);

        }
        
        practiceConceptMultipleSelect.add(inheritedConceptsOptionGroup);
        practiceConceptMultipleSelect.add(otherConceptsOptionGroup);
        
        conceptsTagInput.removeAll();
        conceptsTagInput.add(checkOnLearningConcepts);
        
        conceptMultipleSelect.setValue(checkOnLearningConcepts, true);
        conceptMultipleSelect.refresh();
        
        practiceConceptsTagInput.removeAll();
        practiceConceptsTagInput.add(practiceConcepts);
        
        practiceConceptMultipleSelect.setValue(practiceConcepts, true);
        practiceConceptMultipleSelect.refresh();
        
        conceptsTagPanel.setVisible(!checkOnLearningConcepts.isEmpty());
        practiceConceptsTagPanel.setVisible(practiceConceptMultipleSelect.getSelectedItems() != null 
                && !practiceConceptMultipleSelect.getSelectedItems().isEmpty());
    }
    
    @Override
    public void updatePracticeConcepts(List<String> checkOnLearningConcepts){
        
        OptGroup inheritedConceptsOptionGroup = new OptGroup();
        inheritedConceptsOptionGroup.setStyleName(style.groupLabel());
        inheritedConceptsOptionGroup.setLabel("check on learning concepts");
        OptGroup otherConceptsOptionGroup = new OptGroup();
        otherConceptsOptionGroup.setStyleName(style.groupLabel());
        otherConceptsOptionGroup.setLabel("other course concepts");
        
        List<String> previousPracticeConceptsSelected = new ArrayList<String>();
        List<String> newPracticeConceptsSelected = new ArrayList<String>();
        for(Option practiceOption : practiceConceptMultipleSelect.getSelectedItems()){            
            previousPracticeConceptsSelected.add(practiceOption.getValue());
        }        
        
        logger.info("updating the following check on learning course concepts to "+checkOnLearningConcepts+
                "\ncurrent check on learning concepts: "+this.checkOnLearningConcepts+"\ncurrent pratice concepts: "+previousPracticeConceptsSelected+
                "\ncourse concepts: "+courseConcepts);

        for (String conceptName : this.courseConcepts) {
            
            if(checkOnLearningConcepts.contains(conceptName)){
                // currently selected as a check on learner concept, must be added to practice
                
                Option practiceInheritedConceptOption = new Option();
                practiceInheritedConceptOption.setText(conceptName);
                practiceInheritedConceptOption.setTitle("Practice must include concepts covered (above)");
                practiceInheritedConceptOption.setColor("black");
                practiceInheritedConceptOption.setValue(conceptName);
                practiceInheritedConceptOption.setEnabled(false);  //don't allow the user to deselect
                inheritedConceptsOptionGroup.add(practiceInheritedConceptOption);
                
                newPracticeConceptsSelected.add(conceptName);
                
            }else{
                //add all non-selected check on learner concepts to 'other' group
                Option practiceOtherConceptOption = new Option();
                practiceOtherConceptOption.setText(conceptName);
                practiceOtherConceptOption.setColor("black");
                practiceOtherConceptOption.setValue(conceptName);
                practiceOtherConceptOption.setEnabled(true);
                otherConceptsOptionGroup.add(practiceOtherConceptOption);  
                
                logger.info("Concept "+conceptName+" is in the practice other concepts group");
                
                if(previousPracticeConceptsSelected.contains(conceptName)){            
                    // the concept is currently checked for practice but not a current check on learning concept
                    
                    if(!this.checkOnLearningConcepts.contains(conceptName)){
                        // the previous check on learner concepts doesn't have this concept (and nor does the 
                        // current check on learner concept), therefore its a concept that was selected in 
                        // the other concept group prior to changing the check on learner concepts and should
                        // remain checked
                        newPracticeConceptsSelected.add(conceptName);
                    }   

                }
            }

        }
        
        this.checkOnLearningConcepts.clear();
        this.checkOnLearningConcepts.addAll(checkOnLearningConcepts);
        
        practiceConceptMultipleSelect.clear();
        practiceConceptMultipleSelect.add(inheritedConceptsOptionGroup);
        practiceConceptMultipleSelect.add(otherConceptsOptionGroup);
        
        practiceConceptsTagInput.removeAll();
        practiceConceptsTagInput.add(newPracticeConceptsSelected);
        
        practiceConceptMultipleSelect.setValue(newPracticeConceptsSelected, true);
        practiceConceptMultipleSelect.refresh();

        practiceConceptsTagPanel.setVisible(practiceConceptMultipleSelect.getSelectedItems() != null 
                && !practiceConceptMultipleSelect.getSelectedItems().isEmpty());
    }
    
    @Override
    public HasVisibility getSelectedPracticeConceptsPanel(){
        return practiceConceptsSelectPanel;
    }
    
    @Override
    public HasData<ConceptQuestions> getQuestionCellTable(){
    	return questionCellTable;
    }
    
    @Override
    public void setConceptSelectionColumnFieldUpdater(FieldUpdater<CandidateConcept, Boolean> updater){
    	conceptSelectionColumn.setFieldUpdater(updater);
    }
    
    @Override
    public void setEasyColumnFieldUpdater(FieldUpdater<ConceptQuestions, String> updater){
    	easyColumn.setFieldUpdater(updater);
    }
    
    @Override
    public void setMediumColumnFieldUpdater(FieldUpdater<ConceptQuestions, String> updater){
    	mediumColumn.setFieldUpdater(updater);
    }
    
    @Override
    public void setHardColumnFieldUpdater(FieldUpdater<ConceptQuestions, String> updater){
    	hardColumn.setFieldUpdater(updater);
    }
    
    @Override
    public KnowledgeAssessmentSlider appendSlider(String conceptName) {
    	if(extraneousConceptSliderMap.containsKey(conceptName)) {
    		KnowledgeAssessmentSlider slider = extraneousConceptSliderMap.get(conceptName);
    		slider.setExtraneous(false);
    		conceptSliderMap.put(conceptName, slider);
    		return slider;
    		
    	} else if(!conceptSliderMap.containsKey(conceptName)) {
	    	KnowledgeAssessmentSlider slider = new KnowledgeAssessmentSlider(conceptName);
	    	conceptSliderMap.put(conceptName, slider);
	    	sliderPanel.add(slider);
	    	return slider;
    	} else {
    		return null;
    }
    }
    
    @Override
    public void appendExtraneousSlider(final String conceptName, int totalQuestions, AssessmentRules assessmentRules, final ScheduledCommand removeCmd) {
    	if(!extraneousConceptSliderMap.containsKey(conceptName)) {
	    	KnowledgeAssessmentSlider slider = new KnowledgeAssessmentSlider(conceptName);
	    	extraneousConceptSliderMap.put(conceptName, slider);
	    	slider.setRange(totalQuestions, assessmentRules);
	    	slider.setExtraneous(true);
	    	slider.setRemoveHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					KnowledgeAssessmentSlider slider = extraneousConceptSliderMap.remove(conceptName);
					sliderPanel.remove(slider);
					removeCmd.execute();
				}
	    	});
	    	sliderPanel.add(slider);
    	}
    }
    
    
    @Override
    public void removeSlider(String conceptName) {
    	if(conceptSliderMap.containsKey(conceptName)) {
    		sliderPanel.remove(conceptSliderMap.get(conceptName));
    		conceptSliderMap.remove(conceptName);
    }
    }
    
    @Override
    public KnowledgeAssessmentSlider updateSlider(String conceptName, int totalQuestions, AssessmentRules assessmentRules) {
    	if(conceptSliderMap.containsKey(conceptName)) {
    		KnowledgeAssessmentSlider slider = conceptSliderMap.get(conceptName);
    		slider.setRange(totalQuestions, assessmentRules);
    		return slider;
    	}
    	return null;
    }
    
    @Override
    public void setPracticeApplicationsPanelLoading(boolean loadingContentList){
        
        logger.info("Practice applications panel loading = "+loadingContentList);
        
        if(loadingContentList){
            practiceApplicationProvider.getList().clear();
            practiceApplicationsLoadingIcon.startLoading();
        }else{
            practiceApplicationsLoadingIcon.stopLoading();
        }
           
        practiceApplicationsLoadingPanel.setVisible(loadingContentList);        
    }
    
    @Override
    public void setPracticePanelVisible(boolean visible) {
        
        logger.info("Showing practice panel = "+visible);

        //show or hide the panel based on whether the 'add practice phase' checkbox is checked or not
    	if(visible) {
    		practiceDeckPanel.showWidget(practiceDeckPanel.getWidgetIndex(practicePanel));
    	} else {
    		practiceDeckPanel.showWidget(practiceDeckPanel.getWidgetIndex(noPracticePanel));
    	}
    }
    
    @Override
    public void redrawQuestionsCellTable(){
    	questionCellTable.redraw();
    }
    
    @Override
    public void refreshSliderPanel(List<CandidateConcept> conceptList) {
    	List<String> currentConcepts = new ArrayList<String>();
    	currentConcepts.addAll(conceptSliderMap.keySet());
    	
    	int index = 0;
    	for(CandidateConcept concept : conceptList) {
    		currentConcepts.remove(concept.getConceptName());
    		if(conceptSliderMap.containsKey(concept.getConceptName())) {
    			// Move the widget to the correct index
    			sliderPanel.insert(conceptSliderMap.get(concept.getConceptName()), index);
    			index += 1;
    		}
    	}
    	
    	for(String concept : currentConcepts) {
    		removeSlider(concept);
    	}
    }
    
    @Override
    public void undoQuestionsTableChanges(ConceptQuestions question){
    	((NumericTextInputCell) easyColumn.getCell()).clearViewData(question);
    	((NumericTextInputCell) mediumColumn.getCell()).clearViewData(question);
    	((NumericTextInputCell) hardColumn.getCell()).clearViewData(question);
    }
    
    @Override
    public HasClickHandlers getAddRuleContentButton(){
    	return addRuleContentButton;
    }
    
    @Override
    public HasVisibility getAddRuleContentButtonHasVisibility(){
        return addRuleContentButton;
    }
    
    @Override
    public HasClickHandlers getAddExampleContentButton(){
    	return addExampleContentButton;
    }
    
    @Override
    public HasVisibility getAddExampleContentButtonHasVisibility(){
        return addExampleContentButton;
    }
    
    @Override
    public HasClickHandlers getAddRemediationContentButton(){
    	return addRemediationContentButton;
    }
    
    @Override
    public HasVisibility getAddRemediationContentButtonHasVisibility(){
        return addRemediationContentButton;
    }
    
    @Override
    public HasClickHandlers getAddApplicationButton(){
    	return addApplicationButton;
    }
    
    @Override
    public HasVisibility getAddApplicationButtonHasVisibility(){
        return addApplicationButton;
    }
    
    @Override
    public void showAddContentDialog(){
    	addContentDialog.center();
    }
    
    @Override
    public void hideAddContentDialog(){
    	addContentDialog.hide();
    }
    
    @Override
    public HasData<CandidateConcept> getContentConceptsTable(){
    	return addContentDialog.getConceptsTable();
    }
    
    @Override
    public void setContentConceptSelectionColumnFieldUpdater(FieldUpdater<CandidateConcept, Boolean> updater){
    	addContentDialog.setConceptSelectionColumnFieldUpdater(updater);
    }
    
    @Override
    public HasData<CandidateMetadataAttribute> getContentAttributesTable(){
    	return addContentDialog.getAttributesTable();
    }
    
    @Override
    public HasData<CandidateMetadataAttribute> getRemediationAttributesTable(){
    	return addRemediationDialog.getAttributesTable();
    }
    
    @Override
    public void setContentAttributeSelectionColumnFieldUpdater(FieldUpdater<CandidateMetadataAttribute, Boolean> updater){
    	addContentDialog.setAttributeSelectionColumnFieldUpdater(updater);
    }
    
    @Override
    public void setRemediationAttributeSelectionColumnFieldUpdater(FieldUpdater<CandidateMetadataAttribute, Boolean> updater){
    	addRemediationDialog.setAttributeSelectionColumnFieldUpdater(updater);
    }
    
    @Override
    public void setContentDialogTitle(String text){
    	addContentDialog.setText(text);
    }
    
    @Override
    public void showAddRemediationDialog(){
    	addRemediationDialog.center();
    }
    
    @Override
    public void hideAddRemediationDialog(){
    	addRemediationDialog.hide();
    }
    
    @Override
    public HasData<CandidateConcept> getRemediationConceptsTable(){
    	return addRemediationDialog.getConceptsTable();
    }
    
    @Override
    public void setRemediationConceptSelectionColumnFieldUpdater(FieldUpdater<CandidateConcept, Boolean> updater){
    	addRemediationDialog.setConceptSelectionColumnFieldUpdater(updater);
    }
    
    @Override 
    public HasValue<Boolean> getExcludeRuleExampleContentCheckBox() {
        return excludeRuleExampleContentCheckBox;
    }
    
    @Override
    public HasEnabled getExcludeRuleExampleContentCheckBoxHasEnabled(){
        return excludeRuleExampleContentCheckBox;
    }
    
    @Override 
    public HasValue<Boolean> getShowPracticePanelCheckBox() {
    	return showPracticePanelCheckBox;
    }
    
    @Override
    public HasEnabled getShowPracticePanelCheckboxHasEnabled(){
        return showPracticePanelCheckBox;
    }
    
    @Override
    public void showAddApplicationDialog(){
    	addApplicationDialog.center();
    }
    
    @Override
    public void hideAddApplicationDialog(){
    	addApplicationDialog.hide();
    }
    
    @Override
    public TrainingAppInteropEditor getAddApplicationInteropEditor() {
        return addApplicationDialog.getTAInteropEditor();
    }

    @Override
    public HasData<CandidateConcept> getApplicationConceptsTable(){
    	return addApplicationDialog.getConceptsTable();
    }
    
    @Override
    public void setApplicationConceptSelectionColumnFieldUpdater(FieldUpdater<CandidateConcept, Boolean> updater){
    	addApplicationDialog.setConceptSelectionColumnFieldUpdater(updater);
    }
    
    @Override
    public HasData<CandidateMetadataAttribute> getApplicationAttributesTable(){
    	return addApplicationDialog.getAttributesTable();
    }
    
    @Override
    public void setApplicationAttributeSelectionColumnFieldUpdater(FieldUpdater<CandidateMetadataAttribute, Boolean> updater){
    	addApplicationDialog.setAttributeSelectionColumnFieldUpdater(updater);
    }
    
    @Override
    public void setApplicationDialogTitle(String text){
    	addApplicationDialog.setText(text);
    }
    
    @Override
    public HasClickHandlers getApplicationAddButton(){
    	return addApplicationDialog.getAddButton();
    }
    
    @Override
    public HasEnabled getApplicationAddButtonEnabled(){
    	return addApplicationDialog.getAddButtonEnabled();
    }
    
    @Override
    public HasValue<Boolean> getShowRecallAllowedAttemptsCheckBox(){
        return recallAllowedAttemptsCheckBox;
    }
    
    @Override
    public HasEnabled getShowRecallAllowedAttemptsCheckBoxHasEnabled(){
        return recallAllowedAttemptsCheckBox;
    }
    
    @Override
    public HasVisibility getRecallAllowedAttemptsPanel(){
        return recallAllowedAttemptsPanel;
    }
    
    @Override
    public HasValue<Integer> getRecallAllowedAttemptsSpinner(){
        return recallAllowedAttempts;
    }
    
    @Override
    public HasEnabled getRecallAllowedAttemptsSpinnerHasEnabled(){
        return recallAllowedAttempts;
    }
    
    @Override 
    public HasValue<Boolean> getShowPracticeAllowedAttemptsCheckBox(){
        return practiceAllowedAttemptsCheckBox;
    }
    
    @Override
    public HasEnabled getShowPracticeAllowedAttemptsCheckBoxHasEnabled(){
        return practiceAllowedAttemptsCheckBox;
    }
    
    @Override
    public HasVisibility getPracticeAllowedAttemptsPanel(){
        return practiceAllowedAttemptsPanel;
    }
    
    @Override
    public HasValue<Integer> getPracticeAllowedAttemptsSpinner(){
        return practiceAllowedAttempts;
    }
    
    @Override
    public HasEnabled getPracticeAllowedAttemptsSpinnerHasEnabled(){
        return practiceAllowedAttempts;
    }
    
    @Override
    public HasClickHandlers getContentAddButton(){
		return addContentDialog.getAddButton();
	}
    
    @Override
    public HasEnabled getContentAddButtonEnabled(){
		return addContentDialog.getAddButtonEnabled();
	}
    
    @Override
    public HasHTML getContentValidationErrorText(){
		return addContentDialog.getValidationErrorText();
	}
    
    @Override
    public HasClickHandlers getRemediationAddButton(){
		return addRemediationDialog.getAddButton();
	}
    
    @Override
    public HasEnabled getRemediationAddButtonEnabled(){
		return addRemediationDialog.getAddButtonEnabled();
	}
    
    @Override
    public HasHTML getRemediationValidationErrorText(){
		return addRemediationDialog.getValidationErrorText();
	}
    
    @Override
    public HasHTML getApplicationValidationErrorText(){
		return addApplicationDialog.getValidationErrorText();
	}
    
    @Override
    public HasHTML getRuleTransitionsWarning(){
    	return ruleTransitionsWarning;
    }
    
    @Override
    public HasHTML getExampleTransitionsWarning(){
    	return exampleTransitionsWarning;
    }
    
    @Override
    public HasHTML getRecallTransitionsWarning(){
    	return recallTransitionsWarning;
    }
	
	@Override
	public void showRuleWarning(boolean visible){
		ruleWarningPanel.setVisible(visible);
	}
	
	@Override
	public void showExampleWarning(boolean visible){
		exampleWarningPanel.setVisible(visible);
	}
	
	@Override
	public void showRecallWarning(boolean visible){
		recallWarningPanel.setVisible(visible);
	}
	
	@Override
	public void setRuleFiles(QuadrantResultSet metadataFiles){
		
		ruleFilesList.clear();
		
		if(metadataFiles != null){
		
			if(metadataFiles.getMetadataRefs() != null && !metadataFiles.getMetadataRefs().isEmpty()){
				
				for(final MetadataWrapper metadataWrapper : metadataFiles.getMetadataRefs().values()){
				    
				    if(metadataWrapper.hasExtraneousConcept()){
				        continue;
				    }
				    
				    Command deleteCommand = null;
				    if(!metadataWrapper.hasExtraneousConcept()){
				        deleteCommand = new Command() {
	                        
	                        @Override
	                        public void execute() {
	                            
	                            if (!GatClientUtility.isReadOnly()) {
	                                deleteMetadata(metadataWrapper.getMetadataFileName(), metadataWrapper.getDisplayName(), MerrillQuadrantEnum.RULE);
	                            }
	                            
	                        }
	                    };
				    }
					try{
					    ContentFileWidget contentWidget = new ContentFileWidget(metadataWrapper, metadataObjectDialog, deleteCommand);
					    
		                ruleFilesList.add(contentWidget);

					}catch(Exception e){
					    logger.log(Level.SEVERE, "exception when building content file widget for "+metadataWrapper.getDisplayName(), e);
					}
					
				}
			} else {
                ruleFilesList.add(new HTML(NO_RULE_CONTENT_MESSAGE));
            }
		} else {
            ruleFilesList.add(new HTML(NO_RULE_CONTENT_MESSAGE));
        }

	}
	
	@Override
	public HasClickHandlers getRuleRefreshButton(){
		return ruleRefreshButton;
	}
	
	@Override
	public void setRuleFilePanelLoading(boolean loadingContentList){
	    
	    if(loadingContentList){
	        ruleFilesList.clear();
	        ruleFilesLoadingIcon.startLoading();
	    }else{
	        ruleFilesLoadingIcon.stopLoading();
	    }
	    
	    ruleFilesLoadingPanel.setVisible(loadingContentList);
	}
	
	@Override
	public void setExampleFiles(QuadrantResultSet metadataFiles){
		
		exampleFilesList.clear();
		
		if(metadataFiles != null){
		
			if(metadataFiles.getMetadataRefs() != null && !metadataFiles.getMetadataRefs().isEmpty()){
				
                for(final MetadataWrapper metadataWrapper : metadataFiles.getMetadataRefs().values()){
                    
                    if(metadataWrapper.hasExtraneousConcept()){
                        continue;
                    }
                    
                    Command deleteCommand = null;
                    if(!metadataWrapper.hasExtraneousConcept()){
                        deleteCommand = new Command() {
                            
                            @Override
                            public void execute() {
                                
                                if (!GatClientUtility.isReadOnly()) {
                                    deleteMetadata(metadataWrapper.getMetadataFileName(), metadataWrapper.getDisplayName(), MerrillQuadrantEnum.EXAMPLE);
                                }
                                
                            }
                        };
                    }
                    
                    ContentFileWidget contentWidget = new ContentFileWidget(metadataWrapper, metadataObjectDialog, deleteCommand);
                    
                    exampleFilesList.add(contentWidget);
                }
				
			} else {
				exampleFilesList.add(new HTML(NO_EXAMPLE_CONTENT_MESSAGE));
			}
		} else {
            exampleFilesList.add(new HTML(NO_EXAMPLE_CONTENT_MESSAGE));
        }

	}
	
	@Override
    public void addContentFile(final MetadataWrapper metadataWrapper, final MerrillQuadrantEnum phase){
	    
	    if(metadataWrapper == null){
	        return;
	    }else if(phase == null){
	        throw new IllegalArgumentException("The phase can't be null.");
	    }else if(metadataWrapper.hasExtraneousConcept()){
            return;
        }
	    
	    logger.info("Adding content to the "+phase+" list panel.");
        
        Command deleteCommand = new Command() {
                
            @Override
            public void execute() {
                
                if (!GatClientUtility.isReadOnly()) {
                    deleteMetadata(metadataWrapper.getMetadataFileName(), metadataWrapper.getDisplayName(), phase);
                }
                
            }
        };
        
        ContentFileWidget contentWidget = new ContentFileWidget(metadataWrapper, metadataObjectDialog, deleteCommand);
	    
	    if(phase == MerrillQuadrantEnum.RULE){
	        
	        //handle removing the 'no content' value in the list
	        if(ruleFilesList.getWidgetCount() == 1 && ruleFilesList.getWidget(0) instanceof HTML){
	            ruleFilesList.remove(0);
	        }
	        
            ruleFilesList.insert(contentWidget, 0);
            
	    }else if(phase == MerrillQuadrantEnum.EXAMPLE){
	        
	        //handle removing the 'no content' value in the list
            if(exampleFilesList.getWidgetCount() == 1 && exampleFilesList.getWidget(0) instanceof HTML){
                exampleFilesList.remove(0);
            }
	           
	        exampleFilesList.insert(contentWidget, 0);
	        
	    }else if(phase == MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL){
	        
	        //handle removing the 'no content' value in the list
            if(remediationFilesList.getWidgetCount() == 1 && remediationFilesList.getWidget(0) instanceof HTML){
                remediationFilesList.remove(0);
            }
            
	        remediationFilesList.insert(contentWidget, 0);
	    }
	}
	
	/**
	 * Deletes the given metadata file
	 * 
	 * @param metadataFilePath the metadata file to use to find and delete the metadata on the server and list on the panel.  Can't be null or empty.
	 * @param contentFilePath the content file the metadata references (used for display purposes)
	 * @param phaseMetadataBeingDeleted what phase the metadata is being deleted from, used to remove the item from the appropriate list
	 * by phase in the panel.
	 */
	private void deleteMetadata(final String metadataFilePath, String contentFilePath, final MerrillQuadrantEnum phaseMetadataBeingDeleted){
		
		String objectName = "UNKNOWN";
		
		if(contentFilePath != null){
			objectName = contentFilePath;
		}
		
		OkayCancelDialog.show(
				"Delete Metadata?", 
				"Are you sure you want to delete the metadata for " + (
						objectName != null 
								? "<b>" + objectName + "</b>" 
								: "this content file"
				) + "?", 
				"Yes, delete this metadata", 
				new OkayCancelCallback() {
					
					@Override
					public void okay() {
			    		
						DeleteMetadata action = new DeleteMetadata(
								GatClientUtility.getUserName(), 
								GatClientUtility.getBrowserSessionKey(),
								GatClientUtility.getBaseCourseFolderPath(), 
								metadataFilePath, 
								true
						);
						
						BsLoadingDialogBox.display("Deleting Metadata", "Please wait while GIFT deletes this metadata.");
						
						SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GenericGatServiceResult<Void>>() {

							@Override
							public void onFailure(Throwable caught) {
								
								BsLoadingDialogBox.remove();
								
								DetailedException exception = new DetailedException(
										"GIFT was unable to delete this metadata. An unexpected error occurred "
										+ "during the deletion.", 
										caught.toString(), 
										caught
								);
								
								ErrorDetailsDialog dialog = new ErrorDetailsDialog(
										exception.getReason(), 
										exception.getDetails(), 
										exception.getErrorStackTrace()
								);
								
								dialog.center();
							}

							@Override
							public void onSuccess(GenericGatServiceResult<Void> result) {
								
								BsLoadingDialogBox.remove();
								
								if(result.getResponse().getWasSuccessful()){
								    
								    //instead of making a call to the server to get the list again,
								    //just remove it from the UI's list
								    
								    boolean removed = false;
								    if(phaseMetadataBeingDeleted == MerrillQuadrantEnum.RULE){
								        
								        removed = removeContentWidget(ruleFilesList, metadataFilePath);								        
							            
							            if(ruleFilesList.getWidgetCount() == 0){
							                ruleFilesList.add(new HTML(NO_RULE_CONTENT_MESSAGE));
							            }
								        
								    }else if(phaseMetadataBeingDeleted == MerrillQuadrantEnum.EXAMPLE){
                                        
								        removed = removeContentWidget(exampleFilesList, metadataFilePath);
                                            
                                        if(exampleFilesList.getWidgetCount() == 0){
                                            exampleFilesList.add(new HTML(NO_EXAMPLE_CONTENT_MESSAGE));
                                        }

                                    }else if(phaseMetadataBeingDeleted == MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL){
                                        
                                        removed = removeContentWidget(remediationFilesList, metadataFilePath);
                                        
                                        if(remediationFilesList.getWidgetCount() == 0){
                                            remediationFilesList.add(new HTML(NO_REMEDIATION_CONTENT_MESSAGE));
                                        }
                                    }
									
								    //fail safe just in case... update all content lists
									if(!removed && refreshMetadataCommand != null){
										refreshMetadataCommand.execute();
									}
									
								} else {
									
									ErrorDetailsDialog dialog = new ErrorDetailsDialog(
											result.getResponse().getException().getReason(), 
											result.getResponse().getException().getDetails(), 
											result.getResponse().getException().getErrorStackTrace()
									);
									
									dialog.center();
								}
							}
						});
					    							    
					}
					
					@Override
					public void cancel() {
						//Nothing to do
					}
		});
	}
	
	/**
	 * Remove the entry in the provided phase content panel that is associated with the metadata file path.
	 * 
	 * @param contentListPanel the panel to remove the entry that is mapped to the provided metadata file path
	 * @param metadataFilePath the course folder path to a metadata file to remove
	 * @return true if an entry was removed, false otherwise.
	 */
	private boolean removeContentWidget(FlowPanel contentListPanel, String metadataFilePath){
	    
	    if(contentListPanel == null){
	        return false;
	    }
	    
	    boolean removed = false;
	    int removeWidgetIndex = -1;
        for(int index = 0; index < contentListPanel.getWidgetCount(); index++){
            
            Widget widget = contentListPanel.getWidget(index);
            if(widget instanceof ContentFileWidget){
                
                ContentFileWidget contentFileWidget = (ContentFileWidget)widget;
                if(contentFileWidget.getMetadataFilePath().equals(metadataFilePath)){
                    removeWidgetIndex = index;
                    break;
                }
            }
        }
        
        if(removeWidgetIndex != -1){
            contentListPanel.remove(removeWidgetIndex);
            removed = true;
        }
        
        return removed;
	}
	
	@Override
	public HasClickHandlers getExampleRefreshButton(){
		return exampleRefreshButton;
	}

	
	@Override
	public void setExampleFilePanelLoading(boolean loadingContentList){
	    
        if(loadingContentList){
            exampleFilesList.clear();
            exampleFilesLoadingIcon.startLoading();
        }else{
            exampleFilesLoadingIcon.stopLoading();
        }
	       
	    exampleFilesLoadingPanel.setVisible(loadingContentList);
	}
	
	@Override
	public void setRemediationFiles(QuadrantResultSet metadataFiles){
		
		remediationFilesList.clear();
		
		if(metadataFiles != null){
		
			if(metadataFiles.getMetadataRefs() != null && !metadataFiles.getMetadataRefs().isEmpty()){
				
                for(final MetadataWrapper metadataWrapper : metadataFiles.getMetadataRefs().values()){
                    
                    if(metadataWrapper.hasExtraneousConcept()){
                        continue;
                    }
                    
                    Command deleteCommand = null;
                    if(!metadataWrapper.hasExtraneousConcept() && metadataWrapper.isRemediationOnly()){
                        deleteCommand = new Command() {
                            
                            @Override
                            public void execute() {
                                
                                if (!GatClientUtility.isReadOnly()) {
                                    deleteMetadata(metadataWrapper.getMetadataFileName(), metadataWrapper.getDisplayName(), MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL);
                                }
                                
                            }
                        };
                    }
                    
                    ContentFileWidget contentWidget = new ContentFileWidget(metadataWrapper, metadataObjectDialog, deleteCommand);
                    
                    remediationFilesList.add(contentWidget);
                }
			} else {
                remediationFilesList.add(new HTML(NO_REMEDIATION_CONTENT_MESSAGE));
            }
			
		} else {
            remediationFilesList.add(new HTML(NO_REMEDIATION_CONTENT_MESSAGE));
        }

	}
	
	@Override
	public HasClickHandlers getRemediationRefreshButton(){
		return remediationRefreshButton;
	}
	
	@Override
	public void setRemediationFilePanelLoading(boolean loadingContentList){
	    
        if(loadingContentList){
            remediationFilesList.clear();
            remediationFilesLoadingIcon.startLoading();
        }else{
            remediationFilesLoadingIcon.stopLoading();
        }
           
        remediationFilesLoadingPanel.setVisible(loadingContentList);
	}
	
	@Override
	public boolean removePracticeApplication(String metadataFilePath){
	    
	    if(metadataFilePath == null || metadataFilePath.isEmpty()){
	        return false;
	    }
	    
	    PracticeApplicationObject toRemove = null;
	    for(PracticeApplicationObject practiceApplicationObject : practiceApplicationProvider.getList()){
	        
	        if(practiceApplicationObject.getMetadataFilesMap() != null &&
	                practiceApplicationObject.getMetadataFilesMap().containsKey(metadataFilePath)){
	            toRemove = practiceApplicationObject;
	            break;
	        }
	    }
	    
	    if(toRemove != null){
	        practiceApplicationProvider.getList().remove(toRemove);
	    }
	    
	    return toRemove != null;
	}
	
	@Override
	public void setPracticeApplications(List<PracticeApplicationObject> practiceApplications){
		
		practiceApplicationProvider.getList().clear();
        
		if(practiceApplications != null && !practiceApplications.isEmpty()){
    		Collections.sort(practiceApplications, practiceApplicationListSorter);
    		
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Adding " + practiceApplications.size() + " practice applications entries to table.");
            }
    		
    		practiceApplicationProvider.getList().addAll(practiceApplications);
		}
	}
	
	@Override
	public HasClickHandlers getPracticeRefreshButton(){
		return practiceRefreshButton;
	}
	
	@Override
	public void setCourseFolderPath(String coursePath) {
		addApplicationDialog.getTAInteropEditor().setCourseFolderPath(coursePath);
	}
	
	@Override
	public TrainingAppInteropEditor getPracticeInteropEditor(){
		return addApplicationDialog.getTAInteropEditor();
	}

    @Override
    public SurveyPickerQuestionBank getSurveyPickerQuestionBank() {
        return surveyPickerQuestionBank;
    }

    @Override
    public void onNameBoxUpdated(String name) {   	
        surveyPickerQuestionBank.setTransitionName(name);
        
    }
    
    @Override
    public Column<PracticeApplicationObject, String> getPracticeApplicationDeleteColumn(){
    	return applicationDeleteColumn;
    }

    @Override
    public void setRefreshMetadataCommand(Command command){
    	this.refreshMetadataCommand = command;    	
    }
    
    @Override
    public void setCourseSurveyContextId(BigInteger surveyContextId) {
        addApplicationDialog.setCourseSurveyContextId(surveyContextId);       
    }
    
    @Override
    public OptionalGuidanceCreator getRuleGuidanceCreator(){
    	return ruleGuidanceCreator;
    }
    
    @Override
    public OptionalGuidanceCreator getExampleGuidanceCreator(){
    	return exampleGuidanceCreator;
    }
    
    @Override
    public OptionalGuidanceCreator getRecallGuidanceCreator(){
    	return recallGuidanceCreator;
    }
    
    @Override
    public OptionalGuidanceCreator getPracticeGuidanceCreator(){
    	return addApplicationDialog.getPracticeGuidanceCreator();
    }
    
    @Override 
    public org.gwtbootstrap3.client.ui.Button getChangePracticeApplicationButton(){
		return addApplicationDialog.getChangeApplicationButton();
	}
    
    @Override
    public ContentReferenceEditor getContentReferenceEditor(){
    	return addContentDialog.getReferenceEditor();
    }

    @Override
    public HasValue<Boolean> getDisabledInput() {
        return disabled;
    }
    
    @Override
    public void setMandatoryControlsVisibility(boolean isVisible) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("setMandatoryControlsVisibility(" + isVisible + ")");
        }
        
        if(isVisible) {
            optionTablePanel.removeStyleName(style.optionsTableMandatoryInvisible());
        } else {
            optionTablePanel.addStyleName(style.optionsTableMandatoryInvisible());
        }
    }
    
    @Override
    public HasValue<Boolean> getMandatoryCheckBox(){
        return mandatoryCheckBox;
    }
    
    @Override
    public HasEnabled getMandatoryCheckBoxHasEnabled() {
        return mandatoryCheckBox;
    }
    
    @Override
    public HasValue<MandatoryBehaviorOptionChoice> getMandatoryBehaviorSelector() {
        return mandatoryBehaviorSelector;
    }
    
    @Override
    public HasEnabled getMandatoryBehaviorSelectorHasEnabled() {
        return mandatoryBehaviorSelector;
    }
    
    @Override
    public HasVisibility getMandatoryBehaviorSelectorHasVisibility() {
        return mandatoryBehaviorSelector;
    }
    
    @Override
    public HasValue<Integer> getLearnerStateShelfLife() {
        return learnerStateShelfLife;
    }
    
    @Override
    public HasEnabled getLearnerStateShelfLifeHasEnabled() {
        return learnerStateShelfLife;
    }
    
    @Override
    public HasVisibility getLearnerStateShelfLifeHasVisibility() {
        return learnerStateShelfLifePanel;
    }
    
    @Override
    public ContentReferenceEditor getRemediationContentReferenceEditor(){
    	return addRemediationDialog.getReferenceEditor();
    }

    @Override
    public HasEnabled getDisabledInputHasEnabled() {
        return disabled;
    }
    
    @Override
    public void setMbpOptionsVisible(boolean visible){       
        mbpOptionsPanel.setOpen(visible);
    }
    
    @Override
    public void updateContentOnConceptChange(String concept, boolean selected) {
        addContentDialog.conceptSelected(concept, selected);
        addRemediationDialog.conceptSelected(concept, selected);
    }

	@Override
	public void onReadOnlyChange(boolean isReadOnly) {
		ruleGuidanceCreator.setEnabled(!isReadOnly);
		exampleGuidanceCreator.setEnabled(!isReadOnly);
		recallGuidanceCreator.setEnabled(!isReadOnly);
		recallAllowedAttemptsCheckBox.setEnabled(!isReadOnly);
		recallAllowedAttempts.setEnabled(!isReadOnly);
		excludeRuleExampleContentCheckBox.setEnabled(!isReadOnly);
		showPracticePanelCheckBox.setEnabled(!isReadOnly);
		practiceAllowedAttemptsCheckBox.setEnabled(!isReadOnly);
		practiceAllowedAttempts.setEnabled(!isReadOnly);
		addExampleContentButton.setVisible(!isReadOnly);
		addRemediationContentButton.setVisible(!isReadOnly);
		addRuleContentButton.setVisible(!isReadOnly);
		addApplicationButton.setVisible(!isReadOnly);
	}
}
