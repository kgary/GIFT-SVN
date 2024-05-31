package mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;

/**
 * xAPI Statement Templates defined in ITS xAPI Profile corresponding
 * to Assessed Assessment (summative) xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class SkillAssessmentTemplate extends StatementTemplate {
    
    protected SkillAssessmentTemplate(String id) throws LmsXapiProfileException {
        super(id, giftStatementTemplateSparqlQuery(id, true));
    }
    
    public static class GenericTemplate extends SkillAssessmentTemplate {
        // Singleton
        private static GenericTemplate instance = null;
        // Constructor
        private GenericTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#summative.assessment");
        }
        // Access
        public static GenericTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new GenericTemplate();
            }
            return instance;
        }
    }
    
    public static class GenericReplacementTemplate extends SkillAssessmentTemplate {
        // Singleton
        private static GenericReplacementTemplate instance = null;
        // Constructor
        private GenericReplacementTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#summative.assessment.replacement");
        }
        // Access
        public static GenericReplacementTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new GenericReplacementTemplate();
            }
            return instance;
        }
        
    }
    
    public static class IndividualTemplate extends SkillAssessmentTemplate {
        // Singleton
        private static IndividualTemplate instance = null;
        // Constructor
        private IndividualTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#summative.assessment.individual");
        }
        // Access
        public static IndividualTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new IndividualTemplate();
            }
            return instance;
        }
    }
    
    public static class IndividualReplacementTemplate extends SkillAssessmentTemplate {
        // Singleton
        private static IndividualReplacementTemplate instance = null;
        // Constructor
        private IndividualReplacementTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#summative.assessment.individual.replacement");
        }
        // Access
        public static IndividualReplacementTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new IndividualReplacementTemplate();
            }
            return instance;
        }
    }
    
    public static class TeamTemplate extends SkillAssessmentTemplate {
        // Singleton
        private static TeamTemplate instance = null;
        // Constructor
        private TeamTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#summative.assessment.team");
        }
        // Access
        public static TeamTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new TeamTemplate();
            }
            return instance;
        }
    }
    
    public static class TeamReplacementTemplate extends SkillAssessmentTemplate {
        // Singleton
        private static TeamReplacementTemplate instance = null;
        // Constructor
        private TeamReplacementTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#summative.assessment.team.replacement");
        }
        // Access
        public static TeamReplacementTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new TeamReplacementTemplate();
            }
            return instance;
        }
    }
    
    public static class VoidingTemplate extends SkillAssessmentTemplate {
        // Singleton
        private static VoidingTemplate instance = null;
        // Constructor
        private VoidingTemplate() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#voided.summative.assessment");
        }
        // Access
        public static VoidingTemplate getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new VoidingTemplate();
            }
            return instance;
        }
    }
}
