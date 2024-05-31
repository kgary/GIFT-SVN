package mil.arl.gift.lms.impl.lrs.xapi.profile.mom;

import generated.course.ConceptNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.XapiActivityFormerTuple;
import mil.arl.gift.lms.impl.lrs.xapi.profile.ActivityTypeConcept;

/**
 * Select Activity Type Concept defined in the Master Object Model (MOM) xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class MomActivityTypeConcepts extends ActivityTypeConcept {
    
    protected MomActivityTypeConcepts(String id) throws LmsXapiProfileException {
        super(id, tlaConceptRelationSparqlQuery(id, true));
    }
    
    // Subclass for Assessment
    public static class Assessment extends MomActivityTypeConcepts implements XapiActivityFormerTuple<GradedScoreNode, ConceptNode> {
        // Singleton
        private static Assessment instance = null;
        // Constructor
        private Assessment() throws LmsXapiProfileException {
            super("https://w3id.org/xapi/tla/activity-types/assessment");
        }
        // Access
        public static Assessment getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Assessment();
            }
            return instance;
        }
        // Activity formation
        @Override
        public AssessmentActivity asActivity(GradedScoreNode node) throws LmsXapiActivityException {
            if(node == null) {
                throw new IllegalArgumentException("node can not be null!");
            }
            AssessmentActivity a = new AssessmentActivity(node);
            addToActivity(a);
            return a;
        }
        @Override
        public AssessmentActivity asActivity(GradedScoreNode node, ConceptNode cnode) throws LmsXapiActivityException {
            if(node == null || cnode == null) {
                throw new IllegalArgumentException("node and cnode can not be null!");
            }
            AssessmentActivity a = new AssessmentActivity(node, cnode);
            addToActivity(a);
            return a;
        }
    }
}
