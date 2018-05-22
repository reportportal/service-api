package com.epam.ta.reportportal.store.database.entity.external;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "allowed_value", schema = "public")
public class AllowedValue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long valueId;

	@Column(name = "value_name")
	private String valueName;

	@ManyToOne
	@JoinColumn(name = "post_form_field_id", nullable = false)
	private PostFormField postFormField;

	public AllowedValue() {
	}

	public Long getValueId() {
		return valueId;
	}

	public void setValueId(Long valueId) {
		this.valueId = valueId;
	}

	public String getValueName() {
		return valueName;
	}

	public void setValueName(String valueName) {
		this.valueName = valueName;
	}

	public PostFormField getPostFormField() {
		return postFormField;
	}

	public void setPostFormField(PostFormField postFormField) {
		this.postFormField = postFormField;
	}
}
