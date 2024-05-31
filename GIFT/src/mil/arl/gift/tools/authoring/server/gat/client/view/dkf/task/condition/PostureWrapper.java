/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

/**
 * Holds the name of the posture and a flag to indicate if it is selected.
 * 
 * @author elafave
 *
 */
public class PostureWrapper {

	private String posture;
	
	private boolean selected = false;

	/**
	 * Gets the posture
	 * @return the posture
	 */
	public String getPosture() {
		return posture;
	}

	/**
	 * Sets the posture
	 * @param posture the posture
	 */
	public void setPosture(String posture) {
		this.posture = posture;
	}

	/**
	 * Gets whether or not it is selected
	 * @return true if selected, false otherwise
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets the selected flag.
	 * @param selected Whether the posture wrapper should be selected.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}