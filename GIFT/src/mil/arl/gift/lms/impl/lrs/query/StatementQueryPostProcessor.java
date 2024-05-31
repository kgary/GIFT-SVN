package mil.arl.gift.lms.impl.lrs.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiExtensionException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AbstractGiftActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.ResultExtensionConcept;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts.extensionObjectKeys;
import mil.arl.gift.lms.impl.lrs.xapi.profile.mom.MomActivityTypeConcepts;

/**
 * Helper class for filtering, sorting, etc. collections of xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class StatementQueryPostProcessor {
    
    /**
     * Filters based on specified Activity found within contextActivities; original collection is unaltered.
     * 
     * @param statements - Collection of Statement to filter
     * @param a - Activity which should be found only within ContextActivity
     * 
     * @return possibly empty but not null collection of statements
     */
    public static List<Statement> filterByRelatedContextActivity(List<Statement> statements, Activity a) {
        List<Statement> coll = new ArrayList<Statement>();
        for(Statement stmt : statements) {
            if(stmt.getObject() != null) {
                // Filter out statements where activity is object
                if(!(AbstractGiftActivity.isSameActivityId((Activity) stmt.getObject(), a)) && stmt.getContext() != null) {
                    Context stmtContext = stmt.getContext();
                    List<Activity> parents = ContextActivitiesHelper.getParentActivities(stmtContext);
                    List<Activity> groupings = ContextActivitiesHelper.getGroupingActivities(stmtContext);
                    List<Activity> categories = ContextActivitiesHelper.getCategoryActivities(stmtContext);
                    List<Activity> others = ContextActivitiesHelper.getOtherActivities(stmtContext);
                    // Keep statements that contain activity as a context activity
                    if(CollectionUtils.isNotEmpty(parents)) {
                        if(AbstractGiftActivity.containsActivity(parents, a)) {
                            coll.add(stmt);
                            continue;
                        }
                    }
                    if(CollectionUtils.isNotEmpty(groupings)) {
                        if(AbstractGiftActivity.containsActivity(groupings, a)) {
                            coll.add(stmt);
                            continue;
                        }
                    }
                    if(CollectionUtils.isNotEmpty(categories)) {
                        if(AbstractGiftActivity.containsActivity(categories, a)) {
                            coll.add(stmt);
                            continue;
                        }
                    }
                    if(CollectionUtils.isNotEmpty(others)) {
                        if(AbstractGiftActivity.containsActivity(others, a)) {
                            coll.add(stmt);
                            continue;
                        }
                    }
                }
            }
        }
        return coll;
    }
    
    /**
     * Filters based on specified targetResponse found within response field; original collection is unaltered.
     * 
     * @param statements - Collection of Statements to filter
     * @param targetResponse - String which should be the statement result.response
     * 
     * @return possibly empty but not null collection of statements
     */
    public static List<Statement> filterByResponse(List<Statement> statements, String targetResponse) {
        List<Statement> coll = new ArrayList<Statement>();
        for(Statement stmt : statements) {
            if(stmt.getResult() != null && stmt.getResult().getResponse() != null) {
                if(targetResponse.equalsIgnoreCase(stmt.getResult().getResponse())) {
                    coll.add(stmt);
                }
            }
        }
        return coll;
    }
    
    /**
     * Filters based on specified Activity found within statements; original collection is unaltered.
     *  
     * @param statements - Collection of Statements to filter
     * @param activity - Activity which should be found within Statement
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to parse statement
     */
    public static List<Statement> filterByActivity(List<Statement> statements, AbstractGiftActivity activity) throws LmsXapiActivityException {
        List<Statement> coll = new ArrayList<Statement>();
        for(Statement stmt : statements) {
            if(activity.parseFromStatement(stmt) != null) {
                coll.add(stmt);
            }
        }
        return coll;
    }
    
    /**
     * Filters based on inclusion of Parent Domain Context Activity corresponding to targetDomainId; original collection is unaltered.
     * 
     * @param statements - Collection of Statements to filter
     * @param targetDomainId - Domain Id used to create Domain Activity
     *  
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to form domain activity from targetDomainId
     * @throws LmsXapiProfileException when unable to resolve domain activity type from xAPI Profile
     */
    public static List<Statement> filterByDomain(List<Statement> statements, String targetDomainId) throws LmsXapiActivityException, LmsXapiProfileException {
        return filterByActivity(statements, ItsActivityTypeConcepts.Domain.getInstance().asActivity(targetDomainId));
    }
    
    /**
     * Filters based on inclusion of Parent Domain Context Activity corresponding to Domain Session's runtime id; original collection is unaltered.
     * 
     * @param statements - Collection of Statements to filter
     * @param ds - Domain Session with Domain Id to filter for
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to form activity from domain session
     * @throws LmsXapiProfileException when unable to resolve domain activity type from xAPI Profile
     */
    public static List<Statement> filterByDomain(List<Statement> statements, DomainSession ds) throws LmsXapiActivityException, LmsXapiProfileException {
        return filterByDomain(statements, ds.getDomainRuntimeId());
    }
    
    /**
     * Filters based on inclusion of Grouping Course Record Context Activity; original collection is unaltered.
     * 
     * @param statements - Collection of Statements to filter
     * @param courseRecordRef - reference for the target CourseRecord
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to form Activity from Course Record Reference
     * @throws LmsXapiProfileException when unable to resolve course record reference activity type from xAPI Profile
     */
    public static List<Statement> filterByCourseRecord(List<Statement> statements, CourseRecordRef courseRecordRef) throws LmsXapiActivityException, LmsXapiProfileException {
        return filterByActivity(statements, ItsActivityTypeConcepts.CourseRecord.getInstance().asActivity(courseRecordRef));
    }
    
    /**
     * Filters based on inclusion of Grouping Course Record Context Activity; original collection is unaltered.
     * 
     * @param statements - Collection of Statements to filter
     * @param record - LMSCourseRecord to parse CourseRecordRef from
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to form Activity from Course Record Reference
     * @throws LmsXapiProfileException when unable to resolve course record reference activity type from xAPI Profile
     */
    public static List<Statement> filterByCourseRecord(List<Statement> statements, LMSCourseRecord record) throws LmsXapiActivityException, LmsXapiProfileException {
        return filterByCourseRecord(statements, record.getCourseRecordRef());
    }
    
    /**
     * Filters based on inclusion of Category Context Activity; original collection is unaltered.
     * 
     * @param statements - Collection of Statements to filter
     * @param activity - Activity expected to be found within Category Context Activities
     * 
     * @return possibly empty but not null collection of statements
     */
    public static List<Statement> filterByCategory(List<Statement> statements, Activity activity) {
        List<Statement> coll = new ArrayList<Statement>();
        for(Statement stmt : statements) {
            if(AbstractGiftActivity.containsActivity(ContextActivitiesHelper.getCategoryActivities(stmt.getContext()), activity)) {
                coll.add(stmt);
            }
        }
        return coll;
    }
    
    /**
     * Filters based on inclusion of Task Category Context Activity; original collection is unaltered.
     * 
     * @param statements - Collection of Statements to filter
     * @param taskState - Task Performance State to search for
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to form Activity from taskState
     * @throws LmsXapiProfileException when unable to resolve Task activity type from xAPI Profile
     */
    public static List<Statement> filterByCategoryTaskPerformanceState(List<Statement> statements, TaskPerformanceState taskState) throws LmsXapiActivityException, LmsXapiProfileException {
        return filterByCategory(statements, ItsActivityTypeConcepts.AssessmentNode.Task.getInstance().asActivity(taskState));
    }
    
    /**
     * Filters based on inclusion of Intermediate Concept Category Context Activity; original collection is unaltered.
     * 
     * @param statements - Collection of Statements to filter
     * @param conceptState - Intermediate Concept Performance State to create Activity from
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to form Activity from conceptState
     * @throws LmsXapiProfileException when unable to resolve Intermediate Concept activity type from xAPI Profile
     */
    public static List<Statement> filterByCategoryIntermediateConceptPerformanceState(List<Statement> statements, IntermediateConceptPerformanceState conceptState) throws LmsXapiActivityException, LmsXapiProfileException {
        return filterByCategory(statements, ItsActivityTypeConcepts.AssessmentNode.ConceptIntermediate.getInstance().asActivity(conceptState));
        }
    
    /**
     * Filters based on inclusion of Concept Category Context Activity; original collection is unaltered.
     * 
     * @param statements - Collection of Statements to filter
     * @param conceptState - ConceptPerformanceState to create Activity from
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to form Activity from ConceptPerformanceState
     * @throws LmsXapiProfileException when unable to resolve Concept activity type from xAPI Profile
     */
    public static List<Statement> filterByCategoryConceptPerformanceState(List<Statement> statements, ConceptPerformanceState conceptState) throws LmsXapiActivityException, LmsXapiProfileException {
        return filterByCategory(statements, ItsActivityTypeConcepts.AssessmentNode.Concept.getInstance().asActivity(conceptState));
    }
    
    /**
     * Parses Result Extension Concept from Result Extensions within statement or null if not found.
     * 
     * @param stmt - Statement with Result Extensions
     * @param rExtKind - Result Extension kind to search for
     * 
     * @return Target result extension JsonNode or null
     */
    private static JsonNode findResultExtension(Statement stmt, ResultExtensionConcept rExtKind) {
        if(stmt.getResult() == null || stmt.getResult().getExtensions() == null) {
            return null;
        }
        return rExtKind.parseFromExtensions(stmt.getResult().getExtensions());
    }
    
    /**
     * Parses Performance Measure or Learner State Attribute Result Extension from statement or null if not found.
     * 
     * @param stmt - Statement to with Result Extensions
     * 
     * @return Extension value when found, null otherwise
     * 
     * @throws LmsXapiProfileException when unable to resolve Result Extension from xAPI Profile
     */
    private static JsonNode findExtensionWithAssessment(Statement stmt) throws LmsXapiProfileException {
        JsonNode extVal = null;
        ItsResultExtensionConcepts.PerformanceMeasure performanceMeasure = ItsResultExtensionConcepts.PerformanceMeasure.getInstance();
        ItsResultExtensionConcepts.AttributeMeasure learnerStateAttribute = ItsResultExtensionConcepts.AttributeMeasure.getInstance();
        if(findResultExtension(stmt, performanceMeasure) != null) {
            extVal = findResultExtension(stmt, performanceMeasure);
        } else if(findResultExtension(stmt, learnerStateAttribute) != null) {
            extVal = findResultExtension(stmt, learnerStateAttribute);
        }
        return extVal;
    }
    
    /**
     * Filters based on inclusion of Result Extension with assessment for assessmentKind key; original collection is unaltered.
     * 
     * @param statements - Statements to filter
     * @param assessment - target assessment value
     * @param assessmentKind - target assessment kind (predicted | shortTerm | longTerm)
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiProfileException when unable to resolve Result Extension from xAPI Profile
     */
    private static List<Statement> filterByAssessment(List<Statement> statements, String assessment, String assessmentKind) throws LmsXapiProfileException {
        List<Statement> coll = new ArrayList<Statement>();
        for(Statement stmt : statements) {
            JsonNode extVal = findExtensionWithAssessment(stmt);
            if(extVal != null) {
                JsonNode atAssessment = extVal.get(assessmentKind);
                if(atAssessment != null) {
                    String assessmentKey = ItsResultExtensionConcepts.extensionObjectKeys.ASSESSMENT.getValue();
                    if(atAssessment.get(assessmentKey) != null) {
                        if(assessment.equalsIgnoreCase(atAssessment.get(assessmentKey).asText())) {
                            coll.add(stmt);
                        }
                    }
                }
            }
        }
        return coll;
    }
    
    /**
     * Filters based on inclusion of Result Extension with target 'predicted' assessment; original collection is unaltered.
     * 
     * @param statements - Statements to filter
     * @param assessment - target assessment value
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiProfileException when unable to resolve Result Extension from xAPI Profile
     */
    public static List<Statement> filterByPredictedAssessment(List<Statement> statements, String assessment) throws LmsXapiProfileException {
        return filterByAssessment(statements, assessment, ItsResultExtensionConcepts.extensionObjectKeys.PREDICTED.getValue());
    }
    
    /**
     * Filters based on inclusion of Result Extension with target 'shortTerm' assessment; original collection is unaltered.
     * 
     * @param statements - Statements to filter
     * @param assessment - target assessment value
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiProfileException when unable to resolve Result Extension from xAPI Profile
     */
    public static List<Statement> filterByShortTermAssessment(List<Statement> statements, String assessment) throws LmsXapiProfileException {
        return filterByAssessment(statements, assessment, ItsResultExtensionConcepts.extensionObjectKeys.SHORT_TERM.getValue());
    }
    
    /**
     * Filters based on inclusion of Result Extension with target 'longTerm' assessment; original collection is unaltered.
     * 
     * @param statements - Statements to filter
     * @param assessment - target assessment value
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiProfileException when unable to resolve Result Extension from xAPI Profile
     */
    public static List<Statement> filterByLongTermAssessment(List<Statement> statements, String assessment) throws LmsXapiProfileException {
        return filterByAssessment(statements, assessment, ItsResultExtensionConcepts.extensionObjectKeys.LONG_TERM.getValue());
    }
    
    /**
     * Filters based on inclusion of Graded Score Nodes (one of or all) within Node Hierarchy Context Extension; original collection is unaltered.
     * 
     * @param statements - Statements to Filter
     * @param containsAll - if true, all nodes must be present within Context Extension, otherwise any of the nodes must be present
     * @param nodes - GradeScoreNodes to create Activity from
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to create Activity from node
     * @throws LmsXapiProfileException when unable to resolve Node Hierarchy Context Extension or Assessment Activity Type from xAPI Profile
     */
    public static List<Statement> filterByGradedScoreNodeHierarchy(List<Statement> statements, Boolean containsAll, GradedScoreNode... nodes) throws LmsXapiActivityException, LmsXapiProfileException {
        List<Statement> coll = new ArrayList<Statement>();
        containsAll = containsAll == null ? false : containsAll;
        ItsContextExtensionConcepts.NodeHierarchy nodeHierarchy = ItsContextExtensionConcepts.NodeHierarchy.getInstance();
        MomActivityTypeConcepts.Assessment assessmentAT = MomActivityTypeConcepts.Assessment.getInstance();
        for(Statement stmt : statements) {
            Map<Integer, Activity> hierarchy = new HashMap<Integer, Activity>();
            try {
                hierarchy = nodeHierarchy.parseToMap(stmt);
            } catch (@SuppressWarnings("unused") LmsXapiExtensionException e) {
                continue;
            }
            List<Boolean> misses = new ArrayList<Boolean>();
            List<Boolean> hits = new ArrayList<Boolean>();
            for(GradedScoreNode node : nodes) {
                AssessmentActivity nodeActivity = assessmentAT.asActivity(node);
                if(!hierarchy.containsValue(nodeActivity)) {
                    misses.add(true);
                } else {
                    hits.add(true);
                }
            }
            if(containsAll) {
                if(misses.isEmpty() && !hits.isEmpty()) {
                    coll.add(stmt);
                }
            } else {
                if(!hits.isEmpty()) {
                    coll.add(stmt);
                }
            }
        }
        return coll;
    }
    
    /**
     * Filters based on inclusion of Graded Score Node within Node Hierarchy Context Extension at specified index; original collection is unaltered.
     * 
     * @param statements - Statements to filter
     * @param index - position within hierarchy
     * @param node - target node
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to create Activity from target node
     * @throws LmsXapiProfileException when unable to resolve Node Hierarchy Context Extension or Assessment Activity Type from xAPI Profile
     */
    public static List<Statement> filterByGradedScoreNodeHierarchy(List<Statement> statements, Integer index, GradedScoreNode node) throws LmsXapiActivityException, LmsXapiProfileException {
        List<Statement> coll = new ArrayList<Statement>();
        ItsContextExtensionConcepts.NodeHierarchy nodeHierarchy = ItsContextExtensionConcepts.NodeHierarchy.getInstance();
        MomActivityTypeConcepts.Assessment assessmentAT = MomActivityTypeConcepts.Assessment.getInstance();
        for(Statement stmt : statements) {
            Map<Integer, Activity> hierarchy = new HashMap<Integer, Activity>();
            try {
                hierarchy = nodeHierarchy.parseToMap(stmt);
            } catch (@SuppressWarnings("unused") LmsXapiExtensionException e) {
                continue;
            }
            AssessmentActivity nodeActivity = assessmentAT.asActivity(node);
            Activity atIdx = hierarchy.get(index);
            if(atIdx != null && nodeActivity.equals(atIdx)) {
                coll.add(stmt);
            }
        }
        return coll;
    }
    
    /**
     * Filters based on inclusion of Graded Score Node within Node Hierarchy Context Extension; original collection is unaltered.
     * 
     * @param statements - Statements to filter
     * @param node - target node
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiActivityException when unable to create Activity from target node
     * @throws LmsXapiProfileException when unable to resolve Node Hierarchy Context Extension or Assessment Activity Type from xAPI Profile
     */
    public static List<Statement> filterByGradedScoreNodeHierarchy(List<Statement> statements, GradedScoreNode node) throws LmsXapiActivityException, LmsXapiProfileException {
        return filterByGradedScoreNodeHierarchy(statements, false, node);
    }
    
    /**
     * Filters based on Performance State Attribute Result Extension property values matching passed in arguments; original collection is unaltered.
     * 
     * @param statements - Statements to filter
     * @param containsAll - If true, all provided arguments must be matched for statement to pass through filter, otherwise if any match is passes through
     * @param nodeId - AbstractPerformanceStateAttribute.nodeId
     * @param nodeState - AbstractPerformanceStateAttribute.nodeStateEnum.displayName
     * @param hasObservedAssessment - AbstractPerformanceState.containsObservedAssessment
     * @param confidence - AbstractPerformanceStateAttribute.confidence
     * @param competence - AbstractPerformanceStateAttribute.competence
     * @param trend - AbstractPerformanceStateAttribute.trend
     * 
     * @return possibly empty but not null collection of statements
     * 
     * @throws LmsXapiProfileException when unable to resolve Performance Measure Result Extension from xAPI Profile
     */
    public static List<Statement> filterByPerformanceStateAttributeProperties(List<Statement> statements, Boolean containsAll, Integer nodeId, String nodeState, 
            Boolean hasObservedAssessment, Double confidence, Double competence, Double trend) throws LmsXapiProfileException {
        
        if(nodeId == null && nodeState == null && hasObservedAssessment == null && confidence == null && competence == null && trend == null) {
            throw new IllegalArgumentException("at least one Performance State Attribute Property target value must be non-null!"); 
        }
        
        List<Statement> coll = new ArrayList<Statement>();
        containsAll = containsAll == null ? false : containsAll;
        ItsResultExtensionConcepts.PerformanceMeasure performanceMeasure = ItsResultExtensionConcepts.PerformanceMeasure.getInstance();
        for(Statement stmt : statements) {
            JsonNode extVal = findResultExtension(stmt, performanceMeasure);
            if(extVal != null) {
                // Parse
                JsonNode extNodeId = extVal.get(extensionObjectKeys.ID.getValue());
                JsonNode extNodeState = extVal.get(extensionObjectKeys.STATE.getValue());
                JsonNode extNodeObserved = extVal.get(extensionObjectKeys.OBSERVED.getValue());
                JsonNode extConfidence = extVal.get(extensionObjectKeys.CONFIDENCE.getValue());
                JsonNode extCompetence = extVal.get(extensionObjectKeys.COMPETENCE.getValue());
                JsonNode extTrend = extVal.get(extensionObjectKeys.TREND.getValue());
                // nodeId
                if(nodeId != null) {
                    if(extNodeId != null) {
                        Boolean eqIds = nodeId.equals(extNodeId.asInt()); 
                        if(containsAll && !eqIds) {
                            continue;
                        } else if(!containsAll && eqIds) {
                            coll.add(stmt);
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                // nodeState
                if(nodeState != null) {
                    if(extNodeState != null) {
                        Boolean eqStates = nodeState.equals(extNodeState.asText());
                        if(containsAll && !eqStates) {
                            continue;
                        } else if(!containsAll && eqStates) {
                            coll.add(stmt);
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                // observedAssessment
                if(hasObservedAssessment != null) {
                    if(extNodeObserved != null) {
                        Boolean eqObserved = hasObservedAssessment.equals(extNodeObserved.asBoolean());
                        if(containsAll && !eqObserved) {
                            continue;
                        } else if(!containsAll && eqObserved) {
                            coll.add(stmt);
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                // confidence
                if(confidence != null) {
                    if(extConfidence != null) {
                        Boolean eqConfidence = confidence.equals(extConfidence.asDouble());
                        if(containsAll && !eqConfidence) {
                            continue;
                        } else if(!containsAll && eqConfidence) {
                            coll.add(stmt);
                            continue;   
                        }   
                    } else {
                        continue;
                    }
                }
                // competence
                if(competence != null) {
                    if(extCompetence != null) {
                        Boolean eqCompetence = competence.equals(extCompetence.asDouble());
                        if(containsAll && !eqCompetence) {
                            continue;
                        } else if(!containsAll && eqCompetence) {
                            coll.add(stmt);
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                // trend
                if(trend != null) {
                    if(extTrend != null) {
                        Boolean eqTrend = trend.equals(extTrend.asDouble());
                        if(containsAll && !eqTrend) {
                            continue;
                        } else if(!containsAll && eqTrend) {
                            coll.add(stmt);
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                // Handle fuzzy match
                if(containsAll) {
                    coll.add(stmt);
                }
            }
        }
        return coll;
    }

    /**
     * Sort collection of statements based on event time, most recent events come first.
     * 
     * @param statements - Statements to sort
     */
    public static void sortByTimestamp(List<Statement> statements) {
        Collections.sort(statements, new Comparator<Statement>() {
            @Override
            public int compare(Statement stmt1, Statement stmt2) {
                if(stmt1.equals(stmt2)) {
                    return 0;
                }
                if(stmt1.getTimestamp() == null && stmt2.getTimestamp() == null) {
                    return 0;
                }
                if(stmt1.getTimestamp() == null) {
                    return -1;
                }
                if(stmt2.getTimestamp() == null) {
                    return 1;
                }
                if(stmt1.getTimestamp().equals(stmt2.getTimestamp())) {
                    return 0;
                }
                return stmt1.getTimestamp().isBefore(stmt2.getTimestamp()) ? 1 : -1;
            }
        });
    }
}
