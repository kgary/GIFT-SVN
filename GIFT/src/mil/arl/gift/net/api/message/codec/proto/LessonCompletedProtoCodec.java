/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.LessonCompletedProto;
import generated.proto.common.LessonCompletedProto.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * Used to encode/decode a lesson completed payload using Protobuf definition.
 * @author mhoffman
 *
 */
public class LessonCompletedProtoCodec implements ProtoCodec<LessonCompletedProto.LessonCompleted, LessonCompleted> {

    @Override
    public LessonCompleted convert(LessonCompletedProto.LessonCompleted protoObject) {
        if(protoObject == null){
            return null;
        }
        
        LessonCompletedStatusType statusProto = protoObject.getStatus();
        mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType status = 
                mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType.valueOf(statusProto.name());
        
        return new LessonCompleted(status);
    }

    @Override
    public LessonCompletedProto.LessonCompleted map(LessonCompleted commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        LessonCompletedProto.LessonCompleted.Builder builder = LessonCompletedProto.LessonCompleted.newBuilder();
       
        LessonCompletedStatusType statusProto = LessonCompletedStatusType.valueOf(commonObject.getStatusType().name());
        builder.setStatus(statusProto);
        return builder.build();
    }

}
