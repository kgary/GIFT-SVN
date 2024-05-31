/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.util.JsonFormat;

import generated.proto.common.ProtobufLogMessageProto.ProtobufLogMessage;
import generated.proto.common.ProtobufMessageProto.ProtobufMessage;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.aar.util.LogFilePlaybackMessageManager;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.common.util.ProtobufDefaultsUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.codec.proto.AbstractEnumObjectProtoCodec;
import mil.arl.gift.net.api.message.codec.proto.ProtoMapper;
import mil.arl.gift.net.api.message.codec.proto.ProtobufMessageProtoCodec;

/**
 * Reader for the protobuf binary log file.
 * 
 * @author sharrison, oamer, cpolynice
 */
public class ProtobufMessageLogReader extends MessageLogReader {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ProtobufMessageLogReader.class);

    /** The GIFT protobuf session log file extension */
    public static final String PROTOBUF_LOG_FILE_EXTENSION = ".protobuf.bin";

    /** The file extension for a GIFT session JSON log file, the output of the converted protobuf log  */
    public static final String CONVERTED_LOG_FILE_EXTENSION = ".log";

    /**
     * The number of seconds until we declare a timeout for parsing a single
     * message from the protobuf log
     */
    private static final int PARSE_TIMEOUT = 10;

    /** used to retrieve Proto classes for each message type */
    private static ProtoMapper mapper = ProtoMapper.getInstance();

    /** The codec for protobuf messages */
    private static final ProtobufMessageProtoCodec CODEC = new ProtobufMessageProtoCodec();
    
    /** The codec for protobuf enumerations */
    private static final AbstractEnumObjectProtoCodec ENUM_CODEC = new AbstractEnumObjectProtoCodec();

    /** The parsed {@link ProtobufLogMessage log messages} */
    private final List<ProtobufLogMessage> originalLogMessages = new ArrayList<>();

    /**
     * Parses the protobuf messages from the given input file.
     * 
     * @param inputFile the file containing the binary protobuf messages. Must
     *        be of type {@value #PROTOBUF_LOG_FILE_EXTENSION}.
     * @throws Exception if there was a problem reading the file.
     */
    @Override
    protected void parse(FileProxy inputFile) throws Exception {
        if (inputFile == null) {
            throw new IllegalArgumentException("The parameter 'inputFile' cannot be null.");
        } else if (!inputFile.exists()) {
            throw new IllegalArgumentException("The protobuf input file '" + inputFile + "' doesn't exist.");
        } else if (!isProtobufLogFile(inputFile.getName())) {
            throw new IllegalArgumentException("The protobuf input file '" + inputFile
                    + "' is not the correct type of file. Expected file extension: '*" + PROTOBUF_LOG_FILE_EXTENSION
                    + "'.");
        }

        try (final InputStream in = inputFile.getSingleUseInputStream()) {

            /* This will read the file, one line at a time. When there are no
             * messages left it will return null. */
            while (true) {
                ProtobufLogMessage message;
                try {
                    final CompletableFuture<ProtobufLogMessage> future = new CompletableFuture<ProtobufLogMessage>();
                    CompletableFuture.runAsync(new Runnable() {
                        @Override
                        public void run() {
                            readNextLine(in, future);
                        }
                    });

                    message = future.get(PARSE_TIMEOUT, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new Exception("Unable to parse the input file '" + inputFile + "'.", e);
                }

                if (message == null) {
                    break;
                }

                /* Add all non-null ProtobufLogMessages to the original log
                 * messages list */
                originalLogMessages.add(message);

                /* Convert to Message if possible */
                if (!message.hasMessage()) {
                    continue;
                }

                final ProtobufMessage protoMessage = message.getMessage();

                if (!ProtobufDefaultsUtil.checkRequiredFields(protoMessage)) {
                    logger.error("Found message of type '" + protoMessage.getMessageType()
                            + "' that is missing required fields.");
                    continue;
                }

                if (!haveCheckedIfDSLog) {
                    isDomainSessionLog = protoMessage.hasDomainSessionId();
                    haveCheckedIfDSLog = true;
                    if(logger.isInfoEnabled()){
                        logger.info("Determined that the input file is a domain session message log file");
                    }
                }
                
                Message commonMessage = CODEC.convert(protoMessage);

                /* Convert DomainSessionMessage to DomainSessionMessageEntry */
                if (isDomainSessionLog) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Create domain session message entry from message");
                    }

                    final String elapsedDSTime = message.hasElapsedDsTime()
                            ? TimeUtil.formatTimeRelative(message.getElapsedDsTime().getValue())
                            : TimeUtil.getDefaultRelativeTime();
                    final String elapsedWriteTime = message.hasElapsedWriteTime()
                            ? TimeUtil.formatTimeRelative(message.getElapsedWriteTime().getValue())
                            : TimeUtil.getDefaultRelativeTime();

                    commonMessage = DomainSessionMessageEntry.parseMessageEntry(elapsedDSTime + " " + elapsedWriteTime,
                            (DomainSessionMessage) commonMessage);
                }

                messages.add(commonMessage);
                final MessageTypeEnum typeEnum = commonMessage.getMessageType();
                if (!messagesByType.containsKey(typeEnum)) {
                    messagesByType.put(typeEnum, new ArrayList<>());
                }

                messagesByType.get(typeEnum).add(commonMessage);
            }
        }
    }

    /**
     * Read the next line of the input stream
     * 
     * @param in the input stream to read.
     * @param future the future used to return the parsed log message.
     */
    private void readNextLine(InputStream in, CompletableFuture<ProtobufLogMessage> future) {
        try {
            future.complete(ProtobufLogMessage.parseDelimitedFrom(in));
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
    }

    @Override
    public Stream<Message> streamMessages(FileProxy logFile, int from, int until) throws IOException {
        if (logFile == null) {
            throw new IllegalArgumentException("The parameter 'logFile' cannot be null.");
        } else if (!logFile.exists()) {
            throw new IllegalArgumentException("The protobuf log file '" + logFile + "' doesn't exist.");
        } else if (!isProtobufLogFile(logFile.getName())) {
            throw new IllegalArgumentException("The protobuf log file '" + logFile
                    + "' is not the correct type of file. Expected file extension: '*" + PROTOBUF_LOG_FILE_EXTENSION
                    + "'.");
        }
        
        try {
            super.parseLog(logFile);
            return messages.stream().skip(from).limit(until - from);
        } catch (Exception e) {
            logger.error("Unable to parse protobuf log message.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the given file is a protobuf binary log file.
     * 
     * @param filename the filename the check.
     * @return true if the file has the extension
     *         {@value #PROTOBUF_LOG_FILE_EXTENSION}; false otherwise.
     */
    public static boolean isProtobufLogFile(String filename) {
        if (StringUtils.isBlank(filename)) {
            return false;
        }

        return filename.endsWith(PROTOBUF_LOG_FILE_EXTENSION) ||
                filename.endsWith(PROTOBUF_LOG_FILE_EXTENSION + LogFilePlaybackMessageManager.LOG_PATCH_EXTENSION);
    }
    
    @Override
    public boolean isMessageLog(FileProxy file) {
        return isProtobufLogFile(file.getName());
    }

    /**
     * Checks if the given file was converted from a protobuf binary log file.
     * 
     * @param file the file the check.
     * @return true if the file has the extension
     *         {@value #CONVERTED_LOG_FILE_EXTENSION}; false otherwise.
     */
    public static boolean isConvertedProtobufLogFile(File file) {
        if (file == null) {
            return false;
        }

        return file.getName().endsWith(CONVERTED_LOG_FILE_EXTENSION);
    }

    /**
     * Removes the protobuf log file extension.
     * 
     * @param file the file to remove the extension.
     * @return the file name without the protobuf log file extension.
     */
    public static String removeProtobufLogFileExtension(File file) {
        if (!isProtobufLogFile(file.getName())) {
            return file.getName();
        }

        int lastIndex = file.getName().lastIndexOf(PROTOBUF_LOG_FILE_EXTENSION);
        return file.getName().substring(0, lastIndex);
    }

    @Override
    protected void reset() {
        super.reset();
        originalLogMessages.clear();
    }

    /**
     * Retrieve the parsed messages from the log file.
     * 
     * @return the unmodifiable list of parsed log messages. Each
     *         {@link ProtobufLogMessage} is guaranteed to contain a
     *         {@link ProtobufMessage}.
     */
    public List<ProtobufLogMessage> getOriginalLogMessages() {
        return Collections.unmodifiableList(originalLogMessages);
    }

    /**
     * Convert the protobuf binary log file to a human-readable log file.
     * 
     * @param inputFile the input protobuf binary log file.
     * @param outputFile the output directory to put the converted log file.
     * @return the file that was created
     * @throws Exception if there is a problem reading the log file or if the
     *         output directory doesn't exist.
     */
    public File writeLogAsJSON(File inputFile, File outputFile) throws Exception {
        if (outputFile == null) {
            throw new IllegalArgumentException("The parameter 'outputFile' cannot be null.");
        } else if (outputFile.isDirectory() && !outputFile.exists()) {
            throw new IllegalArgumentException("The output directory '" + outputFile + "' doesn't exist.");
        }

        /* This also checks the input file param */
        parseLog(new FileProxy(new File(inputFile.getAbsolutePath())));

        /* Create output file */
        if (outputFile.isDirectory()) {
            outputFile = new File(outputFile, removeProtobufLogFileExtension(inputFile) + "_"
                    + TimeUtil.formatTimeLogFilename(System.currentTimeMillis()) + CONVERTED_LOG_FILE_EXTENSION);
        }

        /* Writer will automatically close when try is complete */
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (ProtobufLogMessage msg : originalLogMessages) {
                StringBuilder sb = new StringBuilder();

                /* Check if Domain or Admin message. */
                if (msg.hasElapsedDsTime() && msg.hasElapsedWriteTime()) {
                    sb.append(TimeUtil.formatTimeRelative(msg.getElapsedDsTime().getValue())).append(Constants.SPACE)
                            .append(TimeUtil.formatTimeRelative(msg.getElapsedWriteTime().getValue()));

                } else if (msg.hasElapsedDsTime()) {
                    sb.append(TimeUtil.formatTimeSystemLog((long) msg.getElapsedWriteTime().getValue()));
                } else if (msg.hasMessage()) {
                    sb.append(TimeUtil.formatTimeSystemLog(
                            ProtobufConversionUtil.convertTimestampToMillis(msg.getMessage().getTimeStamp())));
                }

                /* Convert to JSON string. Remove extra whitespace (new lines)
                 * and include default values */
                if (msg.hasMessage()) {
                    JsonFormat.TypeRegistry.Builder format = JsonFormat.TypeRegistry.newBuilder();
                    MessageTypeEnum type = (MessageTypeEnum) ENUM_CODEC.convert(msg.getMessage().getMessageType());
                    Class<?> protoClass = mapper.getObjectClass(type);

                    if (protoClass == null) {
                        sb.append("NO PROTOBUF CLASS FOUND FOR ").append(type).append(" MESSAGE TYPE ");
                    } else {
                        AbstractMessage unpackPayload = msg.getMessage().getPayload()
                                .unpack(protoClass.asSubclass(AbstractMessage.class));
                        format.add(unpackPayload.getDescriptorForType());

                        JsonFormat.Printer printer = JsonFormat.printer().usingTypeRegistry(format.build());
                        final String json = printer.omittingInsignificantWhitespace().includingDefaultValueFields()
                                .print(msg.getMessage());
                        sb.append(JSONMessageLogReader.formatPayloadStringForJsonLog(json, false));
                    }
                } else if (msg.hasCustomString()) {
                    sb.append(Constants.SPACE).append(msg.getCustomString().getValue());
                }

                writer.write(sb.toString());
                writer.newLine();
                writer.flush();
            }
        }
        
        return outputFile;
    }

    /**
     * The main method used to convert the binary log file to a human-readable
     * JSON log file.
     * 
     * @param args the parameter arguments. The first argument should be the
     *        source file. The second argument should be the destination folder.
     * @throws Exception if there is a problem reading the file.
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "The converter requires two arguments. The first is the protobuf binary log file path and the second is the destination folder to place the converted human-readable log file.");
        }
        
        // use UMS log4j
        String log4jConfigFileName = PackageUtil.getConfiguration() + "/ums/ums.log4j.properties";
        System.out.println("To configure logging (using log4j), edit "+log4jConfigFileName+" and run again."+
                " Log files will follow the naming convention defined by 'log4j.appender.ums.File', which normally start with 'ums_'.");
        PropertyConfigurator.configureAndWatch(log4jConfigFileName);

        ProtobufMessageLogReader reader = new ProtobufMessageLogReader();
        File outputFile = reader.writeLogAsJSON(new File(args[0]), new File(args[1]));
        System.out.println("Created "+outputFile);
    }
}
