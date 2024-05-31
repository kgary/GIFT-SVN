/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.StringValue;

import generated.proto.common.DisplayMidLessonMediaRequestProto;
import mil.arl.gift.common.DisplayMidLessonMediaRequest;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DisplayMidLessonMediaRequest.
 * 
 * @author cpolynice
 *
 */
public class DisplayMidLessonMediaRequestProtoCodec implements
        ProtoCodec<DisplayMidLessonMediaRequestProto.DisplayMidLessonMediaRequest, DisplayMidLessonMediaRequest> {

    @Override
    public DisplayMidLessonMediaRequest convert(
            DisplayMidLessonMediaRequestProto.DisplayMidLessonMediaRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            generated.dkf.LessonMaterialList lessonMaterial = null;
            if (protoObject.hasLessonMaterial()) {
                String xmlStr = protoObject.getLessonMaterial().getValue();
                UnmarshalledFile uFile = AbstractSchemaHandler.getFromXMLString(xmlStr,
                        generated.dkf.LessonMaterialList.class, null, true);
                lessonMaterial = (generated.dkf.LessonMaterialList) uFile.getUnmarshalled();
            }
            return new DisplayMidLessonMediaRequest(lessonMaterial);

        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }
    }

    @Override
    public DisplayMidLessonMediaRequestProto.DisplayMidLessonMediaRequest map(
            DisplayMidLessonMediaRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        DisplayMidLessonMediaRequestProto.DisplayMidLessonMediaRequest.Builder builder = DisplayMidLessonMediaRequestProto.DisplayMidLessonMediaRequest
                .newBuilder();

        try {
            if (commonObject.getMediaList() != null) {
                builder.setLessonMaterial(
                        StringValue.of(AbstractSchemaHandler.getAsXMLString(commonObject.getLessonMaterial(),
                                generated.dkf.LessonMaterialList.class, AbstractSchemaHandler.DKF_SCHEMA_FILE)));
            }
        } catch (Exception e) {
            throw new MessageEncodeException(this.getClass().getName(), e.getMessage(), e);
        }

        return builder.build();
    }

}
