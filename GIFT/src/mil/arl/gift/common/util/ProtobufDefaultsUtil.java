/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.AbstractMessage;

import generated.proto.common.ProtobufMessageProto.ProtobufMessage;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.net.api.message.codec.proto.LessonCompletedProtoCodec;
import mil.arl.gift.net.api.message.codec.proto.VibrateDeviceProtoCodec;

/**
 * This class is responsible for populating new/required fields in protobuf
 * messages with default values.
 * 
 * 
 * @author cpolynice
 *
 */
public class ProtobufDefaultsUtil {

    /**
     * Creates and returns a new instance of a protobuf message that has been updated to the 
     * correct legacy type. This method is used assuming the caller has already detected that
     * they are handling an older message type that has not been configured with current logic.
     * 
     * @param type the message type that needs to be updated 
     * @param payload the original legacy payload of the message, in case data needs to be extracted
     *        for conversion
     * @return the updated protobuf message, or null if the message type has not been handled
     */
    @SuppressWarnings("unchecked")
    public static AbstractMessage updateLegacyMessage(MessageTypeEnum type, Object payload) {
        if (type == null) {
            return null;
        }
        
        AbstractMessage message = null;
        
        if (MessageTypeEnum.LESSON_COMPLETED.equals(type)) {
            message = new LessonCompletedProtoCodec().map(new LessonCompleted(LessonCompletedStatusType.LEGACY_NOT_SPECIFIED));
        }  
        
        if (MessageTypeEnum.VIBRATE_DEVICE_REQUEST.equals(type)) {
            List<Integer> payloadList = new ArrayList<>();
            
            for (Long num : (List<Long>) payload) {
                payloadList.add(num.intValue());
            }
            
            message = new VibrateDeviceProtoCodec().map(payloadList);
        }
        
        return message;
    }
    
    /**
     * Checks if the given protobuf message is missing required fields, and
     * populates them as necessary.
     * 
     * @param protobufMessage the protobuf message
     * @return true if the protobuf message contains all required fields or was able to be repaired; false
     *         if it is missing required fields and is irreparable.
     */
    public static boolean checkRequiredFields(ProtobufMessage protobufMessage) {
        if (protobufMessage == null) {
            return false;
        }

        /* Should never have an unknown message type (protobuf default when
         * never set) */
        if (!protobufMessage.hasMessageType()) {
            return false;
        }

        // ProtobufMessage.Builder builder = protobufMessage.toBuilder();

        /* When new required fields get added, a default must be set here in
         * case an older message is parsed and doesn't have this new field. */

        /* Message is 'valid' */
        return true;
    }
}
