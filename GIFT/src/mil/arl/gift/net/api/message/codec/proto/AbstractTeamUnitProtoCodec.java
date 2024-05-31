/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractTeamUnitProto;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.LocatedTeamMember;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an AbstractTeamUnit message.
 * 
 * @author cpolynice
 *
 */
public class AbstractTeamUnitProtoCodec
        implements ProtoCodec<AbstractTeamUnitProto.AbstractTeamUnit, AbstractTeamUnit> {

    /* Codec that will be used to convert to/from a {@link Team}. */
    private static TeamProtoCodec teamCodec = new TeamProtoCodec();

    /* Codec that will be used to convert to/from a protobuf {@link
     * MarkedTeamMember}. */
    private static MarkedTeamMemberProtoCodec markedCodec = new MarkedTeamMemberProtoCodec();

    /* Codec that will be used to convert to/from a protobuf {@link
     * LocatedTeamMember}. */
    private static LocatedTeamMemberProtoCodec locatedCodec = new LocatedTeamMemberProtoCodec();

    @Override
    public AbstractTeamUnit convert(AbstractTeamUnitProto.AbstractTeamUnit protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasTeam()) {
            return teamCodec.convert(protoObject.getTeam());
        } else if (protoObject.hasMarkedTeamMember()) {
            return markedCodec.convert(protoObject.getMarkedTeamMember());
        } else if (protoObject.hasLocatedTeamMember()) {
            return locatedCodec.convert(protoObject.getLocatedTeamMember());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractTeamUnitProto.AbstractTeamUnit map(AbstractTeamUnit commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractTeamUnitProto.AbstractTeamUnit.Builder builder = AbstractTeamUnitProto.AbstractTeamUnit.newBuilder();

        if (commonObject instanceof Team) {
            builder.setTeam(teamCodec.map((Team) commonObject));
        } else if (commonObject instanceof MarkedTeamMember) {
            builder.setMarkedTeamMember(markedCodec.map((MarkedTeamMember) commonObject));
        } else if (commonObject instanceof LocatedTeamMember) {
            builder.setLocatedTeamMember(locatedCodec.map((LocatedTeamMember) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled abstract team unit of " + commonObject);
        }

        return builder.build();
    }

}
