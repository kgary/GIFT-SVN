/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.ProtobufMessageProto;
import generated.proto.common.ProtobufMessageProto.ProtobufMessage;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.common.util.ProtobufDefaultsUtil;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.api.message.PayloadDecodeException;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a message class.
 * 
 * @author cpolynice
 */
public class ProtobufMessageProtoCodec implements ProtoCodec<ProtobufMessageProto.ProtobufMessage, Message> {

    /** used to retrieve Proto classes for each message type */
    private static ProtoMapper mapper = null;

    /* Codec that will be used to convert to/from AbstractEnums. */
    private static AbstractEnumObjectProtoCodec enumCodec = null;

    /* Codec that will be used to convert to/from UserSessions. */
    private static UserSessionProtoCodec userSessionCodec = null;

    static {
        mapper = ProtoMapper.getInstance();
        enumCodec = new AbstractEnumObjectProtoCodec();
        userSessionCodec = new UserSessionProtoCodec();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Message convert(ProtobufMessage protoObject) {
        if (protoObject == null) {
            return null;
        }

        int sourceEventId = protoObject.hasSourceEventId() ? protoObject.getSourceEventId().getValue()
                : Message.ID_NOT_AVAILABLE;

        long timestampInMillis = protoObject.hasTimeStamp()
                ? ProtobufConversionUtil.convertTimestampToMillis(protoObject.getTimeStamp())
                : 0;

        ModuleTypeEnum senderModuleType = protoObject.hasSenderModuleType()
                ? ModuleTypeEnum.valueOf(protoObject.getSenderModuleType().getValue())
                : null;
        String senderModuleName = protoObject.hasSenderModuleName()
                ? protoObject.getSenderModuleName().getValue()
                : null;
        String senderQueueName = protoObject.hasSenderQueueName()
                ? protoObject.getSenderQueueName().getValue()
                : null;
        String destinationQueueName = protoObject.hasDestinationQueueName()
                ? protoObject.getDestinationQueueName().getValue()
                : null;

        MessageTypeEnum messageType = protoObject.hasMessageType()
                ? (MessageTypeEnum) enumCodec.convert(protoObject.getMessageType())
                : null;

        int seqNum = protoObject.hasSequenceNumber() ? protoObject.getSequenceNumber().getValue() : Message.UNKNOWN_REPLY_ID;
        int replyToSeqNum = protoObject.hasReplyToSeqNum() ? protoObject.getReplyToSeqNum().getValue()
                : Message.UNKNOWN_REPLY_ID;
        boolean needsACK = protoObject.hasNeedsAck() ? protoObject.getNeedsAck().getValue() : false;

        UserSession userSession = protoObject.hasUserSession() ? userSessionCodec.convert(protoObject.getUserSession())
                : null;

        if (senderModuleType == null) {
            throw new MessageDecodeException("ProtobufMessageProtoCodec", "The sender module type is null");
        } else if (senderModuleName == null) {
            throw new MessageDecodeException("ProtobufMessageProtoCodec", "The sender module name is null");
        } else if (messageType == null) {
            throw new MessageDecodeException("ProtobufMessageProtoCodec", "The message type is null");
        } else if (senderQueueName == null) {
            throw new MessageDecodeException("ProtobufMessageProtoCodec", "The sender queue name is null");
        } else if (destinationQueueName == null) {
            throw new MessageDecodeException("ProtobufMessageProtoCodec", "The destination queue name is null");
        } else if (protoObject.hasDomainSessionId() && userSession == null) {
            throw new MessageDecodeException("ProtobufMessageProtoCodec",
                    "The domain session id is specified but the user id isn't");
        }

        Object payload = null;     
        Exception payloadDecodeException = null;
        
        try {   
            if (payload == null) {
                Class<?> protoClass = mapper.getObjectClass(messageType);
                Class<?> codecClass = mapper.getCodecClass(messageType);
                
                if (protoClass == null || codecClass == null) {
                    throw new Exception(
                            "Unable to find a codec class for message of type " + messageType + ". Is this a new message type?");
                }

                AbstractMessage protoPayload = protoObject.getPayload().unpack(protoClass.asSubclass(AbstractMessage.class));
                payload = ((ProtoCodec<AbstractMessage, Object>) codecClass.getDeclaredConstructor().newInstance()).convert(protoPayload);
            }
        } catch (Exception e) {            
            payloadDecodeException = e;
            if (replyToSeqNum == Message.UNKNOWN_REPLY_ID) {
                throw new MessageDecodeException("ProtobufMessageProtoCodec",
                        "Exception while trying to instantiate payload class = " + mapper.getObjectClass(messageType)
                                + " for message type " + messageType,
                        e);
            }
        }

        Message message;
        if (protoObject.hasDomainSessionId()) {
            message = new DomainSessionMessage(messageType, seqNum, sourceEventId, timestampInMillis, senderModuleName,
                    senderQueueName, senderModuleType, destinationQueueName, payload, userSession,
                    protoObject.getDomainSessionId().getValue(), needsACK);
        } else if (userSession != null) {
            message = new UserSessionMessage(messageType, seqNum, sourceEventId, timestampInMillis, senderModuleName,
                    senderQueueName, senderModuleType, destinationQueueName, payload, userSession, needsACK);
        } else {
            message = new Message(messageType, seqNum, sourceEventId, timestampInMillis, senderModuleName,
                    senderQueueName, senderModuleType, destinationQueueName, payload, needsACK);
        }

        
        if (replyToSeqNum != Message.UNKNOWN_REPLY_ID) {
            message.setReplyToSequenceNumber(replyToSeqNum);

            if (payloadDecodeException != null) {
                throw new PayloadDecodeException("ProtobufMessageProtoCodec", message, payloadDecodeException);
            }
        }

        return message;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ProtobufMessage map(Message commonObject) {
        if (commonObject == null) {
            return null;
        }

        ProtobufMessageProto.ProtobufMessage.Builder builder = ProtobufMessageProto.ProtobufMessage.newBuilder();

        MessageTypeEnum type = commonObject.getMessageType();
        Object payload = commonObject.getPayload();
        
        try {
            Class<?> protoClass = mapper.getObjectClass(type);
            Class<?> codecClass = mapper.getCodecClass(type);

            if (protoClass == null || codecClass == null) {
                throw new Exception(
                        "Unable to find a codec class for message of type " + type + ". Is this a new message type?");
            }

            AbstractMessage message = ((ProtoCodec<AbstractMessage, Object>) codecClass.getDeclaredConstructor().newInstance()).map(payload);
            builder.setPayload(Any.pack(message));
        } catch (Exception e) {
            /* It's possible that we encountered a legacy message here. Update the payload type to the current 
             * version of the message compatible with the protobuf codecs. */
            AbstractMessage legacyMessage = ProtobufDefaultsUtil.updateLegacyMessage(type, payload);
   
            /* If we still were not able to update the message, throw the original exception reported. */
            if (legacyMessage == null) {
                throw new MessageEncodeException(this.getClass().getName(), "Exception logged while encoding", e);
            }
            
            /* Pack the updated legacy payload inside the protobuf message. */
            builder.setPayload(Any.pack(legacyMessage));
        }

        builder.setSequenceNumber(Int32Value.of(commonObject.getSequenceNumber()));
        builder.setSourceEventId(Int32Value.of(commonObject.getSourceEventId()));
        builder.setNeedsAck(BoolValue.of(commonObject.needsHandlingResponse()));
        Optional.ofNullable(ProtobufConversionUtil.convertMillisToTimestamp(commonObject.getTimeStamp()))
                .ifPresent(builder::setTimeStamp);
        Optional.ofNullable(commonObject.getSenderModuleType().toString()).ifPresent(senderModuleType -> {
            builder.setSenderModuleType(StringValue.of(senderModuleType));
        });
        
        Optional.ofNullable(commonObject.getSenderModuleName()).ifPresent(senderModuleName -> {
            builder.setSenderModuleName(StringValue.of(senderModuleName));
        });
        
        Optional.ofNullable(commonObject.getDestinationQueueName()).ifPresent(destinationQueueName -> {
            builder.setDestinationQueueName(StringValue.of(destinationQueueName));
        });
        
        Optional.ofNullable(commonObject.getSenderAddress()).ifPresent(senderAddress -> {
            builder.setSenderQueueName(StringValue.of(senderAddress));
        });
        
        builder.setMessageType(enumCodec.map(commonObject.getMessageType()));

        if (commonObject.isReplyMessage()) {
            builder.setReplyToSeqNum(Int32Value.of(commonObject.getReplyToSequenceNumber()));
        }

        if (commonObject instanceof DomainSessionMessage) {
            builder.setDomainSessionId(Int32Value.of(((DomainSessionMessage) commonObject).getDomainSessionId()));
            Optional.ofNullable(userSessionCodec.map(((UserSessionMessage) commonObject).getUserSession()))
                    .ifPresent(builder::setUserSession);
        } else if (commonObject instanceof UserSessionMessage) {
            Optional.ofNullable(userSessionCodec.map(((UserSessionMessage) commonObject).getUserSession()))
                    .ifPresent(builder::setUserSession);
        }

        return builder.build();
    }
}
