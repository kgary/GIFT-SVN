/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.LearnerConfigurationMaps;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * A ValueListBox that operates on LearnerStateAttributeNameEnums and uses 
 * their display names to populate the list.
 * @author elafave
 *
 */
public class LearnerStateValueListBox extends ValueListBox<LearnerStateAttributeNameEnum> {

	public LearnerStateValueListBox() {
		super(new Renderer<LearnerStateAttributeNameEnum>(){
			@Override
			public String render(LearnerStateAttributeNameEnum learnerState) {
				if(learnerState == null) {
					return "";
				}
				return learnerState.getDisplayName();
			}

			@Override
			public void render(LearnerStateAttributeNameEnum learnerState, Appendable appendable)
					throws IOException {
				String value = render(learnerState);
				appendable.append(value);
			}
		});
		
		Comparator<LearnerStateAttributeNameEnum> comparator = new Comparator<LearnerStateAttributeNameEnum>() {
			@Override
			public int compare(LearnerStateAttributeNameEnum learnerState1, LearnerStateAttributeNameEnum learnerState2) {
				String displayName1 = learnerState1.getDisplayName();
				String displayName2 = learnerState2.getDisplayName();
				int result = displayName1.compareTo(displayName2);
				return result;
			}
		};
		
		Set<LearnerStateAttributeNameEnum> learnerStates = LearnerConfigurationMaps.getInstance().getLearnerStates();
		ArrayList<LearnerStateAttributeNameEnum> acceptableValues = new ArrayList<LearnerStateAttributeNameEnum>(learnerStates);
		Collections.sort(acceptableValues, comparator);
		
		setValue(acceptableValues.iterator().next());
		setAcceptableValues(acceptableValues);
	}
}
