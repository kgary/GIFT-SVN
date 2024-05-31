/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.proto.common.survey.AttributeScorerPropertiesProto;
import generated.proto.common.survey.SurveyScorerProto;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.SurveyScorer;
import mil.arl.gift.common.survey.score.TotalScorer;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;


/** 
 *  This class is responsible for protobuf encoding/decoding a Survey Scorer
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class SurveyScorerProtoCodec implements ProtoCodec<SurveyScorerProto.SurveyScorer, SurveyScorer> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(SurveyScorerProto.SurveyScorer.class);

    /** Codecs that will be used to convert to/from protobuf representations. */
    private static TotalScorerProtoCodec totalScorerCodec = new TotalScorerProtoCodec();
    private static AttributeScorerPropertiesProtoCodec attributeScorerProtoCodec = new AttributeScorerPropertiesProtoCodec();

    @Override
    public SurveyScorer convert(SurveyScorerProto.SurveyScorer protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<AttributeScorerProperties> attributeScorers = new ArrayList<AttributeScorerProperties>();
        TotalScorer totalScorer;

        try {
            totalScorer = protoObject.hasTotalScorer() ? totalScorerCodec.convert(protoObject.getTotalScorer())
                    : null;

            if (CollectionUtils.isNotEmpty(protoObject.getAttributeScorersList())) {
                for (AttributeScorerPropertiesProto.AttributeScorerProperties attributeScorerProperties : protoObject
                        .getAttributeScorersList()) {
                    attributeScorers.add(attributeScorerProtoCodec.convert(attributeScorerProperties));
                }
            }
        } catch (Exception e) {
            logger.error("Caught exception while creating a survey scorer from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }

        return new SurveyScorer(totalScorer, attributeScorers);
    }

    @Override
    public SurveyScorerProto.SurveyScorer map(SurveyScorer commonObject) {
        if (commonObject == null) {
            return null;
        }

        SurveyScorerProto.SurveyScorer.Builder builder = SurveyScorerProto.SurveyScorer.newBuilder();
        Optional.ofNullable(totalScorerCodec.map(commonObject.getTotalScorer())).ifPresent(builder::setTotalScorer);


        if (commonObject.getAttributeScorers() != null) {
            for (AttributeScorerProperties attributeScorers : commonObject.getAttributeScorers()) {
                Optional.ofNullable(attributeScorerProtoCodec.map(attributeScorers))
                        .ifPresent(builder::addAttributeScorers);
            }
        }

        return builder.build();
    }

}
