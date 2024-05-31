/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

/**
 * Contains progress information for loading content in an external training application.
 * 
 * @author mhoffman
 *
 */
public class LoadProgress extends GenericJSONState {
    
    /**
     * JSON keys
     */
    public static final String TASK_KEY = "CurrentTask";
    public static final String SUBTASK_KEY = "CurrentSubtask";
    
    public static final String TASK_PROGRESS_KEY = "TaskProgress";
    public static final String SUBTASK_PROGRESS_KEY = "SubtaskProgress";
    
    public LoadProgress(){
        super();
    }

}
