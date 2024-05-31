/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view;

import java.util.logging.Logger;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.FormFieldFocusEvent;

import com.google.gwt.event.dom.client.KeyUpHandler;

/**
 * An abstract class representing a Composite element that can display help messages back to the user.
 */
public abstract class AbstractHelpEnabledComposite extends Composite implements 
    FocusHandler, BlurHandler, MouseOverHandler, MouseOutHandler, ValueChangeHandler<String>, ClickHandler, ChangeHandler {

	/** The logger. */
	private static Logger logger = Logger.getLogger(AbstractHelpEnabledComposite.class.getName());
	
	/**
	 * Fire focus event.
	 *
	 * @param fieldEnum the field enum
	 */
	protected void fireFocusEvent(HelpMap.FormFieldEnum fieldEnum) {		
		SharedResources.getInstance().getEventBus().fireEvent(new FormFieldFocusEvent(fieldEnum));
	}
	
	/**
	 * Show help for widget.
	 *
	 * @param widget the widget
	 */
	protected abstract void showHelpForWidget(Object widget);
	
	/**
	 * Configure handlers.
	 *
	 * @param w the w
	 */
	protected void configureHandlers(FocusWidget w) {
		
		w.addFocusHandler(this);
		w.addBlurHandler(this);
		w.addMouseOverHandler(this);
		w.addMouseOutHandler(this);		
		
		if(w instanceof TextBox ){
			((TextBox)w).addValueChangeHandler(this);
		}
		else if (w instanceof TextArea) {			
			((TextArea)w).addValueChangeHandler(this);
		}
		else if (w instanceof ListBox) {			
			((ListBox)w).addChangeHandler(this);
		}
		else if (w instanceof RadioButton) {			
			((RadioButton)w).addClickHandler(this);
		}
		else if (w instanceof CheckBox) {			
			((CheckBox)w).addClickHandler(this);
			
		} else if (w instanceof RichTextArea) {
		    ((RichTextArea)w).addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent arg0) {
                    sendDirtyEvent();
                }
		    });
		}
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
	 */
	@Override
	public void onMouseOut(MouseOutEvent event) {
		fireFocusEvent(HelpMap.FormFieldEnum.BLUR);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
	 */
	@Override
	public void onMouseOver(MouseOverEvent event) {
		showHelpForWidget(event.getSource());
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.FocusHandler#onFocus(com.google.gwt.event.dom.client.FocusEvent)
	 */
	@Override
	public void onFocus(FocusEvent event) {
		showHelpForWidget(event.getSource());
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event.dom.client.BlurEvent)
	 */
	@Override
	public void onBlur(BlurEvent event) {
		fireFocusEvent(HelpMap.FormFieldEnum.BLUR);
	}
	
	 /**
 	 * Send dirty event.
 	 */
 	protected void sendDirtyEvent() {
		 logger.fine("sending EditorDirtyEvent");
		 SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
	 }
	
	/* (non-Javadoc)
	 * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
	 */
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		sendDirtyEvent();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
	 */
	@Override
	public void onClick(ClickEvent event) {
		sendDirtyEvent();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event.dom.client.ChangeEvent)
	 */
	@Override
	public void onChange(ChangeEvent event) {
		sendDirtyEvent();
	}	
}
