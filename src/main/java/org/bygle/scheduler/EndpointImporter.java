package org.bygle.scheduler;

import org.bygle.endpoint.managing.EndPointManagerInterface;
import org.bygle.endpoint.managing.EndPointManagerProvider;
import org.springframework.stereotype.Component;

@Component("endpointImporter")
public class EndpointImporter {
	EndPointManagerProvider endPointManagerProvider;
	private boolean isRunnig = false;
	public EndpointImporter() {
	}
	public void executeImport() {
        try {
        	System.out.println("executeImport");
        	if(isRunnig==false){
        		isRunnig = true;
				EndPointManagerInterface endPointManager = endPointManagerProvider.getEndPointManager();
				endPointManager.executeImport();
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
