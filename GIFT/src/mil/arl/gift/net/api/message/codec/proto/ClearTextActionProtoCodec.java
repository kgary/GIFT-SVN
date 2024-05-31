/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.ClearTextActionProto;
import mil.arl.gift.common.ClearTextAction;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a ClearTextAction message.
 * 
 * @author cpolynice
 *
 */
public class ClearTextActionProtoCodec implements ProtoCodec<ClearTextActionProto.ClearTextAction, ClearTextAction> {

    @Override
    public ClearTextAction convert(ClearTextActionProto.ClearTextAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        return new ClearTextAction();
    }

    @Override
    public ClearTextActionProto.ClearTextAction map(ClearTextAction commonObject) {
        if (commonObject == null) {
            return null;
        }

        return ClearTextActionProto.ClearTextAction.newBuilder().build();
    }

}
