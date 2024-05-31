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

import com.google.protobuf.StringValue;

import generated.proto.common.AbstractEnumObjectProto;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an AbstractEnumObject
 * class.
 * 
 * @author sharrison
 */
public class AbstractEnumObjectProtoCodec
        implements ProtoCodec<AbstractEnumObjectProto.AbstractEnumObject, AbstractEnum> {

    /** The logger for the class */
    private static Logger logger = LoggerFactory.getLogger(AbstractEnumObjectProtoCodec.class);

    @Override
    public AbstractEnum convert(AbstractEnumObjectProto.AbstractEnumObject protoObject) {
        if (protoObject == null) {
            return null;
        }

        /* Lookup the subclass of the AbstractEnum using the provided class
         * name */
        Class<? extends AbstractEnum> enumClass;
        try {
            Class<?> tempClass = Class.forName(protoObject.getClassName().getValue());
            enumClass = tempClass.asSubclass(AbstractEnum.class);
        } catch (Throwable t) {
            logger.error("Failed to get an AbstractEnum for enum '" + protoObject.getEnumName() + "'.", t);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }

        return AbstractEnum.valueOf(enumClass, protoObject.getEnumName().getValue());
    }

    @Override
    public AbstractEnumObjectProto.AbstractEnumObject map(AbstractEnum commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractEnumObjectProto.AbstractEnumObject.Builder builder = AbstractEnumObjectProto.AbstractEnumObject
                .newBuilder();

        /* Since abstract enums have many subclasses and no way to identify
         * which class the enum came from by its name, we need to store the enum
         * name along with its class name */
        Optional.ofNullable(commonObject.getName()).ifPresent(enumName -> {
            builder.setEnumName(StringValue.of(enumName));
        });
        
        Optional.ofNullable(commonObject.getClass().getName()).ifPresent(className -> {
            builder.setClassName(StringValue.of(className));
        });

        return builder.build();
    }
}
