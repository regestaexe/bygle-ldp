package org.bygle.db.listener;

import javax.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HibernateListenersConfigurer {
 
	@Autowired
    private SessionFactory sessionFactory;
    @Autowired(required=true)
    DBEventInsertListener dbEventInsertListener;
    @Autowired(required=true)
    DBEventUpdateListener dbEventUpdateListener;
    @Autowired(required=true)
    DBEventDeleteListener dbEventDeleteListener;
   
    @PostConstruct
	public void registerListeners() {
    	EventListenerRegistry registry = ((SessionFactoryImpl) sessionFactory).getServiceRegistry().getService(EventListenerRegistry.class);
	    registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(dbEventInsertListener);
	    registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(dbEventUpdateListener);
	    registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(dbEventDeleteListener);
	}

}