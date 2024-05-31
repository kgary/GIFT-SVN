/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview.PreviewPanel;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview.ConversationWidget.ConversationUpdateCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.properties.ConversationProperties;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.EditorHeaderLabel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;

/**
 * The UI implementation for the Conversation Tree Editor.
 * 
 * @author bzahid
 */
public class TreeEditor extends Composite implements RequiresResize{

    private static TreeEditorUiBinder uiBinder = GWT.create(TreeEditorUiBinder.class);

    interface TreeEditorUiBinder extends UiBinder<Widget, TreeEditor> {
    }
   
    @UiField
    protected FlowPanel mainPanel;
    
    @UiField
    protected Button propertiesButton;
    
    @UiField
    protected Button previewButton;
    
    @UiField
    protected CollapsibleTree tree;
    
    @UiField
    protected EditorHeaderLabel editorHeaderLabel;
    
    private ConversationProperties propertiesPanel;
    
    private PreviewPanel previewPanel;
            
    public TreeEditor() {
    	initWidget(uiBinder.createAndBindUi(this));
    	propertiesPanel = new ConversationProperties();
    	previewPanel = new PreviewPanel();
    	
    	GatClientUtility.initServerProperties(new AsyncCallback<Void>() {
            
            @Override
            public void onSuccess(Void arg0) {
                if(GatClientUtility.getServerProperties() != null){
                    previewPanel.setBackground(GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.BACKGROUND_IMAGE));
                }                
            }
            
            @Override
            public void onFailure(Throwable arg0) {
                // nothing to do                
            }
        });
    	
    	propertiesButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				propertiesPanel.center();
			}
    		
    	});
    	
    	previewButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
			    
			    String courseObjectName = GatClientUtility.getCurrentCourseObjectName();
			    
			    if(courseObjectName != null) {
			        previewPanel.setDetails(courseObjectName, propertiesPanel.getLearnersDescriptionInput().getHTML());
			        
			    } else {
			        previewPanel.setDetails(propertiesPanel.getNameInput().getValue(), propertiesPanel.getLearnersDescriptionInput().getHTML());
			    }
				
			    previewPanel.show();
			}
    		
    	});
    	
    	//update the editor label to the name of the conversation
        getNameInput().addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                String newValue = event.getValue();
                editorHeaderLabel.setText(newValue);
            }
        });
    }

	/**
	 * Updates the collapsible tree widget
	 */
	public void updateTree() {
		tree.updateTree();
	}
	
	/**
	 * Gets the JSON string representation of the conversation tree.
	 * 
	 * @return A JSON string representation of the conversation tree.
	 */
	public String getTreeJSONStr() {
		return tree.getTreeJSONStr();
	}
	
	/**
	 * Creates a collapsible tree widget from a conversation tree JSON string.
	 * 
	 * @param treeJSONStr A JSON string representation of a conversation tree.
	 */
	public void loadTree(String treeJSONStr) {
		tree.loadTree(treeJSONStr);
	}
	
	/**
	 * Creates a new tree and refreshes the view.
	 */
	public void newTree() {
		tree.newTree();
	}
	
	/**
	 * Gets the id of the first node in the conversation tree.
	 * 
	 * @return The id of the first node in the conversation tree.
	 */
	public int getStartNodeId() {
		return tree.getStartNodeId();
	}
	
	/**
	 * Gets the name input.
	 *
	 * @return the name input
	 */
	public HasValue<String> getNameInput() {
		return propertiesPanel.getNameInput();
	}
	
	/**
	 * Gets the author's description input.
	 *
	 * @return the author's description input
	 */
	public RichTextArea getAuthorsDescriptionInput() {
		return propertiesPanel.getAuthorsDescriptionInput();
	}

	/**
	 * Gets the learner's description input.
	 *
	 * @return the learner's description input
	 */
	public RichTextArea getLearnersDescriptionInput() {
		return propertiesPanel.getLearnersDescriptionInput();
	}
	
	public ConversationProperties getProperties() {
		return propertiesPanel;
	}
	
	/**
	 * Gets the preview button.
	 * 
	 * @return The button used to display the conversation tree preview.
	 */
	public HasClickHandlers getPreviewButton() {
		return previewButton;
	}	

	/**
	 * Gets the close button.
	 * 
	 * @return The close button for the preview panel.
	 */
	public HasClickHandlers getClosePreviewButton() {
		return previewPanel.getClosePreviewButton();
	}
	
	/**
	 * Sets the callback to execute when the user selects a choice node in the preview window.
	 * 
	 * @param callback The callback to execute.
	 */
	public void setPreviewSubmitTextCallback(ConversationUpdateCallback callback) {
		previewPanel.setPreviewSubmitTextCallback(callback);
	}
	
	/**
	 * Queues a conversation update to present to the user in the preview window.
	 * 
	 * @param result The update result from the server for this conversation.
	 */
	public void updateConversation(UpdateConversationResult result) {
		previewPanel.updateConversation(result);
	}
	
	/**
	 * Sets the chat id of the conversation in the preview window.
	 * 
	 * @param chatId The chat id.
	 */
	public void setPreviewChatId(int id) {
		previewPanel.setPreviewChatId(id);
	}
	
	/**
	 * Gets the chat id of the conversation in the preview window.
	 * 
	 * @return The chat id.
	 */
	public int getPreviewChatId() {
		return previewPanel.getPreviewChatId();
	}
	
	/**
	 * Closes the preview window.
	 */
	public void closePreview() {
		previewPanel.closePreview();
	}
	
	/**
	 * Sets whether or not the conversation should be shown in full screen when previewed
	 * 
	 * @param fullScreen whether or not to preview in full screen
	 */
	public void setPreviewFullScreen(boolean fullScreen) {
	    previewPanel.setFullScreen(fullScreen);
	}
	
    /**
     * Set whether to allow question answer node concept assessments to be authored
     * @param allow true if assessment authoring should be allowed.  A reason for not allowing
     * might be that the tree is presented during remediation or to deliver content.
     */
    public void setAllowConceptAssessments(boolean allow){
        tree.setAllowConceptAssessments(allow);
    }

	@Override
	public void onResize() {
		tree.onResize();
	}
}
