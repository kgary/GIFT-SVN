/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.OperatorEnum;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.ReturnValueCondition;
import mil.arl.gift.common.survey.score.SurveyScorer;
import mil.arl.gift.common.survey.score.TotalScorer;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyScrollToElementEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.widgets.CollapseButton;

/**
 * The ScoringLogicWidgetCuiS is used for the Collect User Info Scored (CuiS) survey to 
 * control the rules of how the survey will be scored.
 * 
 * @author nblomberg
 *
 */
public class ScoringLogicWidgetCuiS extends Composite  {

    private static Logger logger = Logger.getLogger(ScoringLogicWidgetCuiS.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, ScoringLogicWidgetCuiS> {
	}

	/** contains all the individual scoring rule panels */
	@UiField
	Container rulesContainer;
	
	@UiField
    CollapseButton collapseButton;
	
	@UiField
	protected BlockerPanel rulesBlocker;
	
    @UiField
    MultipleSelect attributeSelect;
    
    @UiHandler("attributeSelect")
    void onValueChangeAttributeSelect(ValueChangeEvent<List<String>> event) {
        logger.info("onValueChangeAttributeSelect triggered: " + event.getValue());
        
        //use set to avoid duplicate learner state attributes
        Set<String> existingAttributes = new HashSet<String>(event.getValue().size());
        
        //
        // Delete any widgets for de-selected learner state attributes
        //
        for (int x=0; x < rulesContainer.getWidgetCount(); x++) {
            
            Widget widget = rulesContainer.getWidget(x);
            
            if (widget instanceof ScoringLogicAttributeWidget) {
                
                ScoringLogicAttributeWidget attributeWidget = (ScoringLogicAttributeWidget)widget;
                LearnerStateAttributeNameEnum attribute = attributeWidget.getAttribute();
                if(!event.getValue().contains(attribute.getName())){
                    //remove this widget
                 
                    AttributeScorerProperties props = widgetToAttributeMap.get(attributeWidget);
                    removeAttributeFromTotalScorer(props);
                    removeAttributeFromAttributeScorer(props);
                    
                    widgetToAttributeMap.remove(widget);
                    attributeWidget.deleteLogicRule();
                    x--;
                    continue;
                }
                
                //add to list of attributes that were found, used
                //to make sure a widget exists for this attribute still
                existingAttributes.add(attribute.getName());
            }
        }
        
        //
        // Create widgets for any selected learner state attributes that don't have a widget already
        //
        ScoringLogicAttributeWidget firstCreatedWidget = null;
        for(String selectedAttribute : event.getValue()){
            
            if(existingAttributes.contains(selectedAttribute)){
                //a widget exist for this selected attribute
                continue;
            }
            
            try{
                ScoringLogicAttributeWidget createdWidget = createAndInitializeScoringLogicAttributeWidget(LearnerStateAttributeNameEnum.valueOf(selectedAttribute));
                if(firstCreatedWidget == null){
                    firstCreatedWidget = createdWidget;
                }
            }catch(Exception e){
                logger.log(Level.WARNING, "Failed to find the learner state attribute enum for the selected learner state attribute name of "+selectedAttribute, e);
            }
        }
        
        //if one or more widgets were created, scroll to the first one created
        if(firstCreatedWidget != null){
            SharedResources.getInstance().getEventBus().fireEvent(new SurveyScrollToElementEvent(firstCreatedWidget.getSurveyWidgetId().getWidgetId()));
            SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
        }
        
        SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
    }
	
	/** The last initialized scoring logic attribute widget */
	private ScoringLogicAttributeWidget widget;
	
	/**
	 * The Survey being loaded into the widget (also represents 
	 * the newly created survey object when authoring a new survey)
	 */
	private Survey loadedSurvey;
	
	/**
	 * Sets whether the Scoring Logic attributes are enabled for editing
	 */
	private Boolean isReadOnly;
	
	/** Contains a mapping of the scoring logic widget to it's associated Scoring Logic property object.
	 *  The object is only valid as long as the loadedSurvey is not changed.
	 */
	private HashMap<ScoringLogicAttributeWidget, AttributeScorerProperties> widgetToAttributeMap = new HashMap<ScoringLogicAttributeWidget, AttributeScorerProperties>();

	/**
	 * Constructor (default)
	 */
	public ScoringLogicWidgetCuiS() {
	    
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));	    
	    
