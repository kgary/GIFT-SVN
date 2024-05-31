/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.io.DetailedException;

/**
 * The result of importing an xtsp into a scenario file
 * 
 * @author jlouis
 */
public class XTSPImporterResult implements IsSerializable {
	
	/* List of course concept names from the DKF */
	List<String> courseConceptNameList;
	
	/* List of errors and warnings logged in XTSPImporter */
	List<DetailedException> errorMessagesList;
	
    /**
     * Default constructor required for GWT serialization
     */
    protected XTSPImporterResult() {}
    
    /**
     * Creates a new result for importing an xTSP into a scenario file
     * 
     * @param courseConceptNameList the list of course concept names from the DKF. Cannot be null.
     * @param errorMessagesList the list of errors and warnings logged in XTSPImporter. Can be null.
     */
    public XTSPImporterResult(List<String> courseConceptNameList, List<DetailedException> errorMessagesList) {
    	
    	if(courseConceptNameList != null) {
    		this.courseConceptNameList = courseConceptNameList;
    	}

    	this.errorMessagesList = errorMessagesList; 	
    }
    
    /**
     * Get the course concept name list.
     * 
     * @return the course concept name list. Cannot be null.
     */
	public List<String> getCourseConceptNameList() {
		return courseConceptNameList;
	}

	/**
     * Get the error messages list.
     * 
     * @return the error messages list. Can be null.
     */
	public List<DetailedException> getErrorMessagesList() {
		return errorMessagesList;
	}

}
