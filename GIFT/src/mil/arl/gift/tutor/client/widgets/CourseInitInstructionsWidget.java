/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import mil.arl.gift.common.DisplayCourseInitInstructionsRequest;
import mil.arl.gift.common.DisplayCourseInitInstructionsRequest.GatewayStateEnum;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.shared.SubmitAction;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.properties.CourseInitInstructionsWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget used to display instructions required to properly initialize a user's Gateway before starting a course.
 * 
 * @author nroberts
 */
public class CourseInitInstructionsWidget extends Composite implements RequiresResize, IsUpdateableWidget {

	/**
	 * The UiBinder used to create the widget layout
	 */
	private static ConnectingToGatewayWidgetUiBinder uiBinder = GWT
			.create(ConnectingToGatewayWidgetUiBinder.class);

	/**
	 * Interface for the UiBinder used to create the widget layout
	 * 
	 * @author nroberts
	 */
	interface ConnectingToGatewayWidgetUiBinder extends
			UiBinder<Widget, CourseInitInstructionsWidget> {
	}
	
	private static Logger logger = Logger.getLogger(CourseInitInstructionsWidget.class.getName());
	
	/** Image URL for a green check mark */
	public static final String CHECK_MARK_IMAGE_URL = "images/clean.png";
	
	/** Image URL for a red X */
	public static final String RED_X_IMAGE_URL = "images/errorIcon.png";
	
	/** Image URL for a loading circle animation */
	public static final String LOADING_IMAGE_URL = "images/loading.gif";
	
	static{
		Image.prefetch(CHECK_MARK_IMAGE_URL);
		Image.prefetch(RED_X_IMAGE_URL);
		Image.prefetch(LOADING_IMAGE_URL);
	}

	@UiField
	protected Widget containerPanel;
	
	@UiField
	protected Label connectionStatusLabel;
	
	@UiField
	protected Image connectionStatusImage;
	
	@UiField
	protected Label configurationStatusLabel;
	
	@UiField
	protected Image configurationStatusImage;
	
	@UiField 
	protected Button continueButton;
	
	@UiField
	protected HasClickHandlers downloadButton;
	
	/** An iframe used to handle downloading the JNLP file without changing the window location */
	private Frame downloadFrame = new Frame();
	
	private boolean hasInitiatedDownloadAlready = false;

