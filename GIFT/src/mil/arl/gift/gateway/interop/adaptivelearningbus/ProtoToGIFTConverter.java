/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.adaptivelearningbus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.protobuf.Timestamp;

import generated.course.ConceptNode;
import generated.course.Concepts;
import generated.course.Concepts.List.Concept;
import generated.dkf.AGL;
import generated.dkf.ActorTypeCategoryEnum;
import generated.dkf.Audio;
import generated.dkf.BooleanEnum;
import generated.dkf.Coordinate;
import generated.dkf.DelayAfterStrategy;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.EnvironmentAdaptation.CreateActors;
import generated.dkf.EnvironmentAdaptation.Endurance;
import generated.dkf.EnvironmentAdaptation.FatigueRecovery;
import generated.dkf.EnvironmentAdaptation.Fog;
import generated.dkf.EnvironmentAdaptation.Overcast;
import generated.dkf.EnvironmentAdaptation.Rain;
import generated.dkf.EnvironmentAdaptation.RemoveActors;
import generated.dkf.EnvironmentAdaptation.Script;
import generated.dkf.EnvironmentAdaptation.Teleport;
import generated.dkf.EnvironmentAdaptation.TimeOfDay;
import generated.dkf.Feedback;
import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.InTutor;
import generated.dkf.InstructionalIntervention;
import generated.dkf.Message;
import generated.dkf.Message.Delivery;
import generated.dkf.Message.Delivery.InTrainingApplication;
import generated.dkf.Message.Delivery.InTrainingApplication.MobileOption;
import generated.dkf.MidLessonMedia;
import generated.dkf.PerformanceAssessment;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.Strategy;
import generated.dkf.StrategyHandler;
import generated.dkf.TeamRef;
import generated.dkf.ToObserverController;
import generated.proto.gateway.AffectiveStateProto;
import generated.proto.gateway.ApplyStrategiesProto;
import generated.proto.gateway.AuthorizeStrategiesRequestProto;
import generated.proto.gateway.AvatarDataProto;
import generated.proto.gateway.BranchAdaptationStrategyProto;
import generated.proto.gateway.ClearTextActionProto;
import generated.proto.gateway.CloseDomainSessionRequestProto;
import generated.proto.gateway.CognitiveStateProto;
import generated.proto.gateway.CoordinateProto;
import generated.proto.gateway.DisplayAvatarActionProto;
import generated.proto.gateway.DisplayHTMLFeedbackActionProto;
import generated.proto.gateway.DisplayTextActionProto;
import generated.proto.gateway.DomainOptionPermissionsProto;
import generated.proto.gateway.DomainOptionProto;
import generated.proto.gateway.DomainOptionProto.DomainOption.ConceptList;
import generated.proto.gateway.DomainOptionProto.DomainOption.ConceptTree;
import generated.proto.gateway.DomainOptionProto.DomainOption.LeafConcept;
import generated.proto.gateway.DomainOptionsReplyProto;
import generated.proto.gateway.DomainOptionsReplyProto.DomainOptionsReply;
import generated.proto.gateway.DomainOptionsRequestProto;
import generated.proto.gateway.DomainSelectionRequestProto;
import generated.proto.gateway.DomainSessionProto;
import generated.proto.gateway.EnvironmentAdaptationProto;
import generated.proto.gateway.EnvironmentControlProto;
import generated.proto.gateway.EvaluatorUpdateRequestProto;
import generated.proto.gateway.FilteredSensorDataProto;
import generated.proto.gateway.InTutorProto;
import generated.proto.gateway.LearnerStateAttributeCollectionProto;
import generated.proto.gateway.LearnerStateAttributeProto;
import generated.proto.gateway.LearnerStateProto;
import generated.proto.gateway.PedRequestBranchAdaptationProto;
import generated.proto.gateway.PedRequestDoNothingStrategyProto;
import generated.proto.gateway.PedRequestInstructionalInterventionProto;
import generated.proto.gateway.PedRequestMidLessonMediaProto;
import generated.proto.gateway.PedRequestPerformanceAssessmentProto;
import generated.proto.gateway.PedRequestScenarioAdaptationProto;
import generated.proto.gateway.PedagogicalRequestProto;
import generated.proto.gateway.PedagogicalRequestTypeProto;
import generated.proto.gateway.PerformanceStateAttributeProto;
import generated.proto.gateway.PerformanceStateProto;
import generated.proto.gateway.PlayAudioActionProto;
import generated.proto.gateway.SensorAttributeValueProto;
import generated.proto.gateway.StrategyProto;
import generated.proto.gateway.StrategyProto.Activity.TypeCase;
import generated.proto.gateway.StrategyProto.InstructionalIntervention.Builder;
import generated.proto.gateway.StrategyToApplyProto;
import generated.proto.gateway.StringPayloadProto;
import generated.proto.gateway.TaskPerformanceStateProto;
import generated.proto.gateway.TeamMemberRefProto;
import generated.proto.gateway.TeamMemberRoleAssignmentRequestProto;
import generated.proto.gateway.TeamRefProto;
import generated.proto.gateway.TutorUserInterfaceFeedbackPayloadProto.TutorUserInterfaceFeedbackPayload;
import mil.arl.gift.common.AbstractPedagogicalRequest;
import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.ClearTextAction;
import mil.arl.gift.common.CloseDomainSessionRequest;
import mil.arl.gift.common.DisplayAvatarAction;
import mil.arl.gift.common.DisplayHTMLFeedbackAction;
import mil.arl.gift.common.DisplayScriptedAvatarAction;
import mil.arl.gift.common.DisplayTextAction;
import mil.arl.gift.common.DisplayTextToSpeechAvatarAction;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.DomainOptionsList;
import mil.arl.gift.common.DomainOptionsRequest;
import mil.arl.gift.common.DomainSelectionRequest;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.ImageData;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.PlayAudioAction;
import mil.arl.gift.common.RequestBranchAdaptation;
import mil.arl.gift.common.RequestDoNothingTactic;
import mil.arl.gift.common.RequestInstructionalIntervention;
import mil.arl.gift.common.RequestMidLessonMedia;
import mil.arl.gift.common.RequestPerformanceAssessment;
import mil.arl.gift.common.RequestScenarioAdaptation;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.course.strategy.AuthorizeStrategiesRequest;
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
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.QuadrantInfo;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.RemediationInfo;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.metadata.AbstractMetadataFileHandler;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.sensor.ImageValue;
import mil.arl.gift.common.sensor.IntegerValue;
import mil.arl.gift.common.sensor.StringValue;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.AbstractPerformanceStateAttribute;
import mil.arl.gift.common.state.AffectiveState;
import mil.arl.gift.common.state.CognitiveState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.MessageEncodeException;

/**
 * A converter class for converting protobuf objects into their GIFT common
 * counterparts.
 *
 * @author sharrison
 */
