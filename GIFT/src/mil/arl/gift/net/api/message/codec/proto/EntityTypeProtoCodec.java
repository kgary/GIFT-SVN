/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.Int32Value;

import generated.proto.common.EntityTypeProto;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.ta.state.EntityType;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an Entity Identifier
 * instance.
 * 
 * @author cpolynice
 */
public class EntityTypeProtoCodec implements ProtoCodec<EntityTypeProto.EntityType, EntityType> {

    @Override
    public EntityType convert(EntityTypeProto.EntityType protoObject) {
        if (protoObject == null) {
            return null;
        }

        int entityKind, domain, country, category, subcategory, specific, extra;
        Long echelon = null;

        entityKind = protoObject.getEntityKind().getValue();
        domain = protoObject.getDomain().getValue();
        country = protoObject.getCountry().getValue();
        category = protoObject.getCategory().getValue();
        subcategory = protoObject.getSubcategory().getValue();
        specific = protoObject.getSpecific().getValue();
        extra = protoObject.getExtra().getValue();

        if (protoObject.hasEchelon()) {
            echelon = Long.valueOf(protoObject.getEchelon().getValue());
        }

        EntityType type = new EntityType(entityKind, domain, country, category, subcategory, specific, extra);
        if (echelon != null) {
            type = type.replaceEchelon(EchelonEnum.valueOf(echelon.intValue()));
        }

        return type;
    }

    @Override
    public EntityTypeProto.EntityType map(EntityType commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        EntityTypeProto.EntityType.Builder builder = EntityTypeProto.EntityType.newBuilder();
        
        builder.setEntityKind(Int32Value.of(commonObject.getEntityKind()));
        builder.setDomain(Int32Value.of(commonObject.getDomain()));
        builder.setCountry(Int32Value.of(commonObject.getCountry()));
        builder.setCategory(Int32Value.of(commonObject.getCategory()));
        builder.setSubcategory(Int32Value.of(commonObject.getSubcategory()));
        builder.setSpecific(Int32Value.of(commonObject.getSpecific()));
        builder.setExtra(Int32Value.of(commonObject.getExtra()));
        
        /* The entity may have a rank/echleon that it belongs to. */
        Optional.ofNullable(commonObject.getEchelon()).ifPresent(ech -> {
            builder.setEchelon(Int32Value.of(ech.getValue()));
        });

        return builder.build();
    }

}
