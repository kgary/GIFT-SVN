/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/              
package mil.arl.gift.common.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * Test methods and logic on TaskPerformanceState class.
 * @author mhoffman
 *
 */
public class TaskPerformanceStateTest {

    /**
     * This junit tests the equals methods overridden in the TaskPerformanceState and
     * related classes.
     */
    @Test
    public void equalsTest() {
        
        ConceptPerformanceState conceptStateA1 = new ConceptPerformanceState();
        ConceptPerformanceState conceptStateB1 = new ConceptPerformanceState();
        
        PerformanceStateAttribute conceptA1Attr = new PerformanceStateAttribute("Team Entered Building (HIDDEN)", 1, "1", 
                AssessmentLevelEnum.AT_EXPECTATION, 1001L, AssessmentLevelEnum.AT_EXPECTATION, 1001L, AssessmentLevelEnum.AT_EXPECTATION, 1001L);
        conceptStateA1.updateState(conceptA1Attr);
        
        PerformanceStateAttribute conceptB1Attr = new PerformanceStateAttribute("Team Entered Building (HIDDEN)", 1, "1", 
                AssessmentLevelEnum.AT_EXPECTATION, 1001L, AssessmentLevelEnum.AT_EXPECTATION, 1001L, AssessmentLevelEnum.AT_EXPECTATION, 1001L);
        conceptStateB1.updateState(conceptB1Attr);
        
        conceptA1Attr.setScenarioSupportNode(true);
        conceptB1Attr.setScenarioSupportNode(true);
        
        conceptA1Attr.setNodeStateEnum(PerformanceNodeStateEnum.ACTIVE);
        conceptB1Attr.setNodeStateEnum(PerformanceNodeStateEnum.ACTIVE);
        
        Map<String, AssessmentLevelEnum> conceptA1Team = new HashMap<>();
        Map<String, AssessmentLevelEnum> conceptB1Team = new HashMap<>();        
        
        conceptA1Team.put("Alpha_Rifleman(Adams)1", AssessmentLevelEnum.UNKNOWN);
        conceptB1Team.put("Alpha_Rifleman(Adams)1", AssessmentLevelEnum.UNKNOWN);
        
        conceptA1Attr.setAssessedTeamOrgEntities(conceptA1Team);
        conceptB1Attr.setAssessedTeamOrgEntities(conceptB1Team);
        
        List<ConceptPerformanceState> taskAConcepts = new ArrayList<>();
        taskAConcepts.add(conceptStateA1);
        
        List<ConceptPerformanceState> taskBConcepts = new ArrayList<>();
        taskBConcepts.add(conceptStateB1);
        
        PerformanceStateAttribute taskAAttr = new PerformanceStateAttribute("task", 1, "8fd05f9d-f62a-4c23-bd04-315cb45591e2", 
                AssessmentLevelEnum.AT_EXPECTATION, AssessmentLevelEnum.AT_EXPECTATION, AssessmentLevelEnum.AT_EXPECTATION);
        
        PerformanceStateAttribute taskBAttr = new PerformanceStateAttribute("task", 1, "8fd05f9d-f62a-4c23-bd04-315cb45591e2", 
                AssessmentLevelEnum.AT_EXPECTATION, AssessmentLevelEnum.AT_EXPECTATION, AssessmentLevelEnum.AT_EXPECTATION);

        TaskPerformanceState taskStateA = new TaskPerformanceState(taskAAttr, taskAConcepts);
        TaskPerformanceState taskStateB = new TaskPerformanceState(taskBAttr, taskBConcepts);
        
        // this values should both be true and therefore equal to each other
        boolean objectDeepEquals = Objects.deepEquals(taskStateA, taskStateB);
        boolean giftEquals = taskStateA.equals(taskStateB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals, objectDeepEquals && giftEquals);
        System.out.println("Finished equalsTest 1");
        
    }
    
