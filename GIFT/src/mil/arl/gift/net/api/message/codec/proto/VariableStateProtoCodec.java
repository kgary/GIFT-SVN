/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.VariableStateProto;
import mil.arl.gift.common.ta.state.VariablesState.VariableNumberState;
import mil.arl.gift.common.ta.state.VariablesState.VariableState;
import mil.arl.gift.common.ta.state.VariablesState.WeaponState;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf VariableState
 * instance.
 * 
 * @author cpolynice
 *
 */
public class VariableStateProtoCodec implements ProtoCodec<VariableStateProto.VariableState, VariableState> {

    /* Codec that will be used to convert to/from a protobuf WeaponState. */
    private static final WeaponStateProtoCodec codec = new WeaponStateProtoCodec();

    @Override
    public VariableState convert(VariableStateProto.VariableState protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasVariableNumberState()) {
            try {
                VariableStateProto.VariableNumberState numState = protoObject.getVariableNumberState();
                String varName = numState.hasVarName() ? numState.getVarName().getValue() : null;
                Number varValue = numState.hasVarValue()
                        ? NumberFormat.getInstance().parse(numState.getVarValue().getValue())
                        : null;
                return new VariableNumberState(varName, varValue);
            } catch (ParseException e) {
                throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
            }
        } else if (protoObject.hasWeaponState()) {
            return codec.convert(protoObject.getWeaponState());
        } else {
            return null;
        }
    }

    @Override
    public VariableStateProto.VariableState map(VariableState commonObject) {
        if (commonObject == null) {
            return null;
        }

        VariableStateProto.VariableState.Builder builder = VariableStateProto.VariableState.newBuilder();

        if (commonObject instanceof VariableNumberState) {
            VariableStateProto.VariableNumberState.Builder numBuilder = VariableStateProto.VariableNumberState
                    .newBuilder();

            Optional.ofNullable(((VariableNumberState) commonObject).getVarName()).ifPresent(varName -> {
                numBuilder.setVarName(StringValue.of(varName));
            });

            Optional.ofNullable(((VariableNumberState) commonObject).getVarValue()).ifPresent(varValue -> {
                numBuilder.setVarValue(StringValue.of(varValue.toString()));
            });

            builder.setVariableNumberState(numBuilder.build());
        } else if (commonObject instanceof WeaponState) {
            builder.setWeaponState(codec.map((WeaponState) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled variable state of type " + commonObject);
        }

        return builder.build();
    }
}
