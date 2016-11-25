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

import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Extension of Spring's {@link ResourceAssemblerSupport}. Adds possibility to
 * converter {@link Page} resources which is basically arrays of entities with
 * pageable information like current page, max pages, total items, etc
 * 
 * @author Andrei Varabyeu
 * 
 * @param <T>
 *            - Type of Entity to be converted
 * @param <R>
 *            - Type of Resource to be created from entity
 */
public abstract class PagedResourcesAssember<T, R extends ResourceSupport> extends ResourceAssemblerSupport<T, R> {

	private static final String PAGE_PARAMETER = "page.page";
	private static final String SIZE_PARAMETER = "page.size";

	public PagedResourcesAssember(Class<?> controllerClass, Class<R> resourceType) {
		super(controllerClass, resourceType);
	}

	/**
	 * Creates {@link PagedResources} from {@link Page} DB query result
	 * 
	 * @param content
	 * @return
	 */
	public PagedResources<R> toPagedResources(Page<T> content) {
		Assert.notNull(content);

		PagedResources<R> pagedResources = new PagedResources<>(toResources(content), new PageMetadata(content.getSize(),
				content.getNumber() + 1, content.getTotalElements(), content.getTotalPages()));

		addPagingLinks(pagedResources, content);
		return pagedResources;
	}

	protected void addPagingLinks(PagedResources<R> pageResource, Page<T> page) {
		addSelfLink(pageResource);
		addFirstLink(pageResource, page, PAGE_PARAMETER, SIZE_PARAMETER);
		addPreviousLink(pageResource, page, PAGE_PARAMETER, SIZE_PARAMETER);
		addNextLink(pageResource, page, PAGE_PARAMETER, SIZE_PARAMETER);
		addLastLink(pageResource, page, PAGE_PARAMETER, SIZE_PARAMETER);
	}

	private void addSelfLink(PagedResources<R> paged) {
		paged.add(new Link(createBuilder().build().toUriString(), Link.REL_SELF));
	}

	private void addPreviousLink(PagedResources<R> paged, Page<T> page, String pageParam, String sizeParam) {
		if (page.hasPrevious()) {
			paged.add(buildPageLink(pageParam, page.getNumber(), sizeParam, page.getSize(), Link.REL_PREVIOUS));
		}
	}

	private void addNextLink(PagedResources<R> paged, Page<T> page, String pageParam, String sizeParam) {
		if (page.hasNext()) {
			paged.add(buildPageLink(pageParam, page.getNumber() + 2, sizeParam, page.getSize(), Link.REL_NEXT));
		}
	}

	private void addFirstLink(PagedResources<R> paged, Page<T> page, String pageParam, String sizeParam) {
		if (!page.isFirst()) {
			paged.add(buildPageLink(pageParam, 1, sizeParam, page.getSize(), Link.REL_FIRST));
		}
	}

	private void addLastLink(PagedResources<R> paged, Page<T> page, String pageParam, String sizeParam) {
		if (!page.isLast()) {
			paged.add(buildPageLink(pageParam, page.getTotalPages(), sizeParam, page.getSize(), Link.REL_LAST));
		}
	}

	private Link buildPageLink(String pageParam, int page, String sizeParam, int size, String rel) {
		String path = createBuilder().replaceQueryParam(pageParam, page).replaceQueryParam(sizeParam, size).build().toUriString();
		return new Link(path, rel);
	}

	private ServletUriComponentsBuilder createBuilder() {
		return ServletUriComponentsBuilder.fromCurrentRequest();
	}
}