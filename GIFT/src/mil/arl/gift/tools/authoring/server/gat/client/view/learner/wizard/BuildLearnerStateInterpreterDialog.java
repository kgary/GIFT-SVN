/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner.wizard;

import generated.learner.Classifier;
import generated.learner.Input;
import generated.learner.Predictor;
import generated.learner.Producers;
import generated.learner.Translator;

import java.io.Serializable;
import java.util.ArrayList;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.ClassifierTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.LearnerConfigurationMaps;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.PredictorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.TranslatorTypeEnum;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wizard-like Dialog Box that walks the user through constructing a new
 * Learner State Interpreter (generated.learner.Input).
 * @author elafave
 */
public class BuildLearnerStateInterpreterDialog extends DialogBox {
	
	/** The ui binder. */
    interface BuildLearnerStateInterpreterDialogUiBinder extends UiBinder<Widget, BuildLearnerStateInterpreterDialog> {} 
	private static BuildLearnerStateInterpreterDialogUiBinder uiBinder = GWT.create(BuildLearnerStateInterpreterDialogUiBinder.class);
	
	private static final String TITLE_PREFIX = "Create Learner State Interpreter: ";
	
	/**
	 * Allows the user to select the Learner State the interpreter will work
	 * with.
	 */
	@UiField
	protected SelectLearnerStatePane selectLearnerStatePane;
	
	/**
	 * Allows the user to select the data sources that will supply the
	 * interpreter with information.
	 */
	@UiField
	protected DataSourcesPane dataSourcesPane;
	
	/**
	 * Allows the user to specify which translator will transform the incoming
	 * data into a more "workable" format.
	 */
	@UiField
	protected TranslatorPane translatorPane;
	
	/**
	 * Allows the user to specify which Classifier will be used to identify the
	 * learner state.
	 */
	@UiField
	protected ClassifierPane classifierPane;
	
	/**
	 * Allows the user to specify which Predictor will be used to predict
	 * future learner state.
	 */
	@UiField
	protected PredictorPane predictorPane;

	/**
	 * Tells the user they're done.
	 */
	@UiField
	protected HTML finishedPane;
	
	@UiField
	protected Button previousButton;
	
	@UiField
	protected Button nextButton;
	
	@UiField
	protected Button finishButton;
	
	@UiField
	protected Button cancelButton;
	
	@UiField
	protected HTML stepLabel;
	
	private int totalSteps = 5;
	
	private int currentStep;
	
