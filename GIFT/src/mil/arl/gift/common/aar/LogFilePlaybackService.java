/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar;

import static mil.arl.gift.common.util.StringUtils.join;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.PublishLessonScore;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.aar.util.AbstractAarAssessmentManager;
import mil.arl.gift.common.aar.util.ApplyPatchRequest;
import mil.arl.gift.common.aar.util.ApplyPatchResult;
import mil.arl.gift.common.aar.util.LogFilePlaybackMessageManager;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.ObserverControls;
import mil.arl.gift.common.course.dkf.session.SessionScenarioInfo;
import mil.arl.gift.common.course.strategy.AuthorizeStrategiesRequest;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.AbstractPerformanceStateAttribute;
import mil.arl.gift.common.state.AffectiveState;
import mil.arl.gift.common.state.CognitiveState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.ta.state.EntityAppearance;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.SequenceNumberGenerator;

/**
 * The service that is used to manage the playback of a log file to a
 * {@link LogFilePlaybackTarget}.
 *
 * @author tflowers
 *
 */
public class LogFilePlaybackService {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(LogFilePlaybackService.class);

    /** white list for message types to process messages as fast as possible (i.e. ignoring message timestamps) when we fast forward */
    private static final Set<MessageTypeEnum> FAST_FORWARD_MESSAGE_TYPE_WHITELIST = new HashSet<MessageTypeEnum>(Arrays
            .asList(MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST, // to help populate the game master strategy history panel
                    MessageTypeEnum.APPLY_STRATEGIES             // to help populate the game master strategy history panel
                    )
            );
    
    /** white list for message types to ignore timestamps of when calculating the amount of time until the next message, i.e. send as fast as possible */
    private static final Set<MessageTypeEnum> IGNORE_MSG_TIMESTAMP_WHITELIST = new HashSet<MessageTypeEnum>(Arrays.asList(
        MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST,
        MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST)
    );

    /** The interface to which the {@link Message} objects should be sent. */
    private final LogFilePlaybackTarget output;

    /**
     * The {@link Set} of all {@link EntityIdentifier} objects contained within
     * the log.
     */
    private final Map<EntityIdentifier, DomainSessionMessageEntry> entityIdToLastEntityMsg = new HashMap<>();

    /** The scheduler that is used to schedule messages for playback. */
    private final ScheduledExecutorService threadPool;

    /** A handle to the next scheduled message. Used to cancel */
    private ScheduledFuture<?> nextMessageFuture = null;

    /**
     * When paused, the time in milliseconds that was remaining before the next
     * message should have been played
     */
    private Long remainingDelayOnPause = null;

    /** A flag indicating whether messages should continue to be played. */
    private final AtomicBoolean isPlaying = new AtomicBoolean(false);

    /**
     * The millisecond difference between a timestamp from the 'past' timeline
     * (the one being played back) and the 'present' timeline (the time observed
     * at runtime).
     */
    private long pastPresentDelta = 0;

    /** The domain session id of the playback log */
    private final int domainSessionId;

    /** The username of the user playing the message back. */
    private final String username;

    /**
     * The location of the file containing the audio captured alongside the log
     * being played back, relative to the location of the course folder
     * containing the log
     */
    private String capturedAudioRef;

    /** The manager that handles the playback messages and patches */
    private final LogFilePlaybackMessageManager messageManager;
    
    /** The manager that should be used to perform complex assessment operations.
     * This will be null if the log being processed does not have a DKF saved 
     * alongside it that can be used to extrapolate assessment rules. */
    private AbstractAarAssessmentManager assessmentManager; 
    
