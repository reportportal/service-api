/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.ws.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Nonnull;

/**
 * Added to avoid issue with page size == 0 (in this case repository layer tries
 * to retrieve all results from database) and page size greater than 300.
 *
 * @author Andrei Varabyeu
 */
public class PagingHandlerMethodArgumentResolver extends org.springframework.data.web.PageableHandlerMethodArgumentResolver {

	public PagingHandlerMethodArgumentResolver() {
		super();
	}

	public PagingHandlerMethodArgumentResolver(SortHandlerMethodArgumentResolver sortResolver) {
		super(sortResolver);
	}

	public static final int DEFAULT_PAGE_SIZE = 50;
	public static final int MAX_PAGE_SIZE = 300;

	@Override
	@Nonnull
	public Pageable resolveArgument(MethodParameter methodParameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
		Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
		if (0 == pageable.getPageSize()) {
			return PageRequest.of(pageable.getPageNumber(), DEFAULT_PAGE_SIZE, pageable.getSort());
		} else if (MAX_PAGE_SIZE < pageable.getPageSize()) {
			return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
		}
		return pageable;
	}

}
