package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Save binary data job. Expected to be executed asynchronously. Statefull, so
 * cannot be a singleton bean. Saves binary data, then updates related log entry
 * with saved data id
 *
 * @author Andrei Varabyeu
 */
@Service("saveBinaryDataJob")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SaveBinaryDataJob implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SaveBinaryDataJob.class);

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private DataStoreService dataStoreService;

	/**
	 * Binary data representation
	 */
	private MultipartFile file;

	/**
	 * {@link Log#id} related to this binary data
	 */
	private Long logId;

	private Long projectId;

	@Override
	@Transactional
	public void run() {

		Optional<BinaryDataMetaInfo> maybeBinaryDataMetaInfo = dataStoreService.save(projectId, file);

		maybeBinaryDataMetaInfo.ifPresent(binaryDataMetaInfo -> {

			Log log = logRepository.findById(logId).orElseThrow(() -> new ReportPortalException(ErrorType.LOG_NOT_FOUND, logId));

			log.setContentType(file.getContentType());
			log.setAttachment(binaryDataMetaInfo.getFileId());
			log.setAttachmentThumbnail(binaryDataMetaInfo.getThumbnailFileId());

			try {

				logRepository.save(log);
			} catch (Exception e) {

				LOGGER.error("Cannot save log to database, remove files ", e);

				dataStoreService.delete(binaryDataMetaInfo.getFileId());
				dataStoreService.delete(binaryDataMetaInfo.getThumbnailFileId());
			}
		});
	}

	public SaveBinaryDataJob withFile(MultipartFile file) {
		Preconditions.checkNotNull(file, "Binary data shouldn't be null");
		this.file = file;
		return this;
	}

	public SaveBinaryDataJob withLogId(Long logId) {
		Preconditions.checkNotNull(logId, "Log id shouldn't be null");
		this.logId = logId;
		return this;
	}

	public SaveBinaryDataJob withProjectId(Long projectId) {
		Preconditions.checkNotNull(projectId, "Project id should not be null");
		this.projectId = projectId;
		return this;
	}
}
