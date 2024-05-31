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
import mil.arl.gift.lms.impl.lrs.xapi.statements.StartedDomainSessionStatement;

/**
 * Generator for Started Domain Session xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class StartedDomainSessionGenerator extends AbstractStatementGenerator {
    /** Domain Session Activity Type from xAPI Profile */
    protected ItsActivityTypeConcepts.DomainSession sessionATC;
    /** User name parsed from started domain session message */
    private String actorSlug;
    /** Event time parsed from started domain session message */
    private DateTime timestamp;
    /** Identifier for the domain session parsed from the started domain session message */
    private Integer domainSessionid;
    /**
     * Sets state used within generateStatement
     * 
     * @param template - xAPI Statement Template describing generated xAPI Statement
     * @param actorSlug - User name parsed from started domain session message
     * @param timestamp - Event time parsed from started domain session message
     * @param domainSessionid - Identifier for the domain session parsed from the started domain session message
     * 
     * @throws LmsXapiGeneratorException when unable to set default appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    protected StartedDomainSessionGenerator(StatementTemplate template, String actorSlug, DateTime timestamp, Integer domainSessionid) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(template, domainSessionid);
        if(timestamp == null) {
            throw new IllegalArgumentException("event time can not be null!");
        }
        this.timestamp = timestamp;
        if(StringUtils.isBlank(actorSlug)) {
            throw new IllegalArgumentException("actorSlug can not be null or empty!");
        }
        this.actorSlug = actorSlug;
        this.domainSessionid = domainSessionid;
        this.sessionATC = ItsActivityTypeConcepts.DomainSession.getInstance();
    }
    /**
     * Sets state used within generateStatement and attaches domain appender
     * 
     * @param actorSlug - User name parsed from started domain session message
     * @param timestamp - Event time parsed from started domain session message
     * @param domainSessionId - Identifier for the domain session parsed from the started domain session message
     * @param domainId - name of the course associated with the domain session
     * 
     * @throws LmsXapiGeneratorException when unable to set default appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public StartedDomainSessionGenerator(String actorSlug, DateTime timestamp, Integer domainSessionId, String domainId) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        this(DomainSessionLifecycleTemplate.StartedTemplate.getInstance(), actorSlug, timestamp, domainSessionId);
        if(StringUtils.isBlank(domainId)) {
            throw new IllegalArgumentException("Domain id can not be null!");
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
            throw new LmsXapiGeneratorException("Unable to create statement actor within started domain session generator!", e);
        }
        // Object
        DomainSessionActivity object;
        try {
            object = sessionATC.asActivity(domainSessionid);
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiGeneratorException("Unable to create statement object within started domain session generator!", e);
        }
        // statement
        StartedDomainSessionStatement stmt;
        try {
            stmt = new StartedDomainSessionStatement(actor, object, timestamp);
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to generate started domain session statement!", e);
        }
        try {
            stmt.addRegistration(domainSessionid);
        } catch (LmsXapiUUIDException e) {
            throw new LmsXapiGeneratorException("Unable to add registration to started domain session statement!", e);
        }
        return generateStatement(stmt, deriveId);
    }
}
