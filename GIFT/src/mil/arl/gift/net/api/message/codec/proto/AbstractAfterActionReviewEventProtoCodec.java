/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.AbstractAfterActionReviewEventProto;
import mil.arl.gift.common.AbstractAfterActionReviewEvent;
import mil.arl.gift.common.AfterActionReviewCourseEvent;
import mil.arl.gift.common.AfterActionReviewRemediationEvent;
import mil.arl.gift.common.AfterActionReviewSurveyEvent;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * AbstractAfterActionReviewEvent.
 * 
 * @author cpolynice
 *
 */
public class AbstractAfterActionReviewEventProtoCodec implements
        ProtoCodec<AbstractAfterActionReviewEventProto.AbstractAfterActionReviewEvent, AbstractAfterActionReviewEvent> {

    static final String LEGACY_COURSE_OBJECT_NAME = "LEGACY - NOT PROVIDED";

    /* Codec that will be used to convert to/from a protobuf {@Link
     * AfterActionReviewCourseEvent}. */
    private static AfterActionReviewCourseEventProtoCodec courseCodec = new AfterActionReviewCourseEventProtoCodec();

    /* Codec that will be used to convert to/from a protobuf {@Link
     * AfterActionReviewSurveyEvent}. */
    private static AfterActionReviewSurveyEventProtoCodec surveyCodec = new AfterActionReviewSurveyEventProtoCodec();

    /* Codec that will be used to convert to/from a protobuf {@Link
     * AfterActionReviewRemediationEvent}. */
    private static AfterActionReviewRemediationEventProtoCodec remediationCodec = new AfterActionReviewRemediationEventProtoCodec();

    @Override
    public AbstractAfterActionReviewEvent convert(
            AbstractAfterActionReviewEventProto.AbstractAfterActionReviewEvent protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasAfterActionReviewCourseEvent()) {
            return courseCodec.convert(protoObject.getAfterActionReviewCourseEvent());
        } else if (protoObject.hasAfterActionReviewSurveyEvent()) {
            return surveyCodec.convert(protoObject.getAfterActionReviewSurveyEvent());
        } else if (protoObject.hasAfterActionReviewRemediationEvent()) {
            return remediationCodec.convert(protoObject.getAfterActionReviewRemediationEvent());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractAfterActionReviewEventProto.AbstractAfterActionReviewEvent map(
            AbstractAfterActionReviewEvent commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractAfterActionReviewEventProto.AbstractAfterActionReviewEvent.Builder builder = AbstractAfterActionReviewEventProto.AbstractAfterActionReviewEvent
                .newBuilder();

        if (commonObject instanceof AfterActionReviewCourseEvent) {
            Optional.ofNullable(courseCodec.map((AfterActionReviewCourseEvent) commonObject))
                    .ifPresent(builder::setAfterActionReviewCourseEvent);
        } else if (commonObject instanceof AfterActionReviewSurveyEvent) {
            Optional.ofNullable(surveyCodec.map((AfterActionReviewSurveyEvent) commonObject))
                    .ifPresent(builder::setAfterActionReviewSurveyEvent);
        } else if (commonObject instanceof AfterActionReviewRemediationEvent) {
            Optional.ofNullable(remediationCodec.map((AfterActionReviewRemediationEvent) commonObject))
                    .ifPresent(builder::setAfterActionReviewRemediationEvent);
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled type of " + commonObject);
        }

        return builder.build();
    }

}
