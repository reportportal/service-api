/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

class PageUtil {

	private static final int DEFAULT_PAGE_SIZE = 50;

	/**
	 * Iterates over all pages found
	 *
	 * @param getFunc  Get Page function
	 * @param consumer Page processor
	 * @param <T>      Type of page entity
	 */
	static <T> void iterateOverPages(Function<Pageable, Page<T>> getFunc, Consumer<List<T>> consumer) {
		iterateOverPages(DEFAULT_PAGE_SIZE, getFunc, consumer);
	}

	/**
	 * Iterates over all pages found
	 *
	 * @param pageSize page size
	 * @param getFunc  Get Page function
	 * @param consumer Page processor
	 * @param <T>      Type of page entity
	 */
	static <T> void iterateOverPages(int pageSize, Function<Pageable, Page<T>> getFunc, Consumer<List<T>> consumer) {
		//first page
		Page<T> pageData = getFunc.apply(new PageRequest(0, pageSize));
		List<T> content = pageData.getContent();
		consumer.accept(content);

		while (!pageData.isLast()) {
			pageData = getFunc.apply(pageData.nextPageable());
			consumer.accept(pageData.getContent());
		}
	}
}
