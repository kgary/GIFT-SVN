/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.presenter.conversation;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.conversation.Conversation;
import generated.conversation.Conversation.Messages;
import generated.conversation.Message;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.file.CancelCallback;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.conversation.ConversationLoadedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.place.ConversationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.DkfPlace;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.AbstractGatPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.ConversationView;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview.ConversationWidget.ConversationUpdateCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview.PreviewPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSaveAsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.FileValidationDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.EndConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.FetchConversationTreeJSON;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.FetchConversationTreeJSONResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.LockConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.SaveConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UnlockConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;

/**
 * A presenter used to load conversations and populate conversation data in the Conversation Editor.
 */
public class ConversationPresenter extends AbstractGatPresenter implements ConversationView.Presenter {
	
	/**
	 * The Interface MyEventBinder.
	 */
	interface MyEventBinder extends EventBinder<ConversationPresenter> {
	}

	/** The logger. */
	private static Logger logger = Logger.getLogger(ConversationPresenter.class.getName());
	
	/** The Constant eventBinder. */
	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	/** The conversation path. */
	private String filePath;

	/** The current conversation. */
	private Conversation currentConversation = null;
	
	/** Whether or not the conversation file has been modified. */
	private boolean isConversationDirty = false;
	
	/** The place controller. */
	@Inject
	private PlaceController placeController;	
	
	/** The conversation view. */
	@Inject
	private ConversationView conversationView;
	
	/** File New dialog */
	private DefaultGatFileSaveAsDialog fileNewDialog;
	
	/** File Save-As dialog. */
	private DefaultGatFileSaveAsDialog fileSaveAsDialog;
	
	/** Heartbeat timer so the server doesn't unlock the file we're editing. */
	private Timer lockTimer = new Timer() {
		@Override
		public void run() {
			if(filePath != null) {
				lockFile(filePath, false);
			}
		}
	};
	
	/** Whether or not this activity should allow editing */
    private boolean readOnly = false;
    
    /** 
     * Special condition if a file is locked and another user tries to open the file
     * should only happen in desktop mode when users can see each other's workspaces
     */
    private boolean fileAlreadyLocked = false;
    
    /** whether or not a new file should be created based on the file path */
    private boolean createNewFile;
    
    /** The name of the course object used to open the current conversation for editing*/
    private String courseObjectName;

