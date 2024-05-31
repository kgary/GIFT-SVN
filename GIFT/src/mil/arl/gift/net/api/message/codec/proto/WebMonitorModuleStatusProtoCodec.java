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

import generated.proto.common.WebMonitorModuleStatusProto;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.WebMonitorModuleStatus;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a WebMonitorModuleStatus class.
 * 
 * @author oamer
 */
public class WebMonitorModuleStatusProtoCodec implements ProtoCodec<WebMonitorModuleStatusProto.WebMonitorModuleStatus, WebMonitorModuleStatus> {
    ModuleStatusProtoCodec moduleStatusProtoCodec = new ModuleStatusProtoCodec();

    @Override
    public WebMonitorModuleStatus convert(
            generated.proto.common.WebMonitorModuleStatusProto.WebMonitorModuleStatus protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        Set<Integer> attachedDomainSession = new HashSet<Integer>(protoObject.getAttachedDomainSessionList());
        
        ModuleStatus moduleStatus = protoObject.hasModuleStatus() ? moduleStatusProtoCodec.convert(protoObject.getModuleStatus()) : null;

        return new WebMonitorModuleStatus(moduleStatus, attachedDomainSession);
    }

    @Override
    public generated.proto.common.WebMonitorModuleStatusProto.WebMonitorModuleStatus map(
            WebMonitorModuleStatus commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        WebMonitorModuleStatusProto.WebMonitorModuleStatus.Builder builder = generated.proto.common.WebMonitorModuleStatusProto.WebMonitorModuleStatus.newBuilder();
        Optional.ofNullable(moduleStatusProtoCodec.map(commonObject)).ifPresent(builder::setModuleStatus);
        Optional.ofNullable(commonObject.getAttachedDomainSessions()).ifPresent(builder::addAllAttachedDomainSession);
        
        return builder.build();
    }

}
