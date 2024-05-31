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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

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
 * The dashboard service defines the rpc interface between the client & server.
 * @author nblomberg
 *
 */
@RemoteServiceRelativePath("rpc")
public interface DashboardService extends RemoteService {

    /**
     * Return the server properties that have been identified as important for the
     * dashboard client.
     *
     * @return ServerProperties the properties needed by the dashboard client
     */
    ServerProperties getServerProperties();
    
    /**
     * Login a user
     *
     * @param username The username of the user to login
     * @param password the password of the user
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    LoginResponse loginUser(String username, String password, String loginAsUserName)
            throws IllegalArgumentException;

    /**
     * Attempts to login from an existing session on the server.
     * If this returns success, the AdditionalInformation parameter
     * in the RpcReponse will contain the userName.
     *
     * @return RpcResponse - Returns true if success, false otherwise.
     * @throws IllegalArgumentException
     */
    ExistingSessionResponse loginFromExistingSession(String browserSessionId) throws IllegalArgumentException;

    /**
     * Logs out the user.
     * @return RpcResponse - true if success on the server, false otherwise.
     * @throws IllegalArgumentException
     */
    RpcResponse logoutUser() throws IllegalArgumentException;


    /**
     * Updates the user state on the server.  This rpc makes sure the server is aware of the
     * state that the client session is in.  The server is the authority on the state.  If the
     * user has logged out (on another browser), then this can return a failure.
     *
     * @param browserSessionId - the browser session id from the client.
     * @param state - The state that the client is going to (currently a screen enum).
     * @return RpcResponse - true if success on the server, false otherwise.
     * @throws IllegalArgumentException
     */
    RpcResponse updateUserState(String browserSessionId, ScreenEnum state) throws IllegalArgumentException;

    /**
     * Updates the permissions for one or more users on the given course (and the course's survey context)
     *
     * @param permissions the permissions to set, can have a null permission enum to clear a user's permissions.
     * @param courseData contains information about the course which permissions are changing
     * @param browserSessionId the browser session id from the client
     * @return true if success on the server, false otherwise.  Null can be returned if the user session is null.
     * @throws IllegalArgumentException  if any of the arguments are not properly provided.
     */
    RpcResponse updateCourseUserPermissions(Set<DomainOptionPermissions> permissions, DomainOption courseData, String browserSessionId) throws IllegalArgumentException;

    /**
     * Gets the progress of updating the user permissions of a course that was requested by the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @return - The progress of the permissions update.  Can be null if there was no update operation
     * associated with this user.
     */
    ProgressResponse getUpdateCourseUserPermissionsProgress(String username);

    /**
     * Updates the permissions for one or more users on the given course (and the course's survey context).  The
     * details of which users to add comes from a previously uploaded csv file.
     * @param filePath the path to the csv file with user permissions to add to the course.  Assumes the path is relative
     * to the Domain directory.  E.g. temp/c9e27c56-f705-41fb-9d2c-07654f03ddac/permissionsTest.csv
     * @param courseData contains information about the course which permissions are changing
     * @param browserSessionId the browser session id from the client making the request
     * @return the updated DomainOption with the current permissions for that course.  Used to refresh the client UI permissions table.
     * @throws IllegalArgumentException if any of the arguments are not properly provided.
     */
    GenericRpcResponse<DomainOption> addCourseUserPermissionsFromFile(String filePath, DomainOption courseData, String browserSessionId) throws IllegalArgumentException;
    
    /**
     * Updates the permissions for one or more users on the given published course.  The
     * details of which users to add comes from a previously uploaded csv file.
     * @param filePath the path to the csv file with user permissions to add to the published course.  Assumes the path is relative
     * to the Domain directory.  E.g. temp/c9e27c56-f705-41fb-9d2c-07654f03ddac/permissionsTest.csv
     * @param dataCollectionItem contains information about the published course which permissions are changing
     * @param browserSessionId the browser session id from the client making the request
     * @return the updated DataCollectionItem with the current permissions for that published course.  Used to refresh the client UI permissions table.
     * @throws IllegalArgumentException if any of the arguments are not properly provided.
     */
    GenericRpcResponse<DataCollectionItem> addPublishedCourseUserPermissionsFromFile(String filePath, DataCollectionItem dataCollectionItem, String browserSessionId) throws IllegalArgumentException;

    /**
     * Updates the permissions for one or more users on the given published course
     *
     * @param permissions the permissions to set, can have a null permission enum to clear a user's permissions.
     * @param dataCollectionId the unique id of the published course to update user permission on
     * @param browserSessionId the browser session id from the client
     * @param callback to be used to hold the RpcReponse. True if success on the server, false otherwise.  Null can be returned if the user session is null.
     * @return true if success on the server, false otherwise.  Null can be returned if the user session is null.
     * @throws IllegalArgumentException  if any of the arguments are not properly provided.
     */
    RpcResponse updatePublishedCourseUserPermissions(Set<DataCollectionPermission> permissions, String dataCollectionId, String browserSessionId) throws IllegalArgumentException;

    /**
     * Gets the course using its unique ID
     *
     * @param browserSessionId The session id from the client.
     * @param courseId the unique ID for the course being retrieved.
     * @return the DomainOption course object
     */
    GenericRpcResponse<DomainOption> getCourseById(String browserSessionId, String courseId);

    /**
     * Gets the course list of the user, which is the available courses that the user has access to.
     * The rpc will check to make sure the user has a session and if so, find the courses for the user.
     * If the user is not in a session, then the rpc 'success' flag will be 'false'.
     *
     * @param browserSessionId - The browser session id from the client.
     * @param validateCoursePaths - Whether or not to validate course paths in the user's workspace
     * @param provideSimpleList - whether or not to retrieve the list of course w/o applying any additional validation, recommendations, etc.
     * Currently if false this method may apply LMS course recommendations for the user (if there are applicable course records and the property
     * for applying recommendations is true)
     * @param filter the filter used to specify which courses to return to the client. If null, no filtering is applied and all courses the user
     * has access to are returned.
     * @return GetCourseListResponse - Returns true if success, false otherwise.
     */
    GetCourseListResponse getCourseList(String browserSessionId, boolean validateCoursePaths, boolean provideSimpleList, CourseListFilter filter);

