/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.util;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;


@ToString
@EqualsAndHashCode
public class OffsetRequest implements Pageable {

  public static final Sort DEFAULT_SORT = Sort.unsorted();

  private final long offset;
  private final int limit;
  private final Sort sort;

  public OffsetRequest(long offset, int limit) {
    this(offset, limit, DEFAULT_SORT);
  }

  public OffsetRequest(long offset, int limit, Sort sort) {
    if (offset < 0) {
      throw new IllegalArgumentException("Negative offset is not allowed: " + offset);
    }
    this.offset = offset;

    this.limit = limit;

    this.sort = sort != null ? sort : DEFAULT_SORT;
  }

  public static OffsetRequest of(int offset, int limit) {
    return of(offset, limit, DEFAULT_SORT);
  }

  public static OffsetRequest of(int offset, int limit, Sort sort) {
    return new OffsetRequest(offset, limit, sort);
  }

  @Override
  public int getPageNumber() {
    if (limit == 0) {
      return 0;
    }
    return Math.toIntExact(offset / limit);
  }

  @Override
  public int getPageSize() {
    return limit;
  }

  @Override
  public long getOffset() {
    return offset;
  }

  @Override
  @NonNull
  public Sort getSort() {
    return sort;
  }

  @Override
  @NonNull
  public Pageable next() {
    return new OffsetRequest(getOffset() + getPageSize(), getPageSize(), getSort());
  }

  public Pageable previous() {
    return hasPrevious() ? new OffsetRequest(getOffset() - getPageSize(), getPageSize(), getSort())
        : this;
  }

  @Override
  @NonNull
  public Pageable previousOrFirst() {
    return hasPrevious() ? previous() : first();
  }

  @Override
  @NonNull
  public Pageable first() {
    return new OffsetRequest(0, getPageSize(), getSort());
  }

  @Override
  @NonNull
  public Pageable withPage(int pageNumber) {
    return new OffsetRequest((long) pageNumber * getPageSize(), getPageSize(), getSort());
  }

  @Override
  public boolean hasPrevious() {
    return offset > limit;
  }

}
