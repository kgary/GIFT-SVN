/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.course.CustomParameters;
import generated.course.Nvpair;
import generated.proto.common.LtiGetProviderUrlRequestProto;
import generated.proto.common.NvPairProto;
import generated.proto.common.NvPairProto.NvPair;
import mil.arl.gift.common.lti.LtiGetProviderUrlRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * LtiGetProviderUrlRequest.
 * 
 * @author cpolynice
 *
 */
public class LtiGetProviderUrlRequestProtoCodec
        implements ProtoCodec<LtiGetProviderUrlRequestProto.LtiGetProviderUrlRequest, LtiGetProviderUrlRequest> {

    /* Codec that will be used to convert to/from a protobuf NVPair. */
    private static final NvPairProtoCodec codec = new NvPairProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<Nvpair> convertCustomParameters(List<NvPair> protoList) {
        if (protoList == null) {
            return null;
        }

        List<Nvpair> commonList = new ArrayList<>();

        for (NvPairProto.NvPair param : protoList) {
            commonList.add(codec.convert(param));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list.
     */
    private static List<NvPair> mapCustomParameters(List<Nvpair> commonList) {
        if (commonList == null) {
            return null;
        }

        List<NvPair> protoList = new ArrayList<>();

        for (Nvpair param : commonList) {
            protoList.add(codec.map(param));
        }

        return protoList;
    }

    @Override
    public LtiGetProviderUrlRequest convert(LtiGetProviderUrlRequestProto.LtiGetProviderUrlRequest protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        String ltiId = protoObject.hasLtiId() ? protoObject.getLtiId().getValue() : null;
        CustomParameters customParameters = null;
        
        if (CollectionUtils.isNotEmpty(protoObject.getCustomParametersList())) {
            customParameters = new CustomParameters();

            List<Nvpair> nvPairList = convertCustomParameters(protoObject.getCustomParametersList());
            customParameters.getNvpair().addAll(nvPairList);
        }
        
        String url = protoObject.hasUrl() ? protoObject.getUrl().getValue() : null;
        int domainSessionId = protoObject.hasDomainSessionId() ? protoObject.getDomainSessionId().getValue() : -1;

        return new LtiGetProviderUrlRequest(ltiId, customParameters, url, domainSessionId);
    }

    @Override
    public LtiGetProviderUrlRequestProto.LtiGetProviderUrlRequest map(LtiGetProviderUrlRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        LtiGetProviderUrlRequestProto.LtiGetProviderUrlRequest.Builder builder = LtiGetProviderUrlRequestProto.LtiGetProviderUrlRequest
                .newBuilder();

        builder.setDomainSessionId(Int32Value.of(commonObject.getDomainSessionId()));

        if (commonObject.getCustomParameters() != null
                && CollectionUtils.isNotEmpty(commonObject.getCustomParameters().getNvpair())) {
            builder.addAllCustomParameters(mapCustomParameters(commonObject.getCustomParameters().getNvpair()));
        }

        Optional.ofNullable(commonObject.getLtiId()).ifPresent(id -> {
            builder.setLtiId(StringValue.of(id));
        });
        Optional.ofNullable(commonObject.getRawUrl()).ifPresent(url -> {
            builder.setUrl(StringValue.of(url));
        });

        return builder.build();
    }

}
