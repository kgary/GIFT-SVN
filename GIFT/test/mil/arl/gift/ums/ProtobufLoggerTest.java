/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.EmptyPayloadProto;
import generated.proto.common.NACKProto;
import generated.proto.common.ProtobufLogMessageProto.ProtobufLogMessage;
import generated.proto.common.ProtobufMessageProto.ProtobufMessage;
import generated.proto.common.UserSessionProto;
import generated.proto.common.UserSessionProto.UserSession;
import generated.proto.common.survey.SubmitSurveyResultsProto.SubmitSurveyResults;
import generated.proto.common.survey.SurveyResponseProto.SurveyResponse;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.logger.ProtobufMessageLogReader;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.api.message.codec.proto.AbstractEnumObjectProtoCodec;
import mil.arl.gift.net.api.message.codec.proto.ProtobufMessageProtoCodec;
import mil.arl.gift.ums.logger.DomainSessionLogger;

/**
 * Tester for the protobuf binary to human readable converter.
 * 
 * @author sharrison, cpolynice, oamer
 */
public class ProtobufLoggerTest {
    /** The protobuf log output for domain session logs */
    private static File protoLogFile;

    /**
     * Deletes the domain session files created in this test.
     */
    @AfterClass
    public static void deleteDomainSessionFiles() {
        if (protoLogFile != null && protoLogFile.exists()) {
            protoLogFile.delete();
        }
    }