	/**
	 * Default public constructor. Needed for UI bindings.
	 */
	public CourseInitInstructionsWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		
		downloadFrame.setVisible(false);
		downloadFrame.getElement().getStyle().setVisibility(Visibility.HIDDEN);
	}
	
	/**
	 * Creates a new gateway connection status widget using an existing abstract widget instance from which to receive its widget properties.
	 * 
	 * @param instance the widget instance from which to get the wiget properties
	 */
	public CourseInitInstructionsWidget(WidgetInstance instance) {
		this();
		
		update(instance);		
	}
	
	@Override
	public void onResize() {
		containerPanel.setHeight((Window.getClientHeight() - 5) + "px");
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tutor.client.IsUpdateableWidget#update(mil.arl.gift.tutor.shared.WidgetInstance)
	 */
	@Override
	public void update(WidgetInstance widgetInstance) {
		
		WidgetProperties properties = widgetInstance.getWidgetProperties();
		
        if (properties != null) {
        	
        	DisplayCourseInitInstructionsRequest request = CourseInitInstructionsWidgetProperties.getDisplayCourseInitInstructionsRequest(properties);
        	
        	final SubmitAction submitAction = new SubmitAction(widgetInstance.getWidgetId(), properties);
        	
        	if(request != null){
        			
        		GatewayStateEnum connectionStatus = request.getGatewayState();
        		
	        	if(connectionStatus == null){  		
	        		//This should never happen
	        		
	        	} else if(connectionStatus.equals(GatewayStateEnum.NOT_CONNECTED)){
	        		
	        		connectionStatusLabel.setText("Connecting...");
	        		connectionStatusImage.setUrl(LOADING_IMAGE_URL);
	        		
	        		configurationStatusLabel.setText("Configuring...");
	        		configurationStatusImage.setUrl(LOADING_IMAGE_URL);
	        		configurationStatusImage.getElement().getStyle().setProperty("visibility", "hidden");
	        		
	        		continueButton.getElement().getStyle().setProperty("visibility", "hidden");
	        		
	        		final List<String> assetURLs = request.getAssetURLs();
	        		
	        		if(assetURLs != null){
	        			
	        			if(!hasInitiatedDownloadAlready){
	        				
	        				//download the assets
		        			for(String assetURL : assetURLs){	    
		        				
		        				final String url = assetURL.replace("\\", "/");
		        				
		        				if(logger.isLoggable(Level.INFO)){
		        				    logger.info("Checking if assest URL is reachable from client: "+url);
		        				}
		        				
		        				BrowserSession.getInstance().isUrlResourceReachable(url, new AsyncCallback<RpcResponse>() {
									
									@Override
									public void onSuccess(RpcResponse response) {
										
										if(response.isSuccess()){
											downloadFile(url);
											
										} else {
											Document.getInstance().displayDialogInDashboard(
													
													"Download Error", 
													
													"A problem occurred while trying to automatically download the communication application "
													+ "needed to run this course. <br/>"
													+ "<br/>"
													+ "You can try to manually download the communication "
													+ "application by following the instructions on this page. If the application does not "
													+ "begin downloading, you can exit this course by clicking the stop button in the "
													+ "upper-right corner of this page.",
													"Advanced Description: "+response.getResponse()
											);
										}
									}
									
									@Override
									public void onFailure(Throwable thrown) {
										
										Document.getInstance().displayDialogInDashboard(
												
												"Download Error", 
												
												"A problem occurred while trying to automatically download the communication application "
												+ "needed to run this course. <br/>"
												+ "<br/>"
												+ "You can try to manually download the communication "
												+ "application by following the instructions on this page. If the application does not "
												+ "begin downloading, you can exit this course by clicking the stop button in the "
												+ "upper-right corner of this page.",
												"Advanced Description: "+thrown.getMessage()
										);
									}
								});	        					        				
		        			}
		        			
		        			hasInitiatedDownloadAlready = true;	        			
	        			
		                	downloadButton.addClickHandler(new ClickHandler() {
		        				
		        				@Override
		        				public void onClick(ClickEvent event) {
		        					
			        				//download the assets
		        					for(String assetURL : assetURLs){	  
		        						
		        						final String url = assetURL.replace("\\", "/");
		        						
		                                if(logger.isLoggable(Level.INFO)){
		                                    logger.info("Checking if assest URL is reachable from client: "+url);
		                                }
		        						
		        						BrowserSession.getInstance().isUrlResourceReachable(url, new AsyncCallback<RpcResponse>() {
											
											@Override
											public void onSuccess(RpcResponse response) {
												
												if(response.isSuccess()){
													
													downloadFile(url);
													
												} else {
													
													Document.getInstance().displayDialogInDashboard(
															
															"Download Error", 
															
															"A problem occurred while downloading the communication application "
															+ "needed to run this course.<br/> "
															+ "<br/>You can try downloading the communcation application again in a "
															+ "few minutes or you can exit this course by clicking the stop button in the "
															+ "upper-right corner of this page.",
															"Advanced Description: "+response.getResponse()
													);
												}
											}
											
											@Override
											public void onFailure(Throwable thrown) {
												
												Document.getInstance().displayDialogInDashboard(
														
														"Download Error", 
														
														"A problem occurred while downloading the communication application "
														+ "needed to run this course.<br/> "
														+ "<br/>You can try downloading the communcation application again in a "
														+ "few minutes or you can exit this course by clicking the stop button in the "
														+ "upper-right corner of this page.",
														"Advanced Description: "+thrown.getMessage()
												);
											}
										});
		    	        			}
		        				}
		        			});
		        		}
	        		}
	        		
	            	if (BrowserSession.getInstance() != null) {

	                    BrowserSession.getInstance().sendActionToServer(submitAction, null);
	                }
	        
	        	} else if(connectionStatus.equals(GatewayStateEnum.CONNECTED)){
	        		
	        		downloadFrame.removeFromParent(); //download has finished, so clean up its frame
	        		
	        		connectionStatusLabel.setText("Connected");
	        		connectionStatusImage.setUrl(CHECK_MARK_IMAGE_URL);
	        		
	        		configurationStatusLabel.setText("Configuring...");
	        		configurationStatusImage.setUrl(LOADING_IMAGE_URL);
	        		
	        		configurationStatusImage.getElement().getStyle().setProperty("visibility", "visible");
	        		
	        		continueButton.getElement().getStyle().setProperty("visibility", "hidden");
	        		
	            	if (BrowserSession.getInstance() != null) {

	                    BrowserSession.getInstance().sendActionToServer(submitAction, null);
	                }
	        		
	        	} else if(connectionStatus.equals(GatewayStateEnum.READY)){
	        		
	        		connectionStatusLabel.setText("Connected");
	        		connectionStatusImage.setUrl(CHECK_MARK_IMAGE_URL);
	        		
	        		configurationStatusLabel.setText("Configured");
	        		configurationStatusImage.setUrl(CHECK_MARK_IMAGE_URL);
	        		
	        		configurationStatusImage.getElement().getStyle().setProperty("visibility", "visible");
	        		
	        		continueButton.getElement().getStyle().setProperty("visibility", "visible");
	        		
	        		continueButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							
				        	if (BrowserSession.getInstance() != null) {
				        	    continueButton.setEnabled(false); // don't allow multiple clicks
				                BrowserSession.getInstance().sendActionToServer(submitAction, null);
				            }
						}
					});
	        	} 
        	}  	
        } 
	}
	
	@Override
	public WidgetTypeEnum getWidgetType() {
		return WidgetTypeEnum.COURSE_INIT_INSTRUCTIONS_WIDGET;
	}
	
	/**
	 * Downloads the file at the specified URL
	 * 
	 * @param url the URL of the file to download
	 */
	private void downloadFile(String url){ 
		
		downloadFrame.removeFromParent();
		
		// Download the file by opening its URL in a temporary iframe. This avoids changing the navigation of the main page.
		downloadFrame.setUrl(url);
		
		RootPanel.get().add(downloadFrame);
	}
}
