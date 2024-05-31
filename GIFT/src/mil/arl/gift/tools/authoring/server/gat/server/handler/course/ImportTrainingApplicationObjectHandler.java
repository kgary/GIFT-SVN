/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.time.FastDateFormat;
import org.hibernate.Session;
import org.xml.sax.SAXException;

import generated.course.AuthoringSupportElements;
import generated.course.EmbeddedApp;
import generated.course.EmbeddedApps;
import generated.course.TrainingApplication;
import generated.course.TrainingApplicationWrapper;
import generated.dkf.Assessments;
import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Scenario;
import generated.dkf.Task;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.GiftScenarioProperties;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.server.GatRpcServiceImpl;
import mil.arl.gift.tools.authoring.server.gat.server.survey.SurveyExportFileUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ImportTrainingApplicationObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ImportTrainingApplicationObjectResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.AbstractFileServices.NameCollisionResolutionBehavior;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.survey.Surveys;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handler to import training applications created in GIFT Wrap
 *
 * @author bzahid
 */
public class ImportTrainingApplicationObjectHandler
        implements ActionHandler<ImportTrainingApplicationObject, ImportTrainingApplicationObjectResult> {

    /** The tag to log this class' metrics */
    private static final String METRICS_TAG = "course.ImportTrainingApplicationObject";

    /** The date format used to create unique survey context keys */
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyyMMdd_HHmmss", null, null);

    /** The instance of File Services */
    private final AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();

    /** instance of the UMS database manager used to perform database operations */
    private static UMSDatabaseManager dbMgr = UMSDatabaseManager.getInstance();

    @Override
    public Class<ImportTrainingApplicationObject> getActionType() {
        return ImportTrainingApplicationObject.class;
    }

    @Override
    public void rollback(ImportTrainingApplicationObject action, ImportTrainingApplicationObjectResult result,
            ExecutionContext context) throws DispatchException {
        /* nothing to rollback */
    }

    @Override
    public ImportTrainingApplicationObjectResult execute(ImportTrainingApplicationObject action,
            ExecutionContext context) throws DispatchException {
        long start = System.currentTimeMillis();

        /* extract the action data */
        final String username = action.getUsername();
        final String sourceRealTimeAssessmentFolder = action.getRealTimeAssessmentFolder();
        final String destinationCourseFolderPath = action.getCourseFolderPath();
        final int surveyContextId = action.getSurveyContextId();
        final String browserSessionKey = action.getBrowserSessionKey();

        ImportTrainingApplicationObjectResult result;
        Session session = null;
        List<String> copiedFiles = new ArrayList<String>();
        try {
            result = new ImportTrainingApplicationObjectResult();

            /* check if a DKf file exists in the destination with the same name as this real time
             * assessment folder, then this assessment has been imported previously and we have a
             * conflict */
            FileTreeModel sourceModel = FileTreeModel.createFromRawPath(sourceRealTimeAssessmentFolder);
            if (!action.isOverwrite()) {
                String dkfPath = destinationCourseFolderPath + File.separator + sourceModel.getFileOrDirectoryName()
                        + AbstractSchemaHandler.DKF_FILE_EXTENSION;
                boolean conflict = fileServices.fileExists(username, dkfPath, false);
                if (conflict) {
                    result = new ImportTrainingApplicationObjectResult();
                    result.setSuccess(true);
                    result.setFoundConflict(true);

                    MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
                    return result;
                }
            }

            /******* STEP 1 *******/
            /* Retrieve the training application and set into result */
            AbstractFolderProxy rtaFolderProxy = fileServices.getFolder(sourceModel.getRelativePathFromRoot(),
                    username);
            final File rtaFolderFile = new File(rtaFolderProxy.getFileId());
            TrainingApplicationWrapper wrapper = getCourseObjectXMLObject(rtaFolderFile, username);
            result.setTaWrapper(wrapper);

            final TrainingApplication trainingApplication = wrapper.getTrainingApplication();

            /******* STEP 2 *******/
            /* Copy folder contents to destination course folder */
            String dkfFilePath = null;
            AbstractFolderProxy taLibFolder = fileServices.getFolder(sourceRealTimeAssessmentFolder, username);
            for (FileProxy childFile : taLibFolder.listFiles(null)) {
                if (childFile.getName().endsWith(AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION)) {
                    /* skip trainingapp.xml files */
                    continue;
                } else if (childFile.getName().endsWith(FileUtil.SURVEY_REF_EXPORT_SUFFIX)) {
                    /* skip survey export files */
                    continue;
                }

                boolean isDkf = childFile.getName().endsWith(AbstractSchemaHandler.DKF_FILE_EXTENSION);
                NameCollisionResolutionBehavior nameCollisionResolutionBehavior = isDkf
                        ? NameCollisionResolutionBehavior.GUARANTEE_UNIQUE_NAME
                        : NameCollisionResolutionBehavior.OVERWRITE;

                FileTreeModel relativeChildPath = fileServices.trimWorkspaceFromPath(childFile.getFileId(), username);
                String copiedPath = fileServices.copyWorkspaceFile(username,
                        relativeChildPath.getRelativePathFromRoot(), destinationCourseFolderPath,
                        nameCollisionResolutionBehavior, null);
                copiedFiles.add(copiedPath);
                if (isDkf) {
                    dkfFilePath = copiedPath;
                }
            }

            if (dkfFilePath == null) {
                throw new Exception(
                        "The source real time assessment folder does not contain a DKF file. There is nothing to import.");
            }

            /* update DKF file name in the training application wrapper since it may have changed
             * during copy */
            FileProxy dkfFileProxy = fileServices.getFile(dkfFilePath, username);
            trainingApplication.getDkfRef().setFile(dkfFileProxy.getName());

            /******* STEP 3 *******/
            /* Import surveys to database */
            session = dbMgr.createNewSession();
            session.beginTransaction();

            HashMap<String, String> oldToNewSurveyKeyMap = new HashMap<String, String>();

            /* Checks if a survey export file exists in the real time assessment folder. If it does,
             * import the surveys contained within and add them to the survey context with the
             * associated survey context id */
            importSurveys(FileTreeModel.createFromRawPath(sourceRealTimeAssessmentFolder), surveyContextId, username,
                    browserSessionKey, oldToNewSurveyKeyMap, session);

            /******* STEP 4 *******/
            /* Retrieve DKF scenario and update survey context ids */
            Scenario scenario = getDKFXMLObject(dkfFileProxy);

            /* update DKF survey context id */
            if (scenario.getResources() != null) {
                scenario.getResources().setSurveyContext(BigInteger.valueOf(surveyContextId));
            }

            /* The survey keys changed during import. Update the scenario's survey references with
             * the new keys */
            updateScenarioWithNewSurveyKeys(scenario, oldToNewSurveyKeyMap);

            /* Write scenario back to file since the survey context id has changed and the survey
             * keys may have changed as well */
            fileServices.marshalToFile(username, scenario, dkfFilePath, null);

            /******* STEP 5 (CONDITIONAL) *******/
            /* Update training application's embedded app with the entry point if it exists */
            String entryPoint = getScenarioEntryPoint(wrapper, destinationCourseFolderPath, username);
            if (StringUtils.isNotBlank(entryPoint)) {
                EmbeddedApps embeddedApps = trainingApplication.getEmbeddedApps();
                if (embeddedApps == null) {
                    embeddedApps = new EmbeddedApps();
                    trainingApplication.setEmbeddedApps(embeddedApps);
                }

                EmbeddedApp embeddedApp = embeddedApps.getEmbeddedApp();
                if (embeddedApp == null) {
                    embeddedApp = new EmbeddedApp();
                    embeddedApps.setEmbeddedApp(embeddedApp);
                }

                embeddedApp.setEmbeddedAppImpl(entryPoint);
            }

            /******* STEP 6 *******/
            /* Commit transaction, close session, and return success */
            session.getTransaction().commit();
            session.close();

            MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            result = new ImportTrainingApplicationObjectResult();
            result.setSuccess(false);
            result.setErrorMsg("The training application at '" + sourceRealTimeAssessmentFolder
                    + "' failed to import because '" + e.getMessage() + "'");
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            result.setErrorDetails("The source folder '" + sourceRealTimeAssessmentFolder + "' could not be copied to '"
                    + destinationCourseFolderPath + "'.");

            /* undo copy and scenario write */
            if (!copiedFiles.isEmpty()) {
                for (String copiedFilePath : copiedFiles) {
                    fileServices.deleteFile(username, browserSessionKey, copiedFilePath, null, false);
                }
            }

            /* rollback survey database changes */
            if (session != null && session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }

            MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
            return result;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Retrieves the trainingApp.xml contents from the course object folder provided. There should
     * only be one trainingApp.xml file found in the course object folder in order for that folder
     * to be GIFT compliant.
     *
     * @param rtaFolderFile contains the trainingApp.xml to read
     * @param username used for authentication
     * @return the contents of the trainingApp.xml found in the course object folder
     * @throws DetailedException if there was a problem finding or parsing the one and only
     *         trainingApp.xml file.
     */
    private generated.course.TrainingApplicationWrapper getCourseObjectXMLObject(File rtaFolderFile, String username)
            throws DetailedException {

        /* find the trainingapp.xml */
        List<FileProxy> trainingAppFiles = new ArrayList<>();
        try {
            AbstractFolderProxy startingDirectory = fileServices.getFolderFromFile(rtaFolderFile, username);
            FileFinderUtil.getFilesByExtension(startingDirectory, trainingAppFiles,
                    AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
        } catch (IOException e) {
            throw new DetailedException("Failed to load the course object.",
                    "There was a problem searching for the " + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION
                            + " file in '" + rtaFolderFile + "'.  The error reads:\n" + e.getMessage(),
                    e);
        }

        /* there should be exactly 1 training application xml in the folder */
        if (trainingAppFiles.isEmpty() || trainingAppFiles.get(0) == null) {
            throw new DetailedException("Failed to load the course object.", "Unable to find a "
                    + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION + " in '" + rtaFolderFile + "'.", null);
        } else if (trainingAppFiles.size() > 1) {
            throw new DetailedException("Failed to load the course object.",
                    "Found " + trainingAppFiles.size() + " " + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION
                            + " files in '" + rtaFolderFile + "'. There may only be 1 "
                            + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION + " file.",
                    null);
        }

        FileProxy trainingAppFile = trainingAppFiles.get(0);

        generated.course.TrainingApplicationWrapper courseObject = null;
        try {
            UnmarshalledFile unmashalledFile = AbstractSchemaHandler.parseAndValidate(
                    generated.course.TrainingApplicationWrapper.class, trainingAppFile.getInputStream(),
                    AbstractSchemaHandler.TRAINING_APP_ELEMENT_SCHEMA_FILE, false);
            courseObject = (TrainingApplicationWrapper) unmashalledFile.getUnmarshalled();
        } catch (Exception e) {
            throw new DetailedException("Failed to load the course object.",
                    "There was a problem with parsing the " + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION
                            + " file of '" + trainingAppFile + "'.  The error reads:\n" + e.getMessage(),
                    e);
        }

        return courseObject;
    }

    /**
     * Import the surveys within the survey export file found in the provided training app folder.
     * There should only be (at most) one survey export file in the folder. The surveys will be
     * moved out of their current survey context and into the {@link SurveyContext} with the
     * provided survey context id. The {@link SurveyContextSurvey survey context survey's} key value
     * will be made unique by appending a timestamp to the end.
     *
     * @param realTimeAssessmentFolder the {@link FileTreeModel} of the real time assessment folder
     *        after it was copied into the TrainingAppsLib directory.
     * @param courseSurveyContextId the survey context id for the course that these training
     *        application surveys are being imported into.
     * @param username the username of the user invoking this operation.
     * @param browserSessionKey the unique identifier of the browser that is performing the
     *        operation.
     * @param oldToNewSurveyKeyMap a map to contain the references for the survey context survey
     *        keys. Maps the original values to the new unique values.
     * @param session a database session with a transaction (that has already began) to do all
     *        theinsert operations in. This is useful for doing rollback operations if any one of
     *        theinserts fails. Can be null if the caller doesn't care about partial fails and wants
     *        this class to manage the session.
     * @throws Exception if there was a problem importing the surveys
     */
    private void importSurveys(FileTreeModel realTimeAssessmentFolder, int courseSurveyContextId, String username,
            String browserSessionKey, HashMap<String, String> oldToNewSurveyKeyMap, Session session) throws Exception {
        AbstractFolderProxy rtaFolderProxy = fileServices.getFolder(realTimeAssessmentFolder.getRelativePathFromRoot(),
                username);
        FileProxy surveyContextProxy = getSurveyContextFile(new File(rtaFolderProxy.getFileId()), username);

        if (surveyContextProxy != null) {
            SurveyContext surveyContext = SurveyExportFileUtil.getSurveyContextFromExport(surveyContextProxy);

            if (surveyContext != null) {
                /* process each survey context survey */
                for (SurveyContextSurvey scs : surveyContext.getContextSurveys()) {
                    /* create unique survey context survey key */
                    final String oldKey = scs.getKey();
                    scs.setKey(oldKey + "_" + DATE_FORMAT.format(new Date()));
                    oldToNewSurveyKeyMap.put(oldKey, scs.getKey());
                }

                Surveys.insertSurveyContextSurveysIntoSurveyContext(surveyContext.getContextSurveys(),
                        courseSurveyContextId, username, session);
            }
        }
    }

    /**
     * Retrieve the survey context export file for the real time assessment folder provided.
     *
     * @param realTimeAssessmentFolder the folder to search for the survey context export file.
     *        Can't be null and must exist.
     * @param username the username of the user invoking this operation.
     * @return the survey context export file found. Null if no export file was found.
     * @throws DetailedException if there was a problem searching for the file or more than one was
     *         found
     */
    private FileProxy getSurveyContextFile(File realTimeAssessmentFolder, String username) throws DetailedException {

        AbstractFolderProxy rtaFolderProxy = fileServices.getFolderFromFile(realTimeAssessmentFolder, username);

        /* find survey export */
        List<FileProxy> surveyExportFiles = new ArrayList<>();
        try {
            FileFinderUtil.getFilesByExtension(rtaFolderProxy, surveyExportFiles, FileUtil.SURVEY_REF_EXPORT_SUFFIX);
        } catch (IOException e) {
            throw new DetailedException("Failed to parse the survey context.",
                    "There was a problem searching for " + FileUtil.SURVEY_REF_EXPORT_SUFFIX + " files in '"
                            + realTimeAssessmentFolder + "'.  The error reads:\n" + e.getMessage(),
                    e);
        }

        if (surveyExportFiles.size() > 1) {
            throw new DetailedException("Failed to parse the survey context.",
                    "Found " + surveyExportFiles.size() + " " + FileUtil.SURVEY_REF_EXPORT_SUFFIX + " files in '"
                            + realTimeAssessmentFolder + "'.  A real time assessment folder may only have 1 "
                            + FileUtil.SURVEY_REF_EXPORT_SUFFIX + " file.",
                    null);
        } else if (surveyExportFiles.isEmpty()) {
            return null;
        } else {
            return surveyExportFiles.get(0);
        }
    }

    /**
     * Retrieves the dkf.xml contents from the file specified.
     *
     * @param dkf the XML file to parse
     * @return the contents of the dkf.xml file
     */
    private generated.dkf.Scenario getDKFXMLObject(FileProxy dkf) {

        try {
            UnmarshalledFile unmashalledFile = AbstractSchemaHandler.parseAndValidate(dkf.getInputStream(),
                    FileType.DKF, false);
            return (generated.dkf.Scenario) unmashalledFile.getUnmarshalled();
        } catch (JAXBException | SAXException | IOException e) {
            throw new DetailedException("Failed to read the DKF.",
                    "There was a problem reading the DKF '" + dkf + "'.  The error reads:\n" + e.getMessage(), e);
        }
    }

    /**
     * Updates any of the {@link SurveyContextSurvey survey context surveys} within the
     * {@link Scenario} with the new survey keys found in the provided map.
     *
     * @param scenario the scenario containing the survey context surveys.
     * @param oldToNewSurveyKeyMap the map containing the old survey key and the new survey key for
     *        the survey context surveys.
     */
    private void updateScenarioWithNewSurveyKeys(Scenario scenario, HashMap<String, String> oldToNewSurveyKeyMap) {
        /* perform all null checks needed to continue safely. */
        if (oldToNewSurveyKeyMap == null || oldToNewSurveyKeyMap.isEmpty() || scenario == null
                || scenario.getAssessment() == null || scenario.getAssessment().getTasks() == null
                || scenario.getAssessment().getTasks().getTask() == null) {
            return;
        }

        for (Task task : scenario.getAssessment().getTasks().getTask()) {
            final Assessments taskAssessments = task.getAssessments();
            if (taskAssessments != null) {
                final List<Serializable> taskAssessmentTypes = taskAssessments.getAssessmentTypes();
                if (taskAssessmentTypes != null) {
                    for (Serializable taskAssessmentType : taskAssessmentTypes) {
                        if (taskAssessmentType instanceof Assessments.Survey) {
                            Assessments.Survey survey = (Assessments.Survey) taskAssessmentType;
                            String surveyKey = survey.getGIFTSurveyKey();
                            if (oldToNewSurveyKeyMap.containsKey(surveyKey)) {
                                survey.setGIFTSurveyKey(oldToNewSurveyKeyMap.get(surveyKey));
                            }
                        }
                    }
                }
            }

            if (task.getConcepts() != null && task.getConcepts().getConcept() != null) {
                for (Concept concept : task.getConcepts().getConcept()) {
                    updateConceptWithNewSurveyKeys(concept, oldToNewSurveyKeyMap);
                }
            }
        }
    }

    /**
     * Updates any of the {@link SurveyContextSurvey survey context surveys} within the
     * {@link Concept} with the new survey keys found in the provided map.
     *
     * @param concept the concept containing the survey context surveys.
     * @param oldToNewSurveyKeyMap the map containing the old survey key and the new survey key for
     *        the survey context surveys.
     */
    private void updateConceptWithNewSurveyKeys(Concept concept, HashMap<String, String> oldToNewSurveyKeyMap) {
        final Assessments conceptAssessments = concept.getAssessments();
        if (conceptAssessments != null) {
            final List<Serializable> conceptAssessmentTypes = conceptAssessments.getAssessmentTypes();
            if (conceptAssessmentTypes != null) {
                for (Serializable conceptAssessmentType : conceptAssessmentTypes) {
                    if (conceptAssessmentType instanceof Assessments.Survey) {
                        Assessments.Survey survey = (Assessments.Survey) conceptAssessmentType;
                        String surveyKey = survey.getGIFTSurveyKey();
                        if (oldToNewSurveyKeyMap.containsKey(surveyKey)) {
                            survey.setGIFTSurveyKey(oldToNewSurveyKeyMap.get(surveyKey));
                        }
                    }
                }
            }
        }

        if (concept.getConditionsOrConcepts() instanceof Concepts) {
            Concepts concepts = (Concepts) concept.getConditionsOrConcepts();
            if (concepts.getConcept() == null) {
                return;
            }

            for (Concept childConcept : concepts.getConcept()) {
                updateConceptWithNewSurveyKeys(childConcept, oldToNewSurveyKeyMap);
            }
        }
    }

    /**
     * Downloads the Unity project specified within the training application's scenario property
     * file to the course folder
     *
     * @param wrapper the training application wrapper that contains the training application
     * @param destinationCourseFolderPath the path to the course folder. The root of the path must
     *        be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param username the username of the user invoking this operation.
     * @return the path of the unzipped folder within the provided course folder.
     * @throws IOException if an I/O exception occurs
     */
    private String getScenarioEntryPoint(TrainingApplicationWrapper wrapper, String destinationCourseFolderPath,
            String username) throws IOException {
        if (wrapper == null || wrapper.getTrainingApplication() == null) {
            return null;
        }

        TrainingApplication ta = wrapper.getTrainingApplication();

        /* Exit early if training app does not contain the Unity scenario map path */
        AuthoringSupportElements supportElements = ta.getAuthoringSupportElements();
        if (supportElements == null || StringUtils.isBlank(supportElements.getTrainingAppsScenarioMapPath())) {
            return null;
        }

        String unityScenarioMapPath = supportElements.getTrainingAppsScenarioMapPath();

        /* Exit early if the type is not Unity */
        TrainingApplicationEnum taType = TrainingAppUtil.getTrainingAppType(ta);
        if (!TrainingApplicationEnum.UNITY_EMBEDDED.equals(taType)) {
            return null;
        }

        /* Retrieve the scenario properties */
        GatRpcServiceImpl rpcService = new GatRpcServiceImpl();
        GenericRpcResponse<GiftScenarioProperties> propertyResponse = rpcService
                .getTrainingApplicationScenarioProperty(unityScenarioMapPath, username);

        /* Exit early if the response does not contain the properties */
        if (propertyResponse == null || !propertyResponse.getWasSuccessful() || propertyResponse.getContent() == null) {
            return null;
        }

        GiftScenarioProperties property = propertyResponse.getContent();
        return property.getScenarioEntryPointPathWithParentPath();
    }
}
