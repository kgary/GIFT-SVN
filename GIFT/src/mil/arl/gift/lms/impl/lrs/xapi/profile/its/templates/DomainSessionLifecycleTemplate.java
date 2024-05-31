package mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;

/**
 * xAPI Statement Templates defined in ITS xAPI Profile corresponding
 * to Started / Closed Domain Session xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class DomainSessionLifecycleTemplate extends StatementTemplate {
    
    protected DomainSessionLifecycleTemplate(String id) throws LmsXapiProfileException {
        super(id, giftStatementTemplateSparqlQuery(id, true));
    }
    
    public static class ClosedTemplate extends DomainSessionLifecycleTemplate {
        // Singleton
        private static ClosedTemplate instance = null;
        // Constructor
        private ClosedTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#closed.domain.session");
        }
        // Access
        public static ClosedTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new ClosedTemplate();
            }
            return instance;
        }
    }
    
    public static class StartedTemplate extends DomainSessionLifecycleTemplate {
        // Singleton
        private static StartedTemplate instance = null;
        // Constructor
        private StartedTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#started.domain.session");
        }
        // Access
        public static StartedTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new StartedTemplate();
            }
            return instance;
        }
    }
}
