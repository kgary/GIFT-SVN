/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action that causes a question export reference file to be created and saved to disk.
 * 
 * @author nroberts
 */
public class GenerateQuestionExportReferenceFile implements Action<GenericGatServiceResult<Void>>{
	
	private AbstractQuestion question;

	/**
	 * The name of the file the training app reference file is being generated for
	 */
	private String targetFilename;
	
	/** The user name. */
	private String userName;

	/**
	 * No-arg constructor. Needed for serialization
	 */
	public GenerateQuestionExportReferenceFile() {
	}

	public AbstractQuestion getQuestion() {
		return question;
	}

	public void setQuestion(AbstractQuestion question) {
		this.question = question;
	}

	/**
	 * Gets the name of the file the question export reference file is being generated for
	 * 
	 * @return the name of the file the question export reference file is being generated for
	 */
	public String getTargetFilename() {
		return targetFilename;
	}

	/**
	 * Sets the name of the file the question export reference file is being generated for
	 * 
	 * @param targetFilename the name of the file the question export reference file is being generated for
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
}
