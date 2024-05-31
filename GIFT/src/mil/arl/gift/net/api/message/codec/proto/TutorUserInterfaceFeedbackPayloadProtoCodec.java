/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.TutorUserInterfaceFeedbackPayloadProto.TutorUserInterfaceFeedbackPayload;
import mil.arl.gift.common.ClearTextAction;
import mil.arl.gift.common.DisplayAvatarAction;
import mil.arl.gift.common.DisplayHTMLFeedbackAction;
import mil.arl.gift.common.DisplayTextAction;
import mil.arl.gift.common.PlayAudioAction;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * TutorUserInterfaceFeedbackPayload.
 * 
 * @author cpolynice
 *
 */
public class TutorUserInterfaceFeedbackPayloadProtoCodec
        implements ProtoCodec<TutorUserInterfaceFeedbackPayload, TutorUserInterfaceFeedback> {

    /* Codec that will be used to convert to/from a protobuf
     * DisplayTextAction. */
    private static final DisplayTextActionProtoCodec displayTextCodec = new DisplayTextActionProtoCodec();

    /* Codec that will be used to convert to/from a protobuf PlayAudioAction. */
    private static final PlayAudioActionProtoCodec playAudioCodec = new PlayAudioActionProtoCodec();

    /* Codec that will be used to convert to/from a protobuf
     * DisplayAvatarAction. */
    private static final DisplayAvatarActionProtoCodec displayAvatarCodec = new DisplayAvatarActionProtoCodec();

    /* Codec that will be used to convert to/from a protobuf ClearTextAction. */
    private static final ClearTextActionProtoCodec clearTextCodec = new ClearTextActionProtoCodec();

    /* Codec that will be used to convert to/from a protobuf
     * DisplayHTMLFeedbackAction. */
    private static final DisplayHTMLFeedbackActionProtoCodec displayHTMLCodec = new DisplayHTMLFeedbackActionProtoCodec();

    @Override
    public TutorUserInterfaceFeedback convert(TutorUserInterfaceFeedbackPayload protoObject) {
        if (protoObject == null) {
            return null;
        }

        DisplayTextAction displayTextAction = protoObject.hasDisplayTextAction()
                ? displayTextCodec.convert(protoObject.getDisplayTextAction())
                : null;
        PlayAudioAction playAudioAction = protoObject.hasPlayAudioAction()
                ? playAudioCodec.convert(protoObject.getPlayAudioAction())
                : null;
        DisplayAvatarAction displayAvatarAction = protoObject.hasDisplayAvatarAction()
                ? displayAvatarCodec.convert(protoObject.getDisplayAvatarAction())
                : null;
        ClearTextAction clearTextAction = protoObject.hasClearTextAction()
                ? clearTextCodec.convert(protoObject.getClearTextAction())
                : null;
        DisplayHTMLFeedbackAction displayHTMLAction = protoObject.hasDisplayHtmlFeedbackAction()
                ? displayHTMLCodec.convert(protoObject.getDisplayHtmlFeedbackAction())
                : null;

        return new TutorUserInterfaceFeedback(displayTextAction, playAudioAction, displayAvatarAction, clearTextAction,
                displayHTMLAction);
    }

    @Override
    public TutorUserInterfaceFeedbackPayload map(TutorUserInterfaceFeedback commonObject) {
        if (commonObject == null) {
            return null;
        }

        TutorUserInterfaceFeedbackPayload.Builder builder = TutorUserInterfaceFeedbackPayload.newBuilder();

        Optional.ofNullable(displayTextCodec.map(commonObject.getDisplayTextAction()))
                .ifPresent(builder::setDisplayTextAction);
        Optional.ofNullable(playAudioCodec.map(commonObject.getPlayAudioAction()))
                .ifPresent(builder::setPlayAudioAction);
        Optional.ofNullable(displayAvatarCodec.map(commonObject.getDisplayAvatarAction()))
                .ifPresent(builder::setDisplayAvatarAction);
        Optional.ofNullable(clearTextCodec.map(commonObject.getClearTextAction()))
                .ifPresent(builder::setClearTextAction);
        Optional.ofNullable(displayHTMLCodec.map(commonObject.getDisplayHTMLAction()))
                .ifPresent(builder::setDisplayHtmlFeedbackAction);

        return builder.build();
    }
}
