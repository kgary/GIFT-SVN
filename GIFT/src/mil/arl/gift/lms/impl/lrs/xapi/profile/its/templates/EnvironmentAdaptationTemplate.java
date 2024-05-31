package mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;

/**
 * xAPI Statement Templates defined in ITS xAPI Profile corresponding
 * to Environment Adaptation xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class EnvironmentAdaptationTemplate extends StatementTemplate {

    protected EnvironmentAdaptationTemplate(String id) throws LmsXapiProfileException {
        super(id, giftStatementTemplateSparqlQuery(id, true));
    }
    
    // Weather
    public static class Weather extends EnvironmentAdaptationTemplate {
        // Singleton
        private static Weather instance = null;
        // Constructor
        private Weather() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.weather");
        }
        // Access
        public static Weather getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Weather();
            }
            return instance;
        }
    }
    
    // Time of Day
    public static class TimeOfDay extends EnvironmentAdaptationTemplate {
        // Singleton
        private static TimeOfDay instance = null;
        // Constructor
        private TimeOfDay() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.time");
        }
        // Access
        public static TimeOfDay getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new TimeOfDay();
            }
            return instance;
        }
    }
    
    // Script
    public static class Script extends EnvironmentAdaptationTemplate {
        // Singleton
        private static Script instance = null;
        // Constructor
        private Script() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.script");
        }
        // Access
        public static Script getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Script();
            }
            return instance;
        }
    }
    
    // Player Status - flat
    public static class PlayerStatus extends EnvironmentAdaptationTemplate {
        // Singleton
        private static PlayerStatus instance = null;
        // Constructor
        private PlayerStatus() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.player.status.actor");
        }
        // Access
        public static PlayerStatus getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new PlayerStatus();
            }
            return instance;
        }
    }
    
    // Player Status - nested
    public static class PlayerStatusNested extends EnvironmentAdaptationTemplate {
        // Singleton
        private static PlayerStatusNested instance = null;
        // Constructor
        private PlayerStatusNested() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.player.status.agent");
        }
        // Access
        public static PlayerStatusNested getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new PlayerStatusNested();
            }
            return instance;
        }
    }
    
    // Teleport - flat
    public static class Teleport extends EnvironmentAdaptationTemplate {
        // Singleton
        private static Teleport instance = null;
        // Constructor
        private Teleport() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.teleport.actor");
        }
        // Access
        public static Teleport getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Teleport();
            }
            return instance;
        }
    }
    
    // Teleport - nested
    public static class TeleportNested extends EnvironmentAdaptationTemplate {
        // Singleton
        private static TeleportNested instance = null;
        // Constructor
        private TeleportNested() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.teleport.agent");
        }
        // Access
        public static TeleportNested getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new TeleportNested();
            }
            return instance;
        }
    }
    
    // Create Actors
    public static class CreateActors extends EnvironmentAdaptationTemplate {
        // Singleton
        private static CreateActors instance = null;
        // Constructor
        private CreateActors() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.create.actors");
        }
        // Access
        public static CreateActors getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CreateActors();
            }
            return instance;
        }
    }
    
    // Remove Actors
    public static class RemoveActors extends EnvironmentAdaptationTemplate {
        // Singleton
        private static RemoveActors instance = null;
        // Constructor
        private RemoveActors() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.remove.actors");
        }
        // Access
        public static RemoveActors getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new RemoveActors();
            }
            return instance;
        }
    }
    
    // Highlight Objects - Location
    public static class HighlightObjectsLocation extends EnvironmentAdaptationTemplate {
        // Singleton
        private static HighlightObjectsLocation instance = null;
        // Constructor
        private HighlightObjectsLocation() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.highlight.location");
        }
        // Access
        public static HighlightObjectsLocation getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new HighlightObjectsLocation();
            }
            return instance;
        }
    }
    
    // Highlight Objects - Actor
    public static class HighlightObjectsActor extends EnvironmentAdaptationTemplate {
        // Singleton
        private static HighlightObjectsActor instance = null;
        // Constructor
        private HighlightObjectsActor() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.highlight.actor");
        }
        // Access
        public static HighlightObjectsActor getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new HighlightObjectsActor();
            }
            return instance;
        }
    }
    
    // Highlight Objects - Agent
    public static class HighlightObjectsAgent extends EnvironmentAdaptationTemplate {
        // Singleton
        private static HighlightObjectsAgent instance = null;
        // Constructor
        private HighlightObjectsAgent() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.highlight.agent");
        }
        // Access
        public static HighlightObjectsAgent getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new HighlightObjectsAgent();
            }
            return instance;
        }
    }
    
    // Remove Highlight
    public static class RemoveHighlight extends EnvironmentAdaptationTemplate {
        // Singleton
        private static RemoveHighlight instance = null;
        // Constructor
        private RemoveHighlight() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.highlight.remove");
        }
        // Access
        public static RemoveHighlight getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new RemoveHighlight();
            }
            return instance;
        }
    }
    
    // Create Bread crumbs - Actor
    public static class CreateBreadcrumbsActor extends EnvironmentAdaptationTemplate {
        // Singleton
        private static CreateBreadcrumbsActor instance = null;
        // Constructor
        private CreateBreadcrumbsActor() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.breadcrumbs.actor");
        }
        // Access
        public static CreateBreadcrumbsActor getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CreateBreadcrumbsActor();
            }
            return instance;
        }
    }
    
    // Create Bread crumbs - Agent
    public static class CreateBreadcrumbsAgent extends EnvironmentAdaptationTemplate {
        // Singleton
        private static CreateBreadcrumbsAgent instance = null;
        // Constructor
        private CreateBreadcrumbsAgent() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.breadcrumbs.agent");
        }
        // Access
        public static CreateBreadcrumbsAgent getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CreateBreadcrumbsAgent();
            }
            return instance;
        }
    }
    
    // Create Bread crumbs - Group
    public static class CreateBreadcrumbsGroup extends EnvironmentAdaptationTemplate {
        // Singleton
        private static CreateBreadcrumbsGroup instance = null;
        // Constructor
        private CreateBreadcrumbsGroup() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.breadcrumbs.group");
        }
        // Access
        public static CreateBreadcrumbsGroup getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CreateBreadcrumbsGroup();
            }
            return instance;
        }
    }
    
    // Remove Bread crumbs
    public static class RemoveBreadcrumbsActor extends EnvironmentAdaptationTemplate {
        // Singleton
        private static RemoveBreadcrumbsActor instance = null;
        // Constructor
        private RemoveBreadcrumbsActor() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.breadcrumbs.actor.remove");
        }
        // Access
        public static RemoveBreadcrumbsActor getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new RemoveBreadcrumbsActor();
            }
            return instance;
        }
    }
    
    public static class RemoveBreadcrumbsAgent extends EnvironmentAdaptationTemplate {
        // Singleton
        private static RemoveBreadcrumbsAgent instance = null;
        // Constructor
        private RemoveBreadcrumbsAgent() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.breadcrumbs.agent.remove");
        }
        // Access
        public static RemoveBreadcrumbsAgent getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new RemoveBreadcrumbsAgent();
            }
            return instance;
        }
    }
    
    public static class RemoveBreadcrumbsGroup extends EnvironmentAdaptationTemplate {
        // Singleton
        private static RemoveBreadcrumbsGroup instance = null;
        // Constructor
        private RemoveBreadcrumbsGroup() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/StatementTemplate#environment.adaptation.breadcrumbs.group.remove");
        }
        // Access
        public static RemoveBreadcrumbsGroup getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new RemoveBreadcrumbsGroup();
            }
            return instance;
        }
    }
}
