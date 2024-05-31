/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashMap;
import java.util.Map;

import generated.proto.common.PublishLessonScoreResponseProto;
import generated.proto.common.PublishLessonScoreResponseProto.PublishedRecord;
import mil.arl.gift.common.LMSConnectionInfo;
import mil.arl.gift.common.PublishLessonScoreResponse;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * PublishLessonScoreResponse.
 * 
 * @author cpolynice
 *
 */
public class PublishLessonScoreResponseProtoCodec
        implements ProtoCodec<PublishLessonScoreResponseProto.PublishLessonScoreResponse, PublishLessonScoreResponse> {

    /* Codec that will be used to convert to/from a protobuf
     * LMSConnectionInfo. */
    private static LMSConnectionInfoProtoCodec lmsConnectionInfoCodec = new LMSConnectionInfoProtoCodec();
    
    /* Codec used to convert to/from a protobuf CourseRecordRef */
    private static CourseRecordRefProtoCodec courseRecordRefCodec = new CourseRecordRefProtoCodec();

    @Override
    public PublishLessonScoreResponse convert(PublishLessonScoreResponseProto.PublishLessonScoreResponse protoObject) {
        if (protoObject == null) {
            return null;
        }

        Map<LMSConnectionInfo, CourseRecordRef> publishedRecordsByLMS = new HashMap<>();

        if (CollectionUtils.isNotEmpty(protoObject.getRecordsList())) {
            for (PublishedRecord record : protoObject.getRecordsList()) {
                /* First, grab the key and value pair from each record entry.
                 * Because this is essentially an entry for a map, we can be
                 * sure that these entries will never be null. */
                LMSConnectionInfo key = lmsConnectionInfoCodec.convert(record.getKey());
                CourseRecordRef value = courseRecordRefCodec.convert(record.getValue());

                /* Check if the key was successfully decoded before adding to the
                 * map. */
                if (key != null) {
                    publishedRecordsByLMS.put(key, value);
                }
            }
        }

        return new PublishLessonScoreResponse(publishedRecordsByLMS);
    }

    @Override
    public PublishLessonScoreResponseProto.PublishLessonScoreResponse map(
            PublishLessonScoreResponse commonObject) {
        if (commonObject == null) {
            return null;
        }

        PublishLessonScoreResponseProto.PublishLessonScoreResponse.Builder builder = PublishLessonScoreResponseProto.PublishLessonScoreResponse
                .newBuilder();

        if (CollectionUtils.isNotEmpty(commonObject.getPublishedRecordsByLMS())) {
            /* To support the custom map, iterate through the map, wrap the
             * key-value pair inside a container, and add to the list of
             * records. */
            for (Map.Entry<LMSConnectionInfo, CourseRecordRef> record : commonObject.getPublishedRecordsByLMS().entrySet()) {
                PublishedRecord.Builder rec = PublishedRecord.newBuilder();
                rec.setKey(lmsConnectionInfoCodec.map(record.getKey()));
                rec.setValue(courseRecordRefCodec.map(record.getValue()));
                builder.addRecords(rec);
            }
        }

        return builder.build();
    }

}
