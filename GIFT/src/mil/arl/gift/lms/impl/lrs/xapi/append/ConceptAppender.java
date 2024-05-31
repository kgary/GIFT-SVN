package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Assessment Activity from Concept Performance State and adds the Activity
 * to xAPI Statement as Other Context Activity. 
 * 
 * @author Yet Analytics
 *
 */
public class ConceptAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Concept Performance State -> Assessment xAPI Activity Appender";
    /** appender description */
    private static final String appenderInfo = "Attaches Assessment xAPI Activity to xAPI Statement as Other Context Activity";
    /** xAPI Activity Type for Concept Assessment Node used as Activity Type within Assessment Activity */
    private ItsActivityTypeConcepts.AssessmentNode.Concept conceptATC;
    /** Concept Performance State to convert */
    private ConceptPerformanceState concept;
    /**
     * Parses Concept Assessment Node Activity Type from xAPI Profile
     * 
     * @param state - Concept Performance State to convert
     * 
     * @throws LmsXapiProfileException when unable to parse Activity Type from xAPI Profile
     */
    public ConceptAppender(ConceptPerformanceState state) throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        if(state == null) {
            throw new IllegalArgumentException("Concept Performance State can not be null!");
        }
        this.concept = state;
        this.conceptATC = ItsActivityTypeConcepts.AssessmentNode.Concept.getInstance();
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        try {
            statement.addOtherActivity(conceptATC.asActivity(concept));
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiAppenderException("Unable to append assessment concept activity!", e);
        }
        return statement;
    }
}
