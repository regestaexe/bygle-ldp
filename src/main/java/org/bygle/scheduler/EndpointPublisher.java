package org.bygle.scheduler;

import org.bygle.endpoint.managing.EndPointManagerInterface;
import org.bygle.endpoint.managing.EndPointManagerProvider;
import org.springframework.stereotype.Component;

@Component("endpointPublisher")
public class EndpointPublisher {
	EndPointManagerProvider endPointManagerProvider;
	private boolean isRunnig = false;
	public EndpointPublisher() {
	}
	public void executePublishing() {
        try {
        	System.out.println("executePublishing");
        	if(isRunnig==false){
	    		isRunnig = true;
				EndPointManagerInterface endPointManager = endPointManagerProvider.getEndPointManager();
				endPointManager.executePublishing();
				isRunnig = false;
        	}
		} catch (Exception e) {
			e.printStackTrace();
			isRunnig = false;
		}
    }
	public void setEndPointManagerProvider(EndPointManagerProvider endPointManagerProvider) {
		this.endPointManagerProvider = endPointManagerProvider;
	}
}
