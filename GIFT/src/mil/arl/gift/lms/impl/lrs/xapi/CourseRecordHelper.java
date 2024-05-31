package mil.arl.gift.lms.impl.lrs.xapi;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Group;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.StatementRef;
import com.rusticisoftware.tincan.StatementTarget;
import generated.course.ConceptNode;
import mil.arl.gift.common.LMSConnectionInfo;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.CourseRecordRef.AbstractCourseRecordRefId;
import mil.arl.gift.common.course.CourseRecordRef.UUIDCourseRecordRefIds;
import mil.arl.gift.common.enums.ScoreNodeTypeEnum;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScore;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.common.score.ScoreUtil;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsInvalidStatementException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiExtensionException;
import mil.arl.gift.lms.impl.common.LmsXapiProcessorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainSessionActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.SkillAssessmentTemplate;

/**
 * This class contains helper methods used in the handling of LMSCourseRecords.
 * 
 * These methods are used within:
 *  - generation xAPI Statement(s) from an LMSCourseRecord
 *  - generation invalidation and replacement xAPI Statement(s) from an updated LMSCourseRecord
 *  - recreation of an LMSCourseRecord from a collection of xAPI Statement(s)
 * 
 * @author Yet Analytics
 *
 */
public class CourseRecordHelper {
    
