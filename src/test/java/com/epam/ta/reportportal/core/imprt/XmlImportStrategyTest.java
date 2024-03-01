package com.epam.ta.reportportal.core.imprt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.imprt.impl.AbstractImportStrategy;
import com.epam.ta.reportportal.core.imprt.impl.ParseResults;
import com.epam.ta.reportportal.core.imprt.impl.XmlImportStrategy;
import com.epam.ta.reportportal.core.imprt.impl.junit.XunitParseJob;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.LaunchImportRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import javax.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class XmlImportStrategyTest {

  @Mock
  private Provider<XunitParseJob> xmlParseJobProvider;

  @Mock
  private StartLaunchHandler startLaunchHandler;

  @Mock
  private FinishLaunchHandler finishLaunchHandler;

  @Mock
  private LaunchRepository launchRepository;

  @InjectMocks
  private XmlImportStrategy xmlImportStrategy;

  private ReportPortalUser.ProjectDetails projectDetails;

  private ReportPortalUser user = mock(ReportPortalUser.class);

  private static final String LAUNCH_ID = "524ec63d-dc77-419f-b3a6-fe82a94a2f6c";

  private static final String BASE_URL = "http://localhost:8080";

  @BeforeEach
  void setUp() {
    projectDetails = mock(ReportPortalUser.ProjectDetails.class);
    user = mock(ReportPortalUser.class);

  }

  @Test
  void whenImportLaunch_thenProcessXmlFile(@TempDir Path tempDir) throws Exception {
    LaunchImportRQ rq = new LaunchImportRQ();

    File xmlFile = createFile(tempDir);

    StartLaunchRS startLaunchRS = mock(StartLaunchRS.class);
    when(startLaunchRS.getId()).thenReturn(LAUNCH_ID);

    Launch launch = mock(Launch.class);

    XunitParseJob xunitParseJob = mock(XunitParseJob.class);
    when(xunitParseJob.withParameters(eq(projectDetails), eq(LAUNCH_ID), eq(user),
        any(InputStream.class), eq(false)
    )).thenReturn(xunitParseJob);
    ParseResults parseResults = mock(ParseResults.class);
    when(parseResults.getEndTime()).thenReturn(Date.from(Instant.EPOCH));
    when(xunitParseJob.call()).thenReturn(parseResults);

    when(startLaunchHandler.startLaunch(any(), any(), any())).thenReturn(startLaunchRS);
    when(launchRepository.findByUuid(any())).thenReturn(Optional.of(launch));
    when(xmlParseJobProvider.get()).thenReturn(xunitParseJob);

    xmlImportStrategy.importLaunch(projectDetails, user, xmlFile, BASE_URL, rq);

    verify(startLaunchHandler, times(1)).startLaunch(any(), any(), any());
    verify(finishLaunchHandler, times(1)).finishLaunch(any(), any(), any(), any(), any());
    verify(launchRepository, times(1)).findByUuid(any());
    verify(launchRepository, times(1)).save(any(Launch.class));
    verify(xmlParseJobProvider, times(1)).get();
  }

  @Test
  void whenImportLaunch_andIsSkippedIssue_thenProcessXmlFileWithSkippedTrue(@TempDir Path tempDir)
      throws Exception {
    LaunchImportRQ rq = new LaunchImportRQ();
    rq.setAttributes(Set.of(new ItemAttributesRQ(AbstractImportStrategy.SKIPPED_IS_NOT_ISSUE, "true")));

    File xmlFile = createFile(tempDir);

    StartLaunchRS startLaunchRS = mock(StartLaunchRS.class);
    when(startLaunchRS.getId()).thenReturn(LAUNCH_ID);

    Launch launch = mock(Launch.class);

    XunitParseJob xunitParseJob = mock(XunitParseJob.class);
    when(xunitParseJob.withParameters(eq(projectDetails), eq(LAUNCH_ID), eq(user),
        any(InputStream.class), eq(true)
    )).thenReturn(xunitParseJob);
    ParseResults parseResults = mock(ParseResults.class);
    when(parseResults.getEndTime()).thenReturn(Date.from(Instant.EPOCH));
    when(xunitParseJob.call()).thenReturn(parseResults);

    when(startLaunchHandler.startLaunch(any(), any(), any())).thenReturn(startLaunchRS);
    when(launchRepository.findByUuid(any())).thenReturn(Optional.of(launch));
    when(xmlParseJobProvider.get()).thenReturn(xunitParseJob);

    xmlImportStrategy.importLaunch(projectDetails, user, xmlFile, BASE_URL, rq);

    verify(startLaunchHandler, times(1)).startLaunch(any(), any(), any());
    verify(finishLaunchHandler, times(1)).finishLaunch(any(), any(), any(), any(), any());
    verify(launchRepository, times(1)).findByUuid(any());
    verify(launchRepository, times(1)).save(any(Launch.class));
    verify(xmlParseJobProvider, times(1)).get();
  }

  private File createFile(Path tempDir) throws Exception{
    File xmlFile = tempDir.resolve("sample.xml").toFile();
    BufferedWriter writer = new BufferedWriter(new FileWriter(xmlFile));
    writer.write("<testsuite><testcase/></testsuite>");
    writer.close();
    return xmlFile;
  }
}