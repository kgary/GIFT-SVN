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

import generated.proto.common.DomainModuleStatusProto;
import mil.arl.gift.common.module.DomainModuleStatus;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a DomainModuleStatus class.
 * 
 * @author oamer
 */
public class DomainModuleStatusProtoCodec implements ProtoCodec<DomainModuleStatusProto.DomainModuleStatus, DomainModuleStatus>{
    ModuleStatusProtoCodec moduleStatusProtoCodec = new ModuleStatusProtoCodec();

    @Override
    public DomainModuleStatus convert(generated.proto.common.DomainModuleStatusProto.DomainModuleStatus protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        ModuleStatus moduleStatus = protoObject.hasModuleStatus() ? moduleStatusProtoCodec.convert(protoObject.getModuleStatus()) : null;
        
        Set<String> logPlaybackTopics = new HashSet<String>(protoObject.getLogPlaybackTopicsList());
        
        if (moduleStatus == null) {
            throw new MessageDecodeException("MessageProto", "The module status is null");
        }
        
        return new DomainModuleStatus(moduleStatus, logPlaybackTopics);
    }

    @Override
    public generated.proto.common.DomainModuleStatusProto.DomainModuleStatus map(DomainModuleStatus commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        DomainModuleStatusProto.DomainModuleStatus.Builder builder = generated.proto.common.DomainModuleStatusProto.DomainModuleStatus.newBuilder();
        
        Optional.ofNullable(moduleStatusProtoCodec.map(commonObject)).ifPresent(builder::setModuleStatus);
        Optional.ofNullable(commonObject.getLogPlaybackTopics()).ifPresent(builder::addAllLogPlaybackTopics);

        return builder.build();
    }

}
