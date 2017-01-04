package com.epam.ta.reportportal.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrei Varabyeu
 */
public class PredicatesTest {

	@Test
	public void checkSpecialCharacters() {
		Assert.assertTrue("Incorrect predicate behavior: only spec chars", Predicates.SPECIAL_CHARS_ONLY.test("_"));
		Assert.assertFalse("Incorrect predicate behavior: spec chars after ASCII", Predicates.SPECIAL_CHARS_ONLY.test("a_"));
		Assert.assertFalse("Incorrect predicate behavior: spec chars before ASCII", Predicates.SPECIAL_CHARS_ONLY.test("_a"));
	}
}