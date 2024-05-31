/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * An extension of {@link CourseObjectModal} that provides special styling and logic for GIFT Wrap
 * 
 * @author nroberts
 */
public class GiftWrapModal extends CourseObjectModal {

	/**
	 * Creates a new modal for GIFT Wrap
	 */
	public GiftWrapModal(){
		super();
		
		courseObjectModal.addStyleName("giftWrapModal");
		
		modalHeader.getElement().getStyle().setProperty("padding", "10px");
		innerModalHeader.getElement().getStyle().setProperty("padding", "0px");
		
		modalTitle.setVisible(false);
		
		modalBody.getElement().getStyle().setProperty("padding", "0px");
		
		modalBody.addStyleName("giftWrapModalCenter");
				
		setSaveAndCloseButtonVisible(false);
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
	}
}
