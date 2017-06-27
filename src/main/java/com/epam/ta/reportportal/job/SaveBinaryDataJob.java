/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */ 

package com.epam.ta.reportportal.job;

import com.epam.reportportal.commons.ContentTypeResolver;
import com.epam.reportportal.commons.Thumbnailator;
import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.entity.BinaryContent;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * Save binary data job. Expected to be executed asynchronously. Statefull, so
 * cannot be a singleton bean. Saves binary data, then updates related log entry
 * with saved data id
 * 
 * @author Andrei Varabyeu
 * 
 */
public class SaveBinaryDataJob implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SaveBinaryDataJob.class);

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private DataStorage dataStorageService;

	@Autowired
	private Thumbnailator thumbnailator;

	@Autowired
	private ContentTypeResolver contentTypeResolver;

	/**
	 * Name of file to be saved
	 */
	private String filename;

	/**
	 * Binary data representation
	 */
	private BinaryData binaryData;
	
	private String project;

	/**
	 * {@link Log} entry related to this binary data
	 */
	private Log log;

	@Override
	public void run() {

		BinaryData toSave;
		if (!Strings.isNullOrEmpty(binaryData.getContentType()) && !MediaType.APPLICATION_OCTET_STREAM_VALUE
				.equals(binaryData.getContentType())) {
			toSave = binaryData;
		} else {
			try {
				byte[] consumedData = ByteStreams.toByteArray(binaryData.getInputStream());
				toSave = new BinaryData(contentTypeResolver.detectContentType(consumedData), binaryData.getLength(),
						new ByteArrayInputStream(consumedData));
			} catch (IOException e) {
				throw new ReportPortalException(ErrorType.BAD_SAVE_LOG_REQUEST, "Unable to read binary data");
			}
		}

		String thumbnailId = null;
		Map<String, String> metadata = Collections.singletonMap("project", project);

		if (isImage(toSave)) {
			try {
				byte[] image = ByteStreams.toByteArray(toSave.getInputStream());
				InputStream thumbnailStream = thumbnailator.createThumbnail(new ByteArrayInputStream(image));
				thumbnailId = dataStorageService.saveData(new BinaryData(toSave.getContentType(), -1L,
						thumbnailStream), "thumbnail-".concat(filename), metadata);
				toSave = new BinaryData(toSave.getContentType(), toSave.getLength(), new ByteArrayInputStream(image));
			} catch (IOException e) {
				// do not propogate. Thumbnail is not so critical
				LOGGER.error("Thumbnail is not created for log [{}]. Error:\n{}", log.getId(), e);
			}
		}

		/*
		 * Saves binary data into storage
		 */
		//String dataId = dataStorageService.saveData(binaryData, filename);
		String dataId = dataStorageService.saveData(toSave, filename, metadata);

		/*
		 * Then updates log with just created binary data id
		 */
		BinaryContent content = new BinaryContent();
		content.setBinaryDataId(dataId);
		content.setContentType(toSave.getContentType());
		if (null != thumbnailId) {
			content.setThumbnailId(thumbnailId);
		}

		/*
		 * Adds thumbnail if created
		 */
		log.setBinaryContent(content);
		logRepository.save(log);
	}

	public SaveBinaryDataJob withBinaryData(BinaryData binaryData) {
		Preconditions.checkNotNull(binaryData, "Binary data shouldn't be null");
		this.binaryData = binaryData;
		return this;
	}

	public SaveBinaryDataJob withLog(Log log) {
		Preconditions.checkNotNull(log, "Log shouldn't be null");
		this.log = log;
		return this;
	}

	public SaveBinaryDataJob withFilename(String filename) {
		Preconditions.checkNotNull(filename, "Filename shouldn't be null");
		this.filename = filename;
		return this;
	}
	
	public SaveBinaryDataJob withProject(String projectName) {
		Preconditions.checkNotNull(projectName, "Project name shouldn't be null");
		this.project = projectName;
		return this;
	}

	private boolean isImage(BinaryData binaryData) {
		return binaryData.getContentType() != null && binaryData.getContentType().contains("image");
	}
}