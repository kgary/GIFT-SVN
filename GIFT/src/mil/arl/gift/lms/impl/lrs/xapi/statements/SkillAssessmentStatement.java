package mil.arl.gift.lms.impl.lrs.xapi.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import com.fasterxml.jackson.databind.JsonNode;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Result;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsInvalidStatementException;
import mil.arl.gift.lms.impl.common.LmsStatementIdException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.CommonLrsEnum;
import mil.arl.gift.lms.impl.lrs.xapi.UUIDHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.mom.MomVerbConcepts;

/**
 * xAPI Statement corresponding to (summative) assessment derived from LMS Course Record.
 * 
 * @author Yet Analytics
 *
 */
public class SkillAssessmentStatement extends AbstractGiftStatement {
    
    public SkillAssessmentStatement(Agent actor, AssessmentActivity activity, DateTime timestamp, Context context, Result result) throws LmsXapiProfileException {
        super(actor, MomVerbConcepts.Assessed.getInstance().asVerb(), activity, timestamp, context, result);
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
        // -> Response
        if(getResult() == null || getResult().getResponse() == null || getResult().getExtensions() == null) {
            throw new LmsInvalidStatementException("Assessment Statement result or result.response or result.extensions was null! "
                    + "Aborting statement id derivation!");
        }
        slugs.add(getResult().getResponse());
        ItsResultExtensionConcepts.ConceptEvaluation conceptEvaluationREC = ItsResultExtensionConcepts.ConceptEvaluation.getInstance();
        JsonNode ext = conceptEvaluationREC.parseFromExtensions(this.getResult().getExtensions());
        if(ext == null) {
            throw new LmsInvalidStatementException("Concept Evaluation (RawScoreNode) extension was null!");
        }
        String assessmentName = ext.get(ItsResultExtensionConcepts.extensionObjectKeys.NAME.getValue()).asText();
        slugs.add(assessmentName);
        String assessmentUnits = ext.get(ItsResultExtensionConcepts.extensionObjectKeys.UNITS.getValue()).asText();
        slugs.add(assessmentUnits);
        String assessmentValue = ext.get(ItsResultExtensionConcepts.extensionObjectKeys.VALUE.getValue()).asText();
        slugs.add(assessmentValue);
        String assessmentAssessment = ext.get(ItsResultExtensionConcepts.extensionObjectKeys.ASSESSMENT.getValue()).asText();
        slugs.add(assessmentAssessment);
        // Optional
        ItsActivityTypeConcepts dATC, dsATC, crATC;
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
        // -> Course Record
        try {
            crATC = ItsActivityTypeConcepts.CourseRecord.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Course Record Activity Type Concept!", e);
        }
        addActivityIdToColl(crATC, this, slugs);
        return UUIDHelper.createUUIDFromData(StringUtils.join(CommonLrsEnum.SEPERATOR_COMMA.getValue(), slugs));
    }
}
