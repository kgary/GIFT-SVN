/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;

import generated.proto.common.EntityAppearanceProto;
import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.PostureEnum;
import mil.arl.gift.common.ta.state.EntityAppearance;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Simulation Address
 * instance.
 * 
 * @author cpolynice
 */
public class EntityAppearanceProtoCodec
        implements ProtoCodec<EntityAppearanceProto.EntityAppearance, EntityAppearance> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EntityAppearanceProtoCodec.class);

    @Override
    public EntityAppearance convert(EntityAppearanceProto.EntityAppearance protoObject) {
        if (protoObject == null) {
            return null;
        }

        DamageEnum damage = null;
        PostureEnum posture = null;
        boolean active = true;

        if (protoObject.hasDamage()) {
            String damageStr = protoObject.getDamage().getValue();
            damage = (damageStr.equals("Incapacitated") || damageStr.equals("DiedOfWounds")) ? DamageEnum.DESTROYED
                    : DamageEnum.valueOf(protoObject.getDamage().getValue());
        }

        if (protoObject.hasPosture()) {
            posture = PostureEnum.valueOf(protoObject.getPosture().getValue());
        }

        if (protoObject.hasActive()) {
            active = protoObject.getActive().getValue();
        }

        if (damage != null) {
            final EntityAppearance toRet = new EntityAppearance(damage, posture);
            toRet.setActive(active);
            return toRet;
        } else {
            logger.error("Could not convert the given protobuf object to the common object.");
        }

        return null;
    }

    @Override
    public EntityAppearanceProto.EntityAppearance map(EntityAppearance commonObject) {
        if (commonObject == null) {
            return null;
        }

        EntityAppearanceProto.EntityAppearance.Builder builder = EntityAppearanceProto.EntityAppearance.newBuilder();

        Optional.ofNullable(commonObject.getDamage()).ifPresent(damage -> {
            builder.setDamage(StringValue.of(damage.toString()));
        });
        Optional.ofNullable(commonObject.getPosture()).ifPresent(posture -> {
            builder.setPosture(StringValue.of(posture.toString()));
        });

        builder.setActive(BoolValue.of(commonObject.isActive()));
        return builder.build();
    }
}
