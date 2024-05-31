/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;

import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.widgets.DynamicHeaderScrollPanel;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.place.CoursePlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.GenericParamPlace;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.CourseWelcomeWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.GatViewScaffold;
import mil.arl.gift.tools.authoring.server.gat.client.view.preview.CoursePreviewWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.wrap.widgets.GIFTWrapHome;
import mil.arl.gift.tools.authoring.server.gat.client.wrap.widgets.SelectExistingObjectWidget;
import mil.arl.gift.tools.authoring.server.gat.client.wrap.widgets.WrapHeaderWidget;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.LogUncaughtClientException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.LogUncaughtClientExceptionResult;
import net.customware.gwt.dispatch.client.DispatchAsync;

/**
 * The GWT entry point class for the GAT client.
 * 
 * @author iapostolos & cragusa
 */
public class GiftAuthoringTool implements EntryPoint {

	/** The logger. */
	private static Logger logger = Logger.getLogger(GiftAuthoringTool.class.getName());
	
	/** URL of the servlet handling files uploaded for importing. This is configurable in src/mil/arl/gift/tools/authoring/gat/war/WEB-INF/web.xml */
    public static final String RESOURCES_SERVLET_URL = "courseResources/";
    
    /** The location of the HTML file to use as the GIFT unavailable page */
    private static final String UNAVAILABLE_PAGE_URL = "Unavailable.html";
    
    /** The Window title for the GIFT Wrap landing page */
    private static final String GIFT_WRAP_WINDOW_TITLE = "GIFT Wrap";

	/* (non-Javadoc)
	 * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
	 */
	@Override
	public void onModuleLoad() {
		initApp();
	}
	
	/**
	 * Initializes the GAT. 
	 */
	private void initApp() {
        /* Module is supposed to be self-contained and if it's open only itself and its children
         * should be focusable. We broke out of its 'intended' use when we open a modal from another
         * modal. This causes focus issues because the bottom modal still enforces the focusable
         * items to be contained within itself, so when the top modal tried to call focus on one of
         * its components, the bottom modal would 'steal' the focus back.
         * 
         * To solve this we are overriding the enforce_focus function for modules with an empty
         * function. This stop the modal from being focus greedy. May cause unknown side effects but
         * none have been observed so far. */
	    overrideEnforceFocus();
	    
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {			
			@Override
			public void onUncaughtException(Throwable throwable) {
			    logUncaughtException(GatGinjector.INSTANCE.getDispatcher(), throwable);
			}
		});
		
        GatClientBundle.INSTANCE.css().ensureInjected();

        String historyToken = History.getToken();

        // remove training slash
        if (historyToken.endsWith(Constants.FORWARD_SLASH)) {
            historyToken = historyToken.substring(0, historyToken.length() - 1);
        }

        if (StringUtils.equals(historyToken, GatClientUtility.GIFT_WRAP_LAUNCH_TOKEN)) {
            // show the gift wrap home page where you can manage gift wrap objects that aren't in courses yet
            
            final WrapHeaderWidget widget = showGIFTWrapHomePage();
            GatClientUtility.initServerProperties(new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void arg0) {
                    Window.setTitle(
                            GIFT_WRAP_WINDOW_TITLE + " " + GatClientUtility.getServerProperties().getVersionName());
                    widget.setSystemIcon(GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.SYSTEM_ICON_SMALL));
                }

