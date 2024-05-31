/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.course.Nvpair;
import generated.proto.common.NvPairProto;
import generated.proto.common.NvPairProto.NvPair;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf Nvpair message.
 * 
 * @author cpolynice
 *
 */
public class NvPairProtoCodec implements ProtoCodec<NvPairProto.NvPair, Nvpair> {

    @Override
    public Nvpair convert(NvPair protoObject) {
        if (protoObject == null) {
            return null;
        }

        Nvpair np = new Nvpair();

        if (protoObject.hasName()) {
            np.setName(protoObject.getName().getValue());
        }

        if (protoObject.hasValue()) {
            np.setValue(protoObject.getValue().getValue());
        }

        return np;
    }

    @Override
    public NvPair map(Nvpair commonObject) {
        if (commonObject == null) {
            return null;
        }

        NvPairProto.NvPair.Builder builder = NvPairProto.NvPair.newBuilder();

        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getValue()).ifPresent(value -> {
            builder.setValue(StringValue.of(value));
        });

        return builder.build();
    }

}
