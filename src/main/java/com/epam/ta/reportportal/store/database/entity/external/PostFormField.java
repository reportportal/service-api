package com.epam.ta.reportportal.store.database.entity.external;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post_form_field", schema = "public")
public class PostFormField {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@Column(name = "field_name")
	private String fieldName;

	@Column(name = "external_system_type")
	private String fieldType;

	@Column(name = "is_required")
	private boolean isRequired;

	@ElementCollection
	@CollectionTable(name = "values")
	private List<String> values;

	@OneToMany(mappedBy = "postFormField", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private List<AllowedValue> definedValues;

	@ManyToOne
	@JoinColumn(name = "external_system_id", nullable = false)
	private ExternalSystem externalSystem;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean required) {
		isRequired = required;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public List<AllowedValue> getDefinedValues() {
		return definedValues;
	}

	public void setDefinedValues(List<AllowedValue> definedValues) {
		this.definedValues = definedValues;
	}

	public ExternalSystem getExternalSystem() {
		return externalSystem;
	}

	public void setExternalSystem(ExternalSystem externalSystem) {
		this.externalSystem = externalSystem;
	}
}
