/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.proto.common.ProtobufMessageProto.ProtobufMessage;
import mil.arl.gift.common.PublishLessonScore;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.logger.JSONMessageLogReader;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.PerformanceStateAttributeDiff;
import mil.arl.gift.common.state.PerformanceStateAttributeDiff.PerformanceStateAttrFields;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.codec.json.MessageJSONCodec;
import mil.arl.gift.net.api.message.codec.proto.ProtobufMessageProtoCodec;

/**
 * Class that manages a log file playback message and it's associated patch.
 * 
 * @author sharrison
 */
public class MessageManager {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageManager.class.getName());
            
    /** The original log file playback message */
    private final DomainSessionMessageEntry originalMessage;

    /**
     * The performance states from the {@link #originalMessage}. This exists to
     * improve performance because the lookup is called often.
     */
    private final Map<String, AbstractPerformanceState> originalPerformanceStates = new HashMap<>();

    /** The patched log file playback message */
    private DomainSessionMessageEntry patchedMessage;
    
    /** 
     * The previous patched log file playback message. This stores the value that {@link #patchedMessage} 
     * had prior to the most recent call to {@link #constructPatchedMessage()} and can be used to compare
     * the state of the message before and after its most recent patch.
     */
    private DomainSessionMessageEntry previousPatchedMessage;

    /** The codec type delimiter character */
    public final static String CODEC_TYPE_DELIM = "#";

    /** Keep track of the patches applied to this message */
    private final TreeMap<PatchedState, Set<PerformanceStateAttrFields>> patchedStates = new TreeMap<PatchedState, Set<PerformanceStateAttrFields>>(
            new Comparator<PatchedState>() {
                @Override
                public int compare(PatchedState o1, PatchedState o2) {
                    /* Sort by time first */
                    int comparison = Long.compare(o1.getTime(), o2.getTime());
                    if (comparison == 0) {
                        /* If time is the same, check id */
                        return o1.getId().compareTo(o2.getId());
                    }

                    return comparison;
                }
            });

    /**
     * A manual override of the message timestamp. This should be used when the
     * messages are being used at a time other than the time specified in the
     * message.
     */
    private Long adjustedTimestamp;

    /**
     * Constructor.
     * 
     * @param message the original log file playback message. Can't be null.
     */
    public MessageManager(DomainSessionMessageEntry message) {
        if (message == null) {
            throw new IllegalArgumentException("The parameter 'message' cannot be null.");
        }

        this.originalMessage = message;

        PerformanceState state = getLearnerPerformanceState(originalMessage);
        if (state != null) {
            for (TaskPerformanceState task : state.getTasks().values()) {
                mapPerformanceStateNames(task, originalPerformanceStates);
            }
        }
    }

    /**
     * Checks if this message has been patched.
     * 
     * @return true if it has been patched; false otherwise.
     */
    public boolean isPatched() {
        return patchedMessage != null;
    }

    /**
     * Returns the most recent version of the message. <br>
     * <br>
     * WARNING!<br>
     * Be careful not to modify the message returned from this call unless you
     * know it's not the original message.
     * 
     * @return the patched message if a patch exists; the original message if no
     *         patch exists.
     */
    public DomainSessionMessageEntry getMessage() {
        return isPatched() ? patchedMessage : originalMessage;
    }

    /**
     * Return the original log file playback message.
     * 
     * @return the log file playback message.
     */
    public DomainSessionMessageEntry getOriginalMessage() {
        return originalMessage;
    }
    
    /**
     * Adds multiple patched states as part of a single user operation.
     * 
     * This should be used instead of {@link #addPatchedState(PatchedState)} in cases where a
     * single user operation could result in a number of patches to be generated. Unlike that 
     * method, this method only updaes the previous patched version of the message AFTER all of
     * the patches have been applied, making it easier to determine the state of the message 
     * before the user operation.
     * 
     * @param patchedState the patch to apply. If null, nothing will be added.
     *        If the timestamp for the patch is after the timestamp for this
     *        message, it will not be added.
     * @return true if the patch was applied; false if it was not.
     */
    public boolean addPatchedStates(Collection<PatchedState> patchedStates) {
        
        /* Can't use patch if it is after the message time */
        if (CollectionUtils.isEmpty(patchedStates)) {
            return false;
        }
        
        /* Save the previous patched message in case it needs to be compared to the final
         * version after all the patches have been applied */
        previousPatchedMessage = patchedMessage;

        /* Apply the patches normally, but do not update the previous patched message */
        for(PatchedState state : patchedStates) {
            addPatchedState(state, true);
        }

        return true;
    }

    /**
     * Add a patched state with no limitations on which fields can be applied.
     * 
     * @param patchedState the patch to apply. If null, nothing will be added.
     *        If the timestamp for the patch is after the timestamp for this
     *        message, it will not be added.
     * @return true if the patch was applied; false if it was not.
     */
    public boolean addPatchedState(PatchedState patchedState) {
        return addPatchedState(patchedState, false);
    }
    
    /**
     * Add a patched state with no limitations on which fields can be applied and, if specified,
     * applies the state as a partial patch that does not change the previously patches message.
     * 
     * @param patchedState the patch to apply. If null, nothing will be added.
     *        If the timestamp for the patch is after the timestamp for this
     *        message, it will not be added.
     * @param isPartialPatch whether the patch should be applied as a part of a larger patch. Partial
     * patches do not change the previously patched version of the message, since said message will be
     * changed upon completion of the larger patch. This makes it easier to track a message's state before
     * and after a single user operation that causes multiple patch updates, such as when performance
     * state patches are rolled up.
     * @return true if the patch was applied; false if it was not.
     */
    private boolean addPatchedState(PatchedState patchedState, boolean isPartialPatch) {
        
        /* Can't use patch if it is after the message time */
        if (patchedState == null || patchedState.getTime() > originalMessage.getTimeStamp()) {
            return false;
        }

        /* Can't use patch if it is for a performance state that this message
         * doesn't have */
        if(patchedState instanceof PerformancePatchState
                && !hasPerformanceStateWithName(((PerformancePatchState) patchedState).getName())) {
            return false;
        }

        /* Check if any existing patch has a conflicting id with the provided
         * one */
        PatchedState conflictedPatch = null;
        for (PatchedState patch : patchedStates.keySet()) {
            if (StringUtils.equals(patchedState.getId(), patch.getId())) {
                conflictedPatch = patch;
                break;
            }
        }

        /* Update or insert patch */
        if (conflictedPatch != null) {
            conflictedPatch.updatePatch(patchedState);
            patchedStates.put(conflictedPatch, null);
            
        } else {
            /* Tree map is sorted by patch time */
            patchedStates.put(patchedState, null);
        }

        constructPatchedMessage(isPartialPatch);

        return true;
    }

    /**
     * Checks if the provided patch is being applied to this message.
     * 
     * @param patchedState the patch to check. Can't be null.
     * @return true if this message contains the patch; false otherwise.
     */
    public boolean hasPatchedState(PatchedState patchedState) {
        for (PatchedState patch : patchedStates.keySet()) {
            if (StringUtils.equals(patchedState.getId(), patch.getId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets an unmodifiable set of patches that this message has applied.
     * 
     * @return the set of patches for this message.
     */
    public Set<PatchedState> getPatchedStates() {
        return Collections.unmodifiableSet(patchedStates.keySet());
    }

    /**
     * Removes a patch from this message, if it exists.
     * 
     * @param patchedState the patch to remove. If null, nothing will be
     *        removed.
     * @return true if the patch was removed; false if it was not found.
     */
    public boolean removePatchedState(PatchedState patchedState) {
        if (patchedState == null) {
            return false;
        }

        boolean removed = patchedStates.containsKey(patchedState);
        patchedStates.remove(patchedState);
        if (removed) {
            constructPatchedMessage();
        }

        return removed;
    }

    /**
     * Refreshes this message with it's applied patches.
     */
    public void refresh() {
        constructPatchedMessage();
    }

    /**
     * Construct the {@link #patchedMessage} using the original message and the
     * patches.
     */
    private void constructPatchedMessage() {
        if (patchedStates.isEmpty()) {
            patchedMessage = null;
            return;
        }

        /* If there are no patches yet, base the next patch off the original message. If there are other patches,
         * base the next patch off the most recently patched version of the message */
        final MessageManager newManager = new MessageManager(
                createNewMessage(originalMessage.getTimeStamp(), patchedMessage != null ? patchedMessage : originalMessage));
        
        for (Entry<PatchedState, Set<PerformanceStateAttrFields>> entry : patchedStates.entrySet()) {
            final PatchedState patch = entry.getKey();
            
            if(patch instanceof PerformancePatchState) {
                
                /* For performance state patches, need to patch each state individually */
                PerformancePatchState perfPatch = (PerformancePatchState) patch;
                AbstractPerformanceState perfState = findPerformanceStateByName(newManager.getMessage(), perfPatch.getName());
                if (perfState == null || perfState.getState() == null) {
                    continue;
                }
                perfPatch.applyPatch(newManager, entry.getValue());
                
            } else {
                
                /* For other patches, apply them normally */
                patch.applyPatch(newManager);
            }
        }

        /* Save the previous patched messsage in case it needs to be compared to the new patch */
        previousPatchedMessage = patchedMessage;
        
        patchedMessage = newManager.getMessage();
    }
    
    /**
     * Construct the {@link #patchedMessage} using the original message and the
     * patches and, optionally, updates the previous version of the patched message.
     * 
     * @param isPartialPatch whether this update to the patched message should be considered
     * a partial, or incremental, patch. Partial patches are part of a larger batch of patch 
     * updates and, therefore, do not change the previous version of the patched message, 
     * since that will be done after the entire batch of patch updates is handled.
     */
    private void constructPatchedMessage(boolean isPartialPatch) {
        if (patchedStates.isEmpty()) {
            patchedMessage = null;
            return;
        }

        final MessageManager newManager = new MessageManager(
                createNewMessage(originalMessage.getTimeStamp(), originalMessage));
        for (Entry<PatchedState, Set<PerformanceStateAttrFields>> entry : patchedStates.entrySet()) {
            final PatchedState patch = entry.getKey();
            
            if(patch instanceof PerformancePatchState) {
                
                /* For performance state patches, need to patch each state individually */
                PerformancePatchState perfPatch = (PerformancePatchState) patch;
                AbstractPerformanceState perfState = findPerformanceStateByName(newManager.getMessage(), perfPatch.getName());
                if (perfState == null || perfState.getState() == null) {
                    continue;
                }
                perfPatch.applyPatch(newManager, entry.getValue());
                
            } else {
                
                /* For other patches, apply them normally */
                patch.applyPatch(newManager);
            }
        }
        
        if(!isPartialPatch) {

            /* Save the previous patched message in case it needs to be compared to the new patch */
            previousPatchedMessage = patchedMessage;
        }
        
        patchedMessage = newManager.getMessage();
    }

    /**
     * Get the message timestamp.
     * 
     * @return the message timestamp.
     */
    public long getTimeStamp() {
        return adjustedTimestamp == null ? getMessage().getTimeStamp() : adjustedTimestamp;
    }

    /**
     * Finds any attributes that the given patch state has different than the
     * {@link #originalMessage}.
     *
     * @param patchState the performance state that has been patched. Can't be
     *        null.
     * @param differentStates the set to place the different states. Can't be
     *        null.
     */
    public void findDifferentStates(AbstractPerformanceState patchState,
            Map<PerformanceStateAttribute, PatchedState> differentStates) {
        final PerformanceStateAttribute patchAttr = patchState.getState();
        AbstractPerformanceState origState = getOriginalPerformanceStateWithName(patchAttr.getName());

        if (origState == null || origState.getState() == null) {
            return;
        }

        final Set<PerformanceStateAttrFields> diffFields = new HashSet<>();
        if (PerformanceStateAttributeDiff.performDiff(patchAttr, origState.getState(), diffFields, false)) {
            PerformancePatchState pState = new PerformancePatchState(patchAttr.getName(), originalMessage.getTimeStamp());
            pState.setPatchedFields(patchAttr, diffFields);
            differentStates.put(patchAttr, pState);
        }

        /* Check children */
        if (patchState instanceof TaskPerformanceState) {
            TaskPerformanceState tConcept = (TaskPerformanceState) patchState;
            for (ConceptPerformanceState concept : tConcept.getConcepts()) {
                findDifferentStates(concept, differentStates);
            }
        } else if (patchState instanceof IntermediateConceptPerformanceState) {
            IntermediateConceptPerformanceState iConcept = (IntermediateConceptPerformanceState) patchState;
            for (ConceptPerformanceState concept : iConcept.getConcepts()) {
                findDifferentStates(concept, differentStates);
            }
        }
    }

    /**
     * Sets a manual override of the message timestamp. This should be used when
     * the messages are being used at a time other than the time specified in
     * the message.
     * 
     * @param timestamp the timestamp to set. If null (default), the adjusted
     *        timestamp will not be used.
     */
    public void setAdjustedTimestamp(Long timestamp) {
        this.adjustedTimestamp = timestamp;
    }

    /**
     * Get the message type.
     * 
     * @return the message type from the managed message. Can be be null if
     *         never set into the message (unlikely).
     */
    public MessageTypeEnum getMessageType() {
        return getMessage().getMessageType();
    }

    /**
     * Maps the performance states with their names.
     * 
     * @param state the parent state to map. Can't be null.
     * @param nameToStateMap the map to populate.
     */
    private void mapPerformanceStateNames(AbstractPerformanceState state,
            Map<String, AbstractPerformanceState> nameToStateMap) {
        nameToStateMap.put(state.getState().getName(), state);

        /* Add children */
        if (state instanceof TaskPerformanceState) {
            TaskPerformanceState tConcept = (TaskPerformanceState) state;
            for (ConceptPerformanceState concept : tConcept.getConcepts()) {
                mapPerformanceStateNames(concept, nameToStateMap);
            }
        } else if (state instanceof IntermediateConceptPerformanceState) {
            IntermediateConceptPerformanceState iConcept = (IntermediateConceptPerformanceState) state;
            for (ConceptPerformanceState concept : iConcept.getConcepts()) {
                mapPerformanceStateNames(concept, nameToStateMap);
            }
        }
    }

    /**
     * Checks if the message (patch if it exists; original if it doesn't)
     * contains an attribute with the given state name.
     * 
     * @param stateName the state name to check.
     * @return true if the message contains an attribute with the given name.
     */
    public boolean hasPerformanceStateWithName(String stateName) {
        return originalPerformanceStates.containsKey(stateName);
    }

    /**
     * Retrieves the original message's attribute with the given state name.
     * 
     * @param stateName the state name to retrieve.
     * @return the state with the given state name. Can be null if not found.
     */
    public AbstractPerformanceState getOriginalPerformanceStateWithName(String stateName) {
        return originalPerformanceStates.get(stateName);
    }

    /**
     * Finds the {@link AbstractPerformanceState} that contains a
     * {@link PerformanceStateAttribute} with the same name as the one provided.
     * 
     * @param msg The {@link DomainSessionMessageEntry} to search.
     * @param stateName The performance state name we are trying to find.
     * @return the {@link AbstractPerformanceState} that contains an attribute
     *         with the same name as the one provided. Can be null if none was
     *         found.
     */
    public static AbstractPerformanceState findPerformanceStateByName(DomainSessionMessageEntry msg, String stateName) {
        PerformanceState performance = getLearnerPerformanceState(msg);
        if (performance != null) {
            for (TaskPerformanceState taskPerfState : performance.getTasks().values()) {
                AbstractPerformanceState perfState = findPerformanceStateByName(taskPerfState, stateName);
                if (perfState != null) {
                    /* Found it, exit early */
                    return perfState;
                }
            }
        }

        return null;
    }

    /**
     * Finds the {@link AbstractPerformanceState} that contains a
     * {@link PerformanceStateAttribute} with the same name as the one provided.
     * 
     * @param perfState The {@link AbstractPerformanceState} to search.
     * @param stateName The performance state name we are trying to find.
     * @return the {@link AbstractPerformanceState} that contains an attribute
     *         with the same name as the one provided. Can be null if none was
     *         found.
     */
    private static AbstractPerformanceState findPerformanceStateByName(AbstractPerformanceState perfState,
            String stateName) {
        final PerformanceStateAttribute cState = perfState.getState();
        if (StringUtils.equals(cState.getName(), stateName)) {
            return perfState;
        }

        List<ConceptPerformanceState> children = null;
        if (perfState instanceof TaskPerformanceState) {
            children = ((TaskPerformanceState) perfState).getConcepts();
        } else if (perfState instanceof IntermediateConceptPerformanceState) {
            children = ((IntermediateConceptPerformanceState) perfState).getConcepts();
        }

        if (children != null) {
            for (ConceptPerformanceState child : children) {
                AbstractPerformanceState foundChild = findPerformanceStateByName(child, stateName);
                if (foundChild != null) {
                    return foundChild;
                }
            }
        }

        return null;
    }

    /**
     * Retrieve the {@link PerformanceState} from the given message if possible.
     * 
     * @param msg the message to use.
     * @return the {@link PerformanceState} if it exists; null otherwise.
     */
    public static PerformanceState getLearnerPerformanceState(DomainSessionMessageEntry msg) {
        if (msg == null || msg.getMessageType() != MessageTypeEnum.LEARNER_STATE
                || !(msg.getPayload() instanceof LearnerState)) {
            return null;
        }

        return ((LearnerState) msg.getPayload()).getPerformance();
    }
    
    /**
     * Retrieve the {@link GradedScoreNode} from the given message if possible.
     * 
     * @param msg the message to use.
     * @return the {@link GradedScoreNode} if it exists; null otherwise.
     */
    public static GradedScoreNode getScore(DomainSessionMessageEntry msg) {
        if (msg == null || msg.getMessageType() != MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST
                || !(msg.getPayload() instanceof PublishLessonScore)) {
            return null;
        }
        
        PublishLessonScore publishedScore = ((PublishLessonScore) msg.getPayload());
        if(publishedScore.getCourseData() == null) {
            return null;
        }

        return publishedScore.getCourseData().getRoot();
    }

    /**
     * Creates a new message at the specified timestamp.
     * 
     * @param timestamp the timestamp for the new message.
     * @param sourceMsg the source message that will be copied as the base of
     *        the new message. Can't be null.
     * @return the created message. Will never be null.
     */
    public static DomainSessionMessageEntry createNewMessage(long timestamp, DomainSessionMessageEntry sourceMsg) {
        
        /* This is a new message that needs to be injected into the list */
        /* Need to create a copy of the previous attribute to be used as a new
         * message. The copy will be updated with the timestamp and new
         * performance state. */
        
        try {
            
            /* Attempt to copy the message using the most up-to-date Protobuf codecs */
            DomainSessionMessageEntry synthEntry = synthesizeMessage(sourceMsg, timestamp);
            ProtobufMessageProtoCodec codec = new ProtobufMessageProtoCodec();
            ProtobufMessage protoMsg = codec.map(
                    convertToDomainSessionMessage(synthEntry)
            );
            
            DomainSessionMessage message = (DomainSessionMessage) codec.convert(protoMsg);
            
            return new DomainSessionMessageEntry(message.getSourceEventId(),
                    message.getDomainSessionId(),
                    message.getUserSession(),
                    synthEntry.getElapsedDSTime(),
                    synthEntry.getTimeStamp(),
                    message);
            
        } catch(Exception e) {
            
            /* This could be a legacy message from an older JSON log that cannot be encoded/decoded using the most
             * up-to-date codecs. As a fallback, attempt to decode the message using the legacy JSON codecs */
            
            try {
            
                JSONObject jsonObj = new JSONObject();
    
                /* Encode a synthesized message into the json object */
                MessageJSONCodec.encode(jsonObj, convertToDomainSessionMessage(synthesizeMessage(sourceMsg, timestamp)));
    
                /* Convert message to string to be re-parsed as a new object */
                final String logStr = JSONMessageLogReader.formatPayloadStringForJsonLog(jsonObj.toString(), true);
                
                /* Grab the starting index of the codec name from the log string. */
                final int codecNameStartIndex = logStr.indexOf(CODEC_TYPE_DELIM);
    
                /* Grab the ending index of the codec name from the log string. */
                final int codecNameEndIndex = logStr.indexOf(CODEC_TYPE_DELIM, codecNameStartIndex+1);
    
                /* Create new instance of the message. This eliminates any
                 * 'by-reference' problems. */
                return (DomainSessionMessageEntry) JSONMessageLogReader.parseMessageFromLogLine(logStr, codecNameStartIndex,
                        codecNameEndIndex, true);
            
            } catch (Throwable e2) {
                
                /* Report if the message cannot be decoded using the current or legacy codecs */
                logger.error("Unable to copy message " + sourceMsg, e);
                throw e2;
            }
        }
    }

    /**
     * Creates a modified version of a provided
     * {@link DomainSessionMessageEntry} with a different timestamp.
     *
     * @param msg The {@link DomainSessionMessageEntry} to duplicate. Can't be
     *        null.
     * @param time The new time to use for the duplicated message.
     * @return The newly created {@link DomainSessionMessageEntry}. Can't be
     *         null.
     */
    private static DomainSessionMessageEntry synthesizeMessage(DomainSessionMessageEntry msg, long time) {
        long timeShift = time - msg.getTimeStamp();
        Message intermediate = new Message(msg.getMessageType(), msg.getSequenceNumber(), msg.getSourceEventId(), time,
                msg.getSenderModuleName(), msg.getSenderAddress(), msg.getSenderModuleType(),
                msg.getDestinationQueueName(), msg.getPayload(), false);
        return new DomainSessionMessageEntry(msg.getSourceEventId(), msg.getDomainSessionId(), msg.getUserSession(),
                msg.getElapsedDSTime() + timeShift, msg.getWriteTime() + timeShift, intermediate);
    }

    /**
     * Converts a {@link DomainSessionMessageEntry} to a
     * {@link DomainSessionMessage}.
     *
     * @param msg The {@link DomainSessionMessageEntry} to convert. Can't be
     *        null.
     * @return The newly created message. Can't be null.
     */
    private static DomainSessionMessage convertToDomainSessionMessage(DomainSessionMessageEntry msg) {
        UserSession uSession = new UserSession(msg.getUserId());
        uSession.setUsername(msg.getUsername());
        uSession.setExperimentId(msg.getExperimentId());
        return new DomainSessionMessage(msg.getMessageType(), msg.getSequenceNumber(), msg.getSourceEventId(),
                msg.getTimeStamp(), msg.getSenderModuleName(), msg.getSenderAddress(), msg.getSenderModuleType(),
                msg.getDestinationQueueName(), msg.getPayload(), uSession, msg.getDomainSessionId(),
                msg.needsHandlingResponse());
    }
    
    /**
     * Returns the version of the message <i>before</i> the most recent patch. This can
     * be used to compare the latest version of the message to the version that existed
     * before the most recent patch.
     * <br><br>
     * WARNING!<br>
     * Be careful not to modify the message returned from this call unless you
     * know it's not the original message.
     * 
     * @return the previously patched message if an older patch exists; the original message if no
     *         patch exists.
     */
    public DomainSessionMessageEntry getPreviousPatchedMessage() {
        return previousPatchedMessage != null ? previousPatchedMessage : originalMessage;
    }
}