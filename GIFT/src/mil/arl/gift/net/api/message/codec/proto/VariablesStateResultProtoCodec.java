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

import com.google.protobuf.StringValue;

import generated.proto.common.VariableStateProto;
import generated.proto.common.VariablesStateResultProto;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VARIABLE_TYPE;
import mil.arl.gift.common.ta.state.VariablesState;
import mil.arl.gift.common.ta.state.VariablesState.VariableState;
import mil.arl.gift.common.ta.state.VariablesStateResult;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * VariablesStateResult object.
 * 
 * @author cpolynice
 *
 */
public class VariablesStateResultProtoCodec
        implements ProtoCodec<VariablesStateResultProto.VariablesStateResult, VariablesStateResult> {

    /* This codec is responsible for converting to/from a protobuf
     * VariableState. */
    private static final VariableStateProtoCodec codec = new VariableStateProtoCodec();

    /**
     * Converts the given protobuf VariablesState object to the common object
     * map representation.
     * 
     * @param protoMap the protobuf map, encoded inside a VariablesState object.
     * @return the common object map
     */
    private Map<String, VariableState> convertTypeToEntityMap(VariablesStateResultProto.VariablesState protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<String, VariableState> commonMap = new HashMap<>();

        for (Map.Entry<String, VariableStateProto.VariableState> typeToEntity : protoMap.getTypeToEntityMap()
                .entrySet()) {
            String key = typeToEntity.getKey();
            VariableState value = codec.convert(typeToEntity.getValue());

            if (value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object map to the protobuf representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map, encoded inside a VariablesState object.
     */
    private VariablesStateResultProto.VariablesState mapTypeToEntityMap(Map<String, VariableState> commonMap) {
        if (commonMap == null) {
            return null;
        }

        VariablesStateResultProto.VariablesState.Builder builder = VariablesStateResultProto.VariablesState
                .newBuilder();

        for (Map.Entry<String, VariableState> typeToEntity : commonMap.entrySet()) {
            String key = typeToEntity.getKey();
            VariableStateProto.VariableState value = codec.map(typeToEntity.getValue());

            if (value != null) {
                builder.putTypeToEntity(key, value);
            }
        }

        return builder.build();
    }

    @Override
    public VariablesStateResult convert(VariablesStateResultProto.VariablesStateResult protoObject) {
        if (protoObject == null) {
            return null;
        }

        String requestId = protoObject.hasRequestId() ? protoObject.getRequestId().getValue() : null;
        VariablesState vStates = new VariablesState();

        for (String varType : protoObject.getTypesMapMap().keySet()) {
            VARIABLE_TYPE vType = VARIABLE_TYPE.valueOf(varType);
            Map<String, VariableState> varMap = convertTypeToEntityMap(protoObject.getTypesMapMap().get(varType));
            vStates.getVariableTypeMap().put(vType, varMap);
        }

        return new VariablesStateResult(requestId, vStates);
    }

    @Override
    public VariablesStateResultProto.VariablesStateResult map(VariablesStateResult commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        VariablesStateResultProto.VariablesStateResult.Builder builder = VariablesStateResultProto.VariablesStateResult.newBuilder();
        
        Optional.ofNullable(commonObject.getRequestId()).ifPresent(id -> {
            builder.setRequestId(StringValue.of(id));
        });
        
        VariablesState vState = commonObject.getVariablesState();
        Map<VARIABLE_TYPE, Map<String, VariableState>> typeToEntityValue = vState.getVariableTypeMap();
        
        for (VARIABLE_TYPE type : typeToEntityValue.keySet()) {
            VariablesStateResultProto.VariablesState value = mapTypeToEntityMap(typeToEntityValue.get(type));
           
           if (value != null) {
               builder.putTypesMap(type.name(), value);
           }
        }
        
        return builder.build();
    }

}
