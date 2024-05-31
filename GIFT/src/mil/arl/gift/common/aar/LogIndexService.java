/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.InitializeLessonRequest;
import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest.ACTION_TYPE;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.logger.MessageLogReader;
import mil.arl.gift.common.logger.ProtobufMessageLogReader;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.codec.json.AbstractKnowledgeSessionJSON;

/**
 * A singleton service that is used to manage the index that contains metadata
 * extracted from domain session logs.
 *
 * @author tflowers
 *
 */
public class LogIndexService {

    /** 
     * The time period to allow for a publish lesson score request message following 
     * a lesson completed message to be considered part of the same session. If the time
     * elapsed after the lesson completed message but before the publish lesson score 
     * message is greater than this time period, then the publish lesson score will
     * not be included in in the knowledge session's indexed time span.
     */
    private static final int PUBLISH_LESSON_COMPLETED_TIMEOUT = 30000;

    /** The extension of the log patch file */
    private static final String LOG_PATCH_EXTENSION = ".logPatch";

    /**
     * The JSON Key for the {@link LogSpan#getStart()} of
     * {@link LogMetadata#getLogSpan()}
     */
    private static final String LAST_MSG_INDEX_KEY = "lastMsgIndex";

    /**
     * The JSON Key for the {@link LogSpan#getEnd()} of
     * {@link LogMetadata#getLogSpan()}
     */
    private static final String FIRST_MSG_INDEX_KEY = "firstMsgIndex";

    private static final String FIRST_MSG_TIME_KEY = "firstMsgTime";

    private static final String LAST_MSG_TIME_KEY = "lastMsgTime";

    /** (optional) key for the list of usernames that marked a log as their favorite */
    private static final String USERS_FAVORITE = "usersFavorite";

    /** The JSON Key for {@link LogMetadata#getSession()} */
    private static final String SESSION_KEY = "session";

    /** The JSON Key for {@link LogMetadata#getLogFile()} */
    private static final String FILE_NAME_KEY = "fileName";

    /** The JSON Key for {@link LogMetadata#getDkf()} */
    private static final String DKF_KEY = "dkf";

    /** the cache file name */
    private static final String FILE_NAME = "logIndex.json";

    /** the progress indicator task description for parsing the cache file */
    private static final String PARSING_CACHE_TASK_DESC = "Parsing cache...";

    /** the progress indicator task percent for parsing the cache file */
    private static final int PARSING_CACHE_TASK_PERC = 0;

    /** the progress indicator task description for parsing the log files */
    private static final String PARSING_LOG_FILES_TASK_DESC = "Parsing log files...";

    /** the progress indicator task percent for parsing the log files */
    private static final int PARSING_LOG_FILES_TASK_PERC = 10;

    /** the progress indicator task description for parsing the log patch files */
    private static final String PARSING_LOG_PATCH_FILES_TASK_DESC = "Parsing log patch files...";

    /** the progress indicator task percent for parsing the log patch files */
    private static final int PARSING_LOG_PATCH_FILES_TASK_PERC = 80;

    /** the progress indicator task description for completing the log fetch request */
    private static final String COMPLETED_TASK_DESC = "Completed updating cache";

    /** the progress indicator task percent for completing the log fetch request */
    private static final int COMPLETED_TASK_PERC = 100;

    /** the progress indicator task description for updating the cache file */
    private static final String UPDATING_CACHE_TASK_DESC = "Updating cache...";

    /** the progress indicator task percent for updating the cache file */
    private static final int UPDATING_CACHE_TASK_PERC = 95;

    /** the progress indicator task description for finding metadata to update for a specific log file */
    private static final String FINDING_METADATA_LOG_INDEX_DESC = "Finding metadata to update";

    /** the progress indicator task description for updating/writing the log index file */
    private static final String UPDATING_METADATA_LOG_INDEX_DESC = "Updating log index";

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(LogIndexService.class);


    /** The singleton instance of this class */
    private static LogIndexService instance = new LogIndexService();

    /**
     * The file that is used to cache the metadata that is extracted for each
     * log file
     */
    private final File LOG_INDEX;

    /**
     * The folder that contains the index file as well as the individual log
     * files that are being indexed.
     */
    private final File LOG_OUTPUT_FOLDER;

    /**
     * The codec that converts a {@link JSONObject} to a
     * {@link AbstractKnowledgeSession}.
     */
    private final AbstractKnowledgeSessionJSON SESSION_CODEC = new AbstractKnowledgeSessionJSON();

    /**
     * Getter for the singleton index of the {@link LogIndexService}.
     *
     * @return The value of {@link #instance}.
     */
    public static LogIndexService getInstance() {
        return instance;
    }

