/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action;

/**
 * We've got several tools (course, dkf, sensor, etc) that save JAXB objects
 * and this is the result object that is paired with those requests.
 * @author elafave
 */
public class SaveJaxbObjectResult extends GatServiceResult {

	/** The version assigned to the jaxb file when it was saved. */
	private String newVersion;
	
	/** Indicates if the file passed schema validation when it was saved. */
	private boolean schemaValid = true;
		
	/**
	 * Class constructor
	 * For serialization only.
	 */
	public SaveJaxbObjectResult() {
    }

	/**
	 * Gets the new version of the file.
	 * @return The new version of the file.
	 */
	public String getNewVersion() {
		return newVersion;
	}

	/**
	 * Sets the new version of the file.
	 * @param newVersion New version of the file.
	 */
	public void setNewVersion(String newVersion) {
		this.newVersion = newVersion;
	}

	/**
	 * 
	 * @return True if the file passed schema validation when it was saved,
	 * false otherwise.
	 */
	public boolean isSchemaValid() {
		return schemaValid;
	}

	/**
	 * 
	 * @param schemaValid True if the file passed schema validation when it was
	 * saved, false otherwise.
	 */
	public void setSchemaValid(boolean schemaValid) {
		this.schemaValid = schemaValid;
	}
}
