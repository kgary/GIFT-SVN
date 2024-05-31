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

import generated.proto.common.MarkedTeamMemberProto;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf MarkedTeamMember
 * instance.
 * 
 * @author cpolynice
 *
 */
public class MarkedTeamMemberProtoCodec
        implements ProtoCodec<MarkedTeamMemberProto.MarkedTeamMember, MarkedTeamMember> {

    @Override
    public MarkedTeamMember convert(MarkedTeamMemberProto.MarkedTeamMember protoObject) {
        if (protoObject == null) {
            return null;
        }

        String teamName = protoObject.hasTeamName() ? protoObject.getTeamName().getValue() : null;
        String identifierName = protoObject.hasIdentifierName() ? protoObject.getIdentifierName().getValue() : null;
        boolean playable = protoObject.getPlayable().getValue();

        MarkedTeamMember teamMember = new MarkedTeamMember(teamName, identifierName);
        teamMember.setPlayable(playable);
        return teamMember;
    }

    @Override
    public MarkedTeamMemberProto.MarkedTeamMember map(MarkedTeamMember commonObject) {
        if (commonObject == null) {
            return null;
        }

        MarkedTeamMemberProto.MarkedTeamMember.Builder builder = MarkedTeamMemberProto.MarkedTeamMember.newBuilder();

        builder.setPlayable(BoolValue.of(commonObject.isPlayable()));

        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setTeamName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getIdentifier()).ifPresent(id -> {
            builder.setIdentifierName(StringValue.of(id));
        });

        return builder.build();
    }

}
