/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.FinishScenarioProto;
import mil.arl.gift.common.FinishScenario;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf FinishScenario
 * message.
 * 
 * @author cpolynice
 *
 */
public class FinishScenarioProtoCodec implements ProtoCodec<FinishScenarioProto.FinishScenario, FinishScenario> {

    @Override
    public FinishScenario convert(FinishScenarioProto.FinishScenario protoObject) {
        if (protoObject == null) {
            return null;
        }

        return new FinishScenario();
    }

    @Override
    public FinishScenarioProto.FinishScenario map(FinishScenario commonObject) {
        if (commonObject == null) {
            return null;
        }

        return FinishScenarioProto.FinishScenario.newBuilder().build();
    }

}
