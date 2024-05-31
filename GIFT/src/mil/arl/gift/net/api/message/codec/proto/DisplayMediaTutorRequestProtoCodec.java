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
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.course.Media;
import generated.proto.common.AbstractDisplayContentTutorRequestProto;
import generated.proto.common.MediaItemProto.MediaItem;
import mil.arl.gift.common.DisplayMediaTutorRequest;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DisplayMediaTutorRequest message.
 * 
 * @author cpolynice
 *
 */
public class DisplayMediaTutorRequestProtoCodec implements
        ProtoCodec<AbstractDisplayContentTutorRequestProto.DisplayMediaTutorRequest, DisplayMediaTutorRequest> {

    /* Codec that will be used to convert to/from a MediaItem. */
    private static MediaItemProtoCodec codec = new MediaItemProtoCodec();

    /**
     * Converts the given protobuf media item list to the common object
     * representation.
     * 
     * @param protoList the protobuf media item list
     * @return the media item list
     */
    private static List<Media> convertList(List<MediaItem> protoList) {
        if (protoList == null) {
            return null;
        }

        List<Media> commonList = new ArrayList<>();

        for (MediaItem mi : protoList) {
            commonList.add(codec.convert(mi));
        }

        return commonList;
    }

    /**
     * Converts the given media item list to the protobuf representation.
     * 
     * @param commonList the media item list
     * @return the protobuf media item list
     */
    private static List<MediaItem> mapList(List<Media> commonList) {
        if (commonList == null) {
            return null;
        }

        List<MediaItem> protoList = new ArrayList<>();

        for (Media mi : commonList) {
            protoList.add(codec.map(mi));
        }

        return protoList;
    }

    @Override
    public DisplayMediaTutorRequest convert(
            AbstractDisplayContentTutorRequestProto.DisplayMediaTutorRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (CollectionUtils.isNotEmpty(protoObject.getMediaListList())) {
            return new DisplayMediaTutorRequest(convertList(protoObject.getMediaListList()));
        } else {
            DisplayMediaTutorRequest displayMediaTutorRequest = new DisplayMediaTutorRequest();
            try {
                if (protoObject.hasGuidance()) {
                    String xmlStr = protoObject.getGuidance().getValue();
                    UnmarshalledFile uFile = AbstractSchemaHandler.getFromXMLString(xmlStr,
                            generated.course.Guidance.class, null, true);
                    generated.course.Guidance guidance = (generated.course.Guidance) uFile.getUnmarshalled();
                    displayMediaTutorRequest.setGuidance(guidance);

                } else if (protoObject.hasMedia()) {
                    String xmlStr = protoObject.getMedia().getValue();
                    UnmarshalledFile uFile = AbstractSchemaHandler.getFromXMLString(xmlStr,
                            generated.course.Media.class, null, true);
                    generated.course.Media media = (generated.course.Media) uFile.getUnmarshalled();
                    displayMediaTutorRequest.setMedia(media);
                }
            } catch (Exception e) {
                throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
            }

            if (protoObject.hasNewWindow()) {
                displayMediaTutorRequest.setShouldOpenInNewWindow(protoObject.getNewWindow().getValue());
            }

            if (protoObject.hasDisplayDuration()) {
                displayMediaTutorRequest.setDisplayDuration(protoObject.getDisplayDuration().getValue());
            }

            if (protoObject.hasWhileTaLoads()) {
                displayMediaTutorRequest.setWhileTrainingAppLoads(protoObject.getWhileTaLoads().getValue());
            }

            return displayMediaTutorRequest;
        }
    }

    @Override
    public AbstractDisplayContentTutorRequestProto.DisplayMediaTutorRequest map(
            DisplayMediaTutorRequest commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        AbstractDisplayContentTutorRequestProto.DisplayMediaTutorRequest.Builder builder = AbstractDisplayContentTutorRequestProto.DisplayMediaTutorRequest
                .newBuilder();
        
        Optional.ofNullable(mapList(commonObject.getMediaList())).ifPresent(builder::addAllMediaList);

        Optional.ofNullable(commonObject.getGuidance()).ifPresent(guidance -> {
            try {
                builder.setGuidance(StringValue.of(AbstractSchemaHandler.getAsXMLString(guidance, generated.course.Guidance.class, AbstractSchemaHandler.COURSE_SCHEMA_FILE)));
            } catch (SAXException | JAXBException e) {
                throw new MessageEncodeException(this.getClass().getName(), e.getMessage());
            }
        });

        Optional.ofNullable(commonObject.getMedia()).ifPresent(media -> {
            try {
                builder.setMedia(StringValue.of(AbstractSchemaHandler.getAsXMLString(media, generated.course.Guidance.class, AbstractSchemaHandler.COURSE_SCHEMA_FILE)));
            } catch (SAXException | JAXBException e) {
                throw new MessageEncodeException(this.getClass().getName(), e.getMessage());
            }
        });

        builder.setDisplayDuration(Int32Value.of(commonObject.getDisplayDuration()));
        builder.setWhileTaLoads(BoolValue.of(commonObject.isWhileTrainingAppLoads()));
        builder.setNewWindow(BoolValue.of(commonObject.shouldOpenInNewWindow()));

        return builder.build();
    }

}
