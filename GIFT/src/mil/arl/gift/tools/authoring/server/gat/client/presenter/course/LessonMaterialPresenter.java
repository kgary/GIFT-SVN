/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.presenter.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.course.BooleanEnum;
import generated.course.Course;
import generated.course.ImageProperties;
import generated.course.LessonMaterial;
import generated.course.LessonMaterialList;
import generated.course.LtiProperties;
import generated.course.LtiProvider;
import generated.course.Media;
import generated.course.SlideShowProperties;
import generated.course.YoutubeVideoProperties;
import mil.arl.gift.common.GenericDataProvider;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseConceptsChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseLtiProvidersChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDisabledEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.AbstractGatPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.lm.AddMediaDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.lm.LessonMaterialView;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.lm.LessonMaterialViewImpl;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.FilePath;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ConvertLessonMaterialFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ConvertLessonMaterialFilesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.WorkspaceFileExists;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.WorkspaceFilesExist;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchDomainContentServerAddress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchDomainContentServerAddressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.WorkspaceFilesExistResult;

/**
 * A presenter used to populate data and handle user interaction in the lesson material editor
 */
public class LessonMaterialPresenter extends AbstractGatPresenter implements LessonMaterialView.Presenter{
    
    /** The Constant logger. */
    private static final Logger logger = Logger.getLogger(LessonMaterialPresenter.class.getName());   
    
    /**
     * Interface for the event binder for this class.
     * 
     * @author sharrison
     */
    interface MyEventBinder extends EventBinder<LessonMaterialPresenter> {
    }

    /** Binder for handling events. */
    private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
    
    /** The base URL to build the full preview URL from */
    private static final String BASE_PREVIEW_URL = "LessonMaterialPreview.html";
    
    /** The full preview URL including the GAT host from the server */
    private static String previewUrl = BASE_PREVIEW_URL;
    
    /**
     * The view used to handle user input and display data back to the user
     */
    private LessonMaterialView view;
    
    /**
     * The {@link LessonMaterial} currently being edited
     */
    private LessonMaterial currentLessonMaterial;
    
    /** The media list data provider. */
    private ListDataProvider<Media> mediaListDataProvider = new ListDataProvider<Media>(new ArrayList<Media>());
    
    /** Data provider for the Concepts table in the media dialogs */
    private ListDataProvider<CandidateConcept> mediaConceptsTableDataProvider = new ListDataProvider<CandidateConcept>();

    /** The list of LTI Providers */
    private GenericDataProvider<LtiProvider> contentLtiProvidersDataProvider = new GenericDataProvider<LtiProvider>();

    /** The path to the course folder. */
    private String courseFolderPath;
    
    /** The url to the course folder. */
    private String courseFolderUrl;
    
    /** The course currently being edited. Used to get the list of concepts. */
    private Course currentCourse = null;
    
    /** The domain content server url. */
    private String domainUrl;
    
    /** A dialog that asks the user how to preview the media uri. */
    private ModalDialogBox previewPrompt = new ModalDialogBox(); 
    
    /** The previewPrompt text. */
    private HTML promptText = new HTML();
    
    /** The help drop down panels for the preview dialog. */
    DisclosurePanel error1Panel = new DisclosurePanel(" HTTP ERROR: 404?");
	DisclosurePanel error2Panel = new DisclosurePanel(" Neither options work?");
	
	/** The media object to use in the preview dialog when it is shown*/
	private Media mediaToPreview = null;
	
	private Media mediaToEdit = null;
	
	private AddMediaDialog addMediaDialog = new AddMediaDialog();
	
	/** Whether or not the lesson material media type is a Slide Show. */
	private boolean isSlideShowType = false;
	
	/** Whether or not the lesson material media type is an LTI. */
    private boolean isLtiType = false;
	
	/**
     * Creates a new presenter managing the given view
     * 
     * @param view the view to be managed
     */
    public LessonMaterialPresenter(LessonMaterialView view){
    	
    	super();
        
        this.view = view;
        
        start();
        
        init();
    }
    
