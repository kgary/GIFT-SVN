/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.Int32Value;

import generated.proto.common.SimulationAddressProto;
import mil.arl.gift.common.ta.state.SimulationAddress;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Simulation Address instance.
 * 
 * @author cpolynice
 */
public class SimulationAddressProtoCodec
        implements ProtoCodec<SimulationAddressProto.SimulationAddress, SimulationAddress> {

    @Override
    public SimulationAddress convert(SimulationAddressProto.SimulationAddress protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        int appId, siteId;
        
        if (protoObject.hasAppId() && protoObject.hasSiteId()) {
            appId = protoObject.getAppId().getValue();
            siteId = protoObject.getSiteId().getValue();
            return new SimulationAddress(siteId, appId);
        } else {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Exception logged while decoding " + protoObject);
        }
    }

    @Override
    public SimulationAddressProto.SimulationAddress map(SimulationAddress commonObject) {
        if (commonObject == null) {
            return null;
        }

        SimulationAddressProto.SimulationAddress.Builder builder = SimulationAddressProto.SimulationAddress
                .newBuilder();

        builder.setAppId(Int32Value.newBuilder().setValue(commonObject.getApplicationID()).build());
        builder.setSiteId(Int32Value.newBuilder().setValue(commonObject.getSiteID()).build());
        return builder.build();
    }
}
