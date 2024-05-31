package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.CourseRecordRef.AbstractCourseRecordRefId;
import mil.arl.gift.common.course.CourseRecordRef.IntCourseRecordRefId;
import mil.arl.gift.common.course.dkf.session.IndividualKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.CourseRecordActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainSessionActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.EchelonActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.KnowledgeSessionActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.LearnerStateAttributeActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.TeamRoleActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.TrainingApplicationActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.XapiActivityFormer;
import mil.arl.gift.lms.impl.lrs.xapi.profile.ActivityTypeConcept;

/**
 * Activity Type Concepts defined within the ITS xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ItsActivityTypeConcepts extends ActivityTypeConcept {
    
    protected ItsActivityTypeConcepts(String id) throws LmsXapiProfileException {
        super(id, giftConceptRelationSparqlQuery(id, true));
    }
    // Subclass for Learner State Attribute Activity Type Concept
    public static class Lsa extends ItsActivityTypeConcepts implements XapiActivityFormer<LearnerStateAttribute> {
        // Singleton
        private static Lsa instance = null;
        // Constructor
        private Lsa() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#learner.state.attribute");
        }
        // Access
        public static Lsa getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Lsa();
            }
            return instance;
        }
        // Activity formation
        @Override
        public LearnerStateAttributeActivity asActivity(LearnerStateAttribute lsa) throws LmsXapiActivityException {
            if(lsa == null) {
                throw new IllegalArgumentException("lsa can not be null!");
            }
            LearnerStateAttributeActivity a = new LearnerStateAttributeActivity(lsa);
            addToActivity(a);
            return a;
        }
    }
    // Subclass for Affective Learner State
    public static class Affective extends ItsActivityTypeConcepts {
        // Singleton
        private static Affective instance = null;
        // Constructor
        private Affective() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#learner.state.affective");
        }
        // Access
        public static Affective getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Affective();
            }
            return instance;
        }
    }
    // Subclass for Cognitive Learner State
    public static class Cognitive extends ItsActivityTypeConcepts {
        // Singleton
        private static Cognitive instance = null;
        // Constructor
        private Cognitive() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#learner.state.cognitive");
        }
        // Access
        public static Cognitive getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Cognitive();
            }
            return instance;
        }
    }
    // Subclass for Concept, Intermediate Concept and Task 
    public static class AssessmentNode extends ItsActivityTypeConcepts {
        protected AssessmentNode(String id) throws LmsXapiProfileException {
            super(id);
        }
        // Singleton
        private static AssessmentNode aInstance = null;
        // Constructor
        private AssessmentNode() throws LmsXapiProfileException {
            this("https://xapinet.org/xapi/stetmt/its/ActivityType#assessment.node");
        }
        public static AssessmentNode getInstance() throws LmsXapiProfileException {
            if(aInstance == null) {
                aInstance = new AssessmentNode();
            }
            return aInstance;
        }
        // Inner class for Concept
        public static class Concept extends AssessmentNode implements XapiActivityFormer<ConceptPerformanceState> {
            // Singleton
            private static Concept instance = null;
            // Constructor
            private Concept() throws LmsXapiProfileException {
                super("https://xapinet.org/xapi/stetmt/its/ActivityType#assessment.node.concept");
            }
            // Access
            public static Concept getInstance() throws LmsXapiProfileException {
                if(instance == null) {
                    instance = new Concept();
                }
                return instance;
            }
            // Activity formation
            @Override
            public AssessmentActivity asActivity(ConceptPerformanceState concept) throws LmsXapiActivityException {
                if(concept == null || concept.getState() == null) {
                    throw new IllegalArgumentException("Can not create xAPI Activity from null Concept or Concept with null state!");
                }
                AssessmentActivity a = new AssessmentActivity(concept);
                addToActivity(a);
                return a;   
            }
        }
        // Inner class for Intermediate Concept
        public static class ConceptIntermediate extends AssessmentNode implements XapiActivityFormer<IntermediateConceptPerformanceState> {
            // Singleton
            private static ConceptIntermediate instance = null;
            // Constructor
            private ConceptIntermediate() throws LmsXapiProfileException {
                super("https://xapinet.org/xapi/stetmt/its/ActivityType#assessment.node.concept.intermediate");
            }
            // Access
            public static ConceptIntermediate getInstance() throws LmsXapiProfileException {
                if(instance == null) {
                    instance = new ConceptIntermediate();
                }
                return instance;
            }
            // Activity formation
            @Override
            public AssessmentActivity asActivity(IntermediateConceptPerformanceState intermediateConcept) throws LmsXapiActivityException {
                if(intermediateConcept == null || intermediateConcept.getState() == null) {
                    throw new IllegalArgumentException("Can not create xAPI Activity from null Intermediate Concept or Intermediate Concept with null state!");
                }
                AssessmentActivity a = new AssessmentActivity(intermediateConcept);
                addToActivity(a);
                return a;
            }
        }
        // Inner class for Task
        public static class Task extends AssessmentNode implements XapiActivityFormer<TaskPerformanceState> {
            // Singleton
            private static Task instance = null;
            // Constructor
            private Task() throws LmsXapiProfileException {
                super("https://xapinet.org/xapi/stetmt/its/ActivityType#assessment.node.task");
            }
            // Access
            public static Task getInstance() throws LmsXapiProfileException {
                if(instance == null) {
                    instance = new Task();
                }
                return instance;
            }
            // Activity formation
            @Override
            public AssessmentActivity asActivity(TaskPerformanceState task) throws LmsXapiActivityException {
                if(task == null || task.getState() == null) {
                    throw new IllegalArgumentException("Can not create xAPI Activity from null Task or Task with null state!"); 
                }
                AssessmentActivity a = new AssessmentActivity(task);
                addToActivity(a);
                return a;
            }
        }
    }
    // Subclass for Course Record
    public static class CourseRecord extends ItsActivityTypeConcepts implements XapiActivityFormer<CourseRecordRef> {
        // Singleton
        private static CourseRecord instance = null;
        // Constructor
        private CourseRecord() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#course.record");
        }
        // Access
        public static CourseRecord getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CourseRecord();
            }
            return instance;
        }
        // Activity formation
        @Override
        public CourseRecordActivity asActivity(CourseRecordRef recordRef) throws LmsXapiActivityException {
            if(recordRef == null) {
                throw new IllegalArgumentException("recordRef can not be null!");
            }
            AbstractCourseRecordRefId ref = recordRef.getRef();
            CourseRecordActivity a;
            if(ref instanceof IntCourseRecordRefId) {
                a = new CourseRecordActivity((IntCourseRecordRefId) ref);
            } else {
                throw new LmsXapiActivityException("Course Record Ref contained unsupported ref!");
            }
            addToActivity(a);
            return a;
        }
    }
    // Subclass for Domain
    public static class Domain extends ItsActivityTypeConcepts implements XapiActivityFormer<String> {
        // Singleton
        private static Domain instance = null;
        // Constructor
        private Domain() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#domain");
        }
        // Access
        public static Domain getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Domain();
            }
            return instance;
        }
        // Activity formation
        @Override
        public DomainActivity asActivity(String domainId) throws LmsXapiActivityException {
            if(domainId == null) {
                throw new IllegalArgumentException("domainId can not be null!");
            }
            DomainActivity a = new DomainActivity(domainId);
            addToActivity(a);
            return a;
        }
    }
    // Subclass for Domain Session
    public static class DomainSession extends ItsActivityTypeConcepts implements XapiActivityFormer<Integer> {
        // Singleton
        private static DomainSession instance = null;
        // Constructor
        private DomainSession() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#domain.session");
        }
        // Access
        public static DomainSession getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new DomainSession();
            }
            return instance;
        }
        // Activity formation
        @Override
        public DomainSessionActivity asActivity(Integer domainSessionId) throws LmsXapiActivityException {
            if(domainSessionId == null) {
                throw new IllegalArgumentException("domainSessionId can not be null!");
            }
            DomainSessionActivity a = new DomainSessionActivity(domainSessionId);
            addToActivity(a);
            return a;
        }
    }
    // Subclass for Team Role
    public static class TeamRole extends ItsActivityTypeConcepts implements XapiActivityFormer<TeamMember<?>> {
        // Singleton
        private static TeamRole instance = null;
        // Constructor
        private TeamRole() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#team.role");
        }
        // Access
        public static TeamRole getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new TeamRole();
            }
            return instance;
        }
        // Activity formation
        @Override
        public TeamRoleActivity asActivity(TeamMember<?> teamRole) throws LmsXapiActivityException {
            if(teamRole == null || teamRole.getName() == null) {
                throw new IllegalArgumentException("can not create TeamRoleActivity from null teamRole, teamRole name can not be null!");
            }
            TeamRoleActivity a = new TeamRoleActivity(teamRole);
            addToActivity(a);
            return a;
        }
    }
    // Subclass for knowledge session type
    public static class KnowledgeSessionType extends ItsActivityTypeConcepts {
        // Singleton
        private static KnowledgeSessionType instance = null;
        // Constructor
        private KnowledgeSessionType() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#knowledge.session.playback");
        }
        // Access
        public static KnowledgeSessionType getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new KnowledgeSessionType();
            }
            return instance;
        }
    }
    // Subclass for individual knowledge session
    public static class KnowledgeSessionIndividual extends ItsActivityTypeConcepts implements XapiActivityFormer<IndividualKnowledgeSession> {
        // Singleton
        private static KnowledgeSessionIndividual instance = null;
        // Constructor
        private KnowledgeSessionIndividual() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#knowledge.session.individual");
        }
        // Access
        public static KnowledgeSessionIndividual getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new KnowledgeSessionIndividual();
            }
            return instance;
        }
        // Activity formation
        @Override
        public KnowledgeSessionActivity asActivity(IndividualKnowledgeSession knowledgeSession) throws LmsXapiActivityException {
            if(knowledgeSession == null || knowledgeSession.getNameOfSession() == null) {
                throw new IllegalArgumentException("knowledgeSession can not be null and must have a non-null name!");
            }
            KnowledgeSessionActivity a = new KnowledgeSessionActivity(knowledgeSession);
            addToActivity(a);
            return a;
        }
    }
    // Subclass for team knowledge session
    public static class KnowledgeSessionTeam extends ItsActivityTypeConcepts implements XapiActivityFormer<TeamKnowledgeSession> {
        // Singleton
        private static KnowledgeSessionTeam instance = null;
        // Constructor
        private KnowledgeSessionTeam() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#knowledge.session.team");
        }
        // Access
        public static KnowledgeSessionTeam getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new KnowledgeSessionTeam();
            }
            return instance;
        }
        // Activity formation
        @Override
        public KnowledgeSessionActivity asActivity(TeamKnowledgeSession knowledgeSession) throws LmsXapiActivityException {
            if(knowledgeSession == null || knowledgeSession.getNameOfSession() == null) {
                throw new IllegalArgumentException("knowledgeSession can not be null and must have a non-null name!");
            }
            KnowledgeSessionActivity a = new KnowledgeSessionActivity(knowledgeSession);
            addToActivity(a);
            return a;
        }
    }
    // Subclass for Team Echelon
    public static class TeamEchelon extends ItsActivityTypeConcepts implements XapiActivityFormer<EchelonEnum> {
        // Singleton
        private static TeamEchelon instance = null;
        // Constructor
        private TeamEchelon() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#team.echelon");
        }
        // Access
        public static TeamEchelon getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new TeamEchelon();
            }
            return instance;
        }
        // Activity formation
        @Override
        public EchelonActivity asActivity(EchelonEnum echelon) throws LmsXapiActivityException {
            if(echelon == null || echelon.getBranch() == null || echelon.getDisplayName() == null || echelon.getComponents() == null) {
                throw new IllegalArgumentException("can not create Activity from null echelon or echelon with unset properties!");
            }
            EchelonActivity a = new EchelonActivity(echelon);
            addToActivity(a);
            return a;
        }
    }
    // Subclass for Environment Adaptation
    public static class EnvironmentAdaptation extends ItsActivityTypeConcepts {
        // Singleton
        private static EnvironmentAdaptation instance = null;
        // Constructor
        private EnvironmentAdaptation() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#environment.adaptation");
        }
        // Access
        public static EnvironmentAdaptation getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new EnvironmentAdaptation();
            }
            return instance;
        }
    }
    // Subclass for Training Application Type
    public static class TrainingApplication extends ItsActivityTypeConcepts implements XapiActivityFormer<TrainingApplicationEnum> {
        // Singleton
        private static TrainingApplication instance = null;
        // Constructor
        private TrainingApplication() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ActivityType#training.application");
        }
        // Access
        public static TrainingApplication getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new TrainingApplication();
            }
            return instance;
        }
        // Activity formation
        @Override
        public TrainingApplicationActivity asActivity(TrainingApplicationEnum trainingApp) throws LmsXapiActivityException {
            if(trainingApp == null || trainingApp.getName() == null || trainingApp.getDisplayName() == null) {
                throw new IllegalArgumentException("trainingApp can not be null and must have name and display name!");
            }
            TrainingApplicationActivity a = new TrainingApplicationActivity(trainingApp);
            addToActivity(a);
            return a;
        }
    }
}
