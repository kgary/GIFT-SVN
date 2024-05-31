/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor;

import generated.sensor.Filter;
import generated.sensor.Sensor;
import generated.sensor.SensorsConfiguration;
import generated.sensor.Writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.file.CancelCallback;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.place.LearnerConfigurationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.SensorsConfigurationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.HelpButtonWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.SensorEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.SensorList;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.SensorNameChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.wizard.BuildSensorDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.wizard.ISensorBuiltHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSaveAsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.FileValidationDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.sensor.LockSensorsConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.sensor.SaveSensorsConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.sensor.UnlockSensorsConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;
import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;

/**
 * The Class SensorsConfigurationEditor.
 */
public class SensorsConfigurationEditor extends Composite implements ISensorBuiltHandler {
	
    /** The logger. */
    private static Logger logger = Logger.getLogger(SensorsConfigurationEditor.class.getName());
            
	/** The ui binder. */
    interface SensorConfigurationEditorUiBinder extends UiBinder<Widget, SensorsConfigurationEditor> {} 
	private static SensorConfigurationEditorUiBinder uiBinder = GWT.create(SensorConfigurationEditorUiBinder.class);
	
	static private final String POTENTIAL_UNSAVED_CHANGES_MESSAGE = "This tool doesn't keep track of whether or not you've made changes since your last save operation. Leaving this tool, as you've requested, will result in any unsaved changes being discarded. Would you like to proceed with your request to leave this tool?";
	
	@UiField 
	protected MenuItem editorIcon;
	
	/**The place controller. */
	@Inject
	private PlaceController placeController;
	
	/** The dispatch service. */
	@Inject
	protected DispatchAsync dispatchAsync;
	
	/**
	 * The user clicks this when they want to start a new file.
	 */
	@UiField
	protected MenuItem fileNewMenuItem;
	
	/**
	 * The user clicks this when they want to save the file.
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
	 * The user clicks this when they want to add a new Sensor.
	 */
	@UiField
	protected MenuItem addMenuItem;
	
	/**
	 * The user clicks this when they want to remove a Sensor.
	 */
	@UiField
	protected MenuItem deleteMenuItem;
	
	/** 
	 * The help button 
	 */
	@UiField 
	protected HelpButtonWidget helpButton;
	
	/**
	 * Allows the user to select the sensor they want to edit.
	 */
	@UiField
	protected SensorList sensorList;
	
	/**
	 * Allows the user to edit the selected sensor.
	 */
	@UiField
	protected SensorEditor sensorEditor;
	
	/** Bug text box. */
    @UiField
    protected TextBox bugTextBox;
    
    @UiField
    protected MenuItem sensorMenu;
    
	/**
	 * Dialog/wizard used to add a new sensor configuration.
	 */
	private BuildSensorDialog addSensorConfigurationDialog = new BuildSensorDialog(this);
	
	/**
	 * File New dialog.
	 */
	private DefaultGatFileSaveAsDialog fileNewDialog = null;
	
	/**
	 * File Save As dialog.
	 */
	private DefaultGatFileSaveAsDialog fileSaveAsDialog = null;
	
	/**
	 * The sensor path.
	 */
	private String filePath;
	
	/** Whether or not this editor is read-only */
	private boolean readOnly;
	
	/** Whether or not a new file should be created based on the file path */
    private boolean createNewFile;
	
	 /** 
     * special condition if a file is locked and another user tries to open the file
     * should only happen in desktop mode when users can see each other's workspaces
     */
    private boolean fileAlreadyLocked = false;
	
	/**
	 * The SensorConfiguration object we're editing
	 */
	private SensorsConfiguration sensorsConfiguration;
	
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
    
    public SensorsConfigurationEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        addMenuItemListeners();

        editorIcon.setHTML(""
        		+ 	"<div style='width: 15px; height: 30px;'>"
				+ 		"<img style='"
				+ 				"position: absolute; width: 40px; height: 40px;"
				+ 				"top: 0px; left: 0px;"
				+ 		"' src='" + GatClientBundle.INSTANCE.sensor_editor_icon().getSafeUri().asString() + "'/>"
				+ 	"<div>");
        
