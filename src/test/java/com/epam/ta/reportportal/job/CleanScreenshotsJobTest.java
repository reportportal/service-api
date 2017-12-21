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
import com.mongodb.gridfs.GridFSDBFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by Andrey_Ivanov1 on 31-May-17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class CleanScreenshotsJobTest {

	@InjectMocks
	private CleanScreenshotsJob cleanScreenshotsJob = new CleanScreenshotsJob();
	@Mock
	private DataStorage gridFS;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private LogRepository logRepository;

	@Test
	public void runTest() {
		String name = "name";
		Project project = new Project();
		Project.Configuration configuration = new Project.Configuration();
		configuration.setKeepScreenshots("1 week");
		project.setName(name);
		project.setConfiguration(configuration);
		Stream<Project> sp = Stream.of(project);

		GridFSDBFile grid = new GridFSDBFile();
		grid.put("_id", name);
		List<GridFSDBFile> list = new ArrayList<>();
		list.add(grid);

		when(projectRepository.streamAllIdsAndConfiguration()).thenReturn(sp);
		when(gridFS.findModifiedLaterAgo(any(Duration.class), anyString())).thenReturn(list);

		cleanScreenshotsJob.execute(null);

		verify(gridFS, times(1)).deleteData(anyString());
		verify(logRepository, times(1)).removeBinaryContent(anyString());
	}

}
