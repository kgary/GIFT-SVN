/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.course.Concepts;
import generated.proto.common.PublishLessonScoreProto;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.PublishLessonScore;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is used to convert to/from a protobuf PublishLessonScore.
 * 
 * @author cpolynice
 *
 */
public class PublishLessonScoreProtoCodec
        implements ProtoCodec<PublishLessonScoreProto.PublishLessonScore, PublishLessonScore> {

    /* Codec that will be used to comvert to/from a protobuf LMSCourseRecord. */
    private static final LMSCourseRecordProtoCodec codec = new LMSCourseRecordProtoCodec();

    @Override
    public PublishLessonScore convert(PublishLessonScoreProto.PublishLessonScore protoObject) {
        if (protoObject == null) {
            return null;
        }

        String lmsUsername = protoObject.hasLmsUsername() ? protoObject.getLmsUsername().getValue() : null;
        LMSCourseRecord record = protoObject.hasCourseData() ? codec.convert(protoObject.getCourseData()) : null;
        Concepts.Hierarchy rootConcept = null;

        if (protoObject.hasRootConcept()) {
            try {
                rootConcept = new Concepts.Hierarchy();
                rootConcept.setConceptNode(
                        PublishLessonScore.getConceptsFromXMLString(protoObject.getRootConcept().getValue()));

            } catch (Exception e) {
                throw new MessageEncodeException(this.getClass().getName(), "Exception logged while decoding", e);
            }
        }
        
        return new PublishLessonScore(lmsUsername, record, rootConcept);
    }

    @Override
    public PublishLessonScoreProto.PublishLessonScore map(PublishLessonScore commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        PublishLessonScoreProto.PublishLessonScore.Builder builder = PublishLessonScoreProto.PublishLessonScore.newBuilder();
        
        Optional.ofNullable(codec.map(commonObject.getCourseData())).ifPresent(builder::setCourseData);
        Optional.ofNullable(commonObject.getLmsUsername()).ifPresent(username -> {
            builder.setLmsUsername(StringValue.of(username));
        });

        if (commonObject.getConcepts() != null && commonObject.getConcepts().getConceptNode() != null) {
            try {
                builder.setRootConcept(StringValue
                        .of(PublishLessonScore.getConceptsAsXMLString(commonObject.getConcepts().getConceptNode())));
            } catch (Exception e) {
                throw new MessageEncodeException(this.getClass().getName(), "Exception logged while encoding", e);
            }
        }

        return builder.build();
    }
}
