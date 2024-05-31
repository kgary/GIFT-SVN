/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DoubleValue;

import generated.proto.common.ProtobufLogMessageProto.ProtobufLogMessage;
import generated.proto.common.ProtobufMessageProto.ProtobufMessage;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.PublishLessonScore;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.aar.LogSpan;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.logger.MessageLogReader;
import mil.arl.gift.common.logger.ProtobufMessageLogReader;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.PerformanceStateAttributeDiff;
import mil.arl.gift.common.state.PerformanceStateAttributeDiff.PerformanceStateAttrFields;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.codec.proto.ProtobufMessageProtoCodec;

/**
 * Class that manages the log file playback messages and the patch file
 * messages.
 * 
 * @author sharrison
 */
public class LogFilePlaybackMessageManager {
    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(LogFilePlaybackMessageManager.class);

    /**
     * The minimum number of seconds that should be allowed between two entity
     * state updates for the same entity. This 'down-sampling' is done for
     * performance reasons in order to minimize the number of messages that are
     * held in memory ({@link #messages}).
     */
    private static final long MAX_SAMPLE_RATE_MS = 250;

    /** The extension for the log file patches */
    public static final String LOG_PATCH_EXTENSION = ".logPatch";

    /** The {@link List} of {@link Message} objects contained in the stream */
    private List<MessageManager> messages;

    /**
     * The {@link File} that contains the logged messages which should be played
     * back
     */
    private final File logFile;

    /**
     * The {@link LogSpan} that defines which messages from the {@link File}
     * should be played back
     */
    private final LogSpan logSpan;

    /**
     * A filter method that can be applied to the messages to decide whether or
     * not they should be sent to the {@link LogFilePlaybackTarget}
     */
    private final Predicate<Message> msgFilter;

    /** The username of the user playing the message back. */
    private final String username;

    /** The patch file containing the patch messages */
    private File patchFile;

    /** The DKF file containing scenario information for the log */
    private File dkf;
    
    /** The index of the next message within {@link #messages} to execute */
    private int currentMessageIndex = 0;

    /** contains all the message types that made it past the filters when reading the log */
    private Set<MessageTypeEnum> messageTypes = new HashSet<>();

    /** The manager that should be used to perform complex assessment operations.
     * This will be null if the log being processed does not have a DKF saved 
     * alongside it that can be used to extrapolate assessment rules. */
    private AbstractAarAssessmentManager assessmentManager;
    
    /**
     * Reads the session log file and any paths files.
     * @param logFile The {@link File} that contains the logged messages which
     *        should be played back. Can't be null.
     * @param logSpan The {@link LogSpan} that defines which messages from the
     *        {@link File} should be played back. Can't be null.
     * @param username The username that the playback should be associated with.
     * @param msgFilter A filter method that can be applied to the messages to
     *        decide whether or not they should be sent to the
     *        {@link LogFilePlaybackTarget}. Can't be null.
     * @param assessmentMgr an optional manager that can be used to provide additional
     *        information about a DKF scenario that is part of the playback. Can be null and 
     *        generally should be null for legacy log folders that do not have associated DKFs.
     * @throws Exception if there is a problem reading the log file.
     */
    public LogFilePlaybackMessageManager(File logFile, LogSpan logSpan, String username, Predicate<Message> msgFilter,
            AbstractAarAssessmentManager assessmentMgr)
            throws Exception {
        if (logFile == null) {
            throw new IllegalArgumentException("The parameter 'logFile' cannot be null.");
        } else if (logSpan == null) {
            throw new IllegalArgumentException("The parameter 'logSpan' cannot be null.");
        } else if (msgFilter == null) {
            throw new IllegalArgumentException("The parameter 'msgFilter' cannot be null.");
        }

        this.logFile = logFile;
        this.logSpan = logSpan;
        this.username = username;
        this.msgFilter = msgFilter;
        this.patchFile = new File(logFile.getAbsolutePath() + LOG_PATCH_EXTENSION);
        
        if(!this.patchFile.exists() && ProtobufMessageLogReader.isConvertedProtobufLogFile(logFile)) {
            
            /* Check if an existing log file for a legacy JSON log was recently updated to protobuf */
            File updatedPatchFile = new File(logFile.getAbsolutePath() + ProtobufMessageLogReader.PROTOBUF_LOG_FILE_EXTENSION + LOG_PATCH_EXTENSION);
            if(updatedPatchFile.exists()) {
                this.patchFile = updatedPatchFile;
            }
        }

        
        
        if(assessmentMgr != null) {
            
            /* Locate the DKF that the assessment manager is trying to use */
            this.dkf = new File(logFile.getParentFile(), assessmentMgr.getDkfFileName());
            
            try {
                
                /* Attempt to load the DKF into the assessment manager*/
                assessmentMgr.loadDkf(this.dkf);
                
                /* The DKF was found and loaded successfully, which means that rollup assessments can be
                 * performed by the assessment manager since the log being played back has a DKF in its log
                 * folder */
                this.assessmentManager = assessmentMgr;
                
            } catch (@SuppressWarnings("unused") FileNotFoundException e) {
                
                /* The log that's loaded is likely an older one that does not have a DKF saved alongside it, since
                 * no DKF file was found */
                logger.warn("Unable to load scenario information into assessment engine because the DKF, "
                        + dkf + " does not exist. This is likely a legacy log that does not have a DKF saved "
                        + "alongside it."); 
            }
        }

        readLogFile();
        readPatchFile();
    }

    /**
     * Clears all messages from memory
     */
    public void clearAll() {
        messages.clear();
    }