    /**
     * Loads the given {@link LessonMaterial} into the view for editing
     * 
     * @param lessonMaterial the lesson material to edit
     * @param course contains the currently authored contents of the course
     */
    public void edit(LessonMaterial lessonMaterial, Course course){
    	
    	this.currentLessonMaterial = lessonMaterial;
    	this.currentCourse = course;
    	
    	isSlideShowType = false;
    	isLtiType = false;
    	
    	if(lessonMaterial.getLessonMaterialList() != null 
    			&& lessonMaterial.getLessonMaterialList().getMedia() != null
    			&& !lessonMaterial.getLessonMaterialList().getMedia().isEmpty()) {

    		if(lessonMaterial.getLessonMaterialList().getMedia().get(0).getMediaTypeProperties() instanceof SlideShowProperties) {

    			isSlideShowType = true;
            } else if (lessonMaterial.getLessonMaterialList().getMedia().get(0).getMediaTypeProperties() instanceof LtiProperties) {

                isLtiType = true;
    		}
    	}

    	populateView();
    }
    
    /**
	 * Adds the media object.
	 */
	private void addMediaObject() {
		
		if(currentLessonMaterial != null){
			
			mediaToEdit = null;
			
			Media media = new Media();
			
			addMediaDialog.setValue(media);
			
			addMediaDialog.center();
		
		} else {
			logger.warning("Tried to add media to a lesson material transition, but no lesson material transition was found.");
		}
	}
	
	/**
	 * Previews the selected media object.
	 */
	private void previewMediaObject(Media media) {
		
		if(currentLessonMaterial != null && currentLessonMaterial.getLessonMaterialList() != null){
			
			if(media != null) {
				
				mediaToPreview = media;
				
				String url = media.getUri();
								
				if(url != null && !url.isEmpty()) {
					
					if(media.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
					
						// open the url in an iframe with the specified width
						generated.course.Size s = ((YoutubeVideoProperties) media.getMediaTypeProperties()).getSize();
						String width = (s == null) ? "81%" : s.getWidth().toString() + "px";
						String height = (s == null) ? "95%" : s.getWidth().toString() + "px";
						
						url = GatClientUtility.getEmbeddedYouTubeUrl(url);
						
						Window.open(previewUrl + "?url=" + URL.encodeQueryString(url) + "&height=" + height + "&width=" + width, "_blank", "");
						
					} else {
						
						promptText.setHTML(" Is <i>" + url + "</i>  a file in your course folder? Please "
								+ "indicate where this lesson material content is located so that GIFT can correctly "
								+ "render it.<br/><br/>Please note that during course execution, GIFT will "
								+ "automatically select the best way to render lesson material content.<br/><br/>");
						
						promptText.getElement().getStyle().setProperty("maxWidth", "655px");
						promptText.getElement().getStyle().setProperty("wordWrap", "break-word");
						
						error1Panel.setOpen(false);
						error2Panel.setOpen(false);
						previewPrompt.center();
					}
					
				} else {
					WarningDialog.warning("URL Error", "Please provide a URL to preview.");
				}
			}
					
		} else {
			logger.warning("Tried to delete media from a lesson material transition, but no lesson material list was found.");
		}
	}
    
    /**
	 * Clears the view, setting all fields to their initial state.
	 */
	private void clearView(){		
		mediaListDataProvider.getList().clear();
		view.getDisabledInput().setValue(null);
		
        // clear the media concepts table
        mediaConceptsTableDataProvider.getList().clear();
        mediaConceptsTableDataProvider.refresh();

        // clear the LTI providers' list
        contentLtiProvidersDataProvider.getList().clear();
        contentLtiProvidersDataProvider.refresh();
	}
    
    /**
	 * Delete media object.
	 */
	private void deleteMediaObject(Media media) {

		if(currentLessonMaterial != null && currentLessonMaterial.getLessonMaterialList() != null){
			
			currentLessonMaterial.getLessonMaterialList().getMedia().remove(media);
			
			if(currentLessonMaterial.getLessonMaterialList().getMedia().isEmpty()){
				currentLessonMaterial.setLessonMaterialList(null);
			}
			
			mediaListDataProvider.getList().remove(media);
			validateMediaElements(false);
			
			eventBus.fireEvent(new EditorDirtyEvent());
		
		} else {
			logger.warning("Tried to delete media from a lesson material transition, but no lesson material list was found.");
		}
	}
    
