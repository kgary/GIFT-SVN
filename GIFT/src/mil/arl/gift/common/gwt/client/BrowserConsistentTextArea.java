/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import org.gwtbootstrap3.client.ui.TextArea;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * An extension of GWT's TextArea class used to maintain consistent behavior for text areas across browsers. 
 * 
 * Internet Explorer version 9 and up makes text areas read only by default. Because of how event handling is managed in IE and 
 * GWT, this can lead to all the text areas on a page becoming unresponsive if a very specific sequence of events is encountered. 
 * This class resolves this issue by programmatically setting the HTML 'readonly' attribute to true whenever the TextArea gains 
 * focus from the web document, overriding Internet Explorer's default behavior and making it behave as it does in other browsers. 
 * 
 * @author nroberts
 */
public class BrowserConsistentTextArea extends TextArea{
	
	/** A focus handler used to make text areas editable when they gain focus */
	private final FocusHandler focusHandler = new FocusHandler(){

		@Override
		public void onFocus(FocusEvent event) {  				
			BrowserConsistentTextArea.this.setCursorPos(BrowserConsistentTextArea.this.getText().length());
			BrowserConsistentTextArea.this.setReadOnly(false);
			BrowserConsistentTextArea.this.selectAll();
		}
		
	};
	
	/** Handler registration used to add and remove the focus handler */
	private HandlerRegistration focusHandlerRegistration;
	
	/**
	 * Constructor
	 */
	public BrowserConsistentTextArea(){
		super();
		
		focusHandlerRegistration = this.addFocusHandler(focusHandler);
	}
	
	/**
	 * Constructor - wraps the specified element in a text area
	 * 
	 * @param element The element to be wrapped in a text area
	 */
	protected BrowserConsistentTextArea(Element element){
		super(element);
		
		focusHandlerRegistration = this.addFocusHandler(focusHandler);
	}
	
	@Override
	public void setReadOnly(boolean readOnly){
		
		if(readOnly){
			focusHandlerRegistration.removeHandler();		
			
		} else {
			focusHandlerRegistration = this.addFocusHandler(focusHandler);
		}
		
		super.setReadOnly(readOnly);
	}

}