    /**
     * Reads the playback log file and parses the messages into
     * {@link #messages}. This overwrites the existing {@link #messages}.
     * 
     * @throws Exception if there is a problem reading the file.
     */
    public void readLogFile() throws Exception {
        /* Save a reference to the output and filter */
        try {
            MessageLogReader reader = MessageLogReader.createMessageLogReader(logFile.getName());
            final Set<SimpleEntry<Integer, String>> foundSourceEventIds = new HashSet<>();
            final Map<EntityIdentifier, Long> entityIdToLastTimeStamp = new HashMap<>();

            final int logStart = logSpan.getStart();
            final int logEnd = logSpan.getEnd();
            try (Stream<Message> messageStream = reader.streamMessages(new FileProxy(logFile), logStart, logEnd)) {
                final Set<Long> learnerStateTimestamps = new HashSet<>();
                /* Get the sorted list of messages by timestamp */
                messages = messageStream.filter(msg -> msg instanceof DomainSessionMessageEntry)
                        .map(msg -> (DomainSessionMessageEntry) msg).filter(msg -> foundSourceEventIds
                                .add(new SimpleEntry<>(msg.getSourceEventId(), msg.getSenderAddress())))
                        // down sample the high frequency entity state messages until we can stream messages from disk
                        // instead of keeping them all in memory
                        .filter(msg -> {
                            final long timeStamp = msg.getTimeStamp();
                            if (msg.getMessageType().equals(MessageTypeEnum.ENTITY_STATE)) {
                                EntityState es = (EntityState) msg.getPayload();
                                final EntityIdentifier entityID = es.getEntityID();
                                final Long lastReportedTime = entityIdToLastTimeStamp.get(entityID);
                                if (lastReportedTime == null || timeStamp - lastReportedTime > MAX_SAMPLE_RATE_MS) {
                                    // allow this message as it has been long enough between entity states                                    
                                    entityIdToLastTimeStamp.put(entityID, timeStamp);
                                    return true;
                                } else {
                                    // drop this message from playback
                                    return false;
                                }
                            }

                            return true;
                        }).filter(msgFilter)
                        .peek(msg -> {
                            messageTypes.add(msg.getMessageType()); // collect all message types that made it this far in the filtering
                            msg.replaceUsername(username);
                        })
                        .sorted((o1, o2) -> Long.compare(o1.getTimeStamp(), o2.getTimeStamp()))
                        .peek(msg -> {
                            if (msg.getMessageType() != MessageTypeEnum.LEARNER_STATE) {
                                return;
                            }

                            /* Guarantee unique timestamp for learner states */
                            long timestamp = msg.getTimeStamp();
                            while (learnerStateTimestamps.contains(timestamp)) {
                                msg.setTimeStamp(timestamp + 1);
                                timestamp = msg.getTimeStamp();
                            }

                            learnerStateTimestamps.add(msg.getTimeStamp());

                            /* Sort again after this */
                        }).sorted((o1, o2) -> Long.compare(o1.getTimeStamp(), o2.getTimeStamp()))
                        .map(msg -> new MessageManager(msg)).collect(Collectors.toList());
            }

            if (messages.isEmpty()) {
                throw new Exception("Failed to find any messages to playback between the index " + logSpan.getStart()
                        + " and " + logSpan.getEnd() + " in " + logFile.getAbsolutePath() + ".");
            }
        } catch (IOException ioEx) {
            logger.error("There was a problem parsing the messages in the log " + logFile.getAbsolutePath(), ioEx);
            throw ioEx;
        }
    }

    /**
     * Whether the playback log contains a team knowledge session or not.<br/>
     * A team knowledge session is one that uses a {@link MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST}.
     * @return true if the log is going to be playing back a team knowledge session, false otherwise.
     */
    public boolean isTeamSession(){
        return messageTypes.contains(MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST);
    }
    
    /**
     * Get the absolute path to the folder containing the log file being used in this manager.
     * @return the absolute path to the parent folder of this log file, 
     * e.g. E:\work\trunk\GIFT\output\domainSessions\domainSession1347_uId1
     */
    public String getLogFileFolder(){
        return logFile.getParentFile().getAbsolutePath();
    }
    
    /**
     * Get the name of the log file used in this manager.
     * @return the log file name, e.g. domainSession1347_uId1_2021-11-10_09-29-59.protobuf.bin
     */
    public String getLogFileName(){
        return logFile.getName();
    }

    /**
     * Read the patch file (if it exists) and update the {@link #messages}.
     */
    private void readPatchFile() {
        /* Nothing to read */
        if (!hasPatch()) {
            return;
        }

        final MessageLogReader reader = MessageLogReader.createMessageLogReader(patchFile.getName());
        try (final Stream<Message> patchMsgStream = reader.streamMessages(new FileProxy(patchFile))
                    .filter(msg -> msg instanceof DomainSessionMessageEntry)) {

            final ListIterator<MessageManager> origMsgItr = messages.listIterator();
            final ListIterator<Message> patchMsgItr = patchMsgStream.collect(Collectors.toList()).listIterator();

            /* Need to apply the patches after we finish the message loop to
             * avoid a ConcurrentModificationException */
            final List<ApplyPatchRequest> patchesToApply = new ArrayList<>();
            final List<GradedScoreNode> scorePatchesToApply = new ArrayList<>();

            /* Loop over the messages list so we can insert the patches in order
             * (by time) */
            while (origMsgItr.hasNext()) {
                final MessageManager origMsg = origMsgItr.next();

                /* Short circuit the loop; merge complete. */
                if (!patchMsgItr.hasNext()) {
                    break;
                    }

                /* Loop over the patch messages until it no longer belongs at
                 * this position in the primary list */
                while (patchMsgItr.hasNext()) {
                    final DomainSessionMessageEntry patchMsg = (DomainSessionMessageEntry) patchMsgItr.next();
                    final long patchMsgTime = patchMsg.getTimeStamp();

                    final int timeCompareResult = Long.compare(patchMsgTime, origMsg.getTimeStamp());
                    if (timeCompareResult == 0 && origMsg.getMessageType().equals(patchMsg.getMessageType())) {
                        /* Found a patch message that has the same timestamp and
                         * message type as the original message */
                        
                        if(MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST.equals(origMsg.getMessageType())){
                            
                            /* This is a graded score patch, so need to apply it as such */
                            GradedScoreNode score = MessageManager.getScore(patchMsg);
                            
                            GradedScorePatchState patch = new GradedScorePatchState(origMsg.getOriginalMessage().getTimeStamp());
                            patch.updatePatchedScore(score);
                            patch.applyPatch(origMsg);
                            
                            scorePatchesToApply.add(score);
                            
                        } else {

                            /* Apply performance node state patches */
                            final Map<PerformanceStateAttribute, PatchedState> patches = findPatchedStates(origMsg,
                                    patchMsg);
                            if (!patches.isEmpty()) {
                                
                                /* Bundle all of the patches made to each performance node in this message */
                                Set<PerformanceStateAttribute> requestPatches = new HashSet<>();
                                for (Entry<PerformanceStateAttribute, PatchedState> entry : patches.entrySet()) {
                                    final PatchedState patch = entry.getValue();
                                    patch.applyPatch(origMsg);
                                    requestPatches.add(entry.getKey());
                                }
                                
                                ApplyPatchRequest patchRequest = new ApplyPatchRequest(requestPatches,
                                        origMsg.getTimeStamp());
                                patchRequest.setUpdateEntireSpan(false);
                                patchesToApply.add(patchRequest);
                            }
                        }
                    } else if (timeCompareResult < 0) {
                        /* Patch message is earlier than the original message.
                         * Insert it before the original message. */

                        /* Need to find the first previous message of the same
                         * type as the patch */
                        int backIndex = 0;
                        while (origMsgItr.hasPrevious()) {
                            backIndex++;
                            final MessageManager prevMessage = origMsgItr.previous();
                            if (prevMessage.getMessageType().equals(patchMsg.getMessageType())) {
                                final MessageManager newManager = new MessageManager(MessageManager
                                        .createNewMessage(patchMsgTime, prevMessage.getOriginalMessage()));
                                final Map<PerformanceStateAttribute, PatchedState> patches = findPatchedStates(newManager,
                                        patchMsg);

                                /* Now that we found the last message of the
                                 * same type, return to the spot right before
                                 * the original iterator position */
                                for (int i = backIndex; i > 1; i--) {
                                    origMsgItr.next();
                                }

                                /* Add the message right before the original
                                 * message and then return the cursor back to
                                 * the correct position */
                                origMsgItr.add(newManager);
                                origMsgItr.next();

                                /* Apply patches */
                                if (!patches.isEmpty()) {
                                    
                                    /* Bundle all of the patches made to each performance node in this message */
                                    Set<PerformanceStateAttribute> requestPatches = new HashSet<>();
                                    for (Entry<PerformanceStateAttribute, PatchedState> entry : patches.entrySet()) {
                                        final PatchedState patch = entry.getValue();
                                        patch.applyPatch(newManager);
                                        requestPatches.add(entry.getKey());
                                    }
                                    
                                    ApplyPatchRequest patchRequest = new ApplyPatchRequest(requestPatches,
                                            origMsg.getTimeStamp());
                                    patchRequest.setUpdateEntireSpan(false);
                                    patchesToApply.add(patchRequest);
                                }

                                /* Reset index so we know we already returned to
                                 * position */
                                backIndex = 0;
                break;
            }
        }

                        /* Return to the original position. This should already
                         * have been done in the above while loop, but if no
                         * previous message matched the message type, then we
                         * would have returned all the way to the beginning of
                         * the list and we will need to return to the original
                         * index. */
                        for (int i = backIndex; i > 0; i--) {
                            origMsgItr.next();
                        }
                    } else {
                        /* Reverse the patch list by 1 since we never used this
                         * message and go to the next original message in the
                         * list */
                        patchMsgItr.previous();
                        break;
                    }
            }
        }

            /* Now we can apply the saved performance node state patches */
            for (ApplyPatchRequest patchRequest : patchesToApply) {
                applyPatchToMessages(patchRequest);
            }
            
            /* Apply graded score patches separately */
            for (GradedScoreNode scorePath : scorePatchesToApply) {
                applyPatchToMessages(scorePath);
            }

            /* Add any remaining patch messages to the end of the original
             * list */
            if (patchMsgItr.hasNext()) {
                final MessageManager lastMessage = messages.get(messages.size() - 1);
                patchMsgItr.forEachRemaining(msg -> {
                    final DomainSessionMessageEntry patchMsg = (DomainSessionMessageEntry) msg;

                    final MessageManager newManager = new MessageManager(
                            MessageManager.createNewMessage(patchMsg.getTimeStamp(), lastMessage.getOriginalMessage()));
                    messages.add(newManager);

                    /* Apply patches */
                    final Map<PerformanceStateAttribute, PatchedState> patches = findPatchedStates(newManager,
                            patchMsg);
                    if (!patches.isEmpty()) {
                        
                        /* Bundle all of the patches made to each performance node in this message */
                        Set<PerformanceStateAttribute> requestPatches = new HashSet<>();
                        for (Entry<PerformanceStateAttribute, PatchedState> entry : patches.entrySet()) {
                            final PatchedState patch = entry.getValue();
                            patch.applyPatch(newManager);
                            requestPatches.add(entry.getKey());
                        }
                        
                        ApplyPatchRequest patchRequest = new ApplyPatchRequest(requestPatches,
                                newManager.getTimeStamp());
                        patchRequest.setUpdateEntireSpan(false);
                        applyPatchToMessages(patchRequest);
                    }
                });
            }
        } catch (IOException ioEx) {
            throw new DetailedException("Failed to retrieve the patch file contents.",
                    "There was a problem fetching the contents for a domain session log patch.", ioEx);
        }
    }

