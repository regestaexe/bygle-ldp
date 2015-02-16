package org.bygle.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.bygle.bean.Records;
import org.bygle.bean.RelationTypes;
import org.bygle.bean.Relations;
import org.bygle.bean.RelationsId;
import org.bygle.db.services.BygleService;
import org.bygle.endpoint.managing.utils.RelationsContainer;
import org.bygle.utils.BygleSystemUtils;
import org.bygle.xml.XMLBuilder;
import org.bygle.xml.XMLReader;
import org.dom4j.tree.DefaultAttribute;
import org.hibernate.HibernateException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("relationsService")
public class RelationsService {
	
	@Autowired
	BygleService bygleService;
	@Autowired
	LDPService ldpService;
	
	public void addRelations(Records records){
		try {
			XMLReader xmlReader = new XMLReader(records.getRdf());
			List<?> nodeList = xmlReader.getNodeList("/rdf:RDF/*/*/@rdf:resource[not(ancestor::rdf:type)]");
			for (Iterator<?> iterator = nodeList.iterator(); iterator.hasNext();) {
				DefaultAttribute defaultAttribute = (DefaultAttribute)iterator.next();
				Records relatedRecord = getRecords(defaultAttribute.getStringValue());
				if(relatedRecord!=null){
					addRelation(records, relatedRecord, defaultAttribute);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addRelations(List<RelationsContainer> relationsContainerList){
		try {
			for (int i = 0; i < relationsContainerList.size(); i++) {
				RelationsContainer relationsContainer = relationsContainerList.get(i);
				List<?> nodeList = relationsContainer.getNodeList();
				Records records = (Records)bygleService.getObject(Records.class,relationsContainer.getIdRecord());
				for (Iterator<?> iterator = nodeList.iterator(); iterator.hasNext();) {
					DefaultAttribute defaultAttribute = (DefaultAttribute)iterator.next();
					Records relatedRecord = getRecords(defaultAttribute.getStringValue());
					if(relatedRecord!=null){
						addRelation(records, relatedRecord, defaultAttribute);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void updateRelations(List<RelationsContainer> relationsContainerList){
		try {
			for (int i = 0; i < relationsContainerList.size(); i++) {
				RelationsContainer relationsContainer = relationsContainerList.get(i);
				Records records = (Records)bygleService.getObject(Records.class,relationsContainer.getIdRecord());
				XMLReader xmlReader = new XMLReader(records.getRdf());
				XMLReader oldxmlReader = new XMLReader(relationsContainer.getOldRdf());
				List<?> nodeList = xmlReader.getNodeList("/rdf:RDF/*/*/@rdf:resource[not(ancestor::rdf:type)]");
				List<?> oldNodeList = oldxmlReader.getNodeList("/rdf:RDF/*/*/@rdf:resource[not(ancestor::rdf:type)]");
				for (Iterator<?> iterator = nodeList.iterator(); iterator.hasNext();) {
					DefaultAttribute defaultAttribute = (DefaultAttribute)iterator.next();
					String xPath="/rdf:RDF/*/"+defaultAttribute.getParent().getNamespace().getPrefix()+":"+defaultAttribute.getParent().getName()+"[@rdf:resource='"+defaultAttribute.getStringValue()+"']";
					if(oldxmlReader.getNodeCount(xPath)==0){
						Records relatedRecord = getRecords(defaultAttribute.getStringValue());
						if(relatedRecord!=null){
							addRelation(records, relatedRecord, defaultAttribute);
						}
					}
				}
				for (Iterator<?> iterator = oldNodeList.iterator(); iterator.hasNext();) {
					DefaultAttribute defaultAttribute = (DefaultAttribute)iterator.next();
					String xPath="/rdf:RDF/*/"+defaultAttribute.getParent().getNamespace().getPrefix()+":"+defaultAttribute.getParent().getName()+"[@rdf:resource='"+defaultAttribute.getStringValue()+"']";
					if(xmlReader.getNodeCount(xPath)==0){
						Records relatedRecord = getRecords(defaultAttribute.getStringValue());
						if(relatedRecord!=null){
							removeRelation(records, relatedRecord, defaultAttribute);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateRelations(Records records,byte[] oldRdf){
		try {
			XMLReader xmlReader = new XMLReader(records.getRdf());
			XMLReader oldxmlReader = new XMLReader(oldRdf);
			List<?> nodeList = xmlReader.getNodeList("/rdf:RDF/*/*/@rdf:resource[not(ancestor::rdf:type)]");
			List<?> oldNodeList = oldxmlReader.getNodeList("/rdf:RDF/*/*/@rdf:resource[not(ancestor::rdf:type)]");
			for (Iterator<?> iterator = nodeList.iterator(); iterator.hasNext();) {
				DefaultAttribute defaultAttribute = (DefaultAttribute)iterator.next();
				String xPath="/rdf:RDF/*/"+defaultAttribute.getParent().getNamespace().getPrefix()+":"+defaultAttribute.getParent().getName()+"[@rdf:resource='"+defaultAttribute.getStringValue()+"']";
				if(oldxmlReader.getNodeCount(xPath)==0){
					Records relatedRecord = getRecords(defaultAttribute.getStringValue());
					if(relatedRecord!=null){
						addRelation(records, relatedRecord, defaultAttribute);
					}
				}
			}
			for (Iterator<?> iterator = oldNodeList.iterator(); iterator.hasNext();) {
				DefaultAttribute defaultAttribute = (DefaultAttribute)iterator.next();
				String xPath="/rdf:RDF/*/"+defaultAttribute.getParent().getNamespace().getPrefix()+":"+defaultAttribute.getParent().getName()+"[@rdf:resource='"+defaultAttribute.getStringValue()+"']";
				if(xmlReader.getNodeCount(xPath)==0){
					Records relatedRecord = getRecords(defaultAttribute.getStringValue());
					if(relatedRecord!=null){
						removeRelation(records, relatedRecord, defaultAttribute);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String testLDPRelations(byte[] content){
		String result = null;
		try {
			XMLReader xmlReader = new XMLReader(content);
			int countContains = xmlReader.getNodeCount("/rdf:RDF/*/ldp:contains");
			for (int i = 0; i < countContains; i++) {
				String relatedAbout = xmlReader.getNodeValue("/rdf:RDF/*/ldp:contains["+(i+1)+"]/@rdf:resource");
				if(getRecords(relatedAbout)==null){
					result = relatedAbout;
				}
			}
		} catch (Exception e) {
		}
		return result;
	}

	 
	public void cleanRelateds(Records records) throws Exception{
		
		
		try{
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Relations.class);
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("recordsByRefIdRecord1",records)).add(Restrictions.eq("recordsByRefIdRecord2", records)));
			List<?> relationsList =bygleService.getList(detachedCriteria);
			for (int i = 0; i < relationsList.size(); i++) {
				Relations relations = (Relations)relationsList.get(i);
				Records relatedRecords = null;
				if(relations.getRecordsByRefIdRecord1().getIdRecord().longValue() == records.getIdRecord().longValue()){
					relatedRecords=relations.getRecordsByRefIdRecord2();
				}else{
					relatedRecords=relations.getRecordsByRefIdRecord1();
				}
				if(relatedRecords.getRecordTypes().getIdRecordType().intValue()!=BygleSystemUtils.RESOURCE_TYPE_BINARY){
					XMLBuilder xmlBuilder = new XMLBuilder(relatedRecords.getRdf());
					XMLReader xmlReader = new XMLReader(relatedRecords.getRdf());
					xmlBuilder.deleteNode("/rdf:RDF/*/*[@rdf:resource='"+records.getRdfAbout()+"']");
					xmlBuilder.deleteNode("/rdf:RDF/*/*[@rdf:resource='"+ ldpService.joinUrl(BygleSystemUtils.getStringProperty("endpoint.defaultDomain"), records.getRdfAbout())+"']");
					xmlBuilder.deleteNode("/rdf:RDF/*/*[@"+BygleSystemUtils.getStringProperty("endpoint.member.customRelation")+"='"+ ldpService.joinUrl(BygleSystemUtils.getStringProperty("endpoint.defaultDomain"), records.getRdfAbout())+"']");
					if(xmlReader.getNodeCount("/rdf:RDF/*/*[@ldp:hasMemberRelation='"+BygleSystemUtils.getStringProperty("endpoint.member.customRelation")+"']")<2){
						//xmlBuilder.deleteNode("/rdf:RDF/*/ldp:hasMemberRelation[/text()='"+ BygleSystemUtils.getStringProperty("endpoint.member.customRelation")+"']");
						xmlBuilder.deleteNode("/rdf:RDF/*/*[@ldp:hasMemberRelation='"+BygleSystemUtils.getStringProperty("endpoint.member.customRelation")+"']");
					}
					xmlBuilder.deleteNode("/rdf:RDF/*/*[@ldp:membershipResource='"+ ldpService.joinUrl(BygleSystemUtils.getStringProperty("endpoint.defaultDomain"), records.getRdfAbout())+"']");
					String rdf = xmlBuilder.getXML(BygleSystemUtils.getStringProperty("default.encoding"), false);
					String md5ETag  = DigestUtils.md5Hex(rdf);
					relatedRecords.setEtag(md5ETag);
					relatedRecords.setRdf(rdf.getBytes());
					relatedRecords.setModifyDate(new Date());
					bygleService.update(relatedRecords);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addRelation(Records records,Records relatedRecord,DefaultAttribute defaultAttribute){
		try {
			RelationTypes relationTypes = getRelationTypes(defaultAttribute.getParent().getNamespace().getPrefix()+":"+defaultAttribute.getParent().getName());
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Relations.class);
			detachedCriteria.add(Restrictions.eq("relationTypes",relationTypes));
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("recordsByRefIdRecord1", records)).add(Restrictions.eq("recordsByRefIdRecord2", relatedRecord)));
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("recordsByRefIdRecord1",relatedRecord)).add(Restrictions.eq("recordsByRefIdRecord2", records)));
			List<?> relationList = bygleService.getList(detachedCriteria);
			if(relationList.size()==0){
				RelationsId relationsId = new RelationsId(records.getIdRecord(), relatedRecord.getIdRecord(), relationTypes.getIdRelationType());
				Relations relations = new Relations(relationsId, records, relatedRecord, relationTypes);
				bygleService.add(relations);
			}
		}catch (HibernateException e) {
			e.printStackTrace();
		}
	}
	
	private void removeRelation(Records records,Records relatedRecord,DefaultAttribute defaultAttribute){
		try {
			RelationTypes relationTypes = getRelationTypes(defaultAttribute.getParent().getNamespace().getPrefix()+":"+defaultAttribute.getParent().getName());
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Relations.class);
			detachedCriteria.add(Restrictions.eq("relationTypes",relationTypes));
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("recordsByRefIdRecord1",records)).add(Restrictions.eq("recordsByRefIdRecord2", relatedRecord)));
			detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("recordsByRefIdRecord1",relatedRecord)).add(Restrictions.eq("recordsByRefIdRecord2", records)));
			List<?> relationList = bygleService.getList(detachedCriteria);
			if(relationList.size()!=0){
				Relations relations = (Relations)relationList.get(0);
				bygleService.remove(relations);
			}
		}catch (HibernateException e) {
			e.printStackTrace();
		}
	}
	
	private RelationTypes getRelationTypes(String relationName){
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(RelationTypes.class);
		detachedCriteria.add(Restrictions.eq("label", relationName));
		List<?> relationTypesList = bygleService.getList(detachedCriteria);
		RelationTypes relationTypes = null;
		if(relationTypesList.size()>0){
			relationTypes = (RelationTypes)relationTypesList.get(0);
		}else{
			relationTypes = new RelationTypes(relationName, relationName);
			bygleService.add(relationTypes);
		}
		return relationTypes;
	}
	
	private Records getRecords(String rdfAbout) throws ConfigurationException{
		String about = rdfAbout;
		if(about.indexOf(BygleSystemUtils.getStringProperty("endpoint.defaultDomain"))!=-1){
			about =  StringUtils.substringAfter(rdfAbout,BygleSystemUtils.getStringProperty("endpoint.defaultDomain"));
			if(!about.startsWith("/"))
				about = "/"+about;
		}
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Records.class);
		detachedCriteria.add(Restrictions.eq("rdfAbout", about));
		List<?> recordsList = bygleService.getList(detachedCriteria);
		if(recordsList.size()>0){
			return (Records) recordsList.get(0);
		}else{
				return null;
		}
	}
	
	
}
