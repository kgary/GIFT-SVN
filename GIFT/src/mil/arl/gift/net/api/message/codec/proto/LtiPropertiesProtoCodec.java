/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.course.BooleanEnum;
import generated.course.CustomParameters;
import generated.course.DisplayModeEnum;
import generated.course.LtiConcepts;
import generated.course.LtiProperties;
import generated.course.Nvpair;
import generated.proto.common.LtiPropertiesProto;
import generated.proto.common.NvPairProto.NvPair;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf LtiProperties
 * message.
 * 
 * @author cpolynice
 *
 */
public class LtiPropertiesProtoCodec implements ProtoCodec<LtiPropertiesProto.LtiProperties, LtiProperties> {

    /* Codec that will be used to convert to/from an Nvpair instance. */
    private static NvPairProtoCodec codec = new NvPairProtoCodec();

    /**
     * Converts the given protobuf Nvpair list to the common object
     * representation.
     * 
     * @param protoList the protobuf list of Nvpairs
     * @return the Nvpair list
     */
    private static List<Nvpair> convertList(List<NvPair> protoList) {
        if (protoList == null) {
            return null;
        }

        List<Nvpair> commonList = new ArrayList<>();

        for (NvPair nv : protoList) {
            commonList.add(codec.convert(nv));
        }

        return commonList;
    }

    /**
     * Maps the given common object Nvpair list to the protobuf representation.
     * 
     * @param commonList the list of Nvpairs
     * @return the protobuf Nvpair list
     */
    private static List<NvPair> mapList(List<Nvpair> commonList) {
        if (commonList == null) {
            return null;
        }

        List<NvPair> protoList = new ArrayList<>();

        for (Nvpair nv : commonList) {
            protoList.add(codec.map(nv));
        }

        return protoList;
    }

    @Override
    public LtiProperties convert(LtiPropertiesProto.LtiProperties protoObject) {
        if (protoObject == null) {
            return null;
        }

        LtiProperties properties = new LtiProperties();

        if (protoObject.hasLtiIdentifier()) {
            properties.setLtiIdentifier(protoObject.getLtiIdentifier().getValue());
        }

        if (CollectionUtils.isNotEmpty(protoObject.getCustomParamList())) {
            CustomParameters customParameters = new CustomParameters();
            customParameters.getNvpair().addAll(convertList(protoObject.getCustomParamList()));
            properties.setCustomParameters(customParameters);
        }

        if (protoObject.hasAllowScore()) {
            properties.setAllowScore(BooleanEnum.fromValue(protoObject.getAllowScore().getValue()));
        }

        if (protoObject.hasSliderMin()) {
            properties.setSliderMinValue(new BigInteger(protoObject.getSliderMin().getValue()));
        }

        if (protoObject.hasSliderMax()) {
            properties.setSliderMaxValue(new BigInteger(protoObject.getSliderMax().getValue()));
        }

        if (CollectionUtils.isNotEmpty(protoObject.getConceptsList())) {
            LtiConcepts ltiConcepts = new LtiConcepts();
            ltiConcepts.getConcepts().addAll(protoObject.getConceptsList());
            properties.setLtiConcepts(ltiConcepts);
        }

        if (protoObject.hasIsKnowledge()) {
            properties.setIsKnowledge(BooleanEnum.fromValue(protoObject.getIsKnowledge().getValue()));
        }

        if (protoObject.hasDisplayMode()) {
            properties.setDisplayMode(DisplayModeEnum.fromValue(protoObject.getDisplayMode().getValue()));
        }

        return properties;
    }

    @Override
    public LtiPropertiesProto.LtiProperties map(LtiProperties commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        LtiPropertiesProto.LtiProperties.Builder builder = LtiPropertiesProto.LtiProperties.newBuilder();
        
        Optional.ofNullable(commonObject.getLtiIdentifier()).ifPresent(id -> {
            builder.setLtiIdentifier(StringValue.of(id));
        });

        if (commonObject.getCustomParameters() != null) {
            Optional.ofNullable(commonObject.getCustomParameters().getNvpair()).ifPresent(nvPair -> {
                builder.addAllCustomParam(mapList(nvPair));
            });
        }
        
        Optional.ofNullable(commonObject.getAllowScore()).ifPresent(allowScore -> {
            builder.setAllowScore(StringValue.of(allowScore.value()));
        });
        
        Optional.ofNullable(commonObject.getSliderMinValue()).ifPresent(minValue -> {
            builder.setSliderMin(StringValue.of(minValue.toString()));
        });

        Optional.ofNullable(commonObject.getSliderMaxValue()).ifPresent(maxValue -> {
            builder.setSliderMax(StringValue.of(maxValue.toString()));
        });

        if (commonObject.getLtiConcepts() != null
                && CollectionUtils.isNotEmpty(commonObject.getLtiConcepts().getConcepts())) {
            builder.addAllConcepts(commonObject.getLtiConcepts().getConcepts());
        }
        
        Optional.ofNullable(commonObject.getIsKnowledge()).ifPresent(knowledge -> {
            builder.setIsKnowledge(StringValue.of(knowledge.value()));
        });
        
        Optional.ofNullable(commonObject.getDisplayMode()).ifPresent(mode -> {
            builder.setDisplayMode(StringValue.of(mode.value()));
        });   

        return builder.build();
    }

}
