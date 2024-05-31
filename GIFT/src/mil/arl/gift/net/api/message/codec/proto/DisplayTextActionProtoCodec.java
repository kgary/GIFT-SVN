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

import generated.dkf.InTutor;
import generated.proto.common.DisplayTextActionProto;
import mil.arl.gift.common.DisplayTextAction;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DisplayTextAction.
 * 
 * @author cpolynice
 *
 */
public class DisplayTextActionProtoCodec
        implements ProtoCodec<DisplayTextActionProto.DisplayTextAction, DisplayTextAction> {

    @Override
    public DisplayTextAction convert(DisplayTextActionProto.DisplayTextAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            String text = protoObject.hasText() ? protoObject.getText().getValue() : null;
            DisplayTextAction action = new DisplayTextAction(text);

            /* Optional. */
            if (protoObject.hasDeliverySettings()) {
                UnmarshalledFile uFile = AbstractSchemaHandler.getFromXMLString(
                        protoObject.getDeliverySettings().getValue(), InTutor.class,
                        AbstractSchemaHandler.DKF_SCHEMA_FILE, true);
                InTutor deliverySettings = (InTutor) uFile.getUnmarshalled();
                action.setDeliverySettings(deliverySettings);
            }

            return action;
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }
    }

    @Override
    public DisplayTextActionProto.DisplayTextAction map(DisplayTextAction commonObject) {
        if (commonObject == null) {
            return null;
        }

        DisplayTextActionProto.DisplayTextAction.Builder builder = DisplayTextActionProto.DisplayTextAction
                .newBuilder();

        Optional.ofNullable(commonObject.getDisplayedText()).ifPresent(text -> {
            builder.setText(StringValue.of(text));
        });

        if (commonObject.getDeliverySettings() != null) {
            try {
                builder.setDeliverySettings(StringValue.of(AbstractSchemaHandler.getAsXMLString(
                        commonObject.getDeliverySettings(), InTutor.class, AbstractSchemaHandler.DKF_SCHEMA_FILE)));
            } catch (Exception e) {
                throw new RuntimeException("There was a problem encoding the Display Text Action object", e);
            }
        }

        return builder.build();
    }

}
