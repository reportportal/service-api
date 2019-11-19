package com.epam.ta.reportportal.core.item.impl.history;

import com.epam.ta.reportportal.ws.param.HistoryRequestParams;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public enum ItemHistoryBaselineEnum {

	FILTER(1, historyRequestParams -> historyRequestParams.getFilterParams().isPresent()),
	ITEM(2, historyRequestParams -> historyRequestParams.getParentId().isPresent() || historyRequestParams.getItemId().isPresent()),
	LAUNCH(3, historyRequestParams -> historyRequestParams.getLaunchId().isPresent());

	private final int priority;
	private final Predicate<HistoryRequestParams> baseLinePredicate;

	public static Optional<ItemHistoryBaselineEnum> resolveType(HistoryRequestParams historyRequestParams) {
		return Arrays.stream(ItemHistoryBaselineEnum.values())
				.sorted(Comparator.comparingInt(ItemHistoryBaselineEnum::getPriority))
				.filter(v -> v.getBaseLinePredicate().test(historyRequestParams))
				.findFirst();
	}

	ItemHistoryBaselineEnum(int priority, Predicate<HistoryRequestParams> baseLinePredicate) {
		this.priority = priority;
		this.baseLinePredicate = baseLinePredicate;
	}

	public int getPriority() {
		return priority;
	}

	public Predicate<HistoryRequestParams> getBaseLinePredicate() {
		return baseLinePredicate;
	}
}
