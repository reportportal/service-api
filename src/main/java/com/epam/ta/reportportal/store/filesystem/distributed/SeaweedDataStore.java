package com.epam.ta.reportportal.store.filesystem.distributed;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.filesystem.DataStore;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.lokra.seaweedfs.core.FileSource;
import org.lokra.seaweedfs.core.FileTemplate;
import org.lokra.seaweedfs.core.file.FileHandleStatus;
import org.lokra.seaweedfs.core.http.StreamResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class SeaweedDataStore implements DataStore {

	private static final Logger logger = LoggerFactory.getLogger(SeaweedDataStore.class);

	private final FileSource fileSource;

	public SeaweedDataStore(FileSource fileSource) {
		this.fileSource = fileSource;
	}

	@Override
	public String save(String fileName, InputStream inputStream) {

		FileTemplate fileTemplate = new FileTemplate(fileSource.getConnection());
		try {

			FileHandleStatus fileHandleStatus = fileTemplate.saveFileByStream(fileName, inputStream);

			return fileHandleStatus.getFileId();
		} catch (IOException e) {

			logger.error("Unable to save log file ", e);

			throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to save log file");
		}
	}

	@Override
	public InputStream load(String filePath) {

		FileTemplate fileTemplate = new FileTemplate(fileSource.getConnection());
		try {

			StreamResponse fileHandleStatus = fileTemplate.getFileStream(filePath);

			return fileHandleStatus.getInputStream();
		} catch (IOException e) {

			logger.error("Unable to find file ", e);

			throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to find file");
		}
	}

	@Override
	public void delete(String filePath) {

		FileTemplate fileTemplate = new FileTemplate(fileSource.getConnection());
		try {

			fileTemplate.deleteFile(filePath);
		} catch (IOException e) {

			logger.error("Unable to delete file ", e);

			throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to delete file");
		}
	}
}
