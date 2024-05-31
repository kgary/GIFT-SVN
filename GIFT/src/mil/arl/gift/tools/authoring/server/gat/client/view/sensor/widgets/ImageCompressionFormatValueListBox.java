/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets;

import generated.sensor.ImageCompressionFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

public class ImageCompressionFormatValueListBox extends ValueListBox<ImageCompressionFormat> {
	
	public ImageCompressionFormatValueListBox() {
		super(new Renderer<ImageCompressionFormat>() {
			@Override
			public String render(ImageCompressionFormat imageCompressionFormat) {
				if(imageCompressionFormat == null) {
					return "";
				}
				return imageCompressionFormat.value();
			}

			@Override
			public void render(ImageCompressionFormat imageCompressionFormat, Appendable appendable)
					throws IOException {
				String value = render(imageCompressionFormat);
				appendable.append(value);
				
			}
		});
		
		Comparator<ImageCompressionFormat> comparator = new Comparator<ImageCompressionFormat>() {
			@Override
			public int compare(ImageCompressionFormat imageCompressionFormat1,
					ImageCompressionFormat imageCompressionFormat2) {
				String value1 = imageCompressionFormat1.value();
				String value2 = imageCompressionFormat2.value();
				int result = value1.compareTo(value2);
				return result;
			}
		};
		
		ImageCompressionFormat [] enums = ImageCompressionFormat.values();
		ArrayList<ImageCompressionFormat> acceptableValues = new ArrayList<ImageCompressionFormat>(Arrays.asList(enums));
		Collections.sort(acceptableValues, comparator);
		acceptableValues.add(0, null);

		setValue(null);
		setAcceptableValues(acceptableValues);
	}
}
