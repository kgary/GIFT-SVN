/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

/**
 * An object that is used as TutorUserInterfaceFeedback parameter to
 * signify that all previous text in the TUI will be removed before 
 * the next message is displayed. 
 *
 * @author mzellars
 *
 */
public class ClearTextAction implements FeedbackAction, Serializable {

	private static final long serialVersionUID = 1L;
	
	public ClearTextAction() {	
	}
	
	@Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[ClearTextAction]");
        return sb.toString();
    }

	@Override
	public boolean hasAudio() {
		return false;
	}
}
