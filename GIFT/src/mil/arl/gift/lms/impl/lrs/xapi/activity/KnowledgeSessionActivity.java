package mil.arl.gift.lms.impl.lrs.xapi.activity;

import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;

import mil.arl.gift.common.course.dkf.session.IndividualKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;

/**
 * Activity representation of a Knowledge Session
 * 
 * @author Yet Analytics
 *
 */
public class KnowledgeSessionActivity extends AbstractGiftActivity {
    
    private static final String individualSlug = "knowledge.session.individual";
    private static final String teamSlug = "knowledge.session.team";
    
    /**
     * Creates knowledge session Activity.
     * 
     * @param slug - distinguish between individual and team knowledge session
     * @param sessionName - name of the session the activity is representing
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    private KnowledgeSessionActivity(String slug, String sessionName) throws LmsXapiActivityException {
        super(createId(slug, sessionName), sessionName);
    }
    
    /**
     * Create Knowledge Session Activity from Individual Knowledge Session.
     * 
     * @param knowledgeSession - Individual Knowledge Session to convert to activity.
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public KnowledgeSessionActivity(IndividualKnowledgeSession knowledgeSession) throws LmsXapiActivityException {
        this(individualSlug, knowledgeSession.getNameOfSession());
    }
    
    /**
     * Create Knowledge Session Activity from Team Knowledge Session.
     * 
     * @param knowledgeSession - Team Knowledge Session to convert to activity.
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public KnowledgeSessionActivity(TeamKnowledgeSession knowledgeSession) throws LmsXapiActivityException {
        this(teamSlug, knowledgeSession.getNameOfSession());
    }
    
    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        return parseFromStatementTarget(statement.getObject());
    }
}