	    widgetToAttributeMap.clear();	   
	}
	
	/**
	 * Inserts a new scoring widget into the scoring rules container
	 * 
	 * @param attribute the learner state attribute to create a widget for
	 * @return the newly created widget
	 */
	ScoringLogicAttributeWidget addScoringLogicAttributeWidget(LearnerStateAttributeNameEnum attribute) {
	    
	    ScoringLogicAttributeWidget widget = new ScoringLogicAttributeWidget(attribute);
	    
	    logger.info("Inserting scoring logic widget before index "+rulesContainer.getWidgetCount());
        rulesContainer.insert(widget,  rulesContainer.getWidgetCount());
        
        return widget;
	}
	
    /**
     * Populates the multi select dropdown with the available learner state attributes that can be chosen.
     */
    private void populateAttributeSelectList() {
       
        for (LearnerStateAttributeNameEnum attribute : LearnerStateAttributeNameEnum.SORTED_VALUES()) {
            
            //currently not supporting these learner state attributes in this survey type
            if(LearnerStateAttributeNameEnum.LEARNING_STYLE.equals(attribute) || 
                    LearnerStateAttributeNameEnum.SELF_EFFICACY.equals(attribute) ||
                    LearnerStateAttributeNameEnum.GOAL_ORIENTATION.equals(attribute) ||
                    LearnerStateAttributeNameEnum.LOCUS_OF_CONTROL.equals(attribute)){
                continue;
            }
            
            Option opt = new Option();
            opt.setText(attribute.getDisplayName());
            opt.setValue(attribute.getName());
            
            attributeSelect.add(opt);
        }        
        
        attributeSelect.refresh();        
    }
	
	/**
	 * Creates and initializes the scoring logic attribute widget.  The widget
	 * is defaulted to assessing on Total for the attribute of Knowledge.
	 * The survey totalScorer property is updated appropriately as well.
	 * 
	 * @param attribute the learner state attribute to create a widget for
	 * @return The ScoringLogicAttributeWidget that was created.
	 */
	ScoringLogicAttributeWidget createAndInitializeScoringLogicAttributeWidget(LearnerStateAttributeNameEnum attribute) {
	    
	    // Create a new attribute scorer property that defaults to the total scorer.
        AttributeScorerProperties attributeProperties = new AttributeScorerProperties(attribute, 
        		SurveyEditorPanel.createReturnValue(attribute));
        loadedSurvey.getProperties().getSurveyScorer().getTotalScorer().getAttributeScorers().add(attributeProperties);
        ScoringLogicAttributeWidget widget = initializeNewWidget(attributeProperties);
                
        return widget;
	}
	
	/**
     * Sets the visibility of the rules panel.
     * 
     * @param visible - true to show the rules panel, false to hide it.
     */
    private void setRulesPanelVisibility(boolean visible) {
        rulesContainer.setVisible(visible);
    }
	
	 /**
     * Refreshes the widget based on the current state values.
     */
    public void refresh() {
        for (int x=0; x < rulesContainer.getWidgetCount(); x++) {
            Widget widget = rulesContainer.getWidget(x);
            if (widget instanceof ScoringLogicAttributeWidget) {
                ScoringLogicAttributeWidget logicWidget = (ScoringLogicAttributeWidget)widget;
                logicWidget.refresh();
            }
        }       
    }

    /**
     * Set the possible total points to be displayed in the scoring header.
     * 
     * @param totalPoints - The total points to be displayed.
     */
    public void setPossibleTotalPoints(Double totalPoints) {
        logger.info("widgetToAttributeMap size = " + widgetToAttributeMap.size());
        for (int x=0; x < rulesContainer.getWidgetCount(); x++) {
            Widget widget = rulesContainer.getWidget(x);
            if (widget instanceof ScoringLogicAttributeWidget) {
                ScoringLogicAttributeWidget logicWidget = (ScoringLogicAttributeWidget)widget;
                
                if (!logicWidget.isScoredOnSameAttribute()) {
                    logicWidget.setPossibleTotalPoints(totalPoints);
                    
                    
                    AttributeScorerProperties props = widgetToAttributeMap.get(logicWidget);
                    
                    if (props != null) {
                        if(props.getReturnConditions().size() < 2){
                        	props.setReturnConditions(SurveyEditorPanel.createReturnValue(props.getAttributeType()));
                        	
                        } else if (props.getReturnConditions().size() == 2){
                        	
                            Double newValue;
                            if(totalPoints != 0) {
                            	newValue = Math.rint((props.getReturnConditions().get(0).getValue() / totalPoints) * 100);
                            } else {
                            	newValue = props.getReturnConditions().get(0).getValue();
                            }                            
                            logicWidget.setCurrentValue(newValue);
                            
                        } else if(props.getReturnConditions().size() == 3){
                        	
                            Range range;                            
                            if(totalPoints != 0) {
                            	range = new Range(Math.rint((props.getReturnConditions().get(1).getValue() / totalPoints) * 100), Math.rint((props.getReturnConditions().get(0).getValue() / totalPoints) * 100));
                            } else {
                            	range = new Range(Math.rint(props.getReturnConditions().get(1).getValue()), Math.rint(props.getReturnConditions().get(0).getValue()));
                            }                            
                            if(range.getMinValue() == 0 && range.getMaxValue() == 0){
                                range = new Range(ScoringLogicAttributeWidget.DEFAULT_MIN_RANGE, ScoringLogicAttributeWidget.DEFAULT_MAX_RANGE);
                            }
                            
                            logicWidget.setCurrentRange(range);
                        }
                    } else {
                        logger.severe("Unable to set the total points.  The attributeScorerProperties was null for the logic widget.");
                    }
                    
                }
            }
        }
        
    }

    /**
     * Initializes the component back to default values.
     * 
     * @param loadedSurvey - The loaded survey to be initialized
     * @param isNewSurvey - If the survey is a new or existing survey
     * @param isReadOnly - Determines if the survey should be editable
     */
    public void initialize(Survey loadedSurvey, boolean isNewSurvey, boolean isReadOnly) {
    	logger.info("initializing container widget");
    	
    	this.isReadOnly = isReadOnly;

    	widgetToAttributeMap.clear();
    	this.loadedSurvey = loadedSurvey;
    	
        rulesContainer.clear();
        attributeSelect.clear();
        populateAttributeSelectList();
        
        if(isNewSurvey){
            //start a new survey of this type with knowledge learner state scoring rule, instead of no rules
            attributeSelect.setValue(Arrays.asList(LearnerStateAttributeNameEnum.KNOWLEDGE.getName()), true);
        }
    }

    /**
     * Sets the total points per attribute for any scoring logic attribute.
     * 
     * @param attributeScoreMap - Map that contains the possible total points per attribute.
     */
    public void setTotalPointsPerAttribute(HashMap<LearnerStateAttributeNameEnum, Double> attributeScoreMap) {
        for (int x=0; x < rulesContainer.getWidgetCount(); x++) {
            Widget widget = rulesContainer.getWidget(x);
            if (widget instanceof ScoringLogicAttributeWidget) {
                ScoringLogicAttributeWidget logicWidget = (ScoringLogicAttributeWidget)widget;
                
                if (logicWidget.isScoredOnSameAttribute()) {
                    logicWidget.setTotalPointsPerAttribute(attributeScoreMap);
                    
                    AttributeScorerProperties props = widgetToAttributeMap.get(logicWidget);
                    
                    if (props != null) {
                        if(props.getReturnConditions().size() < 2){
                        	props.setReturnConditions(SurveyEditorPanel.createReturnValue(props.getAttributeType()));
                        } else if (props.getReturnConditions().size() == 2){
                        	
                        	Double newValue;
                        	if(logicWidget.getPossibleTotalPoints() != 0) {
                        		newValue = Math.rint((props.getReturnConditions().get(0).getValue() / logicWidget.getPossibleTotalPoints()) * 100);	
                        	} else {
                        		newValue = props.getReturnConditions().get(0).getValue();
                        	}                            
                            logicWidget.setCurrentValue(newValue);
                        } else if(props.getReturnConditions().size() == 3){
                        	
                        	Range range;
                        	if(logicWidget.getPossibleTotalPoints() != 0) {
                        		range = new Range(Math.rint((props.getReturnConditions().get(1).getValue() / logicWidget.getPossibleTotalPoints()) * 100), 
                        						  Math.rint((props.getReturnConditions().get(0).getValue() / logicWidget.getPossibleTotalPoints()) * 100));
                        	} else {
                        		range = new Range(ScoringLogicAttributeWidget.DEFAULT_MIN_RANGE, ScoringLogicAttributeWidget.DEFAULT_MAX_RANGE);
                        	}
                            logicWidget.setCurrentRange(range);
                        }
                    }
                    
                }
            }
        }
    }

    /**
     * Called when an existing survey is loaded up with scoring attributes.
     * creates a scoring widget for each scoring attribute
     * 
     * @param survey the existing survey being loaded
     */
	public void load(Survey survey) {
	    logger.info("load()");
		this.loadedSurvey = survey;
		
		if (loadedSurvey != null && loadedSurvey.getProperties().getSurveyScorer() != null) {
		    logger.info("checking survey scorer");
		    SurveyScorer surveyScorer = loadedSurvey.getProperties().getSurveyScorer();

		    List<AttributeScorerProperties> attributes = new ArrayList<AttributeScorerProperties>();
		            
		    loadTotalScorers(surveyScorer, attributes);
		    loadAttributeScorers(surveyScorer, attributes);
		    
            if (attributes.isEmpty()) {
                // Handles loading surveys created in the old SAS with scoring enabled and no attributes
                logger.info("Scorer Attributes are empty, adding blank Knowledge attribute");
                attributeSelect.setValue(Arrays.asList(LearnerStateAttributeNameEnum.KNOWLEDGE.getName()), true);
            } else {
                // sort on attribute display name so they always appear in the same order
                Collections.sort(attributes, new Comparator<AttributeScorerProperties>() {
                    @Override
                    public int compare(AttributeScorerProperties attrib1, AttributeScorerProperties attrib2) {
                        
                        String dn1 = attrib1.getAttributeType().getDisplayName();
                        String dn2 = attrib2.getAttributeType().getDisplayName();
                        
                        return dn1.compareTo(dn2);
                    }
                });
                
                //collect list of learner state attributes with scoring rules in the loaded survey
                List<String> attributesToSelect = new ArrayList<String>(attributes.size());
                
                // always scroll to the first widget on the page
                ScoringLogicAttributeWidget firstWidget = null;
                for (AttributeScorerProperties key : attributes) {
                    
                    if(attributesToSelect.contains(key.getAttributeType().getName())){
                        //don't create duplicate
                        continue;
                    }
                    
                    attributesToSelect.add(key.getAttributeType().getName());
                    
                    ScoringLogicAttributeWidget widget = initializeNewWidget(key);
                    if (firstWidget == null) {
                        firstWidget = widget;
                    }
                }
                
                attributeSelect.setValue(attributesToSelect, false);

                if (firstWidget != null) {
                    SharedResources.getInstance().getEventBus().fireEvent(new SurveyScrollToElementEvent(firstWidget.getSurveyWidgetId().getWidgetId()));
                }
            }
	    }
	}
	
    /**
     * Loads the AttributeScorers from the survey (if any are found). The
     * AttributeScorers contain the logic rules for any attribute that is set to
     * be scored (at the survey level) on "same attribute" rather than "total".
     * 
     * @param surveyScorer
     *            The SurveyScorer object containing the logic rules.
     * @param attributes
     *            the list to populate with the total scorers
     */
    public void loadAttributeScorers(SurveyScorer surveyScorer, List<AttributeScorerProperties> attributes) {
        logger.info("loadAttributeScorers()");

        if (surveyScorer.getAttributeScorers() != null && !surveyScorer.getAttributeScorers().isEmpty()) {
            for (AttributeScorerProperties attribute : surveyScorer.getAttributeScorers()) {
                logger.info("Adding Attribute Scorer for attribute: " + attribute.getAttributeType());
                attributes.add(attribute);
            }
        }
    }

    /**
     * Loads the TotalScorers from the survey (if any are found). The
     * TotalScorers contain the logic rules for any attribute that is set to be
     * scored (at the survey level) on "total" rather than "same attribute".
     * 
     * @param surveyScorer
     *            The SurveyScorer object containing the logic rules.
     * @param attributes
     *            the list to populate with the total scorers
     */
    public void loadTotalScorers(SurveyScorer surveyScorer, List<AttributeScorerProperties> attributes) {
        logger.info("loadTotalScorers()");

        if (surveyScorer.getTotalScorer() != null) {

            TotalScorer totalScorer = surveyScorer.getTotalScorer();

            if (totalScorer.getAttributeScorers() != null) {
                for (AttributeScorerProperties attribute : totalScorer.getAttributeScorers()) {
                    logger.info("Adding Total Scorer for attribute: " + attribute.getAttributeType());
                    attributes.add(attribute);
                }
            }

        }
    }
	
	/**
	 * Initializes a new widget for a scoring attribute, populating the value such as the slider bar range
	 * and the type of attribute, etc. Also sets up all the change handlers to update the survey's properties
	 * as the values are adjusted.
	 * 
	 * @param attribute The attribute to create a widget for
	 * 
	 * @return the final widget that was created and initialized
	 */
	private ScoringLogicAttributeWidget initializeNewWidget(final AttributeScorerProperties attribute){
		logger.info("initializing widget for " + attribute.getAttributeType().getDisplayName());
		final ScoringLogicAttributeWidget widget;
		widget = addScoringLogicAttributeWidget(attribute.getAttributeType());
		logger.info("created scoring logic attribute widget...");
		
		widgetToAttributeMap.put(widget,  attribute);

		widget.getRangeScoringSlider().addSlideStopHandler(new SlideStopHandler<Range>(){

			@Override
			public void onSlideStop(SlideStopEvent<Range> event) {
				attribute.getReturnConditions().clear();
				attribute.getReturnConditions().add(new ReturnValueCondition(OperatorEnum.GTE, (event.getValue().getMaxValue() * widget.getPossibleTotalPoints()) / 100, attribute.getAttributeType().getAttributeAuthoredValues().get(2)));
				attribute.getReturnConditions().add(new ReturnValueCondition(OperatorEnum.GTE, (event.getValue().getMinValue() * widget.getPossibleTotalPoints()) / 100, attribute.getAttributeType().getAttributeAuthoredValues().get(1)));
				attribute.getReturnConditions().add(new ReturnValueCondition(OperatorEnum.GTE, 0, attribute.getAttributeType().getAttributeAuthoredValues().get(0)));
			}

		});
		
		widget.getSimpleScoringSlider().addSlideStopHandler(new SlideStopHandler<Double>(){

			@Override
			public void onSlideStop(SlideStopEvent<Double> event) {
				attribute.getReturnConditions().clear();
				attribute.getReturnConditions().add(new ReturnValueCondition(OperatorEnum.GTE, (event.getValue() * widget.getPossibleTotalPoints()) / 100, attribute.getAttributeType().getAttributeAuthoredValues().get(1)));
				attribute.getReturnConditions().add(new ReturnValueCondition(OperatorEnum.GTE, 0, attribute.getAttributeType().getAttributeAuthoredValues().get(0)));
			}
			
		});
		
		widget.getScoredOnList().setSelectedIndex(hasAttributeInTotalScorerByEnum(attribute.getAttributeType()) ? 0 : 1);
		widget.getScoredOnList().addChangeHandler(new ChangeHandler(){

			@Override
			public void onChange(ChangeEvent event) {
			    logger.info("onChange() - Selected attribute = " + widget.getAttribute().getDisplayName());

	            if(widget.getScoredOnList().getSelectedIndex() == 0 && !hasAttributeInTotalScorer(attribute)){
                    addAttributeToTotalScorer(attribute);
                    removeAttributeFromAttributeScorer(attribute);
                } else if(widget.getScoredOnList().getSelectedIndex() == 1){
                    removeAttributeFromTotalScorer(attribute);
                    addAttributeToAttributeScorer(attribute);
                }
				
			}
			
		});
		widget.updateScoringLevelDescriptions(attribute.getAttributeType());
		
      collapseButton.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                logger.info("scoring logic widget clicked. - set visiblity to "+!collapseButton.isCollapsed());
                
                setRulesPanelVisibility(!collapseButton.isCollapsed());
            }
      });
		
        SharedResources.getInstance().getEventBus().fireEvent(new SurveyScrollToElementEvent(widget.getSurveyWidgetId().getWidgetId()));
        SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
        
        if(this.widget != null){
            // a previous instance of the widget exists, save the 'show percentages' check box
            // value before setting it to a new instance.  Then use that value on the new instance to maintain
            // state.  This is because the survey composer is reloaded upon saving.
            boolean showPercentages = this.widget.isShowPercentages();
            widget.setShowPercentages(showPercentages);
        }
		
		this.widget = widget;
		widget.setEnabled(!isReadOnly);
		setReadOnlyMode(isReadOnly);
		
		logger.info("finished creating");
		
	    return widget;
	}
	
	/**
     * Checks if the total scorer contains the current attribute, checks by the type, not the
     * actual object since the objects won't be the same after saving/loading etc. 
     * 
     * @param type the type of attribute to check for
     * @return true if present, false otherwise
     */
    private boolean hasAttributeInTotalScorerByEnum(LearnerStateAttributeNameEnum type){
        if(loadedSurvey.getProperties().getSurveyScorer() != null){
            for(AttributeScorerProperties attribute : loadedSurvey.getProperties().getSurveyScorer().getTotalScorer().getAttributeScorers()){
                if(attribute.getAttributeType().equals(type)){
                    return true;
                }
            }
        }
        return false;
    }

	/**
	 * Checks if the total scorer contains the current attribute, checks by the type, not the
	 * actual object since the objects won't be the same after saving/loading etc. 
	 * 
	 * @param type the type of attribute to check for
	 * @return true if present, false otherwise
	 */
	private boolean hasAttributeInTotalScorer(AttributeScorerProperties attributeProps){
		if(loadedSurvey.getProperties().getSurveyScorer().getTotalScorer() != null){
		    TotalScorer totalScorer = loadedSurvey.getProperties().getSurveyScorer().getTotalScorer();
		    return totalScorer.getAttributeScorers().contains(attributeProps);
		}
		return false;
	}
	
	/**
     * Checks if the attribute scorer contains the current attribute, checks by the type, not the
     * actual object since the objects won't be the same after saving/loading etc. 
     * 
     * @param type the type of attribute to check for
     * @return true if present, false otherwise
     */
	private boolean hasAttributeInAttributeScorer(AttributeScorerProperties attributeProps) {
	    if(loadedSurvey.getProperties().getSurveyScorer() != null){
            SurveyScorer surveyScorer = loadedSurvey.getProperties().getSurveyScorer();
            return surveyScorer.getAttributeScorers().contains(attributeProps);
        }
        return false;
	}
	
	/**
	 * Removes the attribute with the specified type from the total scorer. If the type is not
	 * present then nothing will happen.
	 *  
	 * @param attributeProps the attribute to remove from the total scorer
	 */
	private void removeAttributeFromTotalScorer(AttributeScorerProperties attributeProps){
	    if (loadedSurvey.getProperties().getSurveyScorer().getTotalScorer() != null) {
            TotalScorer totalScorer = loadedSurvey.getProperties().getSurveyScorer().getTotalScorer();
            logger.info("Revmoing attribute "+attributeProps.getAttributeType().getName()+" from collection of total scorers.");
            totalScorer.getAttributeScorers().remove(attributeProps);
	    }
	    
		
	}
	
	/**
     * Removes the attribute with the specified type from the attribute scorer. If the type is not
     * present then nothing will happen.
     *  
     * @param attributeProps the attribute to remove from the attribute scorer
     */
	private void removeAttributeFromAttributeScorer(AttributeScorerProperties attributeProps) {
	    if (loadedSurvey.getProperties().getSurveyScorer().getAttributeScorers() != null) {
            SurveyScorer surveyScorer = loadedSurvey.getProperties().getSurveyScorer();
            logger.info("Removing attribute "+attributeProps.getAttributeType().getName()+" from collection of attribute scorers.");
            surveyScorer.getAttributeScorers().remove(attributeProps);
	    } 
	    
	}
	
	/**
     * Adds an AttributeScorerProperties object to the TotalScorer (if it doesn't already exist).
     * 
     * The TotalScorer contains a list of all logic rules based on the Total as opposed to 
     * SameAttribute.
     * 
     * @param attributeProps The AttributeScorerProperties object to add to the AttributeScorer.
     */
	private void addAttributeToTotalScorer(AttributeScorerProperties attributeProps) {
	    if (loadedSurvey.getProperties().getSurveyScorer().getTotalScorer() != null) {
	        TotalScorer totalScorer = loadedSurvey.getProperties().getSurveyScorer().getTotalScorer();
	        
	        if (!hasAttributeInTotalScorer(attributeProps)) {
	            logger.info("Adding attribute "+attributeProps.getAttributeType().getName()+" to collection of total scorers.");
	            totalScorer.getAttributeScorers().add(attributeProps);
	        }
	        
	    }
	        
	}
	
	/**
	 * Adds an AttributeScorerProperties object to the AttributeScorer (if it doesn't already exist).
	 * 
	 * The AttributeScorer contains a list of all attribute based logic rules (as opposed to logic 
	 * rules based on Total.
	 * 
	 * @param attributeProps The AttributeScorerProperties object to add to the AttributeScorer.
	 */
	private void addAttributeToAttributeScorer(AttributeScorerProperties attributeProps) {
        if (loadedSurvey.getProperties().getSurveyScorer().getAttributeScorers() != null) {
            SurveyScorer surveyScorer = loadedSurvey.getProperties().getSurveyScorer();
            
            if (!hasAttributeInAttributeScorer(attributeProps)) {
                logger.info("Adding attribute "+attributeProps.getAttributeType().getName()+" to collection of attribute scorers.");
                surveyScorer.getAttributeScorers().add(attributeProps);
            }
        }            
    }
	    
	/**
   	 * Sets whether or not this widget should be read-only
   	 * 
   	 * @param readOnly whether or not this widget should be read-only
   	 */
   	public void setReadOnlyMode(boolean readOnly) {
   	    
   	    this.isReadOnly = readOnly;
   		
   	    attributeSelect.setEnabled(!readOnly);
   	    attributeSelect.setShowActionsBox(!readOnly);
   		
   		if (this.widget != null) {
   		    widget.setEnabled(!readOnly);
   		}
    }
}
