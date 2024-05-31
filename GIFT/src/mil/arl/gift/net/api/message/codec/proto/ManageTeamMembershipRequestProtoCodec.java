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

import generated.proto.common.ManageTeamMembershipRequestProto;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest.ACTION_TYPE;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * ManageTeamMembershipRequest instance.
 * 
 * @author cpolynice
 *
 */
public class ManageTeamMembershipRequestProtoCodec implements
        ProtoCodec<ManageTeamMembershipRequestProto.ManageTeamMembershipRequest, ManageTeamMembershipRequest> {

    @Override
    public ManageTeamMembershipRequest convert(
            ManageTeamMembershipRequestProto.ManageTeamMembershipRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            int dsIdHost = protoObject.hasDsIdHost() ? protoObject.getDsIdHost().getValue() : 0;
            String memberName = protoObject.hasMemberName() ? protoObject.getMemberName().getValue() : null;
            String actionTypeStr = protoObject.hasAction() ? protoObject.getAction().getValue() : null;
            String nameOfSessionToCreateOrDestroy = protoObject.hasSessionName()
                    ? protoObject.getSessionName().getValue()
                    : null;
            ACTION_TYPE actionType = ACTION_TYPE.valueOf(actionTypeStr);

            switch (actionType) {
            case CREATE_TEAM_SESSION:
                return ManageTeamMembershipRequest.createHostedTeamKnowledgeSessionRequest(dsIdHost,
                        nameOfSessionToCreateOrDestroy);
            case DESTROY_TEAM_SESSION:
                return ManageTeamMembershipRequest.createDestroyTeamKnowledgeSessionRequest(dsIdHost);
            case JOIN_TEAM_SESSION:
                return ManageTeamMembershipRequest.createJoinTeamKnowledgeSessionRequest(dsIdHost);
            case LEAVE_TEAM_SESSION:
                return ManageTeamMembershipRequest.createLeaveTeamKnowledgeSessionRequest(dsIdHost);
            case ASSIGN_TEAM_MEMBER:
                return ManageTeamMembershipRequest.createAssignTeamMemberTeamKnowledgeSessionRequest(dsIdHost,
                        memberName);
            case UNASSIGN_TEAM_MEMBER:
                return ManageTeamMembershipRequest.createUnassignTeamMemberTeamKnowledgeSessionRequest(dsIdHost,
                        memberName);
            case CHANGE_TEAM_SESSION_NAME:
                return ManageTeamMembershipRequest.createChangeTeamKnowledgeSessionNameRequest(dsIdHost,
                        nameOfSessionToCreateOrDestroy);
            default:
                throw new Exception("Found unhandled action type of " + actionType);
            }
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }

    }

    @Override
    public ManageTeamMembershipRequestProto.ManageTeamMembershipRequest map(ManageTeamMembershipRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        ManageTeamMembershipRequestProto.ManageTeamMembershipRequest.Builder builder = ManageTeamMembershipRequestProto.ManageTeamMembershipRequest
                .newBuilder();

        builder.setDsIdHost(Int32Value.of(commonObject.getDomainSessionIdOfHost()));
        Optional.ofNullable(commonObject.getTeamMemberName()).ifPresent(memberName -> {
            builder.setMemberName(StringValue.of(memberName));
        });
        Optional.ofNullable(commonObject.getActionType()).ifPresent(action -> {
            builder.setAction(StringValue.of(action.toString()));
        });
        Optional.ofNullable(commonObject.getNameOfSessionToCreateOrDestroy()).ifPresent(sessionName -> {
            builder.setSessionName(StringValue.of(sessionName));
        });

        return builder.build();
    }
}
