/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * A modal that will append itself to the end of the DOM of the highest-level GAT editor (i.e. the Course Editor) when it is shown. A modal
 * of this type will always display above any {@link Modal Modals} or {@link com.google.gwt.user.client.ui.DialogBox DialogBoxes} that are 
 * opened before it, ensuring that it is always the topmost modal (unless, of course, another modal has a higher z-index).
 * 
 * @author nroberts
 */
public class TopLevelModal extends Modal {
	
	 private static Logger logger = Logger.getLogger(TopLevelModal.class.getName());

	@Override
	public void show() {
	    
	    super.show();
		
		//need to append this modal to the base course editor when it is shown so it takes up the full width and height of the editor
		Element body = getBaseWindowBody();
		HTMLPanel bodyPanel = HTMLPanel.wrap(body);		

		if (bodyPanel != null) {
		    bodyPanel.add(this);
		    
		} else {
		    logger.severe("Unable to find the body panel for this modal.");
		}
	}
	
	@Override
	public void hide() {
		
		super.hide();
		
		//need to remove this modal from the base course editor when it is hidden
		Element body = getBaseWindowBody();
		HTMLPanel bodyPanel = HTMLPanel.wrap(body);
		
		bodyPanel.remove(this);
	}
	
	/**
	 * Gets the &lt;body&gt; element for the the window containing the base course editor.
	 */
	private native Element getBaseWindowBody()/*-{
		
		var baseWnd = @mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility::getBaseEditorWindow()();
		
		return baseWnd.document.body;
	}-*/;
}
