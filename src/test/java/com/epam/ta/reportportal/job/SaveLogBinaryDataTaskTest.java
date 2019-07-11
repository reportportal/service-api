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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.core.log.impl.CreateAttachmentHandler;
import com.epam.ta.reportportal.core.log.impl.SaveLogBinaryDataTask;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.Charset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class SaveLogBinaryDataTaskTest {

	@Mock
	private DataStoreService dataStoreService;

	@Mock
	private CreateAttachmentHandler createAttachmentHandler;

	@InjectMocks
	private SaveLogBinaryDataTask saveLogBinaryDataTask;

	@Test
	void saveBinaryDataPositive() {
		long logId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "filename", "text/plain", "some data".getBytes(Charset.forName("UTF-8")));
		long projectId = 2L;
		SaveLogBinaryDataTask saveLogBinaryDataTask = this.saveLogBinaryDataTask.withLogId(logId).withProjectId(projectId).withFile(file);
		BinaryDataMetaInfo binaryData = new BinaryDataMetaInfo("fileId", "thumbnailId", "text/plain");

		when(dataStoreService.save(any(), any())).thenReturn(Optional.of(binaryData));

		saveLogBinaryDataTask.run();

		verify(dataStoreService, times(1)).save(projectId, file);
		verify(createAttachmentHandler, times(1)).create(any(), eq(logId));

	}

	@Test
	void saveBinaryDataNegative() {
		long logId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "filename", "text/plain", "some data".getBytes(Charset.forName("UTF-8")));
		long projectId = 2L;
		SaveLogBinaryDataTask saveLogBinaryDataTask = this.saveLogBinaryDataTask.withLogId(logId).withProjectId(projectId).withFile(file);
		BinaryDataMetaInfo binaryData = new BinaryDataMetaInfo("fileId", "thumbnailId", "text/plain");

		when(dataStoreService.save(any(), any())).thenReturn(Optional.of(binaryData));
		doThrow(ReportPortalException.class).when(createAttachmentHandler).create(any(), eq(logId));

		assertThrows(ReportPortalException.class, saveLogBinaryDataTask::run);

		verify(dataStoreService, times(2)).delete(any());
	}
}