/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractDisplayContentTutorRequestProto;
import mil.arl.gift.common.DisplayMessageTutorRequest;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DisplayMessageTutorRequest message.
 * 
 * @author cpolynice
 *
 */
public class DisplayMessageTutorRequestProtoCodec implements
        ProtoCodec<AbstractDisplayContentTutorRequestProto.DisplayMessageTutorRequest, DisplayMessageTutorRequest> {

    @Override
    public DisplayMessageTutorRequest convert(
            AbstractDisplayContentTutorRequestProto.DisplayMessageTutorRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            DisplayMessageTutorRequest displayGuidanceTutorRequest = new DisplayMessageTutorRequest();

            if (protoObject.hasGuidance()) {
                String xmlStr = protoObject.getGuidance().getValue();
                UnmarshalledFile uFile = AbstractSchemaHandler.getFromXMLString(xmlStr, generated.course.Guidance.class,
                        null, true);
                generated.course.Guidance guidance = (generated.course.Guidance) uFile.getUnmarshalled();
                displayGuidanceTutorRequest.setGuidance(guidance);
            }

            if (protoObject.hasDisplayDuration()) {
                displayGuidanceTutorRequest.setDisplayDuration(protoObject.getDisplayDuration().getValue());
            }

            if (protoObject.hasWhileTaLoads()) {
                displayGuidanceTutorRequest.setWhileTrainingAppLoads(protoObject.getWhileTaLoads().getValue());
            }

            return displayGuidanceTutorRequest;

        } catch (@SuppressWarnings("unused") Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }
    }

    @Override
    public AbstractDisplayContentTutorRequestProto.DisplayMessageTutorRequest map(
            DisplayMessageTutorRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractDisplayContentTutorRequestProto.DisplayMessageTutorRequest.Builder builder = AbstractDisplayContentTutorRequestProto.DisplayMessageTutorRequest
                .newBuilder();

        Optional.ofNullable(commonObject.getGuidance()).ifPresent(guidance -> {
            try {
                builder.setGuidance(StringValue.of(AbstractSchemaHandler.getAsXMLString(guidance,
                        generated.course.Guidance.class, AbstractSchemaHandler.COURSE_SCHEMA_FILE)));
            } catch (SAXException | JAXBException e) {
                throw new MessageEncodeException(this.getClass().getName(), e.getMessage(), e);
            }
        });

        builder.setDisplayDuration(Int32Value.of(commonObject.getDisplayDuration()));
        builder.setWhileTaLoads(BoolValue.of(commonObject.isWhileTrainingAppLoads()));

        return builder.build();
    }

}
