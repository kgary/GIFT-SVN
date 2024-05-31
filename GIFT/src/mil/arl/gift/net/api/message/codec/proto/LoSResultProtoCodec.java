/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.LoSResultProto;
import generated.proto.common.LoSResultProto.VisibilityResultList;
import mil.arl.gift.common.ta.state.LoSResult;
import mil.arl.gift.common.ta.state.LoSResult.VisibilityResult;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf LoSResult.
 * 
 * @author cpolynice
 *
 */
public class LoSResultProtoCodec implements ProtoCodec<LoSResultProto.LoSResult, LoSResult> {

    /** used for legacy messages that didn't have a request id */
    private static final String UNKNOWN_ID = "UNKNOWN";

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<VisibilityResult> convertVisibilityResultList(VisibilityResultList protoList) {
        if (protoList == null || (protoList != null && CollectionUtils.isEmpty(protoList.getVisibilityResultList()))) {
            return null;
        }

        List<VisibilityResult> commonList = new ArrayList<>();

        for (LoSResultProto.VisibilityResult result : protoList.getVisibilityResultList()) {
            int pointIndex = result.hasPointIndex() ? result.getPointIndex().getValue() : 0;
            double visibility = result.hasVisibility() ? result.getVisibility().getValue() : 0.0;
            commonList.add(new VisibilityResult(pointIndex, visibility));
        }

        return commonList;
    }

    /**
     * Converts the given protobuf map to the common object representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map.
     */
    private static Map<String, List<VisibilityResult>> convertEntityResults(
            Map<String, VisibilityResultList> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<String, List<VisibilityResult>> commonMap = new HashMap<>();

        for (Map.Entry<String, VisibilityResultList> entity : protoMap.entrySet()) {
            String key = entity.getKey();
            List<VisibilityResult> value = convertVisibilityResultList(entity.getValue());

            if (value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object list to the protobuf list object
     * representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf VisibilityResultList object.
     */
    private static VisibilityResultList mapVisibilityResultList(List<VisibilityResult> commonList) {
        if (commonList == null) {
            return null;
        }

        VisibilityResultList.Builder protoList = VisibilityResultList.newBuilder();

        for (VisibilityResult result : commonList) {
            int pointIndex = result.getIndexOfPointFromRequest();
            double visibility = result.getVisbilityPercent();
            LoSResultProto.VisibilityResult.Builder resultBuilder = LoSResultProto.VisibilityResult.newBuilder();
            resultBuilder.setPointIndex(Int32Value.of(pointIndex)).setVisibility(DoubleValue.of(visibility));
            protoList.addVisibilityResult(resultBuilder);
        }

        return protoList.build();
    }

    /**
     * Maps the given common object map to the protobuf map representation.
     * 
     * @param commonMap the common object map.
     * @return the protobuf map.
     */
    private static Map<String, VisibilityResultList> mapEntityResults(Map<String, List<VisibilityResult>> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, VisibilityResultList> protoMap = new HashMap<>();

        for (Map.Entry<String, List<VisibilityResult>> entity : commonMap.entrySet()) {
            String key = entity.getKey();
            VisibilityResultList value = mapVisibilityResultList(entity.getValue());

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public LoSResult convert(LoSResultProto.LoSResult protoObject) {
        if (protoObject == null) {
            return null;
        }

        String requestId = protoObject.hasRequestId() ? protoObject.getRequestId().getValue() : UNKNOWN_ID;
        Map<String, List<VisibilityResult>> entityLoSResults;

        if (CollectionUtils.isEmpty(protoObject.getEntityResultsMap())) {
            entityLoSResults = new HashMap<>();
            List<VisibilityResult> results = new ArrayList<>();
            entityLoSResults.put("NULL", results);
        } else {
            entityLoSResults = convertEntityResults(protoObject.getEntityResultsMap());
        }

        return new LoSResult(entityLoSResults, requestId);

    }

    @Override
    public LoSResultProto.LoSResult map(LoSResult commonObject) {
        if (commonObject == null) {
            return null;
        }

        LoSResultProto.LoSResult.Builder builder = LoSResultProto.LoSResult.newBuilder();

        Optional.ofNullable(mapEntityResults(commonObject.getEntitiesLoSResults()))
                .ifPresent(builder::putAllEntityResults);
        Optional.ofNullable(commonObject.getRequestId()).ifPresent(id -> {
            builder.setRequestId(StringValue.of(id));
        });

        return builder.build();
    }

}
