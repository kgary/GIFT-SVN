/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import java.math.BigInteger;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.SurveysChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AbstractSurveyResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AddSurveyDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AddSurveyDialog.AddSurveyDialogChoiceCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AddSurveyDialog.SurveyDialogOption;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SelectSurveyContextCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyEditorModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveySelectionCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveySelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteCourseSurveyReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteSurvey;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteSurveyContextResult;

/**
 * A widget used to pick a survey to use with a course object. From this widget, users can choose to create a new survey to use with the 
 * object or select an existing one. Once a survey has been picked, this widget can also be used to edit the picked survey.
 * 
 * @author nroberts
 */
public class SurveyPicker extends Composite implements
		AddSurveyDialogChoiceCallback, SurveySelectionCallback, SelectSurveyContextCallback,
		HasValue<String> {

    private static Logger logger = Logger.getLogger(SurveyPicker.class.getName());
    
	private static SurveyPickerUiBinder uiBinder = GWT
			.create(SurveyPickerUiBinder.class);

	interface SurveyPickerUiBinder extends UiBinder<Widget, SurveyPicker> {
	}

	@UiField
	protected Button addSurveyButton;

	@UiField
	protected Button editSurveyButton;

	@UiField
	protected Button removeSurveyButton;

	@UiField
	protected Button selectExistingButton;

	@UiField
	protected HTML surveyKeyText;

	@UiField
	protected Widget surveyChoicePanel;
	
	private ModalDialogBox confirmRemoveDialog = new ModalDialogBox();
	
	/** whether the author selected the 'Use Original' check box on the select survey dialog */
	private boolean useOriginal = false;
	
	private Survey survey = null;
	
	private Command surveyLoadedCommand = null;
	
	@UiHandler("editSurveyButton") 
	void onEditSurveyButtonClick(ClickEvent event) {
	    logger.info("EditSurvey button clicked.");

	    if (courseObjData != null) {
	        final int contextId = courseObjData.getSurveyContextId();
	        
	        if (contextId > 0) {
	            SurveyEditorModal.getSurveyFromContextKey(getValue(), contextId, new AsyncCallback<Survey>() {

	                @Override
	                public void onFailure(Throwable t) {
	                    displayError("Failed to get a survey", "An exception was thrown by the server: " + t.getMessage());
	                }

	                @Override
	                public void onSuccess(Survey result) {
	                    survey = result;
                        if(surveyLoadedCommand != null) {
                            surveyLoadedCommand.execute();
                        }
	                    
	                    if (result != null) {
	                    	 onLoadExistingSurveyWithSameContextKey(result);
	                    }
	                    
	                }
	                
	            });
	        } else {
	            displayError("Failed to get course surveys", "Unable to get the survey context from the database.  The context Id is not valid.");
	        }
	        
	    } else {
	        displayError("Failed to get course surveys", "Unable to get the survey context from the database.  The course object data is not valid.");
	    }
	    
	}
	

	/*
	 * The add survey dialog which allows a user to select which type of survey
	 * that is needed.
	 */
	// TODO - This survey dialog may be eventually moved, but will be put here
	// initially while we are developing the new UI flow.
	private AddSurveyDialog addSurveyDialog = null;

	/* The dialog to select an existing survey from the database */
	protected SurveySelectionDialog selectSurveyDialog = null;

	/** The course object data that this survey belongs to. */
	private AbstractSurveyResources courseObjData = null;

	/**
	 * The name of the survey that is displayed in the editor panel. This can be
	 * a default name.
	 */
	private String transitionName = "";

	/** The key for the survey that is currently selected*/
	private String value = null;
	
	/**
     * RPC service that is used to retrieve the surveys from the database
     */
    private final SurveyRpcServiceAsync rpcService = GWT.create(SurveyRpcService.class);
    
    /** The ID of the currently loaded survey context */
    private BigInteger currentSurveyContextId = null;

    /** The type of survey they the author should be allowed to author. If null, the author can author any type of survey. */
    private SurveyTypeEnum targetSurveyType;

	/**
	 * Creates a new survey picker
	 */
	public SurveyPicker() {
		initWidget(uiBinder.createAndBindUi(this));

		// Create the instance of the dialog.
		addSurveyDialog = new AddSurveyDialog(this);
		addSurveyButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent ce) {
				promptSelectSurveyType();
			}

		});

		selectSurveyDialog = new SurveySelectionDialog(courseObjData);

		selectExistingButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(!GatClientUtility.isReadOnly()){
					selectSurveyDialog.showPartOne(SurveyPicker.this);
				}
			}
		});

		removeSurveyButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				confirmRemoveDialog.center();
			}
		});
		
		confirmRemoveDialog.setGlassEnabled(true);
		confirmRemoveDialog.setText("Delete Survey");
		
		HTML confirmMessage = new HTML(
				"Do you wish to <b>permanently</b> delete this survey or simply remove this reference "
				+ "to prevent it from being used in this part of the course?"
				+ "<br/><br/>"
				+ "Other course objects will be unable to use this survey if it is deleted, "
				+ "which may cause validation issues if this survey is being referenced in "
				+ "other parts of the course."
		);
		
		confirmMessage.getElement().getStyle().setPadding(10, Unit.PX);
		confirmMessage.getElement().getStyle().setProperty("maxWidth", "700px");
		
		confirmRemoveDialog.add(confirmMessage);
		
		FlowPanel footer = new FlowPanel();
		
		Button yesButton = new Button("Delete Content & Save", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
			    survey = null;
                if(surveyLoadedCommand != null) {
                    surveyLoadedCommand.execute();
                }
				
				deleteSelectedSurvey(false);
				
				confirmRemoveDialog.hide();
			}
		});
		
		yesButton.setType(ButtonType.WARNING);
		footer.add(yesButton);
		
		Button noButton = new Button("Remove Reference", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
			    survey = null;
                if(surveyLoadedCommand != null) {
                    surveyLoadedCommand.execute();
                }
				
                deleteCourseSurveyReference();
				
				confirmRemoveDialog.hide();
			}
		});
		
		noButton.setType(ButtonType.PRIMARY);
		footer.add(noButton);
		
		Button cancelButton = new Button("Cancel", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				confirmRemoveDialog.hide();
			}
		});
		
		cancelButton.setType(ButtonType.DANGER);
		footer.add(cancelButton);
		
		confirmRemoveDialog.setFooterWidget(footer);
		
		selectSurveyDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
			
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				
				updateDisplay();
				
				//update the display whenever the survey selection dialog is closed in case the user selected or deleted a survey
				SharedResources.getInstance().getEventBus().fireEvent(new SurveysChangedEvent(SurveyPicker.this, useOriginal));
			}
		});

	}

	/**
	 * If this picker is not limited to just one survey type, prompt the author to decide which type of survey they want
	 * to author. If this picker is limited to only one type, this method will automatically pick the type for them
	 * and proceed as if the author selected it.
	 */
	protected void promptSelectSurveyType() {
	    
	    if(targetSurveyType == null) {
        
	        //let the author to pick from all the available types of surveys
    	    if(!GatClientUtility.isReadOnly()){
                // Display the add survey dialog.
                addSurveyDialog.center();
            }
    	    
	    } else {
	        
	        //force the author to use a predefined survey type
	        onChoiceSelected(SurveyDialogOption.valueOf(targetSurveyType.toString()));
	    }
    }

    @Override
	public void onChoiceSelected(SurveyDialogOption choice) {

		switch (choice) {
		case ASSESSLEARNER_QUESTIONBANK:
			// Intentional pass through
		case ASSESSLEARNER_STATIC:
			// Intentional pass through
		case COLLECTINFO_NOTSCORED:
			// Intentional pass trough
		case COLLECTINFO_SCORED:
			// This should only be set for new surveys, not existing surveys.
		    // NOTE - Setting the survey context key to null here so that the survey editor will generate a new one based
	        // on the existing survey that has been selected from the survey selection dialog.
		    SurveyEditorModal.initializeAndShow(choice, getCourseData(),
					getTransitionName(), null, this);
			break;
		case CANCEL:
			// intentional pass through
		default:
			// do nothing.
			break;

		}

	}
	
	/**
	 * Displays an error to the user and logs the error to the browser console.
	 * 
	 * @param title - the title of the error
	 * @param errorStr - The error message to be displayed.
	 */
	private void displayError(String title, String errorStr) {
	    logger.severe(errorStr);
        WarningDialog.error(title, errorStr);
	}
	
	/**
	 * Loads an existing survey and keeps the same survey context key.
	 * 
	 * @param survey - The survey to be loaded.
	 */
	private void onLoadExistingSurveyWithSameContextKey(Survey survey) {
	    
	    //reset the value in case this class instance is used repeatedly
	    this.useOriginal = false;
	    
	    SurveyEditorModal.initializeAndShow(getCourseData(), survey, getTransitionName(), value, this, false);
	}

	@Override
	public void onSelection(Survey survey, boolean useOriginal) {
	    
        logger.fine("onSelection(Survey, " + useOriginal + ")");
	    logger.info("Selected existing survey -Use original survey = "+useOriginal);
	    
	    this.useOriginal = useOriginal;
	    
	    // NOTE - Setting the survey context key to null here so that the survey editor will generate a new one based
	    // on the existing survey that has been selected from the survey selection dialog.
	    SurveyEditorModal.initializeAndShow(getCourseData(), survey, getTransitionName(), null, this, useOriginal);
	}

	@Override
	public void onCancel() {

	}

	/**
	 * Sets the global survey resources (i.e. survey context, concepts, etc.) that should be used by this widget for authoring surveys
	 * 
	 * @param resources the survey resources
	 */
	public void setSurveyResources(AbstractSurveyResources resources) {
		courseObjData = resources;
		
		if(selectSurveyDialog != null) {
		    selectSurveyDialog.setSurveyResources(resources);
		}
	}

	/**
	 * Accessor to get the course object data for this survey.
	 * 
	 * @return Course - The course object data for the survey.
	 */
	public AbstractSurveyResources getCourseData() {
		return courseObjData;
	}

	/**
	 * Sets the survey transition name for the survey choice panel.
	 * 
	 * @param name
	 *            - The name of the survey.
	 */
	public void setTransitionName(String name) {

		if (name != null) {
			this.transitionName = name;
		}
	}

	/**
	 * Gets the survey transition name for the survey choice panel.
	 * 
	 * @return String - the survey transition name for the survey choice panel.
	 */
	public String getTransitionName() {
		return this.transitionName;
	}

    /**
     * Retrieves the widget that is visible to the user. This can be used for validation.
     * 
     * @return the widget representing the visual aspects of the survey picker.
     */
    public Widget getPanelWidget() {
        return surveyChoicePanel;
    }

	public SurveyTypeEnum getSelectedSurveyType() {
	    return survey != null 
	            ? survey.getSurveyType()
                : null;
	}
	
	public void setSurveyLoadedCommand(Command newCommand) {
	    surveyLoadedCommand = newCommand;
	}

	public void updateDisplay() {

		if (value != null) {

			surveyChoicePanel.addStyleName("presentSurveyChoice-Selected");
			
			surveyKeyText.getElement().getStyle().setColor("black");
			surveyKeyText.getElement().getStyle()
					.setFontWeight(FontWeight.NORMAL);

			editSurveyButton.setVisible(true);
			removeSurveyButton.setVisible(true);
			removeSurveyButton.setEnabled(!GatClientUtility.isReadOnly());
			removeSurveyButton.setTitle(GatClientUtility.isReadOnly() ? GatClientUtility.CAN_NOT_DELETE_READ_ONLY : null);
			addSurveyButton.setVisible(false);
			selectExistingButton.setVisible(false);
			
			surveyKeyText.setText("Loading...");
			
			if(currentSurveyContextId != null){

                AsyncCallback<Survey> surveyCallback = new AsyncCallback<Survey>() {
                    @Override
                    public void onFailure(Throwable caught) {

                        surveyKeyText.setText("Missing Survey (key = " + value + ")");

                        surveyKeyText.getElement().getStyle().setColor("red");
                        surveyKeyText.getElement().getStyle().setFontWeight(FontWeight.BOLD);

                        editSurveyButton.setVisible(false);
                    }

                    @Override
                    public void onSuccess(Survey result) {
                        survey = result;
                        if (surveyLoadedCommand != null) {
                            surveyLoadedCommand.execute();
                        }

                        if (result != null && result.getName() != null) {
                            surveyKeyText.setText(result.getName());

                        } else {

                            surveyKeyText.setText("Missing Survey (key = " + value + ")");

                            surveyKeyText.getElement().getStyle().setColor("red");
                            surveyKeyText.getElement().getStyle().setFontWeight(FontWeight.BOLD);

                            editSurveyButton.setVisible(false);
                        }
                    }
                };

                if (GatClientUtility.isGIFTWrapMode()) {
                    /* Attempt to find the survey corresponding to the selected GIFT key in the
                     * export file. */
                    rpcService.getSurveyFromExportFile(value, GatClientUtility.getBaseCourseFolderPath(),
                            GatClientUtility.getUserName(), surveyCallback);
                } else {
                    /* Attempt to find the survey corresponding to the selected GIFT key in the
                     * database and display its name back to the user. If no such survey can be
                     * found, just display the GIFT key */
                    rpcService.getSurveyFromContextKey(value, currentSurveyContextId.intValue(), surveyCallback);
                }
			} else{
				surveyKeyText.setText(value);
			}

		} else {

			surveyChoicePanel.removeStyleName("presentSurveyChoice-Selected");
			
			surveyKeyText.setText("No Survey Defined");
			surveyKeyText.getElement().getStyle().setColor("red");
			surveyKeyText.getElement().getStyle()
					.setFontWeight(FontWeight.BOLD);

			editSurveyButton.setVisible(false);
			removeSurveyButton.setVisible(false);
			addSurveyButton.setVisible(true);
			addSurveyButton.setEnabled(!GatClientUtility.isReadOnly());
	        addSurveyButton.setTitle(GatClientUtility.isReadOnly() ? "Can not create new survey in Read Only mode.": null);
			selectExistingButton.setVisible(true);
			selectExistingButton.setEnabled(!GatClientUtility.isReadOnly());
			selectExistingButton.setTitle(GatClientUtility.isReadOnly() ? "Survey Selection is disabled in Read Only mode.": null);
			
		}
		
		selectSurveyDialog.clearSearchFilter();
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		setValue(value, false);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {

		if (value == this.value
				|| (this.value != null && this.value.equals(value))) {
			return;
		}

		String before = this.value;
		this.value = value;

		updateDisplay();

		if (fireEvents) {
			ValueChangeEvent.fireIfNotEqual(this, before, value);
		}
	}

    @Override
    public void onSurveyContextSelected(String surveyContextKey, int surveyContextId, Survey survey) {

        if (surveyContextKey != null && !surveyContextKey.isEmpty()) {
        	
            logger.info("Changing value of selected survey gift key from "+value+" to "+surveyContextKey);
        	String oldKey = value;
        	
            setValue(surveyContextKey, true);
            
            if(surveyContextKey.equals(oldKey)){
            	
            	//the survey's name might have changed even if the GIFT key is the same, so update the display just in case
            	updateDisplay();
            	
            	SharedResources.getInstance().getEventBus().fireEvent(new SurveysChangedEvent(this, useOriginal));
            }
        }
        
    }
    
    /**
     * Deletes the survey context survey row from the survey db, i.e. removes the reference to the survey from
     * the set of surveys referenced by the survey context for this course.
     */
    private void deleteCourseSurveyReference(){
        
        if(value != null && !GatClientUtility.isReadOnly()){
            
            if(currentSurveyContextId != null){
                
                DeleteCourseSurveyReference action = new DeleteCourseSurveyReference(GatClientUtility.getUserName(), value, currentSurveyContextId);
                
                BsLoadingDialogBox.display("Deleting Reference", "Please wait while GIFT deletes the reference to this survey for this course.");
                
                SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<DeleteSurveyContextResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        
                        BsLoadingDialogBox.remove();
                        
                        OkayCancelDialog.show(
                                "Unable to Delete Survey Reference", 
                                "GIFT was unable to delete the course's reference to this survey from the set of surveys this course is associated with. " 
                                + (caught.getMessage() != null ? caught.getMessage() : "An unknown problem prevented the deletion.")
                                + "<br/><br/>"
                                + "Do you still want to remove the survey course object's reference to the survey?", 
                                "Yes, Remove Reference", 
                                new OkayCancelCallback() {
                            
                            @Override
                            public void okay() {
                                setValue(null, true);
                            }
                            
                            @Override
                            public void cancel() {
                                //Nothing to do
                            }
                        });
                        
                    }

                    @Override
                    public void onSuccess(DeleteSurveyContextResult result) {

                        BsLoadingDialogBox.remove();
                        
                        if(result.isSuccess()){     
                            
                            setValue(null, true);
                            
                        } else {                            
                            
                            OkayCancelDialog.show(
                                    "Unable to Delete Survey Reference", 
                                    "GIFT was unable to delete the course's reference to this survey from the set of surveys this course is associated with. " 
                                    + (result.getErrorMsg() != null ? result.getErrorMsg() : "An unknown problem prevented the deletion.")
                                    + "<br/><br/>"
                                    + "Do you still want to remove this survey course object's reference to the survey?", 
                                    "Yes, Remove Reference", 
                                    new OkayCancelCallback() {
                                
                                @Override
                                public void okay() {
                                    setValue(null, true);
                                }
                                
                                @Override
                                public void cancel() {
                                    //Nothing to do
                                }
                            });                    
                        }
                    }
                    
                });
            } else {
                
                WarningDialog.error("Unable to Delete Reference to Survey", 
                        "GIFT was unable to delete the reference to this survey because this course's survey resources were not loaded properly."
                        + "<br/><br/>"
                        + "Please try reloading this course to see if the problem persists. "
                );
            }
        }
    }
    
    /**
     * Deletes the currently selected survey from the database and removes it from the course's survey context
     * 
     * @param deleteResponses whether or not the survey's learner responses should be deleted alongside it
     */
    private void deleteSelectedSurvey(boolean deleteResponses){
    	
    	if(value != null && !GatClientUtility.isReadOnly()){
    		
    		if(currentSurveyContextId != null){
    		
                DeleteSurvey action;
                if (GatClientUtility.isGIFTWrapMode()) {
                    action = new DeleteSurvey(GatClientUtility.getUserName(), value,
                            GatClientUtility.getBaseCourseFolderPath());
                } else {
                    action = new DeleteSurvey(GatClientUtility.getUserName(), value, currentSurveyContextId,
                            deleteResponses, GatClientUtility.getServerProperties().getBypassSurveyPermissionsCheck());
                }
				
				BsLoadingDialogBox.display("Deleting Survey", "Please wait while GIFT deletes this survey.");
				
				SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<DeleteSurveyContextResult>() {
	
					@Override
					public void onFailure(Throwable caught) {
						
						BsLoadingDialogBox.remove();
						
						OkayCancelDialog.show(
								"Unable to Delete Survey", 
								"GIFT was unable to delete this survey. " 
								+ (caught.getMessage() != null ? caught.getMessage() : "An unknown problem prevented the deletion.")
								+ "<br/><br/>"
								+ "Do you still want to remove this survey reference without deleting the survey?", 
								"Yes, Remove Reference", 
								new OkayCancelCallback() {
							
							@Override
							public void okay() {
								setValue(null, true);
							}
							
							@Override
							public void cancel() {
								//Nothing to do
							}
						});
					}
	
					@Override
					public void onSuccess(DeleteSurveyContextResult result) {
						
						BsLoadingDialogBox.remove();
						
						if(result.isSuccess()){		
							
							setValue(null, true);
							saveCourse();
							
							//update the display to reflect the deletion
							//Note: using false for 'useOriginal' as this logic is deleting the survey and not selecting a survey
							//      which is when the useOriginal flag is needed.
							SharedResources.getInstance().getEventBus().fireEvent(new SurveysChangedEvent(SurveyPicker.this, false));
							
						} else {
							
							if(result.hadSurveyResponses()){
								
								OkayCancelDialog.show(
										"Delete Survey Responses?", 
										"GIFT was unable to delete this survey's resources. " 
										+ (result.getErrorMsg() != null ? result.getErrorMsg() : "One or more survey elements have "
												+ "learner responses that need to be deleted first.")
										+ "<br/><br/>"
										+ "Do you want to delete these responses and continue deleting this survey?", 
										"Yes, Delete Responses", 
										new OkayCancelCallback() {
									
									@Override
									public void okay() {
										deleteSelectedSurvey(true);
									}
									
									@Override
									public void cancel() {
										//Nothing to do
									}
								});
								
							} else {
							
								OkayCancelDialog.show(
										"Unable to Delete Survey", 
										"GIFT was unable to delete this survey. " 
										+ (result.getErrorMsg() != null ? result.getErrorMsg() : "An unknown problem prevented the deletion.")
										+ "<br/><br/>"
										+ "Do you still want to remove this survey reference without deleting the survey?", 
										"Yes, Remove Reference", 
										new OkayCancelCallback() {
									
									@Override
									public void okay() {
										setValue(null, true);
									}
									
									@Override
									public void cancel() {
										//Nothing to do
									}
								});
							}
						}		
					}
				});
				
    		} else {
    			
    			WarningDialog.error("Unable to Delete Survey", 
    					"GIFT was unable to delete this survey because this course's survey resources were not loaded properly."
    					+ "<br/><br/>"
    					+ "Please try reloading this course to see if the problem persists. "
    			);
    		}
    	}
    }
    
    /**
     * Sets survey context ID this widget should use when the user attempts to delete a survey
     * 
     * @param contextId the survey context ID
     */
    public void setSurveyContextId(BigInteger contextId){
		currentSurveyContextId = contextId;
	}

    /**
     * Saves the course in the background so it no longer references
     * a deleted object if the user cancels the editor instead of saving
     */
    private static void saveCourse() {
        saveCourse(GatClientUtility.getBaseEditorWindow());
    }
    
    /**
     * Saves the course in the background so it no longer references
     * a deleted object if the user cancels the editor instead of saving
     */
    private native static void saveCourse(JavaScriptObject window)/*-{
    
        if(window.saveCourse != null){
            window.saveCourse();
        } 
    
    }-*/;
    
    /**
     * Sets the type of survey they the author should be allowed to author. If set to null, the author will be able to 
     * author any type of survey.
     * 
     * @param type the type of survey that the author should be allowed to author
     */
    public void setTargetSurveyType(SurveyTypeEnum type) {
        this.targetSurveyType = type;
        selectSurveyDialog.setTargetSurveyType(type);
    }
    
}
