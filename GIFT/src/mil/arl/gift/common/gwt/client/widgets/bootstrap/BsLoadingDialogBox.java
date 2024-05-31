/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * An extension of {@link ModalDialogBox} that presents a loading indicator
 * 
 * @author bzahid
 */
public class BsLoadingDialogBox extends ModalDialogBox {
	
	/** The instance of the loading dialog box */
	private static BsLoadingDialogBox instance = null;
	
	/** The message HTML element that is being displayed in the dialog box. */
	private HTML messageHtml = null;
	
    /**
     * Constructor
     * 
     * @param title The title of the dialog box
     * @param message The message in the dialog box
     */
	private BsLoadingDialogBox(String title, String message) {
		super();
		
		BsLoadingIcon loadingIcon = new BsLoadingIcon();

		messageHtml = new HTML("Message", true);
        messageHtml.setHTML(message);
        messageHtml.getElement().getStyle().setProperty("fontFamily", "Arial");
        messageHtml.getElement().getStyle().setProperty("fontSize", "15px");
        messageHtml.getElement().getStyle().setProperty("marginBottom", "15px");
        header.setWidth("400px");
        
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.getElement().getStyle().setProperty("padding", "10px");
        verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        verticalPanel.setWidth("400px");
        verticalPanel.setSpacing(5);
        verticalPanel.add(messageHtml);
        verticalPanel.add(loadingIcon);
        
        setCloseable(false);
        setGlassEnabled(true);
        
        setText(title);
        setWidget(verticalPanel);
        loadingIcon.startLoading();
        loadingIcon.setType(IconType.SPINNER);
        loadingIcon.setSize(IconSize.TIMES3);
	}
	
	/** 
	 * Accessor to get the message HTML element of the loading dialog box.
	 * @return The HTML element that contains the message of the loading dialog box.  This can be null.
	 */
	public HTML getMessageHtml() {
	    return messageHtml;
	}
	
    /**
     * Displays the loading dialog box
     * 
     * @param title The title of the dialog box
     * @param message The message to display in the dialog box
     */
	public static void display(String title, String message) {
		if(instance != null) {
			instance.hide();
		}
		
		instance = new BsLoadingDialogBox(title, message);
		instance.center();
	}
	
	/**
     * Hides the loading dialog box
     */
	public static void remove() {
		if(instance != null) {
			instance.hide();
		}
	}
	
	/**
	 * Returns true if the dialog is visible.  False otherwise.
	 * 
	 * @return True if the dialog is visible, false otherwise.
	 */
	public static boolean isDialogVisible() {
	    return (instance != null && instance.isShowing());
	}

    /**
     * Updates the title in the loading dialog box.
     * 
     * @param title The title to be displayed in the loading dialog box.
     */
    public static void updateTitle(String title) {
        if (instance != null) {
            instance.setText(title);
        }
    }

	/** 
	 * Updates the message in the loading dialog box.
	 * 
	 * @param message The message to be displayed in the loading dialog box.
	 */
	public static void updateMessage(String message) {
	    if (instance != null && instance.getMessageHtml() != null) {
	        instance.getMessageHtml().setHTML(message);
	    }
	}
	

}
