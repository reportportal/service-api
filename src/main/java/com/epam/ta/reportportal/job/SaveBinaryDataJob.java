package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.store.service.DataStoreService;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Save binary data job. Expected to be executed asynchronously. Statefull, so
 * cannot be a singleton bean. Saves binary data, then updates related log entry
 * with saved data id
 *
 * @author Andrei Varabyeu
 */
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
	 * {@link Log} entry related to this binary data
	 */
	private Log log;

	private String projectName;

	@Override
	public void run() {

		Optional<BinaryDataMetaInfo> maybeBinaryDataMetaInfo = dataStoreService.save(projectName, file);

		maybeBinaryDataMetaInfo.ifPresent(binaryDataMetaInfo -> {

			log.setContentType(file.getContentType());
			log.setFilePath(binaryDataMetaInfo.getFileId());
			log.setThumbnailFilePath(binaryDataMetaInfo.getThumbnailFileId());

			try {

				logRepository.save(log);
			} catch (RuntimeException e) {

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

	public SaveBinaryDataJob withLog(Log log) {
		Preconditions.checkNotNull(log, "Log shouldn't be null");
		this.log = log;
		return this;
	}

	public SaveBinaryDataJob withProjectName(String projectName) {
		Preconditions.checkArgument(StringUtils.isNotEmpty(projectName), "Project name shouldn't be null");
		this.projectName = projectName;
		return this;
	}
}
