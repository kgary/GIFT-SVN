/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

/**
 * A bootstrap modal used to show that a process is underway by providing a loading indicator. Unlike 
 * {@link BsLoadingDialogBox}, this widget uses a Modal rather than a DialogBox to display its content; 
 * therefore, it should only be used whether other Modals are being used and should not be displayed alongside DialogBoxes
 * 
 * @author nroberts
 */
public class BsLoadingDialogWidget extends AbstractBsWidget {

	private static BsLoadingDialogWidgetUiBinder uiBinder = GWT
			.create(BsLoadingDialogWidgetUiBinder.class);

	interface BsLoadingDialogWidgetUiBinder extends
			UiBinder<Widget, BsLoadingDialogWidget> {
	}
	
	@UiField
	protected Modal dialog;
	
	@UiField
	protected Text headerText;
	
	@UiField
	protected BsLoadingIcon loadingIcon;

	/**
	 * Creates a new loading dialog widget
	 */
	public BsLoadingDialogWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	/**
	 * Creates a new loading dialog widget with the given caption
	 * @param caption The headerText value
	 */
	public BsLoadingDialogWidget(String caption) {
		this();
		
		headerText.setText(caption);
	}
	
	/**
	 * Gets this dialog's loading indicator
	 * 
	 * @return the loading indicator
	 */
	public BsLoadingIcon getLoadingIcon(){
		return loadingIcon;
	}
	
	/**
	 * Sets the caption text for this dialog
	 * 
	 * @param caption the caption text
	 */
	public void setCaption(String caption){
		headerText.setText(caption);
	}
}
