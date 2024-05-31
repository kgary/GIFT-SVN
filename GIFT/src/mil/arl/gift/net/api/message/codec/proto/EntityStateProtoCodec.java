/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.List;
import java.util.Optional;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.google.protobuf.Int32Value;

import generated.proto.common.EntityStateProto;
import mil.arl.gift.common.ta.state.ArticulationParameter;
import mil.arl.gift.common.ta.state.DeadReckoningParameters;
import mil.arl.gift.common.ta.state.EntityAppearance;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityMarking;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.EntityType;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

public class EntityStateProtoCodec implements ProtoCodec<EntityStateProto.EntityState, EntityState> {

    /* Codecs that will be used to convert to/from protobuf instances of the
     * members of the entity state. */
    private static EntityIdentifierProtoCodec eIdCodec = new EntityIdentifierProtoCodec();
    private static EntityTypeProtoCodec eTypeCodec = new EntityTypeProtoCodec();
    private static Vector3DProtoCodec vectorCodec = new Vector3DProtoCodec();
    private static Point3DProtoCodec pointCodec = new Point3DProtoCodec();
    private static ArticulationParametersProtoCodec artParamsCodec = new ArticulationParametersProtoCodec();
    private static EntityAppearanceProtoCodec appearanceCodec = new EntityAppearanceProtoCodec();
    private static EntityMarkingProtoCodec markingCodec = new EntityMarkingProtoCodec();
    private static DeadReckoningParametersProtoCodec deadReckoningCodec = new DeadReckoningParametersProtoCodec();

    @Override
    public EntityState convert(EntityStateProto.EntityState protoObject) {
        if (protoObject == null) {
            return null;
        }

        Integer forceID;
        EntityIdentifier entityID;
        EntityType entityType, altEntityType;
        Vector3d linearVel, orientation;
        Point3d location;
        List<ArticulationParameter> artParams;
        EntityAppearance appearance;
        EntityMarking entityMarking;
        DeadReckoningParameters deadReckoningParameters;

        forceID = protoObject.hasForceId() ? protoObject.getForceId().getValue() : null;
        entityID = protoObject.hasEntityId() ? eIdCodec.convert(protoObject.getEntityId()) : null;
        entityType = protoObject.hasEntityType() ? eTypeCodec.convert(protoObject.getEntityType()) : null;
        linearVel = protoObject.hasLinearVelocity() ? vectorCodec.convert(protoObject.getLinearVelocity()) : null;
        location = protoObject.hasLocation() ? pointCodec.convert(protoObject.getLocation()) : null;
        orientation = protoObject.hasOrientation() ? vectorCodec.convert(protoObject.getOrientation()) : null;
        artParams = protoObject.hasArticulationParameters()
                ? artParamsCodec.convert(protoObject.getArticulationParameters())
                : null;
        appearance = protoObject.hasAppearance() ? appearanceCodec.convert(protoObject.getAppearance()) : null;

        if (protoObject.hasAltEntityType()) {
            altEntityType = eTypeCodec.convert(protoObject.getAltEntityType());
        } else {
            altEntityType = entityType;
        }

        if (protoObject.hasMarking()) {
            entityMarking = markingCodec.convert(protoObject.getMarking());
        } else {
            entityMarking = null;
        }

        if (protoObject.hasDeadReckoning()) {
            deadReckoningParameters = deadReckoningCodec.convert(protoObject.getDeadReckoning());
        } else {
            deadReckoningParameters = null;
        }

        EntityState entityState = new EntityState(entityID, forceID, entityType, linearVel, location, orientation,
                artParams, appearance, entityMarking);
        entityState.setAlternativeEntityType(altEntityType);
        entityState.setDeadReckoningParameters(deadReckoningParameters);
        return entityState;
    }

    @Override
    public EntityStateProto.EntityState map(EntityState commonObject) {
        if (commonObject == null) {
            return null;
        }

        EntityStateProto.EntityState.Builder builder = EntityStateProto.EntityState.newBuilder();

        Optional.ofNullable(eIdCodec.map(commonObject.getEntityID())).ifPresent(builder::setEntityId);
        Optional.ofNullable(commonObject.getForceID()).ifPresent(fID -> {
            builder.setForceId(Int32Value.of(fID));
        });
        Optional.ofNullable(eTypeCodec.map(commonObject.getEntityType())).ifPresent(builder::setEntityType);
        Optional.ofNullable(eTypeCodec.map(commonObject.getAlternativeEntityType()))
                .ifPresent(builder::setAltEntityType);
        Optional.ofNullable(vectorCodec.map(commonObject.getLinearVelocity())).ifPresent(builder::setLinearVelocity);
        Optional.ofNullable(pointCodec.map(commonObject.getLocation())).ifPresent(builder::setLocation);
        Optional.ofNullable(vectorCodec.map(commonObject.getOrientation())).ifPresent(builder::setOrientation);

        if (CollectionUtils.isNotEmpty(commonObject.getArticulationParameters())) {
            builder.setArticulationParameters(artParamsCodec.map(commonObject.getArticulationParameters()));
        }

        Optional.ofNullable(appearanceCodec.map(commonObject.getAppearance())).ifPresent(builder::setAppearance);
        Optional.ofNullable(markingCodec.map(commonObject.getEntityMarking())).ifPresent(builder::setMarking);

        if (commonObject.getDeadReckoningParameters() != null) {
            builder.setDeadReckoning(deadReckoningCodec.map(commonObject.getDeadReckoningParameters()));
        }

        return builder.build();
    }

}
