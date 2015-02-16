package org.bygle.endpoint.managing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("endPointManagerProvider")
public class EndPointManagerProvider {
	@Autowired
	@Qualifier("endPointManager")
	private  EndPointManager endPointManager ;

	public EndPointManager getEndPointManager() {
		return endPointManager;
	}
	public void setEndPointManager(EndPointManager endPointManager) {
		this.endPointManager = endPointManager;
	}
}
