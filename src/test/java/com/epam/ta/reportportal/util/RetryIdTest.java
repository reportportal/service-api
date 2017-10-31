package com.epam.ta.reportportal.util;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;

public class RetryIdTest {

	@Test(expected = IllegalArgumentException.class)
	public void testParseNegative() {
		RetryId.parse("NOT_OK");
	}

	@Test
	public void testParse() {
		RetryId id = RetryId.parse(RetryId.PREFIX + "root" + ":" + "hash");
		Assert.assertThat(id.getItemHash(), equalTo("hash"));
		Assert.assertThat(id.getRootID(), equalTo("root"));
		Assert.assertThat(id.toString(), equalTo("retry:root:hash"));
	}

	@Test
	public void testNew() {
		RetryId id = RetryId.newID("root");
		Assert.assertThat(id.getItemHash(), not(isEmptyString()));
		Assert.assertThat(id.getRootID(), equalTo("root"));
	}

}