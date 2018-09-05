package com.epam.ta.reportportal.core.widget.util;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.data.domain.Sort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants.*;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;

@RunWith(Parameterized.class)
public class ContentFieldMatcherUtilTest {

	@Test
	public void whenCorrectDefectsContentFieldsFormatThenTrue() {

		boolean match = ContentFieldMatcherUtil.match(DEFECTS_REGEX, buildCorrectDefectContentFields());

		Assert.assertTrue(match);
	}

	@Test
	public void whenWrongDefectsContentFieldsFormatThenFalse() {

		Map<Sort, Sort> map = new LinkedHashMap<>();
		System.out.println(map.keySet());
		GROUP_SORTS.apply(map.values());

		boolean match = ContentFieldMatcherUtil.match(DEFECTS_REGEX, buildWrongDefectContentFields());

		Assert.assertFalse(match);
	}

	@Test
	public void whenCorrectExecutionsContentFieldsFormatThenTrue() {

		boolean match = ContentFieldMatcherUtil.match(EXECUTIONS_REGEX, buildCorrectExecutionContentFields());

		Assert.assertTrue(match);
	}

	@Test
	public void whenWrongExecutionsContentFieldsFormatThenFalse() {

		List<String> contentFields = buildWrongExecutionContentFields();
		boolean match = ContentFieldMatcherUtil.match(DEFECTS_REGEX, contentFields);

		Assert.assertFalse(match);
	}

	@Test
	public void whenCorrectCombinedContentFieldsFormatThenTrue() {

		boolean match = ContentFieldMatcherUtil.match(COMBINED_CONTENT_FIELDS_REGEX, buildCorrectCombinedContentFields());

		Assert.assertTrue(match);
	}

	@Test
	public void whenWrongCombinedContentFieldsFormatThenFalse() {

		boolean match = ContentFieldMatcherUtil.match(COMBINED_CONTENT_FIELDS_REGEX, buildWrongCombinedContentFields());

		Assert.assertFalse(match);
	}

	private List<String> buildCorrectDefectContentFields() {
		return Lists.newArrayList(
				"statistics$defects$automation_bug$AB001",
				"statistics$defects$product_bug$PB001",
				"statistics$defects$to_investigate$TI001",
				"statistics$defects$system_issue$SI001",
				"statistics$defects$no_defect$ND001",
				"statistics$defects$no_defect$total",
				"statistics$defects$product_bug$total",
				"statistics$defects$to_investigate$total",
				"statistics$defects$system_issue$total"

		);
	}

	private List<String> buildWrongDefectContentFields() {
		List<String> contentFields = buildCorrectDefectContentFields();
		Random random = new Random();
		int index = random.nextInt(contentFields.size());

		contentFields.set(index, "statistics$wrong$format");

		return contentFields;
	}

	private List<String> buildCorrectExecutionContentFields() {
		return Lists.newArrayList(
				"statistics$executions$passed",
				"statistics$executions$failed",
				"statistics$executions$skipped",
				"statistics$executions$total"
		);
	}

	private List<String> buildWrongExecutionContentFields() {
		List<String> contentFields = buildCorrectExecutionContentFields();

		Random random = new Random();
		int index = random.nextInt(contentFields.size());

		contentFields.set(index, "statistics$wrong$format");

		return contentFields;
	}

	private List<String> buildCorrectCombinedContentFields() {
		return Lists.newArrayList(
				"statistics$executions$passed",
				"statistics$executions$failed",
				"statistics$executions$skipped",
				"statistics$executions$total",
				"statistics$defects$automation_bug$AB001",
				"statistics$defects$product_bug$PB001",
				"statistics$defects$to_investigate$TI001",
				"statistics$defects$system_issue$SI001",
				"statistics$defects$no_defect$ND001",
				"statistics$defects$no_defect$total"
		);
	}

	private List<String> buildWrongCombinedContentFields() {
		List<String> contentFields = buildCorrectCombinedContentFields();

		Random random = new Random();
		int index = random.nextInt(contentFields.size());

		contentFields.set(index, "statistics$wrong$format");

		return contentFields;
	}

	@Parameterized.Parameters
	public static Object[][] data() {
		return new Object[10][0];
	}

}