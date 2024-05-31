/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.ModalHeader;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.DynamicHeaderScrollPanel;

/**
 * An extension of GWT's PopupPanel designed to behave like a GWT DialogBox but look like a Boostrap Modal. This widget exists to help deal with 
 * conflicting behavior that exists between DialogBoxes and Modals by providing a way to keep DialogBox's functionality while using bootstrap elements
 * to keep styling consistent. This class also provides a way to show a modal-like interface without going through all
 */
public class ModalDialogBox extends PopupPanel {
    
    private static final int HEADER_TOP_BOTTOM_PADDING = 3;
    private static final int FOOTER_TOP_BOTTOM_PADDING = 10;
	
	private HasClickHandlers okButton;
	
	private Button closeButton;
	
	protected HTML caption = new HTML();
	
	protected ModalHeader header = new ModalHeader();
	
	protected ModalBody body = new ModalBody();
	
	protected ModalFooter footer = new ModalFooter();
	
	/** Optional callback to send notifications when the buttons are selected. **/
	private ModalDialogCallback modalCallback = null;

	public ModalDialogBox() {
		super();
		
	      
        useSmallHeaderFooterPadding();
        
		header.setClosable(false);
		
		Heading heading = new Heading(HeadingSize.H3);
		heading.add(caption);
		
		header.add(heading);
		
		Div content = new Div();
		content.add(header);
		content.add(body);
		content.add(footer);
		
		super.setWidget(content);
		
		//inherit bootstrap style for modal content
		addStyleName(Styles.MODAL_CONTENT);
		
		addDomHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				int key = event.getNativeKeyCode();
				
				if(key == KeyCodes.KEY_ENTER) {
					HasClickHandlers button = null;
					
					if(okButton != null) { 
						button = okButton;
					} else if(footer.getWidgetCount() == 1 && closeButton != null && footer.getWidgetIndex(closeButton) != -1) {
						button = closeButton;
					}
					
					if(button != null) {
						
						//fire click event. If the button is disabled, the click event should be natively prevented
						DomEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false), button);
					}
				}
			}
			
		}, KeyUpEvent.getType());
	}
	
	/**
	 * Creates a modal dialog box where the footer is frozen at the bottom of the dialog. 
	 * Frozen meaning that the header and footer scroll independently of the body
	 * @param isHeaderAndFooterFrozen Whether or not to freeze the header and footer. 
	 * If false the no argument constructor is called
	 */
	public ModalDialogBox(boolean isHeaderAndFooterFrozen) {
	    this();
	    
	    if(!isHeaderAndFooterFrozen) {
	        return;
	    }
	    
        //Adjust the padding to increase scrollable space for the body
	    useSmallHeaderFooterPadding();
	    
        DynamicHeaderScrollPanel contentPanel = new DynamicHeaderScrollPanel(header, body, footer);
	    super.setWidget(contentPanel);
	}
	
	private void useSmallHeaderFooterPadding(){	    
        header.getElement().getStyle().setPaddingTop(HEADER_TOP_BOTTOM_PADDING, Unit.PX);
        header.getElement().getStyle().setPaddingBottom(HEADER_TOP_BOTTOM_PADDING, Unit.PX);
        footer.getElement().getStyle().setPaddingTop(FOOTER_TOP_BOTTOM_PADDING, Unit.PX);
        footer.getElement().getStyle().setPaddingBottom(FOOTER_TOP_BOTTOM_PADDING, Unit.PX);
	}
	
	
	/**
	 * Sets the optional callback for the modal dialog box.  The callback will send the notification
	 * when the close button is selected in the dialog.
	 * 
	 * @param callback - The callback that the modal dialog class will use.
	 */
	public void setCallback(ModalDialogCallback callback) {
	    modalCallback = callback;
	}

	public void setHtml(SafeHtml html){
		caption.setHTML(html);
	}
	
	public void setHtml(String html){
		caption.setHTML(html);
	}
	
	public void setText(String text){
		caption.setText(text);
	}
	
	public String getHtml(){
		return caption.getHTML();
	}
	
	public String getText(){
		return caption.getText();
	}
	
	@Override
	public void setWidget(IsWidget widget){
		body.clear();
		body.add(widget);
	}
	
	@Override
	public void setWidget(Widget widget){
		body.clear();
		body.add(widget);
	}
	
	@Override
	public void add(IsWidget widget){
		body.add(widget);
	}
	
	@Override
	public void add(Widget widget){
		body.add(widget);
	}
	
	public void setFooterWidget(IsWidget widget){
		footer.insert(widget, 0);
	}
	
	public void removeFooterWidget(IsWidget widget){
		footer.remove(widget);
	}
	
	/** 
	 * Set a button that will be clicked when the enter button is pressed. 
	 * The event will not fire if this button is disabled.
	 * 
	 * @param button the button to click when the user presses the enter button. Can be null. 
	 */
	/*
	 * Nick: Using org.gwtbootstrap3.client.ui.Button for the argument here can cause a rare build error while building any modules that reference this 
	 * method. The reason for this seems to be that a timing issue during the build causes the Button class to not be GWT-compiled by the time this 
	 * method is reached. To avoid this problem, I'm using GWT's HasClickHandlers instead, which is a bit more abstract and will also work for 
	 * GWT buttons.
	 */
	public void setEnterButton(HasClickHandlers button) {
		okButton = button;
	}
	
	/**
	 * Prevents the previously set button from being clicked when enter is pressed
	 * 
	 */
	public void removeEnterButton() {
		okButton = null;
	}

    @Override
    public void hide() {
        if (modalCallback != null) {
            modalCallback.onClose();
        }

        super.hide();
    }

	public void setCloseable(boolean closeable){
		
		if(closeable){
			
			if(closeButton == null){
				
				closeButton = new Button("Close");
				closeButton.setType(ButtonType.DANGER);
				
				closeButton.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						hide();
					}
				});
				
				footer.add(closeButton);
			}
			
		} else {
			
			if(closeButton != null && footer.getWidgetIndex(closeButton) != -1){
				footer.remove(closeButton);
			}
		}
	}
	
	public Button getCloseButton(){
		return closeButton;
	}
}
