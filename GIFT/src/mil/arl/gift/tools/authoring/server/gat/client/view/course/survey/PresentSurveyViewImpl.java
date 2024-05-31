/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.survey;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.HasSafeHtml;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

import generated.course.ConceptQuestions;
import generated.course.ConceptQuestions.AssessmentRules;
import generated.course.Course;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.file.CancelCallback;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider.CourseReadOnlyHandler;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.RealTimeAssessmentPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.CourseSurveyResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.SurveyPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.SurveyPickerQuestionBank;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModalCancelCallback;

public class PresentSurveyViewImpl extends Composite implements PresentSurveyView, CourseObjectModalCancelCallback, CourseReadOnlyHandler {

    private static PresentSurveyViewImplUiBinder uiBinder = GWT
            .create(PresentSurveyViewImplUiBinder.class);

    interface PresentSurveyViewImplUiBinder extends
            UiBinder<Widget, PresentSurveyViewImpl> {
    }
    
    interface UiStyle extends CssResource {
        String optionsTableMandatoryInvisible();
        String mandatoryControlsRow();
    }
    
    @UiField
    protected UiStyle style;
    
    /** The Constant logger. */
    private static final Logger logger = Logger.getLogger(PresentSurveyViewImpl.class.getName());
    
    @UiField
    protected DeckPanel surveyTypeDeckPanel;

    @UiField
    protected CourseObjectModal editorDialog;
 
    
    @UiField
    protected AutoTutorSessionChoicePanel autoTutorSessionChoicePanel;
    
    @UiField
    protected RealTimeAssessmentPanel conversationTreeSelectPanel;
    
    private DefaultGatFileSelectionDialog fileSelectionDialog = new DefaultGatFileSelectionDialog();

    /**
     * Callback used if there is an error with importing the dkf for an autotutor file.
     */
    private CancelCallback dkfErrorCallback;
    
    @UiField
    protected KnowledgeAssessmentQuestionsPanel questionsPanel;
    
    @UiField
    protected DisclosurePanel knowledgeAssessmentOptionsPanel;
    
    @UiField
    protected CheckBox useResultsCheckBox;
    
    @UiField
    protected DisclosurePanel surveyOptionsPanel;
    
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
    
    @UiField
    protected Label showResponsesLabel;
    
    @UiField(provided=true)
    protected CellTable<CandidateConcept> conceptCellTable = new CellTable<CandidateConcept>();      

    @UiField
    protected SurveyPicker surveyChoicePanel;
    
    @UiField
    protected SurveyPickerQuestionBank surveyPickerQuestionBank;
    
    @UiField
    protected CheckBox fullScreenCheckBox;
    
    @UiField
    protected CheckBox showResponsesCheckBox;
    
    /** The disable checkbox. */
    @UiField
    protected CheckBox disabled;
    
    @UiField
    protected Widget warningPanel;
    
    @UiField
    protected HasSafeHtml warningText;
    
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
    
    /** The name column for the 'Concepts:' table */
    private Column<CandidateConcept, String> conceptNameColumn = new Column<CandidateConcept, String>(new TextCell()){

        @Override
        public String getValue(CandidateConcept candidate) {

            if(candidate.getConceptName() != null ){
                return candidate.getConceptName();
            }
            
            return null;
        }
        
    };
    
    @UiField
    protected Widget questionBankPanel;
    
    
    // -- Methods -----------------------------------------------------------------------------------------------------------------------

    public PresentSurveyViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
        CourseReadOnlyProvider.getInstance().addReadOnlyHandlerManaged(this);
        surveyTypeDeckPanel.showWidget(1);
        editorDialog.setCancelCallback(this);
        
        fullScreenCheckBox.setValue(true);
        showResponsesCheckBox.setValue(true);
            
        conceptCellTable.setPageSize(Integer.MAX_VALUE);
         
        // attach the 'Concepts:' cell table to its associated columns 
        
        conceptCellTable.addColumn(conceptSelectionColumn);
        conceptCellTable.setColumnWidth(conceptSelectionColumn, "0px");
        
        conceptCellTable.addColumn(conceptNameColumn);
        
        VerticalPanel emptyConceptsWidget = new VerticalPanel();
        emptyConceptsWidget.setSize("100%", "100%");
        
        emptyConceptsWidget.add(new HTML("<br>"));
        
        Label emptyConceptsLabel = new Label("No course concepts were found. Please make sure you have added a list or hierarchy of concepts to the course summary.");
        emptyConceptsLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
        
