/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets;

import generated.sensor.KinectDepthResolutionEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

public class KinectDepthResolutionValueListBox extends ValueListBox<KinectDepthResolutionEnum> {
	
	public KinectDepthResolutionValueListBox() {
		super(new Renderer<KinectDepthResolutionEnum>() {
			@Override
			public String render(KinectDepthResolutionEnum depthResolution) {
				if(depthResolution == null) {
					return "";
				}
				return depthResolution.value();
			}

			@Override
			public void render(KinectDepthResolutionEnum depthResolution, Appendable appendable)
					throws IOException {
				String value = render(depthResolution);
				appendable.append(value);
			}
		});
		
		Comparator<KinectDepthResolutionEnum> comparator = new Comparator<KinectDepthResolutionEnum>() {
			@Override
			public int compare(KinectDepthResolutionEnum depthResolution1,
					KinectDepthResolutionEnum depthResolution2) {
				String displayName1 = depthResolution1.value();
				String displayName2 = depthResolution2.value();
				int result = displayName1.compareTo(displayName2);
				return result;
			}
		};
		
		KinectDepthResolutionEnum [] depthEnums = KinectDepthResolutionEnum.values();
		ArrayList<KinectDepthResolutionEnum> acceptableValues = new ArrayList<KinectDepthResolutionEnum>(Arrays.asList(depthEnums));
		Collections.sort(acceptableValues, comparator);
		acceptableValues.add(0, null);
		
		setValue(null);
		setAcceptableValues(acceptableValues);
	}
}
