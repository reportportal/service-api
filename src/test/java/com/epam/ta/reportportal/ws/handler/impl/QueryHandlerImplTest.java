package com.epam.ta.reportportal.ws.handler.impl;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.querygen.Filter;
import com.epam.ta.reportportal.store.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.store.database.dao.IntegrationRepository;
import com.epam.ta.reportportal.store.database.dao.LogRepository;
import com.epam.ta.reportportal.store.database.dao.ProjectRepository;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.log.Log;
import com.epam.ta.reportportal.ws.rabbit.QueryRQ;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Yauheni_Martynau
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryHandlerImplTest {

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private IntegrationRepository integrationRepository;

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private LogRepository logRepository;

	@InjectMocks
	private QueryHandlerImpl queryHandler;

	@Test
	public void testFind_withLogRepositoryRequest() {

		//given:
		Filter requestFilter = Filter.builder()
				.withTarget(Log.class)
				.withCondition(FilterCondition.builder()
						.eq("id", "2")
						.build())
				.build();

		QueryRQ queryRQ = new QueryRQ();
		queryRQ.setEntity(Log.class.getSimpleName());
		queryRQ.setFilter(requestFilter);

		//setup:
		when(logRepository.findByFilter(requestFilter)).thenReturn(Lists.newArrayList());

		//when:
		queryHandler.find(queryRQ);

		//then:
		ArgumentCaptor<Filter> captor = ArgumentCaptor.forClass(Filter.class);
		verify(logRepository).findByFilter(captor.capture());

		Filter capturedFilter = captor.getValue();

		assertEquals(requestFilter, capturedFilter);
	}

	@Test(expected = ReportPortalException.class)
	public void testFind_withNotFoundRepository() {

		//given:
		Filter requestFilter = Filter.builder()
				.withTarget(Launch.class)
				.withCondition(FilterCondition.builder()
						.eq("name", "name")
						.build())
				.build();

		QueryRQ queryRQ = new QueryRQ();
		queryRQ.setEntity(Launch.class.getSimpleName());
		queryRQ.setFilter(requestFilter);

		//when:
		queryHandler.find(queryRQ);
	}
}