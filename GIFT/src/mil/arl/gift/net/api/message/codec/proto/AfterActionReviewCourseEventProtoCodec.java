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

import generated.proto.common.AfterActionReviewCourseEventProto;
import mil.arl.gift.common.AfterActionReviewCourseEvent;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * AfterActionReviewCourseEvent.
 * 
 * @author cpolynice
 *
 */
public class AfterActionReviewCourseEventProtoCodec implements
        ProtoCodec<AfterActionReviewCourseEventProto.AfterActionReviewCourseEvent, AfterActionReviewCourseEvent> {

    private static GradedScoreNodeProtoCodec codec = new GradedScoreNodeProtoCodec();

    @Override
    public AfterActionReviewCourseEvent convert(
            AfterActionReviewCourseEventProto.AfterActionReviewCourseEvent protoObject) {
        if (protoObject == null) {
            return null;
        }

        GradedScoreNode scoreNode = null;

        if (protoObject.hasGradedScoreNode()) {
            scoreNode = codec.convert(protoObject.getGradedScoreNode());
        }

        String courseObjectName = protoObject.hasCourseObjectName() ? protoObject.getCourseObjectName().getValue()
                : AbstractAfterActionReviewEventProtoCodec.LEGACY_COURSE_OBJECT_NAME;

        return new AfterActionReviewCourseEvent(courseObjectName, scoreNode);
    }

    @Override
    public AfterActionReviewCourseEventProto.AfterActionReviewCourseEvent map(
            AfterActionReviewCourseEvent commonObject) {
        if (commonObject == null) {
            return null;
        }

        AfterActionReviewCourseEventProto.AfterActionReviewCourseEvent.Builder builder = AfterActionReviewCourseEventProto.AfterActionReviewCourseEvent
                .newBuilder();

        Optional.ofNullable(codec.map(commonObject.getScore())).ifPresent(builder::setGradedScoreNode);
        Optional.ofNullable(commonObject.getCourseObjectName()).ifPresent(courseObjName -> {
            builder.setCourseObjectName(StringValue.of(courseObjName));
        });
        return builder.build();
    }

}
