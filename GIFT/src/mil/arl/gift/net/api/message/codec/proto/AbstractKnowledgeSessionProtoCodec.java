/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractKnowledgeSessionProto;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.IndividualKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * AbstractKnowledgeSession message.
 * 
 * @author cpolynice
 *
 */
public class AbstractKnowledgeSessionProtoCodec
        implements ProtoCodec<AbstractKnowledgeSessionProto.AbstractKnowledgeSession, AbstractKnowledgeSession> {

    /* Codec that will be used to convert to/from a protobuf {@link
     * IndividualKnowledgeSession}. */
    private static IndividualKnowledgeSessionProtoCodec individualCodec = new IndividualKnowledgeSessionProtoCodec();

    /* Codec that will be used to convert to/from a protobuf {@link
     * TeamKnowledgeSession}. */
    private static TeamKnowledgeSessionProtoCodec teamCodec = new TeamKnowledgeSessionProtoCodec();

    @Override
    public AbstractKnowledgeSession convert(AbstractKnowledgeSessionProto.AbstractKnowledgeSession protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasIndividialKnowledgeSession()) {
            return individualCodec.convert(protoObject.getIndividialKnowledgeSession());
        } else if (protoObject.hasTeamKnowledgeSession()) {
            return teamCodec.convert(protoObject.getTeamKnowledgeSession());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractKnowledgeSessionProto.AbstractKnowledgeSession map(AbstractKnowledgeSession commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractKnowledgeSessionProto.AbstractKnowledgeSession.Builder builder = AbstractKnowledgeSessionProto.AbstractKnowledgeSession
                .newBuilder();

        if (commonObject instanceof IndividualKnowledgeSession) {
            builder.setIndividialKnowledgeSession(individualCodec.map((IndividualKnowledgeSession) commonObject));
        } else if (commonObject instanceof TeamKnowledgeSession) {
            builder.setTeamKnowledgeSession(teamCodec.map((TeamKnowledgeSession) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(), "Found unhandled session of " + commonObject);
        }

        return builder.build();
    }

}
