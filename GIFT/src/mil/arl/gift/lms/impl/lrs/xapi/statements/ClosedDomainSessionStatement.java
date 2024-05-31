package mil.arl.gift.lms.impl.lrs.xapi.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import com.rusticisoftware.tincan.Agent;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsStatementIdException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.CommonLrsEnum;
import mil.arl.gift.lms.impl.lrs.xapi.UUIDHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainSessionActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsVerbConcepts;

/**
 * xAPI Statement for closing Domain Session.
 * 
 * @author Yet Analytics
 *
 */
public class ClosedDomainSessionStatement extends AbstractGiftStatement {

    public ClosedDomainSessionStatement(Agent actor, DomainSessionActivity activity, DateTime timestamp) throws LmsXapiProfileException {
        super(actor, ItsVerbConcepts.Closed.getInstance().asVerb(), activity, timestamp);
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
        // Parent Domain Activity
        ItsActivityTypeConcepts dATC;
        // -> Domain
        try {
            dATC = ItsActivityTypeConcepts.Domain.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Domain Activity Type Concept!", e);
        }
        addActivityIdToColl(dATC, this, slugs);
        // derive and return UUID from slugs
        return UUIDHelper.createUUIDFromData(StringUtils.join(CommonLrsEnum.SEPERATOR_COMMA.getValue(), slugs));
    }
}
