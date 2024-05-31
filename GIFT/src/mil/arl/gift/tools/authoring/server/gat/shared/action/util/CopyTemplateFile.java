/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;
 
import net.customware.gwt.dispatch.shared.Action;
 
/**
 * An action telling the sever to copy a file from the templates folder.
 * 
 * @author bzahid
 *
 */
public class CopyTemplateFile implements Action<CopyWorkspaceFilesResult> {
	
	/** The username of the user for which files are being copied. */
	private String username;
	
	/** The destination file name. */
	private String targetFilename;
	
	/** The course folder name. */
	private String courseFolderName;
	
	/** The file extension of the template to copy. */
	private String fileExtension;
	
	/** The flag to indicate if a UUID should be appended to the target filename */
	private boolean appendUUIDToFilename = true;

    /**
     * Required for GWT serialization policy
     */
    @SuppressWarnings("unused")
    private CopyTemplateFile() {
    }
    
    /**
     * Creates a new action.
     * 
     * @param username the username of the user for which files are being copied
     * @param courseFolderName the name of the course folder.  Can't be null or empty.
     * @param targetFilename the new name for the copied file. This name will be appended with a UUID. 
     * @param fileExtension the file extension. This will determine which template is copied. Can't be null or empty.</br>
     * Refer to the logic in
     * {@see mil.arl.gift.tools.authoring.server.gat.server.handler.util.CopyTemplateFileHandler#execute(CopyTemplateFile, net.customware.gwt.dispatch.server.ExecutionContext)}.
     */
    public CopyTemplateFile(String username, String courseFolderName, String targetFilename, String fileExtension) {
        setUsername(username);
        setTargetFilename(targetFilename);
        setCourseFolderName(courseFolderName);
        setFileExtension(fileExtension);
        
    }
    
    /**
     * Gets the username of the user for which files are being copied
     * 
     * @return the username of the user for which files are being copied
     */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username of the user for which files are being copied
	 * 
	 * @param username the username of the user for which files are being copied
	 */
	private void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the destination file name.
	 * 
	 * @return the destination file name.
	 */
	public String getTargetName() {
		return targetFilename;
	}

	/**
	 * Sets the destination file name.
	 * 
	 * @param targetFilename the destination file name.
	 */
	private void setTargetFilename(String targetFilename) {
		this.targetFilename = targetFilename;
	}

	/**
	 * Gets the course folder name
	 * 
	 * @return the course folder name. Won't be null or empty.
	 */
	public String getCourseFolderName() {
		return courseFolderName;
	}

	/**
	 * Sets the course folder name
	 * 
	 * @param courseFolderName the course folder name. Can't be null or empty.
	 */
	private void setCourseFolderName(String courseFolderName) {
	    
        if(courseFolderName == null || courseFolderName.isEmpty()){
            throw new IllegalArgumentException("The course folder name can't be null or empty.");
        }
	       
		this.courseFolderName = courseFolderName;
	}

	/**
	 * Gets the file extension of the template to copy
	 * 
	 * @return the file extension of the template to copy.  Won't be null or empty.
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * Sets the file extension of the template to copy
	 * 
	 * @param fileExtension the file extension of the template to copy.  Can't be null or empty.</br>
	 * Refer to the logic in
     * {@see mil.arl.gift.tools.authoring.server.gat.server.handler.util.CopyTemplateFileHandler#execute(CopyTemplateFile, net.customware.gwt.dispatch.server.ExecutionContext)}.
	 */
	private void setFileExtension(String fileExtension) {
	    
	    if(fileExtension == null || fileExtension.isEmpty()){
	        throw new IllegalArgumentException("The file extension can't be null or empty.");
	    }
		this.fileExtension = fileExtension;
	}

    /**
     * Retrieves the flag to indicate if a UUID should be appended to the {@link #targetFilename}.
     * Default is true.
     * 
     * @return true to append a UUID; false otherwise.
     */
    public boolean isAppendUUIDToFilename() {
        return appendUUIDToFilename;
    }

    /**
     * Set the flag to indicate if a UUID should be appended to the {@link #targetFilename}.
     * Defaults to true.
     * 
     * @param appendUUIDToFilename true to append a UUID; false otherwise.
     */
    public void setAppendUUIDToFilename(boolean appendUUIDToFilename) {
        this.appendUUIDToFilename = appendUUIDToFilename;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[CopyTemplateFile: ");
        sb.append("username = ").append(username);
        sb.append(", targetFilename = ").append(targetFilename);
        sb.append(", courseFolderName = ").append(courseFolderName);
        sb.append(", fileExtension = ").append(fileExtension);
        sb.append(", appendUUIDToFilename = ").append(isAppendUUIDToFilename());
        sb.append("]");

        return sb.toString();
    } 
}
