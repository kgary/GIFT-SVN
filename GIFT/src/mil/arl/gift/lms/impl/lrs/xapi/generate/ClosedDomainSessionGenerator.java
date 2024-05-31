package mil.arl.gift.lms.impl.lrs.xapi.generate;

import org.joda.time.DateTime;
import com.rusticisoftware.tincan.Agent;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiUUIDException;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainSessionActivity;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainAppender;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.DomainSessionLifecycleTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.ClosedDomainSessionStatement;

/**
 * Generator for Closed Domain Session xAPI Statement.
 * 
 * @author Yet Analytics
 *
 */
public class ClosedDomainSessionGenerator extends AbstractStatementGenerator {
    /** Domain Session Activity Type from xAPI Profile */
    protected ItsActivityTypeConcepts.DomainSession sessionATC;
    /** User name from UserSession associated with DomainSession */
    private String actorSlug;
    /** Event time parsed from closed domain session message */
    private DateTime timestamp;
    /**
     * Sets state used within generateStatement
     * 
     * @param template - xAPI Statement Template describing the closed domain session xAPI Statement
     * @param actorSlug - User name from UserSession associated with DomainSession
     * @param timestamp - Event time parsed from closed domain session message
     * @param domainSessionId - Domain Session identifier parsed from closed domain session message
     * 
     * @throws LmsXapiGeneratorException when unable to set default appender
     * @throws LmsXapiProfileException when unable to parse activity type from xAPI Profile
     */
    protected ClosedDomainSessionGenerator(StatementTemplate template, String actorSlug, DateTime timestamp, Integer domainSessionId) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(template, domainSessionId);
        if(timestamp == null) {
            throw new IllegalArgumentException("Event time can not be null!");
        }
        this.timestamp = timestamp;
        if(StringUtils.isBlank(actorSlug)) {
            throw new IllegalArgumentException("actor slug can not be null!");
        }
        this.actorSlug = actorSlug;
        this.sessionATC = ItsActivityTypeConcepts.DomainSession.getInstance();
    }
    /**
     * Sets state used within generateStatement and adds DomainAppender
     * 
     * @param actorSlug - User name from UserSession associated with DomainSession
     * @param timestamp - Event time parsed from closed domain session message
     * @param domainSessionId - Domain Session identifier parsed from closed domain session message
     * @param domainId - Identifier for Domain associated with Domain Session
     * 
     * @throws LmsXapiProfileException when unable to parse activity type or statement template from xAPI Profile
     * @throws LmsXapiGeneratorException when unable to set default appender
     */
    public ClosedDomainSessionGenerator(String actorSlug, DateTime timestamp, Integer domainSessionId, String domainId)
            throws LmsXapiProfileException, LmsXapiGeneratorException {
        this(DomainSessionLifecycleTemplate.ClosedTemplate.getInstance(), actorSlug, timestamp, domainSessionId);
        if(StringUtils.isBlank(domainId)) {
            throw new IllegalArgumentException("domain id can not be null!");
        }
        addStatementAppender(new DomainAppender(domainId));
    }
    
    @Override
    AbstractGiftStatement generateStatement(Boolean deriveId) throws LmsXapiGeneratorException {
        // Actor
        Agent actor;
        try {
            actor = PersonaHelper.createMboxAgent(actorSlug);
        } catch (LmsXapiAgentException e) {
            throw new LmsXapiGeneratorException("Unable to create statement actor within closed domain session generator!", e);
        }
        // Object
        DomainSessionActivity object;
        try {
            object = sessionATC.asActivity(domainSessionId);
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiGeneratorException("Unable to create statement object within closed domain session generator!", e);
        }
        // statement
        ClosedDomainSessionStatement stmt;
        try {
            stmt = new ClosedDomainSessionStatement(actor, object, timestamp);
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to generate closed domain session statement!", e);
        }
        try {
            stmt.addRegistration(domainSessionId);
        } catch (LmsXapiUUIDException e) {
            throw new LmsXapiGeneratorException("Unable to add registration to closed domain session statement!", e);
        }
        return generateStatement(stmt, deriveId);
    }
}
