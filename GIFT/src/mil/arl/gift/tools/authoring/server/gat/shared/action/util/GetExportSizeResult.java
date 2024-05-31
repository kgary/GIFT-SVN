/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * A result containing the size of the export
 * 
 * @author bzahid
 */
public class GetExportSizeResult extends GatServiceResult {
	
	/** the export size */
	private double exportSize;
	
	/**
	 * Class constructor for serialization only.
	 */
	public GetExportSizeResult() {
	}
	
	/**
	 * Sets the export size.
	 * 
	 * @param exportSize the size of the export
	 */
	public void setExportSize(double exportSize) {
		this.exportSize = exportSize;
	}
	
	/**
	 * Gets the export size.
	 * 
	 * @return the export size
	 */
	public double getExportSize() {
		return exportSize;
	}
}
