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

import generated.proto.common.DisplayMediaCollectionRequestProto;
import mil.arl.gift.common.DisplayMediaCollectionRequest;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a
 * DisplayMediaCollectionRequest instance.
 * 
 * @author cpolynice
 *
 */
public class DisplayMediaCollectionRequestProtoCodec implements
        ProtoCodec<DisplayMediaCollectionRequestProto.DisplayMediaCollectionRequest, DisplayMediaCollectionRequest> {
    
    /* Unknown String for legacy content. */
    public static final String LEGACY_CONTENT_REF = "UNKNOWN";

    @Override
    public DisplayMediaCollectionRequest convert(
            DisplayMediaCollectionRequestProto.DisplayMediaCollectionRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        generated.course.LessonMaterial lessonMaterial = null;
        if (protoObject.hasLessonMaterial()) {
            String xmlStr = protoObject.getLessonMaterial().getValue();
            UnmarshalledFile uFile;
            try {
                uFile = AbstractSchemaHandler.getFromXMLString(xmlStr, generated.course.LessonMaterial.class, null,
                        true);
            } catch (Exception e) {
                throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
            }

            lessonMaterial = (generated.course.LessonMaterial) uFile.getUnmarshalled();
        }

        String contentReference;
        if (protoObject.hasContentRef()) {
            contentReference = protoObject.getContentRef().getValue();
        } else {
            contentReference = LEGACY_CONTENT_REF;
        }

        return new DisplayMediaCollectionRequest(lessonMaterial, contentReference);
    }

    @Override
    public DisplayMediaCollectionRequestProto.DisplayMediaCollectionRequest map(
            DisplayMediaCollectionRequest commonObject) {
        DisplayMediaCollectionRequestProto.DisplayMediaCollectionRequest.Builder builder = DisplayMediaCollectionRequestProto.DisplayMediaCollectionRequest
                .newBuilder();
       
        if (commonObject.getMediaList() != null) {    
            try {
                builder.setLessonMaterial(
                        StringValue.of(AbstractSchemaHandler.getAsXMLString(commonObject.getLessonMaterial(),
                                generated.course.LessonMaterial.class, AbstractSchemaHandler.COURSE_SCHEMA_FILE)));
            } catch (Exception e) {
                throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
            }
        } 

        Optional.ofNullable(commonObject.getContentReference()).ifPresent(ref -> {
            builder.setContentRef(StringValue.of(ref));
        });

        return builder.build();
    }
}
