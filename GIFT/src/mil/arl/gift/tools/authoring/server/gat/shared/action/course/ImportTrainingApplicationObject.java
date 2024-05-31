/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.common.util.StringUtils;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for importing a training application from Gift Wrap
 * 
 * @author bzahid
 */
public class ImportTrainingApplicationObject implements Action<ImportTrainingApplicationObjectResult> {

    /** The user attempting the import */
    private String username;

    /** The survey context id of the current course */
    private int surveyContextId;

    /** The path to the GIFT Wrap training application lib folder that should be imported */
    private String realTimeAssessmentFolder;

    /** The path to the current course folder */
    private String courseFolderPath;

    /** The browswer session key */
    private String browserSessionKey;

    /** The flag indicating if we should overwrite if the import causes a naming conflict */
    private boolean overwrite = false;

    /**
     * Required for GWT serialization policy
     */
    private ImportTrainingApplicationObject() {
    }

    /**
     * Constructor
     * 
     * @param username the user attempting to import the training application. Can't be blank.
     * @param surveyContextId The survey context id of the current course. Must be a positive
     *        number.
     * @param sourceTrainingAppsLibFolder The path to the real time assessment folder containing the
     *        training app xml. The path root should be a subdirectory of the workspace folder (e.g.
     *        Public\TrainingAppsLib\MyTALibFolder). Can't be blank.
     * @param destinationCourseFolder the path to the course folder. The root of the path must be a
     *        workspace sub-folder (e.g. Public, &lt;username&gt;). Can't be blank.
     * @param browserSessionKey the browser session key. Can't be blank.
     */
    public ImportTrainingApplicationObject(String username, int surveyContextId, String sourceTrainingAppsLibFolder,
            String destinationCourseFolder, String browserSessionKey) {
        this();
        setUsername(username);
        setSurveyContextId(surveyContextId);
        setRealTimeAssessmentFolder(sourceTrainingAppsLibFolder);
        setCourseFolderPath(destinationCourseFolder);
        setBrowserSessionKey(browserSessionKey);
    }

    /**
     * Sets the survey context id
     * 
     * @param surveyContextId The survey context id of the current course. Must be a positive
     *        number.
     */
    private void setSurveyContextId(int surveyContextId) {
        if (surveyContextId <= 0) {
            throw new IllegalArgumentException("The parameter 'surveyContextId' must be a positive number.");
        }

        this.surveyContextId = surveyContextId;
    }

    /**
     * Gets the survey context id
     * 
     * @return The survey context id of the current course. Will never be negative.
     */
    public int getSurveyContextId() {
        return surveyContextId;
    }

    /**
     * Sets the user attempting to import the training application.
     * 
     * @param username the user attempting to import the training application. Can't be blank.
     */
    private void setUsername(String username) {
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        }

        this.username = username;
    }

    /**
     * Gets the user attempting to import the training application.
     * 
     * @return The the user attempting to import the training application. Will never be blank.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the path to the training application lib real time assessment folder containing the
     * training app xml to be imported.
     * 
     * @param sourceRealTimeAssessmentFolder The path to the real time assessment folder containing
     *        the training app xml. The path root should be a subdirectory of the workspace folder
     *        (e.g. Public\TrainingAppsLib\MyTALibFolder). Can't be blank.
     */
    private void setRealTimeAssessmentFolder(String sourceRealTimeAssessmentFolder) {
        if (StringUtils.isBlank(sourceRealTimeAssessmentFolder)) {
            throw new IllegalArgumentException("The parameter 'sourceRealTimeAssessmentFolder' cannot be blank.");
        }

        this.realTimeAssessmentFolder = sourceRealTimeAssessmentFolder;
    }

    /**
     * Gets the path to the training application lib real time assessment folder containing the
     * training app xml to be imported.
     * 
     * @return The path to the real time assessment folder containing the training app xml. The path
     *         root should be a subdirectory of the workspace folder (e.g.
     *         Public\TrainingAppsLib\MyTALibFolder). Will never be blank.
     */
    public String getRealTimeAssessmentFolder() {
        return realTimeAssessmentFolder;
    }

    /**
     * Sets the path to the course folder.The root of the path must be a workspace sub-folder (e.g.
     * Public, &lt;username&gt;).
     * 
     * @param destinationCourseFolder The path to the course folder. Can't be blank.
     */
    private void setCourseFolderPath(String destinationCourseFolder) {
        if (StringUtils.isBlank(destinationCourseFolder)) {
            throw new IllegalArgumentException("The parameter 'destinationCourseFolder' cannot be blank.");
        }

        this.courseFolderPath = destinationCourseFolder;
    }

    /**
     * Gets the path to the course folder. The root of the path should be a workspace sub-folder
     * (e.g. Public, &lt;username&gt;).
     * 
     * @return The path to the course folder. Will never be blank.
     */
    public String getCourseFolderPath() {
        return courseFolderPath;
    }

    /**
     * Sets the browser session key.
     * 
     * @param browserSessionKey the browser session key. Can't be blank.
     */
    private void setBrowserSessionKey(String browserSessionKey) {
        if (StringUtils.isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
        }

        this.browserSessionKey = browserSessionKey;
    }

    /**
     * Sets the browser session key.
     * 
     * @return browserSessionKey the browser session key. Will never be blank.
     */
    public String getBrowserSessionKey() {
        return browserSessionKey;
    }

    /**
     * Get the flag indicating if we should overwrite if the import causes a naming conflict.
     * 
     * @return true if the import should overwrite any conflicting files; false otherwise.
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * Set the flag indicating if we should overwrite if the import causes a naming conflict. The
     * default value is false.
     * 
     * @param overwrite true if the import should overwrite any conflicting files; false otherwise.
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[ImportTrainingApplicationObject: ");
        sb.append("username = ").append(getUsername());
        sb.append(", surveyContextId = ").append(getSurveyContextId());
        sb.append(", realTimeAssessmentFolder = ").append(getRealTimeAssessmentFolder());
        sb.append(", courseFolderPath = ").append(getCourseFolderPath());
        sb.append(", browserSessionKey = ").append(getBrowserSessionKey());
        sb.append(", overwrite = ").append(isOverwrite());
        sb.append("]");

        return sb.toString();
    }
}
