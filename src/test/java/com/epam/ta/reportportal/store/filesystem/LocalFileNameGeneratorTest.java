package com.epam.ta.reportportal.store.filesystem;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class LocalFileNameGeneratorTest {

	@Test
	public void generate() {

		String test21 = new LocalFilePathGenerator().generate();
		String test22 = new LocalFilePathGenerator().generate();
		assertNotEquals(test21, test22);

		assertTrue(test21.matches("^/\\w{2}/\\w{2}/\\w{2}/.*$"));
		assertTrue(test22.matches("^/\\w{2}/\\w{2}/\\w{2}/.*$"));
	}
}
