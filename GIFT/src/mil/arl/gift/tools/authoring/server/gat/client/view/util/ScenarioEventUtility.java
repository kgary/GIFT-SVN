/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import generated.dkf.AvailableLearnerActions;
import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.PlacesOfInterest;
import generated.dkf.TeamOrganization;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.JumpToEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PopulateScenarioTreesEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestResumeEditEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ReferencesChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;

/**
 * Provides global utilities for the DKF editor events. Should not be used outside of the
 * {@link mil.arl.gift.tools.authoring.server.gat.client.view.dkf dkf editor package}.
 * 
 * @author sharrison
 */
public class ScenarioEventUtility {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScenarioEventUtility.class.getName());

    /**
     * Fires an event to jump to the provided scenario object.
     * 
     * @param scenarioObject the scenario object to jump to. If null, the current selection will be
     *        cleared.
     */
    public static void fireJumpToEvent(Serializable scenarioObject) {
        fireJumpToEvent(scenarioObject, null, false);
    }

    /**
     * Fires an event to jump to the provided scenario object.
     * 
     * @param scenarioObject the scenario object to jump to. If null, the current selection will be
     *        cleared.
     * @param childScenarioObject the child scenario object of the scenario object being jumped to. 
     * This is optional and is used to open a subcomponent on the parent scenario object editor.
     * @param pinTabOnJump true to pin the current tab before jumping to the provided location. This
     *        will only take effect if the scenario object is not null.
     */
    public static void fireJumpToEvent(Serializable scenarioObject, Serializable childScenarioObject, boolean pinTabOnJump) {
        JumpToEvent jumpEvent;
        if (scenarioObject == null) {
            jumpEvent = new JumpToEvent();
        } else {
            jumpEvent = new JumpToEvent(scenarioObject);
            jumpEvent.setPinTabOnJump(pinTabOnJump);
            jumpEvent.setChildScenarioObject(childScenarioObject);
        }

        SharedResources.getInstance().getEventBus().fireEvent(jumpEvent);
    }

    /**
     * Fires an event to jump to the {@link PlacesOfInterestPanel}.
     */
    public static void fireJumpToPlacesOfInterest() {
        PlacesOfInterest placesOfInterest = ScenarioClientUtility.getPlacesOfInterest();
        if (placesOfInterest == null) {
            logger.severe("Tried to jump to places Of Interest, but the places Of Interest object is null.");
            return;
        }

        fireJumpToEvent(placesOfInterest, null, true);
    }
    
    /**
     * Fires an event to jump to the {@link TeamOrganizationPanel}.
     */
    public static void fireJumpToTeamOrganization() {
        TeamOrganization organization = ScenarioClientUtility.getTeamOrganization();
        if (organization == null) {
            logger.severe("Tried to jump to team organization, but the team organization object is null.");
            return;
        }

        fireJumpToEvent(organization, null, true);
    }


    /**
     * Fires an event to jump to the {@link LearnerActionsPanel}.
     */
    public static void fireJumpToLearnerActions() {
        AvailableLearnerActions availableLearnerActions = ScenarioClientUtility.getAvailableLearnerActions();
        fireJumpToEvent(availableLearnerActions, null, true);
    }

    /**
     * Fires a dirty editor event. No validation will occur since a source was not set. Use
     * {@link #fireDirtyEditorEvent(Serializable)} to trigger validation.
     */
    public static void fireDirtyEditorEvent() {
        fireDirtyEditorEvent(null);
    }
    
    /**
     * Finds the parent Concept of the specified Condition
     * @param conditionToFind the Condition whose parent will be identified
     * @return a Concept that is the parent of conditionToFind. Null if no parent Concept was found.
     */
    private static Concept getParentOfCondition(Condition conditionToFind) {
    	for (Concept currentConcept : ScenarioClientUtility.getUnmodifiableConceptList()) {
    		if (currentConcept.getConditionsOrConcepts() instanceof Conditions) {
    			for (Condition conditionToCheck : ((Conditions) currentConcept.getConditionsOrConcepts()).getCondition()) {
    				if (conditionToCheck == conditionToFind) {
    					return currentConcept;
    				}
    			}
    		}
    	}
    	
    	return null;
    }

    /**
     * Fires a dirty editor event. This should be called after all relevant {@link ValidationStatus
     * validation statuses} have been updated.
     * 
     * @param sourceScenarioObject the scenario object that was the origin of the dirty editor
     *        event. Set to null if the event should be untraceable (e.g. no validation needs to
     *        occur).
     */
    public static void fireDirtyEditorEvent(Serializable sourceScenarioObject) {
    	
    	/*
    	 * Before firing the dirty event, if the sourceScenarioObject is a Concept or Condition,
    	 * copy the value to any Concepts that have a matching externalSourceId.
    	 */
        List<Concept> allConceptsList = new ArrayList<>();
    	allConceptsList.addAll(ScenarioClientUtility.getUnmodifiableConceptList());
    	
    	if (sourceScenarioObject instanceof Concept || sourceScenarioObject instanceof Condition) {
    		Concept alteredConcept = null;
    		if (sourceScenarioObject instanceof Concept) {
    			alteredConcept = (Concept) sourceScenarioObject;
    		} else if (sourceScenarioObject instanceof Condition) {
    			alteredConcept = getParentOfCondition((Condition) sourceScenarioObject);
    		}
    		
    		if (alteredConcept == null || StringUtils.isBlank(alteredConcept.getExternalSourceId())) {
    			SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(sourceScenarioObject));
    		} else {
    		    
    		    /* This is a concept with an external source ID, so we need to make sure any duplicates
    		     * with the same ID are kept in sync */
    			final BigInteger alteredNodeId = alteredConcept.getNodeId();
		    	SharedResources.getInstance().getRpcService().copyChangesToConceptsWithDuplicateExternalSourceIds(alteredConcept, ScenarioClientUtility.getScenario(), new AsyncCallback<GenericRpcResponse<Map<BigInteger, Concept>>>() {
		
					@Override
					public void onFailure(Throwable caught) {

						DetailedException e = new DetailedException("An unexpected error ocurred on the server", caught.getMessage(),
                                caught);

                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(),
                                e.getErrorStackTrace());
                        dialog.center();

                        // Send the dirty event even if there is a failure.
				        SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(sourceScenarioObject));
					}
		
					@Override
					public void onSuccess(GenericRpcResponse<Map<BigInteger, Concept>> arg0) {
						
						Map<BigInteger, Concept> conceptMap = arg0.getContent();
						
						for (BigInteger nodeIdToChange : conceptMap.keySet()) {
							Concept conceptToUpdate = ScenarioClientUtility.getConceptWithId(nodeIdToChange);
							if (nodeIdToChange.equals(alteredNodeId) == false) {
								Concept copyConcept = conceptMap.get(nodeIdToChange);
								
								// Keep the concept's name and node ID, but copy all other properties (including child objects).
								conceptToUpdate.setAssessments(copyConcept.getAssessments());
								conceptToUpdate.setCompetenceMetric(copyConcept.getCompetenceMetric());
								conceptToUpdate.setConditionsOrConcepts(copyConcept.getConditionsOrConcepts());
								conceptToUpdate.setConfidenceMetric(copyConcept.getConfidenceMetric());
								conceptToUpdate.setExternalSourceId(copyConcept.getExternalSourceId());
								conceptToUpdate.setPerformanceMetric(copyConcept.getPerformanceMetric());
								conceptToUpdate.setPerformanceMetricArguments(copyConcept.getPerformanceMetricArguments());
								conceptToUpdate.setPriority(copyConcept.getPriority());
								conceptToUpdate.setPriorityMetric(copyConcept.getPriorityMetric());
								conceptToUpdate.setScenarioSupport(copyConcept.isScenarioSupport());
								conceptToUpdate.setTrendMetric(copyConcept.getTrendMetric());
							}
						}
											
				        SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(sourceScenarioObject));
				        firePopulateScenarioTreesEvent(sourceScenarioObject);
					}
		    		
		    	});
    		}
    	} else {
    		SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(sourceScenarioObject));
    	}
    }

    /**
     * Fires an event requesting the creation of a provided scenario object.
     * 
     * @param scenarioObject The scenario object that has been requested for creation. Can't be
     *        null.
     */
    public static void fireCreateScenarioObjectEvent(Serializable scenarioObject) {
        if (scenarioObject == null) {
            throw new IllegalArgumentException("The parameter 'scenarioObject' cannot be null.");
        }

        fireCreateScenarioObjectEvent(scenarioObject, null);
    }

    /**
     * Fires an event requesting the creation of a provided scenario object.
     * 
     * @param scenarioObject The scenario object that has been requested for creation. Can't be
     *        null.
     * @param parent The parent of the scenario object to create. This is only required for creating
     *        {@link Concept concepts} and {@link Condition conditions}.
     */
    public static void fireCreateScenarioObjectEvent(Serializable scenarioObject, Serializable parent) {
        if (scenarioObject == null) {
            throw new IllegalArgumentException("The parameter 'scenarioObject' cannot be null.");
        }

        CreateScenarioObjectEvent createEvent = new CreateScenarioObjectEvent(scenarioObject, parent);
        SharedResources.getInstance().getEventBus().fireEvent(createEvent);
    }
    
    /**
     * Fires an event requesting the deletion of a provided scenario object.
     * 
     * @param scenarioObject The scenario object that has been requested for deletion. Can't be
     *        null.
     * @param parent Optional parent field. Mandatory for {@link Concept} and {@link Condition}
     */
    public static void fireDeleteScenarioObjectEvent(Serializable scenarioObject, Serializable parent) {
        if (scenarioObject == null) {
            throw new IllegalArgumentException("The parameter 'scenarioObject' cannot be null.");
        }

        DeleteScenarioObjectEvent deleteEvent = new DeleteScenarioObjectEvent(scenarioObject, parent);
        SharedResources.getInstance().getEventBus().fireEvent(deleteEvent);
    }

    /**
     * Fires a rename event for a given object
     * 
     * @param scenarioObject The object that has been renamed
     * @param oldName The name the object previously had
     * @param newName The name the object now has
     */
    public static void fireRenameEvent(Serializable scenarioObject, String oldName, String newName) {
        RenameScenarioObjectEvent event = new RenameScenarioObjectEvent(scenarioObject, oldName, newName);
        SharedResources.getInstance().getEventBus().fireEvent(event);
    }
    
    /**
     * Fires a populate scenario trees event
     * @param scenarioObject The object that triggered the event
     */
    public static void firePopulateScenarioTreesEvent(Serializable scenarioObject) {
    	PopulateScenarioTreesEvent event = new PopulateScenarioTreesEvent(scenarioObject);
    	SharedResources.getInstance().getEventBus().fireEvent(event);
    }

    /**
     * Constructs an event indicating a reference change for the provided scenario object that fired
     * the event.
     * 
     * @param refChangedSource the source of the event fired. Can't be null.
     * @param oldValue the old value that was replaced by the new value.
     * @param newValue the new value that the scenario object changed to.
     */
    public static void fireReferencesChangedEvent(Serializable refChangedSource, Serializable oldValue,
            Serializable newValue) {
        ReferencesChangedEvent event = new ReferencesChangedEvent(refChangedSource, oldValue, newValue);
        SharedResources.getInstance().getEventBus().fireEvent(event);
    }
    
    /**
     * Fires an event indicating a place of interest has been edited on the map and to resume editing
     * the condition from which it came from.
     * 
     * @param place the first place on the map that was created or selected
     * @param condition the condition for which the place was edited for and which to resume editing
     * @param cleanupDefault whether the condition should remove the default value on resume
     */
    public static void firePlaceOfInterestResumeEditEvent(Serializable place, Condition condition, boolean cleanupDefault) {
        PlaceOfInterestResumeEditEvent event = new PlaceOfInterestResumeEditEvent(place, condition, cleanupDefault);
        SharedResources.getInstance().getEventBus().fireEvent(event);
    }
}
