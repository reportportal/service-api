/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.bts.DefectFieldAllowedValue;
import com.epam.ta.reportportal.entity.bts.DefectFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
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
		if (!CollectionUtils.isEmpty(field.getValue())) {
			defectFormField.setValues(new HashSet<>(field.getValue()));
		}
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
		postFormField.setValue(new ArrayList<>(defectFormField.getValues()));
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
