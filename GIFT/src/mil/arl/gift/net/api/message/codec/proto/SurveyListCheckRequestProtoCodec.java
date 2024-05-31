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

import generated.proto.common.SurveyCheckRequestProto;
import generated.proto.common.SurveyListCheckRequestProto;
import generated.proto.common.SurveyListCheckRequestProto.SurveyCheckRequestList;
import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.SurveyListCheckRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * SurveyListCheckRequest.
 * 
 * @author cpolynice
 *
 */
public class SurveyListCheckRequestProtoCodec
        implements ProtoCodec<SurveyListCheckRequestProto.SurveyListCheckRequest, SurveyListCheckRequest> {

    /* Codec that will be used to convert to/from a protobuf
     * SurveyCheckRequest. */
    private static final SurveyCheckRequestProtoCodec codec = new SurveyCheckRequestProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<SurveyCheckRequest> convertRequestList(SurveyCheckRequestList protoList) {
        if (protoList == null) {
            return null;
        }

        List<SurveyCheckRequest> commonList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(protoList.getRequestListList())) {
            for (SurveyCheckRequestProto.SurveyCheckRequest request : protoList.getRequestListList()) {
                commonList.add(codec.convert(request));
            }
        }

        return commonList;
    }

    /**
     * Converts the given protobuf map to the common object representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map representation.
     */
    private static Map<String, List<SurveyCheckRequest>> convertRequests(Map<String, SurveyCheckRequestList> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<String, List<SurveyCheckRequest>> commonMap = new HashMap<>();

        for (Map.Entry<String, SurveyCheckRequestList> request : protoMap.entrySet()) {
            String key = request.getKey();
            List<SurveyCheckRequest> value = convertRequestList(request.getValue());

            if (value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list.
     */
    private static SurveyCheckRequestList mapRequestList(List<SurveyCheckRequest> commonList) {
        if (commonList == null) {
            return null;
        }

        SurveyCheckRequestList.Builder protoList = SurveyCheckRequestList.newBuilder();

        for (SurveyCheckRequest request : commonList) {
            protoList.addRequestList(codec.map(request));
        }

        return protoList.build();
    }

    /**
     * Maps the given common object map to the protobuf map representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map representation.
     */
    private static Map<String, SurveyCheckRequestList> mapRequests(Map<String, List<SurveyCheckRequest>> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, SurveyCheckRequestList> protoMap = new HashMap<>();

        for (Map.Entry<String, List<SurveyCheckRequest>> request : commonMap.entrySet()) {
            String key = request.getKey();
            SurveyCheckRequestList value = mapRequestList(request.getValue());

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public SurveyListCheckRequest convert(SurveyListCheckRequestProto.SurveyListCheckRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        return CollectionUtils.isNotEmpty(protoObject.getRequestsMap())
                ? new SurveyListCheckRequest(convertRequests(protoObject.getRequestsMap()))
                : null;
    }

    @Override
    public SurveyListCheckRequestProto.SurveyListCheckRequest map(SurveyListCheckRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        SurveyListCheckRequestProto.SurveyListCheckRequest.Builder builder = SurveyListCheckRequestProto.SurveyListCheckRequest
                .newBuilder();

        return CollectionUtils.isNotEmpty(commonObject.getRequests())
                ? builder.putAllRequests(mapRequests(commonObject.getRequests())).build()
                : builder.build();
    }

}
