/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.EnumerationNotFoundException;

/**
 * Enumeration of the various message types that are sent between modules.
 *
 * @author mhoffman
 *
 */
public class MessageTypeEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;

    private static List<MessageTypeEnum> enumList = new ArrayList<MessageTypeEnum>();
    private static int index = 0;

    /**
     * Heartbeat messages
     */
    public static final MessageTypeEnum MODULE_ALLOCATION_REQUEST    = new MessageTypeEnum("ModuleAllocationRequest", "Module Allocation Request", "Sent by a module to another module of a different type to request the use of that module in a User Session.");
    public static final MessageTypeEnum MODULE_ALLOCATION_REPLY      = new MessageTypeEnum("ModuleAllocationReply", "Module Allocation Reply", "Contains the response to a Module Allocation Request (e.g. can the module handle another user).");
    public static final MessageTypeEnum MODULE_STATUS                = new MessageTypeEnum("ModuleStatus", "Module Status", "A module heartbeat letting other modules know of it's existence.");

    /**
     * Admin messages
     */
    public static final MessageTypeEnum KILL_MODULE             = new MessageTypeEnum("KillModule", "Kill Module", "Request that the module's executable gracefully terminates.");

    /**
     * Login messages
     */
    public static final MessageTypeEnum NEW_USER_REQUEST    = new MessageTypeEnum("NewUserRequest", "New User Request", "Used to register a new User to GIFT.");
    public static final MessageTypeEnum LOGIN_REQUEST       = new MessageTypeEnum("LoginRequest", "Login Request", "Used to login a user.");
    public static final MessageTypeEnum LOGIN_REPLY         = new MessageTypeEnum("LoginReply", "Login Reply", "Reply to a login attempt.");
    public static final MessageTypeEnum LOGOUT_REQUEST      = new MessageTypeEnum("LogOutRequest", "Logout Request", "Used to logout a user.");
    public static final MessageTypeEnum USER_ID_REQUEST     = new MessageTypeEnum("UserIdRequest", "User Id Request", "Used to get the user id for a user's account");
    public static final MessageTypeEnum USER_ID_REPLY       = new MessageTypeEnum("UserIdReply", "User Id Reply", "The user information requested.");
    public static final MessageTypeEnum SUBJECT_CREATED     = new MessageTypeEnum("SubjectCreated", "Subject Created", "A subject in an experiment was created.");
    public static final MessageTypeEnum LTI_GETUSER_REQUEST = new MessageTypeEnum("LtiGetUserRequest", "Lti Get User Request", "Used to get the lti user record.");
    public static final MessageTypeEnum LTI_GETUSER_REPLY   = new MessageTypeEnum("LtiGetUserReply", "Lti Get User Reply", "The LTI user record data.");

    /** LTI messages */
    public static final MessageTypeEnum LTI_GET_PROVIDER_URL_REQUEST = new MessageTypeEnum("LtiGetProviderUrlRequest", "Lti Get Provider URL Request", "Normally this request is sent from the TutorModule (originating from the client) to the Domain Module. This message is used to build an encrypted URL using the raw URL, optional custom parameters, and the LTI provider's client key and secret key. The encrypted URL will be used to connect and send a request to the LTI provider.");
    public static final MessageTypeEnum LTI_GET_PROVIDER_URL_REPLY   = new MessageTypeEnum("LtiGetProviderUrlReply", "Lti Get Provider URL Reply", "Contains the encrypted URL used to send the request to the LTI provider. Normally this reply is sent from the Domain Module back to the Tutor Module.");


    public static final MessageTypeEnum LMS_DATA_REQUEST = new MessageTypeEnum("LMSDataRequest", "LMS Data Request", "Used to request LMS data for a user.");
    public static final MessageTypeEnum LMS_DATA_REPLY   = new MessageTypeEnum("LMSDataReply", "LMS Data Reply", "The LMS data for a user.");

    /**
     * Sensor Modules messages
     */
    public static final MessageTypeEnum SENSOR_FILTER_DATA  = new MessageTypeEnum("SensorFilterData", "Sensor Filter Data", "Contains filtered sensor data.");
    public static final MessageTypeEnum SENSOR_DATA         = new MessageTypeEnum("SensorData", "Sensor Data", "Contains unfiltered/raw sensor data.");
    public static final MessageTypeEnum SENSOR_FILE_CREATED = new MessageTypeEnum("SensorFileCreated", "Sensor File Created", "Notification that a file has been created to store sensor data.");
    public static final MessageTypeEnum SENSOR_STATUS        = new MessageTypeEnum("SensorStatus", "Sensor Status", "The status of a sensor.  This can be used to indicate that a sensor is ready or that there is an issue.");

    /**
     * Learner module messages
     */
    public static final MessageTypeEnum INSTANTIATE_LEARNER_REQUEST = new MessageTypeEnum("InstantiateLearnerRequest", "Instantiate Learner Request", "Used to initialize a learner instance in a domain session.");
    public static final MessageTypeEnum LEARNER_STATE               = new MessageTypeEnum("LearnerState", "Learner State", "The state of the learner at a given point in time.  Includes Cognitive, Affective and Performance states represented across Short-term, Long-term and Predicted temporal categories.");

    /**
     * UMS module messages
     */
    public static final MessageTypeEnum DOMAIN_SELECTION_REQUEST    = new MessageTypeEnum("DomainSelectionRequest", "Domain Selection Request", "Sent by the Tutor describing the domain (i.e. course) selected by the user.");
    public static final MessageTypeEnum DOMAIN_SELECTION_REPLY      = new MessageTypeEnum("DomainSelectionReply", "Domain Selection Reply", "Response to the domain selection request that contains information about the newly started domain session.");

    public static final MessageTypeEnum GET_SURVEY_REQUEST          = new MessageTypeEnum("GetSurveyRequest", "Get Survey Request", "Retrieve the contents of a survey.");
    public static final MessageTypeEnum GET_SURVEY_REPLY            = new MessageTypeEnum("GetSurveyReply", "Get Survey Reply", "The contents of a survey that was requested.");
    public static final MessageTypeEnum SUBMIT_SURVEY_RESULTS       = new MessageTypeEnum("SubmitSurveyResults", "Submit Survey Results", "Contains the entire response to a survey.  This is sent from the Domain to UMS modules in order to store the survey responses.  This is not used for surveys not being stored in the survey database (e.g. Summarize/Highlight passage, mid-lesson survey).");
    public static final MessageTypeEnum SURVEY_CHECK_REQUEST        = new MessageTypeEnum("SurveyCheckRequest", "Survey Check Request", "Check if certain survey references exist.");
    public static final MessageTypeEnum SURVEY_CHECK_RESPONSE       = new MessageTypeEnum("SurveyCheckResponse", "Survey Check Response", "Response to the Survey Check Request that identifies if the survey references exists.");

    public static final MessageTypeEnum GET_EXPERIMENT_REQUEST          = new MessageTypeEnum("GetExperimentRequest", "Get Experiment Request", "Retrieve the metadata of an experiment (e.g. experiment id, author).");
    public static final MessageTypeEnum GET_EXPERIMENT_REPLY            = new MessageTypeEnum("GetExperimentReply", "Get Experiment Reply", "The metadata about an experiment that was requested (e.g. experiment id, author).");

    public static final MessageTypeEnum BRANCH_PATH_HISTORY_UPDATE      = new MessageTypeEnum("BranchPathHistoryUpdate", "Branch Path History Update", "Used to update the number of learners that have entered a course's branch path. This historical information is needed to determine the next learner's path.");
    public static final MessageTypeEnum BRANCH_PATH_HISTORY_REQUEST      = new MessageTypeEnum("BranchPathHistoryRequest", "Branch Path History Request", "Used to request the number of learners that have entered a course's branch path. This historical information is needed to determine the next learner's path.");
    public static final MessageTypeEnum BRANCH_PATH_HISTORY_REPLY      = new MessageTypeEnum("BranchPathHistoryReply", "Branch Path History Reply", "The number of learners that have entered a course's branch path. This historical information is needed to determine the next learner's path.");

    public static final MessageTypeEnum KNOWLEDGE_ASSESSMENT_DETAILS   = new MessageTypeEnum("KnowledgeAssessmentDetails", "Knowledge Assessment Details", "The details of ongoing assessments within a knowledge session, such as state information from condition classes. Used when sharing said details with an external strategy provider.");

    
    /**
     * Tutor module messages
     */
    public static final MessageTypeEnum INITIALIZE_DOMAIN_SESSION_REQUEST     = new MessageTypeEnum("InitializeDomainSessionRequest", "Initialize Domain Session Request", "Used to initialize and synchronize the various modules used in a domain session when a course begins.  Also contains information about the learner's client device (e.g. IP address, mobile app properties)");
    public static final MessageTypeEnum INITIALIZE_LESSON_REQUEST             = new MessageTypeEnum("InitializeLessonRequest", "Initialize Lesson Request", "Used to initialize and synchronize the logic used while in a training application course element in a course.");
    public static final MessageTypeEnum INITIALIZE_PEDAGOGICAL_MODEL_REQUEST  = new MessageTypeEnum("InitializePedagogicalModelRequest", "Initialize Pedagogical Model Request", "Used to initialize the Pedagogical model(s) with authored domain independent strategies.");
    public static final MessageTypeEnum LESSON_STARTED                      = new MessageTypeEnum("LessonStarted", "Lesson Started", "Use to synchronize on when a training application scenario has started in a training application course element in a course.");
    public static final MessageTypeEnum LESSON_COMPLETED                    = new MessageTypeEnum("LessonCompleted", "Lesson Completed", "Use to synchronize on when a training application scenario has finished in a training application course element in a course.");
    public static final MessageTypeEnum PUBLISH_LESSON_SCORE_REQUEST        = new MessageTypeEnum("PublishLessonScoreRequest", "Publish Lesson Score Request", "Request to save the scores for a training application instance in a course.");
    public static final MessageTypeEnum PUBLISH_LESSON_SCORE_REPLY          = new MessageTypeEnum("PublishLessonScoreReply", "Publish Lesson Score Reply", "The reply to the request to save the scores for a training application instance in a course.");
    public static final MessageTypeEnum LESSON_GRADED_SCORE_REQUEST         = new MessageTypeEnum("LessonGradedScoreRequest", "Lesson Graded Score Request", "Request to process and update learner states based on a graded score.");
    public static final MessageTypeEnum CLOSE_DOMAIN_SESSION_REQUEST        = new MessageTypeEnum("CloseDomainSessionRequest", "Close Domain Session Request", "Notification that the domain session is closed, i.e. the execution of the course is finished.");
    public static final MessageTypeEnum START_DOMAIN_SESSION                = new MessageTypeEnum("StartDomainSession", "Start Domain Session", "Use to synchronize on when a domain session has started.");
    public static final MessageTypeEnum PERFORMANCE_ASSESSMENT              = new MessageTypeEnum("PerformanceAssessment", "Performance Assessment", "The latest performance assessment of the domain knowledge (i.e. task and concept assement values like Above, At and Below expectation).");
    public static final MessageTypeEnum LEARNER_TUTOR_ACTION                = new MessageTypeEnum("LearnerTutorAction", "Learner Tutor Action", "An action was taken by the learner via the tutor, e.g. the use selected the 'Use Radio' TUI action button");
    public static final MessageTypeEnum ACTIVE_USER_SESSIONS_REQUEST        = new MessageTypeEnum("ActiveUserSessionsRequest", "Active User Sessions Request", "Request a module's list of active users.");
    public static final MessageTypeEnum ACTIVE_USER_SESSIONS_REPLY          = new MessageTypeEnum("ActiveUserSessionsReply", "Active User Sessions Reply", "Response to a module's request for active users.");
    public static final MessageTypeEnum CHAT_LOG                            = new MessageTypeEnum("ChatLog", "Chat Log", "A history of entries for a chat session.");
    public static final MessageTypeEnum TUTOR_MODULE_STATUS                 = new MessageTypeEnum("TutorModuleStatus", "Tutor Module Status", "An extension of the module status message that provides additional Tutor module information.");

    public static final MessageTypeEnum DISPLAY_AAR_TUTOR_REQUEST               = new MessageTypeEnum("DisplayAarTutorRequest", "Display AAR Tutor Request", "Request that the Tutor display specific After Action Review (AAR) to the user.");
    public static final MessageTypeEnum DISPLAY_FEEDBACK_TUTOR_REQUEST          = new MessageTypeEnum("DisplayFeedbackTutorRequest", "Display Feedback Tutor Request", "Request that the Tutor display specific feedback to the user during a lesson (i.e. training application course element).");
    public static final MessageTypeEnum DISPLAY_LESSON_MATERIAL_TUTOR_REQUEST   = new MessageTypeEnum("DisplayLessonMaterialTutorRequest", "Display Lesson Material Tutor Request", "Request that the Tutor display a list of lesson material to the user where the learner is able to browse at their own discretion.");
    public static final MessageTypeEnum DISPLAY_MIDLESSON_MEDIA_TUTOR_REQUEST   = new MessageTypeEnum("DisplayMidLessonMediaTutorRequest", "Display Mid-Lesson Media Tutor Request", "Request that the Tutor display a list of lesson material to the user during a training application scenario.");
    public static final MessageTypeEnum DISPLAY_SURVEY_TUTOR_REPLY              = new MessageTypeEnum("DisplaySurveyTutorReply", "Display Survey Tutor Reply", "The reply to a request that the Tutor display the survey to the user which contains the answers to the survey.");
    public static final MessageTypeEnum TUTOR_SURVEY_QUESTION_RESPONSE          = new MessageTypeEnum("TutorSurveyQuestionResponse", "Tutor Survey Question Response", "Contains the learner's response to a single survey question.  This is sent everytime a question is answered.  Normally sent from Tutor to Domain module and from Domain to Gateway module.");
    public static final MessageTypeEnum TRAINING_APP_SURVEY_RESPONSE            = new MessageTypeEnum("TrainingAppSurveyResponse", "Training App Survey Response", "The gateway module is attempting to answer the survey that is being displayed by the tutor.");
    public static final MessageTypeEnum TRAINING_APP_SURVEY_SUBMIT              = new MessageTypeEnum("TrainingAppSurveySubmit", "Training App Survey Submit", "The gateway module is attempting to submit the survey that is being displayed by the tutor.");

    public static final MessageTypeEnum DISPLAY_LEARNER_ACTIONS_TUTOR_REQUEST   = new MessageTypeEnum("DisplayLearnerActionsTutorRequest", "Display Learner Actions Tutor Request", "Request that the Tutor display the specified tutor actions during a lesson (i.e. training application course element).");
    public static final MessageTypeEnum DISPLAY_SURVEY_TUTOR_REQUEST            = new MessageTypeEnum("DisplaySurveyTutorRequest", "Display Survey Tutor Request", "Request that the Tutor display the survey to the user.");
    public static final MessageTypeEnum DISPLAY_GUIDANCE_TUTOR_REQUEST          = new MessageTypeEnum("DisplayGuidanceTutorRequest", "Display Guidance Tutor Request", "Request that the Tutor display specific Guidance (i.e. course Guidance transition content) to the user.");
    public static final MessageTypeEnum DISPLAY_CONTENT_TUTOR_REQUEST           = new MessageTypeEnum("DisplayContentTutorRequest", "Display Content Tutor Request", "Request that the Tutor display a single piece of content to the user (i.e. not a collection).");
    public static final MessageTypeEnum DISPLAY_CHAT_WINDOW_REQUEST             = new MessageTypeEnum("DisplayChatWindowRequest", "Display Chat Window Request", "Request that the Tutor display an empty chat window interface (e.g. AutoTutor) to the user.");
    public static final MessageTypeEnum DISPLAY_CHAT_WINDOW_UPDATE_REQUEST      = new MessageTypeEnum("DisplayChatWindowUpdateRequest", "Display Chat Window Update Request", "Request that the Tutor update the current chat window interface with the specified attributes (e.g. a new tutor chat entry).");
    public static final MessageTypeEnum DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST    = new MessageTypeEnum("DisplayCourseInitInstructionsRequest", "Display Course Initialization Instructions Request", "Request that the Tutor display instructions associated with initializing the current course to the user.");
    

    public static final MessageTypeEnum UNDER_DWELL_VIOLATION    = new MessageTypeEnum("UnderDwellViolation", "Under-Dwell Violation", "Notification that the learner violated an under-dwell assessment for a piece of content.");
    public static final MessageTypeEnum OVER_DWELL_VIOLATION    = new MessageTypeEnum("OverDwellViolation", "Over-Dwell Violation", "Notification that the learner violated an over-dwell assessment for a piece of content.");

    /**
     * Pedagogical module messages
     */
    public static final MessageTypeEnum PEDAGOGICAL_REQUEST                 = new MessageTypeEnum("PedagogicalRequest", "Pedagogical Request", "Instructional Strategy request from the Pedagogical model(s) (e.g. user needs feedback on 'map reading').");

    /**
     * Domain module messages
     */
    public static final MessageTypeEnum DOMAIN_OPTIONS_REQUEST              = new MessageTypeEnum("DomainOptionsRequest", "Domain Options Request", "Request for the list of available domains (i.e. courses) for the specified user.");
    public static final MessageTypeEnum DOMAIN_OPTIONS_REPLY                = new MessageTypeEnum("DomainOptionsReply", "Domain Options Reply", "The list of avaialble domains for a user to select from.");
    public static final MessageTypeEnum INIT_INTEROP_CONNECTIONS            = new MessageTypeEnum("InitializeInteropConnections", "Initialize Interop Connections", "Sent to the Gateway module in order to initialize the needed interop plugins for a lesson (i.e. training application instance) that is about to begin.");
    public static final MessageTypeEnum INIT_EMBEDDED_CONNECTIONS           = new MessageTypeEnum("InitEmbeddedConnections", "Initialize Embedded Connections", "Sent to the Tutor module in order to initialize the needed embedded apps for a lesson (i.e. training application instance) that is about to begin.");
    public static final MessageTypeEnum CONFIGURE_INTEROP_CONNECTIONS       = new MessageTypeEnum("ConfigureInteropConnections", "Configure Interop Connections", "Sent to the Gateway module in order to configure the needed interop plugins for a course that is about to begin.");
    public static final MessageTypeEnum DISPLAY_FEEDBACK_GATEWAY_REQUEST    = new MessageTypeEnum("DisplayFeedbackGatewayRequest", "Display Feedback Gateway Request", "Request that the Gateway module provide feedback content to the current interop plugins to allow them to present it instead of the Tutor (i.e. the training application should display specific feedback to the user).");
    public static final MessageTypeEnum DISPLAY_FEEDBACK_EMBEDDED_REQUEST   = new MessageTypeEnum("DisplayFeedbackEmbeddedRequest", "DisplayFeedbackEmbeddedRequest", "Request that the Tutor module show feedback content inside its currently displayed embedded application.");
    public static final MessageTypeEnum VIBRATE_DEVICE_REQUEST              = new MessageTypeEnum("VibrateDeviceRequest", "Vibrate Device Request", "Request that the Tutor module vibrates whatever device it is being hosted in (when applicable).");
    public static final MessageTypeEnum ACTIVE_DOMAIN_SESSIONS_REQUEST      = new MessageTypeEnum("ActiveDomainSessionsRequest", "Active Domain Sessions Request", "Request the list of active domain session from the Domain module.");
    public static final MessageTypeEnum ACTIVE_DOMAIN_SESSIONS_REPLY        = new MessageTypeEnum("ActiveDomainSessionsReply", "Active Domain Sessions Reply", "The list of the Domain module's active domain session.");
    public static final MessageTypeEnum COURSE_STATE                        = new MessageTypeEnum("CourseState", "Course State", "Used by the Domain module to notify other modules about the current state of the course (e.g. which course transition the user is entering).");
    public static final MessageTypeEnum DISPLAY_TEAM_SESSIONS               = new MessageTypeEnum("DisplayTeamSessions", "Display Team Sessions", "Tells the Tutor to display the team session screen prior to transitioning into a training application (dkf).");
    public static final MessageTypeEnum EXPERIMENT_COURSE_REQUEST           = new MessageTypeEnum("ExperimentCourseRequest", "Experiment Course Request", "Request that the Domain find and validate the course used by an experiment.");
    public static final MessageTypeEnum SURVEY_PRESENTED_NOTIFICATION       = new MessageTypeEnum("SurveyPresentedNotification", "Survey Presented Notification", "Notifies the recepient that a survey is being presented to the learner through the tutor user interface.");
    public static final MessageTypeEnum EVALUATOR_UPDATE_REQUEST            = new MessageTypeEnum("EvaluatorUpdateRequest", "Evaluator Update Request", "Used by the Domain module to update a task or concept's metrics with new values.");
    public static final MessageTypeEnum ACTIVE_KNOWLEDGE_SESSIONS_REQUEST   = new MessageTypeEnum("ActiveKnowledgeSessionsRequest", "Active Knowledge Sessions Request", "Request the list of active knowledge session from the Domain module (i.e. real time assessment with a DKF).");
    public static final MessageTypeEnum ACTIVE_KNOWLEDGE_SESSIONS_REPLY     = new MessageTypeEnum("ActiveKnowledgeSessionsReply", "Active Knowledge Sessions Reply", "The list of the Domain module's active knowledge sessions (i.e. real time assessment with a DKF).");
    public static final MessageTypeEnum KNOWLEDGE_SESSION_UPDATED_REPLY     = new MessageTypeEnum("KnowledgeSessionUpdatedReply", "Knowledge Session Updated Reply", "Reply to the request when the active knowledge session has been updated.");
    public static final MessageTypeEnum KNOWLEDGE_SESSION_UPDATED_REQUEST   = new MessageTypeEnum("KnowledgeSessionUpdatedRequest", "Knowledge Session Updated Request", "Notify the tutor that the active knowledge session has been updated.");
    public static final MessageTypeEnum MANAGE_MEMBERSHIP_TEAM_KNOWLEDGE_SESSION = new MessageTypeEnum("ManageMembershipTeamKnowledgeSession", "Manage Membership Team Knowledge Session", "Used to manage a team knowledge session including creating/destroying a hosted session, joining/leaving a hosted session.  A knowledge session is were a real time assessment takes place.");
    public static final MessageTypeEnum START_TEAM_KNOWLEDGE_SESSION_REQUEST = new MessageTypeEnum("StartTeamKnowledgeSessionRequest", "Start Team Knowledge Session Request", "Used to start a team knowledge session.");
    public static final MessageTypeEnum START_TEAM_KNOWLEDGE_SESSION_REPLY  = new MessageTypeEnum("StartTeamKnowledgeSessionReply", "Start Team Knowledge Session Reply", "Returns if the start team knowledge session request was successful or not.");
    public static final MessageTypeEnum AUTHORIZE_STRATEGIES_REQUEST        = new MessageTypeEnum("AuthorizeStrategiesRequest", "Authorize Strategies Request", "Requests the recipient should authorize the execution of a specified set of strategies.  This is normally sent from the Domain module to the Game Master (Dashboard) for approval to apply the strategies specified in the request.");
    public static final MessageTypeEnum EXECUTE_OC_STRATEGY                 = new MessageTypeEnum("ExecuteOcStrategy", "Execute OC Strategy", "Used to notify the recipient that a strategy needs to be executed for the OC. This is normally sent from the Domain module to the Game Master (Dashboard) to show feedback/prompts to the Game Master user (observer).");
    public static final MessageTypeEnum KNOWLEDGE_SESSION_CREATED           = new MessageTypeEnum("KnowledgeSessionCreated", "Knowledge Session Created", "Notifies any listeners that a knowledge session was created.");
    public static final MessageTypeEnum DOMAIN_MODULE_STATUS                 = new MessageTypeEnum("DomainModuleStatus", "Domain Module Status", "An extension of the module status message that provides additional Domain module information.");
    
    /**
     * Web Monitor Messages
     */
    public static final MessageTypeEnum WEB_MONITOR_MODULE_STATUS = new MessageTypeEnum("WebMonitorModuleStatus", "Web Monitor Module Status", "A heartbeat message that is used to communicate which domain sessions should send messages to this module.");
    public static final MessageTypeEnum APPLY_STRATEGIES = new MessageTypeEnum("ApplyStrategies", "Apply Strategies", "Specifies that a specified set of strategies should be executed.  This is sent from the Game Master (Dashboard) to the Domain module so the Domain module can apply the strategies specified in the request.");

    /**
     * Generic messages
     */
    public static final MessageTypeEnum NACK            = new MessageTypeEnum("NACK", "NACK", "A Negative Acknowledgement is sent in response to a received message when the message can't be decoded successfully.");
    public static final MessageTypeEnum ACK             = new MessageTypeEnum("ACK",  "ACK", " A positive Acknowledgement is sent in response to a received message when the message is decoded successfully.");
    public static final MessageTypeEnum PROCESSED_NACK  = new MessageTypeEnum("ProcessedNACK", "Procesed NACK", "A Processed Negative Acknowledgement is sent after a message is decoded successfully but the handling of the message has failed in some way.");
    public static final MessageTypeEnum PROCESSED_ACK   = new MessageTypeEnum("ProcessedACK",  "Processed ACK", "A Processed Negative Acknowledgement is sent after a message is both decoded and handled successfully.");

    /**
     * Gateway messages
     */
    public static final MessageTypeEnum GATEWAY_MODULE_STATUS       = new MessageTypeEnum("GatewayModuleStatus", "Gateway Module Status", "An extension of the module status message that provides additional Gateway module information.");
    public static final MessageTypeEnum INTEROP_CONNECTIONS_INFO    = new MessageTypeEnum("InteropConnectionsInfo", "Interop Connections Information", "Sent to the Domain module in response to an initialize interop connections.  The information provided describes the connection capabilities of the gateway module that has now been initialized.");


    /**
     * Simulation messages
     */
    public static final MessageTypeEnum ENTITY_STATE            = new MessageTypeEnum("EntityState", "Entity State", "Contains state information about an Entity in a training application (e.g. location, orientation, velocity, appearance) .");
    public static final MessageTypeEnum COLLISION               = new MessageTypeEnum("Collision", "Collision", "Contains information about a collision between two objects in a training application (e.g. entity identifiers, location, velocity, mass) .");
    public static final MessageTypeEnum REMOVE_ENTITY           = new MessageTypeEnum("RemoveEntity", "Remove Entity", "Used to remove an entity from an exercise or training application.");
    public static final MessageTypeEnum DETONATION              = new MessageTypeEnum("Detonation",  "Detonation", "Contains information about the detonation of a munition (e.g. location, velocity, target entity info).");
    public static final MessageTypeEnum WEAPON_FIRE             = new MessageTypeEnum("WeaponFire",  "Weapon Fire", "Contains information about the firing of a weapon (e.g. location, velocity, firing entity info).");
    public static final MessageTypeEnum LOS_RESULT              = new MessageTypeEnum("LoSResult",  "LoS Result", "The result of Line-of-Sight query(ies) to a training application.");
    public static final MessageTypeEnum LOS_QUERY               = new MessageTypeEnum("LoSQuery",  "LoS Query", "A request to perform Line-of-Sight query(ies) at specific points of interest.");
    public static final MessageTypeEnum VARIABLE_STATE_RESULT     = new MessageTypeEnum("VariableStateResult",  "Variable State Result", "The information retrieved for specific actors based on a request to a training application.");
    public static final MessageTypeEnum VARIABLE_STATE_REQUEST    = new MessageTypeEnum("VariableStateRequest",  "Variable State Request", "A request to retrieve one or more pieces of information for specific actors from an external training application.");
    public static final MessageTypeEnum POWERPOINT_STATE        = new MessageTypeEnum("PowerPointState", "PowerPoint State", "The state of an executing PowerPoint show (e.g. current slide number).");
    public static final MessageTypeEnum SIMPLE_EXAMPLE_STATE    = new MessageTypeEnum("SimpleExampleState", "Simple Example State");
    public static final MessageTypeEnum LOAD_PROGRESS           = new MessageTypeEnum("LoadProgress", "Load Progress", "Provides an indicator into the progress of loading content into a training application for a learner.  Contains JSON formatted information.");

    public static final MessageTypeEnum SIMAN                   = new MessageTypeEnum("Siman", "Siman", "Simulation Management message that is used to synchronize GIFT and training applications (e.g. load , pause, stop).");
    public static final MessageTypeEnum ENVIRONMENT_CONTROL     = new MessageTypeEnum("EnvironmentControl", "Environment Control", "Contains information about how the currently running training application lesson/scenario should be adapted (e.g. increase fog level, change time of day to midnight) based on an instructional strategy request.");
    public static final MessageTypeEnum START_RESUME            = new MessageTypeEnum("StartResume", "Start Resume", "A DIS driven message used to indicate whether the training application should/has started/resumed execution of a scenario/file.");
    public static final MessageTypeEnum STOP_FREEZE             = new MessageTypeEnum("StopFreeze", "Stop Freeze", "A DIS driven message used to indicate whether the training application should/has stopped/froze execution of a scenario/file.");
    public static final MessageTypeEnum EXTERNAL_MONITOR_CONFIG     = new MessageTypeEnum("ExternalMonitorConfig", "External Monitor Configuration", "Contains information about how the currently running training application should monitor data passed into it.");
    
    public static final MessageTypeEnum GEOLOCATION             = new MessageTypeEnum("Geolocation", "Geolocation", "Contains location information for a learner, entity, unit or actor using the GIFT mobile app or from a training application embedded in the GIFT Tutur User Interface (e.g. Unity WebGL).");
    
    // Legacy - only needed to parsing logs
    public static final MessageTypeEnum WEAPON_STATE_RESULT     = new MessageTypeEnum("WeaponStateResult",  "Weapon State Result", "LEGACY: The result of a Weapon State request to a training application.");
    public static final MessageTypeEnum WEAPON_STATE_REQUEST    = new MessageTypeEnum("WeaponStateRequest",  "Weapon State Request", "LEGACY: A request to perform a check on weapon state for specific actors.");

    /**
     * Message bound for SIMILE processing
     */
    public static final MessageTypeEnum GENERIC_JSON_STATE  = new MessageTypeEnum("GenericJSONState", "Generic JSONState", "Contains JSON formatted training application state information.  This is the main state message used by TC3.  It provides a generic way for states to be communicated without having to develop new state messages in GIFT.");

    /**
     * SCATT messages
     */
    public static final MessageTypeEnum RIFLE_SHOT_MESSAGE = new MessageTypeEnum("RifleShot", "Rifle Shot", "Contains information about a rifle shot.");

    /**
     * Monitor messages
     */
    public static final MessageTypeEnum DOMAIN_SESSION_START_TIME_REQUEST = new MessageTypeEnum("DomainSessionStartTimeRequest", "Domain Session Start Time Request", "Request for the starting time of a particular active domain session.");
    public static final MessageTypeEnum DOMAIN_SESSION_START_TIME_REPLY = new MessageTypeEnum("DomainSessionStartTimeReply", "Domain Session Start Time Reply", "Response to a domain session start time request that contains the starting time in question.");

    /**
     * Collection of Training Application Game state message types
     */
    public static final List<MessageTypeEnum> TRAINING_APP_STATE_MESSAGE_TYPES = Arrays.asList(ENTITY_STATE, COLLISION, DETONATION,
            WEAPON_FIRE, POWERPOINT_STATE, SIMPLE_EXAMPLE_STATE, GENERIC_JSON_STATE, RIFLE_SHOT_MESSAGE, GEOLOCATION);

    /** (optional) information about the message type and its use in GIFT */
    private String description = null;

    /** Empty arg constructor for GWT serialization */
    @SuppressWarnings("unused")
    private MessageTypeEnum() {
    }

    /**
     * Class constructor - set attributes
     *
     * @param name the unique name for the enumeration instance
     * @param displayName the display name for the enumeration
     */
    public MessageTypeEnum(String name, String displayName){
        super(index++, name, displayName);
        enumList.add(this);
    }

    /**
     * Class constructor - set attributes
     *
     * @param name the unique name for the enumeration instance
     * @param displayName the display name for the enumeration
     * @param description information about the message type and its use in GIFT
     */
    private MessageTypeEnum(String name, String displayName, String description){
        this(name, displayName);

        this.description = description;
    }

    /**
     * Return the information about the message type and its use in GIFT.
     *
     * @return String the description value, can be null if not provided.
     */
    public String getDescription(){
        return description;
    }

    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static MessageTypeEnum valueOf(String name)
            throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         value is not found.
     */
    public static MessageTypeEnum valueOf(int value)
            throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<MessageTypeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
