/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner;

import generated.learner.Input;
import generated.learner.Inputs;
import generated.learner.LearnerConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.file.CancelCallback;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.place.LearnerConfigurationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.HelpButtonWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.LearnerStateInterpreterTree;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.wizard.BuildLearnerStateInterpreterDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.wizard.LearnerStateInterpreterBuiltHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSaveAsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.FileValidationDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.FetchDefaultLearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.LockLearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.SaveLearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.UnlockLearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;
import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The Learner Configuration Editor is used to display and modify learner
 * configuration files. A learner configuration file specifies a collection of
 * interpreters that consume data from GIFT and determine the current status
 * of a particular Learner State category as well as predict future states.
 * 
 * @author elafave
 */
public class LearnerConfigurationEditor extends Composite implements LearnerStateInterpreterBuiltHandler {
	
    /** The logger. */
    private static Logger logger = Logger.getLogger(LearnerConfigurationEditor.class.getName());
            
	/** The ui binder. */
    interface LearnerConfigurationEditorUiBinder extends UiBinder<Widget, LearnerConfigurationEditor> {} 
	private static LearnerConfigurationEditorUiBinder uiBinder = GWT.create(LearnerConfigurationEditorUiBinder.class);
	
	private static final String POTENTIAL_UNSAVED_CHANGES_MESSAGE = "This tool doesn't keep track of whether or not you've made changes since your last save operation. Leaving this tool, as you've requested, will result in any unsaved changes being discarded. Would you like to proceed with your request to leave this tool?";
	
	@UiField 
	protected MenuItem editorIcon;
	
	/**
	 * Used to jump back to the dashboard at the user's request.
	 */
	@Inject
	private PlaceController placeController;
	
	/**
	 * Used to communicate with the server.
	 */
	@Inject
	protected DispatchAsync dispatchAsync;
	
	/**
	 * The user clicks this when they want to create a new file.
	 */
	@UiField
	protected MenuItem fileNewMenuItem;
	
	/**
	 * The user clicks this when they want to save their work.
	 */
	@UiField
	protected MenuItem fileSaveMenuItem;
	
	/**
	 * The user clicks this when they want to Save-As the file.
	 */
	@UiField
	protected MenuItem fileSaveAsMenuItem;
		
	/** 
	 * The user clicks this when they want to save and validate their configuration
	 */
	@UiField 
	protected MenuItem fileSaveAndValidateMenuItem;
	
	/**
	 * The user clicks this when they want to discard the changes made to the
	 * current file.
	 */
	@UiField
	protected MenuItem fileDiscardChangesMenuItem;
	
	/**
	 * The user clicks this when they want to add a new learner state interpreter.
	 */
	@UiField
	protected MenuItem addMenuItem;
	
	/**
	 * The user clicks this when they want to remove a learner state interpreter.
	 */
	@UiField
	protected MenuItem deleteMenuItem;
	
	/** 
	 * The help button 
	 */
	@UiField 
	protected HelpButtonWidget helpButton;
	
	/**
	 * Tree that displays the learner state interpreters structured by learner state.
	 */
	@UiField
	protected LearnerStateInterpreterTree learnerStateInterpreterTree;
	
	@UiField
	protected DeckPanel learnerStateInterpreterTreePanel;
	
	@UiField
	protected HTML learnerStateInterpreterTreeEmptyLabel;
	
	/**
	 * Editor allowing the user to view/modify the selected learner state interpreter.
	 */
	@UiField
	protected LearnerStateInterpreterEditor learnerStateInterpreterEditor;
	
	/** Bug text box. */
    @UiField
    protected TextBox bugTextBox;
    
    @UiField
    protected MenuItem editMenu;
	
	/**
	 * Dialog used to build a new learner state interpreter.
	 */
	private BuildLearnerStateInterpreterDialog buildLeanerStateInterpreterDialog = new BuildLearnerStateInterpreterDialog(this);
	
	/**
	 * File New dialog.
	 */
	private DefaultGatFileSaveAsDialog fileNewDialog = null;
	
