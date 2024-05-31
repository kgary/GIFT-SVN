/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;

import generated.proto.common.SimanProto;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingAppRouteTypeEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf Siman.
 * 
 * @author cpolynice
 *
 */
public class SimanProtoCodec implements ProtoCodec<SimanProto.Siman, Siman> {

    /* Constant that will assist in mapping the null value to protobuf since
     * protobuf values itself cannot be null. */
    private static final String MAP_NULL_VALUE = "null";

    /* Codec that will be used to convert to/from a protobuf AbstractEnum. */
    private static final AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /**
     * Converts the given protobuf map to the common object representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map representation.
     */
    private static Map<String, Serializable> convertLoadArgs(Map<String, String> protoMap, boolean isEmbedded) {
        if (protoMap == null) {
            return null;
        }

        Map<String, Serializable> commonMap = new HashMap<>();

        for (Map.Entry<String, String> arg : protoMap.entrySet()) {
            String key = arg.getKey();
            Serializable value;
            try {
                value = !arg.getValue().equals(MAP_NULL_VALUE)
                        ? Siman.getLoadArgFromXMLString(arg.getValue(), isEmbedded)
                        : null;
                commonMap.put(key, value);
            } catch (Exception e) {
                throw new MessageEncodeException(SimanProtoCodec.class.getName(), "Exception logged while decoding", e);
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
    private static Map<String, String> mapLoadArgs(Map<String, Serializable> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, String> protoMap = new HashMap<>();

        for (String key : commonMap.keySet()) {
            try {
                String value = Siman.getLoadArgsAsXMLString(commonMap.get(key));
                protoMap.put(key, value == null ? MAP_NULL_VALUE : value);
            } catch (Exception e) {
                throw new MessageEncodeException(SimanProtoCodec.class.getName(), "Exception logged while encoding", e);
            }
        }

        return protoMap;
    }

    @Override 
    public Siman convert(SimanProto.Siman protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        long fileSize = protoObject.hasFileSize() ? protoObject.getFileSize().getValue() : 0l;
        SimanTypeEnum simanType = protoObject.hasSimanType() ? (SimanTypeEnum) enumCodec.convert(protoObject.getSimanType()) : null;
        TrainingAppRouteTypeEnum routeType = protoObject.hasRouteType() ? (TrainingAppRouteTypeEnum) enumCodec.convert(protoObject.getRouteType()) : TrainingAppRouteTypeEnum.INTEROP;

        if (CollectionUtils.isNotEmpty(protoObject.getLoadArgsMap())) {
            
            Map<String, Serializable> loadArgs = convertLoadArgs(protoObject.getLoadArgsMap(),
                    routeType == TrainingAppRouteTypeEnum.EMBEDDED);
            Siman simanLoad = Siman.CreateLoad(loadArgs);
            
            if (protoObject.hasCourseFolder()){
                simanLoad.setRuntimeCourseFolderPath(protoObject.getCourseFolder().getValue());
            }
            
            simanLoad.setFileSize(fileSize);
            simanLoad.setRouteType(routeType);
            
            return simanLoad;
        } else {
            Siman siman = Siman.Create(simanType);
            siman.setFileSize(fileSize);
            siman.setRouteType(routeType);
            return siman;
        }
    }

    @Override
    public SimanProto.Siman map(Siman commonObject) {
        if (commonObject == null) {
            return null;
        }

        SimanProto.Siman.Builder builder = SimanProto.Siman.newBuilder();

        builder.setFileSize(Int64Value.of(commonObject.getFileSize()));
        Optional.ofNullable(enumCodec.map(commonObject.getSimanTypeEnum())).ifPresent(builder::setSimanType);
        Optional.ofNullable(enumCodec.map(commonObject.getRouteType())).ifPresent(builder::setRouteType);
        Optional.ofNullable(mapLoadArgs(commonObject.getLoadArgs())).ifPresent(builder::putAllLoadArgs);
        Optional.ofNullable(commonObject.getRuntimeCourseFolderPath()).ifPresent(folder -> {
            builder.setCourseFolder(StringValue.of(folder));
        });

        return builder.build();
    }
}
