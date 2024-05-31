/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.tools.authoring.server.gat.server.handler.conversation.EndConversationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.conversation.FetchCharacterServerStatusHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.conversation.FetchConversationTreeJSONHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.conversation.LockConversationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.conversation.SaveConversationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.conversation.UnlockConversationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.conversation.UpdateConversationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.ConvertLessonMaterialFilesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.CreateSlideShowHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.DeleteMetadataHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.FetchAllDomainOptionsHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.FetchContentAddressHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.FetchCourseHistoryHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.FetchLtiPropertiesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.FetchMediaFilesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.GenerateLessonMaterialReferenceFileHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.GenerateMetadataFileHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.GenerateQuestionExportReferenceFileHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.GenerateTrainingAppReferenceFileHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.GetMerrillQuadrantFilesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.GetMetadataFilesForMerrillQuadrantHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.GetPracticeApplicationsHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.ImportTrainingApplicationObjectHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.LockCourseHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.SaveCourseHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.UnlockCourseHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.UnzipFileHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.WorkspaceFileExistsHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.course.WorkspaceFilesExistHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.FetchAvatarKeyNamesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.FetchConditionImplDescriptionHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.FetchConditionImplNamesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.FetchConditionInputParamsHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.FetchConditionsOverallAssessmentTypesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.FetchSimileConceptsHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.FetchStrategyHandlerClassNamesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.FetchTrainingAppScenarioAdaptationsHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.LockDkfHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.SaveDkfHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.dkf.UnlockDkfHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.learner.FetchDefaultLearnerConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.learner.LockLearnerConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.learner.SaveLearnerConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.learner.UnlockLearnerConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.metadata.LockLessonMaterialReferenceHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.metadata.LockMetadataHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.metadata.LockTrainingApplicationReferenceHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.metadata.SaveMetadataHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.metadata.SaveTrainingApplicationReferenceHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.metadata.UnlockLessonMaterialReferenceHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.metadata.UnlockMetadataHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.metadata.UnlockTrainingApplicationReferenceHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.pedagogy.FetchDefaultPedagogyConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.pedagogy.LockPedagogyConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.pedagogy.SavePedagogyConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.pedagogy.UnlockPedagogyConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.sensor.LockSensorsConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.sensor.SaveSensorsConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.sensor.UnlockSensorsConfigurationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.surveyeditor.ImportQsfHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.CancelExportHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.CancelImportHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.CopyTemplateFileHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.CopyWorkspaceFilesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.CreateWorkspaceFolderHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.DeleteCourseSurveyReferenceHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.DeleteExportFileHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.DeleteSurveyHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.DeleteWorkspaceFilesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.DownloadGatErrorsHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.ExportCoursesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.FetchDomainContentServerAddressHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.FetchFilesByExtensionHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.FetchGatServerPropertiesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.FetchInteropImplementationsHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.FetchJAXBObjectHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.FetchQuestionExportHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.FetchRootDirectoryModelHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.FetchSurveyContextByIdHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.FetchSurveyContextSurveysHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.FetchSurveyQuestionsHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.GetExportProgressHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.GetExportSizeHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.GetOrCreateSurveyContextHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.GetProgressHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.GetRemainingDiskSpaceHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.LogUncaughtClientExceptionHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.MoveDomainFileToWorkspaceLocationHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.ParsePlaybackLogHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.UnlockWorkspaceFilesHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.ValidateFileHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.ValidateMediaSemanticsHandler;
import mil.arl.gift.tools.authoring.server.gat.server.handler.util.ValidateUnityWebGLApplicationHandler;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.EndConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.FetchCharacterServerStatus;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.FetchConversationTreeJSON;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.LockConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.SaveConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UnlockConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ConvertLessonMaterialFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShow;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.DeleteMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchAllDomainOptions;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchCourseHistory;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchLtiProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchMediaFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateLessonMaterialReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateMetadataFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateQuestionExportReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateTrainingAppReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMerrillQuadrantFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMetadataFilesForMerrillQuadrant;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetPracticeApplications;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ImportTrainingApplicationObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockCourse;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.SaveCourse;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnlockCourse;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.WorkspaceFileExists;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.WorkspaceFilesExist;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchAvatarKeyNames;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionImplDescription;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionImplNames;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionInputParams;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionsOverallAssessmentTypes;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchSimileConcepts;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchStrategyHandlerClassNames;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchTrainingAppScenarioAdaptations;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.LockDkf;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.SaveDkf;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.UnlockDkf;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.FetchDefaultLearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.LockLearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.SaveLearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.UnlockLearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.LockLessonMaterialReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.LockMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.LockTrainingApplicationReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.SaveMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.SaveTrainingApplicationReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.UnlockLessonMaterialReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.UnlockMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.action.metadata.UnlockTrainingApplicationReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.pedagogy.FetchDefaultPedagogyConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.pedagogy.LockPedagogyConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.pedagogy.SavePedagogyConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.pedagogy.UnlockPedagogyConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.sensor.LockSensorsConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.sensor.SaveSensorsConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.sensor.UnlockSensorsConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.surveyeditor.ImportQsf;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CancelExport;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CancelImport;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyTemplateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CreateWorkspaceFolder;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteCourseSurveyReference;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteExportFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteSurvey;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DownloadGatErrors;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ExportCourses;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchDomainContentServerAddress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchFilesByExtension;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchGatServerProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchInteropImplementations;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchQuestionExport;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextById;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextSurveys;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyQuestions;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetExportProgress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetExportSize;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetOrCreateSurveyContext;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetRemainingDiskSpace;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.LogUncaughtClientException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.MoveDomainFileToWorkspaceLocation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ParsePlaybackLog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.UnlockWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateMediaSemantics;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateUnityWebGLApplication;
import net.customware.gwt.dispatch.server.guice.ActionHandlerModule;


