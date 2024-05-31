/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.GoalOrientationEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.LearningStyleEnum;
import mil.arl.gift.common.enums.LocusOfControlEnum;
import mil.arl.gift.common.enums.LowHighLevelEnum;
import mil.arl.gift.common.enums.LowMediumHighLevelEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.PedagogyConfigurationEditorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets.ExpertiseLevelValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets.GoalOrientationValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets.LearnerStateValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets.LearningStyleValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets.LocusOfControlValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets.LowHighLevelValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets.LowMediumHighLevelValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Every Learner State can be quantified/described by another enum. However,
 * not every Learner State can be quantified/described by the same enum. So
 * this GUI allows the user to select a Learner State value and then quantify
 * or describe that Learner State by selecting the value of its corresponding
 * descriptive enum.
 * @author elafave
 *
 */
public class SelectLearnerStatePane extends Composite {
	
	/** The ui binder. */
    interface SelectLearnerStatePaneUiBinder extends UiBinder<Widget, SelectLearnerStatePane> {} 
	private static SelectLearnerStatePaneUiBinder uiBinder = GWT.create(SelectLearnerStatePaneUiBinder.class);
	
	@UiField
	protected LearnerStateValueListBox learnerStateValueListBox;
	
	@UiField
	protected LowMediumHighLevelValueListBox lowMediumHighLevelValueListBox;
	
	@UiField
	protected GoalOrientationValueListBox goalOrientationValueListBox;
	
	@UiField
	protected LowHighLevelValueListBox lowHighLevelValueListBox;
	
	@UiField
	protected ExpertiseLevelValueListBox expertiseLevelValueListBox;
	
	@UiField
	protected LearningStyleValueListBox learningStyleValueListBox;
	
	@UiField
	protected LocusOfControlValueListBox locusOfControlValueListBox;
	