    /**
     * Find the patched {@link PerformanceStateAttribute}s by diff-ing the
     * source message and the provided patch message.
     *
     * @param source the source message to compare against. If null, no patches
     *        are returned.
     * @param patch the patch message containing the differences to find. If
     *        null, no patches are returned.
     * @return the mapping of modified {@link PerformanceStateAttribute} to
     *         their patch state. Can be empty, will never be null.
     */
    private Map<PerformanceStateAttribute, PatchedState> findPatchedStates(MessageManager source, DomainSessionMessageEntry patch) {
        final Map<PerformanceStateAttribute, PatchedState> patches = new HashMap<>();
        if (source == null || patch == null) {
            return patches;
        } else if (!source.getMessageType().equals(patch.getMessageType())) {
            return patches;
        }

        final PerformanceState patchState = MessageManager.getLearnerPerformanceState(patch);
        if (patchState != null) {
            for (TaskPerformanceState patchTask : patchState.getTasks().values()) {
                source.findDifferentStates(patchTask, patches);
            }
        }

        return patches;
    }

    /**
     * Retrieve the list of playback messages
     * 
     * @return the playback messages
     */
    public List<MessageManager> getMessages() {
        return messages;
    }

    /**
     * Getter for the current message specified by {@link #currentMessageIndex}.
     * Automatically performs bound checks on {@link #messages}.
     *
     * @return The current {@link DomainSessionMessageEntry} or null if there is
     *         no current message.
     */
    public MessageManager getCurrentMessageManager() {
        return currentMessageIndex < messages.size() ? messages.get(currentMessageIndex) : null;
    }

    /**
     * Getter for the current message specified by {@link #currentMessageIndex}.
     * Automatically performs bound checks on {@link #messages}.
     *
     * @return The current {@link DomainSessionMessageEntry} or null if there is
     *         no current message.
     */
    public DomainSessionMessageEntry getCurrentMessage() {
        return currentMessageIndex < messages.size() ? messages.get(currentMessageIndex).getMessage() : null;
    }

    /**
     * Getter for the message specified by the provided index. Automatically
     * performs bound checks on {@link #messages}.
     * 
     * @param index the index of the message to retrieve.
     * @return The {@link DomainSessionMessageEntry} at the specified index or
     *         null if the index is out of bounds.
     */
    public DomainSessionMessageEntry getMessage(int index) {
        if (index < 0 || index >= messages.size()) {
            return null;
        }

        return messages.get(index).getMessage();
    }

    /**
     * Getter for the index of the current message pointer
     * 
     * @return the current message index
     */
    public int getCurrentMessageIndex() {
        return currentMessageIndex;
    }

