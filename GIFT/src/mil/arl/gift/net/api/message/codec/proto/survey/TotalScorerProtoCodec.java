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
import generated.proto.common.survey.TotalScorerProto;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.TotalScorer;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a Total Scorer
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class TotalScorerProtoCodec implements ProtoCodec<TotalScorerProto.TotalScorer, TotalScorer> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(TotalScorerProto.TotalScorer.class);

    /**
     * Codec to convert to/from protobuf Attribute Scorer Properties instance.
     */
    private static AttributeScorerPropertiesProtoCodec codec = new AttributeScorerPropertiesProtoCodec();

    @Override
    public TotalScorer convert(TotalScorerProto.TotalScorer protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<AttributeScorerProperties> attributeProperties = new ArrayList<AttributeScorerProperties>();

        try {
            if (CollectionUtils.isNotEmpty(protoObject.getAttributePropertiesList())) {
                for (AttributeScorerPropertiesProto.AttributeScorerProperties attributeScorerProperties : protoObject
                        .getAttributePropertiesList()) {
                    attributeProperties.add(codec.convert(attributeScorerProperties));
                }
            }
        } catch (Exception e) {
            logger.error("Caught exception while creating a total scorer from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }

        return new TotalScorer(attributeProperties);
    }

    @Override
    public TotalScorerProto.TotalScorer map(TotalScorer commonObject) {
        if (commonObject == null) {
            return null;
        }

        TotalScorerProto.TotalScorer.Builder builder = TotalScorerProto.TotalScorer.newBuilder();

        if (commonObject.getAttributeScorers() != null) {
            for (AttributeScorerProperties totalScorers : commonObject.getAttributeScorers()) {
                Optional.ofNullable(codec.map(totalScorers)).ifPresent(builder::addAttributeProperties);
            }
        }

        return builder.build();
    }

}
