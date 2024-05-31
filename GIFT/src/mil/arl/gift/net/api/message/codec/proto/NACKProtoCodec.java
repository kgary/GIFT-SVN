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

import generated.proto.common.NACKProto;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a NACK class.
 * 
 * @author sharrison
 */
public class NACKProtoCodec implements ProtoCodec<NACKProto.NACK, NACK> {

    @Override
    public NACK convert(NACKProto.NACK protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {

            ErrorEnum errorEnum = ErrorEnum.valueOf(protoObject.getErrorEnum().getValue());

            String errorMsg = protoObject.hasErrorMessage() ? protoObject.getErrorMessage().getValue() : null;
            String errorHelp = protoObject.hasErrorHelp() ? protoObject.getErrorHelp().getValue() : null;

            NACK nack = new NACK(errorEnum, errorMsg);
            nack.setErrorHelp(errorHelp);
            return nack;
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }


    }

    @Override
    public NACKProto.NACK map(NACK commonObject) {
        if (commonObject == null) {
            return null;
        }

        NACKProto.NACK.Builder builder = NACKProto.NACK.newBuilder();

        Optional.ofNullable(commonObject.getErrorEnum()).ifPresent(errEnum -> {
            builder.setErrorEnum(StringValue.of(errEnum.getName()));
        });
        
        Optional.ofNullable(commonObject.getErrorMessage()).ifPresent(errorMsg -> {
            builder.setErrorMessage(StringValue.of(errorMsg));
        });
        
        Optional.ofNullable(commonObject.getErrorHelp()).ifPresent(errorHelp -> {
            builder.setErrorHelp(StringValue.of(errorHelp));
        });

        return builder.build();
    }

}
