package mil.arl.gift.lms.impl.lrs.xapi.generate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.lms.impl.common.LmsInvalidStatementException;
import mil.arl.gift.lms.impl.common.LmsStatementIdException;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.SessionManager;
import mil.arl.gift.lms.impl.lrs.xapi.append.AbstractStatementAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ChainOfCustodyAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ProfileReferenceAppender;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Generic class which is responsible for setting Statement Appenders and performing validation
 * that the created xAPI Statement conforms to the associated xAPI Statement Template determining properties.
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractStatementGenerator {
    /** Statement Template from xAPI Profile */
    private StatementTemplate statementTemplate;
    /** Collection of distinct xAPI Statement modifiers */
    private Set<AbstractStatementAppender> statementAppenders;
    /** Domain Session Identifier for the primary user (typically xAPI Statement Actor) */
    protected Integer domainSessionId;
    /** Chain of Custody information */
    protected AssessmentChainOfCustody chainOfCustody;
    /**
     * State common to all xAPI Statement Generators.
     * 
     * @param template - Statement Template from xAPI Profile that describes the to be generated xAPI Statement
     * @param appenders - Collection of distinct modifiers which mutate xAPI Statement prior to id generation
     * @param dsId - Domain Session identifier for the primary user (typically xAPI Statement Actor)
     * 
     * @throws LmsXapiGeneratorException when unable to set default appenders
     */
    private AbstractStatementGenerator(StatementTemplate template, Set<AbstractStatementAppender> appenders, Integer dsId) throws LmsXapiGeneratorException {
        if(template == null) {
            throw new IllegalArgumentException("Statement Template can not be null!");
        }
        this.statementTemplate = template;
        if(dsId == null) {
            throw new IllegalArgumentException("Domain Session Id can not be null!");
        }
        this.domainSessionId = dsId;
        if(appenders == null) {
            throw new IllegalArgumentException("Statement Appenders can not be null!");
        }
        // All Statements include reference to profile in which their corresponding statement template is defined
        try {
            appenders.add(new ProfileReferenceAppender(statementTemplate));
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiGeneratorException("Unable to initialize generator, can not set profile reference from statement template!", e);
        }
        // Chain of Custody tracking GIFT instance metadata included in all statements
        try {
            appenders.add(new ChainOfCustodyAppender(domainSessionId, SessionManager.getInstance().getUserIdForDomainSessionId(domainSessionId)));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to initialize generator, can not add Chain of Custody appender!", e);
        }
        setStatementAppenders(appenders);
    }
    /**
     * Constructor called by extending classes. Initializes empty collection of Statement Appenders and adds default appenders.
     * 
     * Collection of appenders is updated via the addStatementAppender method defined in this class.
     * 
     * @param template - Statement Template from xAPI Profile that describes the to be generated xAPI Statement
     * @param dsId - Domain Session identifier for the primary user (typically xAPI Statement Actor)
     * 
     * @throws LmsXapiGeneratorException when unable to set default appenders
     */
    public AbstractStatementGenerator(StatementTemplate template, Integer dsId) throws LmsXapiGeneratorException {
        this(template, new HashSet<AbstractStatementAppender>(), dsId);
    }
    /**
     * State common to all xAPI Statement Generators.
     * 
     * @param template - Statement Template from xAPI Profile that describes the to be generated xAPI Statement
     * @param appenders - Collection of distinct modifiers which mutate xAPI Statement prior to id generation
     * @param chain - Chain Of Custody information
     * 
     * @throws LmsXapiGeneratorException when unable to set default appenders
     */
    private AbstractStatementGenerator(StatementTemplate template, Set<AbstractStatementAppender> appenders, AssessmentChainOfCustody chain) throws LmsXapiGeneratorException {
        if(template == null) {
            throw new IllegalArgumentException("Statement Template can not be null!");
        }
        this.statementTemplate = template;
        if(chain == null) {
            throw new IllegalArgumentException("Chain of Custody can not be null!");
        }
        this.domainSessionId = chain.getDomainsessionid();
        this.chainOfCustody = chain;
        if(appenders == null) {
            throw new IllegalArgumentException("Statement Appenders can not be null!");
        }
        // All Statements include reference to profile in which their corresponding statement template is defined
        try {
            appenders.add(new ProfileReferenceAppender(statementTemplate));
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiGeneratorException("Unable to initialize generator, can not set profile reference from statement template!", e);
        }
        // Chain of Custody tracking GIFT instance metadata included in all statements
        try {
            appenders.add(new ChainOfCustodyAppender(chain));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to initialize generator, can not add Chain of Custody appender!", e);
        }
        setStatementAppenders(appenders);
    }
    /**
     * Constructor called by extending classes. Initializes empty collection of Statement Appenders and adds default appenders.
     * 
     * Collection of appenders is updated via the addStatementAppender method defined in this class.
     * 
     * @param template - Statement Template from xAPI Profile that describes the to be generated xAPI Statement
     * @param chain - Chain of custody information
     * 
     * @throws LmsXapiGeneratorException when unable to set default appenders
     */
    public AbstractStatementGenerator(StatementTemplate template, AssessmentChainOfCustody chain) throws LmsXapiGeneratorException {
        this(template, new HashSet<AbstractStatementAppender>(), chain);
    }
    /**
     * @return Generators' xAPI Statement Template
     */
    protected StatementTemplate getStatementTemplate() {
        return statementTemplate;
    }
    /**
     * @return xAPI Statement appenders (mutators)
     */
    protected Set<AbstractStatementAppender> getStatementAppenders() {
        return statementAppenders;
    }
    /**
     * Private setter for xAPI Statement appenders (mutators)
     * 
     * @param appenders - Collection of distinct xAPI Statement appenders
     */
    private void setStatementAppenders(Set<AbstractStatementAppender> appenders) {
        this.statementAppenders = appenders;
    }
    /**
     * Add single xAPI Statement appender to collection of appenders.
     * 
     * @param appender - xAPI Statement mutator
     */
    protected void addStatementAppender(AbstractStatementAppender appender) {
        Set<AbstractStatementAppender> coll = getStatementAppenders();
        coll.add(appender);
        setStatementAppenders(coll);
    }
    /**
     * Add collection of xAPI Statement appender(s) to existing collection of appenders.
     * 
     * @param appenders - xAPI Statement mutator(s)
     */
    protected void addStatementAppender(Set<AbstractStatementAppender> appenders) {
        Set<AbstractStatementAppender> coll = getStatementAppenders();
        coll.addAll(appenders);
        setStatementAppenders(coll);
    }
    /**
     * Takes baseline xAPI Statement and runs it through all appenders attached to the generator. Optionally
     * checks resulting xAPI Statement against associated xAPI Statement Template's determining properties
     * and calls the xAPI Statement's id creation method.
     * 
     * @param stmt - baseline xAPI Statement to potentially modify
     * @param deriveId - flag which controls Statement Template check + id generation
     * 
     * @return possibly modified xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when an appender throws, conflict between xAPI Statement and Template, or unable to create statement id.
     */
    protected AbstractGiftStatement generateStatement(AbstractGiftStatement stmt, Boolean deriveId) throws LmsXapiGeneratorException {
        for(AbstractStatementAppender appender : getStatementAppenders()) {
            try {
                appender.appendToStatement(stmt);
            } catch (LmsXapiAppenderException e) {
                throw new LmsXapiGeneratorException("The "+appender.getName()+" appender was unable to mutate xAPI Statement!", e);
            }
        }
        if(deriveId) {
            try {
                getStatementTemplate().matchingStatement(stmt);
            } catch (LmsInvalidStatementException e) {
                throw new LmsXapiGeneratorException("StatementGenerator created bad statement!", e);
            }
            try {
                stmt.deriveAndSetId();
            } catch (LmsStatementIdException e) {
                String json = stmt.toJSON(true);
                throw new LmsXapiGeneratorException("StatementGenerator was unable to derive and set id for the following xAPI Statement:\n"+json+"\n", e);
            }
        }
        return stmt;
    }
    /**
     * Method expected to be overwritten by extending classes. 
     * 
     * Implementations are expected to end with a call to `generateStatement(stmt, deriveId)`
     * 
     * @param deriveId flag which controls Statement Template check + id generation
     * 
     * @return possibly modified xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to generate expected xAPI Statement
     */
    abstract AbstractGiftStatement generateStatement(Boolean deriveId) throws LmsXapiGeneratorException;
    /**
     * Generate xAPI Statement, perform all appends, conform xAPI Statement to corresponding Statement Template
     * determining properties, derive and set id.
     * 
     * @return xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to generate expected xAPI Statement
     */
    public AbstractGiftStatement generateStatement() throws LmsXapiGeneratorException {
        return generateStatement(true);
    }
    /**
     * Generate xAPI Statement and all to statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * 
     * @throws LmsXapiGeneratorException when unable to generate xAPI Statement
     */
    public void generateAndAdd(List<Statement> statements) throws LmsXapiGeneratorException {
        if(statements == null) {
            throw new IllegalArgumentException("statements can not be null!");
        }
        AbstractGiftStatement stmt = generateStatement();
        statements.add(stmt);
    }
}
