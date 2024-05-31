/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;
import org.gwtbootstrap3.extras.summernote.client.ui.base.Toolbar;
import org.gwtbootstrap3.extras.summernote.client.ui.base.ToolbarButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

/**
 * The Class GuidanceUrlEditor.
 *
 * @author nroberts
 */
public class GuidanceUrlEditor extends Composite {

	/** The ui binder. */
	private static GuidanceUrlEditorUiBinder uiBinder = GWT
			.create(GuidanceUrlEditorUiBinder.class);

	/**
	 * The Interface GuidanceUrlEditorUiBinder.
	 */
	interface GuidanceUrlEditorUiBinder extends
			UiBinder<Widget, GuidanceUrlEditor> {
	}
	
	/** The url address. */
	@UiField
	protected TextBox urlAddress;
	
	@UiField
	protected Button urlPreviewButton;
	
    @UiField
    protected FlowPanel editorPanel;
	
	@UiField
	protected FlowPanel messagePanel;
	
	@UiField
	protected Icon messageIcon;
	
	@UiField
	protected FocusPanel messageButton;
	
	private String message = null;
	
	@UiField(provided=true)
	protected Summernote richTextEditor = new Summernote(){
    	
    	@Override
    	protected void onLoad() {
    		super.onLoad();
    		
    		//need to reconfigure the editor or else the blur event doesn't fire properly
    		richTextEditor.reconfigure();
    		
    		//need to reassign the message HTML to the editor since it gets lost when the editor is detached.
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				
				@Override
				public void execute() {
		    		if(message != null) {
		    			richTextEditor.setCode(message);
					}
				}
			});
    	}
    	
    	@Override
    	public void setCode(String code) {
    		super.setCode(code);
    		message = code;
    	}
	};
	
	/**
	 * Instantiates a new guidance url editor.
	 */
	public GuidanceUrlEditor() {
		initWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
	private void init(){
	    
        urlAddress.getElement().setAttribute("placeholder", "Enter the URL for the resource you wish to use (e.g. http://www.example.com/).");
	    
        if(GatClientUtility.isReadOnly()) {
            
            urlAddress.setEnabled(false);
            richTextEditor.setEnabled(false);
            
        }else{
            
            urlAddress.addKeyDownHandler(new KeyDownHandler() {

                @Override
                public void onKeyDown(KeyDownEvent event) {
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                        urlPreviewButton.click();
                    }
                }
            });
        }
        
        Toolbar defaultToolbar = new Toolbar()
		.addGroup(ToolbarButton.STYLE)
		.addGroup(ToolbarButton.BOLD, ToolbarButton.UNDERLINE, ToolbarButton.ITALIC, ToolbarButton.FONT_SIZE)
		.addGroup(ToolbarButton.LINK, ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.PARAGRAPH)
		.addGroup(ToolbarButton.CODE_VIEW, ToolbarButton.FULL_SCREEN);

		richTextEditor.setToolbar(defaultToolbar);
		richTextEditor.reconfigure();
		
        messageButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				editorPanel.setVisible(!editorPanel.isVisible());
				if(editorPanel.isVisible()) {
					messageIcon.setType(IconType.MINUS_SQUARE);
				} else {
					messageIcon.setType(IconType.PLUS_SQUARE);
				}
			}
		});
	}
	
	/**
	 * Gets the url address input.
	 *
	 * @return the url address input
	 */
	public HasValue<String> getUrlAddressInput(){
		return urlAddress;
	}
	
	/**
	 * Gets the url message input.
	 *
	 * @return the url message input
	 */
	public Summernote getMessageInput(){
		return richTextEditor;
	}
	
	/**
	 * Show or hide the informative message editor.
	 * 
	 * @param hide True to hide the informative message editor.
	 */
	public void hideMessageInput(boolean hide) {
		messagePanel.setVisible(!hide);
	}
	
	/**
	 * Gets the url preview button.
	 * 
	 * @return the url preview button.
	 */
	public HasClickHandlers getUrlPreviewButton() {
		return urlPreviewButton;
	}

}
