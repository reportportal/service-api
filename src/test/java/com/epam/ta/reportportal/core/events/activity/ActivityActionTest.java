package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.entity.activity.ActivityAction;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pavel Bortnik
 */
class ActivityActionTest {

	@Test
	void fromString() {
		List<ActivityAction> values = Arrays.stream(ActivityAction.values()).collect(Collectors.toList());
		List<String> strings = values.stream().map(ActivityAction::getValue).collect(Collectors.toList());
		assertEquals(values.size(), strings.size());
		for (int i = 0; i < strings.size(); i++) {
			Optional<ActivityAction> type = ActivityAction.fromString(strings.get(i));
			assertTrue(type.isPresent());
			assertEquals(type.get(), values.get(i));
		}
		assertFalse(ActivityAction.fromString("no_such_activity").isPresent());
	}

}