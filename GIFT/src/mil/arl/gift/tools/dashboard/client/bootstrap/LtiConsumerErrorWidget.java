/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.HasSafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;

/**
 * An ending page for a user that has experienced an error while executing a
 * GIFT course from an LTI Tool Consumer.
 * 
 * @author nrberts
 *
 */
public class LtiConsumerErrorWidget extends AbstractBsWidget {
	
	private static Logger logger = Logger.getLogger(LtiConsumerErrorWidget.class.getName());

	private static LtiConsumerErrorWidgetUiBinder uiBinder = GWT.create(LtiConsumerErrorWidgetUiBinder.class);

	interface LtiConsumerErrorWidgetUiBinder extends UiBinder<Widget, LtiConsumerErrorWidget> {
	}
	
	@UiField
	protected Widget mainContainer;
	
	@UiField
	protected HasText titleText;
	
	@UiField
	protected HasSafeHtml messageText;
	
	@UiField
	protected HasSafeHtml detailsText;
	
	/** the image containing the configurable logo */
    @UiField
    protected Image logoImage;

	/**
	 * Creates a new error page using the given error data
	 * 
	 * @param initParams the error data. Expected to be an instance of {@link ErrorMessage} or {@link DetailedExceptionSerializedWrapper}
	 * @param backgroundImageFile the path to the background image (in the dashboard war) to use as the background for this widget
	 * Shouldn't be null or blank.
	 */
	public LtiConsumerErrorWidget(Object initParams, String backgroundImageFile) {
		
		logger.info("LtiConsumerErrorWidget() called.");
        initWidget(uiBinder.createAndBindUi(this));
        
        // Close any existing websocket.
        if (BrowserSession.getInstance() != null) {
            BrowserSession.getInstance().closeWebSocket();
        }
        
        // place a red gradient on the background
        mainContainer.getElement().getStyle().setBackgroundImage("url(\"" + backgroundImageFile + "\"), linear-gradient(transparent, rgba(255,0,0,0.6));");
        
        String reason = null;
        String details = null;
        List<String> stackTrace = null;
        
        if (initParams instanceof DetailedExceptionSerializedWrapper) {
        	
            
        	DetailedExceptionSerializedWrapper exceptionWrapper = (DetailedExceptionSerializedWrapper) initParams;
        	
            reason = exceptionWrapper.getReason();           
            details = exceptionWrapper.getDetails();           
            stackTrace = exceptionWrapper.getErrorStackTrace();
            
            logger.severe("Showing error page with reason: " + reason + ", details: " + details);
        
        } else if (initParams instanceof ErrorMessage) {
        	
        	ErrorMessage exceptionWrapper = (ErrorMessage) initParams;
        	
            reason = exceptionWrapper.getReason();           
            details = exceptionWrapper.getDetails();           
            stackTrace = exceptionWrapper.getStackTrace();
            
            logger.severe("Showing error page with reason: " + reason + ", details: " + details);
        }   
        
        if(reason != null){
            
             SafeHtmlBuilder detailsHtml = new SafeHtmlBuilder();
             detailsHtml.appendHtmlConstant(reason);
        	 messageText.setHTML(detailsHtml.toSafeHtml());
        }
        
        if(details != null || stackTrace != null){       	
        	
        	 SafeHtmlBuilder detailsHtml = new SafeHtmlBuilder();
             
        	 if(details != null){
        		 
	        	 detailsHtml.appendHtmlConstant(details)
	             	.appendHtmlConstant("<br/><br/>");
        	 }
             
             if(stackTrace != null){
             	
             	for(String line : stackTrace){
             		
             		if(line.startsWith("at")){
             			detailsHtml.appendHtmlConstant("&nbsp;&nbsp;&nbsp;&nbsp;");
             		}
             		
             		detailsHtml.appendHtmlConstant(line).appendHtmlConstant("<br/>");
             	}
             }
             
             detailsText.setHTML(detailsHtml.toSafeHtml());         
        }
        
        String logoUrl = Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.LOGO);
        logoImage.setUrl(logoUrl);
	}
	
	/**
	 * An error message for the LTI error page
	 * 
	 * @author nroberts
	 */
	public static class ErrorMessage{
		
		private String reason;
		private String details;
		private List<String> stackTrace;
		
		/**
		 * Creates a new error message with the given reason, details, and stack trace
		 * 
		 * @param reason the reason text
		 * @param details the details text
		 * @param stackTrace the list of stack trace lines
		 */
		public ErrorMessage(String reason, String details, List<String> stackTrace){
			this.reason = reason;
			this.details = details;
			this.stackTrace = stackTrace;
		}

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

		public String getDetails() {
			return details;
		}

		public void setDetails(String details) {
			this.details = details;
		}

		public List<String> getStackTrace() {
			return stackTrace;
		}
		
		public void setStackTrace(List<String> stackTrace) {
			this.stackTrace = stackTrace;
		}

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ErrorMessage: reason=");
            builder.append(reason);
            builder.append(", details=");
            builder.append(details);
            builder.append(", stackTrace=\n");
            builder.append(stackTrace);
            builder.append("]");
            return builder.toString();
        }
		
		
	}

}