    @Override
    protected Logger getLogger() {
        return logger;
    }
	
    private void init(){

    	courseFolderPath = GatClientUtility.getBaseCourseFolderPath();
    	    	
    	mediaConceptsTableDataProvider.addDataDisplay(view.getMediaConceptsTable());
    	contentLtiProvidersDataProvider.createChild(view.getLtiProviderIdList());
    	    	
    	handlerRegistrations.add(view.getAddMediaInput().addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				addMediaObject();
			}
    		
    	}));
    	
    	handlerRegistrations.add(view.getDisabledInput().addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentLessonMaterial != null){        
                    
                    currentLessonMaterial.setDisabled(event.getValue() != null && event.getValue()
                            ? BooleanEnum.TRUE
                            : BooleanEnum.FALSE
                    );
                                        
                    eventBus.fireEvent(new EditorDirtyEvent());
                                        
                    SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectDisabledEvent(currentLessonMaterial));
                }
            }
            
        }));
    	
    	handlerRegistrations.add(view.getValidateMediaElementsButton().addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                validateMediaElements(true);
            }
    	    
    	}));
    	
    	view.getPreviewColumn().setFieldUpdater(new FieldUpdater<Media, String>() {
			
			@Override
			public void update(int index, Media record, String value) {
				previewMediaObject(record);
			}
		});
    	
    	view.getEditColumn().setFieldUpdater(new FieldUpdater<Media, String>() {
			
			@Override
			public void update(int index, Media record, String value) {
				
				mediaToEdit = record;
				
				addMediaDialog.setValue(record);
				
				addMediaDialog.center();
			}
		});
    	
    	view.getRemoveColumn().setFieldUpdater(new FieldUpdater<Media, String>() {
			
			@Override
			public void update(int index, Media record, String value) {
				deleteMediaObject(record);
			}
		});
    	
    	mediaListDataProvider.addDataDisplay(view.getMediaListDisplay());       	
    	mediaListDataProvider.refresh();
    	
    	SharedResources.getInstance().getDispatchService().execute(new FetchDomainContentServerAddress(), 
    			new AsyncCallback<FetchDomainContentServerAddressResult>() {

					@Override
					public void onFailure(Throwable t) {
						 logger.warning("FetchDomainContentServerAddressResult returned throwable error: " + t.getMessage());
					}

					@Override
					public void onSuccess(FetchDomainContentServerAddressResult result) {
						domainUrl = result.getDomainContentServerAddress();
				    	courseFolderUrl = domainUrl + "/workspace/" + courseFolderPath;
					}
    		
    	});
    	
    	handlerRegistrations.add(addMediaDialog.addValueChangeHandler(new ValueChangeHandler<Media>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Media> event) {
				
				if(mediaToEdit == null){
					
					if(currentLessonMaterial.getLessonMaterialList() == null){
						currentLessonMaterial.setLessonMaterialList(new LessonMaterialList());
					}
					
					Media media = event.getValue();
					
					currentLessonMaterial.getLessonMaterialList().getMedia().add(media);
					
					mediaListDataProvider.getList().add(media);
					mediaListDataProvider.refresh();
					
					eventBus.fireEvent(new EditorDirtyEvent());
					
				} else {
					
					Media media = event.getValue();
					
					int oldIndex = currentLessonMaterial.getLessonMaterialList().getMedia().indexOf(mediaToEdit);
					
					currentLessonMaterial.getLessonMaterialList().getMedia().remove(oldIndex);
					currentLessonMaterial.getLessonMaterialList().getMedia().add(oldIndex, media);
					
					int oldViewIndex = mediaListDataProvider.getList().indexOf(mediaToEdit);
					mediaListDataProvider.getList().remove(oldViewIndex);
					mediaListDataProvider.getList().add(oldViewIndex, media);
					mediaListDataProvider.refresh();
					
					eventBus.fireEvent(new EditorDirtyEvent());
				}
				
				mediaToEdit = null;
				
				populateView();
				
				addMediaDialog.hide();
			}
		}));
    	
    	// Style the preview prompt
    	
    	Button fileButton = new Button("In Course Folder");
    	Button webButton = new Button("Not in Course Folder");
    	Button cancelButton = new Button("Cancel");
    	Button helpButton = new Button("Having trouble?");
    	VerticalPanel wrapper = new VerticalPanel();
    	
    	HTML error1Html = new HTML();
    	HTML error2Html = new HTML();
    	
    	wrapper.add(promptText);
		wrapper.add(helpButton);
		wrapper.add(error1Panel);
		wrapper.add(error2Panel);
    	
    	previewPrompt.setWidget(wrapper);
    	previewPrompt.setFooterWidget(cancelButton);
		previewPrompt.setFooterWidget(webButton);
    	previewPrompt.setFooterWidget(fileButton);   	
    	previewPrompt.setText("Select a Preview Option");
    	previewPrompt.setCloseable(false);
    	previewPrompt.setGlassEnabled(true);
    	
		wrapper.setWidth("650px");
		cancelButton.setWidth("160px");
    	webButton.setWidth("160px");
    	fileButton.setWidth("160px");
    	promptText.addStyleName("dialogText");
    	error1Html.addStyleName("dialogText");
    	error2Html.addStyleName("dialogText");
    	error1Panel.getElement().getStyle().setProperty("marginBottom", "10px");
    	helpButton.getElement().getStyle().setProperty("padding", "0px 0px 5px 0px");
    	helpButton.getElement().getStyle().setProperty("fontFamily", "Arial");
    	
    	error1Panel.setContent(error1Html);
    	error2Panel.setContent(error2Html);
    	error1Panel.setVisible(false);
    	error2Panel.setVisible(false);
    	
    	error1Html.setHTML("If your preview resulted in an 'HTTP ERROR: 404', try the other button below.");
    	
    	error2Html.setHTML("If you are trying to preview an external website, verify that:"
    			+ "<ul><li>The \"Media URI\" you entered is correct.</li></ul>"
    			+ "If you are trying to preview a file in your workspace, verify that:"
    			+ "<ul><li>The file is located in this course folder</li>"
    			+ "<li>The file name is spelled correctly.</li></ul>");	
    	
    	helpButton.setType(ButtonType.LINK);
		webButton.setType(ButtonType.PRIMARY);
		fileButton.setType(ButtonType.SUCCESS);
		cancelButton.setType(ButtonType.DANGER);
    	
    	// Attach click handlers
    	
    	helpButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				error1Panel.setVisible(!error1Panel.isVisible());
				error2Panel.setVisible(!error2Panel.isVisible());
			}
			
		});
    	
    	fileButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {

				if(GatClientUtility.getServerProperties().getDeploymentMode() == DeploymentModeEnum.SERVER) {

					WarningDialog.info("Coming Soon!", "Previewing files in your course folder is currently not supported. "
							+ "However, you can preview resources that are hosted on other servers (e.g. youtube videos)."
							+ "<br/><br/>Note: This type of preview is supported in the downloadable version of GIFT "
							+ "running in Desktop mode.");
					
				} else if(mediaToPreview != null){
				
					final Media media = mediaToPreview;
					final String uri = media.getUri();
					
					//Checks to make sure the uri doesn't use any parent directory references
					if(uri.equals("..") ||
					        uri.endsWith("/..") ||
					        uri.startsWith("../") ||
					        uri.contains("/../")) {
					    WarningDialog.error("Invalid path", "The URI must reference a course within the course folder.\nThe uri cannot contain any use of the '..' operator.");
    	            }
					
					//Checks to make sure the specified file exists
					else {
    					WorkspaceFileExists action = new WorkspaceFileExists(GatClientUtility.getUserName(), 
    					        GatClientUtility.getBaseCourseFolderPath() + 
    					        Constants.FORWARD_SLASH + uri);
    					
    					SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){
    
                            @Override
                            public void onFailure(Throwable thrown) {
                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        thrown.getMessage(),
                                        "The server was unable to complete the check for the existence of the user defined uri", 
                                        DetailedException.getFullStackTrace(thrown)
                                );
                                
                                dialog.center();
                            }
    
                            @Override
                            public void onSuccess(GatServiceResult result) {
                                if(result.isSuccess()) {
                                    if(media.getMediaTypeProperties() instanceof ImageProperties) {
                                        
                                        Window.open(previewUrl + "?courseFolder=" + courseFolderUrl + "&url=" + URL.encode(media.getUri()) + "&image=true", "_blank", "");
                                        
                                    } else {
                                        Window.open(previewUrl + "?courseFolder=" + courseFolderUrl + "&url=" + URL.encode(media.getUri()), "_blank", "");
                                    }
                                } else {
                                    WarningDialog.error("Preview Failed", result.getErrorMsg());
                                }
                            }
    					    
    					});
					}
				} else {
					WarningDialog.error("Nothing to preview", "There is no lesson material selected to preview.");
				}
				previewPrompt.hide();
			}
    		
    	});
    	
    	webButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				
				if(mediaToPreview != null){

					Media media = mediaToPreview;
					String url = URL.encode(media.getUri());
					
					if(!url.contains("http")) {
						url = "http://" + url;
					}					
	                   
					if(media.getMediaTypeProperties() instanceof ImageProperties) {
						Window.open(previewUrl + "?url=" + url + "&image=true", "_blank", "");
					} else {
						Window.open(previewUrl + "?url=" + url, "_blank", "");
					}
					
					previewPrompt.hide();
				}
			}    		
    	});
    	
    	cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				previewPrompt.hide();
			}
		});
    	
    	previewUrl = GWT.getHostPageBaseURL() + BASE_PREVIEW_URL;
    }

    @EventHandler
    protected void onCourseConceptsChanged(CourseConceptsChangedEvent event){       
        
        //need to refresh the view whenever the course's concepts are changed
        populateView();
    }
    
    @EventHandler
    protected void onCourseLtiProvidersChanged(CourseLtiProvidersChangedEvent event){       

        currentCourse = event.getCourse();
        
        //need to refresh the view whenever the course's LTI providers are changed
        populateView();
    }
    
    /**
     * On course object renamed.
     *
     * @param event the event that contains the course object that was renamed
     */
    @EventHandler
    protected void onCourseObjectRenamedEvent(CourseObjectRenamedEvent event) {
        
        if (event != null && CourseElementUtil.isTransitionLessonMaterial(event.getCourseObject())) {
            
            //need to refresh the view whenever the lesson material transition name is changed
            populateView();
        }
    }
    
	/**
	 * Populates the view based on the current lesson material.
	 */
	private void populateView(){
		
		clearView();
		
		if(currentLessonMaterial != null){
			
            view.getDisabledInput()
                    .setValue(currentLessonMaterial.getDisabled() != null
                            ? currentLessonMaterial.getDisabled().equals(BooleanEnum.TRUE)
                            : false // not checked by default
                            , true);
		    
            if (isLtiType) {

                LtiProperties properties = (LtiProperties) currentLessonMaterial.getLessonMaterialList().getMedia().get(0).getMediaTypeProperties();
                List<String> concepts = CourseConceptsUtil.getConceptNameList(currentCourse.getConcepts());
                Collections.sort(concepts);

                List<String> selectedConcepts = new ArrayList<String>();
                if (properties.getLtiConcepts() != null) {
                    selectedConcepts.addAll(properties.getLtiConcepts().getConcepts());
                }

                // populate the concepts
                for (String conceptName : concepts) {
                    mediaConceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, selectedConcepts.contains(conceptName)));
                }

                mediaConceptsTableDataProvider.refresh();

                // populate the LTI providers' list
                List<LtiProvider> ltiProviders = GatClientUtility.getCourseLtiProviders();
                if (ltiProviders != null) {
                    contentLtiProvidersDataProvider.getList().addAll(ltiProviders);
                    contentLtiProvidersDataProvider.refresh();
                }

                // show media
                view.showMediaType(currentLessonMaterial);
            } else if (isSlideShowType || (currentLessonMaterial.getLessonMaterialList() != null
					&& currentLessonMaterial.getLessonMaterialList().getIsCollection() == BooleanEnum.FALSE)) {
				
                // show media
				view.showMediaType(currentLessonMaterial);	
				
			} else {
			
				view.showMediaCollection();
				
				if(currentLessonMaterial.getLessonMaterialList() != null && !currentLessonMaterial.getLessonMaterialList().getMedia().isEmpty()){				
				    List<Media> mediaList = currentLessonMaterial.getLessonMaterialList().getMedia();
			        mediaListDataProvider.getList().addAll(mediaList);
			        
			        WorkspaceFilesExist action = new WorkspaceFilesExist(GatClientUtility.getUserName(),
			                GatClientUtility.getBaseCourseFolderPath(), mediaList);
			        
			        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<WorkspaceFilesExistResult>() {

			            @Override
			            public void onFailure(Throwable thrown) {
			                if (logger.isLoggable(Level.SEVERE)) {
			                    String courseName = GatClientUtility.getCourseFolderName(GatClientUtility.getBaseCourseFolderPath());
			                    logger.severe("An exception occurred when trying to check if files exist in course '" + courseName + "':\n" + thrown.getMessage());
			                }
			            }

			            @Override
			            public void onSuccess(WorkspaceFilesExistResult result) {
			                if(logger.isLoggable(Level.INFO)) {
			                    logger.info("Successfully checked if files exist");
			                }
			                LessonMaterialViewImpl viewImpl = (LessonMaterialViewImpl) view;
			                
			                Map<FilePath, GatServiceResult> map = result.getFilesExistResults();
			                viewImpl.updateMediaListValidMap(map);
			                viewImpl.redraw();
			            }
			            
			        });
				}
				
				mediaListDataProvider.refresh();
				view.redraw();
				
				if(currentLessonMaterial.getLessonMaterialFiles() != null){
					
					BsLoadingDialogBox.display("Please Wait", "Converting lesson material files...");
					
					ConvertLessonMaterialFiles action = new ConvertLessonMaterialFiles(
							GatClientUtility.getUserName(), 
							currentLessonMaterial.getLessonMaterialFiles(), 
							courseFolderPath
					);
					
					dispatchService.execute(action, new AsyncCallback<ConvertLessonMaterialFilesResult>() {

						@Override
						public void onFailure(Throwable thrown) {
							
							BsLoadingDialogBox.remove();
							
							List<String> stackTrace = new ArrayList<String>();
							
							if(thrown.getStackTrace() != null){
								for(StackTraceElement e : thrown.getStackTrace()){
									stackTrace.add(e.toString());
								}
							}
							
							StringBuilder sb = new StringBuilder();
							sb.append("An error occurred while extracting media from the following lesson material files:");
							sb.append("<ul>");
							
							for(String file : currentLessonMaterial.getLessonMaterialFiles().getFile()){
								sb.append("<li><b>");
								sb.append(file);
								sb.append("</li></b>");
							}
							
							sb.append("As a result of this error, the media contained by these files cannot be displayed or edited through this interface.");
							sb.append("<br/><br/>");
							sb.append("You can still edit this transition and save any changes made to it; however, you will not be able to modify ");
							sb.append("or remove any of the media within these files.");
							
							ErrorDetailsDialog detailsDialog = new ErrorDetailsDialog(sb.toString(), thrown.toString(), stackTrace);
							
							detailsDialog.setDialogTitle("Failed to Convert Lesson Material Files");
							detailsDialog.center();
						}

						@Override
						public void onSuccess(ConvertLessonMaterialFilesResult result) {
							
							BsLoadingDialogBox.remove();
							
							if(result.isSuccess()){							
								
								LessonMaterialList convertedList = result.getList();
								
								if(convertedList != null){
									
									if(currentLessonMaterial.getLessonMaterialList() != null){
										currentLessonMaterial.getLessonMaterialList().getMedia().addAll(convertedList.getMedia());
										
									} else {
										currentLessonMaterial.setLessonMaterialList(convertedList);
									}
								}		
								
								StringBuilder sb = new StringBuilder();
								sb.append("The following lesson material files were discovered while opening this transition for editing:");
								sb.append("<ul>");
								
								for(String file : currentLessonMaterial.getLessonMaterialFiles().getFile()){
									sb.append("<li><b>");
									sb.append(file);
									sb.append("</li></b>");
								}
								
								sb.append("</ul>");
								sb.append("In order to allow the contents of these files to be edited through this interface, the media within these files has been extracted and placed in ");
								sb.append("a single lesson material list for authoring.<br/><br/>");
								sb.append("This will have no effect on course execution.<br/><br/>");
								sb.append("Note that, once this course is saved, the references to these lesson material files will be removed from this transition, ");
								sb.append("and the media within them will be added to the lesson material list. If no other lesson material transitions are referencing  ");
								sb.append("these lesson material files, you can remove the files from your course folder to create more file space.");
								
								WarningDialog.info("Lesson Material reference conversion", sb.toString());
								
								currentLessonMaterial.setLessonMaterialFiles(null);
								
								populateView();
							
							} else {
								
								if(result.getErrorDetails() != null){
									
									StringBuilder sb = new StringBuilder();
									sb.append("An error occurred while extracting media from the following lesson material files:");
									sb.append("<ul>");
									
									for(String file : currentLessonMaterial.getLessonMaterialFiles().getFile()){
										sb.append("<li><b>");
										sb.append(file);
										sb.append("</li></b>");
									}
									
									sb.append("As a result of this error, the media contained by these files cannot be displayed or edited through this interface.");
									sb.append("<br/><br/>");
									sb.append("You can still edit this transition and save any changes made to it; however, you will not be able to modify ");
									sb.append("or remove any of the media within these files.");
									
									ErrorDetailsDialog detailsDialog = new ErrorDetailsDialog(sb.toString(), result.getErrorDetails(), result.getErrorStackTrace());
									
									detailsDialog.setDialogTitle("Failed to Convert Lesson Material Reference Files");
									detailsDialog.center();
									
								} else {
									
									WarningDialog.alert("Failed to convert lesson material reference files", result.getErrorMsg());
								}
							}
						}
					});
				}				
			}
		} else {
			
			logger.warning("Tried to populate lesson material view, but no lesson material was provided.");
		}
    }
	
	/**
	 * Makes a call to the server to validate that each of the URIs specified by the users 
	 * reference valid resources. After the server call the table formatting is reevaluated to 
	 * delineate between valid and invalid resources
	 * @param notifyUser specifies whether or not to notify the user about the actions taking place
	 */
	private void validateMediaElements(final boolean notifyUser) {
		if(mediaListDataProvider.getList().size() == 0) {
			return;
		}
		final Notify notification = notifyUser ? 
				Notify.notify("Validating media for " + currentLessonMaterial.getTransitionName() + "...") : 
				null;
	    WorkspaceFilesExist action = new WorkspaceFilesExist(
	            GatClientUtility.getUserName(),
	            GatClientUtility.getBaseCourseFolderPath(),
	            mediaListDataProvider.getList());
	    
	    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<WorkspaceFilesExistResult>() {

            @Override
            public void onFailure(Throwable t) {
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                        "There was a problem validating the MediaCollection", 
                        t.getMessage(), 
                        DetailedException.getFullStackTrace(t));
                dialog.center();
            }

            @Override
            public void onSuccess(WorkspaceFilesExistResult result) {
                ((LessonMaterialViewImpl) view).updateMediaListValidMap(result.getFilesExistResults());
                mediaListDataProvider.refresh();
                if(notification != null && notifyUser) {
                	notification.hide();
                }
                Notify.notify("Validation for " + currentLessonMaterial.getTransitionName() + " completed");
            }
	        
	    });
	}
	
    @Override
    public void start(){
    	super.start();
    	
    	eventRegistration = eventBinder.bindEventHandlers(this, eventBus);
    }
    
    @Override
    public void stop(){
    	super.stop();
    }    
    
}
