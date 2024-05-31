/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

public class YesNoListBox extends ValueListBox<Boolean> {

	public YesNoListBox() {
		super(new Renderer<Boolean>() {
			@Override
			public String render(Boolean bool) {
				if(bool == null) {
					return "";
				} else if(bool) {
					return "Yes";
				} else {
					return "No";
				}
			}

			@Override
			public void render(Boolean bool, Appendable appendable)
					throws IOException {
				String value = render(bool);
				appendable.append(value);
			}
		});
		
		ArrayList<Boolean> acceptableValues = new ArrayList<Boolean>();
        acceptableValues.add(false);
        acceptableValues.add(true);
        
        setValue(false);
        setAcceptableValues(acceptableValues);
	}
}
