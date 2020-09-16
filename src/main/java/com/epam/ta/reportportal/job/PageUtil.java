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
package com.epam.ta.reportportal.job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PageUtil {

	private PageUtil() {
		//static only
	}

	private static final int DEFAULT_PAGE_SIZE = 50;

	/**
	 * Iterates over all pages found
	 *
	 * @param getFunc  Get Page function
	 * @param consumer Page processor
	 * @param <T>      Type of page entity
	 */
	public static <T> void iterateOverPages(Sort sort, Function<Pageable, Page<T>> getFunc, Consumer<List<T>> consumer) {
		iterateOverPages(DEFAULT_PAGE_SIZE, sort, getFunc, consumer);
	}

	/**
	 * Iterates over all pages found
	 *
	 * @param pageSize page size
	 * @param getFunc  Get Page function
	 * @param consumer Page processor
	 * @param <T>      Type of page entity
	 */
	public static <T> void iterateOverPages(int pageSize, Sort sort, Function<Pageable, Page<T>> getFunc, Consumer<List<T>> consumer) {
		//first page
		Page<T> pageData = getFunc.apply(PageRequest.of(0, pageSize, sort));
		List<T> content = pageData.getContent();
		consumer.accept(content);

		while (!pageData.isLast()) {
			pageData = getFunc.apply(pageData.nextPageable());
			consumer.accept(pageData.getContent());
		}
	}

	/**
	 * Iterates over all pages found
	 *
	 * @param pageSize page size
	 * @param getFunc  Get {@link List} content function
	 * @param consumer Page processor
	 * @param <T>      Type of {@link List} entity
	 */
	public static <T> void iterateOverContent(int pageSize, Function<Pageable, List<T>> getFunc, Consumer<List<T>> consumer) {
		//first page
		Pageable pageRequest = PageRequest.of(0, pageSize);
		List<T> content = getFunc.apply(pageRequest);
		consumer.accept(content);

		while (content.size() >= pageSize) {
			pageRequest = pageRequest.next();
			content = getFunc.apply(pageRequest);
			consumer.accept(content);
		}
	}

}
