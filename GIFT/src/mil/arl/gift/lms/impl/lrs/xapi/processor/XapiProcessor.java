package mil.arl.gift.lms.impl.lrs.xapi.processor;

import java.util.List;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiProcessorException;

/**
 * Defines method implemented by Classes which extend AbstractProcessor. This method serves as the 
 * entry point for the processor in order to process data into xAPI Statement(s).
 * 
 * @author Yet Analytics
 *
 */
public interface XapiProcessor {
    /**
     * Process data into xAPI Statements which get added to the passed in statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * 
     * @throws LmsXapiProcessorException when unable to process data into xAPI Statement
     */
    public void process(List<Statement> statements) throws LmsXapiProcessorException;
}
