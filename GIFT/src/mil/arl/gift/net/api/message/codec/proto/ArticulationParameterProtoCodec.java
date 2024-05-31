/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int32Value;

import generated.proto.common.ArticulationParameterProto;
import mil.arl.gift.common.enums.ArticulationParameterTypeDesignatorEnum;
import mil.arl.gift.common.ta.state.ArticulationParameter;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an Articulation Parameter
 * instance.
 * 
 * @author cpolynice
 */
public class ArticulationParameterProtoCodec
        implements ProtoCodec<ArticulationParameterProto.ArticulationParameter, ArticulationParameter> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ArticulationParameterProtoCodec.class);

    @Override
    public ArticulationParameter convert(ArticulationParameterProto.ArticulationParameter protoObject) {
        if (protoObject == null) {
            return null;
        }

        int parameterChange, partAttachedToID, parameterType;
        double parameterValue;
        ArticulationParameterTypeDesignatorEnum parameterTypeDesignator = null;

        parameterChange = protoObject.getParameterChange().getValue();
        partAttachedToID = protoObject.getPartAttachedToId().getValue();
        parameterType = protoObject.getParameterType().getValue();
        parameterValue = protoObject.getParameterValue().getValue();

        if (protoObject.hasParameterTypeDesignator()) {
            parameterTypeDesignator = ArticulationParameterTypeDesignatorEnum
                    .valueOf(protoObject.getParameterTypeDesignator().getValue());
        }

        if (parameterTypeDesignator != null) {
            return new ArticulationParameter(parameterTypeDesignator, parameterChange, partAttachedToID, parameterType,
                    parameterValue);
        } else {
            logger.error("Could not convert the given protobuf object to the common object.");
            return null;
        }
    }

    @Override
    public ArticulationParameterProto.ArticulationParameter map(ArticulationParameter commonObject) {
        if (commonObject == null) {
            return null;
        }

        ArticulationParameterProto.ArticulationParameter.Builder builder = ArticulationParameterProto.ArticulationParameter
                .newBuilder();

        Optional.ofNullable(commonObject.getParameterTypeDesignator())
                .ifPresent(paramDes -> {
                    builder.setParameterTypeDesignator(Int32Value.of(paramDes.getValue()));
                });

        builder.setParameterChange(Int32Value.of(commonObject.getParameterChange()));
        builder.setPartAttachedToId(Int32Value.of(commonObject.getPartAttachedToID()));
        builder.setParameterType(Int32Value.of(commonObject.getParameterType()));
        builder.setParameterValue(DoubleValue.of(commonObject.getParameterValue()));
        
        return builder.build();
    }

}
