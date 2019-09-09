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

package com.epam.ta.reportportal.store.service;

import com.epam.reportportal.commons.Thumbnailator;
import com.epam.ta.reportportal.binary.impl.DataStoreServiceImpl;
import com.epam.ta.reportportal.filesystem.DataEncoder;
import com.epam.ta.reportportal.filesystem.DataStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Dzianis_Shybeka
 */
@ExtendWith(MockitoExtension.class)
class DataStoreServiceTest {

	@Mock
	private DataStore dataStore;

	@Mock
	private Thumbnailator thumbnailator;

	@Mock
	private DataEncoder dataEncoder;

	@InjectMocks
	private DataStoreServiceImpl dataStoreService;

	@Test
	void saveTest() throws Exception {
		//  given:
		MultipartFile file = mock(MultipartFile.class);

		//  and: setups
		when(dataStore.save("fileName", file.getInputStream())).thenReturn("filePath");
		when(dataEncoder.encode("filePath")).thenReturn("fileId");

		//  when:
		String fileId = dataStoreService.save("fileName", file.getInputStream());

		assertEquals("fileId", fileId);
	}

	@Test
	void saveThumbnailTest() throws IOException {
		MultipartFile file = mock(MultipartFile.class);

		when(dataStore.save("fileName", file.getInputStream())).thenReturn("thumbnailPath");
		when(dataEncoder.encode("thumbnailPath")).thenReturn("thumbnailId");

		assertEquals("thumbnailId", dataStoreService.saveThumbnail("fileName", file.getInputStream()));
	}

	@Test
	void saveThumbnailWithException() throws IOException {
		MultipartFile file = mock(MultipartFile.class);

		when(thumbnailator.createThumbnail(file.getInputStream())).thenThrow(IOException.class);

		assertNull(dataStoreService.saveThumbnail("fileName", file.getInputStream()));
	}

	@Test
	void deleteTest() {
		when(dataEncoder.decode("fileId")).thenReturn("filePath");

		dataStoreService.delete("fileId");

		verify(dataStore, times(1)).delete("filePath");
	}

	@Test
	void loadTest() {
		InputStream inputStream = mock(InputStream.class);

		when(dataEncoder.decode("fileId")).thenReturn("filePath");
		when(dataStore.load("filePath")).thenReturn(inputStream);

		Optional<InputStream> content = dataStoreService.load("fileId");

		assertTrue(content.isPresent());
		assertSame(inputStream, content.get());
	}
}
