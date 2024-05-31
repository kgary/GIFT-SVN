/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.presenter.course;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurEvent;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.binder.EventBinder;

import generated.course.BooleanEnum;
import generated.course.Guidance;
import generated.course.Guidance.File;
import generated.course.Guidance.Message;
import generated.course.Guidance.URL;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDisabledEvent;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.AbstractGatPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance.GuidanceView;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;

/**
 * A presenter used to populate data and handle user interaction in the guidance editor
 */
public class GuidancePresenter extends AbstractGatPresenter implements GuidanceView.Presenter{
	
	 /** The Constant logger. */
    private static final Logger logger = Logger.getLogger(GuidancePresenter.class.getName());
    
    /** The Constant EMPTY_VALUE_STRING. */
    private static final String EMPTY_VALUE_STRING = "";
    
    /**
     * Interface for the event binder for this class.  
     * @author cragusa
     */
    interface MyEventBinder extends EventBinder<GuidancePresenter> {
    }   
    
    /** Binder for handling events. */
    private static final MyEventBinder eventBinder = GWT
            .create(MyEventBinder.class);    
	
	/** The current guidance view. Should only be assigned to either mainGuidanceView or trainingAppGuidanceView. */
	private GuidanceView currentView = null;
	
	/** The {@link Guidance} currently being edited. */
	private Guidance currentGuidance;

	/** A command to be executed when the type of guidance is changed */
	private Command choiceSelectedCommand;
	
    /** The path to the course folder. */
    private String courseFolderPath;
	
	/**
	 * Instantiates a new guidance presenter.
	 */
	public GuidancePresenter(GuidanceView view){
		
		super();
		
		this.currentView = view;
		
		start();
		
		initView(view);
	}
	
