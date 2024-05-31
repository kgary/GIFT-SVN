/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * A result containing progress information related to an ongoing task on the server
 * 
 * @author nroberts
 */
public class GetProgressResult extends GatServiceResult {	
	
    /** indicator of progress */
	private ProgressIndicator progress = null;

    /**
     * Instantiates a new result
     */
    public GetProgressResult() {
        super();
    }

    /**
     * Gets the current progress
     * 
     * @return the current progress
     */
	public ProgressIndicator getProgress() {
		return progress;
	}

	/**
	 * Sets the current progress
	 * 
	 * @param progress the current progress
	 */
	public void setProgress(ProgressIndicator progress) {
		this.progress = progress;
	}
}
