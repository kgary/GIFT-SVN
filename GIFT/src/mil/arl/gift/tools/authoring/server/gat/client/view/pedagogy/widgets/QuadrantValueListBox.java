/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets;

import java.io.IOException;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * ValueListBox which can be populated with MerrillQuadrantEnum. The display
 * names of the enums are the values shown in UI.
 * 
 * @author elafave
 *
 */
public class QuadrantValueListBox extends ValueListBox<MerrillQuadrantEnum> {

	public QuadrantValueListBox() {
		super(new Renderer<MerrillQuadrantEnum>() {
			@Override
			public String render(MerrillQuadrantEnum quadrant) {
				if(quadrant == null) {
					return "";
				}
				return quadrant.getDisplayName();
			}

			@Override
			public void render(MerrillQuadrantEnum quadrant, Appendable appendable)
					throws IOException {
				String value = render(quadrant);
				appendable.append(value);
			}
		});
	}
}
