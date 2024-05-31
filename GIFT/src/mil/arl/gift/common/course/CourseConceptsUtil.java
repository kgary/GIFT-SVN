/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import generated.course.ConceptNode;
import generated.course.Concepts;
import generated.course.Concepts.Hierarchy;
import generated.course.Concepts.List.Concept;
import mil.arl.gift.common.util.CollectionUtils;


/**
 * Utility class that can be used to operate on the course concepts object.
 * This class is meant to be a static utility class and is not meant to be instantiated.
 *
 * @author nblomberg
 *
 */
public class CourseConceptsUtil {

    /**
     * a unique name for the performance node that contains the flat list of course concepts
     * This is used as a unique identifier for sending/retrieving the performance assessment values
     * for course concepts.  These assessments are driven by Question Bank assessment rules authored in a course object.
     */
    public static final String COURSE_CONCEPTS_CONCEPT_NAME = "course concepts";

    /**
     * a unique name for the performance node that contains the flat list of course concepts
     * This is used as a unique identifier for sending/retrieving the performance assessment values
     * for course concepts.  These assessments are driven by the grade score node returned from the LTI provider.
     */
    public static final String COURSE_CONCEPTS_CONCEPT_NAME_SKILL = "course concepts for skill";

    /**
     * Constructor - private to help prevent class instantiation.
     */
    private CourseConceptsUtil() {

    }

    /**
     * Gets the list of concept names (recursively) based on a hierarchy.
     * The root node should be the first node passed in to get the complete list.
     *
     * @param node - The current node to evaluate.  Use the root node to recurse the entire hierarchy.
     * @param conceptNameList - The list of concept names that will be returned.
     */
    public static void getConceptNamesFromHierarchy(ConceptNode node,
                                    ArrayList<String> conceptNameList) {


        if (node != null) {
            conceptNameList.add(node.getName());
            List<ConceptNode> childNodes = node.getConceptNode();

            for (ConceptNode childNode : childNodes) {
                getConceptNamesFromHierarchy(childNode, conceptNameList);
            }
        }


    }

    /**
     * Gets the list of concept names based on a list structure.
     *
     * @param conceptList - The list object containing the concept data.
     * @param conceptNameList - The list of concept names that will be returned.
     */
    private static void getConceptNamesFromList(Concepts.List conceptList,
            ArrayList<String> conceptNameList) {

        if (conceptList != null) {
            for(Concept concept : conceptList.getConcept()){
                conceptNameList.add(concept.getName());
            }
        }
    }

    /**
     * Gets the list of concept names from the concept object (unsorted). The list of concept names
     * is unique and this will return the list of unique names regardless if the concept is a list
     * or a hierarchy.
     *
     * @param concepts - The concepts object containing the concept data.
     * @return the list of concept names. Can be empty if concepts is null or no concepts exist.
     */
    public static List<String> getConceptNameList(Concepts concepts) {

        ArrayList<String> conceptNames = new ArrayList<>();

        if (concepts != null) {

            if (concepts.getListOrHierarchy() instanceof Concepts.List) {

                Concepts.List list = (generated.course.Concepts.List) concepts.getListOrHierarchy();

                getConceptNamesFromList(list, conceptNames);

            } else if (concepts.getListOrHierarchy() instanceof Concepts.Hierarchy) {

                Concepts.Hierarchy hierarchy = (Hierarchy) concepts.getListOrHierarchy();

                ConceptNode rootNode = hierarchy.getConceptNode();
                getConceptNamesFromHierarchy(rootNode, conceptNames);

            }
        }

        return conceptNames;
    }

    /**
     * Utility function to help determine if a concepts object has any concepts.
     *
     * @param concepts - The concepts object to check.
     * @return True if there are any concepts, false otherwise.
     */
    public static boolean hasConcepts(Concepts concepts) {
        boolean hasConcepts = false;

        if (concepts != null) {
            if(concepts.getListOrHierarchy() instanceof Concepts.List){

                Concepts.List list = (generated.course.Concepts.List) concepts.getListOrHierarchy();
                if (list.getConcept() != null) {
                    hasConcepts = true;
                }


            } else if(concepts.getListOrHierarchy() instanceof Concepts.Hierarchy){
                Concepts.Hierarchy hierarchy = (Hierarchy) concepts.getListOrHierarchy();
                if (hierarchy.getConceptNode() != null) {
                    hasConcepts = true;
                }
            }
        }

        return hasConcepts;
    }

