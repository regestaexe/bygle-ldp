package org.bygle.bean;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "relations")
public class Relations implements java.io.Serializable {

	private static final long serialVersionUID = 2187520037301631154L;
	private RelationsId id;
	private Records recordsByRefIdRecord1;
	private Records recordsByRefIdRecord2;
	private RelationTypes relationTypes;
	private Integer relationOrder;
	private String note;

	public Relations() {
	}

	public Relations(RelationsId id, Records recordsByRefIdRecord1,
			Records recordsByRefIdRecord2, RelationTypes relationTypes) {
		this.id = id;
		this.recordsByRefIdRecord1 = recordsByRefIdRecord1;
		this.recordsByRefIdRecord2 = recordsByRefIdRecord2;
		this.relationTypes = relationTypes;
	}

	public Relations(RelationsId id, Records recordsByRefIdRecord1,
			Records recordsByRefIdRecord2, RelationTypes relationTypes,
			Integer relationOrder, String note) {
		this.id = id;
		this.recordsByRefIdRecord1 = recordsByRefIdRecord1;
		this.recordsByRefIdRecord2 = recordsByRefIdRecord2;
		this.relationTypes = relationTypes;
		this.relationOrder = relationOrder;
		this.note = note;
	}

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "refIdRecord1", column = @Column(name = "ref_id_record_1", nullable = false)),
			@AttributeOverride(name = "refIdRecord2", column = @Column(name = "ref_id_record_2", nullable = false)),
			@AttributeOverride(name = "refIdRelationType", column = @Column(name = "ref_id_relation_type", nullable = false)) })
	public RelationsId getId() {
		return this.id;
	}

	public void setId(RelationsId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ref_id_record_1", nullable = false, insertable = false, updatable = false)
	public Records getRecordsByRefIdRecord1() {
		return this.recordsByRefIdRecord1;
	}

	public void setRecordsByRefIdRecord1(Records recordsByRefIdRecord1) {
		this.recordsByRefIdRecord1 = recordsByRefIdRecord1;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ref_id_record_2", nullable = false, insertable = false, updatable = false)
	public Records getRecordsByRefIdRecord2() {
		return this.recordsByRefIdRecord2;
	}

	public void setRecordsByRefIdRecord2(Records recordsByRefIdRecord2) {
		this.recordsByRefIdRecord2 = recordsByRefIdRecord2;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ref_id_relation_type", nullable = false, insertable = false, updatable = false)
	public RelationTypes getRelationTypes() {
		return this.relationTypes;
	}

	public void setRelationTypes(RelationTypes relationTypes) {
		this.relationTypes = relationTypes;
	}

	@Column(name = "relation_order")
	public Integer getRelationOrder() {
		return this.relationOrder;
	}

	public void setRelationOrder(Integer relationOrder) {
		this.relationOrder = relationOrder;
	}

	@Column(name = "note")
	public String getNote() {
		return this.note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
