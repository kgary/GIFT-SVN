/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.WriterTypeEnum;

public class WriterTypeValueListBox extends ValueListBox<WriterTypeEnum> {

	public WriterTypeValueListBox() {
		super(new Renderer<WriterTypeEnum>(){
			@Override
			public String render(WriterTypeEnum writerType) {
				if(writerType == null) {
					return "";
				}
				return writerType.getDisplayName();
			}

			@Override
			public void render(WriterTypeEnum writerType, Appendable appendable)
					throws IOException {
				String value = render(writerType);
				appendable.append(value);
			}
		});
		
		Comparator<WriterTypeEnum> comparator = new Comparator<WriterTypeEnum>() {
			@Override
			public int compare(WriterTypeEnum writerType1, WriterTypeEnum writerType2) {
				String displayName1 = writerType1.getDisplayName();
				String displayName2 = writerType2.getDisplayName();
				int result = displayName1.compareTo(displayName2);
				return result;
			}
		};

        WriterTypeEnum [] writerTypeEnums = WriterTypeEnum.values();
        List<WriterTypeEnum> acceptableValues = Arrays.asList(writerTypeEnums);
        Collections.sort(acceptableValues, comparator);
        
        setValue(acceptableValues.iterator().next());
        setAcceptableValues(acceptableValues);
	}
}
