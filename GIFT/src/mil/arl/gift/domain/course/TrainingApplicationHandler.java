/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.course;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.DISInteropInputs;
import generated.course.HAVENInteropInputs;
import generated.course.LogFile;
import generated.course.LogFile.DomainSessionLog;
import generated.course.Interop;
import generated.course.MobileApp;
import generated.course.RIDEInteropInputs;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.aar.LogFilePlaybackService;
import mil.arl.gift.common.aar.LogFilePlaybackTarget;
import mil.arl.gift.common.aar.LogSpan;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainModule;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.Message;

/**
 * This class contains logic to retrieve the necessary information for a training application course transition.
 *
 * @author mhoffman
 *
 */
public class TrainingApplicationHandler implements TransitionHandler {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(TrainingApplicationHandler.class);

    /** course transition information */
    private generated.course.TrainingApplication trainingAppTransition;
    
    /** whether this training application course object references a playback session log and that log
     * contains a team knowledge session
     */
    private boolean teamSessionPlayback = false;

    /**
     * Class constructor - set attribute
     *
     * @param trainingApplication course information for a training application transition
     */
    public TrainingApplicationHandler(generated.course.TrainingApplication trainingApplication){
        this.trainingAppTransition = trainingApplication;
    }

    /**
     * Return the DKF name referenced by the transition
     *
     * @return String
     */
    public String getDKFName(){
        return trainingAppTransition.getDkfRef().getFile();
    }

    /**
     * Return whether this training application course object references a playback session log and that log
     * contains a team knowledge session
     * @return true if the training app course object is a team knowledge session playback.  Default is false.
     */
    public boolean isTeamSessionPlayback() {
        return teamSessionPlayback;
    }

    /**
     * Set whether this training application course object references a playback session log and that log
     * contains a team knowledge session
     * @param teamSessionPlayback true if the training app course object is a team knowledge session playback.
     */
    public void setTeamSessionPlayback(boolean teamSessionPlayback) {
        this.teamSessionPlayback = teamSessionPlayback;
    }

    /**
     * Populate the interop information objects provided with the appropriate contents from this transition.
     *
     * @return Map<String, generated.course.InteropInputs> - mapping of interop implementation (e.g. gateway.interop.ppt.PPTInterface)
     * to inputs for that plugin (e.g. generated.course.InteropInputs that can contain generated.course.PowerPointInteropInputs) that are used by this training application course transition.
     * Will not be null.  Currently will not be empty.
     * @throws ConfigurationException if there was a problem with the interop configuration parameters known to this class.
     */
    public Map<String, Serializable> processInterops(generated.course.Interops courseInterops) throws ConfigurationException{

        Map<String, Serializable> interops = new HashMap<>();

        for (Interop interop : courseInterops.getInterop()) {

            //get the interop interface implementation class
            String implClass = interop.getInteropImpl();

            //get the inputs to the interop class
            generated.course.InteropInputs interopInputs = interop.getInteropInputs();

            if(interops.containsKey(implClass)){
                throw new ConfigurationException("Failed to process the interop implementation classes.",
                        "Found more than one interop implementation class named "+implClass+" which is not currently supported.",
                        null);
            }

            interops.put(implClass, interopInputs);

        }//end for

        return interops;
    }

