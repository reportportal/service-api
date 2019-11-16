package com.epam.ta.reportportal.ws.param;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public enum ItemHistoryBaselineEnum {

	FILTER(1, HistoryRequestParams::getFilterParams),
	DEFAULT(2, HistoryRequestParams::getParentId);

	private final int priority;
	private final Function<HistoryRequestParams, Optional<?>> converter;

	public static Optional<ItemHistoryBaselineEnum> resolveType(HistoryRequestParams historyRequestParams) {
		return Arrays.stream(ItemHistoryBaselineEnum.values())
				.sorted(Comparator.comparingInt(ItemHistoryBaselineEnum::getPriority))
				.filter(v -> v.matches(historyRequestParams))
				.findFirst();
	}

	ItemHistoryBaselineEnum(int priority, Function<HistoryRequestParams, Optional<?>> converter) {
		this.priority = priority;
		this.converter = converter;
	}

	public int getPriority() {
		return priority;
	}

	private boolean matches(HistoryRequestParams historyRequestParams) {
		return this.converter.apply(historyRequestParams).isPresent();
	}
}
