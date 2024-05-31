/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget use to provide users with a link that can be used to open a dialog with helpful information. By default, the link provided will 
 * be a hyperlink saying "What's this?". This link can be changed by using the {@link #setHelpLink(Widget) setHelpLink} method.
 * 
 * @author nroberts
 */
public class HelpLink extends Composite {

	private static HelpLinkUiBinder uiBinder = GWT
			.create(HelpLinkUiBinder.class);

	interface HelpLinkUiBinder extends UiBinder<Widget, HelpLink> {
	}

	@UiField
	protected FocusPanel helpLinkContainer;
	
	@UiField(provided=true)
	protected Modal helpModal;
	
	@UiField
	protected Text helpModalCaption;
	
	@UiField
	protected SimplePanel helpModalBody;
	
	@UiField
	protected Button cancelButton;
	
	/**
	 * Creates a new link that opens a help dialog.
	 */
	public HelpLink() {
		this(null);
	}
	
	/**
	 * Creates a new link that opens a help dialog. This constructor can be used to provide this help link
	 * with an alternative dialog with which to present its help text, which can be helpful when special
	 * dialog logic is needed.
	 */
	public HelpLink(Modal modal) {
		
		if(modal != null){
			helpModal = modal;
			
		} else {
			helpModal = new Modal();
		}
		
		initWidget(uiBinder.createAndBindUi(this));
		
		helpLinkContainer.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				helpModal.show();
				arg0.preventDefault();
				arg0.stopPropagation();
			}
		});
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				helpModal.hide();
				arg0.preventDefault();
				arg0.stopPropagation();
			}
		});
		
		// add DomHandler to modal window to prevent cursor icon from behaving unexpectedly when clicking modal window
		helpModal.addDomHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent arg0) {
				arg0.preventDefault();
				arg0.stopPropagation();
			}
			
		}, MouseDownEvent.getType());
	}
	
	/**
	 * Sets the widget to use as the body of the help dialog
	 * 
	 * @param widget the body widget
	 */
	@UiChild(tagname="helpBody", limit=1)
	public void setHelpBody(Widget widget) {
		helpModalBody.setWidget(widget);
	}

	/**
	 * Sets the caption to be used by the help dialog
	 * 
	 * @param text the caption to be used by the help dialog
	 */
	public void setHelpCaption(String text) {
		helpModalCaption.setText(text);
	}
	
	/**
	 * Sets the widget to use as the link to open the help dialog
	 * 
	 * @param widget the link widget
	 */
	@UiChild(tagname="linkWidget", limit=1)
	public void setHelpLink(IsWidget widget) {
		helpLinkContainer.setWidget(widget);
	}
}