    /**
     * Corrects the paths for the courses in the given list to ensure that the names of each course's folder, .course.xml file,
     * and Course object match and do not cause conflicts
     *
     * @param browserSessionId the ID identifying the browser session invoking this operation
     * @param courseList the list of courses that need to be corrected
     * @return a response indicating the success or failure of the operation
     */
    CorrectCoursePathsResult correctCoursePaths(final String browserSessionId, List<DomainOption> courseList);

    /**
     * Checks to see if a url is 'reachable'.  Where reachable means that a '200' http code is
     * returned.  If the url returns a 200 http response, then the response to the client will be success.
     * If a non 200 http response code is returned, then the response to the client will be failure.
     *
     * @param url - The url to check (should be in form:  "http://www.somehost.com"  query parameters are optional.
     * @return - true if url returns http response of 200, false otherwise.
     * @throws IllegalArgumentException
     */
    RpcResponse isUrlReachable(String url) throws IllegalArgumentException;

    /**
     * Retrieves all the GIFT users that are currently in the UMS database.  This normally means these users
     * have logged into this GIFT instance successfully in the lifetime of the UMS database.<br/>
     * Note: this will return an empty list when running GIFT in server mode.
     *
     * @return GetUsernamesResponse if the action was successful
     */
    GetUsernamesResponse getUsernames();

    /**
     * Logs in the user in offline mode
     * @return LoginResponse if the action was successful
     */
	LoginResponse loginUserOffline(String username) throws IllegalArgumentException;

	/**
     * Retrieves the userId of a given user
     * @param username the user to retrieve a userId from
     * @return Rpcresponse if the action was successful
     */
	RpcResponse getUserId(String username);


	/**
	 * Retrieves lms data for a specified user.  The Lms data can be retrieved in batches, by specifying a starting
	 * record index, along with a 'pagesize' of how many records to retrieve.
	 *
	 * @param browserSessionId - The session id of the browser.
	 * @param domainId - An optional ID identifying a specific course from which LMS data should be retrieved.<br/>
     * e.g. Public/TSP 07-GFT-0137 Vignettes/TSP 07-GFT-0137 ClearBldg.jtc_shakarat.course.xml
     * @param domainSessionId - An optional domain session id used to retrieve LMS data for that specific session.
	 * @param indexStart - The starting index of the record to start from.
	 * @param pageSize - The maximum number of records to return.
	 * @return LmsCourseRecordsResponse - Response containing the records if successful.
	 * @throws IllegalArgumentException
	 */
	LmsCourseRecordsResponse getLmsData(String browserSessionId, String domainId, Integer domainSessionId, int indexStart, int pageSize) throws IllegalArgumentException;

	/**
	 * Exports the specified course output data
	 *
	 * @param username the username of the user for who is asking for this data export
	 * @param course the course to export the output data for
	 * @return ExportResponse - Response containing the download URL for the export if successful
	 */
	ExportResponse exportCourseData(String username, DomainOption course);

	/**
	 * Gets the progress of the export currently being run for the specified user, if one exists
	 *
	 * @param username the username of the user for which to get progress
	 * @return the progress of the export
	 */
	ProgressResponse getExportCourseDataProgress(String username);

	/**
	 * Cancels the export currently running for the given user, if one exists
	 *
	 * @param username the username of the user for whom to cancel the export
	 * @return a response indicating whether or not the export was canceled
	 */
	RpcResponse cancelExportCourseData(String username);

	/**
	 * Deletes the file generated by an export
	 *
	 * @param exportResult the result of the export which contains the location of the file
	 * @return a response indicating whether or not the delete was successful
	 */
	RpcResponse deleteCourseDataExportFile(DownloadableFileRef exportResult);

	/**
	 * Calculates the size of the export of the specified courses for the specified user
	 *
	 * @param username the username of the user for whom to export the courses
	 * @param coursesToExport the domain options of the courses to export
	 * @return DoubleResponse - Response containing the size of the export
	 */
	DoubleResponse calculateExportSize(String username, List<DomainOption> coursesToExport);
	
	/**
     * Exports the specified courses for the specified user
     *
     * @param username the username of the user for whom to export the courses
     * @param coursesToExport the domain options of the courses to export
     * @return ExportResponse - Response containing the download URL for the export if successful
     */
    ExportResponse exportCourses(String username, List<DomainOption> coursesToExport);

    /**
     * Gets the progress of the export currently being run for the specified user, if one exists
     *
     * @param username the username of the user for which to get progress
     * @return the progress of the export
     */
    ProgressResponse getExportProgress(String username);

    /**
     * Cancels the export currently running for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the export
     * @return a response indicating whether or not the export was canceled
     */
    RpcResponse cancelExport(String username);

    /**
     * Deletes the file generated by an export
     *
     * @param exportResult the result of the export which contains the location of the file
     * @return a response indicating whether or not the delete was successful
     */
    RpcResponse deleteExportFile(DownloadableFileRef exportResult);

	/**
	 * Requests the course to be loaded.  This must be done before the course is started
	 * since the course files may need to be downloaded from the server.
	 *
	 * @param browserSessionId - The session that is making the request (cannot be null).
	 * @param courseData - The course data to load (cannot be null).
	 * @return RpcResponse - true if success on the server, false otherwise.
	 */
	RpcResponse loadCourse(String browserSessionId, DomainOption courseData);

	/**
	 * Notify the server that the load course progress indicator is no longer needed for the specific
	 * browser's loading of the course specified.
	 *
	 * @param browserSessionId the unique id of a browser session used to identify the specific user
	 * @param courseData contains information about the course that was being loaded by the user
	 * @return always returns success because this is best effort.
	 */
	GenericRpcResponse<Void> cleanupLoadCourseIndicator(String browserSessionId, DomainOption courseData);

	/**
	 * Notify the server that the LTI load course indicator is no longer needed for the specific
     * browser's loading of the course specified.
     *
	 * @param consumerKey The consumer key of the lti user loading the course.
     * @param consumerId The consumer id of the lti user loading the course.
     * @param courseSourceId The course id (domain id) to find the progress of the load (cannot be null).
	 * @return always returns success because this is best effort.
	 */
	GenericRpcResponse<Void> cleanupLoadCourseIndicator(String consumerKey, String consumerId, String courseSourceId);

