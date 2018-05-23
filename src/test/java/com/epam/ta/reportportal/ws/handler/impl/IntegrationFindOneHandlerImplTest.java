package com.epam.ta.reportportal.ws.handler.impl;

import com.epam.ta.reportportal.store.database.dao.IntegrationRepository;
import com.epam.ta.reportportal.store.database.entity.integration.Integration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IntegrationFindOneHandlerImplTest {

	@Mock
	private IntegrationRepository integrationRepository;

	@InjectMocks
	private IntegrationFindOneHandlerImpl integrationFindOneHandler;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testFindById_whereEntityNotFound() {

		//given:
		String systemId = "5";

		//setup:
		when(integrationRepository.findById(Long.valueOf(systemId))).thenReturn(Optional.empty());

		//when:
		Integration response = integrationFindOneHandler.findById(systemId);

		//then:
		ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		verify(integrationRepository).findById(idCaptor.capture());

		Long capturedId = idCaptor.getValue();

		assertEquals(Long.valueOf(systemId), capturedId);
		assertNull(response);
	}

	@Test
	public void testFindById_whereEntityIsFound() {

		//given:
		String systemId = "5";
		Integration expectedIntegration = new Integration();

		//setup:
		when(integrationRepository.findById(Long.valueOf(systemId))).thenReturn(Optional.of(expectedIntegration));

		//when:
		Integration response = integrationFindOneHandler.findById(systemId);

		//then:
		ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		verify(integrationRepository).findById(idCaptor.capture());

		Long capturedId = idCaptor.getValue();

		assertEquals(Long.valueOf(systemId), capturedId);
		assertEquals(expectedIntegration, response);
	}
}