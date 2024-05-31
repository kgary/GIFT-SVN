package mil.arl.gift.lms.impl.lrs.xapi.profile;

//import java.util.List;
//import com.rusticisoftware.tincan.LanguageMap;
import mil.arl.gift.lms.impl.lrs.LrsEnum;

public class StatementTemplateRule {

// TODO: proper usage requires jsonPath utilities
//    private String location;
//    private String selector;
//    private PresenceEnum presence;
//    private List<Object> any;
//    private List<Object> all;
//    private List<Object> none;
//    private LanguageMap scopeNote;

    enum PresenceEnum implements LrsEnum {
        INCLUDED("included"),
        EXCLUDED("excluded"),
        RECOMMENDED("recommended");
        private String value;
        PresenceEnum(String s) {
            this.value = s;
        }
        @Override
        public String getValue() {
            return value;
        }
    }
    
    public StatementTemplateRule() {}
}