	/**
     * Notify the server that the Experiment load course indicator is no longer
     * needed for the specific browser's loading of the course specified.
     *
     * @param progressIndicatorId The unique id used to identify a specific
     *        progress indicator for an experiment.
     * @return always returns success because this is best effort.
     */
    GenericRpcResponse<Void> cleanupLoadCourseIndicator(String progressIndicatorId);

	/**
	 * Notify the server that the create data collection item progress indicator is no longer needed for the
	 * user.
	 *
	 * @param username the user who is creating a data collection object
	 * @return always returns success because this is best effort
	 */
	GenericRpcResponse<Void> cleanupCreateDataCollectionItemProgressIndicator(String username);

    /**
     * Notify the server that the data collection course export progress indicator is no longer needed for the
     * user.
     *
     * @param username the user who is exporting a data collection course
     * @return always returns success because this is best effort
     */
    GenericRpcResponse<Void> cleanupCourseExportProgressIndicator(String username);

    /**
     * Notify the server that the data collection raw data export progress indicator is no longer needed for the
     * user.
     *
     * @param username the user who is exporting a data collections raw data
     * @return always returns success because this is best effort
     */
    GenericRpcResponse<Void> cleanupRawDataExportProgressIndicator(String username);

    /**
     * Notify the server that the delete data collection item progress indicator is no longer needed for the
     * user.
     *
     * @param username the user who is deleting a data collection object
     * @return always returns success because this is best effort
     */
    GenericRpcResponse<Void> cleanupDeleteDataCollectionItemProgressIndicator(String username);

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
     * @return The newly created {@link CourseCollection}. Can't be null.
     * @throws DetailedException If there was a problem creating the new
     *         {@link CourseCollection}.
     */
    CourseCollection createCourseCollection(String username, String name, String description) throws DetailedException;

    /**
     * Retrieves the {@link CourseCollection} that matches a given id.
     *
     * @param courseCollectionId The id of the {@link CourseCollection} to fetch.
     *        Can't be null.
     * @return The {@link CourseCollection} that matches the id if found. Null
     *         otherwise.
     */
    CourseCollection getCourseCollection(String courseCollectionId);

    /**
     * Fetches all the {@link CourseCollection} objects in the database that the
     * specified user has access to.
     *
     * @param username The username for which the {@link CourseCollection}
     *        should be queried.
     * @return The {@link Collection} of {@link CourseCollection} that the
     *         specified user has access to. Can be empty. Can't be null.
     */
    Collection<CourseCollection> getCourseCollectionsByUser(String username);

    /**
     * Updates a provided {@link CourseCollection} within the database.
     *
     * @param action The {@link AbstractCourseCollectionAction} that describes
     *        the type of update that needs to be made to the collection. Can't
     *        be null.
     * @return An object that describes the changes that occurred as a result of
     *         the update. Can't be null.
     */
    GenericRpcResponse<DataCollectionItem> updateCourseCollection(AbstractCourseCollectionAction action);

    /**
     * Deletes a specified {@link CourseCollection} within the database.
     * @param username the username of the user requesting the course collection be deleted
     * @param courseCollectionId The id of the {@link CourseCollection} to
     *        delete within the database. Can't be null.
     *
     * @return Nothing
     */
    Void deleteCourseCollection(String username, String courseCollectionId);

    /**
     * Method to retrieve an LtiUserSession based on the consumer key and consumer id of the lti user.
     * A boolean parameter can be set to create the session if it doesn't already exist.
     * If successful, the response will contain the user session id from the server.
     *
     * @param consumerKey The consumer key of the lti user.
     * @param consumerId The consumer id of the lti user.
     * @return RpcResonse True if successful.  The response will also contain the session id that the client can use for future requests
     *         to the server.  False if not successful.
     */
	RpcResponse createLtiUserSession(String consumerKey, String consumerId);

	/**
     * Requests the course to be loaded from an LTI user.  This must be done before the course is started
     * since the course files may need to be downloaded from the server.
     *
     * @param consumerKey - The consumer key of the LTI user launching the course.
     * @param consumerId - The consumer id of the LTI user launching the course.
     * @param courseId - The source course id (this should be the id of the course from the course table).
     * @return Request - An RPC request object that can be used to cancel the RPC.
     */
	GenericRpcResponse<String>  ltiGetCourseSourcePathFromId(String consumerKey, String consumerId, String courseId);


	/**
     * Requests the course to be loaded from an lti user.  This must be done before the course is started
     * since the course files may need to be downloaded from the server.
     *
     * @param consumerKey - The consumer key of the lti user launching the course.
     * @param consumerId - The consumer id of the lti user launching the course.
     * @param courseSourcePath - The source path of the course.
     * @param callback - The callback returning the results of the rpc.
     * @return RpcResponse - true if success on the server, false otherwise.
     */
	GenericRpcResponse<String> ltiLoadCourse(String consumerKey, String consumerId, String courseSourcePath);

	/**
     * Requests the experiment be loaded. This must be done before the
     * experiment is started since the course files will need to be downloaded
     * from the server.
     *
     * @param experimentId the unique id of the experiment to load.
     * @param progressId the unique id that will be used to lookup the loading progress.
     * @return RpcResponse containing the experiment source and runtime directory path.
     */
	LoadExperimentResponse loadExperiment(String experimentId, String progressId);

	/**
     * Gets the current progress of a course that is being loaded.
     *
     * @param browserSessionId - The session that is making the request (cannot be null).
     * @param courseId - The course id (domain id) to find the progress of the load (cannot be null).
     * @return ProgressResponse - true if success on the server, false otherwise.  Also contains progress data (like percentage complete).
     */
	ProgressResponse getLoadCourseProgress(String browserSessionId, String courseId);

	/**
     * Gets the current progress of an experiment course that is being loaded.
	 *
     * @param experimentProgressId - The unique id used to poll for progress (cannot be null).
     * @param callback callback - The callback returning the results of the rpc.
     * @return Request - An rpc request object that can be used to cancel the rpc.
     * @throws IllegalArgumentException
     */
	ProgressResponse getLoadCourseProgress(String experimentProgressId);

