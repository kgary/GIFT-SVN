/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.SurveyCheckRequestProto;
import generated.proto.common.SurveyCheckRequestProto.QuestionCheck;
import generated.proto.common.SurveyCheckRequestProto.ReplyCheck;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest;
import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.SurveyCheckRequest.Question;
import mil.arl.gift.common.SurveyCheckRequest.Reply;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * SurveyCheckRequest.
 * 
 * @author cpolynice
 *
 */
public class SurveyCheckRequestProtoCodec
        implements ProtoCodec<SurveyCheckRequestProto.SurveyCheckRequest, SurveyCheckRequest> {

    /* Codec that will be used to convert to/from a GetSurveyRequest. */
    private static final GetSurveyRequestProtoCodec codec = new GetSurveyRequestProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<Reply> convertReplies(List<ReplyCheck> protoList) {
        if (protoList == null) {
            return null;
        }

        List<Reply> commonList = new ArrayList<>();

        for (ReplyCheck reply : protoList) {
            if (reply.hasReplyId()) {
                commonList.add(new Reply(reply.getReplyId().getValue()));
            }
        }

        return commonList;
    }

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<Question> convertQuestions(List<QuestionCheck> protoList) {
        if (protoList == null) {
            return null;
        }

        List<Question> commonList = new ArrayList<>();

        for (QuestionCheck question : protoList) {
            if (question.hasQuestionId()) {
                int questionId = question.getQuestionId().getValue();
                List<Reply> replies = CollectionUtils.isNotEmpty(question.getRepliesList())
                        ? convertReplies(question.getRepliesList())
                        : null;

                if (replies != null) {
                    Question q = new Question(questionId);
                    q.addReplies(replies);
                    commonList.add(q);
                }
            }
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list.
     */
    private static List<ReplyCheck> mapReplies(List<Reply> commonList) {
        if (commonList == null) {
            return null;
        }

        List<ReplyCheck> protoList = new ArrayList<>();

        for (Reply reply : commonList) {
            protoList.add(ReplyCheck.newBuilder().setReplyId(Int32Value.of(reply.getReplyId())).build());
        }

        return protoList;
    }

    /**
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list.
     */
    private static List<QuestionCheck> mapQuestions(List<Question> commonList) {
        if (commonList == null) {
            return null;
        }

        List<QuestionCheck> protoList = new ArrayList<>();

        for (Question question : commonList) {
            QuestionCheck.Builder qBuilder = QuestionCheck.newBuilder();
            qBuilder.setQuestionId(Int32Value.of(question.getQuestionId()));
            Optional.ofNullable(mapReplies(question.getReplies())).ifPresent(qBuilder::addAllReplies);
            protoList.add(qBuilder.build());
        }

        return protoList;
    }

    @Override
    public SurveyCheckRequest convert(SurveyCheckRequestProto.SurveyCheckRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        int surveyContextId = protoObject.hasSurveyContextId() ? protoObject.getSurveyContextId().getValue() : 0;
        String giftKey = protoObject.hasGiftKey() ? protoObject.getGiftKey().getValue() : null;
        Integer courseObjectIndex = protoObject.hasCourseObjectIndex() ? protoObject.getCourseObjectIndex().getValue()
                : null;
        List<Question> questions = CollectionUtils.isNotEmpty(protoObject.getQuestionsList())
                ? convertQuestions(protoObject.getQuestionsList())
                : new ArrayList<>();

        SurveyCheckRequest surveyCheckRequest;
        if (giftKey == null) {
            surveyCheckRequest = new SurveyCheckRequest(surveyContextId, courseObjectIndex);
        } else {
            surveyCheckRequest = new SurveyCheckRequest(surveyContextId, courseObjectIndex, giftKey);
            surveyCheckRequest
                    .addQuestions(questions); /* Only add questions if the gift
                                               * key is specified. */
        }

        if (protoObject.hasRecallRequest()) {
            GetKnowledgeAssessmentSurveyRequest recallRequest = (GetKnowledgeAssessmentSurveyRequest) codec
                    .convert(protoObject.getRecallRequest());
            surveyCheckRequest.setGetKnowledgeAssessmentSurveyRequest(recallRequest);
        }

        return surveyCheckRequest;
    }

    @Override
    public SurveyCheckRequestProto.SurveyCheckRequest map(SurveyCheckRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        SurveyCheckRequestProto.SurveyCheckRequest.Builder builder = SurveyCheckRequestProto.SurveyCheckRequest
                .newBuilder();

        builder.setSurveyContextId(Int32Value.of(commonObject.getSurveyContextId()));
        Optional.ofNullable(mapQuestions(commonObject.getQuestions())).ifPresent(builder::addAllQuestions);
        Optional.ofNullable(codec.map(commonObject.getKnowledgeAssessmentSurveyRequest()))
                .ifPresent(builder::setRecallRequest);
        Optional.ofNullable(commonObject.getCourseObjectIndex()).ifPresent(index -> {
            builder.setCourseObjectIndex(Int32Value.of(index));
        });
        Optional.ofNullable(commonObject.getGiftKey()).ifPresent(key -> {
            builder.setGiftKey(StringValue.of(key));
        });

        return builder.build();
    }

}
