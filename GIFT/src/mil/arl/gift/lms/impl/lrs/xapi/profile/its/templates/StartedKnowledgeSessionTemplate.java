package mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;

/**
 * xAPI Statement Templates defined in ITS xAPI Profile corresponding
 * to Started Knowledge Session xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class StartedKnowledgeSessionTemplate extends StatementTemplate {
    
    protected StartedKnowledgeSessionTemplate(String id) throws LmsXapiProfileException {
        super(id, giftStatementTemplateSparqlQuery(id, true));
    }
    
    // Subclass for Individual Knowledge Session Statement Object
    public static class IndividualTemplate extends StartedKnowledgeSessionTemplate {
        // Singleton
        private static IndividualTemplate instance = null;
        // Constructor
        private IndividualTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#started.knowledge.session.individual");
        }
        // Access
        public static IndividualTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new IndividualTemplate();
            }
            return instance;
        }
    }
    // Subclass for Team Knowledge Session Statement Object
    public static class TeamTemplate extends StartedKnowledgeSessionTemplate {
        // Singleton
        private static TeamTemplate instance = null;
        // Constructor
        private TeamTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#started.knowledge.session.team");
        }
        // Access
        public static TeamTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new TeamTemplate();
            }
            return instance;
        }
    }
}
