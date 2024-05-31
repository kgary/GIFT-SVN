/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.BoolValue;

import generated.dkf.PreventManualStop;
import generated.dkf.ScenarioControls;
import generated.proto.common.ScenarioControlsProto;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * 
 * This class is responsible for converting to/from a ScenarioControls instance.
 * 
 * @author cpolynice
 *
 */
public class ScenarioControlsProtoCodec
        implements ProtoCodec<ScenarioControlsProto.ScenarioControls, ScenarioControls> {

    @Override
    public ScenarioControls convert(ScenarioControlsProto.ScenarioControls protoObject) {
        if (protoObject == null) {
            return null;
        }

        ScenarioControls controls = new ScenarioControls();
        controls.setPreventManualStop(protoObject.hasPreventManualStop() ? new PreventManualStop() : null);

        return controls;
    }

    @Override
    public ScenarioControlsProto.ScenarioControls map(ScenarioControls commonObject) {
        if (commonObject == null) {
            return null;
        }

        ScenarioControlsProto.ScenarioControls.Builder builder = ScenarioControlsProto.ScenarioControls.newBuilder();

        builder.setPreventManualStop(BoolValue.of(commonObject.getPreventManualStop() != null));
        return builder.build();
    }
}
