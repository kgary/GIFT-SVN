/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test class for AbstractQuestionResponse
 *
 * @author jleonard
 */
public class AbstractQuestionResponseTest {

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

    /**
     * Test of createResponse method, of class QuestionResponse.
     */
    @Test
    public void testCreateResponse() {
        System.out.println("running test: testCreateResponse");

        List<QuestionResponseElement> responses = new ArrayList<>();

        AbstractQuestionResponse frResult = AbstractQuestionResponse.createResponse(new FillInTheBlankSurveyQuestion(), responses);
        assertEquals(FreeResponseQuestionResponse.class, frResult.getClass());

        frResult = AbstractQuestionResponse.createResponse(new FillInTheBlankSurveyQuestion(), 0, responses);
        assertEquals(FreeResponseQuestionResponse.class, frResult.getClass());

        AbstractQuestionResponse mcResult = AbstractQuestionResponse.createResponse(new MultipleChoiceSurveyQuestion(), responses);
        assertEquals(MultipleChoiceQuestionResponse.class, mcResult.getClass());

        mcResult = AbstractQuestionResponse.createResponse(new MultipleChoiceSurveyQuestion(), 0, responses);
        assertEquals(MultipleChoiceQuestionResponse.class, mcResult.getClass());

        AbstractQuestionResponse rsResult = AbstractQuestionResponse.createResponse(new RatingScaleSurveyQuestion(), responses);
        assertEquals(RatingScaleQuestionResponse.class, rsResult.getClass());

        rsResult = AbstractQuestionResponse.createResponse(new RatingScaleSurveyQuestion(), 0, responses);
        assertEquals(RatingScaleQuestionResponse.class, rsResult.getClass());

        AbstractQuestionResponse mocResult = AbstractQuestionResponse.createResponse(new MatrixOfChoicesSurveyQuestion(), responses);
        assertEquals(MatrixOfChoicesQuestionResponse.class, mocResult.getClass());

        mocResult = AbstractQuestionResponse.createResponse(new MatrixOfChoicesSurveyQuestion(), 0, responses);
        assertEquals(MatrixOfChoicesQuestionResponse.class, mocResult.getClass());

        AbstractQuestionResponse sResult = AbstractQuestionResponse.createResponse(new SliderSurveyQuestion(), responses);
        assertEquals(SliderQuestionResponse.class, sResult.getClass());

        sResult = AbstractQuestionResponse.createResponse(new SliderSurveyQuestion(), 0, responses);
        assertEquals(SliderQuestionResponse.class, sResult.getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateResponse_throwException() {
        System.out.println("running test: testCreateResponse_throwException");

        List<QuestionResponseElement> responses = new ArrayList<>();

        @SuppressWarnings("unused")
        AbstractQuestionResponse result = AbstractQuestionResponse.createResponse(new NewSurveyQuestionType(), responses);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateResponse_throwNullPointerException() {
        System.out.println("running test: testCreateResponse_2arg_throwNullPointerException");

        List<QuestionResponseElement> responses = new ArrayList<>();

        @SuppressWarnings("unused")
        AbstractQuestionResponse result = AbstractQuestionResponse.createResponse(null, responses);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateResponse_3arg_throwException() {
        System.out.println("running test: testCreateResponse_3arg_throwException");

        List<QuestionResponseElement> responses = new ArrayList<>();

        @SuppressWarnings("unused")
        AbstractQuestionResponse result = AbstractQuestionResponse.createResponse(new NewSurveyQuestionType(), 0, responses);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateResponse_3arg_throwNullPointerException() {
        System.out.println("running test: testCreateResponse_3arg_throwNullPointerException");

        List<QuestionResponseElement> responses = new ArrayList<>();

        @SuppressWarnings("unused")
        AbstractQuestionResponse result = AbstractQuestionResponse.createResponse(null, 0, responses);
    }

    @Ignore
    private static class NewQuestionType extends AbstractQuestion {

        private static final long serialVersionUID = 1L;

        @Override
        public double getHighestPossibleScore() {
            return 0;
        }

    }

    @Ignore
    private static class NewSurveyQuestionType extends AbstractSurveyQuestion<NewQuestionType> {

        private static final long serialVersionUID = 1L;

        public NewSurveyQuestionType() {
            super();
        }

        @Override
        public double getHighestPossibleScore() {
            return 0;
        }
    }
}
