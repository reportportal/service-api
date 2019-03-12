package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.bts.DefectFieldAllowedValue;
import com.epam.ta.reportportal.entity.bts.DefectFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IntegrationFieldsConverterTest {

	@Test
	void fieldToDb() {
		final PostFormField field = getField();
		final DefectFormField defectFormField = IntegrationFieldsConverter.FIELD_TO_DB.apply(field);

		assertEquals(defectFormField.getFieldId(), field.getId());
		assertEquals(defectFormField.getType(), field.getFieldType());
		assertEquals(defectFormField.isRequired(), field.getIsRequired());
		assertThat(defectFormField.getValues()).containsExactlyElementsOf(field.getValue());
		assertThat(defectFormField.getDefectFieldAllowedValues()).hasSameSizeAs(field.getDefinedValues());
	}

	@Test
	void fieldToModel() {
		final DefectFormField defectFormField = getDefectFormField();
		final PostFormField postFormField = IntegrationFieldsConverter.FIELD_TO_MODEL.apply(defectFormField);

		assertEquals(postFormField.getFieldType(), defectFormField.getType());
		assertEquals(postFormField.getIsRequired(), defectFormField.isRequired());
		assertThat(postFormField.getValue()).containsExactlyInAnyOrder("value1", "value2");
		assertThat(postFormField.getDefinedValues()).hasSameSizeAs(defectFormField.getDefectFieldAllowedValues());
	}

	private static PostFormField getField() {
		final PostFormField postFormField = new PostFormField();
		postFormField.setFieldType("type");
		postFormField.setId("id");
		postFormField.setIsRequired(true);
		postFormField.setFieldName("name");
		final AllowedValue allowedValue = new AllowedValue();
		allowedValue.setValueId("valueId");
		allowedValue.setValueName("valueName");
		postFormField.setDefinedValues(Collections.singletonList(allowedValue));
		postFormField.setValue(Collections.singletonList("value"));
		return postFormField;
	}

	private static DefectFormField getDefectFormField() {
		final DefectFormField defectFormField = new DefectFormField();
		defectFormField.setFieldId("fieldId");
		defectFormField.setRequired(true);
		defectFormField.setType("type");
		defectFormField.setValues(Sets.newHashSet("value1", "value2"));
		final DefectFieldAllowedValue defectFieldAllowedValue = new DefectFieldAllowedValue();
		defectFieldAllowedValue.setValueId("id");
		defectFieldAllowedValue.setValueName("name");
		defectFormField.setDefectFieldAllowedValues(Sets.newHashSet(defectFieldAllowedValue));
		return defectFormField;
	}
}