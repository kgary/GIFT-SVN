/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import generated.conversation.Conversation;
import generated.course.ConceptNode;
import generated.course.Course;
import generated.course.DkfRef;
import generated.course.Interop;
import generated.course.InteropInputs;
import generated.course.Interops;
import generated.course.Media;
import generated.course.RIDEInteropInputs;
import generated.course.SlideShowProperties;
import generated.course.TrainingApplication;
import generated.course.TrainingApplicationWrapper;
import generated.course.Transitions;
import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Coordinate;
import generated.dkf.Resources;
import generated.dkf.Scenario;
import generated.dkf.Task;
import generated.dkf.Scenario.Mission;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.LoginRequest;
import mil.arl.gift.common.TrainingAppCourseObjectWrapper;
import mil.arl.gift.common.coordinate.AbstractCoordinate.CoordinateType;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.CourseValidationResults.CourseObjectValidationResults;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.FailureResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.SuccessfulResponse;
import mil.arl.gift.common.gwt.server.AsyncReturnBlocker;
import mil.arl.gift.common.gwt.server.GiftServletUtils;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr.UserAuthResult;
import mil.arl.gift.common.gwt.shared.MediaHtml;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.GiftScenarioProperties;
import mil.arl.gift.common.io.MapTileProperties;
import mil.arl.gift.common.io.MemoryFileServletRequest;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.metrics.MetricsSender;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.domain.lessonmaterial.LessonMaterialFileHandler;
import mil.arl.gift.net.nuxeo.WritePermissionException;
import mil.arl.gift.net.rest.RESTClient;
import mil.arl.gift.tools.authoring.common.conversion.UnsupportedVersionException;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcService;
import mil.arl.gift.tools.authoring.server.gat.server.handler.conversation.ConversationUpdateCallback;
import mil.arl.gift.tools.authoring.server.gat.server.handler.conversation.ConversationUpdateManager;
import mil.arl.gift.tools.authoring.server.gat.shared.AuthoritativeResourceRecord;
import mil.arl.gift.tools.authoring.server.gat.shared.ExternalScenarioImportResult;
import mil.arl.gift.tools.authoring.server.gat.shared.XTSPImporterResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.wrap.TrainingApplicationObject;
import mil.arl.gift.tools.dashboard.server.DashboardProperties;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.ServicesProperties;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.XTSPImporter;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.table.DbUser;

/**
 * The server side implementation of the RPC service
 */
public class GatRpcServiceImpl extends RemoteServiceServlet implements GatRpcService {

    /** The version specifier of this class used by the serialization logic. */
	private static final long serialVersionUID = 1L;

	/** The metrics sender is responsible for sending metrics of the dashboard rpcs to the metrics server */
    private MetricsSender metrics = new MetricsSender("gat");

    /** The logger for the class */
    private static Logger logger = LoggerFactory.getLogger(GatRpcServiceImpl.class);

    /** The abstract file services to perform file operations */
    private final AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();

    /** metrics reporting key for the checkTrainingApplicationPath method */
    private static final String CHECK_TA_PATH = "checkTrainingApplicationPath";

    /** metrics reporting key for the  deleteTrainingApplicationObject method */
    private static final String DELETE_TA_OBJECT = "deleteTrainingApplicationObject";

    /** progress indicator description for finishing the retrieving of training app objects */
    private static final String TA_OBJECTS_FINISHED = "Finished";

    /** progress indicator percent for finishing the retrieving of training app objects */
    private static final int TA_OBJECTS_PERCENT_COMPLETE = 100;

	/**
	 * Constructor
	 */
	public GatRpcServiceImpl() {
	}

	@Override
    public void init() throws ServletException {
        super.init();

        // Start sending metrics.
        metrics.startSending();
    }

    @Override
    public void destroy() {

        // Stop sending metrics.
        metrics.stopSending();
        super.destroy();
    }

    @Override
	public List<MediaHtml> getMediaHtmlList(List<Media> mediaList, String userName, String courseFolderPath) {
    	if(mediaList == null) {
    		return null;
    	}

    	for(int i = 0; i < mediaList.size(); i++) {
    		// Modify the content urls so they can be presented in the gat preview

    		String uri = mediaList.get(i).getUri();
    		FetchContentAddressResult result = getContentUrl(userName, uri, courseFolderPath, null);
    		if(result.isSuccess()) {
    			uri = result.getContentURL();
    		}

    		mediaList.get(i).setUri(uri);
    	}

    	//copy media to a list that is agnostic of schema type for conversion to HTML
    	List<Serializable> media = new ArrayList<>();
        media.addAll(mediaList);

    	//Check for SOP violations
    	return GiftServletUtils.convertMediaListToHtml(media);
    }

    /**
     * Gets the URL needed to view the content file at the given path in a browser
     *
     * @param userName the user name of the user invoking the operation. Cannot be null or empty.
     * @param contentFilePath the path to the content file whose URL is being requested, relative to the course folder
     * containing it. Cannot be null or empty.
     * @param courseFolderPath the path to the course folder containing the target file. Cannot be null or empty.
     * @return a result containing the content file's URL
     */
    private FetchContentAddressResult getContentAddress(String userName, String contentFilePath, String courseFolderPath) {
    	long start = System.currentTimeMillis();
    	FetchContentAddressResult result = new FetchContentAddressResult();
    	String filePath = null;
    	String fileURL = "";

    	try {
    		AbstractFolderProxy courseFolder = fileServices.getCourseFolder(courseFolderPath, userName);

    		if(courseFolder.fileExists(contentFilePath)) {
                filePath = courseFolderPath + Constants.FORWARD_SLASH + contentFilePath;
            } else {
                throw new FileNotFoundException("The file (" +contentFilePath + ") could not be found.");
            }

    	} catch (Exception e) {
    		logger.warn("Unable to retrieve the course tile image: ", e);
    		filePath = null;
    		result.setErrorMsg("Failed to fetch the URL of the file named '"+courseFolderPath+"'.");
    		result.setErrorDetails("An exception was thrown while checking if the file exists.  The error reads:\n"+e.getMessage());
    		result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
    	}

    	if(logger.isDebugEnabled()){
            logger.debug("FetchContentAddressHandler filePath = " + filePath);
    	}

    	if (filePath != null && !filePath.isEmpty() && userName != null && !userName.isEmpty()) {
    		if (CommonProperties.getInstance().getDeploymentMode().equals(DeploymentModeEnum.SERVER)){

    			fileURL = CommonProperties.getInstance().getDashboardURL();
    			String dashboardPath = CommonProperties.getInstance().getDashboardPath();

    			fileURL = fileURL + Constants.FORWARD_SLASH + dashboardPath + Constants.FORWARD_SLASH +
    					CommonProperties.getInstance().getDashboardMemoryFileServletSubPath() +
    					MemoryFileServletRequest.encode(new MemoryFileServletRequest(filePath, userName));

    		} else {

    			// The url should look something like this: http://<ip>:<port>/workspace/nblomberg/TestCourse/testimage.jpg
    			String workspaceRelativePath = CommonProperties.getInstance().getWorkspaceDirectoryName() + Constants.FORWARD_SLASH + filePath;

    			fileURL = CommonProperties.getInstance().getDomainContentServerAddress() + Constants.FORWARD_SLASH + workspaceRelativePath;
    		}

    		result.setContentURL(fileURL);
    		result.setSuccess(true);

    	} else {
    		result.setSuccess(false);
    	}


    	if(logger.isDebugEnabled()){
            logger.debug("FetchContentAddressHandler imageURL = " + fileURL);
    	}

    	MetricsSenderSingleton.getInstance().endTrackingRpc("getContentAddress", start);

    	return result;
    }

    @Override
    public FetchContentAddressResult getContentUrl(String userName, String contentUrl, String courseFolderPath, Serializable mediaProperties) {
    	FetchContentAddressResult result = getContentAddress(userName, contentUrl, courseFolderPath);

    	if(!result.isSuccess()) {

    		// If the uri wasn't a file reference, check if the url can be corrected

    		try {
    			String url = LessonMaterialFileHandler.createEmbeddedYouTubeUrl(contentUrl, mediaProperties);
    			result.setContentURL(url == null ? contentUrl : url);
    			result.setSuccess(true);

    		} catch (@SuppressWarnings("unused") Exception e) {

    			// This is a different url. Check to see if it violates SOP or contains blocked content

    		    try {
        		    String url = UriUtil.validateUri(
        		            contentUrl,
                            fileServices.getCourseFolder(courseFolderPath, userName),
                            InternetConnectionStatusEnum.UNKNOWN);

                    contentUrl = url == null ? contentUrl : url;

                } catch (Exception e1) {
                    if(logger.isInfoEnabled()) {
                        logger.debug("getContentUrl - Failed to verify the url: " + contentUrl, e1);
                    }
                }

    		    StringBuilder urlStr = new StringBuilder(contentUrl);
    		    UriUtil.validateUriForSOPViolationOrBlockedContent(urlStr);

    		    if(UriUtil.validateUriForSOPViolationOrBlockedContent(urlStr)) {
    		        result.setViolatesSOP(true);
    		    }

    		    // Make sure the uri contains a network identifier

    			if(!contentUrl.contains("://")) {
        			result.setContentURL("http://" + contentUrl);
        		} else {
        			result.setContentURL(contentUrl);
        		}

    			result.setSuccess(true);
    		}
    	}

    	return result;
    }


    @Override
    public SlideShowProperties getSlideShowUrls(SlideShowProperties slideShowProperties, String courseFolderPath, String userName) {
    	List<String> newUrls = new ArrayList<>();
    	for(String url : slideShowProperties.getSlideRelativePath()) {
    		FetchContentAddressResult result = getContentAddress(userName, url, courseFolderPath);
    		if(result.isSuccess()) {
    			newUrls.add(result.getContentURL());
    		} else {
    			// TODO figure out how to report this error, maybe some kind of image not found thing I think we have one
    		}
    	}

    	slideShowProperties.getSlideRelativePath().clear();
    	slideShowProperties.getSlideRelativePath().addAll(newUrls);
    	return slideShowProperties;
    }

    @Override
    public UpdateConversationResult startConversation(Conversation conversation) {
    	return updateConversation(-1, conversation, null);
    }

    @Override
    public UpdateConversationResult updateConversation(int chatId, Conversation conversation, String userText) {

    	final long start = System.currentTimeMillis();

    	final AsyncReturnBlocker<UpdateConversationResult> returnBlocker = new AsyncReturnBlocker<>();
        ConversationUpdateManager.getNextUpdate(chatId, conversation, userText, new ConversationUpdateCallback() {

			@Override
			public void notify(int chatId, List<String> tutorText, List<String> choices, boolean endConversation) {

				UpdateConversationResult result = new UpdateConversationResult();

				result.setSuccess(true);
				result.setChatId(chatId);
				result.setChoices(choices);
				result.setTutorText(tutorText);
				result.setEndConversation(endConversation);

				MetricsSenderSingleton.getInstance().endTrackingRpc("updateConversation", start);
				returnBlocker.setReturnValue(result);
			}

			@Override
			public void failure(UpdateConversationResult result) {

				MetricsSenderSingleton.getInstance().endTrackingRpc("updateConversation", start);
				returnBlocker.setReturnValue(result);
			}

        });

		return returnBlocker.getReturnValueOrTimeout();
    }

