/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.aar.ScoreNodeUpdate;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionScenarioInfo;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.experiment.CourseCollection;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.DataCollectionItem.DataCollectionPermission;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.gwt.client.ErrorDetails;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.shared.CourseValidationParams;
import mil.arl.gift.common.gwt.shared.GetActiveKnowledgeSessionsResponse;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.CourseListFilter;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.ta.util.ExternalMonitorConfig;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;
import mil.arl.gift.tools.dashboard.shared.messages.GatewayConnection;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedBookmarkCache;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedStrategyCache;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageDisplayData;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageEntryMetadata;
import mil.arl.gift.tools.dashboard.shared.rpcs.CopyCourseResult;
import mil.arl.gift.tools.dashboard.shared.rpcs.CorrectCoursePathsResult;
import mil.arl.gift.tools.dashboard.shared.rpcs.DeleteCourseResult;
import mil.arl.gift.tools.dashboard.shared.rpcs.DoubleResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExistingSessionResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExperimentListResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExperimentResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExportResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.GenerateReportStatusResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.GetCourseListResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.GetUsernamesResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ImportCoursesResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.LmsCourseRecordsResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.LoadExperimentResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.LoginResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ProgressResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.SessionTimelineInfo;
import mil.arl.gift.tools.dashboard.shared.rpcs.ValidateCourseResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.WebMonitorStatus;
import mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection.AbstractCourseCollectionAction;
import mil.arl.gift.tools.map.shared.SIDC;

/**
 * The async counterpart for {@link DashboardService}.
 *
 * @author nblomberg
 *
 */
public interface DashboardServiceAsync {

    /**
     * Return the server properties that have been identified as important for the
     * dashboard client.
     *
     * @param callback - Callback that will return the server properties.
     */
    void getServerProperties(AsyncCallback<ServerProperties> callback);
    
    /**
     * Login a user
     *
     * @param username The username of the user to login
     * @param password the password of the user
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @param callback Callback for if the action was successful or not
     * @throws IllegalArgumentException
     */
    void loginUser(String username, String password, String loginAsUserName, AsyncCallback<LoginResponse> callback)
            throws IllegalArgumentException;

    /**
     * Attempts to login from an existing session on the server.
     * If this returns success, the AdditionalInformation parameter
     * in the RpcReponse will contain the username.
     *
     * @param callback Callback to be used to hold the RpcReponse.
     * @throws IllegalArgumentException
     */
    void loginFromExistingSession(String browserSessionId, AsyncCallback<ExistingSessionResponse> callback) throws IllegalArgumentException;

