/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.util.StringUtils;

/**
 * A widget used to display text that can be clicked on to open a text box to that modifies it.
 * <br/><br/>
 * This widget behaves sort of a as a complex hybrid of {@link com.google.gwt.user.client.ui.TextBox TextBox} and 
 * {@link com.google.gwt.cell.client.EditTextCell EditTextCell}.
 * Like TextBox, this widget implements {@link com.google.gwt.user.client.ui.HasValue HasValue}&lt;String&gt;, allowing other 
 * widgets to assign values to it and listen for changes made to it by the user. Like EditTextCell, this widget looks like
 * ordinary text until the user interacts with it, allowing users to see what their text will actually look like outside the 
 * editor and using less space when the editor isn't open.
 * 
 * @author nroberts
 */
public class EditableLabel extends FocusPanel implements HasValue<String>{
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EditableLabel.class.getName());
    
    /** The UiBinder that combines the ui.xml with this java class */
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface WidgetUiBinder extends UiBinder<Widget, EditableLabel> {
    }

	public interface EditableLabelStyle extends CssResource {
		
		String hoverBorder();
		
		String textBoxWithoutIEClearButton();
		
		String inline();
		
		String inlineBlock();
	}
	
	@UiField
    protected EditableLabelStyle style;
	
	@UiField
	protected FlowPanel mainContainer;
	
	@UiField
	protected FlowPanel displayTextContainer;
	
	@UiField
	protected HTML html;
	
	@UiField(provided=true)
	protected TextBox htmlEditor = new TextBox();
	
	@UiField
	protected Tooltip tooltip;
	
	/** The text to use as a placeholder when the user hasn't entered any text yet*/
	protected String placeholder = null;
	
	/** Whether or not users can edit this label */
	boolean editingEnabled = true;

	/** An optional command to be fired whenever the Enter key is pressed*/
	private Command onEnterKeyCommand;

	/**
	 * Creates a new editable HTML element
	 */
	public EditableLabel() {
	    
		setWidget(uiBinder.createAndBindUi(this));
		
		//make the display text show a "text" cursor to help let the user know it is editable
		displayTextContainer.getElement().getStyle().setCursor(Cursor.TEXT);
		
		setTabIndex(-1); //allow this widget to gain focus
		
		htmlEditor.addKeyDownHandler(new KeyDownHandler() {

			@Override
			public void onKeyDown(KeyDownEvent event) {

                boolean enterPressed = KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode();
                
                if (enterPressed){
                	
                	onEnterKeyPressed();
                	
                	htmlEditor.setFocus(false); // This will fire the onBlur handler
                }
			}
		});
	    
		//hide the HTML editor when the user clicks outside this widget
		htmlEditor.addBlurHandler(new BlurHandler() {
			
			@Override
			public void onBlur(BlurEvent event) {
			    
				//if the user clicks outside this widget, we need to hide the editor
				if(htmlEditor.isVisible()){
				    
					htmlEditor.setVisible(false);
					displayTextContainer.setVisible(true);
					
					ValueChangeEvent.fire(EditableLabel.this, htmlEditor.getValue());
				}
			}
			
	    });
	    
		//update the display text when the HTML editor has fired a change
	    htmlEditor.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
			    
				if(StringUtils.isBlank(htmlEditor.getValue())){					
					showPlaceholderText();
					
				} else {
					html.setText(htmlEditor.getValue());
				}	
			}
		});
	    
	    addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				
				if(editingEnabled){
					if(displayTextContainer.isVisible()){
						
						startEditing();
						
						event.preventDefault();
					}
				}
			}
		
	    }, MouseDownEvent.getType());
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {

		return this.addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {		
		return htmlEditor.getValue();
	}

	@Override
	public void setValue(String value) {
		setValue(value, false);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		
		String oldValue = htmlEditor.getValue();
		
		htmlEditor.setValue(value, true);

		if(fireEvents){
			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
		}
	}

	/**
	 * Sets the placeholder text to use when no text has been entered
	 * 
	 * @param placeholder the placeholder text
	 */
	public void setPlaceholder(String placeholder){
		
		this.placeholder = placeholder;
		
		htmlEditor.getElement().setAttribute("placeholder", placeholder);
		
		if(StringUtils.isBlank(htmlEditor.getValue())){			
			showPlaceholderText();
		}
	}
	
	/**
	 * Updates the display text to show the placeholder text
	 */
	private void showPlaceholderText(){
		
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		if (placeholder != null) {
            sb.appendHtmlConstant("<span style='color: gray;'>")
                .appendEscaped(placeholder)
                .appendHtmlConstant("</span>");
		} else {
            sb.appendHtmlConstant("<span style='color: gray;'></span>");
		}
		
		html.setHTML(sb.toSafeHtml());			
	}
	
	/**
	 * Gets the editor used to modify this widget's text
	 * 
	 * @return the editor
	 */
	public TextBox getTextEditor(){
		return htmlEditor;
	}
	
	/**
	 * Sets the focus on this widget and begins editing it's contents
	 */
	public void startEditing(){
		
		htmlEditor.setVisible(true);
		displayTextContainer.setVisible(false);
		
		htmlEditor.setFocus(true);
		
		if(StringUtils.isNotBlank(htmlEditor.getValue())){
			htmlEditor.setSelectionRange(0, htmlEditor.getValue().length());
			
		}
	}
	
	/**
	 * Sets the placement of the tooltip
	 * 
	 * @param placement the placement
	 */
	public void setTooltipPlacement(Placement placement){
		tooltip.setPlacement(placement);
	}
	
	/**
	 * Sets the tooltip's custom container that the hover's HTML will render in.
	 * E.g. use 'body' as an argument to force the tooltip to appear above other components.
	 * 
	 * @param container String selector of where to render the hover's HTML code
	 */
	public void setTooltipContainer(String container){
	    tooltip.setContainer(container);
	}
	
	/**
     * Set to make the hover widget display HTML.
     * 
     * @param isHtml true to render HTML; false otherwise.
     */
    public void setTooltipIsHtml(boolean isHtml){
        tooltip.setIsHtml(isHtml);
    }

    /**
     * Sets the tooltip text for the label.
     * 
     * @param text - the tooltip text that will be displayed.
     */
    public void setTooltipText(String text) {
        tooltip.setTitle(text);
    }
	
	/**
	 * Sets whether or not to allow this label's text to be edited
	 * 
	 * @param enabled whether or not to allow this label's text to be edited
	 * Note: disabling will also hide the tooltip 
	 */
	public void setEditingEnabled(boolean enabled){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setEditingEnabled(" + enabled + ")");
        }
	    
		editingEnabled = enabled;
		htmlEditor.setEnabled(enabled);
		
		if(editingEnabled){
			displayTextContainer.addStyleName(style.hoverBorder());
			displayTextContainer.getElement().getStyle().setCursor(Cursor.TEXT);

            /* These two calls are in response to ticket #3789. I don't fully
             * understand why this fixes the issue. Previously we were calling
             * setTrigger(Trigger.DEFAULT) and then calling show(). Calling hide
             * instead appears to prevent the tool tip from 'sticking' in
             * certain situations. Experimenting with the API calls it appears
             * that the HOVER trigger type is not applied until show or hide is
             * called. For whatever reason calling show made it stick. */
            tooltip.setTrigger(Trigger.HOVER);
			mainContainer.getElement().getStyle().clearProperty("pointerEvents");
			
		} else {		
			displayTextContainer.removeStyleName(style.hoverBorder());
			displayTextContainer.getElement().getStyle().clearCursor();
			tooltip.setTrigger(Trigger.MANUAL);

			//prevents an issue where the tooltip can sometimes show up even when this widget is disabled
			mainContainer.getElement().getStyle().setProperty("pointerEvents", "none");
		}

        tooltip.hide();
	}
	
	/**
	 * Sets the maximum number of characters that can be entered for this label
	 * 
	 * @param length the length
	 */
	public void setMaxLength(int length){
		htmlEditor.setMaxLength(length);
	}
	
	/**
	 * Adds a listener to this widget that will be notified whenever the Enter key is pressed
	 * <br/><br/>
	 * Note: The {@link KeyDownEvent} for the Enter key is event is processed BEFORE the {@link ValueChangeEvent}
	 * that updates this widget's value, so to get the entered value, you'll need to wait until the event loop
	 * finishes via 
	 * {@link com.google.gwt.core.client.Scheduler#scheduleDeferred(com.google.gwt.core.client.Scheduler.ScheduledCommand) 
	 * Scheduler.scheduleDeferred(Command)} and then call {@link #getValue()}. This allows you to perform logic either
	 * before or after the value changes when the user hits the Enter key.
	 * 
	 * @param command the command that will act as the listener and get executed when the Enter key is pressed
	 */
	public void setEnterKeyListener(Command command){
		this.onEnterKeyCommand = command;
	}
	
	/**
	 * Notifies any listeners that are waiting for when the Enter key is pressed
	 */
	private void onEnterKeyPressed(){
		
		if(this.onEnterKeyCommand != null){
			this.onEnterKeyCommand.execute();
		}
	}
}
