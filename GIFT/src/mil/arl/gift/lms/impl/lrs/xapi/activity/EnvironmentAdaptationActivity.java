package mil.arl.gift.lms.impl.lrs.xapi.activity;

import java.io.Serializable;
import java.net.URISyntaxException;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityConcepts;

/**
 * Activity representation of an Environment Adaptation
 * 
 * @author Yet Analytics
 *
 */
public class EnvironmentAdaptationActivity extends AbstractGiftActivity {

    /**
     * Converts from static / canonical Activity found in xAPI Profile to Activity
     * 
     * @param ac - static / canonical Activity parsed from xAPI Profile
     * 
     * @throws LmsXapiActivityException when unable to convert to Activity
     * @throws URISyntaxException when activity id invalid
     */
    protected EnvironmentAdaptationActivity(ItsActivityConcepts ac) throws LmsXapiActivityException, URISyntaxException {
        super(ac.asActivity());
    }

    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        // Environment Adaptation Activity expected to be (Sub)Statement Object
        return parseFromStatementTarget(statement.getObject());
    }
    
    public static EnvironmentAdaptationActivity dispatch(generated.dkf.EnvironmentAdaptation adapt) throws LmsXapiActivityException {
        Serializable kind = adapt.getType();
        try {
            if(kind instanceof EnvironmentAdaptation.Overcast) {
                return EnvironmentAdaptationActivity.Overcast.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.Fog) {
                return EnvironmentAdaptationActivity.Fog.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.Rain) {
                return EnvironmentAdaptationActivity.Rain.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.Script) {
                return EnvironmentAdaptationActivity.Script.getInstance();  
            } else if(kind instanceof EnvironmentAdaptation.CreateActors) {
                return EnvironmentAdaptationActivity.CreateActors.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.RemoveActors) {
                return EnvironmentAdaptationActivity.RemoveActors.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.Teleport) {
                return EnvironmentAdaptationActivity.Teleport.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.FatigueRecovery) {
                return EnvironmentAdaptationActivity.FatigueRecovery.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.Endurance) {
                return EnvironmentAdaptationActivity.Endurance.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.HighlightObjects) {
                return EnvironmentAdaptationActivity.HighlightObjects.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.RemoveHighlightOnObjects) {
                return EnvironmentAdaptationActivity.RemoveHighlight.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.CreateBreadcrumbs) {
                return EnvironmentAdaptationActivity.CreateBreadcrumbs.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.RemoveBreadcrumbs) {
                return EnvironmentAdaptationActivity.RemoveBreadcrumbs.getInstance();
            } else if(kind instanceof EnvironmentAdaptation.TimeOfDay) {
                Serializable type = ((EnvironmentAdaptation.TimeOfDay) kind).getType();
                if(type instanceof EnvironmentAdaptation.TimeOfDay.Midnight) {
                    return EnvironmentAdaptationActivity.Midnight.getInstance();
                } else if(type instanceof EnvironmentAdaptation.TimeOfDay.Dawn) {
                    return EnvironmentAdaptationActivity.Dawn.getInstance();
                } else if(type instanceof EnvironmentAdaptation.TimeOfDay.Dusk) {
                    return EnvironmentAdaptationActivity.Dusk.getInstance();
                } else if(type instanceof EnvironmentAdaptation.TimeOfDay.Midday) {
                    return EnvironmentAdaptationActivity.Midday.getInstance();
                } else {
                    throw new LmsXapiActivityException("unknown TimeOfDay adaptation type! "+type);
                }
            }
            else {
                throw new LmsXapiActivityException("unknown environment adaptation type! "+kind);
            }
        } catch (LmsXapiProfileException | URISyntaxException e) {
            throw new LmsXapiActivityException("Unable to init Environment Adaptation Activity for: "+kind, e);
        }
    }
    
    public static class Overcast extends EnvironmentAdaptationActivity {
        // Singleton
        private static Overcast instance = null;
        // Constructor
        private Overcast() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.Overcast.getInstance());
        }
        // Access
        public static Overcast getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Overcast();
            }
            return instance;
        }
    }
    
    public static class Fog extends EnvironmentAdaptationActivity {
        // Singleton
        private static Fog instance = null;
        // Constructor
        private Fog() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.Fog.getInstance());
        }
        // Access
        public static Fog getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Fog();
            }
            return instance;
        }
    }
    
    public static class Rain extends EnvironmentAdaptationActivity {
        // Singleton
        private static Rain instance = null;
        // Constructor
        private Rain() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.Rain.getInstance());
        }
        // Access
        public static Rain getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Rain();
            }
            return instance;
        }
    }
    
    public static class Midnight extends EnvironmentAdaptationActivity {
        // Singleton
        private static Midnight instance = null;
        // Constructor
        private Midnight() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.Midnight.getInstance());
        }
        // Access
        public static Midnight getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Midnight();
            }
            return instance;
        }
    }
    
    public static class Dawn extends EnvironmentAdaptationActivity {
        // Singleton
        private static Dawn instance = null;
        // Constructor
        private Dawn() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.Dawn.getInstance());
        }
        // Access
        public static Dawn getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Dawn();
            }
            return instance;
        }
    }
    
    public static class Midday extends EnvironmentAdaptationActivity {
        // Singleton
        private static Midday instance = null;
        // Constructor
        private Midday() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.Midday.getInstance());
        }
        // Access
        public static Midday getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Midday();
            }
            return instance;
        }
    }
    
    public static class Dusk extends EnvironmentAdaptationActivity {
        // Singleton
        private static Dusk instance = null;
        // Constructor
        private Dusk() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.Dusk.getInstance());
        }
        // Access
        public static Dusk getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Dusk();
            }
            return instance;
        }
    }
    
    public static class Script extends EnvironmentAdaptationActivity {
        // Singleton
        private static Script instance = null;
        // Constructor
        private Script() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.Script.getInstance());
        }
        // Access
        public static Script getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Script();
            }
            return instance;
        }
    }
    
    public static class FatigueRecovery extends EnvironmentAdaptationActivity {
        // Singleton
        private static FatigueRecovery instance = null;
        // Constructor
        private FatigueRecovery() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.FatigueRecovery.getInstance());
        }
        // Access
        public static FatigueRecovery getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new FatigueRecovery();
            }
            return instance;
        }
    }
    
    public static class Endurance extends EnvironmentAdaptationActivity {
        // Singleton
        private static Endurance instance = null;
        // Constructor
        private Endurance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.Endurance.getInstance());
        }
        // Access
        public static Endurance getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Endurance();
            }
            return instance;
        }
    }
    
    public static class Teleport extends EnvironmentAdaptationActivity {
        // Singleton
        private static Teleport instance = null;
        // Constructor
        private Teleport() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.Teleport.getInstance());
        }
        // Access
        public static Teleport getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new Teleport();
            }
            return instance;
        }
    }
    
    public static class CreateActors extends EnvironmentAdaptationActivity {
        // Singleton
        private static CreateActors instance = null;
        // Constructor
        private CreateActors() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.CreateActors.getInstance());
        }
        // Access
        public static CreateActors getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new CreateActors();
            }
            return instance;
        }
    }
    
    public static class RemoveActors extends EnvironmentAdaptationActivity {
        // Singleton
        private static RemoveActors instance = null;
        // Constructor
        private RemoveActors() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.RemoveActors.getInstance());
        }
        // Access
        public static RemoveActors getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new RemoveActors();
            }
            return instance;
        }
    }
    
    public static class HighlightObjects extends EnvironmentAdaptationActivity {
        // Singleton
        private static HighlightObjects instance = null;
        // Constructor
        private HighlightObjects() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.HighlightObjects.getInstance());
        }
        // Access
        public static HighlightObjects getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new HighlightObjects();
            }
            return instance;
        }
    }
    
    public static class RemoveHighlight extends EnvironmentAdaptationActivity {
        // Singleton
        private static RemoveHighlight instance = null;
        // Constructor
        private RemoveHighlight() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.RemoveHighlight.getInstance());
        }
        // Access
        public static RemoveHighlight getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new RemoveHighlight();
            }
            return instance;
        }
    }
    
    public static class CreateBreadcrumbs extends EnvironmentAdaptationActivity {
        // Singleton
        private static CreateBreadcrumbs instance = null;
        // Constructor
        private CreateBreadcrumbs() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.CreateBreadcrumbs.getInstance());
        }
        // Access
        public static CreateBreadcrumbs getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new CreateBreadcrumbs();
            }
            return instance;
        }
    }
    
    public static class RemoveBreadcrumbs extends EnvironmentAdaptationActivity {
        // Singleton
        private static RemoveBreadcrumbs instance = null;
        // Constructor
        private RemoveBreadcrumbs() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            super(ItsActivityConcepts.RemoveBreadcrumbs.getInstance());
        }
        // Access
        public static RemoveBreadcrumbs getInstance() throws LmsXapiActivityException, LmsXapiProfileException, URISyntaxException {
            if(instance == null) {
                instance = new RemoveBreadcrumbs();
            }
            return instance;
        }
    }
}
