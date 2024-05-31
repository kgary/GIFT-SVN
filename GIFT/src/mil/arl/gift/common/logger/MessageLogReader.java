/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.net.api.message.Message;

/**
 * This class will parse a message log file (e.g. system, domain session message log file) and create Message objects.
 *
 * @author mhoffman
 *
 */
public abstract class MessageLogReader {

    /** the ordered list of messages created from the message log file */
    protected List<Message> messages = new ArrayList<>();

    /** mapping of message type enum to an ordered list of messages of that type as they appear in the log */
    protected Map<MessageTypeEnum, List<Message>> messagesByType = new HashMap<>();

    protected boolean haveCheckedIfDSLog;

    /** flag indicating whether this message log is a domain session log file */
    protected boolean isDomainSessionLog;

    /** the amount of time in milliseconds between the first and last message timestamp. */
    protected long duration = -1;

    /**
     * Class constructor - default
     */
    public MessageLogReader(){

    }

    /**
     * Determines which message log reader is appropriate for the file and
     * creates it.
     * 
     * @param filename the name of the file to read.
     * @return the message log reader for the file. Won't be null.
     */
    public static MessageLogReader createMessageLogReader(String filename) {
        if (ProtobufMessageLogReader.isProtobufLogFile(filename)) {
            return new ProtobufMessageLogReader();
        } else {
            return new JSONMessageLogReader();
        }
    }

    /**
     * Parse the message log file and
     * create new message objects for any message found.
     *
     * @param logFile the message log file being read
     * @throws Exception if there was a severe problem parsing the log file
     */
    public void parseLog(FileProxy logFile) throws Exception {
        reset();
        parse(logFile);
    }

    /**
     * Parse the message log file with the given name (including path) and
     * create new message objects for any message found.
     *
     * @param logFile the message log file being read
     * @throws Exception if there was a severe problem parsing the log file
     */
    protected abstract void parse(FileProxy logFile) throws Exception;
    
    /**
     * Constructs a {@link Stream} of {@link Message} from a provided
     * {@link FileProxy} that efficiently iterates over each {@link Message}
     * contained within the log.
     *
     * @param logFile The {@link FileProxy} from which to extract the
     *        {@link Message} objects. Can't be null.
     * @return The {@link Stream} of {@link Message} that can be iterated over.
     *         Can't be null. <b>IMPORTANT:</b> Call {@link Stream#close()} on
     *         this value when the stream is no longer needed.
     * @throws IOException If there was a problem opening the {@link FileProxy}.
     */
    public Stream<Message> streamMessages(FileProxy logFile) throws IOException {
        return streamMessages(logFile, 0, Integer.MAX_VALUE);
    }

    /**
     * Constructs a {@link Stream} of {@link Message} from a provided
     * {@link FileProxy} that efficiently iterates over each {@link Message}
     * contained within the specified lines of the log.
     *
     * @param logFile The {@link FileProxy} from which to extract the
     *        {@link Message}
     * @param from The inclusive bound of the 0-based index of the line number
     *        on which to start.
     * @param until The exclusive bound of the 0-based index of the line number
     *        which to stop reading messages from.
     * @return The {@link Stream} of {@link Message} that can be iterated over.
     *         Can't be null. <b>IMPORTANT:</b> Call {@link Stream#close()} on
     *         this value when the stream is no longer needed.
     * @throws IOException If there was a problem opening the {@link FileProxy}.
     */
    public abstract Stream<Message> streamMessages(FileProxy logFile, int from, int until) throws IOException;

    /**
     * Return whether this message log is a domain session log file
     *
     * @return boolean
     */
    public boolean isDomainSessionLog(){
        return isDomainSessionLog;
    }

    /**
     * Return whether the file is a message log file by analyzing its content.
     * 
     * @param file the file to check
     * @return boolean
     */
    public abstract boolean isMessageLog(FileProxy file);

