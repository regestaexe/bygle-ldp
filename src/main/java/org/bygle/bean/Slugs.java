package org.bygle.bean;


import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "slugs")
public class Slugs implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7796973244555499852L;
	private Integer idSlug;
	private String slug;

	public Slugs() {
	}

	public Slugs(String slug) {
		super();
		this.slug = slug;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_slug", unique = true, nullable = false)
	public Integer getIdSlug() {
		return this.idSlug;
	}

	public void setIdSlug(Integer idSlug) {
		this.idSlug = idSlug;
	}

	@Column(name = "slug", length = 250)
	public String getSlug() {
		return this.slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

}
