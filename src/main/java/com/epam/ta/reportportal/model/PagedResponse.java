/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import jakarta.validation.constraints.NotNull;

/**
 * Paged representation.
 *
 * @param <T> Type of items
 */
@JsonInclude(Include.NON_NULL)
public class PagedResponse<T> {

  @NotNull
  @JsonProperty(value = "offset")
  private Long offset;

  @NotNull
  @JsonProperty(value = "limit")
  private Integer limit;

  @NotNull
  @JsonProperty(value = "total_count")
  private Long totalCount;

  @NotNull
  @JsonProperty(value = "sort")
  private String sort;

  @NotNull
  @JsonProperty(value = "order")
  private String order;

  @NotNull
  @JsonProperty(value = "items")
  private List<T> items;

  public PagedResponse() {
  }

  public PagedResponse(Long offset, Integer limit, Long totalCount, String sort, String order,
      List<T> items) {
    this.offset = offset;
    this.limit = limit;
    this.totalCount = totalCount;
    this.sort = sort;
    this.order = order;
    this.items = items;
  }

  public Long getOffset() {
    return offset;
  }

  public void setOffset(Long offset) {
    this.offset = offset;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public Long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Long totalCount) {
    this.totalCount = totalCount;
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public List<T> getItems() {
    return items;
  }

  public void setItems(List<T> items) {
    this.items = items;
  }

}
