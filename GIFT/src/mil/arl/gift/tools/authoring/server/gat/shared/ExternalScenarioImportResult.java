/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The result of importing a scenario from an external system
 * 
 * @author nroberts
 */
public class ExternalScenarioImportResult implements IsSerializable {

    /** The path to the course that was created for the imported scenario */
    private String importedCoursePath;
    
    /** The username, as resolved on the server. */
    private String resolvedUsername;
    
    /**
     * Default constructor required for GWT serialization
     */
    protected ExternalScenarioImportResult() {}

    /**
     * Creates a new result for importing a scenario
     * 
     * @param importedCoursePath the path to the course that was created for the imported scenario. Cannot be null.
     * @param resolvedUsername he username, as resolved on the server. Can be null.
     */
    public ExternalScenarioImportResult(String importedCoursePath, String resolvedUsername) {
        this();
        this.importedCoursePath = importedCoursePath;
        this.resolvedUsername = resolvedUsername;
    }
    
    /**
     * Get the username, as resolved on the server
     * 
     * @return the resolved username. Can be null.
     */
    public String getResolvedUsername() {
        return resolvedUsername;
    }

    /**
     * Gets the path to the course that was created from the imported scenario
     * 
     * @return the course path. Cannot be null.
     */
    public String getImportedCoursePath() {
        return importedCoursePath;
    }
}
