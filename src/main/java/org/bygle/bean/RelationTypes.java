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
@Table(name = "relation_types")
public class RelationTypes implements java.io.Serializable {

	private static final long serialVersionUID = -5872731763568026411L;
	private Integer idRelationType;
	private String label;
	private String description;
	private Set<Relations> relationses = new HashSet<Relations>(0);

	public RelationTypes() {
	}

	public RelationTypes(String label, String description) {
		this.label = label;
		this.description = description;
	}
	
	public RelationTypes(String label, String description,
			Set<Relations> relationses) {
		this.label = label;
		this.description = description;
		this.relationses = relationses;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_relation_type", unique = true, nullable = false)
	public Integer getIdRelationType() {
		return this.idRelationType;
	}

	public void setIdRelationType(Integer idRelationType) {
		this.idRelationType = idRelationType;
	}

	@Column(name = "label", length = 100)
	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Column(name = "description", length = 250)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "relationTypes")
	public Set<Relations> getRelationses() {
		return this.relationses;
	}

	public void setRelationses(Set<Relations> relationses) {
		this.relationses = relationses;
	}

}
