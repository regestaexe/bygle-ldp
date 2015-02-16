package org.bygle.db.dao;

import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;

public interface DBManagerInterface {
	
	public List<?> getList(final Class<?> genericClass) throws HibernateException;
	
	public List<?> getPagedList(final Class<?> genericClass,final int start,final int lenght) throws HibernateException;
	
	public List<?> getList(final DetachedCriteria criteria) throws HibernateException;
	
	public Session getSession() throws HibernateException;
	
	public List<?> getPagedList(final DetachedCriteria criteria,final int start,final int lenght) throws HibernateException;
	
	public Object getObject(final Class<?> genericClass,final Object id) throws HibernateException ;
	
	public void update(final Object genericObj)throws HibernateException ;
	
	public void add(final Object genericObj)throws HibernateException ;
	
	public void remove(final Object genericObj)throws HibernateException ;
	
	public void addAll(final Set<?> genericObjects)throws HibernateException ;
	
	public void removeAll(final Set<?> genericObjects)throws HibernateException ;
	
	public void removeAll(final List<?> genericObjects)throws HibernateException ;
	
	public void updateAll(final Set<?> genericObjects)throws HibernateException ;
	
	public List<?> getListFromSQL(final Class<?> genericClass,final String query) throws HibernateException;
	
	public int executeUpdate(final String query) throws HibernateException;
	
	public int getCountFromSQL(final String query) throws HibernateException;
	
	public List<?> getPagedListFromSQL(final Class<?> genericClass,final String query,final int start,final int lenght) throws HibernateException;
	

}
