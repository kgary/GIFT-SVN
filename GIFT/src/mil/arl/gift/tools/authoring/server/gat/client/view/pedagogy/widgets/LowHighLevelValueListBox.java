/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets;

import java.io.IOException;

import mil.arl.gift.common.enums.LowHighLevelEnum;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * ValueListBox which can be populated with LowHighLevelEnum. The display
 * names of the enums are the values shown in UI.
 * 
 * @author elafave
 *
 */
public class LowHighLevelValueListBox extends ValueListBox<LowHighLevelEnum> {

	public LowHighLevelValueListBox() {
		super(new Renderer<LowHighLevelEnum>(){
			@Override
			public String render(LowHighLevelEnum lowHighLevel) {
				if(lowHighLevel == null) {
					return "";
				}
				return lowHighLevel.getDisplayName();
			}

			@Override
			public void render(LowHighLevelEnum lowHighLevel, Appendable appendable)
					throws IOException {
				String value = render(lowHighLevel);
				appendable.append(value);
			}
		});
	}
}