    @Test
    public void equalsTestTwo() {
        
// Old State: 67=[TaskPerformanceState: 
//      state = [PerformanceStateAttribute: name = tsAttr1, course id = 6a6c100e-8471-4bea-a960-404b89da2941, id = 67, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null], difficulty = 1.0, difficulty-reason = null, stress = null, stress-reason = null,
//      concepts = {
//              [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr1, course id = 8c242e5f-9f83-446e-9f68-6e418aba0d1c, id = 32, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//              [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr2, course id = 1b1dcef4-34cf-4907-96eb-4d2074100bbe, id = 31, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {FireTeam Leader=Unknown, Automatic Rifleman=Unknown, Rifleman=Unknown, Grenadier=Unknown}, performanceAssessmentTime = 0, authoritativeResource = null]],
//              [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr3, course id = 6cc8b49c-5880-4a4b-80e1-8560b1a0e2be, id = 34, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//              [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr4, course id = 84517514-b66b-438a-be05-c379577bc365, id = 33, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]], }]
// New State: 67=[TaskPerformanceState: 
//      state = [PerformanceStateAttribute: name = tsAttr1, course id = 6a6c100e-8471-4bea-a960-404b89da2941, id = 67, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null], difficulty = 1.0, difficulty-reason = null, stress = null, stress-reason = null,
//      concepts = {
//              [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr1, course id = 8c242e5f-9f83-446e-9f68-6e418aba0d1c, id = 32, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//              [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr2, course id = 1b1dcef4-34cf-4907-96eb-4d2074100bbe, id = 31, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {FireTeam Leader=Unknown, Automatic Rifleman=Unknown, Rifleman=Unknown, Grenadier=Unknown}, performanceAssessmentTime = 0, authoritativeResource = null]],
//              [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr3, course id = 6cc8b49c-5880-4a4b-80e1-8560b1a0e2be, id = 34, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//              [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr4, course id = 84517514-b66b-438a-be05-c379577bc365, id = 33, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]], }]
        
        TaskPerformanceState tsA, tsB;
        ConceptPerformanceState csA1, csA2, csA3, csA4, csB1, csB2, csB3, csB4;
        PerformanceStateAttribute
        tsAttrA,
            csAttrA1, csAttrA2, csAttrA3, csAttrA4,
        tsAttrB,
            csAttrB1, csAttrB2, csAttrB3, csAttrB4;
        
        csAttrA1 = new PerformanceStateAttribute("csAttr1", 32, "8c242e5f-9f83-446e-9f68-6e418aba0d1c", 
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csAttrB1 = new PerformanceStateAttribute("csAttr1", 32, "8c242e5f-9f83-446e-9f68-6e418aba0d1c", 
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        
        csAttrA2 = new PerformanceStateAttribute("csAttr2", 31, "1b1dcef4-34cf-4907-96eb-4d2074100bbe", 
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csAttrB2 = new PerformanceStateAttribute("csAttr2", 31, "1b1dcef4-34cf-4907-96eb-4d2074100bbe", 
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        Map<String, AssessmentLevelEnum> assessedTOEA2 = new HashMap<>();
        assessedTOEA2.put("FireTeam Leader", AssessmentLevelEnum.UNKNOWN);
        assessedTOEA2.put("Automatic Rifleman", AssessmentLevelEnum.UNKNOWN);
        assessedTOEA2.put("Rifleman", AssessmentLevelEnum.UNKNOWN);
        assessedTOEA2.put("Grenadier", AssessmentLevelEnum.UNKNOWN);
        csAttrA2.setAssessedTeamOrgEntities(assessedTOEA2);
        Map<String, AssessmentLevelEnum> assessedTOEB2 = new HashMap<>();
        assessedTOEB2.put("FireTeam Leader", AssessmentLevelEnum.UNKNOWN);
        assessedTOEB2.put("Automatic Rifleman", AssessmentLevelEnum.UNKNOWN);
        assessedTOEB2.put("Rifleman", AssessmentLevelEnum.UNKNOWN);
        assessedTOEB2.put("Grenadier", AssessmentLevelEnum.UNKNOWN);
        csAttrB2.setAssessedTeamOrgEntities(assessedTOEB2);
        
        csAttrA3 = new PerformanceStateAttribute("csAttr3", 34, "6cc8b49c-5880-4a4b-80e1-8560b1a0e2be",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csAttrB3 = new PerformanceStateAttribute("csAttr3", 34, "6cc8b49c-5880-4a4b-80e1-8560b1a0e2be",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        
        csAttrA4 = new PerformanceStateAttribute("csAttr4", 33, "84517514-b66b-438a-be05-c379577bc365",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csAttrB4 = new PerformanceStateAttribute("csAttr4", 33, "84517514-b66b-438a-be05-c379577bc365",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        
        csA1 = new ConceptPerformanceState(csAttrA1);
        csA2 = new ConceptPerformanceState(csAttrA2);
        csA3 = new ConceptPerformanceState(csAttrA3);
        csA4 = new ConceptPerformanceState(csAttrA4);
        List<ConceptPerformanceState> taskAConcepts = new ArrayList<>();
        taskAConcepts.add(csA1);
        taskAConcepts.add(csA2);
        taskAConcepts.add(csA3);
        taskAConcepts.add(csA4);
        tsAttrA = new PerformanceStateAttribute("tsAttr1", 67, "6a6c100e-8471-4bea-a960-404b89da2941",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        tsA = new TaskPerformanceState(tsAttrA, taskAConcepts);
        
        csB1 = new ConceptPerformanceState(csAttrB1);
        csB2 = new ConceptPerformanceState(csAttrB2);
        csB3 = new ConceptPerformanceState(csAttrB3);
        csB4 = new ConceptPerformanceState(csAttrB4);
        List<ConceptPerformanceState> taskBConcepts = new ArrayList<>();
        taskBConcepts.add(csB1);
        taskBConcepts.add(csB2);
        taskBConcepts.add(csB3);
        taskBConcepts.add(csB4);
        tsAttrB = new PerformanceStateAttribute("tsAttr1", 67, "6a6c100e-8471-4bea-a960-404b89da2941",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        tsB = new TaskPerformanceState(tsAttrB, taskBConcepts);
        
        // this values should both be true and therefore equal to each other
        boolean objectDeepEquals = Objects.deepEquals(tsA, tsB);
        boolean giftEquals = tsA.equals(tsB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals+" prior to setting top level properties", objectDeepEquals && giftEquals);
        
        tsA.setDifficultyReason("difficulty reason");
        tsB.setDifficultyReason("difficulty reason");
        objectDeepEquals = Objects.deepEquals(tsA, tsB);
        giftEquals = tsA.equals(tsB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals+" after setting difficulty reason", objectDeepEquals && giftEquals);
        
        tsA.setStressReason("stress reason");
        tsB.setStressReason("stress reason");
        objectDeepEquals = Objects.deepEquals(tsA, tsB);
        giftEquals = tsA.equals(tsB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals+" after setting stress reason", objectDeepEquals && giftEquals);
        
        // FIXME: tests fail after setting stress
        tsA.setStress(1.0);
        tsB.setStress(1.0);
        objectDeepEquals = Objects.deepEquals(tsA, tsB);
        giftEquals = tsA.equals(tsB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals+" after setting stress", objectDeepEquals && giftEquals);
        
        // FIXME: tests fail after setting difficulty
        tsA.setDifficulty(1.0);
        tsB.setDifficulty(1.0);
        objectDeepEquals = Objects.deepEquals(tsA, tsB);
        giftEquals = tsA.equals(tsB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals+" after setting difficulty", objectDeepEquals && giftEquals);
        
        System.out.println("Finished equalsTest 2");
    }
    
    @Test
    public void equalsTestThree() {
        
//        Old State: 1=[TaskPerformanceState:
//            state = [PerformanceStateAttribute: name = tsAttr, course id = 62c9b3eb-8546-49ca-9a85-0d33d1641084, id = 1, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null],
//            difficulty = 1.0, difficulty-reason = null, stress = null, stress-reason = null,
//            concepts = {
//                    [IntermediateConceptPerformanceState: [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = icsAttr1, course id = 0c35ee52-6488-4d6f-93c5-f62ed590a861, id = 42, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = AboveExpectation, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = AboveExpectation, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                     subconcepts = {
//                             [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr1, course id = c58a91e6-9b44-4d39-8936-fb4d08f0117d, id = 4, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = AtExpectation, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = AtExpectation, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {FireTeam Leader=AtExpectation, Automatic Rifleman=AtExpectation, Rifleman=AtExpectation, Grenadier=AtExpectation}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                             [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr2, course id = ded859e8-a61a-4d2d-9b43-d88b74f62676, id = 43, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                             [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr3, course id = 489944cf-0cca-46bf-bd2b-f2b8dd519131, id = 44, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]], }],
//                    [IntermediateConceptPerformanceState: [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = icsAttr2, course id = 88ab3e8b-cf07-4d32-bcf2-370fdfecd15c, id = 45, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                     subconcepts = {
//                             [IntermediateConceptPerformanceState: [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = icsAttr3, course id = c9aa8944-2ea6-4488-9b81-09a4ad625f49, id = 46, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = AtExpectation, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = AtExpectation, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                              subconcepts = {
//                                      [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr4, course id = f323cb20-48de-44ea-bef7-560557ecc629, id = 68, scenarioSupport = true, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = AtExpectation, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = AtExpectation, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {Rifleman (AI)=AtExpectation, Automatic Rifleman (AI)=AtExpectation, FireTeam Leader (AI)=AtExpectation, Grenadier (AI)=AtExpectation}, performanceAssessmentTime = 0, authoritativeResource = null]], }],
//                             [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr5, course id = 386f6afe-5e26-4a36-a2c9-7720ec9c6651, id = 47, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {FireTeam Leader=Unknown, Automatic Rifleman=Unknown, Rifleman=Unknown, Grenadier=Unknown}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                             [IntermediateConceptPerformanceState: [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = icsAttr4, course id = 9bdf0ba6-41b6-428d-bc1d-360b7a42fa1f, id = 48, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                              subconcepts = {
//                                      [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr6, course id = 9db76ff9-f381-4e41-91c9-00d2cfac712e, id = 2, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                                      [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr7, course id = e8b40f03-6b4a-46e4-a7d3-d3b2fee4dcbf, id = 11, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]], }],
//                             [IntermediateConceptPerformanceState: [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = icsAttr5, course id = 4f9c325a-5fc4-468d-bc13-495ed14de03d, id = 50, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                              subconcepts = {
//                                      [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr8, course id = 9160c7dc-bb4d-4ec3-8425-8f7ceeee23fa, id = 51, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]], }], }], }]
//                                              
//        New State: 1=[TaskPerformanceState: 
//            state = [PerformanceStateAttribute: name = tsAttr, course id = 62c9b3eb-8546-49ca-9a85-0d33d1641084, id = 1, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null],
//            difficulty = 1.0, difficulty-reason = null, stress = null, stress-reason = null,
//            concepts = {
//                    [IntermediateConceptPerformanceState: [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = icsAttr1, course id = 0c35ee52-6488-4d6f-93c5-f62ed590a861, id = 42, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = AboveExpectation, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = AboveExpectation, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                     subconcepts = {
//                             [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr1, course id = c58a91e6-9b44-4d39-8936-fb4d08f0117d, id = 4, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = AtExpectation, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = AtExpectation, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {FireTeam Leader=AtExpectation, Automatic Rifleman=AtExpectation, Rifleman=AtExpectation, Grenadier=AtExpectation}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                             [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr2, course id = ded859e8-a61a-4d2d-9b43-d88b74f62676, id = 43, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                             [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr3, course id = 489944cf-0cca-46bf-bd2b-f2b8dd519131, id = 44, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]], }],
//                    [IntermediateConceptPerformanceState: [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = icsAttr2, course id = 88ab3e8b-cf07-4d32-bcf2-370fdfecd15c, id = 45, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                     subconcepts = {
//                             [IntermediateConceptPerformanceState: [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = icsAttr3, course id = c9aa8944-2ea6-4488-9b81-09a4ad625f49, id = 46, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = AtExpectation, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = AtExpectation, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                              subconcepts = {
//                                      [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr4, course id = f323cb20-48de-44ea-bef7-560557ecc629, id = 68, scenarioSupport = true, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = AtExpectation, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = AtExpectation, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {Rifleman (AI)=AtExpectation, Automatic Rifleman (AI)=AtExpectation, FireTeam Leader (AI)=AtExpectation, Grenadier (AI)=AtExpectation}, performanceAssessmentTime = 0, authoritativeResource = null]], }],
//                             [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr5, course id = 386f6afe-5e26-4a36-a2c9-7720ec9c6651, id = 47, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {FireTeam Leader=Unknown, Automatic Rifleman=Unknown, Rifleman=Unknown, Grenadier=Unknown}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                             [IntermediateConceptPerformanceState: [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = icsAttr4, course id = 9bdf0ba6-41b6-428d-bc1d-360b7a42fa1f, id = 48, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                              subconcepts = {
//                                      [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr6, course id = 9db76ff9-f381-4e41-91c9-00d2cfac712e, id = 2, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                                      [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr7, course id = e8b40f03-6b4a-46e4-a7d3-d3b2fee4dcbf, id = 11, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]], }],
//                             [IntermediateConceptPerformanceState: [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = icsAttr5, course id = 4f9c325a-5fc4-468d-bc13-495ed14de03d, id = 50, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]],
//                              subconcepts = {
//                                      [ConceptPerformanceState:  state = [PerformanceStateAttribute: name = csAttr8, course id = 9160c7dc-bb4d-4ec3-8425-8f7ceeee23fa, id = 51, scenarioSupport = false, priority = null, nodeState = UNACTIVATED, confidence = 1.0, competence = 1.0, trend = 1.0, short-term = Unknown, short-term-timestamp = 1646681456694, long-term = Unknown, long-term-timestamp = 1646681456694, predicted = Unknown, predicted-timestamp = 1646681456694, priorityHold = false, assessmentHold = false, confidenceHold = false, competenceHold = false, trendHold = false, evaluator = null, observerComment = null, observerMedia = null, assessedTeamOrgEntities = {}, performanceAssessmentTime = 0, authoritativeResource = null]], }], }], }]

        ConceptPerformanceState 
        csA1, csA2, csA3, 
        csB1, csB2, csB3,
        csA4, csA5, csA6, csA7, csA8,
        csB4, csB5, csB6, csB7, csB8;
        IntermediateConceptPerformanceState 
        icsA1, 
        icsA2, 
            icsA3, icsA4, icsA5,
        icsB1,
        icsB2, 
            icsB3, icsB4, icsB5;
        TaskPerformanceState 
        tsA, tsB;
        PerformanceStateAttribute 
        tsAttrA,
            icsAttrA1, 
                csAttrA1, csAttrA2, csAttrA3,
            icsAttrA2, 
                icsAttrA3,
                    csAttrA4,
                csAttrA5,
                icsAttrA4,
                    csAttrA6,
                    csAttrA7,
                icsAttrA5,
                    csAttrA8,
        tsAttrB,
            icsAttrB1, 
                csAttrB1, csAttrB2, csAttrB3,
            icsAttrB2,
                icsAttrB3,
                    csAttrB4,
                csAttrB5,
                icsAttrB4,
                    csAttrB6,
                    csAttrB7,
                icsAttrB5,
                    csAttrB8;
        
        // First Intermediate Concept and subconcepts - A
        
        icsAttrA1 = new PerformanceStateAttribute("icsAttr1", 42, "0c35ee52-6488-4d6f-93c5-f62ed590a861",
                AssessmentLevelEnum.ABOVE_EXPECTATION, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.ABOVE_EXPECTATION, 1646681456694L);
        List<ConceptPerformanceState> icsA1Subconcepts = new ArrayList<>();
        
        csAttrA1 = new PerformanceStateAttribute("csAttr1", 4, "c58a91e6-9b44-4d39-8936-fb4d08f0117d",
                AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L);
        Map<String, AssessmentLevelEnum> assessedTOEA1 = new HashMap<>();
        assessedTOEA1.put("FireTeam Leader", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEA1.put("Automatic Rifleman", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEA1.put("Rifleman", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEA1.put("Grenadier", AssessmentLevelEnum.AT_EXPECTATION);
        csAttrA1.setAssessedTeamOrgEntities(assessedTOEA1);
        csAttrA2 = new PerformanceStateAttribute("csAttr2", 43, "ded859e8-a61a-4d2d-9b43-d88b74f62676",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csAttrA3 = new PerformanceStateAttribute("csAttr3", 44, "489944cf-0cca-46bf-bd2b-f2b8dd519131",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        
        csA1 = new ConceptPerformanceState(csAttrA1);
        csA2 = new ConceptPerformanceState(csAttrA2);
        csA3 = new ConceptPerformanceState(csAttrA3);
        icsA1Subconcepts.add(csA1);
        icsA1Subconcepts.add(csA2);
        icsA1Subconcepts.add(csA3);
        icsA1 = new IntermediateConceptPerformanceState(icsAttrA1, icsA1Subconcepts);
        
        // second Intermediate Concept and subconcepts - A
        
        icsAttrA2 = new PerformanceStateAttribute("icsAttr2", 45, "88ab3e8b-cf07-4d32-bcf2-370fdfecd15c",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        List<ConceptPerformanceState> icsA2Subconcepts = new ArrayList<>();
        
        icsAttrA3 = new PerformanceStateAttribute("icsAttr3", 46, "c9aa8944-2ea6-4488-9b81-09a4ad625f49",
                AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L);
        List<ConceptPerformanceState> icsA3Subconcepts = new ArrayList<>();
        
        csAttrA4 = new PerformanceStateAttribute("csAttr4", 68, "f323cb20-48de-44ea-bef7-560557ecc629",
                AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L);
        csAttrA4.setScenarioSupportNode(true);
        Map<String, AssessmentLevelEnum> assessedTOEA4 = new HashMap<>();
        assessedTOEA4.put("Rifleman (AI)", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEA4.put("Automatic Rifleman (AI)", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEA4.put("FireTeam Leader (AI)", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEA4.put("Grenadier (AI)", AssessmentLevelEnum.AT_EXPECTATION);
        csAttrA4.setAssessedTeamOrgEntities(assessedTOEA4);
        csA4 = new ConceptPerformanceState(csAttrA4);
        icsA3Subconcepts.add(csA4);
        
        icsA3 = new IntermediateConceptPerformanceState(icsAttrA3, icsA3Subconcepts);
        
        csAttrA5 = new PerformanceStateAttribute("csAttr5", 47, "386f6afe-5e26-4a36-a2c9-7720ec9c6651",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        Map<String, AssessmentLevelEnum> assessedTOEA5 = new HashMap<>();
        assessedTOEA5.put("FireTeam Leader", AssessmentLevelEnum.UNKNOWN);
        assessedTOEA5.put("Automatic Rifleman", AssessmentLevelEnum.UNKNOWN);
        assessedTOEA5.put("Rifleman", AssessmentLevelEnum.UNKNOWN);
        assessedTOEA5.put("Grenadier", AssessmentLevelEnum.UNKNOWN);
        csAttrA5.setAssessedTeamOrgEntities(assessedTOEA5);
        csA5 = new ConceptPerformanceState(csAttrA5);
        
        icsAttrA4 = new PerformanceStateAttribute("icsAttr4", 48, "9bdf0ba6-41b6-428d-bc1d-360b7a42fa1f",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        List<ConceptPerformanceState> icsA4Subconcepts = new ArrayList<>();
        
        csAttrA6 = new PerformanceStateAttribute("csAttr6", 2, "9db76ff9-f381-4e41-91c9-00d2cfac712e",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csA6 = new ConceptPerformanceState(csAttrA6);
        
        csAttrA7 = new PerformanceStateAttribute("csAttr7", 11, "e8b40f03-6b4a-46e4-a7d3-d3b2fee4dcbf",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csA7 = new ConceptPerformanceState(csAttrA7);
        
        icsA4Subconcepts.add(csA6);
        icsA4Subconcepts.add(csA7);
        icsA4 = new IntermediateConceptPerformanceState(icsAttrA4, icsA4Subconcepts);
        
        icsAttrA5 = new PerformanceStateAttribute("icsAttr5", 50, "4f9c325a-5fc4-468d-bc13-495ed14de03d",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        List<ConceptPerformanceState> icsA5Subconcepts = new ArrayList<>();
        
        csAttrA8 = new PerformanceStateAttribute("csAttr8", 51, "9160c7dc-bb4d-4ec3-8425-8f7ceeee23fa",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csA8 = new ConceptPerformanceState(csAttrA8);
        icsA5Subconcepts.add(csA8);
        
        icsA5 = new IntermediateConceptPerformanceState(icsAttrA5, icsA5Subconcepts);
        
        icsA2Subconcepts.add(icsA3);
        icsA2Subconcepts.add(csA5);
        icsA2Subconcepts.add(icsA4);
        icsA2Subconcepts.add(icsA5);
        
        icsA2 = new IntermediateConceptPerformanceState(icsAttrA2, icsA2Subconcepts);
        
        // Task A
        
        tsAttrA = new PerformanceStateAttribute("tsAttr", 1, "62c9b3eb-8546-49ca-9a85-0d33d1641084",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        List<ConceptPerformanceState> tsAConcepts = new ArrayList<>();
        tsAConcepts.add(icsA1);
        tsAConcepts.add(icsA2);
        
        tsA = new TaskPerformanceState(tsAttrA, tsAConcepts);
        
        // First Intermediate Concept and subconcepts - B
        
        icsAttrB1 = new PerformanceStateAttribute("icsAttr1", 42, "0c35ee52-6488-4d6f-93c5-f62ed590a861",
                AssessmentLevelEnum.ABOVE_EXPECTATION, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.ABOVE_EXPECTATION, 1646681456694L);
        List<ConceptPerformanceState> icsB1Subconcepts = new ArrayList<>();
        
        csAttrB1 = new PerformanceStateAttribute("csAttr1", 4, "c58a91e6-9b44-4d39-8936-fb4d08f0117d",
                AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L);
        Map<String, AssessmentLevelEnum> assessedTOEB1 = new HashMap<>();
        assessedTOEB1.put("FireTeam Leader", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEB1.put("Automatic Rifleman", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEB1.put("Rifleman", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEB1.put("Grenadier", AssessmentLevelEnum.AT_EXPECTATION);
        csAttrB1.setAssessedTeamOrgEntities(assessedTOEB1);
        csAttrB2 = new PerformanceStateAttribute("csAttr2", 43, "ded859e8-a61a-4d2d-9b43-d88b74f62676",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csAttrB3 = new PerformanceStateAttribute("csAttr3", 44, "489944cf-0cca-46bf-bd2b-f2b8dd519131",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        
        csB1 = new ConceptPerformanceState(csAttrB1);
        csB2 = new ConceptPerformanceState(csAttrB2);
        csB3 = new ConceptPerformanceState(csAttrB3);
        icsB1Subconcepts.add(csB1);
        icsB1Subconcepts.add(csB2);
        icsB1Subconcepts.add(csB3);
        icsB1 = new IntermediateConceptPerformanceState(icsAttrB1, icsB1Subconcepts);
        
        // second Intermediate Concept and subconcepts - B
        
        icsAttrB2 = new PerformanceStateAttribute("icsAttr2", 45, "88ab3e8b-cf07-4d32-bcf2-370fdfecd15c",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        List<ConceptPerformanceState> icsB2Subconcepts = new ArrayList<>();
        
        icsAttrB3 = new PerformanceStateAttribute("icsAttr3", 46, "c9aa8944-2ea6-4488-9b81-09a4ad625f49",
                AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L);
        List<ConceptPerformanceState> icsB3Subconcepts = new ArrayList<>();
        
        csAttrB4 = new PerformanceStateAttribute("csAttr4", 68, "f323cb20-48de-44ea-bef7-560557ecc629",
                AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.AT_EXPECTATION, 1646681456694L);
        csAttrB4.setScenarioSupportNode(true);
        Map<String, AssessmentLevelEnum> assessedTOEB4 = new HashMap<>();
        assessedTOEB4.put("Rifleman (AI)", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEB4.put("Automatic Rifleman (AI)", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEB4.put("FireTeam Leader (AI)", AssessmentLevelEnum.AT_EXPECTATION);
        assessedTOEB4.put("Grenadier (AI)", AssessmentLevelEnum.AT_EXPECTATION);
        csAttrB4.setAssessedTeamOrgEntities(assessedTOEB4);
        csB4 = new ConceptPerformanceState(csAttrB4);
        icsB3Subconcepts.add(csB4);
        
        icsB3 = new IntermediateConceptPerformanceState(icsAttrB3, icsB3Subconcepts);
        
        csAttrB5 = new PerformanceStateAttribute("csAttr5", 47, "386f6afe-5e26-4a36-a2c9-7720ec9c6651",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        Map<String, AssessmentLevelEnum> assessedTOEB5 = new HashMap<>();
        assessedTOEB5.put("FireTeam Leader", AssessmentLevelEnum.UNKNOWN);
        assessedTOEB5.put("Automatic Rifleman", AssessmentLevelEnum.UNKNOWN);
        assessedTOEB5.put("Rifleman", AssessmentLevelEnum.UNKNOWN);
        assessedTOEB5.put("Grenadier", AssessmentLevelEnum.UNKNOWN);
        csAttrB5.setAssessedTeamOrgEntities(assessedTOEB5);
        csB5 = new ConceptPerformanceState(csAttrB5);
        
        icsAttrB4 = new PerformanceStateAttribute("icsAttr4", 48, "9bdf0ba6-41b6-428d-bc1d-360b7a42fa1f",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        List<ConceptPerformanceState> icsB4Subconcepts = new ArrayList<>();
        
        csAttrB6 = new PerformanceStateAttribute("csAttr6", 2, "9db76ff9-f381-4e41-91c9-00d2cfac712e",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csB6 = new ConceptPerformanceState(csAttrB6);
        
        csAttrB7 = new PerformanceStateAttribute("csAttr7", 11, "e8b40f03-6b4a-46e4-a7d3-d3b2fee4dcbf",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csB7 = new ConceptPerformanceState(csAttrB7);
        
        icsB4Subconcepts.add(csB6);
        icsB4Subconcepts.add(csB7);
        icsB4 = new IntermediateConceptPerformanceState(icsAttrB4, icsB4Subconcepts);
        
        icsAttrB5 = new PerformanceStateAttribute("icsAttr5", 50, "4f9c325a-5fc4-468d-bc13-495ed14de03d",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        List<ConceptPerformanceState> icsB5Subconcepts = new ArrayList<>();
        
        csAttrB8 = new PerformanceStateAttribute("csAttr8", 51, "9160c7dc-bb4d-4ec3-8425-8f7ceeee23fa",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        csB8 = new ConceptPerformanceState(csAttrB8);
        icsB5Subconcepts.add(csB8);
        
        icsB5 = new IntermediateConceptPerformanceState(icsAttrB5, icsB5Subconcepts);
        
        icsB2Subconcepts.add(icsB3);
        icsB2Subconcepts.add(csB5);
        icsB2Subconcepts.add(icsB4);
        icsB2Subconcepts.add(icsB5);
        
        icsB2 = new IntermediateConceptPerformanceState(icsAttrB2, icsB2Subconcepts);
        
        // Task B
        
        tsAttrB = new PerformanceStateAttribute("tsAttr", 1, "62c9b3eb-8546-49ca-9a85-0d33d1641084",
                AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L, AssessmentLevelEnum.UNKNOWN, 1646681456694L);
        List<ConceptPerformanceState> tsBConcepts = new ArrayList<>();
        tsBConcepts.add(icsB1);
        tsBConcepts.add(icsB2);
        
        tsB = new TaskPerformanceState(tsAttrB, tsBConcepts);
        
        // comparison
        // this values should both be true and therefore equal to each other
        boolean objectDeepEquals = Objects.deepEquals(tsA, tsB);
        boolean giftEquals = tsA.equals(tsB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals+" prior to setting top level properties", objectDeepEquals && giftEquals);
        
        tsA.setDifficultyReason("difficulty reason");
        tsB.setDifficultyReason("difficulty reason");
        objectDeepEquals = Objects.deepEquals(tsA, tsB);
        giftEquals = tsA.equals(tsB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals+" after setting difficulty reason", objectDeepEquals && giftEquals);
        
        tsA.setStressReason("stress reason");
        tsB.setStressReason("stress reason");
        objectDeepEquals = Objects.deepEquals(tsA, tsB);
        giftEquals = tsA.equals(tsB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals+" after setting stress reason", objectDeepEquals && giftEquals);
        
        // FIXME: tests fail after setting stress 
        tsA.setStress(1.0);
        tsB.setStress(1.0);
        objectDeepEquals = Objects.deepEquals(tsA, tsB);
        giftEquals = tsA.equals(tsB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals+" after setting stress", objectDeepEquals && giftEquals);
        
        // FIXME: tests fail after setting difficulty
        tsA.setDifficulty(1.0);
        tsB.setDifficulty(1.0);
        objectDeepEquals = Objects.deepEquals(tsA, tsB);
        giftEquals = tsA.equals(tsB);
        Assert.assertTrue("BOTH SHOULD BE TRUE -> Object.deepEquals result was "+objectDeepEquals+" and GIFT equals was "+giftEquals+" after setting difficulty", objectDeepEquals && giftEquals);
        
        System.out.println("Finished equalsTest 3");
    }
                           
}
