/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.cell;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * An extension of GWT's SelectionCell designed to add new features on top of SelectionCell's default functionality. New
 * functionality incorporated into this class includes the following:<br>
 * <ul>
 * 		<li>
 *  	Ability to dynamically update the list of options to choose from (done via  {@link #setDefaultOptions(List)}),
 *  	</li>
 *  	<li>
 *  	Ability to dynamically set the list of options for each individual row of a HasData display (done via  
 *  	{@link #setRowOptions(Object, List)}). 
 *  	</li>
 *  </ul><br>
 *  This class can be used as a replacement for SelectionCell in cases where it may be helpful to use these features.
 * 
 * @author nroberts
 *
 */
public class ExtendedSelectionCell extends AbstractInputCell<String, String> implements HasRowOptions<Object, String>{

	/**
	 * The Interface Template.
	 */
	interface Template extends SafeHtmlTemplates {
		
		/**
		 * Deselected.
		 *
		 * @param option the option
		 * @return the safe html
		 */
		@Template("<option value=\"{0}\">{0}</option>")
		SafeHtml deselected(String option);

		/**
		 * Selected.
		 *
		 * @param option the option
		 * @return the safe html
		 */
		@Template("<option value=\"{0}\" selected=\"selected\">{0}</option>")
		SafeHtml selected(String option);
	}

	/** The template. */
	private static Template template;

	/**  A map linking each key to another map which links that key's options to their respective indexes. */
	private HashMap<Object, HashMap<String, Integer>> keyToOptionIndexPairs = new HashMap<Object, HashMap<String, Integer>>();

	/**  A map linking each key to its respective list of options. */
	private /*final*/ HashMap<Object, List<String>> keyToOptions;
	
	/** The default set of options to be used by each key until {@link #setRowOptions(Object, List)} is called on it. */
	private List<String> defaultOptions;
	
	/** Constant to control styling in the gat for the drop down lists */
	private static final String GAT_DROP_DOWN_LIST_STYLE = "gatDropDownListStyle";

	/**
	 * Constructs a new ExtendedSelection cell with an empty list of default options.
	 */
	public ExtendedSelectionCell() {
		
		super(BrowserEvents.CHANGE);
		
		if (template == null) {
			template = GWT.create(Template.class);
		}
		
		this.keyToOptions = new HashMap<Object, List<String>>();
		this.defaultOptions = new ArrayList<String>();
	}
	
	/**
	 * Constructs a new ExtendedSelection cell with the specified default list of options to choose from.
	 * 
	 * @param defaultOptions the default list of options to choose from
	 */
	public ExtendedSelectionCell(List<String> defaultOptions){
		this();
		
		this.defaultOptions = defaultOptions;
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.cell.client.AbstractInputCell#onBrowserEvent(com.google.gwt.cell.client.Cell.Context, com.google.gwt.dom.client.Element, java.lang.Object, com.google.gwt.dom.client.NativeEvent, com.google.gwt.cell.client.ValueUpdater)
	 */
	@Override
	public void onBrowserEvent(Context context, Element parent, String value, 
			NativeEvent event, ValueUpdater<String> valueUpdater) {
		
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		
		String type = event.getType();
		
		if (BrowserEvents.CHANGE.equals(type)) {
			
			Object key = context.getKey();
			SelectElement select = parent.getFirstChild().cast();
			
			List<String> keyOptions = 
					keyToOptions.get(context.getKey()) != null && keyToOptions.get(context.getKey()) != null ? 
					keyToOptions.get(context.getKey()) : 
					defaultOptions;
					
			String newValue = keyOptions.get(select.getSelectedIndex());
			setViewData(key, newValue);
			
			finishEditing(parent, newValue, key, valueUpdater);
		
			if (valueUpdater != null) {
				valueUpdater.update(newValue);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
	 */
	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		
		// Get the view data.
		Object key = context.getKey();
		String viewData = getViewData(key);
		
		if (viewData != null && viewData.equals(value)) {
			clearViewData(key);
			viewData = null;
		}

		int selectedIndex = getSelectedIndex(context.getKey(),
				viewData == null ? value : viewData);
		
		sb.appendHtmlConstant("<select tabindex=\"-1\" class='" + GAT_DROP_DOWN_LIST_STYLE + "'>");
		
		int index = 0;
		List<String> keyOptions = context.getKey() != null && keyToOptions.get(context.getKey()) != null ? keyToOptions
				.get(context.getKey()) : defaultOptions;
				
		for (String option : keyOptions) {
			
			if (index++ == selectedIndex) {
				sb.append(template.selected(option));
				
			} else {
				sb.append(template.deselected(option));
			}
		}
		
		sb.appendHtmlConstant("</select>");
	}

	/**
	 * Gets the index of the specified selected value associated with the specified key.
	 * 
	 * @param key The key with which the specified value is associated
	 * @param value The selected value
	 * @return  the index of the selected value
	 */
	private int getSelectedIndex(Object key, String value) {
		
		Integer index = keyToOptionIndexPairs.get(key) != null && keyToOptionIndexPairs.get(key).get(value) != null 
				? keyToOptionIndexPairs.get(key).get(value) 
				: defaultOptions.indexOf(value);
		
		return index.intValue();
	}

	@Override
	public List<String> getRowOptions(Object key) {
		return keyToOptions.get(key);
	}

	@Override
	public void setRowOptions(Object key, List<String> options) {
		
		keyToOptions.put(key, options);

		HashMap<String, Integer> indexForKeyOption = new HashMap<String, Integer>();

		int index = 0;
		for (String option : keyToOptions.get(key)) {
			indexForKeyOption.put(option, index++);
		}

		keyToOptionIndexPairs.put(key, indexForKeyOption);
	}
	
	@Override
	public void setDefaultOptions(List<String> defaultOptions){
		this.defaultOptions = defaultOptions;
	}
	
	@Override
	public List<String> getDefaultOptions(){
		return defaultOptions;
	}
}