    /**
     * Creates .proto.bin file with three messages in output/logger/message.
     */
    @Test
    public void encodeDecodeProtobufTest() {
        UserSessionProto.UserSession userSession = UserSession.newBuilder().setUserId(Int32Value.of(1))
                .setSessionType(StringValue.of(UserSessionType.GIFT_USER.name())).build();
        
        /** Build the messages. */
        ProtobufMessage.Builder msgBuilder = ProtobufMessage.newBuilder();
        msgBuilder.setSequenceNumber(Int32Value.of(1));
        msgBuilder.setSourceEventId(Int32Value.of(10));
        msgBuilder.setReplyToSeqNum(Int32Value.of(10));
        msgBuilder.setDomainSessionId(Int32Value.of(10));
        msgBuilder.setUserSession(userSession);
        msgBuilder.setTimeStamp(ProtobufConversionUtil.convertDateToTimestamp(new Date()));
        msgBuilder.setNeedsAck(BoolValue.of(true));
        msgBuilder.setMessageType(new AbstractEnumObjectProtoCodec().map(MessageTypeEnum.NACK));
        msgBuilder.setSenderModuleName(StringValue.of("Test"));
        msgBuilder.setSenderModuleType(StringValue.of(ModuleTypeEnum.DOMAIN_MODULE.getName()));
        msgBuilder.setSenderQueueName(StringValue.of("Test Queue"));
        msgBuilder.setDestinationQueueName(StringValue.of("Test Destination 1"));
        msgBuilder.setPayload(Any.pack(NACKProto.NACK.newBuilder().setErrorEnum(StringValue.of(ErrorEnum.DB_INSERT_ERROR.getName())).setErrorMessage(StringValue.of("Error Message 1"))
                .setErrorHelp(StringValue.of("Error Help 1")).build()));
        
        ProtobufMessage.Builder msgBuilder2 = ProtobufMessage.newBuilder();
        msgBuilder2.setSequenceNumber(Int32Value.of(1000));
        msgBuilder2.setSourceEventId(Int32Value.of(10));
        msgBuilder2.setReplyToSeqNum(Int32Value.of(10));
        msgBuilder2.setDomainSessionId(Int32Value.of(10));
        msgBuilder2.setUserSession(userSession);
        msgBuilder2.setTimeStamp(ProtobufConversionUtil.convertDateToTimestamp(new Date()));
        msgBuilder2.setNeedsAck(BoolValue.of(true));
        msgBuilder2.setMessageType(new AbstractEnumObjectProtoCodec().map(MessageTypeEnum.ACK));
        msgBuilder2.setSenderModuleName(StringValue.of("Test 2"));
        msgBuilder2.setSenderModuleType(StringValue.of(ModuleTypeEnum.DOMAIN_MODULE.getName()));
        msgBuilder2.setSenderQueueName(StringValue.of("Test Queue 2"));
        msgBuilder2.setDestinationQueueName(StringValue.of("Test Destination 2"));
        msgBuilder2.setPayload(Any.pack(EmptyPayloadProto.EmptyPayload.getDefaultInstance()));

        ProtobufMessage.Builder msgBuilder3 = ProtobufMessage.newBuilder();
        msgBuilder3.setSequenceNumber(Int32Value.of(5000));
        msgBuilder3.setSourceEventId(Int32Value.of(20));
        msgBuilder3.setReplyToSeqNum(Int32Value.of(30));
        msgBuilder3.setDomainSessionId(Int32Value.of(10));
        msgBuilder3.setUserSession(userSession);
        msgBuilder3.setTimeStamp(ProtobufConversionUtil.convertDateToTimestamp(new Date()));
        msgBuilder3.setNeedsAck(BoolValue.of(true));
        msgBuilder3.setMessageType(new AbstractEnumObjectProtoCodec().map(MessageTypeEnum.SUBMIT_SURVEY_RESULTS));
        msgBuilder3.setSenderModuleName(StringValue.of("Test 3"));
        msgBuilder3.setSenderModuleType(StringValue.of(ModuleTypeEnum.DOMAIN_MODULE.getName()));
        msgBuilder3.setSenderQueueName(StringValue.of("Test Queue 3"));
        msgBuilder3.setDestinationQueueName(StringValue.of("Test Destination 3"));

        /** Create nested message attributes. */
        SubmitSurveyResults.Builder surveyBuilder = SubmitSurveyResults.newBuilder();
        surveyBuilder.setGiftKey(StringValue.of("TestKey210")).setCourseName(StringValue.of("Test Name 1"));
        SurveyResponse.Builder survBuilder = SurveyResponse.newBuilder();
        survBuilder.setSurveyId(Int32Value.of(500)).setSurveyResponseId(Int32Value.of(1000)).setSurveyContextId(Int32Value.of(5000))
                .setHasFillInTheBlankQuestionWithIdealAnswer(BoolValue.of(false))
                .setSurveyType(StringValue.of(SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK.name()))
                .build();
        surveyBuilder.setSurveyResponse(survBuilder);

        msgBuilder3.setPayload(Any.pack(surveyBuilder.build()));

        /**
         * Create a new logger instance so the protobuf handleMessage() methods
         * can be called. Write the created messages to a file.
         */
        DomainSessionLogger lm = new DomainSessionLogger(new DomainSession(0, 0, "Test", "Test"));
        ProtobufMessageProtoCodec codec = new ProtobufMessageProtoCodec();
        lm.handleMessage(codec.convert(msgBuilder.build()), null, MessageEncodingTypeEnum.BINARY);
        lm.handleMessage(codec.convert(msgBuilder2.build()), null, MessageEncodingTypeEnum.BINARY);
        lm.handleMessage(codec.convert(msgBuilder3.build()), null, MessageEncodingTypeEnum.BINARY);

        try {
            lm.close();
        } catch (IOException e) {
            fail("Caught exception trying to close the logger: " + e);
        }

        protoLogFile = lm.getFile();

        assertTrue(lm.getFile().exists());

        /**
         * Test that the data was decoded correctly. If only running the test to
         * generate a protobuf logger file, delete the lines below.
         */
        List<ProtobufLogMessage> messages = null;
        try {
            ProtobufMessageLogReader reader = new ProtobufMessageLogReader();
            reader.parseLog(new FileProxy(new File(lm.getFile().getAbsolutePath())));
            messages = reader.getOriginalLogMessages();
        } catch (Exception e) {
            fail("Caught exception parsing the proto file: " + e);
        }

        assertTrue(messages != null);
        assertTrue(messages.size() == 3);
        assertTrue(messages.get(0).getMessage().hasPayload());

        assertTrue(messages.get(1).getMessage().hasPayload());
        assertEquals(messages.get(1).getMessage().getMessageType().getEnumName().getValue(),
                MessageTypeEnum.ACK.getName());

        assertTrue(messages.get(2).getMessage().hasPayload());
        assertEquals(messages.get(2).getMessage().getSequenceNumber(), msgBuilder3.getSequenceNumber());
    }
}