    /**
     * Searches for the latest messages of provided types that came at or before
     * the messaged referenced by {@link #currentMessageIndex}.
     *
     * @param types The {@link MessageTypeEnum} of the messages to search for.
     *        Can't be empty or null.
     * @return A mapping between the {@link MessageTypeEnum} of the desired
     *         message and the {@link Message} that latest message that was
     *         found. No mapping will exist for a {@link Message} if no message
     *         of that type was found before the current
     *         {@link #currentMessageIndex} value.
     */
    public Map<MessageTypeEnum, DomainSessionMessageEntry> getLatestMessagesOfType(MessageTypeEnum... types) {
        Map<MessageTypeEnum, DomainSessionMessageEntry> toRet = new HashMap<>();

        int remainingTypeCount = types.length;
        /* Search at the current index and earlier since the current index
         * points to a message that has already been sent */
        for (int i = currentMessageIndex; i >= 0; i--) {
            final DomainSessionMessageEntry currMsg = messages.get(i).getMessage();

            for (MessageTypeEnum type : types) {
                /* If we've already found a message of this type, skip to the
                 * next type. */
                if (toRet.containsKey(type)) {
                    continue;
                }

                /* If the message is of this type, put it in the map */
                if (currMsg.getMessageType() == type) {
                    /* Check if the learner state is a 'visual only' state (null
                     * assessment). If yes, do not count it as a true learner
                     * state. */
                    if (type == MessageTypeEnum.LEARNER_STATE) {
                        LearnerState learnerState = (LearnerState) currMsg.getPayload();
                        boolean skipMsg = learnerState.getPerformance().getTasks().values().stream()
                                .flatMap(task -> task.getConcepts().stream()).anyMatch(
                                        concept -> concept.getState().getAssessedTeamOrgEntities().containsValue(null));
                        if (skipMsg) {
                            continue;
                        }
                    }

                    toRet.put(type, currMsg);
                    remainingTypeCount--;
                }
            }

            if (remainingTypeCount == 0) {
                break;
            }
        }

        return toRet;
    }

    /**
    * Searches for future messages of provided types that came at or before
    * the messaged referenced by {@link #currentMessageIndex}.
    *
    * @param types The {@link MessageTypeEnum} of the messages to search for.
    *        Can't be empty or null.
    * @return A mapping between the {@link MessageTypeEnum} of the desired
    *         message and the {@link Message} that earliest message that was
    *         found. No mapping will exist for a {@link Message} if no message
    *         of that type was found after the current
    *         {@link #currentMessageIndex} value.
    */
   public Map<MessageTypeEnum, List<DomainSessionMessageEntry>> getFutureMessagesOfType(MessageTypeEnum... types) {
       Map<MessageTypeEnum, List<DomainSessionMessageEntry>> toRet = new HashMap<>();

       int messageCount = messages.size();
       /* Search at the current index and later since the current index
        * points to a message that has already been sent */
       for (int i = currentMessageIndex; i < messageCount; i++) {
           final DomainSessionMessageEntry currMsg = messages.get(i).getMessage();

           for (MessageTypeEnum type : types) {

               /* If the message is of this type, put it in the map */
               if (currMsg.getMessageType() == type) {
                   /* Check if the learner state is a 'visual only' state (null
                    * assessment). If yes, do not count it as a true learner
                    * state. */
                   if (type == MessageTypeEnum.LEARNER_STATE) {
                       LearnerState learnerState = (LearnerState) currMsg.getPayload();
                       boolean skipMsg = learnerState.getPerformance().getTasks().values().stream()
                               .flatMap(task -> task.getConcepts().stream()).anyMatch(
                                       concept -> concept.getState().getAssessedTeamOrgEntities().containsValue(null));
                       if (skipMsg) {
                           continue;
                       }
                   }
                   
                   List<DomainSessionMessageEntry> typeMessages = toRet.get(type);
                   if(typeMessages == null) {
                       typeMessages = new ArrayList<>();
                       toRet.put(type, typeMessages);
                   }
                   
                   typeMessages.add(currMsg);
               }
           }
       }

       return toRet;
   }

    /**
     * Advances the {@link #currentMessageIndex} to the next {@link Message}. If
     * none of the next messages match the filter, the
     * {@link #currentMessageIndex} is set to the value of {@link #messages}'s
     * {@link List#size()} method.
     *
     * @return The {@link DomainSessionMessageEntry} that has been arrived at
     *         after fast-forwarding. If no valid message was reached, null is
     *         returned.
     */
    public DomainSessionMessageEntry fastForwardToNextValidMessage() {
        currentMessageIndex++;
        return getCurrentMessage();
    }

    /**
     * Advances the {@link #currentMessageIndex} to the next {@link Message}
     * that matches the provided message filter. If none of the next messages
     * match the filter, the {@link #currentMessageIndex} is set to the value of
     * {@link #messages}'s {@link List#size()} method.
     *
     * @param msgFilter An additional filter to apply when determining the next
     *        available {@link Message}.
     * @return The {@link DomainSessionMessageEntry} that has been arrived at
     *         after fast-forwarding. If no valid message was reached, null is
     *         returned.
     */
    public synchronized MessageManager fastForwardToNextValidMessageManager(Predicate<MessageManager> msgFilter) {
        /* If current message index is 0; process the first message before
         * looping */
        if (currentMessageIndex == 0) {
            final MessageManager msg = getCurrentMessageManager();
            if (msg != null && msgFilter.test(msg)) {
                return msg;
            }
        }

        while (++currentMessageIndex < messages.size()) {
            final MessageManager msg = getCurrentMessageManager();
            if (msg != null && msgFilter.test(msg)) {
                return msg;
            }
        }

        return null;
    }

    /**
     * Set the {@link #currentMessageIndex} to the message at the provided
     * index.
     * 
     * @param index the value to use to reset the message index.
     */
    public void resetMessageIndexToIndex(int index) {
        if (index < 0) {
            currentMessageIndex = 0;
        } else {
            currentMessageIndex = index;
        }
    }

    /**
     * Set the {@link #currentMessageIndex} to the message at the provided
     * timestamp.
     * 
     * @param timestamp the timestamp to use to reset the message index.
     */
    public void resetMessageIndexToTimestamp(long timestamp) {
        /* Since we have changed the number of messages; reset
         * currentMessageIndex to be where the current timestamp is */
        int newIndex = 0;
        for (int i = 0; i < messages.size(); i++) {
            long msgTimestamp = messages.get(i).getTimeStamp();
            if (Long.compare(msgTimestamp, timestamp) > 0) {
                newIndex = i - 1;
                break;
            }
        }
        currentMessageIndex = newIndex >= 0 ? newIndex : 0;
    }

    /**
     * Checks if there is an existing patch file.
     * 
     * @return true if the patch file exists; false otherwise.
     */
    public boolean hasPatch() {
        return patchFile != null && patchFile.exists();
    }

