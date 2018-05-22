package com.epam.ta.reportportal.ws.handler.impl;

import com.epam.ta.reportportal.store.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.store.database.entity.external.ExternalSystem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExternalSystemFindOneHandlerImplTest {

	@Mock
	private ExternalSystemRepository externalSystemRepository;

	@InjectMocks
	private ExternalSystemFindOneHandlerImpl externalSystemFindOneHandler;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testFindById_whereEntityNotFound() {

		//given:
		String systemId = "5";

		//setup:
		when(externalSystemRepository.findById(Long.valueOf(systemId))).thenReturn(Optional.empty());

		//when:
		ExternalSystem response = externalSystemFindOneHandler.findById(systemId);

		//then:
		ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		verify(externalSystemRepository).findById(idCaptor.capture());

		Long capturedId = idCaptor.getValue();

		assertEquals(Long.valueOf(systemId), capturedId);
		assertNull(response);
	}

	@Test
	public void testFindById_whereEntityIsFound() {

		//given:
		String systemId = "5";
		ExternalSystem expectedSystem = new ExternalSystem();

		//setup:
		when(externalSystemRepository.findById(Long.valueOf(systemId))).thenReturn(Optional.of(expectedSystem));

		//when:
		ExternalSystem response = externalSystemFindOneHandler.findById(systemId);

		//then:
		ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		verify(externalSystemRepository).findById(idCaptor.capture());

		Long capturedId = idCaptor.getValue();

		assertEquals(Long.valueOf(systemId), capturedId);
		assertEquals(expectedSystem, response);
	}
}