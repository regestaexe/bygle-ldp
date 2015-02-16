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
@Table(name = "record_types")
public class RecordTypes implements java.io.Serializable {
	private static final long serialVersionUID = -4586187414088019941L;
	private Integer idRecordType;
	private String label;
	private Set<Records> records = new HashSet<Records>(0);

	public RecordTypes() {
	}

	public RecordTypes(String label,Set<Records> records) {
		this.label = label;
		this.records = records;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_record_type", unique = true, nullable = false)
	public Integer getIdRecordType() {
		return this.idRecordType;
	}

	public void setIdRecordType(Integer idRecordType) {
		this.idRecordType = idRecordType;
	}

	@Column(name = "label", length = 250)
	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "recordTypes")
	public Set<Records> getRecords() {
		return this.records;
	}

	public void setRecords(Set<Records> records) {
		this.records = records;
	}

}
