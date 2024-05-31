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

import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.FilterTypeEnum;

public class FilterTypeValueListBox extends ValueListBox<FilterTypeEnum> {

	public FilterTypeValueListBox() {
		super(new Renderer<FilterTypeEnum>() {
			@Override
			public String render(FilterTypeEnum filterType) {
				if(filterType == null) {
					return "";
				}
				return filterType.getDisplayName();
			}

			@Override
			public void render(FilterTypeEnum filterType, Appendable appendable)
					throws IOException {
				String value = render(filterType);
				appendable.append(value);
			}
		});
		
		Comparator<FilterTypeEnum> comparator = new Comparator<FilterTypeEnum>() {
			@Override
			public int compare(FilterTypeEnum filterType1, FilterTypeEnum filterType2) {
				String displayName1 = filterType1.getDisplayName();
				String displayName2 = filterType2.getDisplayName();
				int result = displayName1.compareTo(displayName2);
				return result;
			}
		};

        FilterTypeEnum [] filterTypeEnums = FilterTypeEnum.values();
        List<FilterTypeEnum> acceptableValues = Arrays.asList(filterTypeEnums);
        Collections.sort(acceptableValues, comparator);
        
        setValue(acceptableValues.iterator().next());
        setAcceptableValues(acceptableValues);
	}
}