    /**
     * Actor (Agent or Group) from a RawScoreNode.
     * 
     * @param node - RawScoreNode to parse Actor from
     * 
     * @return Agent or Group
     * 
     * @throws LmsXapiAgentException when unable to create Agent from RawScoreNode 
     */
    public static Agent createActorFromRawScoreNode(RawScoreNode rsn) throws LmsXapiAgentException {
        Set<String> coll = rsn.getUsernames();
        int collsize = coll.size();
        if(coll == null || collsize == 0) {
            throw new LmsXapiAgentException("Unable to create Agent from RawScoreNode due to missing usernames!");
        }
        if(collsize == 1) {
            return PersonaHelper.createMboxAgent((String) coll.toArray()[0]);
        }
        List<Agent> members = new ArrayList<Agent>();
        for(String memberSlug : coll) {
            members.add(PersonaHelper.createMboxAgent(memberSlug));
        }
        Group anonTeam = new Group();
        anonTeam.setMembers(members);
        return anonTeam;
    }
    /**
     * Determine xAPI Statement Template for generator based on Statement Actor and presence of invalidStmtId
     * 
     * @param rsn - RawScoreNode used to derive Statement Actor
     * @param invalidStmtId - Statement Id of an out-dated xAPI Statement
     * 
     * @return xAPI Statement Template to use within generator
     * 
     * @throws LmsXapiProfileException when unable to parse template from xAPI Profile
     * @throws LmsXapiAgentException when unable to create actor from rsn
     */
    public static StatementTemplate determineTemplate(RawScoreNode rsn, UUID invalidStmtId) throws LmsXapiProfileException, LmsXapiAgentException {
        Agent actor = createActorFromRawScoreNode(rsn);
        if(invalidStmtId != null && actor instanceof Group) {
            return SkillAssessmentTemplate.TeamReplacementTemplate.getInstance();
        } else if(invalidStmtId != null) {
            return SkillAssessmentTemplate.IndividualReplacementTemplate.getInstance();
        } else if(actor instanceof Group) {
            return SkillAssessmentTemplate.TeamTemplate.getInstance();
        } else {
            return SkillAssessmentTemplate.IndividualTemplate.getInstance();
        }
    }
    /**
     * Compare ConceptNode to AbstractScoreNode to determine if they match
     * 
     * @param concept - Concept node from DKF
     * @param node - AbstractScoreNode from LMSCourseRecord
     * 
     * @return - true if match, false otherwise
     */
    public static boolean isCorrespondingNode(ConceptNode concept, AbstractScoreNode node) {
        return node.getName().equalsIgnoreCase(concept.getName());
    }
    /**
     * Compare AbstractScoreNode to possibly all nodes found within DKF ConceptNode 
     * 
     * @param concept - DKF Concept Node
     * @param node - Abstract Score Node from LMSCourseRecord
     * 
     * @return true if DKF Concept Node or child node has same name as the AbstractScoreNode
     */
    public static boolean isCourseConcept(ConceptNode concept, AbstractScoreNode node) {
        if(concept == null || node == null || node.getName() == null) {
            return false;
        }
        if(isCorrespondingNode(concept, node)) {
            return true;   
        }
        for(ConceptNode subConcept : concept.getConceptNode()) {
            if(isCourseConcept(subConcept, node)) {
                return true;
            }
        }
        return false;
    }
    /**
     * find corresponding ConceptNode within dkfConcept or its children
     * 
     * @param dkfConcept - potentially nested ConceptNode
     * @param node - AbstractScoreNode to compare to
     * 
     * @return corresponding ConceptNode if found, null otherwise
     */
    public static ConceptNode getCourseConcept(ConceptNode dkfConcept, AbstractScoreNode node) {
        // compare passed in nodes
        if(isCorrespondingNode(dkfConcept, node)) {
            return dkfConcept;
        }
        // if not found, check subConcepts until possibly found
        for(ConceptNode subConcept : dkfConcept.getConceptNode()) {
            if(subConcept != null) {
                ConceptNode match = getCourseConcept(subConcept, node);
                if(match != null) {
                    return match;
                }
            }
        }
        // not found within any of the children, return null
        return null;
    }
    /**
     * Compare AbstractScoreNode without considering type specific properties
     * 
     * @param nodeA - AbstractScoreNode to compare
     * @param nodeB - AbstractScoreNode to compare
     * 
     * @return true when common ScoreNode properties are the same, false otherwise
     */
    public static boolean isSameNodeShallow(AbstractScoreNode nodeA, AbstractScoreNode nodeB) {
        if(nodeA == null && nodeB == null) {
            throw new IllegalArgumentException("both AbstractScoreNodes are null!");
        } else if(nodeA == null && nodeB != null) {
            return false;
        } else if(nodeA != null && nodeB == null) {
            return false;
        } else if(nodeA != null && nodeB != null) {
            if(!Objects.equals(nodeA.getType(), nodeB.getType())) {
                return false;
            } else if(nodeA.getType() == ScoreNodeTypeEnum.GRADED_SCORE_NODE && 
                    nodeB.getType() == ScoreNodeTypeEnum.GRADED_SCORE_NODE) {
                // handle GradedScoreNode vs TaskScoreNode
                if(nodeA instanceof TaskScoreNode && !(nodeB instanceof TaskScoreNode)) {
                    return false;
                } else if(!(nodeA instanceof TaskScoreNode) && nodeB instanceof TaskScoreNode) {
                    return false;
                }
            }
            if(!Objects.equals(nodeA.getName(), nodeB.getName())) {
                return false;
            }
            if(!Objects.equals(nodeA.getPerformanceNodeId(), nodeB.getPerformanceNodeId())) {
                return false;
            }
            if(!Objects.equals(nodeA.getFullName(), nodeB.getFullName())) {
                return false;
            }
        }
        return true;
    }
    /**
     * Find AbstractScoreNode which has the same top level properties as target
     * 
     * @param target - AbstractScoreNode to search for
     * @param nodes - collection of nodes to search
     * 
     * @return node if found, null otherwise
     */
    public static AbstractScoreNode findNodeInCollShallow(AbstractScoreNode target, List<AbstractScoreNode> nodes) {
        for(AbstractScoreNode node : nodes) {
            if(isSameNodeShallow(target, node)) {
                return node;
            }
        }
        return null;
    }
    /**
     * Search collection of AbstractScoreNode for matching RawScoreNode via isSameNode
     * 
     * @param target - RawScoreNode to search for
     * @param nodes - collection of nodes to search
     * 
     * @return node if found, null otherwise
     */
    public static RawScoreNode findNodeInCollStrict(RawScoreNode target, List<AbstractScoreNode> nodes) {
        for(AbstractScoreNode node : nodes) {
            if(node instanceof RawScoreNode) {
                if(isSameNode(target, (RawScoreNode) node)) {
                    return (RawScoreNode) node;
                }
            }
        }
        return null;
    }
    /**
     * Search collection of AbstractScoreNode for matching GradedScoreNode via isSameNode
     * 
     * @param target - GradedScoreNode to search for
     * @param nodes - collection of nodes to search
     * 
     * @return node if found, null otherwise
     */
    public static GradedScoreNode findNodeInCollStrict(GradedScoreNode target, List<AbstractScoreNode> nodes) {
        for(AbstractScoreNode node : nodes) {
            if(node instanceof GradedScoreNode) {
                if(isSameNode(target, (GradedScoreNode) node)) {
                    return (GradedScoreNode) node;
                }
            }
        }
        return null;
    }
    /**
     * Search collection of AbstractScoreNode for matching TaskScoreNode via isSameNode
     * 
     * @param target - TaskScoreNode to search for
     * @param nodes - collection of nodes to search
     * 
     * @return node if found, null otherwise
     */
    public static TaskScoreNode findNodeInCollStrict(TaskScoreNode target, List<AbstractScoreNode> nodes) {
        for(AbstractScoreNode node : nodes) {
            if(node instanceof TaskScoreNode) {
                if(isSameNode(target, (TaskScoreNode) node)) {
                    return (TaskScoreNode) node;
                }
            }
        }
        return null;
    }
    /**
     * Search collection of AbstractScoreNode for RawScoreNode that matches target at the AbstractScoreNode level and
     * has the same user names and RawScore units
     * 
     * @param target - RawScoreNode to match against
     * @param nodes - collection of nodes to search
     * 
     * @return node if found, null otherwise
     */
    public static RawScoreNode findCorrespondingNodeInColl(RawScoreNode target, List<AbstractScoreNode> nodes) {
        for(AbstractScoreNode node : nodes) {
            if(node instanceof RawScoreNode && isSameNodeShallow(target, node)) {
                // Check for matching user names
                if(Objects.equals(target.getUsernames(), ((RawScoreNode) node).getUsernames())) {
                    // Check for matching Units
                    RawScore rawScoreA = target.getRawScore();
                    RawScore rawScoreB = ((RawScoreNode) node).getRawScore();
                    if(Objects.equals(rawScoreA.getUnitsLabel(), rawScoreB.getUnitsLabel())) {
                        return (RawScoreNode) node;
                    }
                }
            }
        }
        return null;
    }
    /**
     * Determine if two RawScoreNodes are the same
     * 
     * @param nodeA - RawScoreNode to compare
     * @param nodeB - RawScoreNode to compare
     * 
     * @return false if difference detected, true otherwise
     */
    public static boolean isSameNode(RawScoreNode nodeA, RawScoreNode nodeB) {
        if(!isSameNodeShallow(nodeA, nodeB)) {
            return false;
        } else {
            if(!Objects.equals(nodeA.getUsernames(), nodeB.getUsernames())) {
                return false;
            }
            if(!Objects.equals(nodeA.getAssessment(), nodeB.getAssessment())) {
                return false;
            }
            RawScore rawScoreA = nodeA.getRawScore();
            RawScore rawScoreB = nodeB.getRawScore();
            if(!Objects.equals(rawScoreA.getUnitsLabel(), rawScoreB.getUnitsLabel())) {
                return false;
            }
            if(!Objects.equals(rawScoreA.getValueAsString(), rawScoreB.getValueAsString())) {
                return false;
            }
        }
        return true;
    }
    /**
     * Determine if two GradedScoreNodes are the same
     *  
     * @param nodeA - GradedScoreNode to compare
     * @param nodeB - GradedScoreNode to compare
     * 
     * @return false if difference detected, true otherwise
     */
    public static boolean isSameNode(GradedScoreNode nodeA, GradedScoreNode nodeB) {
        if(!isSameNodeShallow(nodeA, nodeB)) {
            return false;
        } else {
            if(!Objects.equals(nodeA.getAssessment(), nodeB.getAssessment())) {
                return false;
            }
            List<AbstractScoreNode> childrenA = nodeA.getChildren();
            List<AbstractScoreNode> childrenB = nodeB.getChildren();
            if(childrenA.size() != childrenB.size()) {
                return false;
            }
            for(AbstractScoreNode childA : childrenA) {
                if(childA.getType() == ScoreNodeTypeEnum.RAW_SCORE_NODE) {
                    RawScoreNode childB = findNodeInCollStrict((RawScoreNode) childA, childrenB);
                    if(childB == null) {
                        return false;
                    }
                } else if(childA.getType() == ScoreNodeTypeEnum.GRADED_SCORE_NODE) {
                    GradedScoreNode childB;
                    if(childA instanceof TaskScoreNode) {
                        childB = findNodeInCollStrict((TaskScoreNode) childA, childrenB);
                        if(childB == null) {
                            return false;
                        }
                    } else {
                        childB = findNodeInCollStrict((GradedScoreNode) childA, childrenB);
                        if(childB == null) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    /**
     * Determine if two TaskScoreNodes are the same, first by comparing as GradedScoreNodes and
     * then checking Stress / Difficulty values.
     * 
     * @param nodeA - TaskScoreNode to compare
     * @param nodeB - TaskScoreNode to compare
     * 
     * @return false if difference detected, true otherwise
     */
    public static boolean isSameNode(TaskScoreNode nodeA, TaskScoreNode nodeB) {
        if(!isSameNode((GradedScoreNode) nodeA, (GradedScoreNode) nodeB)) {
            return false;
        } else {
            // Stress / Difficulty comparison
            if(!Objects.equals(nodeA.getDifficulty(), nodeB.getDifficulty())) {
                return false;
            }
            if(!Objects.equals(nodeA.getDifficultyReason(), nodeB.getDifficultyReason())) {
                return false;
            }
            if(!Objects.equals(nodeA.getStress(), nodeB.getStress())) {
                return false;
            }
            if(!Objects.equals(nodeA.getStressReason(), nodeB.getStressReason())) {
                return false;
            }
        }
        return true;
    }
    /**
     * Given a list of GradedScoreNodes, return a list of TaskScoreNodes. The list of GradedScoreNodes
     * is not altered.
     * 
     * @param nodes - list of GradedScoreNodes, some of which may be TaskScoreNodes
     * 
     * @return possibly empty, never null list of TaskScoreNodes
     */
    public static List<TaskScoreNode> filterForTaskNodes(List<GradedScoreNode> nodes) {
        List<TaskScoreNode> taskNodes = new ArrayList<TaskScoreNode>();
        for(GradedScoreNode node : nodes) {
            if(node instanceof TaskScoreNode) {
                taskNodes.add((TaskScoreNode) node);
            }
        }
        return taskNodes;
    }
    /**
     * Helper for parsing and setting node id from corresponding statement context extension
     * 
     * @param node - GradedScoreNode whose performance node id is set
     * @param nodeActivity - xAPI Activity corresponding to GradedScoreNode
     * @param statement - xAPI Statement that contains xAPI Activity and NodeIdMapping Context Extension
     * 
     * @throws LmsXapiProcessorException when unable to parse NodeIdMapping from xAPI Profile
     */
    private static void parseAndSetNodeId(GradedScoreNode node, AssessmentActivity nodeActivity, Statement statement) throws LmsXapiProcessorException {
        // Activity Id to Node Id mapping
        ItsContextExtensionConcepts.NodeIdMapping mappingExt;
        try {
            mappingExt = ItsContextExtensionConcepts.NodeIdMapping.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("Unable to init Node Id Mapping Context Extension Concept!", e);
        }
        JsonNode idExt = mappingExt.parseFromExtensions(statement.getContext().getExtensions());
        if(idExt != null) {
            JsonNode nodeIdJson = idExt.get(nodeActivity.getId().toString());
            if(nodeIdJson != null && !nodeIdJson.isNull()) {
                Integer nodeId = Integer.parseInt(nodeIdJson.asText());
                node.setPerformanceNodeId(nodeId);
            }
        }
    }
    /**
     * Convert nodeActivity into TaskScoreNode or GradedScoreNode based on presence of the
     * nodeActivity id within the PerformanceCharacteristics extension
     * 
     * @param nodeActivity - AssessmentActivity to convert to Graded||TaskScoreNode
     * @param pcExt - PerformanceCharacteristics extension parsed from xAPI Statement
     * 
     * @return GradedScoreNode or TaskScoreNode with unique values parsed from extension
     */
    private static GradedScoreNode parseFromExtension(AssessmentActivity nodeActivity, JsonNode pcExt) {
        if(pcExt == null) {
            return new GradedScoreNode(nodeActivity.parseActivityName());
        }
        JsonNode tasks = pcExt.get(ItsContextExtensionConcepts.extensionObjectKeys.TASKS.getValue());
        String activityId = nodeActivity.getId().toString();
        GradedScoreNode node = null;
        for(int i = 0; i < tasks.size(); i++) {
            JsonNode task = tasks.get(i);
            if(activityId.equals(task.get(ItsContextExtensionConcepts.extensionObjectKeys.TASK.getValue()).asText())) {
                node = new TaskScoreNode(nodeActivity.parseActivityName());
                JsonNode stress = task.get(ItsContextExtensionConcepts.extensionObjectKeys.STRESS.getValue());
                if(stress != null) {
                    ((TaskScoreNode) node).setStress(stress.asDouble());
                }
                JsonNode stressReason = task.get(ItsContextExtensionConcepts.extensionObjectKeys.STRESS_REASON.getValue());
                if(stressReason != null) {
                    ((TaskScoreNode) node).setStressReason(stressReason.asText());
                }
                JsonNode difficulty = task.get(ItsContextExtensionConcepts.extensionObjectKeys.DIFFICULTY.getValue());
                if(difficulty != null) {
                    ((TaskScoreNode) node).setDifficulty(difficulty.asDouble());
                }
                JsonNode difficultyReason = task.get(ItsContextExtensionConcepts.extensionObjectKeys.DIFFICULTY_REASON.getValue());
                if(difficultyReason != null) {
                    ((TaskScoreNode) node).setDifficultyReason(difficultyReason.asText());
                }
                break;
            }
        }
        if(node == null) {
            node = new GradedScoreNode(nodeActivity.parseActivityName());
        }
        return node;
    }
    /**
     * Recursive search for RawScoreNode
     *  
     * @param node - AbstractScoreNode to search
     *  
     * @return RawScoreNode child when found, null otherwise
     */
    public static RawScoreNode findRsn(AbstractScoreNode node) {
        if(node == null) {
            return null;
        }
        if(node.getType() == ScoreNodeTypeEnum.GRADED_SCORE_NODE) {
            for(AbstractScoreNode child : ((GradedScoreNode) node).getChildren()) {
                return findRsn(child);
            }
        } else if(node.getType() == ScoreNodeTypeEnum.RAW_SCORE_NODE) {
            return (RawScoreNode) node;
        }
        return null;
    }
    /**
     * Return RawScoreNode reconstructed from xAPI Statement
     * 
     * @param stmt - xAPI Statement with contains GradedScoreNode(s) and RawScoreNode
     * 
     * @return reconstructed RawScoreNode
     * 
     * @throws LmsXapiProcessorException when unable to create RawScoreNode from statement
     */
    public static RawScoreNode parseRsnFromStatement(Statement statement) throws LmsXapiProcessorException {
        return findRsn(parseGradedScoreNodeFromStatement(statement));
    }
    /**
     * Parses GradedScoreNode from xAPI Statement
     *  
     * @param statement - Statement with GradedScoreNode Activity Object, CECNodeHierarchy Context Extension and RECFormative Result Extension
     * 
     * @return Possibly nested GradedScoreNode with RawScoreNode at leaf
     * 
     * @throws LmsXapiActivityException when unable to parse CECNodeHierarchy from Statement
     */
    public static GradedScoreNode parseGradedScoreNodeFromStatement(Statement statement) throws LmsXapiProcessorException {
        // PC extension in statement
        ItsContextExtensionConcepts.PerformanceCharacteristics pcCEC;
        try {
            pcCEC = ItsContextExtensionConcepts.PerformanceCharacteristics.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("Unable to init Performance Characteristics context extension!", e);
        }
        JsonNode pcExt = null;
        if(statement.getContext() != null && statement.getContext().getExtensions() != null) {
            pcExt = pcCEC.parseFromExtensions(statement.getContext().getExtensions());
        }
        // Node from Statement Object
        AssessmentActivity childActivity;
        try {
            childActivity = new AssessmentActivity((Activity) statement.getObject());
        } catch (URISyntaxException e) {
            throw new LmsXapiProcessorException("Unable to create Assessment Activity from statement object!", e);
        }
        // Leaf GradedScoreNode
        GradedScoreNode leaf = parseFromExtension(childActivity, pcExt);
        parseAndSetNodeId(leaf, childActivity, statement);
        // Add RawScoreNode
        ItsResultExtensionConcepts.ConceptEvaluation resultExt;
        try {
            resultExt = ItsResultExtensionConcepts.ConceptEvaluation.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("Unable to initialize Concept Evaluation extension!", e);
        }
        try {
            resultExt.parseToGradedScoreNode(leaf, statement);
        } catch (LmsXapiExtensionException e) {
            throw new LmsXapiProcessorException("Unable to create graded score node from statement!", e);
        }
        // Merge into History of GradedScoreNodes
        GradedScoreNode branch = null;
        ItsContextExtensionConcepts.NodeHierarchy contextExt;
        try {
            contextExt = ItsContextExtensionConcepts.NodeHierarchy.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("Unable to initialize node hierarchy extension!", e);
        }
        Map<Integer, Activity> hierarchy;
        try {
            hierarchy = contextExt.parseToMap(statement);
        } catch (LmsXapiExtensionException e) {
            throw new LmsXapiProcessorException("Unable to parse graded score node hierarchy from statement!", e);
        }
        if(CollectionUtils.isNotEmpty(hierarchy)) {
            // start at parent and walk back hierarchy
            Integer lastIdx = hierarchy.size() - 1;
            // one ancestor vs more than one
            if(lastIdx == 0) {
                // parent only ancestor
                AssessmentActivity parentActivity;
                try {
                    parentActivity = new AssessmentActivity(hierarchy.get(lastIdx));
                } catch (URISyntaxException e) {
                    throw new LmsXapiProcessorException("Unable to create parent assessment activity from hierarchy!", e);
                }
                GradedScoreNode parent = parseFromExtension(parentActivity, pcExt);
                parseAndSetNodeId(parent, parentActivity, statement);
                parent.addChild(leaf);
                branch = parent;
            } else {
                // construct root
                GradedScoreNode current = null;
                for(int idx = lastIdx; idx >= 0; idx--) {
                    AssessmentActivity ancestorActivity;
                    try {
                        ancestorActivity = new AssessmentActivity(hierarchy.get(idx));
                    } catch (URISyntaxException e) {
                        throw new LmsXapiProcessorException("Unable to create ancestor assessment activity from hierarchy!", e);
                    }
                    GradedScoreNode ancestor = parseFromExtension(ancestorActivity, pcExt);
                    parseAndSetNodeId(ancestor, ancestorActivity, statement);
                    if(idx == lastIdx) {
                        // ancestor is parent
                        ancestor.addChild(leaf);
                        current = ancestor;
                    } else if(idx == 0) {
                        // at root
                        ancestor.addChild(current);
                        branch = ancestor;
                    } else {
                        // ancestor is grandparent+
                        ancestor.addChild(current);
                        current = ancestor;
                    }
                }
            }
        }
        if(branch == null) {
            return leaf;
        } else {
            return branch;
        }
    }
    /**
     * merge trunk and branch into common, trunk children take priority when in conflict with branch children
     * 
     * @param common - common parent for children of trunk and branch
     * @param trunk - GradedScoreNode that serves as basis of common
     * @param branch - GradedScoreNode to integrate into trunk
     * 
     * @return - GradedScoreNode that's the union of trunk and branch
     */
    private static GradedScoreNode rebaseGradedScoreNodes(GradedScoreNode common, GradedScoreNode trunk, GradedScoreNode branch) {
        if(!trunk.isLeaf() && !branch.isLeaf()) {
            // children - trunk
            List<AbstractScoreNode> childrenTrunk = trunk.getChildren();
            List<String> childrenNamesTrunk = new ArrayList<String>();
            for(AbstractScoreNode cTrunk : childrenTrunk) {
                childrenNamesTrunk.add(cTrunk.getName());
            }
            // children - branch
            List<AbstractScoreNode> childrenBranch = branch.getChildren();
            List<String> childrenNamesBranch = new ArrayList<String>();
            for(AbstractScoreNode cBranch : childrenBranch) {
                childrenNamesBranch.add(cBranch.getName());
            }
            // Iterate over trunk, compare to branch
            for(AbstractScoreNode childNodeTrunk : childrenTrunk) {
                String childNodeNameTrunk = childNodeTrunk.getName();
                if(!childrenNamesBranch.contains(childNodeNameTrunk)) {
                    // unknown to Branch, add to common
                    common.addChild(childNodeTrunk);
                }
            }
            // Iterate over Branch, compare to Trunk
            for(AbstractScoreNode childNodeBranch : childrenBranch) {
                String childNodeNameBranch = childNodeBranch.getName();
                if(!childrenNamesTrunk.contains(childNodeNameBranch)) {
                    // unknown to Trunk, add to common
                    common.addChild(childNodeBranch);
                }
            }
            // handle Children common to Branch and Trunk
            List<String> uncommonChildrenNames = new ArrayList<String>();
            // only disjoint set of children has been added to common so far
            for(AbstractScoreNode uncommonChildNode : common.getChildren()) {
                uncommonChildrenNames.add(uncommonChildNode.getName());
            }
            // Iterate over trunk, find children shared with branch
            for(AbstractScoreNode trunkChildNode : childrenTrunk) {
                String commonChildNodeName = trunkChildNode.getName();
                if(!uncommonChildrenNames.contains(commonChildNodeName)) {
                    // child shared between trunk and branch
                    if(trunkChildNode.getType() == ScoreNodeTypeEnum.GRADED_SCORE_NODE) {
                        // find corresponding child in branch
                        GradedScoreNode branchChildNode = null;
                        for(AbstractScoreNode branchChild : childrenBranch) {
                            String branchChildName = branchChild.getName();
                            if(!uncommonChildrenNames.contains(branchChildName)) {
                                // child shared between trunk and branch
                                if(branchChild.getType() == ScoreNodeTypeEnum.GRADED_SCORE_NODE && branchChildName.equals(commonChildNodeName)) {
                                    branchChildNode = (GradedScoreNode) branchChild;
                                }
                            }
                        }
                        // rebase trunkChildNode based on branchChildNode
                        GradedScoreNode commonChild = rebaseGradedScoreNodes((GradedScoreNode) trunkChildNode, branchChildNode);
                        common.addChild(commonChild);
                    } else {
                        // use what's in the trunk, no merge/comparison performed!
                        common.addChild(trunkChildNode);
                    }
                }
            }
        } else if(trunk.isLeaf() && !branch.isLeaf()) {
            // Branch added children to trunk node
            List<AbstractScoreNode> newBranchChildren = branch.getChildren();
            for(AbstractScoreNode newBranchChild : newBranchChildren) {
                common.addChild(newBranchChild);
            }
        } else if(!trunk.isLeaf() && branch.isLeaf()) {
            // Branch removed all children from trunk node, use what's in the trunk
            List<AbstractScoreNode> existingTrunkChildren = trunk.getChildren();
            for(AbstractScoreNode existingTrunkChild : existingTrunkChildren) {
                common.addChild(existingTrunkChild);
            }
        } else if(trunk.isLeaf() && branch.isLeaf()) {
            // Update common vs different leafs
            if(trunk.getName() != branch.getName()) {
                // different GradedScoreNodes, common is now their shared parent
                common.addChild(trunk);
                common.addChild(branch);
            } else {
                // same GradedScoreNode, return Trunk as common
                common = trunk;
            }
        }
        return common;
    }
    /**
     * Rebase trunk to include branch when they share the same root
     * 
     * @param trunk - GradedScoreNode to merge
     * @param branch - GradedScoreNode to merge
     * 
     * @return - root composed of trunk + branch or null when trunk and branch don't share the same root
     */
    private static GradedScoreNode rebaseGradedScoreNodes(GradedScoreNode trunk, GradedScoreNode branch) {
        String aName = trunk.getName();
        String bName = branch.getName();
        if(!aName.equals(bName)) {
            return null;
        } else if (trunk instanceof TaskScoreNode) {
            TaskScoreNode common = new TaskScoreNode(aName, trunk.getAssessment());
            common.setStress(((TaskScoreNode) trunk).getStress());
            common.setStressReason(((TaskScoreNode) trunk).getStressReason());
            common.setDifficulty(((TaskScoreNode) trunk).getDifficulty());
            common.setDifficultyReason(((TaskScoreNode) trunk).getDifficultyReason());
            return rebaseGradedScoreNodes(common, trunk, branch);
        } else {
            return rebaseGradedScoreNodes(new GradedScoreNode(aName, trunk.getAssessment()), trunk, branch);
        }
    }
    /**
     * Navigates into GradedScoreNode as dictated by path, once at target GradedScoreNode, add RawScoreNode
     * as child.
     * 
     * @param node - GradedScoreNode to navigate into or add RawScoreNode to
     * @param rsn - RawScoreNode to add
     * @param path - collection of GradedScoreNode names denoting navigation pathway
     * @param idx - position within path
     */
    private static void updateNodeWithinRoot(GradedScoreNode node, RawScoreNode rsn, String[] path, int idx) {
        int end = path.length - 1;
        if(idx == end && path[idx].equals(node.getName())) {
            node.addChild(rsn);
        } else {
            for(AbstractScoreNode child : node.getChildren()) {
                if(child.getType() == ScoreNodeTypeEnum.GRADED_SCORE_NODE && child.getName().equals(path[idx])) {
                    if(idx != end) {
                        idx++;
                    }
                    updateNodeWithinRoot((GradedScoreNode) child, rsn, path, idx);
                }
            }
        }
    }
    /**
     * Adds RawScoreNode to some level of depth within GradedScoreNode.
     * 
     * @param root - Root GradedScoreNode from LMSCourseRecord
     * @param leaf - AssessmentActivity parsed from a sumative assessment xAPI Statement
     * @param rsn - RawScoreNode to add at some depth within root
     */
    private static void updateNodeWithinRoot(GradedScoreNode root, AssessmentActivity leaf, RawScoreNode rsn) {
        // no children
        if(root.isLeaf() && root.getName().equals(leaf.parseActivityName())) {
            root.addChild(rsn);
        } else {
            // nav control
            String[] path = leaf.parseActivityDescription().split("\\|");
            int idx = 0;
            // start with root
            if(path[idx].equals(root.getName())) {
                idx++;
                updateNodeWithinRoot(root, rsn, path, idx);
            }
        }
    }
    /**
     * Parse collection of summative assessment statements into a LMSCourseRecord
     * 
     * @param statements - Statements with the same Course Record Context Activity
     * @param recordRef - reference for the target Course Record Activity
     * 
     * @return courseRecord derived from statements
     */
    public static LMSCourseRecord deriveCourseRecord(List<Statement> statements, CourseRecordRef recordRef, LMSConnectionInfo connInfo) throws LmsXapiProcessorException {
        LMSCourseRecord record = null;
        if(CollectionUtils.isNotEmpty(statements)) {
            // keep track of branches
            Set<String> branches = new HashSet<String>();
            GradedScoreNode root = null;
            for(Statement stmt : statements) {
                // a is leaf node within original LMSCourseRecord
                AssessmentActivity a;
                try {
                    a = new AssessmentActivity((Activity) stmt.getObject());
                } catch (URISyntaxException e) {
                    throw new LmsXapiProcessorException("Unable to create Assessment Activity from Statement Object!", e);
                }
                String path = a.parseActivityDescription();
                // build root from each distinct leaf
                if(!branches.contains(path) && root == null) {
                    // first novel pathway to leaf GradedScoreNode + RawScoreNode
                    // any branch can be starting point of reconstruction
                    branches.add(path);
                    try {
                        root = parseGradedScoreNodeFromStatement(stmt);
                    } catch(LmsXapiProcessorException e) {
                        throw new LmsXapiProcessorException("Unable to parse GradedScoreNode from Statement!", e);
                    }
                } else if(!branches.contains(path) && root != null) {
                    // novel pathway to leaf GradedScoreNode + RawScoreNode
                    // merge with working copy of root
                    GradedScoreNode branch = null;
                    branches.add(path);
                    try {
                        branch = parseGradedScoreNodeFromStatement(stmt);
                    } catch(LmsXapiProcessorException e) {
                        throw new LmsXapiProcessorException("Unable to parse GradedScoreNode from Statement!", e);
                    }
                    if(branch != null) {
                        // rebase root to include branch
                        root = rebaseGradedScoreNodes(root, branch);
                    }
                } else if(branches.contains(path) && root != null) {
                    // Handle Graded Score Node with more than 1 raw score node
                    GradedScoreNode commonLeafGsn = parseGradedScoreNodeFromStatement(stmt);
                    // One RSN per stmt
                    RawScoreNode rsn = findRsn(commonLeafGsn);
                    // Navigate into root and update Graded Score Node with Raw Score Node 
                    updateNodeWithinRoot(root, a, rsn);
                } else if(branches.contains(path) && root == null) {
                    // Fail safe case, Root contains the RSN for the current leaf GSN
                    try {
                        root = parseGradedScoreNodeFromStatement(stmt);
                    } catch(LmsXapiProcessorException e) {
                        throw new LmsXapiProcessorException("Unable to parse GradedScoreNode from Statement!", e);
                    }
                }
            }
            if(root != null) {
                // Feb 2022 - Explanation for calling rollup logic:
                // GradedScoreNodes do not have their own statements but the GradedScoreNode assessment value is included within the summative assessment statements
                // Example: 
                //   GradedScoreNode A
                //      RawScoreNode X
                //      RawScoreNode Y
                //  Above results in the creation of 2 xAPI Statements
                //   1. The xAPI Statement for RawScoreNode X includes GradedScoreNode A's assessment and RawScoreNode X's objects attributes
                //   2. The xAPI Statement for RawScoreNode Y includes GradedScoreNode A's assessment and RawScoreNode Y's object attributesMH: why do we need this call, don't the statements contain the rollup scores from when they were created?
                ScoreUtil.performAssessmentRollup(root, true);
                Statement fStmt = statements.get(0);
                Date eventDateTime = fStmt.getTimestamp().toDate();
                ItsActivityTypeConcepts.Domain domainATC;
                try {
                    domainATC = ItsActivityTypeConcepts.Domain.getInstance();
                } catch (LmsXapiProfileException e) {
                    throw new LmsXapiProcessorException("Unable to initialize Domain Activity Type Concept!", e);
                }
                List<Activity> domainActivities;
                try {
                    domainActivities = domainATC.findInstancesInStatement(fStmt);
                } catch (LmsInvalidStatementException e) {
                    throw new LmsXapiProcessorException("Unable to parse Domain Activity from Statement!", e);
                }
                DomainActivity domain = null;
                if(CollectionUtils.isNotEmpty(domainActivities)) {
                    try {
                        domain = new DomainActivity(domainActivities.get(0));
                    } catch (URISyntaxException e) {
                        throw new LmsXapiProcessorException("Unable to initialize Domain Activity!", e);
                    }
                }
                ItsActivityTypeConcepts.DomainSession domainSessionATC;
                try {
                    domainSessionATC = ItsActivityTypeConcepts.DomainSession.getInstance();
                } catch (LmsXapiProfileException e) {
                    throw new LmsXapiProcessorException("Unable to initialize Domain Session Activity Type Concept!", e);
                }
                List<Activity> domainSessionActivities;
                try {
                    domainSessionActivities = domainSessionATC.findInstancesInStatement(fStmt);
                } catch (LmsInvalidStatementException e) {
                    throw new LmsXapiProcessorException("Unable to parse domain session activities from statement!", e);
                }
                DomainSessionActivity domainSession = null;
                if(CollectionUtils.isNotEmpty(domainSessionActivities)) {
                    try {
                        domainSession = new DomainSessionActivity(domainSessionATC.findInstancesInStatement(fStmt).get(0));
                    } catch (LmsInvalidStatementException | URISyntaxException e) {
                        throw new LmsXapiProcessorException("Unable to initialize Domain Session Activity!", e);
                    }
                }
                if(domain != null && domainSession != null) {
                    record = new LMSCourseRecord(recordRef, domain.parseActivityName(), root, eventDateTime);
                    if(connInfo != null) {
                        record.setLMSConnectionInfo(connInfo);
                    }
                    record.setGiftEventId(Integer.parseInt(domainSession.parseActivityName()));
                }
            }
        }
        return record;
    }
    /**
     * Ensures that the course record reference for the updated LMS Course Record reflects the results of
     * the CourseRecordInvalidationProcessor. Only supports UUIDCourseRecordRefIds references.
     * 
     * @param originalRef - CourseRecordRef from the original LMSCourseRecord
     * @param updatedRef - CourseRecordRef from the updated LMSCourseRecord
     * @param statements - xAPI Statements created by the CourseRecordInvalidationProcessor
     */
    public static void updateCourseRecordRef(CourseRecordRef originalRef, CourseRecordRef updatedRef, List<Statement> statements) {
        if(statements == null) {
            throw new IllegalArgumentException("statements can not be null!");
        }
        if(originalRef == null || updatedRef == null) {
            throw new IllegalArgumentException("course record ref can not be null!");
        }
        AbstractCourseRecordRefId originalRefId = originalRef.getRef();
        AbstractCourseRecordRefId updatedRefId = updatedRef.getRef();
        if(originalRefId != null && originalRefId instanceof UUIDCourseRecordRefIds) {
            // xAPI Statement identifiers from original course record
            ArrayList<String> originalStmtIdColl = ((UUIDCourseRecordRefIds) originalRefId).getRecordUUIDs() != null ?
                    ((UUIDCourseRecordRefIds) originalRefId).getRecordUUIDs() : new ArrayList<String>();
            Set<String> ogStmtIdSet = new HashSet<String>(originalStmtIdColl);
            // xAPI Statement identifiers for updated course record
            UUIDCourseRecordRefIds updatedStmtRefIds = (updatedRefId != null && updatedRefId instanceof UUIDCourseRecordRefIds) 
                    ? (UUIDCourseRecordRefIds) updatedRefId : new UUIDCourseRecordRefIds();
            // update ogStmtIdSet to ensure non-invalidated statement identifiers are found within updated reference
            for(Statement stmt : statements) {
                StatementTarget obj = stmt.getObject();
                if(obj instanceof StatementRef) {
                    String invalidatedStmtId = ((StatementRef) obj).getId().toString();
                    ogStmtIdSet.remove(invalidatedStmtId);
                } else {
                    String stmtId = stmt.getId().toString();
                    updatedStmtRefIds.addRecordUUID(stmtId);
                }
            }
            // novel and/or replacement statement identifiers + remaining original statement identifiers
            if(updatedStmtRefIds.getRecordUUIDs() != null) {
                ArrayList<String> coll = updatedStmtRefIds.getRecordUUIDs();
                coll.addAll(ogStmtIdSet);
                updatedStmtRefIds.setRecordUUIDs(coll);
            } else {
                ArrayList<String> coll = new ArrayList<String>(ogStmtIdSet);
                updatedStmtRefIds.setRecordUUIDs(coll);
            }
            updatedRef.setRef(updatedStmtRefIds);
        }
    }
}
