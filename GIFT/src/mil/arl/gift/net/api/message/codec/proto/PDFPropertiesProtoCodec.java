/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.course.PDFProperties;
import generated.proto.common.PDFPropertiesProto;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf PDFProperties
 * message.
 * 
 * @author cpolynice
 *
 */
public class PDFPropertiesProtoCodec implements ProtoCodec<PDFPropertiesProto.PDFProperties, PDFProperties> {

    @Override
    public PDFProperties convert(generated.proto.common.PDFPropertiesProto.PDFProperties protoObject) {
        return new PDFProperties();
    }

    @Override
    public PDFPropertiesProto.PDFProperties map(PDFProperties commonObject) {
        return PDFPropertiesProto.PDFProperties.newBuilder().build();
    }

}
