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

import generated.proto.common.PlayAudioActionProto;
import mil.arl.gift.common.PlayAudioAction;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf PlayAudioAction.
 * 
 * @author cpolynice
 *
 */
public class PlayAudioActionProtoCodec implements ProtoCodec<PlayAudioActionProto.PlayAudioAction, PlayAudioAction> {

    @Override
    public PlayAudioAction convert(PlayAudioActionProto.PlayAudioAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        String mp3File = protoObject.hasMp3File() ? protoObject.getMp3File().getValue() : null;
        String oggFile = protoObject.hasOggFile() ? protoObject.getOggFile().getValue() : null;

        return new PlayAudioAction(mp3File, oggFile);
    }

    @Override
    public PlayAudioActionProto.PlayAudioAction map(PlayAudioAction commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        PlayAudioActionProto.PlayAudioAction.Builder builder = PlayAudioActionProto.PlayAudioAction.newBuilder();
        
        Optional.ofNullable(commonObject.getMp3AudioFile()).ifPresent(mp3 -> {
            builder.setMp3File(StringValue.of(mp3));
        });
        Optional.ofNullable(commonObject.getOggAudioFile()).ifPresent(ogg -> {
            builder.setOggFile(StringValue.of(ogg));
        });

        return builder.build();
    }
}
