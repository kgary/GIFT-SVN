/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.io.Serializable;

import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * A result containing a JAXB object.
 */
public class FetchJAXBObjectResult extends GatServiceResult {
	
	/** The JAXB object. */
	private Serializable theJAXBObject;
	
	/**
	 * Indicates the JAXB Object we first loaded was created against an
	 * outdated schema and therefore we had to try to upconvert it. If
	 * both the success and conversionAttempted attributes are true it
	 * means the JAXObject represents an upconverted version of the
	 * outdated file we originally tried to load and this object has no
	 * on disk representation.
	 */
	private boolean conversionAttempted = false;

	/** The path of the file the object was loaded from */
	private String filePath = null;

	/** The name of the file the object was loaded from*/
	private String fileName = null;
	
	/** Whether or not the current user has write access to object */
	private boolean isModifiable;
	
	/** The course validation results containing detailed errors about the course. */
	private CourseValidationResults courseValidationResults = null;
	
    /**
     * Default public constructor.
     */
    public FetchJAXBObjectResult() {
        super();
    }

	/**
	 * Gets the JAXB object.
	 *
	 * @return the JAXB object
	 */
	public Serializable getJAXBObject() {
		return theJAXBObject;
	}

	/**
	 * Sets the JAXB object.
	 *
	 * @param theJAXBObject the new JAXB object
	 */
	public void setJAXBObject(Serializable theJAXBObject) {
		this.theJAXBObject = theJAXBObject;
	}
	


	/**
	 * 
	 * @return True if the jaxb object we tried to load was built against
	 * an old schema which caused us to try to up-convert it, false otherwise.
	 */
	public boolean wasConversionAttempted() {
		return conversionAttempted;
	}

	/**
	 * Sets the conversion flag.
	 * @param attempted True if the jaxb object we tried to load was built against
	 * an old schema which caused us to try to up-convert it, false otherwise.
	 */
	public void setConversionAttempted(boolean attempted) {
		this.conversionAttempted = attempted;
	}
	
	/**
	 * Returns the path of the original file if this object was converted. Otherwise,
	 * returns null. This is used when a course is converted, where the converted course must
	 * overwrite the original file.
	 * 
	 * @return the file path of the original file or null if the object was not converted.
	 */
	public String getFilePath(){
		return filePath;
	}
	
	/**
	 * Returns the name of the original file if this object was converted. Otherwise,
	 * returns null. This is used when a course is converted, where the converted course must
	 * overwrite the original file.
	 * 
	 * @return the file name of the original file or null if the object was not converted.
	 */
	public String getFileName(){
		return fileName;
	}
	
	/**
	 * Sets the file path of the JAXB object to the path of the original file.
	 * This is used when a course is converted, where the converted course must
	 * overwrite the original file.
	 * 
	 * @param fileModel - the fileModel containing the original file the JAXB object originated from.
	 */
	public void setFilePath(String relativePath) {
		
		filePath = relativePath;
		
		if(relativePath != null){
			
			//get the name of the file itself without the path for display purposes
			fileName = FileTreeModel.createFromRawPath(relativePath).getFileOrDirectoryName();
 			
		} else {
			fileName = null;
		}
	}
	
	/**
	 * get if the selected file is modifiable by the current user
	 * @return if file is modifiable
	 */
	public boolean getModifiable(){
		return isModifiable;
	}
	
	/**
	 * sets if the selected file is modifiable by the current user
	 * @param modifiable if file is modifiable 
	 */
	public void setModifiable(boolean modifiable){
		this.isModifiable = modifiable;
	}
	
	/**
	 * Sets the course validation results to be used by the client 
	 * @param courseValidationResults The course validation results
	 */
	public void setCourseValidationResults(CourseValidationResults courseValidationResults) {
		this.courseValidationResults = courseValidationResults;
	}
	
	/**
	 * Gets the course validation results
	 * @return The course validation results. Can be null.
	 */
	public CourseValidationResults getCourseValidationResults() {
		return courseValidationResults;
	}

	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer("[FetchJAXBObjectResult: ");
		sb.append("file name = ").append(getFileName());
		sb.append(", file path = ").append(getFilePath());
		sb.append(", conversion attempted = ").append(wasConversionAttempted());
		sb.append(", modifiable = ").append(getModifiable());
		sb.append(", jaxbObject = ").append(getJAXBObject());
		sb.append(", ").append(super.toString());
		sb.append("]");		
		
		return sb.toString();
	}
}
