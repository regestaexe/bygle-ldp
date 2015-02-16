package org.bygle.bean;


import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RelationsId implements java.io.Serializable {

	private static final long serialVersionUID = -9204195667090379259L;
	private long refIdRecord1;
	private long refIdRecord2;
	private int refIdRelationType;

	public RelationsId() {
	}

	public RelationsId(long refIdRecord1, long refIdRecord2,
			int refIdRelationType) {
		this.refIdRecord1 = refIdRecord1;
		this.refIdRecord2 = refIdRecord2;
		this.refIdRelationType = refIdRelationType;
	}

	@Column(name = "ref_id_record_1", nullable = false)
	public long getRefIdRecord1() {
		return this.refIdRecord1;
	}

	public void setRefIdRecord1(long refIdRecord1) {
		this.refIdRecord1 = refIdRecord1;
	}

	@Column(name = "ref_id_record_2", nullable = false)
	public long getRefIdRecord2() {
		return this.refIdRecord2;
	}

	public void setRefIdRecord2(long refIdRecord2) {
		this.refIdRecord2 = refIdRecord2;
	}

	@Column(name = "ref_id_relation_type", nullable = false)
	public int getRefIdRelationType() {
		return this.refIdRelationType;
	}

	public void setRefIdRelationType(int refIdRelationType) {
		this.refIdRelationType = refIdRelationType;
	}
    
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof RelationsId))
			return false;
		RelationsId castOther = (RelationsId) other;

		return (this.getRefIdRecord1() == castOther.getRefIdRecord1())
				&& (this.getRefIdRecord2() == castOther.getRefIdRecord2())
				&& (this.getRefIdRelationType() == castOther
						.getRefIdRelationType());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (int) this.getRefIdRecord1();
		result = 37 * result + (int) this.getRefIdRecord2();
		result = 37 * result + this.getRefIdRelationType();
		return result;
	}

}
