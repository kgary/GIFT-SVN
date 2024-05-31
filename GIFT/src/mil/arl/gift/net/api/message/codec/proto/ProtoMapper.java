/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashMap;
import java.util.Map;

import generated.proto.common.AbstractDisplayContentTutorRequestProto;
import generated.proto.common.AbstractDisplayGuidanceTutorRequestProto;
import generated.proto.common.ApplyStrategiesProto;
import generated.proto.common.AuthorizeStrategiesRequestProto;
import generated.proto.common.BranchPathHistoryProto;
import generated.proto.common.BranchPathHistoryRequestProto;
import generated.proto.common.ChatLogProto;
import generated.proto.common.CloseDomainSessionRequestProto;
import generated.proto.common.CollisionProto;
import generated.proto.common.CourseStateProto;
import generated.proto.common.DataCollectionItemProto;
import generated.proto.common.DetonationProto;
import generated.proto.common.DisplayAARTutorRequestProto;
import generated.proto.common.DisplayChatWindowRequestProto;
import generated.proto.common.DisplayChatWindowUpdateRequestProto;
import generated.proto.common.DisplayCourseInitInstructionsRequestProto;
import generated.proto.common.DisplayLearnerActionsTutorRequestProto;
import generated.proto.common.DisplayMediaCollectionRequestProto;
import generated.proto.common.DisplayMidLessonMediaRequestProto;
import generated.proto.common.DisplaySurveyTutorRequestProto;
import generated.proto.common.DomainModuleStatusProto;
import generated.proto.common.DomainOptionsListProto;
import generated.proto.common.DomainOptionsRequestProto;
import generated.proto.common.DomainSelectionRequestProto;
import generated.proto.common.DomainSessionCreatedProto;
import generated.proto.common.DomainSessionListProto;
import generated.proto.common.DomainSessionStartTimeReplyProto;
import generated.proto.common.DomainSessionStartTimeRequestProto;
import generated.proto.common.EmptyPayloadProto;
import generated.proto.common.EntityStateProto;
import generated.proto.common.EnvironmentControlProto;
import generated.proto.common.EvaluatorUpdateRequestProto;
import generated.proto.common.ExecuteOCStrategyProto;
import generated.proto.common.ExperimentCourseRequestProto;
import generated.proto.common.ExternalMonitorConfigProto;
import generated.proto.common.FilteredSensorDataProto;
import generated.proto.common.GatewayModuleStatusProto;
import generated.proto.common.GenericStateProto;
import generated.proto.common.GeolocationProto;
import generated.proto.common.GetExperimentRequestProto;
import generated.proto.common.GetSurveyRequestProto;
import generated.proto.common.InitializeDomainSessionRequestProto;
import generated.proto.common.InitializeEmbeddedConnectionsProto;
import generated.proto.common.InitializeInteropConnectionsProto;
import generated.proto.common.InitializeLessonRequestProto;
import generated.proto.common.InitializePedagogicalModelRequestProto;
import generated.proto.common.InstantiateLearnerRequestProto;
import generated.proto.common.InteropConnectionsInfoProto;
import generated.proto.common.KnowledgeAssessmentDetailsProto;
import generated.proto.common.KnowledgeSessionCreatedProto;
import generated.proto.common.KnowledgeSessionsProto;
import generated.proto.common.KnowledgeSessionsRequestProto;
import generated.proto.common.LMSDataRequestProto;
import generated.proto.common.LMSDataResponseProto;
import generated.proto.common.LearnerStateProto;
import generated.proto.common.LearnerTutorActionProto;
import generated.proto.common.LessonCompletedProto;
import generated.proto.common.LoSQueryProto;
import generated.proto.common.LoSResultProto;
import generated.proto.common.LoginRequestProto;
import generated.proto.common.LtiGetProviderUrlRequestProto;
import generated.proto.common.LtiGetProviderUrlResponseProto;
import generated.proto.common.LtiGetUserRequestProto;
import generated.proto.common.LtiGetUserResponseProto;
import generated.proto.common.LtiLessonGradedScoreRequestProto;
import generated.proto.common.ManageTeamMembershipRequestProto;
import generated.proto.common.ModuleAllocationReplyProto;
import generated.proto.common.ModuleAllocationRequestProto;
import generated.proto.common.ModuleStatusProto;
import generated.proto.common.NACKProto;
import generated.proto.common.PedagogicalRequestProto;
import generated.proto.common.PerformanceAssessmentProto;
import generated.proto.common.PowerPointStateProto;
import generated.proto.common.PublishLessonScoreProto;
import generated.proto.common.PublishLessonScoreResponseProto;
import generated.proto.common.RemoveEntityProto;
import generated.proto.common.RifleShotMessageProto;
import generated.proto.common.SensorFileCreatedProto;
import generated.proto.common.SensorStatusProto;
import generated.proto.common.SimanProto;
import generated.proto.common.SimpleExampleStateProto;
import generated.proto.common.StartResumeProto;
import generated.proto.common.StopFreezeProto;
import generated.proto.common.StringPayloadProto;
import generated.proto.common.SubjectCreatedProto;
import generated.proto.common.SurveyCheckResponseProto;
import generated.proto.common.SurveyListCheckRequestProto;
import generated.proto.common.TutorModuleStatusProto;
import generated.proto.common.TutorUserInterfaceFeedbackPayloadProto;
import generated.proto.common.UnfilteredSensorDataProto;
import generated.proto.common.UserDataProto;
import generated.proto.common.UserSessionListProto;
import generated.proto.common.VariablesStateRequestProto;
import generated.proto.common.VariablesStateResultProto;
import generated.proto.common.VibrateDeviceProto;
import generated.proto.common.WeaponFireProto;
import generated.proto.common.WebMonitorModuleStatusProto;
import generated.proto.common.survey.QuestionResponseProto;
import generated.proto.common.survey.SubmitSurveyResultsProto;
import generated.proto.common.survey.SurveyGiftDataProto;
import generated.proto.common.survey.SurveyResponseProto;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.net.api.message.codec.proto.survey.QuestionResponseProtoCodec;
import mil.arl.gift.net.api.message.codec.proto.survey.SubmitSurveyResultsProtoCodec;
import mil.arl.gift.net.api.message.codec.proto.survey.SurveyGiftDataProtoCodec;
import mil.arl.gift.net.api.message.codec.proto.survey.SurveyResponseProtoCodec;

