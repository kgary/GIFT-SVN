/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.model.dkf;

import java.io.Serializable;

/**
 * The Class DkfStrategySummary.
 */
public class StrategyRefSummary implements Serializable{
	
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

	/** The name. */
	String name;
	
	/** The type. */
	Serializable type;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Serializable getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(Serializable type) {
		this.type = type;
	}		
}