    public SelectLearnerStatePane() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        ValueChangeHandler<LearnerStateAttributeNameEnum> learnerStateChangeHandler = new ValueChangeHandler<LearnerStateAttributeNameEnum>() {
			@Override
			public void onValueChange(ValueChangeEvent<LearnerStateAttributeNameEnum> changeEvent) {
				LearnerStateAttributeNameEnum learnerState = changeEvent.getValue();
				showEnumValueListBox(learnerState);
			}
		};
        learnerStateValueListBox.addValueChangeHandler(learnerStateChangeHandler);
    }
    
    private void showEnumValueListBox(LearnerStateAttributeNameEnum learnerState) {
    	Class<? extends AbstractEnum> descriptiveEnumClass = PedagogyConfigurationEditorUtility.getLearnerStateDescriptiveEnum(learnerState);
    	
    	if(descriptiveEnumClass == LowMediumHighLevelEnum.class) {
    		lowMediumHighLevelValueListBox.setVisible(true);
    	} else {
    		lowMediumHighLevelValueListBox.setVisible(false);
    	}
    	
    	if(descriptiveEnumClass == GoalOrientationEnum.class) {
    		goalOrientationValueListBox.setVisible(true);
    	} else {
    		goalOrientationValueListBox.setVisible(false);
    	}
    	
    	if(descriptiveEnumClass == LowHighLevelEnum.class) {
    		lowHighLevelValueListBox.setVisible(true);
    	} else {
    		lowHighLevelValueListBox.setVisible(false);
    	}
    	
    	if(descriptiveEnumClass == ExpertiseLevelEnum.class) {
    		expertiseLevelValueListBox.setVisible(true);
    	} else {
    		expertiseLevelValueListBox.setVisible(false);
    	}
    	
    	if(descriptiveEnumClass == LearningStyleEnum.class) {
    		learningStyleValueListBox.setVisible(true);
    	} else {
    		learningStyleValueListBox.setVisible(false);
    	}
    	
    	if(descriptiveEnumClass == LocusOfControlEnum.class) {
    		locusOfControlValueListBox.setVisible(true);
    	} else {
    		locusOfControlValueListBox.setVisible(false);
    	}
    }
    
    /**
     * Defines which Learner States the user should be able to select from and
     * subsequently which enum values the user should be able to pick from that
     * describe the Learner State they just selected.
     * @param acceptableValues Maps the select-able Learner States to the
     * Enums that describe the state. Although the API can't explicitly enforce
     * this, it is expected every element of the ArrayList be of the same
     * AbstractEnum subclass.
     */
    public void setAcceptableValues(HashMap<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>> acceptableValues) {
    	//Set the acceptable Learner States values.
    	Comparator<LearnerStateAttributeNameEnum> comparator = new Comparator<LearnerStateAttributeNameEnum>() {
			@Override
			public int compare(LearnerStateAttributeNameEnum learnerState1, LearnerStateAttributeNameEnum learnerState2) {
				String displayName1 = learnerState1.getDisplayName();
				String displayName2 = learnerState2.getDisplayName();
				int result = displayName1.compareTo(displayName2);
				return result;
			}
		};
		
		ArrayList<LearnerStateAttributeNameEnum> acceptableLearnerStates = new ArrayList<LearnerStateAttributeNameEnum>(acceptableValues.keySet());
		Collections.sort(acceptableLearnerStates, comparator);
		
		learnerStateValueListBox.setValue(acceptableLearnerStates.iterator().next(), true);
		learnerStateValueListBox.setAcceptableValues(acceptableLearnerStates);

		//Set the acceptable descriptive enum values.
    	for(LearnerStateAttributeNameEnum learnerState : acceptableLearnerStates) {
    		ArrayList<AbstractEnum> acceptableEnums = acceptableValues.get(learnerState);
    		setAcceptableValues(learnerState, acceptableEnums);
    	}
    }
    
    private void setAcceptableValues(LearnerStateAttributeNameEnum learnerState, ArrayList<AbstractEnum> acceptableValues) {
    	Class<? extends AbstractEnum> descriptiveEnumClass = PedagogyConfigurationEditorUtility.getLearnerStateDescriptiveEnum(learnerState);
    	
    	if(descriptiveEnumClass == LowMediumHighLevelEnum.class) {
    		ArrayList<LowMediumHighLevelEnum> enums = new ArrayList<LowMediumHighLevelEnum>();
    		for(AbstractEnum acceptableValue : acceptableValues) {
    			enums.add((LowMediumHighLevelEnum)acceptableValue);
    		}
    		
    		Comparator<LowMediumHighLevelEnum> comparator = new Comparator<LowMediumHighLevelEnum>() {
    			@Override
    			public int compare(LowMediumHighLevelEnum lowMediumHighLevel1, LowMediumHighLevelEnum lowMediumHighLevel2) {
    				String displayName1 = lowMediumHighLevel1.getDisplayName();
    				String displayName2 = lowMediumHighLevel2.getDisplayName();
    				int result = displayName1.compareTo(displayName2);
    				return result;
    			}
    		};
    		Collections.sort(enums, comparator);
    		
    		lowMediumHighLevelValueListBox.setValue(enums.iterator().next());
    		lowMediumHighLevelValueListBox.setAcceptableValues(enums);
    	} else if(descriptiveEnumClass == GoalOrientationEnum.class) {
    		ArrayList<GoalOrientationEnum> enums = new ArrayList<GoalOrientationEnum>();
    		for(AbstractEnum acceptableValue : acceptableValues) {
    			enums.add((GoalOrientationEnum)acceptableValue);
    		}
    		
    		Comparator<GoalOrientationEnum> comparator = new Comparator<GoalOrientationEnum>() {
    			@Override
    			public int compare(GoalOrientationEnum goalOrientation1, GoalOrientationEnum goalOrientation2) {
    				String displayName1 = goalOrientation1.getDisplayName();
    				String displayName2 = goalOrientation2.getDisplayName();
    				int result = displayName1.compareTo(displayName2);
    				return result;
    			}
    		};
    		Collections.sort(enums, comparator);
    		
    		goalOrientationValueListBox.setValue(enums.iterator().next());
    		goalOrientationValueListBox.setAcceptableValues(enums);
    	} else if(descriptiveEnumClass == LowHighLevelEnum.class) {
    		ArrayList<LowHighLevelEnum> enums = new ArrayList<LowHighLevelEnum>();
    		for(AbstractEnum acceptableValue : acceptableValues) {
    			enums.add((LowHighLevelEnum)acceptableValue);
    		}
    		
    		Comparator<LowHighLevelEnum> comparator = new Comparator<LowHighLevelEnum>() {
    			@Override
    			public int compare(LowHighLevelEnum lowHighLevel1, LowHighLevelEnum lowHighLevel2) {
    				String displayName1 = lowHighLevel1.getDisplayName();
    				String displayName2 = lowHighLevel2.getDisplayName();
    				int result = displayName1.compareTo(displayName2);
    				return result;
    			}
    		};
    		Collections.sort(enums, comparator);
    		
    		lowHighLevelValueListBox.setValue(enums.iterator().next());
    		lowHighLevelValueListBox.setAcceptableValues(enums);
    	} else if(descriptiveEnumClass == ExpertiseLevelEnum.class) {
    		ArrayList<ExpertiseLevelEnum> enums = new ArrayList<ExpertiseLevelEnum>();
    		for(AbstractEnum acceptableValue : acceptableValues) {
    			enums.add((ExpertiseLevelEnum)acceptableValue);
    		}
    		
    		Comparator<ExpertiseLevelEnum> comparator = new Comparator<ExpertiseLevelEnum>() {
    			@Override
    			public int compare(ExpertiseLevelEnum expertiseLevel1, ExpertiseLevelEnum expertiseLevel2) {
    				String displayName1 = expertiseLevel1.getDisplayName();
    				String displayName2 = expertiseLevel2.getDisplayName();
    				int result = displayName1.compareTo(displayName2);
    				return result;
    			}
    		};
    		Collections.sort(enums, comparator);
    		
    		expertiseLevelValueListBox.setValue(enums.iterator().next());
    		expertiseLevelValueListBox.setAcceptableValues(enums);
    	} else if(descriptiveEnumClass == LearningStyleEnum.class) {
    		ArrayList<LearningStyleEnum> enums = new ArrayList<LearningStyleEnum>();
    		for(AbstractEnum acceptableValue : acceptableValues) {
    			enums.add((LearningStyleEnum)acceptableValue);
    		}
    		
    		Comparator<LearningStyleEnum> comparator = new Comparator<LearningStyleEnum>() {
    			@Override
    			public int compare(LearningStyleEnum learningStyle1, LearningStyleEnum learningStyle2) {
    				String displayName1 = learningStyle1.getDisplayName();
    				String displayName2 = learningStyle2.getDisplayName();
    				int result = displayName1.compareTo(displayName2);
    				return result;
    			}
    		};
    		Collections.sort(enums, comparator);
    		
    		learningStyleValueListBox.setValue(enums.iterator().next());
    		learningStyleValueListBox.setAcceptableValues(enums);
    	} else if(descriptiveEnumClass == LocusOfControlEnum.class) {
    		ArrayList<LocusOfControlEnum> enums = new ArrayList<LocusOfControlEnum>();
    		for(AbstractEnum acceptableValue : acceptableValues) {
    			enums.add((LocusOfControlEnum)acceptableValue);
    		}
    		
    		Comparator<LocusOfControlEnum> comparator = new Comparator<LocusOfControlEnum>() {
    			@Override
    			public int compare(LocusOfControlEnum locusOfControl1, LocusOfControlEnum locusOfControl2) {
    				String displayName1 = locusOfControl1.getDisplayName();
    				String displayName2 = locusOfControl2.getDisplayName();
    				int result = displayName1.compareTo(displayName2);
    				return result;
    			}
    		};
    		Collections.sort(enums, comparator);
    		
    		locusOfControlValueListBox.setValue(enums.iterator().next());
    		locusOfControlValueListBox.setAcceptableValues(enums);
    	}
    }
    
    public LearnerStateAttributeNameEnum getLearnerState() {
    	LearnerStateAttributeNameEnum learnerState = learnerStateValueListBox.getValue();
    	return learnerState;
    }
    
    public LowMediumHighLevelEnum getLowMediumHighLevel() {
    	LowMediumHighLevelEnum lowMediumHighLevel = lowMediumHighLevelValueListBox.getValue();
    	return lowMediumHighLevel;
    }
    
    public GoalOrientationEnum getGoalOrientation() {
    	GoalOrientationEnum goalOrientation = goalOrientationValueListBox.getValue();
    	return goalOrientation;
    }
    
    public LowHighLevelEnum getLowHighLevel() {
    	LowHighLevelEnum lowHighLevel = lowHighLevelValueListBox.getValue();
    	return lowHighLevel;
    }
    
    public ExpertiseLevelEnum getExpertiseLevel() {
    	ExpertiseLevelEnum expertiseLevel = expertiseLevelValueListBox.getValue();
    	return expertiseLevel;
    }
    
    public LearningStyleEnum getLearningStyle() {
    	LearningStyleEnum learningStyle = learningStyleValueListBox.getValue();
    	return learningStyle;
    }
    
    public LocusOfControlEnum getLocusOfControl() {
    	LocusOfControlEnum locusOfControl = locusOfControlValueListBox.getValue();
    	return locusOfControl;
    }
}
