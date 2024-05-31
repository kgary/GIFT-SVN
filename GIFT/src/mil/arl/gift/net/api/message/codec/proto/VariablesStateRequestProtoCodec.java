/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.protobuf.StringValue;

import generated.proto.common.VariablesStateRequestProto;
import mil.arl.gift.common.ta.request.VariablesStateRequest;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VARIABLE_TYPE;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VariableInfo;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * VariablesStateRequest.
 * 
 * @author cpolynice
 *
 */
public class VariablesStateRequestProtoCodec
        implements ProtoCodec<VariablesStateRequestProto.VariablesStateRequest, VariablesStateRequest> {

    /**
     * Converts the given protobuf map to the common object map representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map
     */
    private Map<VARIABLE_TYPE, VariableInfo> convertTypesMap(
            Map<String, VariablesStateRequestProto.VariableInfo> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<VARIABLE_TYPE, VariableInfo> commonMap = new HashMap<>();

        for (Map.Entry<String, VariablesStateRequestProto.VariableInfo> type : protoMap.entrySet()) {
            VARIABLE_TYPE key = VARIABLE_TYPE.valueOf(type.getKey());
            VariablesStateRequestProto.VariableInfo vInfo = type.getValue();

            if (key != null && vInfo != null) {
                Set<String> entities = new HashSet<>(vInfo.getEntityIdsList());
                VariableInfo value = new VariableInfo(entities);

                if (vInfo.hasVarName()) {
                    value.setVarName(vInfo.getVarName().getValue());
                }

                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object map to the protobuf representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map
     */
    private Map<String, VariablesStateRequestProto.VariableInfo> mapTypesMap(
            Map<VARIABLE_TYPE, VariableInfo> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, VariablesStateRequestProto.VariableInfo> protoMap = new HashMap<>();

        for (Map.Entry<VARIABLE_TYPE, VariableInfo> type : commonMap.entrySet()) {
            String key = type.getKey().name();
            VariableInfo value = type.getValue();

            if (value != null) {
                VariablesStateRequestProto.VariableInfo.Builder vBuilder = VariablesStateRequestProto.VariableInfo
                        .newBuilder();

                Optional.ofNullable(value.getEntityIds()).ifPresent(vBuilder::addAllEntityIds);
                Optional.ofNullable(value.getVarName()).ifPresent(varName -> {
                    vBuilder.setVarName(StringValue.of(varName));
                });

                protoMap.put(key, vBuilder.build());
            }
        }

        return protoMap;
    }

    @Override
    public VariablesStateRequest convert(VariablesStateRequestProto.VariablesStateRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        String requestId = protoObject.hasRequestId() ? protoObject.getRequestId().getValue() : null;
        VariablesStateRequest request = new VariablesStateRequest(requestId);
        request.getTypeToVarInfoMap().putAll(convertTypesMap(protoObject.getTypesMapMap()));
        return request;
    }

    @Override
    public VariablesStateRequestProto.VariablesStateRequest map(VariablesStateRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        VariablesStateRequestProto.VariablesStateRequest.Builder builder = VariablesStateRequestProto.VariablesStateRequest
                .newBuilder();

        Optional.ofNullable(mapTypesMap(commonObject.getTypeToVarInfoMap())).ifPresent(builder::putAllTypesMap);
        Optional.ofNullable(commonObject.getRequestId()).ifPresent(id -> {
            builder.setRequestId(StringValue.of(id));
        });

        return builder.build();
    }
}
