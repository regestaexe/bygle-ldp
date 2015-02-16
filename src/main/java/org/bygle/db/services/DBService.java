package org.bygle.db.services;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.criterion.DetachedCriteria;



public interface DBService extends Serializable {
	public List<?> getList(Class<?> genericClass)throws HibernateException;
	public List<?> getPagedList(Class<?> genericClass,int start,int lenght)throws HibernateException;
	public List<?> getList(DetachedCriteria criteria)throws HibernateException;
	public List<?> getListFromSQL(Class<?> genericClass,String sql)throws HibernateException;
	public int getCountFromSQL(String sql)throws HibernateException;
	public List<?> getPagedListFromSQL(Class<?> genericClass,String sql,int start,int lenght)throws HibernateException;
	public List<?> getPagedList(DetachedCriteria criteria,int start,int lenght)throws HibernateException;
	public Object getObject(final Class<?> genericClass,final Object id)throws HibernateException;
	public void update(final Object genericObj)throws HibernateException;
	public void add(final Object genericObj)throws HibernateException;
	public void remove(final Object genericObj)throws HibernateException;
	public void addAll(final Set<?> genericObjects)throws HibernateException;
	public void removeAll(final Set<?> genericObjects)throws HibernateException;
	public void updateAll(final Set<?> genericObjects)throws HibernateException;
	public int  executeUpdate(String sql) throws HibernateException;
}
