package org.bygle.db.listener;



import java.io.Serializable;

import org.bygle.bean.Records;
import org.bygle.endpoint.managing.EndPointManagerInterface;
import org.bygle.endpoint.managing.EndPointManagerProvider;
import org.bygle.utils.BygleSystemUtils;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DBEventUpdateListener implements Serializable,PostUpdateEventListener{
	private static final long serialVersionUID = 8628132333422887069L;
	@Autowired
	EndPointManagerProvider endPointManagerProvider;
	
	public void initialize(Configuration configuration) {
		
	}
	@Override
	public void onPostUpdate(PostUpdateEvent arg0) {
		if (arg0.getEntity() instanceof Records) {
			Records records = (Records) arg0.getEntity();
			try {
				if(records.getRecordTypes().getIdRecordType()!=BygleSystemUtils.RESOURCE_TYPE_BINARY){
					EndPointManagerInterface endPointManager = endPointManagerProvider.getEndPointManager();
					endPointManager.rePublishRecord(records.getRdf(),records.getRdfAbout());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
