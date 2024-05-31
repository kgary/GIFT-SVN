package mil.arl.gift.lms.impl.lrs.xapi.statements;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Score;
import com.rusticisoftware.tincan.StatementTarget;
import com.rusticisoftware.tincan.SubStatement;
import com.rusticisoftware.tincan.Verb;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsInvalidStatementException;
import mil.arl.gift.lms.impl.common.LmsStatementIdException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.CommonLrsEnum;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.UUIDHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.EnvironmentAdaptationActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.adl.AdlVerbConcepts;

/**
 * xAPI Statements corresponding to the various Environment Adaptation xAPI Statement configurations.
 * 
 * @author Yet Analytics
 *
 */
public class EnvironmentAdaptationStatement extends AbstractGiftStatement {

    /**
     * Creates Environment Adaptation xAPI Statement with Actor Agent and Adaptation Activity Object
     * 
     * @param actor - Agent created from environment adaptation message user name
     * @param adaptationActivity - represents the adaptation being experienced
     * @throws LmsXapiProfileException when unable to parse Experienced Verb from xAPI Profile
     */
    public EnvironmentAdaptationStatement(Agent actor, EnvironmentAdaptationActivity adaptationActivity) throws LmsXapiProfileException {
        super(actor, AdlVerbConcepts.Experienced.getInstance().asVerb(), adaptationActivity, new DateTime());
    }
    
    /**
     * Creates Environment Adaptation xAPI Statement with Actor Agent and Adaptation Activity or SubStatement Object
     * 
     * @param actor - Agent created from environment adaptation message user name
     * @param subStatement - represents the adaptation being experienced
     * 
     * @throws LmsXapiProfileException when unable to parse Experienced Verb from xAPI Profile
     */
    public EnvironmentAdaptationStatement(Agent actor, StatementTarget subStatement) throws LmsXapiProfileException {
        super(actor, AdlVerbConcepts.Experienced.getInstance().asVerb(), subStatement, new DateTime());
    }
    
    public static class EnvironmentAdaptationSubStatement extends SubStatement {
        
        /**
         * Creates SubStatement from Agent/Group actor and environment adaptation xAPI Activity
         * 
         * @param subStmtActor - Agent or Group experiencing the adaptation
         * @param adaptationActivity - represents the adaptation being experienced
         * 
         * @throws LmsXapiProfileException when unable to parse Experienced Verb from xAPI Profile
         */
        public EnvironmentAdaptationSubStatement(Agent subStmtActor, EnvironmentAdaptationActivity adaptationActivity) throws LmsXapiProfileException {
            super();
            this.setActor(subStmtActor);
            this.setVerb(AdlVerbConcepts.Experienced.getInstance().asVerb());
            this.setObject(adaptationActivity);
        }
        
        /**
         * Creates SubStatement from Agent/Group actor and environment adaptation xAPI Activity with raw score of rate
         * 
         * @param subStmtActor - Agent experiencing the adaptation
         * @param adaptationActivity - represents the adaptation being experienced
         * @param rate - rate at which the adaptation is applied
         * 
         * @throws LmsXapiProfileException when unable to parse Experienced Verb from xAPI Profile
         */
        public EnvironmentAdaptationSubStatement(Agent subStmtActor, EnvironmentAdaptationActivity adaptationActivity, BigDecimal rate) throws LmsXapiProfileException {
            this(subStmtActor, adaptationActivity);
            if(rate == null) {
                throw new IllegalArgumentException("rate can not be null!");
            }
            Result result = this.getResult() != null ? this.getResult() : new Result();
            Score score = result.getScore() != null ? result.getScore() : new Score();
            score.setRaw(rate.doubleValue());
            result.setScore(score);
            this.setResult(result);
        }
        
        /**
         * Creates SubStatement from Agent/Group actor and environment adaptation xAPI Activity with result response of heading
         * 
         * @param subStmtActor - Agent experiencing the adaptation
         * @param adaptationActivity - represents the adaptation being experienced
         * @param heading - heading from the teleport environment adaptation
         * 
         * @throws LmsXapiProfileException when unable to parse Experienced Verb from xAPI Profile
         */
        public EnvironmentAdaptationSubStatement(Agent subStmtActor, EnvironmentAdaptationActivity adaptationActivity, Integer heading) throws LmsXapiProfileException {
            this(subStmtActor, adaptationActivity);
            Result result = this.getResult() != null ? this.getResult() : new Result();
            if(heading != null) {
                result.setResponse(heading.toString());
                this.setResult(result);
            }
        }
        
        /**
         * Creates SubStatement from Agent/Group actor and environment adaptation xAPI Activity with result response of highlightName
         * 
         * @param subStmtActor - Agent experiencing the adaptation
         * @param adaptationActivity - represents the adaptation being experienced
         * @param highlightName - name of the highlight
         * 
         * @throws LmsXapiProfileException when unable to parse Experienced Verb from xAPI Profile
         */
        public EnvironmentAdaptationSubStatement(Agent subStmtActor, EnvironmentAdaptationActivity adaptationActivity, String highlightName) throws LmsXapiProfileException {
            this(subStmtActor, adaptationActivity);
            if(highlightName == null) {
                throw new IllegalArgumentException("highlightName can not be null!");
            }
            Result result = this.getResult() != null ? this.getResult() : new Result();
            result.setResponse(highlightName);
            this.setResult(result);
        }
    }
    
    /**
     * Parses information stored within result and adds to accumulator of slugs
     * 
     * @param slugs - accumulator to update
     * @param result - result to parse strings from
     */
    private void parseResult(List<String> slugs, Result result) {
        if(result != null) {
            // -> response
            if(result.getResponse() != null) {
                slugs.add(result.getResponse());
            }
            // -> Raw Score
            if(result.getScore() != null) {
                slugs.add(result.getScore().getRaw().toString());
            }
        }
    }

    @Override
    UUID deriveStatementId() throws LmsException {
        List<String> slugs = new ArrayList<String>();
        // Common
        // -> Agent
        slugs.add(getActorName());
        // -> Verb
        slugs.add(getVerbId());
        // -> Object (top level or substatement)
        slugs.add(getObjectId());
        // -> Timestamp
        slugs.add(parseTimestamp());
        // Sub Statement
        if(getObject() instanceof SubStatement) {
            SubStatement subStmt = (SubStatement) getObject();
            // -> Agent
            slugs.add(PersonaHelper.getActorName(subStmt));
            // -> Verb
            Verb verb = subStmt.getVerb();
            if(verb == null || verb.getId() == null) {
                throw new LmsInvalidStatementException("SubStatement did not contain a verb with id!");
            }
            slugs.add(verb.getId().toString());
            // -> Result.Score
            parseResult(slugs, subStmt.getResult());
        } else {
            // Top level Result
            parseResult(slugs, getResult());
        }
        // Context
        ItsActivityTypeConcepts dATC, dsATC, ksATC;
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
        // -> Knowledge Session type
        try {
            ksATC = ItsActivityTypeConcepts.KnowledgeSessionType.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Knowledge Session Type Activity Type Concept!", e);
        }
        addActivityIdToColl(ksATC, this, slugs);
        // derive and return UUID from slugs
        return UUIDHelper.createUUIDFromData(StringUtils.join(CommonLrsEnum.SEPERATOR_COMMA.getValue(), slugs));
    }
}
