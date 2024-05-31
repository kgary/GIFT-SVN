/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import static mil.arl.gift.common.util.StringUtils.isBlank;
import static mil.arl.gift.common.util.StringUtils.join;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import generated.course.Course;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.DeploymentModeException;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.DomainOption.DomainOptionRecommendation;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.LMSDataRequest;
import mil.arl.gift.common.LoginRequest;
import mil.arl.gift.common.aar.LogIndexService;
import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.aar.ScoreNodeUpdate;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.SessionScenarioInfo;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.DomainOptionRecommendationEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.experiment.CourseCollection;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.DataCollectionItem.DataCollectionPermission;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.gwt.client.ErrorDetails;
import mil.arl.gift.common.gwt.client.FailureResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.SuccessfulResponse;
import mil.arl.gift.common.gwt.server.AbstractWebSessionData;
import mil.arl.gift.common.gwt.server.AsyncReturnBlocker;
import mil.arl.gift.common.gwt.server.BrowserWebSession;
import mil.arl.gift.common.gwt.server.GiftServletUtils;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationException;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr.UserAuthResult;
import mil.arl.gift.common.gwt.server.authentication.WhiteListUserAuth;
import mil.arl.gift.common.gwt.shared.CourseValidationParams;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.gwt.shared.GetActiveKnowledgeSessionsResponse;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AsyncOperationManager;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.CourseListFilter;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.ExperimentUrlManager;
import mil.arl.gift.common.io.FileExistsException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.ImageProperties;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.common.lti.TrustedLtiConsumer;
import mil.arl.gift.common.metrics.MetricsSender;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.ta.util.ExternalMonitorConfig;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.server.webmonitor.WebMonitorServiceManager;
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
import mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection.AddCourseAction;
import mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection.EditCollectionProperties;
import mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection.ReorderAction;
import mil.arl.gift.tools.map.shared.SIDC;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.ServicesProperties;
import mil.arl.gift.tools.services.db.DbServicesInterface;
import mil.arl.gift.tools.services.experiment.DataCollectionServicesInterface;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.AbstractFileServices.NameCollisionResolutionBehavior;
import mil.arl.gift.tools.services.file.ExportManager;
import mil.arl.gift.tools.services.file.ImportManager;
import mil.arl.gift.ums.db.HibernateObjectReverter;
import mil.arl.gift.ums.db.UMSDatabaseException;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.survey.DeleteSurveyContextResponse;
import mil.arl.gift.ums.db.survey.SurveyValidationException;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.survey.Surveys.ExternalSurveyMapper;
import mil.arl.gift.ums.db.table.DbCourseCollection;
import mil.arl.gift.ums.db.table.DbCourseCollectionEntry;
import mil.arl.gift.ums.db.table.DbUser;

/**
 * The server-side implementation of the RPC service.
 * @author nblomberg
 *
 */
