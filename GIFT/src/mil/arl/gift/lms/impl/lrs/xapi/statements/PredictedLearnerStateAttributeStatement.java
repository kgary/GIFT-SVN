package mil.arl.gift.lms.impl.lrs.xapi.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import com.fasterxml.jackson.databind.JsonNode;
import com.rusticisoftware.tincan.Agent;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsStatementIdException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.CommonLrsEnum;
import mil.arl.gift.lms.impl.lrs.xapi.UUIDHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.LearnerStateAttributeActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsVerbConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts.extensionObjectKeys;

/**
 * xAPI Statement corresponding to (Affective | Cognitive) Learner State Attribute evaluations.
 * 
 * @author Yet Analytics
 *
 */
public class PredictedLearnerStateAttributeStatement extends AbstractGiftStatement {

    public PredictedLearnerStateAttributeStatement(Agent actor, LearnerStateAttributeActivity activity, DateTime timestamp) throws LmsXapiProfileException {
        super(actor, ItsVerbConcepts.Predicted.getInstance().asVerb(), activity, timestamp);
    }

    @Override
    UUID deriveStatementId() throws LmsException {
        List<String> slugs = new ArrayList<String>();
        // Common
        // -> Agent
        slugs.add(getActorName());
        // -> Verb
        slugs.add(getVerbId());
        // -> Object
        slugs.add(getObjectId());
        // -> Timestamp
        slugs.add(parseTimestamp());
        // Result Extension
        ItsResultExtensionConcepts.AttributeMeasure ext;
        try {
            ext = ItsResultExtensionConcepts.AttributeMeasure.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Attribute Measure Result Extension Concept!", e);
        }
        if(getResult() != null && getResult().getExtensions() != null) {
            JsonNode extension = ext.parseFromExtensions(getResult().getExtensions());
            if(extension != null) {
                // -> Predicted
                JsonNode predicted = extension.get(extensionObjectKeys.PREDICTED.getValue());
                if(predicted != null) {
                    addExtensionValToColl(predicted, extensionObjectKeys.ASSESSMENT, slugs);
                    addExtensionValToColl(predicted, extensionObjectKeys.TIMESTAMP, slugs);
                }
                // -> shortTerm
                JsonNode shortTerm = extension.get(extensionObjectKeys.SHORT_TERM.getValue());
                if(shortTerm != null) {
                    addExtensionValToColl(shortTerm, extensionObjectKeys.ASSESSMENT, slugs);
                    addExtensionValToColl(shortTerm, extensionObjectKeys.TIMESTAMP, slugs);
                }
                // -> longTerm
                JsonNode longTerm = extension.get(extensionObjectKeys.LONG_TERM.getValue());
                if(longTerm != null) {
                    addExtensionValToColl(longTerm, extensionObjectKeys.ASSESSMENT, slugs);
                    addExtensionValToColl(longTerm, extensionObjectKeys.TIMESTAMP, slugs);
                }
            }
        }
        // Optional
        ItsActivityTypeConcepts dATC, dsATC, affectiveATC, cognitiveATC, taskATC, icATC, conceptATC;
        // -> Domain
        try {
            dATC = ItsActivityTypeConcepts.Domain.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Domain Activity Type Concept!", e);
        }
        addActivityIdToColl(dATC, this, slugs);
        // -> Domain Session
        try {
            dsATC = ItsActivityTypeConcepts.DomainSession.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Domain Session Activity Type Concept!", e);
        }
        addActivityIdToColl(dsATC, this, slugs);
        // -> Affective Indicator
        try {
            affectiveATC = ItsActivityTypeConcepts.Affective.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Affective Lsa Activity Type Concept!", e);
        }
        addActivityIdToColl(affectiveATC, this, slugs);
        // -> Cognitive Indicator
        try {
            cognitiveATC = ItsActivityTypeConcepts.Cognitive.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Cognitive Lsa Activity Type Concept!", e);
        }
        addActivityIdToColl(cognitiveATC, this, slugs);
        // -> Associated Performance State Attribute
        // --> Task
        try {
            taskATC = ItsActivityTypeConcepts.AssessmentNode.Task.getInstance();
        } catch(LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Task Activity Type Concept!", e);
        }
        addActivityIdToColl(taskATC, this, slugs);
        // --> Intermediate Concept
        try {
            icATC = ItsActivityTypeConcepts.AssessmentNode.ConceptIntermediate.getInstance();
        } catch(LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Intermediate Concept Activity Type Concept!", e);
        }
        addActivityIdToColl(icATC, this, slugs);
        // --> Concept
        try {
            conceptATC = ItsActivityTypeConcepts.AssessmentNode.Concept.getInstance();
        } catch(LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Concept Activity Type Concept!", e);
        }
        addActivityIdToColl(conceptATC, this, slugs);
        // derive and return UUID from slugs
        return UUIDHelper.createUUIDFromData(StringUtils.join(CommonLrsEnum.SEPERATOR_COMMA.getValue(), slugs));
    }
}
