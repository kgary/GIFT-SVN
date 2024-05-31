/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor.LearnerStateOutline;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import org.netbeans.swing.outline.DefaultOutlineCellRenderer;

/**
 * This renderer is used to customize individual cells in a learner state outline.
 * 
 * @author mzellars
 *
 */
public class CellRenderer extends DefaultOutlineCellRenderer {

	/** serial version uid */
	private static final long serialVersionUID = 1L;
	
	// The indices of each table column
	private static final int NAME_COL_INDEX = 0,
							 SHORT_TERM_COL_INDEX = 1,
							 SHORT_TERM_TIMESTAMP_COL_INDEX = 2,
							 LONG_TERM_COL_INDEX = 3,
							 LONG_TERM_TIMESTAMP_COL_INDEX = 4,
							 PREDICTED_COL_INDEX = 5,
							 PREDICTED_COL_TIMESTAMP_INDEX = 6;
	
	// The column colors that are used
	private static final Color NAME_COL_COLOR = Color.WHITE;
	private static final Color LIGHT_COL_COLOR = new Color(0xE9EDF4);
	private static final Color DARK_COL_COLOR = new Color(0xCED8E8);
	
	@Override
	public Component getTableCellRendererComponent(final JTable table, 
												   final Object value, 
												   final boolean isSelected, 
												   final boolean hasFocus, 
												   final int rowIndex, 
												   final int columnIndex) {
		
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, columnIndex);
	
		// Set an appropriate background color for this cell.		
		if (columnIndex == NAME_COL_INDEX) {
			// The name column
			cell.setBackground(NAME_COL_COLOR);
			
		}else if (columnIndex == SHORT_TERM_COL_INDEX || columnIndex == SHORT_TERM_TIMESTAMP_COL_INDEX) {
			// The short term value column and short term timestamp column
			cell.setBackground(DARK_COL_COLOR);
			
		}else if (columnIndex == LONG_TERM_COL_INDEX || columnIndex == LONG_TERM_TIMESTAMP_COL_INDEX) {
			// The long term value column and long term timestamp column
			cell.setBackground(LIGHT_COL_COLOR);
			
		}else if (columnIndex == PREDICTED_COL_INDEX || columnIndex == PREDICTED_COL_TIMESTAMP_INDEX) {
			// The predicted value column and predicted timestamp column
			cell.setBackground(DARK_COL_COLOR);
		}

		return cell;
	}
}
