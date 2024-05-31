/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor.LearnerStateOutline;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.netbeans.swing.outline.RowModel;

/**
 * A learner state outline represents the learner's current state in both 
 * tree form and table form.  This class displays data in the form of a table.
 * 
 * @author mzellars
 *
 */
public class LearnerStateRowModel implements RowModel {
	
	private static final int SHORT_TERM_COL_INDEX = 0,
			 				 SHORT_TERM_TIMESTAMP_COL_INDEX = 1,
			 				 LONG_TERM_COL_INDEX = 2,
			 				 LONG_TERM_TIMESTAMP_COL_INDEX = 3,
			 				 PREDICTED_COL_INDEX = 4,
			 				 PREDICTED_COL_TIMESTAMP_INDEX = 5;
	
	/** Column indices of short-term, long-term, and predicted, as well as their respective timestamps. */
	private static final String[] columns = new String[]{"Short term", "Timestamp", "Long term", "Timestamp", "Predicted", "Timestamp"};

	/** The format used to display timestamps */
	private static final DateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
	
    @Override
    public int getColumnCount() {
        return columns.length;
    }

    /**
     * Displays data in the appropriate column.
     */
    @Override
    public Object getValueFor(Object node, int column) {
    	@SuppressWarnings("unchecked")
		LearnerStateNode<String> n = (LearnerStateNode<String>)node;
    	
    	switch (column) {
    	
    	case SHORT_TERM_COL_INDEX:
    		return n.getShortTermVal(); // Short term column
    	case SHORT_TERM_TIMESTAMP_COL_INDEX:
    		return millisToTime(n.getSTTimestamp()); // Short term timestamp column
    	case LONG_TERM_COL_INDEX:
    		return n.getLongTermVal(); // Long term column
    	case LONG_TERM_TIMESTAMP_COL_INDEX:
    		return millisToTime(n.getLTTimestamp()); // Long term timestamp column
    	case PREDICTED_COL_INDEX:
    		return n.getPredictedVal(); // Predicted column
    	case PREDICTED_COL_TIMESTAMP_INDEX:
    		return millisToTime(n.getPTimestamp()); // Predicted timestamp column
    	default:
    		assert false;
    	}
    	
    	return null;
    }

    @Override
    public Class<?> getColumnClass(int column) {
    	// All column data is of the String class
        return String.class;
    }

    @Override
    public boolean isCellEditable(Object o, int i) {
        return false;
    }

    @Override
    public void setValueFor(Object o, int i, Object o1) {
        // do nothing for now
    }

    @Override
    public String getColumnName(int i) {
        return columns[i];
    }
    
    /**
     * Creates a string to display milliseconds as time
     * 
     * @param ms the milliseconds that will be converted to time
     * 
     * @return the string representing the time
     */
    protected String millisToTime(long ms) {

    	Date date = new Date(ms);
    	
    	return formatter.format(date);
    }
}
