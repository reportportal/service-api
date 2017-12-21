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

package com.epam.ta.reportportal.ws.converter;

import com.google.common.base.Preconditions;
import org.springframework.data.domain.Page;

import java.util.function.Function;

/**
 * Replacement of Spring's ResourceAssemblerSupport. Adds possibility to
 * converter {@link Page} resources which is basically arrays of entities with
 * pageable information like current page, max pages, total items, etc
 *
 * @param <T> - Type of Entity to be converted
 * @param <R> - Type of Resource to be created from entity
 * @author Andrei Varabyeu
 */
public abstract class PagedResourcesAssembler<T, R> extends ResourceAssembler<T, R> {

	public static <T> Function<Page<T>, com.epam.ta.reportportal.ws.model.Page<T>> pageConverter() {
		return page -> new com.epam.ta.reportportal.ws.model.Page<>(page.getContent(),
				new com.epam.ta.reportportal.ws.model.Page.PageMetadata(page.getSize(), page.getNumber() + 1, page.getTotalElements(),
						page.getTotalPages()
				)
		);
	}

	public static <T, R> Function<Page<T>, com.epam.ta.reportportal.ws.model.Page<R>> pageConverter(Function<T, R> modelConverter) {
		return page -> PagedResourcesAssembler.<R>pageConverter().apply(page.map(modelConverter::apply));
	}

	/**
	 * Creates {@link com.epam.ta.reportportal.ws.model.Page} from {@link Page} DB query result
	 *
	 * @param content Page to be processed
	 * @return Transformed Page
	 * @deprecated in favor of using converters based on JDK8 Functions
	 */
	@Deprecated
	public com.epam.ta.reportportal.ws.model.Page<R> toPagedResources(Page<T> content) {
		Preconditions.checkNotNull(content, "Content should be null");

		return new com.epam.ta.reportportal.ws.model.Page<>(toResources(content),
				new com.epam.ta.reportportal.ws.model.Page.PageMetadata(content.getSize(), content.getNumber() + 1,
						content.getTotalElements(), content.getTotalPages()
				)
		);
	}

}