	/**
	 * Gets the current progress of an LTI course that is being loaded for an LTI user.
	 *
	 * @param consumerKey The consumer key of the lti user loading the course.
	 * @param consumerId The consumer id of the lti user loading the course.
	 * @param courseId The course id (domain id) to find the progress of the load (cannot be null).
	 * @return ProgressResponse - true if success on the server, false otherwise.  Also contains progress data (like percentage complete).
	 */
	ProgressResponse getLoadCourseProgress(String consumerKey, String consumerId, String courseId);

	/**
     * Informs the server that a load request has been cancelled.
     *
     * @param browserSessionId - The session that is making the request (cannot be null).
     * @param courseId - The course id (domain id) to cancel the load progress (cannot be null).
     * @return RpcResponse - true if success on the server, false otherwise.
     */
	RpcResponse cancelLoadCourse(String browserSessionId, String courseId);


	/**
     * Runs the validation check to see if the course is valid.
     *
     * @param browserSessionId - The session id from the client.
     * @return courseData - The domain option (course data) that will be validated.
     * @param courseValidationParams - optional parameters to use during course validation.  Can be null if the default server side logic for course validation is wanted.
     * @return The results of the rpc that include the course being validated.  If the course had an issue validating the domain option
     * should have an unavailable recommendation enumeration type and the RpcResponse should have a useful error message to show to the user with a false success value as well.
     * @throws IllegalArgumentException
     */
    ValidateCourseResponse validateCourseData(String browserSessionId, DomainOption courseData, CourseValidationParams courseValidationParams) throws IllegalArgumentException;

	/**
	 * Checks existing survey resources for potential conflicts.
	 * Imports the courses if no conflicts are found.
	 *
	 * @param username - the username of the user for whom to import the courses.
	 * @param exportFileLocation - the server-side location of the export file to import.
	 * @return ImportCoursesResponse - a response containing the conflicts if any are found.
	 */
    ImportCoursesResponse checkForImportConflicts(String username, String exportFileLocation);

	/**
	 * Imports the export file at the specified location for the specified user
	 *
	 * @param username the username of the user for whom to import the courses
	 * @param exportFileLocation the server-side location of the export file to import
	 * @param filesToOverwrite a list of filenames that determines which existing survey
	 * images will be overwritten by imported survey images. Can be null.
	 * @param conflictingCourses a map of conflicting courses to be imported to their new names. Can be null.
	 * @return RpcResponse - true if success on the server, false otherwise.
	 */
	ImportCoursesResponse importCourses(String username, String exportFileLocation, List<String> filesToOverwrite, Map<String, String> conflictingCourses);

	/**
	 * Gets the progress of the import currently being run for the specified user, if one exists
	 *
	 * @param username the username of the user for which to get progress
	 * @return the status of the import.  When finished, it will contain the list of courses imported.
	 */
	LoadedProgressIndicator<List<DomainOption>> getImportProgress(String username);

	/**
	 * Cancels the import currently running for the given user, if one exists
	 *
	 * @param username the username of the user for whom to cancel the import
	 * @return a response indicating whether or not the import was canceled
	 */
	RpcResponse cancelImport(String username);

	/**
	 * The endCourse rpc indicates that the user has ended a course.  The course data such as the domain Id of the course
     * is sent so the server can perform any needed cleanup (such as deleting the runtime folder on disk)
     * after a user completes the course.
     *
	 * @param browserSessionId - The session id from the client.
	 * @param domainId - The runtime id of the course that has ended.
	 * @return RpcResponse - response indicating whether the end course was successful or not.
	 */
	RpcResponse endCourse(String browserSessionId, String domainId);

	/**
     * Create a new experiment for the course provided.
     *
     * @param name the name of the experiment.  Doesn't have to be unique.  Can't be null.
     * @param description information about the experiment.  Can be null.
     * @param username the user name of the user that is creating the experiment
     * @param courseId the unique identifier of the course the experiment will use
     * @param dataSetType the type of data collection set to be created.
     * @return the experiment created
	 */
	ExperimentResponse createExperiment(String name, String description, String username, String courseId, String dataSetType);

	/**
	 * Gets the progress of the experiment creation currently being run for the specified user, if one exists
	 *
	 * @param username the username of the user for which to get progress
	 * @return the progress of the experiment creation
	 */
	ProgressResponse getCreateExperimentProgress(String username);

	/**
	 * Cancels the experiment creation currently running for the given user, if one exists
	 *
	 * @param username the username of the user for whom to cancel the experiment creation
	 * @return a response indicating whether or not the experiment creation was canceled
	 */
	RpcResponse cancelCreateExperiment(String username);

	/**
	 * Gets the list of experiments authored by the given user
	 *
	 * @param username the username of the author
	 * @return a response providing the list of experiments
	 */
	ExperimentListResponse getExperiments(String username);

	/**
	 * Deletes the given experiment for the given user
	 *
	 * @param username the username of user deleting the experiment
	 * @param experimentId the experiment's ID
	 * @return the results of the rpc
	 */
	DetailedRpcResponse deleteExperiment(String username, String experimentId);

	/**
	 * Gets the progress of the experiment deletion currently being run for the specified user, if one exists
	 *
	 * @param username the username of the user for which to get progress
	 * @return the progress of the experiment deletion
	 */
	ProgressResponse getDeleteExperimentProgress(String username);

	/**
	 * Cancels the experiment deletion currently running for the given user, if one exists
	 *
	 * @param username the username of the user for whom to cancel the experiment deletion
	 * @return a response indicating whether or not the experiment deletion was canceled
	 */
	RpcResponse cancelDeleteExperiment(String username);

	/**
	 * Exports the course used by the given experiment for the given user
	 *
	 * @param username the username of user
	 * @param experimentId the experiment's ID
	 * @return the results of the rpc
	 */
	ExportResponse exportExperimentCourse(String username, String experimentId);

	/**
	 * Gets the progress of the experiment course export currently being run for the specified user, if one exists
	 *
	 * @param username the username of the user for which to get progress
	 * @return the progress of the experiment course export
	 */
	ProgressResponse getExportExperimentCourseProgress(String username);

