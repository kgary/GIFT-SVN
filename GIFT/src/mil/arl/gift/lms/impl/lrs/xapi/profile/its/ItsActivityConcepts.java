package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.ActivityConcept;

/**
 * Static / canonical xAPI Activity Concepts defined within the ITS xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ItsActivityConcepts extends ActivityConcept {

    // constructor that calls up to super
    protected ItsActivityConcepts(String id) throws LmsXapiProfileException {
        super(id, giftCanonicalActivitySparqlQuery(id, true));
    }

    public static class CognitiveStateActivity extends ItsActivityConcepts {
        // Singleton
        private static CognitiveStateActivity instance = null;
        // Constructor
        private CognitiveStateActivity() throws LmsXapiProfileException {
            super("activityId:uri/its/learner.state.cognitive");
        }
        // Access
        public static CognitiveStateActivity getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CognitiveStateActivity();
            }
            return instance;
        }
    }

    public static class AffectiveStateActivity extends ItsActivityConcepts {
        // Singleton
        private static AffectiveStateActivity instance = null;
        // Constructor
        private AffectiveStateActivity() throws LmsXapiProfileException {
            super("activityId:uri/its/learner.state.affective");
        }
        // Access
        public static AffectiveStateActivity getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new AffectiveStateActivity();
            }
            return instance;
        }
    }
    
    public static class ActiveKnowledgeSessionActivity extends ItsActivityConcepts {
        // Singleton
        private static ActiveKnowledgeSessionActivity instance = null;
        // Constructor
        private ActiveKnowledgeSessionActivity() throws LmsXapiProfileException {
            super("activityId:uri/its/knowledge.session.playback/active");
        }
        // Access
        public static ActiveKnowledgeSessionActivity getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new ActiveKnowledgeSessionActivity();
            }
            return instance;
        }
    }
    
    public static class ActivePlaybackKnowledgeSessionActivity extends ItsActivityConcepts {
        // Singleton
        private static ActivePlaybackKnowledgeSessionActivity instance = null;
        // Constructor
        private ActivePlaybackKnowledgeSessionActivity() throws LmsXapiProfileException {
            super("activityId:uri/its/knowledge.session.playback/active.playback");
        }
        // Access
        public static ActivePlaybackKnowledgeSessionActivity getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new ActivePlaybackKnowledgeSessionActivity();
            }
            return instance;
        }
    }
    
    public static class Overcast extends ItsActivityConcepts {
        // Singleton
        private static Overcast instance = null;
        // Constructor
        private Overcast() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/overcast");
        }
        // Access
        public static Overcast getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Overcast();
            }
            return instance;
        }
    }
    
    public static class Fog extends ItsActivityConcepts {
        // Singleton
        private static Fog instance = null;
        // Constructor
        private Fog() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/fog");
        }
        // Access
        public static Fog getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Fog();
            }
            return instance;
        }
    }
    
    public static class Rain extends ItsActivityConcepts {
        // Singleton
        private static Rain instance = null;
        // Constructor
        private Rain() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/rain");
        }
        // Access
        public static Rain getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Rain();
            }
            return instance;
        }
    }
    
    public static class Midnight extends ItsActivityConcepts {
        // Singleton
        private static Midnight instance = null;
        // Constructor
        private Midnight() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/midnight");
        }
        // Access
        public static Midnight getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Midnight();
            }
            return instance;
        }
    }
    
    public static class Dawn extends ItsActivityConcepts {
        // Singleton
        private static Dawn instance = null;
        // Constructor
        private Dawn() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/dawn");
        }
        // Access
        public static Dawn getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Dawn();
            }
            return instance;
        }
    }
    
    public static class Midday extends ItsActivityConcepts {
        // Singleton
        private static Midday instance = null;
        // Constructor
        private Midday() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/midday");
        }
        // Access
        public static Midday getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Midday();
            }
            return instance;
        }
    }
    
    public static class Dusk extends ItsActivityConcepts {
        // Singleton
        private static Dusk instance = null;
        // Constructor
        private Dusk() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/dusk");
        }
        // Access
        public static Dusk getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Dusk();
            }
            return instance;
        }
    }
    
    public static class Script extends ItsActivityConcepts {
        // Singleton
        private static Script instance = null;
        // Constructor
        private Script() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/script");
        }
        // Access
        public static Script getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Script();
            }
            return instance;
        }
    }
    
    public static class FatigueRecovery extends ItsActivityConcepts {
        // Singleton
        private static FatigueRecovery instance = null;
        // Constructor
        private FatigueRecovery() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/fatigue.recovery");
        }
        // Access
        public static FatigueRecovery getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new FatigueRecovery();
            }
            return instance;
        }
    }
    
    public static class Endurance extends ItsActivityConcepts {
        // Singleton
        private static Endurance instance = null;
        // Constructor
        private Endurance() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/endurance");
        }
        // Access
        public static Endurance getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Endurance();
            }
            return instance;
        }
    }
    
    public static class Teleport extends ItsActivityConcepts {
        // Singleton
        private static Teleport instance = null;
        // Constructor
        private Teleport() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/teleport");
        }
        // Access
        public static Teleport getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Teleport();
            }
            return instance;
        }
    }
    
    public static class CreateActors extends ItsActivityConcepts {
        // Singleton
        private static CreateActors instance = null;
        // Constructor
        private CreateActors() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/create.actors");
        }
        // Access
        public static CreateActors getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CreateActors();
            }
            return instance;
        }
    }
    
    public static class RemoveActors extends ItsActivityConcepts {
        // Singleton
        private static RemoveActors instance = null;
        // Constructor
        private RemoveActors() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/remove.actors");
        }
        // Access
        public static RemoveActors getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new RemoveActors();
            }
            return instance;
        }
    }
    
    public static class HighlightObjects extends ItsActivityConcepts {
        // Singleton
        private static HighlightObjects instance = null;
        // Constructor
        private HighlightObjects() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/highlight.objects");
        }
        // Access
        public static HighlightObjects getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new HighlightObjects();
            }
            return instance;
        }
    }
    
    public static class RemoveHighlight extends ItsActivityConcepts {
        // Singleton
        private static RemoveHighlight instance = null;
        // Constructor
        private RemoveHighlight() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/remove.highlight");
        }
        // Access
        public static RemoveHighlight getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new RemoveHighlight();
            }
            return instance;
        }
    }
    
    public static class CreateBreadcrumbs extends ItsActivityConcepts {
        // Singleton
        private static CreateBreadcrumbs instance = null;
        // Constructor
        private CreateBreadcrumbs() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/create.breadcrumbs");
        }
        // Access
        public static CreateBreadcrumbs getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CreateBreadcrumbs();
            }
            return instance;
        }
    }
    
    public static class RemoveBreadcrumbs extends ItsActivityConcepts {
        // Singleton
        private static RemoveBreadcrumbs instance = null;
        // Constructor
        private RemoveBreadcrumbs() throws LmsXapiProfileException {
            super("activityId:uri/its/environment.adaptation/remove.breadcrumbs");
        }
        // Access
        public static RemoveBreadcrumbs getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new RemoveBreadcrumbs();
            }
            return instance;
        }
    }
    
}
