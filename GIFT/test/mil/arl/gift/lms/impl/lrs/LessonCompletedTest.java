package mil.arl.gift.lms.impl.lrs;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.lms.impl.lrs.xapi.generate.LessonCompletedGenerator;

public class LessonCompletedTest extends TestUtils {

    private LessonCompletedGenerator genLegacy, genError, genLessonRule, genLearnerEndedObject, genLearnerEndedCourse, 
    genControllerEndedLesson, genInstructionalStratReq;
    private LessonCompleted legacyNotSpecified, error, lessonRule, learnerEndedObject, learnerEndedCourse, 
    controllerEndedLesson, instructionalStrategyRequest;
    private Statement statement;
    
    @Before
    public void setup() throws Exception {
        legacyNotSpecified = new LessonCompleted(LessonCompletedStatusType.LEGACY_NOT_SPECIFIED);
        genLegacy = new LessonCompletedGenerator(teamKnowledgeSession, studentDomainSessionId, studentUsername, legacyNotSpecified);
        
        error = new LessonCompleted(LessonCompletedStatusType.ERROR);
        genError = new LessonCompletedGenerator(teamKnowledgeSession, studentDomainSessionId, studentUsername, error);
        
        lessonRule = new LessonCompleted(LessonCompletedStatusType.LESSON_RULE);
        genLessonRule = new LessonCompletedGenerator(teamKnowledgeSession, studentDomainSessionId, studentUsername, lessonRule);
        
        learnerEndedObject = new LessonCompleted(LessonCompletedStatusType.LEARNER_ENDED_OBJECT);
        genLearnerEndedObject = new LessonCompletedGenerator(teamKnowledgeSession, studentDomainSessionId, studentUsername, learnerEndedObject);
        
        learnerEndedCourse = new LessonCompleted(LessonCompletedStatusType.LEARNER_ENDED_COURSE);
        genLearnerEndedCourse = new LessonCompletedGenerator(teamKnowledgeSession, studentDomainSessionId, studentUsername, learnerEndedCourse);
        
        controllerEndedLesson = new LessonCompleted(LessonCompletedStatusType.CONTROLLER_ENDED_LESSON);
        genControllerEndedLesson = new LessonCompletedGenerator(teamKnowledgeSession, studentDomainSessionId, studentUsername, controllerEndedLesson);
        
        instructionalStrategyRequest = new LessonCompleted(LessonCompletedStatusType.INSTRUCTIONAL_STRATEGY_REQUEST);
        genInstructionalStratReq = new LessonCompletedGenerator(teamKnowledgeSession, studentDomainSessionId, studentUsername, instructionalStrategyRequest);
    }
    
    @Test
    public void testLessonCompletedGen() throws Exception {
        statement = genLegacy.generateStatement();
        Assert.assertEquals(statement.getResult().getResponse(), 
                "Unknown completion status: this message is the default for legacy instances that don't have this field (pre July 2021)");
        statement = genError.generateStatement();
        Assert.assertEquals(statement.getResult().getResponse(), 
                "An error occurred in GIFT and the lesson can no longer continue");
        statement = genLessonRule.generateStatement();
        Assert.assertEquals(statement.getResult().getResponse(),
                "A rule in the lesson (DKF) caused the lesson to end gracefully");
        statement = genLearnerEndedObject.generateStatement();
        Assert.assertEquals(statement.getResult().getResponse(), 
                "The learner ended the course prematurely");
        statement = genLearnerEndedCourse.generateStatement();
        Assert.assertEquals(statement.getResult().getResponse(),
                "The learner ended the course object");
        statement = genControllerEndedLesson.generateStatement();
        Assert.assertEquals(statement.getResult().getResponse(), 
                "Some external controller outside of the modules ended the lesson (e.g. game master, RTA application)");
        statement = genInstructionalStratReq.generateStatement();
        Assert.assertEquals(statement.getResult().getResponse(),
                "some instructional design ended the course prematurely, e.g. too many failed attempts, need to start lesson over");
    }
    
}
