package org.bygle.bean;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "rdf_classes")
public class RdfClasses implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6332745975079568050L;
	private Integer idRdfClass;
	private String className;
	private String rdfType;
	private int count;
	private Set<Records> records = new HashSet<Records>(0);

	public RdfClasses() {
	}



	public RdfClasses(String className, String rdfType,int count) {
		super();
		this.className = className;
		this.rdfType = rdfType;
		this.count = count;
	}



	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_rdf_class", unique = true, nullable = false)
	public Integer getIdRdfClass() {
		return this.idRdfClass;
	}

	public void setIdRdfClass(Integer idRdfClass) {
		this.idRdfClass = idRdfClass;
	}

	@Column(name = "class_name", length = 250)
	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Column(name = "rdf_type", length = 250)
	public String getRdfType() {
		return this.rdfType;
	}

	public void setRdfType(String rdfType) {
		this.rdfType = rdfType;
	}
	
	
	@Column(name = "count", nullable = false)
	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "rdfClasses")
	public Set<Records> getRecords() {
		return this.records;
	}

	public void setRecords(Set<Records> records) {
		this.records = records;
	}

}
