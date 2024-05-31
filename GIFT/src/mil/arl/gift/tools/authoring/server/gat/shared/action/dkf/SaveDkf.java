/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;
 
import generated.dkf.Scenario;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import net.customware.gwt.dispatch.shared.Action;
 
/**
 * An action used to request the server to save a DKF.
 */
public class SaveDkf implements Action<SaveJaxbObjectResult> {

	/** The DKF path to save. */
	private String path;
	
	/** The scenario object to update the DKF with*/
	private Scenario scenario;
	
	/** Flag to indicate if we are accessing the DKF through GIFT Wrap */
	private boolean isGIFTWrap = false;
	
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
     * Default public no-arg constructor. Needed for serialization.
     */
    public SaveDkf() {
        super();
    }

	/**
	 * Gets the DKF path to save.
	 *
	 * @return the DKF path to save
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the relative path.
	 * @param path Relative path to set.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Gets the scenario object to update the DKF with.
	 * 
	 * @return the scenario object to update the DKF with
	 */
	public Scenario getScenario() {
		return scenario;
	}

	/**
	 * Sets the scenario.
	 * @param scenario Scenario to set.
	 */
	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
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

    /**
     * Gets the flag that indicates if the DKF was accessed through GIFT Wrap (the default is
     * false).
     * 
     * @return the isGIFTWrap
     */
    public boolean isGIFTWrap() {
        return isGIFTWrap;
    }

    /**
     * Sets the flag that indicates if the DKF was accessed through GIFT Wrap. (the default is
     * false)
     * 
     * @param isGIFTWrap true if the DKF was opened through GIFT Wrap; false otherwise.
     */
    public void setIsGIFTWrap(boolean isGIFTWrap) {
        this.isGIFTWrap = isGIFTWrap;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[SaveDkf: ");
        sb.append("path = ").append(path);
        sb.append(", scenario = ").append(scenario);
        sb.append(", acquireLockInsteadOfRenew = ").append(acquireLockInsteadOfRenew);
        sb.append(", userName = ").append(userName);
        sb.append(", isGIFTWrap = ").append(isGIFTWrap);
        sb.append("]");

        return sb.toString();
    } 
}
