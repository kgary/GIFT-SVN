/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractDisplayGuidanceTutorRequestProto;
import mil.arl.gift.common.AbstractDisplayGuidanceTutorRequest;
import mil.arl.gift.common.DisplayHtmlPageGuidanceTutorRequest;
import mil.arl.gift.common.DisplayTextGuidanceTutorRequest;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an
 * AbstractDisplayGuidanceTutorRequest message.
 * 
 * @author cpolynice
 *
 */
public class AbstractDisplayGuidanceTutorRequestProtoCodec implements
        ProtoCodec<AbstractDisplayGuidanceTutorRequestProto.AbstractDisplayGuidanceTutorRequest, AbstractDisplayGuidanceTutorRequest> {

    /* Codec that will be used to convert to/from a
     * DisplayHtmlPageGuidanceTutorRequest message. */
    DisplayHtmlPageGuidanceTutorRequestProtoCodec htmlCodec = new DisplayHtmlPageGuidanceTutorRequestProtoCodec();

    /* Codec that will be used to convert to/from a
     * DisplayTextGuidanceTutorRequest message. */
    DisplayTextGuidanceTutorRequestProtoCodec textCodec = new DisplayTextGuidanceTutorRequestProtoCodec();

    @Override
    public AbstractDisplayGuidanceTutorRequest convert(
            AbstractDisplayGuidanceTutorRequestProto.AbstractDisplayGuidanceTutorRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasDisplayHtmlPageGuidanceTutorRequest()) {
            return htmlCodec.convert(protoObject.getDisplayHtmlPageGuidanceTutorRequest());
        } else if (protoObject.hasDisplayTextGuidanceTutorRequest()) {
            return textCodec.convert(protoObject.getDisplayTextGuidanceTutorRequest());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }

    }

    @Override
    public AbstractDisplayGuidanceTutorRequestProto.AbstractDisplayGuidanceTutorRequest map(
            AbstractDisplayGuidanceTutorRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractDisplayGuidanceTutorRequestProto.AbstractDisplayGuidanceTutorRequest.Builder builder = AbstractDisplayGuidanceTutorRequestProto.AbstractDisplayGuidanceTutorRequest
                .newBuilder();

        if (commonObject instanceof DisplayTextGuidanceTutorRequest) {
            builder.setDisplayTextGuidanceTutorRequest(textCodec.map((DisplayTextGuidanceTutorRequest) commonObject));
        } else if (commonObject instanceof DisplayHtmlPageGuidanceTutorRequest) {
            builder.setDisplayHtmlPageGuidanceTutorRequest(
                    htmlCodec.map((DisplayHtmlPageGuidanceTutorRequest) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled guidance type of " + commonObject);
        }

        return builder.build();
    }

}
