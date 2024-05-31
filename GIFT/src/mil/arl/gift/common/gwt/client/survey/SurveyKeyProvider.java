/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.Category;
import mil.arl.gift.common.survey.Folder;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;

import com.google.gwt.view.client.ProvidesKey;

/**
 *
 * @author jleonard
 */
public class SurveyKeyProvider {

    /**
     * The GWT key provider for a question
     */
    public static final ProvidesKey<AbstractQuestion> QUESTION_KEY_PROVIDER = new ProvidesKey<AbstractQuestion>() {
        @Override
        public Object getKey(AbstractQuestion question) {

            return question == null ? null : question.getQuestionId();
        }
    };

    /**
     * Provides the key for identifying a survey
     */
    public static final ProvidesKey<Survey> SURVEY_KEY_PROVIDER = new ProvidesKey<Survey>() {
        @Override
        public Object getKey(Survey survey) {

            return survey == null ? null : survey.getId();
        }
    };

    public static final ProvidesKey<OptionList> OPTION_LIST_KEY_PROVIDER = new ProvidesKey<OptionList>() {
        @Override
        public Object getKey(OptionList optionList) {

            return optionList == null ? null : optionList.getId();
        }
    };

    public static final ProvidesKey<SurveyContext> SURVEY_CONTEXT_KEY_PROVIDER = new ProvidesKey<SurveyContext>() {
        @Override
        public Object getKey(SurveyContext survey) {
            
            return survey == null ? null : survey.getId();
        }
    };

    public static final ProvidesKey<Category> CATEGORY_KEY_PROVIDER = new ProvidesKey<Category>() {
        @Override
        public Object getKey(Category category) {
            
            return category == null ? null : category.getId();
        }
    };

    public static final ProvidesKey<Folder> FOLDER_KEY_PROVIDER = new ProvidesKey<Folder>() {
        @Override
        public Object getKey(Folder folder) {
            
            return folder == null ? null : folder.getId();
        }
    };

    private SurveyKeyProvider() {
    }
}
