/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.store.database.entity.bts;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "defect_form_field", schema = "public")
public class DefectFormField implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "field_id")
	private String fieldId;

	@ManyToOne
	@JoinColumn(name = "bug_tracking_system_id", nullable = false)
	private BugTrackingSystem bugTrackingSystem;

	@Column(name = "type")
	private String type;

	@Column(name = "required")
	private boolean isRequired;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "defect_form_field_value", joinColumns = @JoinColumn(name = "id"))
	@Column(name = "values")
	private Set<String> values;

	@OneToMany(mappedBy = "defectFormField", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<DefectFieldAllowedValue> defectFieldAllowedValues = Sets.newHashSet();

	public DefectFormField() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public BugTrackingSystem getBugTrackingSystem() {
		return bugTrackingSystem;
	}

	public void setBugTrackingSystem(BugTrackingSystem bugTrackingSystem) {
		this.bugTrackingSystem = bugTrackingSystem;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean required) {
		isRequired = required;
	}

	public Set<String> getValues() {
		return values;
	}

	public void setValues(Set<String> values) {
		this.values = values;
	}

	public Set<DefectFieldAllowedValue> getDefectFieldAllowedValues() {
		return defectFieldAllowedValues;
	}

	public void setDefectFieldAllowedValues(Set<DefectFieldAllowedValue> defectFieldAllowedValues) {
		this.defectFieldAllowedValues.clear();
		this.defectFieldAllowedValues.addAll(defectFieldAllowedValues);
		this.defectFieldAllowedValues.forEach(it -> it.setDefectFormField(this));
	}
}
