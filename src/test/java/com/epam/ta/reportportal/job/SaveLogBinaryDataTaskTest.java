/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.core.log.impl.SaveLogBinaryDataTask;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.Charset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class SaveLogBinaryDataTaskTest {

	@Mock
	private LogRepository logRepository;

	@Mock
	private DataStoreService dataStoreService;

	@Mock
	private AttachmentRepository attachmentRepository;

	@InjectMocks
	private SaveLogBinaryDataTask saveLogBinaryDataTask;

	@Test
	void saveBinaryDataPositive() {
		long logId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "filename", "text/plain", "some data".getBytes(Charset.forName("UTF-8")));
		SaveLogBinaryDataTask saveLogBinaryDataTask = this.saveLogBinaryDataTask.withLogId(logId).withProjectId(2L).withFile(file);
		Log log = new Log();
		BinaryDataMetaInfo binaryData = new BinaryDataMetaInfo("fileId", "thumbnailId", "text/plain");

		when(logRepository.findById(logId)).thenReturn(Optional.of(log));
		when(dataStoreService.save(any(), any())).thenReturn(Optional.of(binaryData));

		saveLogBinaryDataTask.run();

		assertEquals(binaryData.getFileId(), log.getAttachment().getFileId());
		assertEquals(binaryData.getThumbnailFileId(), log.getAttachment().getThumbnailId());
		assertEquals(file.getContentType(), log.getAttachment().getContentType());
		verify(logRepository, times(1)).save(log);
		verify(attachmentRepository, times(1)).save(any(Attachment.class));
	}

	@Test
	void saveBinaryDataNegative() {
		long logId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "filename", "text/plain", "some data".getBytes(Charset.forName("UTF-8")));
		SaveLogBinaryDataTask saveLogBinaryDataTaskAsync = this.saveLogBinaryDataTask.withLogId(logId).withProjectId(2L).withFile(file);
		Log log = new Log();
		BinaryDataMetaInfo binaryData = new BinaryDataMetaInfo("fileId", "thumbnailId", "text/plain");

		when(logRepository.findById(logId)).thenReturn(Optional.of(log));
		when(dataStoreService.save(any(), any())).thenReturn(Optional.of(binaryData));
		when(logRepository.save(any())).thenThrow(ReportPortalException.class);

		assertThrows(ReportPortalException.class, saveLogBinaryDataTask::run);

		verify(dataStoreService, times(2)).delete(any());
		verify(attachmentRepository, times(1)).save(any(Attachment.class));
	}

	@Test
	void logNotFoundTest() {
		long logId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "filename", "text/plain", "some data".getBytes(Charset.forName("UTF-8")));
		SaveLogBinaryDataTask saveLogBinaryDataTask = this.saveLogBinaryDataTask.withLogId(logId).withProjectId(2L).withFile(file);
		BinaryDataMetaInfo binaryData = new BinaryDataMetaInfo("fileId", "thumbnailId", "text/plain");

		when(logRepository.findById(logId)).thenReturn(Optional.empty());
		when(dataStoreService.save(any(), any())).thenReturn(Optional.of(binaryData));

		ReportPortalException exception = assertThrows(ReportPortalException.class, saveLogBinaryDataTask::run);
		assertEquals("Log '1' not found. Did you use correct Log ID?", exception.getMessage());

		verify(dataStoreService, times(2)).delete(any());
	}
}