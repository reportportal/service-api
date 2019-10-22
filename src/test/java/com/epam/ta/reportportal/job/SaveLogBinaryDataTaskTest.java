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

import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.core.log.impl.SaveLogBinaryDataTask;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class SaveLogBinaryDataTaskTest {

	@Mock
	private AttachmentBinaryDataService attachmentBinaryDataService;

	@InjectMocks
	private SaveLogBinaryDataTask saveLogBinaryDataTask;

	@Test
	void saveBinaryDataPositive() {
		long logId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "filename", "text/plain", "some data".getBytes(StandardCharsets.UTF_8));
		long projectId = 2L;
		AttachmentMetaInfo attachmentMetaInfo = AttachmentMetaInfo.builder().withLogId(logId).withProjectId(projectId).build();
		SaveLogBinaryDataTask saveLogBinaryDataTask = this.saveLogBinaryDataTask.withFile(file).withAttachmentMetaInfo(attachmentMetaInfo);

		saveLogBinaryDataTask.run();

		verify(attachmentBinaryDataService, times(1)).saveFileAndAttachToLog(file, attachmentMetaInfo);

	}
}