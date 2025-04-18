/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collection;
import java.util.Iterator;
import lombok.Getter;

/**
 * Paged response  representation Re-implementation of Spring's HATEAOS Page implementation to get
 * rid of Spring's deps in model package
 *
 * @author Andrei Varabyeu
 */
public class Page<T> implements Iterable<T> {

  private final Collection<T> content;
  private final PageMetadata page;

  /**
   * Visible for deserializer
   */
  Page() {
    this(null, null);
  }

  public Page(Collection<T> content, PageMetadata page) {
    this.content = content;
    this.page = page;
  }

  public Page(Collection<T> content, long size, long number, long totalElements, long totalPages) {
    this.content = content;
    this.page = new PageMetadata(size, number, totalElements, totalPages);
  }

  public Page(Collection<T> content, long size, long number, long totalElements) {
    this.content = content;
    this.page = new PageMetadata(size, number, totalElements);
  }

  private static void checkArgument(boolean expression, String errorMessage) {
    if (!expression) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  public Collection<T> getContent() {
    return content;
  }

  public PageMetadata getPage() {
    return page;
  }

  @Override
  public Iterator<T> iterator() {
    return content.iterator();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Page{");
    sb.append("content=").append(content);
    sb.append(", page=").append(page);
    sb.append('}');
    return sb.toString();
  }

  @Getter
  @JsonInclude(Include.NON_NULL)
  public static class PageMetadata {

    long number;
    long size;
    Long totalElements;
    Long totalPages;
    Boolean hasNext;

    /**
     * Visible for deserializer
     */
    PageMetadata() {
    }

    public PageMetadata(Integer number, Integer size, Boolean hasNext) {
      this.number = number;
      this.size = size;
      this.hasNext = hasNext;
    }

    public PageMetadata(long size, long number, long totalElements, long totalPages) {
      checkArgument(size > -1, "Size must not be negative!");
      checkArgument(number > -1, "Number must not be negative!");
      checkArgument(totalElements > -1, "Total elements must not be negative!");
      checkArgument(totalPages > -1, "Total pages must not be negative!");

      this.number = number;
      this.size = size;
      this.totalElements = totalElements;
      this.totalPages = totalPages;

    }

    public PageMetadata(long size, long number, long totalElements) {
      this(size, number, totalElements,
          size == 0 ? 0 : (long) Math.ceil((double) totalElements / (double) size));
    }
  }
}
