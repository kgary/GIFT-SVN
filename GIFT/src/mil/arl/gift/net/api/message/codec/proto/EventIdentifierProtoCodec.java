/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.Int32Value;

import generated.proto.common.EventIdentifierProto;
import mil.arl.gift.common.ta.state.EventIdentifier;
import mil.arl.gift.common.ta.state.SimulationAddress;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf EventIdentifier
 * instance.
 * 
 * @author cpolynice
 *
 */
public class EventIdentifierProtoCodec implements ProtoCodec<EventIdentifierProto.EventIdentifier, EventIdentifier> {

    /* Codec that will be used to convert to/from a SimulationAddress
     * instance. */
    private static SimulationAddressProtoCodec codec = new SimulationAddressProtoCodec();

    @Override
    public EventIdentifier convert(EventIdentifierProto.EventIdentifier protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        int eventId = protoObject.hasEventId() ? protoObject.getEventId().getValue() : 0;
        SimulationAddress simAddr = protoObject.hasSimAddr() ? codec.convert(protoObject.getSimAddr()) : null;
        return new EventIdentifier(simAddr, eventId);
    }

    @Override
    public EventIdentifierProto.EventIdentifier map(EventIdentifier commonObject) {
        if (commonObject == null) {
            return null;
        }

        EventIdentifierProto.EventIdentifier.Builder builder = EventIdentifierProto.EventIdentifier.newBuilder();

        builder.setEventId(Int32Value.of(commonObject.getEventID()));
        Optional.ofNullable(codec.map(commonObject.getSimulationAddress())).ifPresent(builder::setSimAddr);
        return builder.build();
    }
}
