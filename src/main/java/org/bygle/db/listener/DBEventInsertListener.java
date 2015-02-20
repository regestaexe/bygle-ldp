package org.bygle.db.listener;

import java.io.Serializable;

import org.bygle.bean.Records;
import org.bygle.endpoint.managing.EndPointManagerInterface;
import org.bygle.endpoint.managing.EndPointManagerProvider;
import org.bygle.utils.BygleSystemUtils;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DBEventInsertListener implements Serializable, PostInsertEventListener {
	@Autowired
	EndPointManagerProvider endPointManagerProvider;

	private static final long serialVersionUID = 8628132333422887069L;

	public void initialize(Configuration configuration) {
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPostInsert(PostInsertEvent arg0) {
		if (arg0.getEntity() instanceof Records) {
			Records records = (Records) arg0.getEntity();
			try {
				if (records.getRecordTypes().getIdRecordType() != BygleSystemUtils.RESOURCE_TYPE_BINARY) {
					EndPointManagerInterface endPointManager = endPointManagerProvider.getEndPointManager();
					endPointManager.publishRecord(records.getRdf(), records.getRdfAbout(), records.getHost());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
