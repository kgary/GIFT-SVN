/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.model.record;

import java.io.Serializable;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class AssessmentRecord.
 */
public class AssessmentRecord{
	
	/** The Question or Reply represented by this record. */
	private Serializable questionOrReply;
	
	/** 
	 * Text associated with this assessment record that should be shown in a HasData display. Questions and replies in a DKF do not have any representative text, 
	 * so this variable is used to make them more readable and distinguishable in a HasData display by allowing such representative text to be added.
	 */
	private String displayText = "";
	
	private SafeHtml statusHtml = SafeHtmlUtils.fromSafeConstant("<div class='validLabel'>Valid</div>");
	
	/**
	 * Instantiates a new assessment record.
	 *
	 * @param questionOrReply the question or reply
	 * @param displayText the display text
	 */
	public AssessmentRecord(Serializable questionOrReply, String displayText){
		this.questionOrReply = questionOrReply;
		this.setDisplayText(displayText);
	}
	
	/**
	 * Instantiates a new assessment record.
	 *
	 * @param questionOrReply the question or reply
	 * @param displayText the display text
	 * @param statusHTML HTML for the record's status
	 */
	public AssessmentRecord(Serializable questionOrReply, String displayText, SafeHtml statusHTML){
		this.questionOrReply = questionOrReply;
		this.setDisplayText(displayText);
		this.statusHtml = statusHTML;
	}

	/**
	 * Gets the question or reply.
	 *
	 * @return the question or reply
	 */
	public Serializable getQuestionOrReply() {
		return questionOrReply;
	}

	/**
	 * Sets the question or reply.
	 *
	 * @param question the new question or reply
	 */
	public void setQuestionOrReply(Serializable question) {
		this.questionOrReply = question;
	}

	/**
	 * Gets the display text.
	 *
	 * @return the display text
	 */
	public String getDisplayText() {
		return displayText;
	}

	/**
	 * Sets the display text.
	 *
	 * @param displayText the new display text
	 */
	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}

	public SafeHtml getStatusHtml() {
		return statusHtml;
	}

	public void setStatusHtml(SafeHtml statusHtml) {
		this.statusHtml = statusHtml;
	}
}