    /**
     * A private constructor that enforces the singleton pattern for this class
     */
    private LogIndexService() {
        if (logger.isTraceEnabled()) {
            logger.trace("LogIndexService()");
        }
  
        LOG_OUTPUT_FOLDER = new File(PackageUtil.getDomainSessions());
        LOG_INDEX = Paths.get(PackageUtil.getDomainSessions(), FILE_NAME).toFile();
    }

    /**
     * Gets the {@link Collection} of {@link AbstractKnowledgeSession} objects
     * that are available for AAR playback.
     *
     * @param username the user requesting the list, used for sorting purposes.  Can be null or empty in which
     * case it won't be used for sorting.
     * @param progressIndicator used to update the caller on progress made fetching the sessions.
     *         Can be null.
     * @return The {@link Collection} of {@link AbstractKnowledgeSession}
     *         objects that are available for playback. Can't be null. Can be
     *         empty.
     * @throws DetailedException if there was a problem getting the previously
     *         indexed files from the disk or writing the new index file to the
     *         disk.
     */
    public synchronized Collection<LogMetadata> getAllSessionsSorted(final String username, ProgressIndicator progressIndicator) throws DetailedException {

        //flatten the session log file mapping to a list of all the sessions found in all the log files
        List<LogMetadata> metadatas = new ArrayList<>();
        getAllSessions(progressIndicator).values().forEach(metadatas::addAll);

        // Sort by:
        // 1. favorites for user
        // 2. start date (currently already implicit based on how the log index is written)
        metadatas.sort(new Comparator<LogMetadata>() {

            @Override
            public int compare(LogMetadata logMetadata1, LogMetadata logMetadata2) {

                int result = 0;
                if(StringUtils.isNotBlank(username)){

                    boolean logMetadata1ContainsUser = logMetadata1.getUsersFavorite().contains(username);
                    boolean logMetadata2ContainsUser = logMetadata2.getUsersFavorite().contains(username);

                    if(logMetadata1ContainsUser && !logMetadata2ContainsUser){
                        result = -1;
                    }else if(!logMetadata1ContainsUser && logMetadata2ContainsUser){
                        result = 1;
                    }
                }

                if(result == 0){
                    // sort by the default priorities
                    result = AbstractKnowledgeSession.defaultSessionComparator.compare(logMetadata1.getSession(), logMetadata2.getSession());
                }
                return result;
            }
        });

        return metadatas;
    }

    /**
     * Searches for a past session matching the provided information and returns the closest matching log metadata containing
     * that session
     * 
     * @param sessionName the name of the session. Can be null.
     * @param startTime the start time of the session
     * @param domainSessionId the domain session ID of the session's host
     * @param userId the user ID of the session's host
     * @param experimentId the experiment ID of the session's host
     * @return the log metadata containing the closest matching past session. Will not be null.
     */
    public synchronized LogMetadata getSession(String sessionName, long startTime, int domainSessionId, int userId, String experimentId) {
        
        try {
            
            // Construct a dummy domain session to build the name of the log file
            DomainSession domainSession = new DomainSession(domainSessionId, userId, DomainSession.UNKNOWN_DOMAIN_NAME, DomainSession.UNKNOWN_DOMAIN_NAME);
            domainSession.setExperimentId(experimentId);
            
            String dsFileName = domainSession.buildLogFileName();
            
            // Find the log file matching the domain session data
            File dsLog = null;
            File dsFolder = new File(LOG_OUTPUT_FOLDER, dsFileName);
            for (File file : dsFolder.listFiles()) {
                if (ProtobufMessageLogReader.isProtobufLogFile(file.getName()) || file.getName().endsWith(".log")) {
                    //found the log corresponding to the domain session information
                    dsLog = file;
                    break;
                }
            }
            
            /* Extract any sessions found in the domain session log and attempt to return
             * the one that most closely matches the provided session data. This is needed
             * because an active session will not exactly match its past session once it
             * is written to the log file. */
            List<LogMetadata> metadatas = extractMetadata(new FileProxy(dsLog));
            
            LogMetadata closestMatch = null;
            for(LogMetadata metadata : metadatas) {
                
                if(closestMatch == null) {
                    closestMatch = metadata;
                    
                } else if (sessionName.equals(metadata.getSession().getNameOfSession())){
                    
                    long closesStartDelta = Math.abs(startTime - closestMatch.getStartTime());
                    long metadataStartDelta = Math.abs(startTime - metadata.getStartTime());
                    
                    if(metadataStartDelta < closesStartDelta) {
                        
                        /* The session represented by this metadata is closer to the start time
                         * we are looking for, so this is the closest match now */
                        closestMatch = metadata;
                    }
                }
            }
            
            return closestMatch;
            
        } catch (Exception e) {
            String msg ="There was a problem reading or the content of the log for domain session " + domainSessionId;
            logger.error(msg, e);
            throw new DetailedException("Domain Session Log IO Error", msg, e);
        }
    }

