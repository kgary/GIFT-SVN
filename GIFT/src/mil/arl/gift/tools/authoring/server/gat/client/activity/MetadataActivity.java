/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.activity;

import java.util.HashMap;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.PlaceChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.place.MetadataPlace;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.metadata.MetadataEditor;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.LockMetadata;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * The Class MetadataActivity.
 */
public class MetadataActivity extends AbstractGatActivity {
   
    /** The logger. */
    private static Logger logger = Logger.getLogger(MetadataActivity.class.getName());
    
    /** A generic mapping of name/value pairs that can be specified when starting the activity/presenter */
    private HashMap<String, String> startParams;
    
    @Inject
    private MetadataEditor metadataEditor;
    
    /**
     * Instantiates a new metadata activity.
     *
     * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
     */
    @Inject
    public MetadataActivity(@Assisted HashMap<String, String> startParams) {
        this.startParams = startParams;   
    }     
    
    /* (non-Javadoc)
     * @see com.google.gwt.activity.shared.Activity#start(com.google.gwt.user.client.ui.AcceptsOneWidget, com.google.gwt.event.shared.EventBus)
     */
    @Override
    public void start(final AcceptsOneWidget containerWidget, final EventBus eventBus) {    	
    	logger.info("starting MetadataActivity");    
    	
    	BsLoadingDialogBox.display("Loading File", "Loading, please wait...");

    	
    	// Retrieve the start params here.
    	 /* 
         * Nick: Need to URL decode the path to prevent an issue in Firefox where URL encoded characters (namely spaces as %20s) aren't 
         * properly decoded before they get to this point
         */
    	String encodedPath = startParams.get(MetadataPlace.PARAM_FILEPATH);
		final String relativePath = encodedPath != null ? URL.decode(encodedPath) : encodedPath;

    	// Default to false, the editor will handle the locking internally.
    	metadataEditor.initMetaDataEditor(startParams);
    	metadataEditor.setReadOnly(false);
    	
    	containerWidget.setWidget(metadataEditor);
    	
    	LockMetadata lockMetadata = new LockMetadata();
    	lockMetadata.setRelativePath(relativePath);
    	lockMetadata.setUserName(GatClientUtility.getUserName());
    	lockMetadata.setBrowserSessionKey(GatClientUtility.getUserName());
    	
    	AsyncCallback<LockFileResult> callback =  new AsyncCallback<LockFileResult>() {
	        @Override
			public void onFailure(Throwable t) {
	        	// Open the file for viewing only
	        	metadataEditor.setReadOnly(true);
	        	eventBus.fireEvent(new PlaceChangedEvent("Metadata Authoring Tool")); 

	        }
	        @Override
			public void onSuccess(LockFileResult result) {
	        	if(!result.isSuccess()) {
	        		// Open the file for viewing only
	        		metadataEditor.setReadOnly(true);
	        		metadataEditor.setFileAlreadyLocked(true);
	        	}
	        	eventBus.fireEvent(new PlaceChangedEvent("Metadata Authoring Tool")); 

	        }
	    };
	    
	    SharedResources.getInstance().getDispatchService().execute(lockMetadata, callback);
	    metadataEditor.loadMetadata(relativePath);
    }
	
	/* (non-Javadoc)
	 * @see com.google.gwt.activity.shared.AbstractActivity#mayStop()
	 */
	@Override
	public String mayStop() {
		return metadataEditor.mayStop();
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.activity.shared.AbstractActivity#onStop()
	 */
	@Override
	public void onStop() {
		logger.info("stopping MetadataActivity");
		
		metadataEditor.stop();
	}
	
	
}
