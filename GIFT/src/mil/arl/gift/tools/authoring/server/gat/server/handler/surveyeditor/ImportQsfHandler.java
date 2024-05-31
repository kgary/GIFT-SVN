/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.surveyeditor;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.server.survey.QualtricsImport;
import mil.arl.gift.tools.authoring.server.gat.shared.action.surveyeditor.ImportQsf;
import mil.arl.gift.tools.authoring.server.gat.shared.action.surveyeditor.QualtricsImportResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Action handler to import a Qualtrics survey
 * @author bzahid
 */
public class ImportQsfHandler implements ActionHandler<ImportQsf, QualtricsImportResult> {

	@Override
	public QualtricsImportResult execute(ImportQsf action, ExecutionContext context) throws DispatchException {
		
		QualtricsImportResult result = new QualtricsImportResult();
		
		try { 
			result = QualtricsImport.importQsf(action.getQsfContent());
			result.setSuccess(true);
			
		} catch (Exception e) {
			
			result.setErrorMsg("An error occurred while reading the .qsf file.");
			result.setErrorDetails("The import qsf action could not be completed. " + e.getMessage());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
			result.setSuccess(false);
		}
		
		return result;
	}

	@Override
	public Class<ImportQsf> getActionType() {
		return ImportQsf.class;
	}

	@Override
	public void rollback(ImportQsf action, QualtricsImportResult result, ExecutionContext context) throws DispatchException {
		// Nothing to rollback
	}

}
