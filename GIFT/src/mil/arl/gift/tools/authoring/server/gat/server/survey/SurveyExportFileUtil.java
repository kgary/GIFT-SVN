/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.survey;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyContextJSON;
import mil.arl.gift.net.nuxeo.WritePermissionException;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;

/**
 * Util class for performing operations on a survey export file
 * 
 * @author sharrison
 */
public class SurveyExportFileUtil {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(SurveyExportFileUtil.class);

    /** The instance of File Services */
    private static final AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();

    /** The default name for a survey export file */
    private static final String DEFAULT_SURVEY_EXPORT_FILENAME = "survey_context_export";

    /**
     * Deletes a survey from an export file.
     * 
     * @param realTimeAssessmentFolder the folder that contains the survey export file.
     * @param surveyKey the unique key designated for the survey to be deleted.
     * @param username information used to authenticate the request.
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular
     *         file, does not exist but cannot be created, or cannot be opened for any other reason
     * @throws IOException if there was a problem updating the file
     * @throws DetailedException if there was a severe problem marshalling the file
     * @throws WritePermissionException if the user does not have write permission to the file
     */
    public static void deleteSurveyFromExportFile(AbstractFolderProxy realTimeAssessmentFolder, String surveyKey,
            String username)
            throws IllegalArgumentException, FileNotFoundException, DetailedException, WritePermissionException {
        // delete survey from the export file in the provided real time assessment folder
        SurveyContext surveyContext = getSurveyContext(realTimeAssessmentFolder, username);
        if (surveyContext == null || surveyContext.getContextSurveys() == null
                || surveyContext.getContextSurveys().isEmpty()) {
            return;
        }

        Iterator<SurveyContextSurvey> scsItr = surveyContext.getContextSurveys().iterator();
        boolean foundSurveyContextSurvey = false;
        while (scsItr.hasNext()) {
            SurveyContextSurvey surveyContextSurvey = scsItr.next();
            if (StringUtils.equalsIgnoreCase(surveyKey, surveyContextSurvey.getKey())) {
                scsItr.remove();
                foundSurveyContextSurvey = true;
                break;
            }
        }

        // nothing to delete
        if (!foundSurveyContextSurvey) {
            return;
        }

        // guaranteed to exist because survey context was found earlier
        FileProxy fileProxy = getSurveyContextFile(realTimeAssessmentFolder, username);

        // get real time assessment folder model without the workspace directory
        FileTreeModel relativeRealTimeAssessmentFolderPath = fileServices
                .trimWorkspaceFromPath(realTimeAssessmentFolder.getFileId(), username);

        String exportFilePath = relativeRealTimeAssessmentFolderPath.getRelativePathFromRoot() + File.separator
                + fileProxy.getName();

        // write back to file
        fileServices.marshalToFile(username, surveyContext, exportFilePath, null, true);
    }

    /**
     * Saves a survey context survey to an export file.
     * 
     * @param surveyContextSurvey the survey context survey to save.
     * @param realTimeAssessmentFolder the folder that contains the survey export file.
     * @param username information used to authenticate the request.
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular
     *         file, does not exist but cannot be created, or cannot be opened for any other reason
     * @throws IOException if there was a problem updating the file
     * @throws DetailedException if there was a severe problem marshalling the file
     * @throws WritePermissionException if the user does not have write permission to the file
     */
    public static void saveSurveyToExportFile(SurveyContextSurvey surveyContextSurvey,
            AbstractFolderProxy realTimeAssessmentFolder, String username)
            throws IllegalArgumentException, FileNotFoundException, DetailedException, WritePermissionException {
        // add survey to the export file in the provided real time assessment folder
        SurveyContext surveyContext = getSurveyContext(realTimeAssessmentFolder, username);
        if (surveyContext == null) {
            surveyContext = new SurveyContext();
        } else if (surveyContext.getContextSurveys() == null) {
            surveyContext.setContextSurveys(new ArrayList<SurveyContextSurvey>());
        }

        // new survey context
        if (surveyContext.getId() == 0) {
            surveyContext.setId(surveyContextSurvey.getSurveyContextId());
            surveyContext.setName(DEFAULT_SURVEY_EXPORT_FILENAME);
        }

        boolean foundSurveyContextSurvey = false;
        Iterator<SurveyContextSurvey> scsItr = surveyContext.getContextSurveys().iterator();
        while (scsItr.hasNext()) {
            SurveyContextSurvey nextScs = scsItr.next();
            foundSurveyContextSurvey = StringUtils.equalsIgnoreCase(nextScs.getKey(), surveyContextSurvey.getKey());

            // if match found, replace survey within the context
            if (foundSurveyContextSurvey) {
                nextScs.setSurvey(surveyContextSurvey.getSurvey());
                break;
            }
        }

        // it's a new survey
        if (!foundSurveyContextSurvey) {
            // set id for new survey
            int maxId = 0;
            for (SurveyContextSurvey scs : surveyContext.getContextSurveys()) {
                maxId = Math.max(maxId, scs.getSurvey().getId());
            }
            surveyContextSurvey.getSurvey().setId(++maxId);
            surveyContext.getContextSurveys().add(surveyContextSurvey);
        }

        // get survey context file
        FileProxy fileProxy = getSurveyContextFile(realTimeAssessmentFolder, username);

        // get real time assessment folder model without the workspace directory
        FileTreeModel relativeRealTimeAssessmentFolderPath = fileServices
                .trimWorkspaceFromPath(realTimeAssessmentFolder.getFileId(), username);

        // use existing or create new
        String exportFilePath = relativeRealTimeAssessmentFolderPath.getRelativePathFromRoot() + File.separator
                + (fileProxy != null ? fileProxy.getName()
                        : DEFAULT_SURVEY_EXPORT_FILENAME + FileUtil.SURVEY_REF_EXPORT_SUFFIX);

        // write back to file
        fileServices.marshalToFile(username, surveyContext, exportFilePath, null, true);
    }

