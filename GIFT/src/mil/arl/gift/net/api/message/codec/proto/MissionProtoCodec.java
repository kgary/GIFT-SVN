/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.StringValue;

import generated.proto.common.MissionProto;
import mil.arl.gift.common.course.dkf.session.Mission;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf Mission.
 * 
 * @author cpolynice
 *
 */
public class MissionProtoCodec implements ProtoCodec<MissionProto.Mission, Mission> {

    @Override
    public Mission convert(MissionProto.Mission protoObject) {
        if (protoObject == null) {
            return null;
        }

        String source = protoObject.hasSource() ? protoObject.getSource().getValue() : null;
        String met = protoObject.hasMet() ? protoObject.getMet().getValue() : null;
        String task = protoObject.hasTask() ? protoObject.getTask().getValue() : null;
        String situation = protoObject.hasSituation() ? protoObject.getSituation().getValue() : null;
        String goals = protoObject.hasGoals() ? protoObject.getGoals().getValue() : null;
        String condition = protoObject.hasCondition() ? protoObject.getCondition().getValue() : null;
        String roe = protoObject.hasRoe() ? protoObject.getRoe().getValue() : null;
        String threatWarning = protoObject.hasThreatWarning() ? protoObject.getThreatWarning().getValue() : null;
        String weaponStatus = protoObject.hasWeaponStatus() ? protoObject.getWeaponStatus().getValue() : null;
        String weaponPosture = protoObject.hasWeaponPosture() ? protoObject.getWeaponPosture().getValue() : null;

        return new Mission(source, met, task, situation, goals, condition, roe, threatWarning, weaponStatus,
                weaponPosture);
    }

    @Override
    public MissionProto.Mission map(Mission commonObject) {
        if (commonObject == null) {
            return null;
        }

        MissionProto.Mission.Builder builder = MissionProto.Mission.newBuilder();

        if (StringUtils.isNotBlank(commonObject.getSource())) {
            builder.setSource(StringValue.of(commonObject.getSource()));
        }

        if (StringUtils.isNotBlank(commonObject.getMET())) {
            builder.setMet(StringValue.of(commonObject.getMET()));
        }

        if (StringUtils.isNotBlank(commonObject.getTask())) {
            builder.setTask(StringValue.of(commonObject.getTask()));
        }

        if (StringUtils.isNotBlank(commonObject.getSituation())) {
            builder.setSituation(StringValue.of(commonObject.getSituation()));
        }

        if (StringUtils.isNotBlank(commonObject.getGoals())) {
            builder.setGoals(StringValue.of(commonObject.getGoals()));
        }

        if (StringUtils.isNotBlank(commonObject.getCondition())) {
            builder.setCondition(StringValue.of(commonObject.getCondition()));
        }

        if (StringUtils.isNotBlank(commonObject.getROE())) {
            builder.setRoe(StringValue.of(commonObject.getROE()));
        }

        if (StringUtils.isNotBlank(commonObject.getThreatWarning())) {
            builder.setThreatWarning(StringValue.of(commonObject.getThreatWarning()));
        }

        if (StringUtils.isNotBlank(commonObject.getWeaponStatus())) {
            builder.setWeaponStatus(StringValue.of(commonObject.getWeaponStatus()));
        }

        if (StringUtils.isNotBlank(commonObject.getWeaponPosture())) {
            builder.setWeaponPosture(StringValue.of(commonObject.getWeaponPosture()));
        }

        return builder.build();
    }

}
