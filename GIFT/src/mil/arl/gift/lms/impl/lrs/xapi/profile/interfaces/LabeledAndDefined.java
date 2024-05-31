package mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces;

import com.rusticisoftware.tincan.LanguageMap;
import mil.arl.gift.lms.impl.lrs.LanguageTagEnum;

/**
 * Helper methods for xAPI Profile Components which have a prefLabel and Definition.
 * 
 * @author Yet Analytics
 *
 */
public interface LabeledAndDefined {
    
    public LanguageMap getPrefLabel();
    public String getEnglishPrefLabel();
    public void setPrefLabel(LanguageTagEnum languageTag, String label);
    public void setEnglishPrefLabel(String label);
    public LanguageMap getDefinition();
    public String getEnglishDefinition();
    public void setDefinition(LanguageTagEnum languageTag, String definition);
    public void setEnglishDefinition(String definition);
}
