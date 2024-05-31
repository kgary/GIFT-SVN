/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.Optional;

import com.google.protobuf.BoolValue;

import generated.proto.common.KnowledgeSessionsRequestProto;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * KnowledgeSessionsRequest instance.
 * 
 * @author cpolynice
 *
 */
public class KnowledgeSessionsRequestProtoCodec
        implements ProtoCodec<KnowledgeSessionsRequestProto.KnowledgeSessionsRequest, KnowledgeSessionsRequest> {

    @Override
    public KnowledgeSessionsRequest convert(KnowledgeSessionsRequestProto.KnowledgeSessionsRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        KnowledgeSessionsRequest request = new KnowledgeSessionsRequest();

        request.setRunningSessions(
                protoObject.hasRunningSessions() ? protoObject.getRunningSessions().getValue() : false);
        request.setIndividualSessions(
                protoObject.hasIndividualSessions() ? protoObject.getIndividualSessions().getValue() : false);
        request.setFullTeamSessions(
                protoObject.hasFullTeamSessions() ? protoObject.getFullTeamSessions().getValue() : false);

        if (CollectionUtils.isNotEmpty(protoObject.getCourseIdsList())) {
            request.setCourseIds(new ArrayList<>(protoObject.getCourseIdsList()));
        }

        return request;
    }

    @Override
    public KnowledgeSessionsRequestProto.KnowledgeSessionsRequest map(KnowledgeSessionsRequest commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        KnowledgeSessionsRequestProto.KnowledgeSessionsRequest.Builder builder = KnowledgeSessionsRequestProto.KnowledgeSessionsRequest.newBuilder();
        
        builder.setRunningSessions(BoolValue.of(commonObject.isRunningSessions()));
        builder.setIndividualSessions(BoolValue.of(commonObject.isIndividualSessions()));
        builder.setFullTeamSessions(BoolValue.of(commonObject.isFullTeamSessions()));
        Optional.ofNullable(commonObject.getCourseIds()).ifPresent(builder::addAllCourseIds);

        return builder.build();
    }

}
