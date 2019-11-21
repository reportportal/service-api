package com.epam.ta.reportportal.ws.param;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Arrays;
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
	private HistoryTypeEnum historyType;
	private FilterParams filterParams;

	private HistoryRequestParams(int historyDepth, Long parentId, Long itemId, Long launchId, String historyType, Long filterId,
			int launchesLimit, boolean isLatest) {
		this.historyDepth = historyDepth;
		this.parentId = parentId;
		this.itemId = itemId;
		this.launchId = launchId;
		ofNullable(historyType).ifPresent(type -> this.historyType = HistoryTypeEnum.fromValue(historyType)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("Wrong history type - '{}'", historyType).get()
				)));
		ofNullable(filterId).ifPresent(id -> this.filterParams = FilterParams.of(filterId, launchesLimit, isLatest));
	}

	public enum HistoryTypeEnum {
		TABLE,
		LINE;

		public static Optional<HistoryTypeEnum> fromValue(String type) {
			return Arrays.stream(HistoryTypeEnum.values()).filter(v -> v.name().equalsIgnoreCase(type)).findFirst();
		}
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

	public Optional<HistoryTypeEnum> getHistoryType() {
		return ofNullable(historyType);
	}

	public static HistoryRequestParams of(int historyDepth, Long parentId, Long itemId, Long launchId, String historyType, Long filterId,
			int launchesLimit, boolean isLatest) {
		return new HistoryRequestParams(historyDepth, parentId, itemId, launchId, historyType, filterId, launchesLimit, isLatest);
	}
}