    /**
     * Retrieves the patch file.
     * 
     * @return the patch file. Does not guarantee that the file exists.
     */
    public File getPatchFile() {
        return patchFile;
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
        final List<DomainSessionMessage> patchedMessageList = new ArrayList<>();
        messages.forEach(msg -> {
            if (!msg.isPatched()) {
                return;
            }
            
            final Set<String> uniquePatchIds = new HashSet<>();

            /* Find out if these patches are new */
            boolean foundNewId = false;
            for (PatchedState patch : msg.getPatchedStates()) {
                if (!uniquePatchIds.contains(patch.getId())) {
                    foundNewId = true;
                }
                uniquePatchIds.add(patch.getId());
            }

            /* Found the first instance of a patch. Write it to the patch
             * file. */
            if (foundNewId) {
                patchedMessageList.add(convertToDomainSessionMessage(msg.getMessage()));
            }
        });

        if (patchedMessageList.isEmpty()) {
            deleteLogPatchFile();
            return null;
        }
        
        if(!ProtobufMessageLogReader.isProtobufLogFile(patchFile.getName())) {
            
            /* 
             * The existing patch file is a legacy patch file written in JSON. 
             * 
             * All new patches need to be written in Protobuf to avoid compatibility issues, so we 
             * need to make a new Protobuf patch file and save the old patch to a backup file 
             */
            File oldPatch = this.patchFile;
            if(oldPatch.exists()) {
                Path renamedOldPatch = oldPatch.toPath().resolveSibling(oldPatch.getName() + Constants.BACKUP_SUFFIX);
                try {
                    Files.move(oldPatch.toPath(), renamedOldPatch);
                    
                } catch (IOException e) {
                    throw new DetailedException("Unable to update patch file.", "A legacy patch file written in JSON could"
                            + "not be replaced with a newer patch.", e);
                }
            }
            
            this.patchFile = new File(logFile.getAbsolutePath() + ProtobufMessageLogReader.PROTOBUF_LOG_FILE_EXTENSION + LOG_PATCH_EXTENSION);
        }

        /* Write the new learner state to the patch file */
        try (FileOutputStream outputStream = new FileOutputStream(patchFile)) {
            
            ProtobufMessageProtoCodec codec = new ProtobufMessageProtoCodec();
            
            for (DomainSessionMessage msg : patchedMessageList) {
                
                ProtobufMessage protoObj = codec.map(msg);
                
                ProtobufLogMessage logMessage = ProtobufLogMessage.newBuilder().setMessage(protoObj)
                        .setElapsedDsTime(DoubleValue.of(Double.valueOf(TimeUtil.getDefaultRelativeTime())))
                        .setElapsedWriteTime(DoubleValue.of(Double.valueOf(TimeUtil.getDefaultRelativeTime()))).build();
                
                logMessage.writeDelimitedTo(outputStream);
            }
            
            return patchFile.getName();
        } catch (FileNotFoundException e) {
            throw new DetailedException("File not found.", "The patch file was not found.", e);
        } catch (IOException e) {
            throw new DetailedException("Error writing the patch file.", "The patch file was not able to be written. User: "+username,
                    e);
        }
    }

    /**
     * Delete the log patch file.
     */
    public void deleteLogPatchFile() {
        if (patchFile.exists()) {
            patchFile.delete();
        }
    }

    /**
     * Converts a {@link DomainSessionMessageEntry} to a
     * {@link DomainSessionMessage}.
     *
     * @param msg The {@link DomainSessionMessageEntry} to convert. Can't be
     *        null.
     * @return The newly created message. Can't be null.
     */
    private DomainSessionMessage convertToDomainSessionMessage(DomainSessionMessageEntry msg) {
        UserSession uSession = new UserSession(msg.getUserId());
        uSession.setUsername(msg.getUsername());
        uSession.setExperimentId(msg.getExperimentId());
        return new DomainSessionMessage(msg.getMessageType(), msg.getSequenceNumber(), msg.getSourceEventId(),
                msg.getTimeStamp(), msg.getSenderModuleName(), msg.getSenderAddress(), msg.getSenderModuleType(),
                msg.getDestinationQueueName(), msg.getPayload(), uSession, msg.getDomainSessionId(),
                msg.needsHandlingResponse());
    }

    /**
     * This method deletes the provided performance state. This will affect
     * messages containing the same performance attribute at and after the
     * provided timestamp.
     * 
     * @param deletionTimestamp the timestamp of the deleted performance state.
     * @param perfStateToDelete the modified performance state to delete from
     *        the messages.
     * @return the updated message at the provided timestamp. Can be null if no
     *         message was updated.
     */
    public ApplyPatchResult removeLogPatchForAttribute(long deletionTimestamp,
            PerformanceStateAttribute perfStateToDelete) {
        if (perfStateToDelete == null) {
            return null;
        }

        final ApplyPatchResult toRet = new ApplyPatchResult();
        final String perfStateName = perfStateToDelete.getName();

        /* Find the message that the timestamp matches. Keep in mind that this
         * timestamp is from the client timeline, so it could have been adjusted
         * from 'fetchLearnerStates'. The message managers should already have
         * accounted for this. */
        final List<MessageManager> matchedMessages = messages.stream().filter(msg -> {
            return Long.compare(deletionTimestamp, msg.getTimeStamp()) == 0
                    && msg.hasPerformanceStateWithName(perfStateName);
        }).collect(Collectors.toList());

        if (matchedMessages.isEmpty()) {
            return null;
        } else if (matchedMessages.size() > 1) {
            logger.warn("Found " + matchedMessages.size() + " messages at timestamp " + deletionTimestamp
                    + " containing the performance state attribute '" + perfStateName + "'.");
        }

        /* This is the timestamp we can use to look up the patches */
        final MessageManager matchedMessage = matchedMessages.get(0);

        PatchedState patchToRemove = null;
        for (PatchedState messagePatch : matchedMessage.getPatchedStates()) {
            if (StringUtils.equals(messagePatch.getId(), PerformancePatchState.buildUniquePatchKey(perfStateName,
                    matchedMessage.getOriginalMessage().getTimeStamp()))) {
                patchToRemove = messagePatch;
                break;
            }
        }

        if (patchToRemove == null) {
        return null;
    }

        final PatchedState finalizedPatchToRemove = patchToRemove;
        messages.forEach(msg -> {
            if (msg.removePatchedState(finalizedPatchToRemove)) {
                /* The first message containing the removed patch is the current
                 * message */
                if (toRet.getCurrentMessage() == null) {
                    toRet.setCurrentMessage(msg);
            }
                toRet.getAffectedMessages().add(msg);
        }
        });

        return toRet;
    }

