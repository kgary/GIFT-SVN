/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner;

import generated.learner.Input;
import generated.learner.Sensor;
import generated.learner.TrainingAppState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.ClassifierValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.DataSourceDataGrid;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.DataSourceModifiedHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.PredictorValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.TranslatorValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Responsible for displaying and allowing the user to modify a single learner
 * state interpreter (generated.learner.Input).
 * 
 * @author elafave
 *
 */
public class LearnerStateInterpreterEditor extends Composite {
	
	/** The ui binder. */
    interface LearnerStateInterpreterEditorUiBinder extends UiBinder<Widget, LearnerStateInterpreterEditor> {} 
	private static LearnerStateInterpreterEditorUiBinder uiBinder = GWT.create(LearnerStateInterpreterEditorUiBinder.class);
	
	/**
	 * Displays all of the data sources (sensors and training apps) in a data
	 * grid form.
	 */
	@UiField
	protected DataSourceDataGrid dataSourceDataGrid;
	
	/**
	 * Users click this when they want to add a new sensor to the collection
	 * of data sources.
	 */
	@UiField
	protected Button addSensorButton;
	
	/**
	 * Users click this when they want to add a new training application to the
	 * collection of data sources.
	 */
	@UiField
	protected Button addTrainingApplicationButton;
	
	/**
	 * Users click this when they want to remove the data source selected in
	 * the data grid.
	 */
	@UiField
	protected Button removeDataSourceButton;
	
	/**
	 * List box of translators that are appropriate for the collection of data
	 * sources.
	 */
	@UiField
	protected TranslatorValueListBox translatorValueListBox;
	
	/**
	 * This warning label is shown if an interpreter is loaded that has
	 * one or more data sources that the translator doesn't support.
	 */
	@UiField
	protected Label translatorWarningLabel;
	
	/**
	 * List box of classifiers that are appropriate for the learner state the
	 * interpreter works with.
	 */
	@UiField
	protected ClassifierValueListBox classifierValueListBox;
	
	/**
	 * List box of predictors that are appropriate for the learner state the
	 * interpreter works with.
	 */
	@UiField
	protected PredictorValueListBox predictorValueListBox;
	
	/**
	 * The actual interpreter that is being displayed/modified by this editor.
	 */
	private Input learnerStateInterpreter;
	
    public LearnerStateInterpreterEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
    
        ClickHandler addSensorHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				Sensor sensor = new Sensor();
				sensor.setType(SensorTypeEnum.BIOHARNESS.getName());
				
				learnerStateInterpreter.getProducers().getProducerType().add(sensor);
				dataSourceDataGrid.addDataSource(sensor, true);
				
