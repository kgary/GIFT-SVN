/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import generated.course.LessonMaterialList;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of a ConvertLessonMaterialFiles action containing the converted LessonMaterialList
 * 
 * @author nroberts
 */
public class ConvertLessonMaterialFilesResult extends GatServiceResult {
	
	private LessonMaterialList list;
		
	/**
	 * Class constructor
	 * For serialization only.
	 */
	public ConvertLessonMaterialFilesResult() {
		
    }

	/**
	 * Gets the converted lesson material list
	 * 
	 * @return the converted lesson material list
	 */
	public LessonMaterialList getList() {
		return list;
	}

	/**
	 * Sets the converted lesson material list
	 * 
	 * @param list the converted lesson material list
	 */
	public void setList(LessonMaterialList list) {
		this.list = list;
	}
}
