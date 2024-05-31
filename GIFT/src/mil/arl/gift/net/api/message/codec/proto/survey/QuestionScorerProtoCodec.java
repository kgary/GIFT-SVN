/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BoolValue;

import generated.proto.common.survey.AttributeScorerPropertiesProto;
import generated.proto.common.survey.QuestionScorerProto;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.QuestionScorer;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a Question Scorer
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class QuestionScorerProtoCodec implements ProtoCodec<QuestionScorerProto.QuestionScorer, QuestionScorer> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(QuestionScorerProto.QuestionScorer.class);

    /**
     * Codec that is used to convert to/from a protobuf Attribute Scorer
     * Properties instance.
     */
    private static AttributeScorerPropertiesProtoCodec codec = new AttributeScorerPropertiesProtoCodec();

    @Override
    public QuestionScorer convert(QuestionScorerProto.QuestionScorer protoObject) {
        if (protoObject == null) {
            return null;
        }

        boolean totalQuestion;
        Set<AttributeScorerProperties> attributeScorers = new HashSet<>();

        try {
            totalQuestion = protoObject.hasTotalQuestion() ? protoObject.getTotalQuestion().getValue() : false;
            if (CollectionUtils.isNotEmpty(protoObject.getAttributeScorersList())) {
                for (AttributeScorerPropertiesProto.AttributeScorerProperties attributeScorerProperties : protoObject
                        .getAttributeScorersList()) {
                    attributeScorers.add(codec.convert(attributeScorerProperties));
                }
            }
        } catch (Exception e) {
            logger.error("Caught exception while creating a option list from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }

        return new QuestionScorer(totalQuestion, attributeScorers);
    }

    @Override
    public QuestionScorerProto.QuestionScorer map(QuestionScorer commonObject) {
        if (commonObject == null) {
            return null;
        }

        QuestionScorerProto.QuestionScorer.Builder builder = QuestionScorerProto.QuestionScorer.newBuilder();
        builder.setTotalQuestion(BoolValue.of(commonObject.getTotalQuestion()));

        if (commonObject.getAttributeScorers() != null) {
            for (AttributeScorerProperties attributeScorers : commonObject.getAttributeScorers()) {
                Optional.ofNullable(codec.map(attributeScorers)).ifPresent(builder::addAttributeScorers);
            }
        }

        return builder.build();
    }

}
