/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import net.customware.gwt.dispatch.shared.Action;

/**
 * An action that gets the description for a condition implementation class
 */
public class FetchConditionImplDescription  implements Action<FetchConditionImplDescriptionResult>{

	private String implClassName;

	/**
	 * Gets the name of the condition implementation class
	 * 
	 * @return the condition implementation class name (without "mil.arl.gift." package prefix)
	 */
	public String getImplClassName() {
		return implClassName;
	}

	/**
	 * Sets the name of the condition implementation class
	 * 
	 * @param implClassName the condition implementation class name (without "mil.arl.gift." package prefix)
	 */
	public void setImplClassName(String implClassName) {
		this.implClassName = implClassName;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchConditionImplDescription: ");
        sb.append("implClassName = ").append(implClassName);
        sb.append("]");

        return sb.toString();
    } 
}
