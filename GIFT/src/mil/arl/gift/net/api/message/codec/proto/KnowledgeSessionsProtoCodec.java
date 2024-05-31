/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.BoolValue;

import generated.proto.common.AbstractKnowledgeSessionProto;
import generated.proto.common.KnowledgeSessionsProto.KnowledgeSessions;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf KnowledgeSessions
 * instance.
 * 
 * @author cpolynice
 *
 */
public class KnowledgeSessionsProtoCodec implements ProtoCodec<KnowledgeSessions, KnowledgeSessionsReply> {

    /* Codec that will be used to convert to/from an AbstractKnowledgeSession
     * instance. */
    private static AbstractKnowledgeSessionProtoCodec codec = new AbstractKnowledgeSessionProtoCodec();

    /**
     * Converts the given protobuf map into the common object representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map.
     */
    private static Map<Integer, AbstractKnowledgeSession> convertSessions(
            Map<Integer, AbstractKnowledgeSessionProto.AbstractKnowledgeSession> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<Integer, AbstractKnowledgeSession> commonMap = new HashMap<>();

        for (Map.Entry<Integer, AbstractKnowledgeSessionProto.AbstractKnowledgeSession> session : protoMap.entrySet()) {
            int key = session.getKey();
            AbstractKnowledgeSession value = codec.convert(session.getValue());

            if (value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object map into the protobuf representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map representation.
     */
    private static Map<Integer, AbstractKnowledgeSessionProto.AbstractKnowledgeSession> mapSessions(
            Map<Integer, AbstractKnowledgeSession> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<Integer, AbstractKnowledgeSessionProto.AbstractKnowledgeSession> protoMap = new HashMap<>();

        for (Map.Entry<Integer, AbstractKnowledgeSession> session : commonMap.entrySet()) {
            int key = session.getKey();
            AbstractKnowledgeSessionProto.AbstractKnowledgeSession value = codec.map(session.getValue());

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public KnowledgeSessionsReply convert(KnowledgeSessions protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap = CollectionUtils
                .isNotEmpty(protoObject.getSessionMapMap()) ? convertSessions(protoObject.getSessionMapMap())
                        : new HashMap<>();
        boolean canHost = protoObject.hasCanHost() ? protoObject.getCanHost().getValue() : false;
        
        KnowledgeSessionsReply reply = new KnowledgeSessionsReply(knowledgeSessionMap);
        reply.setCanHost(canHost);
        return reply;
    }

    @Override
    public KnowledgeSessions map(KnowledgeSessionsReply commonObject) {
        if (commonObject == null) {
            return null;
        }

        KnowledgeSessions.Builder builder = KnowledgeSessions.newBuilder();

        builder.setCanHost(BoolValue.of(commonObject.canHost()));
        Optional.ofNullable(mapSessions(commonObject.getKnowledgeSessionMap())).ifPresent(builder::putAllSessionMap);

        return builder.build();
    }

}
