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

import generated.proto.common.LocatedTeamMemberProto;
import mil.arl.gift.common.coordinate.AbstractCoordinate;
import mil.arl.gift.common.course.dkf.team.LocatedTeamMember;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf LocatedTeamMember
 * message.
 * 
 * @author cpolynice
 *
 */
public class LocatedTeamMemberProtoCodec
        implements ProtoCodec<LocatedTeamMemberProto.LocatedTeamMember, LocatedTeamMember> {

    /* Codec that will be used to convert to/from a protobuf {@link
     * AbstractCoordinate}. */
    private static AbstractCoordinateProtoCodec codec = new AbstractCoordinateProtoCodec();

    @Override
    public LocatedTeamMember convert(LocatedTeamMemberProto.LocatedTeamMember protoObject) {
        if (protoObject == null) {
            return null;
        }

        String teamName = protoObject.hasTeamName() ? protoObject.getTeamName().getValue() : null;
        AbstractCoordinate identifier = protoObject.hasIdentifier() ? codec.convert(protoObject.getIdentifier()) : null;
        boolean playable = protoObject.getPlayable().getValue();

        LocatedTeamMember teamMember = new LocatedTeamMember(teamName, identifier);
        teamMember.setPlayable(playable);
        return teamMember;
    }

    @Override
    public LocatedTeamMemberProto.LocatedTeamMember map(LocatedTeamMember commonObject) {
        if (commonObject == null) {
            return null;
        }

        LocatedTeamMemberProto.LocatedTeamMember.Builder builder = LocatedTeamMemberProto.LocatedTeamMember
                .newBuilder();

        builder.setPlayable(BoolValue.of(commonObject.isPlayable()));

        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setTeamName(StringValue.of(name));
        });
        Optional.ofNullable(codec.map(commonObject.getIdentifier())).ifPresent(builder::setIdentifier);

        return builder.build();
    }

}
