/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;

/**
 * A wrapper for frames used to display editors for the GAT. This widget essentially acts as a container for a GWT {@link Frame}
 * widget that performs some sanitary actions to ensure that the frame is loaded and unloaded properly.
 * <br/><br/>
 * This class mainly exists to help deal with some browser-specific issues with frames (mainly in IE) where contents 
 * sometimes fail to load or leak memory after the frame has been detached from the DOM.
 * 
 * @author nroberts
 */
public class EditorFrame extends Composite {

	private static EditorFrameUiBinder uiBinder = GWT
			.create(EditorFrameUiBinder.class);

	interface EditorFrameUiBinder extends UiBinder<Widget, EditorFrame> {
	}
	
	@UiField
	protected Frame frame;
	
	/** The URL to be assigned to the frame */
	private String editorUrl;

	/**
	 * Creates a new editor frame with no URL
	 */
	public EditorFrame() {
		initWidget(uiBinder.createAndBindUi(this));
		frame.getElement().setAttribute("allowFullScreen", "true");
		frame.getElement().getStyle().setDisplay(Display.BLOCK);
		
		frame.addAttachHandler(new AttachEvent.Handler() {
			
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				
				if(event.isAttached()){
					
					// Apply the URL when the frame is attached to avoid an issue in IE where the frame won't load if the URL is set 
					// while the frame is detached
					frame.setUrl(editorUrl);
					
				} else {
					
					// When the frame is detached, set its internal URL to "about:blank" so that its content is unloaded.
					// This should help with some memory leak problems in IE.
					frame.setUrl("about:blank");
				}
			}
		});
	}
	
	/**
	 * Creates a new editor frame using the given URL
	 * 
	 * @param url the URL for the editor to use
	 */
	public EditorFrame(String url){
		this();
		
		setEditorURL(url);
	}

	/**
	 * Sets the URL to be used by this frame
	 * 
	 * @param url the URL to be used by this frame
	 */
	public void setEditorURL(String url){
		
		editorUrl = url;
		
		if(frame.isAttached()){
			frame.setUrl(editorUrl);
		}
	}
	
	/**
	 * Gets the URL being used by this frame
	 * 
	 * @return the URL being used by this frame
	 */
	public String getEditorURL(){
		return editorUrl;
	}
	
	/**
	 * Returns the inner frame maintained by this widget. You should generally only use this method for styling purposes, since the bulk
	 * of the frame's logic is already being handled by this widget.
	 * 
	 * @return the inner frame maintained by this widget
	 */
	public Frame getInnerFrame(){
		return frame;
	}
}