    /**
     * Searches the course for concepts. Removes duplicates and converts them to
     * lowercase. This is necessary for legacy files.
     *
     * @param course the course the clean
     */
    public static void cleanCourseConcepts(generated.course.Course course) {
        if (course == null) {
            return;
        }

        cleanCourseConcepts(course.getConcepts());
        cleanTransitionConcepts(course.getTransitions());
    }

    /**
     * Processes the provided course concepts instance and ensure unique
     * lowercase concept names (leading/trailing space ignored). For concept
     * list this means removing duplicates. For concept hierarchy this means
     * adding a suffix repeatedly to the concept name until the name is unique.
     *
     * @param courseConcepts contains course concepts to make unique
     */
    private static void cleanCourseConcepts(generated.course.Concepts courseConcepts){

        if(courseConcepts == null){
            return;
        }

        if(courseConcepts.getListOrHierarchy() instanceof generated.course.Concepts.List){
            //concept list

            generated.course.Concepts.List conceptList = (generated.course.Concepts.List) courseConcepts.getListOrHierarchy();

            Set<String> uniqueSet = new HashSet<>(conceptList.getConcept().size());

            Iterator<Concepts.List.Concept> conceptItr = conceptList.getConcept().iterator();
            while(conceptItr.hasNext()){

                Concepts.List.Concept concept = conceptItr.next();
                final String conceptLowercase = concept.getName().toLowerCase().trim();
                if(uniqueSet.contains(conceptLowercase)){
                    //found duplicate - leading/trailing space and case ignored)
                    conceptItr.remove();
                    continue;
                }

                /* Convert concept name to lower case */
                concept.setName(conceptLowercase);
                uniqueSet.add(conceptLowercase);
            }
        }else{
            //concept hierarchy, handle recursively

            Set<String> uniqueSet = new HashSet<>();
            cleanCourseConcepts(((generated.course.Concepts.Hierarchy)courseConcepts.getListOrHierarchy()).getConceptNode(), uniqueSet);
        }
    }

    /**
     * Leaves the provided concept hierarchy with unique lowercase concept
     * names. When a duplicate is found the suffix "(1)" is added repeatedly
     * until the concept name is unique.
     *
     * @param conceptNode the concept hierarchy node to check for and update to
     *        unique concept names
     * @param uniqueSet the unique set of concept names in the entire course
     *        concept hierarchy
     */
    private static void cleanCourseConcepts(generated.course.ConceptNode conceptNode, Set<String> uniqueSet){

        if(conceptNode == null){
            return;
        }else if(uniqueSet == null){
            throw new IllegalArgumentException("The unique set of names can't be null");
        }

        String lowerCaseConcept = conceptNode.getName().toLowerCase().trim();
        if(uniqueSet.contains(lowerCaseConcept)){
            //duplicate - can't remove because this could have implications on the hierarchy, so rename instead

            String suffix = "(1)";
            while(true){

                //add the suffix and check for uniqueness
                lowerCaseConcept += suffix;

                if(!uniqueSet.contains(lowerCaseConcept)){
                    break;
                }
            }
        }

        /* Convert concept name to lower case */
        conceptNode.setName(lowerCaseConcept);
        uniqueSet.add(lowerCaseConcept);

        if(conceptNode.getConceptNode() != null){
            //repeat for each child

            for(generated.course.ConceptNode childConceptNode : conceptNode.getConceptNode()){
                cleanCourseConcepts(childConceptNode, uniqueSet);
            }
        }
    }

