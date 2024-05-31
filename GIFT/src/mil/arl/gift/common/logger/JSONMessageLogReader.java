/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageUtil;

/**
 * This class will parse a message log file (e.g. system, domain session message log file) and create Message objects.
 *
 * @author mhoffman
 *
 */
public class JSONMessageLogReader extends MessageLogReader {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(JSONMessageLogReader.class);

    /** The codec type delimiter character */
    public static final String CODEC_TYPE_DELIM = "#";

    /**
     * Class constructor - default
     */
    public JSONMessageLogReader(){

    }

    @Override
    protected void parse(FileProxy logFile) throws Exception {

        String logFileName = logFile.getName();

        if(logger.isInfoEnabled()){
            logger.info("Starting to parse message log file named "+logFileName);
        }

        try (final Stream<Message> messageStream = streamMessages(logFile)) {

            haveCheckedIfDSLog = false;

            messageStream.forEach(msg -> {
                messages.add(msg);

                List<Message> messagesOfType = messagesByType.get(msg.getMessageType());
                if (messagesOfType == null) {
                    messagesOfType = new ArrayList<>();
                    messagesByType.put(msg.getMessageType(), messagesOfType);
                }

                messagesOfType.add(msg);
            });

            if(logger.isInfoEnabled()){
                logger.info("Successfully read in "+messages.size()+" messages");
            }

            if(messages.isEmpty()){
                logger.error("There were no messages to parse in the message log file named "+logFileName);
            }

        }catch(Exception e){
            throw new Exception("Failed to fully parse " + logFileName, e);
        }

        if(logger.isInfoEnabled()){
            logger.info("Finished parsing");
        }
    }

