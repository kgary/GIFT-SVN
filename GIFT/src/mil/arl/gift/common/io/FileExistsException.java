/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

/**
 * An exception thrown by file operations that check whether or not a file exists
 * 
 * @author nroberts
 */
public class FileExistsException extends DetailedException {

	private static final long serialVersionUID = 1L;
	
	/** The path to the file that exists */
	private String filePath;
	
	/**
	 * Creates a new exception containing the path to the file that exists
	 * 
	 * @param filePath the path to the file that exists. Can't be null;
	 * @param reason user friendly information about the exception.  Can't be null.
     * @param details developer friendly information about the exception. Can't be null.
     * @param cause the exception that caused this exception to be created.  Can be null.
	 */
	public FileExistsException(String filePath, String reason, String details, Throwable cause){
		super(reason, details, cause);
		
		 if(filePath == null || filePath.isEmpty()){
			 throw new IllegalArgumentException("The file path can't be null or empty.");
		 }
		
		this.filePath = filePath;
	}
	
	/**
	 * Gets the path to the file that exists
	 * 
	 * @return the path to the file that exists
	 */
	public String getFilePath(){
		return filePath;
	}
	
	/**
	 * Sets the path to the file that exists
	 * 
	 * @param filePath the path to the file that exists
	 */
	public void setFilePath(String filePath){
		this.filePath = filePath;
	}
}
