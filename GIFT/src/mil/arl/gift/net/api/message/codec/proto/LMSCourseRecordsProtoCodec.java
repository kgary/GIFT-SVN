/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import generated.proto.common.AbstractEnumObjectProto.AbstractEnumObject;
import generated.proto.common.LMSCourseRecordProto;
import generated.proto.common.LMSCourseRecordsProto;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf LMSCourseRecords.
 *
 * @author cpolynice
 *
 */
public class LMSCourseRecordsProtoCodec
        implements ProtoCodec<LMSCourseRecordsProto.LMSCourseRecords, LMSCourseRecords> {

    /* Codec that will be used to convert to/from a protobuf abstract enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /* Codec that will be used to convert to/from a LMSCourseRecord. */
    private static LMSCourseRecordProtoCodec courseCodec = new LMSCourseRecordProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<LMSCourseRecord> convertCourseHistory(List<LMSCourseRecordProto.LMSCourseRecord> protoList) {
        if (protoList == null) {
            return null;
        }

        List<LMSCourseRecord> commonList = new ArrayList<>();

        for (LMSCourseRecordProto.LMSCourseRecord course : protoList) {
            commonList.add(courseCodec.convert(course));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list.
     */
    private static List<LMSCourseRecordProto.LMSCourseRecord> mapCourseHistory(List<LMSCourseRecord> commonList) {
        if (commonList == null) {
            return null;
        }

        List<LMSCourseRecordProto.LMSCourseRecord> protoList = new ArrayList<>();

        for (LMSCourseRecord course : commonList) {
            protoList.add(courseCodec.map(course));
        }

        return protoList;
    }

    /**
     * Maps the given common object map to the protobuf map representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map representation.
     */
    private static Map<String, AbstractEnumObject> mapAssessments(Map<String, AssessmentLevelEnum> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, AbstractEnumObject> protoMap = new HashMap<>();

        for (Map.Entry<String, AssessmentLevelEnum> assessment : commonMap.entrySet()) {
            String key = assessment.getKey();
            AbstractEnumObject value = enumCodec.map(assessment.getValue());

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public LMSCourseRecords convert(LMSCourseRecordsProto.LMSCourseRecords protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<LMSCourseRecord> history = CollectionUtils.isNotEmpty(protoObject.getCourseHistoryList())
                ? convertCourseHistory(protoObject.getCourseHistoryList())
                : new ArrayList<>(0);

        LMSCourseRecords returnObj = new LMSCourseRecords(history);

        if (CollectionUtils.isNotEmpty(protoObject.getAssessmentsMap())) {
            for (Map.Entry<String, AbstractEnumObject> assessment : protoObject.getAssessmentsMap().entrySet()) {
                String name = assessment.getKey();
                AssessmentLevelEnum assessmentLevel = (AssessmentLevelEnum) enumCodec.convert(assessment.getValue());

                if (assessmentLevel != null) {
                    returnObj.addAssessment(name, assessmentLevel);
                }
            }
        }

        return returnObj;
    }

    @Override
    public LMSCourseRecordsProto.LMSCourseRecords map(LMSCourseRecords commonObject) {
        if (commonObject == null) {
            return null;
        }

        LMSCourseRecordsProto.LMSCourseRecords.Builder builder = LMSCourseRecordsProto.LMSCourseRecords.newBuilder();

        Optional.ofNullable(mapCourseHistory(commonObject.getRecords())).ifPresent(builder::addAllCourseHistory);
        Optional.ofNullable(mapAssessments(commonObject.getAssessments())).ifPresent(builder::putAllAssessments);

        return builder.build();
    }

}
