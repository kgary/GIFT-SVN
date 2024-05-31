package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Assessment Activity from Intermediate Concept Performance State and adds the Activity
 * to xAPI Statement as Other Context Activity.
 * 
 * @author Yet Analytics
 *
 */
public class IntermediateConceptAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Intermediate Concept Appender";
    /** appender description */
    private static final String appenderInfo = "Creates Assessment xAPI Activity from Intermediate Concept Performance State and attaches to xAPI Statement as Other Context Activity";
    /** Activity Type from xAPI Profile */
    private ItsActivityTypeConcepts.AssessmentNode.ConceptIntermediate intermediateConceptATC;
    /** Intermediate Concept Performance State to convert */
    private IntermediateConceptPerformanceState intermediateConcept;
    /**
     * Parses Activity Type from xAPI Profile
     * 
     * @param state - Intermediate Concept Performance State to convert
     * 
     * @throws LmsXapiProfileException when unable to parse Activity Type from xAPI Profile
     */
    public IntermediateConceptAppender(IntermediateConceptPerformanceState state) throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        if(state == null) {
            throw new IllegalArgumentException("Intermediate Concept Performance State can not be null!");
        }
        this.intermediateConcept = state;
        this.intermediateConceptATC = ItsActivityTypeConcepts.AssessmentNode.ConceptIntermediate.getInstance();
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        try {
            statement.addOtherActivity(intermediateConceptATC.asActivity(intermediateConcept));
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiAppenderException("Unable to append intermediate concept!", e);
        }
        return statement;
    }
}
