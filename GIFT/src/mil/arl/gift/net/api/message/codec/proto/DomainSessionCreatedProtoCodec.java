/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.DomainSessionCreatedProto.DomainSessionCreated;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DomainSessionCreated.
 * 
 * @author cpolynice
 *
 */
public class DomainSessionCreatedProtoCodec implements ProtoCodec<DomainSessionCreated, DomainSession> {

    /* Codec that will be used to convert to/from a DomainSession. */
    private static DomainSessionProtoCodec codec = new DomainSessionProtoCodec();

    @Override
    public DomainSession convert(DomainSessionCreated protoObject) {
        if (protoObject == null) {
            return null;
        }

        return protoObject.hasDomainSession() ? codec.convert(protoObject.getDomainSession()) : null;
    }

    @Override
    public DomainSessionCreated map(DomainSession commonObject) {
        if (commonObject == null) {
            return null;
        }

        return DomainSessionCreated.newBuilder().setDomainSession(codec.map(commonObject)).build();
    }

}
