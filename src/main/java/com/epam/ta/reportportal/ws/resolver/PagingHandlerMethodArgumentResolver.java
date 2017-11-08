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

package com.epam.ta.reportportal.ws.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

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
	public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) {
		Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
		if (0 == pageable.getPageSize()) {
			return new PageRequest(pageable.getPageNumber(), DEFAULT_PAGE_SIZE, pageable.getSort());
		} else if (MAX_PAGE_SIZE < pageable.getPageSize()) {
			return new PageRequest(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
		}
		return pageable;
	}

}
