/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.io.Serializable;
import java.util.List;

import generated.dkf.StrategyStressCategory;
import generated.dkf.Audio;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.InstructionalIntervention;
import generated.dkf.Message;
import generated.dkf.Message.Delivery;
import generated.dkf.MidLessonMedia;
import generated.dkf.PerformanceAssessment;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.Strategy;

/**
 * A utility class that provides helper methods for a {@link Strategy}.
 * 
 * @author sharrison
 */
public class StrategyUtil {
    
    /** the min value for stress of a strategy - also defined in dkf.xsd */
    public static final Double MIN_STRESS = Double.valueOf(-1.0);
    
    /** the max value for stress of a strategy - also defined in dkf.xsd */
    public static final Double MAX_STRESS = Double.valueOf(1.0);
    
    /**
     * Checks if the provided strategy activity is mandatory.
     * 
     * @param strategyActivity the activity to check.
     * @return true if the activity is mandatory; false otherwise.
     */
    public static boolean isMandatory(Serializable strategyActivity) {
        Boolean mandatory;
        if (strategyActivity instanceof InstructionalIntervention) {
            mandatory = ((InstructionalIntervention) strategyActivity).isMandatory();
        } else if (strategyActivity instanceof MidLessonMedia) {
            mandatory = ((MidLessonMedia) strategyActivity).isMandatory();
        } else if (strategyActivity instanceof PerformanceAssessment) {
            mandatory = ((PerformanceAssessment) strategyActivity).isMandatory();
        } else if (strategyActivity instanceof ScenarioAdaptation) {
            mandatory = ((ScenarioAdaptation) strategyActivity).isMandatory();
        } else {
            String msg = "The wrapper is unexpectedly wrapping type " + strategyActivity.getClass().getSimpleName();
            throw new UnsupportedOperationException(msg);
        }

        return Boolean.TRUE.equals(mandatory);
    }
    
    /**
     * Return the enumerated strategy stress category for the strategy provided.
     * 
     * @param strategyActivity either a {@link InstructionalIntervention} or {@link ScenarioAdaptation} to 
     * get the strategy stress category value from.  
     * @return can return null, otherwise returns the authored strategy stress category value of the provided strategy.
     */
    public static StrategyStressCategory getStrategyStressCategory(Serializable strategyActivity){
        
        StrategyStressCategory category = null;
        if (strategyActivity instanceof InstructionalIntervention) {
            InstructionalIntervention iInterv = (InstructionalIntervention)strategyActivity;
            category = iInterv.getStressCategory();
        }else if (strategyActivity instanceof ScenarioAdaptation) {
            ScenarioAdaptation sadapt = (ScenarioAdaptation)strategyActivity;
            category = getStrategyStressCategory(sadapt.getEnvironmentAdaptation());
        }
        
        return category;
    }
    
