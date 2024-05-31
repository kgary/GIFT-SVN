package mil.arl.gift.lms.impl.lrs.xapi.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import com.fasterxml.jackson.databind.JsonNode;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsStatementIdException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.CommonLrsEnum;
import mil.arl.gift.lms.impl.lrs.xapi.UUIDHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.KnowledgeSessionActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsVerbConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts.extensionObjectKeys;

/**
 * xAPI Statement corresponding to the start of an individual or team knowledge session.
 * 
 * @author Yet Analytics
 *
 */
public class StartedKnowledgeSessionStatement extends AbstractGiftStatement {

    public StartedKnowledgeSessionStatement(Agent actor, KnowledgeSessionActivity activity, DateTime timestamp) throws LmsXapiProfileException {
        super(actor, ItsVerbConcepts.Started.getInstance().asVerb(), activity, timestamp);
    }
    
    public StartedKnowledgeSessionStatement(Agent actor, KnowledgeSessionActivity activity, DateTime timestamp, Context context) throws LmsXapiProfileException {
        super(actor, ItsVerbConcepts.Started.getInstance().asVerb(), activity, timestamp, context);
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
        // Mission Metadata
        ItsContextExtensionConcepts.MissionMetadata ctxExtension;
        try {
            ctxExtension = ItsContextExtensionConcepts.MissionMetadata.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Mission Metadata Context Extension Concept!", e);
        }
        if(getContext() != null && getContext().getExtensions() != null && ctxExtension.parseFromExtensions(getContext().getExtensions()) != null) {
            JsonNode extension = ctxExtension.parseFromExtensions(getContext().getExtensions());
            addExtensionValToColl(extension, extensionObjectKeys.SOURCE, slugs);
            addExtensionValToColl(extension, extensionObjectKeys.MET, slugs);
            addExtensionValToColl(extension, extensionObjectKeys.TASK, slugs);
            addExtensionValToColl(extension, extensionObjectKeys.SITUATION, slugs);
            addExtensionValToColl(extension, extensionObjectKeys.GOALS, slugs);
            addExtensionValToColl(extension, extensionObjectKeys.CONDITION, slugs);
            addExtensionValToColl(extension, extensionObjectKeys.ROE, slugs);
            addExtensionValToColl(extension, extensionObjectKeys.THREAT_WARNING, slugs);
            addExtensionValToColl(extension, extensionObjectKeys.WEAPON_STATUS, slugs);
            addExtensionValToColl(extension, extensionObjectKeys.WEAPON_POSTURE, slugs);
        }
        // Context Activities
        ItsActivityTypeConcepts dATC, dsATC, kstATC;
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
        // -> Knowledge Session Playback Type
        try {
            kstATC = ItsActivityTypeConcepts.KnowledgeSessionType.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsStatementIdException("Unable to initialize Knowledge Session Type Activity Type Concept!", e);
        }
        addActivityIdToColl(kstATC, this, slugs);
        // derive and return UUID from slugs
        return UUIDHelper.createUUIDFromData(StringUtils.join(CommonLrsEnum.SEPERATOR_COMMA.getValue(), slugs));
    }
}
