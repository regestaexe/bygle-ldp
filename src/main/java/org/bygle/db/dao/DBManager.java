package org.bygle.db.dao;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("dbManager")
@Transactional
public class DBManager implements DBManagerInterface{

	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		        hibernateTemplate = new HibernateTemplate(sessionFactory);
	}
	
	public List<?> getList(final Class<?> genericClass) throws HibernateException {
		try {					
			return hibernateTemplate.find("from "+ genericClass.getName());	
		} catch (HibernateException e) {
			throw e;
		}
	}

	public List<?> getPagedList(final Class<?> genericClass,final int start,final int lenght) throws HibernateException {
		try {
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(genericClass);
			return hibernateTemplate.findByCriteria(detachedCriteria, start, lenght);
		} catch (HibernateException e) {
			throw e;
		}
	}
	public List<?> getList(final DetachedCriteria criteria) throws HibernateException{		
		try{
			return hibernateTemplate.findByCriteria(criteria);
		} catch (HibernateException e) {
			throw e;
		}
	}
	
	public Session getSession() throws HibernateException{		
		try{
			Session session = null;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
			}
			return session;
		} catch (HibernateException e) {
			throw e;
		}
	}
	public List<?> getPagedList(final DetachedCriteria criteria,final int start,final int lenght) throws HibernateException{		
		try{
			return hibernateTemplate.findByCriteria(criteria, start, lenght);
		} catch (HibernateException e) {
			throw e;
		}
	}
	public Object getObject(final Class<?> genericClass,final Object id) throws HibernateException {
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			Object result = session.get(genericClass, (Serializable)id);
			if(close)
				session.close();
			return result;
		} catch (HibernateException e) {
			throw e;
		}
	}
	@Transactional(readOnly = false)
	public void update(final Object genericObj)throws HibernateException {
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			session.merge(genericObj);
			if(close)
				session.close();
		} catch (HibernateException e) {
			throw e;
		}
	}
	@Transactional(readOnly = false)
	public void add(final Object genericObj)throws HibernateException {		
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			session.saveOrUpdate(genericObj);
			if(close)
				session.close();
		} catch (HibernateException e) {
			throw e;
		}
	}
	@Transactional(readOnly = false)
	public void remove(final Object genericObj)throws HibernateException {
		
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			session.delete(genericObj);
			if(close)
				session.close();
		} catch (HibernateException e) {
			throw e;
		}
	}
	@Transactional(readOnly = false)
	public void addAll(final Set<?> genericObjects)throws HibernateException {
		
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			for (Iterator<?> it = genericObjects.iterator(); it.hasNext();) {
				session.saveOrUpdate(it.next());
			}
			if(close)
				session.close();
		} catch (HibernateException e) {
			throw e;
		}
	}
	@Transactional(readOnly = false)
	public void removeAll(final Set<?> genericObjects)throws HibernateException {
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			for (Iterator<?> it = genericObjects.iterator(); it.hasNext();) {
				session.delete(it.next());
			}
			if(close)
				session.close();
		} catch (HibernateException e) {
			throw e;
		}
	}
	@Transactional(readOnly = false)
	public void removeAll(final List<?> genericObjects)throws HibernateException {
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			for (Iterator<?> it = genericObjects.iterator(); it.hasNext();) {
				session.delete(it.next());
			}
			if(close)
				session.close();
		} catch (HibernateException e) {
			throw e;
		}
	}
	@Transactional(readOnly = false)
	public void updateAll(final Set<?> genericObjects)throws HibernateException {
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			for (Iterator<?> it = genericObjects.iterator(); it.hasNext();) {
				session.saveOrUpdate(it.next());
			}
			if(close)
				session.close();
		} catch (HibernateException e) {
			throw e;
		}
	}
	public List<?> getListFromSQL(final Class<?> genericClass,final String query) throws HibernateException{		
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			SQLQuery sQLQuery= session.createSQLQuery(query);
			sQLQuery.addEntity(genericClass);
			List<?> result = sQLQuery.list();
			if(close)
				session.close();
			return result;
		} catch (HibernateException e) {
			throw e;
		}
	}
	public int executeUpdate(final String query) throws HibernateException{		
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			SQLQuery sQLQuery= session.createSQLQuery(query);
			int result = sQLQuery.executeUpdate();
			if(close)
				session.close();
			return result;
		} catch (HibernateException e) {
			throw e;
		}
	}
	public int getCountFromSQL(final String query) throws HibernateException{		
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			SQLQuery sQLQuery= session.createSQLQuery(query);
			List<?> resultList = sQLQuery.list();
			int result = DataAccessUtils.intResult(resultList);
			if(close)
				session.close();
			return result;
		}catch (Error e) {
			throw e;
		}catch (Exception e) {
			throw new HibernateException(e);
		}
	}
	public List<?> getPagedListFromSQL(final Class<?> genericClass,final String query,final int start,final int lenght) throws HibernateException{		
		try{
			Session session = null;
			boolean close = false;
			try{
				session = hibernateTemplate.getSessionFactory().getCurrentSession();
			}catch(HibernateException e){
				session = hibernateTemplate.getSessionFactory().openSession();
				close = true;
			}
			SQLQuery sQLQuery= session.createSQLQuery(query);
			sQLQuery.addEntity(genericClass);
			sQLQuery.setFirstResult(start);
			sQLQuery.setMaxResults(lenght);
			List<?> result = sQLQuery.list();
			if(close)
				session.close();
			return result;
		} catch (HibernateException e) {
			throw e;
		}
	}
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

}
