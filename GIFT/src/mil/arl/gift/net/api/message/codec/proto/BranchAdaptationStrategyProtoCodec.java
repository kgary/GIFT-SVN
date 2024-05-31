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

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractRemediationConceptProto;
import generated.proto.common.AdvancementConceptProto;
import generated.proto.common.BranchAdaptationStrategyProto;
import generated.proto.common.BranchAdaptationStrategyProto.AbstractRemediationConceptList;
import generated.proto.common.MetadataAttributeItemProto;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AbstractRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ActiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AdvancementInfo;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AdvancementInfo.AdvancementConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.BranchAdpatationStrategyTypeInterface;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ConstructiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.InteractiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.MetadataAttributeItem;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.PassiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ProgressionInfo;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.RemediationInfo;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.metadata.AbstractMetadataFileHandler;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * BranchAdaptationStrategy message.
 * 
 * @author cpolynice
 *
 */
public class BranchAdaptationStrategyProtoCodec
        implements ProtoCodec<BranchAdaptationStrategyProto.BranchAdaptationStrategy, BranchAdaptationStrategy> {

    /* Codec that will be used to convert to/from the AbstractEnums. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /**
     * Converts the given protobuf metadata attribute list to the common object
     * representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<MetadataAttributeItem> convertAttributesList(
            List<MetadataAttributeItemProto.MetadataAttributeItem> protoList) {
        if (protoList == null) {
            return null;
        }

        List<MetadataAttributeItem> commonList = new ArrayList<>();

        for (MetadataAttributeItemProto.MetadataAttributeItem attribute : protoList) {
            int priority = attribute.hasPriority() ? attribute.getPriority().getValue() : 0;
            String attr = attribute.hasAttribute() ? attribute.getAttribute().getValue() : null;

            MetadataAttributeItem item = new MetadataAttributeItem(attr, priority);

            if (attribute.hasLabel()) {
                item.setLabel(attribute.getLabel().getValue());
            }

            commonList.add(item);
        }

        return commonList;
    }

    /**
     * Converts the given protobuf list of AbstractRemediationConcepts to the
     * common object representation.
     * 
     * @param protoList the protobuf list of AbstractRemediationConcepts
     * @return the common object list.
     */
    private static List<AbstractRemediationConcept> convertConceptsList(AbstractRemediationConceptList protoList) {
        if (protoList == null) {
            return null;
        }

        List<AbstractRemediationConcept> commonList = new ArrayList<>();

        for (AbstractRemediationConceptProto.AbstractRemediationConcept concept : protoList.getConceptsList()) {
            if (concept.hasPassiveRemediationConcept()) {
                AbstractRemediationConceptProto.PassiveRemediationConcept passiveConcept = concept
                        .getPassiveRemediationConcept();

                List<MetadataAttributeItem> attributes = convertAttributesList(passiveConcept.getAttributesList());
                String conceptVal = passiveConcept.hasConcept() ? passiveConcept.getConcept().getValue() : null;
                MerrillQuadrantEnum quadrant = passiveConcept.hasRemediationConcept()
                        ? (MerrillQuadrantEnum) enumCodec.convert(passiveConcept.getRemediationConcept())
                        : null;

                if (conceptVal != null && quadrant != null) {
                    PassiveRemediationConcept passiveRemediation = new PassiveRemediationConcept(conceptVal, attributes,
                            quadrant);
                    commonList.add(passiveRemediation);
                }
            } else if (concept.hasActiveRemediationConcept()) {
                AbstractRemediationConceptProto.ActiveRemediationConcept activeConcept = concept
                        .getActiveRemediationConcept();

                if (activeConcept.hasConcept()) {
                    ActiveRemediationConcept activeRemediation = new ActiveRemediationConcept(
                            activeConcept.getConcept().getValue());
                    commonList.add(activeRemediation);
                }
            } else if (concept.hasConstructiveRemediationConcept()) {
                AbstractRemediationConceptProto.ConstructiveRemediationConcept constructiveConcept = concept
                        .getConstructiveRemediationConcept();

                if (constructiveConcept.hasConcept()) {
                    ConstructiveRemediationConcept constructiveRemediation = new ConstructiveRemediationConcept(
                            constructiveConcept.getConcept().getValue());
                    commonList.add(constructiveRemediation);
                }
            } else if (concept.hasInteractiveRemediationConcept()) {
                AbstractRemediationConceptProto.InteractiveRemediationConcept interactiveConcept = concept
                        .getInteractiveRemediationConcept();

                if (interactiveConcept.hasConcept()) {
                    InteractiveRemediationConcept interactiveRemediation = new InteractiveRemediationConcept(
                            interactiveConcept.getConcept().getValue());
                    commonList.add(interactiveRemediation);
                }
            }
        }

        return commonList;
    }

    /**
     * Converts the protobuf ProgressionInfo object to the given common object
     * representation.
     * 
     * @param protoObject the protobuf ProgressionInfo
     * @return the common object ProgressionInfo depending on attributes.
     */
    private static ProgressionInfo convertProgressionInfo(BranchAdaptationStrategyProto.ProgressionInfo protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasNextQuadrant()) {
            List<MetadataAttributeItem> attributes = convertAttributesList(protoObject.getAttributesList());
            MerrillQuadrantEnum quadrant = (MerrillQuadrantEnum) enumCodec.convert(protoObject.getNextQuadrant());

            return new ProgressionInfo(quadrant, attributes);
        } else {
            return new ProgressionInfo();
        }
    }

    /**
     * Converts the protobuf remediation info object to the common object type.
     * 
     * @param protoObject the protobuf RemediationInfo
     * @return the common object representation.
     */
    private static RemediationInfo convertRemediationInfo(BranchAdaptationStrategyProto.RemediationInfo protoObject) {
        if (protoObject == null) {
            return null;
        }

        RemediationInfo remediationInfo = new RemediationInfo();

        Boolean afterPractice = protoObject.hasAfterPractice() ? protoObject.getAfterPractice().getValue() : null;
        if (afterPractice != null && afterPractice) {
            remediationInfo.setAfterPractice(afterPractice);
        }

        Map<String, List<AbstractRemediationConcept>> remediationMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(protoObject.getRemediationMap())) {
            for (Map.Entry<String, AbstractRemediationConceptList> remediation : protoObject
                    .getRemediationMap().entrySet()) {
                String key = remediation.getKey();
                List<AbstractRemediationConcept> value = convertConceptsList(remediation.getValue());
                remediationMap.put(key, value);
            }
        }

        remediationInfo.setConceptRemediationMap(remediationMap);
        return remediationInfo;
    }

    /**
     * Converts the given protobuf AdvancementInfo into the commmon object type.
     * 
     * @param protoObject the protobuf AdvancementInfo.
     * @return the common object AdvancementInfo.
     */
    private static AdvancementInfo convertAdvancementInfo(BranchAdaptationStrategyProto.AdvancementInfo protoObject) {
        if (protoObject == null) {
            return null;
        }

        Boolean isSkill = false;
        if (protoObject.hasIsSkill()) {
            isSkill = protoObject.getIsSkill().getValue();
        }

        List<AdvancementConcept> concepts = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(protoObject.getConceptList())) {
            for (AdvancementConceptProto.AdvancementConcept conceptItem : protoObject.getConceptList()) {
                String name = conceptItem.hasConcept() ? conceptItem.getConcept().getValue() : null;
                AdvancementConcept concept = new AdvancementConcept(name);

                if (conceptItem.hasAdvancementReason()) {
                    concept.setReason(conceptItem.getAdvancementReason().getValue());
                }

                concepts.add(concept);
            }
        }

        return new AdvancementInfo(concepts, isSkill);
    }
    
    /**
     * Maps the given common object list of metadata attributes to the protobuf
     * representation.
     * 
     * @param commonList the list of metadata attributes.
     * @return the protobuf list representation.
     * @throws SAXException
     * @throws JAXBException
     */
    private static List<MetadataAttributeItemProto.MetadataAttributeItem> mapAttributesList(
            List<MetadataAttributeItem> commonList) throws SAXException, JAXBException {
        if (commonList == null) {
            return null;
        }

        List<MetadataAttributeItemProto.MetadataAttributeItem> protoList = new ArrayList<>();

        for (MetadataAttributeItem attribute : commonList) {

            MetadataAttributeItemProto.MetadataAttributeItem.Builder item = MetadataAttributeItemProto.MetadataAttributeItem
                    .newBuilder();
            item.setPriority(Int32Value.of(attribute.getPriority()));
            Optional.ofNullable(AbstractMetadataFileHandler.getRawAttribute(attribute.getAttribute()))
                    .ifPresent(attr -> {
                        item.setAttribute(StringValue.of(attr));
                    });
            Optional.ofNullable(attribute.getLabel()).ifPresent(label -> {
                item.setLabel(StringValue.of(label));
            });

            protoList.add(item.build());
        }

        return protoList;
    }

    /**
     * Maps the given common object list of AbstractRemediationConcepts to the
     * protobuf representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list representation.
     * @throws SAXException
     * @throws JAXBException
     */
    private static AbstractRemediationConceptList mapConceptsList(
            List<AbstractRemediationConcept> commonList) throws SAXException, JAXBException {
        if (commonList == null) {
            return null;
        }

        AbstractRemediationConceptList.Builder protoList = AbstractRemediationConceptList
                .newBuilder();

        for (AbstractRemediationConcept concept : commonList) {
            if (concept instanceof PassiveRemediationConcept) {
                AbstractRemediationConceptProto.PassiveRemediationConcept.Builder passiveConcept = AbstractRemediationConceptProto.PassiveRemediationConcept
                        .newBuilder();
                PassiveRemediationConcept commonConcept = (PassiveRemediationConcept) concept;

                Optional.ofNullable(mapAttributesList(commonConcept.getAttributes())).ifPresent(passiveConcept::addAllAttributes);
                Optional.ofNullable(commonConcept.getConcept()).ifPresent(conc -> {
                    passiveConcept.setConcept(StringValue.of(conc));
                });
                Optional.ofNullable(enumCodec.map(commonConcept.getQuadrant())).ifPresent(passiveConcept::setRemediationConcept);
                protoList.addConcepts(AbstractRemediationConceptProto.AbstractRemediationConcept.newBuilder().setPassiveRemediationConcept(passiveConcept).build());
            } else if (concept instanceof ActiveRemediationConcept) {
                AbstractRemediationConceptProto.ActiveRemediationConcept.Builder activeConcept = AbstractRemediationConceptProto.ActiveRemediationConcept
                        .newBuilder();
                ActiveRemediationConcept commonConcept = (ActiveRemediationConcept) concept;

                Optional.ofNullable(commonConcept.getConcept()).ifPresent(conc -> {
                    activeConcept.setConcept(StringValue.of(conc));
                });
                protoList.addConcepts(AbstractRemediationConceptProto.AbstractRemediationConcept.newBuilder()
                        .setActiveRemediationConcept(activeConcept).build());
            } else if (concept instanceof ConstructiveRemediationConcept) {
                AbstractRemediationConceptProto.ConstructiveRemediationConcept.Builder constructiveConcept = AbstractRemediationConceptProto.ConstructiveRemediationConcept
                        .newBuilder();
                ConstructiveRemediationConcept commonConcept = (ConstructiveRemediationConcept) concept;

                Optional.ofNullable(commonConcept.getConcept()).ifPresent(conc -> {
                    constructiveConcept.setConcept(StringValue.of(conc));
                });
                protoList.addConcepts(AbstractRemediationConceptProto.AbstractRemediationConcept.newBuilder()
                        .setConstructiveRemediationConcept(constructiveConcept).build());
            } else if (concept instanceof InteractiveRemediationConcept) {
                AbstractRemediationConceptProto.InteractiveRemediationConcept.Builder interactiveConcept = AbstractRemediationConceptProto.InteractiveRemediationConcept
                        .newBuilder();
                InteractiveRemediationConcept commonConcept = (InteractiveRemediationConcept) concept;

                Optional.ofNullable(commonConcept.getConcept()).ifPresent(conc -> {
                    interactiveConcept.setConcept(StringValue.of(conc));
                });
                protoList.addConcepts(AbstractRemediationConceptProto.AbstractRemediationConcept.newBuilder()
                        .setInteractiveRemediationConcept(interactiveConcept).build());
            }
        }

        return protoList.build();
    }

    /**
     * Maps the common object ProgressionInfo to the given protobuf
     * representation.
     * 
     * @param commonObject the common ProgressionInfo
     * @return the protobuf ProgressionInfo depending on attributes.
     */
    private static BranchAdaptationStrategyProto.ProgressionInfo mapProgressionInfo(ProgressionInfo commonObject)
            throws SAXException, JAXBException {
        if (commonObject == null) {
            return null;
        }

        BranchAdaptationStrategyProto.ProgressionInfo.Builder protoObject = BranchAdaptationStrategyProto.ProgressionInfo
                .newBuilder();

        if (commonObject.getQuadrant() != null) {
            Optional.ofNullable(mapAttributesList(commonObject.getAttributes())).ifPresent(protoObject::addAllAttributes);
            Optional.ofNullable(enumCodec.map(commonObject.getQuadrant())).ifPresent(protoObject::setNextQuadrant);       
        } 
        
        return protoObject.build();
    }

    /**
     * Maps the common object RemediationInfo to the protobuf representation.
     * 
     * @param commonObject the common RemediationInfo
     * @return the protobuf object representation.
     * @throws SAXException
     * @throws JAXBException
     */
    private static BranchAdaptationStrategyProto.RemediationInfo mapRemediationInfo(RemediationInfo commonObject)
            throws SAXException, JAXBException {
        if (commonObject == null) {
            return null;
        }

        BranchAdaptationStrategyProto.RemediationInfo.Builder protoObject = BranchAdaptationStrategyProto.RemediationInfo
                .newBuilder();

        protoObject.setAfterPractice(BoolValue.of(commonObject.isAfterPractice()));

        Map<String, AbstractRemediationConceptList> remediationMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(commonObject.getRemediationMap())) {
            for (Map.Entry<String, List<AbstractRemediationConcept>> remediation : commonObject.getRemediationMap()
                    .entrySet()) {
                String key = remediation.getKey();
                AbstractRemediationConceptList value = mapConceptsList(remediation.getValue());

                if (value != null) {
                    remediationMap.put(key, value);
                }
            }
        }

        protoObject.putAllRemediation(remediationMap);
        return protoObject.build();
    }

    /**
     * Maps the given common object AdvancementInfo to the protobuf
     * representation.
     * 
     * @param commonObject the common AdvancementInfo
     * @return the protobuf object representation.
     */
    private static BranchAdaptationStrategyProto.AdvancementInfo mapAdvancementInfo(AdvancementInfo commonObject) {
        if (commonObject == null) {
            return null;
        }

        BranchAdaptationStrategyProto.AdvancementInfo.Builder protoObject = BranchAdaptationStrategyProto.AdvancementInfo
                .newBuilder();
        protoObject.setIsSkill(BoolValue.of(commonObject.isSkill()));

        if (CollectionUtils.isNotEmpty(commonObject.getConcepts())) {
            for (AdvancementConcept conceptItem : commonObject.getConcepts()) {
                AdvancementConceptProto.AdvancementConcept.Builder concept = AdvancementConceptProto.AdvancementConcept
                        .newBuilder();
                Optional.ofNullable(conceptItem.getConcept()).ifPresent(conc -> {
                    concept.setConcept(StringValue.of(conc));
                });
                Optional.ofNullable(conceptItem.getReason()).ifPresent(reason -> {
                    concept.setAdvancementReason(StringValue.of(reason));
                });

                protoObject.addConcept(concept);
            }
        }

        return protoObject.build();
    }
    
    @Override
    public BranchAdaptationStrategy convert(BranchAdaptationStrategyProto.BranchAdaptationStrategy protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasProgressionInfo()) {
            ProgressionInfo info = convertProgressionInfo(protoObject.getProgressionInfo());
            return new BranchAdaptationStrategy(info);
        } else if (protoObject.hasRemediationInfo()) {
            RemediationInfo info = convertRemediationInfo(protoObject.getRemediationInfo());
            return new BranchAdaptationStrategy(info);
        } else if (protoObject.hasAdvancementInfo()) {
            AdvancementInfo info = convertAdvancementInfo(protoObject.getAdvancementInfo());
            return new BranchAdaptationStrategy(info);
        } else {
            throw new IllegalArgumentException(
                    "Found unhandled branch adapatation strategy type of " + protoObject + ".");
        }
    }

    @Override
    public BranchAdaptationStrategyProto.BranchAdaptationStrategy map(BranchAdaptationStrategy commonObject) {
        if (commonObject == null) {
            return null;
        }

        BranchAdaptationStrategyProto.BranchAdaptationStrategy.Builder builder = BranchAdaptationStrategyProto.BranchAdaptationStrategy
                .newBuilder();

        BranchAdpatationStrategyTypeInterface type = commonObject.getStrategyType();

        try {
            if (type instanceof ProgressionInfo) {
                Optional.ofNullable(mapProgressionInfo((ProgressionInfo) type)).ifPresent(builder::setProgressionInfo);
            } else if (type instanceof RemediationInfo) {
                Optional.ofNullable(mapRemediationInfo((RemediationInfo) type)).ifPresent(builder::setRemediationInfo);
            } else if (type instanceof AdvancementInfo) {
                Optional.ofNullable(mapAdvancementInfo((AdvancementInfo) type)).ifPresent(builder::setAdvancementInfo);
            } else {
                throw new IllegalArgumentException("Found unhandled strategy type of " + type + ".");
            }
        } catch (@SuppressWarnings("unused") Exception e) {
            throw new MessageEncodeException(BranchAdaptationStrategy.class.getName(),
                    "There was a problem encoding the metadata item list");
        }

        return builder.build();
    }
}