/**
 * This class contains the mapping of message types to the classes which can
 * Protobuf encode/decode the message type.
 *
 * @author cpolynice
 */
public class ProtoMapper {
    private final static Map<MessageTypeEnum, ProtoClassContainer> messageWrapper = new HashMap<>();

    static {
        messageWrapper.put(MessageTypeEnum.ACK,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REQUEST,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class,
                        EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REPLY, new ProtoClassContainer(
                DomainSessionListProto.DomainSessionList.class, DomainSessionListProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.ACTIVE_KNOWLEDGE_SESSIONS_REQUEST,
                new ProtoClassContainer(KnowledgeSessionsRequestProto.KnowledgeSessionsRequest.class,
                        KnowledgeSessionsRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.ACTIVE_KNOWLEDGE_SESSIONS_REPLY, new ProtoClassContainer(
                KnowledgeSessionsProto.KnowledgeSessions.class, KnowledgeSessionsProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.ACTIVE_USER_SESSIONS_REQUEST,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.ACTIVE_USER_SESSIONS_REPLY, new ProtoClassContainer(UserSessionListProto.UserSessionList.class, UserSessionListProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.BRANCH_PATH_HISTORY_UPDATE, new ProtoClassContainer(BranchPathHistoryProto.BranchPathHistory.class, BranchPathHistoryProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.BRANCH_PATH_HISTORY_REQUEST,
                new ProtoClassContainer(BranchPathHistoryRequestProto.BranchPathHistoryRequest.class,
                        BranchPathHistoryRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.BRANCH_PATH_HISTORY_REPLY,
                new ProtoClassContainer(BranchPathHistoryRequestProto.BranchPathHistoryRequest.class,
                        BranchPathHistoryRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.CHAT_LOG,
                new ProtoClassContainer(ChatLogProto.ChatLog.class, ChatLogProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST,
                new ProtoClassContainer(CloseDomainSessionRequestProto.CloseDomainSessionRequest.class,
                        CloseDomainSessionRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.COLLISION,
                new ProtoClassContainer(CollisionProto.Collision.class, CollisionProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.CONFIGURE_INTEROP_CONNECTIONS,
                new ProtoClassContainer(InitializeInteropConnectionsProto.InitializeInteropConnections.class,
                        InitializeInteropConnectionsProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.COURSE_STATE,
                new ProtoClassContainer(CourseStateProto.CourseState.class, CourseStateProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DETONATION,
                new ProtoClassContainer(DetonationProto.Detonation.class, DetonationProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_AAR_TUTOR_REQUEST, new ProtoClassContainer(
                DisplayAARTutorRequestProto.DisplayAARTutorRequest.class, DisplayAARTutorRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_CHAT_WINDOW_REQUEST,
                new ProtoClassContainer(DisplayChatWindowRequestProto.DisplayChatWindowRequest.class,
                        DisplayChatWindowRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_CHAT_WINDOW_UPDATE_REQUEST,
                new ProtoClassContainer(DisplayChatWindowUpdateRequestProto.DisplayChatWindowUpdateRequest.class,
                        DisplayChatWindowUpdateRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST,
                new ProtoClassContainer(
                        DisplayCourseInitInstructionsRequestProto.DisplayCourseInitInstructionsRequest.class,
                        DisplayCourseInitInstructionsRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_FEEDBACK_TUTOR_REQUEST,
                new ProtoClassContainer(TutorUserInterfaceFeedbackPayloadProto.TutorUserInterfaceFeedbackPayload.class,
                        TutorUserInterfaceFeedbackPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST,
                new ProtoClassContainer(StringPayloadProto.StringPayload.class, StringPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_FEEDBACK_EMBEDDED_REQUEST,
                new ProtoClassContainer(StringPayloadProto.StringPayload.class, StringPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_LEARNER_ACTIONS_TUTOR_REQUEST,
                new ProtoClassContainer(DisplayLearnerActionsTutorRequestProto.DisplayLearnerActionsTutorRequest.class,
                        DisplayLearnerActionsTutorRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_LESSON_MATERIAL_TUTOR_REQUEST,
                new ProtoClassContainer(DisplayMediaCollectionRequestProto.DisplayMediaCollectionRequest.class,
                        DisplayMediaCollectionRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_MIDLESSON_MEDIA_TUTOR_REQUEST,
                new ProtoClassContainer(DisplayMidLessonMediaRequestProto.DisplayMidLessonMediaRequest.class,
                        DisplayMidLessonMediaRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REPLY,
                new ProtoClassContainer(SurveyResponseProto.SurveyResponse.class, SurveyResponseProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REQUEST,
                new ProtoClassContainer(DisplaySurveyTutorRequestProto.DisplaySurveyTutorRequest.class,
                        DisplaySurveyTutorRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_TEAM_SESSIONS, new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_GUIDANCE_TUTOR_REQUEST,
                new ProtoClassContainer(AbstractDisplayGuidanceTutorRequestProto.AbstractDisplayGuidanceTutorRequest.class, AbstractDisplayGuidanceTutorRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DISPLAY_CONTENT_TUTOR_REQUEST,
                new ProtoClassContainer(
                        AbstractDisplayContentTutorRequestProto.AbstractDisplayContentTutorRequest.class,
                        AbstractDisplayContentTutorRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DOMAIN_MODULE_STATUS, new ProtoClassContainer(
                DomainModuleStatusProto.DomainModuleStatus.class, DomainModuleStatusProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DOMAIN_OPTIONS_REQUEST, new ProtoClassContainer(
                DomainOptionsRequestProto.DomainOptionsRequest.class, DomainOptionsRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DOMAIN_OPTIONS_REPLY, new ProtoClassContainer(
                DomainOptionsListProto.DomainOptionsList.class, DomainOptionsListProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DOMAIN_SELECTION_REPLY, new ProtoClassContainer(
                DomainSessionCreatedProto.DomainSessionCreated.class, DomainSessionCreatedProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DOMAIN_SELECTION_REQUEST, new ProtoClassContainer(
                DomainSelectionRequestProto.DomainSelectionRequest.class, DomainSelectionRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.EXPERIMENT_COURSE_REQUEST, new ProtoClassContainer(
                ExperimentCourseRequestProto.ExperimentCourseRequest.class, ExperimentCourseRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.ENTITY_STATE,
                new ProtoClassContainer(EntityStateProto.EntityState.class, EntityStateProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.ENVIRONMENT_CONTROL, new ProtoClassContainer(
                EnvironmentControlProto.EnvironmentControl.class, EnvironmentControlProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.EVALUATOR_UPDATE_REQUEST, new ProtoClassContainer(
                EvaluatorUpdateRequestProto.EvaluatorUpdateRequest.class, EvaluatorUpdateRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.EXTERNAL_MONITOR_CONFIG, new ProtoClassContainer(
                ExternalMonitorConfigProto.ExternalMonitorConfig.class, ExternalMonitorConfigProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SIMPLE_EXAMPLE_STATE, new ProtoClassContainer(
                SimpleExampleStateProto.SimpleExampleState.class, SimpleExampleStateProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.GATEWAY_MODULE_STATUS, new ProtoClassContainer(
                GatewayModuleStatusProto.GatewayModuleStatus.class, GatewayModuleStatusProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.TUTOR_MODULE_STATUS, new ProtoClassContainer(
                TutorModuleStatusProto.TutorModuleStatus.class, TutorModuleStatusProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.GET_SURVEY_REPLY,
                new ProtoClassContainer(SurveyGiftDataProto.SurveyGiftData.class, SurveyGiftDataProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.GET_EXPERIMENT_REPLY, new ProtoClassContainer(
                DataCollectionItemProto.DataCollectionItem.class, DataCollectionItemProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.GET_SURVEY_REQUEST, new ProtoClassContainer(
                GetSurveyRequestProto.GetSurveyRequest.class, GetSurveyRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.GET_EXPERIMENT_REQUEST, new ProtoClassContainer(
                GetExperimentRequestProto.GetExperimentRequest.class, GetExperimentRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST,
                new ProtoClassContainer(InitializeDomainSessionRequestProto.InitializeDomainSessionRequest.class,
                        InitializeDomainSessionRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.INIT_EMBEDDED_CONNECTIONS,
                new ProtoClassContainer(InitializeEmbeddedConnectionsProto.InitializeEmbeddedConnections.class,
                        InitializeEmbeddedConnectionsProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.INIT_INTEROP_CONNECTIONS,
                new ProtoClassContainer(InitializeInteropConnectionsProto.InitializeInteropConnections.class,
                        InitializeInteropConnectionsProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.INITIALIZE_LESSON_REQUEST, new ProtoClassContainer(
                InitializeLessonRequestProto.InitializeLessonRequest.class, InitializeLessonRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.INITIALIZE_PEDAGOGICAL_MODEL_REQUEST,
                new ProtoClassContainer(InitializePedagogicalModelRequestProto.InitializePedagogicalModelRequest.class,
                        InitializePedagogicalModelRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.INSTANTIATE_LEARNER_REQUEST,
                new ProtoClassContainer(InstantiateLearnerRequestProto.InstantiateLearnerRequest.class,
                        InstantiateLearnerRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.INTEROP_CONNECTIONS_INFO, new ProtoClassContainer(
                InteropConnectionsInfoProto.InteropConnectionsInfo.class, InteropConnectionsInfoProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.KILL_MODULE,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.KNOWLEDGE_ASSESSMENT_DETAILS,
                new ProtoClassContainer(KnowledgeAssessmentDetailsProto.KnowledgeAssessmentDetails.class, KnowledgeAssessmentDetailsProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REPLY,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST, new ProtoClassContainer(
                KnowledgeSessionsProto.KnowledgeSessions.class, KnowledgeSessionsProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LEARNER_STATE,
                new ProtoClassContainer(LearnerStateProto.LearnerState.class, LearnerStateProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LEARNER_TUTOR_ACTION, new ProtoClassContainer(
                LearnerTutorActionProto.LearnerTutorAction.class, LearnerTutorActionProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LESSON_COMPLETED,
                new ProtoClassContainer(LessonCompletedProto.LessonCompleted.class, LessonCompletedProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LESSON_STARTED,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LMS_DATA_REPLY,
                new ProtoClassContainer(LMSDataResponseProto.LMSDataResponse.class, LMSDataResponseProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LMS_DATA_REQUEST,
                new ProtoClassContainer(LMSDataRequestProto.LMSDataRequest.class, LMSDataRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LOGIN_REPLY,
                new ProtoClassContainer(UserDataProto.UserData.class, UserDataProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LOGIN_REQUEST,
                new ProtoClassContainer(LoginRequestProto.LoginRequest.class, LoginRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LTI_GETUSER_REQUEST, new ProtoClassContainer(
                LtiGetUserRequestProto.LtiGetUserRequest.class, LtiGetUserRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LTI_GETUSER_REPLY, new ProtoClassContainer(
                LtiGetUserResponseProto.LtiGetUserResponse.class, LtiGetUserResponseProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LTI_GET_PROVIDER_URL_REQUEST,
                new ProtoClassContainer(LtiGetProviderUrlRequestProto.LtiGetProviderUrlRequest.class,
                        LtiGetProviderUrlRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LTI_GET_PROVIDER_URL_REPLY,
                new ProtoClassContainer(LtiGetProviderUrlResponseProto.LtiGetProviderUrlResponse.class,
                        LtiGetProviderUrlResponseProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LESSON_GRADED_SCORE_REQUEST,
                new ProtoClassContainer(LtiLessonGradedScoreRequestProto.LtiLessonGradedScoreRequest.class,
                        LtiLessonGradedScoreRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LOGOUT_REQUEST,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LOAD_PROGRESS,
                new ProtoClassContainer(GenericStateProto.GenericState.class, GenericStateProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LOS_QUERY,
                new ProtoClassContainer(LoSQueryProto.LoSQuery.class, LoSQueryProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.LOS_RESULT,
                new ProtoClassContainer(LoSResultProto.LoSResult.class, LoSResultProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.MANAGE_MEMBERSHIP_TEAM_KNOWLEDGE_SESSION,
                new ProtoClassContainer(ManageTeamMembershipRequestProto.ManageTeamMembershipRequest.class,
                        ManageTeamMembershipRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.MODULE_ALLOCATION_REPLY, new ProtoClassContainer(
                ModuleAllocationReplyProto.ModuleAllocationReply.class, ModuleAllocationReplyProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.MODULE_ALLOCATION_REQUEST, new ProtoClassContainer(
                ModuleAllocationRequestProto.ModuleAllocationRequest.class, ModuleAllocationRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.MODULE_STATUS,
                new ProtoClassContainer(ModuleStatusProto.ModuleStatus.class, ModuleStatusProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.NACK, new ProtoClassContainer(NACKProto.NACK.class, NACKProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.NEW_USER_REQUEST,
                new ProtoClassContainer(UserDataProto.UserData.class, UserDataProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.PEDAGOGICAL_REQUEST, new ProtoClassContainer(
                PedagogicalRequestProto.PedagogicalRequest.class, PedagogicalRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.PERFORMANCE_ASSESSMENT, new ProtoClassContainer(
                PerformanceAssessmentProto.PerformanceAssessment.class, PerformanceAssessmentProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.POWERPOINT_STATE,
                new ProtoClassContainer(PowerPointStateProto.PowerPointState.class, PowerPointStateProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST, new ProtoClassContainer(
                PublishLessonScoreProto.PublishLessonScore.class, PublishLessonScoreProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.PUBLISH_LESSON_SCORE_REPLY,
                new ProtoClassContainer(PublishLessonScoreResponseProto.PublishLessonScoreResponse.class,
                        PublishLessonScoreResponseProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.PROCESSED_ACK,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.PROCESSED_NACK,
                new ProtoClassContainer(NACKProto.NACK.class, NACKProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.RIFLE_SHOT_MESSAGE, new ProtoClassContainer(
                RifleShotMessageProto.RifleShotMessage.class, RifleShotMessageProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.REMOVE_ENTITY,
                new ProtoClassContainer(RemoveEntityProto.RemoveEntity.class, RemoveEntityProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SENSOR_DATA, new ProtoClassContainer(
                UnfilteredSensorDataProto.UnfilteredSensorData.class, UnfilteredSensorDataProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SENSOR_STATUS,
                new ProtoClassContainer(SensorStatusProto.SensorStatus.class, SensorStatusProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SENSOR_FILE_CREATED, new ProtoClassContainer(
                SensorFileCreatedProto.SensorFileCreated.class, SensorFileCreatedProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SENSOR_FILTER_DATA, new ProtoClassContainer(
                FilteredSensorDataProto.FilteredSensorData.class, FilteredSensorDataProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SIMAN,
                new ProtoClassContainer(SimanProto.Siman.class, SimanProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.GENERIC_JSON_STATE,
                new ProtoClassContainer(GenericStateProto.GenericState.class, GenericStateProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.START_DOMAIN_SESSION,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.START_RESUME,
                new ProtoClassContainer(StartResumeProto.StartResume.class, StartResumeProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REPLY,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.STOP_FREEZE,
                new ProtoClassContainer(StopFreezeProto.StopFreeze.class, StopFreezeProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SUBJECT_CREATED,
                new ProtoClassContainer(SubjectCreatedProto.SubjectCreated.class, SubjectCreatedProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SUBMIT_SURVEY_RESULTS, new ProtoClassContainer(
                SubmitSurveyResultsProto.SubmitSurveyResults.class, SubmitSurveyResultsProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SURVEY_CHECK_REQUEST, new ProtoClassContainer(
                SurveyListCheckRequestProto.SurveyListCheckRequest.class, SurveyListCheckRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SURVEY_CHECK_RESPONSE, new ProtoClassContainer(
                SurveyCheckResponseProto.SurveyCheckResponse.class, SurveyCheckResponseProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.SURVEY_PRESENTED_NOTIFICATION,
                new ProtoClassContainer(DisplaySurveyTutorRequestProto.DisplaySurveyTutorRequest.class,
                        DisplaySurveyTutorRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.TRAINING_APP_SURVEY_RESPONSE,
                new ProtoClassContainer(SurveyResponseProto.SurveyResponse.class, SurveyResponseProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.TRAINING_APP_SURVEY_SUBMIT,
                new ProtoClassContainer(EmptyPayloadProto.EmptyPayload.class, EmptyPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE, new ProtoClassContainer(
                QuestionResponseProto.QuestionResponse.class, QuestionResponseProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.USER_ID_REQUEST,
                new ProtoClassContainer(StringPayloadProto.StringPayload.class, StringPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.USER_ID_REPLY,
                new ProtoClassContainer(UserDataProto.UserData.class, UserDataProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.VARIABLE_STATE_REQUEST, new ProtoClassContainer(
                VariablesStateRequestProto.VariablesStateRequest.class, VariablesStateRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.VARIABLE_STATE_RESULT, new ProtoClassContainer(
                VariablesStateResultProto.VariablesStateResult.class, VariablesStateResultProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.WEAPON_FIRE,
                new ProtoClassContainer(WeaponFireProto.WeaponFire.class, WeaponFireProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DOMAIN_SESSION_START_TIME_REQUEST,
                new ProtoClassContainer(DomainSessionStartTimeRequestProto.DomainSessionStartTimeRequest.class,
                        DomainSessionStartTimeRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.DOMAIN_SESSION_START_TIME_REPLY,
                new ProtoClassContainer(DomainSessionStartTimeReplyProto.DomainSessionStartTimeReply.class,
                        DomainSessionStartTimeReplyProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.UNDER_DWELL_VIOLATION,
                new ProtoClassContainer(StringPayloadProto.StringPayload.class, StringPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.OVER_DWELL_VIOLATION,
                new ProtoClassContainer(StringPayloadProto.StringPayload.class, StringPayloadProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.VIBRATE_DEVICE_REQUEST,
                new ProtoClassContainer(VibrateDeviceProto.VibrateDevice.class, VibrateDeviceProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.GEOLOCATION,
                new ProtoClassContainer(GeolocationProto.Geolocation.class, GeolocationProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST,
                new ProtoClassContainer(AuthorizeStrategiesRequestProto.AuthorizeStrategiesRequest.class,
                        AuthorizeStrategiesRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.EXECUTE_OC_STRATEGY, new ProtoClassContainer(
                ExecuteOCStrategyProto.ExecuteOCStrategy.class, ExecuteOCStrategyProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.APPLY_STRATEGIES,
                new ProtoClassContainer(ApplyStrategiesProto.ApplyStrategies.class, ApplyStrategiesProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.WEB_MONITOR_MODULE_STATUS, new ProtoClassContainer(
                WebMonitorModuleStatusProto.WebMonitorModuleStatus.class, WebMonitorModuleStatusProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.KNOWLEDGE_SESSION_CREATED, new ProtoClassContainer(
                KnowledgeSessionCreatedProto.KnowledgeSessionCreated.class, KnowledgeSessionCreatedProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.WEAPON_STATE_REQUEST, new ProtoClassContainer(
                VariablesStateRequestProto.VariablesStateRequest.class, VariablesStateRequestProtoCodec.class));
        messageWrapper.put(MessageTypeEnum.WEAPON_STATE_RESULT, new ProtoClassContainer(
                VariablesStateResultProto.VariablesStateResult.class, VariablesStateResultProtoCodec.class));
    }

    /** the singleton instance of this class */
    private static ProtoMapper instance = null;

    /**
     * Return the singleton instance of this class
     *
     * @return ProtoCodecMapper
     */
    public static ProtoMapper getInstance() {

        if (instance == null) {
            instance = new ProtoMapper();
        }

        return instance;
    }

    private ProtoMapper() {
    }

    /**
     * Return the class which represents the protobuf type for packing and
     * unpacking.
     *
     * @param messageType the type of message to get the object class for
     * @return Class<?>
     */
    public Class<?> getObjectClass(MessageTypeEnum messageType) {

        if (messageWrapper.containsKey(messageType)) {
            return messageWrapper.get(messageType).getObjectClass();
        } else {
            throw new IllegalArgumentException("The message type " + messageType + " isn't supported.");
        }
    }

    /**
     * Return the class which can Protobuf encode/decode the specified message
     * type.
     *
     * @param messageType the type of message to get the codec for
     * @return Class<?>
     */
    public Class<?> getCodecClass(MessageTypeEnum messageType) {

        if (messageWrapper.containsKey(messageType)) {
            return messageWrapper.get(messageType).getCodecClass();
        } else {
            throw new IllegalArgumentException("The message type " + messageType + " isn't supported.");
        }
    }

    /**
     * This class serves as a container for the protobuf class and the codec
     * class type that will be used to convert/map the payload after
     * packing/unpacking.
     * 
     * @author cpolynice
     *
     */
    private static class ProtoClassContainer {

        /**
         * The protobuf class that will be used to pack and unpack the message
         * from binary.
         */
        private Class<?> protoClass;

        /**
         * The protobuf codec class that corresponds to the protobuf class that
         * will convert/map the payload for the message being sent and received.
         */
        private Class<?> protoCodecClass;

        public ProtoClassContainer(Class<?> protoClass, Class<?> protoCodecClass) {
            this.protoClass = protoClass;
            this.protoCodecClass = protoCodecClass;
        }

        /**
         * Getter for the protobuf class.
         * 
         * @return the protobuf class.
         */
        public Class<?> getObjectClass() {
            return protoClass;
        }

        /**
         * Getter for the protobuf codec class.
         * 
         * @return the protobuf codec class.
         */
        public Class<?> getCodecClass() {
            return protoCodecClass;
        }
    }
}