    /**
     * Populate the embedded application information objects provided with the appropriate contents from this transition.
     *
     * @param courseEmbeddedApps the embedded application information objects to populate
     * @param courseRuntimeFolder the folder used to host the course at runtime. Used to ensure that the URLs given for
     * any embedded applications are URI compliant.
     * @return Map<String, generated.course.EmbeddedApps> - mapping of embedded applications' URLs to the inputs for the
     * applications at those URLs (e.g. generated.course.EmbeddedApps that can contain generated.course.EmbeddedApp) that
     * are used by this training application course transition. Will not be null.  Currently will not be empty.
     */
    public Map<String, Serializable> processEmbeddedApps(generated.course.EmbeddedApps courseEmbeddedApps, String courseRuntimeFolder) {

    	Map<String, Serializable> embeddedApps = new HashMap<>();

    	generated.course.EmbeddedApp app = courseEmbeddedApps.getEmbeddedApp();

    	if(app != null){

			//Get the embedded app's implementation
			Serializable impl = app.getEmbeddedAppImpl();

			String url;

			if(impl instanceof MobileApp) {
                url = impl.getClass().getName();

            } else {
                final String implPath = (String) impl;

                try {
                    /* first, see if it's a local path relative to the Training.Apps maps directory */
                    File file = new File(CommonProperties.getInstance().getTrainingAppsDirectory() + File.separator
                            + PackageUtil.getWrapResourcesDir() + File.separator
                            + PackageUtil.getTrainingAppsMaps() + File.separator + implPath);
                    if (file.exists()) {
                        url = DomainModuleProperties.getInstance().getDomainContentServerAddress() + Constants.FORWARD_SLASH
                                + PackageUtil.getTrainingAppsMaps() + Constants.FORWARD_SLASH + UriUtil.makeURICompliant((String) impl);
                    } else {
                        // if not in maps, assume it's in course folder
                        url = DomainModuleProperties.getInstance().getDomainContentServerAddress()
                                + Constants.FORWARD_SLASH + UriUtil.makeURICompliant(courseRuntimeFolder)
                                + Constants.FORWARD_SLASH + implPath;
                    }
                } catch (@SuppressWarnings("unused") Exception e) {
                    // assume it's in course folder
                    url = DomainModuleProperties.getInstance().getDomainContentServerAddress() + Constants.FORWARD_SLASH
                            + UriUtil.makeURICompliant(courseRuntimeFolder) + Constants.FORWARD_SLASH + implPath;
                }
            }

			//get the inputs to the embedded app
			Serializable inputs = app.getEmbeddedAppInputs();

			if(embeddedApps.containsKey(impl)) {
				throw new ConfigurationException("Failed to process the embedded app url",
						"Found more than one url specified as " + url + " which is not currently supported.",
						null);
			}

			embeddedApps.put(url, inputs);
    	}

    	return embeddedApps;
    }

