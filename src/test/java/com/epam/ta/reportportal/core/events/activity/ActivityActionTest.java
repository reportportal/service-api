package com.epam.ta.reportportal.core.events.activity;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
public class ActivityActionTest {

	@Test
	public void fromString() {
		List<ActivityAction> values = Arrays.stream(ActivityAction.values()).collect(Collectors.toList());
		List<String> strings = values.stream().map(ActivityAction::getValue).collect(Collectors.toList());
		Assert.assertEquals(values.size(), strings.size());
		for (int i = 0; i < strings.size(); i++) {
			Optional<ActivityAction> type = ActivityAction.fromString(strings.get(i));
			Assert.assertTrue(type.isPresent());
			Assert.assertEquals(type.get(), values.get(i));
		}
		Assert.assertFalse(ActivityAction.fromString("no_such_activity").isPresent());
	}

}