/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;

import generated.course.ConceptNode;
import generated.course.Concepts;
import generated.course.Concepts.List.Concept;
import generated.course.Course;
import generated.course.LessonMaterial;
import generated.course.MerrillsBranchPoint;
import generated.course.Practice;
import generated.course.PresentSurvey;
import generated.course.Recall;
import generated.course.Transitions;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseConceptsChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.concept.JsConceptNode;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.TeamRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.TeamReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;

/**
 * A utility class used to interact with course concepts and their references within the course editor
 * 
 * @author nroberts
 */
public class CourseConceptUtility {

    private static final Logger logger = Logger.getLogger(CourseConceptUtility.class.getName());
    
    /** A mapping from each concept to all of the course objects that reference it */
    private static HashMap<String, List<TeamReference>> conceptReferences = new HashMap<>();
    
    /** The course that has been loaded and is being edited*/
    private static Course course = null;
    
    static {
        exposeNativeFunctions();
    }
    
    /**
     * Sets the {@link Course} that is currently being edited by the DKF editor. Should only be
     * called from {@link CoursePresenter#loadCourse}
     *
     * @param newScenario The value of the {@link Course} that is currently being edited by the
     *        Course editor. Can not be null.
     * @throws UnsupportedOperationException if the course has already been set
     */
    public static void setCourse(Course newCourse) {
        if (newCourse == null) {
            throw new IllegalArgumentException("The parameter 'newCourse' can not be null");
        } else if (course != null) {
            throw new UnsupportedOperationException("The course can't be set a second time");
        }
        
        course = newCourse;
        
        if(course.getConcepts() == null) {
            course.setConcepts(new Concepts());
        }
        
        //automatically create a hierarchy of concepts if necessary
        ConceptNode root = getRootConcept(true);
        
        //change any duplicate concepts to use different names
        removeDuplicateConcepts(root, new HashSet<String>());
        
        //gather the concept references for the first time
        gatherConceptReferences();
    }
    
    /**
     * Gets the course's concept hierarchy, which will always contain the root team.
     *
     * @return the course's course concept hierarchy. Can be null if the {@link #course} was not set or 
     * the course contains no course concept hierarchy.
     */
    public static Concepts.Hierarchy getConceptHierarchy(){
        
        if(course == null || course.getConcepts() == null) {
            return null;
        }
        return (Concepts.Hierarchy) course.getConcepts().getListOrHierarchy();
    }
    
    /**
     * Gets the root concept of the global concept hierarchy
     * 
     * @return the root concept. Can be null if no root concept has been created.
     */
    public static ConceptNode getRootConcept() {
        
        JavaScriptObject baseWindow = GatClientUtility.getBaseEditorWindow();
        if(baseWindow.equals(GatClientUtility.getEditorWindow())) {
            return getRootConcept(false);
            
        } else {
            JsConceptNode jsNode = getRootConcept(baseWindow);
            return jsNode == null ? null : jsNode.getOriginalNode();
        }
    }
    
    private static native JsConceptNode getRootConcept(JavaScriptObject wnd)/*-{
        return wnd.getRootConcept == undefined ? null : wnd.getRootConcept();
    }-*/;
    
    private static JsConceptNode getJsRootConcept() {
        return JsConceptNode.create(getRootConcept());
    }

    
    /**
     * Gets the root concept of the global concept hierarchy. This method can also optionally
     * create a root concept if none exists.
     * 
     * @param createAndSet whether to create the root concept if it doesn't exist.
     * @return the root concept. Can be null if no root concept has been created AND a new
     * root concept is not being created.
     */
    private static ConceptNode getRootConcept(boolean createAndSet){

        Concepts.Hierarchy hierarchy = course.getConcepts().getListOrHierarchy() instanceof Concepts.Hierarchy 
                ? (Concepts.Hierarchy) course.getConcepts().getListOrHierarchy()
                : null;
                
        if(createAndSet) {
            
            if(hierarchy == null){
                hierarchy = new Concepts.Hierarchy();
            }
            
            if(hierarchy.getConceptNode() == null) {
                
                ConceptNode root = new ConceptNode();
                root.setName("All Concepts"); 
                hierarchy.setConceptNode(root);
            }
            
            if(course.getConcepts() == null) {
                course.setConcepts(new Concepts());
            }
            
            if(!(course.getConcepts().getListOrHierarchy() instanceof Concepts.Hierarchy)) {
                
                if(course.getConcepts().getListOrHierarchy() instanceof Concepts.List) {
                    
                    for(Concept concept : ((Concepts.List) course.getConcepts().getListOrHierarchy()).getConcept()) {
                        
                        ConceptNode toAdd = new ConceptNode();
                        toAdd.setName(concept.getName());
                        hierarchy.getConceptNode().getConceptNode().add(toAdd);
                    }
                }
                
                course.getConcepts().setListOrHierarchy(hierarchy);
            }
        }
        
        
        return hierarchy != null ? hierarchy.getConceptNode() : null;
    }
    