    /**
     * Gets the {@link Collection} of {@link AbstractKnowledgeSession} objects
     * that are available for AAR playback.
     *
     * @param progressIndicator used to update the caller on progress made fetching the sessions.
     *         Can be null.
     * @return a {@link Map} of {@link AbstractKnowledgeSession}
     *         objects that are available for playback, grouped by the
     *         names of the log files they were obtained from. Can't
     *         be null. Can be empty.
     * @throws DetailedException if there was a problem getting the previously
     *         indexed files from the disk or writing the new index file to the
     *         disk.
     */
    private synchronized Map<String, Set<LogMetadata>> getAllSessions(ProgressIndicator progressIndicator)
            throws DetailedException {
        if (logger.isTraceEnabled()) {
            logger.trace("getSessions()");
        }

        try {
            /* Get the current state of the index file */
            if(progressIndicator != null){
                progressIndicator.setTaskDescription(PARSING_CACHE_TASK_DESC);
                progressIndicator.setPercentComplete(PARSING_CACHE_TASK_PERC);
            }
            Map<String, Set<LogMetadata>> indexedFiles = parseLogIndex();

            /* Determine if any of the log files are newer than the current log
             * index or do not have mappings in the log index*/
            List<File> modifiedFiles = new ArrayList<>();
            Set<String> fileIndexesToRemove = new HashSet<>(indexedFiles.keySet());
            File[] listOfFiles = LOG_OUTPUT_FOLDER.listFiles();

            for(File file : listOfFiles) {
                
                // ignore files at the root of the log output folder because each session log is in its own subfolder
                if(!file.isDirectory()){
                    continue;
                }
                for (File file1: file.listFiles()) {
                    if (file1.getName().startsWith("bookmark")) {
                        continue;
                    }

                    /* Check if the file is a protobuf binary log or a legacy
                     * JSON log */
                    if (ProtobufMessageLogReader.isProtobufLogFile(file1.getName())
                            || file1.getName().endsWith(".log")) {
                        String fileName = file.getName() + File.separator + file1.getName();
                        if(file1.lastModified() > LOG_INDEX.lastModified() || !indexedFiles.containsKey(fileName)) {

                            //need to extract metadata since this file is either newer than the log index or not mapped by it
                            modifiedFiles.add(file1);
                        }

                        if(fileIndexesToRemove.contains(fileName)) {

                            //if a file mapped in the log index still exists, remove it from the collection that will be removed later on
                            fileIndexesToRemove.remove(fileName);
                        }
                    }
                }
            }

            //remove mappings in the log index for files that no longer exist
            indexedFiles.keySet().removeAll(fileIndexesToRemove);

            /* Extract the metadata from any new/modified files */
            if (!modifiedFiles.isEmpty()) {
                parseModifiedFiles(progressIndicator, modifiedFiles, indexedFiles);
            }

            /* Update all sessions with a patch file check. This should always
             * be called because the log file cache doesn't keep track of patch
             * files. Make sure this is called after any new/modified files have
             * been parsed to metadata. */
            if (progressIndicator != null) {
                progressIndicator.setTaskDescription(PARSING_LOG_PATCH_FILES_TASK_DESC);
                progressIndicator.setPercentComplete(PARSING_LOG_PATCH_FILES_TASK_PERC);
            }
            for (Entry<String, Set<LogMetadata>> entry : indexedFiles.entrySet()) {
                final String fileName = entry.getKey();
                final File patchFileName = new File(LOG_OUTPUT_FOLDER, fileName + LOG_PATCH_EXTENSION);

                if (patchFileName.exists()) {
                    for (LogMetadata logMeta : entry.getValue()) {
                        logMeta.setLogPatchFile(patchFileName.getName());
                    }
                    
                } else if(!ProtobufMessageLogReader.isProtobufLogFile(fileName)){
                    
                    /* Check if an existing log file for a legacy JSON log was recently updated to protobuf */
                    File updatedPatchFileName = new File(LOG_OUTPUT_FOLDER, fileName + ProtobufMessageLogReader.PROTOBUF_LOG_FILE_EXTENSION + LOG_PATCH_EXTENSION);
                    if(updatedPatchFileName.exists()) {
                    
                        for (LogMetadata logMeta : entry.getValue()) {
                            logMeta.setLogPatchFile(updatedPatchFileName.getName());
                        }
                    }
                }
            }

            /* Write the updated version of the index to disk IFF a new/modified
             * file exists */
            if (!modifiedFiles.isEmpty()) {
                if (progressIndicator != null) {
                    progressIndicator.setTaskDescription(UPDATING_CACHE_TASK_DESC);
                    progressIndicator.setPercentComplete(UPDATING_CACHE_TASK_PERC);
                }
                writeLogIndex(indexedFiles);
            }

            if(progressIndicator != null){
                progressIndicator.setTaskDescription(COMPLETED_TASK_DESC);
                progressIndicator.setPercentComplete(COMPLETED_TASK_PERC);
            }
            if(logger.isDebugEnabled()){
                logger.debug("Found "+indexedFiles.size()+" domain session log files.");
            }
                return indexedFiles;
        } catch (IOException ioEx) {
            String msg = String.format("There was a problem reading or writing the content of the log index file '%s'",
                    LOG_INDEX.getAbsoluteFile());
            logger.error(msg, ioEx);
            throw new DetailedException("Log Index IO Error", msg, ioEx);
        }
    }

