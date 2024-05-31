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
import com.google.protobuf.StringValue;

import generated.proto.common.SessionMemberProto;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.SessionMember.GroupMembership;
import mil.arl.gift.common.course.dkf.session.SessionMember.IndividualMembership;
import mil.arl.gift.common.course.dkf.team.LocatedTeamMember;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf SessionMember
 * message.
 * 
 * @author cpolynice
 *
 */
public class SessionMemberProtoCodec implements ProtoCodec<SessionMemberProto.SessionMember, SessionMember> {

    /* Codec that will be used to convert to/from a protobuf {@link
     * MarkedTeamMember}. */
    private static MarkedTeamMemberProtoCodec markedCodec = new MarkedTeamMemberProtoCodec();

    /* Codec that will be used to convert to/from a protobuf {@link
     * LocatedTeamMember}. */
    private static LocatedTeamMemberProtoCodec locatedCodec = new LocatedTeamMemberProtoCodec();

    /**
     * Creates an individual member that is marked or located for converting to
     * the common object.
     * 
     * @param protoObject the type of member to create
     * @return the individual member with the corresponding membership.
     */
    private IndividualMembership createIndividualMember(SessionMemberProto.IndividualMembership protoObject) {
        String memberName = protoObject.hasUsername() ? protoObject.getUsername().getValue() : null;

        if (memberName == null) {

            /* IssueID #4648: If username is null, then this member is likely
             * coming from an experiment course created before
             * IndividualMembership was introduced. To handle this, we need to
             * provide the same username that the UMS module assigns to new
             * experiment users. */
            memberName = CommonProperties.getInstance().getReadOnlyUser();
        }

        IndividualMembership individualMembership = new IndividualMembership(memberName);
        TeamMember<?> teamMember = null;
        
        if (protoObject.hasMarkedTeamMember()) {
            teamMember = markedCodec.convert(protoObject.getMarkedTeamMember());
            individualMembership.setTeamMember(teamMember);
        } else if (protoObject.hasLocatedTeamMember()) {
            teamMember = locatedCodec.convert(protoObject.getLocatedTeamMember());
            individualMembership.setTeamMember(teamMember);
        }

        return individualMembership;
    }

    /**
     * Creates an individual member that can be marked or located in mapping to
     * protobuf.
     * 
     * @param individualMembership the member that contains an individual
     *        membership
     * @return the protobuf individual membership
     */
    private SessionMemberProto.IndividualMembership mapIndividualMembership(
            IndividualMembership individualMembership) {
        SessionMemberProto.IndividualMembership.Builder indivBuilder = SessionMemberProto.IndividualMembership
                .newBuilder();

        Optional.ofNullable(individualMembership.getUsername()).ifPresent(username -> {
            indivBuilder.setUsername(StringValue.of(username));
        });

        /* Team member data can be null. */
        if (individualMembership.getTeamMember() != null) {
            TeamMember<?> teamMember = individualMembership.getTeamMember();
            if (teamMember instanceof MarkedTeamMember) {
                indivBuilder.setMarkedTeamMember(markedCodec.map((MarkedTeamMember) teamMember)).build();
            } else if (teamMember instanceof LocatedTeamMember) {
                indivBuilder.setLocatedTeamMember(locatedCodec.map((LocatedTeamMember) teamMember)).build();
            } else {
                throw new MessageEncodeException(this.getClass().getName(),
                        "Found unhandled team member data of " + teamMember);
            }
        }

        return indivBuilder.build();
    }

    @Override
    public SessionMember convert(SessionMemberProto.SessionMember protoObject) {
        if (protoObject == null) {
            return null;
        }

        int dsId = protoObject.hasDsId() ? protoObject.getDsId().getValue() : 0;
        int userId = (int) (protoObject.hasUserId() ? protoObject.getUserId().getValue() : 0L);
        String experimentId = protoObject.hasExperimentId() ? protoObject.getExperimentId().getValue() : null;

        UserSession uSession = new UserSession(userId);
        uSession.setExperimentId(experimentId);

        SessionMember sessionMember = null;
        if (protoObject.hasSessionMembership()) {
            SessionMemberProto.SessionMembership membership = protoObject.getSessionMembership();

            if (membership.hasGroupMembership()) {
                SessionMemberProto.GroupMembership gMembership = membership.getGroupMembership();
                String memberName = null;

                if (gMembership.hasUsername()) {
                    memberName = gMembership.getUsername().getValue();
                }

                GroupMembership groupMembership = new GroupMembership(memberName);

                for (SessionMemberProto.IndividualMembership member : gMembership.getMembersList()) {
                    groupMembership.getMembers().add(createIndividualMember(member));
                }

                uSession.setUsername(groupMembership.getUsername());
                sessionMember = new SessionMember(groupMembership, uSession, dsId);
            } else if (membership.hasIndividualMembership()) {

                /* Since the members are represented as a list, for the
                 * individual membership retrieve the first element in the
                 * list. */
                IndividualMembership individualMembership = createIndividualMember(
                        membership.getIndividualMembership());
                uSession.setUsername(individualMembership.getUsername());
                sessionMember = new SessionMember(individualMembership, uSession, dsId);

            } else {
                throw new MessageDecodeException(this.getClass().getName(),
                        "Found unhandled membership of type " + protoObject);
            }
        }

        return sessionMember;
    }

    @Override
    public SessionMemberProto.SessionMember map(SessionMember commonObject) {
        if (commonObject == null) {
            return null;
        }

        SessionMemberProto.SessionMember.Builder builder = SessionMemberProto.SessionMember.newBuilder();

        builder.setDsId(Int32Value.of(commonObject.getDomainSessionId()));
        builder.setUserId(Int32Value.of(commonObject.getUserSession().getUserId()));

        Optional.ofNullable(commonObject.getUserSession().getExperimentId()).ifPresent(expId -> {
            builder.setExperimentId(StringValue.of(expId));
        });

        if (commonObject.getSessionMembership() instanceof IndividualMembership) {
            SessionMemberProto.IndividualMembership individialMembership = mapIndividualMembership(
                    (IndividualMembership) commonObject.getSessionMembership());
            builder.setSessionMembership(
                    SessionMemberProto.SessionMembership.newBuilder().setIndividualMembership(individialMembership));
        } else {
            /* Has to be an instance of a GroupMembership. */
            SessionMemberProto.GroupMembership.Builder groupBuilder = SessionMemberProto.GroupMembership.newBuilder();
            GroupMembership groupMembership = (GroupMembership) commonObject.getSessionMembership();

            if (groupMembership.getUsername() != null) {
                groupBuilder.setUsername(StringValue.of(groupMembership.getUsername()));
            }

            for (IndividualMembership individualMembership : groupMembership.getMembers()) {
                SessionMemberProto.IndividualMembership indivMembership = mapIndividualMembership(
                        individualMembership);
                groupBuilder.addMembers(indivMembership);
            }
            
            builder.setSessionMembership(
                    SessionMemberProto.SessionMembership.newBuilder().setGroupMembership(groupBuilder));
        }

        return builder.build();
    }

}