    /**
     * Gets whether the given concept is the root of the course's global concept hierarchy
     * 
     * @param concept the concept to check. Can be null.
     * @return whether the given concept is the root
     */
    public static boolean isRootConcept(ConceptNode concept) {
        
        ConceptNode root = getRootConcept();
        
        return root != null && concept.equals(root);
    }
    
    /**
     * Finds all of the course objects referencing concepts from the global concept hierarchy
     * and compiles a mapping of their references.
     * <br/><br/>
     * Note that this method will simply do nothing if the current training application does not use a concept hierarchy.
     * (i.e. does not require learner IDs).
     */
    public static void gatherConceptReferences() {

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Updating concept references");
        }

        conceptReferences.clear();

        if(getConceptHierarchy() != null) {

            for (String name : getAllConceptNames()) {

                if(name != null) {
                    conceptReferences.put(name, new ArrayList<TeamReference>());
                }
            }
            
            gatherConceptReferences(course.getTransitions());
            
            SharedResources.getInstance().getEventBus().fireEvent(new CourseConceptsChangedEvent(course));
        }

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished updating concept references");
        }
    }
    
    /**
     * Finds all of the course objects within the given transitions that reference concepts from the global concept hierarchy
     * and compiles a mapping of their references.
     * <br/><br/>
     * Note that this method will simply do nothing if the current training application does not use a concept hierarchy.
     * (i.e. does not require learner IDs).
     */
    private static void gatherConceptReferences(Transitions transitions) {
        
        if(transitions != null) {
            for(Serializable type : transitions.getTransitionType()) {
                
                if (type instanceof MerrillsBranchPoint) {
                    MerrillsBranchPoint mbp = (MerrillsBranchPoint) type;

                    if(mbp.getConcepts() != null) {
                        addConceptReferences(mbp, mbp, mbp.getConcepts().getConcept());
                    }

                    final MerrillsBranchPoint.Quadrants quadrants = mbp.getQuadrants();
                    if (quadrants != null) {
                        for (Serializable content : quadrants.getContent()) {
                            if (content instanceof Practice) {
                                Practice practice = (Practice) content;

                                final Practice.PracticeConcepts practiceConcepts = practice
                                        .getPracticeConcepts();
                                if (practiceConcepts != null) {
                                    addConceptReferences(mbp, mbp, practiceConcepts.getCourseConcept());
                                }
                            } else if (content instanceof Transitions) {
                                gatherConceptReferences((Transitions) content);
                            } else if (content instanceof Recall) {
                                final Recall recall = (Recall) content;

                                /* Convert concept question names to lowercase to
                                 * match the concepts */
                                Recall.PresentSurvey presentSurvey = recall.getPresentSurvey();
                                if (presentSurvey != null && presentSurvey.getSurveyChoice() != null) {
                                    for (generated.course.ConceptQuestions question : presentSurvey.getSurveyChoice().getConceptQuestions()) {
                                        if(question.getName() == null) {
                                            continue;
                                        }
                                        addConceptReferences(mbp, mbp, Collections.singletonList(question.getName()));
                                    }
                                }
                            }
                        }
                    }
                } else if (type instanceof LessonMaterial) {
                   LessonMaterial lessonMaterial = (LessonMaterial) type;
                   if (lessonMaterial.getLessonMaterialList() != null) {
                        for (generated.course.Media media : lessonMaterial.getLessonMaterialList().getMedia()) {
                            if (media.getMediaTypeProperties() instanceof generated.course.LtiProperties) {
                                generated.course.LtiProperties properties = (generated.course.LtiProperties) media
                                        .getMediaTypeProperties();

                                /* Make LTI concepts lowercase */
                                generated.course.LtiConcepts concepts = properties.getLtiConcepts();
                                if (concepts != null && CollectionUtils.isNotEmpty(concepts.getConcepts())) {
                                    addConceptReferences(lessonMaterial, lessonMaterial, concepts.getConcepts());
                                }
                            }
                        }
                    }
                } else if (type instanceof PresentSurvey) {
                    PresentSurvey presentSurvey = (PresentSurvey) type;
                    if (presentSurvey.getSurveyChoice() instanceof generated.course.PresentSurvey.ConceptSurvey) {
                        generated.course.PresentSurvey.ConceptSurvey conceptSurvey = (generated.course.PresentSurvey.ConceptSurvey) presentSurvey
                                .getSurveyChoice();
                        for (generated.course.ConceptQuestions question : conceptSurvey.getConceptQuestions()) {
                            addConceptReferences(presentSurvey, presentSurvey, Collections.singletonList(question.getName()));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Creates a mapping of the given schema object to the concept it references
     *
     * @param parent the parent of the reference object. Used to display unique names associated with the reference object's
     *        nearest named parent so that authors can better identify where the referencing object is in the scenario.
     * @param referenceObject the schema object where a reference was found
     * @param references the referenced concept
     */
    private static void addConceptReferences(Serializable parent, Serializable referenceObject, List<String> references) {

        if (referenceObject == null) {
            return;
        }

        if (references == null) {
            return;
        }

        for(String ref : references) {

            //update the reference mapping of each concept referenced by this object
            List<TeamReference> refs = getReferencesToConcept(ref);
            if(refs != null) {

                boolean refMappingExists = false;

                for(TeamReference conceptRef : refs) {

                    if (referenceObject.equals(conceptRef.getReferenceObject())) {

                        // this object already references this concept, so increment the
                        // number of  references
                        conceptRef.incrementReferences();
                        refMappingExists = true;
                        break;
                    }
                }

                if(!refMappingExists) {

                    // no references to this concept have been found in this object yet,
                    // so add a new reference
                    refs.add(new TeamReference(parent, referenceObject));
                }
            }
        }
    }

    /**
     * Gets all of the course objects referencing the given concept
     *
     * @param conceptName the name of concept to find references for
     * @return the list of course objects referencing the concept. May return null if
     * concept references have not been gathered yet, or an empty list if no course objects reference the team or team member.
     */
    public static List<TeamReference> getReferencesToConcept(String conceptName) {
        return conceptReferences.get(conceptName);
    }
    
    /**
     * Gets the names of all the concepts in the concept hierarchy
     * 
     * @return the names of all the concepts.  Can be null or empty.
     */
    public static List<String> getAllConceptNames() {

        if(getConceptHierarchy() != null) {
            return getAllConceptNames(getRootConcept());

        } else {
            return null;
        }
    }
    
    /**
     * Gets the names of all the concepts in the given concept node
     * 
     * @return the names of all the concepts
     */
    private static List<String> getAllConceptNames(ConceptNode node) {

        List<String> names = new ArrayList<>();

        if(node != null) {

            names.add(node.getName());

            for(ConceptNode subNode : node.getConceptNode()) {
                names.addAll(getAllConceptNames(subNode));
            }
        }

        return names;
    }
    
    /**
     * Updates all references to the concepts with the old name to use the new name instead
     *
     * @param oldName the old concept name to replace
     * @param newName the new concept name to use
     */
    public static void updateConceptReferences(String oldName, String newName) {

        // if the old name is blank or if the old and new values are the same, do nothing.
        if (StringUtils.isBlank(oldName) || StringUtils.equals(oldName, newName)) {
            return;
        }
        
        Set<Serializable> referencingSchemaObjects = new HashSet<Serializable>();
        if(course.getTransitions() != null) {
            for(Serializable type : course.getTransitions().getTransitionType()) {
                
                if(type instanceof MerrillsBranchPoint) {
                    
                    MerrillsBranchPoint mbp = (MerrillsBranchPoint) type;
                    if(mbp.getConcepts() != null) {
                        
                        int index = mbp.getConcepts().getConcept().indexOf(oldName);
                        if(index != -1) {
                            
                            if(StringUtils.isNotBlank(newName)) {
                                mbp.getConcepts().getConcept().set(index, newName); //replace old name reference with new name

                             } else {
                                 mbp.getConcepts().getConcept().remove(index); //new name is empty, so remove reference
                             }
                            
                            referencingSchemaObjects.add(mbp);
                        }
                    }
                }
            }
        }
        
        // references have been updated, so gather the global references
        gatherConceptReferences();
        
        // notify listeners that the place of interest was renamed
        SharedResources.getInstance().getEventBus().fireEvent(new TeamRenamedEvent(oldName, newName));

        /* if the new name is blank, we need to revalidate because this will cause validation
         * issues */
        if (StringUtils.isBlank(newName)) {

            for (Serializable validateObj : referencingSchemaObjects) {
                ScenarioEventUtility.fireDirtyEditorEvent(validateObj);
            }
        }
    }
    
    /**
     * Creates a new concept node with a unique concept name
     * 
     * @return the new concept node. Will not be null;
     */
    public static ConceptNode generateNewConcept() {
        ConceptNode node = new ConceptNode();
        node.setName(generateNewConceptName());
        return node;
    }
    
    /** New {@link ConceptNode} name prefix */
    private final static String NEW_CONCEPT_PREFIX = "Concept ";

    /**
     * Generates a unique {@link ConceptNode} name.
     *
     * @return a unique name for a {@link ConceptNode concept}.
     */
    private static String generateNewConceptName() {
        int index = 1;
        String newName;
        do {
            newName = NEW_CONCEPT_PREFIX + index++;
        } while (!isConceptNameValid(newName));

        return newName;
    }
    
    /**
     * Generates a unique {@link ConceptNode} name using the given base name.
     *
     * @param the base name. If another name with this name is found, a number will be added and incremented.
     * @return a unique name for a {@link ConceptNode concept}.
     */
    public static String generateNewConceptName(String name) {
        int index = 1;
        String newName;
        do {
            newName = name + (index > 1 ? (" " + index) : "");
            index++;
        } while (!isConceptNameValid(newName));

        return newName;
    }
    
    /**
     * Checks whether the given concept name is valid (i.e. unique and not blank)
     * 
     * @param name the concept name to check
     * @return whether the provided name is valid
     */
    public static boolean isConceptNameValid(String name) {

        if (StringUtils.isBlank(name)) {
            return false;
        }

        return getConceptHierarchy() != null && isConceptNameValid(name, getRootConcept());
    }
    
    /**
     * Checks whether the given concept name is valid (i.e. unique and not blank) within the given concept node
     * 
     * @param name the concept name to check
     * @param node the concept node within which to check for name conflicts
     * @return whether the provided name is valid
     */
    private static boolean isConceptNameValid(String name, ConceptNode node) {

        if (StringUtils.isBlank(name)) {
            return false;
        } else if (node == null) {
            return true;
        }

        if (StringUtils.equalsIgnoreCase(name, node.getName())) {
            return false;

        } else {

            for (ConceptNode subNode : node.getConceptNode()) {
                if (!isConceptNameValid(name, subNode)) {
                    return false;
                }
            }
        }

        return true;
    }
    
    public static Concepts getConcepts() {
        return course.getConcepts();
    }
    
    /**
     * Leaves the provided concept hierarchy provided with unique concept names.  When a duplicate is found
     * the suffix "(1)" is added repeatedly until the concept name is unique.
     * 
     * @param conceptNode the concept hierarchy node to check for and update to unique concept names
     * @param uniqueSet the unique set of concept names in the entire course concept hierarchy
     */
    private static void removeDuplicateConcepts(ConceptNode conceptNode, Set<String> uniqueSet){
        
        String lowerCaseConcept = conceptNode.getName().toLowerCase().trim();
        if(uniqueSet.contains(lowerCaseConcept)){
            //duplicate - can't remove because this could have implications on the hierarchy, so rename instead
            
            String suffix = "(1)";
            while(true){
                
                //add the suffix and check for uniqueness
                lowerCaseConcept += suffix;
                
                if(!uniqueSet.contains(lowerCaseConcept)){
                    //adding the suffix to the current string makes it unique, set the unique value
                    conceptNode.setName(lowerCaseConcept);
                    break;
                }
            }
        }

        uniqueSet.add(lowerCaseConcept);
        
        if(conceptNode.getConceptNode() != null){
            //repeat for each child
            
            for(ConceptNode childConceptNode : conceptNode.getConceptNode()){
                removeDuplicateConcepts(childConceptNode, uniqueSet);
            }
        }
    }
    
    /**
     * Gets the course concept with the given name, if such a concept exists
     * 
     * @param name the name of the concept to get. If null, null will be returned.
     * @return the concept with the given name. Can be null, if no such concept is found.
     */
    public static ConceptNode getConceptWithName(String name) {
        
        if(StringUtils.isBlank(name)){
            return null;
        }
        
        ConceptNode root = getRootConcept();
        if(root != null) {
            return getConceptWithName(name, root);
            
        } else {
            return null;
        }
    }
    
    /**
     * Gets the course concept with the given name, if such a concept exists
     * 
     * @param name the name of the concept to get. If null, null will be returned.
     * @param node the concept node to begin searching from. If null, null will be returned.
     * @return the concept with the given name. Can be null, if no such concept is found.
     */
    public static ConceptNode getConceptWithName(String name, ConceptNode node) {
        
        if(node == null || StringUtils.isBlank(name)) {
            return null;
        }
        
        if(name.equalsIgnoreCase(node.getName())) {
            return node;
        
        } else {
            for(ConceptNode child : node.getConceptNode()) {
                
                ConceptNode foundNode = getConceptWithName(name, child);
                if(foundNode != null) {
                    return foundNode;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Return whether the condition provided is under a course concept in the current DKF structure.
     * This concept just has to be under the same task and an ancestor to this concept.
     *
     * @param condition the condition to check its ancestor concepts for course concepts.  If null this returns false.
     * @return true if any of the ancestor concepts to this condition instance is a course concept.
     */
    public static boolean hasCourseConceptAncestor(generated.dkf.Condition condition){
        
        if(condition == null){
            return false;
        }
        
        generated.dkf.Concept parentConcept = ScenarioClientUtility.getReferencesTo(condition);
        if(parentConcept == null){
            // the parent concept to this condition was not found, this is bad!
            return false;
        }
        
        return hasCourseConceptAncestor(parentConcept);
    }
    
    /**
     * Return whether the concept provided is a course concept or any ancestor to this concept
     * is a course concept.
     * @param concept the concept to check if it or any ancestor to it is a course concept. If null
     * this returns false.
     * @return true if the concept or any ancestor concept to the concept is a course concept.
     */
    public static boolean hasCourseConceptAncestor(generated.dkf.Concept concept){
        
        if(concept == null){
            return false;
        }
        
        if(CourseConceptUtility.getConceptWithName(concept.getName()) == null){
            // the concept is not a course concept, check the parent
            
            Iterable<Serializable> iterable = ScenarioClientUtility.getReferencesTo(concept);
            Iterator<Serializable> iterator = iterable.iterator();
            while(iterator.hasNext()){
                
                Serializable refObj = iterator.next();
                if(refObj instanceof generated.dkf.Concept){
                    // found a parent concept to this concept
                    // - there can be only 1 parent to a concept
                    return hasCourseConceptAncestor((generated.dkf.Concept)refObj);
                }
            }
            
            return false;
        }else{
            // the concept is a course concept
            return true;
        }
        
    }
    
    /**
     * Shows the course's concepts by displaying their editor
     */
    public static native void showCourseConceptsEditor()/*-{
        var baseWnd = @mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility::getBaseEditorWindow()();
        baseWnd.showCourseConceptsEditor($wnd);
    }-*/;

    
    private static native void exposeNativeFunctions() /*-{
        
        $wnd.getRootConcept = $entry(function(){
            return @mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility::getJsRootConcept()();
        });
    }-*/;
    
    /**
     * An abstract representation of a hierarchy of concept nodes. This essentially strips a concept hierarchy of 
     * any extraneous data that isn't needed when referencing concepts, leaving behind just the name of each concept.
     * 
     * @author nroberts
     */
    public static class ConceptNodeRef{
        
        /** The name of the concept being referenced */
        private String name;
        
        /** The child concepts that are also being referenced */
        private List<ConceptNodeRef> nodes;
        
        /**
         * Creates a reference to the concept with the given name
         * 
         * @param name the name of the concept being referenced. Cannot be null.
         * @param nodes e child concepts that are also being referenced. Can be null.
         */
        public ConceptNodeRef(String name, List<ConceptNodeRef> nodes) {
            
            if(name == null) {
                throw new IllegalArgumentException("The name of the concept to reference cannot be null");
            }
            
            this.name = name;
            this.nodes = nodes;
        }

        /**
         * Gets the name of the concept being referenced
         * 
         * @return the concept name. Will not be null.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the child concepts nodes that are also being referenced
         * 
         * @return the child concept nodes. Can be null, if this concept has no children
         * that are being referenced.
         */
        public List<ConceptNodeRef> getNodes() {
            return nodes;
        }
    }

}
