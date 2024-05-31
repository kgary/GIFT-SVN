/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets;

import java.io.Serializable;

/**
 * Handler that is called when a data source (Sensor or Training App) is
 * modified in the DataSourceDataGrid.
 * @author elafave
 *
 */
public interface DataSourceModifiedHandler {

	public void onDataSourceModified(Serializable dataSource);
	
}
