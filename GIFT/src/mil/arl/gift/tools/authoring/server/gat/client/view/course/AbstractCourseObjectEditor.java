/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SimplePanel;

import mil.arl.gift.tools.authoring.server.gat.client.presenter.AbstractGatPresenter;

/**
 * A widget used to edit a particular type of course object
 * 
 * @author nroberts
 */
public abstract class AbstractCourseObjectEditor<T extends Serializable> extends SimplePanel {
	
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AbstractCourseObjectEditor.class.getName());

	/** An optional command to execute once the course object has finished loading */
	private Command finishedLoadingCommand = null;

	/** The presenter used by this editor, if applicable */
	protected AbstractGatPresenter presenter;
	
	/** The course object currently being edited */
	private T courseObject;
	
	/**
	 * Loads the given course object into the editor for editing. This method is invoked internally by {@link #edit(Serializable)} after 
	 * it performs some initial loading steps common to all course object editors. Extensions of this class should override this method 
	 * while callers should invoke {@link #edit(Serializable)}.
	 * 
	 * @param courseObject the course object to load
	 */
	abstract protected void editObject(T courseObject);
	
	/**
	 * Loads the given course object into the editor for editing
	 * 
	 * @param courseObject the course object to load
	 */
	public void edit(T courseObject){
		
		this.courseObject = courseObject;
		
		editObject(courseObject);
	}
	
	/**
	 * Gets the course object currently being edited
	 * 
	 * @return the course object being edited
	 */
	public T getCourseObject(){
		return courseObject;
	}

	/**
	 * Stops the presenter associated with this editor, if one exists
	 */
	public void stopEditing() {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("stopEditing()");
        }
		
		if(presenter != null){
			presenter.stopPresenting();
		}
	}
	
	/**
	 * Sets a command to execute once this editor finishes loading a course object. If no command is specified, then only the editor's
	 * internal logic will be invoked when the course object finishes loading
	 * 
	 * @param onFinishedLoading the command to execute when the course object finishes loading.
	 */
	public void setFinishedLoadingCommand(Command onFinishedLoading){
		this.finishedLoadingCommand = onFinishedLoading;
	}
	
	/**
	 * Executes whatever command was specified by {@link #setFinishedLoadingCommand(Command)}, if one was provided. If no command
	 * was provided, this method will do nothing.
	 */
	protected void onFinishedLoading(){
		
		if(finishedLoadingCommand != null){
			finishedLoadingCommand.execute();
		}
	}
}