    /**
     * Logs out the user.
     * @param callback Callback to be used to hold the RpcReponse.
     * @throws IllegalArgumentException
     */
    void logoutUser(AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;


    /**
     * Updates the user state on the server.  This rpc makes sure the server is aware of the
     * state that the client session is in.  The server is the authority on the state.  If the
     * user has logged out (on another browser), then this can return a failure.
     *
     * @param browserSessionId - the session id from the client.
     * @param state - The state that the client is going to (currently a screen enum).
     * @param callback Callback to be used to hold the RpcReponse.
     * @throws IllegalArgumentException
     */
    void updateUserState(String browserSessionId, ScreenEnum state, AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;

    /**
     * Updates the permissions for one or more users on the given course (and the course's survey context)
     *
     * @param permissions the permissions to set, can have a null permission enum to clear a user's permissions.
     * @param courseData contains information about the course which permissions are changing
     * @param browserSessionId the session id from the client
     * @param callback Callback to be used to hold the RpcReponse. True if success on the server, false otherwise.  Null can be returned if the user session is null.
     * @throws IllegalArgumentException  if any of the arguments are not properly provided.
     */
    void updateCourseUserPermissions(Set<DomainOptionPermissions> permissions, DomainOption courseData, String browserSessionId,
            AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;
    
    /**
     * Gets the progress of updating the user permissions of a course that was requested by the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @param callback - The callback returning the progress of the operation. Can be null if there was no update operation
     * associated with this user.
     */
    void getUpdateCourseUserPermissionsProgress(String username, AsyncCallback<ProgressResponse> callback);

    /**
     * Updates the permissions for one or more users on the given published course
     *
     * @param permissions the permissions to set, can have a null permission enum to clear a user's permissions.
     * @param dataCollectionId the unique id of the published course to update user permission on
     * @param browserSessionId the session id from the client
     * @param callback Callback to be used to hold the RpcReponse. True if success on the server, false otherwise.  Null can be returned if the user session is null.
     * @throws IllegalArgumentException  if any of the arguments are not properly provided.
     */
    void updatePublishedCourseUserPermissions(Set<DataCollectionPermission> permissions, String dataCollectionId, String browserSessionId,
            AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;

    /**
     * Gets the course using its unique ID
     *
     * @param browserSessionId The session id from the client.
     * @param courseId the unique ID for the course being retrieved.
     * @param callback used to hold the DomainOption.
     */
    void getCourseById(String browserSessionId, String courseId, AsyncCallback<GenericRpcResponse<DomainOption>> callback);

    /**
     * Gets the course list of the user, which is the available courses that the
     * user has access to. The RPC will check to make sure the user has a
     * session and if so, find the courses for the user. If the user is not in a
     * session, then the RPC 'success' flag will be 'false'.
     *
     * @param browserSessionId - The session id from the client.
     * @param validateCoursePaths - Whether or not to validate course paths in
     *        the user's workspace
     * @param provideSimpleList - whether or not to retrieve the list of course
     *        w/o applying any additional validation, recommendations, etc.
     *        Currently if true this method may apply LMS course recommendations
     *        for the user (if there are applicable course records and the
     *        property for applying recommendations is true)
     * @param filter the filter used to specify which courses to return to the
     *        client. If null, no filtering is applied and all courses the user
     *        has access to are returned.
     * @param callback Callback to be used to hold the GetCourseListResponse.
     * @return A {@link Request} object that can be used to check the status of
     *         or cancel the asynchronous request.
     *
     */
    Request getCourseList(String browserSessionId, boolean validateCoursePaths, boolean provideSimpleList, CourseListFilter filter, AsyncCallback<GetCourseListResponse> callback);

    /**
     * Corrects the paths for the courses in the given list to ensure that the names of each course's folder, .course.xml file,
     * and Course object match and do not cause conflicts
     *
     * @param browserSessionId the ID identifying the session invoking this operation
     * @param courseList the list of courses that need to be corrected
     * @param callback a callback handling a response indicating the success or failure of the operation
     */
    void correctCoursePaths(final String browserSessionId, List<DomainOption> courseList, AsyncCallback<CorrectCoursePathsResult> callback);

    /**
     * Checks to see if a url is 'reachable'. Where reachable means that a '200'
     * HTTP code is returned. If the url returns a 200 HTTP response, then the
     * response to the client will be success. If a non 200 HTTP response code
     * is returned, then the response to the client will be failure.
     *
     * @param url - The url to check (should be in form:
     *        "http://www.somehost.com" query parameters are optional.
     * @param callback Callback to be used to hold the RpcResponse.
     * @throws IllegalArgumentException
     */
    void isUrlReachable(String url, AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;

    /**
     * Retrieves all the GIFT users that are currently in the UMS database.  This normally means these users
     * have logged into this GIFT instance successfully in the lifetime of the UMS database.<br/>
     * Note: this will return an empty list when running GIFT in server mode.
     *
     * @param calback the callback for if the action was successful or not
     */
    void getUsernames(AsyncCallback<GetUsernamesResponse> callback);

    /**
     * Logs in the user in offline mode
     * @param username the user to login
     * @param LoginResponse the callback for if the action was successful or not
     */
    void loginUserOffline(String username, AsyncCallback<LoginResponse> asyncCallback) throws IllegalArgumentException;

    /**
     * Retrieves the userId of a given user
     * @param username the user to retrieve a userId from
     * @param callback the callback for if the action was successful or not
     */
    void getUserId(String username, AsyncCallback<RpcResponse> callback);


    /**
     * Retrieves LMS data for a specified user.  The Lms data can be retrieved in batches, by specifying a starting
     * record index, along with a 'pagesize' of how many records to retrieve.
     *
     * @param browserSessionId - The session id of the browser.
     * @param domainId - An optional ID identifying a specific course from which LMS data should be retrieved.<br/>
     * e.g. Public/TSP 07-GFT-0137 Vignettes/TSP 07-GFT-0137 ClearBldg.jtc_shakarat.course.xml
     * @param domainSessionId - An optional domain session id used to retrieve LMS data for that specific session.
     * @param indexStart - The starting index of the record to start from.
     * @param pageSize - The maximum number of records to return.
     * @param callback - The callback returning the results of the rpc.
     * @throws IllegalArgumentException
     */
    void getLmsData(String browserSessionId, String domainId, Integer domainSessionId, int indexStart, int pageSize, AsyncCallback<LmsCourseRecordsResponse> callback) throws IllegalArgumentException;

    /**
     * Retrieves the latest root LMS data for every course the user with the given ID has taken. This data provides a brief overview
     * of all the courses the user has taken and when they last took said courses.
     *
     * @param browserSessionId the session id of the browser
     * @param callback a callback returning the results
     * @throws IllegalArgumentException if an argument is missing or defined improperly
     */
    void getLatestRootLMSDataPerDomain(String browserSessionId, AsyncCallback<LmsCourseRecordsResponse> callback) throws IllegalArgumentException;

    /**
     * Exports the specified courses for the specified user
     *
     * @param username the username of the user for whom to export the courses
     * @param coursesToExport the domain options of the courses to export
     * @param callback - The callback returning the results of the rpc.
     * @throws IllegalArgumentException
     */
    void exportCourses(String username, List<DomainOption> coursesToExport, AsyncCallback<ExportResponse> callback) throws IllegalArgumentException;

    /**
     * Gets the progress of the export currently being run for the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @param callback - The callback returning the results of the rpc.
     * @throws IllegalArgumentException
     */
    void getExportProgress(String username, AsyncCallback<ProgressResponse> callback) throws IllegalArgumentException;

    /**
     * Cancels the export currently running for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the export
     * @param callback The callback returning the results of the rpc.
     * @throws IllegalArgumentException
     */
    void cancelExport(String username, AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;

    /**
     * Deletes the file generated by an export
     *
     * @param exportResult the result of the export containing the file's location
     * @param callback The callback returning the results of the rpc.
     * @throws IllegalArgumentException
     */
    void deleteExportFile(DownloadableFileRef exportResult, AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;

    /**
     * Calculates the size of the export of the specified courses for the specified user
     *
     * @param username the username of the user for whom to export the courses
     * @param coursesToExport the domain options of the courses to export
     * @param callback - The callback returning the results of the rpc.
     * @throws IllegalArgumentException
     */
    void calculateExportSize(String username, List<DomainOption> coursesToExport, AsyncCallback<DoubleResponse> callback) throws IllegalArgumentException;

    /**
     * Exports the specified course output data
     *
     * @param username the username of the user for who is asking for this data export
     * @param course the course to export the output data for
     * @param callback - The callback returning the results of the rpc.
     */
    void exportCourseData(String username, DomainOption course, AsyncCallback<ExportResponse> callback);

    /**
     * Gets the progress of the export currently being run for the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @param callback - The callback returning the results of the rpc.
     */
    void getExportCourseDataProgress(String username, AsyncCallback<ProgressResponse> callback);

    /**
     * Cancels the export currently running for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the export
     * @param callback - The callback returning the results of the rpc.
     */
    void cancelExportCourseData(String username, AsyncCallback<RpcResponse> callback);

    /**
     * Deletes the file generated by an export
     *
     * @param exportResult the result of the export which contains the location of the file
     * @param callback - The callback returning the results of the rpc.
     */
    void deleteCourseDataExportFile(DownloadableFileRef exportResult, AsyncCallback<RpcResponse> callback);

    /**
     * Requests the course to be loaded.  This must be done before the course is started
     * since the course files may need to be downloaded from the server.
     *
     * @param browserSessionId - The session that is making the request (cannot be null).
     * @param courseData - The course data to load (cannot be null).
     * @param callback - The callback returning the results of the rpc.
     * @return Request - An rpc request object that can be used to cancel the rpc.
     * @throws IllegalArgumentException
     */
    Request loadCourse(String browserSessionId, DomainOption courseData, AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;

    /**
     * Notify the server that the create data collection item progress indicator is no longer needed for the
     * user.
     *
     * @param username the user who is creating a data collection object
     * @param callback always returns success because this is best effort.
     */
    void cleanupCreateDataCollectionItemProgressIndicator(String username, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Notify the server that the data collection course export progress indicator is no longer needed for the
     * user.
     *
     * @param username the user who is exporting a data collection course
     * @param callback always returns success because this is best effort.
     */
    void cleanupCourseExportProgressIndicator(String username, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Notify the server that the data collection raw data export progress indicator is no longer needed for the
     * user.
     *
     * @param username the user who is exporting a data collections raw data
     * @param callback always returns success because this is best effort.
     */
    void cleanupRawDataExportProgressIndicator(String username, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Notify the server that the delete data collection item progress indicator is no longer needed for the
     * user.
     *
     * @param username the user who is deleting a data collection object
     * @param callback always returns success because this is best effort.
     */
    void cleanupDeleteDataCollectionItemProgressIndicator(String username, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Notify the server that the load course indicator is no longer needed for the specific
     * browser's loading of the course specified.
     *
     * @param browserSessionId the unique id of a browser session used to identify the specific user
     * @param courseData contains information about the course that was being loaded by the user
     * @param callback always returns success because this is best effort.
     */
    void cleanupLoadCourseIndicator(String browserSessionId, DomainOption courseData, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Creates a new {@link CourseCollection} in the database.
     *
     * @param username The name of the user who is creating the
     *        {@link CourseCollection}. They will be the owner of the resulting
     *        {@link CourseCollection} that is created. Can't be blank.
     * @param name The display name of the {@link CourseCollection} to create.
     *        Can't be null or empty.
     * @param description An optional description describing the
     *        {@link CourseCollection} in more detail. Can be null.
     * @param callback The callback returning the newly created
     *        {@link CourseCollection}.
     * @throws DetailedException If there was a problem creating the new
     *         {@link CourseCollection}.
     */
    void createCourseCollection(String username, String name, String description, AsyncCallback<CourseCollection> callback);

    /**
     * Retrieves the {@link CourseCollection} that matches a given id.
     *
     * @param courseCollectionId The id of the {@link CourseCollection} to fetch.
     *        Can't be null.
     * @param callback The callback returning the result of the RPC.
     */
    void getCourseCollection(String courseCollectionId, AsyncCallback<CourseCollection> callback);

    /**
     * Fetches all the {@link CourseCollection} objects in the database that the
     * specified user has access to.
     *
     * @param username The username for which the {@link CourseCollection}
     *        should be queried.
     * @param callback The callback returning the {@link Collection} of
     *        {@link CourseCollection} that the specified user has access to.
     */
    void getCourseCollectionsByUser(String username, AsyncCallback<Collection<CourseCollection>> callback);

    void updateCourseCollection(AbstractCourseCollectionAction action, AsyncCallback<GenericRpcResponse<DataCollectionItem>> callback);

    /**
     * Deletes a specified {@link CourseCollection} within the database.
     *
     * @param courseCollectionId The id of the {@link CourseCollection} to
     *        delete within the database. Can't be null.
     * @param callback The callback that is invoked when the RPC finishes
     *        execution on the server.
     */
    void deleteCourseCollection(String username, String courseCollectionId, AsyncCallback<Void> callback);

    /**
     * Notify the server that the LTI load course indicator is no longer needed for the specific
     * browser's loading of the course specified.
     *
     * @param consumerKey The consumer key of the lti user loading the course.
     * @param consumerId The consumer id of the lti user loading the course.
     * @param courseSourceId The course id (domain id) to find the progress of the load (cannot be null).
     * @param callback always returns success because this is best effort.
     */
    void cleanupLoadCourseIndicator(String consumerKey, String consumerId, String courseSourceId, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Notify the server that the Experiment load course indicator is no longer
     * needed for the specific browser's loading of the course specified.
     *
     * @param progressIndicatorId The unique id used to identify a specific
     *        progress indicator for an experiment.
     * @param callback always returns success because this is best effort.
     */
    void cleanupLoadCourseIndicator(String progressIndicatorId, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Method to retrieve an LtiUserSession based on the consumer key and
     * consumer id of the lti user. A boolean parameter can be set to create the
     * session if it doesn't already exist. If successful, the response will
     * contain the user session id from the server.
     *
     * @param consumerKey The consumer key of the lti user.
     * @param consumerId The consumer id of the lti user.
     * @param callback The callback returning the results of the rpc.
     * @return
     * @throws IllegalArgumentException
     */
    Request createLtiUserSession(String consumerKey, String consumerId, AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;


    /**
     * Requests the course to be loaded from an lti user.  This must be done before the course is started
     * since the course files may need to be downloaded from the server.
     *
     * @param consumerKey - The consumer key of the lti user launching the course.
     * @param consumerId - The consumer id of the lti user launching the course.
     * @param courseId - The source course id (this should be the id of the course from the course table).
     * @param callback - The callback returning the results of the rpc.
     * @return Request - An rpc request object that can be used to cancel the rpc.
     * @throws IllegalArgumentException
     */
    Request ltiGetCourseSourcePathFromId(String consumerKey, String consumerId, String courseId, AsyncCallback<GenericRpcResponse<String>> callback) throws IllegalArgumentException;


    /**
     * Requests the course to be loaded from an lti user.  This must be done before the course is started
     * since the course files may need to be downloaded from the server.
     *
     * @param consumerKey - The consumer key of the lti user launching the course.
     * @param consumerId - The consumer id of the lti user launching the course.
     * @param courseSourcePath - The source path of the course.
     * @param callback - The callback returning the results of the rpc.
     * @return Request - An rpc request object that can be used to cancel the rpc.
     * @throws IllegalArgumentException
     */
    Request ltiLoadCourse(String consumerKey, String consumerId, String courseSourcePath, AsyncCallback<GenericRpcResponse<String>> callback) throws IllegalArgumentException;

    /**
     * Requests the experiment be loaded. This must be done before the
     * experiment is started since the course files will need to be downloaded
     * from the server.
     *
     * @param experimentId the unique id of the experiment to load.
     * @param progressId the unique id that will be used to lookup the loading progress.
     * @param callback - The callback returning the results of the rpc.
     * @return RpcResponse containing the source and runtime folders for the experiment.
     */
    Request loadExperiment(String experimentId, String progressId, AsyncCallback<LoadExperimentResponse> callback);

    /**
     * Gets the current progress of a course that is being loaded.
     *
     * @param browserSessionId - The session that is making the request (cannot be null).
     * @param courseId - The course id (domain id) to find the progress of the load (cannot be null).
     * @param callback callback - The callback returning the results of the rpc.
     * @return Request - An rpc request object that can be used to cancel the rpc.
     * @throws IllegalArgumentException
     */
    Request getLoadCourseProgress(String browserSessionId, String courseId, AsyncCallback<ProgressResponse> callback) throws IllegalArgumentException;

    /**
     * Gets the current progress of an experiment course that is being loaded.
     *
     * @param experimentProgressId - The unique id used to poll for progress (cannot be null).
     * @param callback callback - The callback returning the results of the rpc.
     * @return Request - An rpc request object that can be used to cancel the rpc.
     * @throws IllegalArgumentException
     */
    Request getLoadCourseProgress(String experimentProgressId, AsyncCallback<ProgressResponse> callback) throws IllegalArgumentException;

    /**
     * Gets the current progress of an LTI course that is being loaded for an LTI user.
     *
     * @param consumerKey The consumer key of the lti user loading the course.
     * @param consumerId The consumer id of the
     * @param courseId The source course id (this should be the source path of the course).
     * @param callback callback - The callback returning the results of the rpc.
     * @return Request - An rpc request object that can be used to cancel the rpc.
     * @throws IllegalArgumentException
     */
    Request getLoadCourseProgress(String consumerKey, String consumerId, String courseId, AsyncCallback<ProgressResponse> callback) throws IllegalArgumentException;

    /**
     * Informs the server that a load request has been cancelled.
     *
     * @param browserSessionId - The session that is making the request (cannot be null).
     * @param courseId - The course id (domain id) to cancel the load progress (cannot be null).
     * @param callback - The callback returning the results of the rpc.
     * @throws IllegalArgumentException
     */
    void cancelLoadCourse(String browserSessionId, String courseId, AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;

    /**
     * Runs the validation check to see if the course is valid.
     *
     * @param browserSessionId - The session id from the client.
     * @param courseData - The domain option (course data) that will be validated.
     * @param courseValidationParams - optional parameters to use during course validation.  Can be null if the default server side logic for course validation is wanted.
     * @param callback - The results of the rpc that include the course being validated.  If the course had an issue validating the domain option
     * should have an unavailable recommendation enumeration type and the RpcResponse should have a useful error message to show to the user with a false success value as well.
     * @throws IllegalArgumentException
     */
    Request validateCourseData(String browserSessionId, DomainOption courseData, CourseValidationParams courseValidationParams, AsyncCallback<ValidateCourseResponse> callback) throws IllegalArgumentException;

    /**
     * Checks existing survey resources for potential conflicts.
     * Imports the courses if no conflicts are found.
     *
     * @param username - the username of the user for whom to import the courses.
     * @param exportFileLocation - the server-side location of the export file to import.
     * @param callback the callback returning the results of the rpc
     */
    void checkForImportConflicts(String username, String exportFileLocation, AsyncCallback<ImportCoursesResponse> callback);
    
    /**
     * Updates the permissions for one or more users on the given course.  The
     * details of which users to add comes from a previously uploaded csv file.
     * @param filePath the path to the csv file with user permissions to add to the course.  Assumes the path is relative
     * to the Domain directory.  E.g. temp/c9e27c56-f705-41fb-9d2c-07654f03ddac/permissionsTest.csv
     * @param dataCollectionItem contains information about the published course which permissions are changing
     * @param browserSessionId the browser session id from the client making the request
     * @param callback used to notify the caller of the updated DomainOption with the current permissions for that course.  Used to refresh the client UI permissions table.
     * @throws IllegalArgumentException if any of the arguments are not properly provided.
     */
    void addCourseUserPermissionsFromFile(String filePath, DomainOption courseData, String browserSessionId, AsyncCallback<GenericRpcResponse<DomainOption>> callback) throws IllegalArgumentException;

    /**
     * Updates the permissions for one or more users on the given published course.  The
     * details of which users to add comes from a previously uploaded csv file.
     * @param filePath the path to the csv file with user permissions to add to the published course.  Assumes the path is relative
     * to the Domain directory.  E.g. temp/c9e27c56-f705-41fb-9d2c-07654f03ddac/permissionsTest.csv
     * @param dataCollectionItem contains information about the published course which permissions are changing
     * @param browserSessionId the browser session id from the client making the request
     * @param callback used to notify the caller of the updated DataCollectionItem with the current permissions for that published course.  Used to refresh the client UI permissions table.
     * @throws IllegalArgumentException if any of the arguments are not properly provided.
     */
    void addPublishedCourseUserPermissionsFromFile(String filePath, DataCollectionItem dataCollectionItem, String browserSessionId, AsyncCallback<GenericRpcResponse<DataCollectionItem>> callback) throws IllegalArgumentException;

    /**
     * Imports the export file at the specified location for the specified user
     *
     * @param username the username of the user for whom to import the courses
     * @param exportFileLocation the server-side location of the export file to import
     * @param filesToOverwrite a list of filenames that determines which existing survey
     * images will be overwritten by imported survey images. Can be null.
     * @param conflictingCourses a map of conflicting courses to be imported to their new names. Can be null.
     * @param callback the callback returning the results of the rpc
     * @throws IllegalArgumentException
     */
    void importCourses(String username, String exportFileLocation, List<String> filesToOverwrite, Map<String, String> conflictingCourses, AsyncCallback<ImportCoursesResponse> callback)
            throws IllegalArgumentException;

    /**
     * Gets the progress of the import currently being run for the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @param callback - The callback returning the import operation progress. When the import is finished, it will contain the list of courses imported.
     * @throws IllegalArgumentException if the username is not valid.
     */
    void getImportProgress(String username, AsyncCallback<LoadedProgressIndicator<List<DomainOption>>> callback) throws IllegalArgumentException;

    /**
     * Cancels the import currently running for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the import
     * @param callback The callback returning the results of the rpc.
     * @throws IllegalArgumentException
     */
    void cancelImport(String username, AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;

    /**
     * The endCourse rpc indicates that the user has ended a course.  The course data such as the domain Id of the course
     * is sent so the server can perform any needed cleanup (such as deleting the runtime folder on disk)
     * after a user completes the course.
     *
     * @param browserSessionId - The session id from the client.
     * @param domainId - The runtime id of the course that has ended.
     * @param callback The callback returning the results of the rpc.
     * @throws IllegalArgumentException
     */
    void endCourse(String browserSessionId, String domainId, AsyncCallback<RpcResponse> callback) throws IllegalArgumentException;

    /**
     * Create a new experiment for the course provided.
     *
     * @param name the name of the experiment.  Doesn't have to be unique.  Can't be null.
     * @param description information about the experiment.  Can be null.
     * @param username the user name of the user that is creating the experiment
     * @param courseId the unique identifier of the course the experiment will use
     * @param dataSetType the type of data collection set to be created.
     * @param callback The callback returning the results of the rpc.
     */
    void createExperiment(String name, String description, String username, String courseId, String dataSetType, AsyncCallback<ExperimentResponse> callback) throws IllegalArgumentException;

    /**
     * Gets the progress of the experiment creation currently being run for the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @param callback The callback returning the results of the rpc.
     */
    void getCreateExperimentProgress(String username, AsyncCallback<ProgressResponse> callback) throws IllegalArgumentException;

    /**
     * Cancels the experiment creation currently running for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the experiment creation
     * @param callback The callback returning the results of the rpc.
     */
    void cancelCreateExperiment(String username, AsyncCallback<RpcResponse> callback);

    /**
     * Gets the list of experiments authored by the given user
     *
     * @param username the username of the author
     * @param callback The callback returning the results of the rpc.
     */
    void getExperiments(String username, AsyncCallback<ExperimentListResponse> callback);

    /**
     * Deletes the given experiment for the given user
     *
     * @param username the username of user deleting the experiment
     * @param experimentId the experiment's ID
     * @param callback The callback returning the results of the rpc
     */
    void deleteExperiment(String username, String experimentId, AsyncCallback<DetailedRpcResponse> callback);

    /**
     * Gets the progress of the experiment deletion currently being run for the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @param callback The callback returning the results of the rpc.
     */
    void getDeleteExperimentProgress(String username, AsyncCallback<ProgressResponse> callback) throws IllegalArgumentException;

    /**
     * Cancels the experiment deletion currently running for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the experiment deletion
     * @param callback The callback returning the results of the rpc.
     */
    void cancelDeleteExperiment(String username, AsyncCallback<RpcResponse> callback);

    /**
     * Exports the course used by the given experiment for the given user
     *
     * @param username the username of user
     * @param experimentId the experiment's ID
     * @param callback The callback returning the results of the rpc.
     */
    void exportExperimentCourse(String username, String experimentId, AsyncCallback<ExportResponse> callback);

    /**
     * Gets the progress of the experiment course export currently being run for the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @param callback The callback returning the results of the rpc.
     */
    void getExportExperimentCourseProgress(String username, AsyncCallback<ProgressResponse> callback);

    /**
     * Cancels the experiment course export currently running for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the experiment course export
     * @param callback The callback returning the results of the rpc.
     */
    void cancelExportExperimentCourse(String username, AsyncCallback<RpcResponse> callback);

    /**
     * Calculates the size of course export of the specified experiment for the specified user
     *
     * @param username the username of user
     * @param experimentId the experiment's ID
     * @param callback The callback returning the results of the rpc.
     */
    void calculateExperimentCourseExportSize(String username, String experimentId, AsyncCallback<DoubleResponse> callback);


    /**
     * Exports the raw data from the given experiment for the given user
     *
     * @param username the username of user
     * @param experimentId the experiment's ID
     * @param exportConvertedBinaryLogs true to also export the converted human readable binary files.
     * @param callback The callback returning the results of the rpc.
     */
    void exportExperimentRawData(String username, String experimentId, boolean exportConvertedBinaryLogs, AsyncCallback<ExportResponse> callback);

    /**
     * Gets the progress of the experiment raw data export currently being run for the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @param callback The callback returning the results of the rpc.
     */
    void getExportExperimentRawDataProgress(String username, AsyncCallback<ProgressResponse> callback);

    /**
     * Cancels the experiment raw data export currently running for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the experiment course export
     * @param callback The callback returning the results of the rpc.
     */
    void cancelExportExperimentRawData(String username, AsyncCallback<RpcResponse> callback);


    /**
     * Calculates the size of raw data export of the specified experiment for the specified user
     *
     * @param username the username of user
     * @param experimentId the experiment's ID
     * @param callback The callback returning the results of the rpc.
     */
    void calculateExperimentRawDataExportSize(String username, String experimentId, AsyncCallback<DoubleResponse> callback);

    /**
     * Updates the given experiment
     *
     * @param username the username of user
     * @param experimentId the ID of the experiment to update
     * @param name the updated name
     * @param description the updated description
     * @param callback The callback returning the results of the rpc.
     */
    void updateExperiment(String username, String experimentId, String name, String description, AsyncCallback<ExperimentResponse> callback);

    /**
     * Validates the course belonging to the given experiment for the given user
     *
     * @param username the username of user deleting the experiment
     * @param experimentId the experiment's ID
     * @return the results of the rpc
     * @param callback The callback returning the results of the rpc.
     */
    void validateExperimentCourse(String username, String experimentId, AsyncCallback<DetailedRpcResponse> callback);

    /**
     * Creates an error report file using the given information and provides a location that it can be accessed from
     *
     * @param userName the name of the user who initiated the call
     * @param reason the error reason. Can't be null.
     * @param errorDetailsList the list of errors including the details and optional stack trace for each.
     * @param date when the error happened
     * @param courseName the name of the course the error happened in (optional - can be null)
     * @param asyncCallback The callback returning the results of the rpc.
     */
    void exportErrorFile(String userName, String reason, List<ErrorDetails> errorDetailsList, Date date, String courseName, AsyncCallback<ExportResponse> asyncCallback);

    /**
     * Gets the experiment with the given experiment ID
     *
     * @param username the user requesting the experiment
     * @param experimentId the ID of the experiment to get
     * @param callback The callback containing the experiment
     */
    void getExperiment(String username, String experimentId, AsyncCallback<ExperimentResponse> callback);

    /**
     * Sets whether or not the given experiment is active
     *
     * @param username the name of the user who initiated the request
     * @param experimentId the ID of the experiment to update
     * @param active whether or not the experiment should be active
     * @param callback The callback containing the updated experiment
     */
    void setExperimentActive(String username, String experimentId, boolean active, AsyncCallback<ExperimentResponse> callback);

    /**
     * Generates and exports an experiment report for the given experiment for the given user
     *
     * @param username the username of user
     * @param experimentId the experiment's ID
     * @param reportProperties the properties to use to generate the report
     * @param the callback containing the results of the RPC
     */
    void exportExperimentReport(String username, String experimentId, ReportProperties reportProperties,
            AsyncCallback<DetailedRpcResponse> callback);


    /**
     * Gets the progress of the experiment report export currently being run for the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @param the callback containing the results of the RPC
     */
    void getExportExperimentReportProgress(String username, AsyncCallback<GenerateReportStatusResponse> callback);


    /**
     * Cancels the experiment report export currently running for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the experiment report export
     * @param the callback containing the results of the RPC
     */
    void cancelExportExperimentReport(String username, AsyncCallback<RpcResponse> callback);

     /**
     * Gets the progress of the delete operation on the server
     *
     * @param browserSessionId - The session id from the client.
     * @param callback - The callback returning the results of the rpc.
     */
    void getDeleteProgress(String browserSessionId, AsyncCallback<ProgressResponse> callback);

    /**
     * Gets the progress of the get course list operation on the server
     *
     * @param browserSessionId - The user session id from the client.
     * @param callback - The callback returning the results of the rpc.
     */
    void getCourseListProgress(String browserSessionId, AsyncCallback<ProgressResponse> callback);

    /**
     * Gets the progress of the copy operation on the server
     *
     * @param browserSessionId - The session id from the client.
     * @param callback - The callback returning the results of the rpc.
     */
    void getCopyProgress(String browserSessionId, AsyncCallback<LoadedProgressIndicator<CopyCourseResult>> callback);

    /**
     * Gets the progress of the course validation on the server
     *
     * @param browserSessionId the unique id of the session from the client used to lookup
     * the currently running course validation progress indicator.
     * @param courseId unique id of the course validation progress wanting to return.  Currently not used.
     * @param callback the callback for returning the results of the RPC.  In this case it is the current progress of the
     * course validation logic.  If no course validation progress object was found for the user a non-successful response
     * will be returned.  This can happen when the course validation finished and was removed from the server's memory
     * since the last check to get course progress.
     * @return the GWT RPC handle to this server request.  This can be used to cancel the request from the client side.
     */
    Request getValidateCourseProgress(String browserSessionId, String courseId, AsyncCallback<ProgressResponse> callback);


     /**
     * Attempts to delete the specified course(s)
     *
     * @param browserSessionId - The session id from the client.
     * @param courses - The courses to delete
     * @param deleteSurveyResponses - True to delete survey responses. Otherwise, the client will be notified if the survey context contains survey responses
     * @param skipSurveyResources - True to skip deleting the survey context. To be used if deleting the survey context and responses fails.
     * @param callback - The callback returning the results of the rpc.
     */
    void deleteCourses(String browserSessionId, List<DomainOption> courses, boolean deleteSurveyResponses, boolean skipSurveyResources, AsyncCallback<DeleteCourseResult> callback);

    /**
     * Attempts to copy the specified course to the users workspace.
     *
     * @param browserSessionId - The session id from the client.  Used to look up the username.
     * @param newCourseName - The name to give the copied course.  This is useful in case the course is being copied to the
     * same workspace which would cause a course name collision.
     * @param courseData - The domain option (course data) to copy.  This contains the location of the course being copied.
     * @param coursesList - The list of courses available to the user.  This is used to check for course name conflicts.  If empty
     * than course name collisions are not checked for and the course being copied could be merged with another course with the same name.
     * @return the results of the rpc.  Use {@link #getCopyProgress(String)} with to get the progress of this course copy operation
     * including whether a course name collision happened.
     */
    void copyCourse(String browserSessionId, String newCourseName, DomainOption courseData, List<DomainOption> coursesList, AsyncCallback<DetailedRpcResponse> callback);

    /**
     * Attempts to publish a course by copying the course from a user's workspace to the Public workspace.
     *
     * @param browserSessionId - The session id from the client.  Used to look up the username.
     * @param newCourseName - The name to give the copied course.  This is useful in case the course being created from the copy
     * would cause a course name collision.
     * @param courseData - The domain option (course data) to copy.  This contains the location of the course being copied.
     * @return the results of the rpc.  Use {@link #getCopyProgress(String)} with to get the progress of this course copy operation
     * including whether a course name collision happened.
     * @throws DetailedException if there was a problem (other than name collision) during the publishing process
     */
    void publishCourseToPublic(String browserSessionId, String newCourseName, DomainOption courseData, AsyncCallback<DetailedRpcResponse> callback) throws DetailedException;

    /**
     * Retrieves the active {@link AbstractKnowledgeSession knowledge sessions}.
     *
     * @param callback the callback returning the list of active knowledge sessions.
     */
    void fetchActiveKnowledgeSessions(AsyncCallback<GetActiveKnowledgeSessionsResponse> callback);

    /**
     * Registers a user as an {@link AbstractKnowledgeSession} monitor.
     *
     * @param browserSessionKey the key of the browser session being used to monitor the knowledge
     *        session.
     * @param knowledgeSession the session being monitored.
     * @param callback the callback returning whether or not registering completed successfully.
     */
    void registerKnowledgeSessionMonitor(String browserSessionKey, AbstractKnowledgeSession knowledgeSession,
            AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * De-registers a user from being an {@link AbstractKnowledgeSession}
     * monitor.
     *
     * @param browserSessionKey the key of the browser session being used to
     *        monitor the knowledge session.
     * @param knowledgeSession the session no longer being monitored.
     * @param callback the callback returning whether or not de-registering
     *        completed successfully.
     */
    void deregisterKnowledgeSessionMonitor(String browserSessionKey, AbstractKnowledgeSession knowledgeSession,
            AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Request that the log index file be updated for the log metadata provided.
     *
     * @param browserSessionId the client session requesting this update.  Used to track progress
     * for that client.
     * @param logMetadata contains information about a domain session log and that information
     * should be updated in the log index file.
     * @param callback used to notify the client that the server has started to handle the update to the log index.
     * Use {@link #getUpdateLogIndexProgress(String, AsyncCallback)} for progress updates.
     */
    void updateLogIndex(String browserSessionId, LogMetadata logMetadata, AsyncCallback<RpcResponse> callback);

    /**
     * Get the current server side progress of updating the log index file with updates to a
     * specific log metadata entry.
     * @param browserSessionId the client session requesting the log index update.  Used to track progress
     * for that client.
     * @param callback used to provide the client with the current progress on the server corresponding to
     * a previous request to update the log index.
     */
    void getUpdateLogIndexProgress(String browserSessionId, AsyncCallback<LoadedProgressIndicator<Void>> callback);

    /**
     * Request the list of domain session logs that contains DKF lessons available for playback
     * purposes.
     * @param browserSessionId the client session requesting this information.  Used to track progress
     * for that client.
     * @param callback used to provide notification that the server is handling the request
     */
    void fetchLogsAvailableForPlayback(String browserSessionId, AsyncCallback<RpcResponse> callback);

    /**
     * Get the current server side progress of retrieving the list of domain
     * session logs based on the clients request for that information.
     *
     * @param browserSessionId the client session requesting this information.
     *        Used to track progress for that client.
     * @param callback used to provide progress so far
     */
    void getFetchLogsAvailableForPlaybackProgress(String browserSessionId, AsyncCallback<LoadedProgressIndicator<Collection<LogMetadata>>> callback);

    /**
     * Registers the specified browser session to playback a specified log file.
     *
     * @param browserSessionKey The unique identifier of the browser making the
     *        request.
     * @param log The {@link LogMetadata} that specifies which log file should
     *        be played back. Can't be null.
     * @param callback The {@link AsyncCallback} that is invoked when the
     *        operation has completed. Can't be null.
     */
    void registerKnowledgeSessionPlayback(String browserSessionKey, LogMetadata log, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Permanently stops playback of a specified browser session's currently
     * playing log file.
     *
     * @param browserSessionKey The unique identifier of the browser making the
     *        request.
     * @param callback The {@link AsyncCallback} that is invoked when the
     *        operation has completed. Can't be null.
     */
    void deregisterKnowledgeSessionPlayback(String browserSessionKey, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Sets the time of the knowledge session playback being run by the browser
     * session with the given key to when the given task or concept is
     * activated.
     *
     * @param browserSessionKey the key of the browser session being used to
     *        play back a knowledge session. Cannot be null.
     * @param taskConceptName the name of the task or concept to use to find the
     *        activation start time. Can't be null or blank.
     * @param callback The {@link AsyncCallback} that is invoked when the
     *        operation has completed. Can't be null.
     */
    void jumpToActivationStart(String browserSessionKey, String taskConceptName,
            AsyncCallback<GenericRpcResponse<Long>> callback);

    /**
     * Fetches a mapping of a simulation times to {@link LearnerState} objects
     * that were sent at that time for the currently playing log file.
     *
     * @param browserSessionKey The unique identifier of the browser session
     *        making the request. Can't be null.
     * @param callback The {@link AsyncCallback} that is invoked when the
     *        operation has completed. If successful it will be provided the
     *        mapping of simulation epoch time stamps to the
     *        {@link LearnerState} at that given time.
     */
    void fetchLearnerStatesForSession(String browserSessionKey, AsyncCallback<GenericRpcResponse<SessionTimelineInfo>> callback);

    /**
     * Edits a patch for the changes made in the given
     * {@link PerformanceStateAttribute}.
     * 
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @param username The username of the person making the edits.
     * @param timestamp the timestamp of the edit.
     * @param performanceState the performance state that contains the edits
     *        made.
     * @param callback the callback returning whether or not creating the log
     *        patch completed successfully. Contains the log patch file name if
     *        it was created.
     */
    void editLogPatchForPerformanceStateAttribute(String browserSessionKey, String username, long timestamp,
            PerformanceStateAttribute performanceState, AsyncCallback<GenericRpcResponse<String>> callback);

    /**
     * Creates a patch for the changes made in the given
     * {@link EvaluatorUpdateRequest}.
     * 
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @param username The username of the person making the edits.
     * @param timestamp the timestamp of the edit.
     * @param updateEntireSpan true to apply the patch to the entire message
     *        span; false to apply it starting at the provided timestamp (if no
     *        message exists, one will be created).
     * @param evaluatorUpdateRequest the evaluator update that contains the
     *        edits made.
     * @param applyToFutureStates whether the update request should be applied to
     *        future learner states as well.
     * @param callback the callback returning whether or not creating the log
     *        patch completed successfully. Contains the log patch file name if
     *        it was created.
     */
    void createLogPatchForEvaluatorUpdate(String browserSessionKey, String username, long timestamp, boolean updateEntireSpan,
            EvaluatorUpdateRequest evaluatorUpdateRequest, boolean applyToFutureStates, AsyncCallback<GenericRpcResponse<String>> callback);

    /**
     * Removes a patch for a {@link PerformanceStateAttribute}.
     * 
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @param username The username of the person making the edits.
     * @param timestamp the timestamp of the deletion.
     * @param performanceState the performance state that is to be deleted.
     * @param callback the callback returning whether or not creating the log
     *        patch completed successfully. Contains the log patch file name if
     *        it was created.
     */
    void removeLogPatchForAttribute(String browserSessionKey, String username, long timestamp,
            PerformanceStateAttribute performanceState, AsyncCallback<GenericRpcResponse<String>> callback);

    /**
     * Delete the session log patch.
     *
     * @param browserSessionKey The unique identifier of the browser making the
     *        request. Can't be blank.
     * @param callback The {@link AsyncCallback} that is invoked when the
     *        operation has completed. Can't be null.
     */
    void deleteSessionLogPatch(String browserSessionKey, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Sets the time of the knowledge session playback being run by the browser session with the given key
     *
     * @param browserSessionKey the key of the browser session being used to play back a knowledge session. Cannot be null.
     * @param time the time to set the session playback to
     * @param callback the callback returning whether or not setting the playback time completed successfully.
     */
    void setSessionPlaybackTime(String browserSessionKey, long time, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Starts the knowledge session playback associated with the browser session with the given key
     *
     * @param browserSessionKey the key of the browser session being used to play back a knowledge session. Cannot be null.
     * @param callback the callback returning whether or not the operation completed successfully.
     */
    void startSessionPlayback(String browserSessionKey, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Stops the knowledge session playback associated with the browser session with the given key
     *
     * @param browserSessionKey the key of the browser session being used to play back a knowledge session. Cannot be null.
     * @param callback the callback returning whether or not the operation completed successfully.
     */
    void stopSessionPlayback(String browserSessionKey, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Updates the game master session state as being in 'auto' or 'manual' mode. In 'auto' mode,
     * all incoming strategy requests will be automatically approved with the suggested actions.
     *
     * @param browserSessionKey the key of the browser session being used to monitor the knowledge
     *        session.
     * @param isAutoMode true if the game master session is in 'auto' mode; false otherwise.
     * @param callback the callback returning whether or not de-registering completed successfully.
     */
    void updateGameMasterAutoState(String browserSessionKey, boolean isAutoMode,
            AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Caches the processed strategies referenced by the
     * {@link ProcessedStrategyCache} items.
     *
     * @param browserSessionKey the key of the browser session being used to
     *        monitor the knowledge session.
     * @param strategyCache the collection of processed strategies to cache.
     * @param callback the callback returning whether or not caching completed
     *        successfully.
     */
    void cacheProcessedStrategy(String browserSessionKey, Collection<ProcessedStrategyCache> strategyCache,
            AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Update the team org filter state (selected or not selected for each team
     * and team member).
     *
     * @param browserSessionKey the key of the browser session being used to
     *        monitor the knowledge session that these entities belong to.
     * @param domainSessionId the id of the domain session being updated.
     * @param teamRolesSelected the collection of team roles (team and team
     *        members) and whether or not they have been selected to be shown.
     * @param callback the callback returning whether or not updating the team
     *        org filter state completed successfully.
     */
    void updateSessionTeamOrgFilterState(String browserSessionKey, int domainSessionId, Map<String, Boolean> teamRolesSelected, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Sets the gateway connections that monitored game state information should be shared with with when playing
     * back a session
     *
     * @param browserSessionKey the key of the browser session making the request. Cannot be null.
     * @param gatewayConnection the gateway connection to share entity state information with. If not null, the connections
     * will be established as needed. If different than the last gateway connection, appropriate existing connections will be closed.
     * @param callback a callback containing the result of whether the operation completed successfully
     */
    void setGatewayConnections(String browserSessionKey, GatewayConnection gatewayConnection,
            AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Sets the configuration that external monitor applications should use when they are connected and sharing data
     *
     * @param browserSessionKey the key of the browser session making the request. Cannot be null.
     * @param config the configuration to use. Cannot be null.
     * @param callback a callback containing the result of whether the operation completed successfully
     */
    void setExternalMonitorConfig(String browserSessionKey, ExternalMonitorConfig config,
            AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Gets the gateway connections that GIFT is currently sharing knowledge session data with 
     * to monitor said data outside of GIFT
     *
     * @param browserSessionKey the key of the browser session making the request. Cannot be null.
     * @param callback a callback containing the gateway connections that data is being shared with
     */
    void getGatewayConnection(String browserSessionKey, AsyncCallback<GenericRpcResponse<GatewayConnection>> callback);

    /**
     * Properly releases resources that were allocated on the server in relation
     * to the game master web application. This method should be called when a
     * browser stops using the game master.
     *
     * @param browserSessionKey The unique identifier of the browser that is
     *        making this request. Can't be null.
     * @param callback The {@link AsyncCallback} that is invoked when the
     *        request has been fulfilled. A {@link GenericRpcResponse} object is
     *        provided to the callback that details whether or not the request
     *        was processed successfully.
     */
    void cleanupGameMaster(String browserSessionKey, AsyncCallback<GenericRpcResponse<Void>> callback);
    
    /**
     * Gets the SIDC for the military symbol that should be used to represent training application entities with the given
     * role within the knowledge session with the given domain session ID. This can be used to determine what symbol should 
     * be used to represent team roles that haven't been drawn yet due to being filtered out by the session's entity filter.
     * 
     * @param browserSessionKey the key of the browser session requesting the SIDC
     * @param domainSessionId the domain session ID of the knowledge session that the team role is part of
     * @param roleName the name of the team role that a SIDC is being requested for
     * @param callback a callback providing the result containing the SIDC corresponding to the given team role. 
     * The content of the result can be null if no entity data has been processed for an entity with the given 
     * team role or one of its child roles.
     */
    void getSidcForRole(String browserSessionKey, int domainSessionId, String roleName, 
            AsyncCallback<GenericRpcResponse<SIDC>> callback);
    
    /**
     * Gets a URL that can be used to preview the workspace file at the given location so that it can be
     * viewed in the given user's browser
     * 
     * @param userName the user name of the user making the request. Needed to determine if the user has 
     * permission to access the given workspace file. Cannot be null.
     * @param relativeFileName the path to the workspace file to preview, relative to the root workspace folder.
     * Cannot be null.
     * @param callback the callback providing the response that contains the returned preview URL
     */
    void getWorkspaceFilePreviewUrl(String userName, String relativeFileName, AsyncCallback<GenericRpcResponse<String>> callback);

    /**
     * Caches the processed bookmarks referenced by the
     * {@link ProcessedBookmarkCache} items.
     *
     * @param browserSessionKey the key of the browser session being used to
     *        monitor the knowledge session.
     * @param cache the  processed bookmark to cache.
     * @param callback the callback returning whether or not caching completed
     *        successfully.
     */
    void cacheProcessedBookmark(String browserSessionKey, ProcessedBookmarkCache cache,
            AsyncCallback<GenericRpcResponse<Void>> asyncCallback);
    
    /**
     * Requests the log corresponding to the given knowlegde session so that it can be played back
     * 
     * @param browserSessionId the client session requesting this information. Cannot be null.
     * @param activeSession the active knowledge session that we want to find a log for. Cannot be null.
     * @param callback the callback providing the response containing the log file that was found
     */
    void fetchLogForPlayback(String browserSessionId,
            AbstractKnowledgeSession activeSession, AsyncCallback<GenericRpcResponse<LogMetadata>> callback);
    
    /**
     * Gets scenario info about the knowledge session associated with the given browser session 
     * 
     * @param browserSessionKey browserSessionId the client session requesting this information. Cannot be null.
     * @param username the user name of the user making the request. Cannot be null.
     * @param callback a callback that handles the response containing information about the knowledge session scenario
     */
    void getKnowledgeSessionScenario(String browserSessionKey, String username, AsyncCallback<GenericRpcResponse<SessionScenarioInfo>> callback);

    /**
     * Applies the given condition assessments as a patch to the knowledge session playback for the given
     * browser session and publishes the calculated score
     * 
     * @param browserSessionKey the client session requesting this information. Cannot be null.
     * @param username the user name of the user making the request. Cannot be null.
     * @param timestamp the timestamp during the knowledge session at which this assessment was provided.
     * @param conceptToConditionAssessments a mapping from each concept ID to the assessments that were
     * provided to that concept's conditions by the observer controller. Cannot be null.
     * @param courseConcepts the course concepts. Cannot be null.
     * @param callback a callback that handles the response indicating whether the operation completed successfully.
     *        Contains the log patch file name if it was created.
     */
    void publishKnowledgeSessionOverallAssessments(String browserSessionKey, String username, long timestamp,
            Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments, List<String> courseConcepts, 
            AsyncCallback<GenericRpcResponse<String>> callback);
    
    /**
     * Calculates the assessment levels for the parent performance nodes of the given condition assessments
     * 
     * @param browserSessionKey the client session requesting this information. Cannot be null.
     * @param username the user name of the user making the request. Cannot be null.
     * @param conceptToConditionAssessments a mapping from each leaf concept to the assessment levels that were assigned
     * to each of its conditions by the observer controller. Cannot be null.
     * @param callback a callback the handles the response containing the calculated assessment levels of all the parent nodes. 
     * Cannot be null.
     */
    void calculateRollUp(String browserSessionKey, String username,
            Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments, AsyncCallback<GenericRpcResponse<Map<Integer, AssessmentLevelEnum>>> callback);
   
    /**
     * Registers the given browser session to interact with the web monitor service and
     * and receive updates from it
     * 
     * @param username the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param the callback that handles the response indicating whether the operation was successful. Cannot be null.
     */
    void registerMonitorService(String username, String browsers, AsyncCallback<GenericRpcResponse<WebMonitorStatus>> callback);
    
    /**
     * Deregisters the given browser session to stop interacting with the web monitor service and
     * and stop receiving updates from it
     * 
     * @param username the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param the callback that handles the response indicating whether the operation was successful. Cannot be null.
     */
    void deregisterMonitorService(String username, String browsers, AsyncCallback<GenericRpcResponse<Void>> callback);
    
    /**
     * Handles module launch requests
     * 
     * @param username name of the user launching a module(s). Cannot be null.
     * @param browsers the client session requesting this information. Cannot be null.
     * @param modules a list of one of more modules to launch. Cannot be null.
     * @param callback a callback handling the response.
     */
    void launchModules(String username, String browsers, List<ModuleTypeEnum> modules, AsyncCallback<GenericRpcResponse<Void>> callback);
    
    /**
     * Handles module stop requests
     * 
     * @param username name of the user stopping a module(s). Cannot be null.
     * @param browsers the client session requesting this information. Cannot be null.
     * @param modules a list of one of more modules to kill. Cannot be null.
     * @param targetQueue the name of the specific queue to kill. If null, ALL queues with the
     * target modules types will be killed.
     * @param callback a callback handling the response.
     */
    void killModules(String username, String browsers, List<ModuleTypeEnum> modules, String targetQueue, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Gets the full display information for the message with the given metadata.
     * 
     * @param the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the ID of the domain session associated with the message. Can be null for 
     * system messages.
     * @param message the metadata uniquely identifying the message that is being requested.
     * @param a callback that returns a response containing the requested message data. Cannot be null.
     */
    void getMessageDisplayData(String username, String browserSession, Integer domainSessionId, MessageEntryMetadata message, AsyncCallback<GenericRpcResponse<MessageDisplayData>> callback);
    
    /**
     * Toggles the state of the web monitor listening for messages 
     * 
     * @param browserSession the client session requesting this information. Cannot be null.
     * @param listening the flag indicating whether the web monitor listening for message updates
     * @param domainSessionId the ID of the domain session who toggled the listening button. Can be null for system
     *        messages.
     * @param a callback containing a response indicating whether the operation was successful. Cannot be null.
     */
    void setListening(String browserSession, boolean listening, Integer domainSessionId, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Begins monitoring the domain session with the given ID using the web monitor
     * 
     * @param the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the ID of the domain session to monitor. Cannot be null.
     * @param a callback containing a response indicating whether the operation was successful. Cannot be null.
     */
    void watchDomainSession(String username, String browserSession, int domainSessionId, AsyncCallback<GenericRpcResponse<Void>> callback);
    
    /**
     * Filters domain tab messages by entityMarking
     * 
     * @param entityMarking the filter to apply to the domain tab display list
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the ID of the domain session to monitor. Cannot be null.
     * @param callback a response indicating whether the operation was successful. Cannot be null.
     */
    void entityFilter(String entityMarking, String browserSession, int domainSessionId, AsyncCallback<GenericRpcResponse<Void>> callback);
    
    /**
     * Toggles the state of the web monitor's message display panel showing advanced information
     * 
     * @param browserSession the client session requesting this information. Cannot be null.
     * @param advancedHeader the flag indicating whether the message panel should show advanced info
     * @param domainSessionId the ID of the domain session who toggled the advanced header checkbox. Can be null for system
     *        messages.
     * @param a callback containing a response indicating whether the operation was successful. Cannot be null.
     */
    void setAdvancedHeader(String browserSession, boolean advancedHeader, Integer domainSessionId, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Sets the message types to filter for the given domain session
     * 
     * @param userSession the name of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the domain session ID of the filter that is being update. Can be null.
     * @param selectedChoices the new choices provided by the user for what messages should be shown. Cannot be null.
     * @param a callback containing a response indicating whether the operation was successful. Cannot be null.
     */
    void setMessageFilter(String username, String browserSession, Integer domainSessionId,
            Set<MessageTypeEnum> selectedChoices, AsyncCallback<GenericRpcResponse<Void>> callback);
    /**
     * Closes a domain session tab
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the domain session ID of the filter that is being update. Can be null.
     * @param callback a callback containing a response indicating whether the operation was successful. Cannot be null.
     */
    void unwatchDomainSession(String browserSession, Integer domainSessionId, AsyncCallback<GenericRpcResponse<Void>> callback);
    
    /**
     * Refreshes the message information stored in the web monitor panel for the given domain session
     * 
     * @param username the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the ID of the domain session whose panel is being updated. Cannot be null.
     * @return callback a callback containing a response indicating whether the operation was successful. Cannot be null.
     */
    void refreshPanel(String username, String browserSession, Integer domainSessionId, AsyncCallback<GenericRpcResponse<Void>> callback);

    /**
     * Refreshes module status information for the given browser session
     * 
     * @param username the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param callback a callback containing a response indicating whether the operation was successful. Cannot be null.
     */
    void refreshModules(String username, String browserSession, AsyncCallback<GenericRpcResponse<Void>> callback);
}