	/**
	 * Cancels the experiment course export currently running for the given user, if one exists
	 *
	 * @param username the username of the user for whom to cancel the experiment course export
	 * @return a response indicating whether or not the experiment course export was canceled
	 */
	RpcResponse cancelExportExperimentCourse(String username);

	/**
	 * Calculates the size of course export of the specified experiment for the specified user
	 *
	 * @param username the username of user
	 * @param experimentId the experiment's ID
	 * @return the results of the rpc
	 */
	DoubleResponse calculateExperimentCourseExportSize(String username,
			String experimentId);


	/**
	 * Exports the raw data from the given experiment for the given user
	 *
	 * @param username the username of user
	 * @param experimentId the experiment's ID
	 * @param exportConvertedBinaryLogs true to also export the converted human readable binary files.
	 * @return the results of the rpc
	 */
	ExportResponse exportExperimentRawData(String username, String experimentId, boolean exportConvertedBinaryLogs);

	/**
	 * Gets the progress of the experiment raw data export currently being run for the specified user, if one exists
	 *
	 * @param username the username of the user for which to get progress
	 * @return the progress of the experiment raw data export
	 */
	ProgressResponse getExportExperimentRawDataProgress(String username);

	/**
	 * Cancels the experiment raw data export currently running for the given user, if one exists
	 *
	 * @param username the username of the user for whom to cancel the experiment course export
	 * @return a response indicating whether or not the experiment raw data export was canceled
	 */
	RpcResponse cancelExportExperimentRawData(String username);


	/**
	 * Calculates the size of raw data export of the specified experiment for the specified user
	 *
	 * @param username the username of user
	 * @param experimentId the experiment's ID
	 * @return the results of the rpc
	 */
	DoubleResponse calculateExperimentRawDataExportSize(String username,
			String experimentId);


	/**
	 * Updates the given experiment
	 *
	 * @param username the username of user
	 * @param experimentId the ID of the experiment to update
	 * @param name the updated name
	 * @param description the updated description
	 * @return the results of the rpc
	 */
	ExperimentResponse updateExperiment(String username, String experimentId, String name, String description);


	/**
	 * Validates the course belonging to the given experiment for the given user
	 *
	 * @param username the username of user deleting the experiment
	 * @param experimentId the experiment's ID
	 * @return the results of the rpc
	 */
	DetailedRpcResponse validateExperimentCourse(String username, String experimentId);

	/**
	 * Creates an error report file using the given information and provides a location that it can be accessed from
	 *
	 * @param userName the name of the user who initiated the call
	 * @param reason the error reason
	 * @param errorDetailsList the list of errors including the details and optional stack trace for each.
	 * @param date when the error happened
	 * @param courseName the name of the course the error happened in (optional - can be null)
	 * @return the results of the rpc.
	 */
	ExportResponse exportErrorFile(String userName, String reason, List<ErrorDetails> errorDetailsList, Date date, String courseName);


	/**
	 * Gets the experiment with the given experiment ID
	 *
	 * @param username the user requesting the experiment
	 * @param experimentId the ID of the experiment to get
	 * @return the experiment
	 */
	ExperimentResponse getExperiment(String username, String experimentId);


	/**
	 * Sets whether or not the given experiment is active
	 *
	 * @param username the name of the user who initiated the request
	 * @param experimentId the ID of the experiment to update
	 * @param active whether or not the experiment should be active
	 * @return the updated experiment
	 */
	ExperimentResponse setExperimentActive(String username, String experimentId, boolean active);


	/**
	 * Generates and exports an experiment report for the given experiment for the given user
	 *
	 * @param username the username of user
	 * @param experimentId the experiment's ID
	 * @param reportProperties the properties to use to generate the report
	 * @return the results of the rpc
	 */
	DetailedRpcResponse exportExperimentReport(String username, String experimentId, ReportProperties reportProperties);


	/**
	 * Gets the progress of the experiment report export currently being run for the specified user, if one exists
	 *
	 * @param username the username of the user for which to get progress
	 * @return the progress of the experiment report export
	 */
	GenerateReportStatusResponse getExportExperimentReportProgress(String username);


	/**
	 * Cancels the experiment report export currently running for the given user, if one exists
	 *
	 * @param username the username of the user for whom to cancel the experiment report export
	 * @return a response indicating whether or not the experiment report export was canceled
	 */
	RpcResponse cancelExportExperimentReport(String username);

	/**
     * Gets the progress of the delete operation on the server
     *
     * @param browserSessionId - The session id from the client.
     * @return the progress of the delete operation
     */
	ProgressResponse getDeleteProgress(String browserSessionId);

	   /**
     * Gets the progress of the get course list operation on the server
     *
     * @param browserSessionId - The session id from the client.
     * @return the progress of the get course list operation
     */
    ProgressResponse getCourseListProgress(String browserSessionId);

	/**
     * Gets the progress of the copy operation on the server
     *
     * @param browserSessionId - The session id from the client.
     * @return the progress of the copy operation
     */
	LoadedProgressIndicator<CopyCourseResult> getCopyProgress(String browserSessionId);

	/**
	 * Gets the progress of the course validation on the server
	 *
	 * @param browserSessionId the unique browser session id from the client used to lookup
	 * the currently running course validation progress indicator.
	 * @param courseId unique id of the course validation progress wanting to return.  Currently not used.
	 * @return the current progress of the course validation logic.  If no course validation progress object
	 * was found for the user a non-successful response will be returned.  This can happen when the course
	 * validation finished and was removed from the server's memory since the last check to get course progress.
	 */
    ProgressResponse getValidateCourseProgress(String browserSessionId, String courseId);


	 /**
     * Attempts to delete the specified course(s)
     *
     * @param browserSessionId - The session id from the client.
     * @param courses - The course(s) to delete
     * @param deleteSurveyResponses - True to delete survey responses. Otherwise, the client will be notified if the survey context contains survey responses
     * @param skipSurveyResources - True to skip deleting the survey context. To be used if deleting the survey context and responses
     * @return the results of the rpc.
     */
	DeleteCourseResult deleteCourses(String browserSessionId, List<DomainOption> courses, boolean deleteSurveyResponses, boolean skipSurveyResources);

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
	DetailedRpcResponse copyCourse(String browserSessionId, String newCourseName, DomainOption courseData, List<DomainOption> coursesList);

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
	DetailedRpcResponse publishCourseToPublic(String browserSessionId, String newCourseName, DomainOption courseData) throws DetailedException;

