/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets;

import java.io.IOException;

import mil.arl.gift.common.enums.GoalOrientationEnum;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * ValueListBox which can be populated with GoalOrientationEnum. The display
 * names of the enums are the values shown in UI.
 * 
 * @author elafave
 *
 */
public class GoalOrientationValueListBox extends ValueListBox<GoalOrientationEnum> {

	public GoalOrientationValueListBox() {
		super(new Renderer<GoalOrientationEnum>(){
			@Override
			public String render(GoalOrientationEnum goalOrientation) {
				if(goalOrientation == null) {
					return "";
				}
				return goalOrientation.getDisplayName();
			}

			@Override
			public void render(GoalOrientationEnum goalOrientation, Appendable appendable)
					throws IOException {
				String value = render(goalOrientation);
				appendable.append(value);
			}
		});
	}
}
