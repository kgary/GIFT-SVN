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

import com.google.protobuf.Int32Value;

import generated.proto.common.DeadReckoningParametersProto;
import mil.arl.gift.common.ta.state.DeadReckoningParameters;
import mil.arl.gift.common.ta.state.DeadReckoningParameters.DeadReckoningAlgorithmField;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Dead Reckoning
 * Parameters instance.
 * 
 * @author cpolynice
 */
public class DeadReckoningParametersProtoCodec
        implements ProtoCodec<DeadReckoningParametersProto.DeadReckoningParameters, DeadReckoningParameters> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DeadReckoningParametersProtoCodec.class);

    @Override
    public DeadReckoningParameters convert(DeadReckoningParametersProto.DeadReckoningParameters protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasAlgorithm()) {
            DeadReckoningAlgorithmField algorithmField = DeadReckoningAlgorithmField.OTHER;
            int algorithmOrdinalIndex = protoObject.getAlgorithm().getValue();
            if (algorithmOrdinalIndex < DeadReckoningAlgorithmField.values().length) {
                algorithmField = DeadReckoningAlgorithmField.values()[algorithmOrdinalIndex];
            }

            DeadReckoningParameters deadReckoningParameters = new DeadReckoningParameters(algorithmField);
            return deadReckoningParameters;
        } else {
            logger.error("Could not convert the given protobuf object to the common object.");
        }

        return null;
    }

    @Override
    public DeadReckoningParametersProto.DeadReckoningParameters map(DeadReckoningParameters commonObject) {
        if (commonObject == null) {
            return null;
        }

        DeadReckoningParametersProto.DeadReckoningParameters.Builder builder = DeadReckoningParametersProto.DeadReckoningParameters
                .newBuilder();

        Optional.ofNullable(commonObject.getDeadReckoningAlgorithmField()).ifPresent(algField -> {
            builder.setAlgorithm(Int32Value.of(algField.ordinal()));
        });

        return builder.build();
    }

}
