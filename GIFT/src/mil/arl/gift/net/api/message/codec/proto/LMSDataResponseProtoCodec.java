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
import generated.proto.common.AbstractScaleProto;
import generated.proto.common.LMSCourseRecordProto;
import generated.proto.common.LMSDataResponseProto.LMSDataResponse;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.LMSData;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf LMSDataResponse.
 * 
 * @author cpolynice
 *
 */
public class LMSDataResponseProtoCodec implements ProtoCodec<LMSDataResponse, LMSData> {

    /* Codec that will be used to convert to/from a protobuf abstract enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /* Codec that will be used to convert to/from a protobuf AbstractScale. */
    private static AbstractScaleProtoCodec scaleCodec = new AbstractScaleProtoCodec();

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
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<AbstractScale> convertLearnerStateAttributes(List<AbstractScaleProto.AbstractScale> protoList) {
        if (protoList == null) {
            return null;
        }

        List<AbstractScale> commonList = new ArrayList<>();

        for (AbstractScaleProto.AbstractScale scale : protoList) {
            commonList.add(scaleCodec.convert(scale));
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
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list.
     */
    private static List<AbstractScaleProto.AbstractScale> mapLearnerStateAttributes(List<AbstractScale> commonList) {
        if (commonList == null) {
            return null;
        }

        List<AbstractScaleProto.AbstractScale> protoList = new ArrayList<>();

        for (AbstractScale scale : commonList) {
            protoList.add(scaleCodec.map(scale));
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
    public LMSData convert(LMSDataResponse protoObject) {
        if (protoObject == null) {
            return null;
        }

        LMSData lmsData = new LMSData();

        List<LMSCourseRecord> history = CollectionUtils.isNotEmpty(protoObject.getCourseHistoryList())
                ? convertCourseHistory(protoObject.getCourseHistoryList())
                : new ArrayList<>(0);
        List<AbstractScale> scales = CollectionUtils.isNotEmpty(protoObject.getLearnerStateAttributesList())
                ? convertLearnerStateAttributes(protoObject.getLearnerStateAttributesList())
                : new ArrayList<>(0);

        LMSCourseRecords courseRecords = new LMSCourseRecords(history);

        if (CollectionUtils.isNotEmpty(protoObject.getAssessmentsMap())) {
            for (Map.Entry<String, AbstractEnumObject> assessment : protoObject.getAssessmentsMap().entrySet()) {
                String name = assessment.getKey();
                AssessmentLevelEnum assessmentLevel = (AssessmentLevelEnum) enumCodec.convert(assessment.getValue());

                if (assessmentLevel != null) {
                    courseRecords.addAssessment(name, assessmentLevel);
                }
            }
        }

        lmsData.setCourseRecords(courseRecords);
        lmsData.setAbstractScales(scales);
        return lmsData;
    }

    @Override
    public LMSDataResponse map(LMSData commonObject) {
        if (commonObject == null) {
            return null;
        }

        LMSDataResponse.Builder builder = LMSDataResponse.newBuilder();
        LMSCourseRecords lmsCourseRecords = commonObject.getCourseRecords();
        List<AbstractScale> scales = commonObject.getAbstractScales();

        if (lmsCourseRecords != null) {
            Optional.ofNullable(mapCourseHistory(lmsCourseRecords.getRecords()))
                    .ifPresent(builder::addAllCourseHistory);
            Optional.ofNullable(mapAssessments(lmsCourseRecords.getAssessments()))
                    .ifPresent(builder::putAllAssessments);
        }

        if (scales != null) {
            builder.addAllLearnerStateAttributes(mapLearnerStateAttributes(scales));
        }

        return builder.build();
    }

}
