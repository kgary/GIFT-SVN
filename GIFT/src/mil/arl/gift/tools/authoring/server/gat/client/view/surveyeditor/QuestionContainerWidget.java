/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.OptGroup;
import org.gwtbootstrap3.extras.select.client.ui.Option;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.common.enums.QuestionTypeEnum;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.MultipleChoiceWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SaveSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.ReturnValueCondition;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AddSurveyDialog.SurveyDialogOption;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyPageWidget.MoveOrderEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.AssociatedConceptsChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.DifficultyChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyAddSurveyItemEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyAddSurveyItemEvent.InsertOrder;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyCopyQuestionEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyDeleteQuestionEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyMoveQuestionEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveySelectQuestionEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;

/**
 * The QuestionContainerWidget is a container class that houses the generic properties of a question
 * such as a button bar for moving, copying, deleting the question, as well as things such as question name.
 * 
 * This is a "container" widget and will contain a specific "questionContainer" that holds the specific widget
 * such as Multiple Choice, Free Response, etc.  All specific question type widgets should be housed within this
 * question container widget so that the styling & functionality is similar across all question types.  
 * 
 * @author nblomberg
 *
 */
public class QuestionContainerWidget extends Composite  {

    /** Logger for the class */
    private static Logger logger = Logger.getLogger(QuestionContainerWidget.class.getName());
    
    /** The UiBinder that combines the ui.xml file with this java class */
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with the java class */
    interface WidgetUiBinder extends
            UiBinder<Widget, QuestionContainerWidget> {
    }
	
	/** Interface to allow CSS style name access */
	interface Style extends CssResource {
		String groupLabel();
	}
	
    /** A reference to the styles defined in the ui.xml */
    @UiField
	protected Style style;
    
    /** Container for the question widget */
    @UiField
    Container questionContainer;
    
    @UiField
    FormLabel questionNum;
    
    /** Button that moves the question up in the question ordering */
    @UiField
    Button moveUpButton;
    
    /** Button that moves the question down in the question ordering */
    @UiField
    Button moveDownButton;
    
    /** Button that deletes the question from the survey */
    @UiField
    Button deleteButton;
    
    /** Button that makes a copy of the question in the survey */
    @UiField
    Button copyButton;
   
    @UiField
    Row rowContainer;
    
    @UiField
    SimpleCheckBox selectedCheckBox;
    
    /** Button that inserts a new question before this one */
    @UiField
    Button insertBefore;
    
    /** Button that inserts a new question after this one */
    @UiField
    Button insertAfter;
    
    @UiField
    Container mainContainer;
    
    @UiField
    Container attributeContainer;
    
    @UiField
    Container conceptContainer;
    
    @UiField
    MultipleSelect multiSelect;
    
    /** the selectable options for 'associated concepts' */
    @UiField
    MultipleSelect conceptSelect;
    
    @UiField
    ListBox difficultyList;
    
    @UiField
    protected BlockerPanel questionBlocker;
    
    @UiField
    protected BlockerPanel disableBlocker;
    
    @UiField
    protected Icon extraneousConceptsIcon;
    
    private static final String EXTRANEOUS_CONCEPTS_GROUP_LABEL = "Extraneous Course Concepts";
    private static final String COURSE_CONCEPTS_GROUP_LABEL = "Course Concepts";
    private static final String EXTRANEOUS_CONCEPTS_GROUP_TEXT_COLOR = "red";
    
    private int extraneousGroupItemIndex = -1;
    
    @UiHandler("multiSelect")
    void onValueChangeMultiple(ValueChangeEvent<List<String>> event) {
        logger.info("onValueChangeMultiple triggered: " + event.getValue());
        
        Set<AttributeScorerProperties> newProperties = new HashSet<AttributeScorerProperties>();
        for(String attribute : event.getValue()){
            newProperties.add(new AttributeScorerProperties(LearnerStateAttributeNameEnum.valueOf(attribute), new ArrayList<ReturnValueCondition>()));
        }
        
        if(getQuestionWidget() instanceof AbstractQuestionWidget){
            AbstractQuestionWidget questionWidget = (AbstractQuestionWidget) getQuestionWidget();
            questionWidget.setScorerProperty(newProperties);
        } else {
            logger.severe("This question type (" + getQuestionWidget().getClass().getName() + ") should not support question attributes.");
        }
        
        SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
    }
    