	/**
	 * File Save As dialog.
	 */
	private DefaultGatFileSaveAsDialog fileSaveAsDialog = null;
	
	/**
	 * The learner configuration path.
	 */
	private String filePath;
	
	/** Whether or not this editor is read-only */
	private boolean readOnly;
	
	/** Whether or not a new file should be created based on the file path */
    private boolean createNewFile;
	
	/** 
     * special condition if a file is locked and another user tries to open the file
     * should currently only happen in desktop mode when users can see each other's workspaces
     */
    private boolean fileAlreadyLocked = false;
	
	/**
	 * The LearnerConfiguration object we're editing
	 */
	private LearnerConfiguration learnerConfiguration;
	
	/**
	 * Heartbeat timer so the server doesn't unlock the file we're editing.
	 */
	private Timer lockTimer = new Timer() {
		@Override
		public void run() {
			if(filePath != null) {
				lockFile();
			}
		}
	};
    
    public LearnerConfigurationEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        addMenuItemListeners();
        
        
        
        editorIcon.setHTML(""
        		+ 	"<div style='width: 15px; height: 30px;'>"
				+ 		"<img style='"
				+ 				"position: absolute; width: 40px; height: 40px;"
				+ 				"top: 0px; left: 0px;"
				+ 		"' src='" + GatClientBundle.INSTANCE.learner_state_editor_icon().getSafeUri().asString() + "'/>"
				+ 	"<div>");
        