    /**
     * Reset this log reader by clearing out containers with messages parsed from a previous
     * log file parse.
     */
    protected void reset(){
        messages.clear();
        messagesByType.clear();
        haveCheckedIfDSLog = false;
        isDomainSessionLog = false;
        duration = -1;
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /**
     * Return the collection of messages of a particular type from the parsed message
     * log file.
     *
     * @param messageType - the type of messages to return
     * @return the messages of that type.  Can be null or empty.  Order will match how they appear
     * in the log file.
     */
    public List<Message> getMessagesByType(MessageTypeEnum messageType) {
        final List<Message> messagesOfType = messagesByType.get(messageType);
        return messagesOfType != null ? Collections.unmodifiableList(messagesOfType) : null;
    }

    /**
     * Return the collection of message types found in the parsed log file.
     *
     * @return Set<MessageTypeEnum>
     */
    public Set<MessageTypeEnum> getTypesOfMessages(){
        return messagesByType.keySet();
    }


    /**
     * Returns the index of this message in the ordered list of logged messages.</br>
     * Note: this is an expensive method that has to look through the entire list of messages.
     *
     * @param message the message to look for in the list.  If null this method returns -1.
     * @return the 0-based index of the message in the list.  If not found -1 is returned.
     */
    public int getIndexOfMessage(Message message){

        int index = -1;
        if(message != null){
            index = messages.indexOf(message);
        }

        return index;
    }

    /**
     * Return the amount of time in milliseconds between the first and last message timestamp.
     *
     * @return amount of time in milliseconds this log file covers.  -1 will be returned if there are less
     * than 2 messages in the list.
     */
    public long getDuration(){

        if(duration == -1 && messages.size() > 1){
            Message firstMsg = messages.get(0);
            Message lastMsg = messages.get(messages.size()-1);

            duration = lastMsg.getTimeStamp() - firstMsg.getTimeStamp();
        }

        return duration;
    }

    /**
     * Returns an ordered list of events based on gift messages found in the ordered message list using the
     * parameters provided.
     *
     * @param startIndex the index to start looking at messages in the ordered list of messages
     * @param stopLookingMsg the message type to end searching once it is reached
     * @param stopTimerMessageType used to determine when an event has finished.  This is normally a Processed ACK message.</br>
     * In the future this might be paired with each message type in messagesToLookFor parameter.
     * @param messagesToLookFor the message types to look for and correspond to an event that is returned.
     * @return the collection of events found and any metadata about the event (e.g. duration)
     */
    public List<TimedEvent> getMessagesAfterIndexUntilMessageType(int startIndex, MessageTypeEnum stopLookingMsg, MessageTypeEnum stopTimerMessageType, Set<MessageTypeEnum> messagesToLookFor){

        List<TimedEvent> messagesFound = new ArrayList<>(0);
        for(int index = startIndex; index < messages.size(); index++){

            Message candidateMessage = messages.get(index);
            if(candidateMessage.getMessageType() == stopLookingMsg){
                //finished searching
                break;
            }else if(messagesToLookFor.contains(candidateMessage.getMessageType())){

                //get duration until the event is over
                long duration = 0;
                for(int endEventIndex = index + 1; endEventIndex < messages.size(); endEventIndex++){

                    Message endEventCandidateMessage = messages.get(endEventIndex);
                    if(endEventCandidateMessage.getMessageType() == stopLookingMsg){
                        break;
                    }else if(endEventCandidateMessage.getMessageType() == stopTimerMessageType && endEventCandidateMessage.getReplyToSequenceNumber() == candidateMessage.getSequenceNumber()){
                        //found the end, calculate the duration of the event
                        duration = endEventCandidateMessage.getTimeStamp() - candidateMessage.getTimeStamp();
                        break;
                    }
                }

                TimedEvent event = new TimedEvent(candidateMessage, duration);
                messagesFound.add(event);
            }
        }
        return messagesFound;
    }

    /**
     * Metadata about an event in the message log.
     *
     * @author mhoffman
     *
     */
    public class TimedEvent{

        /** the message being analyzed for time */
        private Message message;

        /** how long in milliseconds the event took place */
        private long durationOfEvent;

        /**
         * Set attributes.
         *
         * @param message the event of interest
         * @param durationOfEvent how long in milliseconds the event took place.  A zero or negative value
         * means the duration could not be determined.
         */
        public TimedEvent(Message message, long durationOfEvent){
            this.message = message;
            this.durationOfEvent = durationOfEvent;
        }

        /**
         * Return the message being analyzed for time
         * @return the message being analyzed
         */
        public Message getMessage() {
            return message;
        }

        /**
         * Return how long in milliseconds the event took place
         *
         * @return milliseconds of duration the event took place. A zero or negative value
         * means the duration could not be determined.
         */
        public long getDurationOfEvent() {
            return durationOfEvent;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[TimedEvent: message = ");
            builder.append(message);
            builder.append(", durationOfEvent = ");
            builder.append(durationOfEvent);
            builder.append("]");
            return builder.toString();
        }


    }
}