    /**
     * Constructs a new {@link LogFilePlaybackService} that plays messages
     * matching a provided filter from a specified log file section to a
     * provided destination.
     *
     * @param logFile The {@link File} that contains the logged messages which
     *        should be played back. Can't be null.
     * @param logSpan The {@link LogSpan} that defines which messages from the
     *        {@link File} should be played back. Can't be null.
     * @param username The username that the playback should be associated with.
     * @param output The {@link LogFilePlaybackTarget} to which all messages
     *        should be sent. Can't be null.
     * @param msgFilter A filter method that can be applied to the messages to
     *        decide whether or not they should be sent to the
     *        {@link LogFilePlaybackTarget}. Can't be null.
     * @param assessmentMgr an optional manager that can be used to provide additional
     * information about a DKF scenario that is part of the playback. Can be null and 
     * generally should be null for legacy log folders that do not have associated DKFs.
     * @throws IOException if there is a problem reading from the {@link File}
     *         described by the {@link LogMetadata}.
     * @throws Exception if no messages where found in the log file in the log
     *         span
     */
    public LogFilePlaybackService(final File logFile, LogSpan logSpan, String username, LogFilePlaybackTarget output,
            Predicate<Message> msgFilter, AbstractAarAssessmentManager assessmentMgr) throws IOException, Exception {
        if (logger.isTraceEnabled()) {
            List<Object> params = Arrays.<Object>asList(logFile, logSpan, username, output, msgFilter);
            logger.trace("LogFilePlaybackService(" + join(", ", params) + ")");
        }

        if (logFile == null) {
            throw new IllegalArgumentException("The parameter 'logFile' cannot be null.");
        } else if (logSpan == null) {
            throw new IllegalArgumentException("The parameter 'logSpan' cannot be null.");
        } else if (output == null) {
            throw new IllegalArgumentException("The parameter 'output' cannot be null.");
        } else if (msgFilter == null) {
            throw new IllegalArgumentException("The parameter 'msgFilter' cannot be null.");
        }

        this.username = username;

        /* Create the executor */
        threadPool = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r, "LogFilePlaybackService - " + logFile.getName());
            thread.setDaemon(true);
            return thread;
        });

        /* Save a reference to the output and filter */
        this.output = output;

        messageManager = new LogFilePlaybackMessageManager(logFile, logSpan, username, msgFilter, assessmentMgr);
        
        this.assessmentManager = messageManager.getAssessmentManager();

        final List<MessageManager> messages = messageManager.getMessages();
        domainSessionId = messages.get(0).getMessage().getDomainSessionId();

        /* Inject the hide entity state messages for all the entities and cache
         * a reference to the message. */
        final ListIterator<MessageManager> msgIter = messages.listIterator(messages.size());
        while (msgIter.hasPrevious()) {
            final DomainSessionMessageEntry msg = msgIter.previous().getMessage();
            if (msg.getMessageType() == MessageTypeEnum.ENTITY_STATE) {
                final EntityState es = (EntityState) msg.getPayload();

                /* If the removal message for this entity has already been
                 * added, skip to the next iteration */
                if (entityIdToLastEntityMsg.containsKey(es.getEntityID())) {
                    continue;
                }

                /* Override the last entity state with an inactive flag */
                EntityAppearance modAppearance = es.getAppearance().replaceActive(false);
                EntityState modEs = synthesizeEntityState(es, modAppearance);
                final DomainSessionMessageEntry synMsg = synthesizeMessage(msg, modEs);

                /* Replace the last entity state message with the new
                 * synthesized one */
                msgIter.remove();
                msgIter.add(new MessageManager(synMsg));

                /* Move the iterator back its previous position */
                msgIter.previous();

                entityIdToLastEntityMsg.put(es.getEntityID(), synMsg);
            }
        }
    }
    
    /**
     * Get the absolute path to the folder containing the log file being used in this manager.
     * @return the absolute path to the parent folder of this log file, 
     * e.g. E:\work\trunk\GIFT\output\domainSessions\domainSession1347_uId1
     */
    public String getLogFileFolder(){
        return messageManager.getLogFileFolder();
    }
    
    /**
     * Get the DKF file name for this log being played.  
     * @return can be null for legacy logs that didn't have the dkf file provided, otherwise this will
     * be the dkf file name, e.g. vbs2.TSP 07-GFT-0137 ClearBldg.dkf.xml
     */
    public String getDkfFileName(){
        File dkf = messageManager.getDkf();
        return dkf != null ? dkf.getName() : null;
    }
    
    /**
     * Get the name of the log file used in this manager.
     * @return the log file name, e.g. domainSession1347_uId1_2021-11-10_09-29-59.protobuf.bin
     */
    public String getLogFileName(){
        return messageManager.getLogFileName();
    }
    
    /**
     * Retrieve the full list of playback messages
     * 
     * @return the collection of playback messages. </br>
     * In the future this should be a stream that is not directly associated with
     * the collection of messages being played back.  Until then, callers should
     * NOT manipulate this collection.
     */
    public List<MessageManager> getMessages() {
        return messageManager.getMessages();
    }

    /**
     * Whether the playback log contains a team knowledge session or not.
     * @return true if the log is going to be playing back a team knowledge session, false otherwise.
     */
    public boolean isTeamSession(){
        return messageManager.isTeamSession();
    }

    /**
     * Checks if the playback is currently playing.
     * 
     * @return true if the playback is playing; false otherwise.
     */
    public synchronized boolean isPlaying() {
        return isPlaying.get();
    }

    /**
     * Move the play head to a different position within the message log.
     *
     * @param time The time period to move to measured in milliseconds relative
     *        to the first message. Must be zero or greater.
     * @throws UnsupportedOperationException if time value provided was outside
     *         the range of the
     */
    public synchronized void seek(long time) throws UnsupportedOperationException {
        if (logger.isTraceEnabled()) {
            logger.trace("seek(" + time + ")");
        }

        if (time < 0) {
            throw new UnsupportedOperationException("The time specified must be 0 or greater");
        }

        seek(msg -> {
            return msg.getTimeStamp() >= time;
        });
    }

    /**
     * Move the play head to a different position within the message log.
     *
     * @param msgFilter the filter used to determine where to fast forward to.
     *        Can't be null.
     */
    private synchronized void seek(final Predicate<MessageManager> msgFilter) {
        if (msgFilter == null) {
            throw new IllegalArgumentException("The parameter 'msgFilter' cannot be null.");
        }

        /* Cancel the next message since we are moving the playhead */
        tryToCancelNextMessage();

        DomainSessionMessageEntry currentMessage = messageManager.getCurrentMessage();

        /* Start from the beginning and find the first message that comes after
         * the specified time stamp and matches the filter provided at
         * construction */
        messageManager.resetMessageIndexToIndex(0);

        /* Fast forward to the first message after or at the message specified
         * by the filter */
        currentMessage = messageManager.fastForwardToNextValidMessageManager(msg -> {
            final DomainSessionMessageEntry domainMsg = msg.getMessage();
            if (FAST_FORWARD_MESSAGE_TYPE_WHITELIST.contains(domainMsg.getMessageType())) {
                sendMessage(domainMsg);
            }

            return msgFilter.test(msg);
        }).getMessage();

        long time = messageManager.getCurrentMessageManager().getTimeStamp();
        
        /* Send whatever the last learner state was from the new playhead
         * position */
        Map<MessageTypeEnum, DomainSessionMessageEntry> latestMessages = messageManager
                .getLatestMessagesOfType(MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST, MessageTypeEnum.APPLY_STRATEGIES, MessageTypeEnum.LEARNER_STATE);
        DomainSessionMessageEntry learnerStateMsg = latestMessages.get(MessageTypeEnum.LEARNER_STATE);
        DomainSessionMessageEntry authorizeStrategyMsg = latestMessages
                .get(MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST);

        /* Create an empty learner state if none was found */
        Message synLearnerStateMsg;
        final UserSession userSession = new UserSession(0);
        userSession.setUsername(username);
        if (learnerStateMsg == null) {
            LearnerState newLearnerState = new LearnerState(new PerformanceState(), new CognitiveState(),
                    new AffectiveState());
            DomainSessionMessage domainMsg = new DomainSessionMessage(MessageTypeEnum.LEARNER_STATE,
                    SequenceNumberGenerator.nextSeqNumber(), 0, time, null, null, ModuleTypeEnum.DOMAIN_MODULE, null,
                    newLearnerState, userSession, domainSessionId, false);
            synLearnerStateMsg = new DomainSessionMessageEntry(domainMsg.getSourceEventId(),
                    domainMsg.getDomainSessionId(), domainMsg.getUserSession(), 0, time, domainMsg);
        } else {
            synLearnerStateMsg = synthesizeMessage(learnerStateMsg, time);
        }

        /* Create an empty authorize strategy request if none was found */
        if (authorizeStrategyMsg == null) {
            AuthorizeStrategiesRequest newStrategyRequest = new AuthorizeStrategiesRequest(
                    new HashMap<String, List<StrategyToApply>>(), null);
            DomainSessionMessage domainMsg = new DomainSessionMessage(MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST,
                    SequenceNumberGenerator.nextSeqNumber(), 0, time, null, null, ModuleTypeEnum.DOMAIN_MODULE, null,
                    newStrategyRequest, userSession, domainSessionId, false);
            sendMessage(new DomainSessionMessageEntry(domainMsg.getSourceEventId(), domainMsg.getDomainSessionId(),
                    domainMsg.getUserSession(), 0, time, domainMsg));
        }

        sendMessage(synLearnerStateMsg);

        sendLatestEntityStates(time);

        /* Reestablish the relationship between the past time stamps and present
         * time stamps since we've moved to a new location within the past
         * timeline and therefore broke the existing relationship */
        remapTimePeriods();

        /* Get the next message and calculate the delay */
        final long delay = currentMessage.getTimeStamp() - time;
        if (isPlaying.get()) {
            nextMessageFuture = scheduleNextMessage(delay);
        } else {
            /* Timeline is paused, so set how much time between the seek time
             * and the next message */
            remainingDelayOnPause = delay;
        }
    }

    /**
     * Move the play head to a different position within the message log.
     *
     * @param taskConceptName the name of the task or concept that we are
     *        searching for to jump to the activation start. Can't be null or
     *        empty.
     * @return the timestamp of the activation start.
     */
    public synchronized Long jumpToActivationStart(String taskConceptName) {
        seek(msg -> {
            if (msg.getMessageType() == MessageTypeEnum.LEARNER_STATE) {
                /* Gather learner state messages from the log to populate the
                 * timeline. Check if there is patch for this learner state
                 * before modifying the timestamp. */

                if (msg.hasPerformanceStateWithName(taskConceptName)) {
                    final AbstractPerformanceState origState = msg.getOriginalPerformanceStateWithName(taskConceptName);

                    final boolean isUnactivated = PerformanceNodeStateEnum.UNACTIVATED
                            .equals(origState.getState().getNodeStateEnum());
                    if (!isUnactivated) {
                        return true;
                    }
                }
            }

            return false;
        });

        return messageManager.getCurrentMessage().getTimeStamp();
    }
    
    /**
     * Copies the provided audio file from the course folder to the session output folder for use by applications
     * like the Game Master.  Currently the audio file is placed at the root of the session output folder.
     * @param audioFilePath the course folder relative path to an audio file (e.g. urbanOp_capstone final audio_v4.mp3).
     * If null or empty this method returns null.
     * @param courseFolderPath the full workspace path of the course folder (e.g. Public/Urban Operation - JOURNEYMAN Playback)
     * If null or empty this method returns null.
     * @param capturedAudioProxy the proxy to the audio file, used to copy the files to the new location.  If null, this method returns null.
     * @param sessionOutputFolder the name of the session output folder location in the GIFT/output/domainSessions/ folder (e.g. 'domainSession3_uId1'). If null
     * or empty this method returns null.
     * @return the full workspace path to the audio file, can be used to play the audio file 
     * (e.g. Public/Urban Operation - JOURNEYMAN Playback/urbanOp_capstone final audio_v4.mp3)
     * Can be null under the conditions listed for each @param above.
     * @throws Exception if there was an issue copying the audio file
     */
    public static String copyAudioToSessionOutput(String audioFilePath, String courseFolderPath, FileProxy capturedAudioProxy, 
            String sessionOutputFolder) throws Exception{
        
        if(StringUtils.isBlank(audioFilePath) || StringUtils.isBlank(courseFolderPath) ||
                capturedAudioProxy == null || StringUtils.isBlank(sessionOutputFolder)){
            return null;
        }
        
        InputStream capturedAudioInput = capturedAudioProxy.getInputStream();
        java.nio.file.Path copyTarget = java.nio.file.Paths.get(PackageUtil.getDomainSessions() + File.separator + sessionOutputFolder + File.separator + capturedAudioProxy.getName());
        java.nio.file.Files.copy(capturedAudioInput, copyTarget, StandardCopyOption.REPLACE_EXISTING);
        return courseFolderPath + Constants.FORWARD_SLASH + audioFilePath;
    }
    
    /**
     * Removes the workspace course folder from the path to the captured audio so that it can be played in the Game
     * Master from the session output folder.
     * E.g. transform 'Public/Urban Operation - JOURNEYMAN Playback/urbanOp_capstone final audio_v4.mp3' into
     * 'urbanOp_capstone final audio_v4.mp3'
     * @param observerControls can contain the captured audio.  If this is null or the captured audio path is null
     * this method does nothing.
     */
    public static void prepareSessionOutputAudioFileName(ObserverControls observerControls){
        
        if(observerControls == null || observerControls.getCapturedAudioPath() == null){
            return;
        }
        
        String origPath = observerControls.getCapturedAudioPath();
        if(origPath.contains(File.separator)){
            String newPath = origPath.substring(origPath.lastIndexOf(File.separator)+1);
            observerControls.setCapturedAudioPath(newPath);
        }else if(origPath.contains("/")){
            String newPath = origPath.substring(origPath.lastIndexOf("/")+1);
            observerControls.setCapturedAudioPath(newPath);
        }
    }

    /**
     * Reestablishes the relationship between the simulation timeline and the
     * current time.
     */
    private void remapTimePeriods() {
        final DomainSessionMessageEntry currentMessage = messageManager.getCurrentMessage();
        if (currentMessage != null) {
            final long timeStamp = currentMessage.getTimeStamp();
            pastPresentDelta = System.currentTimeMillis() - timeStamp;
        }
    }

    /**
     * Converts a timestamp from the real-time timeline to the simulation
     * timeline.
     *
     * @param presentTime The real-time timestamp to convert to the simulation
     *        timestamp.
     * @return The simulation timestamp.
     */
    private long presentTimeToPastTime(long presentTime) {
        return presentTime - pastPresentDelta;
    }

    /**
     * Sends an {@link MessageTypeEnum#ENTITY_STATE} {@link Message} for each
     * entity at the provided time.
     *
     * @param time The time in milliseconds for which the updates should be
     *        sent.
     */
    private void sendLatestEntityStates(long time) {
        final Set<EntityIdentifier> foundIds = new HashSet<>();
        for (int i = messageManager.getCurrentMessageIndex() - 1; i >= 0; i--) {
            DomainSessionMessageEntry msg = messageManager.getMessage(i);

            /* If we have looked farther back in time that permitted by our
             * entity timeout, stop processing messages */
            final long ENTITY_TIMEOUT = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);
            if (time - msg.getTimeStamp() > ENTITY_TIMEOUT) {
                break;
            }

            if (msg.getMessageType() == MessageTypeEnum.ENTITY_STATE) {
                final EntityState es = (EntityState) msg.getPayload();
                final EntityIdentifier entityId = es.getEntityID();

                /* If this entity has already been processed, skip to the next
                 * one. */
                if (!foundIds.add(entityId)) {
                    continue;
                }

                /* Send a modified version of that message */
                DomainSessionMessageEntry modMsg = synthesizeMessage(msg, time);
                sendMessage(modMsg);
            }
        }

        /* Send deactivate messages for all the entity ids that were not
         * found */
        final HashSet<EntityIdentifier> remainingIds = new HashSet<>(entityIdToLastEntityMsg.keySet());
        remainingIds.removeAll(foundIds);
        for (EntityIdentifier remainingId : remainingIds) {
            DomainSessionMessageEntry msg = entityIdToLastEntityMsg.get(remainingId);
            DomainSessionMessageEntry modMsg = synthesizeMessage(msg, time);
            sendMessage(modMsg);
        }
    }

    /**
     * Begins playback from the current position
     * 
     * @param ignoreDelay true to ignore any previous message delay from pause
     *        and start executing messages immediately; false to use the delay
     *        (if it exists) before sending the next message.
     */
    public void startPlayback(boolean ignoreDelay) {
        if (logger.isTraceEnabled()) {
            logger.trace("startPlayback()");
        }

        if (isPlaying.get()) {
            return; // don't start the playback if is already playing, or else
                    // it will speed up
        }

        /* Reestablish the relationship between the past time stamps and present
         * time stamps since we've started the playback from a different time in
         * the present. */
        remapTimePeriods();

        isPlaying.set(true);

        if (ignoreDelay || remainingDelayOnPause == null) {
            executeNextMessage();
        } else {
            nextMessageFuture = scheduleNextMessage(remainingDelayOnPause);
        }

        /* Playback has started again, so this value is no longer needed */
        remainingDelayOnPause = null;
    }

    /**
     * Executes the next message and automatically schedules the message after
     * that.
     */
    private void executeNextMessage() {
        if (logger.isTraceEnabled()) {
            logger.trace("executeNextMessage()");
        }

        /* Get the message to send */
        DomainSessionMessageEntry msg = messageManager.getCurrentMessage();

        /* If the termination of playback has been requested or there is no next
         * message, stop playback. */
        if (!isPlaying.get() || msg == null) {
            pausePlayback();
            return;
        }

        /* Since we will execute at least one message, initialize the delay to
         * 0. */
        long delay = 0;

        /* While playback is behind schedule, do not delay the execution of the
         * messages */
        while (delay <= 0) {
            sendMessage(msg);

            /* Fast forward to the next message. */
            msg = messageManager.fastForwardToNextValidMessage();

            /* If there is no next message, cease playback and return early. */
            if (msg == null) {
                pausePlayback();
                return;
            }

            /* Calculate the delay to wait for the next message */
            long nowInPastTime = presentTimeToPastTime(System.currentTimeMillis());
            if(!IGNORE_MSG_TIMESTAMP_WHITELIST.contains(msg.getMessageType())){
                delay = msg.getTimeStamp() - nowInPastTime;
            }
        }

        /* Schedule the next message */
        nextMessageFuture = scheduleNextMessage(delay);
    }

    /**
     * Schedules the execution of the {@link #executeNextMessage()} method for
     * the specified delay.
     *
     * @param delay The amount of time in milliseconds to wait before calling
     *        the {@link #executeNextMessage()} method.
     * @return A {@link ScheduledFuture} that can be used to cancel the pending
     *         call to {@link #executeNextMessage()}. Can't be null.
     */
    private ScheduledFuture<?> scheduleNextMessage(long delay) {
        if (logger.isTraceEnabled()) {
            logger.trace("scheduleNextMessage(" + delay + ")");
        }

        return threadPool.schedule(this::executeNextMessage, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Sends a provided {@link Message} to the {@link #output}.
     *
     * @param msg The {@link Message} to send.
     */
    private void sendMessage(Message msg) {
        try {
            output.handlePlayedbackMessage(msg);
        } catch (Throwable t) {
            logger.error("There was an unhandled error while sending the message " + msg, t);
        }
    }

    /**
     * Ceases playback of the messages.
     */
    public void terminatePlayback() {
        if (logger.isTraceEnabled()) {
            logger.trace("terminatePlayback()");
        }

        pausePlayback();

        threadPool.shutdown();

        messageManager.clearAll();
    }
    
    /**
     * Return whether this playback service was terminated.
     * 
     * @return true if the playback thread pool was shutdown by calling {@link #terminatePlayback()}, meaning no new messages will come out.
     */
    public boolean hasTerminated(){
        return threadPool.isShutdown();
    }

    /**
     * Temporarily pauses playback of the log file. If the playback should be
     * terminated permanently, call {@link #terminatePlayback()} instead.
     */
    public void pausePlayback() {
        if (logger.isTraceEnabled()) {
            logger.trace("pausePlayback()");
        }

        /* Check if it already paused */
        if (!isPlaying.get()) {
            return;
        }

        /* Signal that playback should stop */
        isPlaying.set(false);
        remainingDelayOnPause = tryToCancelNextMessage();
    }

    /**
     * Attempts to cancel the playback of the next message if possible.
     * 
     * @return the remaining delay before the future would have executed. Can be
     *         null.
     */
    private Long tryToCancelNextMessage() {
        if (logger.isTraceEnabled()) {
            logger.trace("tryToCancelNextMessage()");
        }

        Long remainingDelay = null;
        final ScheduledFuture<?> future = nextMessageFuture;
        if (future != null) {
            remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
            future.cancel(false); // avoid interrupting web socket messages,
                                  // since that can kill the connection
        }

        /* Reset */
        nextMessageFuture = null;
        remainingDelayOnPause = null;

        return remainingDelay != null && remainingDelay > 0 ? remainingDelay : null;
    }

    /**
     * Creates a modified version of a provided
     * {@link DomainSessionMessageEntry} with a different payload.
     *
     * @param msg The {@link DomainSessionMessageEntry} to duplicate. Can't be
     *        null.
     * @param payload The new payload object to use.
     * @return The newly created message. Can't be null.
     */
    private DomainSessionMessageEntry synthesizeMessage(DomainSessionMessageEntry msg, Object payload) {
        Message intermediate = new Message(msg.getMessageType(), msg.getSequenceNumber(), msg.getSourceEventId(),
                msg.getTimeStamp(), msg.getSenderModuleName(), msg.getSenderAddress(), msg.getSenderModuleType(),
                msg.getDestinationQueueName(), payload, false);
        return new DomainSessionMessageEntry(msg.getSourceEventId(), msg.getDomainSessionId(), msg.getUserSession(),
                msg.getElapsedDSTime(), msg.getWriteTime(), intermediate);
    }

    /**
     * Creates a modified version of a provided
     * {@link DomainSessionMessageEntry} with a different payload.
     *
     * @param msg The {@link DomainSessionMessageEntry} to duplicate. Can't be
     *        null.
     * @param time The new time to use for the duplicated message.
     * @return The newly created {@link DomainSessionMessageEntry}. Can't be
     *         null.
     */
    private DomainSessionMessageEntry synthesizeMessage(DomainSessionMessageEntry msg, long time) {
        long timeShift = time - msg.getTimeStamp();
        Message intermediate = new Message(msg.getMessageType(), msg.getSequenceNumber(), msg.getSourceEventId(), time,
                msg.getSenderModuleName(), msg.getSenderAddress(), msg.getSenderModuleType(),
                msg.getDestinationQueueName(), msg.getPayload(), false);
        return new DomainSessionMessageEntry(msg.getSourceEventId(), msg.getDomainSessionId(), msg.getUserSession(),
                msg.getElapsedDSTime() + timeShift, msg.getWriteTime() + timeShift, intermediate);
    }

    /**
     * Creates a modified version of a provided {@link EntityState} object with
     * a different {@link EntityAppearance}.
     *
     * @param es The {@link EntityState} to duplicate. Can't be null.
     * @param entityAppearance The new {@link EntityAppearance} to use for the
     *        duplicated {@link EntityState}.
     * @return The newly created {@link EntityState}. can't be null.
     */
    private EntityState synthesizeEntityState(EntityState es, EntityAppearance entityAppearance) {
        EntityState newEntityState = new EntityState(es.getEntityID(), es.getForceID(), es.getEntityType(),
                es.getLinearVelocity(), es.getLocation(), es.getOrientation(), es.getArticulationParameters(),
                entityAppearance, es.getEntityMarking());
        newEntityState.setAlternativeEntityType(es.getAlternativeEntityType());
        return newEntityState;
    }

    /**
     * Gets the location of the file containing the audio captured alongside the
     * log being played back, relative to the location of the course folder
     * containing the log
     *
     * @return the captured audio file's location relative to the course folder.
     *         Can be null, if the log being played back has no captured audio.
     */
    public String getCapturedAudio() {
        return capturedAudioRef;
    }

    /**
     * Gets the location of the file containing the audio captured alongside the
     * log being played back, relative to the location of the course folder
     * containing the log
     *
     * @param capturedAudioRef the captured audio file's location relative to
     *        the course folder. Can be null, if the log being played back has
     *        no captured audio.
     */
    public void setCapturedAudio(String capturedAudioRef) {
        this.capturedAudioRef = capturedAudioRef;
    }
    
    /**
     * Write the patched messages to file.
     * 
     * @param username the username of the person making the edits.
     * @return the patch file name
     * @throws DetailedException if there is a problem writing to the patch
     *         file.
     */
    public String writePatchMessages(String username) throws DetailedException {
        return messageManager.writePatchMessages(username);
    }

    /**
     * Delete the session log patch file.
     * 
     * @param logMetadata the associated {@link LogMetadata} of the patch file
     *        being deleted.
     * @throws DetailedException if there is a problem writing to the patch
     *         file.
     */
    public void deleteLogPatchFile(LogMetadata logMetadata) throws DetailedException {
        long prevMsgIndexTimestamp = 0;
        DomainSessionMessageEntry cMsg = messageManager.getCurrentMessage();
        if (cMsg != null) {
            prevMsgIndexTimestamp = cMsg.getTimeStamp();
        }

        messageManager.deleteLogPatchFile();
        messageManager.clearAll();

        try {
            messageManager.readLogFile();
            seek(prevMsgIndexTimestamp);
        } catch (Exception e) {
            throw new DetailedException("Unable to parse the log file.",
                    "There was a problem reading the log file because " + e.getMessage(), e);
        }
    }

    /**
     * Updates all relevant messages from the log file at and after the given
     * timestamp for the provided {@link PerformanceStateAttribute}.
     * 
     * @param timestamp the timestamp that determines when to start updating log
     *        messages.
     * @param performanceState the performance state attribute that contains the
     *        edits being persisted.
     * @return the results from injecting the patch changes. Will never be null.
     */
    public ApplyPatchResult editLogPatchForPerformanceStateAttribute(long timestamp,
            PerformanceStateAttribute performanceState) {
        
        Set<PerformanceStateAttribute> requestPatches = new HashSet<>();
        requestPatches.add(performanceState);
        
        ApplyPatchRequest request = new ApplyPatchRequest(requestPatches, timestamp);
        final ApplyPatchResult results = messageManager.editPatch(request);

        /* Resend the current message in case it changed */
        if (messageManager.getCurrentMessage() != null) {
            sendMessage(messageManager.getCurrentMessage());
        }

        return results;
    }

    /**
     * Updates all relevant messages from the log file at and after the given
     * timestamp for the provided {@link EvaluatorUpdateRequest}.
     *
     * @param timestamp the timestamp that determines when to start updating log
     *        messages.
     * @param updateEntireSpan true to apply the patch to the entire message
     *        span; false to apply it starting at the provided timestamp (if no
     *        message exists, one will be created).
     * @param request the evaluator update that contains the edits being
     *        persisted.
     * @param applyToFutureStates whether the update request should be applied to
     *        future learner states as well.
     * @return a list of results from injecting the patch changes. If the EvaluatorUpdateRequest is applied, 
     *         then this will contain at least one patch result that changes the assessment of the request's target node.
     *         If the change made by the EvaluatorUpdateRequest causes parent performance nodes to change their 
     *         assessments due to assessment rollup rules, then additional patches will be added to the list to 
     *         change each affected performance node. The first result in the list will always represent a change 
     *         to the target node of the EvaluatorUpdateRequest, while the last result will represent the change 
     *         to the highest parent performance node that the assessment was rolled up to. Each performance node in 
     *         the assessment rollup hierarchy will only have one patch result in the list. Can be null if the
     *         log has no associated DKF AND the performance state to update can't be found.
     */
    public List<ApplyPatchResult> updateMessagesForEvaluatorUpdate(long timestamp, boolean updateEntireSpan, EvaluatorUpdateRequest request, 
            boolean applyToFutureStates) {
        
        List<ApplyPatchResult> toReturn = new ArrayList<>();
        
        final DomainSessionMessageEntry lastKnownLearnerStateMsg = messageManager
                .getLatestMessagesOfType(MessageTypeEnum.LEARNER_STATE).get(MessageTypeEnum.LEARNER_STATE);
        
        List<DomainSessionMessageEntry> msgsToUpdate = new ArrayList<>();
        msgsToUpdate.add(lastKnownLearnerStateMsg);
        
        if(applyToFutureStates) {
            
            /* Get all of the future learner state messages that need to be updated */
            List<DomainSessionMessageEntry> futureLearnerStateMsgs = messageManager
                    .getFutureMessagesOfType(MessageTypeEnum.LEARNER_STATE).get(MessageTypeEnum.LEARNER_STATE);
            
            msgsToUpdate.addAll(futureLearnerStateMsgs);
        }
        
        boolean first = true;
        PerformanceNodeStateEnum lastNodeState = null;
        
        /* Apply the update request to all of the learner states that are being updated */
        for(DomainSessionMessageEntry currentMsg : msgsToUpdate) {
            
            long patchTime;
            if(first) {
                
                /* For the message where the update begins, patch at the timestamp of the update request (i.e.
                 * set the assessment time to when the user chose to change the assessment) 
                 * 
                 * This is especially important when we are setting the assessment from the current
                 * playhead position*/
                first = false;
                patchTime = timestamp;
                
            } else {
                
                /* For subsequent messages, patch at the timestamp of the update */
                patchTime = currentMsg.getTimeStamp();
            }
            
            /* Track the active/inactive state of the node that is targeted by the update request as we 
             * iterate through each learner state message*/
            AbstractPerformanceState state = MessageManager.findPerformanceStateByName(currentMsg,
                    request.getNodeName());
            if(state == null) {
                continue;
            }
            
            PerformanceNodeStateEnum thisNodeState = state.getState().getNodeStateEnum();
            
            /* Need to look at the performance node state to determine if propagation to future learner states should end */
            boolean shouldEndFuturePropagation = false;
            if(lastNodeState != null) {
                
                if(PerformanceNodeStateEnum.ACTIVE.equals(lastNodeState) 
                        && PerformanceNodeStateEnum.FINISHED.equals(thisNodeState)) {
                    
                    /* This is a special case. If the performance node whose assessment is being changed has gone from active
                     * to finished, we do NOT want stop future propagation yet, since we might be editing the assessment of a concept
                     * whose conditions have all finished but whose parent task has not completed. */
                    shouldEndFuturePropagation = false;
                    
                } else {
                    
                    /* Otherwise, for all other cases, stop propagation once the perfornace node state has changed*/
                    shouldEndFuturePropagation = !Objects.equals(thisNodeState, lastNodeState);
                }
            }
            
            if(shouldEndFuturePropagation) {
                
                /* The target performance node switched activity states after the last learner
                 * state, so we need to stop propagating updates to future learner states. 
                 * 
                 * Doing this avoids accidentally adding assessments after a task has gone inactive */
                break;
                
            } else {
                
                /* The target performance node has not switched activity states, so keep propagating updates */
                lastNodeState = thisNodeState;
            }
            
        if(assessmentManager != null) {
            
                /* An assessment manager is present, which means that this log has a DKF saved alongside
             * it that can be used to roll up assessments.
             * 
             * Use this manager to apply the assessment and gather all the performance node states that
             * are modified by the assessment rollup. */
                Set<PerformanceStateAttribute> changdAttributes = new HashSet<>(assessmentManager.applyAndRollUp(request, 
                        MessageManager.getLearnerPerformanceState(currentMsg)));
            
            List<ApplyPatchResult> resultList = new ArrayList<>();
                
                /* Apply a patch for each performance state attribute that was modified*/
                final ApplyPatchRequest patchRequest = new ApplyPatchRequest(changdAttributes, patchTime);
                patchRequest.setUpdateEntireSpan(updateEntireSpan);
                final ApplyPatchResult results = messageManager.applyPatchToMessages(patchRequest);
        
                /* Send this message because evaluator updates are always at 'now'
                 * time */
                if (results.getCurrentMessage() != null) {
                    sendMessage(results.getCurrentMessage().getMessage());
                }
                
                resultList.add(results);
            
                toReturn.addAll(resultList);
            
        } else {
            
            /* Legacy logic used to support patching assessments for logs that do not have a DKF saved
             * along side them.
             * 
             * Since there's no DKF to determine rollup rules for, simply apply the evaluator update 
             * request to the performance state of the node that it targets */
                if (state == null || state.getState() == null) {
                    return null;
                }
        
                PerformanceStateAttribute copy = state.getState().deepCopy();
                updatePerformanceAssessmentMetrics(request, copy);
        
                Set<PerformanceStateAttribute> requestPatches = new HashSet<>();
                requestPatches.add(copy);
                final ApplyPatchRequest patchRequest = new ApplyPatchRequest(requestPatches, patchTime);
                patchRequest.setUpdateEntireSpan(updateEntireSpan);
                final ApplyPatchResult results = messageManager.applyPatchToMessages(patchRequest);
        
                /* Send this message because evaluator updates are always at 'now'
                 * time */
                if (results.getCurrentMessage() != null) {
                    sendMessage(results.getCurrentMessage().getMessage());
                }
                
                toReturn.add(results);
            }
    }

        return toReturn;
    }

    /**
     * Updates all relevant messages from the log file at and after the given
     * timestamp for the deleted {@link PerformanceStateAttribute}.
     * 
     * @param timestamp the timestamp that determines when to start updating log
     *        messages.
     * @param performanceState the performance state attribute that contains the
     *        edits being persisted.
     * @return the results from injecting the patch changes. Will never be null.
     */
    public ApplyPatchResult removeLogPatchForAttribute(long timestamp, PerformanceStateAttribute performanceState) {
        final ApplyPatchResult results = messageManager.removeLogPatchForAttribute(timestamp, performanceState);
        
        /* Resend the current message in case it changed */
        if (messageManager.getCurrentMessage() != null) {
            sendMessage(messageManager.getCurrentMessage());
        }
        
        return results;
    }

    /**
     * Updates the provided assessment metrics with the values in the request.
     *
     * @param request the request containing the new metric values.
     * @param perfStateAttr the {@link AbstractPerformanceStateAttribute}
     */
    private void updatePerformanceAssessmentMetrics(EvaluatorUpdateRequest request,
            AbstractPerformanceStateAttribute perfStateAttr) {
        if (request == null) {
            throw new IllegalArgumentException("The parameter 'request' cannot be null.");
        } else if (perfStateAttr == null) {
            throw new IllegalArgumentException("The parameter 'perfStateAttr' cannot be null.");
        }

        final Map<String, AssessmentLevelEnum> teamOrgEntities = request.getTeamOrgEntities();
        boolean hasTeamMembers = CollectionUtils.isNotEmpty(teamOrgEntities);

        /* If the request has team org entities, then add them to the
         * assessment */
        if (hasTeamMembers) {
            perfStateAttr.setAssessedTeamOrgEntities(teamOrgEntities);
        }

        // update performance metric
        final AssessmentLevelEnum newMetric = request.getPerformanceMetric();
        if (newMetric != null) {
            perfStateAttr.updateShortTerm(newMetric, true);

            /* If the request has no team org entities specified, then apply the
             * assessment update to all previously known entities */
            if (!hasTeamMembers) {
                final Map<String, AssessmentLevelEnum> assessedTeamOrgEntities = perfStateAttr
                        .getAssessedTeamOrgEntities();
                for (String entity : assessedTeamOrgEntities.keySet()) {
                    assessedTeamOrgEntities.put(entity, newMetric);
                }
            }
        }

        perfStateAttr.setObserverComment(request.getReason());
        perfStateAttr.setObserverMedia(request.getMediaFile());

        // update competence metric
        if (request.getCompetenceMetric() != null) {
            perfStateAttr.setCompetence(request.getCompetenceMetric(), true);
        }

        // update confidence metric
        if (request.getConfidenceMetric() != null) {
            perfStateAttr.setConfidence(request.getConfidenceMetric(), true);
        }

        // update priority metric
        if (request.getPriorityMetric() != null) {
            perfStateAttr.setPriority(request.getPriorityMetric(), true);
        }

        // update trend metric
        if (request.getTrendMetric() != null) {
            perfStateAttr.setTrend(request.getTrendMetric(), true);
        }

        // update the user that updated the metrics
        if (request.getEvaluator() != null) {
            perfStateAttr.setEvaluator(request.getEvaluator());
        }

        /* Update the hold states */
        if (request.isAssessmentHold() != null) {
            perfStateAttr.setAssessmentHold(request.isAssessmentHold());
        }

        if (request.isPriorityHold() != null) {
            perfStateAttr.setPriorityHold(request.isPriorityHold());
        }

        if (request.isConfidenceHold() != null) {
            perfStateAttr.setConfidenceHold(request.isConfidenceHold());
        }

        if (request.isCompetenceHold() != null) {
            perfStateAttr.setCompetenceHold(request.isCompetenceHold());
        }

        if (request.isTrendHold() != null) {
            perfStateAttr.setTrendHold(request.isTrendHold());
        }

        if (perfStateAttr.getAssessmentExplanation() == null) {
            perfStateAttr.setAssessmentExplanation(new HashSet<String>(), true);
        }

        perfStateAttr.setPerformanceAssessmentTime(request.getTimestamp());

        /* Set assessment explanation value */
        if (StringUtils.isNotBlank(request.getReason())) {
            /* Use the optional bookmark value provided by the observer */
            perfStateAttr.getAssessmentExplanation().clear();
            perfStateAttr.getAssessmentExplanation().add(request.getReason());
        } else if (hasTeamMembers) {
            /* Create assessment explanation based on team org members
             * selected */
            StringBuilder sb = new StringBuilder("[");
            StringUtils.join(", ", teamOrgEntities.keySet(), sb);
            sb.append("] ").append(teamOrgEntities.size() == 1 ? "has" : "have").append(" been assessed.");
            perfStateAttr.getAssessmentExplanation().clear();
            perfStateAttr.getAssessmentExplanation().add(sb.toString());
        }
    }
    
    /**
     * Gets structural information about the scenario associated with the knowledge session
     * that is being played back 
     * 
     * @return the scenario information. Can be null if this knowledge session does not have
     * an associated DKF.
     */
    public SessionScenarioInfo getKnowledgeSessionScenario() {
        
        if(assessmentManager != null) {
            
            SessionScenarioInfo scenarioInfo = assessmentManager.getScenario();
            
            /* Find the most recently published graded score for the scenario's nodes. 
             * Since multiple scores may have been pushed due to course progress updates*/
            final MessageManager currentMsg = messageManager.getPublishedSummativeAssessmentsMessage();
            if(currentMsg != null){
                
                Message msg = currentMsg.getMessage();
                PublishLessonScore summativeScores = (PublishLessonScore) msg.getPayload();
                
                /* Update the session scenario info with the latest score */
                GradedScoreNode score = summativeScores.getCourseData().getRoot();
                scenarioInfo.setCurrentScore(score);
                scenarioInfo.setCourseConcepts(summativeScores.getConcepts());
            }
            
            return scenarioInfo;
        }
        
        return null;
    }
    
    /**
     * Updates messages in the log with new overall assessments based on 
     * the provided condition assessments
     *
     * @param timestamp the timestamp to apply the overall assessments at
     * @param a map of the condition assessments that were provided by an observer controller. Cannot be null.
     * @param perfStateAttr the {@link AbstractPerformanceStateAttribute}
     * @param courseConcepts the course concepts. Cannot be null.
     */
    public ApplyPatchResult updateMessagesForOverallAssessments(long timestamp, 
            Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments,
            List<String> courseConcepts) {
        
        ApplyPatchResult toReturn = null;
        
        /* Apply the update request to all of the learner states that are being updated */ 
        if(assessmentManager != null) {
            
            GradedScoreNode scoreNode =  assessmentManager.scoreOverallAsessments(conceptToConditionAssessments, courseConcepts);
                
            /* Apply a patch for each performance state attribute that was modified*/
            final ApplyPatchResult results = messageManager.applyPatchToMessages(scoreNode);
    
            /* Send this message because evaluator updates are always at 'now'
             * time */
            if (results.getCurrentMessage() != null) {
                sendMessage(results.getCurrentMessage().getMessage());
            }
            
            toReturn = results;
        }
        
        return toReturn;
    }

    /**
     * Calculates the assessment levels of any parent performance assessment nodes using the given 
     * condition assessments. To accomplish this, the assessment managager associated with this
     * playback service is used to emulate an assessment rollup.
     * 
     * @param conceptToConditionAssessments a mapping from each leaf concept to the assessments
     * of the conditions underneath it. The assessments themselves are assumed to have been provided
     * by an observer controller. Cannot be null.
     * @return the calculated assessment levels of the parent nodes. Can only be null if there is no
     * assessment manager to handle the calculation.
     */
    public Map<Integer, AssessmentLevelEnum> calculateRollUp(
            Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments) {
        
        /* Apply the update request to all of the learner states that are being updated */ 
        if(assessmentManager != null) {
            return assessmentManager.calculateRollUp(conceptToConditionAssessments);
        }
        
        return null;
    }
}