        SelectionHandler<TreeItem> selectionChangeHandler = new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> arg0) {
				Input learnerStateInterpreter = learnerStateInterpreterTree.getSelectedInterpreter();
				if(learnerStateInterpreter == null) {
					//TODO What do we do if they've clicked a learner state instead of an interpreter?
					
					//TODO What do we do if they use Ctrl+Click to de-select a n interpreter?
				}
				else {
					learnerStateInterpreterEditor.setInterpreter(learnerStateInterpreter);
				}
			}
        };
        learnerStateInterpreterTree.addSelectionHandler(selectionChangeHandler);
        
        learnerStateInterpreterTreePanel.showWidget(learnerStateInterpreterTreePanel.getWidgetIndex(learnerStateInterpreterTreeEmptyLabel));
        
		//Every minute remind the server that the LearnerConfiguration needs to 
		//be locked because we're still working on it.
		lockTimer.scheduleRepeating(60000);
        
        /*
         * There is an odd bug that is a result of some combination of text 
         * boxes, iFrames, and Internet Explorer. The text boxes would somehow
         * get into a state where clicking on it would be ignored thus
         * preventing the user from typing in it. I've found that putting a
         * text box in the highest GUI element, scheduling this delayed
         * command, and then making the text box invisible prevents the bug
         * from happening. I don't know why this works.
         */
        ScheduledCommand command = new ScheduledCommand() {
			@Override
			public void execute() {
				bugTextBox.setFocus(true);
				bugTextBox.setVisible(false);
			}
		};
        Scheduler.get().scheduleDeferred(command);
    }
    
    private void createNewFile(String path) {
    	learnerConfiguration = createDefaultLearnerConfiguration();
		displayLearnerConfiguration();
		updateMenuAndEditorVisibility();
		buildLeanerStateInterpreterDialog.center();
    }
    
    private void addMenuItemListeners() {
    	
    	
    	ScheduledCommand newCommand = new ScheduledCommand() {
			@Override
			public void execute() {
				OkayCancelCallback callback = new OkayCancelCallback() {
					@Override
					public void okay() {
						showFileNewDialog();
					}
					
					@Override
					public void cancel() {
					}
				};
				OkayCancelDialog.show("New File", POTENTIAL_UNSAVED_CHANGES_MESSAGE, "Discard Changes", callback);
			}
        };
        fileNewMenuItem.setScheduledCommand(newCommand);
        
        ScheduledCommand saveCommand = new ScheduledCommand() {
        	@Override
        	public void execute() {
        		saveLearnerConfiguration(filePath, true);
        	}
        };
        fileSaveMenuItem.setScheduledCommand(saveCommand);

        ScheduledCommand saveAsCommand = new ScheduledCommand() {
        	@Override
        	public void execute() {
        		showSaveAsDialog();
        	}
        };
        fileSaveAsMenuItem.setScheduledCommand(saveAsCommand);
        
        ScheduledCommand discardChangesCommand = new ScheduledCommand() {
			@Override
			public void execute() {
				loadLearnerConfiguration(filePath);
			}
        };
                
        fileSaveAndValidateMenuItem.setScheduledCommand(new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				saveLearnerConfiguration(filePath, false, true);
			}
		});
        
        fileDiscardChangesMenuItem.setScheduledCommand(discardChangesCommand);
        
        ScheduledCommand addCommand = new ScheduledCommand() {
			@Override
			public void execute() {
				buildLeanerStateInterpreterDialog.center();
			}
		};
		addMenuItem.setScheduledCommand(addCommand);
		
		ScheduledCommand removeCommand = new ScheduledCommand() {
			@Override
			public void execute() {
				//If the user doesn't have a Learner State Interpreter selected
				//(perhaps they don't have anything selected or perhaps they've
				//got the root node selected).
				final Input selectedInterpreter = learnerStateInterpreterTree.getSelectedInterpreter();
				if(selectedInterpreter == null) {
					WarningDialog.warning("Selection missing", "Please select the Learner State Interpreter you'd like to delete before requesting it be deleted.");
					return;
				}

				OkayCancelDialog.show("Confirm Delete", 
						"Are you sure you want to delete this learner state interpreter?", "Delete", new OkayCancelCallback() {

							@Override
							public void okay() {
								boolean success = learnerStateInterpreterTree.removeSelectedInterpreter();
								if(success) {
									learnerConfiguration.getInputs().getInput().remove(selectedInterpreter);
									updateMenuAndEditorVisibility();
								} else {
									//Given the error checking we performed above, it shouldn't
									//be possible to get to this point.
									WarningDialog.warning("Delete failed", "Failed to delete the selected Learner State Interpreter.");
								}
							}

							@Override
							public void cancel() {
								// nothing to do
							}
					
				});
				
			}
		};
		deleteMenuItem.setScheduledCommand(removeCommand);
		learnerStateInterpreterTree.setRemoveInterpreterCommand(removeCommand);
    }
    
    /**
     * Initializes the file new dialog based on the deployment mode. 
     * In this case, if the dkf editor is launched in server mode, then
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
        
        
        fileNewDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION});

        fileNewDialog.setIntroMessageHTML(""
                +   "<div style='padding: 10px; font-weight: bold;'>"
                +       "Please double-click the course folder you would like to associate this learner configuration with to select it.<br/>"
                +       "<br/>"
                +       "Once your course is selected, you can optionally create a subfolder to place the learner configuration in."
                +   "</div>");
        fileNewDialog.setFileNameLabelText("Learner Configuration Name:");
        fileNewDialog.setConfirmButtonText("Create");
        fileNewDialog.setText("New Learner Configuration");
        fileNewDialog.setCloseOnConfirm(false);
        
        fileNewDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
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
                    OkayCancelDialog.show("Confirm Overwrite", "A learner configuration named '" + fileName +"' already exists at '" + path + "'. "
                            + "Would you like to overwrite this learner configuration?", "Overwrite", new OkayCancelCallback() {
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
        });    
        
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
        
        fileSaveAsDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION});
        
        fileSaveAsDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
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
                    saveLearnerConfiguration(path, true);
                } else {
                    OkayCancelDialog.show("Confirm Save As", "Would you like to overwrite the existing file?", "Save Changes", new OkayCancelCallback() {
                        @Override
                        public void okay() {
                            saveLearnerConfiguration(path, true);
                        }
                        
                        @Override
                        public void cancel() {
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Attaches the learner configuration editor to the containing widget.
     * Loads the learner configuration file and sets the read only status
     * 
     * @param containerWidget the widget to attach the leaner configuration editor to
     * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
     */
    public void start(final AcceptsOneWidget containerWidget, HashMap<String, String> startParams) {
    	
        logger.info("start() called with params: " + startParams);
        
        // Setup any start parameters that have been passed in via the url.
        DeploymentModeEnum mode = DeploymentModeEnum.DESKTOP;
        String modeParam = startParams.get(LearnerConfigurationPlace.PARAM_DEPLOYMODE);
        String createParam = startParams.get(LearnerConfigurationPlace.PARAM_CREATENEW);
        
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
        
        // Initialize the dialogs based on what mode the presenter is deployed in.
        initFileNewDialog(mode);
        initFileSaveAsDialog(mode);
        
        /* 
         * Nick: Need to URL decode the path to prevent an issue in Firefox where URL encoded characters (namely spaces as %20s) aren't 
         * properly decoded before they get to this point
         */
        String encodedPath = startParams.get(LearnerConfigurationPlace.PARAM_FILEPATH);
		final String path = encodedPath != null ? URL.decode(encodedPath) : encodedPath;
        
    	if(path == null || path.isEmpty() || path.equals("null")) {
    		setReadOnly(false);
    		startPart2(containerWidget, path);
    	} else {    	
	    	LockLearnerConfiguration lockLearner = new LockLearnerConfiguration();
	    	lockLearner.setRelativePath(path);
	    	lockLearner.setUserName(GatClientUtility.getUserName());
	    	lockLearner.setBrowserSessionKey(GatClientUtility.getUserName());
	    	SharedResources.getInstance().getDispatchService().execute(lockLearner, new AsyncCallback<LockFileResult>() {
	
				@Override
				public void onFailure(Throwable arg0) {
					// Couldn't acquire the lock, open this file as read only
					startPart2(containerWidget, path);
					setReadOnly(true);
				}
	
				@Override
				public void onSuccess(LockFileResult result) {
					// If the lock was not acquired, open the file as read only
					setReadOnly(!result.isSuccess());
					fileAlreadyLocked = !result.isSuccess();
					startPart2(containerWidget, path);
				}    		
	    	});
    	}
    }
    
    /**
     * Attaches the learner configuration editor to the containing widget and loads the learner configuration file.
     * 
     * @param containerWidget the widget to attach the leaner configuration editor to
     * @param path the domain relative path of the file. Can be null.
     */
    private void startPart2(final AcceptsOneWidget containerWidget, final String path) {
    	containerWidget.setWidget(this.asWidget());
    	loadLearnerConfiguration(path);
    }
    
    /**
     * Stops the lock renewing timer and unlocks the file.
     */
    public void stop() {
    	lockTimer.cancel();
		unlockFile();
    }
    
    private String getFileNewPath() {
    	String path = fileNewDialog.getValue();
    	if(path == null) {
    		return null;
    	}
    	
    	if(!path.endsWith(AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION)) {
			path = path.concat(AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION);
		}
		return path;
    }
    
    private String getFileSaveAsPath() {
    	String path = fileSaveAsDialog.getValue();
    	if(path == null) {
    		return null;
    	}
    	
    	if(!path.endsWith(AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION)) {
			path = path.concat(AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION);
		}
		return path;
    }
    
    private void updateMenuAndEditorVisibility() {
    	int learnerStateInterpreterCount = learnerConfiguration.getInputs().getInput().size();
    	boolean isEmpty = false;
    	if(learnerStateInterpreterCount == 0) {
    		isEmpty = true;
    	}

		fileSaveAsMenuItem.setEnabled(!isEmpty);
		fileSaveMenuItem.setEnabled(!isEmpty && !readOnly);
		deleteMenuItem.setEnabled(!isEmpty);
		fileSaveAndValidateMenuItem.setEnabled(fileSaveMenuItem.isEnabled());
		
		learnerStateInterpreterEditor.setVisible(!isEmpty);
		
		if(!isEmpty){
			learnerStateInterpreterEditor.redraw();
		}
		
		if(!isEmpty){
			learnerStateInterpreterTreePanel.showWidget(learnerStateInterpreterTreePanel.getWidgetIndex(learnerStateInterpreterTree));
			
		} else {
			learnerStateInterpreterTreePanel.showWidget(learnerStateInterpreterTreePanel.getWidgetIndex(learnerStateInterpreterTreeEmptyLabel));
		}
    }
    
    private void displayLearnerConfiguration() {
    	List<Input> learnerStateInterpreters = learnerConfiguration.getInputs().getInput();
    	
    	learnerStateInterpreterTree.setInterpreters(learnerStateInterpreters);
    	
    	updateMenuAndEditorVisibility();
    }
    
    static private LearnerConfiguration createDefaultLearnerConfiguration() {

    	Inputs inputs = new Inputs();
    	
    	LearnerConfiguration learnerConfiguration = new LearnerConfiguration();
    	learnerConfiguration.setInputs(inputs);
    	return learnerConfiguration;
    }
    
    /**
     * Loads a learner configuration if a valid path is supplied, if null is
     * supplied then it creates a new learner configuration that only exists on
     * the client side (if the user saves it then it'll be stored on the
     * server)
     * @param path Path to the learner configuration you want to
     * load, NULL if you want to create a new learner configuration.
     */
    public void loadLearnerConfiguration(final String path) {
    	//If null is passed in when a File->New is performed from the
    	//dashboard. It is our job to create a new file.
    	if(path == null || path.equals("")) {
    		//Hide the file loading dialog 
    		BsLoadingDialogBox.remove();
    		
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
    	
    	GatClientUtility.defineSaveEmbeddedCourseObject(createSaveObject(path));
		GatClientUtility.defineAdditionalButtonAction(createAddObject());
    	exposeNativeFunctions();
		
    	if(createNewFile) {
			filePath = path;
			BsLoadingDialogBox.remove();
			
			FetchDefaultLearnerConfiguration action = new FetchDefaultLearnerConfiguration();
			action.setUserName(GatClientUtility.getUserName());
			
			//attempt to load the default pedagogical configuration template from the server
			SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GenericGatServiceResult<LearnerConfiguration>>() {

				@Override
				public void onFailure(Throwable caught) {
					
					BsLoadingDialogBox.remove();
					
					final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							"An error occurred while fetching the configuration template file. Applying backup template.", 
							caught.toString(), 
							null
					);				
					
					dialog.setText("Failed to Load Configuration Template");
					
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						
						@Override
						public void execute() {
							dialog.center();
						}
					});	
					
					createNewFile(filePath);
				}

				@Override
				public void onSuccess(GenericGatServiceResult<LearnerConfiguration> result) {
					
					BsLoadingDialogBox.remove();
					
					GenericRpcResponse<LearnerConfiguration> response = result.getResponse();
					
					if(response.getWasSuccessful()){
						
						//use the default pedagogical configuration
						learnerConfiguration = response.getContent();						
						displayLearnerConfiguration();
						
					} else {
					
						final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
								"An error occurred while fetching the configuration template file. Applying backup template.", 
								response.getException().getReason() + "\n" + response.getException().getDetails(), 
    							response.getException().getErrorStackTrace()
    					);				
						
    					dialog.setText("Failed to Load Configuration Template");
    					
    					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
    						
    						@Override
    						public void execute() {
    							dialog.center();
    						}
    					});	
    					
    					createNewFile(filePath);
					}
				}
			});
			return;
		}
    	
    	AsyncCallback<FetchJAXBObjectResult> callback = new AsyncCallback<FetchJAXBObjectResult>(){
			@Override
			public void onFailure(Throwable t) {
				//Hide the file loading dialog 
				BsLoadingDialogBox.remove();
				
				WarningDialog.error("Load failed", "An error occurred while trying to load the file: " + t.toString());
				closeFile(path);
			}

			@Override
			public void onSuccess(FetchJAXBObjectResult result) {
				//Hide the file loading dialog 
				BsLoadingDialogBox.remove();
				
				if(result.isSuccess()) {
					
					learnerConfiguration = (LearnerConfiguration)result.getJAXBObject();
					if(!fileAlreadyLocked){
						setReadOnly(!result.getModifiable());
					}
					setFilePath(path);
    	    		displayLearnerConfiguration();
    	    		
    	    		if(result.wasConversionAttempted() && result.getModifiable()) {
						
    	    			WarningDialog.warning("File updated", "The file you've loaded was created with an old version of GIFT, "
    	    					+ "but we were able to update it for you. This file has already been saved and "
    	    					+ "is ready for modification. A backup of the original file was created: " 
    	    					+ result.getFileName() + FileUtil.BACKUP_FILE_EXTENSION);
    	    			
    	    			//if the file was converted, we need to save it after the conversion
    	    			saveLearnerConfiguration(path, false);
					}
    	    		
				} else {
					
					final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());				
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
							dialog.center();
						}
					});
				}
			}		
		};
		
		String userName = GatClientUtility.getUserName();
		
		FetchJAXBObject action = new FetchJAXBObject();
		action.setRelativePath(path);
		action.setUserName(userName);
		
		dispatchAsync.execute(action, callback);
    }
    
    /**
     * Saves the LearnerConfiguration to the server. 
     * 
	 * @param path the path where the configuration should be saved to.
	 * @param showConfirmationDialog whether or not to display the confirmation dialog after saving
	 */
	private void saveLearnerConfiguration(final String path, final boolean showConfirmationDialog) {
		saveLearnerConfiguration(path, showConfirmationDialog, false);
	}
    
    /**
     * Saves the LearnerConfiguration to the server. It turns out there is
     * actually a bit of complexity/bookkeeping associated with this:
     * 
     * 1.) If the target path is different from this file's previous path (i.e.
     * Save-As is being performed with a file that already exists on the
     * server) then this method takes care of the bookkeeping to release the
     * lock on the original location and apply it to the new location.
     * 2.) The server supplies an incremented version number every time a file
     * is saved as this method takes care of saving that attribute to the
     * LearnerConfiguration.
     * 3.) Whenever you save a file you have to tell the server to either
     * acquire (save new file or overwrite a different file) or renew (re-save
     * the file) the lock, this method figures out which case is appropriate.
	 *
	 * @param path the path where the configuration should be saved to.
	 * @param showConfirmationDialog whether or not to display the confirmation dialog after saving
	 * @param validateFile whether or not to validate the file after saving
	 */
	private void saveLearnerConfiguration(final String path, final boolean showConfirmationDialog, final boolean validateFile) {
		
		if(readOnly && path != null && filePath != null && path.equals(filePath)){
			WarningDialog.warning("Read only", "Cannot overwrite original file while viewing a read-only copy. <br/>"
					+ "<br/>"
					+ "If you wish to save this learner configuration to a different file, please specify another file name.");
			return;
		}
		
		AsyncCallback<SaveJaxbObjectResult> callback = new AsyncCallback<SaveJaxbObjectResult>() {

			@Override
			public void onFailure(Throwable t) {
				
				if(filePath != null){
					WarningDialog.error("Save failed", "Unable to save learner configuration: " + t.getLocalizedMessage());
					
				} else {
					
					fileNewDialog.hide();
					fileNewDialog.center();
					
					WarningDialog.error("Save failed", "Unable to create learner configuration: " + t.getLocalizedMessage());
				}
			}

			@Override
			public void onSuccess(SaveJaxbObjectResult result) {    
				
				if(result.isSuccess()) {   
					
					fileNewDialog.hide();
					fileSaveAsDialog.hide();	
					
					//Let the user know the save result.
					if(showConfirmationDialog) {
						String message = "File saved successfully!";
						if(!result.isSchemaValid()) {
							message += "\n\nThe authored content is not valid against the schema. Therefore, this file should not be used with GIFT until the contents are correct. Using this incomplete file can result in GIFT not executing correctly.";
						}
						WarningDialog.info("Save successful", message);
					}					
					
					//This condition is true when we load a file from the
					//server and then use the Save-As functionality to save as
					//a different file. In this case it is our responsibility
					//to release the lock on the original file since we're no
					//longer using it.
					if(filePath != null &&
							!filePath.equals(path)) {
						
						if(readOnly){
							setReadOnly(false);
						}
						
						unlockFile();
					}
					
					//When we ask the server to save a file it generates
					//a new version for that file before saving. The newly
					//generated version is based on the previous version.
					//So if we don't save the newly generated version in the
					//LearnerConfiguration object then the server will generate 
					//the same version every time we save.
					String newVersion = result.getNewVersion();
					learnerConfiguration.setVersion(newVersion);					
					setFilePath(path);
				
					if(validateFile) {
						validateFile(filePath, GatClientUtility.getUserName());
					}
					
				} else {
					
					if(filePath != null){
						WarningDialog.error("Save failed", "Unable to save learner configuration: " + result.getErrorMsg());
						
					} else {
						
						fileNewDialog.hide();
						fileNewDialog.center();
						
						WarningDialog.error("Save failed", "Unable to create learner configuration: " + result.getErrorMsg());
					}
				}
			}
		};
		
		//This flag will be false if we're saving to the same file we just
		//opened. It'll be true if we're saving to a brand new file or if we're
		//overwriting an existing file on disk (Save-As).
		boolean acquireInsteadOfRenew = false;
		if(filePath == null ||
				!filePath.equals(path)) {
			acquireInsteadOfRenew = true;
		}

		String userName = GatClientUtility.getUserName();

		SaveLearnerConfiguration action = new SaveLearnerConfiguration();
		action.setRelativePath(path);
		action.setLearnerConfiguration(learnerConfiguration);
		action.setAcquireLockInsteadOfRenew(acquireInsteadOfRenew);
		action.setUserName(userName);
		dispatchAsync.execute(action, callback);
	}
	
	private void showFileNewDialog() { 
		fileNewDialog.clearFileName();
		fileNewDialog.center();
	}
	
	private void showSaveAsDialog() {
		fileSaveAsDialog.clearFileName();
		fileSaveAsDialog.center();
        
        WarningDialog.warning("Save warning", "GIFT files contain relative paths that reference other GIFT files as well as associated content. Saving a file that already exists to a new location has the potential to invalidate these paths. In the future the GIFT team will develop a feature to automatically handle these issues for you but until then use the Save-As feature at your own risk.");
	}
	
	/**
	 * Tells the server to unlock the learner configuration file at the given path.
	 * @param filePath Path of the LearnerConfiguration to unlock.
	 */
	private void unlockFile() {
		
		if(!readOnly){
			
			//This callback does absolutely nothing. In the case of releasing the
			//lock that is the appropriate course of action. It seems the only way
			//the lock wouldn't be released is if communication to the server
			//fails. The probability of that is very low but if we don't handle
			//it then that LearnerConfiguration will be locked forever.
			//
			//TODO Handle the failure case.
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
		    String browserSessionKey = GatClientUtility.getUserName();
		    
		    //Try to lock the file before we open it.
		    UnlockLearnerConfiguration action = new UnlockLearnerConfiguration();
		    action.setRelativePath(filePath);
		    action.setUserName(userName);
		    action.setBrowserSessionKey(browserSessionKey);
		    
		    dispatchAsync.execute(action, callback);
		}
	}
	
	/**
	 * Tells the server to lock the LearnerConfiguration.
	 */
	private void lockFile() {
		
        //This callback does absolutely nothing. In the case of acquiring the
        //lock that is the appropriate course of action. However, there seem to
        //be two ways to fail to acquire the lock:
        //
        //  1.) After saving a brand new file OR executing a Save-As, another
        //  user sees that file in the dashboard and acquires the lock before
        //  you.
        //  2.) The communication to the server simply failed.
        //
        //For the first case we'd probably want to tell the user what happened
        //and jump back to the dashboard. The second case is less clear,
        //perhaps we'd want to do the same.
        //
        //TODO Handle the failure case.
        AsyncCallback<LockFileResult> callback =  new AsyncCallback<LockFileResult>() {
            @Override
            public void onFailure(Throwable t) {
            }
            @Override
            public void onSuccess(LockFileResult result) {
            }
        };
        
        String userName = GatClientUtility.getUserName();
        
        //Try to lock the Learner Configuration.
        LockLearnerConfiguration lockFile = new LockLearnerConfiguration();
        lockFile.setRelativePath(filePath);
        lockFile.setUserName(userName);
        lockFile.setBrowserSessionKey(GatClientUtility.getUserName());
        dispatchAsync.execute(lockFile, callback);
	}

	@Override
	public void onInterpreterBuilt(Input learnerStateInterpreter) {
		learnerConfiguration.getInputs().getInput().add(learnerStateInterpreter);
		learnerStateInterpreterTree.addInterpreter(learnerStateInterpreter);
		updateMenuAndEditorVisibility();
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
		
		String fileName = filePath.substring(
		filePath.lastIndexOf("/") + 1, filePath.length());
		helpButton.setText(fileName + (readOnly ? " (Read-Only)" : ""));
		
	}
	
	public String mayStop() {
		//If the filePath is null it means nothing was ever loaded and
		//therefore there is certainly no reason to prompt the user about
		//losing unsaved changes.
//		if(filePath == null) {
//			return null;
//		} else {
//			return POTENTIAL_UNSAVED_CHANGES_MESSAGE;
//		}
		
		//Nick: Set to null for now to prevent the "Are you sure" dialog from showing
		return null;
	}
	
	/** 
	 * Sets whether or not this editor should be Read-Only
	 * 
	 * @param readOnly whether or not this editor should be Read-Only
	 */
	public void setReadOnly(boolean readOnly){
		this.readOnly = readOnly;
		
		fileSaveMenuItem.setText("Save" + (readOnly ? " (unavailable in Read-Only mode)" : ""));
		fileSaveMenuItem.setEnabled(!readOnly);
		fileSaveAndValidateMenuItem.setEnabled(!readOnly);
		editMenu.setEnabled(!readOnly);
		
		if(filePath != null){
			
			String fileName = filePath.substring(
			filePath.lastIndexOf("/") + 1, filePath.length());
			helpButton.setText(fileName + (readOnly ? " (Read-Only)" : ""));
			
		}
		
		DefaultGatFileSelectionDialog.setReadOnly(readOnly);
	}
		
	private native JavaScriptObject createSaveObject(String path)/*-{
	
		var that = this;
	   	var saveObj = function() {
	        that.@mil.arl.gift.tools.authoring.server.gat.client.view.learner.LearnerConfigurationEditor::saveLearnerConfiguration(Ljava/lang/String;Z)(path, false);
	    };
		
		return saveObj;
	}-*/;
	
	private native JavaScriptObject createAddObject()/*-{
	
		var that = this;
	   	var addObj = function() {
	        that.@mil.arl.gift.tools.authoring.server.gat.client.view.learner.LearnerConfigurationEditor::addLearnerData()();
	    };
		
		return addObj;
	}-*/;
	
	public void addLearnerData() {
		addMenuItem.getScheduledCommand().execute();
	}
	
	/**
	 * Validates the specified file and displays a dialog containing validation 
	 * errors to the client.
	 * 
	 * @param filePath The path of the file to validate.
	 * @param userName The user validating the file.
	 */
	private void validateFile(final String filePath, final String userName) {
		
		ValidateFile action = new ValidateFile();
		
		action.setRelativePath(filePath);
		action.setUserName(userName);
		dispatchAsync.execute(action, new AsyncCallback<ValidateFileResult>() {

			@Override
			public void onFailure(Throwable cause) {

				ArrayList<String> stackTrace = new ArrayList<String>();
				
				if(cause.getStackTrace() != null) {
					stackTrace.add(cause.toString());
					
					for(StackTraceElement e : cause.getStackTrace()) {
						stackTrace.add("at " + e.toString());
					}
				}
				
				ErrorDetailsDialog dialog = new ErrorDetailsDialog(
						"Failed to validate the file.", 
						cause.toString(), stackTrace);
				dialog.setDialogTitle("Validation Failed");
				dialog.center();
			}

			@Override
			public void onSuccess(ValidateFileResult result) {
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
	
	public native void exposeNativeFunctions()/*-{
	    
		var that = this;
		
		$wnd.stop = $entry(function(){
			that.@mil.arl.gift.tools.authoring.server.gat.client.view.learner.LearnerConfigurationEditor::stop()();
		});
	
	}-*/;
}
