/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A GWT Cell used to render a drop-down list.
 */
public class DynamicSelectionCell extends AbstractInputCell<String, String> {

    interface Template extends SafeHtmlTemplates {

        @Template("<option value=\"{0}\">{0}</option>")
        SafeHtml deselected(String option);

        @Template("<option value=\"{0}\" selected=\"selected\">{0}</option>")
        SafeHtml selected(String option);
    }
    private static Template template;
    private HashMap<String, Integer> indexForOption = new HashMap<String, Integer>();
    private final List<String> options;
    
    /** Constant to control styling in the gat for the drop down lists */
    private static final String GAT_DROP_DOWN_LIST_STYLE = "gatDropDownListStyle";

    /**
     * Construct a new GWT SelectionCell with the specified options.
     *
     * @param options the options in the cell
     */
    public DynamicSelectionCell(List<String> options) {
        super("change");
        if (template == null) {
            template = GWT.create(Template.class);
        }
        if (options != null) {
            this.options = new ArrayList<String>(options);
            int index = 0;
            for (String option : options) {
                indexForOption.put(option, index++);
            }
        } else {
            this.options = new ArrayList<String>();
        }
    }
    
    /**
     * Sets the options displayed in this cell.
     * @param options Options to display in this cell.
     */
    public void setOptions(List<String> options) {
    	this.options.clear();
    	for(String option : options) {
    		this.options.add(new String(option));
    	}
    	refreshIndexes();
    }

    /**
     * Add an option to the list
     * 
     * @param newOp The option to add
     */
    public void addOption(String newOp) {
        String option = new String(newOp);
        options.add(option);
        refreshIndexes();
    }

    /**
     * Remove an option from the list
     * 
     * @param op The option to remove
     */
    public void removeOption(String op) {
        String option = new String(op);
        options.remove((int) indexForOption.get(option));
        refreshIndexes();
    }

    /**
     * Refreshes the indexes for options in the list
     */
    private void refreshIndexes() {
        int index = 0;
        for (String option : options) {
            indexForOption.put(option, index++);
        }
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, String value,
            NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        String type = event.getType();
        if ("change".equals(type)) {
            Object key = context.getKey();
            SelectElement select = parent.getFirstChild().cast();
            String newValue = options.get(select.getSelectedIndex());
            setViewData(key, newValue);
            finishEditing(parent, newValue, key, valueUpdater);
            if (valueUpdater != null) {
                valueUpdater.update(newValue);
            }
        }
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        // Get the view data.
        Object key = context.getKey();
        String viewData = getViewData(key);
        if (viewData != null && viewData.equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        int selectedIndex = getIndex(viewData == null ? value : viewData);
        sb.appendHtmlConstant("<select tabindex=\"-1\" class='" + GAT_DROP_DOWN_LIST_STYLE + "'>");
        int index = 0;
        for (String option : options) {
            if (index++ == selectedIndex) {
                sb.append(template.selected(option));
            } else {
                sb.append(template.deselected(option));
            }
        }
        sb.appendHtmlConstant("</select>");
    }

    /**
     * Gets the index for a value in the list
     * 
     * @param value The value to get the index of
     * @return int The index of the value in the list
     */
    public int getIndex(String value) {
        Integer index = indexForOption.get(value);
        if (index == null) {
            return -1;
        }
        return index.intValue();
    }
}
