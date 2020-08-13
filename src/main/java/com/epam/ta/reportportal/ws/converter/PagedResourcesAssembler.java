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


package com.epam.ta.reportportal.ws.converter;

import com.google.common.base.Preconditions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
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
				new com.epam.ta.reportportal.ws.model.Page.PageMetadata(page.getSize(), page.getNumber() + 1L, page.getTotalElements(),
						page.getTotalPages()
				)
		);
	}

	public static <T, R> Function<Page<T>, com.epam.ta.reportportal.ws.model.Page<R>> pageConverter(Function<T, R> modelConverter) {
		return page -> PagedResourcesAssembler.<R>pageConverter().apply(page.map(modelConverter));
	}

	public static <T, R> Function<Page<T>, com.epam.ta.reportportal.ws.model.Page<R>> pageMultiConverter(
			Function<List<T>, List<R>> modelConverter) {
		return page -> PagedResourcesAssembler.<R>pageConverter().apply(new PageImpl<>(modelConverter.apply(page.getContent()),
				page.getPageable(),
				page.getTotalElements()
		));
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
				new com.epam.ta.reportportal.ws.model.Page.PageMetadata(content.getSize(), content.getNumber() + 1L,
						content.getTotalElements(), content.getTotalPages()
				)
		);
	}

}
