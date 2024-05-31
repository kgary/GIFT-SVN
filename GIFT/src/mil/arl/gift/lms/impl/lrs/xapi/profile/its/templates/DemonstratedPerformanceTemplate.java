package mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;

/**
 * xAPI Statement Templates defined in ITS xAPI Profile corresponding 
 * to Demonstrated Performance State Attribute (formative assessment) xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class DemonstratedPerformanceTemplate extends StatementTemplate {
    
    // Singleton
    private static DemonstratedPerformanceTemplate instance = null;
    // Constructors
    protected DemonstratedPerformanceTemplate(String id) throws LmsXapiProfileException {
        super(id, giftStatementTemplateSparqlQuery(id, true));
    }
    private DemonstratedPerformanceTemplate() throws LmsXapiProfileException {
        this("https://xapinet.org/xapi/stetmt/its/StatementTemplate#demonstrated.psa");
    }
    // Access
    public static DemonstratedPerformanceTemplate getInstance() throws LmsXapiProfileException {
        if(instance == null) {
            instance = new DemonstratedPerformanceTemplate();
        }
        return instance;
    }
    
    public static class ReplacementTemplate extends DemonstratedPerformanceTemplate {
        // Singleton
        private static ReplacementTemplate rInstance = null;
        // Constructor
        private ReplacementTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#demonstrated.psa.replacement");
        }
        // Access
        public static ReplacementTemplate getInstance() throws LmsXapiProfileException {
            if(rInstance == null) {
                rInstance = new ReplacementTemplate();
            }
            return rInstance;
        }
    }
    
    public static class VoidingTemplate extends DemonstratedPerformanceTemplate {
        // Singleton
        private static VoidingTemplate vInstance = null;
        // Constructor
        private VoidingTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#voided.demonstrated.psa");
        }
        // Access
        public static VoidingTemplate getInstance() throws LmsXapiProfileException {
            if(vInstance == null) {
                vInstance = new VoidingTemplate();
            }
            return vInstance;
        }
    }
}
