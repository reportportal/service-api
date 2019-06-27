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

import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.google.common.collect.Lists;
import com.mongodb.gridfs.GridFSDBFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class CleanScreenshotsJobTest {

	@Mock
	private DataStorage gridFS;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private LogRepository logRepository;

	@InjectMocks
	private CleanScreenshotsJob cleanScreenshotsJob = new CleanScreenshotsJob();

	@Test
	public void testCleanScreenshotsJob() {

		String name = "name";
		Project project = new Project();
		Project.Configuration configuration = new Project.Configuration();

		configuration.setKeepScreenshots("1 month");
		project.setName(name);
		project.setConfiguration(configuration);

		GridFSDBFile grid = new GridFSDBFile();
		grid.put("_id", "name");
		grid.put("filename", "photo_1");

		GridFSDBFile grid2 = new GridFSDBFile();
		grid2.put("_id", "not_photo");
		grid2.put("filename", "file123");

		when(gridFS.findFirstModifiedLater(any(), eq(project.getName()), eq(150))).thenReturn(
				Arrays.asList(grid, grid2),
				Lists.newArrayList()
		);
		when(projectRepository.findAllIdsAndConfiguration(Mockito.any())).thenReturn(new PageImpl<>(Collections.singletonList(project)));

		cleanScreenshotsJob.execute(null);

		verify(gridFS, times(1)).deleteData(anyListOf(String.class));
		verify(logRepository, times(1)).removeBinaryContent(anyListOf(String.class));
	}

}
