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

import mil.arl.gift.tools.authoring.server.gat.client.view.learner.TranslatorTypeEnum;

/**
 * A ValueListBox that operates on TranslatorTypeEnums and uses their display
 * names to populate the list.
 * @author elafave
 *
 */
public class TranslatorValueListBox extends ValueListBox<TranslatorTypeEnum> {

	public TranslatorValueListBox() {
		super(new Renderer<TranslatorTypeEnum>(){
			@Override
			public String render(TranslatorTypeEnum translatorTypeEnum) {
				if(translatorTypeEnum == null) {
					return "";
				}
				return translatorTypeEnum.getDisplayName();
			}

			@Override
			public void render(TranslatorTypeEnum translatorTypeEnum, Appendable appendable)
					throws IOException {
				String value = render(translatorTypeEnum);
				appendable.append(value);
			}
		});
	}
	
}