				updateTranslatorsAcceptableValues();
			}
        };
        addSensorButton.addClickHandler(addSensorHandler);
    
        ClickHandler addTrainingApplicationClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				TrainingAppState trainingAppState = new TrainingAppState();
				trainingAppState.setType(MessageTypeEnum.COLLISION.getName());

				learnerStateInterpreter.getProducers().getProducerType().add(trainingAppState);
				dataSourceDataGrid.addDataSource(trainingAppState, true);
				
				updateTranslatorsAcceptableValues();
			}
        };
        addTrainingApplicationButton.addClickHandler(addTrainingApplicationClickHandler);
        
        ClickHandler removeDataSourceClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				List<Serializable> dataSources = learnerStateInterpreter.getProducers().getProducerType();
				if(dataSources.size() == 1) {
					WarningDialog.warning("Unable to remove", "You must have at least one data source therefore it cannot be removed.");
					return;
				}
				
				Serializable removedDataSource = dataSourceDataGrid.removeSelectedDataSource();
				if(removedDataSource == null) {
					WarningDialog.warning("Missing selection", "Please select the data source to remove before requesting it be removed.");
					return;
				}
				
				dataSources.remove(removedDataSource);
				
				updateTranslatorsAcceptableValues();
			}
        };
        removeDataSourceButton.addClickHandler(removeDataSourceClickHandler);
        
        DataSourceModifiedHandler dataSourceModifiedHandler = new DataSourceModifiedHandler() {
			@Override
			public void onDataSourceModified(Serializable dataSource) {
				updateTranslatorsAcceptableValues();
			}
		};
		dataSourceDataGrid.addDataSourceModifiedHandler(dataSourceModifiedHandler);
        
        ValueChangeHandler<TranslatorTypeEnum> translatorImplChangeHandler = new ValueChangeHandler<TranslatorTypeEnum>() {
			@Override
			public void onValueChange(ValueChangeEvent<TranslatorTypeEnum> changeEvent) {
				TranslatorTypeEnum translatorTypeEnum = changeEvent.getValue();
				learnerStateInterpreter.getTranslator().setTranslatorImpl(translatorTypeEnum.getTranslatorImpl());
			}
		};
        translatorValueListBox.addValueChangeHandler(translatorImplChangeHandler);
        
        ValueChangeHandler<ClassifierTypeEnum> classifierImplChangeHandler = new ValueChangeHandler<ClassifierTypeEnum>() {
			@Override
			public void onValueChange(ValueChangeEvent<ClassifierTypeEnum> changeEvent) {
				ClassifierTypeEnum classifierTypeEnum = changeEvent.getValue();
				learnerStateInterpreter.getClassifier().setClassifierImpl(classifierTypeEnum.getClassifierImpl());
			}
		};
		classifierValueListBox.addValueChangeHandler(classifierImplChangeHandler);
		
		ValueChangeHandler<PredictorTypeEnum> predictorImplChangeHandler = new ValueChangeHandler<PredictorTypeEnum>() {
			@Override
			public void onValueChange(ValueChangeEvent<PredictorTypeEnum> changeEvent) {
				PredictorTypeEnum predictorTypeEnum = changeEvent.getValue();
				learnerStateInterpreter.getPredictor().setPredictorImpl(predictorTypeEnum.getPredictorImpl());
			}
		};
		predictorValueListBox.addValueChangeHandler(predictorImplChangeHandler);
    }
    
    /**
     * Extracts all of the data from the supplied Learner State Interpreter and
     * displays it in the GUI. Any changes made via the GUI will be saved in
     * interpreter.
     * @param learnerStateInterpreter Learner State Interpreter to display
     * and/or modify.
     */
    public void setInterpreter(Input learnerStateInterpreter) {
    	this.learnerStateInterpreter = learnerStateInterpreter;
    	
    	List<Serializable> dataSources = learnerStateInterpreter.getProducers().getProducerType();
    	dataSourceDataGrid.setDataSources(dataSources);
    	
    	String translatorImpl = learnerStateInterpreter.getTranslator().getTranslatorImpl();
    	TranslatorTypeEnum translatorTypeEnum = TranslatorTypeEnum.fromTranslatorImpl(translatorImpl);
    	translatorValueListBox.setValue(translatorTypeEnum);
    	
    	//Modify the translator if it isn't valid for the data sources we have.
    	boolean hadValidTranslator = updateTranslatorsAcceptableValues();
    	if(hadValidTranslator) {
    		translatorWarningLabel.setVisible(false);
    	} else {
    		//If the translator had to be modified then let the user know about
    		//it.
    		translatorWarningLabel.setVisible(true);
    	}
    	
    	//NOTE We're having to work backwards to determine the set of valid
    	//Classifiers. Ideally, in the future, the Learner State Interpreter
    	//will have a Learner State attribute that will allow us to perform a
    	//more direct look up.
    	String classifierImpl = learnerStateInterpreter.getClassifier().getClassifierImpl();
    	ClassifierTypeEnum classifierTypeEnum = ClassifierTypeEnum.fromClassifierImpl(classifierImpl);
    	ArrayList<ClassifierTypeEnum> acceptableClassifiers = LearnerConfigurationMaps.getInstance().getClassifierTypes(classifierTypeEnum);
    	classifierValueListBox.setValue(classifierTypeEnum);
    	classifierValueListBox.setAcceptableValues(acceptableClassifiers);
    	
    	//NOTE We're having to work backwards to determine the set of valid
    	//Predictors. Ideally, in the future, the Learner State Interpreter
    	//will have a Learner State attribute that will allow us to perform a
    	//more direct look up.
    	String predictorImpl = learnerStateInterpreter.getPredictor().getPredictorImpl();
    	PredictorTypeEnum predictorTypeEnum = PredictorTypeEnum.fromPredictorImpl(predictorImpl);
    	ArrayList<PredictorTypeEnum> acceptablePredictors = LearnerConfigurationMaps.getInstance().getPredictorTypes(predictorTypeEnum);
    	predictorValueListBox.setValue(predictorTypeEnum);
    	predictorValueListBox.setAcceptableValues(acceptablePredictors);
    }
    
    /**
     * Determines the set of acceptable Translators based on the data sources
     * contained within the Learner State Interpreter. If the currently
     * selected Translator isn't "valid" then it is removed and a valid
     * option is selected.
     * @return False if the currently selected Translator had to be swapped
     * for a valid Translator, True otherwise.
     */
    private boolean updateTranslatorsAcceptableValues() {
    	TranslatorTypeEnum selectedTranslator = translatorValueListBox.getValue();
    	List<Serializable> dataSources = learnerStateInterpreter.getProducers().getProducerType();
    	List<TranslatorTypeEnum> acceptableValues = LearnerConfigurationMaps.getInstance().getTranslatorTypes(dataSources);
    	
    	//Handle the case where the current selection is no longer valid.
    	boolean existingSelectionRemainedValid = true;
    	if(!acceptableValues.contains(selectedTranslator)) {
    		existingSelectionRemainedValid = false;
    		
    		//Update the model with the new selection.
    		selectedTranslator = acceptableValues.get(0);
    		learnerStateInterpreter.getTranslator().setTranslatorImpl(selectedTranslator.getTranslatorImpl());
    		
    		//Update the view with the new selection.
    		translatorValueListBox.setValue(selectedTranslator);
    	}
    	
    	translatorValueListBox.setAcceptableValues(acceptableValues);
    	
    	return existingSelectionRemainedValid;
    }

	public void redraw() {
		dataSourceDataGrid.redraw();
	}
}
