/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.io.Serializable;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.course.ImageProperties;
import generated.course.LtiProperties;
import generated.course.Media;
import generated.course.PDFProperties;
import generated.course.SlideShowProperties;
import generated.course.VideoProperties;
import generated.course.WebpageProperties;
import generated.course.YoutubeVideoProperties;
import generated.proto.common.MediaItemProto;
import generated.proto.common.MediaItemProto.MediaItem;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf MediaItem
 * message.
 * 
 * @author cpolynice
 *
 */
public class MediaItemProtoCodec implements ProtoCodec<MediaItemProto.MediaItem, Media> {

    /* Codec that will be used to convert to/from a PDFProperties instance. */
    private static PDFPropertiesProtoCodec pdfCodec = new PDFPropertiesProtoCodec();

    /* Codec that will be used to convert to/from a WebpageProperties
     * instance. */
    private static WebpagePropertiesProtoCodec webCodec = new WebpagePropertiesProtoCodec();

    /* Codec that will be used to convert to/from a YoutubeVideoProperties
     * instance. */
    private static YoutubeVideoPropertiesProtoCodec yvpCodec = new YoutubeVideoPropertiesProtoCodec();

    /* Codec that will be used to convert to/from a SlideShowProperties
     * instance. */
    private static SlideShowPropertiesProtoCodec sspCodec = new SlideShowPropertiesProtoCodec();

    /* Codec that will be used to convert to/from a VideoProperties instance. */
    private static VideoPropertiesProtoCodec videoCodec = new VideoPropertiesProtoCodec();

    /* Codec that will be used to convert to/from a ImageProperties instance. */
    private static ImagePropertiesProtoCodec imageCodec = new ImagePropertiesProtoCodec();

    /* Codec that will be used to convert to/from a LtiProperties instance. */
    private static LtiPropertiesProtoCodec ltiCodec = new LtiPropertiesProtoCodec();

    @Override
    public Media convert(MediaItem protoObject) {
        if (protoObject == null) {
            return null;
        }

        String uri = protoObject.hasUri() ? protoObject.getUri().getValue() : null;
        String name = protoObject.hasName() ? protoObject.getName().getValue() : null;
        Serializable properties;

        if (uri == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The media uri is null");
        }

        if (name == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The media item name is null");
        }

        if (protoObject.hasPdfProperties()) {
            properties = pdfCodec.convert(protoObject.getPdfProperties());
        } else if (protoObject.hasWebpageProperties()) {
            properties = webCodec.convert(protoObject.getWebpageProperties());
        } else if (protoObject.hasYoutubeVideoProperties()) {
            properties = yvpCodec.convert(protoObject.getYoutubeVideoProperties());
        } else if (protoObject.hasSlideShowProperties()) {
            properties = sspCodec.convert(protoObject.getSlideShowProperties());
        } else if (protoObject.hasVideoProperties()) {
            properties = videoCodec.convert(protoObject.getVideoProperties());
        } else if (protoObject.hasImageProperties()) {
            properties = imageCodec.convert(protoObject.getImageProperties());
        } else if (protoObject.hasLtiProperties()) {
            properties = ltiCodec.convert(protoObject.getLtiProperties());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "The property item  is null");
        }

        Media media = new Media();
        media.setName(name);
        media.setUri(uri);
        media.setMediaTypeProperties(properties);

        return media;

    }

    @Override
    public MediaItem map(Media commonObject) {
        if (commonObject == null) {
            return null;
        }

        MediaItemProto.MediaItem.Builder builder = MediaItemProto.MediaItem.newBuilder();

        Optional.ofNullable(commonObject.getUri()).ifPresent(uri -> {
            builder.setUri(StringValue.of(uri));
        });

        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setName(StringValue.of(name));
        });

        Object properties = commonObject.getMediaTypeProperties();

        if (properties instanceof PDFProperties) {
            builder.setPdfProperties(pdfCodec.map((PDFProperties) properties));
        } else if (properties instanceof WebpageProperties) {
            builder.setWebpageProperties(webCodec.map((WebpageProperties) properties));
        } else if (properties instanceof YoutubeVideoProperties) {
            builder.setYoutubeVideoProperties(yvpCodec.map((YoutubeVideoProperties) properties));
        } else if (properties instanceof VideoProperties) {
            builder.setVideoProperties(videoCodec.map((VideoProperties) properties));
        } else if (properties instanceof ImageProperties) {
            builder.setImageProperties(imageCodec.map((ImageProperties) properties));
        } else if (properties instanceof SlideShowProperties) {
            builder.setSlideShowProperties(sspCodec.map((SlideShowProperties) properties));
        } else if (properties instanceof LtiProperties) {
            builder.setLtiProperties(ltiCodec.map((LtiProperties) properties));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled media item property of " + properties);
        }

        return builder.build();
    }

}