/**
 * This class maps the commands to the handlers
 * See BootstrapListener.
 * 
 * @author iapostolos and cragusa
 */
public class ActionsModule extends ActionHandlerModule {
    
    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(ActionsModule.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.guice.ActionHandlerModule#configureHandlers()
     */
    @Override
    protected void configureHandlers() {
    	
    	logger.info("ActionsModule configureHandlers()");
        
        //custnodes 
        bindHandler(LogUncaughtClientException.class, LogUncaughtClientExceptionHandler.class);
        
        // dashboard
        bindHandler(FetchGatServerProperties.class, FetchGatServerPropertiesHandler.class);
        
        //courses
        bindHandler(SaveCourse.class, SaveCourseHandler.class);
        bindHandler(FetchJAXBObject.class, FetchJAXBObjectHandler.class);
        bindHandler(FetchSurveyContextById.class, FetchSurveyContextByIdHandler.class);
        bindHandler(FetchSurveyContextSurveys.class, FetchSurveyContextSurveysHandler.class);
        bindHandler(FetchSurveyQuestions.class, FetchSurveyQuestionsHandler.class);
        bindHandler(FetchQuestionExport.class, FetchQuestionExportHandler.class);
        bindHandler(LockCourse.class, LockCourseHandler.class);
        bindHandler(UnlockCourse.class, UnlockCourseHandler.class);
        bindHandler(FetchConversationTreeJSON.class, FetchConversationTreeJSONHandler.class);
        bindHandler(FetchMediaFiles.class, FetchMediaFilesHandler.class);
        bindHandler(FetchRootDirectoryModel.class, FetchRootDirectoryModelHandler.class);
        bindHandler(FetchInteropImplementations.class, FetchInteropImplementationsHandler.class);
        bindHandler(ParsePlaybackLog.class, ParsePlaybackLogHandler.class);
        bindHandler(GenerateMetadataFile.class, GenerateMetadataFileHandler.class);
        bindHandler(GenerateTrainingAppReferenceFile.class, GenerateTrainingAppReferenceFileHandler.class);
        bindHandler(GenerateLessonMaterialReferenceFile.class, GenerateLessonMaterialReferenceFileHandler.class);
        bindHandler(GenerateQuestionExportReferenceFile.class, GenerateQuestionExportReferenceFileHandler.class);
        bindHandler(MoveDomainFileToWorkspaceLocation.class, MoveDomainFileToWorkspaceLocationHandler.class);
        bindHandler(CreateSlideShow.class, CreateSlideShowHandler.class);
        bindHandler(UnzipFile.class, UnzipFileHandler.class);
        bindHandler(CreateWorkspaceFolder.class, CreateWorkspaceFolderHandler.class);
        bindHandler(CopyWorkspaceFiles.class, CopyWorkspaceFilesHandler.class);
        bindHandler(CopyTemplateFile.class, CopyTemplateFileHandler.class);
        bindHandler(DeleteWorkspaceFiles.class, DeleteWorkspaceFilesHandler.class);
        bindHandler(UnlockWorkspaceFiles.class, UnlockWorkspaceFilesHandler.class);
        bindHandler(ImportTrainingApplicationObject.class, ImportTrainingApplicationObjectHandler.class);
        bindHandler(ImportQsf.class, ImportQsfHandler.class);
        bindHandler(ExportCourses.class, ExportCoursesHandler.class);
        bindHandler(GetProgress.class, GetProgressHandler.class);
        bindHandler(GetExportProgress.class, GetExportProgressHandler.class);
        bindHandler(GetExportSize.class, GetExportSizeHandler.class);
        bindHandler(CancelImport.class, CancelImportHandler.class);
        bindHandler(CancelExport.class, CancelExportHandler.class);
        bindHandler(FetchAllDomainOptions.class, FetchAllDomainOptionsHandler.class);
        bindHandler(DeleteExportFile.class, DeleteExportFileHandler.class);
        bindHandler(ValidateFile.class, ValidateFileHandler.class);
        bindHandler(ValidateMediaSemantics.class, ValidateMediaSemanticsHandler.class);
        bindHandler(ValidateUnityWebGLApplication.class, ValidateUnityWebGLApplicationHandler.class);
        bindHandler(DownloadGatErrors.class, DownloadGatErrorsHandler.class);
        bindHandler(FetchDomainContentServerAddress.class, FetchDomainContentServerAddressHandler.class);
        bindHandler(GetRemainingDiskSpace.class, GetRemainingDiskSpaceHandler.class);
        bindHandler(GetMerrillQuadrantFiles.class, GetMerrillQuadrantFilesHandler.class);
        bindHandler(ConvertLessonMaterialFiles.class, ConvertLessonMaterialFilesHandler.class);
        bindHandler(GetMetadataFilesForMerrillQuadrant.class, GetMetadataFilesForMerrillQuadrantHandler.class);
        bindHandler(FetchConditionImplDescription.class, FetchConditionImplDescriptionHandler.class);
        bindHandler(FetchConditionsOverallAssessmentTypes.class, FetchConditionsOverallAssessmentTypesHandler.class);
        bindHandler(FetchContentAddress.class, FetchContentAddressHandler.class);
        bindHandler(FetchCourseHistory.class, FetchCourseHistoryHandler.class);
        bindHandler(FetchLtiProperties.class, FetchLtiPropertiesHandler.class);
        bindHandler(GetOrCreateSurveyContext.class, GetOrCreateSurveyContextHandler.class);
        bindHandler(DeleteSurvey.class, DeleteSurveyHandler.class);
        bindHandler(DeleteCourseSurveyReference.class, DeleteCourseSurveyReferenceHandler.class);
        bindHandler(GetPracticeApplications.class, GetPracticeApplicationsHandler.class);
        bindHandler(DeleteMetadata.class, DeleteMetadataHandler.class);
        bindHandler(WorkspaceFileExists.class, WorkspaceFileExistsHandler.class);
        bindHandler(WorkspaceFilesExist.class, WorkspaceFilesExistHandler.class);
        bindHandler(FetchTrainingAppScenarioAdaptations.class, FetchTrainingAppScenarioAdaptationsHandler.class);
        
        //dkfs
        bindHandler(SaveDkf.class, SaveDkfHandler.class);
        bindHandler(LockDkf.class, LockDkfHandler.class);
        bindHandler(UnlockDkf.class, UnlockDkfHandler.class);
        bindHandler(FetchStrategyHandlerClassNames.class, FetchStrategyHandlerClassNamesHandler.class);
        bindHandler(FetchFilesByExtension.class, FetchFilesByExtensionHandler.class);
        bindHandler(FetchAvatarKeyNames.class, FetchAvatarKeyNamesHandler.class);
        bindHandler(FetchConditionImplNames.class, FetchConditionImplNamesHandler.class);
        bindHandler(FetchConditionInputParams.class, FetchConditionInputParamsHandler.class);
        bindHandler(FetchSimileConcepts.class, FetchSimileConceptsHandler.class);
        
        //sensors configuration
        bindHandler(LockSensorsConfiguration.class, LockSensorsConfigurationHandler.class);
        bindHandler(SaveSensorsConfiguration.class, SaveSensorsConfigurationHandler.class);
        bindHandler(UnlockSensorsConfiguration.class, UnlockSensorsConfigurationHandler.class);
        
        //learner configuration
        bindHandler(LockLearnerConfiguration.class, LockLearnerConfigurationHandler.class);
        bindHandler(SaveLearnerConfiguration.class, SaveLearnerConfigurationHandler.class);
        bindHandler(UnlockLearnerConfiguration.class, UnlockLearnerConfigurationHandler.class);
        bindHandler(FetchDefaultLearnerConfiguration.class, FetchDefaultLearnerConfigurationHandler.class);
        
        //metadata
        bindHandler(LockMetadata.class, LockMetadataHandler.class);
        bindHandler(SaveMetadata.class, SaveMetadataHandler.class);
        bindHandler(UnlockMetadata.class, UnlockMetadataHandler.class);       
        bindHandler(LockTrainingApplicationReference.class, LockTrainingApplicationReferenceHandler.class);
        bindHandler(SaveTrainingApplicationReference.class, SaveTrainingApplicationReferenceHandler.class);
        bindHandler(UnlockTrainingApplicationReference.class, UnlockTrainingApplicationReferenceHandler.class);
        bindHandler(LockLessonMaterialReference.class, LockLessonMaterialReferenceHandler.class);
        bindHandler(UnlockLessonMaterialReference.class, UnlockLessonMaterialReferenceHandler.class);
        
        //pedagogy configuration
        bindHandler(LockPedagogyConfiguration.class, LockPedagogyConfigurationHandler.class);
        bindHandler(SavePedagogyConfiguration.class, SavePedagogyConfigurationHandler.class);        
        bindHandler(UnlockPedagogyConfiguration.class, UnlockPedagogyConfigurationHandler.class);
        bindHandler(FetchDefaultPedagogyConfiguration.class, FetchDefaultPedagogyConfigurationHandler.class);
        
        //conversation configuration
        bindHandler(SaveConversation.class, SaveConversationHandler.class);
        bindHandler(UpdateConversation.class, UpdateConversationHandler.class);
        bindHandler(EndConversation.class, EndConversationHandler.class);
        bindHandler(LockConversation.class, LockConversationHandler.class);
        bindHandler(UnlockConversation.class, UnlockConversationHandler.class);
        bindHandler(FetchCharacterServerStatus.class, FetchCharacterServerStatusHandler.class);
    }
}