    @UiHandler("difficultyList")
    void onValueChangeDifficulty(ChangeEvent event){
        logger.info("onValueChangeDifficulty triggered: " + difficultyList.getSelectedValue());
        
        if(getQuestionWidget() instanceof AbstractQuestionWidget){
            AbstractQuestionWidget quesionWidget = (AbstractQuestionWidget) getQuestionWidget();
            quesionWidget.setDifficulty(QuestionDifficultyEnum.valueOf(difficultyList.getSelectedValue()));

            SharedResources.getInstance().getEventBus().fireEvent(new DifficultyChangedEvent(widgetId, QuestionDifficultyEnum.valueOf(difficultyList.getSelectedValue())));
        } else {
            logger.severe("This question type (" + getQuestionWidget().getClass().getName() + ") should not support question difficulty.");
        }
        
    }
    
    /**
     * Event handler that is fired when the concept selection selection widget's
     * value changes.
     * 
     * @param event the event that is fired when the selected concepts change
     */
    @UiHandler("conceptSelect")
    void onValueChangeConcepts(ValueChangeEvent<List<String>> event){
        logger.info("onValueChangeConcepts triggered: " + event.getValue());
        if(getQuestionWidget() instanceof AbstractQuestionWidget){
            AbstractQuestionWidget questionWidget = (AbstractQuestionWidget) getQuestionWidget();
            questionWidget.setConcepts((ArrayList<String>) event.getValue());
            
            //
            // Check to see if the extraneous concepts group can be removed 
            //
            List<String> qWidgetConcepts = questionWidget.getConcepts();
            
            logger.info("Question widget concepts are now: "+qWidgetConcepts);
        
            boolean haveExtraneousConcepts = false;
            if (qWidgetConcepts != null && !qWidgetConcepts.isEmpty() && extraneousGroupItemIndex != -1){

                OptGroup optGroup = (OptGroup) conceptSelect.getWidget(extraneousGroupItemIndex);
                for (String conceptName : qWidgetConcepts ) {
                    //search for each of the question's concepts in the extraneous list of course concepts

                    for(int groupIndex = 0; groupIndex < optGroup.getWidgetCount(); groupIndex++){
                                                
                        Widget optGroupWidget = optGroup.getWidget(groupIndex);
                        if(optGroupWidget instanceof Option){
                                                        
                            Option option = (Option)optGroupWidget;
                            if(option.getValue().equals(conceptName)){
                                //found question concept in extraneous concepts list still
                                haveExtraneousConcepts = true;
                                break;
                            }
                        }
                    }//end for
                    
                    if(haveExtraneousConcepts){
                        break;
                    }
                    
                }//end for
            }
            
            
            if(!haveExtraneousConcepts && extraneousGroupItemIndex != -1){
                //remove the extraneous concepts option group from the concept list
                logger.info("Removing the extraneous group item at index "+extraneousGroupItemIndex+" and refreshing the concept select list.");
                conceptSelect.remove(extraneousGroupItemIndex);
                extraneousGroupItemIndex = -1; //reset
                conceptSelect.refresh();
                extraneousConceptsIcon.setVisible(false);
            }
            
            List<String> optionStringList = new ArrayList<String>();
            for(Option o : conceptSelect.getSelectedItems()) {
                optionStringList.add(o.getValue());
            }

            SharedResources.getInstance().getEventBus().fireEvent(new AssociatedConceptsChangedEvent(widgetId, optionStringList));
        } else {
            logger.severe("This question type (" + getQuestionWidget().getClass().getName() + ") should not support question concepts.");
        }
        
    }
    
    /** The prefix for the question number that's displayed in the question sidebar. */
    private static final String Q_PREFIX = "Q";
    
    /** The prefix for the informative text number that's displayed in the survey item sidebar. */
    private static final String I_PREFIX = "I";
    
    /** The id of the question, should be unqiue across all questions in a survey. */
    private SurveyWidgetId widgetId = null;
    
    /** The id of the page the question container belongs to */
    private SurveyWidgetId parentPageId = null;
    
    /** Boolean to indicate if the question item is selected. */
    private boolean isSelected = false;
    
    /** Boolean to indicate if the question item is read-only */
    private boolean readOnly = false;
    
    /** The edit mode that the survey panel is in (writing mode or scoring mode). */
    private SurveyEditMode editMode;
    
    /** The type of survey being edited in the survey editor. */
    private SurveyDialogOption surveyType;
    
