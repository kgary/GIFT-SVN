/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets;

import java.io.IOException;

import mil.arl.gift.common.enums.ExpertiseLevelEnum;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * ValueListBox which can be populated with ExpertiseLevelEnum. The display
 * names of the enums are the values shown in UI.
 * 
 * @author elafave
 *
 */
public class ExpertiseLevelValueListBox extends ValueListBox<ExpertiseLevelEnum> {

	public ExpertiseLevelValueListBox() {
		super(new Renderer<ExpertiseLevelEnum>(){
			@Override
			public String render(ExpertiseLevelEnum expertiseLevel) {
				if(expertiseLevel == null) {
					return "";
				}
				return expertiseLevel.getDisplayName();
			}

			@Override
			public void render(ExpertiseLevelEnum expertiseLevel, Appendable appendable)
					throws IOException {
				String value = render(expertiseLevel);
				appendable.append(value);
			}
		});
	}
}
