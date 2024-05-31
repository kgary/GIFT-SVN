package mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;

/**
 * xAPI Statement Template defined in ITS xAPI Profile corresponding
 * to Lesson Completed xAPI Statement.
 * 
 * @author Yet Analytics
 *
 */
public class LessonCompletedTemplate extends StatementTemplate {
    // Singleton
    private static LessonCompletedTemplate instance = null;
    // Constructors
    protected LessonCompletedTemplate(String id) throws LmsXapiProfileException {
        super(id, giftStatementTemplateSparqlQuery(id, true));
    }
    private LessonCompletedTemplate() throws LmsXapiProfileException {
        this("https://xapinet.org/xapi/stetmt/its/StatementTemplate#completed.lesson");
    }
    // Access
    public static LessonCompletedTemplate getInstance() throws LmsXapiProfileException {
        if(instance == null) {
            instance = new LessonCompletedTemplate();
        }
        return instance;
    }
}
