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

import mil.arl.gift.tools.authoring.server.gat.client.view.learner.ClassifierTypeEnum;

/**
 * A ValueListBox that operates on ClassifierTypeEnums and uses their display
 * names to populate the list.
 * @author elafave
 *
 */
public class ClassifierValueListBox extends ValueListBox<ClassifierTypeEnum> {

	public ClassifierValueListBox() {
		super(new Renderer<ClassifierTypeEnum>(){
			@Override
			public String render(ClassifierTypeEnum classifierTypeEnum) {
				if(classifierTypeEnum == null) {
					return "";
				}
				return classifierTypeEnum.getDisplayName();
			}

			@Override
			public void render(ClassifierTypeEnum classifierTypeEnum, Appendable appendable)
					throws IOException {
				String value = render(classifierTypeEnum);
				appendable.append(value);
			}
		});
	}
}
