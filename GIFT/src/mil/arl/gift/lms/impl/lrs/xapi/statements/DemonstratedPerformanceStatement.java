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
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsVerbConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts.extensionObjectKeys;

/**
 * xAPI Statement for formative assessment corresponding to Performance State Attribute
 * found within Learner State.
 * 
 * @author Yet Analytics
 *
 */
public class DemonstratedPerformanceStatement extends AbstractGiftStatement {

    public DemonstratedPerformanceStatement(Agent actor, AssessmentActivity activity, DateTime timestamp) throws LmsXapiProfileException {
        super(actor, ItsVerbConcepts.Demonstrated.getInstance().asVerb(), activity, timestamp);
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
        ItsResultExtensionConcepts.PerformanceMeasure performanceStateAttributeREC;
        try {
            performanceStateAttributeREC = ItsResultExtensionConcepts.PerformanceMeasure.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Performance Measure Result Extension Concept!", e);
        }
        if(getResult() != null && getResult().getExtensions() != null) {
            JsonNode extension = performanceStateAttributeREC.parseFromExtensions(getResult().getExtensions());
            if(extension != null) {
                addExtensionValToColl(extension, extensionObjectKeys.EXPLANATION, slugs);
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
        ItsActivityTypeConcepts dATC, dsATC, taskATC, icATC;
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
        // -> Parent performance state attribute
        // --> Task
        try {
            taskATC = ItsActivityTypeConcepts.AssessmentNode.Task.getInstance();
        } catch(LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Task Activity Type Concept!", e);
        }
        addActivityIdToColl(taskATC.findInstancesInCollection(getContext().getContextActivities().getParent()), slugs);
        // --> Intermediate Concept
        try {
            icATC = ItsActivityTypeConcepts.AssessmentNode.ConceptIntermediate.getInstance();
        } catch(LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Intermediate Concept Activity Type Concept!", e);
        }
        addActivityIdToColl(icATC.findInstancesInCollection(getContext().getContextActivities().getParent()), slugs);
        // derive and return UUID from slugs
        return UUIDHelper.createUUIDFromData(StringUtils.join(CommonLrsEnum.SEPERATOR_COMMA.getValue(), slugs));
    }
}
