package org.bygle.bean;


import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "records")
public class Records implements java.io.Serializable {
	private static final long serialVersionUID = -2757072151721076046L;
	private Long idRecord;
	private byte[] rdf;
	private Date creationDate;
	private Date modifyDate;
	private String rdfAbout;
	private String contentType;
	private String etag;
	private String creationEtag;
	private String host;
	private RecordTypes recordTypes;
	private RdfClasses rdfClasses;
	private Set<Relations> relationsesForRefIdRecord1 = new HashSet<Relations>(0);
	private Set<Relations> relationsesForRefIdRecord2 = new HashSet<Relations>(0);
	
	
	public Records(RecordTypes recordTypes,RdfClasses rdfClasses,byte[] rdf, Date creationDate,Date modifyDate, String rdfAbout,String contentType, String etag,String creationEtag,String host) {
		super();
		this.recordTypes=recordTypes;
		this.rdfClasses=rdfClasses;
		this.rdf = rdf;
		this.creationDate = creationDate;
		this.modifyDate = modifyDate;
		this.rdfAbout = rdfAbout;
		this.contentType = contentType;
		this.etag = etag;
		this.creationEtag = creationEtag;
		this.host = host;
	}

	public Records() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_record", unique = true, nullable = false)
	public Long getIdRecord() {
		return this.idRecord;
	}

	public void setIdRecord(Long idRecord) {
		this.idRecord = idRecord;
	}


	@Column(name = "rdf",columnDefinition="BLOB")
	public byte[] getRdf() {
		return this.rdf;
	}

	public void setRdf(byte[] rdf) {
		this.rdf = rdf;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_date", length = 19)
	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modify_date", length = 19)
	public Date getModifyDate() {
		return this.modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}


	@Column(name = "rdf_about")
	public String getRdfAbout() {
		return this.rdfAbout;
	}

	public void setRdfAbout(String rdfAbout) {
		this.rdfAbout = rdfAbout;
	}
    
	@Column(name = "content_type")
	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	
	@Column(name = "host")
	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	@Column(name = "creation_etag")
	public String getCreationEtag() {
		return this.creationEtag;
	}

	public void setCreationEtag(String creationEtag) {
		this.creationEtag = creationEtag;
	}
	
	@Column(name = "etag")
	public String getEtag() {
		return this.etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ref_id_record_type", nullable = false, insertable = true, updatable = true)
	public RecordTypes getRecordTypes() {
		return this.recordTypes;
	}

	public void setRecordTypes(RecordTypes recordTypes) {
		this.recordTypes = recordTypes;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ref_id_rdf_class", nullable = true, insertable = true, updatable = true)
	public RdfClasses getRdfClasses() {
		return this.rdfClasses;
	}

	public void setRdfClasses(RdfClasses rdfClasses) {
		this.rdfClasses = rdfClasses;
	}
	
	@OneToMany (fetch = FetchType.LAZY,mappedBy="recordsByRefIdRecord1", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@org.hibernate.annotations.Cascade( {org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	public Set<Relations> getRelationsesForRefIdRecord1() {
		return this.relationsesForRefIdRecord1;
	}

	public void setRelationsesForRefIdRecord1(
			Set<Relations> relationsesForRefIdRecord1) {
		this.relationsesForRefIdRecord1 = relationsesForRefIdRecord1;
	}

	@OneToMany (fetch = FetchType.LAZY,mappedBy="recordsByRefIdRecord2", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@org.hibernate.annotations.Cascade( {org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	public Set<Relations> getRelationsesForRefIdRecord2() {
		return this.relationsesForRefIdRecord2;
	}

	public void setRelationsesForRefIdRecord2(Set<Relations> relationsesForRefIdRecord2) {
		this.relationsesForRefIdRecord2 = relationsesForRefIdRecord2;
	}
}