@SuppressWarnings("serial")
public class DashboardServiceImpl extends RemoteServiceServlet implements
        DashboardService, HttpSessionListener {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    /** Date formatter used for the error timestamp.
     * NOTE: null Locale and Timezone arguments make the formatter use the system defaults. */
    private static final FastDateFormat fdf =  FastDateFormat.getInstance("MM-dd-yyyy HH:mm:ss z", null, null);

    private static final String COURSE_LIST_PROGRESS_DESC = "Retrieving courses...";

    /** capture important properties that the dashboard client may need */
    private static final ServerProperties SERVER_PROPERTIES = new ServerProperties();
    static{
        SERVER_PROPERTIES.addProperty(ServerProperties.READ_ONLY_USER,
                DashboardProperties.getInstance().getReadOnlyUser());
        SERVER_PROPERTIES.addProperty(ServerProperties.DEPLOYMENT_MODE,
                DashboardProperties.getInstance().getDeploymentMode().getName());
        SERVER_PROPERTIES.addProperty(ServerProperties.TUI_URL,
                DashboardProperties.getInstance().getTutorURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.GAT_URL,
                DashboardProperties.getInstance().getGiftAuthorToolURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.ASAT_URL,
                DashboardProperties.getInstance().getASATURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.AUTHORITATIVE_SYSTEM_URL,
        		DashboardProperties.getInstance().getAuthoritativeSystemUrl());
        SERVER_PROPERTIES.addProperty(ServerProperties.TRADEM_URL,
                DashboardProperties.getInstance().getTRADEMURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.WINDOW_TITLE,
                DashboardProperties.getInstance().getWindowTitle());
        SERVER_PROPERTIES.addProperty(ServerProperties.DASHBOARD_WS_URL,
                DashboardProperties.getInstance().getWebSocketUrl());
        SERVER_PROPERTIES.addProperty(ServerProperties.VERSION_NAME,
                Version.getInstance().getName());
        SERVER_PROPERTIES.addProperty(ServerProperties.VERSION_DATE,
                Version.getInstance().getReleaseDate());
        SERVER_PROPERTIES.addProperty(ServerProperties.BUILD_DATE,
                Version.getInstance().getBuildDate());
        SERVER_PROPERTIES.addProperty(ServerProperties.BUILD_LOCATION,
                Version.getInstance().getBuildLocation());
        SERVER_PROPERTIES.addProperty(ServerProperties.DOCUMENTATION_TOKEN,
                Version.getInstance().getDocumentationToken());
        SERVER_PROPERTIES.addProperty(ServerProperties.CMS_URL, DashboardProperties.getInstance().getCMSURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.TUTOR_COURSE_START_TIMEOUT,
                Integer.valueOf(DashboardProperties.getInstance().getTutorCourseStartTimeout()).toString());
        SERVER_PROPERTIES.addProperty(ServerProperties.ENABLE_CLIENT_ANALYTICS, String.valueOf(DashboardProperties.getInstance().isClientAnalyticsEnabled()));
        SERVER_PROPERTIES.addProperty(ServerProperties.CLIENT_ANALYTICS_URL, DashboardProperties.getInstance().getClientAnalyticsUrl());
        SERVER_PROPERTIES.addProperty(ServerProperties.TRUSTED_LTI_CONSUMERS,
                DashboardProperties.getInstance().getPropertyValue(DashboardProperties.TRUSTED_LTI_CONSUMERS));
        SERVER_PROPERTIES.addProperty(ServerProperties.TRUSTED_LTI_PROVIDERS,
                DashboardProperties.getInstance().getPropertyValue(DashboardProperties.TRUSTED_LTI_PROVIDERS));
        SERVER_PROPERTIES.addProperty(ServerProperties.LTI_URL, DashboardProperties.getInstance().getLtiServletUrl());
        SERVER_PROPERTIES.addProperty(ServerProperties.LTI_CONSUMER_URL, DashboardProperties.getInstance().getLtiConsumerServletUrl());
        SERVER_PROPERTIES.addProperty(ServerProperties.LTI_HELP_PAGE_URL, HelpDocuments.getLtiDoc().toString());
        SERVER_PROPERTIES.addProperty(ServerProperties.PROHIBITED_NAMES,
                join(ServerProperties.PROHIBITED_NAMES_DELIM, DashboardProperties.getProhibitedNames()));
        SERVER_PROPERTIES.addProperty(ServerProperties.USE_HTTPS,
                Boolean.toString(DashboardProperties.getInstance().shouldUseHttps()));
        SERVER_PROPERTIES.addProperty(ServerProperties.GOOGLE_MAPS_API_KEY,
                DashboardProperties.getInstance().getPropertyValue(DashboardProperties.GOOGLE_MAPS_API_KEY));
        SERVER_PROPERTIES.addProperty(ServerProperties.ASSESSMENT_NOTIFICATION_DELAY_MS,
                Integer.valueOf(DashboardProperties.getInstance().getAssessmentNotificationDelayMs()).toString());
        SERVER_PROPERTIES.addProperty(ServerProperties.DOMAIN_CONTENT_SERVER_ADDRESS, 
                DomainModuleProperties.getInstance().getDomainContentServerAddress());
        SERVER_PROPERTIES.addProperty(ServerProperties.BYPASS_SURVEY_PERMISSION_CHECK, 
                Boolean.toString(DashboardProperties.getInstance().getByPassSurveyPermissionCheck()));
        SERVER_PROPERTIES.addProperty(ServerProperties.DASHBOARD_URL, DashboardProperties.getInstance().getDashboardURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.DEFAULT_MBP_RECALL_ALLOWED_ATTEMPTS, 
                Integer.toString(MerrillsBranchPointHandler.DEFAULT_RECALL_BAILOUT_CNT));
        SERVER_PROPERTIES.addProperty(ServerProperties.DEFAULT_MBP_PRACTICE_ALLOWED_ATTEMPTS, 
                Integer.toString(MerrillsBranchPointHandler.DEFAULT_PRACTICE_BAILOUT_CNT));
        SERVER_PROPERTIES.addProperty(ServerProperties.DEFAULT_CHARACTER, DashboardProperties.getInstance().getDefaultCharacterPath());
        SERVER_PROPERTIES.addProperty(ServerProperties.MILITARY_SYMBOL_SERVICE_URL, 
                DashboardProperties.getInstance().getMilitarySymbolServiceURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.LESSON_LEVEL, 
                DashboardProperties.getInstance().getLessonLevel().getName());
        SERVER_PROPERTIES.addProperty(ServerProperties.COURSE_LIST_FILTER, 
                DashboardProperties.getInstance().getDefaultCourseListFilter());
        SERVER_PROPERTIES.addProperty(ServerProperties.SESSION_OUTPUT_SERVER_PATH, 
                DashboardProperties.getInstance().getSessionOutputServerPath());
        SERVER_PROPERTIES.addProperty(ServerProperties.MESSAGE_DISPLAY_BUFFER_SIZE, 
                Integer.toString(DashboardProperties.getInstance().getMessageDisplayBufferSize()));
        SERVER_PROPERTIES.addProperty(ServerProperties.EXTERNAL_STRATEGY_PROVIDER_URL, 
                DashboardProperties.getInstance().getExternalStrategyProviderUrl());
        
        // IMAGES
        SERVER_PROPERTIES.addProperty(ServerProperties.BACKGROUND_IMAGE, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.BACKGROUND));
        SERVER_PROPERTIES.addProperty(ServerProperties.ORGANIZATION_IMAGE, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.ORGANIZATION_IMAGE));
        SERVER_PROPERTIES.addProperty(ServerProperties.SYSTEM_ICON_SMALL, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.SYSTEM_ICON_SMALL));
        SERVER_PROPERTIES.addProperty(ServerProperties.LOGO, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.LOGO));

    }
    
    /** A mapping from each username to the progress of their currently running course permissions update process, if one exists */
    private ConcurrentHashMap<String, ProgressIndicator> usernameToCoursePermUpdateProgress = new ConcurrentHashMap<>();

    /** A mapping from each username to the progress of their currently running delete process, if one exists */
    private ConcurrentHashMap<String, ProgressIndicator> usernameToDeleteProgress = new ConcurrentHashMap<>();

    /** A mapping from each username to the progress of their currently running copy process, if one exists */
    private ConcurrentHashMap<String, LoadedProgressIndicator<CopyCourseResult>> usernameToCopyProgress = new ConcurrentHashMap<>();

    /**
     * A mapping from username to the progress of a currently running validate course process, if one exists
     * Note: entries are not removed but rather over-written as a user requests another course to be validated.  This is
     * to prevent any issues from removing the progress object while the client is still requesting the latest progress value.
     */
    private ConcurrentHashMap<String, ProgressIndicator> usernameToValidateProgress = new ConcurrentHashMap<>();

    /**
     * A mapping of browser session id to the progress of a currently running request to fetch the domain session logs
     * on the server that can be used for playback on the game master.
     */
    private ConcurrentHashMap<String, LoadedProgressIndicator<Collection<LogMetadata>>> browserSessionToFetchPlaybackLogsProgress = new ConcurrentHashMap<>();

    /**
     * mapping of browser session id to the progress of a currently running request to update the log index file
     * on the server.
     */
    private ConcurrentHashMap<String, LoadedProgressIndicator<Void>> browserSessionToUpdateLogProgress = new ConcurrentHashMap<>();

    /**
     * A mapping of username to the progress of a currently running get course list process, if one exists
     */
    private ConcurrentHashMap<String, ProgressIndicator> usernameToGetCourseListProgress = new ConcurrentHashMap<>();

    /** The metrics sender is responsible for sending metrics of the dashboard rpcs to the metrics server */
    private MetricsSender metrics = new MetricsSender("dashboard");

    /** The name of the http request header that will contain the value of the token.  This header is ONLY examined
     * if GIFT is configured to be in stress mode.  The header value should contain the 'token' that matches
     * the 'StressToken' value in the common.properites file.  This token is used for security purposes to restrict
     * access to the stress login to authorized stress accounts.
     */
    private static final String STRESS_HEADER_NAME = "StressToken";

    /** authentication the user */
    private static UserAuthenticationMgr authentication = UserAuthenticationMgr.getInstance();

    /** Timer which is called to update the metrics sender with the current session value */
    private static Timer updateValidationCache = new Timer("updateValidationCache");

    /** Instance of the user session manager */
    private UserSessionManager userSessionManager = null;

    /** Instance of the knowledge session message manager. */
    private KnowledgeSessionMessageManager ksmManager = null;
    
    /** The manager that manages the web monitor services that are registered to browser sessions */
    private WebMonitorServiceManager monitorManager = null;

    /**
     * Class that encapsulates necessary information for validation result caching.
     */
    static class CourseValidationEntry {
        final boolean validated;
        final DomainOption course;
        final long lastValidated;

        /**
         * set attributes
         *
         * @param validated true if the course was successfully validated
         * @param course information about the course that was validated
         * @param lastValidated epoch when the last successful validation occurred
         */
        public CourseValidationEntry(boolean validated, DomainOption course, long lastValidated) {
            this.validated = validated;

            if(course == null){
                throw new IllegalArgumentException("The course can't be null.");
            }
            this.course = course;

            this.lastValidated = lastValidated;
        }
    }

    /**
     * A simple representation of a course in a user's domain used to correct problematic course paths
     *
     * @author bzahid
     */
    private static class CourseObject {

        /** The current path to the course */
        private String currentPath;

        /** The original course name prior to being renamed */
        private final String originalName;

        /** A user friendly path to the original course (ie "CourseFolderName/CourseName") */
        private final String originalPathStr;

        /** The username of the user invoking the course path correction */
        private final String username;

        /** The corrected path that the course should use. This will be "username/targetName/targetName.course.xml" */
        private String targetPath;

        /** The corrected course name that should be used the course folder, course.xml file, and course name attribute */
        private String targetName;

        /** Whether or not this course has been successfully renamed */
        private boolean isCorrect = false;

        /**
         * Creates a new object representing a course whose path needs to be corrected for the given user.
         * A path is incorrect if it is not in the format "username/CourseName/CourseName.course.xml" where "Name"
         * matches the name element in the the course.xml file
         *
         * @param option a course in the user's workspace whose path needs to be corrected. Cannot be null.
         * @param username the username of the user for whom the course is being corrected. Cannot be null.
         */
        public CourseObject(DomainOption option, String username) {

            if(option == null) {
                throw new IllegalArgumentException("Cannot construct a CourseObject. The DomainOption cannot be null.");
            }

            if(username == null) {
                throw new IllegalArgumentException("Cannot construct a CourseObject. The username cannot be null.");
            }

            currentPath = option.getDomainId();
            targetName = option.getDomainName().trim();
            originalName = option.getDomainName();
            this.username = username;

            // Create a user friendly string to report the original path of the course as "CourseFolder/CourseName"

            int folderNameStart = currentPath.indexOf(Constants.FORWARD_SLASH) + 1;
            int folderNameEnd = currentPath.lastIndexOf(Constants.FORWARD_SLASH) + 1;

            if(currentPath.startsWith(Constants.FORWARD_SLASH)) {

                // The domainId begins with a forward slash in server mode, so skip it
                folderNameStart = currentPath.substring(1).indexOf(Constants.FORWARD_SLASH) + 1;
            }

            originalPathStr = currentPath.substring(folderNameStart, folderNameEnd) + originalName;

            if(!currentPath.startsWith(username) && !currentPath.startsWith(Constants.FORWARD_SLASH + username)) {
                throw new IllegalArgumentException("Cannot construct a CourseObject because the course does not belong to the user's workspace");
            }

            updateNameAndPath(option.getDomainName());
        }

        /**
         * Sets the target name and path for this course based on the given name. The target path
         * will become "username/newName/newName.course.xml"
         *
         * @param newName the new name to give the course and use in the target path
         */
        public void updateNameAndPath(String newName) {

            this.targetName = newName.trim();
            if(currentPath.startsWith(Constants.FORWARD_SLASH)) {
                targetPath = Constants.FORWARD_SLASH + username;
            } else {
                targetPath = username;
            }

            targetPath += Constants.FORWARD_SLASH + this.targetName + Constants.FORWARD_SLASH + this.targetName + AbstractSchemaHandler.COURSE_FILE_EXTENSION;
        }

        /**
         * Returns whether or not this course's path is already in the format of "username/CourseName/CourseName.course.xml"
         * where "CourseName" is the name element in the course.xml file.
         *
         * @return whether or not this course's path is already in the correct format.
         */
        public boolean isCorrect() {
            return isCorrect;
        }

        /**
         * Updates this course object's current path to the target path. This method should be called when
         * the course path has been updated to the correct format (i.e. "username/CourseName/CourseName.course.xml"
         * where "CourseName" is the name element in the course.xml file)
         */
        public void setCorrect() {
            currentPath = targetPath;
            isCorrect = true;
        }

        /**
         * Gets this course's current path in the domain.
         *
         * @return the current path to the course. This will look something like "username/CourseFolderName/CourseName.course.xml"
         */
        public String getCurrentPath() {
            return currentPath;
        }

        /**
         * Gets the original course path prior to being renamed.
         *
         * @return the original path to the course. This will look something like "username/CourseFolderName/CourseName.course.xml"
         */
        public String getOriginalName() {
            return originalName;
        }

        /**
         * Gets a user friendly path to the original course (ie. "CourseFolderName/CourseName").
         * This should only ever be used for reporting changes made to the user's courses.
         *
         * @return a user friendly path to the original course.
         */
        public String getOriginalPathStr() {
            return originalPathStr;
        }

        /**
         * Gets the correct path that should be used for this course.
         *
         * @return the target path. This will look like "username/CourseName/CourseName.course.xml"
         */
        public String getTargetPath() {
            return targetPath;
        }

        /**
         * Gets a user friendly path to the course (ie. "CourseFolderName/CourseName").
         * This should only ever be used for reporting changes made to the user's courses.
         *
         * @return a user friendly path to the course.
         */
        public String getTargetPathStr() {
            return targetPath.substring(
                    targetPath.indexOf(Constants.FORWARD_SLASH) + 1,
                    targetPath.lastIndexOf(AbstractSchemaHandler.COURSE_FILE_EXTENSION));
        }

        /**
         * Gets the target name that should be used for the course name attribute in the xml file as well as the
         * course.xml file name and the course folder name.
         *
         * @return the target name
         */
        public String getTargetName() {
            return targetName;
        }

        /**
         * Gets the username of the user for whom this course is being corrected
         *
         * @return the username
         */
        public String getUsername() {
            return username;
        }

        @Override
        public String toString() {

            StringBuffer sb = new StringBuffer();
            sb.append("[CourseObject: ");
            sb.append("currentPath = ").append(currentPath);
            sb.append(", targetPath = ").append(targetPath);
            sb.append(", targetName = ").append(targetName);
            sb.append(", originalName = ").append(originalName);
            sb.append(", originalPathStr = ").append(originalPathStr);
            sb.append(", username = ").append(username);
            sb.append("]");

            return sb.toString();
        }
    }

    /** Map caching recent validation results. */
    private final ConcurrentHashMap<String, CourseValidationEntry> validationCache = new ConcurrentHashMap<>();

    /**
     * The interval between purges of the course validation cache. Although the course validation result is
     * invalidated after a period of time, it may still be in the cache and should be periodically purged.
     * It does not need to be done too often.
     */
    private final int COURSE_VALIDATION_PURGE_INTERVAL = 30*60*1000;

    /** Length of time in milliseconds that a course validation result is valid. */
    private final int courseValidationCacheExpiration = DashboardProperties.getInstance().getValidationCacheExpiration()*1000;

    /**
     * Length of time a course that failed validation will be considered valid in the cache.
     * After this amount of time the invalid course will be revalidated.
     */
    private final int invalidCourseCacheExpiration = DashboardProperties.getInstance().getInvalidCourseCacheExpiration()*1000;

    /** Mapping of trusted consumers (based on the consumer key) which is read in from the configuration file at launch. */
    private HashMap<String, TrustedLtiConsumer> consumerMap;

    @Override
    public void init() throws ServletException {

        AsyncOperationManager.getInstance();
        // Read in the trusted lti consumer map from the configuration file.
        consumerMap = DashboardProperties.getInstance().getTrustedLtiConsumers();

        if (consumerMap == null || consumerMap.isEmpty()) {
            logger.error("The Trusted LTI Consumer Map could not be parsed properly from the commons.properties file.  The servlet will not process any incoming LTI requests.");
        }

        super.init();

        // Create an instance of the user session manager.
        UserSessionManager.createInstance();
        userSessionManager = UserSessionManager.getInstance();
        
        KnowledgeSessionMessageManager.createInstance(userSessionManager);
        ksmManager = KnowledgeSessionMessageManager.getInstance();
        
        // Start up the web monitor module
        WebMonitorModule.createInstance(ksmManager);
        
        WebMonitorServiceManager.createInstance(userSessionManager);
        monitorManager = WebMonitorServiceManager.getInstance();

        //add metrics reporting to the data collection manager
        DataCollectionManager.getInstance().setMetricsSender(metrics);

        // Start sending metrics.
        metrics.startSending();

        // reusing the same Timer for now
        updateValidationCache.schedule(new TimerTask() {

            @Override
            public void run() {
                Entry<String, CourseValidationEntry> entry;
                Iterator<Entry<String, CourseValidationEntry>> iter = validationCache.entrySet().iterator();
                long now = System.currentTimeMillis();
                while (iter.hasNext()) {
                    entry = iter.next();
                    if (now - entry.getValue().lastValidated > courseValidationCacheExpiration) {
                        iter.remove();
                    }
                }
            }

        }, COURSE_VALIDATION_PURGE_INTERVAL, COURSE_VALIDATION_PURGE_INTERVAL);

        // auto populates the list of offline users
        autoPopulateOfflineUsers();


    }

    /**
     * Auto populates the UMS database with a list of offline users.
     * This is only valid in desktop mode.  The list of offline users
     * can be empty if the feature is not used.  In offline mode, this list of
     * offline users becomes available in the dashboard offline login widget.
     */
    private void autoPopulateOfflineUsers() {

        DeploymentModeEnum deploymentMode = DashboardProperties.getInstance().getDeploymentMode();
        // only do in Desktop mode -- NOT server mode
        if (deploymentMode == DeploymentModeEnum.DESKTOP) {

            // Create the class to load the offline user list.
            OfflineUsers offlineUsers = new OfflineUsers();
            // Get the list of offline users (if any).
            List<String> usernames = offlineUsers.loadOfflineUsers();
            for (String username : usernames) {
                try {
                    // This creates the user in the UMS db if it doesn't exist as well as
                    // create the workspace folder locally.
                    if(logger.isDebugEnabled()){
                        logger.debug("Creating offline user '"+username+"'.");
                    }
                    ServicesManager.getInstance().getUserServices().loginUser(username, true);
                } catch (DeploymentModeException e) {
                    logger.error("DeploymentModeException caught while trying to login an offline user: " + username +".", e );
                } catch (UMSDatabaseException e) {
                    logger.error("UMSDatabaseException caught while trying to login an offline user: " + username +".", e );
                } catch (ProhibitedUserException e) {
                    logger.error("ProhibitedUserException caught while trying to login an offline user: " + username +".", e );
                } catch (IOException e) {
                    logger.error("IOException caught while trying to login an offline user: " + username +".", e );
                } catch (Exception e) {
                    logger.error("Exception caught while trying to login an offline user: " + username +".", e );
                }
            }


        }
    }

    @Override
    public void destroy() {

        // Stop sending metrics.
        metrics.stopSending();
        super.destroy();

    }

    @Override
    public ServerProperties getServerProperties(){
        
        long start = System.currentTimeMillis();
        // update the dashboard properties.
        DashboardProperties.getInstance().refresh();
        
        ServletRequest request = getThreadLocalRequest();

        SERVER_PROPERTIES.addProperty(ServerProperties.LANDINGPAGE_MESSAGE, DashboardProperties.getInstance().getLandingPageMessage());
        SERVER_PROPERTIES.addProperty(ServerProperties.USE_CLOUDLOGINPAGE, DashboardProperties.getInstance().getUseCloudLoginPage());
        SERVER_PROPERTIES.addProperty(ServerProperties.RESTRICTED_USER_ACCESS, String.valueOf(WhiteListUserAuth.getInstance().isEnabled(request)));
        SERVER_PROPERTIES.addProperty(ServerProperties.JRE_BIT,
                Boolean.toString(DashboardProperties.getInstance().isJRE64Bit()));
        
        if(authentication.isSSOSupported(request)) {
            SERVER_PROPERTIES.addProperty(ServerProperties.USE_SSO_LOGIN, String.valueOf(true));
        }
        
        metrics.endTrackingRpc("getServerProperties", start);
        return SERVER_PROPERTIES;
    }

    @Override
    public LoginResponse loginUser(final String username, final String password, final String loginAsUserName) throws IllegalArgumentException {

        long start = System.currentTimeMillis();

        String encryptedPassword = null;
        if(StringUtils.isNotBlank(password)) {
            
            /* Do encryption here if not using SSO. Otherwise, SSO should handle encryption. */
            encryptedPassword = encryptPassword(password);
        }

        //the address of the client were a use is trying to login to GIFT
        final String clientAddress = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());

        if(logger.isTraceEnabled()){
            logger.trace("User login attempted with: " + username );
        }

        //
        // Authenticate credentials before continuing
        //
        String failedReason = null;
        String resolvedUsername = username;
        boolean isAutoDebug = false;
        try{

            boolean stressMode = DashboardProperties.getInstance().isStressModeEnabled();
            String headerVal = this.getThreadLocalRequest().getHeader(STRESS_HEADER_NAME);
            String stressToken = DashboardProperties.getInstance().getStressToken();

            // ONLY allow stress logins if GIFT is configured to allow them and the proper header token is passed in via the request.
            if (stressMode && headerVal != null && headerVal.compareTo(stressToken) == 0) {

                // Stress logins bypass the redmine authentication.
                if(logger.isTraceEnabled()){
                    logger.trace("Stress login used with username: " + username);
                }
            } else {
                UserAuthResult authResult = authentication.isValidUser(username, password, loginAsUserName, getThreadLocalRequest());
                
                failedReason = authResult.getAuthFailedReason();
                isAutoDebug = authResult.isAutoDebug();
                
                if(authResult.getAuthUsername() != null) {
                    
                    /* Update username if auth service provided a different one */
                    resolvedUsername = authResult.getAuthUsername();
                }
            }

        } catch (UserAuthenticationException userAuthenticationException){
            //the authentication logic has provided additional information as to why they authentication failed

            logger.warn("Someone at "+clientAddress+" is trying to access "+resolvedUsername+"'s account but there was a problem: "+userAuthenticationException+"."
                    + "\nCause: " + userAuthenticationException.getCause());

            if(userAuthenticationException.getCause().getCause() instanceof UnknownHostException ||
                    userAuthenticationException.getCause().getCause() instanceof SocketException) {

                if(DashboardProperties.getInstance().isServerDeploymentMode()){
                    //don't allow offline login when in server mode because they might not have a connection to Nuxeo
                    //which would mean they can't author or run courses.
                    throw new RuntimeException("GIFT does not currently support running in "+DeploymentModeEnum.SERVER.getDisplayName() + " deployment mode while Offline.  Please check your internet connection and try again.");
                }else{
                    // If the user attempts to login without an internet connection, the cause is an UnknownHostException
                    // or SocketException, return this response so that the user can login through 'offline mode'
                    return new LoginResponse(null, null, false, userAuthenticationException.getAuthenticationProblem(), resolvedUsername, encryptedPassword, false, false);
                }
            }

        } catch (Exception e) {
            logger.error("Unhandled exception trying to login user " + resolvedUsername, e);
        }

        if(failedReason != null){

            //an unknown/un-handled authentication problem occurred.  Use the incorrect credentials excuse instead of giving direct
            //insight into the authentication protocol.
            logger.warn("Someone at "+clientAddress+" is trying to access "+resolvedUsername+"'s account but the login attempt failed because "+failedReason);
            return new LoginResponse(null, null, false, failedReason, "", "", true, false);

        }else{

            //
            // get user id in the UMS from username
            //
            try{
                //retrieve the user that will be used to query for courses, learner history, etc.
                DbUser dbUser;
                if(loginAsUserName != null){
                    logger.warn("User '"+resolvedUsername+"' is logging in as user '"+loginAsUserName+"'.");
                    dbUser = UMSDatabaseManager.getInstance().getUserByUsername(loginAsUserName, false);
                }else{
                    dbUser = UMSDatabaseManager.getInstance().getUserByUsername(resolvedUsername, true);
                }

                //
                // login using the user id obtained following the original (now 'simple') login path
                //

                try{
                    LoginRequest request = new LoginRequest(dbUser.getUserId());
                    request.setUsername(dbUser.getUsername());
                    UMSDatabaseManager.getInstance().loginUser(request, DashboardProperties.getInstance().getDeploymentMode());

                    boolean loginSuccess = ServicesManager.getInstance().getUserServices().loginUser(dbUser.getUsername(), true);

                    if (loginSuccess) {
                        if(logger.isTraceEnabled()){
                            logger.trace("Successfully logged in user: " + dbUser.getUsername());
                        }

                        DashboardHttpSessionData dashboardSession = null;

                        // Eventually we need to populate with the session information.
                        UserWebSession userWebSession = userSessionManager.getUserWebSessionByUserName(dbUser.getUsername());
                        BrowserWebSession browserSession = null;
                        if (userWebSession == null) {
                            dashboardSession = new DashboardHttpSessionData(dbUser.getUsername(), password);
                            // New user, so create a user session & browser session.
                            userWebSession = userSessionManager.createUserSession(dashboardSession, clientAddress);
                            browserSession = userSessionManager.createBrowserSession(userWebSession, clientAddress);

                        } else {
                            // Existing user session, just create a browser session.
                            dashboardSession = userWebSession.getUserSessionInfo();
                            browserSession = userSessionManager.createBrowserSession(userWebSession,  clientAddress);
                        }

                        storeScreenStateInSession(browserSession, ScreenEnum.LOGIN);

                        metrics.endTrackingRpc("loginUser", start);
                        return new LoginResponse( userWebSession.getUserSessionKey(),
                                browserSession.getBrowserSessionKey(), true, "", dbUser.getUsername(), encryptedPassword, true, isAutoDebug);

                    } else {
                        logger.error("getUserServices().loginUser() function returned false.  Login failed for user: " + dbUser.getUsername());
                        return new LoginResponse(null, null, false, "Unable to login to the user services interface.  User login was not successful.", dbUser.getUsername(), "", true, isAutoDebug);
                    }



                }catch(Throwable e){
                    logger.error("Caught exception while trying to login user '"+dbUser.getUsername()+"'.", e);

                    if(e instanceof ProhibitedUserException){
                        failedReason = e.getMessage();
                    }else{
                        failedReason = "Unable to login into GIFT's User Management System.";
                    }

                    return new LoginResponse(null, null, false, failedReason, dbUser.getUsername(), "", true, false);
                }


            }catch(Throwable e){

                String userThatFailed = resolvedUsername;
                if(loginAsUserName != null){
                    userThatFailed = loginAsUserName;
                }
                logger.error("Caught exception while trying to retrieve user id by username of '"+userThatFailed+"'.", e);

                if(e instanceof ProhibitedUserException){
                    failedReason = e.getMessage();
                }else{
                    failedReason = "Unable to retrieve the user id for '"+userThatFailed+"' in GIFT's User Management System.";
                }

                return new LoginResponse(null, null, false, failedReason, userThatFailed, "", true, false);
            }

        }//end else on Authentication

    }

    @Override
    public LoginResponse loginUserOffline(final String username) throws IllegalArgumentException {

        LoginResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("User login attempted with: " + username );
        }

        //get the ip of the thread local request.
        final String clientAddress = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());
        // Eventually we need to populate with the session information.
        DashboardHttpSessionData dashboardSession = new DashboardHttpSessionData(username, true);
        // Eventually we need to populate with the session information.
        UserWebSession userWebSession = userSessionManager.getUserWebSessionByUserName(username);
        BrowserWebSession browserSession = null;
        if (userWebSession == null) {
            // New user, so create a user session & browser session.
            userWebSession = userSessionManager.createUserSession(dashboardSession, clientAddress);
            browserSession = userSessionManager.createBrowserSession(userWebSession, clientAddress);

        } else {
            // Existing user session, just create a browser session.
            dashboardSession = userWebSession.getUserSessionInfo();
            browserSession = userSessionManager.createBrowserSession(userWebSession,  clientAddress);
        }

        storeScreenStateInSession(browserSession, ScreenEnum.LOGIN);

        // $TODO$ nblomberg - We should NOT be sending back the password, user data to the client.
        // Eventually we want some token or Single Sign On functionality so we can verify authentication on the backend of the tools.
        response = new LoginResponse( userWebSession.getUserSessionKey(), browserSession.getBrowserSessionKey(), true, "", username, "");

        return response;
    }


    @Override
    public ExistingSessionResponse loginFromExistingSession(String browserSessionId) throws IllegalArgumentException {
        ExistingSessionResponse response = null;

        long start = System.currentTimeMillis();

        UserWebSession userWebSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        BrowserWebSession browserWebSession = userSessionManager.getBrowserSession(browserSessionId);
        logger.debug("loginFromExistingSession lookup results using key  (" + browserSessionId +
                       "), userWebSession(: " + userWebSession + "), browserSession (" + browserWebSession + ")");
        if (userWebSession != null && browserWebSession != null) {

            if(logger.isTraceEnabled()){
                logger.trace("loginFromExistingSession - existing user session found.  Session info will be sent back to the client for user: " + userWebSession );
            }

            DashboardHttpSessionData dashboardSession = userWebSession.getUserSessionInfo();
            // The user was already logged in, so retrieve any state information to send to the client.

            ScreenEnum state = getBrowserStateFromExistingSession(browserWebSession);
            boolean offline = dashboardSession.isOffline();

            if(offline) {

                //if GIFT is offline, strip out the password, since it isn't used without authentication
                response = new ExistingSessionResponse(userWebSession.getUserSessionKey(), browserWebSession.getBrowserSessionKey(), true, "",
                        dashboardSession.getUserName(), null, state, offline);

            } else {

                //otherwise, encrypt the password and include it in the response
                response = new ExistingSessionResponse(userWebSession.getUserSessionKey(), browserWebSession.getBrowserSessionKey(), true, "",
                        dashboardSession.getUserName(), encryptPassword(dashboardSession.getUserPass()), state, offline);
            }

        } else {
            response = new ExistingSessionResponse(null, null, false, "", "", "", ScreenEnum.INVALID, false);
            if(logger.isTraceEnabled()){
                logger.trace("loginFromExistingSession - user is null, which means that we cannot find an existing user session for the user.  This may be okay if the user hasn't logged in.");
            }
        }

        metrics.endTrackingRpc("loginFromExistingSession", start);

        return response;
    }

    @Override
    public RpcResponse logoutUser() throws IllegalArgumentException {
        RpcResponse logoutResponse = null;

        long start = System.currentTimeMillis();
        
        String errorMsg = authentication.logOutUser(getThreadLocalRequest());
        if(errorMsg == null) {
            
            //successful log out
            logoutResponse = new RpcResponse(null, null, true, "");
                    
        } else {
            
            //failed log out
            logoutResponse = new RpcResponse(null, null, false, errorMsg);
        }

        metrics.endTrackingRpc("logoutUser", start);

        return logoutResponse;

    }

    @Override
    public RpcResponse updateUserState(String browserSessionId, ScreenEnum state) throws IllegalArgumentException {
        RpcResponse response = null;

        long start = System.currentTimeMillis();

        if(logger.isTraceEnabled()){
            logger.trace("updateUserState called for browserSessionId: " + browserSessionId);
        }

        BrowserWebSession browserSession = userSessionManager.getBrowserSession(browserSessionId);
        if (browserSession != null && !browserSessionId.isEmpty())  {

            // Update the screen state in the browser session.
            BrowserScreenState browserState = new BrowserScreenState(state);
            browserSession.setSessionData(browserState);

            // Success
            response = new RpcResponse(null, null, true, "");

            if(logger.isTraceEnabled()){
                logger.trace("updateUserState success for browserSessionId(" + browserSessionId + ") - state found: " + state);
            }


        } else {
         // Failure
            response = new RpcResponse(null, null, false, "");
            logger.error("updateUserState - No browser session could be found.");
        }

        metrics.endTrackingRpc("updateUserState", start);

        return response;

    }
    
    @Override
    public ProgressResponse getUpdateCourseUserPermissionsProgress(String username) {
        
        if(StringUtils.isBlank(username)) {
            return new ProgressResponse(false, "Unable to retrieve the course permission update progress without a username.", null);
        }
                
        long start = System.currentTimeMillis();

        ProgressResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getUpdateCourseUserPermissionsProgress called with user: " + username + ".");
        }

        ProgressIndicator progress = usernameToCoursePermUpdateProgress.get(username);

        if(progress != null){
            response = new ProgressResponse(true, null, progress);
            
            // let the client know the job is complete and then remove progress object because
            // the progress shouldn't be updated anymore and we would prefer not to have a lingering
            // completed progress object that could be retrieved at the start of the next job and possibly
            // show 100% on the client side before it resets to 0%
            if(progress.isComplete()) {
                usernameToCoursePermUpdateProgress.remove(username);
            }

        } else {
            response = new ProgressResponse(false, "Could not find any course permission updates in progress for "+username, null);
            if(logger.isInfoEnabled()) {
                logger.info("updateCourseUserPermissions - Could not find any course permission updates in progress for user: " + username);
            }
        }

        metrics.endTrackingRpc("getUpdateCourseUserPermissionsProgress", start);

        return response;
    }

    @Override
    public RpcResponse updateCourseUserPermissions(Set<DomainOptionPermissions> permissions, DomainOption courseData, String browserSessionId) throws IllegalArgumentException {
        
        if (logger.isTraceEnabled()) {
            logger.trace("updateCourseUserPermissions called for userSession: " + browserSessionId);
        }
        
        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession == null) {
            return null;
        }
        
        final ProgressIndicator progress = new ProgressIndicator(0, "Applying, please wait...");
        usernameToCoursePermUpdateProgress.put(userSession.getUserSessionInfo().getUserName(), progress);
        
        return updateCourseUserPermissions(permissions, courseData, browserSessionId, progress);
    }
    
    /**
     * Updates the permissions for one or more users on the given course (and the course's survey context)
     * 
     * @param permissions the permissions to set, can have a null permission enum to clear a user's permissions
     * @param courseData contains information about the course which permissions are changing
     * @param browserSessionId the browser session id from the client
     * @param progress progress tracking that has already been mapped to the user's username for the browser session making the request,
     * therefore shouldn't be null.
     * @return true if success on the server, false otherwise. Null can be returned if the user session is null.
     * @throws IllegalArgumentException  if any of the arguments are not properly provided.
     */
    private RpcResponse updateCourseUserPermissions(Set<DomainOptionPermissions> permissions, DomainOption courseData, String browserSessionId, ProgressIndicator progress) throws IllegalArgumentException {

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession == null) {
            return null;
        }
        
        DashboardHttpSessionData userData = userSession.getUserSessionInfo();
        
        // assume failure
        RpcResponse response = new RpcResponse(userData.getUserSessionId(), null, false, null);
        StringBuilder errorsBuilder = new StringBuilder();

        try{
            // update the course folder permissions
            progress.setSubtaskProgressIndicator(new ProgressIndicator());
            ServicesManager.getInstance().getFileServices().updateCourseUserPermissions(userData.getUserName(), permissions, courseData, browserSessionId, progress.getSubtaskProcessIndicator());

            progress.setPercentComplete(80);
            
            if(courseData.getSurveyContextId() != null && !permissions.isEmpty()) {
                
                double scontextPercInc = 20.0 / permissions.size();
                int preTaskProgress = progress.getPercentComplete();
                int taskComplete = 1;
                
                for(DomainOptionPermissions permission : permissions){
                    if (StringUtils.isNotBlank(permission.getUser())) {
                        // course has a survey context and a user was provided to change permissions for
                        
                        boolean success = Surveys.updateSurveyContextPermissions(userData.getUserName(), courseData.getSurveyContextId(), permission.getUser(), permission.getPermission());
        
                        // collect all the failures
                        if(!success){
                            errorsBuilder.append("\nFailed to update '").append(permission.getUser()).append("' permissions on the course's survey(s). (server side exception).");
                            logger.error("Failed to update '"+permission.getUser()+"' permissions on the course's surveys(s) on behalf of "+userData.getUserName()+" for "+courseData.getSourceId());
                        }
    
                    }
                    
                    progress.setPercentComplete((int) (preTaskProgress + taskComplete * scontextPercInc));
                    taskComplete++;
                }
            }
            
            if(errorsBuilder.length() > 0) {
                throw new Exception("Failed to update survey permissions.\n"+errorsBuilder.toString());
            }
            
            response.setIsSuccess(true);

        }catch(DetailedException e){
            logger.error("Failed to update permission on behalf of "+userData.getUserName()+" for "+courseData.getSourceId(), e);

            response.setErrorStackTrace(e.getErrorStackTrace());
            response.setAdditionalInformation(e.getDetails());

            if(permissions.size() > 1){
                // when the permissions changes include changing more than one user, handle the exception handling differently
                // because it can be overwhelming to list each user or all issues
                response.setResponse("One or more of the user permissions failed to be set correctly.");
            }else{
                response.setResponse(e.getReason());
            }

        }catch(Exception e){
            logger.error("Failed to update permission on behalf of "+userData.getUserName()+" for "+courseData.getSourceId(), e);

            response.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            response.setAdditionalInformation("There was a server side exception of "+e.toString());

            if(permissions.size() > 1){
                // when the permissions changes include changing more than one user, handle the exception handling differently
                // because it can be overwhelming to list each user or all issues
                response.setResponse("One or more of the user permissions failed to be set correctly.");
            }else{
                response.setResponse("Unable to update the course permissions.");
            }
        }
        
        progress.setComplete(true);
        progress.setPercentComplete(100);

        return response;
    }

    @Override
    public RpcResponse updatePublishedCourseUserPermissions(Set<DataCollectionPermission> permissions, String dataCollectionId, String browserSessionId) throws IllegalArgumentException {
        
        if (logger.isTraceEnabled()) {
            logger.trace("updatePublishedCourseUserPermissions called for userSession: " + browserSessionId);
        }

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession == null) {
            return null;
        }
        
        final ProgressIndicator progress = new ProgressIndicator(0, "Applying, please wait...");
        usernameToCoursePermUpdateProgress.put(userSession.getUserSessionInfo().getUserName(), progress);
        
        return updatePublishedCourseUserPermissions(permissions, dataCollectionId, browserSessionId, progress);
    }
    
    /**
     * Updates the permissions for one or more users on the given published course
     * @param permissions the permissions to set, can have a null permission enum to clear a user's permissions
     * @param dataCollectionId the unique id of the published course to update user permission on
     * @param browserSessionId the browser session id from the client
     * @param progress progress tracking that has already been mapped to the user's username for the browser session making the request,
     * therefore shouldn't be null.
     * @return true if success on the server, false otherwise. Null can be returned if the user session is null.
     * @throws IllegalArgumentException  if any of the arguments are not properly provided.
     */
    private RpcResponse updatePublishedCourseUserPermissions(Set<DataCollectionPermission> permissions, 
            String dataCollectionId, String browserSessionId, ProgressIndicator progress) throws IllegalArgumentException {
        int permissionSize;
        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession == null) {
            return null;
        }

        DashboardHttpSessionData userData = userSession.getUserSessionInfo();
        
        // assume failure
        RpcResponse response = new RpcResponse(userData.getUserSessionId(), null, false, null);
        
        permissionSize = permissions.size();
        double percentPerItem = permissionSize == 0 ? 100 : 100.0 / permissionSize; 
        int preTaskProgress = progress.getPercentComplete();
        int taskComplete = 1;

        for(DataCollectionPermission permission : permissions){
            try{
    
                DataCollectionManager.getInstance().updateDataCollectionPermissions(userData.getUserName(), dataCollectionId, permission);
                
                progress.setPercentComplete((int) (preTaskProgress + taskComplete * percentPerItem));
                taskComplete++;                
    
            }catch(DetailedException e){
                logger.error("Failed to update permission on behalf of "+userData.getUserName()+" for "+dataCollectionId, e);

                response.setErrorStackTrace(e.getErrorStackTrace());
                response.setAdditionalInformation("Failed to update permission (server side exception).\n"+e.getDetails());
                
                if(permissions.size() > 1){
                    // when the permissions changes include changing more than one user, handle the exception handling differently
                    // because it can be overwhelming to list each user or all issues
                    response.setResponse("One or more of the user permissions failed to be set correctly.");
                }else{
                    response.setResponse(e.getReason());
                }
    
            }catch(Exception e){
                logger.error("Failed to update permission on behalf of "+userData.getUserName()+" for "+dataCollectionId, e);

                response.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                response.setAdditionalInformation("There was a server side exception of "+e.toString());
                
                if(permissions.size() > 1){
                    // when the permissions changes include changing more than one user, handle the exception handling differently
                    // because it can be overwhelming to list each user or all issues
                    response.setResponse("One or more of the user permissions failed to be set correctly.");
                }else{
                    response.setResponse("Unable to update the course permissions.");
                }
            }
        }
        
        progress.setComplete(true);
        progress.setPercentComplete(100);
        
        if(response.getResponse() == null) {
            response.setIsSuccess(true);
        }

        return response;
    }

    /**
     * Stores the screen state information into the http session.
     * @param state - The screen state to store in the http session.
     */
    private void storeScreenStateInSession(BrowserWebSession session, ScreenEnum state) {

        if (session != null) {
            session.setSessionData(new BrowserScreenState(state));
        }

    }

    /**
     * Retrieves the browser state from an existing session (currently a screen enum of
     * the screen that the browser currently is at).
     *
     * @return ScreenEnum - Returns the state of the current user session.  If the session cannot be found, invalid is returned.
     */
    private ScreenEnum getBrowserStateFromExistingSession(BrowserWebSession browserSession) {
        ScreenEnum state = ScreenEnum.INVALID;
        AbstractWebSessionData webData = browserSession.getSessionData();
        if (webData instanceof BrowserScreenState) {
            BrowserScreenState browserData = (BrowserScreenState)webData;
            state = browserData.getScreenState();
        }

        return state;
    }

    @Override
    public GenericRpcResponse<DomainOption> getCourseById(String browserSessionId, String courseId){
        long start = System.currentTimeMillis();

        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder("getCourseById(");
            List<Object> params = Arrays.<Object>asList(browserSessionId, courseId);
            join(", ", params, sb);
            logger.trace(sb.append(")").toString());
        }

        GenericRpcResponse<DomainOption> response;

        CourseOptionsWrapper courseWrapper = new CourseOptionsWrapper();
        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            DashboardHttpSessionData userData = userSession.getUserSessionInfo();

            String userName = "";
            if (userData != null) {
                userName = userData.getUserName();
            }

            try {
                CourseRecord course = ServicesManager.getInstance().getDbServices().getCourseById(courseId, false);
                if(course == null){
                    response = new FailureResponse<>(
                            new DetailedException("Failed to retrieve the course", 
                                    "The course with id '"+courseId+"' may have been deleted.", null));
                    
                }else{
                    ServicesManager.getInstance().getFileServices().getCourse(userName, course.getCoursePath(),
                            courseWrapper, false, false, false, null);
    
                    metrics.endTrackingRpc("getCourseById", start);
                    response = new SuccessfulResponse<>(
                            courseWrapper.domainOptions.get(course.getCoursePath()));
                }
            } catch (Exception e) {
                String errorMsg = "An error occurred while trying to retrieve course with id '" + courseId + "'.";
                String errorDetails = "Exception caught while trying to retrieve course (" + courseId + ")";
                logger.error("getCourseById - " + errorMsg, e);

                response = new FailureResponse<>(new DetailedException(errorMsg, errorDetails, e));
            }
        } else {
            String errorMsg = "Unable to find user session with browser id '" + browserSessionId + "'.";
            String errorDetails = "Unable to find user session with browser id '" + browserSessionId + "'.";
            logger.error("getCourseById - " + errorMsg);

            response = new FailureResponse<>(new DetailedException(errorMsg, errorDetails, null));
        }

        metrics.endTrackingRpc("getCourseById", start);
        return response;
    }

    @Override
    public GetCourseListResponse getCourseList(String browserSessionId, boolean validateCoursePaths, boolean provideSimpleList, CourseListFilter filter) {
        GetCourseListResponse response = null;

        long start = System.currentTimeMillis();

        if(logger.isTraceEnabled()){
            logger.trace("getCourseList called with browserSessionId: " + browserSessionId);
        }

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {

            DashboardHttpSessionData userData = userSession.getUserSessionInfo();

            String userName = "";
            try {

                CourseOptionsWrapper courseWrapper = new CourseOptionsWrapper();
                ArrayList<DomainOption> courses = new ArrayList<>();
                ProgressIndicator progress = new ProgressIndicator(0, COURSE_LIST_PROGRESS_DESC);

                boolean foundInvalidCoursePath = false;

                if (userData != null) {
                    userName = userData.getUserName();
                    usernameToGetCourseListProgress.put(userName, progress);
                }

                // Do not do any validation here so that we can load the initial course data (not validated) to the user.
                // At a later point, each course will be separately validated.
                // 10.31.17 - applying LMS recommendations here for the dashboard since each course is now validated
                //            when starting the course.  It was that course validation that use to apply LMS recommendations.
                try{
                    ServicesManager.getInstance().getFileServices().getCourses(userName,
                            courseWrapper,
                            filter,
                            false,
                            progress);
                    
                    if(!provideSimpleList && DashboardProperties.getInstance().shouldApplyLMSRecordsAtCourseListRequest()){
                            
                        if(logger.isDebugEnabled()){
                            logger.debug("applying LMS history for "+userName+" to "+courseWrapper.domainOptions.size()+" courses.");
                        }
                        
                        if(progress != null){
                            progress.setTaskDescription(DbServicesInterface.LMS_RECORDS_PROGRESS_DESC);
                        }
                        
                        ServicesManager.getInstance().getDbServices().applyLMSRecommendations(userName, courseWrapper);
 
                        if(progress != null){
                            progress.setPercentComplete(DbServicesInterface.LMS_RECORDS_PERCENT_COMPLETE);
                            progress.setComplete(true);
                        }
                    }
                }catch(LmsException lmsException){
                    //prevent LMS exceptions from causing the course tiles to not show up
                    logger.error("Caught an LMS exception while trying to apply LMS records to the courses for user "+userName, lmsException);
                }

                Iterator<Map.Entry<String, DomainOption>> it = courseWrapper.domainOptions.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry<String, DomainOption> entry = it.next();
                    DomainOption domainItem = entry.getValue();

                    courses.add(domainItem);

                    if(logger.isTraceEnabled()){
                        logger.trace("getCourseList - adding course: " + domainItem);
                    }

                    if (!userName.isEmpty() && validateCoursePaths && !foundInvalidCoursePath) {
                        /* The DomainId may or may not start with a forward slash, so have to take that
                         * into account when checking courses that belong to the user. */
                        if (domainItem.getDomainId().startsWith(userName) || domainItem.getDomainId().substring(1).startsWith(userName)) {

                            String path = domainItem.getDomainId();
                            String name = domainItem.getDomainName();

                            if(!isValidPath(path, name)){
                                foundInvalidCoursePath = true;
                            }

                        }
                    }
                }

                //create options for course that failed to parse so they still show up in the course tile list
                for(String courseFilename : courseWrapper.parseFailedFiles){
                    File courseFile = new File(courseFilename);

                    //remove the path and course.xml suffix to only show the course file name on the course tile
                    String trimmedCourseFileName = courseFile.getName().substring(0, courseFile.getName().indexOf(AbstractSchemaHandler.COURSE_FILE_EXTENSION));
                    DomainOption domainItem = new DomainOption(trimmedCourseFileName, courseFilename, null, userName);
                    DomainOptionRecommendation domainOptionRecommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.UNAVAILABLE_OTHER);
                    domainOptionRecommendation.setReason("There was an issue validating this course that will need to be resolved using the course creator.");
                    domainOptionRecommendation.setDetails("Please use the course creator's validation tool to help resolve validation issues with this course.  "+
                    "If you don't have permission to edit this course than notify the course owner about the issue you are seeing.  "+
                            "You may also use the forums at gifttutoring.org for additional help on fixing this course.");
                    domainItem.setDomainOptionRecommendation(domainOptionRecommendation);
                    courses.add(domainItem);
                }

                response = new GetCourseListResponse(null, null, true, null, courses);
                response.setHasInvalidPaths(foundInvalidCoursePath);
                response.getUpconvertedCourses().addAll(courseWrapper.upconvertedFiles);

                if(logger.isDebugEnabled()){
                    logger.debug("getCourseList - success for user: " + browserSessionId + ". Found " + courses.size() + " courses total.");
                }

            } catch(RuntimeException e) {

                String exceptionMsg = e.getMessage();
                if(exceptionMsg == null){
                    exceptionMsg = "Exception thrown on the server.";
                }
                
                String errorMsg;
                if(SERVER_PROPERTIES.isServerMode()){
                    String cms_url = SERVER_PROPERTIES.getPropertyValue(ServerProperties.CMS_URL);
                    errorMsg = "There was a problem on the server that will need to be investigated.<br/><br/>"+exceptionMsg+"<br/><br/> It is also possible that the content management system at " + cms_url + " could not be reached, therefore no courses"
                            + " will be displayed. Please check your internet connection and try again.<br/><br/>Deployment Mode: "
                            + "Server<br/>CMS URL: " + cms_url + "<br/><br/>If the content management url is incorrect, you may "
                            + "update the CMS_URL property in the <i>common.properties</i> file located at GIFT/config. If you do "
                            + "not have an internet connection, you may change the DeploymentMode property to 'Desktop'.";
                }else{
                   errorMsg = "There was a problem on the server that will need to be investigated.<br/><br/>"+exceptionMsg+
                           ".<br/><br/> You might still be able to create courses but the error needs to be resolved to really use this system.";
                }

                response = new GetCourseListResponse(browserSessionId, null, false, errorMsg, null);
                logger.error("getCourseList - Exception caught while trying to fetch course list for user: " + browserSessionId, e);
             }catch (Exception e) {
                response = new GetCourseListResponse(browserSessionId, null, false, null, null);
                logger.error("getCourseList - Exception caught while trying to fetch course list for user: " + browserSessionId, e);
            } catch (Throwable t) {
                response = new GetCourseListResponse(browserSessionId, null, false, null, null);
                logger.error("getCourseList - Throwable caught while trying to fetch course list for user: " + browserSessionId, t);
            }finally{

                if(StringUtils.isNotBlank(userName)){
                    usernameToGetCourseListProgress.remove(userName);
                }
            }

        } else {
            response = new GetCourseListResponse(null, null, false, "Unable to find the user session "+browserSessionId, null);

            if(logger.isDebugEnabled()){
                logger.debug("getCourseList - User is not found in session: " + browserSessionId);
            }
        }

        metrics.endTrackingRpc("getCourseList", start);
        return response;
    }

    /**
     * Corrects the path for the given courses to ensure that the names of the course's folder, .course.xml file,
     * and Course object match and do not cause conflicts
     *
     * @param course the course currently being corrected
     * @param coursesToUpdate all of the courses that are being corrected
     * @param existingPaths all of the course paths that currently exist. Strings should be in lower case. Used to avoid conflicts.
     * @param displayNames the display names of all the courses that exist. Used to avoid conflicts.
     * @throws DetailedException if an error occurs while correcting the given course's path
     */
    private void correctCoursePath(CourseObject course, HashMap<String, CourseObject> coursesToUpdate, HashSet<String> existingPaths, HashSet<String> displayNames) throws DetailedException{

        if(course.isCorrect()) {
            return;
        }

        if(existingPaths.contains(course.getTargetPath().toLowerCase())) {

            /*
             * If A/A.xml already exists, check if there is a matching display name.
             * Also make sure no two courses have the same target path
             */

            if(displayNames.contains(course.getTargetName()) || coursesToUpdate.containsKey(course.getTargetPath())) {

                /*
                 * If there is a conflict, set the name to "Course Name (1)"
                 * First determine if this course name already ends in with "(int)"
                 */

                Pattern intPattern = Pattern.compile("(.*)\\((\\d+)\\)");
                Matcher intMatcher = intPattern.matcher(course.getTargetName());

                if(intMatcher.matches()) {

                    // Increment the number suffix
                    String prefix = intMatcher.group(1);
                    int num = Integer.parseInt(intMatcher.group(2)) + 1;
                    course.updateNameAndPath(prefix + "(" + num + ")");

                } else {

                    // Add a number suffix to the name and try again
                    course.updateNameAndPath(course.getTargetName() + " (1)");
                }
            } else {

                // Otherwise, A/A.xml exists but should be corrected before this one can continue
                correctCoursePath(coursesToUpdate.get(course.getTargetPath()), coursesToUpdate, existingPaths, displayNames);
            }

            // Try again
            correctCoursePath(course, coursesToUpdate, existingPaths, displayNames);

        } else {

            try {

                // Correct the file path
                AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();

                fileServices.renameCourse(course.getUsername(), course.getCurrentPath(), course.getTargetName());
                displayNames.add(course.getTargetName());
                existingPaths.remove(course.getCurrentPath().toLowerCase());
                existingPaths.add(course.getTargetPath().toLowerCase());
                course.setCorrect();

            } catch (Exception e){

                throw new DetailedException(
                        "Failed to correct the path for the course named '" + course.getTargetName()
                        +  "'. An error occurred while renaming the course. ",
                        "An exception was caught while renaming '" + course.getTargetName() + "'",
                        e
                );
            }
        }
    }

    /**
     * Returns whether the course path and course name adhere to GIFT standards.
     *
     * @param coursePath the workspace relative path to a course.xml file
     * @param courseName the name of a course (doesn't end with .course.xml)
     * @return false if any of the following:
     * 1. course path doesn't end with the <courseName>/<courseName>.course.xml
     * 2. courseName contains leading or trailing spaces
     */
    private boolean isValidPath(String coursePath, String courseName){

        if(!coursePath.endsWith(courseName + Constants.FORWARD_SLASH + courseName + AbstractSchemaHandler.COURSE_FILE_EXTENSION)) {
            // Verify that course paths are in the following format: CourseName/CourseName.course.xml
            return false;
        }else if(courseName.startsWith(Constants.SPACE) || courseName.endsWith(Constants.SPACE)){
            // the name, course folder and course.xml need trim check because GIFT doesn't support leading or trailing spaces
            return false;
        }

        return true;
    }

    @Override
    public CorrectCoursePathsResult correctCoursePaths(final String browserSessionId, List<DomainOption> courseList) {

        long start = System.currentTimeMillis();

        if(logger.isDebugEnabled()){
            logger.debug("correctCoursePaths - start");
        }
        CorrectCoursePathsResult result = new CorrectCoursePathsResult();

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {

            final DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            String userName = userData.getUserName();
            HashSet<String> displayNames = new HashSet<>();
            HashMap<String, CourseObject> coursesToUpdate = new HashMap<>();
            HashSet<String> existingPaths = new HashSet<>();

            // Collect all courses with invalid paths
            for(DomainOption course : courseList) {

                /*
                 * The DomainId may or may not start with a forward slash, so have to take that
                 * into account when checking courses that belong to the user.
                 */
                if (course.getDomainId().startsWith(userName) || course.getDomainId().substring(1).startsWith(userName)) {

                    String path = course.getDomainId();
                    String name = course.getDomainName();

                    // the displayNames list and the CourseObject use the same name, so no need to worry about case sensitivity here
                    displayNames.add(name);

                    // the correctCoursePath method checks for a different string in existingPaths, so use lowercase for case insensitivity
                    existingPaths.add(path.toLowerCase());

                    if(!isValidPath(path, name)){
                        coursesToUpdate.put(path, new CourseObject(course, userName));
                    }

                }
            }

            // Update the course paths
            Iterator<CourseObject> updateIterator = coursesToUpdate.values().iterator();
            while(updateIterator.hasNext()) {
                CourseObject course = updateIterator.next();
                try {
                    correctCoursePath(course, coursesToUpdate, existingPaths, displayNames);

                    if(course.getOriginalName().equals(course.getTargetName())) {
                        result.addRenamedCourse(course.getOriginalPathStr(), course.getTargetPathStr());
                    } else {
                        result.addRenamedCourse(course.getOriginalName(), course.getTargetName());
                    }

                } catch (DetailedException e) {
                    result.addFailedCourse(course.getOriginalPathStr(), e.getReason(), e.getDetails());
                }
            }

        }

        result.setIsSuccess(true);

        logger.debug("correctCoursePaths - end");
        metrics.endTrackingRpc("correctCoursePaths", start);

        return result;
    }

    @Override
    public RpcResponse isUrlReachable(String url) throws IllegalArgumentException {

        RpcResponse response = null;

        // Note that this can 'fail' in secure mode (https protocol) if the certificate is not setup properly.
        // An exception will be thrown if the certificate is not valid.  In this case we still want to return
        // failure since the url will not be accessible to the user until the certifiate issues are resolved.
            try {

                if(logger.isTraceEnabled()){
                    logger.trace("Checking url to see if it is reachable: " + url);
                }


                final int HTTP_OK = 200;
                final int WAIT_TIMEOUT_MS = 5000;
                URL fullUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) fullUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(WAIT_TIMEOUT_MS);  // wait 5 seconds
                connection.connect();
                int responseCode = connection.getResponseCode();
                // Http codes for reference: http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
                if ( responseCode == HTTP_OK) {
                    // Server returned 200 response, so we will return 'true' back to the client.
                    response = new RpcResponse(null, null, true, "");

                    if(logger.isTraceEnabled()){
                        logger.trace("Url is reachable, server returned response of 200: " + url);
                    }
                } else {
                    response = new RpcResponse(null, null, false, "");
                    logger.error("Retrieved response code of: " + connection.getResponseCode() + ", message of: " + connection.getResponseMessage());

                }

            } catch (Exception e) {
                logger.error("Exception caught while checking if URL "+url+" is reachable: ", e);
                response = new RpcResponse(null, null, false, "");
            }

        return response;
    }

    @Override
    public GetUsernamesResponse getUsernames(){

        long start = System.currentTimeMillis();

        GetUsernamesResponse response;
        try {
            ArrayList<String> usernameList = new ArrayList<>();

            if(!DashboardProperties.getInstance().isServerDeploymentMode()){
                //don't allow this expensive call in server mode!
                List<DbUser> userList = UMSDatabaseManager.getInstance().getAllUsers();
                for (DbUser user : userList) {
                    if(user.getUsername() != null) {
                        usernameList.add(user.getUsername());
                    }
                }
            }

            if(logger.isTraceEnabled()){
                logger.trace("getUsernames - returned success: " + usernameList);
            }
            response = new GetUsernamesResponse(true, null, usernameList);
        } catch(Throwable e) {
            logger.error("Caught exception while retrieving usernames from UMS: ", e);
            response = new GetUsernamesResponse(false, e.getMessage(), null);
        }

        metrics.endTrackingRpc("getUsernames", start);

        return response;
    }

    @Override
    public RpcResponse getUserId(String username){

        long start = System.currentTimeMillis();

        if(logger.isTraceEnabled()){
            logger.trace("getUserId rpc is called with username: " + username);
        }
        RpcResponse response;
        try{
            DbUser user = UMSDatabaseManager.getInstance().getUserByUsername(username, false);
            response = new RpcResponse(null, null, true, "" + user.getUserId());
            if(logger.isTraceEnabled()){
                logger.trace("getUserId - success.  Userid found: " + user.getUserId());
            }
        }catch(Throwable e){
            logger.error("Caught exception while retrieving user from UMS by username of '"+username+"'.", e);
            response = new RpcResponse(null, null, false, "Failed to retrieve GIFT user by username of '" + username + "'");
        }

        metrics.endTrackingRpc("getUserId", start);

        return response;
    }

    @Override
    public LmsCourseRecordsResponse getLmsData(String browserSessionId, String domainId, Integer domainSessionId, int indexStart, int pageSize)
            throws IllegalArgumentException {

        long start = System.currentTimeMillis();

        LmsCourseRecordsResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getLmsData - Requesting lms data for browser session: (" + browserSessionId + ")");
        }

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            try {
                DashboardHttpSessionData userData = userSession.getUserSessionInfo();

                if (userData != null && userData.getUserName() != null && !userData.getUserName().isEmpty()) {
                    String userName = userData.getUserName();
                    DbUser dbUser = UMSDatabaseManager.getInstance().getUserByUsername(userName, false);

                    if (dbUser != null) {

                        LMSDataRequest request = new LMSDataRequest(userName);
                        request.setPageStart(indexStart);
                        request.setPageSize(pageSize);
                        request.setShouldSortDescending(true);
                        request.addDomainSessionId(domainSessionId);

                        if(domainId != null){
                            request.addDomainId(domainId);
                        }

                        LMSCourseRecords records = ServicesManager.getInstance().getDbServices().getLMSData(dbUser.getUserId(),  request);

                        if (records != null) {
                             response = new LmsCourseRecordsResponse(null, null, true, null, records);

                             if(logger.isTraceEnabled()){
                                 logger.trace("getLmsData - success.  Found records: " +  records);
                             }
                        } else {
                            response = new LmsCourseRecordsResponse(null, null, false, null, null);
                            logger.error("getLmsData - null records returned for user: " + userName);
                        }


                    } else {
                        response = new LmsCourseRecordsResponse(null, null, false, null, null);
                        logger.error("getLmsData - Unable to get a user id for user: " + userName);
                    }

                } else {
                    response = new LmsCourseRecordsResponse(null, null, false, null, null);
                    logger.error("getLmsData - Unable to find a user from existing session: " + browserSessionId);
                }
            } catch (LmsIoException | LmsInvalidStudentIdException | LmsInvalidCourseRecordException |
                    UMSDatabaseException | ConfigurationException | ProhibitedUserException e) {
                logger.error("Caught exception while trying to get LMS data", e);
                response = new LmsCourseRecordsResponse(null, null, false, null, null);
            }
        } else {
            response = new LmsCourseRecordsResponse(null, null, false, null, null);
            logger.debug("getLmsData - User is not found in session: " + browserSessionId);
        }

        metrics.endTrackingRpc("getLmsData", start);

        return response;
    }

    @Override
    public LmsCourseRecordsResponse getLatestRootLMSDataPerDomain(String browserSessionId)
            throws IllegalArgumentException {

        long start = System.currentTimeMillis();

        LmsCourseRecordsResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getLatestRootLMSDataPerDomain - Requesting lms data for browser session: (" + browserSessionId + ")");
        }
        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            try {
                DashboardHttpSessionData userData = userSession.getUserSessionInfo();

                if (userData != null && userData.getUserName() != null && !userData.getUserName().isEmpty()) {
                    String userName = userData.getUserName();
                    DbUser dbUser = UMSDatabaseManager.getInstance().getUserByUsername(userName, false);

                    if (dbUser != null) {

                        LMSCourseRecords records = ServicesManager.getInstance().getDbServices().getLatestRootLMSDataPerDomain(dbUser.getUserId(), userName);

                        if (records != null) {
                            response = new LmsCourseRecordsResponse(null, null, true, null, records);
                            if(logger.isTraceEnabled()){
                                logger.trace("getLatestRootLMSDataPerDomain - success.  Found records: " +  records);
                            }

                        } else {
                            response = new LmsCourseRecordsResponse(null, null, false, null, null);

                            if(logger.isTraceEnabled()){
                                logger.error("getLatestRootLMSDataPerDomain - null records returned for user: " + userName);
                            }
                        }


                    } else {
                        response = new LmsCourseRecordsResponse(null, null, false, null, null);
                        logger.error("getLatestRootLMSDataPerDomain - Unable to get a user id for user: " + userName);
                    }

                } else {
                    response = new LmsCourseRecordsResponse(null, null, false, null, null);
                    logger.error("getLatestRootLMSDataPerDomain - Unable to find a user from existing browser session: " + browserSessionId);
                }
            } catch (LmsIoException | LmsInvalidStudentIdException | LmsInvalidCourseRecordException |
                    UMSDatabaseException | ConfigurationException | ProhibitedUserException e) {
                logger.error("Caught exception while trying to get LMS data", e);
                response = new LmsCourseRecordsResponse(null, null, false, null, null);
            }
        } else {
            response = new LmsCourseRecordsResponse(null, null, false, null, null);
            logger.debug("getLatestRootLMSDataPerDomain - User is not found in session: " + browserSessionId);
        }

        metrics.endTrackingRpc("getLatestRootLMSDataPerDomain", start);

        return response;
    }
    
    @Override
    public ExportResponse exportCourseData(String username, DomainOption course){
        
        long start = System.currentTimeMillis();
        ExportResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("exportCourseData called with user: " + username + " and course: " + course);
        }
        
        if(course != null){
            try{
                DownloadableFileRef result =  ExportManager.getInstance().exportCourseData(username, course);
    
                response = new ExportResponse(true, null, result);
    
            }catch(Exception e){
                logger.warn("Caught exception while trying to export course data for "+username+" on "+course.getDomainId()+".", e);
    
                StringBuilder errorReason = new StringBuilder();
                errorReason.append("There was a problem while exporting the data for the course '").append(course.getDomainName());
                errorReason.append("'");
    
                response = new ExportResponse(false, errorReason.toString(), null);
    
                String errorDetails = "Failed to export course data because an exception was thrown while exporting the course data.\n"+e.getMessage();    
                if(e.getCause() != null){
                    errorDetails += "\n\n"+e.getCause();
                }
    
                response.setErrorMessage(errorDetails);
                response.setAdditionalInformation(errorDetails);
                response.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            }
    
        } else {
    
            logger.warn("exportCourseData - The course to export data for cannot be null.");
            response = new ExportResponse(false, "Failed to export course data because the course to export cannot be null.", null);
            response.setErrorMessage("Failed to export course data because no course was provided");
        }

        metrics.endTrackingRpc("exportCourseData", start);

        return response;
    }
    
    @Override
    public ProgressResponse getExportCourseDataProgress(String username){
        
        long start = System.currentTimeMillis();

        ProgressResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getExportCourseDataProgress called with user: " + username + ".");
        }

        ProgressIndicator progress = ExportManager.getInstance().getExportProgress(username);

        if(progress != null){
            response = new ProgressResponse(true, null, progress);

        } else {
            response = new ProgressResponse(false, "Could not find any course data exports in progress for this user. ", null);
            logger.info("getExportCourseDataProgress - Could not find any course data exports in progress for user: " + username);
        }

        metrics.endTrackingRpc("getExportCourseDataProgress", start);

        return response;
    }
    
    @Override
    public RpcResponse cancelExportCourseData(String username){
        
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("cancelExportCourseData called with user: " + username + ".");
        }

        ExportManager.getInstance().cancelExport(username);

        response = new RpcResponse(null, null, true, null);

        metrics.endTrackingRpc("cancelExportCourseData", start);

        return response;
    }
    
    @Override
    public RpcResponse deleteCourseDataExportFile(DownloadableFileRef exportResult){
        
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("deleteCourseDataExportFile called with export result: " + exportResult.getLocationOnServer() + ".");
        }

        response = new RpcResponse(null, null, ExportManager.getInstance().deleteExportFile(exportResult), null);

        metrics.endTrackingRpc("deleteCourseDataExportFile", start);

        return response;
    }

    @Override
    public ExportResponse exportCourses(String username, List<DomainOption> coursesToExport){

        long start = System.currentTimeMillis();
        ExportResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("exportCourses called with user: " + username + " and courses: " + coursesToExport);
        }

        if(coursesToExport != null && !coursesToExport.isEmpty()){

            try{
                DownloadableFileRef result =  ExportManager.getInstance().export(username, coursesToExport);

                response = new ExportResponse(true, null, result);

            }catch(Exception e){
                logger.warn("Caught exception while trying to export course(s) for "+username+".", e);

                StringBuilder errorReason = new StringBuilder();
                errorReason.append("There was a problem while exporting the following course(s):<br/><ul>");

                for(DomainOption course : coursesToExport){
                    errorReason.append("<li>");
                    errorReason.append(course.getDomainName());
                    errorReason.append("</li>");
                }

                errorReason.append("</ul>Please check these courses for validation errors, since such errors ");
                errorReason.append("may have prevented a course from being exported.");

                response = new ExportResponse(false, errorReason.toString(), null);

                String errorDetails;
                if(e instanceof CourseFileValidationException){
                    errorDetails = "The course "+((CourseFileValidationException)e).getFileName()+" could not be parsed therefore GIFT was unable"+
                            " to determine what other resources are needed in the export.\n\nThe error was:\n"+((CourseFileValidationException)e).getLocalizedMessage();
                }else{

                    errorDetails = "Failed to export because an exception was thrown while exporting the selected course(s).\n"+e.getMessage();

                    if(e.getCause() != null){
                        errorDetails += "\n\n"+e.getCause();
                    }
                }

                response.setErrorDetails(errorDetails);
                response.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            }

        } else {

            logger.warn("exportCourses - The list of courses to export cannot be null or empty.");
            response = new ExportResponse(false, "Failed to export course because the list of courses to export cannot be null or empty.", null);
            response.setErrorMessage("Failed to export course because an empty list of courses was provided");
        }

        metrics.endTrackingRpc("exportCourses", start);

        return response;
    }

    @Override
    public ProgressResponse getExportProgress(String username){

        long start = System.currentTimeMillis();

        ProgressResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getExportProgress called with user: " + username + ".");
        }

        ProgressIndicator progress = ExportManager.getInstance().getExportProgress(username);

        if(progress != null){
            response = new ProgressResponse(true, null, progress);

        } else {
            response = new ProgressResponse(false, "Could not find any exports in progress for this user. ", null);
            logger.info("exportCourses - Could not find any exports in progress for user: " + username);
        }

        metrics.endTrackingRpc("getExportProgress", start);

        return response;
    }

    @Override
    public RpcResponse cancelExport(String username){

        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("cancelExport called with user: " + username + ".");
        }

        ExportManager.getInstance().cancelExport(username);

        response = new RpcResponse(null, null, true, null);

        metrics.endTrackingRpc("cancelExport", start);

        return response;
    }

    @Override
    public RpcResponse deleteExportFile(DownloadableFileRef exportResult) {
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("deleteExportFile called with export result: " + exportResult.getLocationOnServer() + ".");
        }

        response = new RpcResponse(null, null, ExportManager.getInstance().deleteExportFile(exportResult), null);

        metrics.endTrackingRpc("deleteExportFile", start);

        return response;
    }

    /* (non-Javadoc)
     * @see mil.arl.gift.tools.dashboard.client.DashboardService#calculateExportSize(java.lang.String, java.util.List)
     */
    @Override
    public DoubleResponse calculateExportSize(String username,
            List<DomainOption> coursesToExport) {

        long start = System.currentTimeMillis();
        DoubleResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("calculateExportSize called with user: " + username + " and courses: " + coursesToExport);
        }

        try {
            double exportSize = ExportManager.getInstance().getExportSize(username, coursesToExport);
            response = new DoubleResponse(true, null, exportSize);

        } catch (Exception e) {
            logger.warn("An exception ocurred while calculating export size for user '" + username + "'.", e);
            response = new DoubleResponse(false, "An exception ocurred while calculating the size of the export. " + e.toString(), 0);
        }

        metrics.endTrackingRpc("calculateExportSize", start);

        return response;
    }

    @Override
    public GenericRpcResponse<Void> cleanupLoadCourseIndicator(String browserSessionId, DomainOption courseData){

        if(logger.isTraceEnabled()){
            logger.trace("cleanupLoadCourseIndicator - cleanupLoadCourseIndicator called with browserSessionId: " + browserSessionId);
        }

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            if (userData != null && courseData != null) {
                LoadCourseManager.getInstance().cleanup(userData.getUserName(), courseData.getDomainId());
            }
        }

        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> cleanupLoadCourseIndicator(String consumerKey, String consumerId, String courseSourceId){

        TrustedLtiConsumer consumer = consumerMap.get(consumerKey);

        if (consumer != null) {
            String uniqueConsumerId = TrustedLtiConsumer.getInternalUniqueConsumerId(consumer, consumerId);
            LoadCourseManager.getInstance().cleanup(uniqueConsumerId, courseSourceId);
        }

        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> cleanupLoadCourseIndicator(String progressIndicatorId) {

        if (StringUtils.isNotBlank(progressIndicatorId)) {
            LoadCourseManager.getInstance().cleanup(null, progressIndicatorId);
        }

        return new SuccessfulResponse<>();
    }

    @Override
    public RpcResponse loadCourse(String browserSessionId, DomainOption courseData) {
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("loadCourse - loadCourse called with browserSessionId: " + browserSessionId);
        }

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            if (userData != null) {


                try {
                    if(logger.isDebugEnabled()){
                        logger.debug("start load course called for user: " + userData.getUserName() + ", course id: " + courseData.getDomainId());
                    }
                    String runtimeCourseId = LoadCourseManager.getInstance().startCourseLoad(userData.getUserName(), courseData.getDomainId());

                    // Embed the course id into the rpc response.
                    response = new RpcResponse(userSession.getUserSessionKey(), browserSessionId, true, runtimeCourseId);

                    if(logger.isDebugEnabled()){
                        logger.debug("loadCourse - loadCourse returned success with runtime course id: " + runtimeCourseId);
                    }

                } catch (IllegalArgumentException e) {
                    String errorMsg = "IllegalArgumentException caught while trying to load course for (" + userData.getUserName() + "), and course (" + courseData.getDomainId() + ").";
                    logger.error("loadCourse - " + errorMsg, e);
                    response = new RpcResponse(userSession.getUserSessionKey(), browserSessionId, false, e.getMessage());
                    response.setAdditionalInformation(errorMsg);
                    response.setErrorStackTrace(DetailedException.getFullStackTrace(e));

                } catch (IOException e) {
                    String errorMsg = "IOException caught while trying to load course for (" + userData.getUserName() + "), and course (" + courseData.getDomainId() + ").";
                    logger.error("loadCourse - " + errorMsg, e);
                    response = new RpcResponse(userSession.getUserSessionKey(), browserSessionId, false, e.getMessage());
                    response.setAdditionalInformation(errorMsg);
                    response.setErrorStackTrace(DetailedException.getFullStackTrace(e));

                } catch (DetailedException e) {
                    String errorMsg = "Exception caught while trying to load course for (" + userData.getUserName() + "), and course (" + courseData.getDomainId() + ").";
                    logger.error("loadCourse - " + errorMsg, e);
                    response = new RpcResponse(userSession.getUserSessionKey(), browserSessionId, false, e.getReason());
                    response.setAdditionalInformation(e.getDetails());
                    response.setErrorStackTrace(e.getErrorStackTrace());

                } catch (Exception e) {
                    String errorMsg = "Exception caught while trying to load course for (" + userData.getUserName() + "), and course (" + courseData.getDomainId() + ").";
                    logger.error("loadCourse - " + errorMsg, e);
                    response = new RpcResponse(userSession.getUserSessionKey(), browserSessionId, false, e.getMessage());
                    response.setAdditionalInformation(errorMsg);
                    response.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                }

            } else {
                String errorMsg = "Unable to find usersession for session id: " + browserSessionId;
                logger.error("loadCourse - " + errorMsg);
                response = new RpcResponse(null, null, false, "Unable to find the user session.\n\nHave you been idle for a long time?  Try logging in again.");
                response.setAdditionalInformation(errorMsg);
            }
        } else {
            String errorMsg = "Unable to find user session for session id: " + browserSessionId;
            logger.error("loadCourse - " + errorMsg);
            response = new RpcResponse(null, null, false, "Unable to find the user session.\n\nHave you been idle for a long time?  Try logging in again.");
            response.setAdditionalInformation(errorMsg);
        }

        metrics.endTrackingRpc("loadCourse", start);

        return response;
    }

    private CourseCollection convertCollectionFromDb(DbCourseCollection dbCourseCollection) {
        if (dbCourseCollection == null) {
            throw new IllegalArgumentException("The parameter 'dbCourseCollection' cannot be null.");
        }

        List<DataCollectionItem> experiments = new ArrayList<>();

        /* If there are entries, make sure they are sorted */
        final List<DbCourseCollectionEntry> collectionEntries = dbCourseCollection.getEntries();
        if (collectionEntries != null) {
            Collections.sort(collectionEntries, new Comparator<DbCourseCollectionEntry>() {

                @Override
                public int compare(DbCourseCollectionEntry o1, DbCourseCollectionEntry o2) {
                    return Integer.compare(o1.getPosition(), o2.getPosition());
                }
            });

            HibernateObjectReverter reverter = new HibernateObjectReverter(UMSDatabaseManager.getInstance());
            for (DbCourseCollectionEntry dbCollectionItem : collectionEntries) {
                DataCollectionItem experiment = reverter.convertExperiment(dbCollectionItem.getCourse());
                experiments.add(experiment);
            }
        }


        final String id = dbCourseCollection.getId();
        final String owner = dbCourseCollection.getOwner();
        final String name = dbCourseCollection.getName();
        final String description = dbCourseCollection.getDescription();
        return new CourseCollection(id, owner, name, description, experiments, ExperimentUrlManager.getCourseCollectionUrl(id));
    }

    @Override
    public CourseCollection createCourseCollection(String username, String name, String description)
            throws DetailedException {
        try {
            DbCourseCollection collection = UMSDatabaseManager.getInstance().createCourseCollection(name, username, description);
            return convertCollectionFromDb(collection);
        } catch (Throwable thrown) {
            final String message = "There was a problem creating the course collection '" + name + "'";
            logger.error(message, thrown);
            throw new DetailedException("Error creating course collection", message, thrown);
        }
    }

    @Override
    public CourseCollection getCourseCollection(String courseCollectionId) {
        DbCourseCollection dbCourseCollection = UMSDatabaseManager.getInstance().getCourseCollectionById(courseCollectionId,
                null);
        return convertCollectionFromDb(dbCourseCollection);
    }

    @Override
    public Collection<CourseCollection> getCourseCollectionsByUser(String username) {
        Collection<DbCourseCollection> dbCollections = UMSDatabaseManager.getInstance().getCourseCollectionsByUser(username);
        Collection<CourseCollection> collections = new ArrayList<>(dbCollections.size());
        for (DbCourseCollection dbCollection : dbCollections) {
            CourseCollection courseCollection = convertCollectionFromDb(dbCollection);
            collections.add(courseCollection);
        }

        return collections;
    }
    
    @Override
    public GenericRpcResponse<DomainOption> addCourseUserPermissionsFromFile(String filePath, DomainOption courseData, String browserSessionId) throws IllegalArgumentException{
        
        GenericRpcResponse<DomainOption> response = new GenericRpcResponse<>();
                
        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            
            if(logger.isTraceEnabled()){
                logger.trace("addCourseUserPermissionsFromFile called for course "+courseData.getSourceId()+" with user: " + userSession.getUserSessionInfo().getUserName() + " and file: " + filePath);
            }
            
            final ProgressIndicator progress = new ProgressIndicator(0, "Reading file...");
            usernameToCoursePermUpdateProgress.put(userSession.getUserSessionInfo().getUserName(), progress);
                        
            // parse the file
            File file = new File(DashboardProperties.getInstance().getDomainDirectory() + File.separator + filePath);
            Set<DomainOptionPermissions> permissions = new HashSet<>();
            try (Scanner parser = new Scanner(file)){
                
                //only handling CSV files for now, so assume all tokens are comma-delimited
                String DELIMITER = ",";
                
                int lineNum = 1;
                while(parser.hasNextLine()) {

                    String line = parser.nextLine();
                    String[] values = line.split(DELIMITER);
                    
                    if(values.length != 2){
                        // error - line is not formatted correctly
                        response.setWasSuccessful(false);
                        DetailedException detailedException = new DetailedException(
                                "There was a problem reading line "+lineNum+" in the file provided - "+filePath, 
                                "The format for user permissions should be 'username,{ViewCourse, TakeCourse, EditCourse}' but found '"+line+"'.", null);
                        DetailedExceptionSerializedWrapper exception = new DetailedExceptionSerializedWrapper(detailedException);
                        response.setException(exception);
                        return response;
                    }
                    
                    String username = values[0];
                    String permissionName = values[1];
                    
                    if(StringUtils.isBlank(username)){
                        // ignore empty username
                        continue;
                    }else if(courseData.isOwner(username)){
                        // can't change course owner permissions
                        continue;
                    }
                    
                    SharedCoursePermissionsEnum permType;
                    try{
                        permType = SharedCoursePermissionsEnum.valueOf(permissionName);
                    }catch(Exception e){
                        // error - unhandled permission type
                        response.setWasSuccessful(false);
                        DetailedException detailedException = new DetailedException(
                                "There was a problem reading line "+lineNum+" in the file provided - "+filePath, 
                                "The permission type of "+permissionName+" is not supported.  It must be one of {ViewCourse, TakeCourse, EditCourse}.", e);
                        DetailedExceptionSerializedWrapper exception = new DetailedExceptionSerializedWrapper(detailedException);
                        response.setException(exception);
                        return response;
                    }
                    
                    DomainOptionPermissions newPermissions = new DomainOptionPermissions(username, permType, false);
                    permissions.add(newPermissions);

                    lineNum++;
                    
                }//end while on lines from file   
                
                progress.setTaskDescription("Applying, please wait...");
                
                RpcResponse localResponse = updateCourseUserPermissions(permissions, courseData, browserSessionId, progress);
                if(!localResponse.isSuccess()){
                    // error - failed to update this current users permissions
                    response.setWasSuccessful(false);
                    String details = localResponse.getAdditionalInformation();
                    if(StringUtils.isBlank(details)){
                        details = "No additional information provided";
                    }
                    DetailedException detailedException = new DetailedException(localResponse.getResponse(), details, null);
                    DetailedExceptionSerializedWrapper exception = new DetailedExceptionSerializedWrapper(detailedException);
                    response.setException(exception);
                    return response;
                }else{
                    // update DomainOption that will be returned
                    
                    for(DomainOptionPermissions permission : permissions){
                        courseData.addDomainOptionPermissions(permission);
                    }
                }
                
                response.setWasSuccessful(true);
                response.setContent(courseData);
                
            } catch (FileNotFoundException e) {
                response.setWasSuccessful(false);
                DetailedException detailedException = new DetailedException(
                        "There was a problem reading the file "+filePath, 
                        "The file could not be found.", e);
                DetailedExceptionSerializedWrapper exception = new DetailedExceptionSerializedWrapper(detailedException);
                response.setException(exception);
            } finally{
                
                // clean up - no longer need the file (best effort)
                try{
                    FileUtils.deleteQuietly(file);
                }catch(@SuppressWarnings("unused") Throwable t){
                    // don't care, best effort
                }
                
                // this way the progress dialog goes away
                progress.setComplete(true);
            }
            
        } else {
            response.setWasSuccessful(false);
            DetailedException detailedException = new DetailedException(
                    "Unable to find the user session.\n\nHave you been idle for a long time?  Try logging in again.", 
                    "Unable to find user session for session id: " + browserSessionId, null);
            DetailedExceptionSerializedWrapper exception = new DetailedExceptionSerializedWrapper(detailedException);
            response.setException(exception);
        }

        return response;
    }
    
    @Override
    public GenericRpcResponse<DataCollectionItem> addPublishedCourseUserPermissionsFromFile(String filePath, DataCollectionItem dataCollectionItem, String browserSessionId) throws IllegalArgumentException{
        
        GenericRpcResponse<DataCollectionItem> response = new GenericRpcResponse<>();
        
        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            
            if(logger.isTraceEnabled()){
                logger.trace("addCourseUserPermissionsFromFile called for published course "+dataCollectionItem.getName()+" with user: " + userSession.getUserSessionInfo().getUserName() + " and file: " + filePath);
            }
            
            final ProgressIndicator progress = new ProgressIndicator(0, "Reading file...");
            usernameToCoursePermUpdateProgress.put(userSession.getUserSessionInfo().getUserName(), progress);
            
            Set<DataCollectionPermission> permissions = new HashSet<>();
                        
            // parse the file
            File file = new File(DashboardProperties.getInstance().getDomainDirectory() + File.separator + filePath);
            try (Scanner parser = new Scanner(file)){
                
                //only handling CSV files for now, so assume all tokens are comma-delimited
                String DELIMITER = ",";
                
                int lineNum = 1;
                while(parser.hasNextLine()) {

                    String line = parser.nextLine();
                    String[] values = line.split(DELIMITER);
                    
                    if(values.length != 2){
                        // error - line is not formatted correctly
                        response.setWasSuccessful(false);
                        DetailedException detailedException = new DetailedException(
                                "There was a problem reading line "+lineNum+" in the file provided - "+filePath, 
                                "The format for user permissions should be 'username,{OWNER, MANAGER, RESEARCHER}' but found '"+line+"'.", null);
                        DetailedExceptionSerializedWrapper exception = new DetailedExceptionSerializedWrapper(detailedException);
                        response.setException(exception);
                        return response;
                    }
                    
                    String username = values[0];
                    String permissionName = values[1];
                    
                    if(StringUtils.isBlank(username)){
                        // ignore empty username
                        continue;
                    }else if(dataCollectionItem.getAuthorUsername().equalsIgnoreCase(username)){
                        // can't change owner permissions
                        continue;
                    }
                    
                    DataCollectionUserRole permType;
                    try{
                        permType = DataCollectionUserRole.valueOf(permissionName);
                    }catch(Exception e){
                        // error - unhandled permission type
                        response.setWasSuccessful(false);
                        DetailedException detailedException = new DetailedException(
                                "There was a problem reading line "+lineNum+" in the file provided - "+filePath, 
                                "The permission type of "+permissionName+" is not supported.  It must be one of {OWNER, MANAGER, RESEARCHER}.", e);
                        DetailedExceptionSerializedWrapper exception = new DetailedExceptionSerializedWrapper(detailedException);
                        response.setException(exception);
                        return response;
                    }
                    
                    DataCollectionPermission newPermissions = new DataCollectionPermission(dataCollectionItem.getId(), username, permType);
                    permissions.add(newPermissions);
                    
                    lineNum++;
                    
                }//end while on lines from file
                
                progress.setTaskDescription("Applying, please wait...");
                
                RpcResponse localResponse = updatePublishedCourseUserPermissions(permissions, dataCollectionItem.getId(), browserSessionId);
                if(!localResponse.isSuccess()){
                    // error - failed to update this current users permissions
                    response.setWasSuccessful(false);
                    String details = localResponse.getAdditionalInformation();
                    if(StringUtils.isBlank(details)){
                        details = "No additional information provided";
                    }
                    DetailedException detailedException = new DetailedException(localResponse.getResponse(), details, null);
                    DetailedExceptionSerializedWrapper exception = new DetailedExceptionSerializedWrapper(detailedException);
                    response.setException(exception);
                    return response;
                }else{
                    // update DomainOption that will be returned
                    for(DataCollectionPermission permission : permissions){
                        dataCollectionItem.getPermissions().remove(permission);  // can't just call 'add' when wanting to replace
                        dataCollectionItem.getPermissions().add(permission);
                    }
                }
                
                response.setWasSuccessful(true);
                response.setContent(dataCollectionItem);
                
            } catch (FileNotFoundException e) {
                response.setWasSuccessful(false);
                DetailedException detailedException = new DetailedException(
                        "There was a problem reading the file "+filePath, 
                        "The file could not be found.", e);
                DetailedExceptionSerializedWrapper exception = new DetailedExceptionSerializedWrapper(detailedException);
                response.setException(exception);
            } finally{
                
                // clean up - no longer need the file (best effort)
                try{
                    FileUtils.deleteQuietly(file);
                }catch(@SuppressWarnings("unused") Throwable t){
                    // don't care, best effort
                }
                
                // this way the progress dialog goes away
                progress.setComplete(true);
            }
            
        } else {
            response.setWasSuccessful(false);
            DetailedException detailedException = new DetailedException(
                    "Unable to find the user session.\n\nHave you been idle for a long time?  Try logging in again.", 
                    "Unable to find user session for session id: " + browserSessionId, null);
            DetailedExceptionSerializedWrapper exception = new DetailedExceptionSerializedWrapper(detailedException);
            response.setException(exception);
        }

        return response;
    }


    @Override
    public GenericRpcResponse<DataCollectionItem> updateCourseCollection(AbstractCourseCollectionAction action) {
        final GenericRpcResponse<DataCollectionItem> response = new GenericRpcResponse<>();
        final String username = action.getUsername();
        final String collectionId = action.getCollectionId();
        final DataCollectionManager dcManager = DataCollectionManager.getInstance();

        try {
            if (action instanceof AddCourseAction) {
                AddCourseAction addAction = (AddCourseAction) action;
                final DataCollectionItem newCourse = addAction.getNewCourse();
                DataCollectionItem addedCourse = dcManager.addCourseToCollection(username, collectionId, newCourse);
                if (addedCourse != null) {
                    response.setContent(addedCourse);
                    response.setWasSuccessful(true);
                }
            } else if (action instanceof ReorderAction) {
                ReorderAction reorderAction = (ReorderAction) action;
                final int oldIndex = reorderAction.getOldIndex();
                final int newIndex = reorderAction.getNewIndex();
                final List<DataCollectionItem> currentOrdering = reorderAction.getCurrentOrdering();
                dcManager.reorderCourseInCollection(username, collectionId, oldIndex, newIndex, currentOrdering);
                response.setWasSuccessful(true);
            } else if (action instanceof EditCollectionProperties) {
                EditCollectionProperties editAction = (EditCollectionProperties) action;
                final String name = editAction.getName();
                final String description = editAction.getDescription();
                dcManager.editCourseCollectionMetadata(username, collectionId, name, description);
                response.setWasSuccessful(true);
            } else {
                final String errorMsg = "The action '" + action + "' is not implemented for updateCourseCollection";
                throw new DetailedException(errorMsg, errorMsg, null);
            }
        } catch (DetailedException ex) {
            response.setWasSuccessful(false);
            response.setException(new DetailedExceptionSerializedWrapper(ex));
        }

        return response;
    }

    @Override
    public Void deleteCourseCollection(String username, String courseCollectionId) {
        UMSDatabaseManager.getInstance().deleteCourseCollection(courseCollectionId);
        return null;
    }

    @Override
    public RpcResponse createLtiUserSession(String consumerKey, String consumerId) {
        logger.debug("createLtiUserSession() consumerKey(" + consumerKey + "), consumerId(" + consumerId + ")");
        RpcResponse response = new RpcResponse();
        long start = System.currentTimeMillis();

        //the address of the client were a use is trying to login to GIFT
        final String clientAddress = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());


        // Eventually we need to populate with the session information.
        UserWebSession userWebSession = userSessionManager.getLtiUserWebSession(consumerKey, consumerId);
        LtiUserSession userSession = userSessionManager.getLtiUser(consumerKey, consumerId);
        BrowserWebSession browserSession = null;

        // Eventually we need to populate with the session information.
        if (userWebSession == null) {
            userSession = new LtiUserSession(consumerKey, consumerId);

            // New user, so create a user session & browser session.
            userWebSession = userSessionManager.createUserSession(userSession, clientAddress);
        }

        // Create a new browser session.
        browserSession = userSessionManager.createBrowserSession(userWebSession,  clientAddress);

        if (userSession != null && browserSession != null) {
            logger.debug("createLtiUserSession() returning success: user(" + userSession.getUserSessionId() + "), browser("
                        + browserSession.getBrowserSessionKey() + ")");
            response.setIsSuccess(true);
            response.setBrowserSessionId(browserSession.getBrowserSessionKey());
            response.setUserSessionId(userSession.getUserSessionId());
        } else {
            logger.error("Unable to retrieve an lti user session for consumer key: " + consumerKey + ", consumerId: " + consumerId);
            response.setIsSuccess(false);
            response.setResponse("Unable to retrieve an lti user session.");
        }

        metrics.endTrackingRpc("getLtiUserSession", start);
        return response;
    }

    @Override
    public GenericRpcResponse<String> ltiGetCourseSourcePathFromId(String consumerKey, String consumerId, String courseId)
    {
        GenericRpcResponse<String> response = null;

        // Early return if the lti consumer map is not valid.
        if (consumerMap == null || consumerMap.isEmpty()) {

            logger.error("The lti consumer map was not loaded properly.  Ignoring the lti load course request.");

            response = new FailureResponse<>(new DetailedException(
                    "There was an error processing the LTI request.",
                    "The server is not configured properly to handle LTI requests.",
                    null
            ));

            return response;
        }

        long start = System.currentTimeMillis();
        TrustedLtiConsumer consumer = consumerMap.get(consumerKey);

        if (consumer != null) {

            Date now = new Date();
            Date timestamp = ServicesManager.getInstance().getDbServices().getTimestampForLtiUserRecord(consumerKey,  consumerId);

            if (timestamp != null) {

                long delta = now.getTime() - timestamp.getTime();
                if(logger.isDebugEnabled()){
                    logger.debug("ltiGetCourseSourcePathFromId() - Comparing the times:  delta=" + delta
                            + ", now=" + now.getTime() + ", db Time = " + timestamp.getTime());
                }
                // If a request comes in that is older than 10 seconds, then it is considered 'stale' or invalid.
                // This is an lti security measure to prevent stale/unauthorized access to the gift courses.
                if (delta > DashboardProperties.getInstance().getLtiTimeoutDashboardMs()) {
                    String errorMsg = "The request does not appear to be properly launched from the LTI Consumer.  Please try launching the course from the LTI Consumer.";
                    logger.error("ltiGetCourseSourcePathFromId() - The lti launch request failed because the timestamp did not meet the criteria:  delta=" + delta
                            + ", now=" + now.getTime() + ", db Time = " + timestamp.getTime());
                    response = new FailureResponse<>(new DetailedException(
                            errorMsg,
                            "The LTI launch did not meet the proper timestamp criteria.",
                            null
                    ));

                } else {
                    CourseRecord record = ServicesManager.getInstance().getDbServices().getCourseById(courseId, false);
                    if (record == null) {
                        String errorMsg = "A course could not be found with the id of '" + courseId + "'.  Please make sure the course_id parameter is valid.";
                        logger.error(errorMsg);
                        response = new FailureResponse<>(new DetailedException(
                                "An error occurred while processing the LTI launch request.",
                                errorMsg,
                                null
                        ));
                    } else {

                        // The course path needs to have a forward slash in server mode.
                        String coursePath = record.getCoursePath();
                        if (DashboardProperties.getInstance().isServerDeploymentMode()) {
                            coursePath = record.getCoursePath();
                        }

                        response = new SuccessfulResponse<>(coursePath);
                    }
                }
            } else {

                response = new FailureResponse<>(new DetailedException(
                        "The request does not have the proper LTI launch parameters.",
                        "The LTI launch request could not find the incoming LTI user.",
                        null
                ));
            }

        } else {

            response = new FailureResponse<>(new DetailedException(
                    "The request does not have the proper LTI launch parameters.",
                    "The LTI launch request has an unsupported LTI Tool Consumer key.",
                    null
            ));
        }

        metrics.endTrackingRpc("ltiGetCourseSourcePathFromId", start);

        return response;

    }

    @Override
    public GenericRpcResponse<String> ltiLoadCourse(String consumerKey, String consumerId, String courseSourcePath)
    {
        GenericRpcResponse<String> response = null;

        // Early return if the lti consumer map is not valid.
        if (consumerMap == null || consumerMap.isEmpty()) {

            logger.error("The lti consumer map was not loaded properly.  Ignoring the lti load course request.");

            response = new FailureResponse<>(new DetailedException(
                    "There was an error processing the LTI request.",
                    "The server is not configured properly to handle LTI requests.",
                    null
            ));

            return response;
        }

        long start = System.currentTimeMillis();
        TrustedLtiConsumer consumer = consumerMap.get(consumerKey);

        if (consumer != null) {

            Date now = new Date();
            Date timestamp = ServicesManager.getInstance().getDbServices().getTimestampForLtiUserRecord(consumerKey,  consumerId);

            if (timestamp != null) {

                long delta = now.getTime() - timestamp.getTime();
                if(logger.isDebugEnabled()){
                    logger.debug("ltiLoadCourse() - Comparing the times:  delta=" + delta
                            + ", now=" + now.getTime() + ", db Time = " + timestamp.getTime());
                }
                // If a request comes in that is older than 10 seconds, then it is considered 'stale' or invalid.
                // This is an lti security measure to prevent stale/unauthorized access to the gift courses.
                if (delta > DashboardProperties.getInstance().getLtiTimeoutDashboardMs()) {
                    String errorMsg = "The request does not appear to be properly launched from the LTI Consumer.  Please try launching the course from the LTI Consumer.";
                    logger.error("ltiLoadCourse() - The lti launch request failed because the timestamp did not meet the criteria:  delta=" + delta
                            + ", now=" + now.getTime() + ", db Time = " + timestamp.getTime());
                    response = new FailureResponse<>(new DetailedException(
                            errorMsg,
                            "The LTI launch did not meet the proper timestamp criteria.",
                            null
                    ));

                } else {
                    String uniqueLtiUsername = "";
                    try {
                        uniqueLtiUsername = TrustedLtiConsumer.getInternalUniqueConsumerId(consumer, consumerId);
                        String runtimeFolderName = TrustedLtiConsumer.createUniqueRuntimeFolderName(consumer);
                        String runtimeCourseId = LoadCourseManager.getInstance().startLtiCourseLoad(uniqueLtiUsername, courseSourcePath, runtimeFolderName);

                        // Embed the course id into the rpc response.
                        response = new SuccessfulResponse<>(runtimeCourseId);

                        logger.debug("ltiLoadCourse - ltiLoadCourse returned success with runtime course id: " + runtimeCourseId);

                    } catch (IllegalArgumentException e) {
                        String errorMsg = "IllegalArgumentException caught while trying to load course for consumer (" + consumerId + "), and course (" + courseSourcePath + ").";
                        logger.error("ltiLoadCourse - " + errorMsg, e);

                        response = new FailureResponse<>(new DetailedException(
                                "An error occurred while processing the LTI launch request.",
                                errorMsg,
                                e
                        ));

                    } catch (IOException e) {
                        String errorMsg = "IOException caught while trying to load course for consumer (" + consumerId + "), and course (" + courseSourcePath + ").";
                        logger.error("ltiLoadCourse - " + errorMsg, e);

                        response = new FailureResponse<>(new DetailedException(
                                "An error occurred while processing the LTI launch request.",
                                errorMsg,
                                e
                        ));

                    } catch (DetailedException e) {
                        String errorMsg = "DetailedException caught while trying to load course for consumer (" + consumerId + "), and course (" + courseSourcePath + ").";
                        logger.error("ltiLoadCourse - " + errorMsg, e);

                        response = new FailureResponse<>(new DetailedException(
                                "An error occurred while processing the LTI launch request.",
                                errorMsg,
                                e
                        ));

                    } catch (Exception e) {
                        String errorMsg = "Exception caught while trying to load course for consumer (" + consumerId + "), and course (" + courseSourcePath + ")."
                                        + "  Please make sure the path to the course is setup correctly in the LTI Tool Consumer.  If you are a student, you may need"
                                        + " to have the instructor verify that the course is correct.";
                        logger.error("ltiLoadCourse - " + errorMsg, e);

                        response = new FailureResponse<>(new DetailedException(
                                "An error occurred while processing the LTI launch request.",
                                errorMsg,
                                e
                        ));
                    }
                }
            } else {

                response = new FailureResponse<>(new DetailedException(
                        "The request does not have the proper LTI launch parameters.",
                        "The LTI launch request could not find the incoming LTI user.",
                        null
                ));
            }

        } else {

            response = new FailureResponse<>(new DetailedException(
                    "The request does not have the proper LTI launch parameters.",
                    "The LTI launch request has an unsupported LTI Tool Consumer key.",
                    null
            ));
        }

        metrics.endTrackingRpc("ltiLoadCourse", start);

        return response;

    }

    @Override
    public LoadExperimentResponse loadExperiment(String experimentId, String progressIndicatorId) {
        long start = System.currentTimeMillis();
        LoadExperimentResponse response = null;

        try {
            DataCollectionItem experiment = DataCollectionManager.getInstance().getDataCollectionItem(experimentId);

            if (experiment == null) {
                String errorMsg = "An error occurred while processing the Experiment request.";
                String errorDetails = "An experiment was not found with id '" + experimentId + "'.";
                logger.error("loadExperiment - " + errorMsg);

                response = new LoadExperimentResponse(errorMsg, errorDetails, null);
                metrics.endTrackingRpc("loadExperiment", start);
                return response;
            }

            String sourceCoursePath;
            if (experiment.isLegacyExperiment()) {
                /* Experiment is in the legacy folder so the source is that
                 * folder */
                sourceCoursePath = experiment.getCourseFolder();
            } else {
                CourseRecord sourceCourse = ServicesManager.getInstance().getDbServices()
                        .getCourseById(experiment.getSourceCourseId(), false);
                sourceCoursePath = sourceCourse.getCoursePath();
            }

            String runtimeCourseId = LoadCourseManager.getInstance().startExperimentLoad(experiment, sourceCoursePath,
                    progressIndicatorId);

            if (logger.isWarnEnabled() && StringUtils.isBlank(runtimeCourseId)) {
                logger.warn("Runtime course id was blank after startExperimentLoad.");
            }

            /* Embed the runtime and source course id into the rpc response. */
            response = new LoadExperimentResponse(runtimeCourseId, sourceCoursePath);
        } catch (Exception e) {
            String errorMsg = "An error occurred while processing the Experiment request.";
            String errorDetails = "Exception caught while trying to load experiment (" + experimentId + ")";
            logger.error("loadExperiment - " + errorMsg, e);

            response = new LoadExperimentResponse(errorMsg, errorDetails, e);
        }

        metrics.endTrackingRpc("loadExperiment", start);
        return response;
    }

    @Override
    public DeleteCourseResult deleteCourses(String browserSessionId, List<DomainOption> courses, boolean deleteSurveyResponses, boolean skipSurveyResources) {
        long start = System.currentTimeMillis();

        DeleteCourseResult response = new DeleteCourseResult();
        ProgressIndicator progress = new ProgressIndicator(0, "Initializing request...");
        AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            DashboardHttpSessionData userData = userSession.getUserSessionInfo();

            response.setUserSessionId(userSession.getUserSessionKey());

            String username = userData.getUserName();

            usernameToDeleteProgress.put(username, progress);

            deleteCourses(courses, username, browserSessionId, deleteSurveyResponses, 
                    skipSurveyResources, response, progress, fileServices);


            usernameToDeleteProgress.remove(username);

        } else {
            response.setIsSuccess(false);
            response.setResponse("Unable to find the user session.\n\nHave you been idle for a long time?  Try logging in again.");
            response.setAdditionalInformation("Unable to find user session for session id: " + browserSessionId);
        }

        metrics.endTrackingRpc("deleteCourse", start);
        return response;
    }

    /**
     * Attempts to copy the specified course to the users workspace.
     *
     * @param userSession - The user session from the client.  Used to look up the username.
     * @param newCourseName - The name to give the copied course.  This is useful in case the course is being copied to the
     * same workspace which would cause a course name collision.
     * @param courseData - The domain option (course data) to copy.  This contains the location of the course being copied.
     * @param coursesList - The list of courses available to the user.  This is used to check for course name conflicts.  If empty
     * than course name collisions are not checked for and the course being copied could be merged with another course with the same name.
     * @param toPublicWorkspace whether the course is being copied to the Public workspace.  If true the provider user will need to have
     * write access to that workspace.
     * @param editableToUsernames a set of GIFT usernames to give write access to in the survey context (and descendant element in the survey context).</br>
     * If toPublicWorkspace is true and this set is not empty, the set is used.</br>
     * If toPublicWorkspace is true and this set is null or empty, no user will have write access.</br>
     * If toPublicWorkspace is false and this set is not empty, the set is used.</br>
     * If toPublicWorkspace is false and this set is null or empty, the username found in the userSession is used as the only user with read access.
     * @param visibleToUsernames a set of GIFT usernames to give read access to in the survey context (and descendant elements in the survey context).</br>
     * If toPublicWorkspace is true than this parameter is ignored and a wildcard is used to everyone has read access.</br>
     * (when toPublicWorkspace is false) If this set is null or empty, the username found in the userSession is used as the only user with read access.
     * @return the results of the rpc.  Use {@link #getCopyProgress(String)} with to get the progress of this course copy operation
     * including whether a course name collision happened.
     */
    private DetailedRpcResponse copyCourse(final DashboardHttpSessionData userSession, final String browserSessionId, final String newCourseName, final DomainOption courseData,
            final Collection<DomainOption> coursesList, final boolean toPublicWorkspace, HashSet<String> editableToUsernames, HashSet<String> visibleToUsernames){


        long start = System.currentTimeMillis();
        if(logger.isDebugEnabled()){
            logger.debug("starting copy course on "+courseData+ " for "+userSession);
        }
        DetailedRpcResponse copyResponse = new DetailedRpcResponse();

        LoadedProgressIndicator<CopyCourseResult> currentOp =  usernameToCopyProgress.get(userSession.getUserName());
        if (currentOp != null && currentOp.getPayload() == null) {
            // This means there is a current copy operation in progress for the user that has not completed.
            copyResponse.setUserSessionId(userSession.getUserSessionId());
            copyResponse.setBrowserSessionId(browserSessionId);
            copyResponse.setIsSuccess(false);
            copyResponse.setErrorMessage("Error starting the copy operation.");
            String errorMsg = "Current copy operation in progress.  Please wait for the current copy operation to finish before copying another course.";
            copyResponse.setErrorDetails("There is currently a copy operation in progress.\n\nPlease wait for the operation to finish and try again.");
            copyResponse.setAdditionalInformation(errorMsg);

            logger.error("Start copy operation failed for user: " + userSession.getUserName() + ", because a current copy operation is in progress.");

        } else {
            String validationMsg = DocumentUtil.validateFileName(newCourseName);
            if(validationMsg != null) {
                // This means the newCourseName is invalid
                copyResponse.setUserSessionId(userSession.getUserSessionId());
                copyResponse.setBrowserSessionId(browserSessionId);
                copyResponse.setIsSuccess(false);
                copyResponse.setErrorMessage("Error starting the copy operation.");
                copyResponse.setErrorDetails("The course name was invalid.\n\nPlease fix the name of the course and try again.");
                copyResponse.setAdditionalInformation(validationMsg);

                logger.error("Start copy operation failed for user: " + userSession.getUserName() + ", because the course name was invalid.");
            }else{
                //removing any previous copy progress now instead of when that previous copied finished
                //in order to give any logic watching that progress enough time to show the final progress value
                usernameToCopyProgress.remove(userSession.getUserName());

                final LoadedProgressIndicator<CopyCourseResult> progResponse = new LoadedProgressIndicator<>();
                final String username = userSession.getUserName();
                usernameToCopyProgress.put(username, progResponse);

                //
                // Release the calling thread and make this copy course operation asynchronous
                //
                Runnable copyOperation = new Runnable() {
                    @Override
                    public void run(){
                        long copyStart = System.currentTimeMillis();
                        copyCourse(courseData, username, userSession.getUserSessionId(), browserSessionId, newCourseName, editableToUsernames, visibleToUsernames,
                                toPublicWorkspace, coursesList, progResponse);
                        metrics.endTrackingRpc("copyCourseThread", copyStart);
                    }
                };

                AsyncOperationManager.getInstance().startAsyncOperation("copyCourseThread-" + userSession.getUserSessionId(),  copyOperation);
                copyResponse.setIsSuccess(true);
            }
        }

        if(logger.isDebugEnabled()){
            logger.debug("copyCourse - end");
        }
        metrics.endTrackingRpc("copyCourse", start);

        return copyResponse;
    }

    @Override
    public DetailedRpcResponse copyCourse(final String browserSessionId, final String courseName, final DomainOption courseData, final List<DomainOption> coursesList) {


        
        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        
        if (userSession != null) {
            final DashboardHttpSessionData userData = userSession.getUserSessionInfo();

            return copyCourse(userData, browserSessionId, courseName, courseData, coursesList, false, null, null);

        }else{

            DetailedRpcResponse copyResponse = new DetailedRpcResponse();
            copyResponse.setIsSuccess(false);
            String errorMsg = "Unable to find user session for session id: " + browserSessionId;
            logger.error("copyCourse - " + errorMsg);
            copyResponse.setErrorDetails("Unable to find the user session.\n\nHave you been idle for a long time?  Try logging in again.");
            copyResponse.setAdditionalInformation(errorMsg);

            return copyResponse;
        }
    }

    /**
     * Returns the one and only progress indicator for an ongoing copy course.
     *
     * @param userName used to look up the progress object for a copy course operation
     * @return the progress object for a copy course operation for the specified user.  If there isn't an active copy course
     * operation an object is still returned with the success value set to false and a response message indicating why.
     */
    public LoadedProgressIndicator<CopyCourseResult> getCopyProgressByUserName(String userName) {

        LoadedProgressIndicator<CopyCourseResult> progResponse = usernameToCopyProgress.get(userName);

        if(logger.isInfoEnabled()){
            logger.info("getCopyProgress progResponse = " + progResponse);
        }

        if (progResponse != null) {
            return progResponse;

        } else {

            LoadedProgressIndicator<CopyCourseResult> response = new LoadedProgressIndicator<>();

            CopyCourseResult result = new CopyCourseResult();
            result.setResponse("Unable to find a progress for user : " + userName);

            response.setPayload(result);

            return response;
        }

    }

    @Override
    public LoadedProgressIndicator<CopyCourseResult> getCopyProgress(String browserSessionId) {
        long start = System.currentTimeMillis();

        if(logger.isInfoEnabled()){
            logger.info("getCopyProgress called: " + browserSessionId);
        }

        LoadedProgressIndicator<CopyCourseResult> response = null;

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            DashboardHttpSessionData userData = userSession.getUserSessionInfo();

            response = getCopyProgressByUserName(userData.getUserName());

        }else{

            response = new LoadedProgressIndicator<>();

            CopyCourseResult result = new CopyCourseResult();
            result.setUserSessionId("");
            result.setBrowserSessionId(browserSessionId);
            result.setResponse("Unable to find user session for session id: " + browserSessionId);

            response.setPayload(result);
        }

        metrics.endTrackingRpc("getCopyProgress", start);

        return response;

    }

    @Override
    public ProgressResponse getDeleteProgress(String browserSessionId) {
        long start = System.currentTimeMillis();

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            ProgressIndicator progress = usernameToDeleteProgress.get(userData.getUserName());

            if(progress != null) {

                metrics.endTrackingRpc("getDeleteProgress", start);
                return new ProgressResponse(true, null, progress);
            } else {

                metrics.endTrackingRpc("getDeleteProgress", start);
                return new ProgressResponse(false, null, progress);
            }
        }

        metrics.endTrackingRpc("getDeleteProgress", start);
        return new ProgressResponse(false, "Unable to find user session for session id: " + browserSessionId, null);
    }

    @Override
    public ProgressResponse getCourseListProgress(String browserSessionId){

        long start = System.currentTimeMillis();

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {
            DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            ProgressIndicator progress = usernameToGetCourseListProgress.get(userData.getUserName());

            if(progress != null) {

                metrics.endTrackingRpc("getCourseListProgress", start);
                return new ProgressResponse(true, null, progress);
            } else {

                metrics.endTrackingRpc("getCourseListProgress", start);
                return new ProgressResponse(false, null, progress);
            }
        }

        metrics.endTrackingRpc("getCourseListProgress", start);
        return new ProgressResponse(false, "Unable to find user session for session id: " + browserSessionId, null);
    }

    @Override
    public ProgressResponse getLoadCourseProgress(String browserSessionId, String courseId) {
        long start = System.currentTimeMillis();
        ProgressResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getLoadCourseProgress - called with browserSessionId: " + browserSessionId);
        }


        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {

            DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            if (userData != null) {

                AsyncReturnBlocker<ProgressIndicator> blocker = new AsyncReturnBlocker<>();
                AtomicBoolean isRunning = new AtomicBoolean(true);
                startProgressFetchThread(userData.getUserName(), courseId, blocker, isRunning);
                ProgressIndicator progress = blocker.getReturnValueOrTimeout();
                isRunning.set(false);

                if(progress != null){
                    response = new ProgressResponse(true, null, progress);

                } else {
                    response = new ProgressResponse(false, "Could not find a load in progress for this user. ", null);

                    if(logger.isInfoEnabled()){
                        logger.info("getLoadCourseProgress - Could not find a load in progress for user: " + userSession);
                    }
                }

            } else {
                String errorMsg = "Unable to find usersession for session id: " + browserSessionId;
                logger.error("getLoadCourseProgress - " + errorMsg);
                response = new ProgressResponse(false, errorMsg, null);
            }
        } else {
            String errorMsg = "Unable to find user session for session id: " + browserSessionId;
            logger.error("getLoadCourseProgress - " + errorMsg);
            response = new ProgressResponse(false, errorMsg, null);
        }

        metrics.endTrackingRpc("getLoadCourseProgress", start);
        return response;
    }

    @Override
    public ProgressResponse getValidateCourseProgress(String browserSessionId, String courseId) {
        long start = System.currentTimeMillis();
        ProgressResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getValidateCourseProgress - called with browserSessionId: " + browserSessionId);
        }

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {

            DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            if (userData != null) {

                ProgressIndicator progress = usernameToValidateProgress.get(userData.getUserName());

                if(progress != null){
                    response = new ProgressResponse(true, null, progress);

                } else {
                    response = new ProgressResponse(false, "Could not find a course validate in progress for this user. ", null);

                    if(logger.isInfoEnabled()){
                        logger.info("getValidateCourseProgress - Could not find a course validate in progress for user: " + userSession);
                    }
                }

            } else {
                String errorMsg = "Unable to find usersession for session id: " + browserSessionId;
                logger.error("getLoadCourseProgress - " + errorMsg);
                response = new ProgressResponse(false, errorMsg, null);
            }
        } else {
            String errorMsg = "Unable to find user session for session id: " + browserSessionId;
            logger.error("getValidateCourseProgress - " + errorMsg);
            response = new ProgressResponse(false, errorMsg, null);
        }

        metrics.endTrackingRpc("getValidateCourseProgress", start);
        return response;
    }

    @Override
    public ProgressResponse getLoadCourseProgress(String consumerKey, String consumerId, String courseId) {
        long start = System.currentTimeMillis();
        ProgressResponse response = null;

        TrustedLtiConsumer consumer = consumerMap.get(consumerKey);

        if (consumer != null) {
            String uniqueConsumerId = TrustedLtiConsumer.getInternalUniqueConsumerId(consumer, consumerId);

            AsyncReturnBlocker<ProgressIndicator> blocker = new AsyncReturnBlocker<>();
            AtomicBoolean isRunning = new AtomicBoolean(true);
            startProgressFetchThread(uniqueConsumerId, courseId, blocker, isRunning);
            ProgressIndicator progress = blocker.getReturnValueOrTimeout();
            isRunning.set(false);

            if(progress != null){
                response = new ProgressResponse(true, null, progress);

            } else {
                logger.error("getLoadCourseProgress - Could not find a load in progress for lti user: " + uniqueConsumerId);
                response = new ProgressResponse(false, "Could not find a load in progress for this lti user. ", null);
            }
        } else {
            response = new ProgressResponse(false, "Could not find a consumer for the getLoadCourseProgress request. ", null);
            logger.error("Could not find a consumer for the getLoadCourseProgress request with key: " + consumerKey);
        }


        metrics.endTrackingRpc("getLoadCourseProgress", start);
        return response;
    }

    @Override
    public ProgressResponse getLoadCourseProgress(String experimentProgressId) {
        long start = System.currentTimeMillis();
        ProgressResponse response = null;

        AsyncReturnBlocker<ProgressIndicator> blocker = new AsyncReturnBlocker<>();
        AtomicBoolean isRunning = new AtomicBoolean(true);
        startProgressFetchThread(null, experimentProgressId, blocker, isRunning);
        ProgressIndicator progress = blocker.getReturnValueOrTimeout();
        isRunning.set(false);

        if (progress != null) {
            response = new ProgressResponse(true, null, progress);

        } else {
            logger.error("Could not find a load in progress for experiment progress id: " + experimentProgressId);
            response = new ProgressResponse(false, "Could not find a load in progress.", null);
        }

        metrics.endTrackingRpc("getLoadCourseProgress", start);
        return response;
    }

    /**
     * Creates a thread that will repeatedly check for a
     * {@link ProgressIndicator} and push it into a {@link AsyncReturnBlocker}
     * when available.
     *
     * @param username The name of the user for which the
     *        {@link ProgressIndicator} is being fetched. Can be null if the
     *        {@link ProgressIndicator} that is being fetched is not associated
     *        with a username (e.g. experiment load progress).
     * @param courseId The id of the course that is being loaded.
     * @param blocker The {@link AsyncReturnBlocker} that the
     *        {@link ProgressIndicator} is pushed into once it is available.
     *        Can't be null.
     * @param isRunning The {@link AtomicBoolean} that is used to signal to the
     *        thread that it should terminate early if the
     *        {@link ProgressIndicator} doesn't become available. Can't be null.
     */
    private void startProgressFetchThread(String username, String courseId, AsyncReturnBlocker<ProgressIndicator> blocker, AtomicBoolean isRunning) {
        if (blocker == null) {
            throw new IllegalArgumentException("The parameter 'blocker' cannot be null.");
        } else if (isRunning == null) {
            throw new IllegalArgumentException("The parameter 'isRunning' cannot be null.");
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ProgressIndicator progress;
                    do {
                        /* If the signal has been sent to stop running, then
                         * stop */
                        if (!isRunning.get()) {
                            return;
                        }

                        /* See if the progress is available in the
                         * LoadCourseManager yet. If it is, break the loop. */
                        progress = LoadCourseManager.getInstance().getCourseLoadProgress(username, courseId);
                        if (progress != null) {
                            break;
                        }

                        /* Wait a little bit before trying again */
                        Thread.sleep(1000);
                    } while (true);

                    blocker.setReturnValue(progress);
                } catch (InterruptedException interruptEx) {
                    final String errMsg = String.format("There was an exception that interrupted the thread");
                    logger.error(errMsg, interruptEx);
                }
            }
        }).start();
    }

    @Override
    public RpcResponse cancelLoadCourse(String browserSessionId, String courseId) {
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("cancelLoadCourse - called with browserSessionId: " + browserSessionId);
        }

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {

            DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            LoadCourseManager.getInstance().cancelLoadCourse(userData.getUserName(), courseId);

            response = new RpcResponse(null, null, true, null);

            if(logger.isDebugEnabled()){
                logger.debug("cancelLoadCourse - success for user: " + userData.getUserName() + ", course: " + courseId);
            }

        } else {
            String errorMsg = "Unable to find user session for session id: " + browserSessionId;
            logger.error("cancelLoadCourse - " + errorMsg);
            response = new RpcResponse(null, null, false, errorMsg);
        }

        metrics.endTrackingRpc("cancelLoadCourse", start);
        return response;
    }

    /**
     * Return whether the cached version of a domain option should be used instead of retrieving a new,
     * updated instance of the course.
     *
     * @param entry contains information about the cached domain option including a timestamp of when it was last validated
     * @param current the current data about the domain option being checked.  This is used to determine if the metadata for the
     * course has changed.
     * @param now the current system time
     * @return true if the cached domain option should be used instead of retrieving a new instance of it
     */
    private boolean shouldUseCourseValidationCacheValue(final CourseValidationEntry entry, final DomainOption current, long now) {
        // If validated use regular cache expiration, otherwise use the invalid course cache expiration
        long timeSinceValidation = now - entry.lastValidated;
        long cacheDurationLimit = entry.validated ? courseValidationCacheExpiration : invalidCourseCacheExpiration;

        return timeSinceValidation < cacheDurationLimit && entry.course.equals(current);
    }

    @Override
    public ValidateCourseResponse validateCourseData(String browserSessionId, DomainOption courseData, CourseValidationParams courseValidationParams) {
        ValidateCourseResponse response = null;

        long start = System.currentTimeMillis();
        if(logger.isTraceEnabled()){
            logger.trace("validateCourseData - called for browser: " + browserSessionId + ", trying to validate course: " + courseData);
        }

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (courseData != null && userSession != null) {

            //handle any courses that are deemed invalid prior to this call, e.g. the course failed to parse when retrieving the
            //collection of all available courses
            if(courseData.getDomainOptionRecommendation() != null){
                DomainOptionRecommendationEnum recommendationEnum = courseData.getDomainOptionRecommendation().getDomainOptionRecommendationEnum();
                if(recommendationEnum.isUnavailableType()){
                    response = new ValidateCourseResponse(browserSessionId, null, false, "Did not perform course validation because the course recommendation value is an unavailable type", courseData);
                    return response;
                }
            }

            DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            if (userData != null) {
                String userName = userData.getUserName();

                //store the progress indicator to retrieve in a separate rpc call by the client
                ProgressIndicator progressIndicator = new ProgressIndicator(0, AbstractFileServices.VALIDATE_COURSE_PROGRESS_DESC);
                usernameToValidateProgress.put(userName, progressIndicator);

                CourseOptionsWrapper courseWrapper = new CourseOptionsWrapper();

                //whether the course folder has changed since the last successful validation
                boolean hasCourseChanged = false;
                if(courseValidationParams == null ||
                        courseValidationParams != null && courseValidationParams.isAllowCourseLogicValidation() && !courseValidationParams.isForceCourseLogicValidation()){
                    //only check if the default validation logic is requested (i.e. null params) or course validation is allowed but not forced [if forced there would be no need to check this timestamp]
                    hasCourseChanged = ServicesManager.getInstance().getFileServices().hasCourseChangedSinceLastValidation(courseData.getDomainId(), userName);
                }

                boolean hasSurveyContextChanged = false;
                if(courseValidationParams == null ||
                        courseValidationParams != null && courseValidationParams.isAllowSurveyValidation() && !courseValidationParams.isForceSurveyValidation()){
                    //only check if the default validation logic is requested (i.e. null params) or survey validation is allowed but not forced [if forced there would be no need to check this timestamp]
                    Date lastSuccessfulValidation = ServicesManager.getInstance().getFileServices().getCourseLastSuccessfulValidationDate(courseData.getDomainId(), userName);
                    hasSurveyContextChanged = ServicesManager.getInstance().getDbServices().hasSurveyContextChangeSinceLastValidation(lastSuccessfulValidation, courseData.getSurveyContextId(), userName);
                }

                CourseValidationEntry validationEntry = validationCache.get(courseData.getDomainId());

                //don't check cache if being forced to validate
                if (!(courseValidationParams != null && (courseValidationParams.isForceCourseLogicValidation() || courseValidationParams.isForceSurveyValidation())) &&
                        validationEntry != null && !hasCourseChanged &&
                        shouldUseCourseValidationCacheValue(validationEntry, courseData, start)) {
                    //use the cached instance for validation

                    //since course validation isn't happening, artifically set to 100 for anyone watching this progress indicator
                    progressIndicator.setPercentComplete(100);

                    if(DashboardProperties.getInstance().shouldApplyLMSRecordsAtCourseListRequest() && validationEntry.course != null){
                        //still provide LMS recommendations even if validation cached
                        try {
                            ServicesManager.getInstance().getDbServices().applyLMSRecommendations(userName, validationEntry.course);
                        } catch (LmsIoException e) {
                            logger.error("validateCourseData - ",e);
                            String errorMsg = "An LmsIoException occurred while trying to get course data for course(" + courseData
                                    + "). There is probably an issue connecting to the LMS; check your connection properties. Message: "
                                    + e.getMessage();                            response = new ValidateCourseResponse(null, null, false, errorMsg, null);
                        } catch (LmsInvalidStudentIdException e) {
                            logger.error("validateCourseData - ",e);
                            String errorMsg = "An LmsInvalidStudentIdException occurred while trying to getcourse data for course(" + courseData + "): " + e.getMessage();
                            response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                        } catch (LmsInvalidCourseRecordException e) {
                            logger.error("validateCourseData - ",e);
                            String errorMsg = "An LmsInvalidCourseRecordException occurred while trying to getcourse data for course(" + courseData + "): " + e.getMessage();
                            response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                        } catch (UMSDatabaseException e) {
                            logger.error("validateCourseData - ",e);
                            String errorMsg = "A UMSDatabaseException occurred while trying to getcourse data for course(" + courseData + "): " + e.getMessage();
                            response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                        } catch (Throwable e) {
                            logger.error("validateCourseData - ",e);
                            String errorMsg = "An exception occurred while trying to get course data for course(" + courseData + "): " + e.getMessage();
                            response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                        }
                    }

                    response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, validationEntry.validated, null, validationEntry.course);
                } else {
                    boolean success = false;
                    DomainOption domainItem = null;
                    try {

                        boolean validateCourse = hasCourseChanged;
                        if(!validateCourse && DashboardProperties.getInstance().shouldValidateCourseContentAtCourseListRequest()){
                            //over-ride the validate course property when the course hasn't changed
                            validateCourse = true;
                        }

                        if(!validateCourse && courseValidationParams != null && courseValidationParams.isAllowCourseLogicValidation() && courseValidationParams.isForceCourseLogicValidation()){
                            //forcing course validation no matter the last modified time stamp logic
                            validateCourse = true;
                        }

                        boolean validateSurvey = hasSurveyContextChanged;
                        if(!validateSurvey && DashboardProperties.getInstance().shouldValidateSurveyAtCourseListRequest()){
                            //over-ride the validate survey property when the course hasn't changed
                            validateSurvey = true;
                        }

                        if(!validateSurvey && courseValidationParams != null && courseValidationParams.isAllowSurveyValidation() && courseValidationParams.isForceSurveyValidation()){
                            //forcing survey validation no matter the last modified time stamp logic
                            validateSurvey = true;
                        }

                        // This can (based on properties) validate the courses, retrieve the LMS records for the course and
                        // apply labeling based on LMS records (e.g. recommend, refresh).
                        ServicesManager.getInstance().getFileServices().getCourse(userName, courseData.getDomainId(), courseWrapper,
                                validateCourse,
                                validateSurvey,
                                true,
                                progressIndicator);
                        
                        if(DashboardProperties.getInstance().shouldApplyLMSRecordsAtCourseListRequest()){
                            ServicesManager.getInstance().getDbServices().applyLMSRecommendations(userName, 
                                    courseWrapper.domainOptions.get(courseData.getDomainId()));
                        }

                        //finished validation, make sure at 100%.
                        progressIndicator.setPercentComplete(100);

                        Iterator<Map.Entry<String, DomainOption>> it = courseWrapper.domainOptions.entrySet().iterator();

                        // We are only expecting one item returned here.
                        if (it.hasNext()) {
                            Map.Entry<String, DomainOption> entry = it.next();
                            domainItem = entry.getValue();
                            success = true;
                            response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, true, null, domainItem);

                            if(logger.isTraceEnabled()){
                                logger.trace("validateCourseData - returned success with domain item of: " + domainItem);
                            }

                        } else {
                            String errorMsg = "The getCourse request did not return any domainOption data.";
                            logger.error("validateCourseData - " + errorMsg);

                            response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                        }

                    } catch (IllegalArgumentException e) {
                        logger.error("validateCourseData - ",e);
                        String errorMsg = "An IllegalArgumentException occurred while trying to getcourse data for course(" + courseData + "): " + e.getMessage();
                        response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                    } catch (FileNotFoundException e) {
                        logger.error("validateCourseData - ",e);
                        String errorMsg = "A FileNotFoundException occurred while trying to getcourse data for course(" + courseData + "): " + e.getMessage();
                        response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                    } catch (SurveyValidationException e) {
                        logger.error("validateCourseData - ",e);
                        String errorMsg = "A SurveyValidationException occurred while trying to getcourse data for course(" + courseData + "): " + e.getMessage();
                        response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                    } catch (LmsIoException e) {
                        logger.error("validateCourseData - ",e);
                        String errorMsg = "An LmsIoException occurred while trying to get course data for course(" + courseData
                                + "). There is probably an issue connecting to the LMS; check your connection properties. Message: " + e.getMessage();
                        response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                    } catch (LmsInvalidStudentIdException e) {
                        logger.error("validateCourseData - ",e);
                        String errorMsg = "An LmsInvalidStudentIdException occurred while trying to getcourse data for course(" + courseData + "): " + e.getMessage();
                        response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                    } catch (LmsInvalidCourseRecordException e) {
                        logger.error("validateCourseData - ",e);
                        String errorMsg = "An LmsInvalidCourseRecordException occurred while trying to getcourse data for course(" + courseData + "): " + e.getMessage();
                        response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                    } catch (UMSDatabaseException e) {
                        logger.error("validateCourseData - ",e);
                        String errorMsg = "A UMSDatabaseException occurred while trying to getcourse data for course(" + courseData + "): " + e.getMessage();
                        response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                    } catch (Throwable e) {
                        logger.error("validateCourseData - ",e);
                        String errorMsg = "An exception occurred while trying to get course data for course(" + courseData + "): " + e.getMessage();
                        response = new ValidateCourseResponse(userSession.getUserSessionKey(), browserSessionId, false, errorMsg, null);
                    }

                    if(domainItem != null){
                        validationCache.put(courseData.getDomainId(), new CourseValidationEntry(success, domainItem, System.currentTimeMillis()));
                    }
                }

            } else {
                String errorMsg = "Unable to find usersession for session id: " + browserSessionId;
                logger.error("validateCourseData - " + errorMsg);
                response = new ValidateCourseResponse(null, null, false, errorMsg, null);
            }


        }else if(courseData == null){
            response = new ValidateCourseResponse(null, null, false, "Unable to validate the course because the course was not provided to the server call", courseData);

        } else {
            String errorMsg = "Unable to find user for browser session id: " + browserSessionId;
            logger.error("validateCourseData - " + errorMsg);
            response = new ValidateCourseResponse(null, null, false, errorMsg, null);
        }

        metrics.endTrackingRpc("validateCourseData", start);
        return response;
    }

    /* (non-Javadoc)
     * @see mil.arl.gift.tools.dashboard.client.DashboardService#checkForImportConflicts(java.lang.String, java.lang.String)
     */
    @Override
    public ImportCoursesResponse checkForImportConflicts(String username, String exportFileLocation) {

        long start = System.currentTimeMillis();
        ImportCoursesResponse response = new ImportCoursesResponse();

        if(logger.isTraceEnabled()){
            logger.trace("importCourses called with user: " + username + " and export file: " + exportFileLocation);
        }

        if(exportFileLocation != null && !exportFileLocation.isEmpty()){

            try{
                // check to see if there are existing files that the user can overwrite
                Map<File, File> conflictMap = ImportManager.getInstance().checkForConflicts(username, exportFileLocation);

                if(conflictMap != null && !conflictMap.isEmpty()) {
                    // add the filenames and a user-friendly message to the response

                    Set<File> conflictingFiles = conflictMap.keySet();

                    for(File file : conflictingFiles) {

                        if(file.getName().endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION)) {
                            /* A conflicting file could be an image or a course. If the file is a course,
                             * pass the domain relative path and a rename prompt back to the client. */

                            String domainRelativePath = file.getPath();
                            domainRelativePath = domainRelativePath.substring(domainRelativePath.indexOf("Domain"));

                            //get the name of the course folder whose course name the file conflicts with
                            String courseFolderName = conflictMap.get(file).getName();

                            response.addCourseRenamePrompt(domainRelativePath, "A course named <b>" + courseFolderName + "</b> already exists:<br/><br/>" +
                                 "Please enter a new name for the course.");

                        } else {
                            // For now, images are either overwritten or not

                            response.addImageOverwritePrompt(file.getName(), ImportManager.createOverwritePrompt(file, conflictMap.get(file)));
                        }
                    }

                    response.setIsSuccess(true);

                } else {

                    // continue with the import if no conflicts found
                    response = importCourses(username, exportFileLocation, null, null);
                }

            }catch(Exception e){
                logger.warn("Caught exception while trying to import.", e);

                DetailedException importException;
                if(e instanceof DetailedException) {
                    importException = (DetailedException)e;
                }else{
                    importException = new DetailedException("Failed to import the course(s).", "There was an exception thrown that reads:\n"+e.getMessage(), e);
                }

                response.setResponse(importException.getReason());
                response.setAdditionalInformation(importException.getDetails());
                response.setErrorStackTrace(importException.getErrorStackTrace());

                response.setIsSuccess(false);
            }

        } else {

            response.setIsSuccess(false);
            response.setResponse("The location of the export file cannot be null or empty.");
            logger.warn("importCourses - The location of the export file cannot be null or empty.");
        }

        metrics.endTrackingRpc("checkForImportConflicts", start);
        return response;
    }

    @Override
    public ImportCoursesResponse importCourses(String username, String exportFileLocation, List<String> filesToOverwrite, Map<String, String> courseToNameMap) {

        long start = System.currentTimeMillis();
        ImportCoursesResponse response = new ImportCoursesResponse();

        if(logger.isTraceEnabled()){
            logger.trace("importCourses called with user: " + username + " and export file: " + exportFileLocation);
        }

        if(exportFileLocation != null && !exportFileLocation.isEmpty()){

            try{
                ImportManager.getInstance().importCourses(username, exportFileLocation, filesToOverwrite, courseToNameMap);
                response.setIsSuccess(true);

            }catch(Exception e){
                logger.warn("Caught exception while trying to import.", e);
                response.setIsSuccess(false);

                if(e instanceof DetailedException) {
                    response.setResponse(((DetailedException) e).getReason());
                    response.setAdditionalInformation(((DetailedException) e).getDetails());
                    response.setErrorStackTrace(((DetailedException) e).getErrorStackTrace());
                } else {
                    response.setResponse(e.getMessage());
                }
            }

        } else {

            response.setIsSuccess(false);
            response.setResponse("The location of the export file cannot be null or empty.");
            logger.warn("importCourses - The location of the export file cannot be null or empty.");
        }

        metrics.endTrackingRpc("importCourses", start);
        return response;
    }

    @Override
    public LoadedProgressIndicator<List<DomainOption>> getImportProgress(String username) {
        long start = System.currentTimeMillis();

        if(logger.isTraceEnabled()){
            logger.info("getImportProgress called: " + username);
        }

        LoadedProgressIndicator<List<DomainOption>> importStatus = ImportManager.getInstance().getImportStatus(username);

        if (importStatus != null) {

            if(logger.isInfoEnabled()){
                logger.info("getImportStatus importStatus = " + importStatus);
            }

            // save the metric for the time it took to import the course SUCCESSFULLY, a failed attempt
            // will skew the graph toward a lower time since it exited prematurely
            if(importStatus.isComplete() && importStatus.getException() == null){
                long creationTime = importStatus.getCreationTime();
                metrics.endTrackingRpc("importCoursesThread", creationTime);
            }

            metrics.endTrackingRpc("getImportProgress", start);

            return importStatus;

        } else {

            if(logger.isInfoEnabled()){
                logger.info("getImportStatus importStatus = null");
            }

            LoadedProgressIndicator<List<DomainOption>> failureStatus = new LoadedProgressIndicator<>();
            failureStatus.setException(new DetailedException(
                    "Failed to retrieve import status due to an unexpected error on the server.",
                    "Unable to find import status for user: " + username,
                    null
            ));

            metrics.endTrackingRpc("getImportProgress", start);

            return failureStatus;
        }
    }

    /* (non-Javadoc)
     * @see mil.arl.gift.tools.dashboard.client.DashboardService#cancelImport(java.lang.String)
     */
    @Override
    public RpcResponse cancelImport(String username) {
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("cancelImport called with user: " + username + ".");
        }

        ImportManager.getInstance().cancelImport(username);

        response = new RpcResponse(null, null, true, null);
        metrics.endTrackingRpc("cancelImport", start);
        return response;
    }

    @Override
    public RpcResponse endCourse(String browserSessionId, String domainId) {
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isDebugEnabled()){
            logger.debug("endCourse - called with browserSessionId: " + browserSessionId + ", domainId =" + domainId);
        }

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {

            DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            if (userData != null) {
                boolean success = ServicesManager.getInstance().getFileServices().cleanupCourse(domainId);

                if (success) {
                    response = new RpcResponse(null, null, true, null);
                    if(logger.isDebugEnabled()){
                        logger.debug("endCourse - success for user: " + userData.getUserName() + ", course: " + domainId);
                    }
                } else {
                    String errorMsg = "endCourse - cleanupCourse failed for domainId =  " + domainId;
                    response = new RpcResponse(null, null, false, errorMsg);
                }
            } else {
                String errorMsg = "Unable to find usersession for session id: " + browserSessionId;
                logger.error("endCourse - " + errorMsg);
                response = new RpcResponse(null, null, false, errorMsg);
            }

        } else {
            String errorMsg = "Unable to find user session for session id: " + browserSessionId;
            logger.error("endCourse - " + errorMsg);
            response = new RpcResponse(null, null, false, errorMsg);
        }

        metrics.endTrackingRpc("endCourse", start);
        return response;
    }

    @Override
    public ExperimentResponse createExperiment(String name, String description,
            String username, String courseId, String dataSetType) {


        long start = System.currentTimeMillis();
        ExperimentResponse response = new ExperimentResponse();

        if(logger.isTraceEnabled()){
            logger.trace("createExperiment called with user: " + username + " and course ID: " + courseId);
        }

        boolean success = true;

        DataSetType dataSetCollectionType = DataSetType.EXPERIMENT;
        try {
            dataSetCollectionType = DataSetType.valueOf(dataSetType);
        } catch (IllegalArgumentException e) {
            success = false;
            response.setIsSuccess(false);
            response.setErrorMessage("An invalid published course type was received: " + dataSetType);
            logger.error("An invalid published course type was received: " + dataSetType, e);
        }


        if (success) {
            if(courseId != null && !courseId.isEmpty()){

                if (dataSetCollectionType.equals(DataSetType.LTI)) {

                    try{
                        FileProxy proxy = ServicesManager.getInstance().getFileServices().getFile(courseId, username);

                        if (!proxy.canWrite(username)) {
                            response.setIsSuccess(false);
                            response.setErrorMessage("Unable to create an LTI published course for the course.");
                            response.setErrorDetails("You must have edit permissions to the course to publish an LTI course for it.  " +
                                    "Try loading the course in the Course Creator to ensure you have permissions to edit the course.");

                            metrics.endTrackingRpc("createExperiment", start);

                            return response;
                        }
                    } catch (Exception e) {
                        response.setIsSuccess(false);
                        response.setErrorMessage("Caught exception getting the course details for the published course.");
                        response.setErrorDetails(e.getMessage());

                        metrics.endTrackingRpc("createExperiment", start);

                        return response;
                    }
                }



                try{
                    response = new ExperimentResponse(true, null, DataCollectionManager.getInstance().createDataCollectionItem(name, description, username, courseId, dataSetCollectionType));

                }catch(DetailedException e){

                    logger.warn("Caught exception while trying to create a published course.", e);
                    response.setIsSuccess(false);
                    response.setErrorMessage(e.getReason());
                    response.setErrorDetails(e.getDetails());
                    response.setErrorStackTrace(e.getErrorStackTrace());

                }catch(Exception e){

                    logger.warn("Caught exception while trying to create a published course.", e);
                    response.setIsSuccess(false);
                    response.setErrorMessage(e.toString());
                }

            } else {

                response.setIsSuccess(false);
                response.setErrorMessage("The course ID cannot be null.");
                logger.warn("createExperiment - The course ID cannot be null.");
            }
        }

        metrics.endTrackingRpc("createExperiment", start);

        return response;
    }

    @Override
    public ProgressResponse getCreateExperimentProgress(String username) {

        long start = System.currentTimeMillis();
        ProgressResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getCreateExperimentProgress called with user: " + username + ".");
        }

        ProgressIndicator progress = DataCollectionManager.getInstance().getDataCollectionItemCreationProgress(username);

        if(progress != null){
            response = new ProgressResponse(true, null, progress);

        } else {
            response = new ProgressResponse(false, null, null);
            response.setErrorMessage("Could not find any published course creations in progress for this user. ");
            logger.info("getCreateExperimentProgress - Could not find any published course creations in progress for user: " + username);
        }

        metrics.endTrackingRpc("getCreateExperimentProgress", start);
        return response;
    }

    @Override
    public RpcResponse cancelCreateExperiment(String username) {
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("cancelCreateExperiment called with user: " + username + ".");
        }

        DataCollectionManager.getInstance().cancelDataCollectionItemCreation(username);

        response = new RpcResponse(null, null, true, null);

        metrics.endTrackingRpc("cancelCreateExperiment", start);
        return response;
    }

    @Override
    public ExperimentListResponse getExperiments(String username) {

        long start = System.currentTimeMillis();

        if(logger.isTraceEnabled()){
            logger.trace("getExperiments called with user: " + username + ".");
        }

        ArrayList<DataCollectionItem> experiments = DataCollectionManager.getInstance().getDataCollectionItems(username);

        ExperimentListResponse response = new ExperimentListResponse(true, null, experiments);

        metrics.endTrackingRpc("getExperiments", start);

        return response;
    }

    @Override
    public DetailedRpcResponse deleteExperiment(String username, String experimentId) {

        long start = System.currentTimeMillis();
        DetailedRpcResponse response = new DetailedRpcResponse();

        if(logger.isTraceEnabled()){
            logger.trace("deleteExperiment called with user: " + username + " and experiment ID: " + experimentId);
        }

        if(experimentId != null && !experimentId.isEmpty()){

            try{
                DataCollectionManager.getInstance().deleteDataCollectionItem(username, experimentId);

                response.setIsSuccess(true);

            }catch(DetailedException e){

                logger.warn("Caught exception while trying to delete a published course with ID: " + experimentId + ".", e);
                response.setIsSuccess(false);
                response.setErrorMessage(e.getReason());
                response.setErrorDetails(e.getDetails());
                response.setErrorStackTrace(e.getErrorStackTrace());

            }catch(Exception e){

                logger.warn("Caught exception while trying to delete a published course with ID: " + experimentId + ".", e);
                response.setIsSuccess(false);
                response.setErrorMessage(e.toString());
            }

        } else {

            response.setIsSuccess(false);
            response.setErrorMessage("The published course ID cannot be null or empty.");
            logger.warn("deleteExperiment - The published course ID cannot be null empty.");
        }

        metrics.endTrackingRpc("deleteExperiment", start);
        return response;
    }

    @Override
    public ProgressResponse getDeleteExperimentProgress(String username) {
        long start = System.currentTimeMillis();
        ProgressResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getDeleteExperimentProgress called with user: " + username + ".");
        }

        ProgressIndicator progress = DataCollectionManager.getInstance().getDataCollectionItemDeletionProgress(username);

        if(progress != null){
            response = new ProgressResponse(true, null, progress);

        } else {
            response = new ProgressResponse(false, null , null);
            response.setErrorMessage("Could not find any published course deletions in progress.");
            logger.info("getDeleteExperimentProgress - Could not find any published course deletions in progress for user: " + username);
        }

        metrics.endTrackingRpc("getDeleteExperimentProgress", start);
        return response;
    }

    @Override
    public RpcResponse cancelDeleteExperiment(String username) {
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("cancelDeleteExperiment called with user: " + username + ".");
        }

        DataCollectionManager.getInstance().cancelDataCollectionItemDeletion(username);

        response = new RpcResponse(null, null, true, null);
        metrics.endTrackingRpc("cancelDeleteExperiment", start);
        return response;
    }

    @Override
    public DoubleResponse calculateExperimentCourseExportSize(String username, String experimentId) {
        long start = System.currentTimeMillis();
        DoubleResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("calculateExperimentCourseExportSize called with user: " + username + " and courses: " + experimentId);
        }

        try {
            double exportSize = DataCollectionManager.getInstance().getCourseExportSize(username, experimentId);
            response = new DoubleResponse(true, null, exportSize);

        }catch(DetailedException e){

            logger.warn("An exception ocurred while calculating export size for user '" + username + "'.", e);

            response = new DoubleResponse(false, null, 0);

            response.setIsSuccess(false);
            response.setErrorMessage(e.getReason());
            response.setErrorDetails(e.getDetails());
            response.setErrorStackTrace(e.getErrorStackTrace());

        }catch(Exception e){

            logger.warn("An exception ocurred while calculating export size for user '" + username + "'.", e);

            response = new DoubleResponse(false, null, 0);

            response.setIsSuccess(false);
            response.setErrorMessage(e.toString());
        }

        metrics.endTrackingRpc("calculateExperimentCourseExportSize", start);
        return response;
    }

    @Override
    public DoubleResponse calculateExperimentRawDataExportSize(String username, String experimentId) {

        long start = System.currentTimeMillis();
        DoubleResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("calculateExperimentCourseExportSize called with user: " + username + " and courses: " + experimentId);
        }

        try {
            double exportSize = DataCollectionManager.getInstance().getRawDataExportSize(username, experimentId);
            response = new DoubleResponse(true, null, exportSize);

        }catch(DetailedException e){

            logger.warn("An exception ocurred while calculating export size for user '" + username + "'.", e);

            response = new DoubleResponse(false, null, 0);

            response.setIsSuccess(false);
            response.setErrorMessage(e.getReason());
            response.setErrorDetails(e.getDetails());
            response.setErrorStackTrace(e.getErrorStackTrace());

        }catch(Exception e){

            logger.warn("An exception ocurred while calculating export size for user '" + username + "'.", e);

            response = new DoubleResponse(false, null, 0);

            response.setIsSuccess(false);
            response.setErrorMessage(e.toString());
        }

        metrics.endTrackingRpc("calculateExperimentRawDataExportSize", start);
        return response;
    }

    @Override
    public ExportResponse exportExperimentCourse(String username,
            String experimentId) {

        long start = System.currentTimeMillis();
        ExportResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("exportExperimentCourse called with user: " + username + " and experiment ID: " + experimentId);
        }

        if(experimentId != null){

            if(!experimentId.isEmpty()){

                try{
                    DownloadableFileRef result =  DataCollectionManager.getInstance().exportDataCollectionItemCourse(username, experimentId);

                     response = new ExportResponse(true, null, result);

                }catch(DetailedException e){

                    response = new ExportResponse(false, null, null);

                    logger.warn("Caught exception while trying to export an experiment's course.", e);
                    response.setIsSuccess(false);
                    response.setErrorMessage(e.getReason());
                    response.setErrorDetails(e.getDetails());
                    response.setErrorStackTrace(e.getErrorStackTrace());

                }catch(Exception e){

                    response = new ExportResponse(false, null, null);

                    logger.warn("Caught exception while trying to export a published course's course.", e);
                    response.setIsSuccess(false);
                    response.setErrorMessage(e.toString());
                }

            } else {

                throw new IllegalArgumentException("The published course ID cannot be empty.");
            }

        } else {
            response = new ExportResponse(false, null, null);
            response.setErrorMessage("The published course ID cannot be null.");
            logger.warn("exportExperimentCourse - The published course ID cannot be null.");
        }

        metrics.endTrackingRpc("exportExperimentCourse", start);
        return response;
    }

    @Override
    public ProgressResponse getExportExperimentCourseProgress(String username) {
        long start = System.currentTimeMillis();
        ProgressResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getExportExperimentCourseProgress called with user: " + username + ".");
        }

        ProgressIndicator progress = DataCollectionManager.getInstance().getExportDataCollectionItemCourseProgress(username);

        if(progress != null){
            response = new ProgressResponse(true, null, progress);

        } else {
            response = new ProgressResponse(false, null, null);
            response.setErrorMessage("Could not find any published course course exports in progress for this user. ");
            logger.info("getExportExperimentCourseProgress - Could not find any published course course exports in progress for user: " + username);
        }

        metrics.endTrackingRpc("getExportExperimentCourseProgress", start);
        return response;
    }

    @Override
    public RpcResponse cancelExportExperimentCourse(String username) {
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("cancelExportExperimentCourse called with user: " + username + ".");
        }

        DataCollectionManager.getInstance().cancelExportDataCollectionItemCourse(username);

        response = new RpcResponse(null, null, true, null);
        metrics.endTrackingRpc("cancelExportExperimentCourse", start);
        return response;
    }

    @Override
    public ExportResponse exportExperimentRawData(String username,
            String experimentId, boolean exportConvertedBinaryLogs) {
        long start = System.currentTimeMillis();
        ExportResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("exportExperimentRawData called with user: " + username + " and experiment ID: " + experimentId);
        }

        if(experimentId != null){

            if(!experimentId.isEmpty()){

                try{
                    DownloadableFileRef result =  DataCollectionManager.getInstance().exportDataCollectionItemRawData(username, experimentId, exportConvertedBinaryLogs);

                     response = new ExportResponse(true, null, result);

                }catch(DetailedException e){

                    response = new ExportResponse(false, null, null);

                    logger.warn("Caught exception while trying to export a published course's raw participant data.", e);
                    response.setIsSuccess(false);
                    response.setErrorMessage(e.getReason());
                    response.setErrorDetails(e.getDetails());
                    response.setErrorStackTrace(e.getErrorStackTrace());

                }catch(Exception e){

                    response = new ExportResponse(false, null, null);

                    logger.warn("Caught exception while trying to export a published course's raw participant data.", e);
                    response.setIsSuccess(false);
                    response.setErrorMessage(e.toString());
                    response.setErrorStackTrace(DetailedException.getFullStackTrace(e));
                }

            } else {

                throw new IllegalArgumentException("The published course ID cannot be empty.");
            }

        } else {
            response = new ExportResponse(false, null, null);
            response.setErrorMessage("The published course ID cannot be null.");
            logger.warn("exportExperimentRawData - The published course ID cannot be null.");
        }

        metrics.endTrackingRpc("exportExperimentRawData", start);
        return response;
    }

    @Override
    public ProgressResponse getExportExperimentRawDataProgress(String username) {
        long start = System.currentTimeMillis();
        ProgressResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getExportExperimentRawDataProgress called with user: " + username + ".");
        }

        ProgressIndicator progress = DataCollectionManager.getInstance().getExportDataCollectionItemRawDataProgress(username);

        if(progress != null){
            response = new ProgressResponse(true, null, progress);

        } else {
            response = new ProgressResponse(false, null, null);
            response.setErrorMessage("Could not find any published course raw data exports in progress for this user. ");
            logger.info("getExportExperimentRawDataProgress - Could not find any published course raw data exports in progress for user: " + username);
        }
        metrics.endTrackingRpc("getExportExperimentRawDataProgress", start);
        return response;
    }

    @Override
    public RpcResponse cancelExportExperimentRawData(String username) {
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("cancelExportExperimentRawData called with user: " + username + ".");
        }

        DataCollectionManager.getInstance().cancelExportDataCollectionItemRawData(username);

        response = new RpcResponse(null, null, true, null);
        metrics.endTrackingRpc("getExportExperimentRawDataProgress", start);
        return response;
    }

    @Override
    public ExperimentResponse updateExperiment(String username, String experimentId, String name, String description){
        long start = System.currentTimeMillis();
        ExperimentResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("updateExperiment called with user: " + username + " and experiment: " + experimentId + ".");
        }

        try{
            DataCollectionItem experiment = DataCollectionManager.getInstance().updateDataCollectionItem(username, experimentId, name, description);

            response = new ExperimentResponse(true, null, experiment);

        }catch(DetailedException e){

            response = new ExperimentResponse(false, null, null);

            logger.warn("Caught exception while updating a published course.", e);
            response.setIsSuccess(false);
            response.setErrorMessage(e.getReason());
            response.setErrorDetails(e.getDetails());
            response.setErrorStackTrace(e.getErrorStackTrace());

        }catch(Exception e){

            response = new ExperimentResponse(false, null, null);

            logger.warn("Caught exception while updating an published course.", e);
            response.setIsSuccess(false);
            response.setErrorMessage(e.toString());
        }

        metrics.endTrackingRpc("updateExperiment", start);
        return response;
    }

    @Override
    public DetailedRpcResponse validateExperimentCourse(String username, String experimentId) {

        long start = System.currentTimeMillis();
        DetailedRpcResponse response = new DetailedRpcResponse();

        if(logger.isTraceEnabled()){
            logger.trace("validateExperimentCourse called with user: " + username + " and experiment ID: " + experimentId);
        }

        if(experimentId != null){

            if(!experimentId.isEmpty()){

                try{
                    ServicesManager.getInstance().getDataCollectionServices().validateDataCollectionItemCourse(username, experimentId);

                    response.setIsSuccess(true);

                }catch(DetailedException e){

                    logger.warn("Caught exception while trying to validate a course for a published course with ID: " + experimentId + ".", e);
                    response.setIsSuccess(false);
                    response.setErrorMessage(e.getReason());
                    response.setErrorDetails(e.getDetails());
                    response.setErrorStackTrace(e.getErrorStackTrace());

                }catch(Exception e){

                    logger.warn("Caught exception while trying to validate a course for a published course with ID: " + experimentId + ".", e);
                    response.setIsSuccess(false);
                    response.setErrorMessage(e.toString());
                }

            } else {

                throw new IllegalArgumentException("The published course ID cannot be empty.");
            }

        } else {

            response.setIsSuccess(false);
            response.setErrorMessage("The published course ID cannot be null.");
            logger.warn("validateExperimentCourse - The published course ID cannot be null.");
        }

        metrics.endTrackingRpc("validateExperimentCourse", start);
        return response;
    }

    @Override
    public ExportResponse exportErrorFile(String username, String reason, List<ErrorDetails> errorDetailsList, Date date, String courseName) {

        long start = System.currentTimeMillis();
        ExportResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("exportErrorFile called with user: " + username);
        }

        if(username != null && !username.isEmpty()){

            try{

                //construct export file name using current time and user information
                StringBuilder exportFileName = new StringBuilder();
                exportFileName.append(username);
                exportFileName.append("_error_report_");
                exportFileName.append(TimeUtil.formatCurrentTime());

                StringBuilder sb = new StringBuilder();
                DomainModuleProperties properties = DomainModuleProperties.getInstance();

                String logFileName = exportFileName.toString();

                final File zipFile = new File(properties.getExportDirectory() + File.separator + logFileName + ".zip");
                final File logDir  = new File(properties.getExportDirectory() +  File.separator + logFileName);
                logDir.mkdir();
                final File logFile = new File(logDir, logFileName + ".log");

                logFile.deleteOnExit();
                logDir.deleteOnExit();
                zipFile.deleteOnExit();

                sb.append("The following is an error report generated by the GIFT server.");
                sb.append("\n\nUsername:  ").append(username);
                sb.append("\nDate: ").append(fdf.format(date));
                if(courseName != null){
                    sb.append("\nCourse: ").append(courseName);
                }

                sb.append(":\n\n");
                sb.append("Reason\n").append(reason).append("\n\n");

                //
                // First write the details
                //
                sb.append("Details\n");
                int index = 1;
                for(ErrorDetails errorDetail : errorDetailsList){

                    // replace html formatting
                    String detail = errorDetail.getDetails();
                    detail = detail.replaceAll("<br/>", "\n");
                    detail = detail.replaceAll("<img(.)*?>", "(image unavailable)");
                    detail = detail.replaceAll("<(.)*?>", "");
                    sb.append(index++).append(". ").append(detail).append("\n\n");

                }

                //
                // Second write the stack traces
                //
                sb.append("Stack Trace\n");
                index = 1; //reset
                for(ErrorDetails errorDetail : errorDetailsList){

                    sb.append(index++).append(". ");

                    if(errorDetail.getStacktrace() != null && !errorDetail.getStacktrace().isEmpty()){

                        for (String e : errorDetail.getStacktrace()) {
                            sb.append(e).append("\n");
                        }

                    }else{
                        sb.append("No stack trace available.\n");
                    }

                    sb.append("\n\n");

                }

                if(logger.isInfoEnabled()){
                    logger.info("Attempting to write to file at " + logFile.getPath());
                }
                FileUtils.writeStringToFile(logFile, sb.toString());

                List<File> files = new ArrayList<>();
                files.add(logDir);

                if(logger.isInfoEnabled()){
                    logger.info("Zipping log file to " + zipFile.getPath());
                }
                ZipUtils.zipFiles(files, zipFile);

                FileUtils.deleteDirectory(logDir);

                DownloadableFileRef result = new DownloadableFileRef(ServicesManager.getExportURL(zipFile).toString(), zipFile.getPath());

                Thread deleteLogFileThread = new Thread("Delete Error Report Thread"){

                    @Override
                    public void run(){

                        long deleteStart = System.currentTimeMillis();

                        try {

                            synchronized (this) {
                                //wait 30 minutes and then delete the log file if it still exists
                                wait(1800000);
                            }

                            FileUtils.forceDelete(zipFile);
                            FileUtils.forceDelete(logFile);
                            FileUtils.forceDelete(logDir);

                        } catch (InterruptedException e) {
                            logger.warn("An exception occurred while waiting to delete the error report files:\n"+zipFile+"\n"+logFile+"\n"+logDir, e);

                        } catch (@SuppressWarnings("unused") FileNotFoundException e){
                            //file might have been deleted by other logic, but its gone so nothing to log

                        } catch (IOException e) {
                            logger.warn("An exception occurred while waiting to delete the error report files:\n"+zipFile+"\n"+logFile+"\n"+logDir, e);
                        }

                        metrics.endTrackingRpc("deleteErrorFile", deleteStart);
                    }
                };

                deleteLogFileThread.start();

                 response = new ExportResponse(true, null, result);

            } catch(DetailedException e){

                response = new ExportResponse(false, null, null);

                logger.warn("Caught exception while trying to generate an error report.", e);
                response.setIsSuccess(false);
                response.setErrorMessage("An error occurred while generating the error report: " + e.getReason());
                response.setErrorDetails(e.getDetails());
                response.setErrorStackTrace(e.getErrorStackTrace());

            } catch(Exception e){

                response = new ExportResponse(false, null, null);

                logger.warn("Caught exception while trying to generate an error report.", e);
                response.setIsSuccess(false);
                response.setErrorMessage("An error occurred while generating the error report.");
                response.setErrorDetails(e.toString());
                response.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            }

        } else {
            response = new ExportResponse(false, null, null);
            response.setErrorMessage("There was a problem generating your error report. Please contact your system administrator.");
            response.setErrorDetails("The error report cannot be created because was no username provided.");
            logger.warn("exportErrorFile - The username cannot be empty.");
        }

        metrics.endTrackingRpc("exportErrorFile", start);
        return response;
    }

    @Override
    public ExperimentResponse getExperiment(String username, String experimentId){
        long start = System.currentTimeMillis();
        ExperimentResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getExperiment called with user: " + username + " and experiment: " + experimentId + ".");
        }

        try{
            DataCollectionItem experiment = DataCollectionManager.getInstance().getDataCollectionItem(experimentId);

            response = new ExperimentResponse(true, null, experiment);

        }catch(DetailedException e){

            response = new ExperimentResponse(false, null, null);

            logger.warn("Caught exception while getting a published course for "+username+" with experiment id "+experimentId+".", e);
            response.setIsSuccess(false);
            response.setErrorMessage(e.getReason());
            response.setErrorDetails(e.getDetails());
            response.setErrorStackTrace(e.getErrorStackTrace());

        }catch(Exception e){

            response = new ExperimentResponse(false, null, null);

            logger.warn("Caught exception while getting a published course for "+username+" with experiment id "+experimentId+".", e);
            response.setIsSuccess(false);
            response.setErrorMessage(e.toString());
        }

        metrics.endTrackingRpc("getExperiment", start);
        return response;
    }

    @Override
    public ExperimentResponse setExperimentActive(String username, String experimentId, boolean active){
        long start = System.currentTimeMillis();
        ExperimentResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("setExperimentActive called with user: " + username + " and experiment: " + experimentId + ".");
        }

        try{
            DataCollectionItem existingExperiment = DataCollectionManager.getInstance().getDataCollectionItem(experimentId);

            if (existingExperiment != null) {

                ExperimentStatus currentStatus = existingExperiment.getStatus();

                // A user can only update the status of an experiment if it is in a running or paused state.
                if (currentStatus.equals(ExperimentStatus.RUNNING) ||
                    currentStatus.equals(ExperimentStatus.PAUSED)) {
                    ExperimentStatus newStatus = ExperimentStatus.RUNNING;
                    if (!active) {
                        newStatus = ExperimentStatus.PAUSED;
                    }
                    DataCollectionItem experiment = DataCollectionManager.getInstance().setDataCollectionItemStatus(username, experimentId, newStatus);

                    response = new ExperimentResponse(true, null, experiment);
                } else {
                    response = new ExperimentResponse(false, null, null);

                    response.setIsSuccess(false);
                    response.setErrorMessage("The experiment status cannot be modified.  It is currnetly in status of: " +
                            currentStatus);
                }
            } else {
                response = new ExperimentResponse(false, null, null);

                response.setIsSuccess(false);
                response.setErrorMessage("The experiment status cannot be modified.  Unable to find an experiment with id: " +
                        experimentId);
            }



        }catch(DetailedException e){

            response = new ExperimentResponse(false, null, null);

            logger.warn("Caught exception while updating a published course.", e);
            response.setIsSuccess(false);
            response.setErrorMessage(e.getReason());
            response.setErrorDetails(e.getDetails());
            response.setErrorStackTrace(e.getErrorStackTrace());

        }catch(Exception e){

            response = new ExperimentResponse(false, null, null);

            logger.warn("Caught exception while updating a published course.", e);
            response.setIsSuccess(false);
            response.setErrorMessage(e.toString());
        }

        metrics.endTrackingRpc("setExperimentActive", start);
        return response;
    }

    /**
     * Note this occurs on a separate thread from the dashboardserviceimpl object.  Objects referenced in here must be
     * unique to the thread or static instances.
     */
    @Override
    public void sessionCreated(HttpSessionEvent event) {

        if(logger.isTraceEnabled()){
            logger.trace("session created: " + event.getSession() );
        }
// TODO - This likely can be removed with websocket implementation, but we should test setting
// the http sesssion timeout value and make sure the web clients still work properly if this is called.
// For now as long as the websocket is open, the browser session should remain available.

    }


    /**
     * Note this occurs on a separate thread from the dashboardserviceimpl object.  Objects referenced in here must be
     * unique to the thread or static instances.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {

        if(logger.isTraceEnabled()){
            logger.trace("session destroyed: " + event.getSession());
        }

 // TODO - This likely can be removed with websocket implementation, but we should test setting
 // the http sesssion timeout value and make sure the web clients still work properly if this is called.
 // For now as long as the websocket is open, the browser session should remain available.

    }

    @Override
    public DetailedRpcResponse exportExperimentReport(String username,
            String experimentId, ReportProperties reportProperties) {
        long start = System.currentTimeMillis();
        DetailedRpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("exportExperimentReport called with user: " + username + " and experiment ID: " + experimentId);
        }

        if(experimentId != null){

            if(!experimentId.isEmpty()){

                try{
                    DataCollectionManager.getInstance().exportDataCollectionItemReport(username, experimentId, reportProperties);

                     response = new DetailedRpcResponse();
                     response.setIsSuccess(true);

                }catch(DetailedException e){

                    response = new ExportResponse(false, null, null);

                    logger.warn("Caught exception while trying to export a published course report.", e);
                    response.setIsSuccess(false);
                    response.setErrorMessage(e.getReason());
                    response.setErrorStackTrace(e.getErrorStackTrace());

                }catch(Exception e){

                    response = new ExportResponse(false, null, null);

                    logger.warn("Caught exception while trying to export a published course report", e);
                    response.setIsSuccess(false);
                    response.setErrorMessage(e.toString());
                }

            } else {

                Exception e =  new IllegalArgumentException("The published course ID cannot be empty.");

                response = new ExportResponse(false, null, null);

                logger.warn("Caught exception while trying to export a published course report.", e);
                response.setIsSuccess(false);
                response.setErrorMessage(e.toString());
                response.setErrorStackTrace(null);
            }

        } else {
            response = new ExportResponse(false, null, null);
            response.setErrorMessage("The published course ID cannot be null.");
            logger.warn("exportExperimentReport - The published course ID cannot be null.");
        }

        metrics.endTrackingRpc("exportExperimentReport", start);
        return response;
    }

    @Override
    public GenerateReportStatusResponse getExportExperimentReportProgress(String username) {
        long start = System.currentTimeMillis();
        GenerateReportStatusResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("getExportExperimentReportProgress called with user: " + username + ".");
        }

        GenerateReportStatus status = DataCollectionManager.getInstance().getExportDataCollectionItemReportProgress(username);

        if(status != null){
            response = new GenerateReportStatusResponse(true, null, status);

        } else {
            response = new GenerateReportStatusResponse(false, null, null);
            response.setErrorMessage("Could not find any published course report exports in progress for this user. ");
            logger.info("getExportExperimentReportProgress - Could not find any published course report exports in progress for user: " + username);
        }
        metrics.endTrackingRpc("getExportExperimentReportProgress", start);
        return response;
    }

    @Override
    public RpcResponse cancelExportExperimentReport(String username) {
        long start = System.currentTimeMillis();
        RpcResponse response = null;

        if(logger.isTraceEnabled()){
            logger.trace("cancelExportExperimentReport called with user: " + username + ".");
        }

        DataCollectionManager.getInstance().cancelExportDataCollectionItemReport(username);

        response = new RpcResponse(null, null, true, null);
        metrics.endTrackingRpc("getExportExperimentReportProgress", start);
        return response;
    }

    /**
     * This will encrypt the user's password based upon agreed upon methods between this and
     * the DashboardServiceImpl and return the encrypted password. The software uses a
     * common password to encrypt/decrypt the user's password.
     * @param originalPassword - the original unencrypted password
     * @return String - the encrypted password
     */
    private String encryptPassword(String originalPassword) {
        String pw = "THIS PASSWORD DID NOT ENCODE PROPERLY";

        try {
            // use a password property to use for encrypting the user's password.
            //
            char[] encryptKey = CommonProperties.getInstance().getCiphorPassword().toCharArray();
            byte[] salt = "21125150OU812".getBytes();

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey tmp = factory.generateSecret(new PBEKeySpec(encryptKey, salt, 20, 128));
            SecretKeySpec key = new SecretKeySpec(tmp.getEncoded(), "AES");
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Initialize the cipher with the PBKDF2WithHmacSHA256 key.
            //
            cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);

            // Perform the encryption.
            //
            byte[] encryptedBytes = cipher.doFinal(originalPassword.getBytes("UTF-8"));

            // get an Internet friendly format
            //
            pw = Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (@SuppressWarnings("unused") Exception e) {
            logger.error("Something blew up trying to encode the password");
        }

        return pw;
    }

    /**
     * Attempts to publish a course by copying the course from a user's workspace to the Public workspace.
     * This method is useful for non-rpc, non-client calls such as junit tests.
     *
     * @param userSession - The user session that contains information about the user.  Used to look up the username.
     * @param newCourseName - The name to give the copied course.  This is useful in case the course being created from the copy
     * would cause a course name collision.
     * @param courseData - The domain option (course data) to copy.  This contains the location of the course being copied.
     * @param editableToUsernames a set of GIFT usernames to give write access to in the survey context (and descendant element in the survey context).</br>
     * If toPublicWorkspace is true and this set is not empty, the set is used.</br>
     * If toPublicWorkspace is true and this set is null or empty, no user will have write access.</br>
     * If toPublicWorkspace is false and this set is not empty, the set is used.</br>
     * If toPublicWorkspace is false and this set is null or empty, the username found in the userSession is used as the only user with read access.
     * @param visibleToUsernames a set of GIFT usernames to give read access to in the survey context (and descendant elements in the survey context).</br>
     * If toPublicWorkspace is true than this parameter is ignored and a wildcard is used to everyone has read access.</br>
     * (when toPublicWorkspace is false) If this set is null or empty, the username found in the userSession is used as the only user with read access.
     * @return the results of the rpc.  Use {@link #getCopyProgress(String)} with to get the progress of this course copy operation
     * including whether a course name collision happened.
     * @throws DetailedException if there was a problem (other than name collision) during the publishing process
     */
    public DetailedRpcResponse publishCourseToPublic(DashboardHttpSessionData userSession, String browserSessionId, String newCourseName, DomainOption courseData,
            HashSet<String> editableToUsernames, HashSet<String> visibleToUsernames)
            throws DetailedException{

        //get public courses
        CourseOptionsWrapper courseWrapper = new CourseOptionsWrapper();

        // Do not do any validation here so that we can load the initial course data (not validated) to the user.
        // At a later point, each course will be separately validated.
        try {
            ServicesManager.getInstance().getFileServices().getCourses(userSession.getUserName(),
                    courseWrapper,
                    false,
                    null);
        } catch (IllegalArgumentException e) {
            String errorMsg = "An IllegalArgumentException occurred while trying to publish the course to public workspace for course(" + courseData + "): " + e.getMessage();
            DetailedRpcResponse response = new DetailedRpcResponse();
            response.setErrorMessage(errorMsg);
            return response;
        } catch (FileNotFoundException e) {
            String errorMsg = "A FileNotFoundException occurred while trying to publish the course to public workspace for course(" + courseData + "): " + e.getMessage();
            DetailedRpcResponse response = new DetailedRpcResponse();
            response.setErrorMessage(errorMsg);
            return response;
        } catch (Throwable e) {
            String errorMsg = "An Exception occurred while trying to publish the course to public workspace for course(" + courseData + "): " + e.getMessage();
            DetailedRpcResponse response = new DetailedRpcResponse();
            response.setErrorMessage(errorMsg);
            return response;
        }

        Iterator<Map.Entry<String, DomainOption>> it = courseWrapper.domainOptions.entrySet().iterator();

        //keep all public courses in the collection in order to perform name collision test
        while (it.hasNext()) {
            Map.Entry<String, DomainOption> entry = it.next();
            DomainOption domainItem = entry.getValue();

            if(!domainItem.getDomainId().startsWith(AbstractFileServices.PUBLIC_WORKSPACE_FOLDER_NAME, 1)){
                it.remove();
            }
        }

        //call copy course logic
        return copyCourse(userSession, browserSessionId, newCourseName, courseData, courseWrapper.domainOptions.values(), true, editableToUsernames, visibleToUsernames);
    }

    @Override
    public DetailedRpcResponse publishCourseToPublic(String browserSessionId, String newCourseName, DomainOption courseData)
            throws DetailedException{

        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession == null) {
            //ERROR
            DetailedRpcResponse copyResponse = new DetailedRpcResponse();
            copyResponse.setIsSuccess(false);
            String errorMsg = "Unable to find user session for session id: " + browserSessionId;
            logger.error("copyCourse - " + errorMsg);
            copyResponse.setErrorDetails("Unable to find the user session.\n\nHave you been idle for a long time?  Try logging in again.");
            copyResponse.setAdditionalInformation(errorMsg);
            return copyResponse;
        }

        DashboardHttpSessionData userData = userSession.getUserSessionInfo();
        return publishCourseToPublic(userData, browserSessionId, newCourseName, courseData, null, null);
    }

    @Override
    public GetActiveKnowledgeSessionsResponse fetchActiveKnowledgeSessions() {
        WebMonitorModule moduleInstance = WebMonitorModule.getInstance();

        final AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse> returnBlocker = new AsyncReturnBlocker<>();

        moduleInstance.requestActiveKnowledgeSessionsFromDomain(new MessageCollectionCallback() {
                    @Override
                    public void success() {
                        // do nothing
                    }

                    @Override
                    public void received(Message msg) {
                        final Object payload = msg.getPayload();
                        KnowledgeSessionsReply knowledgeSessionReply = (KnowledgeSessionsReply)payload;
                        GetActiveKnowledgeSessionsResponse response = new GetActiveKnowledgeSessionsResponse(true,
                                "success", knowledgeSessionReply);

                        returnBlocker.setReturnValue(response);
                    }

                    @Override
                    public void failure(String why) {
                        logger.warn("Failed to retrieve the active knowledge sessions because '" + why + "'.");
                        GetActiveKnowledgeSessionsResponse response = new GetActiveKnowledgeSessionsResponse(false,
                                "Failed to retrieve the active knowledge sessions because '" + why + "'.", null);
                        returnBlocker.setReturnValue(response);
                    }

                    @Override
                    public void failure(Message msg) {
                        logger.warn("Failed to retrieve the active knowledge sessions.");
                        GetActiveKnowledgeSessionsResponse response = new GetActiveKnowledgeSessionsResponse(false,
                                "Failed to retrieve the active knowledge sessions because '" + msg + "'.", null);
                        returnBlocker.setReturnValue(response);
                    }
                });

        return returnBlocker.getReturnValueOrTimeout();
    }

    @Override
    public GenericRpcResponse<Void> registerKnowledgeSessionMonitor(String browserSessionKey,
            AbstractKnowledgeSession knowledgeSession) {
        if (isBlank(browserSessionKey)) {
            return new FailureResponse<>(new DetailedException(
                    "Unable to register a monitor for the knowledge session because the session is null.",
                    "Unable to register a monitor for the knowledge session because the session is null.", null));
        }

        ksmManager.registerBrowser(browserSessionKey, knowledgeSession);

        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> deregisterKnowledgeSessionMonitor(String browserSessionKey,
            AbstractKnowledgeSession knowledgeSession) {
        if (isBlank(browserSessionKey)) {
            return new FailureResponse<>(new DetailedException(
                    "Unable to de-register a monitor for the knowledge session because the session is null.",
                    "Unable to de-register a monitor for the knowledge session because the session is null.", null));
        }

        /* Removes the entry from the map iff the browser session key is in the map AND the key is
         * mapped to the provided knowledge session */
        ksmManager.deregisterBrowser(browserSessionKey, knowledgeSession);

        return new SuccessfulResponse<>();
    }

    @Override
    public RpcResponse updateLogIndex(String browserSessionId, LogMetadata logMetadata){

        final LoadedProgressIndicator<Void> progResponse = new LoadedProgressIndicator<>();

        // for now... old entries will be removed from the map when the next update request is made
        // to give the client enough time to show the completed progress
        browserSessionToUpdateLogProgress.put(browserSessionId, progResponse);

        //
        // Release the calling thread and make this copy course operation asynchronous
        //
        Runnable updateOperation = new Runnable() {
            @Override
            public void run(){
                try{
                    LogIndexService.getInstance().updateLogMetadata(logMetadata, progResponse);
                    progResponse.setComplete(true);
                }catch(Throwable throwable){
                    logger.error("There was an issue while updating the past session log index on the server for browser session id "+browserSessionId+".", progResponse);
                    progResponse.setException(new DetailedException("Failed to update the past session log index.",
                            "There was an issue with the request from browser session id "+browserSessionId+" to update the past ession log index.", throwable));
                }
            }
        };

        AsyncOperationManager.getInstance().startAsyncOperation("updateLogIndex-" + browserSessionId,  updateOperation);
        RpcResponse response = new RpcResponse();
        response.setIsSuccess(true);
        return response;
    }

    @Override
    public LoadedProgressIndicator<Void> getUpdateLogIndexProgress(String browserSessionId){

        long start = System.currentTimeMillis();

        if(logger.isInfoEnabled()){
            logger.info("getUpdateLogIndexProgress called: " + browserSessionId);
        }

        LoadedProgressIndicator<Void> response = browserSessionToUpdateLogProgress.get(browserSessionId);
        if (response == null) {

            response = new LoadedProgressIndicator<>();
            response.setException(
                    new DetailedException("Unable to find update log index progress indicator for session id: " + browserSessionId, "", null));
            response.setComplete(true);
        } else if (response.isComplete()) {
            browserSessionToUpdateLogProgress.remove(browserSessionId);
        }

        metrics.endTrackingRpc("getUpdateLogIndexProgress", start);

        return response;
    }
    
    @Override
    public GenericRpcResponse<LogMetadata> fetchLogForPlayback(String browserSessionId, AbstractKnowledgeSession activeSession){
        
        if(activeSession == null) {
            return new FailureResponse<>(new DetailedException("Unable to get playback information for this session",
                    "The session to play back cannot be null.",
                    null));
        }
        
        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession == null) {
            return new FailureResponse<>(new DetailedException("Unable to get playback information for this session",
                    "There is no user session associated with the browser session ID of " + browserSessionId,
                    null));
        }
            
        DashboardHttpSessionData userData = userSession.getUserSessionInfo();
        if (userData == null) {
            return new FailureResponse<>(new DetailedException("Unable to get playback information for this session",
                    "There is no user session info associated with the browser session ID of " + browserSessionId,
                    null));
        }
        
        try {
            LogMetadata metadata = LogIndexService.getInstance().getSession(
                    activeSession.getNameOfSession(),
                    activeSession.getSessionStartTime(), 
                    activeSession.getHostSessionMember().getDomainSessionId(), 
                    activeSession.getHostSessionMember().getUserSession().getUserId(), 
                    activeSession.getHostSessionMember().getUserSession().getExperimentId());
            
            metadata.getSession().setPlaybackId(browserSessionId);
            
            return new SuccessfulResponse<LogMetadata>(metadata);
            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);
        }
    }

    @Override
    public RpcResponse fetchLogsAvailableForPlayback(String browserSessionId) {

        RpcResponse response = new RpcResponse();
        UserWebSession userSession = userSessionManager.getUserSessionByBrowserKey(browserSessionId);
        if (userSession != null) {

            DashboardHttpSessionData userData = userSession.getUserSessionInfo();
            if (userData != null) {

                final LoadedProgressIndicator<Collection<LogMetadata>> progResponse = new LoadedProgressIndicator<>();

                // for now... old entries will be removed from the map when the next update request is made
                // to give the client enough time to show the completed progress
                browserSessionToFetchPlaybackLogsProgress.put(browserSessionId, progResponse);

                //
                // Release the calling thread and make this copy course operation asynchronous
                //
                Runnable fetchOperation = new Runnable() {
                    @Override
                    public void run(){
                        try{
                            Collection<LogMetadata> sessions = LogIndexService.getInstance().getAllSessionsSorted(userData.getUserName(), progResponse);
                            for(LogMetadata session : sessions) {
                                
                                /* Associate each returned knowledge session with this browser session to avoid
                                 * potential collisions if other browser sessions attempt to play back
                                 * the same sessions */
                                session.getSession().setPlaybackId(browserSessionId);
                            }
                            
                            progResponse.setPayload(sessions);
                            progResponse.setComplete(true);
                        }catch(Throwable throwable){
                            logger.error("There was an issue while retrieving the domain session logs on the server for browser session id "+browserSessionId+".", throwable);
                            progResponse.setException(new DetailedException("Failed to retrieve the sesssion log files.",
                                    "There was an issue with the request from browser session id "+browserSessionId+" to retrieve the sessions.", throwable));
                        }
                    }
                };

                AsyncOperationManager.getInstance().startAsyncOperation("fetchPlaybackLogsThread-" + browserSessionId,  fetchOperation);
                response.setIsSuccess(true);
            }else{
                response.setIsSuccess(false);
                response.setResponse("Failed to find the user session information for user session "+userSession);
            }
        }else{
            response.setIsSuccess(false);
            response.setResponse("Failed to find the user session information for browser session "+browserSessionId);
        }

        return response;
    }

    @Override
    public LoadedProgressIndicator<Collection<LogMetadata>> getFetchLogsAvailableForPlaybackProgress(String browserSessionId){

        long start = System.currentTimeMillis();

        if(logger.isInfoEnabled()){
            logger.info("getFetchLogsAvailableForPlaybackProgress called: " + browserSessionId);
        }

        LoadedProgressIndicator<Collection<LogMetadata>> response = browserSessionToFetchPlaybackLogsProgress.get(browserSessionId);
        if (response == null) {

            response = new LoadedProgressIndicator<>();
            response.setException(
                    new DetailedException("Unable to find fetch playback logs progress indicator for session id: " + browserSessionId, 
                            "Perhaps the fetch for that session id has completed or was never started successfully.", null));
            response.setComplete(true);
        } else if (response.isComplete()) {
            browserSessionToFetchPlaybackLogsProgress.remove(browserSessionId);
        }

        metrics.endTrackingRpc("getFetchLogsAvailableForPlaybackProgress", start);

        return response;
    }

    @Override
    public GenericRpcResponse<Long> jumpToActivationStart(String browserSessionKey,
            String taskConceptName) {
        try {
            return new SuccessfulResponse<>(ksmManager.jumpToActivationStart(browserSessionKey, taskConceptName));
        } catch (Throwable t) {
            return new FailureResponse<>(new DetailedException(
                    "Failed to jump to the activation start time for " + taskConceptName + ".",
                    "Failed to jump to the activation start time for " + taskConceptName + " because " + t.getMessage(),
                    t));
        }
    }

    @Override
    public GenericRpcResponse<SessionTimelineInfo> fetchLearnerStatesForSession(String browserSessionKey) {
        try {
            return new SuccessfulResponse<>(ksmManager.fetchLearnerStates(browserSessionKey));
        } catch (DetailedException detailedEx) {
            return new FailureResponse<>(detailedEx);
        }
    }

    @Override
    public GenericRpcResponse<String> editLogPatchForPerformanceStateAttribute(String browserSessionKey,
            String username, long timestamp, PerformanceStateAttribute performanceState) {
        try {
            return ksmManager.editLogPatchForPerformanceStateAttribute(browserSessionKey, username,
                    timestamp, performanceState);
        } catch (DetailedException detailedEx) {
            return new FailureResponse<>(detailedEx);
        }
    }

    @Override
    public GenericRpcResponse<String> createLogPatchForEvaluatorUpdate(String browserSessionKey, String username,
            long timestamp, boolean updateEntireSpan, EvaluatorUpdateRequest evaluatorUpdateRequest, boolean applyToFutureStates) {
        try {
            return ksmManager.createLogPatchForEvaluatorUpdate(browserSessionKey, username, timestamp,
                    updateEntireSpan, evaluatorUpdateRequest, applyToFutureStates);
        } catch (DetailedException detailedEx) {
            return new FailureResponse<>(detailedEx);
        }
    }

    @Override
    public GenericRpcResponse<String> removeLogPatchForAttribute(String browserSessionKey, String username,
            long timestamp, PerformanceStateAttribute performanceState) {
        try {
            String logFileName = ksmManager.removeLogPatchForAttribute(browserSessionKey, username, timestamp,
                    performanceState);
            return new SuccessfulResponse<>(logFileName);
        } catch (DetailedException detailedEx) {
            return new FailureResponse<>(detailedEx);
        }
    }

    @Override
    public GenericRpcResponse<Void> deleteSessionLogPatch(String browserSessionKey) {
        try {
            ksmManager.deleteSessionLogPatch(browserSessionKey);
            return new SuccessfulResponse<>();
        } catch (DetailedException detailedEx) {
            return new FailureResponse<>(detailedEx);
        }
    }

    @Override
    public GenericRpcResponse<Void> registerKnowledgeSessionPlayback(String browserSessionKey, LogMetadata log) {
        try {
            ksmManager.registerBrowserPlayback(browserSessionKey, log);
            return new SuccessfulResponse<>();
        } catch (Throwable t) {
            String msg = "There was a problem starting playback for the following log: " + log;
            DetailedException detailedEx = new DetailedException("Start Playback Error", msg, t);
            return new FailureResponse<>(detailedEx);
        }
    }

    @Override
    public GenericRpcResponse<Void> deregisterKnowledgeSessionPlayback(String browserSessionKey) {
        ksmManager.deregisterBrowserPlayback(browserSessionKey);
        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> updateGameMasterAutoState(String browserSessionKey, boolean isAutoMode) {
        if (isBlank(browserSessionKey)) {
            return new FailureResponse<>(new DetailedException(
                    "Unable to update the game master auto state because the browserSessionKey is null.",
                    "Unable to update the game master auto state because the browserSessionKey is null.", null));
        }

        /* Updates the game master's 'auto' state */
        ksmManager.updateGameMasterAutoState(browserSessionKey, isAutoMode);

        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> cacheProcessedStrategy(String browserSessionKey,
            Collection<ProcessedStrategyCache> strategyCache) {
        if (isBlank(browserSessionKey)) {
            return new FailureResponse<>(new DetailedException(
                    "Unable to cache the processed strategy because the browserSessionKey is null.",
                    "Unable to cache the processed strategy because the browserSessionKey is null.", null));
        }

        /* Caches the processed strategy */
        ksmManager.cacheProcessedStrategy(browserSessionKey, strategyCache);

        return new SuccessfulResponse<>();
    }
    
    @Override
    public GenericRpcResponse<Void> cacheProcessedBookmark(String browserSessionKey,
            ProcessedBookmarkCache cache) {
        if (isBlank(browserSessionKey)) {
            return new FailureResponse<>(new DetailedException(
                    "Unable to cache the processed bookmark because the browserSessionKey is null.",
                    "Unable to cache the processed bookmark because the browserSessionKey is null.", null));
        }

        /* Caches the processed strategy */
        ksmManager.cacheProcessedBookmark(browserSessionKey, cache);

        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> cleanupCreateDataCollectionItemProgressIndicator(String username) {
        DataCollectionManager.getInstance().cleanupCreateDataCollectionItemProgressIndicator(username);
        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> cleanupCourseExportProgressIndicator(String username) {
        DataCollectionManager.getInstance().cleanupCourseExportProgressIndicator(username);
        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> cleanupRawDataExportProgressIndicator(String username) {
        DataCollectionManager.getInstance().cleanupRawDataExportProgressIndicator(username);
        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> cleanupDeleteDataCollectionItemProgressIndicator(String username) {
        DataCollectionManager.getInstance().cleanupDeleteDataCollectionItemProgressIndicator(username);
        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> setSessionPlaybackTime(String browserSessionKey, long time) {
        try {
            ksmManager.setPlaybackTime(browserSessionKey, time);
            return new SuccessfulResponse<>();
        } catch (Throwable t) {
            return new FailureResponse<>(new DetailedException("Seek failed to process.",
                    "Seek failed to process because " + t.getMessage(), t));
        }
    }

    @Override
    public GenericRpcResponse<Void> startSessionPlayback(String browserSessionKey) {
        ksmManager.startPlayback(browserSessionKey);
        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> stopSessionPlayback(String browserSessionKey) {
        ksmManager.stopPlayback(browserSessionKey);
        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> updateSessionTeamOrgFilterState(String browserSessionKey, int domainSessionId, Map<String, Boolean> teamRolesSelected) {
        ksmManager.updateSessionTeamOrgFilterState(browserSessionKey, domainSessionId, teamRolesSelected);
        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> setGatewayConnections(String browserSessionKey,
            GatewayConnection gatewayConnection) {

        try {
            ksmManager.setGatewayConnections(browserSessionKey, gatewayConnection);

        } catch(DetailedException de) {

            logger.error("Unable to connect to the following Gateway connections to send playback data.\n"+gatewayConnection, de);
            return new FailureResponse<>(de);

        } catch(Exception e) {

            String connectionNames = StringUtils.join(Constants.COMMA, gatewayConnection.getTaTypes());
            if(gatewayConnection.shouldUseDIS()){                
                connectionNames += connectionNames.isEmpty() ? "DIS" : ", DIS";
            }

            logger.error("Unable to connect to the following Gateway connections to send playback data.\n"+gatewayConnection, e);
            return new FailureResponse<>(new DetailedException(
                    "The server was unable to connect to the Gateway connections of "+connectionNames+" and cannot send "
                            + "playback data to it as a result.<br/><br/>Are " + connectionNames +
                            " currently running and configured to connect to GIFT according to the interop configuration "+
                            "in GIFT\\config\\gateway\\configurations\\default.interopConfig.xml?",
                    "Unable to establish Gateway connection to send playback data to "
                            + connectionNames + " Gateway connections",
                    e));
        }

        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<Void> setExternalMonitorConfig(String browserSessionKey, ExternalMonitorConfig config) {

        try {
            ksmManager.setExternalMonitorConfig(config);

        } catch(DetailedException de) {

            logger.error("Unable to send configuration settings to external monitor application. Reason: ", de);
            return new FailureResponse<>(de);

        } catch(Exception e) {

            logger.error("Unable to send configuration settings to external monitor application. Reason: ", e);
            return new FailureResponse<>(new DetailedException(
                    "The server was unable to apply your settings to the application that playback is being shared with.",
                    "An unexpected error was encountered while sending settings to an external monitor application",
                    e));
        }

        return new SuccessfulResponse<>();
    }

    @Override
    public GenericRpcResponse<GatewayConnection> getGatewayConnection(String browserSessionKey) {

        try {
            return new SuccessfulResponse<>(ksmManager.getGatewayConnection());

        } catch(Exception e) {

            //determine which user is making this request
            BrowserWebSession browserSession = userSessionManager.getBrowserSession(browserSessionKey);
            if(browserSession != null) {

                UserWebSession userSession = userSessionManager.getUserSession(browserSession.getUserSessionKey());
                if(userSession != null) {
                    logger.error("Unable to determine the training application that has been connected to in order"
                            + "to send playback data for user '" + userSession + "'. Reason: ", e);
                } else {
                    logger.error("Unable to determine the training application that has been connected to in order"
                            + "to send playback data for browser session '" + browserSession + "'. Reason: ", e);
                }

            } else {
                logger.error("Unable to determine the training application that has been connected to in order"
                        + "to send playback data for browser session key '" + browserSessionKey + "'. Reason: ", e);
            }

            if(e instanceof DetailedException) {
                return new FailureResponse<>((DetailedException) e);

            } else {
                return new FailureResponse<>(new DetailedException(
                        "The server was unable to apply your settings to the application that playback is being shared with.",
                        "An unexpected error was encountered while sending settings to an external monitor application",
                        e));
            }
        }
    }

    @Override
    public GenericRpcResponse<Void> cleanupGameMaster(String browserSessionKey) {

        /* Stop playback for this scenario. */
        final GenericRpcResponse<Void> stopPlaybackResult = deregisterKnowledgeSessionPlayback(browserSessionKey);

        /* If stopping playback failed, return its error response. */
        if (stopPlaybackResult instanceof FailureResponse<?>) {
            return stopPlaybackResult;
        }

        ksmManager.cleanupSession(browserSessionKey);

        /* If disconnecting the connection to the training app fails, return its
         * error response. */
        final GenericRpcResponse<Void> trainingAppStopResponse = setGatewayConnections(browserSessionKey, null);
        if (trainingAppStopResponse instanceof FailureResponse<?>) {
            return trainingAppStopResponse;
        }

        return new SuccessfulResponse<>(null);
    }

    @Override
    public GenericRpcResponse<SIDC> getSidcForRole(String browserSessionKey, int domainSessionId, String roleName){
        try {
            return new SuccessfulResponse<>(ksmManager.getSidcForRole(browserSessionKey, roleName));

        } catch(DetailedException de) {

            logger.error("Unable to get SIDC for role " + roleName + ". Reason: ", de);
            return new FailureResponse<>(de);

        } catch(Exception e) {

            logger.error("Unable to get SIDC for role " + roleName + ". Reason: ", e);
            return new FailureResponse<>(new DetailedException(
                    "The server was unable to retrieve a symbol for " + roleName + ".",
                    "An unexpected error was encountered while determining which symbol to use",
                    e));
        }
    }

    @Override
    public GenericRpcResponse<String> getWorkspaceFilePreviewUrl(String userName, String relativeFileName){

        // Ensure arguments are not null
        if(userName == null) {
            return new FailureResponse<>(new DetailedException(
                    "An error occurred while obtaining a preview URL for a workspace file.",
                    "Can't fetch content address because the userName was empty or undefined.",
                    null));
        }

        if(relativeFileName == null) {
            return new FailureResponse<>(new DetailedException(
                    "An error occurred while obtaining a preview URL for a workspace file.",
                    "Can't fetch content address because the relativeFileName was empty or undefined.",
                    null));
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
        return new SuccessfulResponse<>(contentURL);
    }
    
    /**
     * 
     * Attempts to copy the specified course to the users workspace. Checks 
     * for naming conflicts between the provided courses and the new course name.
     *
     * @param courseData The domain option (course data) to copy.  This contains the location of the course being copied.
     * @param username Name of the user whose workspace is being copied to. Can't be empty or null.
     * @param userSessionId  Used to store info in the copy course result. 
     * @param browserSessionId User's browser session Id.  Can't be null.
     * @param newCourseName The name to give the copied course. This is useful in case the course is being copied to the
     * same workspace which would cause a course name collision.
     * @param editableToUsernames a set of GIFT usernames to give write access to in the survey context (and descendant element in the survey context).
     * If toPublicWorkspace is true and this set is not empty, the set is used.
     * If toPublicWorkspace is true and this set is null or empty, no user will have write access.
     * If toPublicWorkspace is false and this set is not empty, the set is used.
     * If toPublicWorkspace is false and this set is null or empty, the username found in the userSession is used as the only user with read access.
     * @param visibleToUsernames  a set of GIFT usernames to give read access to in the survey context (and descendant elements in the survey context).
     * If toPublicWorkspace is true than this parameter is ignored and a wildcard is used to everyone has read access.
     * @param toPublicWorkspace whether the course is being copied to the Public workspace. If true the provider user will need to havewrite access to that workspace.
     * @param userSession The user session from the client.  Used to look up the username.
     * @param coursesList The list of courses available to the user. Used to check for name conflicts.
     * @param progResponse Indicator to keep user updated on copy progress.
     */
    public void copyCourse(DomainOption courseData, String username, String userSessionId, String browserSessionId, String newCourseName,
            HashSet<String> editableToUsernames, HashSet<String> visibleToUsernames, boolean toPublicWorkspace, 
            Collection<DomainOption> coursesList, LoadedProgressIndicator<CopyCourseResult> progResponse) {

        CopyCourseResult result = new CopyCourseResult();
        result.setIsSuccess(false);
        result.setUserSessionId(userSessionId);
        result.setBrowserSessionId(browserSessionId);
        try {

            if(logger.isDebugEnabled()){
                logger.debug("copyCourse thread start()");
        }

            //
            // First: Check the course list for a course with the same name (case insensitive) in the destination workspace
            //

            boolean conflict = false;

            for(DomainOption course : coursesList) {

                if(toPublicWorkspace){
                    //check the public workspace for course name conflict

                    if(course.getDomainId().startsWith(AbstractFileServices.PUBLIC_WORKSPACE_FOLDER_NAME) ||
                            course.getDomainId().startsWith(AbstractFileServices.PUBLIC_WORKSPACE_FOLDER_NAME, 1)){

                        if(course.getDomainName().equalsIgnoreCase(newCourseName)) {
                            conflict = true;
                            break;
                        }
                    }

                }else{
                    //check the user's workspace for course name conflict

                    if(course.getDomainId().startsWith(username)
                            || course.getDomainId().startsWith(Constants.FORWARD_SLASH + username)) {

                        if(course.getDomainName().equalsIgnoreCase(newCourseName)) {
                            conflict = true;
                            break;
                        }

                    }
                }
            }//end for

            if(conflict) {
                //let the watcher of this copy course progress know about the conflict which is
                //also ending this logic
                result.setCourseAlreadyExists(true);

            } else {

                progResponse.setPercentComplete(0);
                progResponse.setTaskDescription("Copying course files...");

                progResponse.setSubtaskProgressIndicator(new ProgressIndicator());

                if(logger.isInfoEnabled()){
                    logger.info("Adding asyncprogress response: " + progResponse);
                }
                usernameToCopyProgress.put(username, progResponse);

                AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();

                //Get destination workspace folder
                String destinationFolder;
                if(toPublicWorkspace){
                    destinationFolder = AbstractFileServices.PUBLIC_WORKSPACE_FOLDER_NAME;
                }else{
                    destinationFolder = username;
                }

                //Get destination course folder path with the appropriate workspace
                String newCourseFolderPath;
                if(toPublicWorkspace){
                    newCourseFolderPath = AbstractFileServices.PUBLIC_WORKSPACE_FOLDER_NAME + File.separator + newCourseName;
                }else{
                    newCourseFolderPath = username + File.separator + newCourseName;
                }

                String originalCourse = FileTreeModel.correctFilePath(courseData.getDomainId());
                String originalCourseFolder = FileTreeModel.createFromRawPath(originalCourse).getParentTreeModel().getRelativePathFromRoot();

                //relative to the workspace folder (e.g. Desktop = workspace, Server = Workspaces)
                String newCourseXMLPath = newCourseFolderPath + Constants.FORWARD_SLASH + newCourseName + AbstractSchemaHandler.COURSE_FILE_EXTENSION;

                progResponse.setPercentComplete(5);

                Set<String> sharedSurveyGiftKeys = null;
                try {
                    FileTreeModel originalCourseTree = fileServices.getFileTreeByPath(username, originalCourseFolder);

                    // Copy course files
                    fileServices.createFolder(username, destinationFolder, newCourseName, false);

                    List<FileTreeModel> files = originalCourseTree.getSubFilesAndDirectories();

                    int index = 0;
                    int length = files.size();

                    AbstractFolderProxy courseFolder = null;
                    for(FileTreeModel file : files) {

                        if(file.getFileOrDirectoryName().endsWith(FileUtil.BACKUP_FILE_EXTENSION)) {
                            // Don't copy any backup files that may have been created if the course was imported
                            continue;
                        }

                        if(file.getFileOrDirectoryName().endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION)) {
                            //found a course file

                            //
                            // Rename the course file
                            //

                            //copy the original course.xml to the new course folder
                            String copyPath = fileServices.copyWorkspaceFile(username, originalCourse, newCourseXMLPath, NameCollisionResolutionBehavior.OVERWRITE, progResponse.getSubtaskProcessIndicator());

                            //
                            // Set the course.xml name attribute (as part of the rename)
                            //
                            try{

                                courseFolder = fileServices.getCourseFolder(copyPath, username);
                                FileProxy courseFileProxy = courseFolder.getRelativeFile(FileTreeModel.createFromRawPath(newCourseXMLPath).getFileOrDirectoryName());
                                DomainCourseFileHandler courseHandler = new DomainCourseFileHandler(courseFileProxy, courseFolder, false);

                                Course newCourse = courseHandler.getCourse();
                                if(newCourseName != null) {
                                    newCourse.setName(newCourseName);
                                }


                                // determine which surveys don't need to be copied because the survey course object is using
                                // a shared survey
                                sharedSurveyGiftKeys = courseHandler.getSharedSurveyGiftKeys();

                                // Write the updated course xml to disk
                                fileServices.marshalToFile(username, newCourse, copyPath, null);

                            }catch (FileNotFoundException e) {
                                throw new DetailedException("Failed to change the course name value because there was a problem finding the course folder for the course.xml file.",
                                        "An exception was thrown that reads:\n"+e.getLocalizedMessage(),
                                        e);
                            }catch (IOException e) {
                                  throw new DetailedException("Failed to change the course name value because there was a problem while trying to read or write the course.xml file.",
                                            "An exception was thrown that reads:\n"+e.getLocalizedMessage(),
                                            e);
                            }

                        } else {

                            //copy other resource files needed by the original course to the destination course folder
                            fileServices.copyWorkspaceFile(username, file.getRelativePathFromRoot(), newCourseFolderPath, NameCollisionResolutionBehavior.OVERWRITE, progResponse.getSubtaskProcessIndicator());
                        }

                        index++;

                        //incrementally update the progress per file, up to a max of 75%
                        progResponse.setPercentComplete(Math.round(5 + index / (float) length * 70));
                    }
                    
                    // create the course in the database
                    final DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
                    CourseRecord courseRecord = dbServices.createCourseRecordIfNeeded(username, newCourseXMLPath.replace("\\", "/"), true);
                    if (courseRecord == null) {
                        throw new DetailedException("Failed to create the course record in the database for the course '"+newCourseXMLPath+"'.", 
                                "The course record could not be created.", null);
                    }

                    // make sure a 'course tile' published course exists for all courses that are created (in this case a new course from 
                    // a course copy).  This way the data for those that take this course can be analyzed using the publish course page.
                    ServicesManager.getInstance().getDataCollectionServices().createDefaultDataCollectionItemIfNeeded(username, courseRecord);

                    progResponse.setTaskDescription("Copying survey resources...");

                    // Create survey context copy
                    SurveyContext surveyContext = null;

                    if(courseData.getSurveyContextId() != null) {
                        surveyContext = Surveys.getSurveyContext(courseData.getSurveyContextId(), false);
                    }

                    if(surveyContext != null) {
                        //the surveys for the course need to be copied

                        Session session = Surveys.createSession();
                        session.beginTransaction();

                        ExternalSurveyMapper mapper = new ExternalSurveyMapper();

                        //set the permissions of the common survey context object
                        //this will not change the permissions of the survey context id being imported (the original/source survey context)
                        if(toPublicWorkspace){

                            //ALWAYS set wildcard so that everyone can see the course survey context
                            // NOTE: this ignores the parameterized set provided to this method
                            surveyContext.setVisibleToUserNames(new HashSet<>(Arrays.asList(mil.arl.gift.common.survey.Constants.VISIBILITY_WILDCARD)));

                            if(editableToUsernames != null && !editableToUsernames.isEmpty()){
                                //set custom list of editors
                                surveyContext.setEditableToUserNames(editableToUsernames);
                            }

                        }else{

                            HashSet<String> finalVisibleToUsernames;
                            if(visibleToUsernames != null && !visibleToUsernames.isEmpty()){
                                //set custom list of users that can see this new survey context
                                finalVisibleToUsernames = visibleToUsernames;
                            }else{
                                //custom list was not provided, use default which is the username of the workspace the course came from
                                finalVisibleToUsernames = new HashSet<>(Arrays.asList(username));
                            }
                            surveyContext.setVisibleToUserNames(finalVisibleToUsernames);

                            HashSet<String> finalEditableToUsernames;
                            if(editableToUsernames != null && !editableToUsernames.isEmpty()){
                                //set custom list of users that can edit this new survey context
                                finalEditableToUsernames = editableToUsernames;
                            }else{
                                //custom list was not provided, use default which is the username of the workspace the course came from
                                finalEditableToUsernames = new HashSet<>(Arrays.asList(username));
                            }
                            surveyContext.setEditableToUserNames(finalEditableToUsernames);
                        }
                        
                        /* Look at the surveys within the survey context to update the location that
                         * they pull their media files from. */
                        for(SurveyContextSurvey contextSurvey : surveyContext.getContextSurveys()) {
                            
                            if(contextSurvey.getSurvey() == null) {
                                continue;
                            }
                            
                            Survey survey = contextSurvey.getSurvey();
                            if(survey.getProperties() != null 
                                    && survey.getProperties().hasProperty(SurveyPropertyKeyEnum.MEDIA_FILE_SOURCE)) {
                                
                                /* Update the survey to pull its media files from the new course folder */
                                survey.getProperties().setPropertyValue(
                                        SurveyPropertyKeyEnum.MEDIA_FILE_SOURCE, 
                                        newCourseFolderPath);
                            }
                        }

                        // Create new survey rows and update survey references in gift files
                        try {

                            try{
                                //save new survey context that has new ids
                                surveyContext.setName(newCourseName);
                                surveyContext = Surveys.insertExternalSurveyContext(surveyContext, mapper, sharedSurveyGiftKeys, session);

                            }catch(Exception e) {
                                //add a message about what specifically failed, throw exception to the next try to deal with cleanup
                                throw new Exception("Failed to insert a new survey context.", e);
                            }

                            progResponse.setPercentComplete(80);

                            HashMap<FileProxy, AbstractSchemaHandler> filesToUpdate = new HashMap<>();

                            progResponse.setTaskDescription("Updating survey references...");
                            progResponse.setPercentComplete(90);

                            //Note: not placing try/catch around this line because try/catch it is currently in
                            //      handles its possible exceptions as well as provides a mention of copying survey resources
                            //      in the error message
                            Map<String, String> fileToNewContent = ServicesManager.getInstance().getDbServices().copyCourseSurveyReferences(username, 
                                    newCourseXMLPath, newCourseName, courseFolder, mapper, filesToUpdate);
                            for(String file : fileToNewContent.keySet()){
                                fileServices.updateFileContents(username, file, fileToNewContent.get(file), false, false);
                            }


                            session.getTransaction().commit();
                            session.close();

                            progResponse.setPercentComplete(100);

                        } catch (DetailedException e) {

                            result.setErrorMessage("There was a problem copying the course's survey resources. " + e.getReason());
                            result.setErrorDetails(e.getDetails());
                            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));

                            session.close();

                            // Clean up the copied files
                            fileServices.deleteFile(username, browserSessionId, newCourseFolderPath, null);

                        } catch (Exception e) {

                            result.setErrorMessage("There was a problem copying the course's survey resources.");
                            result.setErrorDetails(e.getMessage());
                            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));

                            session.close();

                            // Clean up the copied files
                            fileServices.deleteFile(username, browserSessionId, newCourseFolderPath, null);
                        }

                    }//end if on survey context

                    if(result.getErrorMessage() == null){
                        result.setIsSuccess(true);
                    }

                } catch (@SuppressWarnings("unused") FileExistsException e) {
                    result.setCourseAlreadyExists(true);

                } catch (DetailedException e) {
                    result.setErrorMessage(e.getReason());
                    result.setErrorDetails(e.getDetails());
                    result.setErrorStackTrace(e.getErrorStackTrace());

                    // Clean up any copied files
                    fileServices.deleteFile(username, browserSessionId, newCourseFolderPath, null);

                } catch (Exception e) {
                    result.setErrorMessage("An error occurred while copying the course.");
                    
                    String details = e.getMessage();
                    if(StringUtils.isBlank(details)){
                        details = e.getLocalizedMessage();
                        if(StringUtils.isBlank(details) && e.getStackTrace() != null){
                            details = e.getStackTrace().length > 0  ? e.getStackTrace()[0].toString() : "unable to extract the reason from the exception.  Please look at the stack trace.";
                        }
                    }
                    result.setErrorDetails(details);
                    result.setErrorStackTrace(DetailedException.getFullStackTrace(e));

                    // Clean up the copied files
                    fileServices.deleteFile(username, browserSessionId, newCourseFolderPath, null);

                } finally{
                    // Nothing to do here yet.
                }
            }

            // A result MUST be returned for the operation to be considered 'finished'.
            progResponse.setPayload(result);

            if(logger.isDebugEnabled()){
                logger.debug("copyCourse thread end(): " +  progResponse);
            }
        } catch (Throwable t) {
            logger.error("Caught exception running copyCourse thread: ", t);

            result.setErrorMessage("There was an error while copying the course.");
            result.setErrorDetails("The server threw an error - "+t.toString());
            result.setErrorStackTrace(DetailedException.getFullStackTrace(t));

            // A result MUST be returned for the operation to be considered 'finished'.
            progResponse.setPayload(result);
        }
    }
    
    /**
     * Deletes the provided courses and related survey contexts for a specified user.
     * 
     * @param courses The list of courses to delete. Can't be null or empty.
     * @param username The name of the user to delete survey contexts for. Can't be null or empty.
     * @param browserSessionId Used to lock files when deleting. Can't be null or empty.
     * @param deleteSurveyResponses Whether or not to delete survey responses.
     * @param skipSurveyResources Whether or not to skip survey resources.
     * @param response Used to store useful information about delete process. Useful for errors. Can't be null.
     * @param progress Indicator to keep user updated on delete progress.
     * @param fileServices Used to delete specific files found in a course. Can't be null
     */
    public void deleteCourses(List<DomainOption> courses, String username, String browserSessionId, boolean deleteSurveyResponses,
            boolean skipSurveyResources, DeleteCourseResult response, ProgressIndicator progress, AbstractFileServices fileServices) {
        
        if(courses.isEmpty()){
            response.setIsSuccess(true);
            return;
        }
        
        for (DomainOption course : courses) {
            try {
                progress.setTaskDescription("Locating '"+course.getDomainName()+"'...");

                progress.increasePercentComplete(10);

                // Delete survey context (and surveys)
                if(course.getSurveyContextId() != null && !skipSurveyResources) {

                    progress.setTaskDescription("Deleting survey resources for '"+course.getDomainName()+"'...");
                    try{
                        DeleteSurveyContextResponse deleteResult = Surveys.deleteSurveyContextAndResponses(
                                course.getSurveyContextId(), username, deleteSurveyResponses);

                        response.setDeleteSurveyFailed(!deleteResult.isSuccess());

                        if(!deleteResult.isSuccess()) {

                            response.setHadSurveyResponses(deleteResult.hadSurveyResponses());
                            response.setResponse(deleteResult.getResponse());
                            response.setErrorStackTrace(deleteResult.getErrorStackTrace());
                            response.setIsSuccess(false);
                            response.setCourseWithIssue(course);

                            return;
                        }

                    }catch(Exception e){

                        logger.error("Failed to delete the survey context for '"+username+"' when attempting to delete the course of "+course, e);

                        response.setDeleteSurveyFailed(true);
                        response.setResponse("Could not delete the course surveys and any survey responses because an exception was thrown.");
                        response.setAdditionalInformation("An attempt was made to delete the course folder despite this error.");
                        response.setErrorStackTrace(DetailedException.getFullStackTrace(e));

                        response.setIsSuccess(false);
                        response.setCourseWithIssue(course);

                        return;

                    }
                }

                // Check if there are any dependent published courses for the course.  If so, there may need to be some updates
                // done for the published course to show that the source course no longer exists.
                DataCollectionServicesInterface expServices = ServicesManager.getInstance().getDataCollectionServices();

                if (expServices.doesCourseHaveDataCollectionDataSets(username, course.getDomainId())){
                    expServices.endDataCollectionDataSets(username,  course.getDomainId());
                }

                FileTreeModel courseFolderToDelete = FileTreeModel.createFromRawPath(course.getDomainId()).getParentTreeModel();

                boolean success = fileServices.deleteFile(username, browserSessionId, courseFolderToDelete.getRelativePathFromRoot(), progress);

                if(success){

                    // Delete the course record for the course.
                    // Do this last, in case of failure with other, above, logic.
                    DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
                    dbServices.deleteCourseRecord(course.getDomainId());

                }else{
                    response.setResponse("Could not delete '" + courseFolderToDelete.getRelativePathFromRoot(true)+"'.");
                    response.setAdditionalInformation("The server was unable to delete the course.  This normally happens because the server can't find the course.  Does the name look correct?");
                }
                response.setIsSuccess(success);

            } catch (DetailedException e) {

                response.setIsSuccess(false);
                response.setCourseWithIssue(course);
                response.setResponse(e.getReason());
                response.setAdditionalInformation(e.getDetails());
                response.setErrorStackTrace(e.getErrorStackTrace());

            } catch (Throwable e) {

                response.setIsSuccess(false);
                response.setCourseWithIssue(course);
                response.setResponse("Could not delete " + course.getDomainId());
                response.setAdditionalInformation(e.getMessage());
                response.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            }
            if(!response.isSuccess()){
                //bail out on first failure
                break;
            }
        } // end for
    }

        
    @Override
    public GenericRpcResponse<SessionScenarioInfo> getKnowledgeSessionScenario(String browserSessionKey, String username){
        try {
            SessionScenarioInfo scenario = ksmManager.getKnowledgeSessionScenario(browserSessionKey, username);
            return new SuccessfulResponse<>(scenario);
        } catch (DetailedException detailedEx) {
            return new FailureResponse<>(detailedEx);

        }
    }
    
    @Override
    public GenericRpcResponse<String> publishKnowledgeSessionOverallAssessments(String browserSessionKey, String username, long timestamp, Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments, List<String> courseConcepts){
        try {
            return ksmManager.publishKnowledgeSessionOverallAssessments(browserSessionKey, username, timestamp, conceptToConditionAssessments, courseConcepts);
        } catch (DetailedException detailedEx) {
            return new FailureResponse<>(detailedEx);
        }
    }
    
    @Override
    public GenericRpcResponse<Map<Integer, AssessmentLevelEnum>> calculateRollUp(String browserSessionKey, String username, Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments){
        try {
            return ksmManager.calculateRollUp(browserSessionKey, username, conceptToConditionAssessments);
        } catch (DetailedException detailedEx) {
            return new FailureResponse<>(detailedEx);
        }
    }
    
    @Override
    public GenericRpcResponse<WebMonitorStatus> registerMonitorService(String username, String browserSession) {
        try {
            return new SuccessfulResponse<>(monitorManager.registerBrowser(browserSession));
            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);
                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while registering for the web monitor service.", 
                    "Could not register the browser session " + browserSession + " for user " + username
                        + " to use the web monitor service", 
                    e));
        }
    }
    
    @Override
    public GenericRpcResponse<Void> deregisterMonitorService(String username, String browserSession) {
        try {
            monitorManager.deregisterBrowser(browserSession);
            return new SuccessfulResponse<>();
            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);
                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while deregistering for the web monitor service.", 
                    "Could not deregister the browser session " + browserSession + " for user " + username
                        + " to stop using the web monitor service", 
                    e));
        }
    }

    @Override
    public GenericRpcResponse<Void> launchModules(String username, String browserSession, List<ModuleTypeEnum> modules) {
        try {
            monitorManager.launchModules(modules);
            
            return new SuccessfulResponse<>();
            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);
                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while launching the specified module(s).", 
                    "An unexpected exception occurred while launching " + modules, 
                    e));
        }
    }
    
    @Override
    public GenericRpcResponse<Void> killModules(String username, String browserSession, List<ModuleTypeEnum> modules, String targetQueue) {
        try {
            monitorManager.killModules(modules, targetQueue);
            
            return new SuccessfulResponse<>();
            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);
                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while killing the specified module(s).", 
                    "An unexpected exception occurred while killing " + modules, 
                    e));
        }
    }
    
    @Override
    public GenericRpcResponse<MessageDisplayData> getMessageDisplayData(String username, String browserSession, Integer domainSessionId, MessageEntryMetadata message) {
        try {
            return new SuccessfulResponse<>(monitorManager.getDisplayData(domainSessionId, message));
            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);
                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while getting display data for a message", 
                    "An unexpected exception occurred while getting the display information for " + message, 
                    e));
        }
    }
    @Override
    public GenericRpcResponse<Void> setListening(String browserSession, boolean listening, Integer domainSessionId) {
        try {
            monitorManager.setListening(domainSessionId, listening);            
            return new SuccessfulResponse<>();            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while setting the listening state of the web monitor.", 
                    "An unexpected exception occurred while setting the listening state: ", 
                    e));
        }
    }
    
    @Override
    public GenericRpcResponse<Void> watchDomainSession(String username, String browserSession, int domainSessionId) {
        
        try {
            monitorManager.watchDomainSession(browserSession, domainSessionId);
            
            return new SuccessfulResponse<>();
            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);
                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while attempting to watch a domain session", 
                    "An unexpected exception occurred while trying to watch " + domainSessionId, 
                    e));
        }
    }
    
    @Override
    public GenericRpcResponse<Void> entityFilter(String entityMarking, String browserSession, int domainSessionId) {
        
        try {
            monitorManager.entityFilter(entityMarking, domainSessionId);
            
            return new SuccessfulResponse<>();
            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while attempting to filer by entity marking.",
                    "An unexpected exception occured while applying a filter to messages for domain tab " + domainSessionId,
                    e));
        }
    }
    
    @Override
    public GenericRpcResponse<Void> setAdvancedHeader(String browserSession, boolean advancedHeader, Integer domainSessionId) {
        try {
            monitorManager.setAdvancedHeader(domainSessionId, advancedHeader);            
            return new SuccessfulResponse<>();            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while setting the advanced header state of the web monitor.", 
                    "An unexpected exception occurred while setting the listening state: " + advancedHeader, 
                    e));
        }
    }
    
    @Override
    public GenericRpcResponse<Void> setMessageFilter(String username, String browserSession, Integer domainSessionId, Set<MessageTypeEnum> selectedChoices){
        
        try {
            monitorManager.setFilterChoices(domainSessionId, selectedChoices);     
            return new SuccessfulResponse<>();            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while changing the message filter", 
                    "An unexpected exception occurred while selecting the message choices: " + selectedChoices, 
                    e));
        }
    }
    
    @Override
    public GenericRpcResponse<Void> unwatchDomainSession(String browserSession, Integer domainSessionId){
        try {
            monitorManager.unwatchDomainSession(browserSession, domainSessionId);
            return new SuccessfulResponse<>();
        } catch(DetailedException e) {
            return new FailureResponse<>(e);                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while closing the domain session", 
                    "An unexpected exception occurred while closing the domain session", 
                    e));
        }
    }
    
    @Override
    public GenericRpcResponse<Void> refreshPanel(String username, String browserSession, Integer domainSessionId){
        
        try {
            monitorManager.refreshPanel(browserSession, domainSessionId);     
            return new SuccessfulResponse<>();            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while refreshing a domain session panel", 
                    "An unexpected exception occurred while refreshing the panel for the domain session: " + domainSessionId, 
                    e));
        }
    }
    
    @Override
    public GenericRpcResponse<Void> refreshModules(String username, String browserSession){
        
        try {
            monitorManager.refreshModules(browserSession);     
            return new SuccessfulResponse<>();            
        } catch(DetailedException e) {
            return new FailureResponse<>(e);                    
        } catch(Exception e) {
            return new FailureResponse<>(new DetailedException(
                    "An error occured while refreshing module statuses", 
                    "An unexpected exception occurred while refreshing the modules statuses for the browser session: " 
                    + browserSession, 
                    e));
        }
    }
}