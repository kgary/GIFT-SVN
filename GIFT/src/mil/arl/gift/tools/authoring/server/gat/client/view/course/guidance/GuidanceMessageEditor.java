/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance;

import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurEvent;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurHandler;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;
import org.gwtbootstrap3.extras.summernote.client.ui.base.Toolbar;
import org.gwtbootstrap3.extras.summernote.client.ui.base.ToolbarButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class GuidanceMessageEditor.
 *
 * @author nroberts
 */
public class GuidanceMessageEditor extends Composite {

	/** The ui binder. */
	private static GuidanceMessageEditorUiBinder uiBinder = GWT
			.create(GuidanceMessageEditorUiBinder.class);

	/**
	 * The Interface GuidanceMessageEditorUiBinder.
	 */
	interface GuidanceMessageEditorUiBinder extends
			UiBinder<Widget, GuidanceMessageEditor> {
	}
	
    /** The rich text area. */
	@UiField(provided=true)
    protected Summernote richTextArea = new Summernote(){
    	
    	@Override
    	protected void onLoad() {
    		super.onLoad();
    		
    		//need to reconfigure the editor or else the blur event doesn't fire properly
    		richTextArea.reconfigure();
    		
    		if(msgHtmlValue != null){
    			
    			//need to reassign the message HTML to the editor since it gets lost when the editor is detached.
    			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					
					@Override
					public void execute() {
						richTextArea.setCode(msgHtmlValue);
					}
				});		
			}
    	}
    };
    
    /** 
     * The value of the editor used to handle the message HTML. Summernote technically handles this, but it behaves strangely when
     * the editor is attached and detached from the page, so we use this variable to help reassign the correct value whenever the 
     * editor is reattached.
     */
    private String msgHtmlValue = null;

	/**
	 * Instantiates a new guidance message editor.
	 */
	public GuidanceMessageEditor() {
		initWidget(uiBinder.createAndBindUi(this));
		
		Toolbar defaultToolbar = new Toolbar()
	       .addGroup(ToolbarButton.STYLE)
	       .addGroup(ToolbarButton.BOLD, ToolbarButton.UNDERLINE, ToolbarButton.ITALIC)
	       .addGroup(ToolbarButton.FONT_NAME)
	       .addGroup(ToolbarButton.FONT_SIZE, ToolbarButton.COLOR)
	       .addGroup(ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.PARAGRAPH)
	       .addGroup(ToolbarButton.TABLE)
	       .addGroup(ToolbarButton.UNDO, ToolbarButton.REDO)
	       .addGroup(ToolbarButton.CODE_VIEW, ToolbarButton.FULL_SCREEN);
		
		richTextArea.setToolbar(defaultToolbar);
		richTextArea.setDialogsInBody(true);
		richTextArea.reconfigure();
		
		richTextArea.addSummernoteBlurHandler(new SummernoteBlurHandler() {
			
			@Override
			public void onSummernoteBlur(SummernoteBlurEvent event) {
				
				//need to store the value the user entered so that we can reload it if this widgets is reattached
				msgHtmlValue = richTextArea.getCode();
			}
		});
		
		if(!GatClientUtility.isReadOnly()) {
			addAttachHandler(new AttachEvent.Handler() {
	
				@Override
				public void onAttachOrDetach(AttachEvent arg0) {
					initCodeViewBlur(richTextArea.getElement().getParentElement());
				}
			});
		}
		
	}
	
	/**
	 * Toggling the Summernote editor code-view prevents the blur event from being fired. 
	 * This method attaches a blur handler to the code-view and bubbles it up.
	 * 
	 * @param element The Summernote parent element
	 */
	private native void initCodeViewBlur(Element element) /*-{
		var that = this;
		var summernote = element.getElementsByClassName("note-editor")[0];
		var editor = summernote.getElementsByClassName("note-editing-area")[0];
		var codeView = editor.getElementsByClassName("note-codable")[0];
		codeView.onblur = function() {
			that.@mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance.GuidanceMessageEditor::fireBlurEvent()();
		};
	}-*/;
	
	/**
	 * Fires the blur event on the Summernote editor
	 */
	private void fireBlurEvent() {
		SummernoteBlurEvent.fire(richTextArea);
	}
	
	/**
	 * Sets the message html
	 * 
	 * @param html The html message to set
	 */
	public void setMsgHTML(final String html) {
		
		richTextArea.setCode(html);				
		
		msgHtmlValue = html;
	}
	
	/**
	 * Gets the message html
	 * 
	 * @return The html message
	 */
	public String getMsgHTML() {
		return richTextArea.getCode();
	}
	
	/**
	 * Adds a blur handler for the message content input.
	 * 
	 * @param handler the handler to add.
	 */
	public void addMessageContentInputBlurHandler(SummernoteBlurHandler handler){
		richTextArea.addSummernoteBlurHandler(handler);
	}
	
	/**
	 * Sets the editability of the text field
	 * 
	 * @param enabled whether the text field is enabled
	 */
	public void setEnabled(boolean enabled){
	    richTextArea.setEnabled(enabled);
	}
	
}
