package com.epam.ta.reportportal.ws.param;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class HistoryRequestParams {

	private int historyDepth;
	private Long parentId;
	private Long itemId;
	private Long launchId;
	private FilterParams filterParams;

	private HistoryRequestParams(int historyDepth, Long parentId, Long itemId, Long launchId, Long filterId, int launchesLimit,
			boolean isLatest) {
		this.historyDepth = historyDepth;
		this.parentId = parentId;
		this.itemId = itemId;
		this.launchId = launchId;
		ofNullable(filterId).ifPresent(id -> this.filterParams = FilterParams.of(filterId, launchesLimit, isLatest));
	}

	public static final class FilterParams {
		private Long filterId;
		private int launchesLimit;
		private boolean isLatest;

		private FilterParams(Long filterId, int launchesLimit, boolean isLatest) {
			this.filterId = filterId;
			this.launchesLimit = launchesLimit;
			this.isLatest = isLatest;
		}

		public Long getFilterId() {
			return filterId;
		}

		public int getLaunchesLimit() {
			return launchesLimit;
		}

		public boolean isLatest() {
			return isLatest;
		}

		public static FilterParams of(Long filterId, int launchesLimit, boolean isLatest) {
			return new FilterParams(filterId, launchesLimit, isLatest);
		}

	}

	public int getHistoryDepth() {
		return historyDepth;
	}

	public Optional<FilterParams> getFilterParams() {
		return ofNullable(filterParams);
	}

	public Optional<Long> getParentId() {
		return ofNullable(parentId);
	}

	public Optional<Long> getItemId() {
		return ofNullable(itemId);
	}

	public Optional<Long> getLaunchId() {
		return ofNullable(launchId);
	}

	public static HistoryRequestParams of(int historyDepth, Long parentId, Long itemId, Long launchId, Long filterId, int launchesLimit,
			boolean isLatest) {
		return new HistoryRequestParams(historyDepth, parentId, itemId, launchId, filterId, launchesLimit, isLatest);
	}
}
