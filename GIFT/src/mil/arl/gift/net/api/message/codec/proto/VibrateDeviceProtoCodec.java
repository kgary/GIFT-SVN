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

import generated.proto.common.VibrateDeviceProto.VibrateDevice;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf VibrateDevice.
 * 
 * @author cpolynice
 *
 */
public class VibrateDeviceProtoCodec implements ProtoCodec<VibrateDevice, List<Integer>> {

    @Override
    public List<Integer> convert(VibrateDevice protoObject) {
        if (protoObject == null) {
            return null;
        }

        return CollectionUtils.isNotEmpty(protoObject.getPatternArrayPropertyList())
                ? new ArrayList<>(protoObject.getPatternArrayPropertyList())
                : null;
    }

    @Override
    public VibrateDevice map(List<Integer> commonObject) {
        VibrateDevice.Builder builder = VibrateDevice.newBuilder();
        return CollectionUtils.isNotEmpty(commonObject) ? builder.addAllPatternArrayProperty(commonObject).build()
                : builder.build();
    }

}
