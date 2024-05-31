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
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.tincan.TincanVerbConcepts;

/**
 * xAPI Statement for creation of global bookmark (found within top level Performance State).
 * 
 * @author Yet Analytics
 *
 */
public class BookmarkStatement extends AbstractGiftStatement {

    public BookmarkStatement(Agent actor, DomainActivity domainActivity, DateTime timestamp) throws LmsXapiProfileException {
        super(actor, TincanVerbConcepts.Bookmarked.getInstance().asVerb(), domainActivity, timestamp);
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
        // Context
        ItsActivityTypeConcepts dsATC, ksATC;
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
        // Result
        // -> response
        if(this.getResult() != null && this.getResult().getResponse() != null) {
            slugs.add(this.getResult().getResponse());
        }
        // derive and return UUID from slugs
        return UUIDHelper.createUUIDFromData(StringUtils.join(CommonLrsEnum.SEPERATOR_COMMA.getValue(), slugs));
    }
}