	/**
     * Retrieves the latest root LMS data for every course the user with the given ID has taken. This data provides a brief overview
     * of all the courses the user has taken and when they last took said courses.
     *
     * @param browserSessionId the session id of the browser
     * @return the result containing the requested LMS data
     * @throws IllegalArgumentException if an argument is missing or defined improperly
     */
	LmsCourseRecordsResponse getLatestRootLMSDataPerDomain(String browserSessionId) throws IllegalArgumentException;

    /**
     * Retrieves the active {@link AbstractKnowledgeSession knowledge sessions}.
     *
     * @return the result containing the list of active knowledge sessions.
     */
    GetActiveKnowledgeSessionsResponse fetchActiveKnowledgeSessions();

    /**
     * Registers a user as an {@link AbstractKnowledgeSession} monitor.
     *
     * @param browserSessionKey the key of the browser session being used to monitor the knowledge
     *        session.
     * @param knowledgeSession the session being monitored.
     * @return the result of whether or not registering completed successfully.
     */
    GenericRpcResponse<Void> registerKnowledgeSessionMonitor(String browserSessionKey,
            AbstractKnowledgeSession knowledgeSession);

    /**
     * De-registers a user from being an {@link AbstractKnowledgeSession} monitor.
     *
     * @param browserSessionKey the key of the browser session being used to monitor the knowledge
     *        session.
     * @param knowledgeSession the session no longer being monitored.
     * @return the result of whether or not de-registering completed successfully.
     */
    GenericRpcResponse<Void> deregisterKnowledgeSessionMonitor(String browserSessionKey,
            AbstractKnowledgeSession knowledgeSession);

    /**
     * Request that the log index file be updated for the log metadata provided.
     *
     * @param browserSessionId the client session requesting this update.  Used to track progress
     * for that client.
     * @param logMetadata contains information about a domain session log and that information
     * should be updated in the log index file.
     * @return the response to the clients request, should normally be a success as this method just
     * starts logic in a new thread.
     */
    RpcResponse updateLogIndex(String browserSessionId, LogMetadata logMetadata);

    /**
     * Get the current server side progress of updating the log index file with updates to a
     * specific log metadata entry.
     * @param browserSessionId the client session requesting the log index update.  Used to track progress
     * for that client.
     * @return the current progress on the server corresponding to a previous request to update the log index.
     */
    LoadedProgressIndicator<Void> getUpdateLogIndexProgress(String browserSessionId);

    /**
     * Request the list of domain session logs that contains DKF lessons available for playback
     * purposes.
     * @param browserSessionId the client session requesting this information.  Used to track progress
     * for that client.
     * @return the response to the clients request, should normally be a success as this method just
     * starts logic in a new thread.
     */
    RpcResponse fetchLogsAvailableForPlayback(String browserSessionId);

    /**
     * Get the current server side progress of retrieving the list of domain session logs based on the
     * clients request for that information.
     * @param browserSessionId the client session requesting this information.  Used to track progress
     * for that client.
     * @return the current progress and eventually the information about the session log files found on the server.
     */
    LoadedProgressIndicator<Collection<LogMetadata>> getFetchLogsAvailableForPlaybackProgress(String browserSessionId);

    /**
     * Registers the specified browser session to playback a specified log file.
     *
     * @param browserSessionKey The unique identifier of the browser making the
     *        request.
     * @param log The {@link LogMetadata} that specifies which log file should
     *        be played back. Can't be null.
     * @return The {@link GenericRpcResponse} Indicating whether or not the
     *         action was successful.
     */
    GenericRpcResponse<Void> registerKnowledgeSessionPlayback(String browserSessionKey, LogMetadata log);

    /**
     * Permanently stops playback of a specified browser session's currently
     * playing log file.
     *
     * @param browserSessionKey The unique identifier of the browser making the
     *        request.
     * @return The {@link GenericRpcResponse} Indicating whether or not the
     *         action was successful.
     */
    GenericRpcResponse<Void> deregisterKnowledgeSessionPlayback(String browserSessionKey);

    /**
     * Sets the time of the knowledge session playback being run by the browser
     * session with the given key to when the given task or concept is
     * activated.
     *
     * @param browserSessionKey the key of the browser session being used to
     *        play back a knowledge session. Cannot be null.
     * @param taskConceptName the name of the task or concept to use to find the
     *        activation start time. Can't be null or blank.
     * @return a response indicating whether or not jumping to the activation
     *         start completed successfully
     */
    GenericRpcResponse<Long> jumpToActivationStart(String browserSessionKey, String taskConceptName);

    /**
     * Fetches a mapping of a simulation times to {@link LearnerState} objects
     * that were sent at that time for the currently playing log file.
     *
     * @param browserSessionKey The unique identifier of the browser session
     *        making the request. Can't be null.
     * @return The {@link GenericRpcResponse} Indicating whether or not the
     *         action was successful. If successful it will contain the mapping
     *         of simulation epoch time stamps to the {@link LearnerState} at
     *         that given time.
     */
    GenericRpcResponse<SessionTimelineInfo> fetchLearnerStatesForSession(String browserSessionKey);

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
     * @return a response indicating whether or not creating the log patch
     *         completed successfully. Contains the log patch file name if it
     *         was created.
     */
    GenericRpcResponse<String> editLogPatchForPerformanceStateAttribute(String browserSessionKey, String username, long timestamp, PerformanceStateAttribute performanceState);

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
     * @return a response indicating whether or not creating the log patch
     *         completed successfully. Contains the log patch file name if it
     *         was created.
     */
    GenericRpcResponse<String> createLogPatchForEvaluatorUpdate(String browserSessionKey, String username, long timestamp,
            boolean updateEntireSpan, EvaluatorUpdateRequest evaluatorUpdateRequest, boolean applyToFutureStates);