    /** The set of global survey resources (i.e. survey context, concepts, etc.) that should be referred to while editing surveys */
    private AbstractSurveyResources surveyResources= null;

    /**
     * Constructor 
     * @param pageId - The parent page id that contains the question.
     * @param mode - The current mode of the survey editor (writing or scoring).
     * @param type - The type of survey that is being edited in the editor.
     */
    public QuestionContainerWidget(SurveyWidgetId pageId, SurveyEditMode mode, SurveyDialogOption type) {
        
        logger.info("constructor()");
        initWidget(uiBinder.createAndBindUi(this));
        
        setEditMode(mode);
        setSurveyType(type);
        
        widgetId = new SurveyWidgetId();
        parentPageId = pageId;
        
        // Set the main container 'id' element to the unique widget id.
        mainContainer.setId(widgetId.getWidgetId());

        moveUpButton.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyMoveQuestionEvent(parentPageId, widgetId, MoveOrderEnum.ORDER_UP));
                
                // Prevent the click event from being handled on any parent widget.  This prevents the widget from being selected
                // when the button is clicked.
                event.stopPropagation();
            }
            
        });
        
        moveDownButton.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyMoveQuestionEvent(parentPageId, widgetId, MoveOrderEnum.ORDER_DOWN));
                
                // Prevent the click event from being handled on any parent widget.  This prevents the widget from being selected
                // when the button is clicked.
                event.stopPropagation();
                
            }
            
        });
        
        copyButton.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyCopyQuestionEvent(parentPageId, widgetId));
                
                // Prevent the click event from being handled on any parent widget.  This prevents the widget from being selected
                // when the button is clicked.
                event.stopPropagation();
                
            }
            
        });
        
        deleteButton.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event){          
                OkayCancelDialog.show("Delete Question", 
                        "Are you sure that you would like to <b>permanently</b> delete the question?", 
                        "Delete", 
                        new OkayCancelCallback(){

                            @Override
                            public void okay() {
                                //Delete the question
                                SharedResources.getInstance().getEventBus().fireEvent(new SurveyDeleteQuestionEvent(parentPageId, widgetId)); 
                            }

                            @Override
                            public void cancel() {
                                //Do nothing
                            }
                    
                });
                
                // Don't propagate the click since the item will be deleted.
                event.stopPropagation();
            }
            
        });
        
        rowContainer.addDomHandler(new MouseDownHandler() {
            
            @Override
            public void onMouseDown(MouseDownEvent event) {
                
                logger.info("question item was clicked.");
                
                final boolean ctrlKeyPressed = event.getNativeEvent().getCtrlKey();

                if(!ctrlKeyPressed){
                    SharedResources.getInstance().getEventBus().fireEvent(new SurveySelectQuestionEvent(parentPageId, widgetId));
                    
                } else {
                    SharedResources.getInstance().getEventBus().fireEvent(new SurveySelectQuestionEvent(parentPageId, widgetId, true));
                }
            }
        }, MouseDownEvent.getType());
        
        selectedCheckBox.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {               

                //prevent the rowContainer mousedownhandler from being called
                event.stopPropagation();
                
                //if the checkbox is clicked, then select this question container without clearing out previous selections
                SharedResources.getInstance().getEventBus().fireEvent(new SurveySelectQuestionEvent(parentPageId, widgetId, true));
            }
            
        });
        
        selectedCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                // stop handlers after this to prevent them from setting the checkox value because the 
                // SurveySelectQuestionEvent in handlers (above) will cause the setSelected method in this class
                // to set the checkbox value.
                event.preventDefault();
            }
        });
                
        insertBefore.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyAddSurveyItemEvent(parentPageId, widgetId, InsertOrder.ORDER_BEFORE));
            }
        });

        insertAfter.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyAddSurveyItemEvent(parentPageId, widgetId, InsertOrder.ORDER_AFTER));
            }
        });
        
        updateControlsVisibilityAndUsability();
        }
        
    /**
     * Populates the multi select dropdown with the available learner state attributes that can be chosen.
     */
    private void populateMultiSelectList() {
       
        for (LearnerStateAttributeNameEnum attribute : LearnerStateAttributeNameEnum.SORTED_VALUES()) {
            
            Option opt = new Option();
            opt.setText(attribute.getDisplayName());
            opt.setValue(attribute.getName());
            
            multiSelect.add(opt);
        }
        
        if(getQuestionWidget() instanceof AbstractQuestionWidget){
            AbstractQuestionWidget questionWidget = (AbstractQuestionWidget) getQuestionWidget();
            if(questionWidget.isScoredType()){
                multiSelect.setValue(questionWidget.getScoringAttributesAsStringList());
            }
        }
        
        
        multiSelect.refresh();
        
    }
    
    /**
     * Sets the question container widget.  This is used for example to set the container to be a specific type such 
     * as a multiple choice widget, free response widget, etc.
     * 
     * @param widget - The widget that the question container will render.
     */
    private void setQuestionContainer(Widget widget) {
        questionContainer.clear();
        questionContainer.add(widget);
    }


    /**
     * Updates the visibility of the attribute container based on the
     * type of survey being edited and the current edit mode.
     */
    private void updateAttributeContainerVisibility() {
        attributeContainer.setVisible(false);
        Widget widget = getQuestionWidget();
        if (widget instanceof AbstractQuestionWidget) {
            AbstractQuestionWidget qWidget = (AbstractQuestionWidget)widget;
            if (qWidget.isScoredType() && 
                    getEditMode() == SurveyEditMode.ScoringMode && 
                    getSurveyType() == SurveyDialogOption.COLLECTINFO_SCORED) {
                
                attributeContainer.setVisible(true);
            } 
        }
    }

    /**
     * Sets the number for the question that will be displayed in the question number label.
     * 
     * @param qNumber - The number for the question.
     */
    public void setQuestionNum(int qNumber) { 
        questionNum.setText(Q_PREFIX + qNumber);
    }
    
    /**
     * Sets the label for the informative text that will be displayed in the survey item number label.
     */
    private void setInformationLabel() { 
        questionNum.setText(I_PREFIX);
    }
    
    /**
     * Accessor to get the widget id for the question container.
     * 
     * @return SurveyWidgetId - The widget id of the question container.
     */
    public SurveyWidgetId getWidgetId() {
        return widgetId;
    }

    /**
     * Accessor to retrieve if the question is selected.
     * 
     * @return boolean - true if the question is selected, false otherwise.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /** 
     * Accessor to set if the question is selected.
     * 
     * @param isSelected - true if the question is selected, false otherwise.
     */
    public void setSelected(boolean isSelected) {
        logger.info("setSelected(" + isSelected + ")");
        this.isSelected = isSelected;
        
        updateControlsVisibilityAndUsability();

        selectedCheckBox.setValue(isSelected);

        // get the widget of each type of question to set the placeholder response visibility
        if(getQuestionWidget() instanceof AbstractQuestionWidget){
            AbstractQuestionWidget questionWidget = (AbstractQuestionWidget) getQuestionWidget();
            if(questionWidget instanceof MultipleChoiceWidget) {
                MultipleChoiceWidget multipleChoice = (MultipleChoiceWidget) questionWidget;
                multipleChoice.setPlaceholderResponseVisible(isSelected);
            } else if(questionWidget instanceof MatrixOfChoicesWidget) {
                MatrixOfChoicesWidget matrixOfChoice = (MatrixOfChoicesWidget) questionWidget;
                matrixOfChoice.setPlaceholderResponseVisible(isSelected);
            } else if (questionWidget instanceof RatingScaleWidget) {
                RatingScaleWidget ratingScale = (RatingScaleWidget) questionWidget;
                ratingScale.setPlaceholderResponseVisible(isSelected);
            }
        }
        
        if(isSelected){
            rowContainer.addStyleName("surveyElementEditorSelected");
            
        } else {
            rowContainer.removeStyleName("surveyElementEditorSelected");
        }
    }

    
    /**
     * Gets the question widget for the question container.  The question container
     * should only ever contain one question widget.  The question widget is the
     * question specific widget (such as FreeResponseWidget, MultipleChoiceWidget, etc.)
     * 
     * @return Widget - The question widget for the selected question container widget.  Can return null.
     */
    public Widget getQuestionWidget() {
        Widget qWidget = null;
        
        if (questionContainer.getWidgetCount() > 0) {
            qWidget = questionContainer.getWidget(0);
        }
        
        return qWidget;
    }

    /**
     * Handler for when the edit mode is changed in the Survey Editor.
     * 
     * @param editMode - The new edit mode that was selected (writing or scoring).
     */
    public void onEditorModeChanged(SurveyEditMode editMode) {
        
        setEditMode(editMode);
        
        if (getQuestionWidget() instanceof AbstractQuestionWidget) {
            AbstractQuestionWidget scoredWidget = (AbstractQuestionWidget)(getQuestionWidget());
            
            scoredWidget.onEditorModeChanged(editMode);
            
            scoredWidget.refresh();
            
            conceptSelect.setEnabled(!scoredWidget.isReadOnly());
            conceptSelect.setShowActionsBox(!scoredWidget.isReadOnly());
            multiSelect.setEnabled(!scoredWidget.isReadOnly());
        }
        
        refresh();
    }

    /**
     * Refreshes the question container widgets.
     */
    private void refresh() {
        updateAttributeContainerVisibility();
        updateConceptContainerVisibility();
    }

    private void updateConceptContainerVisibility() {
        conceptContainer.setVisible(false);
        Widget widget = getQuestionWidget();
        if (widget instanceof AbstractQuestionWidget) {
            AbstractQuestionWidget qWidget = (AbstractQuestionWidget)widget;
            if (qWidget.isScoredType() && 
                    getEditMode() == SurveyEditMode.ScoringMode && 
                    getSurveyType() == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
                
                conceptContainer.setVisible(true);
            } 
        }
    }

    /**
     * Gets the current edit mode for the question container.
     * 
     * @return SurveyEditMode - The current mode of the question container (writing or scoring).
     */
    public SurveyEditMode getEditMode() {
        return editMode;
    }

    /**
     * Sets the current edit mode for the question container.
     * 
     * @param editMode - The current edit mode for the question container.
     */
    private void setEditMode(SurveyEditMode editMode) {
        logger.info("setEditMode(" + editMode + ")");
        this.editMode = editMode;
        updateControlsVisibilityAndUsability();
    }

    /**
     * Gets the survey type for the question container.
     * 
     * @return SurveyDialogOption - The current type of survey that is being edited for the question container.
     */
    public SurveyDialogOption getSurveyType() {
        return surveyType;
    }

    /**
     * Sets the survey type for the question container.
     * 
     * @param surveyType - The current type of survey that is being edited for the question container.
     */
    private void setSurveyType(SurveyDialogOption surveyType) {
        this.surveyType = surveyType;
    }

    /**
     * Computes the possible total points that can be given on a per attribute basis.
     * For example, if the question is associated with the attribute of "Anxious" then it's scoring values will
     * count towards the overall total, but also to the total for all questions that are set to "Anxious".
     *  
     * @param attributeScoreMap - The mapping of possible total points per attribute.
     */
    public void computeTotalPointsPerAttribute(HashMap<LearnerStateAttributeNameEnum, Double> attributeScoreMap) {
        
        List<LearnerStateAttributeNameEnum> attributes = getLearnerStatesForQuestion();
        logger.info("attributes size = " + attributes.size());
        for (LearnerStateAttributeNameEnum attribute : attributes) {
            Widget widget = getQuestionWidget();
            
            if (widget instanceof AbstractQuestionWidget) {
                
                AbstractQuestionWidget scoredWidget = (AbstractQuestionWidget)(widget);
                
                Double points = scoredWidget.getPossibleTotalPoints();
                
                if (points > 0.0) {
                    
                    Double existingPoints = attributeScoreMap.get(attribute);
                    
                    if (existingPoints != null) {
                        existingPoints += points;
                        attributeScoreMap.put(attribute, existingPoints);
                    } else {
                        attributeScoreMap.put(attribute, points);
                    }
                }
            }
            
        }
        
    }

    /** 
     * Returns the list of LearnerStateAttributeNameEnum values that are associated with the question.
     * Not all survey types require a question to have LearnerStateAttributeNameEnums associated with them.
     * 
     * @return List - list of LearnerStateAttributeNameEnum values associated with the question.  The list can be empty if there are no
     *                learner states associated with the question.
     */
    private List<LearnerStateAttributeNameEnum> getLearnerStatesForQuestion() {
        ArrayList<LearnerStateAttributeNameEnum> attributes = new ArrayList<LearnerStateAttributeNameEnum>();
        List<String> attributeStrList = multiSelect.getValue();
        for (String attributeStr : attributeStrList) {
            
            try {
                LearnerStateAttributeNameEnum learnerState = LearnerStateAttributeNameEnum.valueOf(attributeStr);
                
                attributes.add(learnerState);
                
            } catch (@SuppressWarnings("unused") EnumerationNotFoundException e) {
                
                // Ignore and do not try to add if the learner state attribute is not found.
                logger.info("Could not find attribute mapping: " + attributeStr);
            }
            
        }
        return attributes;
    }

    /**
     * Returns the possible total points for the question (if the question is scored or contains scoring values).
     * 
     * @return Double - The possible total points for the question.  0.0 is returned if the question is not scored.
     */
    public Double getPossibleTotalPoints() {
        
        Double totalPoints = 0.0;

        Widget widget = getQuestionWidget();
        if (widget instanceof AbstractQuestionWidget) {
            AbstractQuestionWidget scoredWidget = (AbstractQuestionWidget)(widget);
            totalPoints += scoredWidget.getPossibleTotalPoints();
        }

        return totalPoints;
        
    }

    /**
     * Sets the global survey resources (i.e. survey context, concepts, etc.) that should be used by this widget for authoring surveys
     * 
     * @param resources the survey resources
     */
    private void setSurveyResources(AbstractSurveyResources resources) {
        surveyResources = resources;
    }
    
    /**
     * Pre-selects the items in the concept list based on the default values.
     * 
     * @param isNew if this is a new question being added or a an existing one being loaded
     */
    private void preselectConceptsFromDefaults(boolean isNew) {
        List<String> conceptList = null;
        AbstractQuestionWidget questionWidget = (AbstractQuestionWidget) getQuestionWidget();
        
        //collect any selected course concepts for this question
        if(questionWidget != null && questionWidget.getConcepts() != null){
            if(((questionWidget.getConcepts().isEmpty()) && !isNew) || !questionWidget.getConcepts().isEmpty()){
                conceptList = questionWidget.getConcepts();
            }
        }
        
        if(conceptList == null){
            logger.info("Apply NO selected concepts for question widget.");

            //setting null here was causing a null ptr
            conceptSelect.setValue(new ArrayList<String>(0));
        }else{
            
            // #3340 - make question concepts lower case in order to ignore case sensitivity
            ListIterator<String> iterator = conceptList.listIterator();
            while (iterator.hasNext()){
                iterator.set(iterator.next().toLowerCase());
            }
            
            logger.info("Apply selected concepts of "+conceptList+" for question widget.");

            conceptSelect.setValue(conceptList, true);
        }
        
        conceptSelect.refresh();
       
    }
    
    /**
     * Populates the multi select dropdown with the available learner state attributes that can be chosen.
     */
    private void populateConceptSelectList() {
       
        conceptSelect.clear();
        if (surveyResources != null) {
            
            OptGroup courseOptionGroup = new OptGroup();
            if (surveyResources.getConcepts() != null) {
                List<String> conceptList = surveyResources.getConcepts();
                logger.info("Adding course concepts to question widget.  The course concepts are " + conceptList);
                for (String conceptName : conceptList ) {
                    
                    Option opt = new Option();
                    opt.setText(conceptName);
                    opt.setValue(conceptName.toLowerCase());  // #3340 - make course concepts lower case in order to ignore case sensitivity when comparing to question concepts
                    courseOptionGroup.add(opt);
                    
                }
                conceptSelect.add(courseOptionGroup);
            } else {
                logger.info("No course concepts from the survey were inherited from the course");
            }
            
            logger.info("Finished collecting course concepts.");
            
            AbstractQuestionWidget questionWidget = (AbstractQuestionWidget) getQuestionWidget();
            if(questionWidget == null){
                logger.warning("Unable to merge the course concepts with the current survey item widget concepts because the current survey item widget is null.");
            }else{
                List<String> qWidgetConcepts = questionWidget.getConcepts();
            
                if (qWidgetConcepts != null && !qWidgetConcepts.isEmpty()){
                    OptGroup questionOptionGroup = new OptGroup();
                    questionOptionGroup.setStyleName(style.groupLabel());
                    questionOptionGroup.setLabel(EXTRANEOUS_CONCEPTS_GROUP_LABEL);
                    logger.info("The current concepts for the question widget are " + qWidgetConcepts);
                    for (String conceptName : qWidgetConcepts ) {
                        
                        Option opt = new Option();
                        opt.setText(conceptName);
                        opt.setColor(EXTRANEOUS_CONCEPTS_GROUP_TEXT_COLOR);
                        opt.setValue(conceptName);
                        
                        for(Option option : conceptSelect.getItems()){
                            if(option.getValue().equalsIgnoreCase(opt.getValue())){
                                break;
                            }
                            if(conceptSelect.getItems().indexOf(option) == conceptSelect.getItems().size() - 1){
                                questionOptionGroup.add(opt);
                                courseOptionGroup.setStyleName(style.groupLabel());
                                courseOptionGroup.setLabel(COURSE_CONCEPTS_GROUP_LABEL);  // only add this label because there are extraneous concepts for 
                                                                                          // this survey item which will have a label for that section of listed concepts
                            }
                        }
                    }
                    
                    if(questionOptionGroup.getWidgetCount() != 0){
                        extraneousGroupItemIndex = conceptSelect.getWidgetCount();  //store for later use
                        conceptSelect.add(questionOptionGroup);
                    }
                    
                    //show/hide the extraneous concepts for this survey item error text
                    extraneousConceptsIcon.setVisible(questionOptionGroup.getWidgetCount() != 0);
                } else {
                    logger.info("No course concepts from the survey were inherited from the question because the current survey item widget's concept list is null.  Widget is "+questionWidget);
                }
            }
            
            
        } else {
            logger.severe("populateConceptSelectDefaults() - courseObjData is null.");
        }
        
        
        conceptSelect.refresh();
        
    }

    /**
     * Initializes the question container widget (should be called after constructing the object.)
     * 
     * @param widget - The specific question widget that the container will hold (multiple choice, essay, etc).
     * @param resources - The global set of survey resources (e.g. survey context, concepts, etc.) that this widget should use
     * @param questionCount - The question number for the question.
     * @param isNew - if this is a new question being added or an existing one being loaded
     */
    public void initialize(Widget widget, AbstractSurveyResources resources, int questionCount, boolean isNew) {
        setQuestionContainer(widget);
        setSurveyResources(resources);
        if(widget instanceof InformativeTextWidget) {
            setInformationLabel();
        } else {
            setQuestionNum(questionCount);
        }
        
        
        if (getSurveyType() == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
            // Only modify the concept container if the survey type is a dynamic question bank.
            // Populate the concept select list.
            logger.info("Populating concepts for question widget.");
            populateConceptSelectList();
            logger.info("Populating difficulties for question widget.");
            populateDifficultyList();
            
            // Pre-select the concepts in the list (currently all concepts are selected.)
            logger.info("Auto-selecting course concepts for question widget.");
            preselectConceptsFromDefaults(isNew);
        } else if (getSurveyType() == SurveyDialogOption.COLLECTINFO_SCORED) {
            // Only modify the attribute list if the survey type supports editing attributes.
            populateMultiSelectList();
        }
        
        logger.info("Refreshing question widget.");
        refresh();
        
    }

    private void populateDifficultyList() {
        if(getQuestionWidget() instanceof AbstractQuestionWidget){
            AbstractQuestionWidget questionWidget = (AbstractQuestionWidget) getQuestionWidget();
            
            if(questionWidget != null && questionWidget.getDifficulty() != null){
                difficultyList.setSelectedIndex(QuestionDifficultyEnum.VALUES().indexOf(QuestionDifficultyEnum.valueOf(questionWidget.getDifficulty())));
            } else {
                // Set difficulty to Easy by default
                difficultyList.setSelectedIndex(QuestionDifficultyEnum.VALUES().indexOf(QuestionDifficultyEnum.EASY));
            }
        }
    }

    /**
     * Creates a new AbstractSurveyElement from this widget and adds it to the
     * given list.
     * 
     * @param surveyElements - The surveyElement list that will be saved to the
     * database.
     * @param editableToUsernames - gift usernames that have write permissions
     * to this survey question.
     * @param visibleToUsernames - gift usernames that have read permissions to
     * this survey question.
     * @throws SaveSurveyException if there is an error while saving the survey
     */
    public void save(List<AbstractSurveyElement> surveyElements, HashSet<String> editableToUsernames, HashSet<String> visibleToUsernames) throws SaveSurveyException {
        
        try {
            Widget widget = this.getQuestionWidget();
            
            // TODO - Fill in the question categories. (once the authoring tool allows this to be authored again)
            List<String> categories = new ArrayList<String>();
            
            AbstractQuestionWidget absWidget = null;
            
            AbstractSurveyElement absElement = null;
    
            if (widget instanceof AbstractQuestionWidget) {
                absWidget = (AbstractQuestionWidget) widget;
                
                if (absWidget instanceof InformativeTextWidget) {
                    // This is an instance of a TextElement not a Question Element.
                    absElement = new TextSurveyElement(0, 0, new SurveyItemProperties());
                } else {
                    
                    QuestionTypeEnum questionType = null;
                    
                    if (absWidget instanceof EssayWidget) {
                        questionType = QuestionTypeEnum.ESSAY;
                    } else if (absWidget instanceof FreeResponseWidget) {
                        questionType = QuestionTypeEnum.FREE_RESPONSE;
                    }  else if (absWidget instanceof MultipleChoiceWidget) {
                        questionType = QuestionTypeEnum.MULTIPLE_CHOICE;
                    }  else if (absWidget instanceof TrueFalseWidget) {
                        questionType = QuestionTypeEnum.TRUE_FALSE;
                    }  else if (absWidget instanceof RatingScaleWidget) {
                        questionType = QuestionTypeEnum.RATING_SCALE;
                    }  else if (absWidget instanceof MatrixOfChoicesWidget) {
                        questionType = QuestionTypeEnum.MATRIX_OF_CHOICES;
                    }  else if (absWidget instanceof SliderBarWidget) {
                        questionType = QuestionTypeEnum.SLIDER_BAR;
                    }  else {
                    
                        logger.severe("Add save support for widget of type: " + absWidget.getClass().getName());
                        throw new SaveSurveyException("Trying to save widget of type "  + absWidget.getClass().getName() +
                                                      " but there is no save handler implemented for that type.  Implement the save " +
                                                      " handler to fix this error.");
                    }
                   
                    // Create the question based on the type enum specified above and create the abstract survey question.
                    AbstractQuestion absQuestion = AbstractQuestion.createQuestion(questionType, 0, "", new SurveyItemProperties(), 
                           categories, visibleToUsernames, editableToUsernames);
    
                    absElement = AbstractSurveyQuestion.createSurveyQuestion(0, 0, absQuestion, new SurveyItemProperties());
                }
              
    
                if (absElement != null) {
                    
                    absWidget.save(absElement);
                    absElement.setWidgetId(widgetId.getWidgetId());
                    surveyElements.add(absElement);
                    
                } else {
                    logger.severe("Unable to save the survey, encountered a null AbstractSurveyElement.");
                    throw new SaveSurveyException("Encountered a null AbstractSurveyElement when saving the survey.");
                } 
            }
        } catch(Exception e) {
            
            if(e instanceof SaveSurveyException) {
                throw e;
                
            } else {
                
                //as a precaution, catch uncaught exceptions here so that the user doesn't get stuck
                throw new SaveSurveyException("Encountered an unexpected error while saving", e);
            }
        }
    }
    
    /**
     * Sets whether or not this widget should be read-only
     * 
     * @param readOnly whether or not this widget should be read-only
     */
    public void setReadOnlyMode(boolean readOnly) {
        logger.info("setReadOnlyMode(" + readOnly + ")");
        this.readOnly = readOnly;
        updateControlsVisibilityAndUsability();
}
    
    /**
     * Sets whether or not any user interaction with this widget will be
     * handled. If this widget is disabled, users won't be able to select it or
     * edit any of its contents.
     * 
     * @param enabled True to enable, false to disable
     */
    public void setEnabled(boolean enabled){
        
        disableBlocker.setVisible(!enabled);
    }

    /**
     * Updates the visibility of the controls contained within the
     * {@link QuestionContainerWidget} based on the state of the
     * {@link QuestionContainerWidget}
     */
    private void updateControlsVisibilityAndUsability() {
        logger.info("updateControlsVisibilityAndUsability()");

        boolean canReorder = !readOnly && surveyType != SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK;
        boolean isScoringMode = editMode == SurveyEditMode.ScoringMode;

        moveUpButton.setVisible(canReorder && isSelected);
        moveDownButton.setVisible(canReorder && isSelected);
        copyButton.setVisible(!readOnly && isSelected && !isScoringMode);
        deleteButton.setVisible(!readOnly && isSelected);

        insertBefore.setVisible(canReorder && isSelected && !isScoringMode);
        insertAfter.setVisible(canReorder && isSelected && !isScoringMode);

        questionBlocker.setVisible(readOnly);

        //only enable/disable the multi select if necessary, since enabling/disabling causes a refresh that hides the selection dropdown
        if(!readOnly && isScoringMode) {
            
            if(!multiSelect.isEnabled()) {
                multiSelect.setEnabled(true);
            }
            
        } else {
            
            if(multiSelect.isEnabled()) {
                multiSelect.setEnabled(false);
            }
        }
    }
}