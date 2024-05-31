/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets;

import java.io.IOException;

import mil.arl.gift.common.enums.LocusOfControlEnum;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * ValueListBox which can be populated with LocusOfControlEnum. The display
 * names of the enums are the values shown in UI.
 * 
 * @author elafave
 *
 */
public class LocusOfControlValueListBox extends ValueListBox<LocusOfControlEnum> {

	public LocusOfControlValueListBox() {
		super(new Renderer<LocusOfControlEnum>(){
			@Override
			public String render(LocusOfControlEnum locusOfControl) {
				if(locusOfControl == null) {
					return "";
				}
				return locusOfControl.getDisplayName();
			}

			@Override
			public void render(LocusOfControlEnum locusOfControl, Appendable appendable)
					throws IOException {
				String value = render(locusOfControl);
				appendable.append(value);
			}
		});
	}
}