    /**
     * Parse the modified files and populate the indexed files with the result.
     * 
     * @param progressIndicator used to update the caller on progress made
     *        fetching the sessions. Can be null.
     * @param modifiedFiles the list of modified files that need to be parsed.
     *        If null or empty, nothing will be parsed.
     * @param indexedFiles the collection of log metadata objects for each file.
     *        This will be populated with the result from parsing the modified
     *        files. Can't be null.
     */
    private void parseModifiedFiles(ProgressIndicator progressIndicator, List<File> modifiedFiles,
            Map<String, Set<LogMetadata>> indexedFiles) {
        if (CollectionUtils.isEmpty(modifiedFiles)) {
            return;
        } else if (indexedFiles == null) {
            throw new IllegalArgumentException("The parameter 'indexedFiles' cannot be null.");
            }

            if(progressIndicator != null){
                progressIndicator.setTaskDescription(PARSING_LOG_FILES_TASK_DESC);
                progressIndicator.setPercentComplete(PARSING_LOG_FILES_TASK_PERC);
            }

            /* Extract the metadata from the new files */
            int delta = UPDATING_CACHE_TASK_PERC - PARSING_LOG_FILES_TASK_PERC;
            StringBuilder failedSb = null;
            int index = 1;
            for (File file : modifiedFiles) {
                try{
                    List<LogMetadata> sessionsToAdd = extractMetadata(new FileProxy(file));

                    final Set<LogMetadata> mappedSessions = indexedFiles.computeIfAbsent(file.getParentFile().getName() + File.separator + file.getName(),
                            key -> new HashSet<>());
                    for(LogMetadata session : sessionsToAdd) {

                        if(mappedSessions.contains(session)) {
                            Iterator<LogMetadata> existingSessionItr = mappedSessions.iterator();
                            while(existingSessionItr.hasNext()) {

                                LogMetadata mappedSession = existingSessionItr.next();

                                if(mappedSession.equals(session)) {

                                    /*
                                     * Another session with the same hash was already found in the log index file.
                                     * When this happens, we need to replace the existing session but preserve
                                     * any users that have marked it as a favorite.
                                     */
                                    session.getUsersFavorite().addAll(mappedSession.getUsersFavorite());

                                    existingSessionItr.remove();
                                }
                            }
                        }

                        mappedSessions.add(session);
                    }

                } catch (Throwable t) {
                    if (failedSb == null) {
                        failedSb = new StringBuilder("Message folder files that failed to parse:\n");
                    }

                    failedSb.append(file.getName()).append(" : ").append(t).append("\n");
                }

                if(progressIndicator != null){
                progressIndicator.setPercentComplete((int)(PARSING_LOG_FILES_TASK_PERC + index * (delta/(double)modifiedFiles.size())));
                }
                
                index++;
            }

            if(failedSb != null){
                logger.warn(failedSb.toString());
            }
    }

    /**
     * Parses the existing index file if it exists.
     *
     * @return The {@link Map} of previously extracted {@link AbstractKnowledgeSession}
     *         metadata, grouped by the names of the log files it was obtained from.
     *         Can't be null.
     * @throws DetailedException if there was an error parsing the JSON as the
     *         log index format.
     * @throws IOException if there was an error reading the content of file.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Set<LogMetadata>> parseLogIndex() throws DetailedException, IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("parseLogIndex()");
        }

        /* If the file does not already exist, return an empty list. */
        if (!LOG_INDEX.exists()) {
            return new HashMap<>();
        }