    /**
     * Convert transition concepts to lowercase. This is necessary due to legacy
     * files that still contain uppercase concepts.
     *
     * @param transitions the transitions to clean
     */
    private static void cleanTransitionConcepts(generated.course.Transitions transitions) {
        if (transitions == null || CollectionUtils.isEmpty(transitions.getTransitionType())) {
            return;
        }

        for (Serializable transitionType : transitions.getTransitionType()) {
            if (transitionType instanceof generated.course.MerrillsBranchPoint) {
                generated.course.MerrillsBranchPoint mbp = (generated.course.MerrillsBranchPoint) transitionType;

                /* Convert MBP concepts to lowercase */
                final generated.course.MerrillsBranchPoint.Concepts mbpConcepts = mbp.getConcepts();
                if (mbpConcepts != null) {
                    List<String> conceptNames = mbpConcepts.getConcept();
                    for (int i = 0; i < conceptNames.size(); i++) {
                        conceptNames.set(i, conceptNames.get(i).toLowerCase().trim());
                    }
                }

                final generated.course.MerrillsBranchPoint.Quadrants quadrants = mbp.getQuadrants();
                if (quadrants != null) {
                    for (Serializable content : quadrants.getContent()) {
                        if (content instanceof generated.course.Practice) {
                            generated.course.Practice practice = (generated.course.Practice) content;

                            /* Convert practice concepts to lowercase */
                            final generated.course.Practice.PracticeConcepts practiceConcepts = practice
                                    .getPracticeConcepts();
                            if (practiceConcepts != null) {
                                List<String> conceptNames = practiceConcepts.getCourseConcept();
                                for (int i = 0; i < conceptNames.size(); i++) {
                                    conceptNames.set(i, conceptNames.get(i).toLowerCase().trim());
                                }
                            }
                        } else if (content instanceof generated.course.Transitions) {
                            cleanTransitionConcepts((generated.course.Transitions) content);
                        } else if (content instanceof generated.course.Recall) {
                            final generated.course.Recall recall = (generated.course.Recall) content;

                            /* Convert concept question names to lowercase to
                             * match the concepts */
                            generated.course.Recall.PresentSurvey presentSurvey = recall.getPresentSurvey();
                            if (presentSurvey != null && presentSurvey.getSurveyChoice() != null) {
                                for (generated.course.ConceptQuestions question : presentSurvey.getSurveyChoice().getConceptQuestions()) {
                                    question.setName(question.getName().toLowerCase().trim());
                                }
                            }
                        }
                    }
                }
            } else if (transitionType instanceof generated.course.LessonMaterial) {
                generated.course.LessonMaterial lessonMaterial = (generated.course.LessonMaterial) transitionType;
                if (lessonMaterial.getLessonMaterialList() != null) {
                    cleanMediaConcepts(lessonMaterial.getLessonMaterialList().getMedia());
                }
            } else if (transitionType instanceof generated.course.PresentSurvey) {
                /* Convert concept question names to lowercase to match the
                 * concepts */
                generated.course.PresentSurvey presentSurvey = (generated.course.PresentSurvey) transitionType;
                if (presentSurvey.getSurveyChoice() instanceof generated.course.PresentSurvey.ConceptSurvey) {
                    generated.course.PresentSurvey.ConceptSurvey conceptSurvey = (generated.course.PresentSurvey.ConceptSurvey) presentSurvey
                            .getSurveyChoice();
                    for (generated.course.ConceptQuestions question : conceptSurvey.getConceptQuestions()) {
                        question.setName(question.getName().toLowerCase().trim());
                    }
                }
            }
        }
    }

    /**
     * Convert media concepts to lowercase. This is necessary due to legacy
     * files that still contain uppercase concepts.
     *
     * @param mediaList the media to clean
     */
    public static void cleanMediaConcepts(List<generated.course.Media> mediaList) {
        if (CollectionUtils.isEmpty(mediaList)) {
            return;
        }

        for (generated.course.Media media : mediaList) {
            if (media.getMediaTypeProperties() instanceof generated.course.LtiProperties) {
                generated.course.LtiProperties properties = (generated.course.LtiProperties) media
                        .getMediaTypeProperties();

                /* Make LTI concepts lowercase */
                generated.course.LtiConcepts concepts = properties.getLtiConcepts();
                if (concepts != null && CollectionUtils.isNotEmpty(concepts.getConcepts())) {
                    final List<String> conceptList = concepts.getConcepts();
                    for (int i = 0; i < conceptList.size(); i++) {
                        conceptList.set(i, conceptList.get(i).toLowerCase().trim());
                    }
                }
            }
        }
    }

    /**
     * Convert metadata concepts to lowercase. This is necessary due to legacy
     * files that still contain uppercase concepts.
     *
     * @param metadata the metadata file whose concepts should be cleaned. Can't
     *        be null.
     */
    public static void cleanMetadataConcepts(generated.metadata.Metadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("The parameter 'metadata' cannot be null.");
        }

        final generated.metadata.Metadata.Concepts concepts = metadata.getConcepts();
        if (concepts == null) {
            return;
        }

        for (generated.metadata.Concept concept : concepts.getConcept()) {
            concept.setName(concept.getName().toLowerCase().trim());
        }
    }
}
