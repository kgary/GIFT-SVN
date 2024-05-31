/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Iterator;

import com.google.protobuf.Int32Value;
import com.google.protobuf.ProtocolStringList;

import generated.proto.common.CourseRecordRefProto;
import generated.proto.common.CourseRecordRefProto.IntCourseRecordRef;
import generated.proto.common.CourseRecordRefProto.UUIDCourseRecordRefs;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.CourseRecordRef.AbstractCourseRecordRefId;
import mil.arl.gift.common.course.CourseRecordRef.IntCourseRecordRefId;
import mil.arl.gift.common.course.CourseRecordRef.UUIDCourseRecordRefIds;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf CourseRecordRef.
 * @author mhoffman
 *
 */
public class CourseRecordRefProtoCodec implements ProtoCodec<CourseRecordRefProto.CourseRecordRef, CourseRecordRef> {

    @Override
    public CourseRecordRef convert(generated.proto.common.CourseRecordRefProto.CourseRecordRef protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        CourseRecordRef ref = new CourseRecordRef();
        
        if(protoObject.hasIntId()){
            IntCourseRecordRef intRef = protoObject.getIntId();
            int intId = intRef.getRecordId().getValue();
            IntCourseRecordRefId intRefId = new IntCourseRecordRefId(intId);
            ref.setRef(intRefId);
            
        }else if(protoObject.hasUuids()){
            
            UUIDCourseRecordRefIds uuidRefIds = new UUIDCourseRecordRefIds();
            
            UUIDCourseRecordRefs uuidRef = protoObject.getUuids();
            ProtocolStringList list = uuidRef.getRecordIdList();
            Iterator<String> itr = list.iterator();
            while(itr.hasNext()){
                
                String id = itr.next();
                uuidRefIds.addRecordUUID(id);
            }
            
            ref.setRef(uuidRefIds);
        }
        return ref;
    }

    @Override
    public generated.proto.common.CourseRecordRefProto.CourseRecordRef map(CourseRecordRef commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        CourseRecordRefProto.CourseRecordRef.Builder builder = CourseRecordRefProto.CourseRecordRef.newBuilder();
        
        AbstractCourseRecordRefId aRefId = commonObject.getRef();
        if(aRefId instanceof IntCourseRecordRefId){
            IntCourseRecordRefId intRefId = (IntCourseRecordRefId)aRefId;
            IntCourseRecordRef.Builder intBuilder = IntCourseRecordRef.newBuilder();
            Int32Value int32Value = Int32Value.of(intRefId.getRecordId());
            intBuilder.setRecordId(int32Value);
            builder.setIntId(intBuilder.build());
            
        }else if(aRefId instanceof UUIDCourseRecordRefIds){
            UUIDCourseRecordRefIds uuidRefId = (UUIDCourseRecordRefIds)aRefId;
            
            UUIDCourseRecordRefs.Builder uuidBuilder = UUIDCourseRecordRefs.newBuilder();
            if(uuidRefId.getRecordUUIDs() != null){
                for(String id : uuidRefId.getRecordUUIDs()){
                    uuidBuilder.addRecordId(id);
                }
            }

            builder.setUuids(uuidBuilder.build());
        }
        
        return builder.build();
    }

}
