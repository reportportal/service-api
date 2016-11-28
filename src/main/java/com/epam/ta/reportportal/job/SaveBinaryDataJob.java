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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.entity.BinaryContent;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.reportportal.commons.Thumbnailator;
import com.google.common.base.Preconditions;

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

		String thumbnailId = null;
		Map<String, String> metadata = new HashMap<>();
		metadata.put("project", project);

		if (isImage(binaryData)) {
			try {
				byte[] image = IOUtils.toByteArray(binaryData.getInputStream());
				byte[] thumbnailBytes = thumbnailator.createThumbnail(image);
				thumbnailId = dataStorageService.saveData(new BinaryData(binaryData.getContentType(), (long) thumbnailBytes.length,
						new ByteArrayInputStream(thumbnailBytes)), "thumbnail-".concat(filename), metadata);
				binaryData = new BinaryData(binaryData.getContentType(), binaryData.getLength(), new ByteArrayInputStream(image));
			} catch (IOException e) {
				// do not propogate. Thumbnail is not so critical
				LOGGER.error("Thumbnail is not created for log [{}]. Error:\n{}", log.getId(), e);
			}
		}

		/*
		 * Saves binary data into storage
		 */
		//String dataId = dataStorageService.saveData(binaryData, filename);
		String dataId = dataStorageService.saveData(binaryData, filename, metadata);

		/*
		 * Then updates log with just created binary data id
		 */
		BinaryContent content = new BinaryContent();
		content.setBinaryDataId(dataId);
		content.setContentType(binaryData.getContentType());
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