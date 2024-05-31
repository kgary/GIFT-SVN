/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.AfterActionReviewRemediationEventProto;
import mil.arl.gift.common.AfterActionReviewRemediationEvent;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * AfterActionReviewRemediationEvent.
 * 
 * @author cpolynice
 *
 */
public class AfterActionReviewRemediationEventProtoCodec implements
        ProtoCodec<AfterActionReviewRemediationEventProto.AfterActionReviewRemediationEvent, AfterActionReviewRemediationEvent> {

    @Override
    public AfterActionReviewRemediationEvent convert(
            AfterActionReviewRemediationEventProto.AfterActionReviewRemediationEvent protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<String> remediationInfo = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(protoObject.getRemediationListList())) {
            remediationInfo = new ArrayList<>(protoObject.getRemediationListList());
        }

        String courseObjectName = protoObject.hasCourseObjectName() ? protoObject.getCourseObjectName().getValue()
                : AbstractAfterActionReviewEventProtoCodec.LEGACY_COURSE_OBJECT_NAME;

        AfterActionReviewRemediationEvent remediationEvent = new AfterActionReviewRemediationEvent(courseObjectName,
                remediationInfo);
        return remediationEvent;
    }

    @Override
    public AfterActionReviewRemediationEventProto.AfterActionReviewRemediationEvent map(
            AfterActionReviewRemediationEvent commonObject) {
        if (commonObject == null) {
            return null;
        }

        AfterActionReviewRemediationEventProto.AfterActionReviewRemediationEvent.Builder builder = AfterActionReviewRemediationEventProto.AfterActionReviewRemediationEvent
                .newBuilder();
        Optional.ofNullable(commonObject.getRemediationInfo()).ifPresent(builder::addAllRemediationList);
        Optional.ofNullable(commonObject.getCourseObjectName()).ifPresent(courseObjName -> {
            builder.setCourseObjectName(StringValue.of(courseObjName));
        });
        return builder.build();
    }
}
