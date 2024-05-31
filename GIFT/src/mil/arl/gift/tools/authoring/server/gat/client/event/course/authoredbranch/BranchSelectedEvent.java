/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course.authoredbranch;

import generated.course.AuthoredBranch;
import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * An event used to indicate when the user has selected an authored branch whose tree should be shown
 * 
 * @author nroberts
 */
public class BranchSelectedEvent extends GenericEvent {
	
	/** The branch that was selected */
	private AuthoredBranch branch;

	/**
	 * Creates a new event indicating that the given branch was selected
	 * 
	 * @param branch the branch that was selected.  Null is used to indicate the current tree is being exited.  E.g.
	 * the return to main branch breadcrumb was selected from within an authored branch course object.
	 */
	public BranchSelectedEvent(AuthoredBranch branch){
		this.branch = branch;
	}

	/**
	 * Gets the branch that was selected
	 * 
	 * @return the branch that was selected. Null is used to indicate the current tree is being exited.  E.g.
     * the return to main branch breadcrumb was selected from within an authored branch course object.
	 */
	public AuthoredBranch getBranch() {
		return branch;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[BranchSelectedEvent: branchName =");
        builder.append(branch.getTransitionName());
        builder.append("]");
        return builder.toString();
    }
	
	
}
