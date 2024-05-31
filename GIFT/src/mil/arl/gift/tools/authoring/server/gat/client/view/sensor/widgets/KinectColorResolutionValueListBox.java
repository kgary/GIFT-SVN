/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets;

import generated.sensor.KinectColorResolutionEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

public class KinectColorResolutionValueListBox extends ValueListBox<KinectColorResolutionEnum> {
	
	public KinectColorResolutionValueListBox() {
		super(new Renderer<KinectColorResolutionEnum>() {
			@Override
			public String render(KinectColorResolutionEnum colorResolution) {
				if(colorResolution == null) {
					return "";
				}
				return colorResolution.value();
			}

			@Override
			public void render(KinectColorResolutionEnum colorResolution, Appendable appendable)
					throws IOException {
				String value = render(colorResolution);
				appendable.append(value);
				
			}
		});
		
		Comparator<KinectColorResolutionEnum> comparator = new Comparator<KinectColorResolutionEnum>() {
			@Override
			public int compare(KinectColorResolutionEnum colorResolution1,
					KinectColorResolutionEnum colorResolution2) {
				String displayName1 = colorResolution1.value();
				String displayName2 = colorResolution2.value();
				int result = displayName1.compareTo(displayName2);
				return result;
			}
		};
		
		KinectColorResolutionEnum [] colorValues = KinectColorResolutionEnum.values();
		ArrayList<KinectColorResolutionEnum> acceptableValues = new ArrayList<KinectColorResolutionEnum>(Arrays.asList(colorValues));
		Collections.sort(acceptableValues, comparator);
		acceptableValues.add(0, null);

		setValue(null);
		setAcceptableValues(acceptableValues);
	}
}
