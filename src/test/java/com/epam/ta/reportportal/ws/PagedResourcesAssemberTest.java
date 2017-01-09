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
 
package com.epam.ta.reportportal.ws;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.epam.ta.BaseTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.ws.converter.LaunchResourceAssembler;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;

/**
 * Unit test for paged resource assembler
 * 
 * @author Andrei Varabyeu
 * 
 */
@SpringFixture("unitTestsProjectTriggers")
public class PagedResourcesAssemberTest  extends BaseTest {

	private static final String REQUEST_URI = "/default_project/item";

	private static final String QUERY_STRING_PATTERN = "page.sort=start_time&page.sort.dir=DESC&filter.ex.parent=false&page.page=%d&page.size=%d";

	private static final String CURRENT_PAGE_PARAMETER = "page.page";

	private static final String PAGE_SIZE_PARAMETER = "page.size";

	@Autowired
	protected LaunchRepository launchRepository;

	@Autowired
	protected LaunchResourceAssembler launchResourceAssembler;

	@Autowired
	protected ProjectRepository projectRepository;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	private void prepareRequestContext(int currentPage, int currentPageSize) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(REQUEST_URI);
		request.setParameter(CURRENT_PAGE_PARAMETER, String.valueOf(currentPage));
		request.setParameter(PAGE_SIZE_PARAMETER, String.valueOf(currentPageSize));

		request.setQueryString(String.format(QUERY_STRING_PATTERN, currentPage, currentPageSize));
		ServletRequestAttributes attributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(attributes);
	}

	@Test
	public void testFirst() {

		int currentPage = 1;
		int pageSize = 2;
		prepareRequestContext(currentPage, pageSize);

		Page<Launch> savedLaunch = launchRepository.findAll(new PageRequest(currentPage - 1, pageSize));

		for (Launch l : savedLaunch) {
			LoggerFactory.getLogger(getClass()).warn("Looking via ORM");
			projectRepository.findOne(l.getProjectRef());
		}

		PagedResources<LaunchResource> pagedResources = launchResourceAssembler.toPagedResources(savedLaunch);

		/*
		 * Test First
		 */
		Assert.assertThat(pagedResources.getLink(Link.REL_FIRST), nullValue());

		/*
		 * Test Previous
		 */
		Assert.assertThat(pagedResources.getLink(Link.REL_PREVIOUS), nullValue());

		/*
		 * Test Current
		 */
		Assert.assertThat(pagedResources.getMetadata().getNumber(), is(1L));
		Assert.assertThat(pagedResources.getId().getHref(), endsWith(String.format(QUERY_STRING_PATTERN, currentPage, pageSize)));

		/*
		 * Test Next
		 */
		Assert.assertThat(pagedResources.getLink(Link.REL_NEXT).getHref(),
				endsWith(String.format(QUERY_STRING_PATTERN, currentPage + 1, pageSize)));

		/*
		 * Test Last
		 */
		Assert.assertThat(pagedResources.getLink(Link.REL_LAST).getHref(),
				endsWith(String.format(QUERY_STRING_PATTERN, (launchRepository.count() - 2), pageSize)));
	}

	@Test
	public void testLast() {
		long realSize = launchRepository.count();

		int currentPage = ((int) realSize);
		int pageSize = 1;
		prepareRequestContext(currentPage, pageSize);

		Page<Launch> savedLaunch = launchRepository.findAll(new PageRequest(currentPage - 1, 1));
		PagedResources<LaunchResource> pagedResources = launchResourceAssembler.toPagedResources(savedLaunch);

		/*
		 * Test First
		 */
		Assert.assertThat(pagedResources.getLink(Link.REL_FIRST).getHref(), endsWith(String.format(QUERY_STRING_PATTERN, 1, pageSize)));

		/*
		 * Test Previous
		 */
		Assert.assertThat(pagedResources.getLink(Link.REL_PREVIOUS).getHref(),
				endsWith(String.format(QUERY_STRING_PATTERN, currentPage - 1, pageSize)));

		/*
		 * Test Current
		 */
		Assert.assertThat(pagedResources.getMetadata().getNumber(), is(realSize));
		Assert.assertThat(pagedResources.getId().getHref(), endsWith(String.format(QUERY_STRING_PATTERN, currentPage, pageSize)));

		/*
		 * Test Next
		 */
		Assert.assertThat(pagedResources.getLink(Link.REL_NEXT), nullValue());

		/*
		 * Test Last
		 */
		Assert.assertThat(pagedResources.getLink(Link.REL_LAST), nullValue());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegative() {
		launchRepository.findAll(new PageRequest(-1, 1));
	}
}