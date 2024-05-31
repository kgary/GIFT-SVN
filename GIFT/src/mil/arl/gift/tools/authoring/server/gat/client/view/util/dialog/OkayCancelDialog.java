/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;

/**
 * The Class OkayCancelDialog.
 * Displays a dialog box with specified text, widget, and callback. 
 * The dialog is automatically hidden when the user clicks Ok or Cancel.
 * Title, message, and confirm button text of the dialog cannot be null.
 */
public class OkayCancelDialog extends ModalDialogBox {

	/** The Constant dialog. */
	private static final OkayCancelDialog dialog = new OkayCancelDialog();

	/**
	 * Show.
	 *
	 * @param title the title (cannot be empty or null)
	 * @param msgHtml the msg html  (cannot be empty or null)
	 * @param widget the widget (can be null)
	 * @param confirm the confirm button text (cannot be empty or null)
	 * @param callback the callback (cannot be null)
	 */
	public static void show(String title, String msgHtml, Widget widget, String confirm, OkayCancelCallback callback) {
		
		if(title == null || title.isEmpty()) {
			throw new IllegalArgumentException("Title cannot be null or empty");
		} else if(msgHtml == null || msgHtml.isEmpty()) {
			throw new IllegalArgumentException("Dialog message text cannot be null or empty");
		} else if(confirm == null || confirm.isEmpty()) {
			throw new IllegalArgumentException("Confirm button text cannot be null or empty");
		} else if(callback == null) {
			throw new IllegalArgumentException("Callback cannot be null");
		} 
		
		dialog.setText(title);
		dialog.setMessage(msgHtml);
		dialog.setMyWidget(widget);
		dialog.setCallback(callback);
		dialog.setConfirmText(confirm);
		dialog.center();
	}
	
	/**
	 * Show.
	 *
	 * @param title the title (cannot be empty or null)
	 * @param msgHtml the msg html  (cannot be empty or null)
	 * @param confirm the confirm button text (cannot be empty or null)
	 * @param callback the callback (cannot be null)
	 */
	public static void show(String title, String msgHtml, String confirm, OkayCancelCallback callback) {
		show(title, msgHtml, null, confirm, callback);
	}
	
	/**
	 * Shows the dialog with caller specifiable widget and button text.
	 * 
	 * @param title the title of the dialog (cannot be empty or null)
	 * @param msgHtml the message to display within the dialog (cannot be empty or null)
	 * @param widget the widget that the dialog will contain (can be null)
	 * @param confirm the text that the confirm button will display (cannot be empty or null)
	 * @param cancel the text that the cancel button will display (cannot be empty or null)
	 * @param callback the callback that represents the code to execute when one of the two buttons are clicked (cannot be null)
	 */
	public static void show(String title, String msgHtml, Widget widget, String confirm, String cancel, OkayCancelCallback callback) {
	    dialog.setCancelText(cancel);
	    show(title, msgHtml, widget, confirm, callback);
	}
		
	/** The callback. */
	private OkayCancelCallback callback;
	
	/** The html. */
	private HTML html = new HTML();
	
	/** The widget. */
	private Widget widget;
	
	/** The flow panel. */
	private FlowPanel flowPanel = new FlowPanel();
	
	/** The confirm button */
	private Button confirmButton;
	
	/** The cancel button */
	private Button cancelButton;
	
	/**
	 * Sets the message.
	 *
	 * @param msgHtml the new message
	 */
	private void setMessage(String msgHtml) {
		html.setHTML(msgHtml);
	}
	
	/**
	 * Sets the callback.
	 *
	 * @param callback the new callback
	 */
	private void setCallback(OkayCancelCallback callback) {
		this.callback = callback;
	}
	
	/**
	 * Gets the callback.
	 *
	 * @return the callback
	 */
	private OkayCancelCallback getCallback() {
		return callback;
	}
	
	/**
	 * Sets the my widget.
	 *
	 * @param widget the new my widget
	 */
	private void setMyWidget(Widget widget) {
		
		if(this.widget != null) {
			flowPanel.remove(this.widget);
			this.widget = null;
		}
		
		if(widget != null) {
			this.widget = widget;
			flowPanel.add(widget);
		}
	}
	
	/**
	 * Instantiates a new okay cancel dialog.
	 */
	private OkayCancelDialog() {
		
		setGlassEnabled(true);
		
		confirmButton = new Button("OK");
		confirmButton.setType(ButtonType.DANGER);  //make the action button a more intense color than cancel button to indicate
		                                           //something is going to happen
		ClickHandler okayHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getCallback().okay();
				hide();
			}
		};		
		confirmButton.addClickHandler(okayHandler);
		
		cancelButton = new Button("Cancel");
		cancelButton.setType(ButtonType.PRIMARY);
		ClickHandler cancelHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getCallback().cancel();
				hide();
			}
		};
		cancelButton.addClickHandler(cancelHandler);
		
		VerticalPanel vPanel = new VerticalPanel();
		
		vPanel.getElement().getStyle().setProperty("maxWidth", "700px");
		vPanel.getElement().getStyle().setProperty("padding", "10px 10px 0px 10px");
		vPanel.setSpacing(10);
		
		vPanel.add(html);
		vPanel.add(flowPanel);
		
		FlowPanel footerPanel = new FlowPanel();
		footerPanel.add(confirmButton);
		footerPanel.add(cancelButton);
		
		footerPanel.getElement().getStyle().setProperty("display", "inline");
		
		setFooterWidget(footerPanel);
		setEnterButton(confirmButton);		
		
		setWidget(vPanel);
	}	
	
	/**
	 * Sets the text for the confirm option button
	 * 
	 * @param confirm Text for the confirm option button.
	 */
	public void setConfirmText(String confirm) {
		confirmButton.setText(confirm);
	}
	
	/**
	 * Programmatically cancels the dialog, as if the user clicked the cancel button. This can be useful if the dialog contains 
	 * interactive elements that trigger actions that should automatically close the dialog.
	 */
	public static void cancel() {
	    dialog.cancelButton.click();
	}
	
	/**
	 * Sets the text of the dialog's cancel button
	 * @param cancel the value to which to update the cancel button's text
	 */
	public void setCancelText(String cancel) {
	    cancelButton.setText(cancel);
	}
}
