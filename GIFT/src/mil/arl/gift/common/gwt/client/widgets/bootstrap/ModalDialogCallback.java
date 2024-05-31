/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;


/**
 * The ModalDialogCallback interface is used to provide notifications when the close
 * button is selected in the modal dialog box class.
 * 
 * @author nblomberg
 *
 */
public interface ModalDialogCallback { 
	
    /**
     * Handler for when the close button is selected in the modal dialog box.
     */
    void onClose();
	
}