        SelectionChangeEvent.Handler sensorSelectionChangeHandler = new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent arg0) {
				Sensor sensor = sensorList.getSelectedSensor();
				handleSensorSelected(sensor);
			}
        };
        sensorList.addSelectionChangeHandler(sensorSelectionChangeHandler);
        
        sensorEditor.addSensorNameChangedCallback(new SensorNameChangedCallback() {
			
			@Override
			public void onSensorNameChanged(Sensor sensor) {
				sensorList.refresh();
			}
		});
        
		//Every minute remind the server that the SensorsConfiguration needs to 
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
    	SensorsConfigurationFactory.resetIdGenerator();
		sensorsConfiguration = SensorsConfigurationFactory.createDefaultSensorsConfiguration();
		saveSensorsConfiguration(path, false);
		displaySensorsConfiguration();
		addSensorConfigurationDialog.center();
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
	        	saveSensorsConfiguration(filePath, true);
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
        
        final ScheduledCommand discardChangesCommand = new ScheduledCommand() {
			@Override
			public void execute() {
				loadSensorsConfiguration(filePath);
			}
        };
                
        fileSaveAndValidateMenuItem.setScheduledCommand(new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				saveSensorsConfiguration(filePath, false, true);
			}
		});
        
        fileDiscardChangesMenuItem.setScheduledCommand(discardChangesCommand);
        
        ScheduledCommand addSensorCommand = new ScheduledCommand() {
			@Override
			public void execute() {
				addSensorConfigurationDialog.center();
			}
		};
		
		addMenuItem.setScheduledCommand(addSensorCommand);
		
		final ScheduledCommand removeSensorCommand = new ScheduledCommand() {
			@Override
			public void execute() {
				
				if(sensorList.getSelectedSensor() == null) {
					WarningDialog.warning("Selection missing", "Please select a sensor to delete.");
					return;
				}
				
				OkayCancelDialog.show("Confirm Delete", 
						"Are you sure you want to delete this sensor configuration?", "Delete", new OkayCancelCallback() {

					@Override
					public void okay() {
						Sensor removedSensor = sensorList.removeSelectedSensor();
						sensorsConfiguration.getSensors().getSensor().remove(removedSensor);
						SensorsConfigurationUtility.removeUnreferencedFiltersAndWriters(sensorsConfiguration);

						updateMenuAndEditorVisibility();	
					}

					@Override
					public void cancel() {
						// nothing to do
					}

				});
						
			}
		};
		
		deleteMenuItem.setScheduledCommand(removeSensorCommand);
		
		sensorList.getRemoveColumn().setFieldUpdater(new FieldUpdater<Sensor, String>() {

			@Override
			public void update(int index, Sensor key, String value) {
				removeSensorCommand.execute();
			}
        	
        });
		
    }
    
    private String getFileNewPath() {
    	String path = fileNewDialog.getValue();
    	if(path == null) {
    		return null;
    	}
    	
    	if(!path.endsWith(AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION)) {
			path = path.concat(AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION);
		}
		return path;
    }
    
    private String getFileSaveAsPath() {
    	String path = fileSaveAsDialog.getValue();
    	if(path == null) {
    		return null;
    	}
    	
    	if(!path.endsWith(AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION)) {
			path = path.concat(AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION);
		}
		return path;
    }
    
    private void updateMenuAndEditorVisibility() {
    	boolean isEmpty = sensorsConfiguration.getSensors().getSensor().isEmpty();
		fileSaveAsMenuItem.setEnabled(!isEmpty);
		fileSaveMenuItem.setEnabled(!isEmpty && !readOnly);
		fileSaveAndValidateMenuItem.setEnabled(fileSaveMenuItem.isEnabled());
		deleteMenuItem.setEnabled(!isEmpty);
		sensorEditor.setSensorEditorVisible(!isEmpty);
    }
    
    /**
     * Initializes the file new dialog based on the deployment mode. 
     * In this case, if the dkf editor is launched in server mode, then
     * the dialog should not allow the user to save outside of the username folder.
     * 
     * @param mode - The deployment mode that the application is in (eg. SERVER/DESKTOP/EXPERIMENT)
     */
    private void initFileNewDialog(DeploymentModeEnum mode) {
    	
    	//if in server mode, special "new file" dialog must be used and another new dialog will be presented upon saving
        final boolean isServerMode = mode == DeploymentModeEnum.SERVER ? true : false;
        
        if (isServerMode) {
            fileNewDialog = new DefaultGatFileSaveAsDialog(true);
        } else {
            fileNewDialog = new DefaultGatFileSaveAsDialog(false);
        }

        fileNewDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION});

        fileNewDialog.setIntroMessageHTML(""
                +   "<div style='padding: 10px; font-weight: bold;'>"
                +       "Please double-click the course folder you would like to associate this sensor configuration with to select it.<br/>"
                +       "<br/>"
                +       "Once your course is selected, you can optionally create a subfolder to place the sensor configuration in."
                +   "</div>");
        fileNewDialog.setFileNameLabelText("Sensor Configuration Name:");
        fileNewDialog.setConfirmButtonText("Create");
        fileNewDialog.setText("New Sensor Configuration");
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
                	if(isServerMode){
                		WarningDialog.warning("Warning about Sensor Configurations", "<html>The Sensor Configuration for a course can't be changed in this version of GIFT. "
	                    		+ "You can, however, author a sensor configuration file that can be used in the Desktop version of GIFT, "
	                    		+ "which can be downloaded at <a href=\"https://gifttutoring.org\" target=\"_blank\">www.gifttutoring.org</a></html>.");
                	}
                	createNewFile(path);
                } else {
                    OkayCancelDialog.show("Confirm Overwrite", "A sensor configuration named '" + fileName +"' already exists at '" + path + "'. "
                            + "Would you like to overwrite this sensor configuration?", "Overwrite", new OkayCancelCallback() {
                        @Override
                        public void okay() {
                        	if(isServerMode){
                        		WarningDialog.warning("Warning about Sensor Configurations", "<html>The Sensor Configuration for a course can't be changed in this version of GIFT. "
        	                    		+ "You can, however, author a sensor configuration file that can be used in the Desktop version of GIFT, "
        	                    		+ "which can be downloaded at <a href=\"https://gifttutoring.org\" target=\"_blank\">www.gifttutoring.org</a></html>.");
                        	}
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
                    GatClientUtility.cancelModal();
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
        
        fileSaveAsDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION});
        
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
                    saveSensorsConfiguration(path, true);
                } else {
                    OkayCancelDialog.show("Confirm Save As", "Would you like to overwrite the existing file?", "Save Changes", new OkayCancelCallback() {
                        @Override
                        public void okay() {
                            saveSensorsConfiguration(path, true);
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
        String encodedPath = startParams.get(SensorsConfigurationPlace.PARAM_FILEPATH);
		final String path = encodedPath != null ? URL.decode(encodedPath) : encodedPath;
        
    	if(path == null || path.isEmpty() || path.equals("null")) {
    		startPart2(containerWidget, path);
    		setReadOnly(false);
    	} else {
    		
    		LockSensorsConfiguration lockSensor = new LockSensorsConfiguration();
    		lockSensor.setRelativePath(path);
    		lockSensor.setUserName(GatClientUtility.getUserName());
    		lockSensor.setBrowserSessionKey(GatClientUtility.getUserName());
    		
    		SharedResources.getInstance().getDispatchService().execute(lockSensor, new AsyncCallback<LockFileResult>() {
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
    	loadSensorsConfiguration(path);
    }
    
    /**
     * Stops the lock renewing timer and unlocks the file.
     */
    public void stop() {
    	lockTimer.cancel();
		unlockFile();
    }
    
    private void displaySensorsConfiguration() {
    	List<Sensor> sensors = sensorsConfiguration.getSensors().getSensor();
		sensorList.setSensors(sensors);
		
		updateMenuAndEditorVisibility();
    }
    
    /**
     * Loads a sensors configuration if a valid path is supplied, if null is
     * supplied then it creates a new sensors configuration that only exists on
     * the client side (if the user saves it then it'll be stored on the
     * server)
     * @param path Path to the sensors configuration you want to
     * load, NULL if you want to create a new sensors configuration.
     */
    public void loadSensorsConfiguration(final String path) {
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
			createNewFile(filePath);
			return;
		}
    	
    	//Otherwise it is our job to load the given file.
		AsyncCallback<FetchJAXBObjectResult> asyncCallback = new AsyncCallback<FetchJAXBObjectResult>(){
			@Override
			public void onFailure(Throwable t) {
				//Hide the file loading dialog 
				BsLoadingDialogBox.remove();
				
				WarningDialog.error("Load failed", "An error occurred while trying to load the file: " + t.toString());
				GatClientUtility.cancelModal();
			}

			@Override
			public void onSuccess(FetchJAXBObjectResult result) {
				//Hide the file loading dialog 
				BsLoadingDialogBox.remove();
				
				if(result.isSuccess()) {

					sensorsConfiguration = (SensorsConfiguration)result.getJAXBObject();
    	    		SensorsConfigurationFactory.resetIdGenerator(sensorsConfiguration);
    	    		if(!fileAlreadyLocked){
    	    			setReadOnly(!result.getModifiable());
    	    		}
					setFilePath(path);					
					
					if(result.wasConversionAttempted() && result.getModifiable()) {
						
    	    			WarningDialog.warning("File updated", "The file you've loaded was created with an old version of GIFT, "
    	    					+ "but we were able to update it for you. This file has already been saved and "
    	    					+ "is ready for modification. A backup of the original file was created: " 
    	    					+ result.getFileName() + FileUtil.BACKUP_FILE_EXTENSION);
    	    			
    	    			//if the file was converted, we need to save it after the conversion
    	    			saveSensorsConfiguration(path, false);
					}
					
					displaySensorsConfiguration();

				} else {
					
					final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
						result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());				
					dialog.setText("Failed to Load File");
					
					dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
						
						@Override
						public void onClose(CloseEvent<PopupPanel> event) {
							GatClientUtility.cancelModal();
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
		
		dispatchAsync.execute(action, asyncCallback);
    }
   
    /**
   	 * Save the SensorsConfiguration
   	 *
   	 * @param path the path where the metadata should be saved to.
	 * @param showConfirmationDialog whether or not to display the confirmation dialog after saving
   	 */
   	private void saveSensorsConfiguration(final String path, boolean showConfirmationDialog) {
   		saveSensorsConfiguration(path, showConfirmationDialog, false);	
   	}
    
    /**
	 * Save the SensorsConfiguration
	 *
	 * @param path the path where the metadata should be saved to.
	 * @param showConfirmationDialog whether or not to display the confirmation dialog after saving
	 * @param validateFile whether or not to validate the file after saving
	 */
	private void saveSensorsConfiguration(final String path, final boolean showConfirmationDialog, final boolean validateFile) {
		
		if(readOnly && path != null && filePath != null && path.equals(filePath)){
			WarningDialog.warning("Read only", "Cannot overwrite original file while viewing a read-only copy. <br/>"
					+ "<br/>"
					+ "If you wish to save this sensor configuration to a different file, please specify another file name.");
			return;
		}
		
		AsyncCallback<SaveJaxbObjectResult> callback = new AsyncCallback<SaveJaxbObjectResult>() {

			@Override
			public void onFailure(Throwable t) {
				
				if(filePath != null){
					WarningDialog.error("Save failed", "Unable to save sensor configuration: " + t.getLocalizedMessage());
					
				} else {
					
					fileNewDialog.hide();
					fileNewDialog.center();
					
					WarningDialog.error("Save failed", "Unable to create sensor configuration: " + t.getLocalizedMessage());
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
					
					//This condition is true when the user uses the Save-As
					//feature which means we have to release the lock on the
					//original file.
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
					//SensorsConfiguration object then the server will generate 
					//the same version every time we save.
					String newVersion = result.getNewVersion();
					sensorsConfiguration.setVersion(newVersion);
					setFilePath(path);
				
					if(validateFile) {
						validateFile(filePath, GatClientUtility.getUserName());
					}
					
				} else {
					
					if(filePath != null){
						WarningDialog.error("Save failed", "Unable to save sensor configuration: " + result.getErrorMsg());
						
					} else {
						
						fileNewDialog.hide();
						fileNewDialog.center();
						
						WarningDialog.error("Save failed", "Unable to create sensor configuration: " + result.getErrorMsg());
					}
				}
			}
		};
		
		//This flag will be false if we're saving to the same file we just
		//opened. It'll be true if we're saving to a brand new file or if we're
		//overwriting an existing file on disk (Save-As).
		boolean acquireInsteadOfRenew = false;
		if(filePath == null ||
				!filePath.equals(path) || createNewFile) {
			acquireInsteadOfRenew = true;
		}
		
		String userName = GatClientUtility.getUserName();

		SaveSensorsConfiguration action = new SaveSensorsConfiguration();
		action.setRelativePath(path);
		action.setSensorsConfiguration(sensorsConfiguration);
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
	 * Unlocks the sensors configuration file.
	 */
	private void unlockFile() {
		
		if(!readOnly){
			
			//This callback does absolutely nothing. In the case of releasing the
			//lock that is the appropriate course of action. It seems the only way
			//the lock wouldn't be released is if communication to the server
			//fails. The probability of that is very low but if we don't handle
			//it then that SensorsConfiguration will be locked forever.
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
		    String browserSessionKey = GatClientUtility.getBrowserSessionKey();
		    
		    //Try to lock the SensorConfiguration before we open it.
		    UnlockSensorsConfiguration action = new UnlockSensorsConfiguration();
		    action.setRelativePath(filePath);
		    action.setUserName(userName);
		    action.setBrowserSessionKey(browserSessionKey);
		    
		    dispatchAsync.execute(action, callback);
		}
	}
	
	/**
	 * Tells the server to lock the SensorsConfiguration.
	 */
	private void lockFile() {
		
        //This callback does absolutely nothing. In the case of acquiring the
        //lock that is the appropriate course of action. However, there seem to
        //be two ways to fail to acquire the lock:
        //
        //  1.) After saving a brand new file OR executing a Save-As, another
        //  user sees that SensorsConfiguration in the dashboard and acquires 
        //  the lock before you.
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
        
        //Try to lock the file.
        LockSensorsConfiguration action = new LockSensorsConfiguration();
        action.setRelativePath(filePath);
        action.setUserName(userName);
        action.setBrowserSessionKey(GatClientUtility.getUserName());
        dispatchAsync.execute(action, callback);
	}
	
	private void handleSensorSelected(Sensor sensor) {
		//TODO Sensor will be null if the user Ctrl+Clicks to de-select a row.
		//Ideally we'd like to prevent the user from doing such a thing but
		//for now we'll pretend like that item is still selected.
		if(sensor !=  null) {
			sensorEditor.setSensor(sensor, sensorsConfiguration);
		}
	}

	@Override
	public void onSensorBuilt(Sensor sensor, Filter filter, ArrayList<Writer> writers) {
		sensorsConfiguration.getSensors().getSensor().add(sensor);
		if(filter != null) {
			sensorsConfiguration.getFilters().getFilter().add(filter);
		}
		sensorsConfiguration.getWriters().getWriter().addAll(writers);
		
		sensorList.addSensor(sensor);
		updateMenuAndEditorVisibility();
	}
	
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
		
		sensorMenu.setEnabled(!readOnly);
		
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
	        that.@mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationEditor::saveSensorsConfiguration(Ljava/lang/String;Z)(path, false);
	    };
		
		return saveObj;
	}-*/;
	
	private native JavaScriptObject createAddObject()/*-{
	
		var that = this;
	   	var addObj = function() {
	        that.@mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationEditor::addSensorData()();
	    };
		
		return addObj;
	}-*/;
	
	public void addSensorData() {
		addMenuItem.getScheduledCommand().execute();
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
			that.@mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationEditor::stop()();
		});
	
	}-*/;
}
