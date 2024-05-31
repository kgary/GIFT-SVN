/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;

import generated.proto.common.VariableStateProto;
import mil.arl.gift.common.ta.state.VariablesState.WeaponState;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf WeaponState.
 * 
 * @author cpolynice
 *
 */
public class WeaponStateProtoCodec
        implements ProtoCodec<VariableStateProto.WeaponState, WeaponState> {

    /* Codec that will be used to convert to/from a protobuf Vector3D. */
    private static final Vector3DProtoCodec codec = new Vector3DProtoCodec();

    @Override
    public WeaponState convert(VariableStateProto.WeaponState protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasEntityMarking()) {
            WeaponState weaponState = new WeaponState(protoObject.getEntityMarking().getValue());

            if (protoObject.hasWeaponSafetyStatus()) {
                weaponState.setWeaponSafetyStatus(protoObject.getWeaponSafetyStatus().getValue());
            }

            if (protoObject.hasHasWeapon()) {
                weaponState.setHasWeapon(protoObject.getHasWeapon().getValue());
            }

            if (protoObject.hasWeaponAim()) {
                weaponState.setWeaponAim(codec.convert(protoObject.getWeaponAim()));
            }

            return weaponState;
        } else {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Found unhandled weapon state type of " + protoObject);
        }
    }

    @Override
    public VariableStateProto.WeaponState map(WeaponState commonObject) {
        if (commonObject == null) {
            return null;
        }

        VariableStateProto.WeaponState.Builder builder = VariableStateProto.WeaponState.newBuilder();

        Optional.ofNullable(codec.map(commonObject.getWeaponAim())).ifPresent(builder::setWeaponAim);
        Optional.ofNullable(commonObject.getEntityMarking()).ifPresent(marking -> {
            builder.setEntityMarking(StringValue.of(marking));
        });
        Optional.ofNullable(commonObject.getWeaponSafetyStatus()).ifPresent(safety -> {
            builder.setWeaponSafetyStatus(BoolValue.of(safety));
        });
        Optional.ofNullable(commonObject.getHasWeapon()).ifPresent(weapon -> {
            builder.setHasWeapon(BoolValue.of(weapon));
        });

        return builder.build();
    }
}
