/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.wizard;

import generated.ped.Attribute;
import generated.ped.EMAP;
import generated.ped.MetadataAttribute;
import generated.ped.MetadataAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.PedagogyConfigurationEditorUtility;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wizard-like Dialog Box that walks the user through constructing a new
 * Attribute (generated.ped.Attribute).
 * @author elafave
 */
public class BuildAttributeDialog extends DialogBox {
	
	/** The ui binder. */
    interface BuildAttributeDialogUiBinder extends UiBinder<Widget, BuildAttributeDialog> {} 
	private static BuildAttributeDialogUiBinder uiBinder = GWT.create(BuildAttributeDialogUiBinder.class);
	
	@UiField
	protected SelectQuadrantPane selectQuadrantPane;
	
	@UiField
	protected SelectLearnerStatePane selectLearnerStatePane;
	
	@UiField
	protected SelectMetadataAttributesPane selectMetadataAttributesPane;

	@UiField
	protected Label finishedPane;
	
	@UiField
	protected Button previousButton;
	
	@UiField
	protected Button nextButton;
	
	@UiField
	protected Button finishButton;
	
	@UiField
	protected Button cancelButton;
	
	@UiField
	protected Label stepLabel;
	
	private int totalSteps = 3;
	private int currentStep = 1;
	
	private HashMap<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>> possibleLearnerStateSelectionsForRule;
	
	private HashMap<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>> possibleLearnerStateSelectionsForExample;
	
	private HashMap<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>> possibleLearnerStateSelectionsForRecall;
	
