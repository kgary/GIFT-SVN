/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;

import generated.proto.common.InitializeEmbeddedConnectionsProto;
import mil.arl.gift.common.InitializeEmbeddedConnections;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * InitializeEmbeddedConnections instance.
 * 
 * @author cpolynice
 *
 */
public class InitializeEmbeddedConnectionsProtoCodec implements
        ProtoCodec<InitializeEmbeddedConnectionsProto.InitializeEmbeddedConnections, InitializeEmbeddedConnections> {

    @Override
    public InitializeEmbeddedConnections convert(
            InitializeEmbeddedConnectionsProto.InitializeEmbeddedConnections protoObject) {
        if (protoObject == null) {
            return null;
        }

        return CollectionUtils.isNotEmpty(protoObject.getUrlsList())
                ? new InitializeEmbeddedConnections(new ArrayList<>(protoObject.getUrlsList()))
                : null;
    }

    @Override
    public InitializeEmbeddedConnectionsProto.InitializeEmbeddedConnections map(
            InitializeEmbeddedConnections commonObject) {
        if (commonObject == null) {
            return null;
        }

        InitializeEmbeddedConnectionsProto.InitializeEmbeddedConnections.Builder builder = InitializeEmbeddedConnectionsProto.InitializeEmbeddedConnections
                .newBuilder();

        return CollectionUtils.isNotEmpty(commonObject.getUrls()) ? builder.addAllUrls(commonObject.getUrls()).build()
                : builder.build();
    }

}
