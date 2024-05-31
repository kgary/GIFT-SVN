/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import java.util.ArrayList;
import java.util.List;

public class InitTrainingAppAction extends AbstractAction {

	private List<String> urls;
	
	private InitTrainingAppAction() {
		super(ActionTypeEnum.INIT_APP);
	}
	
	public InitTrainingAppAction(List<String> urls) {
		this();
		
		if(urls == null || urls.isEmpty()) {
			throw new IllegalArgumentException("The parameter urls can not be null or empty");
		}
		
		this.urls = new ArrayList<String>(urls);
	}
	
	public List<String> getUrls() {
		return urls;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[InitTrainingAppAction: urls=");
        builder.append(urls);
        builder.append(", ").append(super.toString());
        builder.append("]");
        return builder.toString();
    }	
	
}