	private HashMap<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>> possibleLearnerStateSelectionsForPractice;
	
	
    public BuildAttributeDialog(final AttributeBuiltHandler callback) {
    	setWidget(uiBinder.createAndBindUi(this));
    	setGlassEnabled(true);
    	
    	ClickHandler previousHandler = new ClickHandler() {
    		@Override
    		public void onClick(ClickEvent arg0) {
    			currentStep--;
    			back();
    		}
    	};
    	previousButton.addClickHandler(previousHandler);
    	
    	ClickHandler nextHandler = new ClickHandler() {
    		@Override
    		public void onClick(ClickEvent arg0) {
    			currentStep++;
    			next();
    		}
    	};
    	nextButton.addClickHandler(nextHandler);
    	
    	ClickHandler finishHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				MerrillQuadrantEnum quadrant = selectQuadrantPane.getQuadrant();
				Attribute attribute = constructAttribute();
				callback.onAttributeBuilt(quadrant, attribute);
				hide();
			}
    	};
    	finishButton.addClickHandler(finishHandler);
        
        ClickHandler cancelHandler = new ClickHandler(){
        	@Override
			public void onClick(ClickEvent arg0) {
        		hide();
			}
		};
        cancelButton.addClickHandler(cancelHandler);
    }
    
    /**
     * Examines the supplied Pedagogy Configuration to determine which
     * permutations of Quadrant/Learner State/Desciptive Enum Values the 
     * pedogogyConfiguration's attributes already represent. With this
     * knowledge the user is then prevented from creating a duplicate
     * Attribute.
     * @param pedagogyConfiguration PedagogyConfiguration
     * @return False if all permutations have already been defined, True
     * otherwise.
     */
    public boolean setExistingPedagogyConfiguration(EMAP pedagogyConfiguration) {
    	//Determine which Attribute permutations can be selected for each quadrant.
    	List<Attribute> ruleAttributes = pedagogyConfiguration.getRule().getAttributes().getAttribute();
    	possibleLearnerStateSelectionsForRule = PedagogyConfigurationEditorUtility.getRemainingAttributePermutations(ruleAttributes);

    	List<Attribute> exampleAttributes = pedagogyConfiguration.getExample().getAttributes().getAttribute();
    	possibleLearnerStateSelectionsForExample = PedagogyConfigurationEditorUtility.getRemainingAttributePermutations(exampleAttributes);

    	List<Attribute> recallAttributes = pedagogyConfiguration.getRecall().getAttributes().getAttribute();
    	possibleLearnerStateSelectionsForRecall = PedagogyConfigurationEditorUtility.getRemainingAttributePermutations(recallAttributes);

    	List<Attribute> practiceAttributes = pedagogyConfiguration.getPractice().getAttributes().getAttribute();
    	possibleLearnerStateSelectionsForPractice = PedagogyConfigurationEditorUtility.getRemainingAttributePermutations(practiceAttributes);
    	
    	//Determine which quadrants can be shown.
    	ArrayList<MerrillQuadrantEnum> acceptableQuadrants = new ArrayList<MerrillQuadrantEnum>();
    	if(possibleLearnerStateSelectionsForRule.size() != 0) {
    		acceptableQuadrants.add(MerrillQuadrantEnum.RULE);
    	}
    	if(possibleLearnerStateSelectionsForExample.size() != 0) {
    		acceptableQuadrants.add(MerrillQuadrantEnum.EXAMPLE);
    	}
    	if(possibleLearnerStateSelectionsForRecall.size() != 0) {
    		acceptableQuadrants.add(MerrillQuadrantEnum.RECALL);
    	}
    	if(possibleLearnerStateSelectionsForPractice.size() != 0) {
    		acceptableQuadrants.add(MerrillQuadrantEnum.PRACTICE);
    	}
    	selectQuadrantPane.setAcceptableQuadrants(acceptableQuadrants);
    	
    	//If we can't show any quadrants then
    	if(acceptableQuadrants.isEmpty()) {
    		return false;
    	} else {
    		return true;
    	}
    }

	@Override
	public void show() {
		super.show();
		currentStep = 1;
		stepLabel.setVisible(true);
		selectQuadrantPane.setVisible(true);
		selectLearnerStatePane.setVisible(false);
		selectMetadataAttributesPane.setVisible(false);
		finishedPane.setVisible(false);
		
		refreshButtons();
		refreshTitle();
		recountSteps();
	}
	
	private void next() {
		if(selectQuadrantPane.isVisible()) {
			selectQuadrantPane.setVisible(false);
			
			MerrillQuadrantEnum quadrant = selectQuadrantPane.getQuadrant();
			if(quadrant == MerrillQuadrantEnum.RULE) {
				selectLearnerStatePane.setAcceptableValues(possibleLearnerStateSelectionsForRule);
			} else if(quadrant == MerrillQuadrantEnum.EXAMPLE) {
				selectLearnerStatePane.setAcceptableValues(possibleLearnerStateSelectionsForExample);
			} else if(quadrant == MerrillQuadrantEnum.RECALL) {
				selectLearnerStatePane.setAcceptableValues(possibleLearnerStateSelectionsForRecall);
			} else if(quadrant == MerrillQuadrantEnum.PRACTICE) {
				selectLearnerStatePane.setAcceptableValues(possibleLearnerStateSelectionsForPractice);
			}
			selectLearnerStatePane.setVisible(true);
		} else if(selectLearnerStatePane.isVisible()) {
			selectLearnerStatePane.setVisible(false);
			
			MerrillQuadrantEnum quadrant = selectQuadrantPane.getQuadrant();
			selectMetadataAttributesPane.reset(quadrant);
			selectMetadataAttributesPane.setVisible(true);
		} else if(selectMetadataAttributesPane.isVisible()) {
			selectMetadataAttributesPane.setVisible(false);
			stepLabel.setVisible(false);
			finishedPane.setVisible(true);
		}

		center();
		refreshTitle();
		refreshButtons();
		recountSteps();
	}
	
	private void back() {
		if(selectLearnerStatePane.isVisible()) {
			selectQuadrantPane.setVisible(true);
			selectLearnerStatePane.setVisible(false);
		} else if(selectMetadataAttributesPane.isVisible()) {
			selectLearnerStatePane.setVisible(true);
			selectMetadataAttributesPane.setVisible(false);
		} else if(finishedPane.isVisible()) {
			selectMetadataAttributesPane.setVisible(true);
			stepLabel.setVisible(true);
			finishedPane.setVisible(false);
		}

		center();
		refreshTitle();
		refreshButtons();
		recountSteps();
	}
	
	private void refreshTitle() {
		if(selectQuadrantPane.isVisible()) {
			setText("Define Metadata for Quadrant/Learner State");
		} else if(selectLearnerStatePane.isVisible()) {
			setText("Define Metadata for Quadrant/Learner State");
		} else if(selectMetadataAttributesPane.isVisible()) {
			setText("Define Metadata for Quadrant/Learner State");
		} else if(finishedPane.isVisible()) {
			setText("Finished!");
		}
	}
	
	private void refreshButtons() {
		boolean onFirstPane = selectQuadrantPane.isVisible();
		boolean onLastPane = finishedPane.isVisible();
		
		previousButton.setEnabled(!onFirstPane);
		nextButton.setEnabled(!onLastPane);
		finishButton.setEnabled(onLastPane);
	}
	
	private Attribute constructAttribute() {
		LearnerStateAttributeNameEnum learnerState = selectLearnerStatePane.getLearnerState();
		String type = learnerState.getName();
		String value = null;
		if(learnerState == LearnerStateAttributeNameEnum.ANXIOUS ||
				learnerState == LearnerStateAttributeNameEnum.AROUSAL ||
				learnerState == LearnerStateAttributeNameEnum.BORED ||
				learnerState == LearnerStateAttributeNameEnum.CONFUSED ||
				learnerState == LearnerStateAttributeNameEnum.ENG_CONCENTRATION ||
				learnerState == LearnerStateAttributeNameEnum.ENGAGEMENT ||
				learnerState == LearnerStateAttributeNameEnum.FRUSTRATION ||
				learnerState == LearnerStateAttributeNameEnum.GENERAL_INTELLIGENCE ||
				learnerState == LearnerStateAttributeNameEnum.LEARNER_ABILITY ||
				learnerState == LearnerStateAttributeNameEnum.LT_EXCITEMENT ||
				learnerState == LearnerStateAttributeNameEnum.MEDITATION ||
				learnerState == LearnerStateAttributeNameEnum.OFFTASK ||
				learnerState == LearnerStateAttributeNameEnum.ST_EXCITEMENT ||
				learnerState == LearnerStateAttributeNameEnum.SOCIO_ECONOMIC_STATUS ||
				learnerState == LearnerStateAttributeNameEnum.SURPRISED ||
				learnerState == LearnerStateAttributeNameEnum.UNDERSTANDING) {
			value = selectLearnerStatePane.getLowMediumHighLevel().getName();
		} else if(learnerState == LearnerStateAttributeNameEnum.GOAL_ORIENTATION) {
			value = selectLearnerStatePane.getGoalOrientation().getName();
		} else if(learnerState == LearnerStateAttributeNameEnum.GRIT ||
				learnerState == LearnerStateAttributeNameEnum.MOTIVATION ||
				learnerState == LearnerStateAttributeNameEnum.SELF_REGULATORY_ABILITY) {
			value = selectLearnerStatePane.getLowHighLevel().getName();
		} else if(learnerState == LearnerStateAttributeNameEnum.KNOWLEDGE ||
				learnerState == LearnerStateAttributeNameEnum.PRIOR_KNOWLEDGE ||
				learnerState == LearnerStateAttributeNameEnum.SKILL) {
			value = selectLearnerStatePane.getExpertiseLevel().getName();
		} else if(learnerState == LearnerStateAttributeNameEnum.LEARNING_STYLE ||
				learnerState == LearnerStateAttributeNameEnum.SELF_EFFICACY) {
			value = selectLearnerStatePane.getLearningStyle().getName();
		} else if(learnerState == LearnerStateAttributeNameEnum.LOCUS_OF_CONTROL) {
			value = selectLearnerStatePane.getLocusOfControl().getName();
		}
		
		if(value == null) {
			return null;
		}
		
		ArrayList<MetadataAttribute> values = selectMetadataAttributesPane.getMetadataAttributes();
		MetadataAttributes metadataAttributes = new MetadataAttributes();
		metadataAttributes.getMetadataAttribute().addAll(values);
		
		Attribute attribute = new Attribute();
		attribute.setMetadataAttributes(metadataAttributes);
		attribute.setType(type);
		attribute.setValue(value);
		
		return attribute;
	}
	
	private void recountSteps() {
		stepLabel.setText("Step " + currentStep + " of " + totalSteps);
	}
}
