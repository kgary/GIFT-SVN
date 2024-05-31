/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner;

/**
 * Each enum represents a "type" of Translator that can be used in a
 * LearnerConfiguration. This is particularly useful for end users because it
 * allows them to identify a Translator using the enum's displayName rather than
 * the TranslatorImpl that it maps to.
 * @author elafave
 *
 */
public enum TranslatorTypeEnum {

	DEFAULT("Default Translator", "learner.clusterer.data.DefaultTranslator"),
	MOUSE("Mouse Translator", "learner.clusterer.data.MouseTranslator"),
	SELF_ASSESSMENT("Self Assessment Translator", "learner.clusterer.data.SelfAssessmentTranslator"),
	SINE_WAVE("Sine Wave Translator", "learner.clusterer.data.SineWaveTranslator");
	
	private final String displayName;
	
	private final String translatorImpl;

    private TranslatorTypeEnum(String displayName, String translatorImpl){
        this.displayName = displayName;
        this.translatorImpl = translatorImpl;
    }
    
    /**
     * 
     * @return A human-friendly string that identifies the TranslatorTypeEnum.
     */
    public String getDisplayName() {
    	return displayName;
    }
    
    /**
     * 
     * @return The TranslatorImpl used by the software in LearnerConfiguration.
     */
    public String getTranslatorImpl() {
    	return translatorImpl;
    }
    
    /**
     * Performs a reverse look up based on the supplied translatorImpl to find
     * the TranslatorTypeEnum with that implementation.
     * @param translatorImpl Translator Implementation of the
     * TranslatorTypeEnum to be returned.
     * @return TranslatorTypeEnum with the supplied translatorImpl or NULL if
     * one couldn't be found.
     */
    static public TranslatorTypeEnum fromTranslatorImpl(String translatorImpl) {
    	TranslatorTypeEnum[] values = TranslatorTypeEnum.values();
    	for(TranslatorTypeEnum translatorTypeEnum : values) {
    		if(translatorTypeEnum.getTranslatorImpl().equals(translatorImpl)) {
    			return translatorTypeEnum;
    		}
    	}
    	return null;
    }
}
