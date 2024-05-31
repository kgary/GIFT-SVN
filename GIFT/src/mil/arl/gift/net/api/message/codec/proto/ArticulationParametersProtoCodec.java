/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.proto.common.ArticulationParameterProto;
import generated.proto.common.ArticulationParametersProto;
import generated.proto.common.ArticulationParametersProto.ArticulationParameters;
import mil.arl.gift.common.ta.state.ArticulationParameter;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a list of ArticulationParameter
 * instances.
 * 
 * @author cpolynice
 */
public class ArticulationParametersProtoCodec
        implements ProtoCodec<ArticulationParametersProto.ArticulationParameters, List<ArticulationParameter>> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ArticulationParametersProtoCodec.class);

    /* Codec that will be used to convert to/from protobuf ArticulationParameter
     * instances. */
    private final ArticulationParameterProtoCodec codec = new ArticulationParameterProtoCodec();

    @Override
    public List<ArticulationParameter> convert(ArticulationParameters protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<ArticulationParameter> artParams = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(protoObject.getArticulationParametersList())) {
            for (ArticulationParameterProto.ArticulationParameter protoAp : protoObject
                    .getArticulationParametersList()) {
                ArticulationParameter value = codec.convert(protoAp);

                if (value != null) {
                    artParams.add(value);
                }
            }

            return artParams;
        } else {
            logger.error("Could not convert the given protobuf object to the common object.");
            return null;
        }
    }

    @Override
    public ArticulationParameters map(List<ArticulationParameter> commonObject) {
        if (CollectionUtils.isEmpty(commonObject)) {
            return null;
        }
        
        ArticulationParametersProto.ArticulationParameters.Builder builder = ArticulationParametersProto.ArticulationParameters.newBuilder();
        
        for (ArticulationParameter ap : commonObject) {
            ArticulationParameterProto.ArticulationParameter value = codec.map(ap);

            if (value != null) {
                builder.addArticulationParameters(value);
            }
        }

        return builder.build();
    }

}
