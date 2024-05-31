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

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractAfterActionReviewEventProto;
import generated.proto.common.DisplayAARTutorRequestProto.DisplayAARTutorRequest;
import mil.arl.gift.common.AbstractAfterActionReviewEvent;
import mil.arl.gift.common.DisplayAfterActionReviewTutorRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DisplayAARTutorRequest.
 * 
 * @author cpolynice
 *
 */
public class DisplayAARTutorRequestProtoCodec
        implements ProtoCodec<DisplayAARTutorRequest, DisplayAfterActionReviewTutorRequest> {

    /* Default title for the request. */
    private static final String DEFAULT_TITLE = "Structured Review";

    /* Codec that will be used to convert to/from AbstractAfterActionReviewEvent
     * instances. */
    private static AbstractAfterActionReviewEventProtoCodec codec = new AbstractAfterActionReviewEventProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the converted common object list.
     */
    private static List<AbstractAfterActionReviewEvent> convertList(
            List<AbstractAfterActionReviewEventProto.AbstractAfterActionReviewEvent> protoList) {
        if (protoList == null) {
            return null;
        }

        List<AbstractAfterActionReviewEvent> commonList = new ArrayList<>();

        for (AbstractAfterActionReviewEventProto.AbstractAfterActionReviewEvent event : protoList) {
            commonList.add(codec.convert(event));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf representation.
     * 
     * @param commonList the common object list
     * @return the mapped protobuf list.
     */
    private static List<AbstractAfterActionReviewEventProto.AbstractAfterActionReviewEvent> mapList(
            List<AbstractAfterActionReviewEvent> commonList) {
        if (commonList == null) {
            return null;
        }

        List<AbstractAfterActionReviewEventProto.AbstractAfterActionReviewEvent> protoList = new ArrayList<>();

        for (AbstractAfterActionReviewEvent event : commonList) {
            protoList.add(codec.map(event));
        }

        return protoList;
    }

    @Override
    public DisplayAfterActionReviewTutorRequest convert(DisplayAARTutorRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<AbstractAfterActionReviewEvent> events = convertList(
                CollectionUtils.isNotEmpty(protoObject.getEventsList()) ? protoObject.getEventsList()
                        : new ArrayList<>());
        String title = protoObject.hasTitle() ? protoObject.getTitle().getValue() : null;

        if (title == null || title.isEmpty()) {
            title = DEFAULT_TITLE;
        }

        DisplayAfterActionReviewTutorRequest request = new DisplayAfterActionReviewTutorRequest(title, events);
        if (protoObject.hasFullscreen()) {
            request.setFullScreen(protoObject.getFullscreen().getValue());
        }

        return request;
    }

    @Override
    public DisplayAARTutorRequest map(DisplayAfterActionReviewTutorRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        DisplayAARTutorRequest.Builder builder = DisplayAARTutorRequest.newBuilder();

        builder.setFullscreen(BoolValue.of(commonObject.getFullScreen()));
        Optional.ofNullable(mapList(commonObject.getEvents())).ifPresent(builder::addAllEvents);
        Optional.ofNullable(commonObject.getTitle()).ifPresent(title -> {
            builder.setTitle(StringValue.of(title));
        });

        return builder.build();
    }
}
