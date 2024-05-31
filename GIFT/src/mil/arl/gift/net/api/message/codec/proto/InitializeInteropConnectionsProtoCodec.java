/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;

import generated.proto.common.InitializeInteropConnectionsProto;
import mil.arl.gift.common.InitializeInteropConnections;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * InitializeInteropConnections instance.
 * 
 * @author cpolynice
 *
 */
public class InitializeInteropConnectionsProtoCodec implements
        ProtoCodec<InitializeInteropConnectionsProto.InitializeInteropConnections, InitializeInteropConnections> {

    /* Default server address. */
    private static final String LEGACY_CONTENT_SERVER_ADDR = "http://legacy.init.interops.message:1234";

    @Override
    public InitializeInteropConnections convert(
            InitializeInteropConnectionsProto.InitializeInteropConnections protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<String> interops = CollectionUtils.isNotEmpty(protoObject.getInteropsList())
                ? new ArrayList<>(protoObject.getInteropsList())
                : new ArrayList<>();

        String contentServerAddress = protoObject.hasContentServerAddr() ? protoObject.getContentServerAddr().getValue()
                : LEGACY_CONTENT_SERVER_ADDR;
        String requestingObserver = protoObject.hasRequestingObserver() ? protoObject.getRequestingObserver().getValue()
                : null;
        InitializeInteropConnections init = new InitializeInteropConnections(contentServerAddress, interops,
                requestingObserver);

        boolean isPlayback = protoObject.hasPlayback() ? protoObject.getPlayback().getValue() : false;
        init.setPlayback(isPlayback);

        return init;
    }

    @Override
    public InitializeInteropConnectionsProto.InitializeInteropConnections map(
            InitializeInteropConnections commonObject) {
        if (commonObject == null) {
            return null;
        }

        InitializeInteropConnectionsProto.InitializeInteropConnections.Builder builder = InitializeInteropConnectionsProto.InitializeInteropConnections
                .newBuilder();

        builder.setPlayback(BoolValue.of(commonObject.isPlayback()));
        Optional.ofNullable(commonObject.getInterops()).ifPresent(builder::addAllInterops);
        Optional.ofNullable(commonObject.getDomainServerAddress()).ifPresent(addr -> {
            builder.setContentServerAddr(StringValue.of(addr));
        });
        Optional.ofNullable(commonObject.getRequestingObserver()).ifPresent(req -> {
            builder.setRequestingObserver(StringValue.of(req));
        });

        return builder.build();
    }

}
