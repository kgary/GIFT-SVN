/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.protobuf.StringValue;

import generated.proto.common.GatewayModuleStatusProto;
import mil.arl.gift.common.module.GatewayModuleStatus;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a GatewayModuleStatus class.
 * 
 * @author oamer
 */
public class GatewayModuleStatusProtoCodec implements ProtoCodec<GatewayModuleStatusProto.GatewayModuleStatus, GatewayModuleStatus>{
    ModuleStatusProtoCodec moduleStatusProtoCodec = new ModuleStatusProtoCodec();

    @Override
    public GatewayModuleStatus convert(
            generated.proto.common.GatewayModuleStatusProto.GatewayModuleStatus protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        String topicName = protoObject.hasTopicName() ? protoObject.getTopicName().getValue() : null;
        ModuleStatus moduleStatus = protoObject.hasModuleStatus()
                ? moduleStatusProtoCodec.convert(protoObject.getModuleStatus())
                : null;
        Set<String> ipAddresses;
        
        if (CollectionUtils.isNotEmpty(protoObject.getIpAddressesList())) {
            ipAddresses = new HashSet<String>(protoObject.getIpAddressesList());
            return new GatewayModuleStatus(topicName, moduleStatus, ipAddresses);
        } else {
            return new GatewayModuleStatus(topicName, moduleStatus);
        }
    }

    @Override
    public GatewayModuleStatusProto.GatewayModuleStatus map(GatewayModuleStatus commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        GatewayModuleStatusProto.GatewayModuleStatus.Builder builder = generated.proto.common.GatewayModuleStatusProto.GatewayModuleStatus.newBuilder();
        
        Optional.ofNullable(moduleStatusProtoCodec.map(commonObject)).ifPresent(builder::setModuleStatus);
        Optional.ofNullable(commonObject.getIPAddresses()).ifPresent(builder::addAllIpAddresses);
        Optional.ofNullable(commonObject.getTopicName()).ifPresent(topicName -> {
            builder.setTopicName(StringValue.of(topicName));
        });

        return builder.build();
    }

}
