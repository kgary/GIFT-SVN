/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import generated.metadata.Metadata;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action that causes a metadata file to be created and saved to disk.
 * 
 * @author nroberts
 */
public class GenerateMetadataFile implements Action<GenerateMetadataFileResult>{
	
	/** 
	 * The metadata to be generated
	 */
	private Metadata metadata;

	/**
	 * The name of the file the metadata is being generated for
	 */
	private String targetFilename;
	
	/** The user name. */
	private String userName;

	/**
	 * No-arg constructor. Needed for serialization
	 */
	public GenerateMetadataFile() {
	}	
	
	/**
	 * Constructor.
	 *
	 * @param metadata the metadata to be generated
	 * @param targetFilename the name of the file the metadata is being generated for
	 */
	public GenerateMetadataFile(Metadata metadata, String targetFilename) {
		this.metadata = metadata;
		this.setTargetFilename(targetFilename);
	}

	/**
	 * Gets the metadata to be generated 
	 * 
	 * @return the metadata to be generated 
	 */
	public Metadata getMetadata() {
		return metadata;
	}

	/**
	 * Sets the metadata to be generated 
	 * 
	 * @param metadata the metadata to be generated. 
	 */
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * Gets the name of the file the metadata is being generated for
	 * 
	 * @return the name of the file the metadata is being generated for
	 */
	public String getTargetFilename() {
		return targetFilename;
	}

	/**
	 * Sets the name of the file the metadata is being generated for
	 * 
	 * @param targetFilename the name of the file the metadata is being generated for
	 */
	public void setTargetFilename(String targetFilename) {
		this.targetFilename = targetFilename;
	}

	/**
	 * Gets the user name.
	 * @return User name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * @param userName User name.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[GenerateMetadataFile: ");
        sb.append("metadata").append(metadata);
        sb.append(", targetFilename").append(targetFilename);
        sb.append(", userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
