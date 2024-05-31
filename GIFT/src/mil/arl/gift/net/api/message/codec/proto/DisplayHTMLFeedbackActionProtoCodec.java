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

import generated.proto.common.DisplayHTMLFeedbackActionProto;
import mil.arl.gift.common.DisplayHTMLFeedbackAction;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a DisplayHTMLFeedbackAction
 * instance.
 * 
 * @author cpolynice
 *
 */
public class DisplayHTMLFeedbackActionProtoCodec
        implements ProtoCodec<DisplayHTMLFeedbackActionProto.DisplayHTMLFeedbackAction, DisplayHTMLFeedbackAction> {

    @Override
    public DisplayHTMLFeedbackAction convert(DisplayHTMLFeedbackActionProto.DisplayHTMLFeedbackAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        return new DisplayHTMLFeedbackAction(protoObject.hasUrl() ? protoObject.getUrl().getValue() : null);
    }

    @Override
    public DisplayHTMLFeedbackActionProto.DisplayHTMLFeedbackAction map(DisplayHTMLFeedbackAction commonObject) {
        if (commonObject == null) {
            return null;
        }

        DisplayHTMLFeedbackActionProto.DisplayHTMLFeedbackAction.Builder builder = DisplayHTMLFeedbackActionProto.DisplayHTMLFeedbackAction
                .newBuilder();
        Optional.ofNullable(commonObject.getDomainURL()).ifPresent(url -> {
            builder.setUrl(StringValue.of(url));
        });

        return builder.build();
    }
}