	/**
     * Loads the given {@link Guidance} into the view for editing
     * 
     * @param guidance the guidance to edit
     */
    public void edit(Guidance guidance){
    	
    	this.currentGuidance = guidance;
    	
    	courseFolderPath = GatClientUtility.getBaseCourseFolderPath();
    	
    	populateView();
    }

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView.Presenter#start()
	 */
	/**
	 * Start.
	 */
	@Override
	public void start() {
		super.start();
		
		if(eventBus == null){
			eventBus = SharedResources.getInstance().getEventBus();
		}
		
    	eventRegistration = eventBinder.bindEventHandlers(this, eventBus); 
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView.Presenter#stop()
	 */
	/**
	 * Stop.
	 */
	@Override
	public void stop() {
		
		super.stop();
	}
	
	private void initView(final GuidanceView view){
		
		handlerRegistrations.add(view.getDisplayTimeInput().addValueChangeHandler(new ValueChangeHandler<String>(){

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {

				if(currentGuidance != null){
					
					if(event.getValue() != null && !event.getValue().isEmpty()) {						
						try {
						
							BigDecimal time = new BigDecimal(event.getValue());
						
							if(time.compareTo(BigDecimal.ZERO) < 0) {
								// if the user entered a negative time value, change it to zero
								
								time = BigDecimal.ZERO;
								view.getDisplayTimeInput().setValue(time.toString());
								
								WarningDialog.error("Invalid Display Time", "The display time of '" + event.getValue() + "' seconds is not allowed "
										+ "and has been changed to '0' seconds.<br/>You may enter other positive values instead." );						
							}
							
							currentGuidance.setDisplayTime(time);
							eventBus.fireEvent(new EditorDirtyEvent());
							
						} catch(@SuppressWarnings("unused") NumberFormatException e) {
							view.getDisplayTimeInput().setValue(null);
						}
						
					} else {
						
						currentGuidance.setDisplayTime(null);
						eventBus.fireEvent(new EditorDirtyEvent());
					}
				}
			}
			
		}));
		
		handlerRegistrations.add(view.getFullScreenInput().addValueChangeHandler(new ValueChangeHandler<Boolean>(){

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(currentGuidance != null){		
					
					currentGuidance.setFullScreen(event.getValue() != null && event.getValue()
							? BooleanEnum.TRUE
							: BooleanEnum.FALSE
					);
										
					eventBus.fireEvent(new EditorDirtyEvent());
				}
			}
			
		}));
		
		handlerRegistrations.add(view.getDisabledInput().addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentGuidance != null){        
                    
                    currentGuidance.setDisabled(event.getValue() != null && event.getValue()
                            ? BooleanEnum.TRUE
                            : BooleanEnum.FALSE
                    );
                                        
                    eventBus.fireEvent(new EditorDirtyEvent());
                                        
                    SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectDisabledEvent(currentGuidance));
                }
            }
            
        }));
		
		handlerRegistrations.add(view.getUseMessageContentInput().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
			    
			    logger.info("notified that guidance Message type was clicked");
				
				if(currentGuidance != null){
				    
				    logger.info("setting message type as guidance choice");
					
					Message message = new Message();
					message.setContent("Enter your message here!");
					
					currentGuidance.setGuidanceChoice(message);
					
					populateView();
					currentView.hideDisabledOption(true);
					
					if(choiceSelectedCommand != null){
						choiceSelectedCommand.execute();
					}
					
					eventBus.fireEvent(new EditorDirtyEvent());
				}
			}
		}));
		
		handlerRegistrations.add(view.getUseUrlInput().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(currentGuidance != null){				
					
					URL url = new URL();
					url.setAddress("http://www.example.com/");
					
					currentGuidance.setGuidanceChoice(url);
					
					populateView();
					
					if(choiceSelectedCommand != null){
						choiceSelectedCommand.execute();
					}
					
					eventBus.fireEvent(new EditorDirtyEvent());
				}
			}
		}));
		
		handlerRegistrations.add(view.getUseFileInput().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(currentGuidance != null){						
					
					File file = new File();
					
					currentGuidance.setGuidanceChoice(file);
					
					populateView();
					
					if(choiceSelectedCommand != null){
						choiceSelectedCommand.execute();
					}
					
					eventBus.fireEvent(new EditorDirtyEvent());
				}
			}
		}));
		
		view.addMessageContentInputBlurHandler(new SummernoteBlurHandler(){
			
			@Override
			public void onSummernoteBlur(SummernoteBlurEvent event) {
				
				if(currentGuidance != null 
						&& currentGuidance.getGuidanceChoice() != null
						&& currentGuidance.getGuidanceChoice() instanceof Message){
					
					((Message) currentGuidance.getGuidanceChoice()).setContent(view.getMessageContent());
					
					eventBus.fireEvent(new EditorDirtyEvent());
				}
			}
			
		});
		
		handlerRegistrations.add(view.getUrlAddressInput().addValueChangeHandler(new ValueChangeHandler<String>(){

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(currentGuidance != null
						&& currentGuidance.getGuidanceChoice() != null
						&& currentGuidance.getGuidanceChoice() instanceof URL){
					
					((URL) currentGuidance.getGuidanceChoice()).setAddress(event.getValue() != null && !event.getValue().isEmpty()
							? event.getValue()
							: null
					);
					
					eventBus.fireEvent(new EditorDirtyEvent());				
				}
			}
			
		}));
		
		handlerRegistrations.add(view.getUrlMessageInput().addSummernoteBlurHandler(new SummernoteBlurHandler(){

			@Override
			public void onSummernoteBlur(SummernoteBlurEvent event) {
				
				String message = view.getUrlMessageInput().getCode();
				
				if(currentGuidance != null
						&& currentGuidance.getGuidanceChoice() != null
						&& currentGuidance.getGuidanceChoice() instanceof URL){
					
					((URL) currentGuidance.getGuidanceChoice()).setMessage(message != null && !message.isEmpty()
							? message
							: null
					);
					
					eventBus.fireEvent(new EditorDirtyEvent());				
				}
			}
			
		}));
		
		handlerRegistrations.add(view.getUrlPreviewButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				
				String url = view.getUrlAddressInput().getValue();
				
				if(url == null || url.isEmpty()) {
					WarningDialog.error("URL Error", "Please provide a URL to preview.");
				} else {
					
					if(!url.startsWith("http")){
						url = "http://".concat(url);
					}
					
					Window.open(url, "_blank", "");
				}
			}
		}));
		
        handlerRegistrations.add(view.getFilePreviewButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                
                String courseFolderFile = view.getFileNameLabel().getText();
                
                if(courseFolderFile == null || courseFolderFile.isEmpty()) {
                    WarningDialog.error("File Not Provided", "Please provide a file to preview.");
                } else {
                    
                    String userName = GatClientUtility.getUserName();
                    final FetchContentAddress action = new FetchContentAddress(courseFolderPath, courseFolderFile, userName);

                    dispatchService.execute(action, new AsyncCallback<FetchContentAddressResult>() {

                        @Override
                        public void onFailure(Throwable cause) {
                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                    "Preview Failure", 
                                    cause.getMessage(), 
                                    DetailedException.getFullStackTrace(cause));    
                            dialog.setDialogTitle("Preview Failed");
                            dialog.center();
                        }

                        @Override
                        public void onSuccess(FetchContentAddressResult result) {
                            logger.info("Guidance file URL request = " + result);

                            if(result.isSuccess()) {
                                
                                String url = result.getContentURL();
                                if(!url.startsWith("http")){
                                    url = "http://".concat(url);
                                }
                                
                                Window.open(url, "_blank", "");
                            } else {
                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        "Preview Failure", 
                                        result.getErrorMsg() + " because " + result.getErrorDetails(), 
                                        null);    
                                dialog.setDialogTitle("Preview Failed");
                                dialog.center();
                            }

                        }           
                    });

                }
            }
        }));
		
		handlerRegistrations.add(view.getFileSelectionDialog().addValueChangeHandler(new ValueChangeHandler<String>(){

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {

				if(currentGuidance != null 
						&& currentGuidance.getGuidanceChoice() != null 
						&& currentGuidance.getGuidanceChoice() instanceof File){
					
					((File) currentGuidance.getGuidanceChoice()).setHTML(event.getValue() != null && !event.getValue().isEmpty()
							? event.getValue()
							: null
					);
					
					view.setGuidanceFileAttributes(event.getValue());
					
					eventBus.fireEvent(new EditorDirtyEvent());
				}
			}
			
		}));
		
		//handler for removing a guidance file
		handlerRegistrations.add(view.getRemoveFileInput().addClickHandler(new ClickHandler() {

	            @Override
	            public void onClick(ClickEvent event) {

	                 DeleteRemoveCancelDialog.show("Delete Content", 
	                        "Do you wish to <b>permanently delete</b> '"+view.getFileNameLabel().getText()+
	                        "' from the course or simply remove the reference to that content in this course object?<br><br>"+
	                                "Other course objects will be unable to use this content if it is deleted, which may cause validation issues if it is being referenced in other parts of the course.",
	                        new DeleteRemoveCancelCallback() {
	                    
	                            @Override
	                            public void cancel() {
	                                
	                            }

                                @Override
                                public void delete() {
                                    
                                  String username = GatClientUtility.getUserName();
                                  String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                  List<String> filesToDelete = new ArrayList<String>();
                                  final String filePath = courseFolderPath + "/" + view.getFileNameLabel().getText();
                                  filesToDelete.add(filePath);
                                  
                                  DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                                  SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                      @Override
                                      public void onFailure(Throwable error) {
                                          ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                  "Failed to delete the file.", 
                                                  error.getMessage(), 
                                                  DetailedException.getFullStackTrace(error));
                                          dialog.setDialogTitle("Deletion Failed");
                                          dialog.center();
                                      }

                                      @Override
                                      public void onSuccess(GatServiceResult result) {
                                          
                                          currentGuidance.setGuidanceChoice(new File());
                                          view.setGuidanceFileAttributes(null);
                                          if(result.isSuccess()){
                                              logger.warning("Successfully deleted the file '"+filePath+"'.");
                                          } else{
                                              logger.warning("Was unable to delete the file: " + filePath + "\nError Message: " + result.getErrorMsg());
                                          }
                                          
                                          //save the course since the content file was deleted and
                                          //a course undo operation would result in the reference being
                                          //brought back but the file would still not exist
                                          GatClientUtility.saveCourseAndNotify();
                                          eventBus.fireEvent(new EditorDirtyEvent());
                                      }
                                      
                                  });
                                }

                                @Override
                                public void remove() {
                                    currentGuidance.setGuidanceChoice(new File());
                                    view.setGuidanceFileAttributes(null);
                                    
                                    eventBus.fireEvent(new EditorDirtyEvent());
                                }

	                        });
	            }
	            
	        }));
		
		handlerRegistrations.add(view.getFileMessageInput().addSummernoteBlurHandler(new SummernoteBlurHandler(){

			@Override
			public void onSummernoteBlur(SummernoteBlurEvent event) {
				
				String message = view.getFileMessageInput().getCode();
				
				if(currentGuidance != null
						&& currentGuidance.getGuidanceChoice() != null
						&& currentGuidance.getGuidanceChoice() instanceof File){
					
					((File) currentGuidance.getGuidanceChoice()).setMessage(message != null && !message.isEmpty()
							? message
							: null
					);
					
					eventBus.fireEvent(new EditorDirtyEvent());				
				}
			}
		}));
		
		//TODO: Figure this out
