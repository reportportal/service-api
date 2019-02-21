package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.dao.LogRepository;
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
class SaveBinaryDataJobTest {

	@Mock
	private LogRepository logRepository;

	@Mock
	private DataStoreService dataStoreService;

	@InjectMocks
	private SaveBinaryDataJob saveBinaryDataJob;

	@Test
	void saveBinaryDataPositive() {
		long logId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "filename", "text/plain", "some data".getBytes(Charset.forName("UTF-8")));
		SaveBinaryDataJob saveBinaryDataJob = this.saveBinaryDataJob.withLogId(logId).withProjectId(2L).withFile(file);
		Log log = new Log();
		BinaryDataMetaInfo binaryData = new BinaryDataMetaInfo("fileId", "thumbnailId");

		when(logRepository.findById(logId)).thenReturn(Optional.of(log));
		when(dataStoreService.save(any(), any())).thenReturn(Optional.of(binaryData));

		saveBinaryDataJob.run();

		assertEquals(binaryData.getFileId(), log.getAttachment());
		assertEquals(binaryData.getThumbnailFileId(), log.getAttachmentThumbnail());
		assertEquals(file.getContentType(), log.getContentType());
		verify(logRepository, times(1)).save(log);
	}

	@Test
	void saveBinaryDataNegative() {
		long logId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "filename", "text/plain", "some data".getBytes(Charset.forName("UTF-8")));
		SaveBinaryDataJob saveBinaryDataJob = this.saveBinaryDataJob.withLogId(logId).withProjectId(2L).withFile(file);
		Log log = new Log();
		BinaryDataMetaInfo binaryData = new BinaryDataMetaInfo("fileId", "thumbnailId");

		when(logRepository.findById(logId)).thenReturn(Optional.of(log));
		when(dataStoreService.save(any(), any())).thenReturn(Optional.of(binaryData));
		when(logRepository.save(any())).thenThrow(ReportPortalException.class);

		assertThrows(ReportPortalException.class, saveBinaryDataJob::run);

		verify(dataStoreService, times(2)).delete(any());
	}

	@Test
	void logNotFoundTest() {
		long logId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "filename", "text/plain", "some data".getBytes(Charset.forName("UTF-8")));
		SaveBinaryDataJob saveBinaryDataJob = this.saveBinaryDataJob.withLogId(logId).withProjectId(2L).withFile(file);
		BinaryDataMetaInfo binaryData = new BinaryDataMetaInfo("fileId", "thumbnailId");

		when(logRepository.findById(logId)).thenReturn(Optional.empty());
		when(dataStoreService.save(any(), any())).thenReturn(Optional.of(binaryData));

		ReportPortalException exception = assertThrows(ReportPortalException.class, saveBinaryDataJob::run);
		assertEquals("Log '1' not found. Did you use correct Log ID?", exception.getMessage());

		verify(dataStoreService, times(2)).delete(any());
	}
}