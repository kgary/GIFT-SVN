/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets;

import java.io.IOException;

import mil.arl.gift.common.enums.LowMediumHighLevelEnum;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * ValueListBox which can be populated with LowMediumHighLevelEnum. The display
 * names of the enums are the values shown in UI.
 * 
 * @author elafave
 *
 */
public class LowMediumHighLevelValueListBox extends ValueListBox<LowMediumHighLevelEnum> {

	public LowMediumHighLevelValueListBox() {
		super(new Renderer<LowMediumHighLevelEnum>(){
			@Override
			public String render(LowMediumHighLevelEnum lowMediumHighLevel) {
				if(lowMediumHighLevel == null) {
					return "";
				}
				return lowMediumHighLevel.getDisplayName();
			}

			@Override
			public void render(LowMediumHighLevelEnum lowMediumHighLevel, Appendable appendable)
					throws IOException {
				String value = render(lowMediumHighLevel);
				appendable.append(value);
			}
		});
	}
}
