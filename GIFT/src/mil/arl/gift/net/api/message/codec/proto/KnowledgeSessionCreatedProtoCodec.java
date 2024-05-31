/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.KnowledgeSessionCreatedProto;
import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * KnowledgeSessionCreated instance.
 * 
 * @author cpolynice
 *
 */
public class KnowledgeSessionCreatedProtoCodec
        implements ProtoCodec<KnowledgeSessionCreatedProto.KnowledgeSessionCreated, KnowledgeSessionCreated> {

    /* Codec that will be used to convert to/from an AbstractKnowledgeSession
     * instance. */
    private static AbstractKnowledgeSessionProtoCodec codec = new AbstractKnowledgeSessionProtoCodec();

    @Override
    public KnowledgeSessionCreated convert(KnowledgeSessionCreatedProto.KnowledgeSessionCreated protoObject) {
        if (protoObject == null) {
            return null;
        }

        return protoObject.hasKnowledgeSession()
                ? new KnowledgeSessionCreated(codec.convert(protoObject.getKnowledgeSession()))
                : null;
    }

    @Override
    public KnowledgeSessionCreatedProto.KnowledgeSessionCreated map(KnowledgeSessionCreated commonObject) {
        if (commonObject == null) {
            return null;
        }

        KnowledgeSessionCreatedProto.KnowledgeSessionCreated.Builder builder = KnowledgeSessionCreatedProto.KnowledgeSessionCreated
                .newBuilder();

        return commonObject.getKnowledgeSession() != null
                ? builder.setKnowledgeSession(codec.map(commonObject.getKnowledgeSession())).build()
                : builder.build();
    }
}
