/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.item.impl.history;

import com.epam.ta.reportportal.core.item.impl.history.param.HistoryRequestParams;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Enum for {@link com.epam.ta.reportportal.entity.item.history.TestItemHistory} retrieving type resolving.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public enum ItemHistoryBaselineEnum {

	COMPARING(1,
			historyRequestParams -> historyRequestParams.getHistoryType()
					.map(HistoryRequestParams.HistoryTypeEnum.COMPARING::equals)
					.orElse(Boolean.FALSE)
	),
	FILTER(2, historyRequestParams -> historyRequestParams.getFilterParams().isPresent()),
	ITEM(3, historyRequestParams -> historyRequestParams.getParentId().isPresent() || historyRequestParams.getItemId().isPresent()),
	LAUNCH(4, historyRequestParams -> historyRequestParams.getLaunchId().isPresent());

	private final int priority;
	private final Predicate<HistoryRequestParams> baseLinePredicate;

	/**
	 * {@link ItemHistoryBaselineEnum} is resolved using {@link Predicate},
	 * types ordered by `priority` field in ascending order, first matched type is returned.
	 *
	 * @param historyRequestParams {@link HistoryRequestParams}
	 * @return {@link Optional} with {@link ItemHistoryBaselineEnum}
	 */
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