    public BuildLearnerStateInterpreterDialog(final LearnerStateInterpreterBuiltHandler callback) {
    	setWidget(uiBinder.createAndBindUi(this));
    	setGlassEnabled(true);
    	
    	ClickHandler previousHandler = new ClickHandler() {
    		@Override
    		public void onClick(ClickEvent arg0) {
    			back();
    		}
    	};
    	previousButton.addClickHandler(previousHandler);
    	
    	ClickHandler nextHandler = new ClickHandler() {
    		@Override
    		public void onClick(ClickEvent arg0) {
    			next();
    		}
    	};
    	nextButton.addClickHandler(nextHandler);
    	
    	ClickHandler finishHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				Input learnerStateInterpreter = constructLearnerStateInterpreter();
				callback.onInterpreterBuilt(learnerStateInterpreter);
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

	@Override
	public void show() {
		super.show();
		refreshPanes();
		refreshButtons();
		refreshTitle();
		refreshStepLabel();
	}
	
	private void next() {
		currentStep++;
		
		if(selectLearnerStatePane.isVisible()) {
			selectLearnerStatePane.setVisible(false);
			dataSourcesPane.setVisible(true);
			dataSourcesPane.redraw();
		} else if(dataSourcesPane.isVisible()) {
			//Figure out which translators are acceptable given the data sources.
			ArrayList<Serializable> dataSources = dataSourcesPane.getDataSources();
			ArrayList<TranslatorTypeEnum> acceptableTranslators = LearnerConfigurationMaps.getInstance().getTranslatorTypes(dataSources);
			
			//Apply the acceptable translators to the next pane.
			TranslatorTypeEnum currentlySelectedTranslator = translatorPane.getTranslatorType();
			if(!acceptableTranslators.contains(currentlySelectedTranslator)) {
				translatorPane.setTranslatorType(acceptableTranslators.get(0));
			}
			translatorPane.setAcceptableTranslatorTypes(acceptableTranslators);
			
			//Switch to the next pane.
			dataSourcesPane.setVisible(false);
			translatorPane.setVisible(true);
		} else if(translatorPane.isVisible()) {
			//Figure out which classifiers are acceptable given the learner state
			LearnerStateAttributeNameEnum learnerState = selectLearnerStatePane.getLearnerState();
			ArrayList<ClassifierTypeEnum> acceptableClassifiers = LearnerConfigurationMaps.getInstance().getClassifierTypes(learnerState);
			
			//Apply the acceptable classifiers to the next pane.
			ClassifierTypeEnum currentlySelectedClassifier = classifierPane.getClassifierType();
			if(!acceptableClassifiers.contains(currentlySelectedClassifier)) {
				classifierPane.setClassifierType(acceptableClassifiers.get(0));
			}
			classifierPane.setAcceptableClassifierTypes(acceptableClassifiers);
			
			//Switch to the next pane.
			translatorPane.setVisible(false);
			classifierPane.setVisible(true);
		} else if(classifierPane.isVisible()) {
			//Figure out which predictors are acceptable given the leaner state
			LearnerStateAttributeNameEnum learnerState = selectLearnerStatePane.getLearnerState();
			ArrayList<PredictorTypeEnum> acceptablePredictors = LearnerConfigurationMaps.getInstance().getPredictorTypes(learnerState);
			
			//Apply the acceptable predictors to the next pane.
			PredictorTypeEnum currentlySelectedPredictor = predictorPane.getPredictorType();
			if(!acceptablePredictors.contains(currentlySelectedPredictor)) {
				predictorPane.setPredictorType(acceptablePredictors.get(0));
			}
			predictorPane.setAcceptablePredictorTypes(acceptablePredictors);
			
			//Switch to the next pane.
			classifierPane.setVisible(false);
			predictorPane.setVisible(true);
		} else if(predictorPane.isVisible()) {
			predictorPane.setVisible(false);
			stepLabel.setVisible(false);
			finishedPane.setVisible(true);
		}
		
		center();
		refreshTitle();
		refreshButtons();
		refreshStepLabel();
	}
	
	private void back() {
		currentStep--;
		
		if(dataSourcesPane.isVisible()) {
			selectLearnerStatePane.setVisible(true);
			dataSourcesPane.setVisible(false);
		} else if(translatorPane.isVisible()) {
			dataSourcesPane.setVisible(true);
			dataSourcesPane.redraw();
			translatorPane.setVisible(false);
		} else if(classifierPane.isVisible()) {
			translatorPane.setVisible(true);
			classifierPane.setVisible(false);
		} else if(predictorPane.isVisible()) {
			classifierPane.setVisible(true);
			predictorPane.setVisible(false);
		} else if(finishedPane.isVisible()) {
			stepLabel.setVisible(true);
			predictorPane.setVisible(true);
			finishedPane.setVisible(false);
		}
		
		center();
		refreshTitle();
		refreshButtons();
		refreshStepLabel();
	}
	
	private void refreshTitle() {
		if(selectLearnerStatePane.isVisible()) {
			setText(TITLE_PREFIX + "Select Learner State");
		} else if(dataSourcesPane.isVisible()) {
			setText(TITLE_PREFIX + "Select Data Sources");
		} else if(translatorPane.isVisible()) {
			setText(TITLE_PREFIX + "Select Data Translator");
		} else if(classifierPane.isVisible()) {
			setText(TITLE_PREFIX + "Select Learner State Classifier");
		} else if(predictorPane.isVisible()) {
			setText(TITLE_PREFIX + "Select Learner State Predictor");
		} else if(finishedPane.isVisible()) {
			setText(TITLE_PREFIX + "You're Done!");
		}
	}
	
	private void refreshButtons() {
		boolean onFirstPane = selectLearnerStatePane.isVisible();
		boolean onLastPane = finishedPane.isVisible();
		
		previousButton.setEnabled(!onFirstPane);
		nextButton.setEnabled(!onLastPane);
		finishButton.setEnabled(onLastPane);
	}
	
	private void refreshPanes() {
		
		currentStep = 1;
		
		selectLearnerStatePane.reset();
		selectLearnerStatePane.setVisible(true);
		
		dataSourcesPane.reset();
		dataSourcesPane.setVisible(false);
		
		translatorPane.setVisible(false);
		
		classifierPane.setVisible(false);
		
		predictorPane.setVisible(false);
		
		finishedPane.setVisible(false);
	}
	
	private Input constructLearnerStateInterpreter() {
		LearnerStateAttributeNameEnum learnerState = selectLearnerStatePane.getLearnerState();
		ArrayList<Serializable> dataSources = dataSourcesPane.getDataSources();
		TranslatorTypeEnum translatorType = translatorPane.getTranslatorType();
		ClassifierTypeEnum classifierType = classifierPane.getClassifierType();
		PredictorTypeEnum predictorType = predictorPane.getPredictorType(); 
		
		Producers producers = new Producers();
    	producers.getProducerType().addAll(dataSources);
    	
    	Translator translator = new Translator();
    	translator.setTranslatorImpl(translatorType.getTranslatorImpl());
    	
    	Classifier classifier = new Classifier();
    	classifier.setClassifierImpl(classifierType.getClassifierImpl());
    	
    	Predictor predictor = new Predictor();
    	predictor.setPredictorImpl(predictorType.getPredictorImpl());
    	
    	Input learnerStateInterpreter = new Input();
    	learnerStateInterpreter.setName(learnerState.getDisplayName() + " Interpreter");
    	learnerStateInterpreter.setProducers(producers);
    	learnerStateInterpreter.setTranslator(translator);
    	learnerStateInterpreter.setClassifier(classifier);
    	learnerStateInterpreter.setPredictor(predictor);
		
		return learnerStateInterpreter;
	}
	
	/**
	 * Updates the step counter label on the dialog
	 */
	private void refreshStepLabel() {
		stepLabel.setText("Step " + currentStep + " of " + totalSteps);
	}
}