    /**
     * Removes a patch for a {@link PerformanceStateAttribute}.
     * 
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @param username The username of the person making the edits.
     * @param timestamp the timestamp of the deletion.
     * @param performanceState the performance state that is to be deleted.
     * @return a response indicating whether or not creating the log patch
     *         completed successfully. Contains the log patch file name if it
     *         was created.
     */
    GenericRpcResponse<String> removeLogPatchForAttribute(String browserSessionKey, String username,
            long timestamp, PerformanceStateAttribute performanceState);

    /**
     * Delete the session log patch.
     *
     * @param browserSessionKey The unique identifier of the browser making the
     *        request. Can't be blank.
     * @return a response indicating whether or not deleting the log patch
     *         completed successfully.
     */
    GenericRpcResponse<Void> deleteSessionLogPatch(String browserSessionKey);

    /**
     * Sets the time of the knowledge session playback being run by the browser session with the given key
     *
     * @param browserSessionKey the key of the browser session being used to play back a knowledge session. Cannot be null.
     * @param time the time to set the session playback to
     * @return a response indicating whether or not setting the playback time completed successfully
     */
    GenericRpcResponse<Void> setSessionPlaybackTime(String browserSessionKey, long time);

    /**
     * Starts the knowledge session playback associated with the browser session with the given key
     *
     * @param browserSessionKey the key of the browser session being used to play back a knowledge session. Cannot be null.
     * @return a response indicating whether or not the operation completed successfully
     */
    GenericRpcResponse<Void> startSessionPlayback(String browserSessionKey);

    /**
     * Stops the knowledge session playback associated with the browser session with the given key
     *
     * @param browserSessionKey the key of the browser session being used to play back a knowledge session. Cannot be null.
     * @return a response indicating whether or not the operation completed successfully
     */
    GenericRpcResponse<Void> stopSessionPlayback(String browserSessionKey);

    /**
     * Updates the game master session state as being in 'auto' or 'manual' mode. In 'auto' mode,
     * all incoming strategy requests will be automatically approved with the suggested actions.
     *
     * @param browserSessionKey the key of the browser session being used to monitor the knowledge
     *        session.
     * @param isAutoMode true if the game master session is in 'auto' mode; false otherwise.
     * @return the result of whether or not updating the game master auto state completed
     *         successfully.
     */
    GenericRpcResponse<Void> updateGameMasterAutoState(String browserSessionKey, boolean isAutoMode);

    /**
     * Caches the processed strategies referenced by the
     * {@link ProcessedStrategyCache} items.
     *
     * @param browserSessionKey the key of the browser session being used to
     *        monitor the knowledge session.
     * @param strategyCache the collection of processed strategies to cache.
     * @return the result of whether or not caching completed successfully.
     */
    GenericRpcResponse<Void> cacheProcessedStrategy(String browserSessionKey,
            Collection<ProcessedStrategyCache> strategyCache);

    GenericRpcResponse<Void> updateSessionTeamOrgFilterState(String browserSessionKey, int domainSessionId, Map<String, Boolean> teamRolesSelected);

    /**
     * Sets the gateway connection that monitored game state information should be shared with with when playing
     * back a session
     *
     * @param browserSessionKey the key of the browser session making the request. Cannot be null.
     * @param gatewayConnection the gateway connection to share entity state information with. If not null, the connections
     * will be established as needed. If different than the last gateway connection, appropriate existing connections will be closed.
     * @return the result of whether the operation completed successfully
     */
    GenericRpcResponse<Void> setGatewayConnections(String browserSessionKey, GatewayConnection gatewayConnection);

    /**
     * Sets the configuration that external monitor applications should use when they are connected and sharing data
     *
     * @param browserSessionKey the key of the browser session making the request. Cannot be null.
     * @param config the configuration to use. Cannot be null.
     * @return the result of whether the operation completed successfully
     */
    GenericRpcResponse<Void> setExternalMonitorConfig(String browserSessionKey, ExternalMonitorConfig config);

    /**
     * Gets the gateway connection that GIFT is currently sharing knowledge session data with 
     * to monitor said data outside of GIFT
     *
     * @param browserSessionKey the key of the browser session making the request. Cannot be null.
     * @return the result containing the gateway connection that data is being shared with.
     */
    GenericRpcResponse<GatewayConnection> getGatewayConnection(String browserSessionKey);

    /**
     * Properly releases resources that were allocated on the server in relation
     * to the game master web application. This method should be called when a
     * browser stops using the game master.
     *
     * @param browserSessionKey The unique identifier of the browser that is
     *        making this request. Can't be null.
     * @return The {@link GenericRpcResponse} object that details whether or not
     *         the request was processed successfully.
     */
    GenericRpcResponse<Void> cleanupGameMaster(String browserSessionKey);

    /**
     * Gets the SIDC for the military symbol that should be used to represent training application entities with the given
     * role within the knowledge session with the given domain session ID. This can be used to determine what symbol should 
     * be used to represent team roles that haven't been drawn yet due to being filtered out by the session's entity filter.
     * 
     * @param browserSessionKey the key of the browser session requesting the SIDC
     * @param domainSessionId the domain session ID of the knowledge session that the team role is part of
     * @param roleName the name of the team role that a SIDC is being requested for
     * @return the result containing the SIDC corresponding to the given team role. The content of the result can be null 
     * if no entity data has been processed for an entity with the given team role or one of its child roles.
     */
    GenericRpcResponse<SIDC> getSidcForRole(String browserSessionKey, int domainSessionId, String roleName);

    /**
     * Gets a URL that can be used to preview the workspace file at the given location so that it can be
     * viewed in the given user's browser
     * 
     * @param userName the user name of the user making the request. Needed to determine if the user has 
     * permission to access the given workspace file. Cannot be null.
     * @param relativeFileName the path to the workspace file to preview, relative to the root workspace folder.
     * Cannot be null.
     * @return a response that contains the returned preview URL
     */
    GenericRpcResponse<String> getWorkspaceFilePreviewUrl(String userName, String relativeFileName);
    
