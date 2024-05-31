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

import generated.proto.common.AbstractPedagogicalRequestProto;
import generated.proto.common.PedagogicalRequestProto;
import generated.proto.common.PedagogicalRequestProto.PedagogicalRequestList;
import mil.arl.gift.common.AbstractPedagogicalRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * PedagogicalRequest.
 * 
 * @author cpolynice
 *
 */
public class PedagogicalRequestProtoCodec
        implements ProtoCodec<PedagogicalRequestProto.PedagogicalRequest, PedagogicalRequest> {

    /* Codec that will be used to convert tp/from a protobuf
     * AbstractPedagogicalRequest. */
    private static final AbstractPedagogicalRequestProtoCodec codec = new AbstractPedagogicalRequestProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<AbstractPedagogicalRequest> convertRequestList(PedagogicalRequestList protoList) {
        if (protoList == null) {
            return null;
        }

        List<AbstractPedagogicalRequest> commonList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(protoList.getRequestListList())) {
            for (AbstractPedagogicalRequestProto.AbstractPedagogicalRequest request : protoList.getRequestListList()) {
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
    private static Map<String, List<AbstractPedagogicalRequest>> convertRequests(
            Map<String, PedagogicalRequestList> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<String, List<AbstractPedagogicalRequest>> commonMap = new HashMap<>();

        for (Map.Entry<String, PedagogicalRequestList> request : protoMap.entrySet()) {
            String key = request.getKey();
            List<AbstractPedagogicalRequest> value = convertRequestList(request.getValue());

            if (CollectionUtils.isNotEmpty(value)) {
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
    private static PedagogicalRequestList mapRequestList(List<AbstractPedagogicalRequest> commonList) {
        if (commonList == null) {
            return null;
        }

        PedagogicalRequestList.Builder protoList = PedagogicalRequestList.newBuilder();

        for (AbstractPedagogicalRequest request : commonList) {
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
    private static Map<String, PedagogicalRequestList> mapRequests(
            Map<String, List<AbstractPedagogicalRequest>> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, PedagogicalRequestList> protoMap = new HashMap<>();

        for (Map.Entry<String, List<AbstractPedagogicalRequest>> request : commonMap.entrySet()) {
            String key = request.getKey();
            PedagogicalRequestList value = mapRequestList(request.getValue());

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public PedagogicalRequest convert(PedagogicalRequestProto.PedagogicalRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        Map<String, List<AbstractPedagogicalRequest>> requestsMap = CollectionUtils.isNotEmpty(
                protoObject.getRequestsMap()) ? convertRequests(protoObject.getRequestsMap()) : new HashMap<>();
        return new PedagogicalRequest(requestsMap);

    }

    @Override
    public PedagogicalRequestProto.PedagogicalRequest map(PedagogicalRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        PedagogicalRequestProto.PedagogicalRequest.Builder builder = PedagogicalRequestProto.PedagogicalRequest
                .newBuilder();

        Optional.ofNullable(mapRequests(commonObject.getRequests())).ifPresent(builder::putAllRequests);
        return builder.build();
    }
}