//		trainingAppGuidanceView.getDisplayTimeTooltip().setHTML(
//				"Amount of time to display the guidance message while the training application loads. "
//				+ "<br/><br/>"
//				+ "Once the training application starts, the guidance will be removed even if the duration hasn't been reached. "
//				+ "<br/><br/>"
//				+ "If a value is not specified or the value is 0, the guidance will be displayed until the training application starts.");
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#getLogger()
	 */
	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	/**
	 * Populates the view.
	 */
	private void populateView(){
		
		clearView();
		
		if(currentGuidance != null){
			
			currentView.getDisplayTimeInput().setValue(currentGuidance.getDisplayTime() != null 
					? currentGuidance.getDisplayTime().toString() 
					: null
			);
			
			currentView.getFullScreenInput().setValue(currentGuidance.getFullScreen() != null
					? currentGuidance.getFullScreen().equals(BooleanEnum.TRUE)
					: true  //checked by default
			, true);
			
			currentView.getDisabledInput().setValue(currentGuidance.getDisabled() != null
                    ? currentGuidance.getDisabled().equals(BooleanEnum.TRUE)
                    : false  //not checked by default
            , true);
					
			if(currentGuidance.getGuidanceChoice() != null){
				
				if(currentGuidance.getGuidanceChoice() instanceof Message){
					
					currentView.showGuidanceMessageEditor();
					
					Message message = (Message) currentGuidance.getGuidanceChoice();
					
					currentView.setMessageContent(message.getContent() != null
							? message.getContent()
							: EMPTY_VALUE_STRING
                    );

                } else if (currentGuidance.getGuidanceChoice() instanceof URL){
					
					currentView.showGuidanceUrlEditor();
					
					URL url = (URL) currentGuidance.getGuidanceChoice();
					
					currentView.getUrlAddressInput().setValue(url.getAddress() != null
							? url.getAddress()
							: null
					);
					
					currentView.getUrlMessageInput().setCode(url.getMessage() != null
							? url.getMessage()
							: null
					);
					
					if(currentGuidance.getDisplayTime() == null) {
					    // #4636 - URL guidance is a legacy course object
					    // Only display the disable course object option, hide the others
					    currentView.hideDisplayFullScreenOption(true);
					    currentView.hideDisplayTimePanel(true);
					}
					
				} else if(currentGuidance.getGuidanceChoice() instanceof File){
					
					File file  = (File) currentGuidance.getGuidanceChoice();
					String value = file.getHTML();					
					
					currentView.setGuidanceFileAttributes(value);

                    currentView.showGuidanceFileEditor();
				
                    currentView.getFileMessageInput().setCode(file.getMessage() != null
							? file.getMessage()
							: null
                    );
                    
                    if(currentGuidance.getDisplayTime() == null) {
                        // #4636 - File guidance is a legacy course object
                        // Only display the disable course object option, hide the others
                        currentView.hideDisplayFullScreenOption(true);
                        currentView.hideDisplayTimePanel(true);
        			}
                    
				} else {
					logger.warning("Tried to populate guidance choice editor, but the choice specified is unrecognized- "+currentGuidance.getGuidanceChoice());
				}
			
			} else {

				//show the choice panel so the user can decide what type of guidance to use
				currentView.showChoicePanel();
			}		    
		}
	}
	
	/**
	 * Clears the view.
	 */
	private void clearView() {
		
		currentView.getDisplayTimeInput().setValue(null);
		currentView.getFullScreenInput().setValue(null);
		currentView.getDisabledInput().setValue(null);
		
		currentView.setMessageContent(EMPTY_VALUE_STRING);
		
		currentView.getUrlAddressInput().setValue(null);
		currentView.getUrlMessageInput().setCode(null);
		
		currentView.getFileSelectionDialog().setValue(null);
	}

	/**
	 * Assigns a listener that will be notified when the user selects a different type of guidance
	 * 
	 * @param command the listener command
	 */
	public void setChoiceSelectionListener(Command command) {
		this.choiceSelectedCommand = command;
	}
	
	/**
	 * Show or hide the informative message editor.
	 * 
	 * @param hide True to hide the informative message editor.
	 */
	public void hideInfoMessage(boolean hide) {
		currentView.hideInfoMessage(hide);
	}
}
