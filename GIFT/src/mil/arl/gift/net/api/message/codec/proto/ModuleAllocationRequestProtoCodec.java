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

import generated.proto.common.ModuleAllocationRequestProto;
import generated.proto.common.ModuleAllocationRequestProto.ModuleValue;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.module.GatewayModuleStatus;
import mil.arl.gift.common.module.ModuleAllocationRequest;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * ModuleAllocationRequest instance.
 * 
 * @author cpolynice
 *
 */
public class ModuleAllocationRequestProtoCodec
        implements ProtoCodec<ModuleAllocationRequestProto.ModuleAllocationRequest, ModuleAllocationRequest> {

    /* Codec that will be used to convert to/from a protobuf ModuleStatus. */
    private static final ModuleStatusProtoCodec moduleCodec = new ModuleStatusProtoCodec();

    /* Codec that will be used to convert to/from a protobuf
     * GatewayModuleStatus. */
    private static final GatewayModuleStatusProtoCodec gatewayCodec = new GatewayModuleStatusProtoCodec();

    /**
     * Converts the given protobuf map to the common object representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map representation.
     */
    private static Map<ModuleTypeEnum, ModuleStatus> convertAllocatedModules(Map<String, ModuleValue> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<ModuleTypeEnum, ModuleStatus> commonMap = new HashMap<>();

        for (Map.Entry<String, ModuleValue> info : protoMap.entrySet()) {
            ModuleTypeEnum key = ModuleTypeEnum.valueOf(info.getKey());
            ModuleStatus value;

            if (info.getValue().hasGatewayModuleValue()) {
                value = gatewayCodec.convert(info.getValue().getGatewayModuleValue());
            } else {
                value = moduleCodec.convert(info.getValue().getModuleValue());
            }

            if (key != null && value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object map to the protobuf map representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map representation.
     */
    private static Map<String, ModuleValue> mapAllocatedModules(Map<ModuleTypeEnum, ModuleStatus> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, ModuleValue> protoMap = new HashMap<>();

        for (Map.Entry<ModuleTypeEnum, ModuleStatus> info : commonMap.entrySet()) {
            String key = info.getKey().getName();
            ModuleValue.Builder value = ModuleValue.newBuilder();

            if (info.getValue() instanceof GatewayModuleStatus) {
                value.setGatewayModuleValue(gatewayCodec.map((GatewayModuleStatus) info.getValue()));
            } else {
                value.setModuleValue(moduleCodec.map(info.getValue()));
            }

            if (key != null) {
                protoMap.put(key, value.build());
            }
        }

        return protoMap;
    }

    @Override
    public ModuleAllocationRequest convert(ModuleAllocationRequestProto.ModuleAllocationRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        ModuleStatus requestor = protoObject.hasRequestor() ? moduleCodec.convert(protoObject.getRequestor()) : null;
        Map<ModuleTypeEnum, ModuleStatus> allocatedModules = CollectionUtils.isNotEmpty(
                protoObject.getAllocatedModulesMap()) ? convertAllocatedModules(protoObject.getAllocatedModulesMap())
                        : null;

        /* Check for required inputs. */
        if (requestor == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The requestor status is null");
        }

        ModuleAllocationRequest request = new ModuleAllocationRequest(requestor);
        request.setAllocatedModule(allocatedModules);
        return request;
    }

    @Override
    public ModuleAllocationRequestProto.ModuleAllocationRequest map(ModuleAllocationRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        ModuleAllocationRequestProto.ModuleAllocationRequest.Builder builder = ModuleAllocationRequestProto.ModuleAllocationRequest
                .newBuilder();

        Optional.ofNullable(moduleCodec.map(commonObject.getRequestorInfo())).ifPresent(builder::setRequestor);
        Optional.ofNullable(mapAllocatedModules(commonObject.getAllocatedModules()))
                .ifPresent(builder::putAllAllocatedModules);

        return builder.build();
    }
}
