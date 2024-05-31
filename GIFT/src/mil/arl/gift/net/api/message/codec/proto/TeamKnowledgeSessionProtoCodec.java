/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractKnowledgeSessionProto;
import generated.proto.common.SessionMemberProto;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.KnowledgeSessionCourseInfo;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.ObserverControls;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.common.course.dkf.session.Mission;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * TeamKnowledgeSession message.
 * 
 * @author cpolynice
 *
 */
public class TeamKnowledgeSessionProtoCodec
        implements ProtoCodec<AbstractKnowledgeSessionProto.TeamKnowledgeSession, TeamKnowledgeSession> {

    /* Codec that will be used to convert to/from protobuf enumerations. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /* Codec that will be used to convert to/from a protobuf {@link
     * SessionMember}. */
    private static SessionMemberProtoCodec sessionCodec = new SessionMemberProtoCodec();

    /* Codec that will be used to convert to/from a protobuf {@link Team}. */
    private static TeamProtoCodec teamCodec = new TeamProtoCodec();

    /* Codec that will be used to convert to/from a protobuf (@link Mission}. */
    private static final MissionProtoCodec missionCodec = new MissionProtoCodec();

    /**
     * Converts the protobuf node id to name map to the common object
     * representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map.
     */
    private Map<BigInteger, String> convertNodeIdToNameMap(Map<String, String> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<BigInteger, String> commonMap = new HashMap<>();

        for (Map.Entry<String, String> nodeIdName : protoMap.entrySet()) {
            BigInteger key = new BigInteger(nodeIdName.getKey());
            String value = StringUtils.isNotBlank(nodeIdName.getValue()) ? nodeIdName.getValue() : null;

            if (value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Converts the protobuf session member map to the common object
     * representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map.
     */
    private Map<Integer, SessionMember> convertSessionMemberMap(
            Map<Integer, SessionMemberProto.SessionMember> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<Integer, SessionMember> commonMap = new HashMap<>();

        for (Map.Entry<Integer, SessionMemberProto.SessionMember> sessionMember : protoMap.entrySet()) {
            Integer key = sessionMember.getKey();
            SessionMember value = sessionCodec.convert(sessionMember.getValue());

            if (value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the common object node id to name map to the protobuf
     * representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map.
     */
    private Map<String, String> mapNodeIdToNameMap(Map<BigInteger, String> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, String> protoMap = new HashMap<>();

        for (Map.Entry<BigInteger, String> nodeIdName : commonMap.entrySet()) {
            String key = nodeIdName.getKey().toString();
            String value = StringUtils.isNotBlank(nodeIdName.getValue()) ? nodeIdName.getValue() : null;

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    /**
     * Maps the common object session member map to the protobuf representaion.
     * 
     * @param commonMap the common object map
     * @return the protobuf map.
     */
    private Map<Integer, SessionMemberProto.SessionMember> mapSessionMemberMap(Map<Integer, SessionMember> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<Integer, SessionMemberProto.SessionMember> protoMap = new HashMap<>();

        for (Map.Entry<Integer, SessionMember> sessionMember : commonMap.entrySet()) {
            Integer key = sessionMember.getKey();
            SessionMemberProto.SessionMember value = sessionCodec.map(sessionMember.getValue());

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public TeamKnowledgeSession convert(AbstractKnowledgeSessionProto.TeamKnowledgeSession protoObject) {
        if (protoObject == null) {
            return null;
        }

        String courseRuntimeId = protoObject.hasCourseId() ? protoObject.getCourseId().getValue() : null;
        String courseSourceId = protoObject.hasCourseSourceId() ? protoObject.getCourseSourceId().getValue() : null;
        String nameOfSession = protoObject.hasNameOfSession() ? protoObject.getNameOfSession().getValue() : null;
        String scenarioDescription = protoObject.hasDescription() ? protoObject.getDescription().getValue() : null;
        String courseName = protoObject.hasCourseName() ? protoObject.getCourseName().getValue() : null;
        String experimentName = protoObject.hasExperimentName() ? protoObject.getExperimentName().getValue() : null;
        SessionMember hostSessionMember = protoObject.hasHostSessionMember()
                ? sessionCodec.convert(protoObject.getHostSessionMember())
                : null;
        String playbackId = protoObject.hasPlaybackId() ? protoObject.getPlaybackId().getValue() : null;
        String goodAudioUrl = protoObject.hasGoodPerformanceAudio() ? protoObject.getGoodPerformanceAudio().getValue()
                : null;
        String poorAudioUrl = protoObject.hasPoorPerformanceAudio() ? protoObject.getPoorPerformanceAudio().getValue()
                : null;
        String capturedAudioPath = protoObject.hasCapturedAudio() ? protoObject.getCapturedAudio().getValue() : null;

        ObserverControls observerControls = goodAudioUrl != null || poorAudioUrl != null || capturedAudioPath != null
                ? new ObserverControls().setGoodPerformanceAudioUrl(goodAudioUrl)
                        .setPoorPerformanceAudioUrl(poorAudioUrl).setCapturedAudioPath(capturedAudioPath)
                : null;
        Team team = protoObject.hasTeamStructure() ? teamCodec.convert(protoObject.getTeamStructure()) : null;

        Map<BigInteger, String> nodeIdToNameMap = convertNodeIdToNameMap(protoObject.getNodeIdToNameMapMap());
        TrainingApplicationEnum trainingAppType = (TrainingApplicationEnum) enumCodec
                .convert(protoObject.getTrainingAppType());

        int allowedMemberCnt = protoObject.hasAllowedMemeberCnt() ? protoObject.getAllowedMemeberCnt().getValue() : 0;
        Map<Integer, SessionMember> teamMembersMap = convertSessionMemberMap(protoObject.getTeamMembersMap());

        List<String> roleList = CollectionUtils.isNotEmpty(protoObject.getTeamRolesList())
                ? new ArrayList<>(protoObject.getTeamRolesList())
                : new ArrayList<>();

        SessionType sessionType = protoObject.hasSessionPlayType()
                ? SessionType.valueOf(protoObject.getSessionPlayType().getValue())
                : null;
        long startTime = protoObject.hasSessionStartTime()
                ? ProtobufConversionUtil.convertTimestampToMillis(protoObject.getSessionStartTime())
                : 0;

        KnowledgeSessionCourseInfo courseInfo = new KnowledgeSessionCourseInfo(courseName, courseRuntimeId,
                courseSourceId, experimentName);
        courseInfo.setDomainSessionLogFileName(
                protoObject.hasDsLogfileName() ? protoObject.getDsLogfileName().getValue() : null);

        Mission mission = protoObject.hasMission() ? missionCodec.convert(protoObject.getMission()) : null;

        TeamKnowledgeSession session = new TeamKnowledgeSession(nameOfSession, scenarioDescription, courseInfo,
                allowedMemberCnt, hostSessionMember, teamMembersMap, roleList, team, nodeIdToNameMap, trainingAppType,
                sessionType, startTime, mission, observerControls);
        session.setPlaybackId(playbackId);

        return session;
    }

    @Override
    public AbstractKnowledgeSessionProto.TeamKnowledgeSession map(TeamKnowledgeSession commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractKnowledgeSessionProto.TeamKnowledgeSession.Builder builder = AbstractKnowledgeSessionProto.TeamKnowledgeSession
                .newBuilder();

        Optional.ofNullable(commonObject.getCourseRuntimeId()).ifPresent(id -> {
            builder.setCourseId(StringValue.of(id));
        });
        Optional.ofNullable(commonObject.getCourseSourceId()).ifPresent(id -> {
            builder.setCourseSourceId(StringValue.of(id));
        });
        Optional.ofNullable(commonObject.getNameOfSession()).ifPresent(sessionName -> {
            builder.setNameOfSession(StringValue.of(sessionName));
        });
        Optional.ofNullable(commonObject.getScenarioDescription()).ifPresent(description -> {
            builder.setDescription(StringValue.of(description));
        });
        Optional.ofNullable(commonObject.getCourseName()).ifPresent(name -> {
            builder.setCourseName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getExperimentName()).ifPresent(expName -> {
            builder.setExperimentName(StringValue.of(expName));
        });
        Optional.ofNullable(sessionCodec.map(commonObject.getHostSessionMember()))
                .ifPresent(builder::setHostSessionMember);

        Optional.ofNullable(mapNodeIdToNameMap(commonObject.getNodeIdToNameMap()))
                .ifPresent(builder::putAllNodeIdToNameMap);

        Optional.ofNullable(enumCodec.map(commonObject.getTrainingAppType())).ifPresent(builder::setTrainingAppType);

        Optional.ofNullable(teamCodec.map(commonObject.getTeamStructure())).ifPresent(builder::setTeamStructure);

        Optional.ofNullable(commonObject.getPlaybackId()).ifPresent(playback -> {
            builder.setPlaybackId(StringValue.of(playback));
        });
        Optional.ofNullable(commonObject.getSessionType()).ifPresent(type -> {
            builder.setSessionPlayType(StringValue.of(type.name()));
        });

        builder.setSessionStartTime(
                ProtobufConversionUtil.convertMillisToTimestamp(commonObject.getSessionStartTime()));
        builder.setSessionEndTime(ProtobufConversionUtil.convertMillisToTimestamp(commonObject.getSessionEndTime()));

        Optional.ofNullable(commonObject.getDomainSessionLogFileName()).ifPresent(dsLog -> {
            builder.setDsLogfileName(StringValue.of(dsLog));
        });


        if (commonObject.getObserverControls() != null) {
            ObserverControls oControls = commonObject.getObserverControls();

            Optional.ofNullable(oControls.getGoodPerformanceAudioUrl()).ifPresent(url -> {
                builder.setGoodPerformanceAudio(StringValue.of(url));
            });
            Optional.ofNullable(oControls.getPoorPerformanceAudioUrl()).ifPresent(url -> {
                builder.setPoorPerformanceAudio(StringValue.of(url));
            });
            Optional.ofNullable(oControls.getCapturedAudioPath()).ifPresent(path -> {
                builder.setCapturedAudio(StringValue.of(path));
            });
        }

        builder.setAllowedMemeberCnt(Int32Value.of(commonObject.getTotalPossibleTeamMembers()));

        Optional.ofNullable(mapSessionMemberMap(commonObject.getJoinedMembers())).ifPresent(builder::putAllTeamMembers);
        Optional.ofNullable(commonObject.getTeamRoles()).ifPresent(builder::addAllTeamRoles);
        Optional.ofNullable(missionCodec.map(commonObject.getMission())).ifPresent(builder::setMission);

        return builder.build();
    }

}
