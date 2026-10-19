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

package com.epam.reportportal.base.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Paged response: re-implementation of Spring HATEOAS-style page without those dependencies in the model layer.
 *
 * @param <T> page content element type
 * @author Andrei Varabyeu
 */
@Getter
@ToString
@EqualsAndHashCode
public class Page<T> implements Iterable<T> {

  private final Collection<T> content;
  private final PageMetadata page;

  @JsonCreator
  public Page(@JsonProperty("content") Collection<T> content, @JsonProperty("page") PageMetadata page) {
    this.content = content;
    this.page = page;
  }

  public Page(Collection<T> content, long size, long number, long totalElements, long totalPages) {
    this(content, new PageMetadata(size, number, totalElements, totalPages));
  }

  public Page(Collection<T> content, long size, long number, long totalElements) {
    this(content, new PageMetadata(size, number, totalElements));
  }

  @Override
  public Iterator<T> iterator() {
    return content == null ? Collections.emptyIterator() : content.iterator();
  }

  @Getter
  @ToString
  @EqualsAndHashCode
  @JsonInclude(Include.NON_NULL)
  public static class PageMetadata {

    private long number;
    private long size;
    private Long totalElements;
    private Long totalPages;
    private Boolean hasNext;

    /**
     * Canonical creator for Jackson.
     */
    @JsonCreator
    public PageMetadata(
        @JsonProperty("size") long size,
        @JsonProperty("number") long number,
        @JsonProperty("totalElements") Long totalElements,
        @JsonProperty("totalPages") Long totalPages,
        @JsonProperty("hasNext") Boolean hasNext) {
      checkArgument(size > -1, "Size must not be negative!");
      checkArgument(number > -1, "Number must not be negative!");
      this.size = size;
      this.number = number;
      this.totalElements = totalElements;
      this.totalPages = totalPages;
      this.hasNext = hasNext;
    }

    /**
     * Offset-based pagination with explicit total pages.
     */
    public PageMetadata(long size, long number, long totalElements, long totalPages) {
      this(size, number, totalElements, totalPages, null);
      checkArgument(totalElements > -1, "Total elements must not be negative!");
      checkArgument(totalPages > -1, "Total pages must not be negative!");
    }

    /**
     * Offset-based pagination; total pages computed.
     */
    public PageMetadata(long size, long number, long totalElements) {
      this(size, number, totalElements, size == 0 ? 0 : (long) Math.ceil((double) totalElements / (double) size));
    }

    /**
     * Cursor/slice-based pagination.
     */
    public PageMetadata(long size, long number, boolean hasNext) {
      this(size, number, null, null, hasNext);
    }

    private static void checkArgument(boolean expression, String msg) {
      if (!expression) {
        throw new IllegalArgumentException(msg);
      }
    }
  }
}
