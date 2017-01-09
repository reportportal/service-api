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

import java.util.List;
import java.util.stream.Collectors;

import com.epam.ta.reportportal.database.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.KeepScreenshotsDelay;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * Clear screenshots from GridFS in accordance with projects settings
 * 
 * @author Andrei_Ramanchuk
 */
@Service
public class CleanScreenshotsJob implements Runnable {

	final static String PHOTO_PREFIX = "photo_";

	@Autowired
	private DataStorage gridFS;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private LogRepository logRepository;

	@Override
	@Scheduled(cron = "${com.ta.reportportal.job.clean.screenshots.cron}")
	public void run() {
		List<Project> projects = projectRepository.findAll();
		for (Project project : projects) {
			Time period = Time.days(KeepScreenshotsDelay.findByName(project.getConfiguration().getKeepScreenshots()).getDays());
			List<GridFSDBFile> files = gridFS.findModifiedLaterAgo(period, project.getId());
			/* Clear binary_content fields from log repository */
			files.stream().filter(file -> !file.getFilename().startsWith(PHOTO_PREFIX)).forEach(file -> {
				gridFS.deleteData(file.getId().toString());
				/* Clear binary_content fields from log repository */
				clearLogsBinaryContent(file.getId().toString());
			});
		}
	}

	private void clearLogsBinaryContent(String fileId) {
		List<Log> logList = logRepository.findLogsByFileId(fileId).stream().map(log -> {
			log.setBinaryContent(null);
			return log;
		}).collect(Collectors.toList());
		try {
			logRepository.save(logList);
		} catch (Exception e) {
			throw new ReportPortalException("Exception during update binary content field of Log item", e);
		}
	}
}