    /**
     * Return the enumerated strategy stress category for the environment adaptation provided.
     * 
     * @param eAdapt an environment adaptation to get the stress category for.
     * @return can return null, otherwise returns the stress category value of the provided environment adaptation.
     */
    public static StrategyStressCategory getStrategyStressCategory(EnvironmentAdaptation eAdapt){
        
        StrategyStressCategory category = null;
        if(eAdapt == null) {
            return category;
        }
        
        Serializable type = eAdapt.getType();
        if(type instanceof EnvironmentAdaptation.Fog) {
            category = EnvironmentAdaptation.Fog.STRESS_CATEGORY;
        }else if(type instanceof EnvironmentAdaptation.CreateActors) {
            EnvironmentAdaptation.CreateActors cActor = (EnvironmentAdaptation.CreateActors)type;
            category = cActor.getStressCategory();
        }else if(type instanceof EnvironmentAdaptation.CreateBreadcrumbs) {
            EnvironmentAdaptation.CreateBreadcrumbs cBread = (EnvironmentAdaptation.CreateBreadcrumbs)type;
            category = cBread.getStressCategory();
        }else if(type instanceof EnvironmentAdaptation.Endurance) {
            EnvironmentAdaptation.Endurance endurance = (EnvironmentAdaptation.Endurance)type;
            category = endurance.getStressCategory();
        }else if(type instanceof EnvironmentAdaptation.FatigueRecovery) {
            EnvironmentAdaptation.FatigueRecovery fatigue = (EnvironmentAdaptation.FatigueRecovery)type;
            category = fatigue.getStressCategory();
        }else if(type instanceof EnvironmentAdaptation.HighlightObjects) {
            EnvironmentAdaptation.HighlightObjects highlight = (EnvironmentAdaptation.HighlightObjects)type;
            category = highlight.getStressCategory();
        }else if(type instanceof EnvironmentAdaptation.Overcast) {
            category = EnvironmentAdaptation.Overcast.STRESS_CATEGORY;
        }else if(type instanceof EnvironmentAdaptation.Rain) {
            category = EnvironmentAdaptation.Rain.STRESS_CATEGORY;
        }else if(type instanceof EnvironmentAdaptation.RemoveActors) {
            EnvironmentAdaptation.RemoveActors rActors = (EnvironmentAdaptation.RemoveActors)type;
            category = rActors.getStressCategory();
        }else if(type instanceof EnvironmentAdaptation.RemoveBreadcrumbs) {
            EnvironmentAdaptation.RemoveBreadcrumbs rBread = (EnvironmentAdaptation.RemoveBreadcrumbs)type;
            category = rBread.getStressCategory();
        }else if(type instanceof EnvironmentAdaptation.RemoveHighlightOnObjects) {
            EnvironmentAdaptation.RemoveHighlightOnObjects rHighlight = (EnvironmentAdaptation.RemoveHighlightOnObjects)type;
            category = rHighlight.getStressCategory();
        }else if(type instanceof EnvironmentAdaptation.TimeOfDay) {
            category = EnvironmentAdaptation.TimeOfDay.STRESS_CATEGORY;
        }else if(type instanceof EnvironmentAdaptation.Script) {
            EnvironmentAdaptation.Script script = (EnvironmentAdaptation.Script)type;
            category = script.getStressCategory();
        }else if(type instanceof EnvironmentAdaptation.Teleport) {
            EnvironmentAdaptation.Teleport teleport = (EnvironmentAdaptation.Teleport)type;
            category = teleport.getStressCategory();
        }
        
        return category;
    }

    /**
     * Sets the mandatory flag for the provided strategy activity.
     * 
     * @param strategyActivity the activity to update.
     * @param mandatory true to mark the activity as mandatory; false otherwise.
     */
    public static void setMandatory(Serializable strategyActivity, boolean mandatory) {
        if (strategyActivity instanceof InstructionalIntervention) {
            ((InstructionalIntervention) strategyActivity).setMandatory(mandatory);
        } else if (strategyActivity instanceof MidLessonMedia) {
            ((MidLessonMedia) strategyActivity).setMandatory(mandatory);
        } else if (strategyActivity instanceof PerformanceAssessment) {
            ((PerformanceAssessment) strategyActivity).setMandatory(mandatory);
        } else if (strategyActivity instanceof ScenarioAdaptation) {
            ((ScenarioAdaptation) strategyActivity).setMandatory(mandatory);
        } else {
            String msg = "The wrapper is unexpectedly wrapping type " + strategyActivity.getClass().getSimpleName();
            throw new UnsupportedOperationException(msg);
        }
    }

    /**
     * Checks if the provided strategy's activities are only directed at the OC.
     * 
     * @param strategy the strategy to check.
     * @return true if the strategy's activities are only directed at the OC;
     *         false otherwise.
     */
    public static boolean isToControllerOnly(Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        final List<Serializable> strategyActivities = strategy.getStrategyActivities();
        if (CollectionUtils.isEmpty(strategyActivities)) {
            return false;
        }

        /* Check if the strategy only contains 'to controller' messages */
        for (Serializable activity : strategyActivities) {
            if (!(activity instanceof InstructionalIntervention)) {
                return false;
            }

            InstructionalIntervention ii = (InstructionalIntervention) activity;

            if (CollectionUtils.isNotEmpty(ii.getFeedback().getTeamRef())) {
                return false;
            }

            Serializable presentation = ii.getFeedback().getFeedbackPresentation();
            if (presentation instanceof Message) {
                Message message = (Message) presentation;
                final Delivery delivery = message.getDelivery();
                if (delivery == null || delivery.getToObserverController() == null) {
                    return false;
                }
            } else if (presentation instanceof Audio) {
                Audio audio = (Audio) presentation;
                if (audio.getToObserverController() == null) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }
}
