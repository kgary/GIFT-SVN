/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.rpcs.DoubleResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget used to provide a summary of an experiment course export
 * 
 * @author nroberts
 */
public class ExperimentRawDataExportSummaryWidget extends Composite {

	private static BsExportSummaryWidgetUiBinder uiBinder = GWT
			.create(BsExportSummaryWidgetUiBinder.class);

	interface BsExportSummaryWidgetUiBinder extends
			UiBinder<Widget, ExperimentRawDataExportSummaryWidget> {
	}
	
	@UiField
	protected InlineLabel estimatedSizeLabel;
	
	@UiField
	protected BsLoadingIcon ctrlLoadIcon;
	
	@UiField
	protected HasText experimentName;
	
	/**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);

	public ExperimentRawDataExportSummaryWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	/**
	 * Sets the experiment from which the raw data should be exported. This is used to calculate the export size.
	 * 
	 * @param experiment
	 */
	public void setExperiment(DataCollectionItem experiment){
		
		if(experiment != null && experiment.getId() != null && experiment.getCourseFolder() != null){
			
			if(experiment.getName() != null){
				experimentName.setText(experiment.getName());
				
			} else {
				experimentName.setText("this experiment");
			}
			
			estimatedSizeLabel.setText("Calculating...");
			ctrlLoadIcon.startLoading();
			
			dashboardService.calculateExperimentRawDataExportSize(UiManager.getInstance().getUserName(), experiment.getId(), new AsyncCallback<DoubleResponse>() {
	
				@Override
				public void onFailure(Throwable e) {
					
					estimatedSizeLabel.setText("An error occurred while calculating the download size.");
					
					ctrlLoadIcon.stopLoading();
				}
	
				@Override
				public void onSuccess(DoubleResponse response) {
					
				    int value = (int) response.getValue();
					String valueString;
					if(value == 0){
					    valueString = "< 1";
					}else{
					    valueString = Integer.toString(value);
					}
					
					estimatedSizeLabel.setText(valueString + " MB");
					
					ctrlLoadIcon.stopLoading();
					
				}
			});
		}
	}
}
