package mil.arl.gift.lms.impl.lrs.xapi.statements;

import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import com.fasterxml.jackson.databind.JsonNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Group;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.StatementTarget;
import com.rusticisoftware.tincan.SubStatement;
import com.rusticisoftware.tincan.Verb;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsInvalidStatementException;
import mil.arl.gift.lms.impl.common.LmsStatementIdException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiUUIDException;
import mil.arl.gift.lms.impl.lrs.LrsEnum;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.UUIDHelper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.ActivityTypeConcept;

/**
 * Abstract class that wraps the tincan Statement class and provides additional utilities.
 * 
 * @author Yet Analytics
 * 
 */
public abstract class AbstractGiftStatement extends Statement {

    protected AbstractGiftStatement(Agent actor, Verb verb, StatementTarget object) {
        super(actor, verb, object);
        Context ctx = getContext();
        setContext(ctx);
    }
    
    public AbstractGiftStatement(Agent actor, Verb verb, StatementTarget object, DateTime timestamp) {
        this(actor, verb, object);
        if(timestamp != null) {
            setTimestamp(timestamp);
        } else {
            setTimestamp(new DateTime());
        }
    }
    
    public AbstractGiftStatement(Agent actor, Verb verb, StatementTarget object, DateTime timestamp, Result result) {
        this(actor, verb, object, timestamp);
        if(result != null) {
            setResult(result);
        }
    }
    
    public AbstractGiftStatement(Agent actor, Verb verb, StatementTarget object, DateTime timestamp, Context context) {
        super(actor, verb, object);
        if(context != null) {
            setContext(context);
        }
        Context ctx = getContext();
        setContext(ctx);
        if(timestamp != null) {
            setTimestamp(timestamp);
        } else {
            setTimestamp(new DateTime());
        }
    }
    
    public AbstractGiftStatement(Agent actor, Verb verb, StatementTarget object, DateTime timestamp, Context context, Result result) {
        this(actor, verb, object, timestamp, context);
        if(result != null) {
            setResult(result);
        }
    }
    
    public void addParentActivity(Activity a) {
        ContextActivitiesHelper.addParentActivity(a, getContext());
    }
    
    public void addGroupingActivity(Activity a) {
        ContextActivitiesHelper.addGroupingActivity(a, getContext());
    }
    
    public void addCategoryActivity(Activity a) {
        ContextActivitiesHelper.addCategoryActivity(a, getContext());
    }
    
    public void addOtherActivity(Activity a) {
        ContextActivitiesHelper.addOtherActivity(a, getContext());
    }
    
    public void addRegistration(Integer domainSessionId) throws LmsXapiUUIDException {
        getContext().setRegistration(UUIDHelper.createUUIDFromData(domainSessionId.toString()));
    }
    
    public String getActorName() throws LmsXapiAgentException {
        return PersonaHelper.getActorName(this);
    }
    
    public String getVerbId() throws LmsInvalidStatementException {
        if(getVerb() == null || getVerb().getId() == null) {
            throw new LmsInvalidStatementException("Verb or verb.id was null!");
        }
        return getVerb().getId().toString();
    }
    
    private String getObjectId(StatementTarget target) throws LmsInvalidStatementException, LmsXapiAgentException {
        if(target instanceof Activity) {
            Activity a = (Activity) target;
            if(a.getId() == null) {
                throw new LmsInvalidStatementException("Statement Object.id was null!");
            }
            return a.getId().toString();
        } else if(target instanceof Group) {
            return PersonaHelper.parseGroupName((Group) target);
        } else if(target instanceof Agent) {
            return PersonaHelper.parseAgentName((Agent) target);
        } else {
            // SubStatements can't have subStatement objects
            throw new LmsInvalidStatementException("Statement Object was an unsupported type!"); 
        }
    }
    
    public String getObjectId() throws LmsInvalidStatementException, LmsXapiAgentException {
        StatementTarget target = getObject();
        if(target == null) {
            throw new LmsInvalidStatementException("Statement Object was null!");
        }
        if(target instanceof SubStatement) {
            SubStatement subStmt = (SubStatement) target;
            return getObjectId(subStmt.getObject());
        } else {
            return getObjectId(target);
        }
    }
    
    public String parseTimestamp() throws LmsInvalidStatementException {
        if(getTimestamp() == null) {
            throw new LmsInvalidStatementException("All GIFT statements are expected to have a timestamp set!");
        }
        return getTimestamp().toString();
    }
    
    protected static void addActivityIdToColl(List<Activity> found, List<String> coll) {
        if(CollectionUtils.isNotEmpty(found)) {
            Activity a = found.get(0);
            if(a != null && a.getId() != null) {
               coll.add(a.getId().toString());
            }
        }
    }
    
    protected static void addActivityIdToColl(ActivityTypeConcept activityType, Statement stmt, List<String> coll) throws LmsInvalidStatementException {
        addActivityIdToColl(activityType.findInstancesInStatement(stmt), coll);
    }
    
    public static void addExtensionValToColl(JsonNode node, LrsEnum key, List<String> coll) {
        JsonNode val = node.get(key.getValue());
        if(val != null) {
            coll.add(val.asText());
        }
    }
    
    public void deriveAndSetId() throws LmsStatementIdException {
        UUID stmtId;
        try {
            stmtId = deriveStatementId();
        } catch (LmsException e) {
            throw new LmsStatementIdException("Abstract method deriveStatementId threw the following exception while attempting to derive the statement's id!", e);
        }
        setId(stmtId);
    }
    
    @Override
    public Context getContext() {
        return super.getContext() != null ? super.getContext() : new Context();
    }
    
    @Override
    public Result getResult() {
        return super.getResult() != null ? super.getResult() : new Result();
    }
    
    /**
     * Classes which extend this class define how to derive the statement id.
     * 
     * @return UUID (v2 recommended but not enforced) potentially derived from statement properties
     * @throws LmsException
     */
    abstract UUID deriveStatementId() throws LmsException;
    
}
