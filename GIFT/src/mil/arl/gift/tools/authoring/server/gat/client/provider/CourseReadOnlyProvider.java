/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.provider;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.HasAttachHandlers;

/**
 * Handler for all interfaces affected by readOnly changes
 * 
 * @author mweinert
 *
 */

/**
 * @author mweinert
 *
 */
public class CourseReadOnlyProvider {

	/** The logger for the class */
	private static final Logger logger = Logger.getLogger(CourseReadOnlyProvider.class.getName());

	/** The parent instance that applies changes to it's children */
	private static final CourseReadOnlyProvider instance = new CourseReadOnlyProvider();

	/** Set of all interfaces effected and handled by the CourseReadOnlyProvider */
	private final Set<CourseReadOnlyHandler> readOnlyHandlers = new HashSet<>();

	/**	Used to change between read only modes for set of interfaces */
	private boolean isReadOnly = false;

	/** Used to obtain the instance of the desired interface
	 * 
	 * @return the instance
	 */
	public static CourseReadOnlyProvider getInstance() {
		return instance;
	}

	/** Adds an interface to the set that is to be handled by readOnly changes. It is up to the caller
	 * 	to remove the handler via {@link #removeReadOnlyHandler(CourseReadOnlyHandler)} and it is suggested 
	 * 	to use {@link #addReadOnlyHandler(CourseReadOnlyHandler)} if the handler can be managed automatically.
	 * 
	 * @param readOnlyHandler which to be added to the set of read only mode instances
	 */
	public void addReadOnlyHandler(CourseReadOnlyHandler readOnlyHandler) {
		readOnlyHandlers.add(readOnlyHandler);
	}
	
	/** Determines if the interface's handler in question is added or removed.
	 *  Adding/removing handler is automatic on attach/detach.
	 * 
	 * @param <T> the generic type for the interfaces
	 * @param widget the interface that is attaching or detaching handlers to
	 */
	public <T extends CourseReadOnlyHandler & HasAttachHandlers> void addReadOnlyHandlerManaged(final T widget) {
		widget.addAttachHandler(new Handler() {
			
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if(event.isAttached()) {
					addReadOnlyHandler(widget);					
				} else {
					removeReadOnlyHandler(widget);
				}
			}
		});
	}

	/** Removes the interface from the readOnlyHandlers set
	 *	
	 *	@param readOnlyHandler which to be removed from the set of read only mode instances
	 */
	public void removeReadOnlyHandler(CourseReadOnlyHandler readOnlyHandler) {
		readOnlyHandlers.remove(readOnlyHandler);
	}

	/** Indicates whether the instance is readOnly or not
	 * 
	 * 	@return readOnly boolean to check if an instance is in read only mode
	 */
	public boolean isReadOnly() {
		return isReadOnly;
	}

	/** Implements the instance's readOnly change to all of the interfaces within the readOnlyHandlers set
	 * 	
	 * 	@param isReadOnly the boolean to determine if course is in readOnlyMode
	 */
	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;

		for (CourseReadOnlyHandler courseReadOnlyHandler : readOnlyHandlers) {
			try {
				courseReadOnlyHandler.onReadOnlyChange(isReadOnly);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "A handler failed to process courseReadOnlyHandler().", e);
			}
		}
	}


	/** Changes the instanced interfaces according to new readOnly values
	 *	
	 *	@author mweinert
	 */
	public interface CourseReadOnlyHandler {

		/**	Used to track and push the read only changes within the set of instances
		 * 	with the handler's attached.
		 * 
		 * @param isReadOnly boolean to change the instance
		 */
		void onReadOnlyChange(boolean isReadOnly);
	}
}