/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.store.service;

import com.epam.reportportal.commons.ContentTypeResolver;
import com.epam.reportportal.commons.Thumbnailator;
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.filesystem.DataEncoder;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.filesystem.FilePathGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Dzianis_Shybeka
 */
class DataStoreServiceTest {

	private DataStore dataStore;

	private Thumbnailator thumbnailator;

	private ContentTypeResolver contentTypeResolver;

	private FilePathGenerator filePathGenerator;

	private DataEncoder dataEncoder;

	private DataStoreService dataStoreService;

	@BeforeEach
	void setUp() {

		dataStore = mock(DataStore.class);
		thumbnailator = mock(Thumbnailator.class);
		contentTypeResolver = mock(ContentTypeResolver.class);
		filePathGenerator = mock(FilePathGenerator.class);
		dataEncoder = mock(DataEncoder.class);

		dataStoreService = new DataStoreService(dataStore, thumbnailator, contentTypeResolver, filePathGenerator, dataEncoder);
	}

	@Test
	void save_empty_on_file_io_error() throws Exception {
		//  given:
		MultipartFile file = mock(MultipartFile.class);

		//  and: setups
		when(file.getInputStream()).thenThrow(new IOException());

		//  when:
		Optional<BinaryDataMetaInfo> maybeResult = dataStoreService.save(123L, file);

		//  then:
		assertFalse(maybeResult.isPresent());
	}

	@Test
	void save_use_type_resolver() throws Exception {
		//  given:
		MultipartFile file = mock(MultipartFile.class);
		Long projectId = 123L;

		//  and: setups
		ByteArrayInputStream fileInputStream = new ByteArrayInputStream("test".getBytes(Charset.forName("UTF-8")));
		when(file.getInputStream()).thenReturn(fileInputStream);
		String fileName = "file.test";
		when(file.getOriginalFilename()).thenReturn(fileName);

		String resolvedContentType = "resolved-type";
		when(contentTypeResolver.detectContentType(fileInputStream)).thenReturn(resolvedContentType);

		String generatedFilePath = File.separator + "test" + File.separator + "path";
		when(filePathGenerator.generate()).thenReturn(generatedFilePath);

		String expectedFilePath = projectId + generatedFilePath + File.separator + fileName;
		when(dataStore.save(expectedFilePath, fileInputStream)).thenReturn(expectedFilePath);

		String fileId = "expected-encoded-file-id";
		when(dataEncoder.encode(expectedFilePath)).thenReturn(fileId);

		//  when:
		Optional<BinaryDataMetaInfo> maybeResult = dataStoreService.save(projectId, file);

		//  then:
		assertTrue(maybeResult.isPresent());
		assertEquals(fileId, maybeResult.get().getFileId());
		assertNull(maybeResult.get().getThumbnailFileId());
	}

	@Test
	void save_does_not_use_type_resolver() throws Exception {
		//  given:
		MultipartFile file = mock(MultipartFile.class);
		Long projectId = 132L;

		//  and: setups
		ByteArrayInputStream fileInputStream = new ByteArrayInputStream("test".getBytes(Charset.forName("UTF-8")));
		when(file.getInputStream()).thenReturn(fileInputStream);
		when(file.getContentType()).thenReturn("test-content-type");
		String fileName = "file.test";
		when(file.getOriginalFilename()).thenReturn(fileName);

		String generatedFilePath = File.separator + "test" + File.separator + "path2";
		when(filePathGenerator.generate()).thenReturn(generatedFilePath);

		String expectedFilePath = projectId + generatedFilePath + File.separator + fileName;
		when(dataStore.save(expectedFilePath, fileInputStream)).thenReturn(expectedFilePath);

		String fileId = "expected-encoded-file-id2";
		when(dataEncoder.encode(expectedFilePath)).thenReturn(fileId);

		//  when:
		Optional<BinaryDataMetaInfo> maybeResult = dataStoreService.save(projectId, file);

		//  then:
		assertTrue(maybeResult.isPresent());
		assertEquals(fileId, maybeResult.get().getFileId());
		assertNull(maybeResult.get().getThumbnailFileId());

		//  and:
		verifyZeroInteractions(contentTypeResolver);
	}

