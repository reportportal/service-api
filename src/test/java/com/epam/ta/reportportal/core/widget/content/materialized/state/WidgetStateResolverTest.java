package com.epam.ta.reportportal.core.widget.content.materialized.state;

import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class WidgetStateResolverTest {

	private final WidgetStateResolver widgetStateResolver = new WidgetStateResolver();

	private static Stream<Arguments> provideWidgetOptions() {
		return Arrays.stream(WidgetState.values()).map(state -> Arguments.of(state, getWidgetOptions(STATE, state.getValue())));
	}

	private static WidgetOptions getWidgetOptions(String key, String value) {
		return new WidgetOptions(Map.of(key, value));
	}

	@ParameterizedTest
	@MethodSource("provideWidgetOptions")
	void generate(WidgetState expectedState, WidgetOptions widgetOptions) {
		Assertions.assertEquals(expectedState, widgetStateResolver.resolve(widgetOptions));
	}

	@Test
	void shouldThrowWhenNoState() {
		final ReportPortalException reportPortalException = Assertions.assertThrows(ReportPortalException.class,
				() -> widgetStateResolver.resolve(getWidgetOptions("key", "value"))
		);
		Assertions.assertEquals("Unable to load widget content. Widget proprties contains errors: Widget state not provided", reportPortalException.getMessage());
	}

	@Test
	void shouldThrowWhenInvalidState() {
		final ReportPortalException reportPortalException = Assertions.assertThrows(ReportPortalException.class,
				() -> widgetStateResolver.resolve(getWidgetOptions(STATE, "wrong"))
		);
		Assertions.assertEquals("Unable to load widget content. Widget proprties contains errors: Widget state not provided", reportPortalException.getMessage());
	}

}