        emptyConceptsWidget.add(emptyConceptsLabel);
        
        emptyConceptsWidget.add(new HTML("<br>"));
        
        conceptCellTable.setEmptyTableWidget(emptyConceptsWidget); 
        
        mandatoryBehaviorSelector.setValue(MandatoryBehaviorOptionChoice.ALWAYS);
        mandatoryBehaviorSelector.setAcceptableValues(Arrays.asList(MandatoryBehaviorOptionChoice.ALWAYS, MandatoryBehaviorOptionChoice.AFTER));
    }
    
    @Override
    public void setSurveyTypeEditingMode(SurveyTypeEditingMode mode){
        
        if(mode.equals(SurveyTypeEditingMode.AUTOTUTOR)){
            surveyTypeDeckPanel.showWidget(surveyTypeDeckPanel.getWidgetIndex(autoTutorSessionChoicePanel));
            
        } else if(mode.equals(SurveyTypeEditingMode.GIFT_SURVEY)){
            surveyTypeDeckPanel.showWidget(surveyTypeDeckPanel.getWidgetIndex(surveyChoicePanel));
            
        } else if(mode.equals(SurveyTypeEditingMode.CONVERSATION_TREE)){
            surveyTypeDeckPanel.showWidget(surveyTypeDeckPanel.getWidgetIndex(conversationTreeSelectPanel));
            
        } else if(mode.equals(SurveyTypeEditingMode.QUESTION_BANK)){
            surveyTypeDeckPanel.showWidget(surveyTypeDeckPanel.getWidgetIndex(questionBankPanel));
        }
    }
    
    @Override
    public void setAutotutorTypeEditingMode(GIFTAutotutorSessionTypeEditingMode mode){
        autoTutorSessionChoicePanel.setAutotutorTypeEditingMode(mode);
        
        if(autoTutorSessionChoicePanel.isVisible()
                && mode == GIFTAutotutorSessionTypeEditingMode.SKO) {
            
            warningText.setHTML(SafeHtmlUtils.fromSafeConstant(
                    "Provide the location of where the AutoTutor conversation is hosted. \r\n" + 
                    "<br/><br/>\r\n" + 
                    "This location needs to be accessible by the GIFT instance running the course. \r\n" + 
                    "Ideally the conversation should be hosted on the same AutoTutor server that this \r\n" + 
                    "GIFT instance is using at " + GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.ASAT_URL) + 
                    " to ensure compatibility."
            ));
            
            warningPanel.setVisible(true);
            
        } else {
            
            warningText.setHTML(SafeHtmlUtils.fromSafeConstant(""));
            warningPanel.setVisible(false);
        }
    }
    
    @Override
    public HasClickHandlers getSelectDKFFileButton(){
        return autoTutorSessionChoicePanel.getSelectDKFFileButton();
    }
    
    @Override
    public HasClickHandlers getEditDkfButton(){
        return autoTutorSessionChoicePanel.getEditDkfButton();
    }
    
    @Override
    public void showDkfFileLabel(String path){
        if(path != null && !path.isEmpty()) {
            autoTutorSessionChoicePanel.showDkfFileLabel(path);
        } else {
            hideDkfFileLabel();
        }
    }
    
    @Override
    public void hideDkfFileLabel(){
        autoTutorSessionChoicePanel.hideDkfFileLabel();
    }
    
    @Override
    public void setShowResponsesVisibility(boolean visible){
        
        showResponsesCheckBox.setVisible(visible);
        showResponsesLabel.setVisible(visible);
    }
    
    @Override
    public HasClickHandlers getRemoveDkfButton(){
        return autoTutorSessionChoicePanel.getRemoveDkfButton();
    }
    
    @Override
    public HasValue<String> getFileSelectionDialog(){
        return fileSelectionDialog;
    }
    
    @Override
    public HasClickHandlers getSelectConversationTreeFileButton(){
        return conversationTreeSelectPanel.getAddAssessmentButton();
    }
    
    @Override
    public void setFileSelectionDialogIntroMessage(String msg){
        fileSelectionDialog.setIntroMessageHTML(msg);
    }
    
    @Override
    public void hideConversationTreeFileLabel() {
        conversationTreeSelectPanel.removeAssessment();
    }
    
    @Override
    public void showConversationTreeFileLabel(String path) {
        if(path != null && !path.isEmpty()){
            conversationTreeSelectPanel.setAssessment(path);
            conversationTreeSelectPanel.setNameOfAssessedItem(CourseObjectName.CONVERSATION_TREE.getDisplayName());
        } else {
            hideConversationTreeFileLabel();
        }
    }
    
    @Override
    public HasClickHandlers getRemoveConversationTreeButton() {
        return conversationTreeSelectPanel.getDeleteButton();
    }
    
    @Override
    public HasClickHandlers getEditConversationTreeButton() {
        return conversationTreeSelectPanel.getEditButton();
    }
    
    @Override
    public FileSelectionView getFileSelector(){
        return fileSelectionDialog.getFileSelector();
    }
    
    @Override
    public void setFileSelectionDialogVisible(boolean visible){
        
        if(visible){
            fileSelectionDialog.center();
        
        } else {
            fileSelectionDialog.hide();
        }
    }
    
    @Override
    public void showDkfModalEditor(final String coursePath, final String url) {
        logger.info("showDkfModalEditor() called with url: " + url);
        editorDialog.setCourseObjectUrl(CourseObjectName.DKF.getDisplayName() + (GatClientUtility.isReadOnly() ? " (Read Only)" : ""), url);
        editorDialog.setSaveButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                GatClientUtility.saveEmbeddedCourseObject();
                String filename = GatClientUtility.getFilenameFromModalUrl(coursePath, url, AbstractSchemaHandler.DKF_FILE_EXTENSION);
                fileSelectionDialog.setValue(filename, true);
                editorDialog.stopEditor();
                
                if(!GatClientUtility.isReadOnly()){
                    GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this survey being edited
                }
            }
            
        });
        editorDialog.show();
    }
    
    @Override
    public void showConversationTreeModalEditor(final String coursePath, final String url, final String courseObjectName) {
        editorDialog.setCourseObjectUrl(CourseObjectName.CONVERSATION_TREE.getDisplayName() + (GatClientUtility.isReadOnly() ? " (Read Only)" : ""), url);
        editorDialog.setSaveButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                GatClientUtility.saveEmbeddedCourseObject();
                String filename = GatClientUtility.getFilenameFromModalUrl(coursePath, url, AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION);
                fileSelectionDialog.setValue(filename, true);
                editorDialog.stopEditor();
                
                if(!GatClientUtility.isReadOnly()){
                    GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this conversation being edited
                }
            }
            
        });
        
        editorDialog.getEditorFrame().getInnerFrame().addLoadHandler(new LoadHandler() {
            
            @Override
            public void onLoad(LoadEvent arg0) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        setEditorCourseObjectName(editorDialog.getEditorFrame().getInnerFrame().getElement(), courseObjectName);
                    }
                });
            }
        });
        
        editorDialog.show();
    
    }
    
    /**
     * Provides the editor corresponding to the given element with the name of the course object used to open it
     * 
     * @param element the iframe element corresponding to the target editor
     * @param string the course object name to provide the editor
     */
    private native void setEditorCourseObjectName(Element element, String name)/*-{
        element.contentWindow.setCourseObjectName(name);        
    }-*/;


    @Override
    public void onNameBoxUpdated(String name) {
        logger.info("onNameBoxUpdated(): " + name);
        surveyChoicePanel.setTransitionName(name);
        
    }

    @Override
    public void setCourseData(Course currentCourse) {
        surveyChoicePanel.setSurveyResources(new CourseSurveyResources(currentCourse));
        surveyPickerQuestionBank.setCourseData(currentCourse);
    }

    @Override
    public void onCancelModal(boolean removeSelection) {
        logger.info("onCancelModal() called, removeSelection= " + removeSelection);
        if (removeSelection && dkfErrorCallback != null) {
            dkfErrorCallback.onCancel();
        }
    }

    @Override
    public void setCancelCallback(CancelCallback callback) {
        this.dkfErrorCallback = callback;
        
    }
    
    @Override
    public HasData<CandidateConcept> getConceptCellTable(){
        return conceptCellTable;
    }
   
    
    @Override
    public HasData<ConceptQuestions> getQuestionCellTable(){
        return questionsPanel.getQuestionCellTable();
    }
    
    
    @Override
    public void setConceptSelectionColumnFieldUpdater(FieldUpdater<CandidateConcept, Boolean> updater){
        conceptSelectionColumn.setFieldUpdater(updater);
    }
    
    
    @Override
    public void setEasyColumnFieldUpdater(FieldUpdater<ConceptQuestions, String> updater){
        questionsPanel.setEasyColumnFieldUpdater(updater);
    }
    
    
    @Override
    public void setMediumColumnFieldUpdater(FieldUpdater<ConceptQuestions, String> updater){
        questionsPanel.setMediumColumnFieldUpdater(updater);
    }
    
    
    @Override
    public void setHardColumnFieldUpdater(FieldUpdater<ConceptQuestions, String> updater){
        questionsPanel.setHardColumnFieldUpdater(updater);
    }
    
    
    @Override
    public void redrawQuestionsCellTable(){
        questionsPanel.redrawQuestionsCellTable();
    }
    
    
    @Override
    public KnowledgeAssessmentSlider appendSlider(String conceptName) {
        return questionsPanel.appendSlider(conceptName);
    }
    
    @Override
    public void appendExtraneousSlider(String conceptName, int totalQuestions, AssessmentRules assessmentRules, ScheduledCommand removeCmd) {
        questionsPanel.appendExtraneousSlider(conceptName, totalQuestions, assessmentRules, removeCmd);
    }
    
    @Override
    public void removeSlider(String conceptName) {
        questionsPanel.removeSlider(conceptName);
    }
    
    
    @Override
    public KnowledgeAssessmentSlider updateSlider(String conceptName, int totalQuestions, AssessmentRules assessmentRules) {
        return questionsPanel.updateSlider(conceptName, totalQuestions, assessmentRules);
    }
    
    @Override
    public void refreshSliderPanel(List<CandidateConcept> conceptList) {
        questionsPanel.refreshSliderPanel(conceptList);
    }
    
    @Override
    public void undoQuestionsTableChanges(ConceptQuestions question){
        questionsPanel.undoQuestionsTableChanges(question);
    }
    
    
    @Override
    public HasValue<Boolean> getFullScreenCheckBox(){
        return fullScreenCheckBox;
    }
    
    @Override
    public HasEnabled getFullScreenCheckBoxHasEnabled(){
        return fullScreenCheckBox;
    }
    
    @Override
    public HasValue<Boolean> getDisabledInput(){
        return disabled;
    }
    
    @Override
    public HasEnabled getDisabledInputHasEnabled(){
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
    public HasValue<Boolean> getShowResponsesCheckBox(){
        return showResponsesCheckBox;
    }
    
    @Override
    public HasEnabled getShowResponsesCheckboxHasEnabled(){
        return showResponsesCheckBox;
    }    
   
   
    @Override
    public HasValue<Boolean> getUseResultsCheckBox(){
        return useResultsCheckBox;
    }
    
    @Override
    public HasEnabled getUseResultsCheckboxHasEnabled(){
        return useResultsCheckBox;
    }    
   
   
    @Override
    public void setSurveyOptionsVisible(boolean visible){       
        surveyOptionsPanel.setOpen(visible);
    }
    
   
    @Override
    public void setKnowledgeAssessmentOptionsVisible(boolean visible){
        knowledgeAssessmentOptionsPanel.setOpen(visible);
    }
    
    /**
     * Sets the survey context ID to use to get the list of surveys. If no survey context ID is set, then no surveys will be loaded.
     * 
     * @param id the ID to use
     */
    @Override
    public void setSurveyContextId(BigInteger id){
        surveyChoicePanel.setSurveyContextId(id);
        surveyPickerQuestionBank.setSurveyContextId(id);
    }
    
    @Override
    public void setTransitionName(String name) {
        surveyChoicePanel.setTransitionName(name);
        surveyPickerQuestionBank.setTransitionName(name);
    }
    
    /**
     * Gets the widget used to pick surveys
     * 
     * @return the survey picker
     */
    @Override
    public SurveyPicker getSurveyPicker(){
        return surveyChoicePanel;
    }

    /**
     * Return the survey picker question bank ui widget.  This is the widget that
     * allows users to select a question bank.
     * 
     * @return - The survey picker question bank ui widget.
     */
    @Override
    public SurveyPickerQuestionBank getSurveyPickerQuestionBank() {
        return surveyPickerQuestionBank;
    }
    
    /**
     * Refreshes the view of the survey choice panel.  This should be called when
     * the dialog containing the survey choice panel is displayed.
     */
    @Override
    public void refreshView() {
        surveyChoicePanel.updateDisplay();
        surveyPickerQuestionBank.updateDisplay();
    }
    
    @Override
    public TextBox getConversationUrlBox(){
        return autoTutorSessionChoicePanel.getConversationUrlBox();
    }

	@Override
	public void onReadOnlyChange(boolean isReadOnly) {
		fullScreenCheckBox.setEnabled(!isReadOnly);
		showResponsesCheckBox.setEnabled(!isReadOnly);
		disabled.setEnabled(!isReadOnly);
	}
}
