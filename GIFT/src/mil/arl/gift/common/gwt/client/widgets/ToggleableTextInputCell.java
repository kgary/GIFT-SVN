/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell that can be toggled between edit mode or display only mode.
 * @author elafave
 *
 */
public class ToggleableTextInputCell extends TextInputCell{
	
    /** Indicates if the cell is editable */
	private boolean editable = true;
	
	@Override
	public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
	    if(editable) {
			super.render(context, value, sb);
		} else {
			sb.appendEscaped(value == null ? "" : value);
		}
	}

	/**
	 * Determines if the cell is editable.
	 * @return True if the cell is editable, false otherwise.
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * Sets the editable status of the cell.
	 * @param editable True for the cell to be editable, false otherwise.
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

}
