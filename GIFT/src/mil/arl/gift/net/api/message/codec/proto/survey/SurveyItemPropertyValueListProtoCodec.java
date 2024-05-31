/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import generated.proto.common.survey.SurveyItemPropertyValueProto.SurveyItemPropertyValue;
import generated.proto.common.survey.SurveyItemPropertyValueProto.SurveyItemPropertyValueList;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * SurveyItemPropertyValueList.
 * 
 * @author cpolynice
 *
 */
public class SurveyItemPropertyValueListProtoCodec
        implements ProtoCodec<SurveyItemPropertyValueList, List<Serializable>> {

    /* Codec that will be used to convert to/from individual
     * SurveyItemPropertyValues. */
    private static SurveyItemPropertyValueProtoCodec codec = new SurveyItemPropertyValueProtoCodec();

    @Override
    public List<Serializable> convert(SurveyItemPropertyValueList protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            List<Serializable> propertyValuesList = new ArrayList<>();

            if (CollectionUtils.isNotEmpty(protoObject.getPropertyValueListList())) {
                for (SurveyItemPropertyValue propVal : protoObject.getPropertyValueListList()) {
                    propertyValuesList.add(codec.convert(propVal));
                }
            }

            return propertyValuesList;
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }
    }

    @Override
    public SurveyItemPropertyValueList map(List<Serializable> commonObject) {
        if (commonObject == null) {
            throw new NullPointerException("The list of question property values is null");
        }

        SurveyItemPropertyValueList.Builder builder = SurveyItemPropertyValueList.newBuilder();

        try {
            for (Serializable propVal : commonObject) {
                Optional.ofNullable(codec.map(propVal)).ifPresent(builder::addPropertyValueList);
            }

            return builder.build();
        } catch (Exception e) {
            throw new MessageEncodeException(this.getClass().getName(),
                    "There was a problem encoding the list of question property values", e);
        }
    }
}