	@Test
	void save_does_not_use_type_resolver_with_thumbnail() throws Exception {
		//  given:
		MultipartFile file = mock(MultipartFile.class);
		Long projectId = 123L;

		//  and: setups
		ByteArrayInputStream fileInputStream = new ByteArrayInputStream("test".getBytes(Charset.forName("UTF-8")));
		ByteArrayInputStream thumbnailFileInputStream = new ByteArrayInputStream("thumbnail-test".getBytes(Charset.forName("UTF-8")));
		when(file.getInputStream()).thenReturn(fileInputStream);
		when(file.getContentType()).thenReturn("image/png");
		String fileName = "file.test";
		when(file.getOriginalFilename()).thenReturn(fileName);
		when(file.getName()).thenReturn(fileName);

		String generatedFilePath = File.separator + "test" + File.separator + "path2";
		when(filePathGenerator.generate()).thenReturn(generatedFilePath);

		when(thumbnailator.createThumbnail(file.getInputStream())).thenReturn(thumbnailFileInputStream);

		//  save thumbnail file
		String expectedThumbnailFilePath = projectId + generatedFilePath + File.separator + "thumbnail-" + fileName;
		when(dataStore.save(expectedThumbnailFilePath, thumbnailFileInputStream)).thenReturn(expectedThumbnailFilePath);

		String thumbnailFileId = "thumbnail-encoded-file-id2";
		when(dataEncoder.encode(expectedThumbnailFilePath)).thenReturn(thumbnailFileId);

		//  save original file
		String expectedFilePath = projectId + generatedFilePath + File.separator + fileName;
		when(dataStore.save(expectedFilePath, fileInputStream)).thenReturn(expectedFilePath);

		String fileId = "expected-encoded-file-id2";
		when(dataEncoder.encode(expectedFilePath)).thenReturn(fileId);

		//  when:
		Optional<BinaryDataMetaInfo> maybeResult = dataStoreService.save(projectId, file);

		//  then:
		assertTrue(maybeResult.isPresent());
		assertEquals(fileId, maybeResult.get().getFileId());
		assertEquals(thumbnailFileId, maybeResult.get().getThumbnailFileId());

		//  and:
		verifyZeroInteractions(contentTypeResolver);
	}

	@Test
	void save_with_fail_on_thumbnail() throws Exception {
		//  given:
		MultipartFile file = mock(MultipartFile.class);
		Long projectId = 123L;

		//  and: setups
		ByteArrayInputStream fileInputStream = new ByteArrayInputStream("test".getBytes(Charset.forName("UTF-8")));
		when(file.getInputStream()).thenReturn(fileInputStream);
		when(file.getContentType()).thenReturn("image/png");
		String fileName = "file.test";
		when(file.getOriginalFilename()).thenReturn(fileName);
		when(file.getName()).thenReturn(fileName);

		String generatedFilePath = File.separator + "test" + File.separator + "path2";
		when(filePathGenerator.generate()).thenReturn(generatedFilePath);

		when(thumbnailator.createThumbnail(file.getInputStream())).thenThrow(IOException.class);

		//  save original file
		String expectedFilePath = projectId + generatedFilePath + File.separator + fileName;
		when(dataStore.save(expectedFilePath, fileInputStream)).thenReturn(expectedFilePath);

		String fileId = "expected-encoded-file-id2";
		when(dataEncoder.encode(expectedFilePath)).thenReturn(fileId);

		//  when:
		Optional<BinaryDataMetaInfo> maybeResult = dataStoreService.save(projectId, file);

		//  then:
		assertTrue(maybeResult.isPresent());
		assertEquals(fileId, maybeResult.get().getFileId());
		assertNull(maybeResult.get().getThumbnailFileId());

		//  and:
		verifyZeroInteractions(contentTypeResolver);
		verify(dataStore, times(1)).save(anyString(), any(InputStream.class));
	}

	@Test
	void load() throws Exception {
		//  given:
		String fileId = "test-file.id";
		InputStream expectedFile = new ByteArrayInputStream("test-content".getBytes(Charset.forName("UTF-8")));

		//  and: setups
		String expectedFilePath = File.separator + "file" + File.separator + "path.test";
		when(dataEncoder.decode(fileId)).thenReturn(expectedFilePath);

		when(dataStore.load(expectedFilePath)).thenReturn(expectedFile);

		//  when:
		InputStream loaded = dataStoreService.load(fileId);

		//  then:
		assertSame(expectedFile, loaded);
	}

	@Test
	void delete() throws Exception {
		//  given:
		String fileId = "test-file.id";

		//  and: setups
		String expectedFilePath = File.separator + "file" + File.separator + "path.test";
		when(dataEncoder.decode(fileId)).thenReturn(expectedFilePath);

		//  when:
		dataStoreService.delete(fileId);

		//  then:
		verify(dataStore).delete(expectedFilePath);
	}
}
