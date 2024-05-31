/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor.LearnerStateOutline;

import mil.arl.gift.common.io.Constants;

/**
 * A custom row model for performance learner state that adds a column for the performance node (i.e. task/concept) state.
 * 
 * @author mhoffman
 *
 */
public class PerformanceLearnerStateRowModel extends LearnerStateRowModel {

    private static final int NODE_STATE_COL_INDEX = 0,
            SHORT_TERM_COL_INDEX = 1,
             SHORT_TERM_TIMESTAMP_COL_INDEX = 2,
             LONG_TERM_COL_INDEX = 3,
             LONG_TERM_TIMESTAMP_COL_INDEX = 4,
             PREDICTED_COL_INDEX = 5,
             PREDICTED_COL_TIMESTAMP_INDEX = 6;
    
    /** Column indices of short-term, long-term, and predicted, as well as their respective timestamps. */
    private static final String[] columns = new String[]{"State", "Short term", "Timestamp", "Long term", "Timestamp", "Predicted", "Timestamp"};
    
    /**
     * Displays data in the appropriate column.
     */
    @Override
    public Object getValueFor(Object node, int column) {
        @SuppressWarnings("unchecked")
        LearnerStateNode<String> n = (LearnerStateNode<String>)node;
        
        switch (column) {
        
        case NODE_STATE_COL_INDEX:
            return n.getNodeState();
        case SHORT_TERM_COL_INDEX:
            return n.getShortTermVal() + (n.isShortTemValHold() ? " (HOLD)" : Constants.EMPTY); // Short term column
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
    public int getColumnCount() {
        return columns.length;
    }
    
    @Override
    public String getColumnName(int i) {
        return columns[i];
    }
}
