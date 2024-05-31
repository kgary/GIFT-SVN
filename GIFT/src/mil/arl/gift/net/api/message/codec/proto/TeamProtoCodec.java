/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.AbstractTeamUnitProto;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf Team message.
 * 
 * @author cpolynice
 *
 */
public class TeamProtoCodec implements ProtoCodec<AbstractTeamUnitProto.Team, Team> {

    /* Codec that will be used to convert to/from an {@link
     * AbstractTeamUnit}. */
    private static AbstractTeamUnitProtoCodec codec = new AbstractTeamUnitProtoCodec();

    /**
     * Converts the protobuf AbstractTeamUnit list into the common object
     * representation.
     * 
     * @param protoList the protobuf list
     * @return the common object AbstractTeamUnit list
     */
    private List<AbstractTeamUnit> convertList(List<AbstractTeamUnitProto.AbstractTeamUnit> protoList) {
        if (protoList == null) {
            return null;
        }

        List<AbstractTeamUnit> commonList = new ArrayList<>();

        for (AbstractTeamUnitProto.AbstractTeamUnit tUnit : protoList) {
            commonList.add(codec.convert(tUnit));
        }

        return commonList;
    }

    /**
     * Maps the common object list to the protobuf AbstractTeamUnit list.
     * 
     * @param commonList the common object list
     * @return the protobuf AbstractTeamUnit test
     */
    private List<AbstractTeamUnitProto.AbstractTeamUnit> mapList(List<AbstractTeamUnit> commonList) {
        if (commonList == null) {
            return null;
        }

        List<AbstractTeamUnitProto.AbstractTeamUnit> protoList = new ArrayList<>();

        for (AbstractTeamUnit tUnit : commonList) {
            protoList.add(codec.map(tUnit));
        }

        return protoList;
    }

    @Override
    public Team convert(AbstractTeamUnitProto.Team protoObject) {
        if (protoObject == null) {
            return null;
        }

        String memberName = protoObject.hasTeamName() ? protoObject.getTeamName().getValue() : null;
        List<AbstractTeamUnit> teamUnits = convertList(protoObject.getTeamUnitsList());
        String echelon = protoObject.hasEchelon() ? protoObject.getEchelon().getValue() : null;
        EchelonEnum echelonEnum = null;

        if (echelon != null) {
            echelonEnum = EchelonEnum.valueOf(echelon, EchelonEnum.VALUES());
        }

        return new Team(memberName, echelonEnum, teamUnits);
    }

    @Override
    public AbstractTeamUnitProto.Team map(Team commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractTeamUnitProto.Team.Builder builder = AbstractTeamUnitProto.Team.newBuilder();

        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setTeamName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getEchelon()).ifPresent(echelon -> {
            builder.setEchelon(StringValue.of(echelon.getName()));
        });
        Optional.ofNullable(mapList(commonObject.getUnits())).ifPresent(builder::addAllTeamUnits);
        
        return builder.build();
    }

}
