/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets;

import java.io.IOException;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

import mil.arl.gift.tools.authoring.server.gat.client.view.learner.PredictorTypeEnum;

/**
 * A ValueListBox that operates on PredictorTypeEnums and uses their display
 * names to populate the list.
 * @author elafave
 *
 */
public class PredictorValueListBox extends ValueListBox<PredictorTypeEnum> {

	public PredictorValueListBox() {
		super(new Renderer<PredictorTypeEnum>(){
			@Override
			public String render(PredictorTypeEnum predictorTypeEnum) {
				if(predictorTypeEnum == null) {
					return "";
				}
				return predictorTypeEnum.getDisplayName();
			}

			@Override
			public void render(PredictorTypeEnum predictorTypeEnum, Appendable appendable)
					throws IOException {
				String value = render(predictorTypeEnum);
				appendable.append(value);
			}
		});
	}
}
