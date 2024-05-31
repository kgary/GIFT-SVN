/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.List;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.rpcs.DoubleResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author nroberts
 *
 */
public class BsExportSummaryWidget extends Composite {

	private static BsExportSummaryWidgetUiBinder uiBinder = GWT
			.create(BsExportSummaryWidgetUiBinder.class);

	interface BsExportSummaryWidgetUiBinder extends
			UiBinder<Widget, BsExportSummaryWidget> {
	}
	
	@UiField
	protected HTML courseNamesList;
	
	@UiField
	protected InlineLabel estimatedSizeLabel;
	
	@UiField
	protected BsLoadingIcon ctrlLoadIcon;
	
	/**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);

	public BsExportSummaryWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void buildCoursesToExportSummary(List<DomainOption> courses){
		
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		sb.appendHtmlConstant("<ul>");
		
		for(DomainOption course : courses){
			
			if(course.getDomainName() != null){
				sb.appendHtmlConstant("<li>");
				sb.appendEscaped(course.getDomainName());
				sb.appendHtmlConstant("</li>");
			}
		}
		
		sb.appendHtmlConstant("</ul>");
		
		courseNamesList.setHTML(sb.toSafeHtml());
		
		estimatedSizeLabel.setText("Calculating...");
		ctrlLoadIcon.startLoading();
		
		dashboardService.calculateExportSize(UiManager.getInstance().getUserName(), courses, new AsyncCallback<DoubleResponse>() {

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
