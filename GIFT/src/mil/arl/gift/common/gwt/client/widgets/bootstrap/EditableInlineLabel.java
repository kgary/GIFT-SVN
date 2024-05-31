/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.HTML;

/**
 * An extension of {@link EditableLabel} that is displayed as an inline element (i.e. a &lt;span&gt;) rather than a block element 
 * (i.e. a &lt;div&gt;). This can be helpful in cases where an editable label needs to be placed alongside other inline elements, 
 * such as radio buttons, without breaking to a new line.
 * 
 * @author nroberts
 */
public class EditableInlineLabel extends EditableLabel {
	
	/** A hidden HTML element used to calculate the rendered width of a text string without actually displaying it to the screen .*/
	private HTML helperHTML;

	public EditableInlineLabel(){
		super();
		
		helperHTML = new HTML();
		helperHTML.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP); //prevent the text from breaking to a new line
		helperHTML.getElement().getStyle().setVisibility(Visibility.HIDDEN); //render the hidden HTML element, but make it transparent
		helperHTML.getElement().getStyle().setPosition(Position.ABSOLUTE); //keep the element from interrupting the document flow
		helperHTML.setHeight("0px"); //make sure the element doesn't take up any vertical space
		
		mainContainer.add(helperHTML);
		
		displayTextContainer.getElement().getStyle().setProperty("minWidth", "25px");
		
		htmlEditor.getElement().getStyle().setProperty("minWidth", "25px");
		htmlEditor.getElement().getStyle().setProperty("textAlign", "inherit");
		htmlEditor.getElement().getStyle().setProperty("textAlign", "inherit");
		htmlEditor.getElement().getStyle().setProperty("marginTop", "-5px");
		htmlEditor.getElement().getStyle().setProperty("marginBottom", "-1px");
		htmlEditor.getElement().getStyle().clearProperty("height");
		
		htmlEditor.setWidth("25px");
		
		addInputHandler(htmlEditor.getElement());
		
		htmlEditor.addFocusHandler(new FocusHandler() {
			
			@Override
			public void onFocus(FocusEvent event) {
				resizeTextBoxToFitContents();
			}
		});

		//make all of this widget's components display as inline elements
		getElement().getStyle().setDisplay(Display.INLINE);
		mainContainer.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		mainContainer.getElement().getStyle().setMarginTop(4, Unit.PX);
		displayTextContainer.addStyleName(style.inline());
		htmlEditor.addStyleName(style.inline());
		htmlEditor.addStyleName(style.textBoxWithoutIEClearButton()); //remove IE's clear button from the text box since it messes with sizing
		htmlEditor.getElement().getStyle().clearWidth();
	}
	
	/**
	 * Resizes the text box used to edit the label so that it matches the width of its corresponding display text.
	 * <br/><br/>
	 * Since the actual display text is a hidden inline element, we can't actually get the size of it while the text box is showing. 
	 * To get around this, this method uses a transparent block element with no height to calculate the width without having any
	 * visual effect on the DOM.
	 * <br/><br/>
	 * At first glance, you might think that setting the width via 
	 * {@link com.google.gwt.user.client.ui.TextBox#setVisibleLength(int) TextBox.setVisibleLength(int)} would be a better option, 
	 * but resizing with that approach only works well with fixed-size fonts, since the method expects that all characters will 
	 * have the same width. The same problem applies to using 'em' and 'ch' units. The only way to ensure the width is 100% 
	 * accurate for all fonts is is to actually render the text somewhere, hence why this method uses the approach it does.
	 */
	public void resizeTextBoxToFitContents() {	
		
		if(htmlEditor.getText() != null && !htmlEditor.getText().isEmpty()){
			
			//if some text has been entered, use that to set the width
			helperHTML.setText(htmlEditor.getText());
			htmlEditor.setWidth(helperHTML.getOffsetWidth() + 10 + "px");
			
		} else {
			
			if(placeholder != null && !placeholder.isEmpty()){
				
				//otherwise, if a placeholder has been set, use it to set the width instead
				helperHTML.setText(placeholder);
				htmlEditor.setWidth(helperHTML.getOffsetWidth() + 10 + "px");
				
			} else {
				
				//otherwise, if no condition is met, apply a little width to make sure things are visible
				htmlEditor.setWidth("25px");
			}
		}
	}
	
	@Override
	public void setPlaceholder(String placeholder){
		super.setPlaceholder(placeholder);
		
		resizeTextBoxToFitContents();
	}
	
	/**
	 * Attaches a native event handler to an input element so that logic can be invoked when an 'oninput' event is 
	 * fired on that element. GWT doesn't provide an event class for the 'oninput' event, so this method is used as 
	 * a quick workaround, instead of creating a whole new event class and adding it to the event bus.
	 * 
	 * @param element the input element to handle events for
	 */
	public native void addInputHandler(Element element) /*-{
		
        var that = this;
        element.oninput = function(e) {
            that.@mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableInlineLabel::resizeTextBoxToFitContents()();
        }
    }-*/;
	
	/**
	 * Sets whether or not the inner text should be styled as an inline block. This can be helpful for when instances of this
	 * class could potentially overflow to the next line.
	 * 
	 * @param inlineBlock whether or not the inner text should be styled as an inline block
	 */
	public void setInlineBlock(boolean inlineBlock){
		
		if(inlineBlock){
			displayTextContainer.addStyleName(style.inlineBlock());
			displayTextContainer.removeStyleName(style.inline());
			
		} else {			
			displayTextContainer.addStyleName(style.inline());
			displayTextContainer.removeStyleName(style.inlineBlock());
		}
	}
}
