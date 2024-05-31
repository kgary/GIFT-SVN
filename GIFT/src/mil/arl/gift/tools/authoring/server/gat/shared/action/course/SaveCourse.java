/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import generated.course.Course;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action that causes a (presumably edited) Course to be saved to disk.
 * @author cragusa
 */
public class SaveCourse implements Action<SaveJaxbObjectResult>{
	
	/** 
	 * The course to be saved. 
	 */
	private Course course;

	/**
	 * The course path.
	 */
	private String path;
	
	/**
	 * This data member effectively serves two purposes:
	 * 1.) If it is set to true and the file you're trying to write to is
	 * already locked then the save will fail. This was designed specifically
	 * to handle the Save-As function that tries to overwrite an existing file.
	 * We can't let the user overwrite a file somebody else has locked and is
	 * working on.
	 * 2.) After saving a new file the client used to follow that up with an
	 * additional call to the server to acquire the lock for that file. So to
	 * make life easier on the client code and to minimize network traffic, the
	 * server will now acquire the lock if this is true and renew the lock if
	 * this is false...assuming the save is successful.
	 */
	private boolean acquireLockInsteadOfRenew = true;
	
	/** The user name. */
	private String userName;

	/**
	 * No-arg constructor. Needed for serialization.
	 */
	public SaveCourse() {
	}

	/**
	 * Gets the course path.
	 * 
	 * @return the course path. e.g. mhoffman/4879 test new1 - Copy/4879 test new1 - Copy.course.xml
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the course path.
	 * 
	 * @param path the course path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Gets the course to be saved.
	 * 
	 * @return the course
	 */
	public Course getCourse() {
		return course;
	}

	/**
	 * Sets the course to be saved.
	 * 
	 * @param course the course to set
	 */
	public void setCourse(Course course) {
		this.course = course;
	}

	/**
	 * Determines if the lock should be acquired or renewed after the file is
	 * successfully saved.
	 * @return True if the lock should be acquired after the file is saved,
	 * false if the lock should be renewed after the file is saved.
	 */
	public boolean isAcquireLockInsteadOfRenew() {
		return acquireLockInsteadOfRenew;
	}

	/**
	 * Tells the server how to update the lock status after the file is saved.
	 * @param acquireLockInsteadOfRenew True if the lock should be acquired and
	 * false if it should be renewed.
	 */
	public void setAcquireLockInsteadOfRenew(boolean acquireLockInsteadOfRenew) {
		this.acquireLockInsteadOfRenew = acquireLockInsteadOfRenew;
	}

	/**
	 * Gets the user name.
	 * @return User name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * @param userName User name.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[SaveCourse: ");
        sb.append("course = ").append(course);
        sb.append(", path").append(path);
        sb.append(", acquireLockInsteadOfRenew").append(acquireLockInsteadOfRenew);
        sb.append(", userName").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
