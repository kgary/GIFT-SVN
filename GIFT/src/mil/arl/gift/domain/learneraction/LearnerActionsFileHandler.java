/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.learneraction;

import java.io.FileNotFoundException;
import java.io.Serializable;

import mil.arl.gift.common.course.dkf.LearnerActionsFileValidationException;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.UnmarshalledFile;

/**
 * Handles parsing a file containing the list of learner actions for a domain
 *
 * @author jleonard
 */
public class LearnerActionsFileHandler extends AbstractSchemaHandler {
    
    private FileProxy learnerActionFile;

    /** the generated class instance for learner actions */
    private generated.dkf.LearnerActionsList learnerActions = null;

    /**
     * Constructor
     *
     * @param learnerActionFile The file containing the learner action definitions
     * @throws FileNotFoundException if the file was not found
     * @throws FileValidationException if there was a problem validating the file
     */
    public LearnerActionsFileHandler(FileProxy learnerActionFile) throws FileNotFoundException, FileValidationException {
        super(LEARNER_ACTIONS_FILE);
               
        this.learnerActionFile = learnerActionFile;
        if(!learnerActionFile.exists()){
            throw new FileNotFoundException("Unable to find the file named "+learnerActionFile.getFileId());
        }
        
        parse();
    }

    /**
     * Gets the learner actions from the file, available after it is parsed
     *
     * @return Learner Actions The learner actions from the file, available
     * after it is parsed
     */
    public generated.dkf.LearnerActionsList getLearnerActions() {
        return learnerActions;
    }

    /**
     * Parse the learner actions file and get the list of learner actions.
     */
    private void parse() throws FileValidationException {
        
        try {
            UnmarshalledFile uFile = parseAndValidate(generated.dkf.LearnerActionsList.class, learnerActionFile.getInputStream(), true);
            Serializable learnerActionsObj = uFile.getUnmarshalled();
            setLearnerActions((generated.dkf.LearnerActionsList) learnerActionsObj);
            
        } catch (Exception e) {
            
            //found a null exception message which is not allowed for the details parameter of LearnerActionsFileValidationException
            String details = e.getMessage() != null ? e.getMessage() : "An exception was thrown.";
            throw new LearnerActionsFileValidationException("Failed to parse and validate the learner actions file.",
                    details, 
                    learnerActionFile.getFileId(),
                    e);
        }

    }
    
    /**
     * Set the generated class instance of learner actions
     * 
     * @param learnerActions dkf learner actions list information
     */
    private void setLearnerActions(generated.dkf.LearnerActionsList learnerActions){        
        this.learnerActions = learnerActions;
    }

}
