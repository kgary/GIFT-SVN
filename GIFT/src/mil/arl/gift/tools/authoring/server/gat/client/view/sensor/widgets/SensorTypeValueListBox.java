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
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationMaps;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

public class SensorTypeValueListBox extends ValueListBox<SensorTypeEnum> {

	public SensorTypeValueListBox() {
		super(new Renderer<SensorTypeEnum>() {
			@Override
			public String render(SensorTypeEnum sensorType) {
				if(sensorType == null) {
					return "";
				}
				return sensorType.getDisplayName();
			}

			@Override
			public void render(SensorTypeEnum sensorType, Appendable appendable)
					throws IOException {
				String value = render(sensorType);
				appendable.append(value);
			}
		});
		
		Comparator<SensorTypeEnum> comparator = new Comparator<SensorTypeEnum>() {
			@Override
			public int compare(SensorTypeEnum sensorType1, SensorTypeEnum sensorType2) {
				String displayName1 = sensorType1.getDisplayName();
				String displayName2 = sensorType2.getDisplayName();
				int result = displayName1.compareTo(displayName2);
				return result;
			}
		};
		
        Set<SensorTypeEnum> sensorTypeEnums = SensorsConfigurationMaps.getInstance().getSensorTypeToImplMap().keySet();
        ArrayList<SensorTypeEnum> acceptableValues = new ArrayList<SensorTypeEnum>(sensorTypeEnums);
        Collections.sort(acceptableValues, comparator);
        
        setValue(acceptableValues.iterator().next());
        setAcceptableValues(acceptableValues);
	}
}