    @Override
    public FetchJAXBObjectResult getCoursePreviewObject(String userName, String relativePath) {
    	FetchJAXBObjectResult result = new FetchJAXBObjectResult();

		        	ValidateFileResult validation = validateFile(userName, relativePath, null);
		        	CourseValidationResults courseValidation = validation.getCourseValidationResults();

		        	if(courseValidation != null && courseValidation.getCriticalIssue() == null
		        			&& (courseValidation.getImportantIssues() == null || courseValidation.getImportantIssues().isEmpty())) {
		        		// If there are no critical or other issues, proceed to get the generated course object

		        		result = getJAXBObject(userName, relativePath, false);
		        		result.setErrorMsg(validation.getReason());
		    			result.setErrorDetails(validation.getDetails());
		    			result.setErrorStackTrace(validation.getStackTrace());

		        	} else if (!validation.isSuccess()) {
		        		// If the validation failed, report the errors.

		        		result.setSuccess(false);
		        		result.setErrorMsg(validation.getReason());
		    			result.setErrorDetails(validation.getDetails());
		    			result.setErrorStackTrace(validation.getStackTrace());

		        	} else {
		        		result = getJAXBObject(userName, relativePath, false);
		        	}

		        	result.setCourseValidationResults(courseValidation);
		return result;
    }

    @Override
    public GenericRpcResponse<Boolean> checkCourseName(String username, String courseFile, String newCourseName) {

        // Extract the original course name for error reporting purposes
        String oldCourseName = courseFile.substring(
                courseFile.lastIndexOf(Constants.FORWARD_SLASH) + 1,
                courseFile.lastIndexOf(AbstractSchemaHandler.COURSE_FILE_EXTENSION));

        FileTreeModel workspaceFolder = null;

        try {

            //retrieve the other courses present within this user's workspace so that we can check for course name conflicts

            workspaceFolder = fileServices.getUsersWorkspace(username);

        } catch (DetailedException e) {

            logger.error("Caught exception while getting domain options for user " + username, e);
            return new FailureResponse<>(e);
        }

        //check to make sure the new course name doesn't conflict with any existing courses in this workspace

        for(String fileName : workspaceFolder.getFileNamesUnderModel()) {

            // Check to see if there is a conflict with the course folder name.

            if(fileName.endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION)) {

                if(!CommonProperties.getInstance().getDeploymentMode().equals(DeploymentModeEnum.SERVER)) {

                    /*
                     * The fileName will look like 'CourseName/CourseName.course.xml' in Desktop mode
                     * or workspace/CourseName/CourseName.course.xml in Server mode
                     *
                     * Prepend the fileName with the workspace folder for comparison purposes in Desktop mode
                     * to see if the courseFile and the current workspace file are identical
                     */

                    fileName = workspaceFolder.getFileOrDirectoryName() + Constants.FORWARD_SLASH + fileName;
                }

                String courseName = fileName.substring(fileName.indexOf(Constants.FORWARD_SLASH) + 1, fileName.lastIndexOf(Constants.FORWARD_SLASH));

                if(newCourseName.equalsIgnoreCase(courseName) && !courseFile.equals(fileName)) {

                    // If another course has the same name and the course.xml file paths are different, report a conflict

                    return new FailureResponse<>(new DetailedException(
                            "The course '" + oldCourseName + "' cannot be renamed to '" + newCourseName
                            + "' because there is another course with the same name. "
                            + "<br/><b>Note</b>: Course names are not case sensitive<br/><br/>"
                            + "Please rename the course and try again.",
                            "Failed to rename course because another course with the same name already exists within this workspace.",
                            null
                    ));
                }
            }
        }

