/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.score;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

/**
 * A jUnit test for the GradedScoreNode
 *
 * @author jleonard
 */
public class GradedScoreNodeTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testScoreNode() {

        GradedScoreNode rootScore = new GradedScoreNode("Lesson");

        GradedScoreNode perimeter = new GradedScoreNode("PerimeterSweep");
        GradedScoreNode xyz = new GradedScoreNode("Task 2");
        
        rootScore.setPerformanceNodeId(1);
        perimeter.setPerformanceNodeId(2);
        xyz.setPerformanceNodeId(3);

        rootScore.addChild(perimeter);
        rootScore.addChild(xyz);

        GradedScoreNode corridor = new GradedScoreNode("Stay within Corridor");
        corridor.setPerformanceNodeId(4);

        perimeter.addChild(corridor);

        DefaultRawScore corridorCountScore = new DefaultRawScore("2", "violations");
        Set<String> userGroupA = new HashSet<>();
        userGroupA.add("player A");
        RawScoreNode corridorCount = new RawScoreNode("Violation Count", corridorCountScore, AssessmentLevelEnum.ABOVE_EXPECTATION, userGroupA);
        corridor.addChild(corridorCount);

        DefaultRawScore corridorTimeScore = new DefaultRawScore(10.2 + "", "seconds");
        Set<String> userGroupB = new HashSet<>();
        userGroupB.add("player B");
        RawScoreNode corridorTime = new RawScoreNode("Violation Time", corridorTimeScore, AssessmentLevelEnum.BELOW_EXPECTATION, userGroupB);
        corridor.addChild(corridorTime);
        
        ScoreUtil.performAssessmentRollup(rootScore, true);
        AssessmentLevelEnum grade = rootScore.getAssessment();
        Assert.assertTrue("The root score grade must be "+AssessmentLevelEnum.BELOW_EXPECTATION, grade == AssessmentLevelEnum.BELOW_EXPECTATION);
        
        Assert.assertTrue("The root score grade node must have 2 children", rootScore.getChildren().size() == 2);
        
    }
}
