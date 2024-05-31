/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets;

import java.io.IOException;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * ValueListBox which can be populated with LearnerStateAttributeNameEnum. The 
 * display names of the enums are the values shown in UI.
 * 
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
	}
}
