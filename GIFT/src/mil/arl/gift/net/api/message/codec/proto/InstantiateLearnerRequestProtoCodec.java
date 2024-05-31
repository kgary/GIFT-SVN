/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.io.Serializable;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.InstantiateLearnerRequestProto;
import mil.arl.gift.common.InstantiateLearnerRequest;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * InstantiateLearnerRequest.
 * 
 * @author cpolynice
 *
 */
public class InstantiateLearnerRequestProtoCodec
        implements ProtoCodec<InstantiateLearnerRequestProto.InstantiateLearnerRequest, InstantiateLearnerRequest> {

    @Override
    public InstantiateLearnerRequest convert(InstantiateLearnerRequestProto.InstantiateLearnerRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        InstantiateLearnerRequest learnerRequest;
        if (protoObject.hasLmsUsername()) {
            learnerRequest = new InstantiateLearnerRequest(protoObject.getLmsUsername().getValue());
        } else {
            learnerRequest = new InstantiateLearnerRequest();
        }

        if (protoObject.hasCourseConcepts()) {
            /* new May 2021 */
            String xmlConcepts = protoObject.getCourseConcepts().getValue();
            UnmarshalledFile uFile;
            try {
                uFile = AbstractSchemaHandler.getFromXMLString(xmlConcepts, generated.course.Concepts.class,
                        AbstractSchemaHandler.COURSE_SCHEMA_FILE, false);
                Serializable decodedObj = uFile.getUnmarshalled();
                learnerRequest.setCourseConcepts((generated.course.Concepts) decodedObj);
            } catch (Exception e) {
                throw new MessageDecodeException(this.getClass().getName(),
                        "Exception logged while decoding instantiate learner request object", e);
            }
        }

        learnerRequest
                .setLearnerConfig(protoObject.hasConfiguration() ? protoObject.getConfiguration().getValue() : null);
        return learnerRequest;
    }

    @Override
    public InstantiateLearnerRequestProto.InstantiateLearnerRequest map(InstantiateLearnerRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        InstantiateLearnerRequestProto.InstantiateLearnerRequest.Builder builder = InstantiateLearnerRequestProto.InstantiateLearnerRequest
                .newBuilder();

        Optional.ofNullable(commonObject.getLMSUserName()).ifPresent(username -> {
            builder.setLmsUsername(StringValue.of(username));
        });
        Optional.ofNullable(commonObject.getLearnerConfig()).ifPresent(config -> {
            builder.setConfiguration(StringValue.of(config));
        });

        generated.course.Concepts courseConcepts = commonObject.getCourseConcepts();
        if (courseConcepts != null && courseConcepts.getListOrHierarchy() != null) {
            try {
                Optional.ofNullable(AbstractSchemaHandler.getAsXMLString(courseConcepts,
                        generated.course.Concepts.class, AbstractSchemaHandler.COURSE_SCHEMA_FILE))
                        .ifPresent(concepts -> {
                            builder.setCourseConcepts(StringValue.of(concepts));
                        });
            } catch (Exception e) {
                throw new MessageEncodeException(InstantiateLearnerRequest.class.getName(),
                        "Failed to encode the string representation of the course concepts", e);
            }
        }

        return builder.build();
    }

}
