package mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;

/**
 * xAPI Statement Template defined in ITS xAPI Profile corresponding to bookmark xAPI Statement.
 * 
 * @author Yet Analytics
 *
 */
public class BookmarkedTemplate extends StatementTemplate {
    // Singleton
    private static BookmarkedTemplate instance = null;
    // Constructors
    protected BookmarkedTemplate(String id) throws LmsXapiProfileException {
        super(id, giftStatementTemplateSparqlQuery(id, true));
    }
    private BookmarkedTemplate() throws LmsXapiProfileException {
        this("https://xapinet.org/xapi/stetmt/its/StatementTemplate#bookmarked");
    }
    // Access
    public static BookmarkedTemplate getInstance() throws LmsXapiProfileException {
        if(instance == null) {
            instance = new BookmarkedTemplate();
        }
        return instance;
    }
}
