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

package com.epam.ta.reportportal.core.item.impl.history.param;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import java.util.Arrays;
import java.util.Optional;

/**
 * NULL-safe container for
 * {@link com.epam.ta.reportportal.ws.controller.TestItemController#getItemsHistory} request params
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class HistoryRequestParams {

  private int historyDepth;
  private Long parentId;
  private Long itemId;
  private Long launchId;
  private HistoryTypeEnum historyType;
  private FilterParams filterParams;

  private HistoryRequestParams(int historyDepth, Long parentId, Long itemId, Long launchId,
      String historyType, Long filterId,
      int launchesLimit, boolean isLatest) {
    this.historyDepth = historyDepth;
    this.parentId = parentId;
    this.itemId = itemId;
    this.launchId = launchId;
    ofNullable(historyType).ifPresent(
        type -> this.historyType = HistoryTypeEnum.fromValue(historyType)
            .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
                Suppliers.formattedSupplier("Wrong history type - '{}'", historyType).get()
            )));
    ofNullable(filterId).ifPresent(
        id -> this.filterParams = FilterParams.of(filterId, launchesLimit, isLatest));
  }

  public enum HistoryTypeEnum {
    TABLE,
    LINE,
    COMPARING;

    public static Optional<HistoryTypeEnum> fromValue(String type) {
      return Arrays.stream(HistoryTypeEnum.values()).filter(v -> v.name().equalsIgnoreCase(type))
          .findFirst();
    }
  }

  /**
   * Container for {@link com.epam.ta.reportportal.ws.controller.TestItemController#getItemsHistory}
   * launch's filter-related request params
   */
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

  public static HistoryRequestParams of(int historyDepth, Long parentId, Long itemId, Long launchId,
      String historyType, Long filterId,
      int launchesLimit, boolean isLatest) {
    return new HistoryRequestParams(historyDepth, parentId, itemId, launchId, historyType, filterId,
        launchesLimit, isLatest);
  }
}