    /**
     * Caches the processed bookmarks referenced by the
     * {@link ProcessedBookmarkCache} items.
     *
     * @param browserSessionKey the key of the browser session being used to
     *        monitor the knowledge session.
     * @param cache the processed bookmarks to cache.
     * @return the result of whether or not caching completed successfully.
     */
    GenericRpcResponse<Void> cacheProcessedBookmark(String browserSessionKey, ProcessedBookmarkCache cache);

    /**
     * Requests the log corresponding to the given knowlegde session so that it can be played back
     * 
     * @param browserSessionId the client session requesting this information. Cannot be null.
     * @param activeSession the active knowledge session that we want to find a log for. Cannot be null.
     * @return a response containing the log file that was found
     */
    GenericRpcResponse<LogMetadata> fetchLogForPlayback(String browserSessionId,
            AbstractKnowledgeSession activeSession);

    /**
     * Gets scenario info about the knowledge session associated with the given browser session 
     * 
     * @param browserSessionKey browserSessionId the client session requesting this information. Cannot be null.
     * @param username the user name of the user making the request. Cannot be null.
     * @return a response containing information about the knowledge session scenario
     */
    GenericRpcResponse<SessionScenarioInfo> getKnowledgeSessionScenario(String browserSessionKey, String username);

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
     * @return a response indicating whether the operation completed successfully. Contains the log patch file name if it
     *         was created.
     */
    GenericRpcResponse<String> publishKnowledgeSessionOverallAssessments(String browserSessionKey, String username,
            long timestamp, Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments, List<String> courseConcepts);

    /**
     * Calculates the assessment levels for the parent performance nodes of the given condition assessments
     * 
     * @param browserSessionKey the client session requesting this information. Cannot be null.
     * @param username the user name of the user making the request. Cannot be null.
     * @param conceptToConditionAssessments a mapping from each leaf concept to the assessment levels that were assigned
     * to each of its conditions by the observer controller. Cannot be null.
     * @return the calculated assessment levels of all the parent nodes. Cannot be null.
     */
    GenericRpcResponse<Map<Integer, AssessmentLevelEnum>> calculateRollUp(String browserSessionKey, String username,
            Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments);
    
    /**
     * Registers the given browser session to interact with the web monitor service and
     * and receive updates from it
     * 
     * @param username the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @return a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<WebMonitorStatus> registerMonitorService(String username, String browserSession);
    
    /**
     * Deregisters the given browser session to stop interacting with the web monitor service and
     * and stop receiving updates from it
     * 
     * @param username the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @return a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> deregisterMonitorService(String username, String browserSession);
    
    /**
     * Handles module launch requests
     * 
     * @param username name of the user launching a module(s). Cannot be null.
     * @param browsers the client session requesting this information. Cannot be null.
     * @param modules a list of one of more modules to launch. Cannot be null.
     * @return a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> launchModules(String username, String browsers, List<ModuleTypeEnum> modules);
    
    /**
     * Handles module stop requests
     * 
     * @param username name of the user stopping a module(s). Cannot be null.
     * @param browsers the client session requesting this information. Cannot be null.
     * @param modules a list of one of more modules to kill. Cannot be null.
     * @param targetQueue the name of the specific queue to kill. If null, ALL queues with the
     * target modules types will be killed.
     * @return a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> killModules(String username, String browsers, List<ModuleTypeEnum> modules, String targetQueue);
    
    /**
     * Gets the full display information for the message with the given metadata.
     * 
     * @param the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the ID of the domain session associated with the message. Can be null for 
     * system messages.
     * @param message the metadata uniquely identifying the message that is being requested.
     * @return a response containing the requested message data. Cannot be null.
     */
    GenericRpcResponse<MessageDisplayData> getMessageDisplayData(String username, String browserSession, Integer domainSessionId, MessageEntryMetadata message);
    
    
    /**
     * Toggles the state of the web monitor listening for messages 
     * 
     * @param browserSession the client session requesting this information. Cannot be null.
     * @param listening the flag indicating whether the web monitor listening for message updates
     * @param domainSessionId the ID of the domain session who toggled the listening button. Can be null for system 
     *        messages.
     * @return a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> setListening(String browserSession, boolean listening, Integer domainSessionId);

    /**
     * Begins monitoring the domain session with the given ID using the web monitor
     * 
     * @param the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the ID of the domain session to monitor. Cannot be null.
     * @return a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> watchDomainSession(String username, String browserSession, int domainSessionId);
    
    /**
     * Filters domain tab messages by entityMarking
     * 
     * @param entityMarking the filter to apply to the domain tab display list
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the ID of the domain session to monitor. Cannot be null.
     * @param callback a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> entityFilter(String entityMarking, String browserSession, int domainSessionId);
    
    /**
     * Toggles the state of the web monitor's message display panel showing advanced information 
     * 
     * @param browserSession the client session requesting this information. Cannot be null.
     * @param advancedHeader the flag indicating whether the message panel should show advanced info
     * @param domainSessionId the ID of the domain session who toggled the advanced header checkbox. Can be null for system
     *        messages.
     * @return a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> setAdvancedHeader(String browserSession, boolean advancedHeader, Integer domainSessionId);

    /**
     * Sets the message types to filter for the given domain session
     * 
     * @param userSession the name of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the domain session ID of the filter that is being update. Can be null.
     * @param selectedChoices the new choices provided by the user for what messages should be shown. Cannot be null.
     * @return a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> setMessageFilter(String userSession, String browserSession, Integer domainSessionId,
            Set<MessageTypeEnum> selectedChoices);
    
    /**
     * Closes a domain session tab
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the domain session ID of the filter that is being update. Can be null.
     * @param callback a callback containing a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> unwatchDomainSession(String browserSession, Integer domainSessionId);

    /**
     * Refreshes the message information stored in the web monitor panel for the given domain session
     * 
     * @param username the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the ID of the domain session whose panel is being updated. Cannot be null.
     * @return a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> refreshPanel(String username, String browserSession, Integer domainSessionId);

    /**
     * Refreshes module status information for the given browser session
     * 
     * @param username the username of the user making the request. Cannot be null.
     * @param browserSession the ID of the browser session making the request. Cannot be null.
     * @return a response indicating whether the operation was successful. Cannot be null.
     */
    GenericRpcResponse<Void> refreshModules(String username, String browserSession);

}
