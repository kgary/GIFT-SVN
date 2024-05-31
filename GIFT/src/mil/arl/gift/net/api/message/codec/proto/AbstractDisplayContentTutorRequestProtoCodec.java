/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractDisplayContentTutorRequestProto;
import mil.arl.gift.common.AbstractDisplayContentTutorRequest;
import mil.arl.gift.common.DisplayMediaTutorRequest;
import mil.arl.gift.common.DisplayMessageTutorRequest;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * AbstractDisplayContentTutorRequest message.
 * 
 * @author cpolynice
 *
 */
public class AbstractDisplayContentTutorRequestProtoCodec implements
        ProtoCodec<AbstractDisplayContentTutorRequestProto.AbstractDisplayContentTutorRequest, AbstractDisplayContentTutorRequest> {

    /* Codec that will be used to convert to/from a DisplayMessageTutorRequest
     * instance. */
    private static DisplayMessageTutorRequestProtoCodec messageCodec = new DisplayMessageTutorRequestProtoCodec();

    /* Codec that will be used to convert to/from a DisplayMediaTutorRequest
     * instance. */
    private static DisplayMediaTutorRequestProtoCodec mediaCodec = new DisplayMediaTutorRequestProtoCodec();

    @Override
    public AbstractDisplayContentTutorRequest convert(
            AbstractDisplayContentTutorRequestProto.AbstractDisplayContentTutorRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasDisplayMessageTutorRequest()) {
            return messageCodec.convert(protoObject.getDisplayMessageTutorRequest());
        } else if (protoObject.hasDisplayMediaTutorRequest()) {
            return mediaCodec.convert(protoObject.getDisplayMediaTutorRequest());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractDisplayContentTutorRequestProto.AbstractDisplayContentTutorRequest map(
            AbstractDisplayContentTutorRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractDisplayContentTutorRequestProto.AbstractDisplayContentTutorRequest.Builder builder = AbstractDisplayContentTutorRequestProto.AbstractDisplayContentTutorRequest
                .newBuilder();

        if (commonObject instanceof DisplayMessageTutorRequest) {
            builder.setDisplayMessageTutorRequest(messageCodec.map((DisplayMessageTutorRequest) commonObject));
        } else if (commonObject instanceof DisplayMediaTutorRequest) {
            builder.setDisplayMediaTutorRequest(mediaCodec.map((DisplayMediaTutorRequest) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled type of " + commonObject);
        }

        return builder.build();
    }

}
