/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.surveyeditor;

import java.util.List;

import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of a Qualtrics survey import from a Qualtrics survey export (.qsf) file.
 */
public class QualtricsImportResult extends GatServiceResult {
	
	private List<SurveyPage> surveyPages;

	private List<String> failedItems;

	/**
	 * Information about Qualtrics survey items that weren't imported successfully.  This
	 * is most likely due to GIFT not supported a Qualtrics survey item type or setting.
	 * 
	 * @return collection of messages about Qualtrics survey item's that weren't imported.
	 * Can be null or empty.
	 */
	public List<String> getFailedItems() {
		return failedItems;
	}
	
	public void setFailedItems(List<String> failedItems) {
		this.failedItems = failedItems;
	}

	/**
	 * The survey pages created as part of the Qualtrics survey import.  Each GIFT survey page
	 * will contain only the survey items that could be imported from the Qualtrics survey export.
	 * 
	 * @return collection of survey pages with survey items that were created as part of the 
	 * Qualtrics survey import.  If there are any survey items that failed to import, those
	 * items will be missing from the associated survey pages.
	 */
	public List<SurveyPage> getSurveyPages() {
		return surveyPages;
	}

	public void setSurveyPages(List<SurveyPage> surveyPages) {
		this.surveyPages = surveyPages;
	}        

	@Override
    public String toString(){
	    
	    StringBuffer sb = new StringBuffer();
	    sb.append("[QualtircsImportResult: ");
	    sb.append("num of pages = ").append(getSurveyPages().size());
	    sb.append(", failed items = {");
	    for(String item : failedItems){
	        sb.append("\n").append(item);
	    }
	    
	    sb.append("}");
	    sb.append("]");
	    return sb.toString();
	}
}
