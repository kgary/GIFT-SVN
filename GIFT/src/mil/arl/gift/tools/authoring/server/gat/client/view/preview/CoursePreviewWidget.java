/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.preview;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.NavbarBrand;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import generated.conversation.Conversation;
import generated.course.AAR;
import generated.course.BooleanEnum;
import generated.course.ConversationTreeFile;
import generated.course.Course;
import generated.course.Guidance;
import generated.course.LessonMaterial;
import generated.course.LessonMaterialList;
import generated.course.LtiProperties;
import generated.course.Media;
import generated.course.PresentSurvey;
import generated.course.SlideShowProperties;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.CourseValidationResults.CourseObjectValidationResults;
import mil.arl.gift.common.gwt.client.survey.SurveyWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.shared.MediaHtml;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.SurveyResponseMetadata;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.place.CoursePlace;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.util.PlaceParamParser;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview.ConversationWidget.ConversationUpdateCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview.PreviewPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.PreviewSurveyPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ConvertLessonMaterialFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ConvertLessonMaterialFilesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import mil.arl.gift.tutor.client.coursewidgets.CollectionWidget;
import mil.arl.gift.tutor.client.coursewidgets.ContentWidget;
import mil.arl.gift.tutor.client.coursewidgets.CourseHeaderWidget;
import mil.arl.gift.tutor.client.coursewidgets.SlideShowWidget;
import mil.arl.gift.tutor.client.coursewidgets.SurveyResponsesWidget;

/**
 * A widget that displays previews of course objects similar to the way they are presented into TUI
 */
public class CoursePreviewWidget extends Composite {


	interface CoursePreviewWidgetUiBinder extends UiBinder<Widget, CoursePreviewWidget> {
	}

	private static CoursePreviewWidgetUiBinder uiBinder = GWT.create(CoursePreviewWidgetUiBinder.class);

    /** The logger for the class */
	private static Logger logger = Logger.getLogger(CoursePreviewWidget.class.getName());

	/**
     * RPC service that is used to retrieve the surveys from the database
     */
    private final SurveyRpcServiceAsync surveyRpcService = GWT.create(SurveyRpcService.class);

    private final GatRpcServiceAsync rpcService = GWT.create(GatRpcService.class);

	@UiField
	protected DeckPanel previewDeck;

	@UiField
	protected Container loadPanel;

	@UiField
	protected Container tuiPanel;

	@UiField
	protected FlowPanel courseObjectPanel;

	@UiField
	protected AnchorButton ctrlUserInfo;

	@UiField
	protected AnchorListItem ctrlUserAction;

	@UiField
	protected Button ctrlStop;

	@UiField
	protected Span txtCourseName;

	@UiField
	protected BsLoadingIcon ctrlLoadIcon;
	
	/**
	 * where the header image will be placed
	 */
    @UiField
    NavbarBrand navBarHeader;
    
    /**
     * the nav bar header image
     */
    Image headerImage = null;

	@UiField (provided = true)
	protected CourseHeaderWidget headerWidget = new CourseHeaderWidget() {

    	@Override
		public void setHeaderTitle(String name) {
    		headerWidget.setName(name);
			headerWidget.show();
		}

    	@Override
    	public void handleContinue() {
    		showNextWidget();
    	}
    };

	private Course course;

	private Integer index = 0;

	private String courseFolderPath;
	
	/** The URL that can be used to reach media files inside the course folder */
	private String courseFolderUrl;

	private PreviewUnavailableWidget unavailableWidget;

	private CourseValidationResults validationResults = null;

	/** Accumulates responses to be shown in the next AAR */
	private List<SurveyResponse> surveyResponses = new ArrayList<SurveyResponse>();

	/** A dialog used to prompt the author to start the preview whenever they are ready */
	private ModalDialogBox startPreviewDialog = new ModalDialogBox();

	/**
	 * Constructor
	 */
	public CoursePreviewWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		
		logger.info("Creating course preview widget");

		// Get params from the url
		String url = Location.getHref();
		url = url.substring(url.indexOf(CoursePlace.PARAM_FILEPATH));

		HashMap<String, String> map = PlaceParamParser.getParams(url);
		final String filePath = URL.decode(map.get(CoursePlace.PARAM_FILEPATH));
		index = Integer.valueOf(map.get(GatClientUtility.PREVIEW_INDEX));
		String username = GatClientUtility.getUserName();

