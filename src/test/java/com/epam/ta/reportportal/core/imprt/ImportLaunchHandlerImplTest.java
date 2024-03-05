package com.epam.ta.reportportal.core.imprt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ImportFinishedEvent;
import com.epam.ta.reportportal.core.imprt.impl.ImportStrategyFactory;
import com.epam.ta.reportportal.core.imprt.impl.ImportType;
import com.epam.ta.reportportal.core.imprt.impl.XmlImportStrategy;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.model.LaunchImportCompletionRS;
import com.epam.ta.reportportal.model.launch.LaunchImportRQ;
import com.epam.ta.reportportal.util.sample.LaunchSampleUtil;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import java.io.File;
import java.util.Optional;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ImportLaunchHandlerImplTest {

  @InjectMocks
  private ImportLaunchHandlerImpl importLaunchHandlerImpl;

  @Mock
  private ImportStrategyFactory importStrategyFactory;

  @Mock
  private MessageBus messageBus;

  @Mock
  private LaunchRepository launchRepository;

  @Captor
  private ArgumentCaptor<ImportFinishedEvent> importFinishedEventCaptor;

  private final String FORMAT = "xunit";

  private static final String BASE_URL = "/v1/default_personal";

  private static final String INCORRECT_FILE_NAME = "file.jpg";

  private static final String FILE_NAME = "file.xml";

  private static final int MAX_FILE_SIZE = 32 * 1024 * 1024;

  private static final Long FILE_SIZE = 32 * 1024L;

  private static final String LAUNCH_ID = "94e83a8c-862c-4b72-95d0-f42665c90e7b";

  private static final Long ID = 1L;

  private static final String USER_NAME = "default";

  private ReportPortalUser.ProjectDetails projectDetails;

  private ReportPortalUser reportPortalUser;

  private MultipartFile multipartFile;

  @BeforeEach
  public void setUp() {
    projectDetails = mock(ReportPortalUser.ProjectDetails.class);
    lenient().when(projectDetails.getProjectId()).thenReturn(ID);
    reportPortalUser = mock(ReportPortalUser.class);
    lenient().when(reportPortalUser.getUserId()).thenReturn(ID);
    lenient().when(reportPortalUser.getUsername()).thenReturn(USER_NAME);
    multipartFile = mock(MultipartFile.class);
  }

  @Test
  public void whenImportLaunch_AndFileNameIsNotValid_ThenThrowException() {
    ReportPortalException reportPortalException = assertThrows(ReportPortalException.class,
        () -> importLaunchHandlerImpl.importLaunch(projectDetails, reportPortalUser, FORMAT,
            multipartFile, BASE_URL, new LaunchImportRQ()
        )
    );

    assertEquals(ErrorType.INCORRECT_REQUEST, reportPortalException.getErrorType());
    assertEquals(
        "Incorrect Request. File name should be not empty.", reportPortalException.getMessage());
  }

  @Test
  public void whenImportLaunch_AndFileExtensionIsNotValid_ThenThrowException() {
    when(multipartFile.getOriginalFilename()).thenReturn(INCORRECT_FILE_NAME);
    ReportPortalException reportPortalException = assertThrows(ReportPortalException.class,
        () -> importLaunchHandlerImpl.importLaunch(projectDetails, reportPortalUser, FORMAT,
            multipartFile, BASE_URL, new LaunchImportRQ()
        )
    );

    assertEquals(ErrorType.INCORRECT_REQUEST, reportPortalException.getErrorType());
    assertEquals(
        "Incorrect Request. Should be a zip archive or an xml file " + INCORRECT_FILE_NAME,
        reportPortalException.getMessage()
    );
  }

  @Test
  public void whenImportLaunch_AndFileSizeIsTooHigh_ThenThrowException() {
    when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
    when(multipartFile.getSize()).thenReturn(MAX_FILE_SIZE + 1L);
    ReportPortalException reportPortalException = assertThrows(ReportPortalException.class,
        () -> importLaunchHandlerImpl.importLaunch(projectDetails, reportPortalUser, FORMAT,
            multipartFile, BASE_URL, new LaunchImportRQ()
        )
    );

    assertEquals(ErrorType.INCORRECT_REQUEST, reportPortalException.getErrorType());
    assertEquals("Incorrect Request. File size is more than 32 Mb.",
        reportPortalException.getMessage()
    );
  }

  @Test
  public void whenImportLaunch_AndFileIsValid_ThenCallImportLaunch() {
    when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
    when(multipartFile.getSize()).thenReturn(FILE_SIZE);

    File tempFile = mock(File.class);
    try (MockedStatic<File> fileMockedStatic = Mockito.mockStatic(File.class)) {
      fileMockedStatic.when(
              () -> File.createTempFile(eq(FILE_NAME), eq("." + FilenameUtils.getExtension(FILE_NAME))))
          .thenReturn(tempFile);
      XmlImportStrategy xmlImportStrategy = mock(XmlImportStrategy.class);
      LaunchImportRQ rq = new LaunchImportRQ();
      when(xmlImportStrategy.importLaunch(projectDetails, reportPortalUser, tempFile, BASE_URL,
          rq
      )).thenReturn(LAUNCH_ID);
      when(importStrategyFactory.getImportStrategy(ImportType.XUNIT, FILE_NAME)).thenReturn(
          xmlImportStrategy);

      var sampleLaunch = LaunchSampleUtil.getSampleLaunch(LAUNCH_ID);
      when(launchRepository.findByUuid(LAUNCH_ID)).thenReturn(Optional.of(sampleLaunch));

      var response = (LaunchImportCompletionRS) importLaunchHandlerImpl.importLaunch(projectDetails,
          reportPortalUser, FORMAT, multipartFile, BASE_URL, rq
      );

      assertEquals(sampleLaunch.getUuid(), response.getData().getId());
      assertEquals(sampleLaunch.getName(), response.getData().getName());
      assertEquals(sampleLaunch.getNumber(), response.getData().getNumber());

      verify(importStrategyFactory).getImportStrategy(ImportType.XUNIT, FILE_NAME);
      verify(xmlImportStrategy).importLaunch(projectDetails, reportPortalUser, tempFile, BASE_URL,
          rq
      );
      verify(messageBus).publishActivity(importFinishedEventCaptor.capture());
      ImportFinishedEvent importFinishedEvent = importFinishedEventCaptor.getValue();
      assertEquals(ID, importFinishedEvent.getProjectId());
      assertEquals(USER_NAME, importFinishedEvent.getUserLogin());
      assertEquals(ID, importFinishedEvent.getUserId());
      assertEquals(FILE_NAME, importFinishedEvent.getFileName());

    }
  }
}