    @Override
    public Stream<Message> streamMessages(FileProxy logFile, int from, int until) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(logFile.getSingleUseInputStream()));
        return br.lines().skip(from).limit(until - from).map(this::parseMessageFromLogLine).filter(Objects::nonNull)
                .onClose(() -> {
                    try {
                        br.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    /**
     * Parses a message from the given log line.
     * 
     * @param line the line to parse.
     * @return the parsed {@link Message}. Can be null.
     */
    private Message parseMessageFromLogLine(String line) {
        int codecNameStartIndex = line.indexOf(CODEC_TYPE_DELIM);
        if (codecNameStartIndex == -1) {
            /* unable to find the Codec name in the line read from the file */
            if (logger.isInfoEnabled()) {
                logger.info("Skipping line with content of " + line);
            }

            return null;
        }

        int codecNameEndIndex = line.indexOf(CODEC_TYPE_DELIM, codecNameStartIndex+1);
        if (codecNameEndIndex == -1) {
            /* unable to find the Codec name in the line read from the file */
            if (logger.isInfoEnabled()) {
                logger.info("Skipping line with content of " + line);
            }

            return null;
        }

        if (!haveCheckedIfDSLog) {
            isDomainSessionLog = isDomainSessionMessageLog(line.substring(0, codecNameStartIndex));
            haveCheckedIfDSLog = true;
            if(logger.isInfoEnabled()){
                logger.info("Determined that the input file is a domain session message log file");
            }
        }

        return parseMessageFromLogLine(line, codecNameStartIndex, codecNameEndIndex, isDomainSessionLog);
    }

    /**
     * Parses a message from the given log line.
     * 
     * @param line the line to parse.
     * @param isDomainSessionLog true if the line contains a domain session
     *        message; false otherwise.
     * @return the parsed {@link Message}. Can be null.
     */
    public static Message parseMessageFromLogLine(String line, int codecNameStartIndex, int codecNameEndIndex,
            boolean isDomainSessionLog) {
        if (codecNameStartIndex == -1) {
            /* unable to find the Codec name in the line read from the file */
            if (logger.isInfoEnabled()) {
                logger.info("Unable to find the Codec name in the line read with content of " + line);
            }

            return null;
        }

        if (codecNameEndIndex == -1) {
            /* unable to find the Codec name in the line read from the file */
            if (logger.isInfoEnabled()) {
                logger.info("Unable to find the Codec name in the line read with content of " + line);
            }

            return null;
        }

        String encodingTypeName = line.substring(codecNameStartIndex + 1, codecNameEndIndex);
        MessageEncodingTypeEnum encodingType = MessageEncodingTypeEnum.valueOf(encodingTypeName);
        if (encodingType == null) {
            return null;
        }

        String dsTimePrefixStr = line.substring(0, codecNameStartIndex);
        if (logger.isTraceEnabled()) {
            logger.trace("Decoding message of type " + encodingType + " from " + line);
        }

        Message message = MessageUtil.getMessageFromString(line.substring(codecNameEndIndex + 1), encodingType);

        if (isDomainSessionLog) {
            // using trace logging because of how frequent this can happen
            if (logger.isTraceEnabled()) {
                logger.trace("Create domain session message entry from message");
            }
            message = DomainSessionMessageEntry.parseMessageEntry(dsTimePrefixStr, (DomainSessionMessage) message);
        }

        return message;
    }

    /**
     * Return whether the provided line from the log file contains information relating to
     * an entry in a domain session message log.
     *
     * @param strLine
     * @return boolean
     */
    private boolean isDomainSessionMessageLog(String strLine){

        //check for 2 time stamps before the message encoding
        String[] timestamps = strLine.split(" ");

        return timestamps.length == 2;
    }
    
    /**
     * Return whether the file is a message log file by analyzing its content.
     * Specifically this method checks for the codec delimiters.
     * 
     * @param file the file to check
     * @return boolean
     */
    @Override
    public boolean isMessageLog(FileProxy file){
        
        boolean checkPassed = false;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(file.getSingleUseInputStream()))){
            String strLine;
            int codecNameStartIndex, codecNameEndIndex;  
            
            while((strLine = br.readLine()) != null){
                
                codecNameStartIndex = strLine.indexOf(CODEC_TYPE_DELIM);
                codecNameEndIndex = strLine.indexOf(CODEC_TYPE_DELIM, codecNameStartIndex+1);
                
                if(codecNameStartIndex != -1 && codecNameEndIndex != -1){
                    //found a codec string
                    checkPassed = true;
                    break;
                }
            }
            
        }catch(Exception e){
            logger.error("Caught exception while trying to determine if the file: "+file.getName()+" is a message log file", e);
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Check of file named = "+file.getName()+" to be a message log event source file resulted in "+checkPassed);
        }
        
        return checkPassed;
    }

    /**
     * Formats the payload string into a full log message by prepending the
     * timestamp and codec type.
     * 
     * @param payloadString the payload string value.
     * @param prependDefaultTimestamp true to prepend the log message with a
     *        default relative time; false to not prepend a time.
     * @return the full log message string. Will return null if the payload
     *         string is null or empty.
     */
    public static String formatPayloadStringForJsonLog(String payloadString, boolean prependDefaultTimestamp) {
        if (StringUtils.isBlank(payloadString)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (prependDefaultTimestamp) {
            sb.append(TimeUtil.getDefaultRelativeTime()).append(Constants.SPACE)
                    .append(TimeUtil.getDefaultRelativeTime());
        }
        sb.append(Constants.SPACE).append(CODEC_TYPE_DELIM).append(MessageEncodingTypeEnum.JSON.getName())
                .append(CODEC_TYPE_DELIM).append(Constants.SPACE).append(payloadString);

        return sb.toString();
    }

    /**
     * Used to launch and test an instance of the log reader.
     *
     * @param args - arg[0] is the name of a message log to parse
     */
    public static void main(String[] args) {

        if(args.length == 1){

            String filename = args[0];
            MessageLogReader reader = new JSONMessageLogReader();
            try{
                reader.parse(new FileProxy(new File(filename)));
            }catch(Exception e){
                e.printStackTrace();
            }

        }else{
            System.out.println("ERROR: Please provide the message log file name to read as the only argument");
        }

        System.out.println("Good-bye");
    }
}