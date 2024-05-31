/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets;

import generated.sensor.BooleanEnum;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

public class BooleanEnumListBox extends ValueListBox<BooleanEnum> {

	public BooleanEnumListBox() {
		super(new Renderer<BooleanEnum>() {
			@Override
			public String render(BooleanEnum booleanEnum) {
				if(booleanEnum == null) {
					return "";
				} else if(booleanEnum == BooleanEnum.TRUE) {
					return "On";
				} else {
					return "Off";
				}
			}

			@Override
			public void render(BooleanEnum booleanEnum, Appendable appendable)
					throws IOException {
				String value = render(booleanEnum);
				appendable.append(value);
			}
		});
		
		ArrayList<BooleanEnum> acceptableValues = new ArrayList<BooleanEnum>();
        acceptableValues.add(null);
        acceptableValues.add(BooleanEnum.TRUE);
        acceptableValues.add(BooleanEnum.FALSE);
        
        setValue(null);
        setAcceptableValues(acceptableValues);
	}
}
