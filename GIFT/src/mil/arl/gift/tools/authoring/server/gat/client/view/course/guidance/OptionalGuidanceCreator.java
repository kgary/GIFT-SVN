/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.HasSafeHtml;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import generated.course.BooleanEnum;
import generated.course.Guidance;

/**
 * An interactive widget that allows users to optionally create and edit a {@link Guidance} course object.
 * 
 * @author nroberts
 */
public class OptionalGuidanceCreator extends Composite implements HasHTML, HasSafeHtml, HasValue<Guidance>, HasEnabled{
    
    /** The Constant logger. */
    private static final Logger logger = Logger.getLogger(OptionalGuidanceCreator.class.getName());

	private static OptionalGuidanceCreatorUiBinder uiBinder = GWT.create(OptionalGuidanceCreatorUiBinder.class);

	interface OptionalGuidanceCreatorUiBinder extends UiBinder<Widget, OptionalGuidanceCreator> {
	}
	
	@UiField
	protected CheckBox checkBox;
	
	@UiField
	protected SimplePanel editorPanel;
	
	@UiField
	protected DeckPanel guidanceDeck;
	
	@UiField
	protected Widget noGuidancePanel;
	
	@UiField
	protected Widget guidancePanel;
	
	@UiField
	protected Button changeTypeButton;
	
	/** The widget's current value */
	private Guidance value;
	
	/** The widget's last value */
	private Guidance lastValue;
	
	/** 
	 * The editor used to modify the widget's value. This editor is only created and attached to this widget when it is needed,
	 * since users may not even try to author guidance when it's optional. This field should be accessed using the {@link #getEditor()}
	 * method so that the editor is properly initialized if it has not been already.
	 */
	private GuidanceEditor editor = null;
	
	private boolean shouldHideMessage = false;

	/** Whether this editor is embedded within a training application editor */
    private boolean isTrainingAppEmbedded = false;

	/**
	 * Creates a new widget for authoring optional guidance objects
	 */
	public OptionalGuidanceCreator() {
		initWidget(uiBinder.createAndBindUi(this));
		
		checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(event.getValue()){
					
					if(lastValue != null){
						setValue(lastValue, true);
						
					} else {
					    // #5026 - an empty Guidance is not complete, don't notify Presenters (e.g. MbpPresenter) until
					    // an actual Guidance choice is made (e.g. Message)
						setValue(new Guidance(), false);
					}
					
				} else {
					setValue(null, true);
				}
			}
		});
		
		changeTypeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(value != null){
					
					//reset the current guidance's choice
					value.setDisplayTime(null);
					value.setGuidanceChoice(null);
					value.setFullScreen(BooleanEnum.TRUE);
					
					//update the editor so it allows the user to pick a new guidance choice
					getEditor().edit(value);
					
					updateDisplay();
				}
			}
		});
		
		setValue(null);
	}

	@Override
	public String getText() {		
		return checkBox.getText();
	}

	@Override
	public void setText(String text) {
		checkBox.setText(text);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Guidance> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Guidance getValue() {
		return value;
	}

	@Override
	public void setValue(Guidance value) {
		setValue(value, false);
	}

	@Override
	public void setValue(Guidance value, boolean fireEvents) {
		
		if (value == this.value
				|| (this.value != null && this.value.equals(value))) {
		    logger.fine("setValue() - return: old "+this.value+", new "+value);
			return;
		}

		logger.fine("setValue() - old "+this.value+", new "+value+" fire "+fireEvents);
		lastValue = this.value;
		this.value = value;

		updateDisplay();

		if (fireEvents) {
			ValueChangeEvent.fireIfNotEqual(this, lastValue, value);
		}
	}

	/**
	 * Updates this widget's visuals to match its current backing state
	 */
	private void updateDisplay() {
		
		if(value != null){
			
			getEditor().edit(value);
			getEditor().hideDisabledOption(true);
			
			checkBox.setValue(true);
			
			guidanceDeck.showWidget(guidanceDeck.getWidgetIndex(guidancePanel));
			
			if(value.getGuidanceChoice() != null){	
				changeTypeButton.setVisible(true);	
				if(shouldHideMessage) {
					hideMessageEditor(value);
				}
				
				// #5026 - notify presenters (e.g. MbpPresenter) that a value guidance choice is now set
	            ValueChangeEvent.fire(this, value);
				
			} else {
				changeTypeButton.setVisible(false);	
			}
			
		} else {
		    
		    logger.fine("value is null - updatedisplay");
			
			checkBox.setValue(false);
			
			guidanceDeck.showWidget(guidanceDeck.getWidgetIndex(noGuidancePanel));
			
			changeTypeButton.setVisible(false);	
		}
	}

	@Override
	public String getHTML() {		
		return checkBox.getHTML();
	}

	@Override
	public void setHTML(String html) {
		checkBox.setHTML(html);
	}

	@Override
	public void setHTML(SafeHtml html) {
		checkBox.setHTML(html);
	}
	
	/**
	 * Hides the message editor if there is no message authored.
	 * 
	 * @param guidance The guidance being edited
	 */
	public void hideMessageEditor(boolean hide) {
		shouldHideMessage = hide;
	}

	/**
	 * Hides the message editor if there is no message authored.
	 * 
	 * @param guidance The guidance being edited
	 */
	private void hideMessageEditor(Guidance guidance) {
		String infoMessage = null;
		shouldHideMessage = true;
		
		// Prevent adding an informative message unless there is one already authored
		if(guidance.getGuidanceChoice() instanceof Guidance.File) {
			infoMessage = ((Guidance.File) guidance.getGuidanceChoice()).getMessage();
			
		} else if (guidance.getGuidanceChoice() instanceof Guidance.URL) {
			infoMessage = ((Guidance.URL) guidance.getGuidanceChoice()).getMessage();
		}
		
		getEditor().hideInfoMessage(infoMessage == null || infoMessage.isEmpty());
	}

	/**
	 * Resets this widget to its initial state. This also permanently discards the last guidance this widget was editing, which
	 * will prevent that guidance from being reloaded if the user re-checks the check box.
	 */
	public void reset(){
		
		lastValue = null;
		
		setValue(null);
	}

    @Override
    public boolean isEnabled() {
        return checkBox.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        checkBox.setEnabled(enabled);
        changeTypeButton.setEnabled(false);
    }
    
    /**
     * Gets the guidance editor used to modify this widget's value. If the editor has not been created yet, this method will create it.
     * This ensures that the editor is not created until it is actually needed, saving some loading time for users that don't plan
     * on authoring optional guidance messages.
     * 
     * @return the editor
     */
    private GuidanceEditor getEditor(){
    	
    	if(editor == null){
    		
    		editor = new GuidanceEditor();
    		
    		editor.setTrainingAppEmbedded(isTrainingAppEmbedded);
    		
    		editor.setChoiceSelectionListener(new Command() {
    			
    			@Override
    			public void execute() {
    			    
    				//update the UI whenever the user selects a new guidance choice
    				updateDisplay();
    			}
    		});
    		
    		editorPanel.add(editor);
    	}
    	
    	return editor;
    }
    
    /**
     * Sets whether or not this editor is embedded within a training application editor and adjusts its UI components appropriately
     * 
     * @param isTrainingAppEmbedded whether this editor is embedded within a training application editor
     */
    public void setTrainingAppEmbedded(boolean embedded) {
        this.isTrainingAppEmbedded = embedded;
        
        if(editor != null) {
            editor.setTrainingAppEmbedded(isTrainingAppEmbedded);
        }
    }
}
