/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.conversation.properties;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.client.view.util.RichTextToolbar;

/**
 * A popup panel that displays the conversation properties
 */
public class ConversationProperties extends PopupPanel {
    
	/**
	 * The Interface ConversationPropertiesUiBinder.
	 */
	interface ConversationPropertiesUiBinder extends
	UiBinder<Widget, ConversationProperties> {
	}	

	/** The ui binder. */
	private static ConversationPropertiesUiBinder uiBinder = GWT
			.create(ConversationPropertiesUiBinder.class);	
	
	/** The name text box. */
	@UiField 
	protected TextBox nameTextBox;
	
	@UiField
	protected Button closeButton;
	
	/** The author's description text area. */
	@UiField (provided=true) 
	protected RichTextArea authorsDescriptionTextArea = new RichTextArea();
	
	/** The learner's description text area. */
	@UiField (provided=true)
	protected RichTextArea learnersDescriptionTextArea = new RichTextArea();
	
	/** The rich text area toolbar. */
	@UiField (provided=true)
	protected Widget authorsDescriptionToolbar = new RichTextToolbar(authorsDescriptionTextArea);
	
	/** The rich text area toolbar. */
	@UiField (provided=true) 
	protected Widget learnersDescriptionToolbar = new RichTextToolbar(learnersDescriptionTextArea);
	
	
	/**
	 * Creates a popup panel containing the conversation properties.
	 */
	public ConversationProperties() {
		setWidget(uiBinder.createAndBindUi(this)); 
				
		closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				ConversationProperties.this.hide();
			}
			
		});
		
		setGlassEnabled(true);
		getElement().getStyle().setProperty("borderRadius", "8px");
		getElement().getStyle().setProperty("border", "none");
		getElement().getStyle().setProperty("boxShadow", "rgba(0, 0, 0, 0.43) 4px 4px 13px");
	}
	
	/**
	 * Gets the name input.
	 *
	 * @return the name input
	 */
	public HasValue<String> getNameInput() {
		return nameTextBox;
	}
	
	/**
	 * Gets the author's description input.
	 *
	 * @return the author's description input
	 */
	public RichTextArea getAuthorsDescriptionInput() {
		return authorsDescriptionTextArea;
	}

	/**
	 * Gets the learner's description input.
	 *
	 * @return the learner's description input
	 */
	public RichTextArea getLearnersDescriptionInput() {
		return learnersDescriptionTextArea;
	}
}
