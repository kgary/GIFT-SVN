/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditorResources;
import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Singleton class that provides access to shared resources.
 * (NOTE: this class will go away, once we have dependency injection
 *  more widely implemented.)
 *
 * @author cragusa
 */
public class SharedResources {
	
	/** The Constant INSTANCE. */
	private static final SharedResources INSTANCE =  new SharedResources();

	/** The dispatch service. */
	private DispatchAsync dispatchService = GatGinjector.INSTANCE.getDispatcher();

	/** The event bus. */
	private EventBus eventBus = GatGinjector.INSTANCE.getEventBus();
	
	/** The interface for invoking RPCs */
    private GatRpcServiceAsync rpcService = GWT.create(GatRpcService.class);

	/**
	 * Instantiates a new shared resources.
	 */
	private SharedResources() {
		
		// Nick - I had to move some of the survey editor's event handling into the common.gwt package so that some survey editor
		// components could be used across GWT apps, so I'm reattaching that event handling logic to this class's event bus to keep
		// things roughly the same as they were. I don't know if this line is necessary at the moment, but it could be if more events
		// start getting thrown around the survey editor, so I'm adding it just in case.
		SurveyEditorResources.getInstance().setEventBus(eventBus);
	}
	
	/**
	 * Gets the single instance of SharedResources.
	 *
	 * @return single instance of SharedResources
	 */
	public static SharedResources getInstance() {		
		return INSTANCE;
	}
	
	/**
	 * Retrieve the EventBus.
	 *
	 * @return the EventBus
	 */
	public EventBus getEventBus() {
		return  eventBus;
	}

	/**
	 * Retrieve the DispatchAsync.
	 *
	 * @return the DispatchAsync
	 */
	public DispatchAsync getDispatchService() {
		return  dispatchService;
	}
	
	/**
	 * Retrieve the RPC service used to invoke operations on the server
	 * 
	 * @return the RPC service
	 */
	public GatRpcServiceAsync getRpcService() {
	    return rpcService;
	}
}
