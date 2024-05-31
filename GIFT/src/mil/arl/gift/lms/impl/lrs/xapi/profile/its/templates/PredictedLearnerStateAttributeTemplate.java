package mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;

/**
 * xAPI Statement Templates defined in ITS xAPI Profile corresponding
 * to Predicted (Affective | Cognitive) Learner State Attribute xAPI Statements.
 *
 * @author Yet Analytics
 *
 */
public class PredictedLearnerStateAttributeTemplate extends StatementTemplate {
    // Singleton
    private static PredictedLearnerStateAttributeTemplate instance = null;
    // Constructors
    protected PredictedLearnerStateAttributeTemplate(String id) throws LmsXapiProfileException {
        super(id, giftStatementTemplateSparqlQuery(id, true));
    }
    private PredictedLearnerStateAttributeTemplate() throws LmsXapiProfileException {
        this("https://xapinet.org/xapi/stetmt/its/StatementTemplate#predicted.lsa");
    }
    // Access
    public static PredictedLearnerStateAttributeTemplate getInstance() throws LmsXapiProfileException {
        if(instance == null) {
            instance = new PredictedLearnerStateAttributeTemplate();
        }
        return instance;
    }
    // Replacement Template
    public static class ReplacementTemplate extends PredictedLearnerStateAttributeTemplate {
        // Singleton
        private static ReplacementTemplate rInstance = null;
        // Constructor
        private ReplacementTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#predicted.lsa.replacement");
        }
        // Access
        public static ReplacementTemplate getInstance() throws LmsXapiProfileException {
            if(rInstance == null) {
                rInstance = new ReplacementTemplate();
            }
            return rInstance;
        }
    }
    // Voiding Template
    public static class VoidingTemplate extends PredictedLearnerStateAttributeTemplate {
        // Singleton
        private static VoidingTemplate vInstance = null;
        // Constructor
        private VoidingTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#voided.predicted.lsa");
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