                @Override
                public void onFailure(Throwable arg0) {
                    // do nothing
                }
            });
            
        } else if (StringUtils.equals(historyToken, GatClientUtility.GIFT_WRAP_LAUNCH_SELECT_EXISTING_TOKEN)) {
            // show the real time assessment editor for the selected gift wrap object
            final WrapHeaderWidget widget = showSelectExistingObjectPage(GatClientUtility.getGIFTWrapType());
            GatClientUtility.initServerProperties(new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void arg0) {
                    widget.setSystemIcon(GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.SYSTEM_ICON_SMALL));
                }

                @Override
                public void onFailure(Throwable arg0) {
                    // do nothing
                }
            });
        } else if ((StringUtils.isNotBlank(GatClientUtility.getUserName()) && isEmbedded())
                || StringUtils.isNotBlank(GatClientUtility.getExternalScenarioId())) {

            final EventBus eventBus = GatGinjector.INSTANCE.getEventBus();		
			ActivityMapper activityMapper = GatGinjector.INSTANCE.getActivityMapper();		
			ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
			
			GatViewScaffold scaffold = new GatViewScaffold();		
			activityManager.setDisplay(scaffold.getAppPanel());
	
			RootLayoutPanel.get().add(scaffold);
			scaffold.getAppPanel().setWidget(new CourseWelcomeWidget());
			
			if(GatClientUtility.isPreviewMode()) {
			    // show the preview widget (normally in its own modal window)
			    final CoursePreviewWidget preview = new CoursePreviewWidget();
			    GatClientUtility.initServerProperties(new AsyncCallback<Void>() {
                    
                    @Override
                    public void onSuccess(Void arg0) {
                        String backgroundUrl = GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
                        preview.setBackground(backgroundUrl);
                        
                        String systemIconUrl = GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.SYSTEM_ICON_SMALL);
                        preview.setSystemIcon(systemIconUrl);
                    }
                    
                    @Override
                    public void onFailure(Throwable arg0) {
                        // nothing to do                        
                    }
                });
			    RootLayoutPanel.get().clear();
			    RootLayoutPanel.get().add(preview);
			    

			} else {
			    // show the course creator (normally embedded in a dashboard iframe)
			    
			    // Note: the CourseCreator.setReadOnly method requires the ServerProperties, therefore need to wait
			    //       until the client has the properties before loading the CourseCreator.
		        GatClientUtility.initServerProperties(new AsyncCallback<Void>() {
                    
                    @Override
                    public void onSuccess(Void arg0) {
                        logger.info("Successfully received the server properties, continuing to CoursePlace");
                        PlaceHistoryMapper placeHistoryMapper = GWT.create(GatPlaceHistoryMapper.class);        
                        PlaceHistoryHandler placeHistoryHandler = new PlaceHistoryHandler(placeHistoryMapper);
                        PlaceController placeController = GatGinjector.INSTANCE.getPlaceController();
                        Place defaultPlace = new CoursePlace();
                        placeHistoryHandler.register(placeController, eventBus, defaultPlace);
                        placeHistoryHandler.handleCurrentHistory();                        
                    }
                    
                    @Override
                    public void onFailure(Throwable error) {
                        logger.log(Level.SEVERE, "Failed to retrieve the server properties", error);
                    }
                });

			}
		} else {
		    // show an error page
			
		    // attempt to get the server properties which has the configurable background image to use
		    GatClientUtility.initServerProperties(new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void arg0) {
                    showSomething();
                }

                @Override
                public void onFailure(Throwable arg0) {
                    showSomething();
                }
                
                private void showSomething(){
                    
                    Frame unavailableFrame = new Frame();
                    unavailableFrame.getElement().getStyle().setProperty("border", "none");
                    
                    String backgroundUrl = GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
                    unavailableFrame.getElement().getStyle().setBackgroundImage("linear-gradient(transparent, rgba(255,0,0,0.6)), url('"+backgroundUrl+"')");
                    unavailableFrame.setSize("100%", "100%");
                    unavailableFrame.setUrl(UNAVAILABLE_PAGE_URL);
                    RootLayoutPanel.get().clear();
                    RootLayoutPanel.get().add(unavailableFrame);
                }
            });

			
		}
	}

    /**
     * Shows the home page for GIFT Wrap, where users can create new training application objects,
     * edit existing objects, or delete existing objects.
     * 
     * @return the GIFT Wrap header widget that was created
     */
    public WrapHeaderWidget showGIFTWrapHomePage() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showGIFTWrapHomePage()");
        }

        /* The header bar appended at the top of every page */
        WrapHeaderWidget headerBar = new WrapHeaderWidget();

        /* A widget wrapping the &lt;body&gt; element in GIFT Wrap's DOM */
        RootLayoutPanel document = RootLayoutPanel.get();

        document.clear();

        DynamicHeaderScrollPanel headerScrollerPanel = new DynamicHeaderScrollPanel();
        headerScrollerPanel.setHeader(headerBar);
        headerScrollerPanel.setCenter(new GIFTWrapHome());

        document.add(headerScrollerPanel);
        
        return headerBar;
    }

    /**
     * Shows the home page for GIFT Wrap, where users can create new training application objects,
     * edit existing objects, or delete existing objects.
     * 
     * @param wrapType the type of application to show in the select existing table
     * @return the GIFT Wrap header widget that was created
     */
    public WrapHeaderWidget showSelectExistingObjectPage(TrainingApplicationEnum wrapType) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showSelectExistingObjectPage(" + wrapType + ")");
        }

        /* The header bar appended at the top of every page */
        WrapHeaderWidget headerBar = new WrapHeaderWidget();

        /* A widget wrapping the &lt;body&gt; element in GIFT Wrap's DOM */
        RootLayoutPanel document = RootLayoutPanel.get();

        document.clear();

        headerBar.setHeaderText("GIFT Wrap - Select Existing");

        DynamicHeaderScrollPanel headerScrollerPanel = new DynamicHeaderScrollPanel();
        headerScrollerPanel.setHeader(headerBar);
        headerScrollerPanel.setCenter(new SelectExistingObjectWidget(wrapType));

        document.add(headerScrollerPanel);
        
        return headerBar;
    }

	/**
	 * Gen log entry.
	 *
	 * @param sb the sb
	 * @param throwable the throwable
	 * @return the string
	 */
	private String genLogEntry(StringBuilder sb, Throwable throwable) {
		
		sb.append("ERROR: Uncaught Exception on Client: ");
		sb.append(throwable.getLocalizedMessage());
				
		for(StackTraceElement stackTrace : throwable.getStackTrace()) {
			sb.append("\n\t").append(stackTrace.getClassName());
			sb.append("::").append(stackTrace.getMethodName());
			sb.append("() [").append(stackTrace.getFileName());
			sb.append(" (").append(stackTrace.getLineNumber()).append(")");
			sb.append("]");
		}
		
		if(throwable.getCause() != null) {
			sb.append("\nCaused by:");
			genLogEntry(sb, throwable.getCause());
		}
		
		return sb.toString();
	}	
	
	/**
	 * Log uncaught exception.
	 *
	 * @param dispatchService the dispatch service
	 * @param throwable the throwable
	 */
	private void logUncaughtException(DispatchAsync dispatchService, final Throwable throwable) {
		
		StringBuilder sb = new StringBuilder();
		
		final String logEntry = genLogEntry(sb, throwable);
		
	    logger.warning(logEntry); 
		
		AsyncCallback<LogUncaughtClientExceptionResult> callback = new AsyncCallback<LogUncaughtClientExceptionResult>() {

			@Override
			public void onFailure(Throwable throwable) {
				WarningDialog.error("Uncaught exception", "Server threw an exception.\n" + throwable.getLocalizedMessage());				
			}

			@Override
			public void onSuccess(LogUncaughtClientExceptionResult result) {	
			    
			    if(logger.isLoggable(Level.FINE)){
			        logger.fine("logUncaughtException: " + (result.isSuccess() ? "SUCCESS" : "FAILED"));		
			    }
			}		
		};
		
		if(logger.isLoggable(Level.FINE)){
		    logger.fine("logUncaughtException: " + throwable.toString());
		}
				
		LogUncaughtClientException action = new LogUncaughtClientException(logEntry);
		dispatchService.execute(action, callback);		
	}
	
    /**
     * Overrides the default behavior for a modal to only allow itself and its children to be
     * focusable.
     */
    private static native void overrideEnforceFocus() /*-{
		$wnd.jQuery.fn.modal.Constructor.prototype.enforceFocus = function() {
		};
    }-*/;
	
	private static native boolean isEmbedded() /*-{
		try {
			// check to see if the gat was loaded in an iframe
			return (parent.window != parent.parent.window);
		} catch (e) {
			return true;
		}
	}-*/;
}