package com.epam.ta.reportportal.ws.handler.impl;

import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
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

public class TestItemFindOneHandlerImplTest {

	@Mock
	private TestItemRepository testItemRepository;

	@InjectMocks
	private TestItemFindOneHandlerImpl testItemFindOneHandler;

	@Before
	public void init() {

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testFindOne_whereEntityNotFound() {

		//given:
		String itemId = "12";

		//setup:
		when(testItemRepository.findById(Long.valueOf(itemId))).thenReturn(Optional.empty());

		//when:
		TestItem response = testItemFindOneHandler.findOne(itemId);

		//then:
		ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		verify(testItemRepository).findById(idCaptor.capture());

		Long capturedId = idCaptor.getValue();

		assertEquals(Long.valueOf(itemId), capturedId);
		assertNull(response);
	}

	@Test
	public void testFindOne_whereEntityIsFound() {

		//given:
		String itemId = "12";
		TestItem expectedItem = new TestItem();

		//setup:
		when(testItemRepository.findById(Long.valueOf(itemId))).thenReturn(Optional.of(expectedItem));

		//when:
		TestItem response = testItemFindOneHandler.findOne(itemId);

		//then:
		ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		verify(testItemRepository).findById(idCaptor.capture());

		Long capturedId = idCaptor.getValue();

		assertEquals(Long.valueOf(itemId), capturedId);
		assertEquals(expectedItem, response);
	}
}