/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.learner;
 
import generated.learner.LearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import net.customware.gwt.dispatch.shared.Action;
 
/**
 * An action used to request the server to save a learner configuration.
 */
public class SaveLearnerConfiguration implements Action<SaveJaxbObjectResult> {

	/** The learner configuration path to save. */
	private String domainRelativePath;
	
	/** The learner configuration object to save. */
	private LearnerConfiguration learnerConfiguration;
	
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
    public SaveLearnerConfiguration() {
        super();
    }

	/**
	 * Gets the learner configuration path to save.
	 *
	 * @return the learner configuration path to save
	 */
	public String getRelativePath() {
		return domainRelativePath;
	}
	
	/**
	 * Sets the relative path.
	 * @param domainRelativePath Relative path.
	 */
	public void setRelativePath(String domainRelativePath) {
		this.domainRelativePath = domainRelativePath;
	}

	/**
	 * Gets the LearnerConfiguration object to update the learner configuration file with.
	 * 
	 * @return the LearnerConfiguration object to update the learner configuration file with
	 */
	public LearnerConfiguration getLearnerConfiguration() {
		return learnerConfiguration;
	}
	
	/**
	 * Sets the LearnerConfiguration.
	 * @param learnerConfiguration The learner configuration.
	 */
	public void setLearnerConfiguration(LearnerConfiguration learnerConfiguration) {
		this.learnerConfiguration = learnerConfiguration;
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
        sb.append("[SaveLearnerConfiguration: ");
        sb.append("domainRelativePath = ").append(userName);
        sb.append(", learnerConfiguration").append(learnerConfiguration);
        sb.append(", acquireLockInsteadOfRenew").append(acquireLockInsteadOfRenew);
        sb.append(", userName").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