		unavailableWidget = new PreviewUnavailableWidget();

		//Nick - Need to URL decode the course path to remove special characters like %20
		courseFolderPath = filePath.substring(0,  filePath.lastIndexOf("/"));

		previewDeck.showWidget(previewDeck.getWidgetIndex(loadPanel));
		ctrlUserInfo.setText(username);
		ctrlLoadIcon.startLoading();

		ClickHandler closeHandler = new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				GatClientUtility.closePreviewWindow();
			}

		};

		ctrlStop.addClickHandler(closeHandler);
		ctrlUserAction.addClickHandler(closeHandler);

		startPreviewDialog.setText("Start Preview");
        startPreviewDialog.setWidget(new HTML("Please click 'Start Preview' whenever you are ready to begin the course preview."));
        startPreviewDialog.setGlassEnabled(true);
        startPreviewDialog.setModal(true);
        startPreviewDialog.setCloseable(true);
        startPreviewDialog.getCloseButton().setText("Start Preview");
        startPreviewDialog.setCallback(new ModalDialogCallback() {

            @Override
            public void onClose() {
                showCourseObject(index);
            }
        });

		rpcService.getCoursePreviewObject(username, filePath, new AsyncCallback<FetchJAXBObjectResult>(){

			@Override
			public void onFailure(Throwable cause) {
				ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
						"There was an error previewing the course.", cause.toString(), null);
				errorDialog.setText("Error");
				errorDialog.center();
			}

			@Override
			public void onSuccess(FetchJAXBObjectResult result) {
				if(!result.isSuccess() && result.getJAXBObject() == null) {

					headerWidget.setName("Error");
					showError("There are errors preventing this course from being previewed.<br/>"
							+ "For more information, use the Validate Course button in the Course Authoring Tool.");

				} else {
					course = (Course) result.getJAXBObject();
					txtCourseName.setText(course.getName());
					validationResults = result.getCourseValidationResults();

					//prompt the author to start the preview whenever they are ready
					startPreviewDialog.center();
					startPreviewDialog.setPopupPosition(startPreviewDialog.getAbsoluteLeft(), 70);
				}
            }
        });

		/* Get a URL that can be used to reach files inside the course folder. This is needed to allow the survey
         * composer to reach media files in the course folder. */
		rpcService.getAssociatedCourseImage(GatClientUtility.getUserName(), courseFolderPath, new AsyncCallback<FetchContentAddressResult>() {
            
            @Override
            public void onSuccess(FetchContentAddressResult result) {
                courseFolderUrl = result.getContentURL();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                logger.severe("Failed to get a URL for the course folder. This may affect previewing some course resources. " + caught);
            }
        });
	}
	
	/**
	 * Set the background to the image provided.
	 * 
	 * @param backgroundUrl shouldn't be null or empty
	 */
	public void setBackground(String backgroundUrl){
        tuiPanel.getElement().getStyle().setBackgroundImage("url('"+backgroundUrl+"')");
	}
	
	/**
	 * Set the nav bar system icon.
	 * @param systemIconUrl the URL the GAT can use to retrieve the system icon for the nav bar.  Shouldn't be null or empty.
	 */
	public void setSystemIcon(String systemIconUrl){
	    
	    if(headerImage == null){
            headerImage = new Image();
            headerImage.addStyleName("headerIconAdjustment");
            navBarHeader.add(headerImage);
	    }
	    
        headerImage.setUrl(systemIconUrl);
	}

	/**
	 * Displays the course object at the specified index.
	 *
	 * @param index The index of the course object to display. If null, the first course object will be displayed.
	 */
	private void showCourseObject(Integer index) {

		if(index == null) {
			index = 0;
		}

		if(course == null || index >= course.getTransitions().getTransitionType().size()) {
			GatClientUtility.closePreviewWindow();

		} else {

			headerWidget.hide();
			headerWidget.showWarning(false);
			Serializable transition = course.getTransitions().getTransitionType().get(index);

			CourseObjectValidationResults results = null;
			if(validationResults != null) {
				results = validationResults.getCourseObjectResults().get(index);
			}


			if (CourseElementUtil.isTransitionDisabled(transition)) {
			    showNextWidget();
            } else if (results != null && results.getValidationResults() != null &&
                    (results.getValidationResults().hasCriticalIssue() || results.getValidationResults().hasImportantIssues())) {
                try {
					headerWidget.setName(CourseElementUtil.getTransitionName(transition));
				} catch  (Exception e) {
					logger.log(Level.SEVERE, "There was a problem setting the course object header. Failed to retrieve the course object name.", e);
				}
				showError("There are errors preventing this course object from being previewed.<br/>"
						+ "For more information, use the Validate Course button in the Course Authoring Tool.");

			} else if(transition instanceof LessonMaterial) {
				handleLessonMaterial(transition);
				showHeader(true);

			} else if(transition instanceof Guidance) {
				handleGuidance(transition);

			} else if (transition instanceof PresentSurvey) {
				handleSurvey(transition);

			} else if (transition instanceof AAR) {
				handleAAR(transition);

			} else {
				headerWidget.setName(CourseElementUtil.getTransitionName(transition));
				showUnavailable();
			}

		}
	}

	/**
	 * Displays the widget with the tutor background
	 *
	 * @param widget The widget to display
	 * @param infoMessage the message to display. Can be null.
	 */
	private void showWidget(Widget widget, final String infoMessage) {
		courseObjectPanel.clear();
		courseObjectPanel.add(widget);
		previewDeck.showWidget(previewDeck.getWidgetIndex(tuiPanel));
		headerWidget.showLoading(false);
		headerWidget.setContinueButtonEnabled(true);

		if(infoMessage != null && !infoMessage.isEmpty()) {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {

				@Override
				public void execute() {
					headerWidget.setInfoMessage(infoMessage, true);
				}
			});
		}
	}

	/**
	 * Displays the widget with the tutor background
	 *
	 * @param widget The widget to display
	 */
	private void showWidget(Widget widget) {
		showWidget(widget, null);
	}

	/**
	 * Presents a Lesson Material course object
	 *
	 * @param transition The Lesson Material course object
	 */
	private void handleLessonMaterial(Serializable transition) {

		final LessonMaterial lm = (LessonMaterial) transition;

		if(lm.getLessonMaterialFiles() != null && lm.getLessonMaterialFiles().getFile() != null
				&& !lm.getLessonMaterialFiles().getFile().isEmpty()) {
			// This is an older course with lessonMaterial.xml file references

			ConvertLessonMaterialFiles action = new ConvertLessonMaterialFiles(
					GatClientUtility.getUserName(),
					lm.getLessonMaterialFiles(),
					courseFolderPath
			);

			SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<ConvertLessonMaterialFilesResult>() {

				@Override
				public void onFailure(Throwable thrown) {

					List<String> stackTrace = new ArrayList<String>();

					if(thrown.getStackTrace() != null){
						for(StackTraceElement e : thrown.getStackTrace()){
							stackTrace.add(e.toString());
						}
					}

					StringBuilder sb = new StringBuilder();
					sb.append("An error occurred while extracting media from the following lesson material files:");
					sb.append("<ul>");

					for(String file : lm.getLessonMaterialFiles().getFile()){
						sb.append("<li><b>");
						sb.append(file);
						sb.append("</li></b>");
					}

					sb.append("As a result of this error, this Lesson Material course object cannot be previewed.");

					ErrorDetailsDialog detailsDialog = new ErrorDetailsDialog(sb.toString(), thrown.toString(), stackTrace);

					detailsDialog.setDialogTitle("Failed to Retrieve Lesson Material Files");
					detailsDialog.center();

					showWidget(new PreviewUnavailableWidget("There was a problem prevewing the Lesson Material."));
				}

				@Override
				public void onSuccess(ConvertLessonMaterialFilesResult result) {

					if(result.isSuccess()){

						LessonMaterialList convertedList = result.getList();

						if(convertedList != null){

							if(lm.getLessonMaterialList() != null){
								lm.getLessonMaterialList().getMedia().addAll(convertedList.getMedia());

							} else {
								lm.setLessonMaterialList(convertedList);
							}

							lm.setLessonMaterialFiles(null);
							handleLessonMaterial(lm);

						} else {

							showWidget(new PreviewUnavailableWidget("There was a problem prevewing the Lesson Material: "
									+ "The Lesson Material Files list was empty."));

						}

					} else {

						if(result.getErrorDetails() != null){

							StringBuilder sb = new StringBuilder();
							sb.append("An error occurred while retreiving media from the following Lesson Material files:");
							sb.append("<ul>");

							for(String file : lm.getLessonMaterialFiles().getFile()){
								sb.append("<li><b>");
								sb.append(file);
								sb.append("</li></b>");
							}

							sb.append("As a result of this error, this Lesson Material course object cannot be previewed.");
							ErrorDetailsDialog detailsDialog = new ErrorDetailsDialog(sb.toString(), result.getErrorDetails(), result.getErrorStackTrace());

							detailsDialog.setDialogTitle("Failed to Retrieve Lesson Material Files");
							detailsDialog.center();
						}

						showWidget(new PreviewUnavailableWidget("There was a problem previewing the Lesson Material."));
					}
				}
			});

		} else if(lm.getLessonMaterialList().getIsCollection() == BooleanEnum.FALSE) {
			// Show a single media item

			final Media media = lm.getLessonMaterialList().getMedia().get(0);

			if(media.getMediaTypeProperties() instanceof SlideShowProperties) {
				headerWidget.showLoading(true);

				rpcService.getSlideShowUrls((SlideShowProperties) media.getMediaTypeProperties(), courseFolderPath,
						GatClientUtility.getUserName(), new AsyncCallback<SlideShowProperties>() {

					@Override
					public void onFailure(Throwable cause) {
						ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
								"There was a problem retrieivng the slides for this slide show.",
								cause.toString(), null);
						errorDialog.setTitle("Error");
						errorDialog.center();

						showWidget(new PreviewUnavailableWidget("There was a problem previewing this Slide Show."));
					}

					@Override
					public void onSuccess(SlideShowProperties properties) {
						SlideShowWidget widget = new SlideShowWidget();
						widget.setProperties(properties);
						showWidget(widget);
					}

				});

			} else if(media.getMediaTypeProperties() instanceof LtiProperties) {
			    headerWidget.setName(CourseElementUtil.getTransitionName(transition));
                showUnavailable();
			} else {

				final ContentWidget widget = new ContentWidget();
				headerWidget.showLoading(true);

				getContentUrl(media.getUri(), media.getMediaTypeProperties(), new AsyncCallback<FetchContentAddressResult>() {

					@Override
					public void onFailure(Throwable cause) {
						widget.setContentUrl(media.getUri(), media.getMediaTypeProperties());
						showWidget(widget, media.getMessage());
					}

					@Override
					public void onSuccess(FetchContentAddressResult result) {

					    if(result.violatesSOP()) {
					        widget.setLinkUrl(result.getContentURL(), media.getName());

					    } else {
					        widget.setContentUrl(result.getContentURL(), media.getMediaTypeProperties());
					    }

						showWidget(widget, media.getMessage());
					}
				});
			}

			headerWidget.setName(media.getName());

		} else {
			// Show a media collection

			headerWidget.showLoading(true);
			rpcService.getMediaHtmlList(lm.getLessonMaterialList().getMedia(), GatClientUtility.getUserName(),
					courseFolderPath, new AsyncCallback<List<MediaHtml>> () {

				@Override
				public void onFailure(Throwable cause) {
					ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
							"There was a problem retrieivng the content in this Media Collection.",
							cause.toString(), null);
					errorDialog.setTitle("Error");
					errorDialog.center();

					showWidget(new PreviewUnavailableWidget("There was a problem previewing this Media Collection."));
				}

				@Override
				public void onSuccess(List<MediaHtml> mediaList) {
					CollectionWidget widget = new CollectionWidget(mediaList);
					showWidget(widget);
				}

			});
		}
	}

	/**
	 * Presents a Guidance course object
	 *
	 * @param transition The Guidance course object
	 */
	private void handleGuidance(Serializable transition) {

		final Guidance guidance = (Guidance) transition;
		Serializable choice = guidance.getGuidanceChoice();
		final ContentWidget widget = new ContentWidget();

		if(choice instanceof Guidance.Message) {
			widget.setMessageHTML(((Guidance.Message) choice).getContent());
			showWidget(widget);

		} else if (choice instanceof Guidance.URL) {

		    final String url = ((Guidance.URL) choice).getAddress();
		    getContentUrl(url, null, new AsyncCallback<FetchContentAddressResult>() {

                @Override
                public void onFailure(Throwable cause) {
                    widget.setContentUrl(url, null);
                    showWidget(widget);
                }

                @Override
                public void onSuccess(FetchContentAddressResult result) {

                    if(result.violatesSOP()) {
                        widget.setLinkUrl(result.getContentURL(), guidance.getTransitionName());

                    } else {
                        widget.setContentUrl(result.getContentURL(), null);
                    }

                    showWidget(widget);
                }
            });

		} else if (choice instanceof Guidance.File) {
			final Guidance.File file = (Guidance.File) choice;
			final String url = file.getHTML();
			headerWidget.showLoading(true);

			getContentUrl(url, null, new AsyncCallback<FetchContentAddressResult>() {

				@Override
				public void onFailure(Throwable cause) {
					widget.setContentUrl(url, null);
					showWidget(widget, file.getMessage());
				}

				@Override
				public void onSuccess(FetchContentAddressResult result) {
					widget.setContentUrl(result.getContentURL(), null);
					showWidget(widget, file.getMessage());
				}
			});
		}

		if(guidance.getDisplayTime() != null && guidance.getDisplayTime().compareTo(BigDecimal.ZERO) == 1) {
			Timer timer = new Timer() {
				@Override
				public void run() {
					showNextWidget();
				}
			};

			timer.schedule((int)(guidance.getDisplayTime().doubleValue() * 1000));

		} else {
			headerWidget.setName(guidance.getTransitionName());
			showHeader(true);
		}
	}

	/**
	 * Presents a PresentSurvey course object
	 *
	 * @param transition The Survey course pbject
	 */
	private void handleSurvey(final Serializable transition) {

		Object choice = ((PresentSurvey) transition).getSurveyChoice();

		if(choice instanceof String) {

			headerWidget.showLoading(true);
			surveyRpcService.getSurveyFromContextKey((String) choice, course.getSurveyContext().intValue(), new AsyncCallback<Survey>() {

                @Override
				public void onFailure(Throwable cause) {
					ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
							"There was a problem retrieivng the survey.",
							cause.toString(), null);
					errorDialog.setTitle("Error");
					errorDialog.center();

					headerWidget.setName(((PresentSurvey) transition).getTransitionName());
					showWidget(new PreviewUnavailableWidget("There was a problem previewing this Survey."));
					showHeader(true);
				}

				@Override
				public void onSuccess(Survey survey) {
					showHeader(false);
					
					/* Let the survey widget access media in the course folder */
					survey.applySurveyMediaHost(courseFolderUrl);
					
					SurveyWidget widget = new SurveyWidget(survey, false, GatClientUtility.isDebug());

					widget.addCloseHandler(new CloseHandler<SurveyResponse>() {

						@Override
						public void onClose(CloseEvent<SurveyResponse> event) {
							surveyResponses.add(event.getTarget());
							showNextWidget();
						}
					});

					boolean fullScreen = BooleanEnum.TRUE.equals(((PresentSurvey) transition).getFullScreen());

					showWidget(new PreviewSurveyPanel(widget, fullScreen, GatClientUtility.isDebug()));
				}
			});


		} else if (choice instanceof PresentSurvey.ConceptSurvey) {
			surveyRpcService.getConceptSurvey(course.getSurveyContext().intValue(), (PresentSurvey.ConceptSurvey) choice, new AsyncCallback<Survey>() {

				@Override
				public void onFailure(Throwable cause) {
					ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
							"There was a problem retrieivng the survey.",
							cause.toString(), null);
					errorDialog.setTitle("Error");
					errorDialog.center();

					headerWidget.setName(((PresentSurvey) transition).getTransitionName());
					showWidget(new PreviewUnavailableWidget("There was a problem previewing this Question Bank."));
					showHeader(true);
				}

				@Override
				public void onSuccess(Survey survey) {
					showHeader(false);
					SurveyWidget widget = new SurveyWidget(survey, false, GatClientUtility.isDebug());

					widget.addCloseHandler(new CloseHandler<SurveyResponse>() {

						@Override
						public void onClose(CloseEvent<SurveyResponse> event) {
						    surveyResponses.add(event.getTarget());
                            showNextWidget();
						}
					});
					showWidget(widget);
				}

			});

		} else if (choice instanceof generated.course.Conversation) {
			generated.course.Conversation conversation = (generated.course.Conversation) choice;

			if(conversation.getType() instanceof ConversationTreeFile) {
				ConversationTreeFile file = (ConversationTreeFile) conversation.getType();
				handleConversation(file.getName(), (PresentSurvey) transition);

			} else {
				headerWidget.setName(((PresentSurvey) transition).getTransitionName());
				showUnavailable();
			}
		}
	}

	/**
	 * Presents a Conversation course object
	 *
	 * @param conversationFilePath The path to the conversation file
	 * @param transition The PresentSurvey transition that contains the name of this conversation
	 */
	private void handleConversation(String conversationFilePath, final PresentSurvey transition) {

		String relativePath = courseFolderPath + "/" + conversationFilePath;

		rpcService.getJAXBObject(GatClientUtility.getUserName(), relativePath, false, new AsyncCallback<FetchJAXBObjectResult>() {

			@Override
			public void onFailure(Throwable cause) {
				ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
						"There was a problem retrieving the conversation.",
						cause.toString(), null);
				errorDialog.setTitle("Error");
				errorDialog.center();

				headerWidget.setName(transition.getTransitionName());
				showWidget(new PreviewUnavailableWidget("There was a problem previewing this Conversation."));
				showHeader(true);
			}

			@Override
			public void onSuccess(FetchJAXBObjectResult result) {
				if(!result.isSuccess()) {
					ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
							"There was a problem retrieving the conversation: " + result.getErrorMsg(),
							result.getErrorDetails(), result.getErrorStackTrace());
					errorDialog.setTitle("Error");
					errorDialog.center();

					headerWidget.setName(transition.getTransitionName());
					showWidget(new PreviewUnavailableWidget("There was a problem previewing this Conversation."));
					showHeader(true);

				} else {

					showHeader(false);
					final Conversation conversation = (Conversation) result.getJAXBObject();
					final PreviewPanel chatPreview = new PreviewPanel();
					if(GatClientUtility.getServerProperties() != null){
					    chatPreview.setBackground(GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.BACKGROUND_IMAGE));
					}

                    chatPreview.addContinueClickHandler(createContinueButtonHandler());
					chatPreview.setDetails(transition.getTransitionName(), conversation.getLearnersDescription());
					chatPreview.setPreviewSubmitTextCallback(new ConversationUpdateCallback() {

						@Override
						public void getUpdate(UpdateConversation action) {
							rpcService.updateConversation(action.getChatId(), conversation, action.getUserText(), new AsyncCallback<UpdateConversationResult>() {

								@Override
								public void onFailure(Throwable cause) {
									ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
											"There was a problem getting the next conversation update.",
											cause.toString(), null);
									chatPreview.addContinueClickHandler(createContinueButtonHandler());
									errorDialog.setText("Error");
									errorDialog.center();

									chatPreview.addContinueClickHandler(createContinueButtonHandler());
									chatPreview.showContinueButton();
								}

								@Override
								public void onSuccess(UpdateConversationResult result) {

									if(!result.isSuccess()) {
										ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
												"There was a problem getting the next conversation update: " + result.getErrorMsg(),
												result.getErrorDetails(),
												result.getErrorStackTrace());
										chatPreview.addContinueClickHandler(createContinueButtonHandler());
										chatPreview.showContinueButton();
										errorDialog.setText("Error");
										errorDialog.center();

										chatPreview.addContinueClickHandler(createContinueButtonHandler());

									} else {
										chatPreview.updateConversation(result);
									}
								}

							});
						}

					});

					rpcService.startConversation(conversation, new AsyncCallback<UpdateConversationResult>() {

						@Override
						public void onFailure(Throwable cause) {
							ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
									"There was a problem starting the conversation.",
									cause.toString(), null);
							chatPreview.addContinueClickHandler(createContinueButtonHandler());
							errorDialog.setText("Error");
							errorDialog.center();

							chatPreview.addContinueClickHandler(createContinueButtonHandler());
							chatPreview.showContinueButton();
						}

						@Override
						public void onSuccess(UpdateConversationResult result) {

							if(!result.isSuccess()) {
								ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(
										"There was a problem starting the conversation: " + result.getErrorMsg(),
										result.getErrorDetails(),
										result.getErrorStackTrace());
								chatPreview.addContinueClickHandler(createContinueButtonHandler());
								chatPreview.showContinueButton();
								errorDialog.setText("Error");
								errorDialog.center();

							} else {
								chatPreview.setPreviewChatId(result.getChatId());
								chatPreview.updateConversation(result);
							}
						}

					});

					chatPreview.setFullScreen(BooleanEnum.TRUE.equals(transition.getFullScreen()));
					showWidget(chatPreview.getChatPanel());
				}
			}
		});
	}

	/**
	 * Presents an AAR course object
	 *
	 * @param transition The AAR course object
	 */
	private void handleAAR(Serializable transition) {

	    Widget widget;

	    if(surveyResponses.isEmpty()) {
	        String msg = "<span style=\"font-family: \"Open Sans\", \"Helvetica Neue\", Helvetica, Arial, sans-serif\"; font-size: 14pt;\">" +
	                     "The course author wanted to present a review of your performance at this time,<br/>" +
	                     "however there is nothing to review.<br/><br/>If possible, after the course has completed, " +
	                     "please make your instructor or GIFT administrator aware that this message appeared.<br/><br/>" +
	                     "Press continue to move forward in the course.</span>";
	        widget = new ContentWidget();
	        ((ContentWidget) widget).setMessageHTML(msg);

	    } else {
    	    widget = new SurveyResponsesWidget();

    		for(final SurveyResponse response : surveyResponses) {
    		    Survey survey = Survey.createFromResponse(response);
    		    ((SurveyResponsesWidget) widget).addSurveyResponse(null, new SurveyResponseMetadata(response), survey);
    		}

    		surveyResponses.clear();
	    }

		headerWidget.setName(((AAR) transition).getTransitionName());
		showWidget(widget);
		showHeader(true);
	}

	/**
	 * Creates a continue button widget that shows the next widget when clicked
	 *
	 * @return a continue button widget
	 */
    private ClickHandler createContinueButtonHandler() {
        return new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(index < course.getTransitions().getTransitionType().size()) {
					showNextWidget();
				} else {
					GatClientUtility.closePreviewWindow();
                }
			}

        };
	}

	/**
	 * Shows an unavailable message
	 */
	private void showUnavailable() {
		showHeader(true);
		showWidget(unavailableWidget);
	}

	/**
     * Shows an error message
     *
     * @param message The error message to show.
     */
	private void showError(String message) {
		showHeader(true);
		headerWidget.showWarning(true);

		PreviewUnavailableWidget widget = new PreviewUnavailableWidget(message);
		showWidget(widget);
	}

	/**
	 * Shows or hides the course header widget
	 *
	 * @param show True to hide the widget, false otherwise
	 */
	private void showHeader(boolean show) {
		if(show) {
			headerWidget.show();
		} else {
			headerWidget.hide();
		}
	}

	/**
	 * Gets the url for content so that it can be rendered correctly in the preview iframe
	 *
	 * @param url The original url
	 * @param mediaProperties the media type properties. Can be null.
	 * @param callback The callback for the result
	 */
	private void getContentUrl(final String url, Serializable mediaProperties, final AsyncCallback<FetchContentAddressResult> callback) {

		if(url == null || url.isEmpty()) {
			callback.onFailure(null);
			return;
		}

		rpcService.getContentUrl(GatClientUtility.getUserName(), url, courseFolderPath, mediaProperties, new AsyncCallback<FetchContentAddressResult>() {

			@Override
			public void onFailure(Throwable cause) {
				logger.warning("Failed to get content url: " + cause);
				callback.onFailure(cause);
			}

			@Override
			public void onSuccess(FetchContentAddressResult result) {

				String contentUrl;
				if(result.isSuccess()) {
					contentUrl = result.getContentURL();

				} else {

					if(!url.contains("://")) {
						contentUrl = "http://" + url;
					} else {
						contentUrl = url;
					}
				}
				result.setContentURL(contentUrl);
				callback.onSuccess(result);
			}
		});
	}

	/**
	 * Shows the next widget
	 */
	private void showNextWidget() {
		index += 1;
		headerWidget.setContinueButtonEnabled(false);
		showCourseObject(index);
	}

}
