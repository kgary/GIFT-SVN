/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets;

import java.io.IOException;

import mil.arl.gift.common.enums.LearningStyleEnum;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * ValueListBox which can be populated with LearningStyleEnum. The display
 * names of the enums are the values shown in UI.
 * 
 * @author elafave
 *
 */
public class LearningStyleValueListBox extends ValueListBox<LearningStyleEnum> {

	public LearningStyleValueListBox() {
		super(new Renderer<LearningStyleEnum>(){
			@Override
			public String render(LearningStyleEnum learningStyle) {
				if(learningStyle == null) {
					return "";
				}
				return learningStyle.getDisplayName();
			}

			@Override
			public void render(LearningStyleEnum learningStyle, Appendable appendable)
					throws IOException {
				String value = render(learningStyle);
				appendable.append(value);
			}
		});
	}
}
