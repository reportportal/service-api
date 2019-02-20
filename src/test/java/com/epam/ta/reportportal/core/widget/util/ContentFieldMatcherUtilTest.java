package com.epam.ta.reportportal.core.widget.util;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
//TODO replace random wrong content field generation with the specified loop
public class ContentFieldMatcherUtilTest {

	@Test
	void whenCorrectDefectsContentFieldsFormatThenTrue() {

		boolean match = ContentFieldMatcherUtil.match(DEFECTS_REGEX, buildCorrectDefectContentFields());

		assertTrue(match);
	}

	@Test
	void whenWrongDefectsContentFieldsFormatThenFalse() {

		boolean match = ContentFieldMatcherUtil.match(DEFECTS_REGEX, buildWrongDefectContentFields());

		assertFalse(match);
	}

	@Test
	void whenCorrectExecutionsContentFieldsFormatThenTrue() {

		boolean match = ContentFieldMatcherUtil.match(EXECUTIONS_REGEX, buildCorrectExecutionContentFields());

		assertTrue(match);
	}

	@Test
	void whenWrongExecutionsContentFieldsFormatThenFalse() {

		List<String> contentFields = buildWrongExecutionContentFields();
		boolean match = ContentFieldMatcherUtil.match(DEFECTS_REGEX, contentFields);

		assertFalse(match);
	}

	@Test
	void whenCorrectCombinedContentFieldsFormatThenTrue() {

		boolean match = ContentFieldMatcherUtil.match(COMBINED_CONTENT_FIELDS_REGEX, buildCorrectCombinedContentFields());

		assertTrue(match);
	}

	@Test
	void whenWrongCombinedContentFieldsFormatThenFalse() {

		boolean match = ContentFieldMatcherUtil.match(COMBINED_CONTENT_FIELDS_REGEX, buildWrongCombinedContentFields());

		assertFalse(match);
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

	public static Object[][] data() {
		return new Object[1][0];
	}

}