    /**
     * Edits an existing patch with the changes in the request.
     * 
     * @param patchRequest the request containing the patched
     *        {@link PerformanceStateAttribute}. Can't be null.
     * @return the {@link ApplyPatchResult} containing the current and affected
     *         messages from this edit. Can be null.
     */
    public ApplyPatchResult editPatch(ApplyPatchRequest patchRequest) {
        final ApplyPatchResult toRet = new ApplyPatchResult();
        final Set<PerformanceStateAttribute> patchedPerformanceState = patchRequest.getPatchedPerformanceState();

        /* Find the message that the patch request timestamp matches. Keep in
         * mind that this timestamp is from the client timeline, so it could
         * have been adjusted from 'fetchLearnerStates'. The message manager's
         * should already have accounted for this. */
        final List<MessageManager> matchedMessages = messages.stream().filter(msg -> {
            for(PerformanceStateAttribute attr : patchedPerformanceState) {
                if(msg.hasPerformanceStateWithName(attr.getName())) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());

        if (matchedMessages.isEmpty()) {
            return null;
        } else if (matchedMessages.size() > 1) {
            logger.warn("Found " + matchedMessages.size() + " messages at timestamp " + patchRequest.getTimestamp()
                    + " containing the performance states" + patchedPerformanceState + "'.");
        }

        /* This is the timestamp we can use to look up the patches */
        final MessageManager matchedMessage = matchedMessages.get(0);

        /* Edit all of the existing patches for each performance node as part of a single bundle of patches */
        List<PerformancePatchState> patchesToEdit = new ArrayList<>();
        for(PerformanceStateAttribute patchedAttr : patchedPerformanceState) {
            
            PerformancePatchState patchToEdit = null;
            for (PatchedState messagePatch : matchedMessage.getPatchedStates()) {
            
                if (messagePatch instanceof PerformancePatchState &&
                        StringUtils.equals(messagePatch.getId(), PerformancePatchState.buildUniquePatchKey(patchedAttr.getName(),
                        matchedMessage.getOriginalMessage().getTimeStamp()))) {
                    
                    patchToEdit = (PerformancePatchState) messagePatch;
                    break;
                }
            }
            
            if (patchToEdit == null) {
                return null;
            } else {
                
                /* Add this edit to the bundle */
                patchesToEdit.add(patchToEdit);
            }
            
            /* Make edit changes to patch */
            final Set<PerformanceStateAttrFields> diffFields = new HashSet<>();
            AbstractPerformanceState perfState = MessageManager.findPerformanceStateByName(matchedMessage.getMessage(), patchedAttr.getName());
            if (perfState != null 
                    && PerformanceStateAttributeDiff.performDiff(patchedAttr, perfState.getState(), diffFields, false)) {
                patchToEdit.updatePatchedFields(patchedAttr, diffFields);
            }
        }
        
        if(patchesToEdit.isEmpty()) {
            return null;
        }

        /* Refresh the messages with this bundle of patches */
        final List<PerformancePatchState> finalizedPatchToEdit = patchesToEdit;
        messages.forEach(msg -> {
            for(PatchedState state : finalizedPatchToEdit) {
                if (msg.hasPatchedState(state)) {
                    msg.refresh();
    
                    /* The first message containing the edited patch is the current
                     * message */
                    if (toRet.getCurrentMessage() == null) {
                        toRet.setCurrentMessage(msg);
                    }
                    toRet.getAffectedMessages().add(msg);
                }
            }
        });

        return toRet;
    }

    /**
     * This method applies the patched performance state attribute into the
     * messages.
     * 
     * @param patchRequest the request properties to apply the patch. Can't be
     *        null.
     * @return the patch injection results.
     */
    public ApplyPatchResult applyPatchToMessages(ApplyPatchRequest patchRequest) {
        final ApplyPatchResult toRet = new ApplyPatchResult(); 

        final Set<PerformanceStateAttribute> patchedAttr = patchRequest.getPatchedPerformanceState();


        MessageManager closestMessage = null;
        List<PerformanceStateAttribute> closestMessageAttr = null;
        List<PatchedState> patchToApply = null;

        final List<MessageManager> msgSpan = new ArrayList<>();

        /* Need to store and insert any new message later after we are finished
         * using the messages iterator so we don't cause a
         * ConcurrentModificationException. */
        MessageManager newMessage = null;

        /* Filter messages on only the ones that have the patched performance
         * states */
        final ListIterator<MessageManager> itr = messages.stream().filter(msg -> {
            
            for(PerformanceStateAttribute attr : patchedAttr) {
                if(msg.hasPerformanceStateWithName(attr.getName())) {
                    return true;
                }
            }
            
            return false;
        }).collect(Collectors.toList()).listIterator();

        /* Loop over messages - before/at timestamp */
        while (itr.hasNext()) {
            final MessageManager currentMsg = itr.next();
            final int timeCompareResult = Long.compare(currentMsg.getOriginalMessage().getTimeStamp(), patchRequest.getTimestamp());

            /* Exit case */
            if (timeCompareResult > 0) {
                /* Move backward one so we don't skip this message. Returns
                 * currentMsg again */
                itr.previous();

                /* Returns the message before currentMsg */
                if (itr.hasPrevious()) {
                    closestMessage = itr.previous();
                    
                    closestMessageAttr = new ArrayList<>();
                    
                    /* Bundle all of the patches made to each performance node in this message */
                    patchToApply = new ArrayList<>();
                    for(PerformanceStateAttribute attr : patchedAttr) {
                        
                        AbstractPerformanceState perfState = MessageManager
                                .findPerformanceStateByName(closestMessage.getMessage(), attr.getName());
                        
                        if(perfState == null) {
                            continue;
                        }
                        
                        PerformanceStateAttribute closest = perfState.getState().deepCopy();
                        
                        closestMessageAttr.add(closest);
                        
                        /* Build the patch to be applied */
                        final PerformancePatchState newPatch = buildPatchState(closest, attr,
                                patchRequest.isUpdateEntireSpan() ? msgSpan.get(0).getOriginalMessage().getTimeStamp()
                                        : patchRequest.getTimestamp());
                        if (newPatch != null) {
                            patchToApply.add(newPatch);
                    }
                    }

                    /* Bring the cursor back to the original position */
                    itr.next();
                }
    
                break;
            }

            /* Collect all the messages that span together in the timeline
             * (merge to form 1 rectangle). We only care about past and current
             * messages here since future messages will be updated later on. */
            if (patchRequest.isUpdateEntireSpan()) {
                if (!msgSpan.isEmpty()) {
                    
                    for(PerformanceStateAttribute attr : patchedAttr) {
                        if (!isMessagePartOfSpan(msgSpan.get(0), currentMsg, attr.getName())) {
                            /* Broke the span, restart */
                            msgSpan.clear();
                            break;
                        }
                    }
                }
                msgSpan.add(currentMsg);
            }

            if (timeCompareResult == 0) {
                /* We found the current message */
                toRet.setCurrentMessage(currentMsg);
                            }

            if(!itr.hasNext()) {

                /* If the current message is the last message in the log, then we need to start and end 
                 * the span here at this message.
                 * 
                 * Failing to do this causes a problem where any patches that modify the last message
                 * will be incorrectly applied to the 2nd-to-last message instead */
                closestMessage = currentMsg;
                closestMessageAttr = new ArrayList<>();
                
                /* Bundle all of the patches made to each performance node in this message */
                patchToApply = new ArrayList<>();
                for(PerformanceStateAttribute attr : patchedAttr) {
                    
                    PerformanceStateAttribute closest = MessageManager
                            .findPerformanceStateByName(closestMessage.getMessage(), attr.getName()).getState()
                            .deepCopy();
                    
                    closestMessageAttr.add(closest);
                    
                    /* Build the patch to be applied */
                    final PerformancePatchState newPatch = buildPatchState(closest, attr,
                            patchRequest.isUpdateEntireSpan() ? msgSpan.get(0).getOriginalMessage().getTimeStamp()
                                    : patchRequest.getTimestamp());
                    if (newPatch != null) {
                        patchToApply.add(newPatch);
                }
            }
        }
        }

        /* Can't process the patch without something to compare against */
        if (patchToApply == null || closestMessage == null || closestMessageAttr == null) {
            return toRet;
                        }

        /* Update message span or update at timestamp */
        if (patchRequest.isUpdateEntireSpan()) {
            for (MessageManager msg : msgSpan) {
                final boolean changed = msg.addPatchedStates(patchToApply);
                if (changed) {
                    toRet.getAffectedMessages().add(msg);
                }
            }

            /* Did not find a message at timestamp; use the closest message
             * (which should also be the last message in the span) as the
             * "current" message since it's the best we have. */
            if (toRet.getCurrentMessage() == null) {
                toRet.setCurrentMessage(closestMessage);
                        }
        } else {
            if (toRet.getCurrentMessage() != null) {
                /* Found message at timestamp; update it with the patch */
                final boolean changed = toRet.getCurrentMessage().addPatchedStates(patchToApply);
                if (changed) {
                    toRet.getAffectedMessages().add(toRet.getCurrentMessage());
                        }
            } else {

                /* Did not find a message at timestamp; create new with patch
                 * applied. The original message will be the same as the
                 * previous */
                newMessage = new MessageManager(MessageManager.createNewMessage(patchRequest.getTimestamp(),
                        closestMessage.getOriginalMessage()));

                /* Copy the patches from the closest message into the new message. This will
                 * ensure that any existing changes that were patched in will not be lost. */
                newMessage.addPatchedStates(closestMessage.getPatchedStates());

                final boolean changed = newMessage.addPatchedStates(patchToApply);
                        
                toRet.setCurrentMessage(newMessage);
                if (changed) {
                    toRet.getAffectedMessages().add(newMessage);
                    }
                }

            /* Since we are not updating the entire span, populate it with the
             * message at the current timestamp since this is a new span
             * starting with itself. */
            msgSpan.clear();
            msgSpan.add(toRet.getCurrentMessage());
        }

        /* Continue looping over messages - after timestamp */
        while (itr.hasNext()) {
            final MessageManager msg = itr.next();
            
            /* Bundle all of the patches made to each performance node in this message */
            for(PerformanceStateAttribute attr : patchedAttr) {
                final PerformanceStateAttribute currentMsgAttr = MessageManager
                        .findPerformanceStateByName(msg.getMessage(), attr.getName()).getState();
    
                /* When the node states change, this breaks the flow */
                if (CollectionUtils.isNotEmpty(closestMessageAttr) 
                        && closestMessageAttr.get(0).getNodeStateEnum() != currentMsgAttr.getNodeStateEnum()) {
                    break;
                }
    
                /* When the original assessments change, this breaks the flow if
                 * hold is not toggled */
                if (isMessagePartOfSpan(msgSpan.get(0), msg, attr.getName())) {
                    /* Still in the flow. Update everything from the patch. */
                    msg.addPatchedStates(patchToApply);
                    toRet.getAffectedMessages().add(msg);
                        } else {
                    /* Flow is broken; exit */
                    break;
                }
            }
        }

        /* Now that the messages iterator is complete, we can call the list
         * iterator again to insert the new message */
        if (newMessage != null) {
            ListIterator<MessageManager> messagesItr = messages.listIterator();
            while (messagesItr.hasNext()) {
                if (Long.compare(messagesItr.next().getTimeStamp(), patchRequest.getTimestamp()) > 0) {
                    messagesItr.previous();
                    messagesItr.add(newMessage);
                    break;
                }
            }
        }

        return toRet;
    }
    
    /**
     * This method applies the patched graded score node into the
     * messages.
     * 
     * @param patchedScore the patched score node to apply. Can't be null.
     * @return the patch injection results.
     */
    public ApplyPatchResult applyPatchToMessages(GradedScoreNode patchedScore) {
        final ApplyPatchResult toRet = new ApplyPatchResult();

        MessageManager closestMessage = getPublishedSummativeAssessmentsMessage();
        GradedScorePatchState patchToApply = null;

        final List<MessageManager> msgSpan = new ArrayList<>();

        if(closestMessage != null) {
            
            /* We found the current message */
            toRet.setCurrentMessage(closestMessage);
            
            patchToApply = new GradedScorePatchState(closestMessage.getTimeStamp());
            patchToApply.updatePatchedScore(patchedScore);
        }

        /* Can't process the patch without something to compare against */
        if (patchToApply == null || closestMessage == null) {
            return toRet;
        }

        /* Update message span or update at timestamp */
        if (toRet.getCurrentMessage() != null) {
            /* Found message at timestamp; update it with the patch */
            final boolean changed = toRet.getCurrentMessage().addPatchedState(patchToApply);
            if (changed) {
                toRet.getAffectedMessages().add(toRet.getCurrentMessage());
            }
            
        } else {

            /* Can't process the patch if no message is found */
            return toRet;
        }

        /* Since we are not updating the entire span, populate it with the
         * message at the current timestamp since this is a new span
         * starting with itself. */
        msgSpan.clear();
        msgSpan.add(toRet.getCurrentMessage());

        return toRet;
    }

    /**
     * Checks if the test message is part of the same span (forms 1 rectangle in
     * the game master past session timeline) as the span message.
     * 
     * @param spanMsg the span message to compare against. Can be null, will
     *        always return false.
     * @param toTestMsg the message to compare against the span. Can't be null.
     * @param patchedAttrName the attribute name that is being patched. Can't be
     *        null or empty.
     * @return true if the tested message is part of the span; false otherwise.
     */
    public boolean isMessagePartOfSpan(MessageManager spanMsg, MessageManager toTestMsg, String patchedAttrName) {
        if (toTestMsg == null) {
            throw new IllegalArgumentException("The parameter 'toTestMsg' cannot be null.");
        }
        if (StringUtils.isBlank(patchedAttrName)) {
            throw new IllegalArgumentException("The parameter 'patchedAttrName' cannot be blank.");
        } else if (spanMsg == null) {
            return false;
        }

        AbstractPerformanceState spanPerfState = MessageManager
                .findPerformanceStateByName(spanMsg.getOriginalMessage(), patchedAttrName);
        if(spanPerfState == null) {
            return false;
        }
        
        final PerformanceStateAttribute spanState = spanPerfState.getState();
        
        AbstractPerformanceState currentMsgState = MessageManager
                .findPerformanceStateByName(toTestMsg.getMessage(), patchedAttrName);
        if(currentMsgState == null) {
            return false;
        }
        
        final PerformanceStateAttribute currentMsgAttr = currentMsgState.getState();
        
        /*- Check if this message is part of the previous span. The span can be
         * broken if any of these occur:
         * 1. Short Term assessment changes
         * 2. Short Term assessment timestamp changes
         * 3. The Node state changes (except active to finished) */
        final boolean shortTermDiff = !spanState.getShortTerm().equals(currentMsgAttr.getShortTerm());
        final boolean assessmentTimeDiff = Long.compare(spanState.getShortTermTimestamp(),
                currentMsgAttr.getShortTermTimestamp()) != 0;
        final boolean nodeStateDiff = spanState.getNodeStateEnum() != currentMsgAttr.getNodeStateEnum()
                && !(spanState.getNodeStateEnum() == PerformanceNodeStateEnum.ACTIVE
                        && currentMsgAttr.getNodeStateEnum() == PerformanceNodeStateEnum.FINISHED);
        if (shortTermDiff || assessmentTimeDiff || nodeStateDiff) {
            return false;
                }

        return true;
                }

    /**
     * Builds a patch state from the diff of the source and patch attributes.
     * 
     * @param sourceAttr the source attribute to compare against. Can't be null.
     * @param patchAttr the patch attribute containing the changes. Can't be
     *        null.
     * @param messageTimestamp the timestamp at which the patch occurs.
     * @return the {@link PatchedState} containing the differences between the
     *         source and patch attributes.
     */
    private PerformancePatchState buildPatchState(PerformanceStateAttribute sourceAttr, PerformanceStateAttribute patchAttr,
            long messageTimestamp) {
        if (sourceAttr == null) {
            throw new IllegalArgumentException("The parameter 'sourceAttr' cannot be null.");
        } else if (patchAttr == null) {
            throw new IllegalArgumentException("The parameter 'patchAttr' cannot be null.");
        }

        final Set<PerformanceStateAttrFields> diffFields = new HashSet<>();
        if (PerformanceStateAttributeDiff.performDiff(patchAttr, sourceAttr, diffFields, false)) {
            PerformancePatchState pState = new PerformancePatchState(patchAttr.getName(), messageTimestamp);
            pState.setPatchedFields(patchAttr, diffFields);
            return pState;
            }

        return null;
        }

   /**
    * Gets the DKF associated with the log
     *
    * @return the DKF. Can be null for older logs that lack DKF files.
     */
   public File getDkf() {
       return this.dkf;
    }

    /**
     * The key used to identify specific {@link PerformanceStateAttribute
     * performance state attributes} by comparing the timestamp and node name.
     * 
     * @author sharrison
     */
    public class PerformanceStateAttributeMessageKey {
        /** The timestamp of the attribute */
        final long timestamp;

        /** The parent node name */
        final String nodeName;

        /**
         * Constructor
         * 
         * @param timestamp the timestamp of the attribute
         * @param nodeName the parent node name
         */
        public PerformanceStateAttributeMessageKey(long timestamp, String nodeName) {
            if (StringUtils.isBlank(nodeName)) {
                throw new IllegalArgumentException("The parameter 'nodeName' cannot be blank.");
            }

            this.timestamp = timestamp;
            this.nodeName = nodeName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
            result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (!(obj instanceof PerformanceStateAttributeMessageKey)) {
                return false;
            }

            PerformanceStateAttributeMessageKey other = (PerformanceStateAttributeMessageKey) obj;
            return timestamp == other.timestamp && StringUtils.equals(nodeName, other.nodeName);
        }
    }

    /**
     * Gets the assessment manager that this message manager uses in order to load scenario-specific
     * information when processing a log associated with a DKF
     * 
     * @return the assessment manager. Can be null if the log being processed does not have a DKF saved
     * alongside it (i.e. it is a legacy log)
     */
    public AbstractAarAssessmentManager getAssessmentManager() {
        return this.assessmentManager;
}
    
    /**
     * Gets the message containing the summative assessment scores that were published
     * within the span of log messages being played back. Generally, this should be a 
     * PublishLessonScoreRequest matching the name of the DKF scenario that the session
     * was running, though. For legacy logs that do not have an accompanying DKF, the last
     * PublishedLessonScoreRequest that was sent is used as a fallback.
     * 
     * @return the message containing the summative assessment scores. Can be null, if no
     * summative scores were ever published, though generally this shouldn't happen unless
     * the log is old or somehow corrupted.
     */
    public MessageManager getPublishedSummativeAssessmentsMessage() {
        
        /* Filter messages on only the ones that publish a lesson score.
         * There should generally only be one of these per knowledge session. */
        final ListIterator<MessageManager> itr = messages.stream().filter(msg -> {
            return MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST.equals(msg.getMessageType());
        }).collect(Collectors.toList()).listIterator();

        
        /* If scenario information from the DKF was loaded by the assessment manager, get the 
         * name of the scenario so we can tell whether a given published lesson score is 
         * for that scenario.
         * 
         * NOTE: The code that places the scenario name in the published lesson score is in 
         * domain.knowledge.Scenario.getScores(). If that method is changed, this logic should
         * be changed as well. */
        String scenarioName = null;
        if(this.assessmentManager != null && this.assessmentManager.getScenario() != null) {
            scenarioName = this.assessmentManager.getScenario().getScenario().getName();
}
        
        MessageManager closestMessage = null;
        
        /* Get the last publish lesson score request found that matches the given scenario name */
        while (itr.hasNext()) {
            final MessageManager currentMsg = itr.next();
            
            /* Find the most recent published lesson score that matches the DKF scenario whose information
             * was loaded into the assessment manager */
            if(MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST.equals(currentMsg.getMessageType())){
                
                Message msg = currentMsg.getMessage();
                if(msg.getPayload() instanceof PublishLessonScore) {
                    
                    if(scenarioName == null) {
                        
                        /* Fallback for old log folders that do not have DKF files saved alongside them. In
                         * this case, there is no scenario information to reference, so simply get the most 
                         * recently published lesson score instead */
                        closestMessage = currentMsg;
                        continue;
                    }
                    
                    PublishLessonScore score = (PublishLessonScore) msg.getPayload();
                    
                    if(score.getCourseData() != null
                            && score.getCourseData().getRoot() != null) {
                        
                        if(scenarioName.equals(score.getCourseData().getRoot().getName())){
                            
                            /* The name of the node matches the name of the scenario, so this score 
                             * contains the summative assessments from that scenario */
                            closestMessage = currentMsg;
                            break;
                        }
                    }
                }
            }
        }
        
        return closestMessage;
    }
}
