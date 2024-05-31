/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of a GenerateMetadataFile action
 * 
 * @author nroberts
 */
public class GenerateMetadataFileResult extends GatServiceResult {
    
    private MetadataWrapper metadataWrapper;
		
	/**
	 * Class constructor
	 * For serialization AND error/failure results only.
	 */
	public GenerateMetadataFileResult() {
		
    }
	
	/**
	 * Set the metadata information.
	 * 
	 * @param metadataWrapper contains information about the metadata content that was just created on the server.
	 * Can't be null.
	 */
	public GenerateMetadataFileResult(MetadataWrapper metadataWrapper){
	    setMetadataWrapper(metadataWrapper);
	}

	/**
	 * Return information about the metadata content that was just created on the server.
	 * 
	 * @return will be null only when the result is an error/failure.
	 */
    public MetadataWrapper getMetadataWrapper() {
        return metadataWrapper;
    }

    private void setMetadataWrapper(MetadataWrapper metadataWrapper) {
        if(metadataWrapper == null){
            throw new IllegalArgumentException("The metadata wrapper can't be null.");
        }
        
        this.metadataWrapper = metadataWrapper;    
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[GenerateMetadataFileResult: metadataWrapper=");
        builder.append(metadataWrapper);
        builder.append("]");
        return builder.toString();
    }
    
    
}