    /**
     * Searches the provided real time assessment folder for a survey export file. The survey export
     * file is used to find a specific Survey with a key that matches the provided survey key.
     * 
     * @param realTimeAssessmentFolder the real time assessment folder that contains the survey
     *        export file.
     * @param surveyKey the unique key designated for the survey to be found.
     * @param username information used to authenticate the request.
     * @return the survey found in the export file; null if it isn't found.
     */
    public static Survey findSurvey(AbstractFolderProxy realTimeAssessmentFolder, String surveyKey, String username) {
        // add survey to the export file in the provided real time assessment folder
        SurveyContext surveyContext = getSurveyContext(realTimeAssessmentFolder, username);
        if (surveyContext == null || surveyContext.getContextSurveys() == null) {
            return null;
        }

        Iterator<SurveyContextSurvey> scsItr = surveyContext.getContextSurveys().iterator();
        while (scsItr.hasNext()) {
            SurveyContextSurvey nextScs = scsItr.next();
            if (StringUtils.equalsIgnoreCase(nextScs.getKey(), surveyKey)) {
                return nextScs.getSurvey();
            }
        }

        return null;
    }

    /**
     * Retrieve the survey context from a survey context export file found in the real time
     * assessment folder. There should be only one (at most) survey export.
     * 
     * @param realTimeAssessmentFolder the folder to search for the survey context export file.
     *        Can't be null and must exist.
     * @param username information used to authenticate the request.
     * @return the survey context found in the survey export. Will not be null. If a survey export
     *         was not found a new, empty, survey context will be returned.
     * @throws DetailedException if there was a problem searching for the file or more than one was
     *         found
     */
    private static SurveyContext getSurveyContext(AbstractFolderProxy realTimeAssessmentFolder, String username)
            throws DetailedException {

        SurveyContext surveyContext;

        FileProxy surveyContextFile = getSurveyContextFile(realTimeAssessmentFolder, username);
        if (surveyContextFile == null) {
            if (logger.isInfoEnabled()) {
                logger.info("No survey export file was found in '" + realTimeAssessmentFolder
                        + "', therefore creating new survey context.");
            }

            surveyContext = new SurveyContext();
        } else {
            try {
                surveyContext = getSurveyContextFromExport(surveyContextFile);
            } catch (Exception e) {
                throw new DetailedException("Failed to parse the survey context.",
                        "There was a problem reading in the survey export file of '" + surveyContextFile + "'.", e);
            }
        }

        return surveyContext;
    }

    /**
     * Retrieve the survey context export file for the real time assessment folder provided.
     * 
     * @param realTimeAssessmentFolder the folder to search for the survey context export file.
     *        Can't be null and must exist.
     * @param username information used to authenticate the request.
     * @return the survey context export file found. Null if no export file was found.
     * @throws DetailedException if there was a problem searching for the file or more than one was
     *         found
     */
    private static FileProxy getSurveyContextFile(AbstractFolderProxy realTimeAssessmentFolder, String username)
            throws DetailedException {
        if (realTimeAssessmentFolder == null) {
            throw new IllegalArgumentException("The parameter 'realTimeAssessmentFolder' cannot be null.");
        }

        // find survey export
        List<FileProxy> surveyExportFiles = new ArrayList<>();
        try {
            FileFinderUtil.getFilesByExtension(realTimeAssessmentFolder, surveyExportFiles,
                    FileUtil.SURVEY_REF_EXPORT_SUFFIX);
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
            if (logger.isInfoEnabled()) {
                logger.info("No survey export file was found in '" + realTimeAssessmentFolder
                        + "', therefore creating new survey context.");
            }

            return null;
        } else {
            return surveyExportFiles.get(0);
        }
    }

    /**
     * Retrieve the survey context from the survey export file.
     * 
     * @param surveyExportFile contains the contents of a survey context for a real time assessment.
     *        Can't be null and must exist.
     * @return the survey context from the survey export file. Won't be null.
     * @throws MessageDecodeException if there was a problem parsing the contents of the survey
     *         export
     * @throws IOException if there was a problem with the file
     */
    public static SurveyContext getSurveyContextFromExport(FileProxy surveyExportFile)
            throws MessageDecodeException, IOException {

        // read in JSON entry(ies).
        InputStream is = surveyExportFile.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        SurveyContext surveyContext = null;
        try {
            String line = br.readLine();
            JSONObject obj = (JSONObject) JSONValue.parse(line);
            SurveyContextJSON json = new SurveyContextJSON();
            surveyContext = (SurveyContext) json.decode(obj);
        } finally {
            br.close();
        }

        return surveyContext;
    }
}
