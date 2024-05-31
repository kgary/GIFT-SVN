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

import com.google.protobuf.StringValue;

import generated.proto.common.SurveyCheckResponseProto;
import generated.proto.common.SurveyCheckResponseProto.ResponseInterfaceList;
import mil.arl.gift.common.SurveyCheckResponse;
import mil.arl.gift.common.SurveyCheckResponse.FailureResponse;
import mil.arl.gift.common.SurveyCheckResponse.ResponseInterface;
import mil.arl.gift.common.SurveyCheckResponse.SuccessResponse;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * SurveyCheckResponse.
 * 
 * @author cpolynice
 *
 */
public class SurveyCheckResponseProtoCodec
        implements ProtoCodec<SurveyCheckResponseProto.SurveyCheckResponse, SurveyCheckResponse> {

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<ResponseInterface> convertResponseList(
            ResponseInterfaceList protoList) {
        if (protoList == null) {
            return null;
        }

        List<ResponseInterface> commonList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(protoList.getResponseListList())) {
            for (SurveyCheckResponseProto.ResponseInterface response : protoList.getResponseListList()) {
                if (response.hasFailureResponse() && response.getFailureResponse().hasMessage()) {
                    commonList.add(new FailureResponse(response.getFailureResponse().getMessage().getValue()));
                } else {
                    commonList.add(new SuccessResponse());
                }
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
    private static Map<String, List<ResponseInterface>> convertResponses(Map<String, ResponseInterfaceList> protoMap) {
        if (protoMap == null) {
            return null;
        }
        
        Map<String, List<ResponseInterface>> commonMap = new HashMap<>();
        
        for (Map.Entry<String, ResponseInterfaceList> response : protoMap.entrySet()) {
            String key = response.getKey();
            List<ResponseInterface> value = CollectionUtils.isNotEmpty(response.getValue().getResponseListList())
                    ? convertResponseList(response.getValue())
                    : null;

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
    private static ResponseInterfaceList mapResponseList(List<ResponseInterface> commonList) {
        if (commonList == null) {
            return null;
        }

        ResponseInterfaceList.Builder protoList = ResponseInterfaceList.newBuilder();

        for (ResponseInterface response : commonList) {
            if (response instanceof FailureResponse) {
                Optional.ofNullable(((FailureResponse) response).getMessage()).ifPresent(message -> {
                    SurveyCheckResponseProto.FailureResponse.Builder fBuilder = SurveyCheckResponseProto.FailureResponse
                            .newBuilder();
                    fBuilder.setMessage(StringValue.of(message));
                    protoList.addResponseList(
                            SurveyCheckResponseProto.ResponseInterface.newBuilder().setFailureResponse(fBuilder));
                });
            } else {
                SurveyCheckResponseProto.SuccessResponse success = SurveyCheckResponseProto.SuccessResponse.newBuilder()
                        .build();
                protoList.addResponseList(
                        SurveyCheckResponseProto.ResponseInterface.newBuilder().setSuccessResponse(success));
            }
        }

        return protoList.build();
    }

    /**
     * Maps the given common object map to the protobuf map representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map representation.
     */
    private static Map<String, ResponseInterfaceList> mapResponses(Map<String, List<ResponseInterface>> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, ResponseInterfaceList> protoMap = new HashMap<>();

        for (Map.Entry<String, List<ResponseInterface>> response : commonMap.entrySet()) {
            String key = response.getKey();
            ResponseInterfaceList value = mapResponseList(response.getValue());

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public SurveyCheckResponse convert(SurveyCheckResponseProto.SurveyCheckResponse protoObject) {
        if (protoObject == null) {
            return null;
        }

        return CollectionUtils.isNotEmpty(protoObject.getResponsesMap())
                ? new SurveyCheckResponse(convertResponses(protoObject.getResponsesMap()))
                : null;
    }

    @Override
    public SurveyCheckResponseProto.SurveyCheckResponse map(SurveyCheckResponse commonObject) {
        if (commonObject == null) {
            return null;
        }

        SurveyCheckResponseProto.SurveyCheckResponse.Builder builder = SurveyCheckResponseProto.SurveyCheckResponse
                .newBuilder();

        return CollectionUtils.isNotEmpty(commonObject.getResponses())
                ? builder.putAllResponses(mapResponses(commonObject.getResponses())).build()
                : builder.build();
    }
}
