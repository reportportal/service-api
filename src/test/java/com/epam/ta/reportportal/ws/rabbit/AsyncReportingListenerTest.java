package com.epam.ta.reportportal.ws.rabbit;

import static com.epam.ta.reportportal.ws.converter.converters.LogConverter.LOG_FULL_TO_LOG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.configs.rabbit.DeserializablePair;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.converter.builders.LogFullBuilder;
import com.epam.ta.reportportal.ws.reporting.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.reporting.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ.File;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AsyncReportingListenerTest {

  @InjectMocks
  private AsyncReportingListener asyncReportingListener;

  @Mock
  private StartLaunchHandler startLaunchHandler;

  @Mock
  private FinishLaunchHandler finishLaunchHandler;

  @Mock
  private StartTestItemHandler startTestItemHandler;

  @Mock
  private FinishTestItemHandler finishTestItemHandler;

  @Mock
  private DatabaseUserDetailsService userDetailsService;

  @Mock
  private LogRepository logRepository;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private TestItemRepository testItemRepository;

  @Mock
  private TestItemService testItemService;

  @Mock
  private AttachmentBinaryDataService attachmentBinaryDataService;

  @Mock
  private ProjectExtractor projectExtractor;

  @Mock
  private LogService logService;

  @Mock
  private MessageConverter messageConverter;

  @Mock
  private AmqpTemplate amqpTemplate;

  private static final String USERNAME = "rpuser";

  private static final String PROJECT_NAME = "test";

  private static final Long ID = 1L;
  private static final String LAUNCH_ID = "94e83a8c-862c-4b72-95d0-f42665c90e7b";

  private static final String BASE_URL = "/v1/default_personal";

  private static final String ITEM_ID = "c0237647-7f6d-4cb9-b885-72ee9fd7a24b";
  private MessageProperties messageProperties;
  private ReportPortalUser userDetails;
  private MembershipDetails membershipDetails;
  private Message message;

  @BeforeEach
  void setUp() {
    messageProperties = new MessageProperties();
    messageProperties.setHeader(MessageHeaders.USERNAME, USERNAME);
    messageProperties.setHeader(MessageHeaders.PROJECT_NAME, PROJECT_NAME);

    userDetails = mock(ReportPortalUser.class);
    projectDetails = mock(ReportPortalUser.ProjectDetails.class);

    lenient().when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
    lenient().when(projectExtractor.extractMemberShipDetails(userDetails, PROJECT_NAME))
        .thenReturn(projectDetails);

    byte[] messageBody = "message body".getBytes();
    message = new Message(messageBody, messageProperties);
  }

  @Test
  void whenMessageReceived_thenProcessStartLaunch() {
    messageProperties.setHeader(MessageHeaders.REQUEST_TYPE, RequestType.START_LAUNCH.name());

    StartLaunchRQ startLaunchRQ = mock(StartLaunchRQ.class);
    when(messageConverter.fromMessage(message)).thenReturn(startLaunchRQ);

    asyncReportingListener.onMessage(message);

    // Verify that the correct handler method is called
    verify(startLaunchHandler).startLaunch(userDetails, projectDetails, startLaunchRQ);
  }

  @Test
  void whenMessageReceived_thenProcessFinishLaunch() {
    messageProperties.setHeader(MessageHeaders.REQUEST_TYPE, RequestType.FINISH_LAUNCH.name());
    messageProperties.setHeader(MessageHeaders.LAUNCH_ID, LAUNCH_ID);
    messageProperties.setHeader(MessageHeaders.BASE_URL, BASE_URL);

    FinishExecutionRQ finishExecutionRQ = mock(FinishExecutionRQ.class);
    when(messageConverter.fromMessage(message)).thenReturn(finishExecutionRQ);

    asyncReportingListener.onMessage(message);

    // Verify that the correct handler method is called
    verify(finishLaunchHandler).finishLaunch(
        LAUNCH_ID, finishExecutionRQ, projectDetails, userDetails, BASE_URL);
  }

  @Test
  void whenMessageReceived_andParentItemIdIsNotNull_thenCallStartChildItem() {
    messageProperties.setHeader(MessageHeaders.REQUEST_TYPE, RequestType.START_TEST.name());
    messageProperties.setHeader(MessageHeaders.PARENT_ITEM_ID, ITEM_ID);

    StartTestItemRQ startTestItemRQ = mock(StartTestItemRQ.class);
    when(messageConverter.fromMessage(message)).thenReturn(startTestItemRQ);

    asyncReportingListener.onMessage(message);

    // Verify that the correct handler method is called
    verify(startTestItemHandler).startChildItem(
        userDetails, projectDetails, startTestItemRQ, ITEM_ID);
    verify(startTestItemHandler, never()).startRootItem(
        userDetails, projectDetails, startTestItemRQ);
  }

  @Test
  void whenMessageReceived_andParentItemIdIsNull_thenCallStartRootItem() {
    messageProperties.setHeader(MessageHeaders.REQUEST_TYPE, RequestType.START_TEST.name());

    StartTestItemRQ startTestItemRQ = mock(StartTestItemRQ.class);
    when(messageConverter.fromMessage(message)).thenReturn(startTestItemRQ);

    asyncReportingListener.onMessage(message);

    // Verify that the correct handler method is called
    verify(startTestItemHandler, never()).startChildItem(
        userDetails, projectDetails, startTestItemRQ, ITEM_ID);
    verify(startTestItemHandler).startRootItem(userDetails, projectDetails, startTestItemRQ);
  }

  @Test
  void whenMessageReceived_thenProcessFinishTest() {
    messageProperties.setHeader(MessageHeaders.REQUEST_TYPE, RequestType.FINISH_TEST.name());
    messageProperties.setHeader(MessageHeaders.LAUNCH_ID, LAUNCH_ID);
    messageProperties.setHeader(MessageHeaders.ITEM_ID, ITEM_ID);

    FinishTestItemRQ finishTestItemRQ = mock(FinishTestItemRQ.class);
    when(messageConverter.fromMessage(message)).thenReturn(finishTestItemRQ);

    asyncReportingListener.onMessage(message);

    // Verify that the correct handler method is called
    verify(finishTestItemHandler).finishTestItem(userDetails, projectDetails, ITEM_ID,
        finishTestItemRQ
    );
  }

  @Test
  void whenMessageReceived_andItemIsNotPresent_thenCreateLaunchLog() {
    messageProperties.setHeader(MessageHeaders.REQUEST_TYPE, RequestType.LOG.name());
    messageProperties.setHeader(MessageHeaders.PROJECT_ID, ID);

    SaveLogRQ saveLogRQ = mock(SaveLogRQ.class);
    when(saveLogRQ.getLaunchUuid()).thenReturn(LAUNCH_ID);
    File mockFile = new File();
    mockFile.setName("name");
    when(saveLogRQ.getFile()).thenReturn(mockFile);
    BinaryDataMetaInfo binaryDataMetaInfo = mock(BinaryDataMetaInfo.class);
    DeserializablePair<SaveLogRQ, BinaryDataMetaInfo> deserializablePair =
        DeserializablePair.of(saveLogRQ, binaryDataMetaInfo);

    Jackson2JsonMessageConverter jackson2JsonMessageConverter =
        spy(Jackson2JsonMessageConverter.class);

    ReflectionTestUtils.setField(
        asyncReportingListener, "messageConverter", jackson2JsonMessageConverter);

    doReturn(deserializablePair).when(jackson2JsonMessageConverter)
        .fromMessage(argThat(argument -> argument.equals(message)),
            any(ParameterizedTypeReference.class)
        );

    Launch launch = mock(Launch.class);
    when(launch.getId()).thenReturn(ID);
    when(launchRepository.findByUuid(LAUNCH_ID)).thenReturn(Optional.of(launch));

    LogFull logFull = new LogFullBuilder().addSaveLogRq(saveLogRQ).addLaunch(launch)
        .addProjectId(ID).get();
    final Log log = LOG_FULL_TO_LOG.apply(logFull);

    asyncReportingListener.onMessage(message);

    verify(logRepository).save(log);
    verify(logService).saveLogMessage(logFull, ID);
    verify(attachmentBinaryDataService).attachToLog(
        eq(binaryDataMetaInfo), any(AttachmentMetaInfo.class));
  }

  @Test
  void whenMessageReceived_andItemIsPresent_thenCreateItemLog() {
    messageProperties.setHeader(MessageHeaders.REQUEST_TYPE, RequestType.LOG.name());
    messageProperties.setHeader(MessageHeaders.PROJECT_ID, ID);

    SaveLogRQ saveLogRQ = mock(SaveLogRQ.class);
    when(saveLogRQ.getItemUuid()).thenReturn(ITEM_ID);
    File mockFile = new File();
    mockFile.setName("name");
    when(saveLogRQ.getFile()).thenReturn(mockFile);
    BinaryDataMetaInfo binaryDataMetaInfo = mock(BinaryDataMetaInfo.class);
    DeserializablePair<SaveLogRQ, BinaryDataMetaInfo> deserializablePair =
        DeserializablePair.of(saveLogRQ, binaryDataMetaInfo);

    Jackson2JsonMessageConverter jackson2JsonMessageConverter =
        spy(Jackson2JsonMessageConverter.class);

    ReflectionTestUtils.setField(
        asyncReportingListener, "messageConverter", jackson2JsonMessageConverter);

    doReturn(deserializablePair).when(jackson2JsonMessageConverter)
        .fromMessage(argThat(argument -> argument.equals(message)),
            any(ParameterizedTypeReference.class)
        );

    TestItem testItem = mock(TestItem.class);
    when(testItemRepository.findByUuid(ITEM_ID)).thenReturn(Optional.of(testItem));

    LogFull logFull = new LogFullBuilder().addSaveLogRq(saveLogRQ).addTestItem(testItem)
        .addProjectId(ID).get();
    final Log log = LOG_FULL_TO_LOG.apply(logFull);

    Launch launch = mock(Launch.class);
    when(launch.getId()).thenReturn(ID);
    when(testItemService.getEffectiveLaunch(testItem)).thenReturn(launch);

    asyncReportingListener.onMessage(message);

    verify(logRepository).save(log);
    verify(logService).saveLogMessage(logFull, ID);
    verify(attachmentBinaryDataService).attachToLog(
        eq(binaryDataMetaInfo), any(AttachmentMetaInfo.class));
  }

}
