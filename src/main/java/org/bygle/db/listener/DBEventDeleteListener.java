package org.bygle.db.listener;



import java.io.Serializable;

import org.bygle.bean.Records;
import org.bygle.endpoint.managing.EndPointManagerInterface;
import org.bygle.endpoint.managing.EndPointManagerProvider;
import org.bygle.utils.BygleSystemUtils;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DBEventDeleteListener implements Serializable,PostDeleteEventListener{
	private static final long serialVersionUID = -4638389810690052022L;
	@Autowired
	EndPointManagerProvider endPointManagerProvider;
	@Override
	public void onPostDelete(PostDeleteEvent arg0) {
		if(arg0.getEntity() instanceof Records){
			Records records = (Records)arg0.getEntity();
			try {
				if(records.getRecordTypes().getIdRecordType()!=BygleSystemUtils.RESOURCE_TYPE_BINARY){
					EndPointManagerInterface endPointManager = endPointManagerProvider.getEndPointManager();
					endPointManager.dePublishRecord(records.getRdf(),records.getRdfAbout(),records.getHost());
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
