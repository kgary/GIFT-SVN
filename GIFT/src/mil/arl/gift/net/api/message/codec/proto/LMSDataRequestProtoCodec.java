/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.LMSDataRequestProto;
import generated.proto.common.PublishLessonScoreResponseProto;
import mil.arl.gift.common.InstantiateLearnerRequest;
import mil.arl.gift.common.LMSDataRequest;
import mil.arl.gift.common.PublishLessonScoreResponse;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf LMSDataRequest.
 * 
 * @author cpolynice
 *
 */
public class LMSDataRequestProtoCodec implements ProtoCodec<LMSDataRequestProto.LMSDataRequest, LMSDataRequest> {

    /* Codec that will be used to convert to/from a
     * PublishLessonScoreResponse. */
    private static PublishLessonScoreResponseProtoCodec codec = new PublishLessonScoreResponseProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<PublishLessonScoreResponse> convertPublishedResponses(
            List<PublishLessonScoreResponseProto.PublishLessonScoreResponse> protoList) {
        if (protoList == null) {
            return null;
        }

        List<PublishLessonScoreResponse> commonList = new ArrayList<>();

        for (PublishLessonScoreResponseProto.PublishLessonScoreResponse score : protoList) {
            commonList.add(codec.convert(score));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list.
     */
    private static List<PublishLessonScoreResponseProto.PublishLessonScoreResponse> mapPublishedResponses(
            List<PublishLessonScoreResponse> commonList) {
        if (commonList == null) {
            return null;
        }

        List<PublishLessonScoreResponseProto.PublishLessonScoreResponse> protoList = new ArrayList<>();

        for (PublishLessonScoreResponse score : commonList) {
            protoList.add(codec.map(score));
        }

        return protoList;
    }

    @Override
    public LMSDataRequest convert(LMSDataRequestProto.LMSDataRequest protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        String userName = protoObject.hasUserName() ? protoObject.getUserName().getValue() : null;
        List<PublishLessonScoreResponse> publishedScores = CollectionUtils
                .isNotEmpty(protoObject.getPublishedResponsesList())
                        ? convertPublishedResponses(protoObject.getPublishedResponsesList())
                        : null;
        int pageStart = protoObject.hasPageStart() ? protoObject.getPageStart().getValue() : 0;
        int pageSize = protoObject.hasPageSize() ? protoObject.getPageSize().getValue() : 0;
        boolean sortDescending = protoObject.hasSortDescending() ? protoObject.getSortDescending().getValue() : true;
        boolean learnerRequest = protoObject.hasLearnerRequest() ? protoObject.getLearnerRequest().getValue() : false;
        
        LMSDataRequest request = new LMSDataRequest(userName);
        request.setPageStart(pageStart);
        request.setPageSize(pageSize);
        request.setShouldSortDescending(sortDescending);
        request.setPublishedScores(publishedScores);
        
        if (CollectionUtils.isNotEmpty(protoObject.getDomainNamesList())) {
            request.addDomainIds(new HashSet<>(protoObject.getDomainNamesList()));
        }
        
        if (protoObject.hasCourseConcepts()) {
            /* new May 2021 */
            String xmlConcepts = protoObject.getCourseConcepts().getValue();
            UnmarshalledFile uFile;
            try {
                uFile = AbstractSchemaHandler.getFromXMLString(xmlConcepts, generated.course.Concepts.class,
                        AbstractSchemaHandler.COURSE_SCHEMA_FILE, false);
                Serializable decodedObj = uFile.getUnmarshalled();
                request.setCourseConcepts((generated.course.Concepts) decodedObj);
            } catch (Exception e) {
                throw new MessageDecodeException(this.getClass().getName(),
                        "Exception logged while decoding instantiate learner request object", e);
            }
        }

        request.setLearnerRequest(learnerRequest);
        return request;
    }

    @Override
    public LMSDataRequestProto.LMSDataRequest map(LMSDataRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        LMSDataRequestProto.LMSDataRequest.Builder builder = LMSDataRequestProto.LMSDataRequest.newBuilder();

        builder.setPageSize(Int32Value.of(commonObject.getPageSize()));
        builder.setPageStart(Int32Value.of(commonObject.getPageStart()));
        builder.setSortDescending(BoolValue.of(commonObject.getSortDescending()));
        builder.setLearnerRequest(BoolValue.of(commonObject.isLearnerRequest()));
        Optional.ofNullable(mapPublishedResponses(commonObject.getPublishedScores()))
                .ifPresent(builder::addAllPublishedResponses);
        Optional.ofNullable(commonObject.getDomainIds()).ifPresent(builder::addAllDomainNames);
        Optional.ofNullable(commonObject.getUserName()).ifPresent(username -> {
            builder.setUserName(StringValue.of(username));
        });

        if (commonObject.getCourseConcepts() != null) {
            try {
                Optional.ofNullable(AbstractSchemaHandler.getAsXMLString(commonObject.getCourseConcepts(),
                        generated.course.Concepts.class, AbstractSchemaHandler.COURSE_SCHEMA_FILE))
                        .ifPresent(concepts -> {
                            builder.setCourseConcepts(StringValue.of(concepts));
                        });
            } catch (Exception e) {
                throw new MessageEncodeException(InstantiateLearnerRequest.class.getName(),
                        "Failed to encode the string representation of the course concepts", e);
            }
        }

        return builder.build();
    }
}