	/**
	 * Instantiates a new conversation presenter.
	 */
	@Inject
	public ConversationPresenter() {
		super();		
		exposeNativeFunctions();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.ConversationView.Presenter#confirmStop()
	 */
	@Override
	public String confirmStop() {		
//		if( isConversationDirty ) {
//		    return createUnsavedMessage();
//		} else {
//			return null;
//		}
		//Nick: Set to null for now to prevent the "Are you sure" dialog from showing
		return null;
	}
	
	/**
	 * Create a message to show to the user indicating the current document has unsaved changes.
	 * 
	 * @return the message to display
	 */
	private String createUnsavedMessage(){
	    return "There are unsaved changes to '"+filePath+"' that will be discarded.";
	}
	
	/**
	 * Creates the valid conversation object.
	 *
	 * @return the conversation
	 */
	private Conversation createValidConversationObject(String conversationName) {

		Conversation conversation = new Conversation();
		conversation.setName(conversationName);
		conversation.setAuthorsDescription("A conversation to tutor learners about a concept.");
		conversation.setLearnersDescription("This conversation will help you learn about a concept.");
		
		Message msg = new Message();
		msg.setText("Welcome to the conversation!");
		
		Messages msgs = new Messages();
		msgs.getMessage().add(msg);
		conversation.setMessages(msgs);
		
		conversationView.newTree();
		conversation.setStartNodeId(BigInteger.valueOf(conversationView.getStartNodeId()));

		return conversation;
	}
	
	
	/**
	 * Gets the Conversation JAXB object corresponding to conversation file given by the specified path.
	 * 
	 * @param path the path to the conversation file from which to get the Conversation
	 */
	private void doGetConversationObject(final String path) {
		final String msg = "doGetConversationObject";
		logger.info(msg);
		
		if(path == null) {
    		//I don't know why showFileNewDialog can't be called directly but
    		//I think there is an issue with the file browser classes.
    		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					showFileNewDialog();
				}
			});
    		return;
    	}
		
		if(createNewFile) {
			
            String conversationTreeName = courseObjectName != null 
            		? courseObjectName 
            		: "New " + CourseObjectName.CONVERSATION_TREE.getDisplayName();
            
            currentConversation = createValidConversationObject(conversationTreeName);
            filePath = path;
            saveConversation(path, false);
            loadConversation(false);
            
            return;
        }

		AsyncCallback<FetchJAXBObjectResult> callback = new AsyncCallback<FetchJAXBObjectResult>(){

			@Override
			public void onFailure(Throwable t) {
				handleCallbackFailure(logger, msg, t);
				closeFile(path);
				showWaiting(false);
			}

			@Override
			public void onSuccess(FetchJAXBObjectResult result) {
				
				if(result.isSuccess()){
					if(result.wasConversionAttempted() && result.getModifiable()) {
    	    			WarningDialog.warning("Update file", "The file you've loaded was created with an old version of GIFT, "
    	    					+ "but we were able to update it for you. This file has already been saved and "
    	    					+ "is ready for modification. A backup of the original file was created: " 
    	    					+ result.getFileName() + FileUtil.BACKUP_FILE_EXTENSION);
					}

					currentConversation = (Conversation) result.getJAXBObject();
					if(!fileAlreadyLocked){
						setReadOnly(!result.getModifiable());
					}
					setFilePath(path);
					loadConversation(true);
					//saveConversation(path, false);
					
				} else {
					
					final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							result.getErrorMsg(), 
							result.getErrorDetails(), 
							result.getErrorStackTrace());				
					dialog.setText("Failed to Load File");
					
					dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
						
						@Override
						public void onClose(CloseEvent<PopupPanel> event) {
							closeFile(path);
						}
					});
					
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						
						@Override
						public void execute() {
							showWaiting(false);
							dialog.center();
						}
					});
				}
					
				logger.fine("Got conversation object for Conversation at '" + filePath + "'.");
			}		
		};
		
		logger.info("Getting conversation object for Conversation at '" + path + "'");
		
		String userName = GatClientUtility.getUserName();
		
		FetchJAXBObject action = new FetchJAXBObject();
		action.setRelativePath(path);
		action.setUserName(userName);
		showWaiting(true);
		
		dispatchService.execute(action, callback);
	}
	
	/**
	 * Loads a conversation.
	 * 
	 * @param existing Whether or not an existing file is being loaded.
	 */
	private void loadConversation(boolean existing) {
		isConversationDirty = false;
		
		if(existing) {
			FetchConversationTreeJSON action = new FetchConversationTreeJSON();
			action.setConversation(currentConversation);
			dispatchService.execute(action, new AsyncCallback<FetchConversationTreeJSONResult>() {
	
				@Override
				public void onFailure(Throwable thrown) {
					ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							"An error occurred on the server.", 
							thrown.getMessage(), 
							DetailedException.getFullStackTrace(thrown));
					dialog.setDialogTitle("Server Error");
					dialog.center();
				}
	
				@Override
				public void onSuccess(FetchConversationTreeJSONResult result) {
					BsLoadingDialogBox.remove();
					showWaiting(false);
					
					if(result.isSuccess()) {
						conversationView.loadTree(result.getConversationTreeJSON());
					} else {
						ErrorDetailsDialog dialog = new ErrorDetailsDialog(
								result.getErrorMsg(), 
								result.getErrorDetails(), 
								result.getErrorStackTrace());
						dialog.setDialogTitle("An Error Occurred");
						dialog.center();
					}
					
				}
				
			});
		}
		
		if(!existing){
		    BsLoadingDialogBox.remove();
		}
		
		//set the conversation name (also fire change event so editor header is updated to that value)
		conversationView.getNameInput().setValue(currentConversation.getName());
		ValueChangeEvent.fire(conversationView.getNameInputValueChange(), currentConversation.getName());
		
		conversationView.getAuthorsDescriptionInput().setHTML(
				currentConversation.getAuthorsDescription() == null ? "" : currentConversation.getAuthorsDescription());
		
		conversationView.getLearnersDescriptionInput().setHTML(
				currentConversation.getLearnersDescription() == null ? "" : currentConversation.getLearnersDescription());
		
		eventBus.fireEvent(new ConversationLoadedEvent(currentConversation));
		
		GatClientUtility.defineSaveEmbeddedCourseObject(createSaveObject(filePath));
	}
			
	private void showFileNewDialog() {
		fileNewDialog.clearFileName();
		fileNewDialog.center();
	}
    
	private String getFileNewPath() {
		String path = fileNewDialog.getValue();
		if(path == null) {
			return null;
		}
		
		if(!path.endsWith(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)) {
			path = path.concat(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION);
		}
		return path;
    }
	
	private void showSaveAsDialog() {
		fileSaveAsDialog.clearFileName();
		fileSaveAsDialog.center();
        
        WarningDialog.warning("File references", "GIFT files contain relative paths that reference other GIFT files as well as associated content. Saving a file that already exists to a new location has the potential to invalidate these paths. In the future the GIFT team will develop a feature to automatically handle these issues for you but until then use the Save-As feature at your own risk.");
	}
	
	private String getFileSaveAsPath() {
		String path = fileSaveAsDialog.getValue();
		if(path == null) {
			return null;
		}
		
		if(!path.endsWith(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)) {
			path = path.concat(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION);
		}
		return path;
	}
	
	/**
	 * Save the conversation.
	 *
	 * @param path the path to save the conversation to.
	 * @param showConfirmationDialog whether or not to display the confirmation dialog
	 */
	private void saveConversation(final String path, final boolean showConfirmationDialog) {
		saveConversation(path, showConfirmationDialog, false);
	}

	/**
	 * Save the conversation.
	 *
	 * @param path the path to save the conversation to.
	 * @param showConfirmationDialog whether or not to display the confirmation dialog
	 * @param validateConversation whether or not to validate the conversation after saving
	 */
	private void saveConversation(final String path, final boolean showConfirmationDialog, final boolean validateConversation) {
		
		if(readOnly && path != null && filePath != null && path.equals(filePath)){
			WarningDialog.warning("Read Only", "Cannot overwrite original file while viewing a read-only copy. <br/>"
					+ "<br/>"
					+ "If you wish to save this conversation to a different file, please specify another file name.");
			return;
		}
		
		AsyncCallback<SaveJaxbObjectResult> callback = new AsyncCallback<SaveJaxbObjectResult>() {
			
			@Override
			public void onFailure(Throwable throwable) {
				showWaiting(false);		

				String label = "Unable to save conversation: ";
				String title = "Failed to Save Conversation";

				if(filePath == null){
					label = "Unable to create conversation: ";
					title = "Failed to Create Conversation";
					
					fileNewDialog.hide();
					fileNewDialog.center();
				}
				
				ErrorDetailsDialog error = new ErrorDetailsDialog(
						label + throwable.getMessage(),
						throwable.toString(), 
						DetailedException.getFullStackTrace(throwable));
				error.setText(title);
				error.center();
			}
			
			@Override
			public void onSuccess(SaveJaxbObjectResult result) {
				showWaiting(false);
				
				if(result.isSuccess()) {
					
					fileNewDialog.hide();
					fileSaveAsDialog.hide();
					
					//Let the user know the save result.
					if(showConfirmationDialog) {
						String message = "File saved successfully!";
						if(!result.isSchemaValid()) {
							message += "<p>The authored content is not valid against the schema. Therefore, this file should not be used with GIFT until the contents are correct. Using this incomplete file can result in GIFT not executing correctly.";
						}
						WarningDialog.info("Save Successful", message);
					}
					
					//This condition is true when we load a file from the
					//server and then use the Save-As functionality to save as
					//a different file. In this case it is our responsibility
					//to release the lock on the original file since we're no
					//longer using it.
					if(filePath != null && !filePath.equals(path)) {
						
						if(readOnly){
							setReadOnly(false);
						}
						
						unlockFile();
					}

					//When we ask the server to save a file it generates
					//a new version for that file before saving. The newly
					//generated version is based on the previous version.
					//So if we don't save the newly generated version in the
					//conversation object then the server will generate the same
					//version every time we save.
					String newVersion = result.getNewVersion();
					currentConversation.setVersion(newVersion);
					setFilePath(path);
					
					isConversationDirty = false;
					
					if(validateConversation) {
						validateFile(filePath, GatClientUtility.getUserName());
					}
					
				} else {
					
					String label = "Unable to save conversation: ";
					String title = "Failed to Save Conversation";
					
					if(filePath == null){						
						fileNewDialog.hide();
						fileNewDialog.center();
						label = "Unable to create conversation: ";
						title = "Failed to Create Conversation";
					}
					
					ErrorDetailsDialog error = new ErrorDetailsDialog(
							label + result.getErrorMsg(),
							result.getErrorDetails(), 
							result.getErrorStackTrace());
					error.setText(title);
					error.center();
				}
			}
		};
		
		//This flag will be false if we're saving to the same file we just
		//opened. It'll be true if we're saving to a brand new file or if we're
		//overwriting an existing file on disk (Save-As).
		boolean acquireInsteadOfRenew = false;
		if(filePath == null || !filePath.equals(path)) {
			acquireInsteadOfRenew = true;
			
		}
		
		String userName = GatClientUtility.getUserName();
		logger.fine("Saving conversation: " + path);	
		SaveConversation action = new SaveConversation();
		action.setConversationJSONStr(PreviewPanel.buildConversationJSONStr(currentConversation));
		action.setConversationTreeJSONStr(conversationView.getTreeJSONStr());
		action.setPath(path);
		action.setAcquireLockInsteadOfRenew(acquireInsteadOfRenew);
		action.setUserName(userName);
		action.setIsGIFTWrap(GatClientUtility.isGIFTWrapMode());
		showWaiting(true);
		dispatchService.execute(action, callback);		
	}
		
	/**
	 * Tells the server to lock the conversation at the given path.
	 * @param path Path of the conversation to lock.
	 * @param acquisition True if we're trying to acquire the lock for the
	 * first time, false if we're simply renewing the lock.
	 */
	private void lockFile(String path, final boolean acquisition) {
		
        //This callback does absolutely nothing. In the case of acquiring the
        //lock that is the appropriate course of action. However, there seem to
        //be two ways to fail to acquire the lock:
        //
        //  1.) After saving a brand new file OR executing a Save-As+, another
        //  user sees that conversation in the dashboard and acquires the lock before
        //  you.
        //  2.) The communication to the server simply failed.
        //
        //For the first case we'd probably want to tell the user what happened
        //and jump back to the dashboard. The second case is less clear,
        //perhaps we'd want to do the same.
        AsyncCallback<LockFileResult> callback =  new AsyncCallback<LockFileResult>() {
            @Override
            public void onFailure(Throwable t) {
                // nothing to do
            }
            @Override
            public void onSuccess(LockFileResult result) {
                // nothing to do
            }
        };
        
        String userName = GatClientUtility.getUserName();
        
        //Try to lock the conversation.
        LockConversation lockConversation = new LockConversation();
        lockConversation.setRelativePath(path);
        lockConversation.setUserName(userName);
        lockConversation.setBrowserSessionKey(GatClientUtility.getUserName());
        dispatchService.execute(lockConversation, callback);
	}
	
	/**
	 * Releases the lock on the current conversation.
	 */
	private void unlockFile() {
		
        //This callback does absolutely nothing. In the case of releasing the
        //lock that is the appropriate course of action. It seems the only way
        //the lock wouldn't be released is if communication to the server
        //fails. The probability of that is very low but if we don't handle
        //it then that file will be locked forever.
        //
        AsyncCallback<GatServiceResult> callback =  new AsyncCallback<GatServiceResult>() {
            @Override
            public void onFailure(Throwable t) {
                // nothing to do
            }
            @Override
            public void onSuccess(GatServiceResult result) {
                // nothing to do
            }
        };
        
        String userName = GatClientUtility.getUserName();
        String browserSessionKey = GatClientUtility.getBrowserSessionKey();
        
        //Try to lock the conversation.
        UnlockConversation unlockConversation = new UnlockConversation();
        unlockConversation.setPath(filePath);
        unlockConversation.setUserName(userName);
        unlockConversation.setBrowserSessionKey(browserSessionKey);
        
        dispatchService.execute(unlockConversation, callback);
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#getLogger()
	 */
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	private void createNewFile(String path) {

		String conversationName = fileNewDialog.getFileNameTextBox().getValue();
		currentConversation = createValidConversationObject(conversationName);
		try {
			saveConversation(path, false);
		} catch (Exception e) {
			WarningDialog.error("Failed to save", e.toString());
		}
		loadConversation(false);
	}

	/**
	 * Initializes the view.
	 */
	@Inject
	private void init() {
		
		//
		// Conversation name TextBox handler
		//
		handlerRegistrations.add(conversationView.getNameInput().addValueChangeHandler(new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(currentConversation != null && !event.getValue().trim().isEmpty()){
					
					currentConversation.setName(event.getValue());
					eventBus.fireEvent(new EditorDirtyEvent());
				
				} else if (currentConversation != null) {
					conversationView.getNameInput().setValue(currentConversation.getName());				
				}
			}    		
    	}));
				
		//
    	// Author's Description TextArea handler
    	//
		handlerRegistrations.add(conversationView.getAuthorsDescriptionInput().addKeyUpHandler(new KeyUpHandler(){

			@Override
			public void onKeyUp(KeyUpEvent arg0) {
				if(currentConversation != null){
					currentConversation.setAuthorsDescription(conversationView.getAuthorsDescriptionInput().getHTML());
					eventBus.fireEvent(new EditorDirtyEvent());
				}    	
			}
				
    	}));
		
		//
    	// Learner's Description TextArea handler
    	//
		handlerRegistrations.add(conversationView.getLearnersDescriptionInput().addKeyUpHandler(new KeyUpHandler(){

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if(currentConversation != null){
					currentConversation.setLearnersDescription(conversationView.getLearnersDescriptionInput().getHTML());
					eventBus.fireEvent(new EditorDirtyEvent());
				}    	
			}
				
    	}));
		
		//
    	// Callback to execute when the user selects a choice node in the preview window.
    	//
		conversationView.setPreviewSubmitTextCallback(new ConversationUpdateCallback() {

			@Override
			public void getUpdate(UpdateConversation action) {
				action.setConversationJSONStr(PreviewPanel.buildConversationJSONStr(currentConversation));
				action.setConversationTreeJSONStr(conversationView.getTreeJSONStr());
				dispatchService.execute(action, new AsyncCallback<UpdateConversationResult>() {

					@Override
					public void onFailure(Throwable thrown) {
						ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
								"An error occurred while attempting to update the conversation.", 
								thrown.toString(), DetailedException.getFullStackTrace(thrown));
						
						errorDialog.setText("Failed to Update Conversation");
						errorDialog.center();
					}

					@Override
					public void onSuccess(UpdateConversationResult result) {
						if(result.isSuccess()) {
							conversationView.updateConversation(result);
							
						} else {
							ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
									result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
							
							errorDialog.setText("Failed to Update Conversation");
							errorDialog.center();
						}
					}
					
				});
			}
			
		});
		
		//
		// Preview button handler
		//
		conversationView.getPreviewButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				UpdateConversation action = new UpdateConversation();
				action.setConversationJSONStr(PreviewPanel.buildConversationJSONStr(currentConversation));
				action.setConversationTreeJSONStr(conversationView.getTreeJSONStr());
				dispatchService.execute(action, new AsyncCallback<UpdateConversationResult>() {

					@Override
					public void onFailure(Throwable thrown) {
						ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
								"An error occurred while attempting to start the conversation.", 
								thrown.toString(), DetailedException.getFullStackTrace(thrown));
						
						errorDialog.setText("Failed to Start Conversation");
						errorDialog.center();
					}

					@Override
					public void onSuccess(UpdateConversationResult result) {
						if(result.isSuccess()) {
							conversationView.setPreviewChatId(result.getChatId());
							conversationView.updateConversation(result);
							
						} else {
							conversationView.closePreview();
							ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
									"An error occurred while trying to previe the conversation: " 
									+ result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
							
							errorDialog.setText("Failed to Start Conversation");
							errorDialog.center();
						}
					}
					
				});
			}
			
		});
		
		conversationView.getClosePreviewButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				EndConversation action = new EndConversation();
				action.setChatId(conversationView.getPreviewChatId());
				dispatchService.execute(action, new AsyncCallback<GatServiceResult>() {

					@Override
					public void onFailure(Throwable thrown) {
						// nothing to do.
					}

					@Override
					public void onSuccess(GatServiceResult result) {
						// nothing to do.
					}
					
				});
			}
			
		});
					
	}
	
	/**
	 * On editor dirty event.
	 *
	 * @param event the event
	 */
	@EventHandler
	protected void onEditorDirtyEvent(EditorDirtyEvent event) {
		isConversationDirty = true;
	}

	/**
	 * On file discard changes.
	 */
	private void onFileDiscardChanges() {
		
		logger.info("onFileDiscardChanges");
		
		String msg = "Are you sure you want to discard your changes?";
		
		OkayCancelDialog.show("Warning!", msg, "Discard Changes", new OkayCancelCallback() {

			@Override
			public void cancel() {
			}

			@Override
			public void okay() {
				doGetConversationObject(filePath);
			}
			
		});
	}

	/**
     * Initializes the file new dialog based on the deployment mode. 
     * In this case, if the editor is launched in server mode, then
     * the dialog should not allow the user to save outside of the username folder.
     * 
     * @param mode - The deployment mode that the application is in (eg. SERVER/DESKTOP/EXPERIMENT)
     */
    private void initFileNewDialog(DeploymentModeEnum mode) {
        
        if (mode == DeploymentModeEnum.SERVER) {
            fileNewDialog = new DefaultGatFileSaveAsDialog(true);
        } else {
            fileNewDialog = new DefaultGatFileSaveAsDialog(false);
        }
        
        fileNewDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION});
	    fileNewDialog.setIntroMessageHTML(""
                +   "<div style='padding: 10px; font-weight: bold;'>"
                +       "Please double-click the course folder you would like to associate this Conversation with to select it.<br/>"
                +       "<br/>"
                +       "Once your course is selected, you can optionally create a subfolder to place the Conversation in."
                +   "</div>");
        fileNewDialog.setFileNameLabelText("Conversation Name:");     
        fileNewDialog.setConfirmButtonText("Create");
        fileNewDialog.setText("New Conversation");
        fileNewDialog.setCloseOnConfirm(false);
                
        handlerRegistrations.add(fileNewDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String path = getFileNewPath();
                if(path == null) {                    
                    fileNewDialog.reallowConfirm();
                    return;
                }
                
                //Checks for illegal characters in the name of the file.
                String validationMsg = DocumentUtil.validateFileName(fileNewDialog.getFileNameTextBox().getValue());
                if(validationMsg != null){
                	WarningDialog.illegalCharactersWarning(validationMsg);
                	fileNewDialog.reallowConfirm();
                	return;
                } 

                //If the path is within the course folder then the first
                //element will be the user name, the second will be the course
                //folder, and the last element will be the file name.
                int length = path.split("/").length;
                if(length < 3) {
                    
                	WarningDialog.warning("Invalid path", "All new files must be created inside a course folder.");
                     
                    fileNewDialog.reallowConfirm();
                    return;
                }
                
                int index = path.lastIndexOf("/") + 1;
                String fileName = path.substring(index);
                boolean exists = fileNewDialog.isFileOrFolderInCurrentDirectory(fileName);
                if(!exists || path.equals(filePath)) {
                    createNewFile(path);
                } else {
                    OkayCancelDialog.show("Confirm Overwrite", "A Conversation named '" + fileName +"' already exists at '" + path + "'. "
                            + "Would you like to overwrite this Conversation?", "Overwrite", new OkayCancelCallback() {
                        @Override
                        public void okay() {
                            createNewFile(path);
                        }
                        
                        @Override
                        public void cancel() {
                            fileNewDialog.reallowConfirm();
                        }
                    });
                }
            }
        }));
        
        fileNewDialog.setCancelCallback(new CancelCallback() {
            
            @Override
            public void onCancel() {
                if(filePath == null) {
                    fileNewDialog.hide();
                    returnToDashboard();
                }
            }
        });
        
    }
    
    /**
     * Initializes the file save as dialog based on the deployment mode. 
     * In this case, if the dkf editor is launched in server mode, then
     * the dialog should not allow the user to save outside of the username folder.
     * 
     * @param mode - The deployment mode that the application is in (eg. SERVER/DESKTOP/EXPERIMENT)
     */
    private void initFileSaveAsDialog(DeploymentModeEnum mode) {
        
    	if (mode == DeploymentModeEnum.SERVER) {
	        fileSaveAsDialog = new DefaultGatFileSaveAsDialog(true);
        } else {
            fileSaveAsDialog = new DefaultGatFileSaveAsDialog(false);
        }
	    
	    fileSaveAsDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION});
	    
	    // Setup handler registrations.
        HandlerRegistration registration = null;
        
        registration = fileSaveAsDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String path = getFileSaveAsPath();
                if(path == null) {
                    return;
                }
                
                //If the path is within the course folder then the first
                //element will be the user name, the second will be the course
                //folder, and the last element will be the file name.
                int length = path.split("/").length;
                if(length < 3) {
                    WarningDialog.warning("Invalid path", "All new files must be created inside a course folder.");
                    return;
                }
                
                int index = path.lastIndexOf("/") + 1;
                String fileName = path.substring(index);
                boolean exists = fileSaveAsDialog.isFileOrFolderInCurrentDirectory(fileName);
                if(!exists || path.equals(filePath)) {
                    saveConversation(path, true);
                } else {
                    OkayCancelDialog.show("Confirm Save As", "Would you like to overwrite the existing file?", "Save Changes", new OkayCancelCallback() {
                        @Override
                        public void okay() {
                            saveConversation(path, true);
                        }
                        
                        @Override
                        public void cancel() {
                        }
                    });
                }
            }
        });
        handlerRegistrations.add(registration);
    }
    
	@Override
	public void start(final AcceptsOneWidget containerWidget, HashMap<String, String> startParams) {		
		super.start();
		
		logger.info("start() called with params: " + startParams);
		
		// Setup any start parameters that have been passed in via the url.
        DeploymentModeEnum mode = DeploymentModeEnum.DESKTOP;
        String modeParam = startParams.get(DkfPlace.PARAM_DEPLOYMODE);
        String createParam = startParams.get(DkfPlace.PARAM_CREATENEW);
        String fullScreenParam = startParams.get(DkfPlace.PARAM_FULLSCREEN);
        String allowAssessmentsParam = startParams.get(ConversationPlace.PARAM_ALLOW_ASSESSMENTS);
        
        // Initialize the dialogs based on what mode the presenter is deployed in.
        initFileNewDialog(mode);
        initFileSaveAsDialog(mode);
        

        if (modeParam != null) {
            mode = DeploymentModeEnum.valueOf(modeParam);
        } else {
            logger.severe("Start mode could not be determined.  Defaulting to DESKTOP.");
        }
        
        if(createParam != null) {
            createNewFile = Boolean.valueOf(createParam);
        } else {
            createNewFile = false;
        }
        
        boolean fullScreen = false;
        
        if(fullScreenParam != null) {
            try{
                fullScreen = Boolean.valueOf(fullScreenParam);
            }catch(@SuppressWarnings("unused") Throwable t){
                logger.warning("Found unhandled full screen param of '"+fullScreenParam+"'.");
            }
        }
        
        //set whether or not the conversation should be previewed in full screen
        conversationView.setPreviewFullScreen(fullScreen);
        
        if(allowAssessmentsParam != null){
            try{
                conversationView.setAllowConceptAssessments(Boolean.valueOf(allowAssessmentsParam));
            }catch(@SuppressWarnings("unused") Throwable t){
                logger.warning("Found unhandled allow assessments param of '"+allowAssessmentsParam+"'.");
                // default - allow
                conversationView.setAllowConceptAssessments(true);
            }
        }else{
            // default - allow
            conversationView.setAllowConceptAssessments(true);
        }
        
        String encodedPath = startParams.get(ConversationPlace.PARAM_FILEPATH);
		final String path = encodedPath != null ? URL.decode(encodedPath) : encodedPath;
		
		if(path == null || path.isEmpty()) {
			startPart2(containerWidget, null);
			return;
		}
		
		AsyncCallback<LockFileResult> callback =  new AsyncCallback<LockFileResult>() {
	        @Override
			public void onFailure(Throwable t) {
	            
	            logger.log(Level.SEVERE, "Failed to acquire lock on conversation - "+t, t);
	        	
	        	startPart2(containerWidget, path);
        		setReadOnly(true);
	        }
	        
	        @Override
			public void onSuccess(LockFileResult result) {
	            
	            logger.info("Received lock conversation result - "+result);
	        	
	        	if(result.isSuccess()) {
	        		
	        		startPart2(containerWidget, path);
	        		setReadOnly(false);
	        		
	        	} else {
	        		
	        		startPart2(containerWidget, path);
	        		fileAlreadyLocked = true;
	        		setReadOnly(true);
	        	}
	        	
	        }
	    };
	    
	    String userName = GatClientUtility.getUserName();
	    
	    //Try to lock the Conversation
	    LockConversation lockConversation = new LockConversation();
	    lockConversation.setRelativePath(path);
	    lockConversation.setUserName(userName);
	    lockConversation.setBrowserSessionKey(GatClientUtility.getUserName());
	    dispatchService.execute(lockConversation, callback);
	}
	
	private void startPart2(AcceptsOneWidget containerWidget, String path) {
		//Every minute remind the server that the file needs to 
		//be locked because we're still working on it.
		lockTimer.scheduleRepeating(60000);
		
		setupView(containerWidget, conversationView.asWidget());
		eventRegistration = eventBinder.bindEventHandlers(this, eventBus);
		
		doGetConversationObject(path);
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#stop()
	 */
	@Override
	public void stop() {
		super.stop();
		
		lockTimer.cancel();
		
		//This method gets called when we're jumping back to the dashboard, we
		//need to unlock the file so it is editable again.
		unlockFile();
	}
	
	private void returnToDashboard(){		
		closeFile(filePath);
	}
	
	private native void closeFile(String path)/*-{
	
		if($wnd.parent != null){
			$wnd.parent.closeFile(path);
			
		}
		
	}-*/;
	
	/**
	 * Sets the file path and updates the GUI to reflect the
	 * new path.
	 * @param path
	 */
	private void setFilePath(String path) {
		
		filePath = path;
		
		//Let the file selection dialog know which folder files should be
		//uploaded/copied to.
		String courseFolderPath = filePath.substring(0,  filePath.lastIndexOf("/"));
		DefaultGatFileSelectionDialog.courseFolderPath = courseFolderPath;
	}

	/**
	 * Sets the file's readOnly status
	 * 
	 * @param readOnly true if the file is read-only.
	 */
	@Override
    public void setReadOnly(boolean readOnly){
		this.readOnly = readOnly;
		conversationView.setReadOnly(readOnly);		
	}
	
	/**
	 * Validates the specified file and displays a dialog containing validation 
	 * errors to the client.
	 * 
	 * @param filePath The path of the file to validate.
	 * @param userName The user validating the file.
	 */
	private void validateFile(String filePath, final String userName) {
		
		ValidateFile action = new ValidateFile();		
		action.setRelativePath(filePath);
		action.setUserName(userName);
		
		BsLoadingDialogBox.display("Validating File", "Validating, please wait...");
		
		dispatchService.execute(action, new AsyncCallback<ValidateFileResult>() {

			@Override
			public void onFailure(Throwable cause) {
				BsLoadingDialogBox.remove();
								
				ErrorDetailsDialog dialog = new ErrorDetailsDialog(
						"Failed to validate the file.", 
						cause.getMessage(), 
						DetailedException.getFullStackTrace(cause));
				dialog.setDialogTitle("Validation Failed");
				dialog.center();
			}

			@Override
			public void onSuccess(ValidateFileResult result) {
				BsLoadingDialogBox.remove();
				
				if(result.isSuccess()) {
					ModalDialogBox dialog = new ModalDialogBox();
					dialog.setText("Validation Complete");
					dialog.setWidget(new Label("No errors found."));
					dialog.setCloseable(true);
					dialog.center();
				} else {
					FileValidationDialog dialog = new FileValidationDialog(result.getFileName(),
							result.getReason(), result.getDetails(), result.getStackTrace());
					dialog.setText("Validation Errors");
					dialog.center();
				}				
			}			
		});
	}
	
	/**
	 * Sets the name of the course object used to open the current conversation for editing
	 * 
	 * @param name the course object name
	 */
	private void setCourseObjectName(String name){		
		
		courseObjectName = name;
		
		if(currentConversation != null){
			currentConversation.setName(name);
		}
		
		conversationView.getNameInput().setValue(currentConversation.getName());
		ValueChangeEvent.fire(conversationView.getNameInputValueChange(), currentConversation.getName());
	}
	
	public native void exposeNativeFunctions()/*-{
    
		var that = this;
		
		$wnd.stop = $entry(function(){
			that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.conversation.ConversationPresenter::stop()();
		});
		
		$wnd.setCourseObjectName = $entry(function(name){
			that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.conversation.ConversationPresenter::setCourseObjectName(Ljava/lang/String;)(name);
		});
	
	}-*/;

	
	private native JavaScriptObject createSaveObject(String path)/*-{
	
		var that = this;
	   	var saveObj = function() {
	        that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.conversation.ConversationPresenter::saveConversation(Ljava/lang/String;Z)(path, false);
	    };
		
		return saveObj;
	}-*/;
}