    /**
     * If necessary based on the provided interops, creates a service to play
     * back simulation messages from a log file
     *
     * @param courseFolder the location of the course's runtime folder. Cannot
     *        be null.
     * @param interops the interops used to determine if a log playback service
     *        is needed. Cannot be null.
     * @param domainSession the domain session used to invoke this method. Will
     *        be used to provide domain session information for messages played
     *        back from the log file. Cannot be null
     * @param logPlaybackTopicName the name of the topic that should be used to
     *        send simulation messages from the log playback service. Cannot be
     *        null.
     * @return A future that represents the asynchronous action which will yield
     *         the created log file playback service. Can not be null but the
     *         {@link CompletableFuture} can yield null if the provided interops
     *         did not need a log service.  To monitor for errors the calling thread
     *         must join the CompletableFuture and catch a CompletionException.
     */
    public CompletableFuture<LogFilePlaybackService> createPlaybackServiceIfRequested(File courseFolder, 
            generated.course.Interops interops, DomainSession domainSession, String logPlaybackTopicName) {

        if(StringUtils.isBlank(logPlaybackTopicName)){
            throw new IllegalArgumentException("The log playback topic name can't be null or empty");
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("Attempting to create playback service for "+domainSession+" for log in folder '"+courseFolder+"'.");
        }
        for (Interop interop : interops.getInterop()) {
            final Serializable interopInput = interop.getInteropInputs().getInteropInput();
            LogFile logFileFromInterop = null;
            if (interopInput instanceof DISInteropInputs) {
                DISInteropInputs disInteropInputs = (DISInteropInputs) interopInput;
                logFileFromInterop = disInteropInputs.getLogFile();
            } else if (interopInput instanceof HAVENInteropInputs) {
                HAVENInteropInputs havenInteropInputs = (HAVENInteropInputs) interopInput;
                logFileFromInterop = havenInteropInputs.getLogFile();
            } else if (interopInput instanceof RIDEInteropInputs) {
                RIDEInteropInputs rideInteropInputs = (RIDEInteropInputs) interopInput;
                logFileFromInterop = rideInteropInputs.getLogFile();
            }
            
            final LogFile logFileSpecification = logFileFromInterop;
                
            if (logFileSpecification != null) {
                final DomainSessionLog domainSessionLog = logFileSpecification.getDomainSessionLog();
                String domainSessionLogRelativePath = domainSessionLog.getValue();

                /* Calculate the location of the log file */
                File logFile = Paths.get(courseFolder.getAbsolutePath(), domainSessionLogRelativePath).toFile();
                int start = domainSessionLog.getStart().intValue();
                int end = domainSessionLog.getEnd().intValue();
                String audioCapture = logFileSpecification.getCapturedAudioFile();
                LogSpan logSpan = new LogSpan(start, end);
                LogFilePlaybackTarget target = msg -> {
                    /* Blindly send all learner states because only the ones
                     * we want to send to the monitor have been allowed past
                     * the filter */
                    if (msg.getMessageType().equals(MessageTypeEnum.LEARNER_STATE)) {
                        DomainModule.getInstance().sendLogPlaybackMessage(SubjectUtil.MONITOR_TOPIC, msg,
                                domainSession);
                    } else {
                        DomainModule.getInstance().sendLogPlaybackMessage(logPlaybackTopicName, msg, domainSession);
                    }
                };
                Predicate<Message> msgFilter = new Predicate<Message>() {

                    private boolean hasEncounteredStartResume = false;

                    @Override
                    public boolean test(Message msg) {
                        final String destinationQueueName = msg.getDestinationQueueName();
                        if (destinationQueueName.startsWith(SubjectUtil.GATEWAY_TOPIC_PREFIX) ||
                                destinationQueueName.startsWith(SubjectUtil.DOMAIN_TOPIC_PREFIX)) {
                            // send any training app state messages (e.g. DIS Entity State) that:
                            // 1. GATEWAY_TOPIC_PREFIX - sent by the GW module to DM in a live session (e.g. VBS sending DIS messages)
                            // 2. DOMAIN_TOPIC_PREFIX - sent by the DM module to DM in a playback session (e.g. VBS course object with GIFT session log)
                            
                            if (msg.getMessageType().equals(MessageTypeEnum.START_RESUME)) {
                                final boolean oldFlag = hasEncounteredStartResume;
                                hasEncounteredStartResume = true;
                                return oldFlag;
                            }

                            return true;
                        } else if(msg.getMessageType().equals(MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST) ||
                                msg.getMessageType().equals(MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST)){
                            // these messages are for hosting and adding all joiners to the team session for a replay
                            // of the session as if the learners are actively participating in the session even know they aren't
                            return true;
                        } else if (msg.getMessageType().equals(MessageTypeEnum.LEARNER_STATE)) {
                            /* Allow learner states past the filter to be
                             * sent directly to the monitor IFF it contains
                             * a null assessment for a team org entity. This
                             * is a hack implemented to show flashing orange
                             * squares in ARES. */
                            LearnerState learnerState = (LearnerState) msg.getPayload();
                            return learnerState.getPerformance().getTasks().values().stream()
                                    .flatMap(task -> task.getConcepts().stream()).anyMatch(concept -> concept
                                            .getState().getAssessedTeamOrgEntities().containsValue(null));
                        }

                        return false;
                    }
                };

                return CompletableFuture.supplyAsync(() -> {
                    try {
                        final String username = domainSession.getUsername();
                        final String userId = String.valueOf(domainSession.getUserId());
                        LogFilePlaybackService playbackService = new LogFilePlaybackService(logFile, logSpan,
                                username != null ? username : userId, target, msgFilter, null);
                        playbackService.setCapturedAudio(audioCapture);
                        return playbackService;
                    } catch (Exception e) {
                        String msg = "There was a problem creating a playback service for a training application's interops";
                        if(StringUtils.isNotBlank(e.getMessage())){
                            msg += " because "+e.getMessage();
                        }
                        throw new CompletionException(msg, e);
                    }

                });
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