public class ProtoToGIFTConverter {

    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ProtoToGIFTConverter.class);

    /** The default class for the strategy handler */
    private static final String DEFAULT_STRATEGY_HANDLER_CLASS = "domain.knowledge.strategy.DefaultStrategyHandler";

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj the protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static DomainOptionsRequest convertFromProto(DomainOptionsRequestProto.DomainOptionsRequest protoObj) {
        if (protoObj == null) {
            return null;
        }

        WebClientInformation info = new WebClientInformation();
        DomainOptionsRequest commonObj = new DomainOptionsRequest(info);

        final String lmsUsername = StringUtils.isNotBlank(protoObj.getLMSUserName()) ? protoObj.getLMSUserName() : null;
        commonObj.setLMSUserName(lmsUsername);

        return commonObj;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj the protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static DomainSelectionRequest convertFromProto(DomainSelectionRequestProto.DomainSelectionRequest protoObj) {
        if (protoObj == null) {
            return null;
        }

        WebClientInformation info = new WebClientInformation();
        final String lmsUsername = StringUtils.isNotBlank(protoObj.getLmsUsername()) ? protoObj.getLmsUsername() : null;
        final String dsId = StringUtils.isNotBlank(protoObj.getDomainSourceId()) ? protoObj.getDomainSourceId() : null;

        return new DomainSelectionRequest(lmsUsername, null, dsId, info, null);
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj the protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static CloseDomainSessionRequest convertFromProto(
            CloseDomainSessionRequestProto.CloseDomainSessionRequest protoObj) {
        if (protoObj == null) {
            return null;
        }

        final String reason = StringUtils.isNotBlank(protoObj.getReason()) ? protoObj.getReason() : null;
        return new CloseDomainSessionRequest(reason);
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj the protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static ApplyStrategies convertFromProto(ApplyStrategiesProto.ApplyStrategies protoObj) {
        if (protoObj == null) {
            return null;
        }

        final List<StrategyToApply> strategies = new ArrayList<>(protoObj.getStrategiesCount());
        for (StrategyToApplyProto.StrategyToApply protoStrat : protoObj.getStrategiesList()) {
            strategies.add(convertFromProto(protoStrat));
        }

        final String evaluator = StringUtils.isNotBlank(protoObj.getEvaluator()) ? protoObj.getEvaluator() : null;
        return new ApplyStrategies(strategies, evaluator);
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj the protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static StrategyToApply convertFromProto(StrategyToApplyProto.StrategyToApply protoObj) {
        if (protoObj == null || !protoObj.hasStrategy()) {
            return null;
        }

        try {
            StrategyProto.Strategy protoStrategy = protoObj.getStrategy();
            Strategy strategy = convertFromProto(protoStrategy);

            final String trigger = StringUtils.isNotBlank(protoObj.getTrigger()) ? protoObj.getTrigger() : null;
            final String evaluator = StringUtils.isNotBlank(protoObj.getEvaluator()) ? protoObj.getEvaluator() : null;
            return new StrategyToApply(strategy, trigger, evaluator);
        } catch (Exception e) {
            logger.error("Caught exception while trying to convert a strategy to apply.", e);
            throw new RuntimeException("There was a problem decoding the strategy to apply.");
        }
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static Strategy convertFromProto(StrategyProto.Strategy protoObj) {
        if (protoObj == null) {
            return null;
        }

        final Strategy strategy = new Strategy();
        final String name = StringUtils.isNotBlank(protoObj.getName()) ? protoObj.getName() : null;
        strategy.setName(name);

        final List<Serializable> activities = protoObj.getActivitiesList().stream()
                .map(ProtoToGIFTConverter::convertFromProto).collect(Collectors.toList());

        strategy.getStrategyActivities().addAll(activities);
        return strategy;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static Serializable convertFromProto(StrategyProto.Activity protoObj) {
        if (protoObj == null) {
            return null;
        }

        if (protoObj.hasInstructionalIntervention()) {
            return convertFromProto(protoObj.getInstructionalIntervention());
        } else if (protoObj.hasMidLessonMedia()) {
            return new MidLessonMedia();
        } else if (protoObj.hasScenarioAdaptation()) {
            return convertFromProto(protoObj.getScenarioAdaptation());
        } else {
            final TypeCase typeCase = protoObj.getTypeCase();
            final String typeName = typeCase != null ? typeCase.name() : "null";
            throw new IllegalArgumentException("Unable to convert '" + typeName + "' to a JAXB strategy activity.");
        }
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static ScenarioAdaptation convertFromProto(StrategyProto.ScenarioAdaptation protoObj) {
        if (protoObj == null) {
            return null;
        }

        ScenarioAdaptation adaptation = new ScenarioAdaptation();
        if (protoObj.getDelayAfterStrategy() != 0) {
            DelayAfterStrategy delay = new DelayAfterStrategy();
            delay.setDuration(BigDecimal.valueOf(protoObj.getDelayAfterStrategy()));
            adaptation.setDelayAfterStrategy(delay);
        }

        if (protoObj.hasEnvironmentAdaptation()) {
            adaptation.setEnvironmentAdaptation(convertFromProto(protoObj.getEnvironmentAdaptation()));
        }

        final String description = StringUtils.isNotBlank(protoObj.getDescription()) ? protoObj.getDescription() : null;
        adaptation.setDescription(description);
        adaptation.setMandatory(protoObj.getMandatory());

        StrategyHandler handler = new StrategyHandler();
        handler.setImpl(DEFAULT_STRATEGY_HANDLER_CLASS);
        adaptation.setStrategyHandler(handler);

        return adaptation;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static EnvironmentAdaptation convertFromProto(EnvironmentAdaptationProto.EnvironmentAdaptation protoObj) {
        if (protoObj == null) {
            return null;
        }

        EnvironmentAdaptation adaptation = new EnvironmentAdaptation();
        if (protoObj.hasOvercast()) {
            EnvironmentAdaptationProto.Overcast protoAdaptation = protoObj.getOvercast();
            Overcast overcast = new Overcast();
            overcast.setScenarioAdaptationDuration(BigInteger.valueOf(protoAdaptation.getDuration()));
            overcast.setValue(BigDecimal.valueOf(protoAdaptation.getValue()));
            adaptation.setType(overcast);
        } else if (protoObj.hasFog()) {
            EnvironmentAdaptationProto.Fog protoAdaptation = protoObj.getFog();
            Fog fog = new Fog();
            fog.setScenarioAdaptationDuration(BigInteger.valueOf(protoAdaptation.getDuration()));

            /* Convert color */
            Fog.Color color = new Fog.Color();
            color.setBlue(protoAdaptation.getColor().getBlue());
            color.setGreen(protoAdaptation.getColor().getGreen());
            color.setRed(protoAdaptation.getColor().getRed());
            fog.setColor(color);

            fog.setDensity(BigDecimal.valueOf(protoAdaptation.getDensity()));
            adaptation.setType(fog);
        } else if (protoObj.hasRain()) {
            EnvironmentAdaptationProto.Rain protoAdaptation = protoObj.getRain();
            Rain rain = new Rain();
            rain.setScenarioAdaptationDuration(BigInteger.valueOf(protoAdaptation.getDuration()));
            rain.setValue(BigDecimal.valueOf(protoAdaptation.getValue()));
            adaptation.setType(rain);
        } else if (protoObj.hasTimeOfDay()) {
            EnvironmentAdaptationProto.TimeOfDay protoAdaptation = protoObj.getTimeOfDay();
            TimeOfDay timeOfDay = new TimeOfDay();

            if (EnvironmentAdaptationProto.TimeOfDay.Time.MIDNIGHT.equals(protoAdaptation.getTime())) {
                timeOfDay.setType(new TimeOfDay.Midnight());
            } else if (EnvironmentAdaptationProto.TimeOfDay.Time.DAWN.equals(protoAdaptation.getTime())) {
                timeOfDay.setType(new TimeOfDay.Dawn());
            } else if (EnvironmentAdaptationProto.TimeOfDay.Time.MIDDAY.equals(protoAdaptation.getTime())) {
                timeOfDay.setType(new TimeOfDay.Midday());
            } else if (EnvironmentAdaptationProto.TimeOfDay.Time.DUSK.equals(protoAdaptation.getTime())) {
                timeOfDay.setType(new TimeOfDay.Dusk());
            }

            adaptation.setType(timeOfDay);
        } else if (protoObj.hasCreateActors()) {
            EnvironmentAdaptationProto.CreateActors protoAdaptation = protoObj.getCreateActors();
            CreateActors createActors = new CreateActors();

            createActors.setCoordinate(convertFromProto(protoAdaptation.getCoordinate()));
            
            if (protoAdaptation.getHeading() != null) {
                CreateActors.Heading heading = new CreateActors.Heading();
                heading.setValue(protoAdaptation.getHeading().getValue());
                createActors.setHeading(heading);
            }
            
            createActors.setType(protoAdaptation.getType());
            try {
                ActorTypeCategoryEnum catEnum = ActorTypeCategoryEnum.valueOf(protoAdaptation.getTypeCategory());
                createActors.setTypeCategory(catEnum);
            }catch(Exception e) {
                logger.warn("Found unhandled actor type category enum of "+protoAdaptation.getTypeCategory()+" for create actors environment adaptation", e);
            }

            if (EnvironmentAdaptationProto.CreateActors.Side.CIVILIAN.equals(protoAdaptation.getSide())) {
                CreateActors.Side side = new CreateActors.Side();
                side.setType(new CreateActors.Side.Civilian());
                createActors.setSide(side);
            } else if (EnvironmentAdaptationProto.CreateActors.Side.BLUFOR.equals(protoAdaptation.getSide())) {
                CreateActors.Side side = new CreateActors.Side();
                side.setType(new CreateActors.Side.Blufor());
                createActors.setSide(side);
            } else if (EnvironmentAdaptationProto.CreateActors.Side.OPFOR.equals(protoAdaptation.getSide())) {
                CreateActors.Side side = new CreateActors.Side();
                side.setType(new CreateActors.Side.Opfor());
                createActors.setSide(side);
            }

            adaptation.setType(createActors);
        } else if (protoObj.hasRemoveActors()) {
            EnvironmentAdaptationProto.RemoveActors protoAdaptation = protoObj.getRemoveActors();
            RemoveActors removeActors = new EnvironmentAdaptation.RemoveActors();
            
            removeActors.setType(protoAdaptation.getName());
            
            try {
                ActorTypeCategoryEnum catEnum = ActorTypeCategoryEnum.valueOf(protoAdaptation.getTypeCategory());
                removeActors.setTypeCategory(catEnum);
            }catch(Exception e) {
                logger.warn("Found unhandled actor type category enum of "+protoAdaptation.getTypeCategory()+" for create actors environment adaptation", e);
            }

            adaptation.setType(removeActors);
        } else if (protoObj.hasTeleport()) {
            EnvironmentAdaptationProto.Teleport protoAdaptation = protoObj.getTeleport();
            Teleport teleport = new EnvironmentAdaptation.Teleport();

            teleport.setCoordinate(convertFromProto(protoAdaptation.getCoordinate()));

            if (protoAdaptation.getHeading() != null) {
                Teleport.Heading heading = new Teleport.Heading();
                heading.setValue(protoAdaptation.getHeading().getValue());
                teleport.setHeading(heading);
            }

            if (protoAdaptation.getReference() != null) {
                Teleport.TeamMemberRef memberRef = new Teleport.TeamMemberRef();
                memberRef.setValue(protoAdaptation.getReference().getReference());
                teleport.setTeamMemberRef(memberRef);
            }

            adaptation.setType(teleport);
        } else if (protoObj.hasFatigueRecovery()) {
            EnvironmentAdaptationProto.FatigueRecovery protoAdaptation = protoObj.getFatigueRecovery();
            FatigueRecovery fatigue = new EnvironmentAdaptation.FatigueRecovery();

            fatigue.setRate(BigDecimal.valueOf(protoAdaptation.getRate()));

            if (protoAdaptation.getTeamMemberReference() != null) {
                FatigueRecovery.TeamMemberRef memberRef = new FatigueRecovery.TeamMemberRef();
                memberRef.setValue(protoAdaptation.getTeamMemberReference().getReference());
                fatigue.setTeamMemberRef(memberRef);
            }

            adaptation.setType(fatigue);
        } else if (protoObj.hasEndurance()) {
            EnvironmentAdaptationProto.Endurance protoAdaptation = protoObj.getEndurance();
            Endurance endurance = new EnvironmentAdaptation.Endurance();

            endurance.setValue(BigDecimal.valueOf(protoAdaptation.getValue()));

            if (protoAdaptation.getTeamMemberReference() != null) {
                Endurance.TeamMemberRef memberRef = new Endurance.TeamMemberRef();
                memberRef.setValue(protoAdaptation.getTeamMemberReference().getReference());
                endurance.setTeamMemberRef(memberRef);
            }

            adaptation.setType(endurance);
        } else if (protoObj.hasScript()) {
            EnvironmentAdaptationProto.Script protoAdaptation = protoObj.getScript();
            Script script = new EnvironmentAdaptation.Script();
            script.setValue(protoAdaptation.getValue());
            adaptation.setType(script);
        }

        return adaptation;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static Coordinate convertFromProto(CoordinateProto.Coordinate protoObj) {
        if (protoObj == null) {
            return null;
        }

        Coordinate coord = new Coordinate();

        if (protoObj.hasGcc()) {
            GCC gcc = new GCC();
            gcc.setX(BigDecimal.valueOf(protoObj.getGcc().getX()));
            gcc.setY(BigDecimal.valueOf(protoObj.getGcc().getY()));
            gcc.setZ(BigDecimal.valueOf(protoObj.getGcc().getZ()));
            coord.setType(gcc);
        } else if (protoObj.hasGdc()) {
            GDC gdc = new GDC();
            gdc.setElevation(BigDecimal.valueOf(protoObj.getGdc().getElevation()));
            gdc.setLatitude(BigDecimal.valueOf(protoObj.getGdc().getLatitude()));
            gdc.setLongitude(BigDecimal.valueOf(protoObj.getGdc().getLongitude()));
            coord.setType(gdc);
        } else if (protoObj.hasAgl()) {
            AGL agl = new AGL();
            agl.setX(BigDecimal.valueOf(protoObj.getAgl().getX()));
            agl.setY(BigDecimal.valueOf(protoObj.getAgl().getY()));
            agl.setElevation(BigDecimal.valueOf(protoObj.getAgl().getElevation()));
            coord.setType(agl);
        }

        return coord;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static InstructionalIntervention convertFromProto(StrategyProto.InstructionalIntervention protoObj) {
        if (protoObj == null) {
            return null;
        }

        InstructionalIntervention ii = new InstructionalIntervention();
        if (protoObj.getDelay() != 0) {
            DelayAfterStrategy delay = new DelayAfterStrategy();
            delay.setDuration(BigDecimal.valueOf(protoObj.getDelay()));
            ii.setDelayAfterStrategy(delay);
        }

        if (protoObj.hasFeedback()) {
            ii.setFeedback(convertFromProto(protoObj.getFeedback()));
        }

        ii.setMandatory(protoObj.getMandatory());

        StrategyHandler handler = new StrategyHandler();
        handler.setImpl(DEFAULT_STRATEGY_HANDLER_CLASS);
        ii.setStrategyHandler(handler);

        return ii;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static Feedback convertFromProto(StrategyProto.Feedback protoObj) {
        if (protoObj == null) {
            return null;
        }

        Feedback feedback = new Feedback();
        if (protoObj.hasPresentation()) {
            feedback.setFeedbackPresentation(convertFromProto(protoObj.getPresentation()));
        }

        final String affectiveFeedback = StringUtils.isNotBlank(protoObj.getAffectiveFeedbackType())
                ? protoObj.getAffectiveFeedbackType()
                : null;
        final String feedbackSpecificity = StringUtils.isNotBlank(protoObj.getFeedbackSpecificityType())
                ? protoObj.getFeedbackSpecificityType()
                : null;

        feedback.setAffectiveFeedbackType(affectiveFeedback);
        feedback.setFeedbackSpecificityType(feedbackSpecificity);

        if (protoObj.getDuration() != 0) {
            feedback.setFeedbackDuration(BigDecimal.valueOf(protoObj.getDuration()));
        }

        for (TeamRefProto.TeamRef teamRef : protoObj.getTeamRefList()) {
            feedback.getTeamRef().add(convertFromProto(teamRef));
        }

        return feedback;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static TeamRef convertFromProto(TeamRefProto.TeamRef protoObj) {
        if (protoObj == null) {
            return null;
        }

        TeamRef teamRef = new TeamRef();
        final String reference = StringUtils.isNotBlank(protoObj.getReference()) ? protoObj.getReference() : null;
        teamRef.setValue(reference);
        return teamRef;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static Serializable convertFromProto(StrategyProto.FeedbackPresentation protoObj) {
        if (protoObj == null) {
            return null;
        }

        if (protoObj.hasMessage()) {
            return convertFromProto(protoObj.getMessage());
        } else if (protoObj.hasAudio()) {
            return convertFromProto(protoObj.getAudio());
        } else if (protoObj.hasFile()) {
            return convertFromProto(protoObj.getFile());
        }

        return null;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static Message convertFromProto(StrategyProto.FeedbackMessage protoObj) {
        if (protoObj == null) {
            return null;
        }

        Message message = new Message();

        final String content = StringUtils.isNotBlank(protoObj.getContent()) ? protoObj.getContent() : null;
        message.setContent(content);

        if (protoObj.hasDelivery()) {
            message.setDelivery(convertFromProto(protoObj.getDelivery()));
        }

        return message;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static Delivery convertFromProto(StrategyProto.FeedbackMessage.Delivery protoObj) {
        if (protoObj == null) {
            return null;
        }

        Delivery delivery = new Delivery();

        if (protoObj.hasInTrainingApplication()) {
            final StrategyProto.InTrainingApplication inTrainingApplication = protoObj.getInTrainingApplication();
            delivery.setInTrainingApplication(convertFromProto(inTrainingApplication));
        }

        if (protoObj.hasInTutor()) {
            final InTutorProto.InTutor inTutor = protoObj.getInTutor();
            delivery.setInTutor(convertFromProto(inTutor));
        }

        if (protoObj.hasToObserverController()) {
            final StrategyProto.ToObserverController toObserverController = protoObj.getToObserverController();
            delivery.setToObserverController(convertFromProto(toObserverController));
        }

        return delivery;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    private static InTrainingApplication convertFromProto(StrategyProto.InTrainingApplication protoObj) {
        if (protoObj == null) {
            return null;
        }

        InTrainingApplication toRet = new InTrainingApplication();

        final BooleanEnum isEnabled = protoObj.getEnabled() ? BooleanEnum.TRUE : BooleanEnum.FALSE;
        toRet.setEnabled(isEnabled);
        if (protoObj.hasMobileOption()) {
            MobileOption mobileOption = convertFromProto(protoObj.getMobileOption());
            toRet.setMobileOption(mobileOption);
        }

        return toRet;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    private static MobileOption convertFromProto(StrategyProto.MobileOption protoObj) {
        if (protoObj == null) {
            return null;
        }

        final MobileOption toRet = new MobileOption();

        final boolean isVibrate = protoObj.getVibrate();
        toRet.setVibrate(isVibrate);

        return toRet;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    private static InTutor convertFromProto(InTutorProto.InTutor protoObj) {
        if (protoObj == null) {
            return null;
        }

        InTutor toRet = new InTutor();

        final String msgPresentation = StringUtils.isNotBlank(protoObj.getMessagePresentation())
                ? protoObj.getMessagePresentation()
                : null;
        final String txtEnhancement = StringUtils.isNotBlank(protoObj.getTextEnhancement())
                ? protoObj.getTextEnhancement()
                : null;

        toRet.setMessagePresentation(msgPresentation);
        toRet.setTextEnhancement(txtEnhancement);
        return toRet;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static ToObserverController convertFromProto(StrategyProto.ToObserverController protoObj) {
        if (protoObj == null) {
            return null;
        }

        ToObserverController controller = new ToObserverController();

        final String value = StringUtils.isNotBlank(protoObj.getValue()) ? protoObj.getValue() : null;
        controller.setValue(value);

        return controller;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static Audio convertFromProto(StrategyProto.Audio protoObj) {
        if (protoObj == null) {
            return null;
        }

        Audio audio = new Audio();

        final String ogg = StringUtils.isNotBlank(protoObj.getOggFile()) ? protoObj.getOggFile() : null;
        final String mp3 = StringUtils.isNotBlank(protoObj.getMp3File()) ? protoObj.getMp3File() : null;

        audio.setOGGFile(ogg);
        audio.setMP3File(mp3);

        if (protoObj.hasToObserverController()) {
            audio.setToObserverController(convertFromProto(protoObj.getToObserverController()));
        }

        return audio;
    }

    /**
     * Convert the protobuf object into the GIFT common version.
     *
     * @param protoObj The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static Feedback.File convertFromProto(StrategyProto.FeedbackFile protoObj) {
        if (protoObj == null) {
            return null;
        }

        Feedback.File file = new Feedback.File();

        final String html = StringUtils.isNotBlank(protoObj.getHtml()) ? protoObj.getHtml() : null;
        file.setHTML(html);
        return file;
    }

    /**
     * Converts an {@link AbstractTeamUnit} into its protobuf equivalent.
     *
     * @param unit The {@link AbstractTeamUnit} to convert.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static TeamMemberRoleAssignmentRequestProto.TeamElement convertToProto(AbstractTeamUnit unit) {
        if (unit == null) {
            return null;
        }

        final TeamMemberRoleAssignmentRequestProto.TeamElement.Builder builder = TeamMemberRoleAssignmentRequestProto.TeamElement.newBuilder();
        if (unit instanceof Team) {
            Team team = (Team) unit;
            Optional.ofNullable(convertToProto(team)).ifPresent(builder::setSubTeam);
        } else if (unit instanceof TeamMember<?>) {
            final TeamMember<?> member = (TeamMember<?>) unit;
            Optional.ofNullable(convertToProto(member)).ifPresent(builder::setMember);
        } else {
            final String unitClass = unit.getClass().getName();
            throw new IllegalArgumentException("Unable to convert type '" + unitClass + "'.");
        }

        return builder.build();
    }

    /**
     * Converts a {@link Team} into its protobuf equivalent.
     *
     * @param team The {@link Team} to convert.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    public static TeamMemberRoleAssignmentRequestProto.Team convertToProto(Team team) {
        if (team == null) {
            return null;
        }

        TeamMemberRoleAssignmentRequestProto.Team.Builder teamBuilder = TeamMemberRoleAssignmentRequestProto.Team
                .newBuilder();
        for (AbstractTeamUnit unit : team.getUnits()) {
            Optional.ofNullable(convertToProto(unit)).ifPresent(teamBuilder::addTeamElements);
        }

        return teamBuilder.build();
    }

    /**
     * Converts a {@link TeamMember} into its protobuf equivalent.
     *
     * @param member The {@link TeamMember} to convert.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static TeamMemberRoleAssignmentRequestProto.TeamMember convertToProto(TeamMember<?> member) {
        if (member == null) {
            return null;
        }

        TeamMemberRoleAssignmentRequestProto.TeamMember.Builder builder = TeamMemberRoleAssignmentRequestProto.TeamMember
                .newBuilder();
        Optional.ofNullable(member.getName()).ifPresent(builder::setRoleName);
        return builder.build();
    }

    /**
     * Convert from {@link DomainOptionsList} to its equivalent protobuf type.
     *
     * @param domainOptionsList The {@link DomainOptionsList} to convert. Can be
     *        null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    public static DomainOptionsReplyProto.DomainOptionsReply convertToProto(DomainOptionsList domainOptionsList) {
        if (domainOptionsList == null) {
            return null;
        }

        DomainOptionsReply.Builder builder = DomainOptionsReply.newBuilder();
        if (domainOptionsList.getOptions() != null) {
            for (DomainOption option : domainOptionsList.getOptions()) {
                Optional.ofNullable(convertToProto(option)).ifPresent(builder::addDomainOptions);
            }
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static DomainOptionProto.DomainOption convertToProto(DomainOption commonObj) {
        if (commonObj == null) {
            return null;
        }

        DomainOptionProto.DomainOption.Builder builder = DomainOptionProto.DomainOption.newBuilder();
        Optional.ofNullable(commonObj.getUsername()).ifPresent(builder::setUsername);
        Optional.ofNullable(commonObj.getDomainName()).ifPresent(builder::setDomainName);
        Optional.ofNullable(commonObj.getDomainId()).ifPresent(builder::setDomainId);
        Optional.ofNullable(commonObj.getSourceId()).ifPresent(builder::setSourceId);
        Optional.ofNullable(commonObj.getDescription()).ifPresent(builder::setDescription);

        if (commonObj.getDomainOptionRecommendation() != null) {
            builder.setType(commonObj.getDomainOptionRecommendation().getDomainOptionRecommendationEnum().getName());
            Optional.ofNullable(commonObj.getDomainOptionRecommendation().getDetails()).ifPresent(builder::setDetails);
            Optional.ofNullable(commonObj.getDomainOptionRecommendation().getReason()).ifPresent(builder::setReason);
        }

        if (commonObj.getDomainOptionPermissions() != null) {
            for (DomainOptionPermissions commonPermission : commonObj.getDomainOptionPermissions()) {
                Optional.ofNullable(convertToProto(commonPermission)).ifPresent(builder::addDomainOptionPermissions);
            }
        }

        final Concepts concepts = commonObj.getConcepts();
        if (concepts != null) {
            try {
                final Serializable listOrHierarchy = concepts.getListOrHierarchy();
                if (listOrHierarchy instanceof Concepts.List) {
                    final Concepts.List conceptList = (Concepts.List) listOrHierarchy;
                    Optional.ofNullable(convertToProto(conceptList)).ifPresent(builder::setList);
                } else if (listOrHierarchy instanceof Concepts.Hierarchy) {
                    final Concepts.Hierarchy hierarchy = (Concepts.Hierarchy) listOrHierarchy;
                    Optional.ofNullable(convertToProto(hierarchy)).ifPresent(builder::setTree);
                }
            } catch (Exception e) {
                logger.error("Caught exception while trying to convert the domain option concepts.", e);
                throw new RuntimeException("There was a problem encoding the Domain Option object");
            }
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static ConceptList convertToProto(Concepts.List commonObj) {
        if (commonObj == null) {
            return null;
        }

        final ConceptList.Builder builder = ConceptList.newBuilder();
        for (Concept c : commonObj.getConcept()) {
            if (c == null || c.getName() == null) {
                continue;
            }

            builder.addConcepts(LeafConcept.newBuilder().setName(c.getName()));
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static DomainOptionProto.DomainOption.ConceptTree convertToProto(Concepts.Hierarchy commonObj) {
        if (commonObj == null) {
            return null;
        }

        final ConceptTree.Builder builder = ConceptTree.newBuilder();
        Optional.ofNullable(convertToProto(commonObj.getConceptNode())).ifPresent(builder::setRootConcept);
        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static DomainOptionProto.DomainOption.Concept convertToProto(ConceptNode commonObj) {
        if (commonObj == null) {
            return null;
        }

        final DomainOptionProto.DomainOption.Concept.Builder builder = DomainOptionProto.DomainOption.Concept
                .newBuilder();

        if (commonObj.getConceptNode().isEmpty()) {
            final LeafConcept.Builder protoLeaf = LeafConcept.newBuilder();
            Optional.ofNullable(commonObj.getName()).ifPresent(protoLeaf::setName);
            builder.setLeafConcept(protoLeaf);
        } else {
            DomainOptionProto.DomainOption.IntermediateConcept.Builder iConceptBuilder = DomainOptionProto.DomainOption.IntermediateConcept
                    .newBuilder();
            Optional.ofNullable(commonObj.getName()).ifPresent(iConceptBuilder::setName);
            for (ConceptNode subNode : commonObj.getConceptNode()) {
                Optional.ofNullable(convertToProto(subNode)).ifPresent(iConceptBuilder::addSubConcepts);
            }
            builder.setIntermediateConcept(iConceptBuilder);
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static DomainOptionPermissionsProto.DomainOptionPermissions convertToProto(
            DomainOptionPermissions commonObj) {
        if (commonObj == null) {
            return null;
        }

        DomainOptionPermissionsProto.DomainOptionPermissions.Builder builder = DomainOptionPermissionsProto.DomainOptionPermissions
                .newBuilder();
        Optional.ofNullable(commonObj.isOwner()).ifPresent(builder::setIsOwner);
        Optional.ofNullable(commonObj.getUser()).ifPresent(builder::setUser);

        if (commonObj.getPermission() != null) {
            builder.setPermissionType(commonObj.getPermission().getName());
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static DomainSessionProto.DomainSession convertToProto(DomainSession commonObj) {
        if (commonObj == null) {
            return null;
        }

        DomainSessionProto.DomainSession.Builder builder = DomainSessionProto.DomainSession.newBuilder();
        Optional.ofNullable(commonObj.getDomainSessionId()).ifPresent(builder::setDomainSessionId);

        String domainSourceId = commonObj.getDomainSourceId();
        if (domainSourceId.endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION)) {
            // remove the course XML file extension
            domainSourceId = domainSourceId.substring(0,
                    domainSourceId.lastIndexOf(AbstractSchemaHandler.COURSE_FILE_EXTENSION));
        }

        // now remove file path
        int indexSlash = domainSourceId.lastIndexOf("\\");
        if (indexSlash == -1) {
            indexSlash = domainSourceId.lastIndexOf("/");
        }

        if (indexSlash != -1 && domainSourceId.length() > indexSlash) {
            domainSourceId = domainSourceId.substring(indexSlash + 1);
        }

        Optional.ofNullable(domainSourceId).ifPresent(builder::setDomainName);
        Optional.ofNullable(commonObj.getUsername()).ifPresent(builder::setUsername);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PerformanceStateAttributeProto.PerformanceStateAttribute convertToProto(
            PerformanceStateAttribute commonObj) {
        if (commonObj == null) {
            return null;
        }

        PerformanceStateAttributeProto.PerformanceStateAttribute.Builder builder = PerformanceStateAttributeProto.PerformanceStateAttribute
                .newBuilder();

        Optional.ofNullable(commonObj.getNodeId()).ifPresent(builder::setId);
        Optional.ofNullable(commonObj.getPriority()).ifPresent(builder::setPriority);
        Optional.ofNullable(commonObj.getShortTermTimestamp()).ifPresent(builder::setShortTermTimestamp);
        Optional.ofNullable(commonObj.getLongTermTimestamp()).ifPresent(builder::setLongTermTimestamp);
        Optional.ofNullable(commonObj.getPredictedTimestamp()).ifPresent(builder::setPredictedTimestamp);
        Optional.ofNullable(commonObj.getConfidence()).ifPresent(builder::setConfidence);
        Optional.ofNullable(commonObj.getCompetence()).ifPresent(builder::setCompetence);
        Optional.ofNullable(commonObj.getTrend()).ifPresent(builder::setTrend);
        Optional.ofNullable(commonObj.isAssessmentHold()).ifPresent(builder::setAssessmentHold);
        Optional.ofNullable(commonObj.isPriorityHold()).ifPresent(builder::setPriorityHold);
        Optional.ofNullable(commonObj.isTrendHold()).ifPresent(builder::setTrendHold);
        Optional.ofNullable(commonObj.isScenarioSupportNode()).ifPresent(builder::setScenarioSupport);
        Optional.ofNullable(commonObj.getName()).ifPresent(builder::setName);
        Optional.ofNullable(commonObj.getNodeCourseId()).ifPresent(builder::setCourseId);
        Optional.ofNullable(commonObj.getShortTerm().toString()).ifPresent(builder::setShortTerm);
        Optional.ofNullable(commonObj.getLongTerm().toString()).ifPresent(builder::setLongTerm);
        Optional.ofNullable(commonObj.getPredicted().toString()).ifPresent(builder::setPredicted);
        Optional.ofNullable(commonObj.getNodeStateEnum().getName()).ifPresent(builder::setNodeState);
        Optional.ofNullable(commonObj.getEvaluator()).ifPresent(builder::setEvaluator);
        Optional.ofNullable(commonObj.getObserverComment()).ifPresent(builder::setObserverComment);
        Optional.ofNullable(commonObj.getObserverMedia()).ifPresent(builder::setObserverMedia);

        if (commonObj.getAssessmentExplanation() != null) {
            for (String explanation : commonObj.getAssessmentExplanation()) {
                Optional.ofNullable(explanation).ifPresent(builder::addAssessmentExplanation);
            }
        }

        if (commonObj.getAssessedTeamOrgEntities() != null) {
            for (Map.Entry<String, AssessmentLevelEnum> entity : commonObj.getAssessedTeamOrgEntities().entrySet()) {
                builder.putAssessedTeamOrgEntries(entity.getKey(), entity.getValue().getName());
            }
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static TaskPerformanceStateProto.TaskPerformanceState convertToProto(TaskPerformanceState commonObj) {
        if (commonObj == null) {
            return null;
        }

        TaskPerformanceStateProto.TaskPerformanceState.Builder builder = TaskPerformanceStateProto.TaskPerformanceState
                .newBuilder();
        Optional.ofNullable(commonObj.isContainsObservedAssessmentCondition())
                .ifPresent(builder::setHasObservedAssessment);
        Optional.ofNullable(convertToProto(commonObj.getState())).ifPresent(builder::setState);

        if (commonObj.getConcepts() != null) {
            for (ConceptPerformanceState concept : commonObj.getConcepts()) {
                Optional.ofNullable(convertToProto(concept)).ifPresent(builder::addConcepts);
            }
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static TaskPerformanceStateProto.PerformanceStateAttributeType convertToProto(
            AbstractPerformanceStateAttribute commonObj) {
        if (commonObj == null) {
            return null;
        }

        TaskPerformanceStateProto.PerformanceStateAttributeType.Builder builder = TaskPerformanceStateProto.PerformanceStateAttributeType
                .newBuilder();

        if (commonObj instanceof PerformanceStateAttribute) {
            Optional.ofNullable(convertToProto((PerformanceStateAttribute) commonObj)).ifPresent(builder::setPerformanceStateAttribute);
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static TaskPerformanceStateProto.PerformanceStateConceptType convertToProto(
            AbstractPerformanceState commonObj) {
        if (commonObj == null) {
            return null;
        }

        TaskPerformanceStateProto.PerformanceStateConceptType.Builder builder = TaskPerformanceStateProto.PerformanceStateConceptType
                .newBuilder();

        if (commonObj instanceof ConceptPerformanceState) {
            Optional.ofNullable(convertToProto((ConceptPerformanceState) commonObj))
                    .ifPresent(builder::setConceptPerformanceState);
        } else if (commonObj instanceof IntermediateConceptPerformanceState) {
            Optional.ofNullable(convertToProto((IntermediateConceptPerformanceState) commonObj))
                    .ifPresent(builder::setIntermediateConceptPerformanceState);
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static TaskPerformanceStateProto.ConceptPerformanceState convertToProto(ConceptPerformanceState commonObj) {
        if (commonObj == null) {
            return null;
        }

        TaskPerformanceStateProto.ConceptPerformanceState.Builder builder = TaskPerformanceStateProto.ConceptPerformanceState
                .newBuilder();
        Optional.ofNullable(commonObj.isContainsObservedAssessmentCondition())
                .ifPresent(builder::setHasObservedAssessment);
        Optional.ofNullable(convertToProto(commonObj.getState())).ifPresent(builder::setPerformanceStateAttribute);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static TaskPerformanceStateProto.IntermediateConceptPerformanceState convertToProto(
            IntermediateConceptPerformanceState commonObj) {
        if (commonObj == null) {
            return null;
        }

        TaskPerformanceStateProto.IntermediateConceptPerformanceState.Builder builder = TaskPerformanceStateProto.IntermediateConceptPerformanceState
                .newBuilder();
        Optional.ofNullable(commonObj.isContainsObservedAssessmentCondition())
                .ifPresent(builder::setHasObservedAssessment);
        Optional.ofNullable(convertToProto(commonObj.getState())).ifPresent(builder::setState);

        if (commonObj.getConcepts() != null) {
            for (ConceptPerformanceState concept : commonObj.getConcepts()) {
                Optional.ofNullable(convertToProto(concept)).ifPresent(builder::addConcepts);
            }
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PerformanceStateProto.PerformanceState convertToProto(PerformanceState commonObj) {
        if (commonObj == null) {
            return null;
        }

        PerformanceStateProto.PerformanceState.Builder builder = PerformanceStateProto.PerformanceState.newBuilder();

        if (commonObj.getTasks() != null) {
            for (Map.Entry<Integer, TaskPerformanceState> task : commonObj.getTasks().entrySet()) {
                builder.putTasks(task.getKey(), convertToProto(task.getValue()));
            }
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static LearnerStateAttributeProto.LearnerStateAttribute convertToProto(LearnerStateAttribute commonObj) {
        if (commonObj == null) {
            return null;
        }

        LearnerStateAttributeProto.LearnerStateAttribute.Builder builder = LearnerStateAttributeProto.LearnerStateAttribute
                .newBuilder();
        Optional.ofNullable(commonObj.getShortTermTimestamp()).ifPresent(builder::setShortTermTimestamp);
        Optional.ofNullable(commonObj.getLongTermTimestamp()).ifPresent(builder::setLongTermTimestamp);
        Optional.ofNullable(commonObj.getPredictedTimestamp()).ifPresent(builder::setPredictedTimestamp);
        Optional.ofNullable(commonObj.getName().toString()).ifPresent(builder::setName);
        Optional.ofNullable(commonObj.getShortTerm().toString()).ifPresent(builder::setShortTerm);
        Optional.ofNullable(commonObj.getLongTerm().toString()).ifPresent(builder::setLongTerm);
        Optional.ofNullable(commonObj.getPredicted().toString()).ifPresent(builder::setPredicted);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static LearnerStateAttributeCollectionProto.LearnerStateAttributeCollection convertToProto(
            LearnerStateAttributeCollection commonObj) {
        if (commonObj == null) {
            return null;
        }

        LearnerStateAttributeCollectionProto.LearnerStateAttributeCollection.Builder builder = LearnerStateAttributeCollectionProto.LearnerStateAttributeCollection
                .newBuilder();
        Optional.ofNullable(commonObj.getName().toString()).ifPresent(builder::setName);
        Optional.ofNullable(commonObj.getShortTerm().toString()).ifPresent(builder::setShortTerm);
        Optional.ofNullable(commonObj.getLongTerm().toString()).ifPresent(builder::setLongTerm);
        Optional.ofNullable(commonObj.getPredicted().toString()).ifPresent(builder::setPredicted);

        if (commonObj.getAttributes() != null) {
            for (Map.Entry<String, LearnerStateAttribute> attribute : commonObj.getAttributes().entrySet()) {
                builder.putAttributeCollection(attribute.getKey(), convertToProto(attribute.getValue()));
            }
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static CognitiveStateProto.CognitiveState convertToProto(CognitiveState commonObj) {
        if (commonObj == null) {
            return null;
        }

        CognitiveStateProto.CognitiveState.Builder builder = CognitiveStateProto.CognitiveState.newBuilder();

        if (commonObj.getAttributes() != null) {
            for (Map.Entry<LearnerStateAttributeNameEnum, LearnerStateAttribute> attribute : commonObj.getAttributes()
                    .entrySet()) {
                builder.putAttributeCollection(attribute.getKey().getName(), convertToProto(attribute.getValue()));
            }
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static AffectiveStateProto.AffectiveState convertToProto(AffectiveState commonObj) {
        if (commonObj == null) {
            return null;
        }

        AffectiveStateProto.AffectiveState.Builder builder = AffectiveStateProto.AffectiveState.newBuilder();

        if (commonObj.getAttributes() != null) {
            for (Map.Entry<LearnerStateAttributeNameEnum, LearnerStateAttribute> attribute : commonObj.getAttributes()
                    .entrySet()) {
                builder.putAttributeCollection(attribute.getKey().getName(), convertToProto(attribute.getValue()));
            }
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @param domainSessionId The id of the domain session that sent the
     *        {@link LearnerState}.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static LearnerStateProto.LearnerState convertToProto(LearnerState commonObj, int domainSessionId) {
        if (commonObj == null) {
            return null;
        }

        LearnerStateProto.LearnerState.Builder builder = LearnerStateProto.LearnerState.newBuilder()
                .setDomainSessionId(domainSessionId);

        Optional.ofNullable(convertToProto(commonObj.getPerformance())).ifPresent(builder::setPerformanceState);
        Optional.ofNullable(convertToProto(commonObj.getCognitive())).ifPresent(builder::setCognitiveState);
        Optional.ofNullable(convertToProto(commonObj.getAffective())).ifPresent(builder::setAffectiveState);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @param domainSessionId The id of the domain session that sent the
     *        {@link AuthorizeStrategiesRequest}.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static AuthorizeStrategiesRequestProto.AuthorizeStrategiesRequest convertToProto(
            AuthorizeStrategiesRequest commonObj, int domainSessionId) {
        if (commonObj == null) {
            return null;
        }

        AuthorizeStrategiesRequestProto.AuthorizeStrategiesRequest.Builder builder = AuthorizeStrategiesRequestProto.AuthorizeStrategiesRequest
                .newBuilder().setDomainSessionId(domainSessionId);
        Optional.ofNullable(commonObj.getEvaluator()).ifPresent(builder::setEvaluator);

        for (Entry<String, List<StrategyToApply>> mapEntry : commonObj.getRequests().entrySet()) {
            
            AuthorizeStrategiesRequestProto.StrategyList.Builder strategyListBuilder = AuthorizeStrategiesRequestProto.StrategyList.newBuilder();
            for(StrategyToApply strategyToApply : mapEntry.getValue()) {
                // Note: currently only translating strategy, StrategyToApply is new used object here for #5174 Feb 2022
                StrategyProto.Strategy strategyProto = convertToProto(strategyToApply.getStrategy());
                strategyListBuilder.addStrategy(strategyProto);
            }
            builder.putRequests(mapEntry.getKey(), strategyListBuilder.build());
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @param domainSessionId The id of the domain session that sent the
     *        {@link CloseDomainSessionRequest}.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static CloseDomainSessionRequestProto.CloseDomainSessionRequest convertToProto(
            CloseDomainSessionRequest commonObj, int domainSessionId) {
        if (commonObj == null) {
            return null;
        }

        CloseDomainSessionRequestProto.CloseDomainSessionRequest.Builder builder = CloseDomainSessionRequestProto.CloseDomainSessionRequest
                .newBuilder().setDomainSessionId(domainSessionId);
        Optional.ofNullable(commonObj.getReason()).ifPresent(builder::setReason);
        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static AuthorizeStrategiesRequestProto.StrategyList convertToProto(List<Strategy> commonObj) {
        if (commonObj == null) {
            return null;
        }

        AuthorizeStrategiesRequestProto.StrategyList.Builder builder = AuthorizeStrategiesRequestProto.StrategyList
                .newBuilder();

        for (Strategy strategy : commonObj) {
            Optional.ofNullable(convertToProto(strategy)).ifPresent(builder::addStrategy);
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static StringPayloadProto.StringPayload convertToProto(String commonObj) {
        if (commonObj == null) {
            return null;
        }

        final StringPayloadProto.StringPayload.Builder builder = StringPayloadProto.StringPayload.newBuilder();
        Optional.ofNullable(commonObj).ifPresent(builder::setStringPayloadProperty);
        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     *  @param domainSessionId The id of the domain session that sent the
     *        {@link TutorUserInterfaceFeedback}.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static TutorUserInterfaceFeedbackPayload convertToProto(TutorUserInterfaceFeedback commonObj, int domainSessionId) {
        if (commonObj == null) {
            return null;
        }

        TutorUserInterfaceFeedbackPayload.Builder builder = TutorUserInterfaceFeedbackPayload.newBuilder()
                .setDomainSessionId(domainSessionId);

        Optional.ofNullable(convertToProto(commonObj.getDisplayTextAction())).ifPresent(builder::setDisplayTextAction);
        Optional.ofNullable(convertToProto(commonObj.getPlayAudioAction())).ifPresent(builder::setPlayAudioAction);
        Optional.ofNullable(convertToProto(commonObj.getDisplayAvatarAction()))
                .ifPresent(builder::setDisplayAvatarAction);
        Optional.ofNullable(convertToProto(commonObj.getClearTextAction())).ifPresent(builder::setClearTextAction);
        Optional.ofNullable(convertToProto(commonObj.getDisplayHTMLAction()))
                .ifPresent(builder::setDisplayHTMLFeedbackAction);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static DisplayTextActionProto.DisplayTextAction convertToProto(DisplayTextAction commonObj) {
        if (commonObj == null) {
            return null;
        }

        DisplayTextActionProto.DisplayTextAction.Builder builder = DisplayTextActionProto.DisplayTextAction
                .newBuilder();
        Optional.ofNullable(commonObj.getDisplayedText()).ifPresent(builder::setText);
        Optional.ofNullable(convertToProto(commonObj.getDeliverySettings())).ifPresent(builder::setDeliverySettings);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PlayAudioActionProto.PlayAudioAction convertToProto(PlayAudioAction commonObj) {
        if (commonObj == null) {
            return null;
        }

        PlayAudioActionProto.PlayAudioAction.Builder builder = PlayAudioActionProto.PlayAudioAction.newBuilder();
        Optional.ofNullable(commonObj.getMp3AudioFile()).ifPresent(builder::setMP3File);
        Optional.ofNullable(commonObj.getOggAudioFile()).ifPresent(builder::setOGGFile);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static AvatarDataProto.AvatarData convertToProto(AvatarData commonObj) {
        if (commonObj == null) {
            return null;
        }

        AvatarDataProto.AvatarData.Builder builder = AvatarDataProto.AvatarData.newBuilder();
        Optional.ofNullable(commonObj.getURL()).ifPresent(builder::setUrl);
        Optional.ofNullable(commonObj.getHeight()).ifPresent(builder::setHeight);
        Optional.ofNullable(commonObj.getWidth()).ifPresent(builder::setWidth);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static DisplayAvatarActionProto.DisplayAvatarAction convertToProto(DisplayAvatarAction commonObj) {
        if (commonObj == null) {
            return null;
        }

        DisplayAvatarActionProto.DisplayAvatarAction.Builder builder = DisplayAvatarActionProto.DisplayAvatarAction
                .newBuilder();

        if (commonObj instanceof DisplayTextToSpeechAvatarAction) {
            Optional.ofNullable(convertToProto((DisplayTextToSpeechAvatarAction) commonObj))
                    .ifPresent(builder::setDisplayTextToSpeechAvatarAction);
        } else if (commonObj instanceof DisplayScriptedAvatarAction) {
            Optional.ofNullable(convertToProto((DisplayScriptedAvatarAction) commonObj))
                    .ifPresent(builder::setDisplayScriptedAvatarAction);
        }

        Optional.ofNullable(convertToProto(commonObj.getAvatar())).ifPresent(builder::setAvatar);
        Optional.ofNullable(commonObj.isPreloadOnly()).ifPresent(builder::setPreloadOnly);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static DisplayAvatarActionProto.DisplayTextToSpeechAvatarAction convertToProto(
            DisplayTextToSpeechAvatarAction commonObj) {
        if (commonObj == null) {
            return null;
        }

        DisplayAvatarActionProto.DisplayTextToSpeechAvatarAction.Builder builder = DisplayAvatarActionProto.DisplayTextToSpeechAvatarAction
                .newBuilder();
        Optional.ofNullable(commonObj.getText()).ifPresent(builder::setText);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static DisplayAvatarActionProto.DisplayScriptedAvatarAction convertToProto(
            DisplayScriptedAvatarAction commonObj) {
        if (commonObj == null) {
            return null;
        }

        DisplayAvatarActionProto.DisplayScriptedAvatarAction.Builder builder = DisplayAvatarActionProto.DisplayScriptedAvatarAction
                .newBuilder();
        Optional.ofNullable(commonObj.getAction()).ifPresent(builder::setKey);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static ClearTextActionProto.ClearTextAction convertToProto(ClearTextAction commonObj) {
        if (commonObj == null) {
            return null;
        }

        return ClearTextActionProto.ClearTextAction.newBuilder().build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static DisplayHTMLFeedbackActionProto.DisplayHTMLFeedbackAction convertToProto(
            DisplayHTMLFeedbackAction commonObj) {
        if (commonObj == null) {
            return null;
        }

        DisplayHTMLFeedbackActionProto.DisplayHTMLFeedbackAction.Builder builder = DisplayHTMLFeedbackActionProto.DisplayHTMLFeedbackAction
                .newBuilder();
        Optional.ofNullable(commonObj.getDomainURL()).ifPresent(builder::setUrl);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static EnvironmentControlProto.EnvironmentControl convertToProto(EnvironmentControl commonObj) {
        if (commonObj == null) {
            return null;
        }

        EnvironmentControlProto.EnvironmentControl.Builder builder = EnvironmentControlProto.EnvironmentControl
                .newBuilder();
        Optional.ofNullable(convertToProto(commonObj.getEnvironmentStatusType()))
                .ifPresent(builder::setEnvironmentStatusType);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PedRequestInstructionalInterventionProto.PedRequestInstructionalIntervention convertToProto(
            RequestInstructionalIntervention commonObj) {
        if (commonObj == null) {
            return null;
        }

        PedRequestInstructionalInterventionProto.PedRequestInstructionalIntervention.Builder builder = PedRequestInstructionalInterventionProto.PedRequestInstructionalIntervention
                .newBuilder();

        Optional.ofNullable(commonObj.getDelayAfterStrategy()).ifPresent(builder::setWaitTime);
        Optional.ofNullable(commonObj.getStrategyName()).ifPresent(builder::setStrategyName);
        Optional.ofNullable(commonObj.getReasonForRequest()).ifPresent(builder::setReasonForRequest);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PedRequestMidLessonMediaProto.PedRequestMidLessonMedia convertToProto(RequestMidLessonMedia commonObj) {
        if (commonObj == null) {
            return null;
        }

        PedRequestMidLessonMediaProto.PedRequestMidLessonMedia.Builder builder = PedRequestMidLessonMediaProto.PedRequestMidLessonMedia
                .newBuilder();

        Optional.ofNullable(commonObj.getDelayAfterStrategy()).ifPresent(builder::setWaitTime);
        Optional.ofNullable(commonObj.getStrategyName()).ifPresent(builder::setStrategyName);
        Optional.ofNullable(commonObj.getReasonForRequest()).ifPresent(builder::setReasonForRequest);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PedRequestPerformanceAssessmentProto.PedRequestPerformanceAssessment convertToProto(
            RequestPerformanceAssessment commonObj) {
        if (commonObj == null) {
            return null;
        }

        PedRequestPerformanceAssessmentProto.PedRequestPerformanceAssessment.Builder builder = PedRequestPerformanceAssessmentProto.PedRequestPerformanceAssessment
                .newBuilder();

        Optional.ofNullable(commonObj.getDelayAfterStrategy()).ifPresent(builder::setWaitTime);
        Optional.ofNullable(commonObj.getStrategyName()).ifPresent(builder::setStrategyName);
        Optional.ofNullable(commonObj.getReasonForRequest()).ifPresent(builder::setReasonForRequest);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PedRequestScenarioAdaptationProto.PedRequestScenarioAdaptation convertToProto(
            RequestScenarioAdaptation commonObj) {
        if (commonObj == null) {
            return null;
        }

        PedRequestScenarioAdaptationProto.PedRequestScenarioAdaptation.Builder builder = PedRequestScenarioAdaptationProto.PedRequestScenarioAdaptation
                .newBuilder();

        Optional.ofNullable(commonObj.getDelayAfterStrategy()).ifPresent(builder::setWaitTime);
        Optional.ofNullable(commonObj.getStrategyName()).ifPresent(builder::setStrategyName);
        Optional.ofNullable(commonObj.getReasonForRequest()).ifPresent(builder::setReasonForRequest);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static BranchAdaptationStrategyProto.BranchAdaptationStrategy convertToProto(BranchAdaptationStrategy commonObj) {
        if (commonObj == null) {
            return null;
        }

        BranchAdaptationStrategyProto.BranchAdaptationStrategy.Builder builder = BranchAdaptationStrategyProto.BranchAdaptationStrategy
                .newBuilder();

        try {
            BranchAdpatationStrategyTypeInterface type = commonObj.getStrategyType();

            if (type instanceof ProgressionInfo) {
                builder.setType(ProgressionInfo.class.getName());

                for (MetadataAttributeItem item : ((ProgressionInfo) type).getAttributes()) {
                    Optional.ofNullable(convertToProto(item)).ifPresent(builder::addAttributes);
                }

                final MerrillQuadrantEnum quadInfo = ((QuadrantInfo) type).getQuadrant();
                if (quadInfo != null) {
                    Optional.ofNullable(quadInfo.getName()).ifPresent(builder::setNextQuadrant);
                }
            } else if (type instanceof RemediationInfo) {
                builder.setType(RemediationInfo.class.getName());

                final RemediationInfo remediationInfo = (RemediationInfo) type;

                Optional.ofNullable(remediationInfo.isAfterPractice()).ifPresent(builder::setAfterPractice);

                for (Map.Entry<String, List<AbstractRemediationConcept>> remediation : remediationInfo
                        .getRemediationMap().entrySet()) {

                    BranchAdaptationStrategyProto.BranchAdaptationStrategy.ListRemediationConcept.Builder listBuilder = BranchAdaptationStrategyProto.BranchAdaptationStrategy.ListRemediationConcept
                            .newBuilder();

                    for (AbstractRemediationConcept concept : remediation.getValue()) {
                        Optional.ofNullable(convertToProto(concept)).ifPresent(listBuilder::addRemediationConcepts);
                    }

                    builder.putRemediation(remediation.getKey(), listBuilder.build());
                }
            } else if (type instanceof AdvancementInfo) {
                builder.setType(AdvancementInfo.class.getName());
                final AdvancementInfo advancementInfo = (AdvancementInfo) type;

                Optional.ofNullable(advancementInfo.isSkill()).ifPresent(builder::setIsSkill);

                for (AdvancementConcept concept : advancementInfo.getConcepts()) {
                    Optional.ofNullable(convertToProto(concept)).ifPresent(builder::addAdvancement);
                }
            } else {
                throw new IllegalArgumentException("Found unhandled strategy type of " + type + ".");
            }
        } catch (Exception e) {
            logger.error("Caught exception while encoding the metadata item list.", e);
            throw new MessageEncodeException(BranchAdaptationStrategy.class.getName(),
                    "There was a problem encoding the metadata item list");
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static BranchAdaptationStrategyProto.MetadataAttributeItem convertToProto(MetadataAttributeItem commonObj)
            throws SAXException, JAXBException {
        if (commonObj == null) {
            return null;
        }

        BranchAdaptationStrategyProto.MetadataAttributeItem.Builder builder = BranchAdaptationStrategyProto.MetadataAttributeItem
                .newBuilder();
        Optional.ofNullable(commonObj.getPriority()).ifPresent(builder::setPriority);
        Optional.ofNullable(AbstractMetadataFileHandler.getRawAttribute(commonObj.getAttribute()))
                .ifPresent(builder::setAttribute);
        Optional.ofNullable(commonObj.getLabel()).ifPresent(builder::setLabel);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static BranchAdaptationStrategyProto.RemediationConcept convertToProto(AbstractRemediationConcept commonObj)
            throws SAXException, JAXBException {

        if (commonObj == null) {
            return null;
        }

        BranchAdaptationStrategyProto.RemediationConcept.Builder builder = BranchAdaptationStrategyProto.RemediationConcept
                .newBuilder();

        if (commonObj instanceof PassiveRemediationConcept) {
            PassiveRemediationConcept passiveConcept = (PassiveRemediationConcept) commonObj;
            builder.setRemediationType(PassiveRemediationConcept.class.getName());
            Optional.ofNullable(passiveConcept.getConcept()).ifPresent(builder::setConcept);
            Optional.ofNullable(passiveConcept.getQuadrant().getName()).ifPresent(builder::setRemediationQuadrant);

            for (MetadataAttributeItem item : passiveConcept.getAttributes()) {
                Optional.ofNullable(convertToProto(item)).ifPresent(builder::addAttributes);
            }
        } else if (commonObj instanceof ActiveRemediationConcept) {
            ActiveRemediationConcept activeConcept = (ActiveRemediationConcept) commonObj;
            builder.setRemediationType(ActiveRemediationConcept.class.getName());
            Optional.ofNullable(activeConcept.getConcept()).ifPresent(builder::setConcept);
        } else if (commonObj instanceof ConstructiveRemediationConcept) {
            ConstructiveRemediationConcept constructiveConcept = (ConstructiveRemediationConcept) commonObj;
            builder.setRemediationType(ConstructiveRemediationConcept.class.getName());
            Optional.ofNullable(constructiveConcept.getConcept()).ifPresent(builder::setConcept);
        } else if (commonObj instanceof InteractiveRemediationConcept) {
            InteractiveRemediationConcept interactiveConcept = (InteractiveRemediationConcept) commonObj;
            builder.setRemediationType(InteractiveRemediationConcept.class.getName());
            Optional.ofNullable(interactiveConcept.getConcept()).ifPresent(builder::setConcept);
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static BranchAdaptationStrategyProto.AdvancementConcept convertToProto(AdvancementConcept commonObj) {
        if (commonObj == null) {
            return null;
        }

        BranchAdaptationStrategyProto.AdvancementConcept.Builder builder = BranchAdaptationStrategyProto.AdvancementConcept
                .newBuilder();
        Optional.ofNullable(commonObj.getConcept()).ifPresent(builder::setConcept);
        Optional.ofNullable(commonObj.getReason()).ifPresent(builder::setAdvancementReason);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PedRequestBranchAdaptationProto.PedRequestBranchAdaptation convertToProto(RequestBranchAdaptation commonObj) {
        if (commonObj == null) {
            return null;
        }

        PedRequestBranchAdaptationProto.PedRequestBranchAdaptation.Builder builder = PedRequestBranchAdaptationProto.PedRequestBranchAdaptation
                .newBuilder();
        Optional.ofNullable(convertToProto(commonObj.getStrategy())).ifPresent(builder::setStrategy);
        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PedRequestDoNothingStrategyProto.PedRequestDoNothingStrategy convertToProto(
            RequestDoNothingTactic commonObj) {
        if (commonObj == null) {
            return null;
        }

        PedRequestDoNothingStrategyProto.PedRequestDoNothingStrategy.Builder builder = PedRequestDoNothingStrategyProto.PedRequestDoNothingStrategy
                .newBuilder();

        Optional.ofNullable(commonObj.getDelayAfterStrategy()).ifPresent(builder::setWaitTime);
        Optional.ofNullable(commonObj.getStrategyName()).ifPresent(builder::setStrategyName);
        Optional.ofNullable(commonObj.getReasonForRequest()).ifPresent(builder::setReasonForRequest);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PedagogicalRequestTypeProto.PedagogicalRequestType convertToProto(AbstractPedagogicalRequest commonObj) {
        if (commonObj == null) {
            return null;
        }

        PedagogicalRequestTypeProto.PedagogicalRequestType.Builder builder = PedagogicalRequestTypeProto.PedagogicalRequestType
                .newBuilder();

        try {
            if (commonObj instanceof RequestInstructionalIntervention) {
                Optional.ofNullable(convertToProto((RequestInstructionalIntervention) commonObj))
                        .ifPresent(builder::setPedRequestInstructionalIntervention);
            } else if (commonObj instanceof RequestMidLessonMedia) {
                Optional.ofNullable(convertToProto((RequestMidLessonMedia) commonObj))
                        .ifPresent(builder::setPedRequestMidLessonMedia);
            } else if (commonObj instanceof RequestPerformanceAssessment) {
                Optional.ofNullable(convertToProto((RequestPerformanceAssessment) commonObj))
                        .ifPresent(builder::setPedRequestPerformanceAssessment);
            } else if (commonObj instanceof RequestScenarioAdaptation) {
                Optional.ofNullable(convertToProto((RequestScenarioAdaptation) commonObj))
                        .ifPresent(builder::setPedRequestScenarioAdaptation);
            } else if (commonObj instanceof RequestBranchAdaptation) {
                Optional.ofNullable(convertToProto((RequestBranchAdaptation) commonObj))
                        .ifPresent(builder::setPedRequestBranchAdaptation);
            } else if (commonObj instanceof RequestDoNothingTactic) {
                Optional.ofNullable(convertToProto((RequestDoNothingTactic) commonObj))
                        .ifPresent(builder::setPedRequestDoNothingStrategy);
            } else {
                throw new MessageEncodeException(AbstractPedagogicalRequest.class.getName(),
                        "Found unhandled ped request of " + commonObj);
            }

            Optional.ofNullable(commonObj.isMacroRequest()).ifPresent(builder::setMacro);
            Optional.ofNullable(commonObj.getStrategyName()).ifPresent(builder::setStrategyName);
            Optional.ofNullable(commonObj.getDelayAfterStrategy()).ifPresent(builder::setWaitTime);
            Optional.ofNullable(commonObj.getReasonForRequest()).ifPresent(builder::setReason);

            return builder.build();
        } catch (Exception e) {
            logger.error("Caught exception while encoding " + AbstractPedagogicalRequest.class.getName(), e);
            throw new MessageEncodeException(AbstractPedagogicalRequest.class.getName(),
                    "There was a problem encoding the ped request");
        }
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @param domainSessionId The id of the domain session that sent the
     *        {@link PedagogicalRequest}.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static PedagogicalRequestProto.PedagogicalRequest convertToProto(PedagogicalRequest commonObj, int domainSessionId) {
        if (commonObj == null) {
            return null;
        }

        PedagogicalRequestProto.PedagogicalRequest.Builder builder = PedagogicalRequestProto.PedagogicalRequest
                .newBuilder().setDomainSessionId(domainSessionId);

        for (Map.Entry<String, List<AbstractPedagogicalRequest>> requestMap : commonObj.getRequests().entrySet()) {
            PedagogicalRequestProto.PedagogicalRequest.ListPedagogicalRequestType.Builder listBuilder = PedagogicalRequestProto.PedagogicalRequest.ListPedagogicalRequestType
                    .newBuilder();

            for (AbstractPedagogicalRequest request : requestMap.getValue()) {
                Optional.ofNullable(convertToProto(request)).ifPresent(listBuilder::addPedagogicalRequestType);
            }

            builder.putRequests(requestMap.getKey(), listBuilder.build());
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static SensorAttributeValueProto.SensorAttributeValue convertToProto(
            AbstractSensorAttributeValue commonObj) {
        if (commonObj == null) {
            return null;
        }

        SensorAttributeValueProto.SensorAttributeValue.Builder builder = SensorAttributeValueProto.SensorAttributeValue
                .newBuilder();

        try {
            if (commonObj instanceof DoubleValue) {
                Optional.ofNullable(convertToProto((DoubleValue) commonObj)).ifPresent(builder::setSensorDoubleValue);
            } else if (commonObj instanceof IntegerValue) {
                Optional.ofNullable(convertToProto((IntegerValue) commonObj)).ifPresent(builder::setSensorIntegerValue);
            } else if (commonObj instanceof StringValue) {
                Optional.ofNullable(convertToProto((StringValue) commonObj)).ifPresent(builder::setSensorStringValue);
            } else if (commonObj instanceof Tuple3dValue) {
                Optional.ofNullable(convertToProto((Tuple3dValue) commonObj)).ifPresent(builder::setSensorTuple3D);
            } else if (commonObj instanceof ImageValue) {
                Optional.ofNullable(convertToProto((ImageValue) commonObj)).ifPresent(builder::setSensorImageValue);
            } else {
                throw new MessageEncodeException(SensorAttributeValueProto.SensorAttributeValue.class.getName(),
                        "Found unhandled value of " + commonObj);
            }

            if (commonObj.getName() != null) {
                Optional.ofNullable(commonObj.getName().getName()).ifPresent(builder::setName);
            }

            return builder.build();
        } catch (Exception e) {
            logger.error("Caught exception while encoding "
                    + SensorAttributeValueProto.SensorAttributeValue.class.getName(), e);
            throw new MessageEncodeException(
                    SensorAttributeValueProto.SensorAttributeValue.class.getName(),
                    "There was a problem encoding the value");
        }
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static SensorAttributeValueProto.SensorDoubleValue convertToProto(DoubleValue commonObj) {
        if (commonObj == null) {
            return null;
        }

        SensorAttributeValueProto.SensorDoubleValue.Builder builder = SensorAttributeValueProto.SensorDoubleValue
                .newBuilder();
        if (commonObj.getNumber() != null) {
            builder.setValue(commonObj.getNumber().doubleValue());
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static SensorAttributeValueProto.SensorIntegerValue convertToProto(IntegerValue commonObj) {
        if (commonObj == null) {
            return null;
        }

        SensorAttributeValueProto.SensorIntegerValue.Builder builder = SensorAttributeValueProto.SensorIntegerValue
                .newBuilder();
        if (commonObj.getNumber() != null) {
            builder.setValue(commonObj.getNumber().intValue());
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static SensorAttributeValueProto.SensorStringValue convertToProto(StringValue commonObj) {
        if (commonObj == null) {
            return null;
        }

        SensorAttributeValueProto.SensorStringValue.Builder builder = SensorAttributeValueProto.SensorStringValue
                .newBuilder();
        Optional.ofNullable(commonObj.getString()).ifPresent(builder::setValue);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static SensorAttributeValueProto.SensorTuple3d convertToProto(Tuple3dValue commonObj) {
        if (commonObj == null) {
            return null;
        }

        SensorAttributeValueProto.SensorTuple3d.Builder builder = SensorAttributeValueProto.SensorTuple3d.newBuilder();
        Optional.ofNullable(convertToProto(commonObj.getTuple3d())).ifPresent(builder::setValue);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static SensorAttributeValueProto.SensorImageValue convertToProto(ImageValue commonObj) {
        if (commonObj == null) {
            return null;
        }

        SensorAttributeValueProto.SensorImageValue.Builder builder = SensorAttributeValueProto.SensorImageValue
                .newBuilder();
        Optional.ofNullable(convertToProto(commonObj.getImageData())).ifPresent(builder::setValue);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static SensorAttributeValueProto.Tuple3d convertToProto(Tuple3d commonObj) {
        if (commonObj == null) {
            return null;
        }

        SensorAttributeValueProto.Tuple3d.Builder builder = SensorAttributeValueProto.Tuple3d
                .newBuilder();

        if (commonObj instanceof Vector3d) {
            Optional.ofNullable(convertToProto((Vector3d) commonObj)).ifPresent(builder::setVector3D);
        } else if (commonObj instanceof Point3d) {
            Optional.ofNullable(convertToProto((Point3d) commonObj)).ifPresent(builder::setPoint3D);
        } else {
            throw new MessageEncodeException(SensorAttributeValueProto.Tuple3d.class.getName(),
                    "Found unhandled value of " + commonObj);
        }

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static SensorAttributeValueProto.Vector3d convertToProto(Vector3d commonObj) {
        if (commonObj == null) {
            return null;
        }

        SensorAttributeValueProto.Vector3d.Builder builder = SensorAttributeValueProto.Vector3d
                .newBuilder();

        Optional.ofNullable(commonObj.getX()).ifPresent(builder::setX);
        Optional.ofNullable(commonObj.getY()).ifPresent(builder::setY);
        Optional.ofNullable(commonObj.getZ()).ifPresent(builder::setZ);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static SensorAttributeValueProto.Point3d convertToProto(Point3d commonObj) {
        if (commonObj == null) {
            return null;
        }

        SensorAttributeValueProto.Point3d.Builder builder = SensorAttributeValueProto.Point3d
                .newBuilder();

        Optional.ofNullable(commonObj.getX()).ifPresent(builder::setX);
        Optional.ofNullable(commonObj.getY()).ifPresent(builder::setY);
        Optional.ofNullable(commonObj.getZ()).ifPresent(builder::setZ);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static SensorAttributeValueProto.ImageData convertToProto(ImageData commonObj) {
        if (commonObj == null) {
            return null;
        }

        SensorAttributeValueProto.ImageData.Builder builder = SensorAttributeValueProto.ImageData
                .newBuilder();

        Optional.ofNullable(commonObj.getWidth()).ifPresent(builder::setWidth);
        Optional.ofNullable(commonObj.getHeight()).ifPresent(builder::setHeight);
        Optional.ofNullable(Base64.getEncoder().encodeToString(commonObj.getData())).ifPresent(builder::setData);
        Optional.ofNullable(commonObj.getFormat().getName()).ifPresent(builder::setFormat);

        return builder.build();
    }

    /**
     * Convert the GIFT common object into the protobuf version.
     *
     * @param commonObj the GIFT common object to convert.
     * @return the protobuf version of the GIFT common object. Will be null if
     *         the provided GIFT common object is null.
     */
    public static FilteredSensorDataProto.FilteredSensorData convertToProto(FilteredSensorData commonObj) {
        if (commonObj == null) {
            return null;
        }

        FilteredSensorDataProto.FilteredSensorData.Builder builder = FilteredSensorDataProto.FilteredSensorData
                .newBuilder();

        Optional.ofNullable(commonObj.getElapsedTime()).ifPresent(builder::setElapsedTime);
        Optional.ofNullable(commonObj.getSensorName()).ifPresent(builder::setSensorName);
        Optional.ofNullable(commonObj.getSensorType().getName()).ifPresent(builder::setSensorType);
        Optional.ofNullable(commonObj.getFilterName()).ifPresent(builder::setFilterName);

        for (Entry<SensorAttributeNameEnum, AbstractSensorAttributeValue> values : commonObj.getAttributeValues()
                .entrySet()) {
            builder.putAttributeValues(values.getKey().getName(), convertToProto(values.getValue()));
        }

        return builder.build();
    }

    /**
     * Converts the time in milliseconds to the protobuf Timestamp object.
     *
     * @param timeInMillis the time in milliseconds.
     * @return the protobuf Timestamp object.
     */
    public static Timestamp convertToProto(long timeInMillis) {
        return Timestamp.newBuilder().setSeconds(timeInMillis / 1000).setNanos((int) (timeInMillis % 1000 * 1000000))
                .build();
    }

    /**
     * Converts the time in milliseconds to the protobuf Timestamp object.
     *
     * @param teamRef the time in milliseconds.
     * @return the protobuf Timestamp object. Can be null if the provided
     *         parameter was null.
     */
    public static TeamRefProto.TeamRef convertToProto(TeamRef teamRef) {
        if (teamRef == null) {
            return null;
        }

        final TeamRefProto.TeamRef.Builder builder = TeamRefProto.TeamRef.newBuilder();
        Optional.ofNullable(teamRef.getValue()).ifPresent(builder::setReference);

        return builder.build();
    }

    /**
     * Convert from {@link Coordinate} to its equivalent protobuf type.
     *
     * @param coordinate The {@link Coordinate} to convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    public static CoordinateProto.Coordinate convertToProto(Coordinate coordinate) {
        if (coordinate == null) {
            return null;
        }

        CoordinateProto.Coordinate.Builder protoCoordinate = CoordinateProto.Coordinate.newBuilder();
        final Serializable type = coordinate.getType();
        if (type instanceof GCC) {
            final GCC gcc = (GCC) type;
            final long x = gcc.getX().longValue();
            final long y = gcc.getY().longValue();
            final long z = gcc.getZ().longValue();

            final CoordinateProto.Coordinate.GCC protoGcc = CoordinateProto.Coordinate.GCC.newBuilder()
                    .setX(x)
                    .setY(y)
                    .setZ(z)
                    .build();

            protoCoordinate.setGcc(protoGcc);
        } else if (type instanceof GDC) {
            final GDC gdc = (GDC) type;
            final long lat = gdc.getLatitude().longValue();
            final long lng = gdc.getLongitude().longValue();
            final long elevation = gdc.getElevation().longValue();

            final CoordinateProto.Coordinate.GDC protoGdc = CoordinateProto.Coordinate.GDC.newBuilder().setLatitude(lat)
                    .setLongitude(lng).setElevation(elevation).build();

            protoCoordinate.setGdc(protoGdc);
        } else if (type instanceof AGL) {
            final AGL agl = (AGL) type;
            final long x = agl.getX().longValue();
            final long y = agl.getY().longValue();
            final long elevation = agl.getElevation().longValue();

            CoordinateProto.Coordinate.AGL protoAgl = CoordinateProto.Coordinate.AGL.newBuilder()
                    .setX(x)
                    .setY(y)
                    .setElevation(elevation)
                    .build();

            protoCoordinate.setAgl(protoAgl);
        } else {
            final String typeName = type != null ? type.getClass().getName() : "null";
            throw new IllegalArgumentException(
                    "Unable to convert the '" + typeName + "' coordinate to an equivalent protobuf version.");
        }

        return protoCoordinate.build();
    }

    /**
     * Convert from {@link Strategy} to its equivalent protobuf type.
     *
     * @param strategy The {@link Strategy} to convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    public static StrategyProto.Strategy convertToProto(Strategy strategy) {
        if (strategy == null) {
            return null;
        }

        StrategyProto.Strategy.Builder builder = StrategyProto.Strategy.newBuilder();

        builder.setName(strategy.getName());

        for (Serializable activity : strategy.getStrategyActivities()) {
            final StrategyProto.Activity.Builder protoActivity = StrategyProto.Activity.newBuilder();
            if (activity instanceof InstructionalIntervention) {
                Optional.ofNullable(convertToProto((InstructionalIntervention) activity))
                        .ifPresent(protoActivity::setInstructionalIntervention);
            } else if (activity instanceof MidLessonMedia) {
                Optional.ofNullable(convertToProto((MidLessonMedia) activity))
                        .ifPresent(protoActivity::setMidLessonMedia);
            } else if (activity instanceof ScenarioAdaptation) {
                Optional.ofNullable(convertToProto((ScenarioAdaptation) activity))
                        .ifPresent(protoActivity::setScenarioAdaptation);
            } else if (activity instanceof PerformanceAssessment) {
                continue;
            } else {
                final String activityType = activity != null ? activity.getClass().getName() : "null";
                throw new IllegalArgumentException(
                        "An activity of type '" + activityType + "' cannot be converted to protobuf.");
            }

            builder.addActivities(protoActivity);
        }

        return builder.build();
    }

    /**
     * Convert from {@link InstructionalIntervention} to its equivalent protobuf
     * type.
     *
     * @param instructionalIntervention The {@link InstructionalIntervention} to
     *        convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static StrategyProto.InstructionalIntervention convertToProto(InstructionalIntervention instructionalIntervention) {
        if (instructionalIntervention == null) {
            return null;
        }

        long delay = 0;
        if (instructionalIntervention.getDelayAfterStrategy() != null
                && instructionalIntervention.getDelayAfterStrategy().getDuration() != null) {
            delay = instructionalIntervention.getDelayAfterStrategy().getDuration().longValue();
        }

        final Builder builder = StrategyProto.InstructionalIntervention.newBuilder().setDelay(delay);
        Optional.ofNullable(convertToProto(instructionalIntervention.getFeedback())).ifPresent(builder::setFeedback);
        return builder.build();
    }

    /**
     * Convert from {@link ToObserverController} to its equivalent protobuf
     * type.
     *
     * @param toObserverController The {@link ToObserverController} to convert.
     *        Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static StrategyProto.ToObserverController convertToProto(ToObserverController toObserverController) {
        if (toObserverController == null) {
            return null;
        }

        final StrategyProto.ToObserverController.Builder builder = StrategyProto.ToObserverController.newBuilder();
        Optional.ofNullable(toObserverController.getValue()).ifPresent(builder::setValue);
        return builder.build();
    }

    /**
     * Convert from {@link Feedback} to its equivalent protobuf type.
     *
     * @param feedback The {@link Feedback} to convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static StrategyProto.Feedback convertToProto(Feedback feedback) {
        if (feedback == null) {
            return null;
        }

        final long duration;
        if (feedback.getFeedbackDuration() != null) {
            duration = feedback.getFeedbackDuration().longValue();
        } else {
            duration = 0;
        }

        final Serializable presentation = feedback.getFeedbackPresentation();
        final StrategyProto.FeedbackPresentation.Builder protoPresentation = StrategyProto.FeedbackPresentation
                .newBuilder();
        if (presentation instanceof Message) {
            Optional.ofNullable(convertToProto((Message) presentation)).ifPresent(protoPresentation::setMessage);
        } else if (presentation instanceof Audio) {
            Optional.ofNullable(convertToProto((Audio) presentation)).ifPresent(protoPresentation::setAudio);
        } else if (presentation instanceof Feedback.File) {
            Optional.ofNullable(convertToProto((Feedback.File) presentation)).ifPresent(protoPresentation::setFile);
        } else {
            final String typeName = presentation != null ? presentation.getClass().getName() : "null";
            throw new IllegalArgumentException("Unable to convert '" + typeName + "' to an equivalent protobuf type.");
        }

        Iterable<TeamRefProto.TeamRef> teamRefs = feedback.getTeamRef()
                .stream()
                .map(ProtoToGIFTConverter::convertToProto)
                .collect(Collectors.toList());

        final StrategyProto.Feedback.Builder builder = StrategyProto.Feedback.newBuilder();
        Optional.ofNullable(feedback.getAffectiveFeedbackType()).ifPresent(builder::setAffectiveFeedbackType);
        Optional.ofNullable(feedback.getFeedbackSpecificityType()).ifPresent(builder::setFeedbackSpecificityType);

        return builder.addAllTeamRef(teamRefs).setDuration(duration).setPresentation(protoPresentation).build();
    }

    /**
     * Convert from {@link Message} to its equivalent protobuf type.
     *
     * @param feedback The {@link Message} to convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static StrategyProto.FeedbackMessage convertToProto(Message feedback) {
        if (feedback == null) {
            return null;
        }

        final StrategyProto.FeedbackMessage.Builder builder = StrategyProto.FeedbackMessage.newBuilder();

        Optional.ofNullable(feedback.getContent()).ifPresent(builder::setContent);

        Optional.ofNullable(feedback.getDelivery())
                .map(ProtoToGIFTConverter::convertToProto)
                .ifPresent(builder::setDelivery);

        return builder.build();
    }

    /**
     * Convert from {@link Delivery} to its equivalent protobuf type.
     *
     * @param delivery The {@link Delivery} to convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static StrategyProto.FeedbackMessage.Delivery convertToProto(Delivery delivery) {
        if (delivery == null) {
            return null;
        }

        final StrategyProto.FeedbackMessage.Delivery.Builder builder = StrategyProto.FeedbackMessage.Delivery.newBuilder();

        Optional.ofNullable(delivery.getInTrainingApplication())
                .map(ProtoToGIFTConverter::convertToProto)
                .ifPresent(builder::setInTrainingApplication);

        Optional.ofNullable(delivery.getInTutor())
                .map(ProtoToGIFTConverter::convertToProto)
                .ifPresent(builder::setInTutor);

        Optional.ofNullable(delivery.getToObserverController())
                .map(ProtoToGIFTConverter::convertToProto)
                .ifPresent(builder::setToObserverController);

        return builder.build();
    }

    /**
     * Convert from {@link InTrainingApplication} to its equivalent protobuf
     * type.
     *
     * @param inTrainingApp The {@link InTrainingApplication} to
     *        convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static StrategyProto.InTrainingApplication convertToProto(InTrainingApplication inTrainingApp) {
        if (inTrainingApp == null) {
            return null;
        }

        final StrategyProto.InTrainingApplication.Builder builder = StrategyProto.InTrainingApplication.newBuilder();

        final boolean isEnabled = inTrainingApp.getEnabled() == BooleanEnum.TRUE;
        builder.setEnabled(isEnabled);

        Optional.ofNullable(inTrainingApp.getMobileOption())
                .map(ProtoToGIFTConverter::convertToProto)
                .ifPresent(builder::setMobileOption);

        return builder.build();
    }

    /**
     * Convert from {@link InTutor} to its equivalent protobuf type.
     *
     * @param inTutor The {@link InTutor} to convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static InTutorProto.InTutor convertToProto(InTutor inTutor) {
        if (inTutor == null) {
            return null;
        }

        final InTutorProto.InTutor.Builder builder = InTutorProto.InTutor.newBuilder();

        Optional.ofNullable(inTutor.getMessagePresentation()).ifPresent(builder::setMessagePresentation);

        Optional.ofNullable(inTutor.getTextEnhancement()).ifPresent(builder::setTextEnhancement);

        return builder.build();
    }

    /**
     * Convert from {@link MobileOption} to its equivalent protobuf type.
     *
     * @param mobileOption The {@link MobileOption} to convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static StrategyProto.MobileOption convertToProto(MobileOption mobileOption) {
        if (mobileOption == null) {
            return null;
        }

        return StrategyProto.MobileOption.newBuilder()
                .setVibrate(mobileOption.isVibrate())
                .build();
    }

    /**
     * Converts an {@link Audio} to a protobuf version of itself.
     *
     * @param audio The {@link Audio} to convert.
     * @return The protobuf representation of the provided {@link Audio}. Can be
     *         null if the parameter is null.
     */
    private static StrategyProto.Audio convertToProto(Audio audio) {
        if (audio == null) {
            return null;
        }

        final StrategyProto.Audio.Builder builder = StrategyProto.Audio.newBuilder();

        Optional.ofNullable(audio.getMP3File()).ifPresent(builder::setMp3File);

        Optional.ofNullable(audio.getOGGFile()).ifPresent(builder::setOggFile);

        Optional.ofNullable(audio.getToObserverController())
                .map(ProtoToGIFTConverter::convertToProto)
                .ifPresent(builder::setToObserverController);

        return builder.build();
    }

    /**
     * Convert from {@link generated.dkf.Feedback.File} to its equivalent
     * protobuf type.
     *
     * @param feedbackFile The {@link generated.dkf.Feedback.File} to convert.
     *        Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static StrategyProto.FeedbackFile convertToProto(Feedback.File feedbackFile) {
        if (feedbackFile == null) {
            return null;
        }

        final StrategyProto.FeedbackFile.Builder builder = StrategyProto.FeedbackFile.newBuilder();

        Optional.ofNullable(feedbackFile.getHTML())
                .ifPresent(builder::setHtml);

        return builder.build();
    }

    /**
     * Converts an {@link Audio} to a protobuf version of itself.
     *
     * @param midLessonMedia The {@link MidLessonMedia} to convert.
     * @return The protobuf representation of the provided
     *         {@link MidLessonMedia}. Can be null if the parameter is null.
     */
    private static StrategyProto.MidLessonMedia convertToProto(MidLessonMedia midLessonMedia) {
        if (midLessonMedia == null) {
            return null;
        }

        /* TODO CHUCK: Implement */
        return StrategyProto.MidLessonMedia.newBuilder().build();
    }

    /**
     * Convert from {@link ScenarioAdaptation} to its equivalent protobuf type.
     *
     * @param scenarioAdaptation The {@link ScenarioAdaptation} to convert. Can
     *        be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static StrategyProto.ScenarioAdaptation convertToProto(ScenarioAdaptation scenarioAdaptation) {

        final StrategyProto.ScenarioAdaptation.Builder builder = StrategyProto.ScenarioAdaptation.newBuilder();

        Optional.ofNullable(scenarioAdaptation.getDelayAfterStrategy())
                .map(DelayAfterStrategy::getDuration)
                .map(BigDecimal::longValue)
                .ifPresent(builder::setDelayAfterStrategy);

        Optional.ofNullable(scenarioAdaptation.getDescription())
                .ifPresent(builder::setDescription);

        Optional.ofNullable(scenarioAdaptation.getEnvironmentAdaptation())
                .map(ProtoToGIFTConverter::convertToProto)
                .ifPresent(builder::setEnvironmentAdaptation);

        return builder.build();
    }

    /**
     * Convert from {@link EnvironmentAdaptation} to its equivalent protobuf
     * type.
     *
     * @param envAdapt The {@link EnvironmentAdaptation} to convert. Can be
     *        null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.EnvironmentAdaptation convertToProto(EnvironmentAdaptation envAdapt) {
        if (envAdapt == null) {
            return null;
        }

        final EnvironmentAdaptationProto.EnvironmentAdaptation.Builder builder = EnvironmentAdaptationProto.EnvironmentAdaptation
                .newBuilder();

        final Serializable type = envAdapt.getType();
        if (type instanceof EnvironmentAdaptation.Overcast) {
            Optional.ofNullable(convertToProto((EnvironmentAdaptation.Overcast) type)).ifPresent(builder::setOvercast);
        } else if (type instanceof EnvironmentAdaptation.Fog) {
            Optional.ofNullable(convertToProto((EnvironmentAdaptation.Fog) type)).ifPresent(builder::setFog);
        } else if (type instanceof EnvironmentAdaptation.Rain) {
            Optional.ofNullable(convertToProto((EnvironmentAdaptation.Rain) type)).ifPresent(builder::setRain);
        } else if (type instanceof EnvironmentAdaptation.TimeOfDay) {
            Optional.ofNullable(convertToProto((EnvironmentAdaptation.TimeOfDay) type))
                    .ifPresent(builder::setTimeOfDay);
        } else if (type instanceof EnvironmentAdaptation.CreateActors) {
            Optional.ofNullable(convertToProto((EnvironmentAdaptation.CreateActors) type))
                    .ifPresent(builder::setCreateActors);
        } else if (type instanceof EnvironmentAdaptation.RemoveActors) {
            Optional.ofNullable(convertToProto((EnvironmentAdaptation.RemoveActors) type))
                    .ifPresent(builder::setRemoveActors);
        } else if (type instanceof EnvironmentAdaptation.Teleport) {
            Optional.ofNullable(convertToProto((EnvironmentAdaptation.Teleport) type)).ifPresent(builder::setTeleport);
        } else if (type instanceof EnvironmentAdaptation.FatigueRecovery) {
            Optional.ofNullable(convertToProto((EnvironmentAdaptation.FatigueRecovery) type))
                    .ifPresent(builder::setFatigueRecovery);
        } else if (type instanceof EnvironmentAdaptation.Endurance) {
            Optional.ofNullable(convertToProto((EnvironmentAdaptation.Endurance) type))
                    .ifPresent(builder::setEndurance);
        } else if (type instanceof EnvironmentAdaptation.Script) {
            Optional.ofNullable(convertToProto((EnvironmentAdaptation.Script) type)).ifPresent(builder::setScript);
        } else {
            final String typeName = type != null ? type.getClass().getName() : "null";
            throw new IllegalArgumentException("Unable to convert '" + typeName + "' to a protobuf equivalent.");
        }

        return builder.build();
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.Overcast} to its
     * equivalent protobuf type.
     *
     * @param overcast The {@link generated.dkf.EnvironmentAdaptation.Overcast}
     *        to convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.Overcast convertToProto(EnvironmentAdaptation.Overcast overcast) {
        if (overcast == null) {
            return null;
        }

        final EnvironmentAdaptationProto.Overcast.Builder builder = EnvironmentAdaptationProto.Overcast.newBuilder();

        Optional.ofNullable(overcast.getScenarioAdaptationDuration())
                .map(BigInteger::longValue)
                .ifPresent(builder::setDuration);

        Optional.ofNullable(overcast.getValue())
                .map(BigDecimal::longValue)
                .ifPresent(builder::setValue);

        return builder.build();
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.Fog} to its
     * equivalent protobuf type.
     *
     * @param fog The {@link generated.dkf.EnvironmentAdaptation.Fog} to
     *        convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.Fog convertToProto(EnvironmentAdaptation.Fog fog) {
        if (fog == null) {
            return null;
        }

        final EnvironmentAdaptationProto.Fog.Builder builder = EnvironmentAdaptationProto.Fog.newBuilder();

        Optional.ofNullable(fog.getColor()).map(color -> {
            return EnvironmentAdaptationProto.Fog.Color.newBuilder()
                    .setRed(color.getRed())
                    .setGreen(color.getGreen())
                    .setBlue(color.getBlue());
        }).ifPresent(builder::setColor);

        Optional.ofNullable(fog.getDensity())
                .map(BigDecimal::doubleValue)
                .ifPresent(builder::setDensity);

        Optional.ofNullable(fog.getScenarioAdaptationDuration())
                .map(BigInteger::longValue)
                .ifPresent(builder::setDuration);

        return builder.build();
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.Rain} to its
     * equivalent protobuf type.
     *
     * @param rain The {@link generated.dkf.EnvironmentAdaptation.Rain} to
     *        convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.Rain convertToProto(EnvironmentAdaptation.Rain rain) {
        if (rain == null) {
            return null;
        }

        final EnvironmentAdaptationProto.Rain.Builder builder = EnvironmentAdaptationProto.Rain.newBuilder();

        Optional.ofNullable(rain.getScenarioAdaptationDuration())
                .map(BigInteger::longValue)
                .ifPresent(builder::setDuration);

        Optional.ofNullable(rain.getValue())
                .map(BigDecimal::doubleValue)
                .ifPresent(builder::setValue);

        return builder.build();
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.TimeOfDay} to its
     * equivalent protobuf type.
     *
     * @param timeOfDay The
     *        {@link generated.dkf.EnvironmentAdaptation.TimeOfDay} to convert.
     *        Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.TimeOfDay convertToProto(EnvironmentAdaptation.TimeOfDay timeOfDay) {
        if (timeOfDay == null) {
            return null;
        }

        final EnvironmentAdaptationProto.TimeOfDay.Builder builder = EnvironmentAdaptationProto.TimeOfDay.newBuilder();
        Optional.ofNullable(timeOfDay.getType())
                .map(ProtoToGIFTConverter::convertTimeOfDayToProto)
                .ifPresent(builder::setTime);

        return builder.build();
    }

    private static EnvironmentAdaptationProto.TimeOfDay.Time convertTimeOfDayToProto(Serializable type) {
        if (type == null) {
            return null;
        }

        if (type instanceof EnvironmentAdaptation.TimeOfDay.Midnight) {
            return EnvironmentAdaptationProto.TimeOfDay.Time.MIDNIGHT;
        } else if (type instanceof EnvironmentAdaptation.TimeOfDay.Dawn) {
            return EnvironmentAdaptationProto.TimeOfDay.Time.DAWN;
        } else if (type instanceof EnvironmentAdaptation.TimeOfDay.Midday) {
            return EnvironmentAdaptationProto.TimeOfDay.Time.MIDDAY;
        } else if (type instanceof EnvironmentAdaptation.TimeOfDay.Dusk) {
            return EnvironmentAdaptationProto.TimeOfDay.Time.DUSK;
        } else {
            final String typeName = type.getClass().getName();
            throw new IllegalArgumentException("Unable to convert '" + typeName + "' to an equivalent protobuf type.");
        }
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.CreateActors} to
     * its equivalent protobuf type.
     *
     * @param createActors The
     *        {@link generated.dkf.EnvironmentAdaptation.CreateActors} to
     *        convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.CreateActors convertToProto(EnvironmentAdaptation.CreateActors createActors) {
        if (createActors == null) {
            return null;
        }

        final EnvironmentAdaptationProto.CreateActors.Builder builder = EnvironmentAdaptationProto.CreateActors.newBuilder();

        Optional.ofNullable(createActors.getCoordinate())
                .map(ProtoToGIFTConverter::convertToProto)
                .ifPresent(builder::setCoordinate);
        
        Optional.ofNullable(createActors.getHeading())
            .map(ProtoToGIFTConverter::convertToProto)
            .ifPresent(builder::setHeading);

        Optional.ofNullable(createActors.getSide())
                .map(ProtoToGIFTConverter::convertCreateActorsSideToProto)
                .ifPresent(builder::setSide);

        Optional.ofNullable(createActors.getType())
                .ifPresent(builder::setType);

        return builder.build();
    }

    private static EnvironmentAdaptationProto.CreateActors.Side convertCreateActorsSideToProto(Serializable side) {
        if (side == null) {
            return null;
        }

        if (side instanceof EnvironmentAdaptation.CreateActors.Side.Blufor) {
            return EnvironmentAdaptationProto.CreateActors.Side.BLUFOR;
        } else if (side instanceof EnvironmentAdaptation.CreateActors.Side.Opfor) {
            return EnvironmentAdaptationProto.CreateActors.Side.OPFOR;
        } else if (side instanceof EnvironmentAdaptation.CreateActors.Side.Civilian) {
            return EnvironmentAdaptationProto.CreateActors.Side.CIVILIAN;
        } else {
            final String sideName = side.getClass().getName();
            throw new IllegalArgumentException("Unable to convert '" + sideName + "' to an equivanlent protobuf type.");
        }
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.RemoveActors} to
     * its equivalent protobuf type.
     *
     * @param removeActors The
     *        {@link generated.dkf.EnvironmentAdaptation.RemoveActors} to
     *        convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.RemoveActors convertToProto(EnvironmentAdaptation.RemoveActors removeActors) {
        if (removeActors == null) {
            return null;
        }

        final EnvironmentAdaptationProto.RemoveActors.Builder builder = EnvironmentAdaptationProto.RemoveActors.newBuilder();
        
        Serializable specifier = removeActors.getType();
        if (specifier instanceof String) {
            String name = (String) specifier;
            builder.setName(name);
        } else if (specifier instanceof EnvironmentAdaptation.RemoveActors.Location) {
            EnvironmentAdaptation.RemoveActors.Location location = (EnvironmentAdaptation.RemoveActors.Location) specifier;
            final CoordinateProto.Coordinate protoCoordinate = convertToProto(location.getCoordinate());
            if (protoCoordinate != null) {
                builder.setLocation(protoCoordinate);
            }
        } else {
            final String typeName = specifier.getClass().getName();
            throw new IllegalArgumentException("Unable to convert '" + typeName + "' to an equivalent protobuf type.");
        }
        
        
        ActorTypeCategoryEnum actorTypeCategory = removeActors.getTypeCategory();
        builder.setTypeCategory(actorTypeCategory.name());

        return builder.build();
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.Teleport} to its
     * equivalent protobuf type.
     *
     * @param teleport The {@link generated.dkf.EnvironmentAdaptation.Teleport}
     *        to convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.Teleport convertToProto(EnvironmentAdaptation.Teleport teleport) {
        if (teleport == null) {
            return null;
        }

        final EnvironmentAdaptationProto.Teleport.Builder builder = EnvironmentAdaptationProto.Teleport.newBuilder();
        Optional.ofNullable(teleport.getCoordinate())
                .map(ProtoToGIFTConverter::convertToProto)
                .ifPresent(builder::setCoordinate);

        Optional.ofNullable(teleport.getHeading())
                .map(ProtoToGIFTConverter::convertToProto)
                .ifPresent(builder::setHeading);

        Optional.ofNullable(teleport.getTeamMemberRef())
                .map(ref -> TeamMemberRefProto.TeamMemberRef.newBuilder().setReference(ref.getValue()))
                .ifPresent(builder::setReference);

        return builder.build();
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.CreateActors.Heading}
     * to its equivalent protobuf type.
     *
     * @param heading The
     *        {@link generated.dkf.EnvironmentAdaptation.CreateActors.Heading} to
     *        convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.Heading convertToProto(EnvironmentAdaptation.CreateActors.Heading heading) {
        if (heading == null) {
            return null;
        }

        final EnvironmentAdaptationProto.Heading.Builder builder = EnvironmentAdaptationProto.Heading.newBuilder();
        Optional.ofNullable(heading.getValue()).ifPresent(builder::setValue);
        return builder.build();
    }
    
    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.Teleport.Heading}
     * to its equivalent protobuf type.
     *
     * @param heading The
     *        {@link generated.dkf.EnvironmentAdaptation.Teleport.Heading} to
     *        convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.Heading convertToProto(EnvironmentAdaptation.Teleport.Heading heading) {
        if (heading == null) {
            return null;
        }

        final EnvironmentAdaptationProto.Heading.Builder builder = EnvironmentAdaptationProto.Heading.newBuilder();
        Optional.ofNullable(heading.getValue()).ifPresent(builder::setValue);
        return builder.build();
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.Endurance} to its
     * equivalent protobuf type.
     *
     * @param endurance The
     *        {@link generated.dkf.EnvironmentAdaptation.Endurance} to convert.
     *        Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.Endurance convertToProto(EnvironmentAdaptation.Endurance endurance) {
        if (endurance == null) {
            return null;
        }

        final EnvironmentAdaptationProto.Endurance.Builder builder = EnvironmentAdaptationProto.Endurance.newBuilder();

        Optional.ofNullable(endurance.getTeamMemberRef())
                .map(ref -> TeamMemberRefProto.TeamMemberRef.newBuilder().setReference(ref.getValue()))
                .ifPresent(builder::setTeamMemberReference);

        Optional.ofNullable(endurance.getValue())
                .map(BigDecimal::doubleValue)
                .ifPresent(builder::setValue);

        return builder.build();
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.FatigueRecovery}
     * to its equivalent protobuf type.
     *
     * @param fatigue The
     *        {@link generated.dkf.EnvironmentAdaptation.FatigueRecovery} to
     *        convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.FatigueRecovery convertToProto(EnvironmentAdaptation.FatigueRecovery fatigue) {
        if (fatigue == null) {
            return null;
        }

        final EnvironmentAdaptationProto.FatigueRecovery.Builder builder = EnvironmentAdaptationProto.FatigueRecovery.newBuilder();

        Optional.ofNullable(fatigue.getRate())
                .map(BigDecimal::doubleValue)
                .ifPresent(builder::setRate);
        Optional.ofNullable(fatigue.getTeamMemberRef())
                .map(ref -> TeamMemberRefProto.TeamMemberRef.newBuilder().setReference(ref.getValue()))
                .ifPresent(builder::setTeamMemberReference);

        return builder.build();
    }

    /**
     * Convert from {@link generated.dkf.EnvironmentAdaptation.Script} to its
     * equivalent protobuf type.
     *
     * @param script The {@link generated.dkf.EnvironmentAdaptation.Script} to
     *        convert. Can be null.
     * @return The resulting protobuf object. Can be null if the provided
     *         parameter was null.
     */
    private static EnvironmentAdaptationProto.Script convertToProto(EnvironmentAdaptation.Script script) {
        if (script == null) {
            return null;
        }

        final EnvironmentAdaptationProto.Script.Builder builder = EnvironmentAdaptationProto.Script.newBuilder();
        Optional.ofNullable(script.getValue()).ifPresent(builder::setValue);
        return builder.build();
    }
    
    /**
     * Convert from {@link EvaluatorUpdateRequestProto.EvaluatorUpdateRequest} to the GIFT common version of
     * {@link EvaluatorUpdateRequest}.
     * 
     * @param evaluatorUpdateRequestProto The protobuf object to convert.
     * @return the GIFT common version of the protobuf object. Will be null if
     *         the provided protobuf object is null.
     */
    public static EvaluatorUpdateRequest convertFromProto(EvaluatorUpdateRequestProto.EvaluatorUpdateRequest evaluatorUpdateRequestProto){
        
        if(evaluatorUpdateRequestProto == null){
            return null;
        }
        
        EvaluatorUpdateRequest evaluatorUpdateRequest = new EvaluatorUpdateRequest(evaluatorUpdateRequestProto.getTaskConceptName(), evaluatorUpdateRequestProto.getEvaluator(), evaluatorUpdateRequestProto.getTimestamp());
        evaluatorUpdateRequest.setAssessmentHold(evaluatorUpdateRequestProto.getAssessmentHold());
        evaluatorUpdateRequest.setCompetenceHold(evaluatorUpdateRequestProto.getCompetenceHold());
        if(evaluatorUpdateRequestProto.hasCompetenceMetric()){
            evaluatorUpdateRequest.setCompetenceMetric(evaluatorUpdateRequestProto.getCompetenceMetric().getValue());
        }
        evaluatorUpdateRequest.setConfidenceHold(evaluatorUpdateRequestProto.getConfidenceHold());
        if(evaluatorUpdateRequestProto.hasConfidenceMetric()){
            evaluatorUpdateRequest.setConfidenceMetric(evaluatorUpdateRequestProto.getConfidenceMetric().getValue());
        }
        evaluatorUpdateRequest.setMediaFile(evaluatorUpdateRequestProto.getMediaFile());
        
        if(evaluatorUpdateRequestProto.getPerformanceMetric() != null){
            evaluatorUpdateRequest.setPerformanceMetric(AssessmentLevelEnum.valueOf(evaluatorUpdateRequestProto.getPerformanceMetric()));
        }
        
        evaluatorUpdateRequest.setPriorityHold(evaluatorUpdateRequestProto.getPriorityHold());
        if(evaluatorUpdateRequestProto.hasPriorityMetric()){
            evaluatorUpdateRequest.setPriorityMetric(evaluatorUpdateRequestProto.getPriorityMetric().getValue());
        }
        evaluatorUpdateRequest.setReason(evaluatorUpdateRequestProto.getReason());
        
        if(StringUtils.isNotBlank(evaluatorUpdateRequestProto.getTaskStateEnum())){
            evaluatorUpdateRequest.setState(PerformanceNodeStateEnum.valueOf(evaluatorUpdateRequestProto.getTaskStateEnum()));
        }
        
        Map<String, AssessmentLevelEnum> teamOrgEntities = null;
        if(evaluatorUpdateRequestProto.getTeamOrgEntitiesMap() != null){
            teamOrgEntities = new HashMap<>();
            for(Entry<String, String> entry : evaluatorUpdateRequestProto.getTeamOrgEntitiesMap().entrySet()){
                teamOrgEntities.put(entry.getKey(), AssessmentLevelEnum.valueOf(entry.getValue()));
            }
        }
        evaluatorUpdateRequest.setTeamOrgEntities(teamOrgEntities);
        evaluatorUpdateRequest.setTrendHold(evaluatorUpdateRequestProto.getTrendHold());
        if(evaluatorUpdateRequestProto.hasTrendMetric()){
            evaluatorUpdateRequest.setTrendMetric(evaluatorUpdateRequestProto.getTrendMetric().getValue());
        }
        
        
        return evaluatorUpdateRequest;
    }
}
