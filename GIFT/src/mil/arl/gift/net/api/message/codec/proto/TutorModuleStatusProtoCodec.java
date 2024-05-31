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

import generated.proto.common.TutorModuleStatusProto;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.TutorModuleStatus;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a TutorModuleStatus class.
 * 
 * @author oamer
 */
public class TutorModuleStatusProtoCodec implements ProtoCodec<TutorModuleStatusProto.TutorModuleStatus, TutorModuleStatus>{
    ModuleStatusProtoCodec moduleStatusProtoCodec = new ModuleStatusProtoCodec();

    @Override
    public TutorModuleStatus convert(generated.proto.common.TutorModuleStatusProto.TutorModuleStatus protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        String topicName = protoObject.hasTopicName() ? protoObject.getTopicName().getValue() : null;
        
        ModuleStatus moduleStatus = protoObject.hasModuleStatus() ? moduleStatusProtoCodec.convert(protoObject.getModuleStatus()) : null;
        
        return new TutorModuleStatus(topicName, moduleStatus);
    }

    @Override
    public generated.proto.common.TutorModuleStatusProto.TutorModuleStatus map(TutorModuleStatus commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        TutorModuleStatusProto.TutorModuleStatus.Builder builder = generated.proto.common.TutorModuleStatusProto.TutorModuleStatus.newBuilder();
        
        Optional.ofNullable(moduleStatusProtoCodec.map(commonObject)).ifPresent(builder::setModuleStatus);
        Optional.ofNullable(commonObject.getTopicName()).ifPresent(topicName -> {
            builder.setTopicName(StringValue.of(topicName));
        });
        
        return builder.build();
    }

}
