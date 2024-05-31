/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractAssessmentProto;
import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class will be responsible for protobuf encoding/decoding the base
 * message representation of an Abstract Assessment.
 * 
 * @author cpolynice
 *
 */
public class AbstractAssessmentProtoCodec
        implements ProtoCodec<AbstractAssessmentProto.AbstractAssessment, AbstractAssessment> {

    /* Codec that will be used to convert to/from a Concept Assessment
     * message. */
    private static ConceptAssessmentProtoCodec conceptCodec = new ConceptAssessmentProtoCodec();

    /* Codec that will be used to convert to/from a Task Assessment message. */
    private static TaskAssessmentProtoCodec taskCodec = new TaskAssessmentProtoCodec();

    @Override
    public AbstractAssessment convert(AbstractAssessmentProto.AbstractAssessment protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasConceptAssessment()) {
            return conceptCodec.convert(protoObject.getConceptAssessment());
        } else if (protoObject.hasTaskAssessment()) {
            return taskCodec.convert(protoObject.getTaskAssessment());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractAssessmentProto.AbstractAssessment map(AbstractAssessment commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        AbstractAssessmentProto.AbstractAssessment.Builder builder = AbstractAssessmentProto.AbstractAssessment.newBuilder();
        
        if (commonObject instanceof ConceptAssessment) {
            builder.setConceptAssessment(conceptCodec.map((ConceptAssessment) commonObject));
        } else if (commonObject instanceof TaskAssessment) {
            builder.setTaskAssessment(taskCodec.map((TaskAssessment) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled type of " + commonObject);
        }

        return builder.build();
    }

}
