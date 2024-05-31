/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * An extension of GWT's TextInputCell designed specifically to handle number-only inputs. 
 * 
 * Note: This class does NOT handle improperly formatted number values but simply prevents non-numeric characters from being entered. 
 * Because of this, number format exceptions should be expected and should be handled by the appropriate FieldUpdaters.
 * 
 * @author nroberts
 */
public class NumericTextInputCell extends TextInputCell{
	

	interface Template extends SafeHtmlTemplates {
	    @Template("<input style='text-align: right; {1}' placeholder='Enter a number' type=\"text\" value=\"{0}\" tabindex=\"-1\"></input>")
	    SafeHtml input(String value, String styleProperties);
	  }
	
	
	private static Template template;
	
	/**
	 * Creates a NumericTextInputCell
	 */
	public NumericTextInputCell(){
		super();
		
		if (template == null) {
			template = GWT.create(Template.class);
		}
	}
	
	private String styleProperties = null;
	
	/**
	 * Creates a NumericTextInputCell with the specified style properties. Since the styling of TextInputCells can only be modified by 
	 * overriding the class' template, this constructor exists as a way to set the styling from other classes.
	 * 
	 * !IMPORTANT!: The string passed in must be of the form "property1: value; property2: value;" to qualify as valid CSS. This is 
	 * because all cells use SafeHTML to handle the rendering process. As a result, if you give this constructor a string that does 
	 * not follow this form, you may run into runtime errors related to improperly formatted HTML.
	 * 
	 * @param styleProperties the style properties to use
	 */
	public NumericTextInputCell(String styleProperties){
		super();
		
		if (template == null) {
			template = GWT.create(Template.class);
		}
		
		this.styleProperties = styleProperties;
	}

	@Override
	public void render(Context context, java.lang.String value, SafeHtmlBuilder sb){
		
		// Get the view data.
		Object key = context.getKey();
		ViewData viewData = getViewData(key);
		if (viewData != null && viewData.getCurrentValue().equals(value)) {
			clearViewData(key);
			viewData = null;
		}

		String s = (viewData != null) ? viewData.getCurrentValue() : value;
		if (s != null) {
			
			if(styleProperties != null){
				sb.append(template.input(s, styleProperties));

			} else {
				sb.append(template.input(s, ""));
			}
		} else {
			sb.appendHtmlConstant("<input style='text-align: right;' placeholder='Enter a number' type=\"text\" tabindex=\"-1\"></input>");
		}
	}
	
	@Override
	public void onBrowserEvent(Cell.Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> updater){
								
		if(!KeyboardEventFilterUtil.filterOutNonNumericKeys(event)){
			super.onBrowserEvent(context, parent, value, event, updater);
		}
	}
}