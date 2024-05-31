/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.conversation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview.ConversationWidget.ConversationUpdateCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree.TreeEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;

/**
 * The view for the conversation editor.
 */
public class ConversationViewImpl extends Composite implements ConversationView {
		
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The ui binder. */
	private static EditConversationViewImplUiBinder uiBinder = GWT
            .create(EditConversationViewImplUiBinder.class);

    /**
     * The Interface EditConversationViewImplUiBinder.
     */
    interface EditConversationViewImplUiBinder extends UiBinder<Widget, ConversationViewImpl> {
    } 	
    
    @UiField(provided=true)
    protected DockLayoutPanel mainContainer = new DockLayoutPanel(Unit.PX){
    	
    	@Override
    	public void onResize(){
    		super.onResize();
    		
    		treeEditor.onResize();
    	}
    };
		
	@UiField protected TreeEditor treeEditor;
	
    /**
     * Instantiates a new conversation view impl.
     */
    @Inject
    public ConversationViewImpl() {		
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    /**
     * Inits the editor
     */
    @Inject
    private void init() {    
    	
    }
    
	@Override
	public void showConfirmDialog(String msgHtml, String confirmMsg,
			final OkayCancelCallback callback) {	
		OkayCancelDialog.show("Confirm", msgHtml, confirmMsg, callback);
	}

	@Override 
	public String getTreeJSONStr() {
		return treeEditor.getTreeJSONStr();
	}
	
	@Override
	public void loadTree(String treeJSONStr) {
		treeEditor.loadTree(treeJSONStr);
	}
	
	@Override
	public void newTree() {
		treeEditor.newTree();
	}
	
	@Override
	public int getStartNodeId() {
		return treeEditor.getStartNodeId();
	}
	
	@Override
	public void setReadOnly(boolean readOnly){
		DefaultGatFileSelectionDialog.setReadOnly(readOnly);
	}
	
	@Override
	public HasValue<String> getNameInput() {
		return treeEditor.getNameInput();
	}
	
	@Override
	public HasValueChangeHandlers<String> getNameInputValueChange(){
	    return treeEditor.getNameInput();
	}
	
	@Override
	public RichTextArea getAuthorsDescriptionInput() {
		return treeEditor.getAuthorsDescriptionInput();
	}

	@Override
	public RichTextArea getLearnersDescriptionInput() {
		return treeEditor.getLearnersDescriptionInput();
	}
	
	@Override
	public void setPreviewSubmitTextCallback(ConversationUpdateCallback callback) {
		treeEditor.setPreviewSubmitTextCallback(callback);
	}
	
	@Override 
	public HasClickHandlers getPreviewButton() {
		return treeEditor.getPreviewButton();
	}
	
	@Override 
	public HasClickHandlers getClosePreviewButton() {
		return treeEditor.getClosePreviewButton();
	}
	
	@Override
	public void setPreviewChatId(int id) {
		treeEditor.setPreviewChatId(id);
	}
	
	@Override
	public int getPreviewChatId() {
		return treeEditor.getPreviewChatId();
	}
	
	@Override
	public void updateConversation(UpdateConversationResult result) {
		treeEditor.updateConversation(result);
	}
		
	@Override
	public void closePreview() {
		treeEditor.closePreview();
	}
	
	@Override
	public void setPreviewFullScreen(boolean fullScreen) {
	    treeEditor.setPreviewFullScreen(fullScreen);
	}
	
	@Override
    public void setAllowConceptAssessments(boolean allow){
        treeEditor.setAllowConceptAssessments(allow);
    }
		
}