        try (BufferedReader fileReader = new BufferedReader(new FileReader(LOG_INDEX))) {
            final JSONParser jsonParser = new JSONParser();
            Object parsedObject = jsonParser.parse(fileReader);
            Map<String, Set<LogMetadata>> extractedMetadata = new HashMap<>();
            JSONObject logFileMap = null;

            if(parsedObject instanceof JSONArray) {

                //if an array is found, this is an old log index file, so convert it to the newer map structure
                logFileMap = new JSONObject();

                JSONArray logIndexArray = (JSONArray) parsedObject;
                for (Object object : logIndexArray) {
                    if (object instanceof JSONObject) {
                        JSONObject jsonMetadata = (JSONObject) object;

                        /* Determine the path of the log file */
                        String logFile = (String) jsonMetadata.get(FILE_NAME_KEY);
                        if(logFile != null) {

                            Object logFileMetas = logFileMap.get(logFile);
                            if(!(logFileMetas instanceof JSONArray)) {

                                //if no mapping exists for this log file yet, add one
                                logFileMetas = new JSONArray();
                                logFileMap.put(logFile, logFileMetas);
                            }

                            //add this metadata to the mapping's list of associated metadatas
                            ((JSONArray) logFileMetas).add(jsonMetadata);
                        }

                    } else {
                        throw new DetailedException("Log Index Parse Error",
                                "Expected a JSON Object but instead found a " + object.getClass().getName(), null);
                    }
                }

            } else if(parsedObject instanceof JSONObject) {
                logFileMap = (JSONObject) parsedObject;
            }

            if (logFileMap != null) {

                //iterate through each log file mapping in the log index file to gather its metadata
                for(Object key : logFileMap.keySet()) {

                    if(!(key instanceof String)) {
                        continue;
                    }

                    //obtain the log file's name from its key
                    String logFile = (String) key;

                    Object value = logFileMap.get(key);
                    if(!(value instanceof JSONArray)) {
                        continue;
                    }

                    Set<LogMetadata> associatedMetadatas = extractedMetadata.get(logFile);
                    if (associatedMetadatas == null) {
                        // if no mapping exists for this log file, create one
                        associatedMetadatas = new HashSet<>();
                        extractedMetadata.put(logFile, associatedMetadatas);
                    }

                    try {
                        // iterate through all of the metadatas associated with this
                        // log file name
                        for(Object metadataEntry : (JSONArray) value) {
    
                            if(!(metadataEntry instanceof JSONObject)) {
                                continue;
                            }
    
                            JSONObject jsonMetadata = (JSONObject) metadataEntry;
    
                            /* Get the session details from the log */
                            JSONObject jsonSession = (JSONObject) jsonMetadata.get(SESSION_KEY);
                            AbstractKnowledgeSession session = (AbstractKnowledgeSession) SESSION_CODEC.decode(jsonSession);
    
                            /* Get the index values of the start and end messages */
                            Long firstMsgIndex = (Long) jsonMetadata.get(FIRST_MSG_INDEX_KEY);
                            Long lastMsgIndex = (Long) jsonMetadata.get(LAST_MSG_INDEX_KEY);
    
                            /* Get the time values of the start and end messages */
                            Long firstMsgTime = (Long) jsonMetadata.get(FIRST_MSG_TIME_KEY);
                            Long lastMsgTime = (Long) jsonMetadata.get(LAST_MSG_TIME_KEY);
    
                            if (firstMsgIndex == null || lastMsgIndex == null) {
                                final String msg = "The index entry " + jsonMetadata.toJSONString()
                                        + " does not contain both a start and end index for its log span";
                                logger.error(msg);
                                throw new DetailedException("Log Index Parse Exception", msg, null);
                            }
    
                            session.setInPastSessionMode(true);
                            session.setSessionEndTime(lastMsgTime);
                            session.setDomainSessionLogFileName(logFile);
    
                            final LogSpan logSpan = new LogSpan(firstMsgIndex.intValue(), lastMsgIndex.intValue());
                            final LogMetadata logMetadata = new LogMetadata(session, logFile, logSpan, firstMsgTime,
                                    lastMsgTime);
    
                            if(jsonMetadata.containsKey(USERS_FAVORITE)){
                                JSONArray usersArray = (JSONArray) jsonMetadata.get(USERS_FAVORITE);
                                for(Object username : usersArray){
                                    logMetadata.addUserToFavorites((String) username);
                                }
                            }
    
                            final Path logFilePath = Paths.get(logFile);
                            logMetadata.getVideoFiles().addAll(
                                    findVideoMetaFiles(new File(LOG_OUTPUT_FOLDER, logFilePath.getParent().toString())));
    
                            if(jsonMetadata.containsKey(DKF_KEY)){
                                String dkf = (String) jsonMetadata.get(DKF_KEY);
                                logMetadata.setDkf(dkf);
                            }
                            
                            // #5089 - updating observer controls audio path for Game Master playback
                            LogFilePlaybackService.prepareSessionOutputAudioFileName(session.getObserverControls());
    
                            //add this metadata to the log file's mapping
                            associatedMetadatas.add(logMetadata);
                        }
                    }catch(Throwable t) {
                        throw new DetailedException("Failed to parse the metadata for the log file entry mapped to '"+logFile+"'.", 
                                "There was a server side error when creating the objects with the data found for that entry in '"+LOG_INDEX.getAbsolutePath()+"'.", t);
                    }
                }

                return extractedMetadata;

            } else {
                throw new DetailedException("Log Index Parse Error",
                        "Expected the root object to be a JSON Array but instead it was a "
                                + parsedObject.getClass().getName(),
                        null);
            }
        } catch (ParseException parseEx) {
            String msg = String.format("There was a problem parsing the JSON within the log index file '%s'",
                    LOG_INDEX.getAbsoluteFile());
            throw new DetailedException("Log Index Parse Error", msg, parseEx);
        }
    }

    /**
     * Finds the video files in the provided course folder.
     * 
     * @param sessionFolder the session instance folder to search. Can't be null.
     * @return the list of video files found in the course folder. Will never be null.
     */
    private static Set<VideoMetadata> findVideoMetaFiles(File sessionFolder) {
        final Set<VideoMetadata> toRet = new HashSet<>();

        try {
            File[] files = sessionFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(AbstractSchemaHandler.VIDEO_FILE_EXTENSION);
                }
            });

            if(files != null){
            for (File file : files) {
                VideoMetadataFileHandler fileHandler = new VideoMetadataFileHandler(new FileProxy(file), sessionFolder,
                        true);
                toRet.add(fileHandler.getVideoMetadata());
            }
            }
        } catch (IOException e) {
            logger.error("Failed to get files with extension '" + AbstractSchemaHandler.VIDEO_FILE_EXTENSION + "' in '"+sessionFolder+"'.", e);
        }

        return toRet;
    }

    /**
     * Update the entry in the log index file for the log metadata specified.
     *
     * @param updatedLogMetadata the log metadata to update in the log index file.  Can't be null.
     * @param progressIndicator optional progress indicator used updating progress on updating the log index file.  Can be null.
     * @throws IOException if there is a problem updating the log index file
     */
    public void updateLogMetadata(LogMetadata updatedLogMetadata, ProgressIndicator progressIndicator) throws IOException{
        
        /* remove the playback ID associated with the session in the log metadata being updated, since playback IDs are 
         * tied to live browser sessions and shouldn't be written to the log index */
        updatedLogMetadata.getSession().setPlaybackId(null);

        // get current info from file
        Map<String, Set<LogMetadata>> logMetadatas = getAllSessions(progressIndicator);

        if(progressIndicator != null){
            progressIndicator.setTaskDescription(FINDING_METADATA_LOG_INDEX_DESC);
        }

        // update specific logMetadata
        Set<LogMetadata> logFileMetadatas = logMetadatas.get(updatedLogMetadata.getLogFile());
        if(logFileMetadatas != null) {

            if(logFileMetadatas.contains(updatedLogMetadata)) {

                //log metadatas are hashed by log file name and start time, so replace the existing metadata with the same hash
                logFileMetadatas.remove(updatedLogMetadata);
                logFileMetadatas.add(updatedLogMetadata);

                if(progressIndicator != null){
                    progressIndicator.setTaskDescription(UPDATING_METADATA_LOG_INDEX_DESC);
                }
                writeLogIndex(logMetadatas);
            }
        }
    }

    /**
     * Writes the {@link Map} of {@link AbstractKnowledgeSession} to the
     * {@link #LOG_INDEX}. Be aware, the provided {@link Map} will overwrite
     * the the content of the {@link #LOG_INDEX} file. It will not append to the
     * file.
     *
     * @param metadatas The {@link Map} of {@link AbstractKnowledgeSession} to
     *        write to the file, grouped by the names of the log files they
     *        were obtained from. Can't be null. Can be empty.
     * @throws IOException if there was a problem writing the updated content to
     *         the index file.
     */
    @SuppressWarnings("unchecked")
    private void writeLogIndex(Map<String, Set<LogMetadata>> metadatas) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("writeLogIndex(" + metadatas + ")");
        }

        if (metadatas == null) {
            throw new IllegalArgumentException("The parameter 'sessions' cannot be null.");
        }

        //build a mapping from each log file name to its associated metadata
        JSONObject logFileMap = new JSONObject();

        for(Map.Entry<String, Set<LogMetadata>> entry : metadatas.entrySet()) {

            JSONArray jsMetadatas = new JSONArray();

            for(LogMetadata metadata : entry.getValue()) {

                /* populate the metadata for each log file name*/
                JSONObject jsMetadata = new JSONObject();

                /* Encode the session */
                JSONObject jsonSession = new JSONObject();
                SESSION_CODEC.encode(jsonSession, metadata.getSession());
                jsMetadata.put(SESSION_KEY, jsonSession);

                /* Encode the file name */
                jsMetadata.put(FILE_NAME_KEY, metadata.getLogFile());

                /* Encode the indices */
                jsMetadata.put(FIRST_MSG_INDEX_KEY, metadata.getLogSpan().getStart());
                jsMetadata.put(LAST_MSG_INDEX_KEY, metadata.getLogSpan().getEnd());

                /* Encode the start/end times */
                jsMetadata.put(FIRST_MSG_TIME_KEY, metadata.getStartTime());
                jsMetadata.put(LAST_MSG_TIME_KEY, metadata.getEndTime());

                /* Encode the DKF name */
                jsMetadata.put(DKF_KEY, metadata.getDkf());

                if(!metadata.getUsersFavorite().isEmpty()){
                    JSONArray usernameArray = new JSONArray();
                    usernameArray.addAll(metadata.getUsersFavorite());
                    jsMetadata.put(USERS_FAVORITE, usernameArray);
                }

                jsMetadatas.add(jsMetadata);
            }

            logFileMap.put(entry.getKey(), jsMetadatas);
        }

        try {
            try (Writer fileWriter = new BufferedWriter(new FileWriter(LOG_INDEX))) {
                JSONObject.writeJSONString(logFileMap, fileWriter);
            }
        } catch (IOException ioEx) {
            String msg = String.format("There was a problem writing the updated index to the log index file '%s'",
                    LOG_INDEX.getAbsolutePath());
            logger.error(msg, ioEx);
            throw ioEx;
        }
    }

    /**
     * Extracts the {@link AbstractKnowledgeSession} metadata from a given
     * {@link File}.
     *
     * @param logFile The file from which to extract the metadata. Can't be
     *        null.
     * @return The extracted {@link AbstractKnowledgeSession} metadata. Can't be
     *         null.
     */
    @SuppressWarnings("fallthrough")
    public static List<LogMetadata> extractMetadata(final FileProxy logFile) {
        if (logger.isTraceEnabled()) {
            logger.trace("extractMetadata(" + logFile + ")");
        }

        if (logFile == null) {
            throw new IllegalArgumentException("The parameter 'file' cannot be null.");
        }

        List<LogMetadata> toRet = new ArrayList<>();

        final MessageLogReader reader = MessageLogReader.createMessageLogReader(logFile.getName());
        try (final Stream<Message> messageStream = reader.streamMessages(logFile)) {
            Iterator<Message> msgIter = messageStream.iterator();

            int msgIndex = 0;
            AbstractKnowledgeSession session = null;
            int firstIndex = -1;
            Message firstMsg = null;
            Integer createTeamSequenceNumber = null;
            String dkf = null;
            long firstEntityStateTime = -1;
            long lastLessonCompletedTime = -1;

            while (msgIter.hasNext()) {
                /* If this log is not a domain session log (a system log),
                 * return an empty list since it won't contain any executed DKF
                 * scenarios. */
                if (!reader.isDomainSessionLog()) {
                    return toRet;
                }

                /* Unpack elements of the message */
                final Message msg = msgIter.next();
                final MessageTypeEnum msgType = msg.getMessageType();
                final Object payload = msg.getPayload();

                if (msgType == MessageTypeEnum.LESSON_STARTED && firstIndex == -1) {
                    // found lesson started and never found manage membership, meaning this is a single playable
                    // knowledge session
                    if (firstIndex == -1) {
                        firstIndex = msgIndex;
                        firstMsg = msg;
                        firstEntityStateTime = -1;
                        lastLessonCompletedTime = -1;
                    }

                } else if (msgType == MessageTypeEnum.MANAGE_MEMBERSHIP_TEAM_KNOWLEDGE_SESSION) {
                    ManageTeamMembershipRequest manageRequest = (ManageTeamMembershipRequest) payload;
                    final ACTION_TYPE actionType = manageRequest.getActionType();
                    switch (actionType) {
                    case CREATE_TEAM_SESSION: /* Intentional fall-through */
                        
                        // entering the team lobby 
                        // (which could be done multiple times before starting a team session, looking for last time)
                        firstIndex = msgIndex;
                        firstMsg = msg;
                        firstEntityStateTime = -1;
                        lastLessonCompletedTime = -1;
                                              
                    case ASSIGN_TEAM_MEMBER: /* Intentional fall-through */
                    case UNASSIGN_TEAM_MEMBER:
                        createTeamSequenceNumber = msg.getSequenceNumber();
                        break;
                    case DESTROY_TEAM_SESSION: /* Intentional fall-through */
                    case JOIN_TEAM_SESSION: /* Intentional fall-through */
                    case LEAVE_TEAM_SESSION:
                        createTeamSequenceNumber = null;
                        session = null;
                        break;
                    case CHANGE_TEAM_SESSION_NAME: /* Intentional
                                                    * fall-through */
                    default:
                        /* Do nothing */
                    }
                } else if (msgType == MessageTypeEnum.ACTIVE_KNOWLEDGE_SESSIONS_REPLY
                        || msgType == MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST
                        || msgType == MessageTypeEnum.KNOWLEDGE_SESSION_CREATED) {
                    int dsId;
                    if (msg instanceof DomainSessionMessageInterface) {
                        DomainSessionMessageInterface domainLogMsg = (DomainSessionMessageInterface) msg;
                        dsId = domainLogMsg.getDomainSessionId();
                    } else {
                        String message = String.format("Unable to process the message %s at line %d of file %s", msg,
                                msgIndex, logFile.getFileId());
                        throw new RuntimeException(message);
                    }

                    final boolean isUpdateRequest = msgType == MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST;
                    if (isUpdateRequest
                            || Objects.deepEquals(msg.getReplyToSequenceNumber(), createTeamSequenceNumber)) {
                        KnowledgeSessionsReply knowledgeReply = (KnowledgeSessionsReply) payload;
                        createTeamSequenceNumber = null;
                        final Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap = knowledgeReply
                                .getKnowledgeSessionMap();
                        if (knowledgeSessionMap.containsKey(dsId)) {
                            session = knowledgeSessionMap.get(dsId);
                        }

                    } else if (msgType == MessageTypeEnum.KNOWLEDGE_SESSION_CREATED) {

                        KnowledgeSessionCreated knowledgeCreated = (KnowledgeSessionCreated) payload;
                        createTeamSequenceNumber = null;
                        session = knowledgeCreated.getKnowledgeSession();
                    }

                } else if (firstEntityStateTime == -1 && msgType == MessageTypeEnum.ENTITY_STATE) {
                    firstEntityStateTime = msg.getTimeStamp();
                    
                } else if (msgType.equals(MessageTypeEnum.INITIALIZE_LESSON_REQUEST)) {
                    
                    if (msg instanceof DomainSessionMessageInterface) {
                        InitializeLessonRequest request = (InitializeLessonRequest) msg.getPayload();
                        
                        if (request != null && request.getContentReference() != null) {
                            
                            /* Attempt to get the DKF file name from this request */
                            String fileName = Paths.get(request.getContentReference()).getFileName().toString();
                            dkf = fileName;
                        }
                    }
                    
                } else if (msgType == MessageTypeEnum.LESSON_COMPLETED
                        || msgType == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST) {
                    if (firstIndex != -1 && session != null && firstMsg != null) {
                        final long startTime;
                        if (firstEntityStateTime != -1) {
                            startTime = firstEntityStateTime;
                        } else {
                            startTime = firstMsg.getTimeStamp();
                        }

                        final long endTime = msg.getTimeStamp();
                        final LogSpan logSpan = new LogSpan(firstIndex, msgIndex + 1);
                        final Path parentPath = Paths.get(logFile.getFileId()).getParent();
                        String parentFileName = parentPath.getFileName().toString();

                        session.setInPastSessionMode(true);
                        session.setSessionEndTime(endTime);
                        session.setDomainSessionLogFileName(parentFileName + File.separator + logFile.getName());

                        LogMetadata metadata = new LogMetadata(session, parentFileName + File.separator + logFile.getName(), logSpan, startTime, endTime);
                        metadata.getVideoFiles().addAll(findVideoMetaFiles(parentPath.toFile()));
                        metadata.setDkf(dkf);
                        
                        // #5089 - updating observer controls audio path for Game Master playback
                        LogFilePlaybackService.prepareSessionOutputAudioFileName(session.getObserverControls());                        
                        
                        toRet.add(metadata);
                        
                        lastLessonCompletedTime = msg.getTimeStamp();
                    }

                    /* Reset all the aggregating variables. */
                    session = null;
                    firstEntityStateTime = -1;
                    firstIndex = -1;
                    firstMsg = null;
                    createTeamSequenceNumber = null;
                    dkf = null;
                    
                } else if (msgType == MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST 
                        && lastLessonCompletedTime != -1
                        && (msg.getTimeStamp() - lastLessonCompletedTime < PUBLISH_LESSON_COMPLETED_TIMEOUT)) {
                    
                    /* 
                     * If a publish lesson score message is received within a certain timeout after the last 
                     * lesson was completed, then we need to include the published lesson score in the log 
                     * metadata's time span so that the score associated with that lesson can be 
                     * modified in Game Master
                     */
                        
                    /* Update the end time of the latest log metadata to include the publish lesson score request.*/
                    final long endTime = msg.getTimeStamp();
                    final int endIndex = msgIndex;
                    
                    LogMetadata lastMetadata = toRet.get(toRet.size() - 1);
                    lastMetadata.getLogSpan().setEnd(endIndex + 1);
                    lastMetadata.setEndTime(endTime);
                    lastMetadata.getSession().setSessionEndTime(endTime); 
                }

                msgIndex++;
            }
            return toRet;
        } catch (Exception ex) {
            logger.error("There was a problem parsing the log " + logFile.getFileId(), ex);
            return toRet;
        }
    }
}
