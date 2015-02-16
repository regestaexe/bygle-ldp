package org.bygle.db.services;

import java.util.List;
import java.util.Set;

import org.bygle.db.dao.DBManagerInterface;
import org.hibernate.HibernateException;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("bygleService")
public class BygleService implements DBService{

	private static final long serialVersionUID = 1L;
	
	private DBManagerInterface dbManager;
	

	public List<?> getList(Class<?> genericClass) throws HibernateException{
		return dbManager.getList(genericClass);
	}
	public List<?> getPagedList(Class<?> genericClass,int start,int lenght) throws HibernateException{
		return dbManager.getPagedList(genericClass,start,lenght);
	}
	public List<?> getList(DetachedCriteria criteria) throws HibernateException{
		return dbManager.getList(criteria);
	}
	public List<?> getListFromSQL(Class<?> genericClass,String sql) throws HibernateException{
		return dbManager.getListFromSQL(genericClass,sql);
	}
	public List<?> getPagedList(DetachedCriteria criteria,int start,int lenght) throws HibernateException{
		return dbManager.getPagedList(criteria, start, lenght);
	}
	public List<?> getPagedListFromSQL(Class<?> genericClass,String sql,int start,int lenght) throws HibernateException{
		return dbManager.getPagedListFromSQL(genericClass,sql,start,lenght);
	}
	public void add(Object genericObj) throws HibernateException{
		dbManager.add(genericObj);
	}
	public Object getObject(Class<?> genericClass, Object id) throws HibernateException{
		Object object = null;
		try {
			object = dbManager.getObject(genericClass, id);
		} catch (HibernateException e) {
			throw e;
		}
		return object;
	}

	public void update(Object genericObj) throws HibernateException{
		dbManager.update(genericObj);
	}

	public void addAll(Set<?> genericObjects) throws HibernateException {
		dbManager.addAll(genericObjects);		
	}

	public void remove(Object genericObj) throws HibernateException {
		dbManager.remove(genericObj);
		
	}

	public void removeAll(Set<?> genericObjects) throws HibernateException {
		dbManager.removeAll(genericObjects);
		
	}
	public void removeAll(List<?> genericObjects) throws HibernateException {
		dbManager.removeAll(genericObjects);
		
	}
	public void updateAll(Set<?> genericObjects) throws HibernateException {
		dbManager.updateAll(genericObjects);
		
	}
	public int executeUpdate(String sql) throws HibernateException {
		return dbManager.executeUpdate(sql);
	}
	public int getCountFromSQL(String sql) throws HibernateException {
		return dbManager.getCountFromSQL(sql);
	}
	@Autowired
    public void setDBManager(DBManagerInterface dbManager) {
            this.dbManager = dbManager;
    }
	
}
