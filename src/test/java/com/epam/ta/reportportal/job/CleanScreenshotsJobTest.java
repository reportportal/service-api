package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;

import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
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

		when(gridFS.findModifiedLaterAgo(any(), eq(project.getName()), any())).thenReturn(new PageImpl<>(Arrays.asList(grid, grid2)));
		when(projectRepository.findAllIdsAndConfiguration(Mockito.any())).thenReturn(new PageImpl<>(Collections.singletonList(project)));

		cleanScreenshotsJob.execute(null);

		verify(gridFS, times(1)).deleteData(anyListOf(String.class));
		verify(logRepository, times(1)).removeBinaryContent(anyListOf(String.class));
	}

}
