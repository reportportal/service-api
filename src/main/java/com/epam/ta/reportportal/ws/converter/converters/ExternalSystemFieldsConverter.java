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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.store.database.entity.bts.DefectFieldAllowedValue;
import com.epam.ta.reportportal.store.database.entity.bts.DefectFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.google.common.base.Preconditions;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
public final class ExternalSystemFieldsConverter {

	public ExternalSystemFieldsConverter() {
		//static only
	}

	public static final Function<PostFormField, DefectFormField> FIELD_TO_DB = field -> {
		Preconditions.checkNotNull(field);
		DefectFormField defectFormField = new DefectFormField();
		defectFormField.setFieldId(field.getFieldName());
		defectFormField.setType(field.getFieldType());
		defectFormField.setRequired(field.getIsRequired());
		//defectFormField.setValues(field.getValue().toArray(new String[field.getValue().size()]));
		defectFormField.setDefectFieldAllowedValues(
				field.getDefinedValues().stream().map(ExternalSystemFieldsConverter.VALUE_TO_DB).collect(Collectors.toSet()));
		return defectFormField;
	};

	public static final Function<DefectFormField, PostFormField> FIELD_TO_MODEL = defectFormField -> {
		Preconditions.checkNotNull(defectFormField);
		PostFormField postFormField = new PostFormField();
		postFormField.setFieldName(defectFormField.getFieldId());
		postFormField.setFieldType(defectFormField.getType());
		postFormField.setIsRequired(defectFormField.isRequired());
		postFormField.setDefinedValues(defectFormField.getDefectFieldAllowedValues()
				.stream()
				.map(ExternalSystemFieldsConverter.VALUE_TO_MODEL)
				.collect(Collectors.toList()));
		return postFormField;
	};

	public static final Function<AllowedValue, DefectFieldAllowedValue> VALUE_TO_DB = value -> {
		Preconditions.checkNotNull(value);
		DefectFieldAllowedValue allowedValue = new DefectFieldAllowedValue();
		allowedValue.setValueId(value.getValueId());
		allowedValue.setValueName(value.getValueName());
		return allowedValue;
	};

	public static final Function<DefectFieldAllowedValue, AllowedValue> VALUE_TO_MODEL = defectFieldAllowedValue -> {
		Preconditions.checkNotNull(defectFieldAllowedValue);
		AllowedValue allowedValue = new AllowedValue();
		allowedValue.setValueId(defectFieldAllowedValue.getValueId());
		allowedValue.setValueName(defectFieldAllowedValue.getValueName());
		return allowedValue;
	};
}
