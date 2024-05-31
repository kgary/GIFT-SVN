/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.ModuleStatusProto;
import mil.arl.gift.common.enums.ModuleStateEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a ModuleStatus class.
 * 
 * @author oamer
 */
public class ModuleStatusProtoCodec implements ProtoCodec<ModuleStatusProto.ModuleStatus, ModuleStatus>{
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();
    
    @Override
    public ModuleStatus convert(ModuleStatusProto.ModuleStatus protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        ModuleTypeEnum moduleType = protoObject.hasModuleType() ? (ModuleTypeEnum) enumCodec.convert(protoObject.getModuleType()): null;
        String moduleName = protoObject.hasModuleName() ? protoObject.getModuleName().getValue() : null;
        String queueName = protoObject.hasQueueName() ? protoObject.getQueueName().getValue() : null;
        ModuleStatus status = new ModuleStatus(moduleName, queueName, moduleType);

        if (protoObject.hasModuleState()) {
            status.setModuleState((ModuleStateEnum) enumCodec.convert(protoObject.getModuleState()));
        }
                
        if (moduleType == null) {
            throw new MessageDecodeException("MessageProto", "The module type is null");
        } else if (moduleName == null) {
            throw new MessageDecodeException("MessageProto", "The module name is null");
        } else if (queueName == null) {
            throw new MessageDecodeException("MessageProto", "The queue name is null");
        }
        
        return status;
    }

    @Override
    public ModuleStatusProto.ModuleStatus map(ModuleStatus commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        ModuleStatusProto.ModuleStatus.Builder builder = ModuleStatusProto.ModuleStatus.newBuilder();
        
        Optional.ofNullable(commonObject.getModuleName()).ifPresent(moduleName -> {
            builder.setModuleName(StringValue.of(moduleName));
        });
                
        Optional.ofNullable(commonObject.getQueueName()).ifPresent(queueName -> {
            builder.setQueueName(StringValue.of(queueName));
        });
        
        Optional.ofNullable(enumCodec.map(commonObject.getState())).ifPresent(builder::setModuleState);
        
        Optional.ofNullable(enumCodec.map(commonObject.getModuleType())).ifPresent(builder::setModuleType);

        return builder.build();
        
    }
}