        return new SuccessfulResponse<>(true);
    }

    @Override
    public GenericRpcResponse<String> renameCourse(String username, String courseFile, String newCourseName) {

        /* Check for conflicts in case the user created/modified other courses
         * in another browser window */
        GenericRpcResponse<Boolean> conflictResponse = checkCourseName(username, courseFile, newCourseName);

        if (conflictResponse.getWasSuccessful()) {

            try {
                /* Rename the course to the new course name */
                final String renamedCourseFile = fileServices.renameCourse(username, courseFile, newCourseName);
                return new SuccessfulResponse<>(renamedCourseFile);

            } catch (DetailedException e) {
                return new FailureResponse<>(e);
            }

        } else {
            return new FailureResponse<>(new DetailedException(
                    conflictResponse.getException().getMessage(),
                    conflictResponse.getException().getDetails(), null));
        }
    }

    /**
     * Validates the given file within the given user's workspace, returning any detected validation problems.
     *
     * @param userName the user name of the user invoking the operation. Cannot be null or empty.
     * @param relativeFilePath the path to the file to validate, relative to the user's workspace. Cannot be null or empty.
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @return the results of the validation, including any detected validation problems.
     */
    public ValidateFileResult validateFile(String userName, String relativeFilePath, ProgressIndicator progressIndicator) {
	    long start = System.currentTimeMillis();
		ValidateFileResult result;
		Throwable cause = null;

		// validate the file, catch exceptions & initialize the result
		try {
			GIFTValidationResults validationResults = fileServices.validateFile(relativeFilePath, userName, false, progressIndicator);
			if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues() || validationResults.hasWarningIssues()){

			    String reason;
			    FileType fileType = AbstractSchemaHandler.getFileType(relativeFilePath);
			    if(fileType == FileType.COURSE){
		             reason = "There are one or more validation issues with the course.";
			    }else{
		             reason = "There are one or more validation issues with the file '"+relativeFilePath+"'.";
			    }

	            StringBuffer details = new StringBuffer("Below is a list of validation issues that were found.  If you need additional help, please create a new thread in the forums on gifttutoring.org.\n\n<b>Validation Issues:</b>\n<ol>");

	            if(validationResults.getCriticalIssue() != null){
	                details.append("<li>").append(validationResults.getCriticalIssue().getMessage()).append("</li>");
	            }

	            if(validationResults.getImportantIssues() != null){
    	            for(Throwable throwable : validationResults.getImportantIssues()){
    	                details.append("<li>").append(throwable.getMessage()).append("</li>");
    	            }
	            }

                if(validationResults.getWarningIssues() != null){
                    for(Throwable throwable : validationResults.getWarningIssues()){
                        details.append("<li>").append(throwable.getMessage()).append("</li>");
                    }
                }

	            if(validationResults instanceof CourseValidationResults){
	                //handle course object validation results

	                CourseValidationResults courseValidationResults = (CourseValidationResults)validationResults;
	                for(CourseObjectValidationResults courseObjectResults : courseValidationResults.getCourseObjectResults()){

	                    if(courseObjectResults.getValidationResults() != null){

	                        GIFTValidationResults courseObjectValidationResults = courseObjectResults.getValidationResults();

	                        //are there any reported problems?
	                        boolean hasIssue = false;
	                        if(courseObjectValidationResults.hasCriticalIssue() || courseObjectValidationResults.hasImportantIssues() || courseObjectValidationResults.hasWarningIssues()){

	                            hasIssue = true;

	                            //create a heading for this course object
	                            details.append("<li>'").append(courseObjectResults.getCourseObjectName()).append("' course object issue(s):<ul>");
	                        }

	                        if(courseObjectValidationResults.getCriticalIssue() != null){
	                            details.append("<li>").append(courseObjectValidationResults.getCriticalIssue().getMessage()).append("</li>");
	                        }

	                        if(courseObjectValidationResults.getImportantIssues() != null){
	                            for(Throwable throwable : courseObjectValidationResults.getImportantIssues()){
	                                details.append("<li>").append(throwable.getMessage()).append("</li>");
	                            }
	                        }

	                        if(courseObjectValidationResults.getWarningIssues() != null){
	                            for(Throwable throwable : courseObjectValidationResults.getWarningIssues()){
	                                details.append("<li>").append(throwable.getMessage()).append("</li>");
	                            }
	                        }

	                        if(hasIssue){

	                            details.append("</ul></li>");
	                        }
	                    }
	                }
	            }
	            details.append("</ol>");

	            details.append("\n\n<b>Help:</b>Validation issues can be anything from a missing XML tag")
                        .append(" needed to ensure the general XML structure was followed (i.e. all start and end tags are found), to a missing required field (e.g. course name is")
                        .append(" required) or the value for a field doesn't satisfy the schema requirements (i.e. the course name must be at least 1 character).\n\n")
                        .append("Please take a look at the following, albeit sometimes hard to read (thanks Java), XML validation errors for a hint at the problem(s) or ask for help on the GIFT <a href=\"https://gifttutoring.org/projects/gift/boards\" target=\"_blank\">forums</a>.\n\n")
                        .append("<b>For Example: </b><div style=\"padding: 20px; border: 1px solid gray; background-color: #DDDDDD\">This example validation error that indicates the course name ('#AnonType_nameCourse') value doesn't satisfy the minimum length requirement of 1 character:\n\n")
                        .append("<i>cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type '#AnonType_nameCourse'</i></div>");

                result = new ValidateFileResult(relativeFilePath, reason, details.toString(), null);
                result.setSuccess(false);

                if(validationResults instanceof CourseValidationResults) {
                	result.setCourseValidationResults((CourseValidationResults) validationResults);
                }

			}else{
    			result = new ValidateFileResult();
    			result.setCourseValidationResults((CourseValidationResults) validationResults);

    	        try{
    	            result.setLastSuccessfulValidationDate(validationResults.getLastSuccessfulValidationDate());
    	        }catch(@SuppressWarnings("unused") Exception e){ /*best effort*/ }

    			result.setSuccess(true);
			}

		} catch (FileValidationException e) {

			// remove the workspace path from the filename
			String filePath = FileFinderUtil.getCourseFolderRelativePath(relativeFilePath, e.getFileName());
			if(filePath == null) {
				// if e.getFileName is already a relative path, but may contain a leading slash that should be removed.
				filePath = e.getFileName().startsWith("/") ? e.getFileName().substring(1) : e.getFileName();
			}

			result = new ValidateFileResult(filePath, e.getReason(), e.getDetails(), null);
			cause = e.getCause() == null ? e : e.getCause();
			result.setSuccess(false);

		} catch (ConfigurationException e) {
			result = new ValidateFileResult(relativeFilePath, e.getReason(), e.getDetails(),null);
			cause = e.getCause() == null ? e : e.getCause();
			result.setSuccess(false);

		} catch (IllegalArgumentException | IOException | UnsupportedVersionException e) {
			result = new ValidateFileResult(relativeFilePath, e.getMessage(), e.getCause() == null ? "Unable to validate file." :  e.getCause().toString(), null);
			cause = e.getCause() == null ? e : e.getCause();
			result.setSuccess(false);

		}  catch (DetailedException e) {
			result = new ValidateFileResult(relativeFilePath, e.getReason(), e.getDetails(), e.getErrorStackTrace());
			cause = e.getCause() == null ? e : e.getCause();
			result.setSuccess(false);
		}

		if(!result.isSuccess() && cause != null) {
			// format the stack trace
			result.setStackTrace(DetailedException.getFullStackTrace(cause));
		}
		MetricsSenderSingleton.getInstance().endTrackingRpc("validateFile", start);
		return result;
	}

    @Override
    public FetchJAXBObjectResult getJAXBObject(String userName, String relativePath, boolean useParentAsCourse) {
	    long start = System.currentTimeMillis();
	    if(logger.isDebugEnabled()){
            logger.debug("execute()");
	    }

	    ///////////////////////////////////////////////////////////////////////
	    //Rudimentary error checking
	    ///////////////////////////////////////////////////////////////////////

	    //Quick null check because of a hack in the DKF presenter.
	    if(relativePath == null) {
	    	FetchJAXBObjectResult result = new FetchJAXBObjectResult();
	    	result.setSuccess(false);
	    	result.setErrorMsg("An error occurred while loading the file from the server.");
	    	result.setErrorDetails("Can't parse a JAXB object when supplied with an empty or undefined path.");
	    	return result;
	    }

	    //Make sure we support parsing this extension.
	    boolean isSupportedFile = false;
	    if(relativePath.endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION)) {
	    	isSupportedFile = true;
	    } else if(relativePath.endsWith(AbstractSchemaHandler.DKF_FILE_EXTENSION)) {
	    	isSupportedFile = true;
	    }  else if(relativePath.endsWith(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)) {
	    	isSupportedFile = true;
	    } else if(relativePath.endsWith(AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION)) {
	    	isSupportedFile = true;
	    } else if(relativePath.endsWith(AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION)) {
	    	isSupportedFile = true;
	    } else if(relativePath.endsWith(AbstractSchemaHandler.METADATA_FILE_EXTENSION)) {
	    	isSupportedFile = true;
	    } else if(relativePath.endsWith(AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION)) {
	    	isSupportedFile = true;
	    } else if(relativePath.endsWith(AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION)) {
	    	isSupportedFile = true;
	    } else if(relativePath.endsWith(AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION)) {
	    	isSupportedFile = true;
	    }
	    if(!isSupportedFile) {
	    	FetchJAXBObjectResult result = new FetchJAXBObjectResult();
	    	result.setSuccess(false);
	    	result.setErrorMsg("The file '" + relativePath + "' has a file extension that is not authorable and cannot be loaded into the course authoring tool.");
	    	result.setErrorDetails("The course authoring tool can only author files with the following extesions: "
	    			+ AbstractSchemaHandler.COURSE_FILE_EXTENSION + ", "
	    			+ AbstractSchemaHandler.DKF_FILE_EXTENSION + ", "
	    			+ AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION + ", "
	    			+ AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION + ", "
	    			+ AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION + ", "
	    			+ AbstractSchemaHandler.METADATA_FILE_EXTENSION + ", "
	    			+ AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION + ".");
	    	return result;
	    }

		///////////////////////////////////////////////////////////////////////
		//Handling the normal load case.
		///////////////////////////////////////////////////////////////////////
	    UnmarshalledFile loadedFile = null;

	    try{
	    	loadedFile = fileServices.unmarshalFile(userName, relativePath);
	    } catch(Exception e){

	    	logger.error("An error occurred while unmarshalling a JAXB object at '" + relativePath + "'.", e);

	    	FetchJAXBObjectResult result = new FetchJAXBObjectResult();
	       	result.setSuccess(false);

	        if(e instanceof DetailedException){
	        	result.setErrorMsg(((DetailedException) e).getReason());
	        	result.setErrorDetails(((DetailedException) e).getDetails());

	        } else {
	           	result.setErrorMsg("There was a problem reading the file at '" + relativePath + "' on the server. "
	           			+ "<br/>The most common issues are that the file doesn't exist, the file type is unknown or unhandled or the conversion wizard logic failed.");
	           	result.setErrorDetails(e.toString());
	        }

	        result.setErrorStackTrace(DetailedException.getFullStackTrace(e));

	       	return result;
	    }

	    Serializable jaxbObject = loadedFile.getUnmarshalled();

	    if(jaxbObject == null) {

	    	FetchJAXBObjectResult result = new FetchJAXBObjectResult();
	       	result.setSuccess(false);
	       	result.setErrorMsg("An error occurred while returning '" + relativePath + "' from the server.");
	       	result.setErrorDetails("Failed to unmarshal the JAXB object at '" + relativePath + "'.");

	       	return result;

	    } else {

	    	FetchJAXBObjectResult result = new FetchJAXBObjectResult();
	       	result.setJAXBObject(jaxbObject);

	       	boolean hasWritePermissions = false;

	       	try{

	        	hasWritePermissions = fileServices.hasWritePermissions(userName, relativePath);

		        if (jaxbObject instanceof generated.course.Course) {

	                // This is successful if the record is created or if the record already exists.
	                CourseRecord courseRecord = ServicesManager.getInstance().getDbServices().createCourseRecordIfNeeded(userName, relativePath, hasWritePermissions);
	                if (courseRecord == null) {
	                    result.setSuccess(false);
	                    result.setErrorMsg("There was a database error trying to create the course record for '" + relativePath + "'.");
	                    return result;
	                }

                    generated.course.Course course = (generated.course.Course) result.getJAXBObject();
                    CourseConceptsUtil.cleanCourseConcepts(course);
                } else if (jaxbObject instanceof generated.metadata.Metadata) {
                    generated.metadata.Metadata metadata = (generated.metadata.Metadata) result.getJAXBObject();
                    CourseConceptsUtil.cleanMetadataConcepts(metadata);
                } else if (jaxbObject instanceof generated.course.LessonMaterialList) {
                    generated.course.LessonMaterialList lessonMaterialList = (generated.course.LessonMaterialList) result
                            .getJAXBObject();
                    CourseConceptsUtil.cleanMediaConcepts(lessonMaterialList.getMedia());
                }

		        result.setSuccess(true);
		        result.setModifiable(hasWritePermissions);

	        } catch(DetailedException e){

		       	result.setSuccess(false);
	        	result.setErrorMsg(e.getReason());
	        	result.setErrorDetails(e.getDetails());

	        	MetricsSenderSingleton.getInstance().endTrackingRpc("util.FetchJAXBObject", start);

	        	return result;
            }

            if (loadedFile.isUpconverted()) {
	        	result.setConversionAttempted(true);

	        	result.setFilePath(relativePath);

	        	if(loadedFile.getPreConverted() != null && hasWritePermissions) {
	        		/* Workaround for #1617 (bugs with saving courses containing a Merrill's Branch Point)
	                 * Current solution is to backup the original file and overwrite it with the converted object.
	                 * When the result is returned, the file path is retrieved and the new file is saved.
	                 */
	        		try {

	        			String backupPath = relativePath + FileUtil.BACKUP_FILE_EXTENSION;

	            		fileServices.marshalToFile(userName, loadedFile.getPreConverted(), backupPath, loadedFile.getPreconvertedVersion(), useParentAsCourse);

					} catch(@SuppressWarnings("unused") WritePermissionException e){

					    if(logger.isInfoEnabled()){
				            logger.info("Tried to back up " + relativePath + " to " + relativePath
								+ FileUtil.BACKUP_FILE_EXTENSION +  "before converting it to the latest XML schema, but user '"
								+ userName + "' does not have permission to write the backup file.");
					    }

					} catch (IllegalArgumentException | IOException e) {

						logger.error("Caught exception while backing up previous file: " +  relativePath, e);

			           	result.setSuccess(false);
			           	result.setErrorMsg("An error occurred while trying to bring '" + relativePath + "' up to date with the latest version of GIFT.");
			           	result.setErrorDetails("Failed to create a backup of the original file named '" + relativePath
			           			+ "' while attempting to convert it to the latest version. " + e.toString());

			           	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));

			           	return result;

					}
	            }
            }

            MetricsSenderSingleton.getInstance().endTrackingRpc("util.FetchJAXBObject", start);
	        return result;
	    }
    }

    @Override
    public FetchContentAddressResult getAssociatedCourseImage(String userName, String relativeFileName) {
    	FetchContentAddressResult result = new FetchContentAddressResult();

    	// Ensure arguments are not null
	    if(userName == null) {
	    	result.setSuccess(false);
	    	result.setErrorMsg("An error occurred while loading the course image from the server.");
	    	result.setErrorDetails("Can't fetch content address because the userName was empty or undefined.");
	    	return result;
        }

	    if(relativeFileName == null) {
	    	result.setSuccess(false);
	    	result.setErrorMsg("An error occurred while loading the course image from the server.");
	    	result.setErrorDetails("Can't fetch content address because the relativeFileName was empty or undefined.");
	    	return result;
	    }

    	// Ensure relativeFileName starts with forward slash in server mode
        if (DomainModuleProperties.getInstance().getDeploymentMode().equals(DeploymentModeEnum.SERVER)){
            if (!relativeFileName.startsWith(Constants.FORWARD_SLASH)) {
                relativeFileName = Constants.FORWARD_SLASH + relativeFileName;
            }
        }

        DesktopFolderProxy desktopWorkspace = new DesktopFolderProxy(new File(ServicesProperties.getInstance().getWorkspaceDirectory()));
        String fileId = desktopWorkspace.getFileId();

        String contentURL = DomainCourseFileHandler.getAssociatedCourseImage(fileId, relativeFileName, userName);
    	result.setContentURL(contentURL);
        return result;
    }

    @Override
    public Set<ConditionInfo> getConditionsForTrainingApplication(TrainingApplicationEnum trainingApp) {
        return DomainKnowledgeUtil.getConditionInfosForTrainingApp(trainingApp);
    }

    @Override
    public Set<String> getConditionsThatCanComplete(){
        return DomainKnowledgeUtil.getConditionsThatCanComplete();
    }

    @Override
    public List<ConditionInfo> getConditionsForLearnerActions() {
        return DomainKnowledgeUtil.getConditionInfosForAllLearnerActions();
    }
    
    @Override
    public Boolean doesConceptHaveConflictingExternalResourceReferences(generated.dkf.Concept initialConcept, List<Concept> conceptList) {
    	if (initialConcept.getExternalSourceId() != null && initialConcept.getConditionsOrConcepts() instanceof generated.dkf.Conditions) {
    		// Loop through the Concepts to compare any concepts with the same external source ID
    		String initialSourceId = initialConcept.getExternalSourceId();
    		String initialXmlString = null;
			try {
				initialXmlString = AbstractSchemaHandler.getAsXMLString((generated.dkf.Conditions) initialConcept.getConditionsOrConcepts(), generated.dkf.Conditions.class, AbstractSchemaHandler.DKF_SCHEMA_FILE);
			} catch (SAXException e) {
				logger.error("Caught exception while checking for conflicting external source references for the Concept: " +  initialConcept.getName(), e);
			} catch (JAXBException e) {
				logger.error("Caught exception while checking for conflicting external source references for the Concept: " +  initialConcept.getName(), e);
			}
    		for (Concept conceptToCompare : conceptList) {
    			String sourceIdToCheck = conceptToCompare.getExternalSourceId();
    			if (sourceIdToCheck != null && sourceIdToCheck.equals(initialSourceId) && conceptToCompare.getConditionsOrConcepts() instanceof generated.dkf.Conditions) {
    				// Compare the conceptToCheck's conditions to initialConcept's conditions
    				String comparisonXmlString;
					try {
						comparisonXmlString = AbstractSchemaHandler.getAsXMLString((generated.dkf.Conditions) conceptToCompare.getConditionsOrConcepts(), generated.dkf.Conditions.class, AbstractSchemaHandler.DKF_SCHEMA_FILE);
					
	    				if (initialXmlString != null && !initialXmlString.equals(comparisonXmlString)) {
	    					return true;
	    				}
					} catch (SAXException e) {
						logger.error("Caught exception while checking for conflicting external source references for the Concept: " +  initialConcept.getName() + ". The error occurred while checking against the Concept: " + conceptToCompare.getName(), e);
					} catch (JAXBException e) {
						logger.error("Caught exception while checking for conflicting external source references for the Concept: " +  initialConcept.getName() + ". The error occurred while checking against the Concept: " + conceptToCompare.getName(), e);
					}
    			}
    		}
    	}
    	
    	// If no conflicts are found, return false.
    	return false;
    }
    
    @Override
    public GenericRpcResponse<Map<BigInteger, Concept>> copyChangesToConceptsWithDuplicateExternalSourceIds(Concept alteredConcept, Scenario dkfScenario) {
    	Map<BigInteger, Concept> alteredConceptsMap = new HashMap<BigInteger, Concept>();
    	
    	for (generated.dkf.Task currentTask : dkfScenario.getAssessment().getTasks().getTask()) {
    		for (generated.dkf.Concept startingConcept : currentTask.getConcepts().getConcept()) {
    			alteredConceptsMap = traverseConceptsAndCopyChangesToDuplicateExternalSourceIds(alteredConcept, startingConcept, alteredConceptsMap);
    		}
    	}
    	
    	return new SuccessfulResponse<Map<BigInteger, Concept>>(alteredConceptsMap);
    }

    private Map<BigInteger, Concept> traverseConceptsAndCopyChangesToDuplicateExternalSourceIds(Concept alteredConcept,
			Concept currentConcept, Map<BigInteger, Concept> currentMap) {
    	if (currentConcept.getExternalSourceId() != null && currentConcept.getExternalSourceId().equals(alteredConcept.getExternalSourceId())) {
    		try {
				String alteredConceptString = AbstractSchemaHandler.getAsXMLString(alteredConcept, generated.dkf.Concept.class, AbstractSchemaHandler.DKF_SCHEMA_FILE);
				try {
					UnmarshalledFile unmarshalledConceptCopy = AbstractSchemaHandler.getFromXMLString(alteredConceptString, generated.dkf.Concept.class, AbstractSchemaHandler.DKF_SCHEMA_FILE, false);
					Concept copyConcept = null;
					if (unmarshalledConceptCopy.getUnmarshalled() instanceof Concept) {
						copyConcept = (Concept) unmarshalledConceptCopy.getUnmarshalled();
					}
					if (copyConcept != null) {
						// Keep the concept's name and node ID, but copy all other properties (including child objects).
						currentConcept.setAssessments(copyConcept.getAssessments());
						currentConcept.setCompetenceMetric(copyConcept.getCompetenceMetric());
						currentConcept.setConditionsOrConcepts(copyConcept.getConditionsOrConcepts());
						currentConcept.setConfidenceMetric(copyConcept.getConfidenceMetric());
						currentConcept.setExternalSourceId(copyConcept.getExternalSourceId());
						currentConcept.setPerformanceMetric(copyConcept.getPerformanceMetric());
						currentConcept.setPerformanceMetricArguments(copyConcept.getPerformanceMetricArguments());
						currentConcept.setPriority(copyConcept.getPriority());
						currentConcept.setPriorityMetric(copyConcept.getPriorityMetric());
						currentConcept.setScenarioSupport(copyConcept.isScenarioSupport());
						currentConcept.setTrendMetric(copyConcept.getTrendMetric());
						
						currentMap.put(currentConcept.getNodeId(), copyConcept);
					}
				} catch (UnsupportedEncodingException | FileNotFoundException e) {
					logger.error("Caught exception while copying changes from " +  alteredConcept.getName() + " to other Concepts with the same externalSourceId", e);
				}
    		} catch (SAXException | JAXBException e) {
				logger.error("Caught exception while copying changes from " +  alteredConcept.getName() + " to other Concepts with the same externalSourceId", e);
			}
    	}
		
    	// Traverse any child Concepts
    	if (currentConcept.getConditionsOrConcepts() instanceof Concepts) {
    		List<Concept> childConcepts = ((Concepts) currentConcept.getConditionsOrConcepts()).getConcept();
    		for (Concept conceptToTraverse : childConcepts) {
    			currentMap = traverseConceptsAndCopyChangesToDuplicateExternalSourceIds(alteredConcept, conceptToTraverse, currentMap);
    		}
    	}
    	
    	return currentMap;
	}

	@Override
    public GenericRpcResponse<List<TrainingApplicationObject>> getTrainingApplicationObjects(
            TrainingApplicationEnum trainingApplicationType, String username, ProgressIndicator progressIndicator) {
        final String methodName = "getTrainingApplicationObjects";
        if (logger.isTraceEnabled()) {
            logger.trace(methodName + "(" + username + ")");
        }

        final long start = System.currentTimeMillis();

        Map<FileTreeModel, TrainingAppCourseObjectWrapper> libraryPathToTA = null;

        try {
            /* finds the training applications within the Public and username folders inside
             * workspace\Public\TrainingAppsLib */
            libraryPathToTA = fileServices.getTrainingAppCourseObjects(username, progressIndicator);
        } catch (Exception e) {
            if (e instanceof DetailedException) {
                MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
                return new FailureResponse<>((DetailedException) e);
            } else {
                MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
                return new FailureResponse<>(new DetailedException(
                        "An unexpected error ocurred while getting the list of training application objects.",
                        e.getMessage(), e));
            }
        }

        List<TrainingApplicationObject> libs = new ArrayList<>();
        if (libraryPathToTA != null) {
            for (Entry<FileTreeModel, TrainingAppCourseObjectWrapper> entry : libraryPathToTA.entrySet()) {
                /* no filter, add all */
                if (trainingApplicationType == null) {
                    libs.add(new TrainingApplicationObject(entry.getKey(), entry.getValue()));
                    continue;
                }

                /* check if the training application is of the correct type */
                final TrainingAppCourseObjectWrapper appObjWrapper = entry.getValue();
                if (appObjWrapper != null && appObjWrapper.getTrainingApplicationObj() != null) {
                    TrainingApplicationEnum typeToCompare = TrainingAppUtil
                            .getTrainingAppType(appObjWrapper.getTrainingApplicationObj());
                    if (trainingApplicationType.equals(typeToCompare)) {
                        libs.add(new TrainingApplicationObject(entry.getKey(), appObjWrapper));
                    }
                }
            }
        }

        if(progressIndicator != null){
            progressIndicator.setTaskDescription(TA_OBJECTS_FINISHED);
            progressIndicator.setPercentComplete(TA_OBJECTS_PERCENT_COMPLETE);
        }

        MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
        return new SuccessfulResponse<>(libs);
    }

    @Override
    public GenericRpcResponse<TrainingApplicationObject> checkTrainingApplicationPath(
            FileTreeModel trainingApplicationFolderPath, String username) {
        final String methodName = CHECK_TA_PATH;
        final long start = System.currentTimeMillis();

        GenericRpcResponse<List<TrainingApplicationObject>> trainingAppObjectsResponse = getTrainingApplicationObjects(
                null, username, null);
        if (trainingAppObjectsResponse.getWasSuccessful()) {

            final String toCheckPath = trainingApplicationFolderPath.getRelativePathFromRoot();
            for (TrainingApplicationObject appObj : trainingAppObjectsResponse.getContent()) {
                if (appObj.getLibraryPath() == null) {
                    continue;
                }

                final String appObjPath = appObj.getLibraryPath().getRelativePathFromRoot();
                if (StringUtils.equalsIgnoreCase(toCheckPath, appObjPath)) {
                    MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
                    return new SuccessfulResponse<>(appObj);
                }
            }

            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new SuccessfulResponse<>(null);
        }

        GenericRpcResponse<TrainingApplicationObject> response = new GenericRpcResponse<>();
        response.setWasSuccessful(false);
        response.setException(trainingAppObjectsResponse.getException());

        MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
        return response;
    }

    @Override
    public GenericRpcResponse<FileTreeModel> getTrainingAppsLibUserFolder(String username) {
        final long start = System.currentTimeMillis();

        // get the training apps lib user folder, but don't create the folder if it doesn't exist
        final FileTreeModel trainingAppsLibUserFolder = fileServices.getTrainingAppsLibUserFolder(username, false);

        MetricsSenderSingleton.getInstance().endTrackingRpc("getTrainingAppsLibUserFolder", start);
        return new SuccessfulResponse<>(trainingAppsLibUserFolder);

    }

    @Override
    public GenericRpcResponse<Void> deleteTrainingApplicationObject(TrainingApplicationObject object, String username) {
        final String methodName = DELETE_TA_OBJECT;
        if (logger.isTraceEnabled()) {
            logger.trace(methodName + "(" + username + ")");
        }

        final long start = System.currentTimeMillis();

        if (object != null && object.getLibraryPath() != null) {
            FileTreeModel taObjectFolder = object.getLibraryPath();

            try {
                fileServices.deleteFile(username, username,
                        taObjectFolder.getRelativePathFromRoot(true), null, false);
                MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
                return new SuccessfulResponse<>();
            } catch (DetailedException e) {
                MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
                return new FailureResponse<>(e);
            } catch (IllegalArgumentException e) {
                MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
                return new FailureResponse<>(new DetailedException(
                        "A problem with the training application object's folder has prevented it from being deleted.",
                        "Detected a problem with " + taObjectFolder.getRelativePathFromRoot()
                                + " while attempting to delete it. " + "The deletion operation has been aborted.",
                        e));
            }
        }

        // find app name if it exists for error messages
        String appName = null;
        if (object != null) {
            if (object.getTrainingApplication() != null
                    && object.getTrainingApplication().getTrainingApplicationObj() != null) {
                appName = object.getTrainingApplication().getTrainingApplicationObj().getTransitionName();
            } else if (object.getLibraryPath() != null) {
                appName = object.getLibraryPath().getFileOrDirectoryName();
            }
        }

        /* add quotes to make for easier reading, or make an empty string to remove it from the
         * error message. */
        appName = StringUtils.isNotBlank(appName) ? appName = " '" + appName + "'" : "";

        MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
        return new FailureResponse<>(new DetailedException(
                "A problem occurred while attempting to delete the training application object" + appName
                        + ". The object you attempted to delete might be corrupted. <br/><br/>Try refreshing the page and reattempting the delete to see if the problem persists.",
                "Insufficient data to connect to delete the training application object" + appName
                        + ". Could not determine the path to the training application folder to delete.",
                new IllegalArgumentException(
                        "Failed to provide enough information to delete a training application object" + appName
                                + ". The given object does not contain a path to its associated folder.")));
    }

    @Override
    public GenericRpcResponse<FileTreeModel> createTrainingAppsLibFolder(String folderName, String username) {
        final String methodName = "createTrainingAppsLibFolder";
        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder(methodName).append("(");
            List<Object> params = Arrays.<Object>asList(folderName, username);
            StringUtils.join(", ", params, sb);
            logger.trace(sb.append(")").toString());
        }

        if (StringUtils.isBlank(folderName)) {
            throw new IllegalArgumentException("The parameter 'folderName' cannot be blank.");
        } else if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        }

        final long start = System.currentTimeMillis();

        try {
            if (StringUtils.isBlank(folderName)) {
                MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
                return new FailureResponse<>(new DetailedException(
                        "Cannot create a folder with a blank name.",
                        "Encountered an error when trying to create a new training app libs folder with a blank name.",
                        null));
            }

            FileTreeModel parentFolder = fileServices.getTrainingAppsLibUserFolder(username, true);
            final FileTreeModel childFolder = parentFolder.getModelFromRelativePath(folderName);
            /* if folder already exists, delete it so it can be create anew. Training Apps Lib
             * requires a fresh folder */
            if (fileServices.fileExists(username, childFolder.getRelativePathFromRoot(true), true)) {
                fileServices.deleteFile(username, username, childFolder.getRelativePathFromRoot(true), null, false);
            }

            fileServices.createFolder(username, parentFolder.getRelativePathFromRoot(true), folderName, false);

            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new SuccessfulResponse<>(parentFolder.getModelFromRelativePath(folderName));
        } catch (DetailedException e) {
            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new FailureResponse<>(e);
        } catch (IllegalArgumentException e) {
            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new FailureResponse<>(new DetailedException(
                    "Failed to create training apps lib folder '" + folderName + "' for user '" + username + "'.",
                    "An error occurred when trying to create training apps lib folder '" + folderName + "' for user '"
                            + username + "'. Reason: " + e.getMessage(),
                    e));
        }
    }

    @Override
    public GenericRpcResponse<Void> saveTACourseObject(TrainingApplicationObject objectData,
            FileTreeModel oldCourseFolderPath, String username) {
        final String methodName = "saveTACourseObject";

        if (objectData == null) {
            throw new IllegalArgumentException("The parameter 'objectData' cannot be null.");
        } else if (objectData.getLibraryPath() == null) {
            throw new IllegalArgumentException("The parameter 'objectData.getLibraryPath()' cannot be null.");
        } else if (objectData.getTrainingApplication() == null) {
            throw new IllegalArgumentException("The parameter 'objectData.getTrainingApplication()' cannot be null.");
        } else if (objectData.getTrainingApplication().getTrainingApplicationObj() == null) {
            throw new IllegalArgumentException(
                    "The parameter 'objectData.getTrainingApplication().getTrainingApplicationObj()' cannot be null.");
        } else if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        }

        final long start = System.currentTimeMillis();

        final FileTreeModel trainingAppLibraryPath = objectData.getLibraryPath();
        final String newName = trainingAppLibraryPath.getFileOrDirectoryName();

        try {
            if (oldCourseFolderPath != null
                    && !StringUtils.equals(oldCourseFolderPath.getFileOrDirectoryName(), newName)) {
                /* rename the folder within training apps lib and any descendent files that have the
                 * same name */
                fileServices.renameTrainingAppsLibFolder(username, oldCourseFolderPath.getRelativePathFromRoot(true),
                        newName);
            }

            FileTreeModel fileToCreate = trainingAppLibraryPath
                    .getModelFromRelativePath(newName + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
            final String currentSchemaVersion = Version.getInstance().getCurrentSchemaVersion();

            // create Training Application Wrapper
            TrainingApplicationWrapper appWrapper = new TrainingApplicationWrapper();
            appWrapper.setTrainingApplication(objectData.getTrainingApplication().getTrainingApplicationObj());
            appWrapper.setVersion(CommonUtil.generateVersionAttribute(null, currentSchemaVersion));
            fileServices.marshalToFile(username, appWrapper, fileToCreate.getRelativePathFromRoot(true), null, true);

            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new SuccessfulResponse<>();
        } catch (DetailedException de) {
            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new FailureResponse<>(de);
        } catch (Exception e) {
            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new FailureResponse<>(new DetailedException(e.getMessage(), e.getMessage(), e));
        }
    }

    @Override
    public GenericRpcResponse<List<GiftScenarioProperties>> getTrainingApplicationScenarioProperties(String username) {
        final String methodName = "getTrainingApplicationScenarioProperties";

        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        }

        final long start = System.currentTimeMillis();

        List<GiftScenarioProperties> scenarioProperties;
        try {
            scenarioProperties = fileServices.getTrainingApplicationScenarioProperties(username);
        } catch (IOException e) {
            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new FailureResponse<>(new DetailedException(
                    "Failed to retrieve the training application scenario property files for user '" + username + "'.",
                    "An error occurred when trying to retrieve the training application scenario property files for user '"
                            + username + "'. Reason: " + e.getMessage(),
                    e));
        }

        MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
        return new SuccessfulResponse<>(scenarioProperties);
    }

    @Override
    public GenericRpcResponse<GiftScenarioProperties> getTrainingApplicationScenarioProperty(String folderPath,
            String username) {
        final String methodName = "getTrainingApplicationScenarioProperty";

        if (StringUtils.isBlank(folderPath)) {
            throw new IllegalArgumentException("The parameter 'folderPath' cannot be blank.");
        } else if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        }

        final long start = System.currentTimeMillis();

        GiftScenarioProperties scenarioProperties;
        try {
            scenarioProperties = fileServices.getTrainingApplicationScenarioProperty(folderPath, username);
        } catch (IOException e) {
            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new FailureResponse<>(new DetailedException(
                    "Failed to retrieve the training application scenario properties for user '" + username + "'.",
                    "An error occurred when trying to retrieve the training application scenario properties for user '"
                            + username + "'. Reason: " + e.getMessage(),
                    e));
        }

        MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
        return new SuccessfulResponse<>(scenarioProperties);
    }

    @Override
    public GenericRpcResponse<List<MapTileProperties>> getScenarioMapTileProperties(String folderPath,
            String username) {
        final String methodName = "getScenarioMapTileProperties";

        if (StringUtils.isBlank(folderPath)) {
            throw new IllegalArgumentException("The parameter 'folderPath' cannot be blank.");
        } else if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        }

        final long start = System.currentTimeMillis();

        List<MapTileProperties> mapTileProperties;
        try {
            mapTileProperties = fileServices.getScenarioMapTileProperties(folderPath, username);
        } catch (IOException e) {
            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new FailureResponse<>(new DetailedException(
                    "Failed to retrieve the training application scenario map tile property files for user '" + username
                            + "'.",
                    "An error occurred when trying to retrieve the training application scenario map tile property files for user '"
                            + username + "'. Reason: " + e.getMessage(),
                    e));
        }
        
        MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
        return new SuccessfulResponse<>(mapTileProperties);
    }

    @Override
    public GenericRpcResponse<List<Serializable>> getPlacesOfInterestFromFile(String username, String domainFilePath, Boolean ignoreElevation) {

        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        } else if (StringUtils.isBlank(domainFilePath)) {
            throw new IllegalArgumentException("The parameter 'domainFilePath' cannot be blank.");
        }

        final String methodName = "getPlacesOfInterestFromFile";
        final long start = System.currentTimeMillis();

        List<Serializable> gatheredPois = new ArrayList<>();

        File file = new File(CommonProperties.getInstance().getDomainDirectory() + File.separator + domainFilePath);

        if(ignoreElevation == null) {

            //user cancelled when asked whether to ignore elevation, so just clean up the uploaded file
            if(!file.getParent().equals(CommonProperties.getInstance().getDomainDirectory())) {

                try {

                    // delete the unique folder that was generated for this file
                    FileUtils.deleteDirectory(file.getParentFile());

                } catch (IOException e) {
                    logger.warn("Failed to clean up " + domainFilePath + " after attempting to gather places of interest from it.", e);
                }

            } else {
               file.delete();
            }

            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new SuccessfulResponse<>(gatheredPois);

        }

        try (Scanner parser = new Scanner(file)){

            //only handling CSV files for now, so assume all tokens are comma-delimited
            String DELIMITER = ",";
            final String NAME_TAG = "annotation";

            // GDC column names
            final String LATITUDE_TAG = "lat";
            final String LONGITUDE_TAG = "lon";
            final String ELEVATION_TAG = "elevation";

            // GCC column names
            final String GCC_X_TAG = "x";
            final String GCC_Y_TAG = "y";
            final String GCC_Z_TAG = "z";
            
            // AGL column names
            final String AGL_X_TAG = "aglx";
            final String AGL_Y_TAG = "agly";
            final String AGL_ELEV_TAG = "aglelevation";

            // GDC column index (0 based)
            Integer latitudeColumn = null;
            Integer longitudeColumn = null;
            Integer elevationColumn = null;
            Integer nameColumn = null;
            
            // GCC column index (0 based)
            Integer gccXColumn = null;
            Integer gccYColumn = null;
            Integer gccZColumn = null;
            
            // AGL column index (0 based)
            Integer aglXColumn = null;
            Integer aglYColumn = null;
            Integer aglElevColumn = null;
            
            CoordinateType coordType = null;
            boolean isMissingGDCColumn = true;
            boolean isMissingGCCColumn = true;
            boolean isMissingAGLColumn = true;
            
            while(parser.hasNextLine()) {

                String line = parser.nextLine();
                String[] values = line.split(DELIMITER);

                if(coordType == null ||
                        (coordType == CoordinateType.GDC && isMissingGDCColumn) || 
                        (coordType == CoordinateType.GCC && isMissingGCCColumn) ||
                        (coordType == CoordinateType.AGL && isMissingAGLColumn) && 
                        nameColumn == null) {

                    //header tags should be present on the first row, so gather them first
                    for(int i = 0 ; i < values.length; i++) {

                        String columnName = values[i];

                        switch(columnName.toLowerCase()) {

                            case LATITUDE_TAG:
                                coordType = CoordinateType.GDC;
                                latitudeColumn = i;
                                break;

                            case LONGITUDE_TAG:
                                coordType = CoordinateType.GDC;
                                longitudeColumn = i;
                                break;

                            case ELEVATION_TAG:
                                coordType = CoordinateType.GDC;
                                elevationColumn = i;
                                break;

                            case NAME_TAG:
                                nameColumn = i;
                                break;
                                
                            case GCC_X_TAG:
                                coordType = CoordinateType.GCC;
                                gccXColumn = i;
                                break;
                                
                            case GCC_Y_TAG:
                                coordType = CoordinateType.GCC;
                                gccYColumn = i;
                                break;
                                
                            case GCC_Z_TAG:
                                coordType = CoordinateType.GCC;
                                gccZColumn = i;
                                break;
                                
                            case AGL_X_TAG:
                                coordType = CoordinateType.AGL;
                                aglXColumn = i;
                                break;
                                
                            case AGL_Y_TAG:
                                coordType = CoordinateType.AGL;
                                aglYColumn = i;
                                break;
                                
                            case AGL_ELEV_TAG:
                                coordType = CoordinateType.AGL;
                                aglElevColumn = i;
                                break;
                        }
                    }                    
                    
                    isMissingGDCColumn = coordType == CoordinateType.GDC ? latitudeColumn == null || longitudeColumn == null ||
                            (elevationColumn == null && !ignoreElevation) : true;
                            
                    isMissingGCCColumn = coordType == CoordinateType.GCC ? gccXColumn == null || gccYColumn == null ||
                            gccZColumn == null : true;
                    
                    isMissingAGLColumn = coordType == CoordinateType.AGL ? aglXColumn == null || aglYColumn == null ||
                            aglElevColumn == null : true;

                    if(coordType != null && (
                            (coordType == CoordinateType.GDC && isMissingGDCColumn) || 
                            (coordType == CoordinateType.GCC && isMissingGCCColumn) ||
                            (coordType == CoordinateType.AGL && isMissingAGLColumn))) {

                        //if any required header tags are still missing, give the user an error indicating which tags are missing
                        List<String> missingTags = new ArrayList<>();

                        switch(coordType){
                        case GDC:
                            if(latitudeColumn == null) {
                                missingTags.add(LATITUDE_TAG);
                            }
    
                            if(longitudeColumn == null) {
                                missingTags.add(LONGITUDE_TAG);
                            }
    
                            if(elevationColumn == null) {
                                missingTags.add(ELEVATION_TAG);
                            }
                            break;
                        case GCC:
                            if(gccXColumn == null){
                                missingTags.add(GCC_X_TAG);
                            }
                            
                            if(gccYColumn == null){
                                missingTags.add(GCC_Y_TAG);
                            }
                            
                            if(gccZColumn == null){
                                missingTags.add(GCC_Z_TAG);
                            }
                            
                            break;
                        case AGL:
                            if(aglXColumn == null){
                                missingTags.add(AGL_X_TAG);
                            }
                            
                            if(aglYColumn == null){
                                missingTags.add(AGL_Y_TAG);
                            }
                            
                            if(aglElevColumn == null){
                                missingTags.add(AGL_ELEV_TAG);
                            }
                            
                            break;
                        }

                        if(nameColumn == null) {
                            missingTags.add(NAME_TAG);
                        }

                        StringBuilder tagsString = new StringBuilder();
                        int tagsSize = missingTags.size();

                        for(int i = 0; i < tagsSize; i++) {

                            String tag = missingTags.get(i);

                            if(i != 0 && i == tagsSize - 1) {
                                tagsString.append("and ");
                            }

                            tagsString.append("\"")
                            .append(tag)
                            .append("\"");

                            if(i != tagsSize - 1) {
                                tagsString.append(", ");
                            }
                        }

                        throw new DetailedException(
                                "Places of interest could not be created from the uploaded file because the file's "
                                + "first line did not contain the following required column names: "
                                + tagsString
                                + ". Please check your file and make sure it contains these column names before "
                                + "reattempting to create places of interest using it.",
                                "Failed to parse '" + domainFilePath + "' because it did not contain one or more required columns.",
                                new IllegalArgumentException("The following columns were not found in the uploaded file: " + tagsString));
                    }

                } else {

                    //once header tags have been determined, use them to create places of interest based on values in their columns
                    String name = null;
                    generated.dkf.Coordinate coordinate = null;
                    switch(coordType){
                    case GDC:
                        double latitude = 0;
                        double longitude = 0;
                        double elevation = 0;
    
                        for(int column = 0; column < values.length; column++) {
    
                            String currentToken = values[column];
    
                            if(currentToken != null) {
    
                                if(latitudeColumn != null && column == latitudeColumn){
    
                                    try {
                                        latitude = Double.valueOf(currentToken);
    
                                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                                        //just use 0 instead of this value, since it is malformed
                                    }
    
                                } else if(longitudeColumn != null && column == longitudeColumn) {
    
                                    try {
                                        longitude = Double.valueOf(currentToken);
    
                                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                                        //just use 0 instead of this value, since it is malformed
                                    }
    
                                } else if(!ignoreElevation && elevationColumn != null && column == elevationColumn) {
    
                                    try {
                                        elevation = Double.valueOf(currentToken);
    
                                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                                        //just use 0 instead of this value, since it is malformed
                                    }
    
                                } else if(nameColumn != null && column == nameColumn) {
    
                                    //trim quotes and whitespace from place of interest names
                                    String tempName = StringUtils.trim(currentToken, true, "\"");
    
                                    if(StringUtils.isNotBlank(tempName)) {
                                        name = tempName;
                                    }
                                }
                            }
                        }
                        
                        generated.dkf.GDC gdclocation = new generated.dkf.GDC();
                        gdclocation.setLatitude(BigDecimal.valueOf(latitude));
                        gdclocation.setLongitude(BigDecimal.valueOf(longitude));
                        gdclocation.setElevation(BigDecimal.valueOf(elevation));
    
                        coordinate = new Coordinate();
                        coordinate.setType(gdclocation);
                        
                        break;
                    case GCC:
                        
                        double x = 0;
                        double y = 0;
                        double z = 0;
    
                        for(int column = 0; column < values.length; column++) {
    
                            String currentToken = values[column];
    
                            if(currentToken != null) {
    
                                if(gccXColumn != null && column == gccXColumn){
    
                                    try {
                                        x = Double.valueOf(currentToken);
    
                                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                                        //just use 0 instead of this value, since it is malformed
                                    }
    
                                } else if(gccYColumn != null && column == gccYColumn) {
    
                                    try {
                                        y = Double.valueOf(currentToken);
    
                                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                                        //just use 0 instead of this value, since it is malformed
                                    }
    
                                } else if(gccZColumn != null && column == gccZColumn) {
    
                                    try {
                                        z = Double.valueOf(currentToken);
    
                                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                                        //just use 0 instead of this value, since it is malformed
                                    }
    
                                } else if(nameColumn != null && column == nameColumn) {
    
                                    //trim quotes and whitespace from place of interest names
                                    String tempName = StringUtils.trim(currentToken, true, "\"");
    
                                    if(StringUtils.isNotBlank(tempName)) {
                                        name = tempName;
                                    }
                                }
                            }
                        }
                        
                        generated.dkf.GCC gcclocation = new generated.dkf.GCC();
                        gcclocation.setX(BigDecimal.valueOf(x));
                        gcclocation.setY(BigDecimal.valueOf(y));
                        gcclocation.setZ(BigDecimal.valueOf(z));
    
                        coordinate = new Coordinate();
                        coordinate.setType(gcclocation);
                        
                        break;
                    case AGL:
                        double aglx = 0;
                        double agly = 0;
                        double elev = 0;
    
                        for(int column = 0; column < values.length; column++) {
    
                            String currentToken = values[column];
    
                            if(currentToken != null) {
    
                                if(aglXColumn != null && column == aglXColumn){
    
                                    try {
                                        x = Double.valueOf(currentToken);
    
                                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                                        //just use 0 instead of this value, since it is malformed
                                    }
    
                                } else if(aglYColumn != null && column == aglYColumn) {
    
                                    try {
                                        y = Double.valueOf(currentToken);
    
                                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                                        //just use 0 instead of this value, since it is malformed
                                    }
    
                                } else if(aglElevColumn != null && column == aglElevColumn) {
    
                                    try {
                                        elev = Double.valueOf(currentToken);
    
                                    } catch(@SuppressWarnings("unused") NumberFormatException e){
                                        //just use 0 instead of this value, since it is malformed
                                    }
    
                                } else if(nameColumn != null && column == nameColumn) {
    
                                    //trim quotes and whitespace from place of interest names
                                    String tempName = StringUtils.trim(currentToken, true, "\"");
    
                                    if(StringUtils.isNotBlank(tempName)) {
                                        name = tempName;
                                    }
                                }
                            }
                        }
                        
                        generated.dkf.AGL agllocation = new generated.dkf.AGL();
                        agllocation.setX(BigDecimal.valueOf(aglx));
                        agllocation.setY(BigDecimal.valueOf(agly));
                        agllocation.setElevation(BigDecimal.valueOf(elev));
    
                        coordinate = new Coordinate();
                        coordinate.setType(agllocation);
                        break;
                    }

                    if(name != null && coordinate != null) {

                        generated.dkf.Point pointOfInterest = new generated.dkf.Point();
                        pointOfInterest.setName(name);
                        pointOfInterest.setCoordinate(coordinate);

                        gatheredPois.add(pointOfInterest);
                    }
                }
            }

        } catch (FileNotFoundException e) {

            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new FailureResponse<>(new DetailedException(
                    "Importing places from " + domainFilePath + " failed because the file was not found on the server.",
                    "Failed to initialize scanner for " + domainFilePath + " because the file does not exist.",
                    e));

        } catch (DetailedException e) {

            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new FailureResponse<>(e);

        } catch (Exception e) {

            MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
            return new FailureResponse<>(new DetailedException(
                    "Importing places from " + domainFilePath + " failed because of an unexpected error.",
                    "Failed to parse " + domainFilePath + " for places of interest because an exception occurred on the server.",
                    e));

        } finally {

            if(!file.getParent().equals(CommonProperties.getInstance().getDomainDirectory())) {

                try {

                    // delete the unique folder that was generated for this file
                    FileUtils.deleteDirectory(file.getParentFile());

                } catch (IOException e) {
                    logger.warn("Failed to clean up " + domainFilePath + " after attempting to gather places of interest from it.", e);
                }

            } else {
               file.delete();
            }
        }

        MetricsSenderSingleton.getInstance().endTrackingRpc(methodName, start);
        return new SuccessfulResponse<>(gatheredPois);
    }

    @Override
    public GenericRpcResponse<XTSPImporterResult> importXtspIntoScenarioFile(String username, String dkfPath, String xtspFilePath, TrainingApplicationEnum trainingAppType) {
        try {
            if (StringUtils.isBlank(username)) {
                throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
            } else if (StringUtil.isBlank(dkfPath)) {
                throw new IllegalArgumentException("The parameter 'dkfPath' cannot be blank.");
            } else if (StringUtil.isBlank(xtspFilePath)) {
                throw new IllegalArgumentException("The parameter 'xtspFilePath' cannot be blank.");
            } else if (trainingAppType == null) {
                throw new IllegalArgumentException("The parameter 'trainingAppType' cannot be null.");
            }
            
            final long start = System.currentTimeMillis();

            // parse the DKF
            FileProxy xtspProxy = fileServices.getFile(xtspFilePath, username);
            FetchJAXBObjectResult jaxbResult = getJAXBObject(username, dkfPath, false);
            
            Serializable jaxbObject = jaxbResult.getJAXBObject();
                        
            if(jaxbResult.isSuccess() && jaxbObject instanceof Scenario) {
                Scenario dkfScenario = (Scenario) jaxbObject;
                XTSPImporter importer = new XTSPImporter();
                importer.importXtspIntoDkf(username, dkfScenario, dkfPath, xtspProxy, TrainingApplicationEnum.getValidCoordinateTypes(trainingAppType));
                
                List<String> courseConceptNameList = importer.getCourseConceptNames();
                List<DetailedException> errorLogList = importer.getErrorLogList();
                
                XTSPImporterResult xtspImporterResult = new XTSPImporterResult(courseConceptNameList, errorLogList);
                
                MetricsSenderSingleton.getInstance().endTrackingRpc("importXtspIntoScenarioFile", start);
                
                return new SuccessfulResponse<XTSPImporterResult>(xtspImporterResult);
            } else {
                throw new Exception("Error reading the DKF template file while importing data from an XTSP file.");
            }
        } catch (IllegalArgumentException e) {
            String details;
            
            /* 
             * For an IllegalArgumentException, the exception's message should define which argument is invalid. 
             * If this is not the case, use a more general explanation.
             */
            if (StringUtils.isBlank(e.getMessage())) {
                details = "An invalid argument was passed into GatRpcServiceImpl.importXtspIntoScenarioFile." 
                        + " The user name, DKF path, or XTSP file path are invalid. Check that these values are being set properly.";
            } else {
                details = "An invalid argument was passed into GatRpcServiceImpl.importXtspIntoScenarioFile. " + e.getMessage();
            }
            
            return new FailureResponse<>(new DetailedException(
                    "Error importing XTSP file. Make sure valid file paths were set.",
                    details,
                    e));
        }
        catch (Throwable e) {
            return new FailureResponse<>(new DetailedException(
                    "Error importing XTSP file. Make sure the XTSP JSON file provided is the correct format and version that GIFT is expecting.",
                    "Make sure the XTSP JSON file provided is the correct format and version that GIFT is expecting. An error occurred importing XTSP file "
                            + xtspFilePath + " into DKF " + dkfPath + ".",
                    e));
        }
        
    }

    @Override
    public GenericRpcResponse<Set<String>> getScenarioConcepts(String username, String scenarioFilePath, Set<String> courseConcepts){

        Set<String> assessedCourseConcepts = new HashSet<>();

        try {

            //parse the scenario file
            FetchJAXBObjectResult jaxbResult = getJAXBObject(username, scenarioFilePath, false);

            Serializable jaxbObject = jaxbResult.getJAXBObject();
            if(jaxbResult.isSuccess() && jaxbObject instanceof Scenario) {

                //read the extracted scenario to figure out which course concepts it implements
                Scenario scenario = (Scenario) jaxbObject;
                if(scenario.getAssessment() != null && scenario.getAssessment().getTasks() != null) {

                    for(Task task : scenario.getAssessment().getTasks().getTask()) {

                        if(task.getConcepts() != null) {
                            for(Concept concept : task.getConcepts().getConcept()) {
                                getAssessingCourseConcepts(concept, courseConcepts, assessedCourseConcepts);
                            }
                        }
                    }
                }
            }

        } catch(DetailedException de){
            return new FailureResponse<>(de);

        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "Getting scenario concepts from " + scenarioFilePath + " failed because of an unexpected error.",
                    "Failed to parse " + scenarioFilePath + "  for concepts because an exception occurred on the server.",
                    e));
        }

        return new SuccessfulResponse<>(assessedCourseConcepts);
    }

    /**
     * Populated the assessed course concepts collection with the set of course concepts that
     * are a match to concepts at or under this concept
     *
     * @param concept the concept currently being checked. Will not be assessed if null.
     * @param courseConcepts the course concepts to check for at or under this concept.  Can contain case sensitive strings 
     * (i.e. the course concepts appear as they were authored, with capital letters)
     * @param assessedCourseConcepts the set of course concepts found to be a match by string comparison
     * from the course concepts set.
     */
    public void getAssessingCourseConcepts(Concept concept, Collection<String> courseConcepts, Set<String> assessedCourseConcepts){

        if(concept == null) {
            return;
        }

        if(CollectionUtils.isEmpty(courseConcepts)){
            return;

        }else if(assessedCourseConcepts == null){
            throw new IllegalArgumentException("The assessed course concepts is null.");
        }

        // perform a case insensitive comparison to see if the concept is in the course concepts
        final String conceptName = concept.getName().toLowerCase().trim();
        for(String courseConcept : courseConcepts){
            if(courseConcept.equalsIgnoreCase(conceptName)){
                assessedCourseConcepts.add(conceptName);
                break;
            }
        }

        if(concept.getConditionsOrConcepts() instanceof Concepts) {

            for(Concept childConcept : ((Concepts )concept.getConditionsOrConcepts()).getConcept()){
                getAssessingCourseConcepts(childConcept, courseConcepts, assessedCourseConcepts);
            }
        }
    }
    
    /**
     * Parses the given object as an authoritative resource and uses it to build an {@link AuthoritativeResourceRecord} that authors can interact with
     *
     * @param object the object to build the resource record from. Cannot be null.
     * @throws ParseException if the given object cannot be parsed as an authoritative resource. 
     */
    private AuthoritativeResourceRecord convertAuthoritativeResource(Object parsedObject) throws ParseException{
    	
    	JSONObject jsonObj = (JSONObject) parsedObject;
    	
    	String id = null;
        Object idObj = jsonObj.get("@id");
        if (idObj instanceof String) {
            id = (String) idObj;

        }
        String resourceName = null;
        Object nameObj = (jsonObj).get("name");
        if (nameObj instanceof JSONArray) {

            for (Object nameItem : ((JSONArray) nameObj)) {
                if (nameItem instanceof String) {
                    resourceName = (String) nameItem;
                    continue;

                } else if (nameItem instanceof JSONObject) {
                    Object value = ((JSONObject) nameItem).get("@value");
                    if (value instanceof String) {
                        resourceName = (String) value;
                    }
                }
            }
            
        } else if(nameObj instanceof JSONObject) {
            Object value = ((JSONObject) nameObj).get("@value");
            if(value instanceof String) {
                resourceName = (String) value;
            }
            
        } else if(nameObj instanceof String) {
            resourceName = (String) nameObj;
        }

        String description = null;
        Object descriptionObj = jsonObj.get("description");
        if (descriptionObj instanceof JSONArray) {
            for (Object descriptionItem : ((JSONArray) descriptionObj)) {
                if (descriptionItem instanceof String) {
                    description = (String) descriptionItem;
                    continue;

                } else if (descriptionItem instanceof JSONObject) {
                    Object value = ((JSONObject) descriptionItem).get("@value");
                    if (value instanceof String) {
                       description = (String) value;
                    }
                }
            }
            
        } else if (descriptionObj instanceof JSONObject) {
            Object value = ((JSONObject) descriptionObj).get("@value");
            if (value instanceof String) {
               description = (String) value;
            }
            
        } else if(descriptionObj instanceof String) {
            description = (String) descriptionObj;
        }

        String resourceType = null;
        Object typeObj = jsonObj.get("@type");
        if (typeObj instanceof String) {
            resourceType = (String) typeObj;
        }

        List<String> children = null;
        Object compencencyObj = jsonObj.get("competency");
        if (compencencyObj instanceof JSONArray) {
            children = new ArrayList<>();

            for (Object competency : ((JSONArray) compencencyObj)) {
                if (competency instanceof String) {
                    children.add((String) competency);
                }
            }
            
        }
        
           if(children == null) {
            children = new ArrayList<String>();
            children.add("Example Subcompetency");
        }

        AuthoritativeResourceRecord resource = new AuthoritativeResourceRecord(id, resourceName,
                description, resourceType, children);
        
        return resource;
    }

    @Override
    public GenericRpcResponse<AuthoritativeResourceRecord> getAuthoritiativeResource(String id){
  
         try {
				return new SuccessfulResponse<>(requestAuthoritativeResource(id));
			} catch (Exception e) {
				 return new FailureResponse<>(new DetailedException(
		                    "Unable to retrive authoritative resource", 
		                    "An error occurred while attempting to locate the authoritative resource", 
		                    e));
			            }
                }
    

    @Override
    public GenericRpcResponse<List<AuthoritativeResourceRecord>> queryAuthoritativeResources(String type, String name,
            Integer start, Integer size) {
    
        RESTClient restClient = new RESTClient();
        String authoritativeSystem = CommonProperties.getInstance().getAuthoritativeSystemUrl();
        byte[] response;
        String query = "q=";
        
        if(StringUtils.isBlank(authoritativeSystem)) {
        	return new FailureResponse<>(new DetailedException(
        		    "No resources could be found because GIFT is not configured to connect to an authoritative resource system. Please contact your system administrator.",
        		    "No authoritative system URL has been configured for this instance of GIFT",
        		    null));
        }
        
        String url = authoritativeSystem + "?" + query;
        
        List<AuthoritativeResourceRecord> resourceList = new ArrayList<>();
        String typeParam = "@type:"  + (type != null ? type : "Framework");  
        query += typeParam; 
        
        
        if(name != null) {
        	query += "%20AND%20name:" + name;
        }
        
        if(start != null) {
        	query += "&start=" + start;
        }
        
        if(size != null) {
        	query += "&size=" + size;
        }
        
        url = CommonProperties.getInstance().getAuthoritativeSystemUrl() + "?" + query;
        
        try {

            response = restClient.get(new URL(url));

        } catch (MalformedURLException e) {
             return new FailureResponse<>(new DetailedException(
                     "Unable to retrive authoritative resource",
                     "The ID of the authoritative resource is malformed",
             e));
        } catch (IOException e) {
            return new FailureResponse<>(new DetailedException("Unable to retrive authoritative resource",
                    "An error occurred while attempting to locate the authoritative resource", e));
        }

        String responseStr = new String(response);
      
        // parse the returned JSON to construct an authoritative resource to
        // send back to the client
        final JSONParser jsonParser = new JSONParser();
        try {
            Object parsedObject = jsonParser.parse(responseStr);
            JSONArray jsonArray = (JSONArray) parsedObject;
            if (parsedObject instanceof JSONArray) {

                  for (Object jsonObj : jsonArray) {
                    AuthoritativeResourceRecord resource = convertAuthoritativeResource(jsonObj);
                    resourceList.add(resource);
                 }
                  
                return new SuccessfulResponse<>(resourceList);

            } else {
                 return new FailureResponse<>(new DetailedException(
                         "Unable to retrive authoritative resource", 
                         "The parsed authoritative resource is not in the correct format", 
                         null));
            }
        } catch (ParseException e) {
             return new FailureResponse<>(new DetailedException(
                     "Unable to retrive authoritative resource", 
                     "An error occurred while attempting to parse the obtained authoritative resource", 
                     e));
        }
    }
    
    @Override
    public GenericRpcResponse<List<AuthoritativeResourceRecord>> getAuthoritativeResources(List<String> ids) {
    	
    	List<AuthoritativeResourceRecord> resources = new ArrayList<>();
    	for(String id: ids){
    	    try{
    	        resources.add(requestAuthoritativeResource(id));
    	    } catch (Exception e) {
				 return new FailureResponse<>(new DetailedException(
		                    "Unable to retrive authoritative resource", 
		                    "An error occurred while attempting to locate the authoritative resource", 
		                    e));
			      }
    	    }
               
    	    return new SuccessfulResponse<>(resources);
       }
    
    /**
     * Requests the authoritative resource with the given ID from the authoritative resource system and converts the response to an {@link AuthoritativeResourceRecord}
     *
     * @param id the ID of the authoritative resource that is being requested. Cannot be null.
     * @return an {@AuthoritativeResourceRecord} describing the authoritative resource
     */
    public AuthoritativeResourceRecord requestAuthoritativeResource(String id) throws Exception {
    	
    	if(StringUtils.isBlank(id)) {
    		throw new IllegalArgumentException("ID of the requested resource cannot be null or empty");
    	}
    	
    	  //assume that ID is a rest URL and request it
        RESTClient restClient = new RESTClient();
        byte[] response = null;
        try {
            response = restClient.get(new URL(id));
            
        } catch (@SuppressWarnings("unused") MalformedURLException e) {
//            return new FailureResponse<>(new DetailedException(
//                    "Unable to retrive authoritative resource", 
//                    "The ID of the authoritative resource is malformed", 
//                    e));
            //TODO: Remove once relationships between competencies are handled
            return new AuthoritativeResourceRecord(id, id, "Description", "Competency", null);
        } 
        String responseStr = new String(response);
        
        //parse the returned JSON to construct an authoritative resource to send back to the client
        final JSONParser jsonParser = new JSONParser();
        
            Object parsedObject = jsonParser.parse(responseStr);
            if(parsedObject instanceof JSONObject) {
            	
                AuthoritativeResourceRecord resource = convertAuthoritativeResource(parsedObject);
                return resource;
                
            } 
            
            else { 
            	
            	throw new MalformedURLException("Unable to retrive authoritative resource. The parsed authoritative resource is not in the correct format");
            }
      }

    @Override
    public GenericRpcResponse<ExternalScenarioImportResult> createCourseForScenario(String scenarioId) {
        
        try {
            if(StringUtils.isBlank(ServicesProperties.getInstance().getPropertyValue(ServicesProperties.XTSP_REST_ENDPOINT_ADDRESS))){
                return new FailureResponse<>(new DetailedException(
                        "No REST endpoint has been specified to request a scenario from outside GIFT. "
                        + "Check the REST endpoint configuration in GIFT/config/tools/services/services.properties", 
                        "Unable to perform REST request because the configured endpoint address is empty", 
                        null
                ));
            }
            
            String importUrl = ServicesProperties.getInstance().getPropertyValue(ServicesProperties.XTSP_REST_ENDPOINT_ADDRESS) 
                    + ServicesProperties.getInstance().getPropertyValue(ServicesProperties.IMPORT_XTSP_REST_FUNCTION);
            
            importUrl = importUrl + "?scenarioId=" + scenarioId;
            
            RESTClient restClient = new RESTClient();
            
            byte[] response = restClient.get(new URL(importUrl));
            if(response != null) {
                
                XTSPImporter importer = new XTSPImporter();
                Scenario scenario = new Scenario();
                scenario.setName("UNNAMED SCENARIO");
                scenario.setMission(new Mission());
                
                /* Rely on SSO authentication to sign in rather than redirecting login page */
                UserAuthResult authResult = UserAuthenticationMgr.getInstance().isValidUser(null, null, null, getThreadLocalRequest());
                if(authResult.getAuthFailedReason() != null) {
                    
                    return new FailureResponse<>(new DetailedException(
                            "Failed to create a course from external scenario " + scenarioId + ". A problem occurred while logging in.", 
                            authResult.getAuthFailedReason(), 
                            null));
                }
                
                String username = authResult.getAuthUsername();
                
                //retrieve the user that will be used to query for courses, learner history, etc.
                DbUser dbUser = UMSDatabaseManager.getInstance().getUserByUsername(username, true);
                
                LoginRequest request = new LoginRequest(dbUser.getUserId());
                request.setUsername(dbUser.getUsername());
                UMSDatabaseManager.getInstance().loginUser(request, DashboardProperties.getInstance().getDeploymentMode());

                boolean loginSuccess = ServicesManager.getInstance().getUserServices().loginUser(dbUser.getUsername(), true);
                if(!loginSuccess) {
                    return new FailureResponse<>(new DetailedException(
                            "Failed to create a course from external scenario " + scenarioId + ". A problem occurred while logging in.", 
                            "This user's credentials passed the login authentication but a workspace could not be created for them", 
                            null));
                }
                
                ByteArrayInputStream stream = new ByteArrayInputStream(response);
                Set<String> allowedCoordinates = new HashSet<>();
                allowedCoordinates.add("GDC");
                allowedCoordinates.add("GCC");
                importer.importXtspIntoDkf(username, scenario, stream, allowedCoordinates);
                
                if(scenario.getResources() == null) {
                    scenario.setResources(new Resources());
                }
                
                /* Save the scenario ID to the DKF's source path so we know where it came from */
                scenario.getResources().setSourcePath(scenarioId);
                
                //We have to update the version number every time we save the file. I
                //would have done this on the client side but the "common" code that
                //handles the version logic isn't accessible on the client side.
                String dkfCurrentVersion = scenario.getVersion();
                String dkfSchemaVersion = CommonUtil.getSchemaVersion(AbstractSchemaHandler.DKF_SCHEMA_FILE);
                String dkfNewVersion = CommonUtil.generateVersionAttribute(dkfCurrentVersion, dkfSchemaVersion);
                scenario.setVersion(dkfNewVersion);
                
                AbstractSchemaHandler.writeToFile(scenario, System.out, FileType.DKF, true);
               
                String dkfFileName = "scenario.dkf.xml";
                
                /* Sanitize the scenario name by parsing out any file separators. This should ensure
                 * that these names are safe to use for the corresponding files in the workspace
                 * folder. Other special characters like parentheses and quotes will cause exceptions
                 * when they are written and will, therefore, already be caught */
                String safeScenarioName = scenario.getName();
                if(safeScenarioName.contains("/")) {
                    safeScenarioName = safeScenarioName.substring(safeScenarioName.indexOf("/") + 1);
                } else if(safeScenarioName.contains("\\")) {
                    safeScenarioName = safeScenarioName.substring(safeScenarioName.indexOf("\\") + 1);
                }
                
                FileTreeModel treeModel = ServicesManager.getInstance().getFileServices().getUsersWorkspace(username);
                FileTreeModel courseFile = treeModel.getModelFromRelativePath(safeScenarioName + "/" + scenario.getName().trim() + AbstractSchemaHandler.COURSE_FILE_EXTENSION);
                FileTreeModel dkfFile = treeModel.getModelFromRelativePath(safeScenarioName + "/" + dkfFileName);
                
                /* TODO: For now, assume incoming XTSP is always for RIDE, at least until we can
                 * have XDT somehow tell us the application type */
                RIDEInteropInputs taInputs = new RIDEInteropInputs();
                InteropInputs inputs = new InteropInputs();
                inputs.setInteropInput(taInputs);
                Interop interop = new Interop();
                interop.setInteropImpl(TrainingAppUtil.RIDE_PLUGIN_INTERFACE);
                interop.setInteropInputs(inputs);
                Interops interops = new Interops();
                interops.getInterop().add(interop);
                
                TrainingApplication trainingApp = new TrainingApplication();
                trainingApp.setTransitionName(scenario.getName().trim());
                DkfRef dkfRef = new DkfRef();
                dkfRef.setFile(dkfFileName);
                trainingApp.setDkfRef(dkfRef);
                trainingApp.setInterops(interops); 
                
                Course course = new Course();
                
                generated.course.Concepts concepts = new generated.course.Concepts();
                
                generated.course.Concepts.Hierarchy conceptHierarchy = new generated.course.Concepts.Hierarchy();
                ConceptNode rootConcept = new ConceptNode();
                rootConcept.setName("all concepts");
                conceptHierarchy.setConceptNode(rootConcept);
                concepts.setListOrHierarchy(conceptHierarchy);
                
                course.setConcepts(concepts);
                
                /* Add the tasks from the DKFs to the course concepts */
                for(generated.dkf.Task importedTask : scenario.getAssessment().getTasks().getTask()) {
                    
                    ConceptNode importedConceptNode = new ConceptNode();
                    importedConceptNode.setName(importedTask.getName());
                    rootConcept.getConceptNode().add(importedConceptNode);   
                    
                    for(generated.dkf.Concept importedConcept : importedTask.getConcepts().getConcept()) {
                        importAsCourseConcept(importedConcept, importedConceptNode);
                    }
                }
                
                /* Clean up duplicate concepts and correct capitalization if needed */
                CourseConceptsUtil.cleanCourseConcepts(course);
                
                //We have to update the version number every time we save the file. I
                //would have done this on the client side but the "common" code that
                //handles the version logic isn't accessible on the client side.
                String currentVersion = course.getVersion();
                String schemaVersion = CommonUtil.getSchemaVersion(AbstractSchemaHandler.COURSE_SCHEMA_FILE);
                String newVersion = CommonUtil.generateVersionAttribute(currentVersion, schemaVersion);
                course.setVersion(newVersion);
                
                course.setName(scenario.getName().trim());
                course.setTransitions(new Transitions());
                course.getTransitions().getTransitionType().add(trainingApp);
                
                if(!fileServices.fileExists(username, dkfFile.getParentTreeModel().getRelativePathFromRoot(), true)){
                    
                    String userFolder = courseFile.getParentTreeModel().getParentTreeModel().getRelativePathFromRoot();
                    String courseFolderName = courseFile.getParentTreeModel().getFileOrDirectoryName();
                    fileServices.createFolder(username, userFolder, courseFolderName, true);
                }
                
                /* Need to save the course first, since without it, the file services 
                 * layer doesn't properly acknowledge this as a course folder */
                fileServices.marshalToFile(username, course, courseFile.getRelativePathFromRoot(), null);

                /* Then, save the DKF after the course */
                fileServices.marshalToFile(username, scenario, dkfFile.getRelativePathFromRoot(), null);
                
                return new SuccessfulResponse<>(new ExternalScenarioImportResult(courseFile.getRelativePathFromRoot(false), username));
            }
            
        } catch (Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "GIFT was unable to export an external scenario", 
                    "An error occured while converting the exported scenario into a course", 
                    e));
        }
        
        return null;
    }
    
    /**
     * Imports the given DKF concept as a course concept by converting it into a Course ConceptNode 
     * and adding it as a child to the given parent concept node
     * 
     * @param concept the DKF concept to import. If null, this method will do nothing.
     * @param parentConceptNode the parent Course concept node to import to. If null, this method will do nothing.
     */
    private void importAsCourseConcept(generated.dkf.Concept concept, ConceptNode parentConceptNode) {
        
        if(parentConceptNode == null || concept == null) {
            return;
        }
        
        ConceptNode importedConceptNode = new ConceptNode();
        importedConceptNode.setName(concept.getName());
        
        if(concept.getConditionsOrConcepts() instanceof generated.dkf.Concepts) {
            
            /* The DKF concept has sub-concepts, so they need to be imported too */
            for(generated.dkf.Concept childConcept : 
                ((generated.dkf.Concepts) concept.getConditionsOrConcepts()).getConcept()) {
                
                importAsCourseConcept(childConcept, importedConceptNode);
            }
        }
        
        parentConceptNode.getConceptNode().add(importedConceptNode);
    }
}